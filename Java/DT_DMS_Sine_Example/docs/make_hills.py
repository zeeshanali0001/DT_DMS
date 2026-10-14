"""Draws the two automata of the DT-DMS sine example in the HiLLS graphical notation.

The notation follows the HiLLS concrete syntax (H-System box, ports, phases and transitions):
  - H-System: a box with its name, the parameters as double-bordered boxes on the top border, the input
    ports as boxes on the left border and the output ports on the right border, then three compartments:
    state variables, operations, and the phase transition diagram.
  - Phase: a circle with the phase name above it, the characteristic (condition) in the upper half and the
    activities in the lower half. A passive phase (T = infinity) is a double circle, a transient phase
    (T = 0) is a dashed circle, an active phase (T = t) is a single circle.
  - Transition: an external transition is a dashed arrow, an internal transition is a solid arrow.
    A small diamond is a conditional transition.
  - The initial phase is pointed to by a thick arrow starting from a black dot.

The labels use the mathematical expressions of the formal specification <Lambda, X, Y, Sigma, Phi, Delta, Delta*>:
"Delta^in : ? (port, value) / update" for an external transition, "Delta^time : [guard] / ! (port, value)"
for an internal transition, and "Delta*" for the initialization.

Usage (from the docs folder; needs the PyMuPDF library:  pip install pymupdf):
    python make_hills.py
It writes DT_DMS_Sine_HiLLS.pdf, DT_DMS_Sine_HiLLS.svg and DT_DMS_Sine_HiLLS.png.
"""
import math
import re

import pymupdf

FONTS = "C:/Windows/Fonts/"
REGULAR = pymupdf.Font(fontfile=FONTS + "arial.ttf")
BOLD = pymupdf.Font(fontfile=FONTS + "arialbd.ttf")
MATH = pymupdf.Font(fontfile=FONTS + "cambria.ttc")        # for the mathematical expressions
SYMBOL = pymupdf.Font(fontfile=FONTS + "seguisym.ttf")     # fallback for a missing symbol
BLACK = (0, 0, 0)
WHITE = (1, 1, 1)


