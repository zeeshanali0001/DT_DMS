// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_DMS_Architecture.cpp
/// \brief Example run of the framework on a temperature Digital Twin. Replace it with your own example.

#include <filesystem>
#include <iostream>
#include <memory>

#include "Config.h"
#include "DT_Console.hpp"
#include "DT_DMS_Architecture.hpp"

using Automaton::DT_Automaton;
using Automaton::formatDouble;

/// \brief Example run of the framework on a temperature Digital Twin. Replace it with your own example.
/// \return 0.
int main() {
  enableUtf8Console();
  std::cout << "Welcome to DMS Framework (C++ version " << DT_DMS_VERSION << ")." << std::endl;

  // ---- DT = ⟨D, M, S, μ⟩ with a simulated clock: t_now = 100, δ = 5 → real-time window [95, 100]
  double now = 100.0;
  DT_DMS_Architecture<double> dt(5.0);
  dt.setName("Temperature");
  dt.setClock([&now]() { return now; });

  // D, with the characterization of its data
  auto data = std::make_shared<Data::DT_Data<double>>("Sensor A", "Temperature");
  data->getCharacterization().setUnit("Celsius");
  data->getCharacterization().setSignalType("digital");
  data->getCharacterization().setDataFlowType("periodic");
  data->getCharacterization().setSensorPrecision("±0.1 °C");
  data->getCharacterization().setSensorCalibrationStatus("calibrated");
  // M = M_desc ∪ M_pred ∪ M_presc
  auto descriptive = std::make_shared<Models::DT_Model_Descriptive<double>>("Temperature summary", "Sensor A");
  auto predictive = std::make_shared<Models::DT_Model_Predictive<double>>("Temperature forecast", "Moving average", 3);
  auto prescriptive = std::make_shared<Models::DT_Model_Prescriptive<double>>(
      "Temperature setpoint", "Keep temperature at 22", 22.0, "The heater reacts linearly");
  // S = S_moni ∪ S_pred ∪ S_diag ∪ S_opti ∪ S_cont
  auto monitoring = std::make_shared<Services::DT_Service_Monitoring<double>>("Temperature monitoring", "", "Sensor A",
                                                                              10.0, 30.0);

  dt.data = data;
  dt.descriptiveModel = descriptive;
  dt.predictiveModel = predictive;
  dt.prescriptiveModel = prescriptive;
  dt.monitoring = monitoring;
  dt.prediction = std::make_shared<Services::DT_Service_Prediction<double>>("Temperature prediction", "");
  dt.diagnosis = std::make_shared<Services::DT_Service_Diagnosis<double>>("Temperature diagnosis", "", 10.0, 30.0);
  dt.optimization = std::make_shared<Services::DT_Service_Optimization<double>>("Setpoint optimization", "", "Rule-based");
  dt.control = std::make_shared<Services::DT_Service_Control<double>>("Heater control", "", "Proportional", 0.5);
  dt.connect();
  std::cout << dt.toString() << std::endl;

  // ---- μ: real-time data is assimilated synchronously, historical data asynchronously
  std::cout << "\n-- Data assimilation (μ)" << std::endl;
  data->updateData(99.0, 21.5);  // 99 ≥ 100 − 5 → real-time, assimilated at once
  data->updateData(80.0, 19.0);  // 80 < 100 − 5 → historical, queued
  std::cout << "Assimilated by the predictive model: " << predictive->historyToString()
            << ", waiting: " << dt.getMu().getPendingCount() << std::endl;
  dt.getMu().flush();  // asynchronous assimilation of the queue
  std::cout << "After flush(): " << predictive->historyToString() << std::endl;

  // ---- a mixed segment ω = ω_hist ⊕ ω_rt is split at t_now − δ = 95
  Data::DT_DataSegment<double> omega;
  omega.put(90.0, 20.0);
  omega.put(96.0, 21.0);
  omega.put(98.0, 35.0);
  std::cout << "ω = " << omega.toString() << " is "
            << Data::DT_DataSegment<double>::kindName(omega.kind(dt.tNow(), dt.getDelta()))
            << ": ω_hist = " << omega.historicalPart(dt.tNow(), dt.getDelta()).toString()
            << ", ω_rt = " << omega.realTimePart(dt.tNow(), dt.getDelta()).toString() << std::endl;
  dt.assimilate(omega);  // policy SPLIT: ω_rt at once, ω_hist queued
  dt.getMu().flush();

  // ---- D = D_hist ∪ D_rt
  std::cout << "\n-- Data (D)" << std::endl;
  for (const auto& segment : dt.getRealTimeData()) std::cout << "D_rt   = [" << segment.toString() << "]" << std::endl;
  for (const auto& segment : dt.getHistoricalData()) std::cout << "D_hist = [" << segment.toString() << "]" << std::endl;

  // ---- iterator pattern over the models
  std::cout << "\n-- Models (M)" << std::endl;
  for (dt.beginModels(); !dt.endModels(); dt.nextModel()) dt.currentModel()->executeModel();

  // ---- services s : M × Ω_D → Y_s, run on the real-time segment ω_rt
  std::cout << "\n-- Services (S)" << std::endl;
  Data::DT_DataSegment<double> realTime = omega.realTimePart(dt.tNow(), dt.getDelta());
  for (dt.beginServices(); !dt.endServices(); dt.nextService())
    std::cout << dt.currentService()->getServiceName() << " → "
              << Services::DT_Service<double>::outputToString(dt.runService(dt.currentService(), &realTime))
              << std::endl;

  // ---- the monitoring service specified as an automaton 𝒮 = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩
  std::cout << "\n-- Automaton of the monitoring service" << std::endl;
  monitoring->addParameter("upperThreshold", 30.0);                                                     // Λ
  monitoring->addInput("temperature");                                                                  // X
  monitoring->addOutput("alert");                                                                       // Y
  monitoring->addStateVariable("value", 0.0);                                                           // Σ
  monitoring->addPhase(DT_Automaton::Phase("NORMAL", nullptr, nullptr, DT_Automaton::infinity()));     // Φ
  monitoring->addPhase(DT_Automaton::Phase("ALARM", nullptr, nullptr, 0.0));
  monitoring->addTransition(DT_Automaton::Transition(                                                  // Δ^in
      DT_Automaton::TransitionKind::In, "NORMAL", "ALARM", {"temperature"}, nullptr, nullptr,
      [](Automaton::State q, const DT_Automaton::Event* x) {
        q["value"] = x->value;
        return q;
      }));
  monitoring->addTransition(DT_Automaton::Transition(                                                  // Δ^time
      DT_Automaton::TransitionKind::Time, "ALARM", "NORMAL", {}, nullptr,
      [](const Automaton::State& q) { return std::vector<DT_Automaton::Event>{{"alert", q.at("value")}}; },
      nullptr));
  monitoring->setInitialization(DT_Automaton::Initialization("NORMAL", nullptr, 0.0));                // Δ*
  monitoring->initialize();
  std::cout << "Phase: " << monitoring->getCurrentPhase() << std::endl;
  monitoring->externalTransition(DT_Automaton::Event("temperature", 35.0), 1.0);
  std::cout << "Phase after input (temperature, 35.0): " << monitoring->getCurrentPhase() << ", state "
            << Automaton::mapToString(monitoring->getState()) << std::endl;
  std::vector<DT_Automaton::Event> outputs = monitoring->internalTransition();
  std::cout << "Output on internal transition: " << DT_Automaton::eventsToString(outputs)
            << ", phase: " << monitoring->getCurrentPhase() << std::endl;

  // ---- validity of the DT: services versus models, models versus data, data versus services
  std::cout << "\n-- Validity" << std::endl;
  bool consistent = dt.checkConsistency(std::cout);
  std::cout << "Consistent: " << (consistent ? "true" : "false") << std::endl;

  // ---- the DT draws its own Data-Models-Services diagram (Graphviz)
  std::cout << "\n-- Graphviz" << std::endl;
  std::string dotFile = std::filesystem::is_directory("docs") ? "docs/DT_DMS_Temperature.dot" : "DT_DMS_Temperature.dot";
  try {
    dt.dumpGraphviz(dotFile);
    std::cout << "Written " << dotFile << " (render it with: dot -Tpng " << dotFile << " -o DT_DMS_Temperature.png)"
              << std::endl;
  } catch (const std::exception& e) {
    std::cout << "Cannot write " << dotFile << ": " << e.what() << std::endl;
  }
  return 0;
}
