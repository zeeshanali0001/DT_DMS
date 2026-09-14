
# Configuration file for the Sphinx documentation builder.

import os
import sys
sys.path.insert(0, os.path.abspath('../../'))  # path to your project code

extensions = [
    'sphinx.ext.autodoc',
    'sphinx.ext.napoleon',   # if you use Google or NumPy style docstrings
    'sphinx.ext.viewcode',   # adds links to source code
    'sphinx.ext.inheritance_diagram',
    'sphinx.ext.graphviz',
]

#
# For the full list of built-in configuration values, see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Project information -----------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#project-information

project = 'DT_DMS'
copyright = '2026, Milad Zahediyami'
author = 'Milad Zahediyami'
release = '2026'

# -- General configuration ---------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#general-configuration

templates_path = ['_templates']
exclude_patterns = []



# -- Options for HTML output -------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#options-for-html-output

html_theme = 'alabaster'
html_static_path = ['_static']

