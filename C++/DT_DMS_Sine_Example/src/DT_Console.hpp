// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_Console.hpp
/// \brief Lets the Windows console show the UTF-8 symbols of the framework (μ, δ, ω, ⟨⟩, ...).

#ifndef DT_DMS_CONSOLE_HPP
#define DT_DMS_CONSOLE_HPP

#ifdef _WIN32
extern "C" __declspec(dllimport) int __stdcall SetConsoleOutputCP(unsigned int codePage);
#endif

/// Switches the Windows console to UTF-8 (code page 65001); does nothing on other systems.
inline void enableUtf8Console() {
#ifdef _WIN32
  SetConsoleOutputCP(65001u);
#endif
}

#endif  // DT_DMS_CONSOLE_HPP
