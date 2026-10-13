package Visualization;

import org.knowm.xchart.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Live chart of the sine example: it plots the raw sine data measured on the ToI (real space) and the
 * value of the DT model (virtual space). It uses the XChart library ({@code lib/xchart-3.8.8.jar}).
 *
 * @author Zeeshan ALI, Mamadou Kaba TRAORE
 * @version 2.0
 */
public class DT_ChartPanel extends JFrame {

    private final XYChart chart;
    private final List<Double> xData = new ArrayList<>();
    private final List<Double> rawSineData = new ArrayList<>();
    private final List<Double> modelData = new ArrayList<>();

    /** Creates and shows the chart window. */
    public DT_ChartPanel() {
        // Create chart
        chart = new XYChartBuilder()
                .width(900)
                .height(600)
                .title("Digital Twin Diagnosis - Sine Wave")
                .xAxisTitle("Sample Index")
                .yAxisTitle("Value")
                .build();

        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setMarkerSize(0);

        // Add empty series initially
        chart.addSeries("Raw Sine Data (RealSpace)", new double[]{0}, new double[]{0});
        chart.addSeries("DT Model Predicted Data (VirtualSpace)", new double[]{0}, new double[]{0});

        XChartPanel<XYChart> chartPanel = new XChartPanel<>(chart);
        setContentPane(chartPanel);
        setTitle("Digital Twin Live Chart");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }

    /**
     * Update the live chart with new values
     *
     * @param index      the sample index.
     * @param rawSine    the raw value measured on the ToI.
     * @param modelValue the value of the DT model.
     */
    public synchronized void updateChart(double index, double rawSine, double modelValue) {
        xData.add(index);
        rawSineData.add(rawSine);
        modelData.add(modelValue);

        // Keep only last 300 points for performance
        if (xData.size() > 300) {
            xData.remove(0);
            rawSineData.remove(0);
            modelData.remove(0);
        }

        chart.updateXYSeries("Raw Sine Data (RealSpace)", xData, rawSineData, null);
        chart.updateXYSeries("DT Model Predicted Data (VirtualSpace)", xData, modelData, null);

        repaint();
    }
}
