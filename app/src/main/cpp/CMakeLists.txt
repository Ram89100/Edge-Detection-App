cmake_minimum_required(VERSION 3.18.1)

project("native-lib")

add_library(native-lib SHARED native-lib.cpp)

# OpenCV static/shared library from module
add_library(opencv_java4 SHARED IMPORTED)
set_target_properties(opencv_java4 PROPERTIES
        IMPORTED_LOCATION "${CMAKE_SOURCE_DIR}/../../../../opencv/build/intermediates/cmake/debug/obj/${ANDROID_ABI}/libopencv_java4.so"
)

# Include OpenCV headers
include_directories("${CMAKE_SOURCE_DIR}/../../../../opencv/src/main/jni/include")

target_link_libraries(native-lib
        opencv_java4
        log
        android)
