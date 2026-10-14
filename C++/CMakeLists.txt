# DT-DMS framework (C++): the library "dtdms" and the example executable "DT_DMS_Architecture".
#
#   cmake -S . -B build -DCMAKE_BUILD_TYPE=Release
#   cmake --build build --config Release
#   build/bin/DT_DMS_Architecture            (Windows: build\bin\DT_DMS_Architecture.exe)
#   cmake --build build --target doc         (Doxygen/Graphviz documentation in docs/html)
#
# Or simply run build.bat (Windows) or ./build.sh (Linux, macOS, Git Bash).
cmake_minimum_required(VERSION 3.16)

project(DT_DMS VERSION 2.0.0
        DESCRIPTION "Digital Twin DT = <D, M, S, mu>: Data, Models, Services and model inference"
        LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
if(NOT CMAKE_BUILD_TYPE AND NOT CMAKE_CONFIGURATION_TYPES)
  set(CMAKE_BUILD_TYPE Release)
endif()

# All executables in build/bin, all libraries in build/lib
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
foreach(config Debug Release RelWithDebInfo MinSizeRel)
  string(TOUPPER ${config} CONFIG)
  set(CMAKE_RUNTIME_OUTPUT_DIRECTORY_${CONFIG} ${CMAKE_BINARY_DIR}/bin)
  set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY_${CONFIG} ${CMAKE_BINARY_DIR}/lib)
endforeach()

# Version header (Config.h) generated in the build tree
configure_file(${PROJECT_SOURCE_DIR}/src/Config.h.in ${PROJECT_BINARY_DIR}/generated/Config.h)

# ---------------------------------------------------------------- the framework library
set(DTDMS_HEADERS
  src/DT_DMS.hpp
  src/DT_Console.hpp
  src/Automaton/DT_Automaton.hpp
  src/Data/DT_Data.hpp
  src/Data/DT_DataSegment.hpp
  src/Data/DT_DataCharacterization.hpp
  src/Models/DT_Model.hpp
  src/Models/DT_Model_Descriptive.hpp
  src/Models/DT_Model_Predictive.hpp
  src/Models/DT_Model_Prescriptive.hpp
  src/Mu/DT_Mu.hpp
  src/Mu/ObserverData.hpp
  src/Mu/ObserverDataManager.hpp
  src/Mu/ObserverModel.hpp
  src/Services/DT_Service.hpp
  src/Services/DT_Service_Monitoring.hpp
  src/Services/DT_Service_Prediction.hpp
  src/Services/DT_Service_Diagnosis.hpp
  src/Services/DT_Service_Optimization.hpp
  src/Services/DT_Service_Control.hpp
)
add_library(dtdms STATIC src/Automaton/DT_Automaton.cpp ${DTDMS_HEADERS})
target_include_directories(dtdms PUBLIC
  $<BUILD_INTERFACE:${PROJECT_SOURCE_DIR}/src>
  $<BUILD_INTERFACE:${PROJECT_BINARY_DIR}/generated>)
target_compile_features(dtdms PUBLIC cxx_std_17)
if(MSVC)
  # /utf-8: the sources use UTF-8 symbols (μ, δ, ω, ⟨⟩, ...)
  target_compile_options(dtdms PUBLIC /utf-8 /W4 /permissive- /EHsc)
else()
  target_compile_options(dtdms PUBLIC -Wall -Wextra -Wpedantic)
endif()

# ---------------------------------------------------------------- the example executable
add_executable(DT_DMS_Architecture src/DT_DMS_Architecture.cpp src/DT_DMS_Architecture.hpp)
target_link_libraries(DT_DMS_Architecture PRIVATE dtdms)

# ---------------------------------------------------------------- documentation (Doxygen + Graphviz)
find_package(Doxygen OPTIONAL_COMPONENTS dot)
if(DOXYGEN_FOUND)
  add_custom_target(doc
    COMMAND ${DOXYGEN_EXECUTABLE} Doxyfile
    WORKING_DIRECTORY ${PROJECT_SOURCE_DIR}
    COMMENT "Generating the documentation in docs/html"
    VERBATIM)
endif()
