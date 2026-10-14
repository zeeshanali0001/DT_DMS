# DT-DMS (Python) - Doxygen input filter, set in the Doxyfile (INPUT_FILTER = "python doxygen_filter.py").
"""Prints a source file for Doxygen without "(metaclass=ABCMeta)", which Doxygen would show as a base class."""

import sys

with open(sys.argv[1], encoding="utf-8") as source:
    text = source.read().replace("(metaclass=ABCMeta)", "")
sys.stdout.buffer.write(text.encode("utf-8"))
