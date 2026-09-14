"""
DT_ChartPanel.py

Live chart window that plots the raw sine data ("RealSpace" / Physical Twin)
against the Digital Twin model's predicted data ("VirtualSpace" / Digital
Model), exactly like the original Java version built with XChart + Swing.

Python has no built-in equivalent of XChart/Swing, so this port uses
matplotlib in interactive mode (plt.ion()) to reproduce the same live,
continuously-updating line chart with two series.

If no graphical display is available (e.g. running on a headless server or
CI), the chart automatically falls back to a non-interactive mode: it keeps
recording the data and silently skips on-screen rendering, so the rest of
the simulation still runs without errors. In that fallback mode, a snapshot
image ('dt_dms_chart.png') is written to disk at the end so results are
still visible.

This is the Python equivalent of Visualization/DT_ChartPanel.java.

@author Zeeshan ALI, Mamadou Kaba TRAORE
@version 1.0 (Python port)
"""

import matplotlib

# Detect whether we can actually open an interactive window. If not
# (headless environment, no DISPLAY, etc.), fall back to the 'Agg' backend
# which never opens a window but never raises an error either.
try:
    import matplotlib.pyplot as plt
    plt.ion()
    _test_fig = plt.figure()
    plt.close(_test_fig)
    _INTERACTIVE = True
except Exception:
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
    _INTERACTIVE = False


class DT_ChartPanel:
    """Live line-chart of raw sine values vs. Digital Twin model predictions."""

    # Keep only the last N points on screen, matching the Java version's
    # 'if (xData.size() > 300)' trimming behaviour.
    MAX_POINTS = 300

    def __init__(self):
        # Data buffers (equivalent of xData / rawSineData / modelData in Java)
        self.xData = []
        self.rawSineData = []
        self.modelData = []

        self.fig, self.ax = plt.subplots(figsize=(9, 6))
        self.fig.canvas.manager.set_window_title("Digital Twin Live Chart")
        self.ax.set_title("Digital Twin Diagnosis - Sine Wave")
        self.ax.set_xlabel("Sample Index")
        self.ax.set_ylabel("Value")

        # Two line series, matching the two XYChart series in the Java version.
        (self.raw_line,) = self.ax.plot(
            [], [], label="Raw Sine Data (RealSpace)", linewidth=1.5
        )
        (self.model_line,) = self.ax.plot(
            [], [], label="DT Model Predicted Data (VirtualSpace)", linewidth=1.5
        )
        self.ax.legend(loc="upper right")

        if _INTERACTIVE:
            plt.show(block=False)

    def update_chart(self, index: float, raw_sine: float, model_value: float):
        """
        Update the live chart with new values.

        :param index: Sample index (x-axis value).
        :param raw_sine: Actual/raw sine value (from the Physical Twin / data).
        :param model_value: Predicted value (from the Digital Model).
        """
        self.xData.append(index)
        self.rawSineData.append(raw_sine)
        self.modelData.append(model_value)

        # Keep only the last MAX_POINTS points for performance
        if len(self.xData) > self.MAX_POINTS:
            self.xData.pop(0)
            self.rawSineData.pop(0)
            self.modelData.pop(0)

        self.raw_line.set_data(self.xData, self.rawSineData)
        self.model_line.set_data(self.xData, self.modelData)

        self.ax.relim()
        self.ax.autoscale_view()

        if _INTERACTIVE:
            self.fig.canvas.draw_idle()
            self.fig.canvas.flush_events()
            plt.pause(0.001)

    def save_snapshot(self, path: str = "dt_dms_chart.png"):
        """Save the current chart to an image file (useful in headless mode)."""
        self.fig.savefig(path)
        print(f"[DT_ChartPanel] Chart snapshot saved to '{path}'")
