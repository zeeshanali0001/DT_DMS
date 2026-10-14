// DT-DMS sine example (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_ChartPanel.hpp
/// \brief Chart of the sine example, written as an SVG image and a CSV file.

#ifndef DT_DMS_CHART_PANEL_HPP
#define DT_DMS_CHART_PANEL_HPP

#include <algorithm>
#include <cmath>
#include <cstdio>
#include <fstream>
#include <stdexcept>
#include <string>
#include <vector>

/// \brief The visualization of the sine example.
namespace Visualization {

/// \brief Chart of the sine example: it plots the raw sine data measured on the ToI (real space) and the value
/// of the DT model (virtual space).
///
/// The Java version shows a live window with the XChart library. The C++ version needs no graphics library: it
/// collects the same points with updateChart() and writes them as an SVG image (open it in any web browser) and
/// as a CSV file (for a spreadsheet, gnuplot, Python, ...).
class DT_ChartPanel {

 public:
  /// \brief Adds a new point to the chart; only the last 300 points are kept, as in the Java version.
  /// \param index      the sample index.
  /// \param rawSine    the raw value measured on the ToI.
  /// \param modelValue the value of the DT model.
  void updateChart(double index, double rawSine, double modelValue) {
    xData.push_back(index);
    rawSineData.push_back(rawSine);
    modelData.push_back(modelValue);
    if (xData.size() > 300) {
      xData.erase(xData.begin());
      rawSineData.erase(rawSineData.begin());
      modelData.erase(modelData.begin());
    }
  }

  /// \brief Writes the points as CSV: `index,raw,model`.
  /// \param fileName the name of the `.csv` file.
  /// \throw std::runtime_error if the file cannot be written.
  void writeCsv(const std::string& fileName) const {
    std::ofstream out(fileName, std::ios::binary);
    if (!out) throw std::runtime_error("Cannot write " + fileName);
    out << "index,raw_sine,model\n";
    char line[96];
    for (std::size_t i = 0; i < xData.size(); ++i) {
      std::snprintf(line, sizeof(line), "%g,%.17g,%.17g\n", xData[i], rawSineData[i], modelData[i]);
      out << line;
    }
  }

