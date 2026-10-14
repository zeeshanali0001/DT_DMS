// DT-DMS sine example (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_DMS_SineCubicBP.hpp
/// \brief Main class of the Sine Wave Cubic B-Spline Digital Twin example.

#ifndef DT_DMS_SINE_CUBIC_BP_HPP
#define DT_DMS_SINE_CUBIC_BP_HPP

#include <memory>
#include <string>

#include "DT_DMS.hpp"
#include "Data/DT_Data_SineValues.hpp"
#include "Models/DT_Model_Descr_CubicBSpline.hpp"
#include "Services/DT_Service_Diagnosis_Sine.hpp"
#include "Visualization/DT_ChartPanel.hpp"

/// \brief Main class of the Sine Wave Cubic B-Spline Digital Twin example: a minimal "Hello World" Digital Twin
/// `DT = ⟨D, M, S, μ⟩` built with the DT-DMS framework.
/// - `D`: the sine values measured on the ToI (Data::DT_Data_SineValues).
/// - `M`: one descriptive model, the Cubic B-Spline (Models::DT_Model_Descr_CubicBSpline).
/// - `S`: one diagnosis service (Services::DT_Service_Diagnosis_Sine), coupled to the model.
/// - `μ`: the data assimilation function of the framework; it observes the data and updates the model.
///
/// Information flow for each sample: Data → μ → Model → Diagnosis, with charting and anomaly detection. The
/// model and the service are both specified as automata (see their classes).
class DT_DMS_SineCubicBP : public DT_DMS<double> {

 public:
  /// Step size between two samples.
  static constexpr double STEP = 0.05;
  /// Freshness threshold `δ`: data measured during the last time unit is real-time.
  static constexpr double DELTA = 1.0;
  /// Largest residual considered normal by the diagnosis service.
  static constexpr double ANOMALY_THRESHOLD = 0.05;

  /// \brief Builds the Digital Twin: creates the data, the model and the service and connects them.
  /// \param showChart `true` to record the chart (written by writeChart()).
  explicit DT_DMS_SineCubicBP(bool showChart);

  /// \brief Runs the simulation: measures `nbVal` sine values, one per step. For each value:
  /// 1. the data source measures the ToI at `t_now`;
  /// 2. `μ` assimilates the real-time datum synchronously into the model;
  /// 3. the diagnosis service is run on the datum, `s(m, ω)`;
  /// 4. the chart is updated.
  ///
  /// At the end, the interpolation is checked at random points and the validity of the DT is checked.
  /// \param nbVal the number of values to measure.
  void execute(int nbVal);

  /// \brief Writes the Data-Models-Services diagram of the Digital Twin (DT_DMS::dumpGraphviz) in the `docs`
  /// folder if it exists, else in the current folder.
  void writeGraphviz() const;

  /// Writes the chart as `DT_DMS_Sine_Chart.svg` and `.csv` (in `docs` if it exists), if it was recorded.
  void writeChart() const;

#ifdef DOXYGEN_RUNNING
  // Documentation view of the fields. In the code, each field is a smart pointer (e.g.,
  // std::shared_ptr<Data::DT_Data_SineValues>); Doxygen draws the relation to a class, with its parent classes,
  // only for this plain form.

  /// The sine data source (`D`); in the code: `std::shared_ptr<Data::DT_Data_SineValues>`.
  Data::DT_Data_SineValues sineData;
  /// The Cubic B-Spline model (`M`); in the code: `std::shared_ptr<Models::DT_Model_Descr_CubicBSpline>`.
  Models::DT_Model_Descr_CubicBSpline splineModel;
  /// The diagnosis service (`S`); in the code: `std::shared_ptr<Services::DT_Service_Diagnosis_Sine>`.
  Services::DT_Service_Diagnosis_Sine diagnosis;
  /// The data assimilation function (`μ`), the same object as getMu(); in the code: `std::shared_ptr<Mu::DT_Mu<double>>`.
  Mu::DT_Mu assimilation;
  /// The chart (empty if not recorded); in the code: `std::unique_ptr<Visualization::DT_ChartPanel>`.
  Visualization::DT_ChartPanel chart;
#else
  std::shared_ptr<Data::DT_Data_SineValues> sineData;
  std::shared_ptr<Models::DT_Model_Descr_CubicBSpline> splineModel;
  std::shared_ptr<Services::DT_Service_Diagnosis_Sine> diagnosis;
  std::shared_ptr<Mu::DT_Mu<double>> assimilation;
  std::unique_ptr<Visualization::DT_ChartPanel> chart;
#endif

 private:
  /// The simulated current time `t_now`.
  double now = 0.0;
};

#endif  // DT_DMS_SINE_CUBIC_BP_HPP
