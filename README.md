# 🚜 VisionUPS: Detección de Maquinaria Pesada en Android (C++ Native)

> **Examen de Recuperación - Sistemas Inteligentes**
> **Autor:** Frank Bernardo Chuchuca Torres
> **Universidad Politécnica Salesiana (UPS)**

## 📋 Descripción del Proyecto
Aplicación móvil nativa para la detección de objetos en tiempo real, enfocada en **Seguridad Industrial y Maquinaria Pesada**.

A diferencia de las implementaciones estándar en Java/Kotlin, este proyecto ejecuta todo el pipeline de visión artificial (Pre-procesamiento, Inferencia y Post-procesamiento) en la **capa nativa (C++)** utilizando **ONNX Runtime** y **OpenCV**, maximizando el rendimiento en dispositivos con recursos limitados.

## 🚀 Características Técnicas (Nivel Avanzado)
Este proyecto cumple con los requisitos de la categoría "Avanzado" de la rúbrica:

* **🧠 Motor de Inferencia Nativo:** Uso de `ONNX Runtime C++ API` a través de JNI. No se utiliza TensorFlow Lite ni interpretadores Java.
* **🎯 Modelo Personalizado:** YOLOv8 Nano (`yolov8n`) re-entrenado con un dataset específico de **Construction Site Safety** (exclusivo para este examen).
* **⚙️ Procesamiento en C++:**
    * **Pre-procesamiento:** Resize, Normalización y conversión RGBA -> RGB con OpenCV.
    * **Post-procesamiento:** Implementación manual de **Non-Maximum Suppression (NMS)** usando `cv::dnn::NMSBoxes` para filtrar detecciones duplicadas.
* **⚡ Rendimiento:** Optimizado para correr en CPU móvil (probado en Tecno Pop 8).

## 🏗️ Clases Detectadas (Dataset Propio)
El modelo ha sido entrenado para detectar 15 clases críticas en una obra de construcción:

1.  `Excavator` (Excavadora)
2.  `Dump Truck` (Volqueta)
3.  `Front End Loader` (Cargadora Frontal)
4.  `Bulldozer` (Topadora/Oruga)
5.  `Concrete Mixer` (Mixer)
6.  `Crane` (Grúa)
7.  `Tractor Trailer` (Trailer)
8.  `Skid Steer` (Minicargadora)
9.  `Hard Hat ON/OFF` (Uso de Casco)
10. `Safety Vest ON/OFF` (Uso de Chaleco)
11. `Gloves ON/OFF` (Uso de Guantes)
12. `Worker` (Trabajador)

## 📂 Estructura del Código (Entregables)
Siguiendo los requisitos del examen, los archivos críticos se encuentran en:

* **Lógica C++ / JNI:** [`app/src/main/cpp/native-lib.cpp`](app/src/main/cpp/native-lib.cpp) (Contiene la carga del modelo, pre-proceso e inferencia).
* **Modelo Entrenado:** [`app/src/main/assets/yolov8n.onnx`](app/src/main/assets/yolov8n.onnx) (Archivo binario del modelo exportado).
* **Configuración de Build:** [`app/src/main/cpp/CMakeLists.txt`](app/src/main/cpp/CMakeLists.txt) (Enlace de librerías nativas).

## 🛠️ Requisitos de Compilación
Para compilar este proyecto, se requiere la siguiente configuración de entorno:

1.  **Android Studio:** Versión Iguana o superior.
2.  **NDK:** Versión 26.x.
3.  **Librerías Externas (Rutas Absolutas):**
    * El proyecto espera encontrar **OpenCV Android SDK** en: `C:/OpenCV`
    * El proyecto espera encontrar **ONNX Runtime (Headers + JNI)** en: `C:/ONNX`
    * *(Nota: Si sus rutas son diferentes, por favor modifique el archivo `CMakeLists.txt`)*.

## 📸 Evidencia de Funcionamiento
El sistema realiza inferencia en tiempo real visualizando:
* Bounding Boxes con colores por clase.
* Etiqueta de la clase y porcentaje de confianza.
* Contador de FPS y cantidad de objetos detectados.

---
*Desarrollado para la materia de Sistemas Inteligentes - 2026*
