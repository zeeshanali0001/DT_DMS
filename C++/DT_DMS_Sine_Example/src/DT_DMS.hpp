// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_DMS.hpp
/// \brief The general class of the DT-DMS framework: `DT = ⟨D, M, S, μ⟩`.

#ifndef DT_DMS_HPP
#define DT_DMS_HPP

#include <algorithm>
#include <cmath>
#include <fstream>
#include <iostream>
#include <memory>
#include <stdexcept>
#include <string>
#include <vector>

#include "Automaton/DT_Automaton.hpp"
#include "Data/DT_Data.hpp"
#include "Data/DT_DataSegment.hpp"
#include "Models/DT_Model.hpp"
#include "Models/DT_Model_Descriptive.hpp"
#include "Models/DT_Model_Predictive.hpp"
#include "Models/DT_Model_Prescriptive.hpp"
#include "Mu/DT_Mu.hpp"
#include "Services/DT_Service.hpp"
#include "Services/DT_Service_Control.hpp"
#include "Services/DT_Service_Diagnosis.hpp"
#include "Services/DT_Service_Monitoring.hpp"
#include "Services/DT_Service_Optimization.hpp"
#include "Services/DT_Service_Prediction.hpp"

/// \brief The `DT_DMS` class is the general class of the Digital Twin Data-Models-Services (DT-DMS) framework.
///
/// A digital twin is a real-time virtual clone of an object, a process, an environment, a scientific
/// instrument, a set of mechanisms or a complex system: the Twin of Interest (ToI). It relies on the dual pair
/// real/virtual and a real-time communication process with the physical entity it refers to. The class holds
/// the formal specification of a Digital Twin:
///
///     DT = ⟨ D, M, S, μ ⟩
///
/// Key features of the class include:
/// - `D`: the ToI data (Data::DT_Data), `D = D_hist ∪ D_rt`.
/// - `M`: the models of the ToI (Models::DT_Model), `M = M_desc ∪ M_pred ∪ M_presc`.
/// - `S`: the DT services (Services::DT_Service), `S = S_moni ∪ S_pred ∪ S_diag ∪ S_opti ∪ S_cont`, with
///   `s : M × Ω_D → Y_s`.
/// - `μ`: the model inference or data assimilation function (Mu::DT_Mu), `μ : Ω_D × M → M`.
/// - It connects the Data, Models and Services of the Digital Twin and manages the information flow between
///   them: `μ` observes every data source and updates every model, and each model notifies the services
///   coupled to it.
/// - The iterator design pattern over the data, the models and the services (`begin…()`, `current…()`,
///   `next…()`, `end…()`).
/// - Validity checks of the DT: services versus models, models versus data and data versus services
///   (checkConsistency()).
/// - The export of its own Data-Models-Services diagram in the Graphviz format (dumpGraphviz()).
///
/// The components are held by `std::shared_ptr`: data, models, services and `μ` are parts of the DT, which can
/// share them with other objects (e.g., a service shares its model).
/// \tparam T the type of a datum `d ∈ D`.
template <typename T>
class DT_DMS {

 public:
  /// \brief Constructs an empty Digital Twin `⟨D, M, S, μ⟩`.
  /// \param delta the freshness threshold `δ` (same time unit as the data measurement times).
  explicit DT_DMS(double delta) : DT_DMS("Digital Twin", delta) {}

  /// \brief Constructs an empty named Digital Twin `⟨D, M, S, μ⟩`.
  /// \param name  the name of the Digital Twin.
  /// \param delta the freshness threshold `δ` (same time unit as the data measurement times).
  DT_DMS(std::string name, double delta)
      : name(std::move(name)), delta(delta), clock(std::make_shared<Data::Clock>(Data::systemTimeInSeconds)) {
    mu = std::make_shared<Mu::DT_Mu<T>>(delta, sharedClock());
  }

  /// \brief Copy constructor: the new Digital Twin has the same data, models and services as `DT`.
  ///
  /// The lists are copied but the components themselves are shared, and so are `μ`, `δ` and the clock, so that
  /// every datum is still assimilated once into each model.
  /// \param DT the Digital Twin to copy.
  DT_DMS(const DT_DMS& DT) = default;

  /// Destructor
  virtual ~DT_DMS() = default;