class Canvas:
    """A page with a few drawing helpers."""

    def __init__(self, width, height):
        self.doc = pymupdf.open()
        self.page = self.doc.new_page(width=width, height=height)
        self.writer = pymupdf.TextWriter(self.page.rect)

    # ------------------------------------------------------------------ text
    def _runs(self, text, size, font):
        """Splits the text into runs (string, font, size, raise). "^{...}" writes a superscript."""
        runs = []
        for i, part in enumerate(re.split(r"\^\{([^}]*)\}", text)):
            superscript = i % 2 == 1
            for ch in part:
                f = font if font.has_glyph(ord(ch)) else SYMBOL
                s = size * 0.7 if superscript else size
                dy = -size * 0.38 if superscript else 0
                if runs and runs[-1][1] is f and runs[-1][2] == s:
                    runs[-1][0] += ch
                else:
                    runs.append([ch, f, s, dy])
        return runs

    def text_width(self, text, size, font=REGULAR):
        return sum(f.text_length(t, fontsize=s) for t, f, s, _ in self._runs(text, size, font))

    def text(self, x, y, text, size=10, font=REGULAR, align="left"):
        """Writes one line of text; (x, y) is the left, centre or right end of its baseline."""
        width = self.text_width(text, size, font)
        if align == "center":
            x -= width / 2
        elif align == "right":
            x -= width
        for t, f, s, dy in self._runs(text, size, font):
            self.writer.append((x, y + dy), t, font=f, fontsize=s)
            x += f.text_length(t, fontsize=s)

    def math(self, x, y, text, size=11, align="center"):
        """Writes a mathematical expression."""
        self.text(x, y, text, size=size, font=MATH, align=align)

    # ------------------------------------------------------------------ shapes
    def rect(self, x0, y0, x1, y1, width=1.2, fill=None):
        self.page.draw_rect(pymupdf.Rect(x0, y0, x1, y1), color=BLACK, fill=fill, width=width)

    def line(self, x0, y0, x1, y1, width=1.2):
        self.page.draw_line((x0, y0), (x1, y1), color=BLACK, width=width)

    def arrow_head(self, tip, origin, size=9):
        """Draws a filled arrow head at 'tip', pointing away from 'origin'."""
        angle = math.atan2(tip[1] - origin[1], tip[0] - origin[0])
        left = (tip[0] - size * math.cos(angle - 0.38), tip[1] - size * math.sin(angle - 0.38))
        right = (tip[0] - size * math.cos(angle + 0.38), tip[1] - size * math.sin(angle + 0.38))
        self.page.draw_polyline([tip, left, right, tip], color=BLACK, fill=BLACK, width=0.5)

    def curve(self, p0, p1, p2, p3, dashed=False, arrow=True, width=1.3):
        """Draws a Bezier curve from p0 to p3 (control points p1 and p2), with an arrow head at p3."""
        self.page.draw_bezier(p0, p1, p2, p3, color=BLACK, width=width,
                              dashes="[5 4] 0" if dashed else None)
        if arrow:
            self.arrow_head(p3, p2)

    # ------------------------------------------------------------------ HiLLS elements
    def parameter(self, cx, cy, name, w=92, h=30):
        """A parameter: a double-bordered box centred on the top border of the H-System."""
        self.rect(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2, fill=WHITE)
        self.rect(cx - w / 2 + 4, cy - h / 2 + 4, cx + w / 2 - 4, cy + h / 2 - 4)
        self.text(cx, cy + 4, name, size=11, align="center")

    def port(self, x0, y0, label, w=104, h=62):
        """An input or output port: a box with a triangle and the port name and domain."""
        self.rect(x0, y0, x0 + w, y0 + h, fill=WHITE)
        cx = x0 + w / 2
        self.page.draw_polyline([(cx - 11, y0 + 10), (cx + 13, y0 + 22), (cx - 11, y0 + 34), (cx - 11, y0 + 10)],
                                color=BLACK, width=1.2)
        self.text(cx, y0 + 52, label, size=10.5, font=BOLD, align="center")

    def phase(self, cx, cy, name, delay, characteristic, activities, kind, r=46):
        """A phase: 'passive' (double circle), 'transient' (dashed circle) or 'active' (single circle)."""
        if kind == "passive":
            self.page.draw_circle((cx, cy), r, color=BLACK, fill=WHITE, width=1.6)
            self.page.draw_circle((cx, cy), r - 5, color=BLACK, width=1.6)
            inner = r - 5
        else:
            self.page.draw_circle((cx, cy), r, color=BLACK, fill=WHITE, width=1.3,
                                  dashes="[4 3] 0" if kind == "transient" else None)
            inner = r
        self.line(cx - inner, cy, cx + inner, cy, width=1.2)
        self.text(cx, cy - r - 21, name, size=11.5, font=BOLD, align="center")
        self.math(cx, cy - r - 7, delay, size=10)
        self.math(cx, cy - 11, characteristic, size=10.5)
        self.text(cx, cy + 19, activities, size=9.5, align="center")

    def initial(self, x, y, target, label, r=8):
        """The initial marker: a black dot with a thick arrow to the initial phase."""
        self.page.draw_circle((x, y), r, color=BLACK, fill=BLACK)
        self.page.draw_line((x, y), target, color=BLACK, width=3.6)
        self.arrow_head(target, (x, y), size=15)
        self.math(x - 4, y - 16, label, size=11, align="right")

    def diamond(self, cx, cy, s=7):
        """A conditional transition point."""
        self.page.draw_polyline([(cx, cy - s), (cx + s * 0.7, cy), (cx, cy + s), (cx - s * 0.7, cy), (cx, cy - s)],
                                color=BLACK, fill=WHITE, width=1.2)

    def h_system(self, x0, y0, w, title, specification, parameters, variables, operations, diagram_height,
                 inputs=(), outputs=()):
        """Draws the box of an H-System and returns the top of its diagram compartment."""
        top = y0 + 15                                   # the top border; parameters sit on it
        y = top + 62
        self.text(x0 + w / 2, top + 33, title, size=14, font=BOLD, align="center")
        self.math(x0 + w / 2, top + 51, specification, size=10.5)
        self.line(x0, y, x0 + w, y)                     # variables compartment
        variables_top = y
        for i, (name, meaning, domain) in enumerate(variables):
            self.text(x0 + 74, y + 18 + 16 * i, name, size=10.5)
            self.text(x0 + 150, y + 18 + 16 * i, meaning, size=10.5)
            self.text(x0 + w - 74, y + 18 + 16 * i, domain, size=10.5, align="right")
        y += 16 * len(variables) + 14
        self.line(x0, y, x0 + w, y)                     # operations compartment
        for i, operation in enumerate(operations):
            self.text(x0 + 74, y + 18 + 16 * i, operation, size=10.5)
        y += 16 * len(operations) + 14
        self.line(x0, y, x0 + w, y)                     # diagram compartment
        self.rect(x0, top, x0 + w, y + diagram_height, width=1.5)
        for i, name in enumerate(parameters):
            self.parameter(x0 + 70 + 112 * i, top, name)
        for i, label in enumerate(inputs):              # ports straddle the left and right borders
            self.port(x0 - 52, variables_top - 6 + 70 * i, label)
        for i, label in enumerate(outputs):
            self.port(x0 + w - 52, variables_top - 6 + 70 * i, label)
        return y

    def save(self, name):
        self.writer.write_text(self.page, color=BLACK)
        self.doc.save(name + ".pdf")
        self.page.get_pixmap(dpi=300).save(name + ".png")
        with open(name + ".svg", "w", encoding="utf-8") as f:
            f.write(self.page.get_svg_image(text_as_path=True))


