# DT-DMS Framework

The Digital Twin Data-Models-Services (DT-DMS) framework implements the formal specification of a
Digital Twin (DT) of a Thing of Interest (ToI):

    DT = ⟨ D, M, S, μ ⟩

| Symbol | Meaning | Package | Main class |
|--------|---------|---------|------------|
| D | the ToI data, D = D_hist ∪ D_rt | `Data` | `DT_Data`, `DT_DataSegment` |
| M | the models of the ToI, M = M_desc ∪ M_pred ∪ M_presc | `Models` | `DT_Model` and its 3 child classes |
| S | the DT services, S = S_moni ∪ S_pred ∪ S_diag ∪ S_opti ∪ S_cont | `Services` | `DT_Service` and its 5 child classes |
| μ | the model inference or data assimilation function, μ : Ω_D × M → M | `Mu` | `DT_Mu` |
| 𝒜 | the class of automata, M ⊆ 𝒜 and S ⊆ 𝒜, 7-tuple ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩ | `Automaton` | `DT_Automaton` |

The general class `DT_DMS` holds D, M, S and μ and offers the iterator design pattern over the data, the
models and the services. `DT_DMS_Architecture` connects one component of each kind; its collaboration
diagram is the architecture image of the framework.

## Data (D)

A datum d = ω(t) is real-time iff t ≥ t_now − δ, and historical otherwise, where t is the measurement
time, t_now the current time and δ the data scope during which data is considered real-time.

## Models (M) and Services (S)

Every model and every service is an automaton (discrete, continuous or hybrid) specified with HiLLS, based
on DEVS, by the 7-tuple ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩:

- Λ: the parameter set, X: the input set, Y: the output set, Σ: the state-variable set;
- Φ: the phase set, each phase being ⟨Cond, Action, Delay⟩;
- Δ: the phase-to-phase transitions: external Δ^in = ⟨In, Guard, Update⟩, internal
  Δ^time = ⟨Guard, Send, Update⟩ and confluent Δ^{in,time} = ⟨In, Guard, Send, Update⟩;
- Δ*: the initialization ⟨φ_0, Update_0, τ_0⟩.

A service is parameterized by a model and/or the data segments it consumes: s : M × Ω_D → Y_s.

## Model inference (μ)

μ(ω, m) = m' is the model m updated with the segment ω; m' keeps the type of m. A measured segment
ω : T_U ⇀ D has no datum in the future (sup Dom(ω) ≤ t_now) and is, at t_now:

- real-time iff inf Dom(ω) ≥ t_now − δ ∧ sup Dom(ω) ≤ t_now: assimilated synchronously;
- historical iff sup Dom(ω) < t_now − δ: assimilated asynchronously;
- mixed iff inf Dom(ω) < t_now − δ ∧ t_now − δ ≤ sup Dom(ω) ≤ t_now: split at t_now − δ into
  ω = ω_hist ⊕ ω_rt and assimilated part by part, or assimilated as a whole like a real-time or like a
  historical segment (`DT_Mu.MixedPolicy`).

## Build, run and document

From this folder (Git Bash):

    javac -encoding UTF-8 -d out $(find src -name "*.java")
    java -cp out DT_DMS_Architecture
    doxygen Doxyfile

The documentation is generated in `docs/html/index.html`. The architecture image is the collaboration
diagram of `DT_DMS_Architecture`; a copy is kept in `docs/DT_DMS_Architecture.svg` and
`docs/DT_DMS_Architecture.png`.

Authors: ALI Zeeshan, Mamadou Kaba TRAORÉ. Version 2.0.