  // ---------------------------------------------------------------- time

  /// \return the current time `t_now`.
  double tNow() const { return (*clock)(); }

  /// \brief Replaces the clock giving `t_now` (e.g., a simulated clock). It is shared with `μ` and the data.
  /// \param newClock the new clock.
  void setClock(Data::Clock newClock) { *clock = std::move(newClock); }

  /// \return the freshness threshold `δ`.
  double getDelta() const { return delta; }

  // ---------------------------------------------------------------- name

  /// \return the name of the Digital Twin.
  const std::string& getName() const { return name; }
  /// \param value the new name of the Digital Twin.
  void setName(const std::string& value) { name = value; }

  // ---------------------------------------------------------------- D

  /// \brief Adds a data source to `D`; it shares the clock of the DT and `μ` starts observing it.
  /// \param d the data source.
  void addData(const std::shared_ptr<Data::DT_Data<T>>& d) {
    if (std::find(dataList.begin(), dataList.end(), d) == dataList.end()) dataList.push_back(d);
    d->setClock(sharedClock());
    mu->observe(d);
  }

  /// \return `D_rt`: for each data source, its data measured at `t ≥ t_now − δ`.
  std::vector<Data::DT_DataSegment<T>> getRealTimeData() const {
    std::vector<Data::DT_DataSegment<T>> result;
    for (const auto& d : dataList) result.push_back(d->getRealTimeData(delta));
    return result;
  }

  /// \return `D_hist`: for each data source, its data measured at `t < t_now − δ`.
  std::vector<Data::DT_DataSegment<T>> getHistoricalData() const {
    std::vector<Data::DT_DataSegment<T>> result;
    for (const auto& d : dataList) result.push_back(d->getHistoricalData(delta));
    return result;
  }

  // ---------------------------------------------------------------- M

  /// \brief Adds a model to `M`; it will be updated by `μ`.
  /// \param m the model.
  /// \throw std::invalid_argument if `m` is not descriptive, predictive or prescriptive.
  void addModel(const std::shared_ptr<Models::DT_Model<T>>& m) {
    if (!std::dynamic_pointer_cast<Models::DT_Model_Descriptive<T>>(m) &&
        !std::dynamic_pointer_cast<Models::DT_Model_Predictive<T>>(m) &&
        !std::dynamic_pointer_cast<Models::DT_Model_Prescriptive<T>>(m))
      throw std::invalid_argument("A model must be descriptive, predictive or prescriptive: " +
                                  (m ? m->toString() : std::string("null")));
    if (std::find(modelList.begin(), modelList.end(), m) == modelList.end()) modelList.push_back(m);
    mu->addModel(m);
  }

  /// \return `M_desc`: the descriptive models.
  std::vector<std::shared_ptr<Models::DT_Model_Descriptive<T>>> getDescriptiveModels() const {
    return filter<Models::DT_Model_Descriptive<T>>(modelList);
  }

  /// \return `M_pred`: the predictive models.
  std::vector<std::shared_ptr<Models::DT_Model_Predictive<T>>> getPredictiveModels() const {
    return filter<Models::DT_Model_Predictive<T>>(modelList);
  }

  /// \return `M_presc`: the prescriptive models.
  std::vector<std::shared_ptr<Models::DT_Model_Prescriptive<T>>> getPrescriptiveModels() const {
    return filter<Models::DT_Model_Prescriptive<T>>(modelList);
  }

  // ---------------------------------------------------------------- S

  /// \brief Adds a service to `S` and couples it to the model `m`: the service will be notified each time `μ`
  /// updates `m` (Models::DT_Model::addServiceToBeUpdated).
  /// \param s the service.
  /// \param m the model the service is coupled to (added to `M` if needed), or `nullptr` for a purely
  ///          data-driven service.
  /// \throw std::invalid_argument if `s` is not one of the five service types.
  void addService(const std::shared_ptr<Services::DT_Service<T>>& s, const std::shared_ptr<Models::DT_Model<T>>& m) {
    if (!std::dynamic_pointer_cast<Services::DT_Service_Monitoring<T>>(s) &&
        !std::dynamic_pointer_cast<Services::DT_Service_Prediction<T>>(s) &&
        !std::dynamic_pointer_cast<Services::DT_Service_Diagnosis<T>>(s) &&
        !std::dynamic_pointer_cast<Services::DT_Service_Optimization<T>>(s) &&
        !std::dynamic_pointer_cast<Services::DT_Service_Control<T>>(s))
      throw std::invalid_argument("A service must be monitoring, prediction, diagnosis, optimization or control: " +
                                  (s ? s->toString() : std::string("null")));
    if (s->getModel() && s->getModel() != m) s->getModel()->removeServiceToBeUpdated(s);
    if (m) {
      addModel(m);
      m->addServiceToBeUpdated(s);
    }
    s->setModel(m);
    if (std::find(serviceList.begin(), serviceList.end(), s) == serviceList.end()) serviceList.push_back(s);
  }

