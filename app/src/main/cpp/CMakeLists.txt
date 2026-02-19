cmake_minimum_required(VERSION 3.22.1)

# Nombre de la librería coincidiendo con tu proyecto
project("visionups")

find_package(OpenCV REQUIRED PATHS ${OpenCV_DIR})

add_library(onnxruntime SHARED IMPORTED)
set_target_properties(onnxruntime PROPERTIES IMPORTED_LOCATION ${ONNX_DIR}/jni/${ANDROID_ABI}/libonnxruntime.so)

# Creamos la librería "visionups"
add_library(visionups SHARED native-lib.cpp)

target_include_directories(visionups PRIVATE ${ONNX_DIR}/include ${OpenCV_DIR}/../include)
target_link_libraries(visionups ${OpenCV_LIBS} onnxruntime android log)