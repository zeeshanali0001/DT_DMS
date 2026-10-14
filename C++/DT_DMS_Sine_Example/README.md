# DT-DMS Sine Example (C++)

A minimal "Hello World" Digital Twin built with the C++ version of the DT-DMS framework. The Twin of Interest
(ToI) produces a sine wave; the Digital Twin follows it with a Cubic B-Spline model and diagnoses its
anomalies. It is the C++ version of the Java example (`Java/DT_DMS_Sine_Example`) and gives the same
results.

    DT = ⟨ D, M, S, μ ⟩

| Symbol | In this example | Class |
|--------|-----------------|-------|
| D | the sine values measured on the ToI, one every 0.05 time unit | `Data::DT_Data_SineValues` |
| M | one descriptive model: a uniform cubic B-spline of the measured values | `Models::DT_Model_Descr_CubicBSpline` |
| S | one diagnosis service, coupled to the model | `Services::DT_Service_Diagnosis_Sine` |
| μ | the data assimilation function of the framework | `Mu::DT_Mu` |

The main class is `DT_DMS_SineCubicBP` (`main` is in `DT_DMS_SineCubicBP.cpp`). The other classes (`DT_DMS`,
`DT_Automaton`, `DT_Data`, `DT_Model`, `DT_Service`, `DT_Mu`, ...) are the DT-DMS framework, copied unchanged
from `DT_DMS`, so that this project builds on its own.

## Information flow

For each sample, at the current time t_now:

1. `DT_Data_SineValues::measure(t_now)` produces the datum d = ω(t_now) = sin(t_now).
2. μ observes the data source. The datum is real-time (t ≥ t_now − δ, with δ = 1), so it is assimilated
   synchronously: μ(ω, m) = m', the value becomes the next control point of the B-spline.
3. The diagnosis service is run on the datum, s(m, ω): it compares d with the value expected by the model
   and reports an anomaly when the residual exceeds the threshold (0.05).
4. The chart records the raw value and the model value.

A spike of +0.5 is injected on the ToI at t = 3.0. It is detected at t = 3.0 and again at t = 3.05, because
the spike also disturbs the spline at the next sample.

At the end of the run:

- the interpolation is checked at 10 random points between the samples against the exact sine function
  (`DT_Service_Diagnosis_Sine::performRandomCheck`, the check of the first C++ version);
- the validity of the Digital Twin is checked (`DT_DMS::checkConsistency`): services versus models, models
  versus data (|ω(t) − m(t)| over the real-time data) and data versus services;
- the Data-Models-Services diagram of the Digital Twin is written in `docs/DT_DMS_Sine.dot`;
- the chart is written in `docs/DT_DMS_Sine_Chart.svg` (open it in a web browser) and its data in
  `docs/DT_DMS_Sine_Chart.csv`. (The Java version shows a live window with XChart; the C++ version needs no
  graphics library.) These files are written again at each run and are not kept in the repository.

## The model as an automaton ℳ = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩

| Element | Cubic B-Spline model |
|---------|----------------------|
| Λ | step, leftEndpoint |
| X | data: a measured sine value |
| Y | updated: the number of control points |
| Σ | count, last |
| Φ | WAIT ⟨count ≥ 0, NIL, ∞⟩, UPDATE ⟨count ≥ 1, NIL, 0⟩ |
| Δ^in | WAIT → UPDATE on (data, d); Update: count := count + 1, last := d |
| Δ^time | UPDATE → WAIT; Send: (updated, count) |
| Δ* | ⟨WAIT, NIL, 0⟩ |

## The service as an automaton 𝒮 = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩

| Element | Sine diagnosis service |
|---------|------------------------|
| Λ | threshold |
| X | data: a measured sine value |
| Y | anomaly: the residual of an anomalous value |
| Σ | residual |
| Φ | IDLE ⟨residual ≥ 0, NIL, ∞⟩, CHECK ⟨residual ≥ 0, NIL, 0⟩ |
| Δ^in | IDLE → CHECK on (data, d); Update: residual := \|d − m(t)\| |
| Δ^time | CHECK → IDLE; Guard: residual > threshold; Send: (anomaly, residual) |
| Δ^time | CHECK → IDLE; Guard: residual ≤ threshold; no output |
| Δ* | ⟨IDLE, NIL, 0⟩ |

## Build, run and document

The example needs only a C++17 compiler and CMake 3.16 or later (no external library). On Windows with
Visual Studio 2019/2022 or the Build Tools, double-click `build.bat` or run it from a terminal; on Linux,
macOS or Git Bash with g++ or clang++, run `./build.sh`. Both build in Release and run the example. By hand:

    cmake -S . -B build -DCMAKE_BUILD_TYPE=Release
    cmake --build build --config Release
    build/bin/DT_DMS_SineCubicBP

Add the argument `nochart` to run without writing the chart. To generate the documentation (Doxygen and
Graphviz must be installed):

    doxygen Doxyfile                          (or: cmake --build build --target doc, or: build.bat doc)

The documentation is generated in `docs/html/index.html` (`docs/index.html` opens it). The diagrams of the
example (the same classes and automata as in the Java version) are:

- `docs/DT_DMS_Sine_Architecture` (PNG and SVG): the full architecture with all fields and methods, generated
  by Doxygen and Graphviz from `DT_DMS_SineCubicBP` (to regenerate it after `doxygen Doxyfile`, from the
  `docs` folder: `dot -Tpng -Gdpi=110 -Gbgcolor=white html/class_d_t___d_m_s___sine_cubic_b_p__coll__graph.dot
  -o DT_DMS_Sine_Architecture.png`);
- `docs/DT_DMS_Sine_Architecture_Simplified` (PNG and SVG): the classes by name only, drawn from
  `docs/DT_DMS_Sine_Architecture_Simplified.dot` (`sh make_overview.sh DT_DMS_Sine_Architecture_Simplified`);
- `docs/DT_DMS_Sine_HiLLS` (PNG and SVG): the two automata in the HiLLS graphical notation.

Authors: Vincent DELOS (first C++ version), Zeeshan ALI, Mamadou Kaba TRAORE. Version 2.0.
License: GNU General Public License v3 (see `License.txt`).