  /// \brief Runs a service with the model it is coupled to: `s(m, ω) ∈ Y_s`.
  /// \param s     the service.
  /// \param omega the data segment it consumes (may be `nullptr`).
  /// \return the output of the service.
  typename Services::DT_Service<T>::Output runService(const std::shared_ptr<Services::DT_Service<T>>& s,
                                                      const Data::DT_DataSegment<T>* omega) {
    return s->execute(s->getModel(), omega);
  }

  /// \return `S_moni`: the monitoring services.
  std::vector<std::shared_ptr<Services::DT_Service_Monitoring<T>>> getMonitoringServices() const {
    return filter<Services::DT_Service_Monitoring<T>>(serviceList);
  }

  /// \return `S_pred`: the prediction services.
  std::vector<std::shared_ptr<Services::DT_Service_Prediction<T>>> getPredictionServices() const {
    return filter<Services::DT_Service_Prediction<T>>(serviceList);
  }

  /// \return `S_diag`: the diagnosis services.
  std::vector<std::shared_ptr<Services::DT_Service_Diagnosis<T>>> getDiagnosisServices() const {
    return filter<Services::DT_Service_Diagnosis<T>>(serviceList);
  }

  /// \return `S_opti`: the optimization services.
  std::vector<std::shared_ptr<Services::DT_Service_Optimization<T>>> getOptimizationServices() const {
    return filter<Services::DT_Service_Optimization<T>>(serviceList);
  }

  /// \return `S_cont`: the control services.
  std::vector<std::shared_ptr<Services::DT_Service_Control<T>>> getControlServices() const {
    return filter<Services::DT_Service_Control<T>>(serviceList);
  }

  // ---------------------------------------------------------------- μ

  /// \return the model inference or data assimilation function `μ`.
  Mu::DT_Mu<T>& getMu() { return *mu; }

  /// \brief Assimilates a data segment into the models of the DT, according to its kind at `t_now`
  /// (real-time, historical or mixed).
  /// \param omega the data segment `ω`.
  void assimilate(const Data::DT_DataSegment<T>& omega) { mu->assimilate(omega); }

  // ---------------------------------------------------------------- validity

  /// \brief Checks the consistency of the services with respect to the models they are coupled to.
  /// - A coupled model must belong to `M`.
  /// - A prediction service coupled to a model needs a predictive model.
  /// - An optimization or a control service needs a prescriptive model (it cannot run without it).
  /// - Monitoring and diagnosis services can use any model, or none (data-driven services).
  /// \param out where the report is written.
  /// \return `true` if every service is consistent with its model.
  bool checkServicesVersusModels(std::ostream& out) const {
    bool ok = true;
    out << "Services versus models:\n";
    for (const auto& s : serviceList) {
      const auto& m = s->getModel();
      std::string problem;
      bool needsPrescriptive = std::dynamic_pointer_cast<Services::DT_Service_Optimization<T>>(s) ||
                               std::dynamic_pointer_cast<Services::DT_Service_Control<T>>(s);
      if (m && std::find(modelList.begin(), modelList.end(), m) == modelList.end())
        problem = "its model " + m->getModelName() + " is not in M";
      else if (needsPrescriptive && !std::dynamic_pointer_cast<Models::DT_Model_Prescriptive<T>>(m))
        problem = "it needs a prescriptive model";
      else if (std::dynamic_pointer_cast<Services::DT_Service_Prediction<T>>(s) && m &&
               !std::dynamic_pointer_cast<Models::DT_Model_Predictive<T>>(m))
        problem = "it is coupled to a model that is not predictive";
      std::string model = m ? m->getModelName() : "no model (data-driven)";
      if (problem.empty()) {
        out << "  OK    " << s->getServiceName() << " → " << model << "\n";
      } else {
        out << "  ERROR " << s->getServiceName() << " → " << model << ": " << problem << "\n";
        ok = false;
      }
    }
    return ok;
  }

