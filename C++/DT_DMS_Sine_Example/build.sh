#!/bin/sh
# Builds the DT-DMS sine example with CMake (g++ or clang++, C++17) and runs the example.
# Usage: ./build.sh          build in Release and run build/bin/DT_DMS_SineCubicBP
#        ./build.sh doc      also generate the Doxygen/Graphviz documentation in docs/html
set -e
cd "$(dirname "$0")"
cmake -S . -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build --config Release
if [ "$1" = "doc" ]; then cmake --build build --target doc; fi
echo
echo "Running build/bin/DT_DMS_SineCubicBP"
echo
./build/bin/DT_DMS_SineCubicBP