  /// \brief Writes the chart as an SVG image (900 × 600, as the Java window).
  /// \param fileName the name of the `.svg` file.
  /// \throw std::runtime_error if the file cannot be written.
  void writeSvg(const std::string& fileName) const {
    std::ofstream out(fileName, std::ios::binary);
    if (!out) throw std::runtime_error("Cannot write " + fileName);
    const double width = 900, height = 600, left = 70, right = 30, top = 50, bottom = 60;
    double xMin = xData.empty() ? 0 : xData.front(), xMax = xData.empty() ? 1 : xData.back();
    double yMin = -1.0, yMax = 1.0;
    for (std::size_t i = 0; i < xData.size(); ++i) {
      for (double v : {rawSineData[i], modelData[i]}) {
        if (!std::isnan(v)) {
          yMin = std::min(yMin, v);
          yMax = std::max(yMax, v);
        }
      }
    }
    if (xMax <= xMin) xMax = xMin + 1;
    yMin = std::floor(yMin * 2) / 2;
    yMax = std::ceil(yMax * 2) / 2;
    auto px = [&](double x) { return left + (x - xMin) / (xMax - xMin) * (width - left - right); };
    auto py = [&](double y) { return top + (yMax - y) / (yMax - yMin) * (height - top - bottom); };
    char buffer[256];

    out << "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"900\" height=\"600\" viewBox=\"0 0 900 600\" "
           "font-family=\"Arial, sans-serif\">\n";
    out << "<rect width=\"900\" height=\"600\" fill=\"white\"/>\n";
    out << "<text x=\"450\" y=\"30\" text-anchor=\"middle\" font-size=\"18\">Digital Twin Diagnosis - Sine Wave</text>\n";
    // grid and axis labels
    for (double y = yMin; y <= yMax + 1e-9; y += 0.5) {
      std::snprintf(buffer, sizeof(buffer),
                    "<line x1=\"%.1f\" y1=\"%.1f\" x2=\"%.1f\" y2=\"%.1f\" stroke=\"#ddd\"/>"
                    "<text x=\"%.1f\" y=\"%.1f\" text-anchor=\"end\" font-size=\"12\">%.1f</text>\n",
                    left, py(y), width - right, py(y), left - 8, py(y) + 4, y);
      out << buffer;
    }
    double xStep = std::max(1.0, std::ceil((xMax - xMin) / 10.0 / 10.0) * 10.0);
    for (double x = std::ceil(xMin / xStep) * xStep; x <= xMax + 1e-9; x += xStep) {
      std::snprintf(buffer, sizeof(buffer),
                    "<line x1=\"%.1f\" y1=\"%.1f\" x2=\"%.1f\" y2=\"%.1f\" stroke=\"#ddd\"/>"
                    "<text x=\"%.1f\" y=\"%.1f\" text-anchor=\"middle\" font-size=\"12\">%g</text>\n",
                    px(x), top, px(x), height - bottom, px(x), height - bottom + 18, x);
      out << buffer;
    }
    std::snprintf(buffer, sizeof(buffer), "<rect x=\"%.1f\" y=\"%.1f\" width=\"%.1f\" height=\"%.1f\" fill=\"none\" stroke=\"#333\"/>\n",
                  left, top, width - left - right, height - top - bottom);
    out << buffer;
    out << "<text x=\"" << (left + width - right) / 2 << "\" y=\"" << height - 15
        << "\" text-anchor=\"middle\" font-size=\"14\">Sample Index</text>\n";
    out << "<text x=\"20\" y=\"" << (top + height - bottom) / 2 << "\" text-anchor=\"middle\" font-size=\"14\" "
        << "transform=\"rotate(-90 20 " << (top + height - bottom) / 2 << ")\">Value</text>\n";
    // the two series
    polyline(out, rawSineData, px, py, "#1f77b4", "");
    polyline(out, modelData, px, py, "#ff7f0e", " stroke-dasharray=\"6 4\"");
    // legend
    out << "<g font-size=\"13\">"
           "<line x1=\"90\" y1=\"68\" x2=\"120\" y2=\"68\" stroke=\"#1f77b4\" stroke-width=\"2\"/>"
           "<text x=\"126\" y=\"72\">Raw Sine Data (RealSpace)</text>"
           "<line x1=\"90\" y1=\"88\" x2=\"120\" y2=\"88\" stroke=\"#ff7f0e\" stroke-width=\"2\" stroke-dasharray=\"6 4\"/>"
           "<text x=\"126\" y=\"92\">DT Model Predicted Data (VirtualSpace)</text></g>\n";
    out << "</svg>\n";
  }

  /// \return the number of points of the chart.
  std::size_t size() const { return xData.size(); }

 private:
  /// Writes one series as an SVG polyline (the `NaN` values are skipped).
  template <typename PX, typename PY>
  void polyline(std::ofstream& out, const std::vector<double>& values, PX px, PY py, const char* color,
                const char* style) const {
    out << "<polyline fill=\"none\" stroke=\"" << color << "\" stroke-width=\"2\"" << style << " points=\"";
    char point[48];
    for (std::size_t i = 0; i < values.size(); ++i) {
      if (std::isnan(values[i])) continue;
      std::snprintf(point, sizeof(point), "%.1f,%.1f ", px(xData[i]), py(values[i]));
      out << point;
    }
    out << "\"/>\n";
  }

  /// The sample indexes.
  std::vector<double> xData;
  /// The raw sine values measured on the ToI.
  std::vector<double> rawSineData;
  /// The values of the DT model.
  std::vector<double> modelData;
};

}  // namespace Visualization

#endif  // DT_DMS_CHART_PANEL_HPP