  /// \brief Checks the fitting of the data versus the models: for every data source `D_i` and every model `M_j`
  /// that gives values in time (Models::DT_Model::evaluate), computes `δ = |ω(t) − m(t)|` over the real-time
  /// data of the source.
  /// - If `δ < ε` for every datum, the model fits the data very well.
  /// - If the mean of `δ` is greater than `bigM`, the model does not fit the data well and should be updated,
  ///   re-parameterized or discarded.
  /// - Otherwise the fitting is acceptable.
  ///
  /// Models that give no value in time are skipped.
  /// \param out     where the report is written.
  /// \param epsilon the tolerance `ε` under which a datum is fitted very well.
  /// \param bigM    the mean deviation `M` over which the model does not fit the data.
  /// \return `false` if some model does not fit some data source.
  bool checkModelsVersusData(std::ostream& out, double epsilon = 1e-3, double bigM = 0.1) const {
    using Automaton::formatDouble;
    bool ok = true;
    out << "Models versus data (ε=" << formatDouble(epsilon) << ", M=" << formatDouble(bigM) << "):\n";
    for (const auto& m : modelList) {
      for (const auto& d : dataList) {
        double max = 0.0;
        double sum = 0.0;
        int count = 0;
        for (const auto& value : d->getRealTimeData(delta).getValues()) {
          double measured = Automaton::numericValue(value.second);
          double expected = m->evaluate(value.first);
          if (!std::isnan(measured) && !std::isnan(expected)) {
            double deviation = std::fabs(measured - expected);
            max = std::max(max, deviation);
            sum += deviation;
            count++;
          }
        }
        std::string pair = m->getModelName() + " / " + d->getSource();
        if (count == 0) {
          out << "  SKIP  " << pair << ": the model gives no value at the real-time data\n";
        } else if (max < epsilon) {
          out << "  OK    " << pair << ": fits very well (max δ=" << formatDouble(max) << ")\n";
        } else if (sum / count > bigM) {
          out << "  ERROR " << pair << ": does not fit (mean δ=" << formatDouble(sum / count)
              << "): update, re-parameterize or discard the model\n";
          ok = false;
        } else {
          out << "  OK    " << pair << ": acceptable (mean δ=" << formatDouble(sum / count)
              << ", max δ=" << formatDouble(max) << ")\n";
        }
      }
    }
    return ok;
  }

  /// \brief Checks the data characteristics (formats, units, ...) with respect to the services of the DT.
  /// - Services need data: `S ≠ ∅ ⇒ D ≠ ∅`.
  /// - Every data source must be valid (Data::DT_Data::validateData).
  /// - Warnings (not errors): a data source without unit, and a monitoring service whose monitored entity is
  ///   neither the source nor the type of a data source.
  /// \param out where the report is written.
  /// \return `true` if the data can be consumed by the services.
  bool checkDataVersusServices(std::ostream& out) const {
    bool ok = true;
    out << "Data versus services:\n";
    if (!serviceList.empty() && dataList.empty()) {
      out << "  ERROR the DT has services but no data\n";
      ok = false;
    }
    for (const auto& d : dataList) {
      if (!d->validateData()) {
        out << "  ERROR " << d->toString() << ": source or data type missing\n";
        ok = false;
      } else if (d->getCharacterization().getUnit().empty()) {
        out << "  WARN  " << d->getSource() << ": no unit of measurement\n";
      } else {
        out << "  OK    " << d->getSource() << " (" << d->getDataType() << ", " << d->getCharacterization().getUnit()
            << ")\n";
      }
    }
    for (const auto& s : getMonitoringServices()) {
      bool found = false;
      for (const auto& d : dataList)
        found = found || s->getMonitoredEntity() == d->getSource() || s->getMonitoredEntity() == d->getDataType();
      if (!found)
        out << "  WARN  " << s->getServiceName() << ": no data source for the monitored entity "
            << s->getMonitoredEntity() << "\n";
    }
    return ok;
  }