def main():
    w = 660                                             # width of an H-System
    x0 = 80
    c = Canvas(width=x0 * 2 + w, height=875)

    # ------------------------------------------------------------------ the model
    top = c.h_system(
        x0, 20, w, "DT_Model_Descr_CubicBSpline",
        "ℳ = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩      Λ = {step, leftEndpoint}    X = {data}    Y = {updated}    Σ = {count, last}",
        parameters=["step", "leftEndpoint"],
        variables=[("count", "(number of control points)", "where count ∈ ℕ"),
                   ("last", "(last value assimilated)", "where last ∈ ℝ")],
        operations=["update (d)", "getSineModelValue (x)"],
        diagram_height=240,
        inputs=["data ∈ ℝ"], outputs=["updated ∈ ℕ"])
    cy = top + 128
    wait, update = (x0 + 185, cy), (x0 + 475, cy)
    mid = (wait[0] + update[0]) / 2
    c.phase(*wait, "WAIT", "Delay = ∞", "count ≥ 0", "–", "passive")
    c.phase(*update, "UPDATE", "Delay = 0", "count ≥ 1", "update (d)", "transient")
    c.initial(x0 + 92, cy + 72, (wait[0] - 36, wait[1] + 30), "Δ*")
    # external transition WAIT -> UPDATE (dashed)
    c.curve((wait[0] + 40, cy - 26), (wait[0] + 110, cy - 62), (update[0] - 110, cy - 62), (update[0] - 41, cy - 25),
            dashed=True)
    c.math(mid, cy - 62, "Δ^{in} :   ? (data, d)   /")
    c.math(mid, cy - 30, "count := count + 1,  last := d")
    # internal transition UPDATE -> WAIT (solid)
    c.curve((update[0] - 37, cy + 29), (update[0] - 105, cy + 78), (wait[0] + 105, cy + 78), (wait[0] + 42, cy + 32))
    c.math(mid, cy + 86, "Δ^{time} :   ! (updated, count)")

    # ------------------------------------------------------------------ the service
    top = c.h_system(
        x0, 455, w, "DT_Service_Diagnosis_Sine",
        "𝒮 = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩      Λ = {threshold}    X = {data}    Y = {anomaly}    Σ = {residual}",
        parameters=["threshold"],
        variables=[("residual", "(distance between the measured and the model value)", "where residual ≥ 0")],
        operations=["residualOf (d)", "executeService ( )"],
        diagram_height=262,
        inputs=["data ∈ ℝ"], outputs=["anomaly ∈ ℝ"])
    cy = top + 122
    idle, check = (x0 + 160, cy), (x0 + 500, cy)
    mid = (idle[0] + check[0]) / 2
    c.phase(*idle, "IDLE", "Delay = ∞", "residual ≥ 0", "–", "passive")
    c.phase(*check, "CHECK", "Delay = 0", "residual ≥ 0", "residualOf (d)", "transient")
    c.initial(x0 + 68, cy + 72, (idle[0] - 36, idle[1] + 30), "Δ*")
    # external transition IDLE -> CHECK (dashed)
    c.curve((idle[0] + 40, cy - 26), (idle[0] + 120, cy - 62), (check[0] - 120, cy - 62), (check[0] - 41, cy - 25),
            dashed=True)
    c.math(mid, cy - 62, "Δ^{in} :   ? (data, d)   /")
    c.math(mid, cy - 30, "residual := |d − m(t)|")
    # internal transition CHECK -> IDLE (solid), through a conditional transition point
    d = (check[0] - 78, cy + 44)
    c.curve((check[0] - 40, cy + 23), (check[0] - 52, cy + 32), (d[0] + 22, d[1] - 4), (d[0] + 6, d[1]))
    c.diamond(*d)
    c.curve((d[0] - 5, d[1]), (d[0] - 70, d[1] - 2), (idle[0] + 120, cy + 30), (idle[0] + 46, cy + 12))
    c.math(d[0] - 108, cy + 12, "Δ^{time} :  [residual > threshold]  /", size=10.5)
    c.math(d[0] - 96, cy + 56, "! (anomaly, residual)", size=10.5)
    c.curve((d[0], d[1] + 7), (d[0] - 20, cy + 128), (idle[0] + 70, cy + 132), (idle[0] + 20, cy + 43))
    c.math(d[0] - 96, cy + 128, "Δ^{time} :  [residual ≤ threshold]", size=10.5)

    c.save("DT_DMS_Sine_HiLLS")
    print("Wrote DT_DMS_Sine_HiLLS.pdf, .svg and .png")


if __name__ == "__main__":
    main()
