# DT-DMS Framework (C++)

The Digital Twin Data-Models-Services (DT-DMS) framework implements the formal specification of a
Digital Twin (DT) of a Twin of Interest (ToI):

    DT = ⟨ D, M, S, μ ⟩

This is the C++17 version of the framework. It has the same classes, the same behaviour and the same
output as the Java version (`Java`); the Java packages are C++ namespaces.

| Symbol | Meaning | Namespace | Main class |
|--------|---------|-----------|------------|
| D | the ToI data, D = D_hist ∪ D_rt | `Data` | `DT_Data`, `DT_DataSegment`, `DT_DataCharacterization` |
| M | the models of the ToI, M = M_desc ∪ M_pred ∪ M_presc | `Models` | `DT_Model` and its 3 child classes |
| S | the DT services, S = S_moni ∪ S_pred ∪ S_diag ∪ S_opti ∪ S_cont | `Services` | `DT_Service` and its 5 child classes |
| μ | the model inference or data assimilation function, μ : Ω_D × M → M | `Mu` | `DT_Mu` |
| 𝒜 | the class of automata, M ⊆ 𝒜 and S ⊆ 𝒜, 7-tuple ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩ | `Automaton` | `DT_Automaton` |

The general class `DT_DMS` holds D, M, S and μ and offers the iterator design pattern over the data, the
models and the services, the validity checks of the DT and the export of its Data-Models-Services diagram
to Graphviz. `DT_DMS_Architecture` connects one component of each kind; its collaboration diagram is the
architecture image of the framework.

## Data (D)

A datum d = ω(t) is real-time iff t ≥ t_now − δ, and historical otherwise, where t is the measurement
time, t_now the current time and δ the data scope during which data is considered real-time. Each data
source has a characterization (`DT_DataCharacterization`): unit, operational status, signal type (analog or
digital), data flow type (event-triggered, continuous or periodic), sensor precision, sensor location,
calibration status and date of the last maintenance.

## Models (M) and Services (S)

Every model and every service is an automaton (discrete, continuous or hybrid) specified with HiLLS, based
on DEVS, by the 7-tuple ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩:

- Λ: the parameter set, X: the input set, Y: the output set, Σ: the state-variable set;
- Φ: the phase set, each phase being ⟨Cond, Action, Delay⟩;
- Δ: the phase-to-phase transitions: external Δ^in = ⟨In, Guard, Update⟩, internal
  Δ^time = ⟨Guard, Send, Update⟩ and confluent Δ^{in,time} = ⟨In, Guard, Send, Update⟩
  (`TransitionKind::In`, `Time` and `InTime`);
- Δ*: the initialization ⟨φ_0, Update_0, τ_0⟩.

A service is parameterized by a model and/or the data segments it consumes: s : M × Ω_D → Y_s. A service
coupled to a model (`DT_DMS::addService(s, m)`) is notified each time μ updates that model
(`DT_Service::updateWithModel`).

## Model inference (μ)

μ(ω, m) = m' is the model m updated with the segment ω; m' keeps the type of m. A measured segment
ω : T_U ⇀ D has no datum in the future (sup Dom(ω) ≤ t_now) and is, at t_now:

- real-time iff inf Dom(ω) ≥ t_now − δ ∧ sup Dom(ω) ≤ t_now: assimilated synchronously;
- historical iff sup Dom(ω) < t_now − δ: assimilated asynchronously;
- mixed iff inf Dom(ω) < t_now − δ ∧ t_now − δ ≤ sup Dom(ω) ≤ t_now: split at t_now − δ into
  ω = ω_hist ⊕ ω_rt and assimilated part by part, or assimilated as a whole like a real-time or like a
  historical segment (`DT_Mu::MixedPolicy`).

## Validity of the DT

`DT_DMS::checkConsistency` runs three checks and writes a report:

- `checkServicesVersusModels`: every service is coupled to a model of `M` of a suitable type (a prediction
  service to a predictive model, an optimization or control service to a prescriptive model);
- `checkModelsVersusData`: for every model that gives values in time (`DT_Model::evaluate`), the deviation
  δ = |ω(t) − m(t)| over the real-time data: the model fits very well if δ < ε for every datum, and does not
  fit (it should be updated, re-parameterized or discarded) if the mean deviation is greater than M;
- `checkDataVersusServices`: the data sources are valid and have a unit, and every monitored entity has a
  data source.

## Build, run and document

The framework needs only a C++17 compiler and CMake 3.16 or later (no external library). On Windows with
Visual Studio 2019/2022 or the Build Tools, double-click `build.bat` or run it from a terminal; on Linux,
macOS or Git Bash with g++ or clang++, run `./build.sh`. Both build in Release and run the example. By
hand:

    cmake -S . -B build -DCMAKE_BUILD_TYPE=Release
    cmake --build build --config Release
    build/bin/DT_DMS_Architecture

The build produces the static library `build/lib/dtdms` and the executable
`build/bin/DT_DMS_Architecture` (`.exe` on Windows). The example writes the Data-Models-Services diagram of
its Digital Twin in `docs/DT_DMS_Temperature.dot` (`dot -Tpng docs/DT_DMS_Temperature.dot -o
docs/DT_DMS_Temperature.png`).

To generate the documentation (Doxygen and Graphviz must be installed):

    doxygen Doxyfile                          (or: cmake --build build --target doc, or: build.bat doc)

The documentation is generated in `docs/html/index.html` (`docs/index.html` opens it). The architecture image
is the collaboration diagram of `DT_DMS_Architecture`; a copy is kept in `docs/DT_DMS_Architecture.svg` and
`docs/DT_DMS_Architecture.png`.

## Use the framework in your own project

Copy the `src` folder and `CMakeLists.txt`, or add this folder with `add_subdirectory` and link your
executable with the `dtdms` library. Derive your data, models and services from the framework classes (see
`DT_DMS_Sine_Example` for a complete example), and add them to a `DT_DMS<T>`:

    auto dt = std::make_shared<DT_DMS<double>>("My twin", 1.0);   // δ = 1
    dt->addData(myData);                                          // μ observes the data
    dt->addService(myService, myModel);                           // adds the model and couples the service
    dt->checkConsistency(std::cout);

Data, models and services are held by `std::shared_ptr`.

Authors: Vincent DELOS (first C++ version), ALI Zeeshan, Mamadou Kaba TRAORÉ. Version 2.0.
License: GNU General Public License v3 (see `License.txt`).