  /// \brief Runs the three validity checks of the DT: services versus models, models versus data (with the
  /// default tolerances) and data versus services.
  /// \param out where the report is written.
  /// \return `true` if all the checks succeed.
  bool checkConsistency(std::ostream& out) const {
    bool servicesModels = checkServicesVersusModels(out);
    bool modelsData = checkModelsVersusData(out);
    bool dataServices = checkDataVersusServices(out);
    return servicesModels && modelsData && dataServices;
  }

  // ---------------------------------------------------------------- Graphviz

  /// \brief Returns the Data-Models-Services diagram of the DT in the Graphviz format: one node lists the data
  /// sources, one the models and one the services; `μ` links the data to the models.
  /// Render it with `dot -Tpng file.dot -o file.png`.
  /// \return the diagram, in the dot language.
  std::string toGraphviz() const {
    std::vector<std::string> data, models, services;
    for (const auto& d : dataList) data.push_back(d->getSource() + " (" + d->getDataType() + ")");
    for (const auto& m : modelList) models.push_back(m->getModelName());
    for (const auto& s : serviceList) services.push_back(s->getServiceName());

    std::string dot = "digraph \"DMS Framework for the Digital Twin " + escape(name) + "\" {\n";
    dot += "nodesep=0.3;\n";
    dot += "center=true;\n";
    dot += "rankdir=LR;\n";
    dot += "node [shape=box, style=rounded, fontname=\"Arial\"];\n";
    dot += block("a", "DATA", data, "red");
    dot += block("b", "MODELS", models, "darkgreen");
    dot += block("c", "SERVICES", services, "blue");
    dot += "a -> b [label=<&#956;>, dir=both];\n";
    dot += "c -> b [dir=both];\n";
    dot += "a -> c [dir=both];\n";
    dot += "}\n";
    return dot;
  }

  /// \brief Writes the Data-Models-Services diagram of the DT in the Graphviz format (toGraphviz()).
  /// \param out where the diagram is written.
  void dumpGraphviz(std::ostream& out) const { out << toGraphviz(); }

  /// \brief Writes the Data-Models-Services diagram of the DT in a Graphviz file (toGraphviz()).
  /// \param fileName the name of the `.dot` file.
  /// \throw std::runtime_error if the file cannot be written.
  void dumpGraphviz(const std::string& fileName) const {
    std::ofstream file(fileName, std::ios::binary);
    if (!file) throw std::runtime_error("Cannot write " + fileName);
    file << toGraphviz();
  }

  // ---------------------------------------------------------------- Iterators

  /// For the design pattern iterator: initialize the list of models to run.
  void beginModels() { modelIterator = 0; }
  /// \brief For the design pattern iterator: return the current model.
  ///
  ///     for (dt.beginModels(); !dt.endModels(); dt.nextModel())
  ///       std::cout << dt.currentModel()->getModelName();
  /// \return the current model.
  const std::shared_ptr<Models::DT_Model<T>>& currentModel() const { return modelList.at(modelIterator); }
  /// For the design pattern iterator: move to the next model.
  void nextModel() { modelIterator++; }
  /// \return `true` if no other model exists (design pattern iterator).
  bool endModels() const { return modelIterator >= modelList.size(); }
  /// \return the number of models.
  std::size_t getNumberOfModels() const { return modelList.size(); }
  /// \return the position of the iterator over the models.
  std::size_t getModelsIteratorPosition() const { return modelIterator; }

  /// For the design pattern iterator: initialize the list of services to run.
  void beginServices() { serviceIterator = 0; }
  /// \brief For the design pattern iterator: return the current service.
  ///
  ///     for (dt.beginServices(); !dt.endServices(); dt.nextService())
  ///       std::cout << dt.currentService()->getServiceName();
  /// \return the current service.
  const std::shared_ptr<Services::DT_Service<T>>& currentService() const { return serviceList.at(serviceIterator); }
  /// For the design pattern iterator: move to the next service.
  void nextService() { serviceIterator++; }
  /// \return `true` if no other service exists (design pattern iterator).
  bool endServices() const { return serviceIterator >= serviceList.size(); }
  /// \return the number of services.
  std::size_t getNumberOfServices() const { return serviceList.size(); }
  /// \return the position of the iterator over the services.
  std::size_t getServicesIteratorPosition() const { return serviceIterator; }

