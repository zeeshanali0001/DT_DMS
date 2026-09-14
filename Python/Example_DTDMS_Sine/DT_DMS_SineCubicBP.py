"""
DT_DMS_SineCubicBP.py

Main orchestrator class for the Sine Wave Cubic B-Spline Digital Twin example.
This class sets up the entire Digital Twin system:
    - Creates the Data source (Physical Twin)
    - Creates the Model (Digital Model)
    - Creates the Diagnosis Service
    - Links them using the Observer Pattern
    - Runs the simulation by pushing sine values

This is a minimal "Hello World" Digital Twin that demonstrates:
Data -> Model -> Diagnosis with live charting and anomaly detection.

This is the Python equivalent of src/DT_DMS_SineCubicBP.java.

@author Zeeshan ALI, Mamadou Kaba TRAORE
@version 1.0 (Python port)
"""

import math

from DT_DMS import DT_DMS
from Data.DT_Data_SineValues import DT_Data_SineValues
from Models.DT_Models_Descr_CubicBSpline import DT_Models_Descr_CubicBSpline
from Services.DT_Services_Diagnosis_Sine import DT_Services_Diagnosis_Sine


class DT_DMS_SineCubicBP(DT_DMS):

    def __init__(self):
        """Default constructor."""
        super().__init__()

        # ==================== Fields kept for documentation parity ====================
        # These mirror the (unused/reference-only) commented fields in the
        # original Java class.

        # Reference to the Cubic B-Spline model
        self.cBSModel = None

        # Reference to the sine data source
        self.cBS = None

        # Reference to the diagnosis service
        self.sineDiagnosisService = None

    def execute(self):
        """
        Orchestrates the complete setup and execution of the Digital Twin system.

        Execution flow:
            1. Create Model (Cubic B-Spline)
            2. Create Diagnosis Service
            3. Create Data source (Sine Values)
            4. Register Model and Service as observers to the Data (Observer Pattern)
            5. Link Service with Data for chart updates and diagnosis
            6. Simulate incoming data by calling push_back() in a loop
        """
        print("Sine_DMS::execute() begin")

        # Step 1: Create the Digital Model (Cubic B-Spline)
        step = 0.05
        cBSModel = DT_Models_Descr_CubicBSpline(step)
        self.addModel(cBSModel)

        # Step 2: Create the Diagnosis Service
        svService = DT_Services_Diagnosis_Sine(cBSModel)
        self.addService(svService)

        # Step 3: Create the Data Source (Physical Twin)
        svData = DT_Data_SineValues("Sine values")
        self.addData(svData)

        # Step 4: Link using Observer Pattern
        # Both Model and Service will be automatically notified when data changes
        svData.register_observer(cBSModel)
        svData.register_observer(svService)

        # Step 5: Link the Service with the Data source
        # This is REQUIRED for chart updates and accessing raw values in diagnosis
        svService.set_latest_data(svData)

        # Step 6: Connect model and service directly (kept from the original design)
        cBSModel.add_service_2_be_updated(svService)

        print("Starting simulation - pushing 100 sine values...\n")

        nbVal = 100
        for index in range(nbVal):
            xraw = index * step
            x = round(xraw * 1000.0) / 1000.0  # round to 3 decimal places
            sineValue = math.sin(x)

            # Push new value -> This triggers the Observer Pattern every 10 values
            svData.push_back(sineValue)

            print(f"Data index {index} (x={x}), sine value = {sineValue}")

        print("\nSine_DMS::execute() end")
        print("Digital Twin simulation completed successfully.")

        # Save a final snapshot of the chart to disk so results are visible
        # even when running headless (no display available).
        svService.chartPanel.save_snapshot()
