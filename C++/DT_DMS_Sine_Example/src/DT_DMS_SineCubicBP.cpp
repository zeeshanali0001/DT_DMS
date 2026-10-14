// DT-DMS sine example (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_DMS_SineCubicBP.cpp
/// \brief Implementation and `main` of the Sine Wave Cubic B-Spline Digital Twin example.

#include "DT_DMS_SineCubicBP.hpp"

#include <cmath>
#include <cstring>
#include <filesystem>
#include <iostream>

#include "Config.h"
#include "DT_Console.hpp"

using Automaton::formatDouble;

namespace {
/// \return `docs/<name>` if the folder `docs` exists, else `<name>`.
std::string outputPath(const std::string& name) {
  return std::filesystem::is_directory("docs") ? "docs/" + name : name;
}
}  // namespace

DT_DMS_SineCubicBP::DT_DMS_SineCubicBP(bool showChart) : DT_DMS<double>("Sine", DELTA) {
  setClock([this]() { return now; });
  assimilation = mu;

  sineData = std::make_shared<Data::DT_Data_SineValues>("Sine values");
  sineData->getCharacterization().setUnit("dimensionless");
  sineData->getCharacterization().setSignalType("digital");
  sineData->getCharacterization().setDataFlowType("periodic");
  splineModel = std::make_shared<Models::DT_Model_Descr_CubicBSpline>(STEP, 0.0);
  diagnosis = std::make_shared<Services::DT_Service_Diagnosis_Sine>(ANOMALY_THRESHOLD);

  addData(sineData);                   // μ observes the data
  addService(diagnosis, splineModel);  // adds the model to M and couples the service to it

  if (showChart) chart = std::make_unique<Visualization::DT_ChartPanel>();
}

void DT_DMS_SineCubicBP::execute(int nbVal) {
  std::cout << "Sine_DMS::execute() begin" << std::endl;
  std::cout << toString() << std::endl;

  for (int index = 0; index < nbVal; ++index) {
    now = std::round(index * STEP * 1000.0) / 1000.0;  // t_now, rounded to 3 decimal places

    double sineValue = sineData->measure(now);  // D, then μ(ω, m) = m'
    Data::DT_DataSegment<double> omega(now, sineValue);
    std::string report = Services::DT_Service<double>::outputToString(runService(diagnosis, &omega));  // s(m, ω)

    if (index % 10 == 0 || report != "OK")
      std::cout << "Data index " << index << " (t=" << formatDouble(now) << "), sine value = " << formatDouble(sineValue)
                << ", model = " << formatDouble(splineModel->getSineModelValue(now)) << " → " << report << std::endl;
    if (chart) chart->updateChart(index, sineValue, splineModel->getSineModelValue(now));
  }

  std::cout << "\nSine_DMS::execute() end" << std::endl;
  splineModel->executeModel();
  std::cout << "Anomalies detected at t = " << diagnosis->anomalyTimesToString() << std::endl;
  std::cout << "D_rt   (t ≥ t_now − δ): " << sineData->getRealTimeData(DELTA).size() << " values" << std::endl;
  std::cout << "D_hist (t < t_now − δ): " << sineData->getHistoricalData(DELTA).size() << " values" << std::endl;
  std::cout << "Model automaton:   phase " << splineModel->getCurrentPhase() << ", state "
            << Automaton::mapToString(splineModel->getState()) << std::endl;
  std::cout << "Service automaton: phase " << diagnosis->getCurrentPhase() << ", state "
            << Automaton::mapToString(diagnosis->getState()) << std::endl;

  // check of the interpolation between the samples, against the exact sine function
  std::cout << "\nRandom check of the interpolation (10 points between the samples):" << std::endl;
  double maxDeviation = diagnosis->performRandomCheck(10, 42u, true);
  std::cout << "Largest deviation |sin(x) − m(x)| = " << formatDouble(maxDeviation) << std::endl;

  // validity of the DT
  std::cout << std::endl;
  bool consistent = checkConsistency(std::cout);
  std::cout << "Consistent: " << (consistent ? "true" : "false") << std::endl;
}

void DT_DMS_SineCubicBP::writeGraphviz() const {
  std::string dotFile = outputPath("DT_DMS_Sine.dot");
  try {
    dumpGraphviz(dotFile);
    std::cout << "Written " << dotFile << " (render it with: dot -Tpng " << dotFile << " -o DT_DMS_Sine.png)" << std::endl;
  } catch (const std::exception& e) {
    std::cout << "Cannot write " << dotFile << ": " << e.what() << std::endl;
  }
}

void DT_DMS_SineCubicBP::writeChart() const {
  if (!chart) return;
  std::string svgFile = outputPath("DT_DMS_Sine_Chart.svg");
  std::string csvFile = outputPath("DT_DMS_Sine_Chart.csv");
  try {
    chart->writeSvg(svgFile);
    chart->writeCsv(csvFile);
    std::cout << "Written the chart " << svgFile << " (open it in a web browser) and its data " << csvFile << std::endl;
  } catch (const std::exception& e) {
    std::cout << "Cannot write the chart: " << e.what() << std::endl;
  }
}

/// \brief Runs the example. Pass the argument `nochart` to run without the chart files.
/// \param argc the number of arguments.
/// \param argv optional: `nochart`.
/// \return 0.
int main(int argc, char* argv[]) {
  enableUtf8Console();
  std::cout << "DT-DMS sine example (C++ version " << DT_DMS_VERSION << ")" << std::endl;
  bool showChart = !(argc > 1 && (std::strcmp(argv[1], "nochart") == 0 || std::strcmp(argv[1], "NOCHART") == 0));

  DT_DMS_SineCubicBP dtSystem(showChart);
  dtSystem.sineData->injectAnomaly(3.0, 0.5);  // a spike on the ToI, to be found by the diagnosis
  dtSystem.execute(100);
  dtSystem.writeGraphviz();
  dtSystem.writeChart();
  return 0;
}