  /// For the design pattern iterator: initialize the list of data to run.
  void beginData() { dataIterator = 0; }
  /// \brief For the design pattern iterator: return the current data source.
  ///
  ///     for (dt.beginData(); !dt.endData(); dt.nextData())
  ///       std::cout << dt.currentData()->getSource();
  /// \return the current data source.
  const std::shared_ptr<Data::DT_Data<T>>& currentData() const { return dataList.at(dataIterator); }
  /// For the design pattern iterator: move to the next data source.
  void nextData() { dataIterator++; }
  /// \return `true` if no other data source exists (design pattern iterator).
  bool endData() const { return dataIterator >= dataList.size(); }
  /// \return the number of data sources.
  std::size_t getNumberOfData() const { return dataList.size(); }
  /// \return the position of the iterator over the data.
  std::size_t getDataIteratorPosition() const { return dataIterator; }

  /// \brief Provides a summary of the Digital Twin, useful for debugging or logging.
  /// \return the name, the sizes of `D`, `M` and `S` and the value of `δ`.
  virtual std::string toString() const {
    return "DT '" + name + "' = ⟨D, M, S, μ⟩ with |D|=" + std::to_string(dataList.size()) +
           ", |M|=" + std::to_string(modelList.size()) + " (desc=" + std::to_string(getDescriptiveModels().size()) +
           ", pred=" + std::to_string(getPredictiveModels().size()) +
           ", presc=" + std::to_string(getPrescriptiveModels().size()) + "), |S|=" + std::to_string(serviceList.size()) +
           ", δ=" + Automaton::formatDouble(delta);
  }

 protected:
  /// The name of the Digital Twin.
  std::string name;
  /// `D`: the data sources of the ToI.
  std::vector<std::shared_ptr<Data::DT_Data<T>>> dataList;
  /// `M`: the models of the ToI.
  std::vector<std::shared_ptr<Models::DT_Model<T>>> modelList;
  /// `S`: the DT services.
  std::vector<std::shared_ptr<Services::DT_Service<T>>> serviceList;
  /// `μ`: the model inference or data assimilation function.
  std::shared_ptr<Mu::DT_Mu<T>> mu;
  /// The freshness threshold `δ ∈ ℝ+`: the data scope during which data is considered real-time.
  double delta;
  /// Gives the current time `t_now ∈ T_U` (default: system time in seconds); shared with `μ` and the data.
  std::shared_ptr<Data::Clock> clock;

 private:
  /// \return a clock that always reads the current clock of the DT, even after setClock().
  Data::Clock sharedClock() const {
    std::shared_ptr<Data::Clock> holder = clock;
    return [holder]() { return (*holder)(); };
  }

  /// \return the elements of `list` that are of type `U`.
  template <typename U, typename V>
  static std::vector<std::shared_ptr<U>> filter(const std::vector<std::shared_ptr<V>>& list) {
    std::vector<std::shared_ptr<U>> result;
    for (const auto& element : list)
      if (auto typed = std::dynamic_pointer_cast<U>(element)) result.push_back(typed);
    return result;
  }

  /// One node of the diagram: a title and one line per element ("(none)" if there is none).
  static std::string block(const std::string& id, const std::string& title, const std::vector<std::string>& lines,
                           const std::string& color) {
    std::string label;
    for (std::size_t i = 0; i < lines.size(); ++i) label += (i > 0 ? " \\n " : "") + escape(lines[i]);
    if (lines.empty()) label = "(none)";
    return id + " [label=\" " + title + " \\n " + label + " \", color=\"" + color + "\", fontcolor=\"" + color + "\"];\n";
  }

  /// Escapes the backslashes and double quotes of a name for a Graphviz label.
  static std::string escape(const std::string& s) {
    std::string result;
    for (char c : s) {
      if (c == '\\' || c == '"') result += '\\';
      result += c;
    }
    return result;
  }

  /// Position of the iterator over the data.
  std::size_t dataIterator = 0;
  /// Position of the iterator over the models.
  std::size_t modelIterator = 0;
  /// Position of the iterator over the services.
  std::size_t serviceIterator = 0;
};

#endif  // DT_DMS_HPP
