# DT_DMS_Sine_Example (Python port)

A faithful Python replica of the Java `DT_DMS_Sine_Example` project
(https://github.com/zeeshanali0001/DT_DMS/tree/main/Java/DT_DMS_Sine_Example).

It reproduces the same **Digital Twin** architecture and Observer Pattern:

```
Data (Physical Twin)  --push_back()-->  notifies observers every 10 samples
        |
        +--> Model (Digital Model: Cubic B-Spline placeholder)
                |
                +--> Service (Diagnosis: anomaly detection + live chart)
```

## 1. Install dependencies

```bash
pip install -r requirements.txt
```

Only one third-party library is needed:

| Library      | Purpose                                                      | Java equivalent |
|--------------|---------------------------------------------------------------|------------------|
| `matplotlib` | Live line chart of raw sine data vs. model prediction          | XChart + Swing (`DT_ChartPanel`) |

Everything else (`math`, `random`, `abc`) is part of the Python standard library.

## 2. Run

From the project's root folder:

```bash
python DT_DMS.py
```

This mirrors running the Java `DT_DMS` class, whose `main()` method builds a
`DT_DMS_SineCubicBP` and calls `execute()`.

The program will:
1. Create a `DT_Models_Descr_CubicBSpline` model.
2. Create a `DT_Services_Diagnosis_Sine` diagnosis service.
3. Create a `DT_Data_SineValues` data source.
4. Register the model and service as observers of the data (Observer Pattern).
5. Push 100 sine samples; every 10 samples the observers are notified,
   the live chart is updated, and anomaly detection runs.
6. Print progress to the console and save a final chart snapshot to
   `dt_dms_chart.png`.

If a graphical display is available, a live-updating matplotlib window
("Digital Twin Live Chart") opens automatically, just like the Java Swing
window. If no display is available (e.g. a headless server), the program
detects this automatically and simply skips the live window — all logic
(observer notifications, anomaly detection, console output) still runs
normally, and a final chart image is written to disk.

## 3. Project structure

Directory / file layout mirrors the original Java package structure 1:1
(Java packages -> Python packages/folders):

```
DT_DMS.py                              # DT_DMS .java  (framework base + main())
DT_DMS_SineCubicBP.py                  # DT_DMS_SineCubicBP.java (orchestrator)
Data/
    DT_Data.py                         # Data/DT_Data.java
    DT_Data_SineValues.py              # Data/DT_Data_SineValues.java
DataObserver/
    ObserverData.py                    # DataObserver/ObserverData.java
    ObserverDataManager.py             # DataObserver/ObserverDataManager.java
Models/
    DT_Models.py                       # Models/DT_Models.java
    DT_Models_Descriptive.py           # Models/DT_Models_Descriptive.java
    DT_Models_Descr_CubicBSpline.py    # Models/DT_Models_Descr_CubicBSpline.java
Services/
    DT_Services.py                     # Services/DT_Services.java
    DT_Services_Diagnostic.py          # Services/DT_Services_Diagnostic.java
    DT_Services_Diagnosis_Sine.py      # Services/DT_Services_Diagnosis_Sine.java
Visualization/
    DT_ChartPanel.py                   # Visualization/DT_ChartPanel.java
requirements.txt
```

Every class and method keeps the same name as the Java original (converted
to Python's `snake_case` only for method calls that are conventionally
`camelCase` verbs, e.g. `registerObserver` -> `register_observer`,
`push_back` -> `push_back` unchanged, etc.) so you can cross-reference the
two codebases line by line.

## 4. Notable porting decisions

- **Interfaces -> Abstract Base Classes**: Java's `ObserverData<T>` and
  `ObserverDataManager<T>` interfaces became Python `ABC` classes with
  `@abstractmethod`s.
- **Generics dropped**: Python doesn't need `<T>` type parameters for this
  pattern to work; duck typing handles it.
- **Circular imports avoided**: `DT_DMS.py` and `DT_DMS_SineCubicBP.py`
  reference each other (base class / subclass) just like in Java, where the
  compiler resolves this automatically. Python cannot import both modules
  at top level in a cycle, so `DT_DMS.main()` imports
  `DT_DMS_SineCubicBP` lazily, inside the function body.
- **XChart/Swing -> matplotlib**: `DT_ChartPanel` now uses matplotlib in
  interactive mode instead of XChart, with an automatic headless fallback so
  the program always runs without errors.
