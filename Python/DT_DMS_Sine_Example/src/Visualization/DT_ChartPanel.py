# DT-DMS sine example (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_ChartPanel.py
@brief Live chart of the sine example (matplotlib).
"""

from typing import List


class DT_ChartPanel:
    """!
    @brief Live chart of the sine example: it plots the raw sine data measured on the ToI (real space) and the
    value of the DT model (virtual space).

    It uses matplotlib in interactive mode, as the Java version uses XChart. matplotlib is optional: available()
    tells whether a chart window can be shown (matplotlib installed and a display available).
    """

    ## Only the last 300 points are kept, as in the Java version.
    MAX_POINTS = 300

    @staticmethod
    def available() -> bool:
        """! @return `True` if matplotlib is installed and can open a window."""
        try:
            import matplotlib
            import matplotlib.pyplot as plt
        except ImportError:
            return False
        return matplotlib.get_backend().lower() not in ("agg", "pdf", "ps", "svg", "template")

    def __init__(self):
        """! @brief Creates and shows the chart window."""
        import matplotlib.pyplot as plt
        ## The pyplot module.
        self._plt = plt
        ## The sample indexes.
        self.x_data: List[float] = []
        ## The raw sine values measured on the ToI.
        self.raw_sine_data: List[float] = []
        ## The values of the DT model.
        self.model_data: List[float] = []
        plt.ion()
        self._fig, self._ax = plt.subplots(figsize=(9, 6))
        self._fig.canvas.manager.set_window_title("Digital Twin Live Chart")
        self._ax.set_title("Digital Twin Diagnosis - Sine Wave")
        self._ax.set_xlabel("Sample Index")
        self._ax.set_ylabel("Value")
        (self._raw_line,) = self._ax.plot([], [], label="Raw Sine Data (RealSpace)")
        (self._model_line,) = self._ax.plot([], [], label="DT Model Predicted Data (VirtualSpace)")
        self._ax.legend(loc="upper right")
        plt.show(block=False)

    def update_chart(self, index: float, raw_sine: float, model_value: float) -> None:
        """!
        @brief Updates the live chart with new values.
        @param index       the sample index.
        @param raw_sine    the raw value measured on the ToI.
        @param model_value the value of the DT model.
        """
        self.x_data.append(index)
        self.raw_sine_data.append(raw_sine)
        self.model_data.append(model_value)
        if len(self.x_data) > self.MAX_POINTS:
            del self.x_data[0], self.raw_sine_data[0], self.model_data[0]
        self._raw_line.set_data(self.x_data, self.raw_sine_data)
        self._model_line.set_data(self.x_data, self.model_data)
        self._ax.relim()
        self._ax.autoscale_view()
        self._plt.pause(0.001)

    def keep_open(self) -> None:
        """! @brief Keeps the window open until the user closes it (as the Java window)."""
        self._plt.ioff()
        self._plt.show()
