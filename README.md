# 🚜 VisionUPS: Detección de Maquinaria Pesada en Android (C++ Native)

> **Examen de Recuperación - Sistemas Inteligentes**
> **Autor:** Frank Bernardo Chuchuca Torres
> **Universidad Politécnica Salesiana (UPS)**

## 📋 Descripción del Proyecto
Aplicación móvil nativa para la detección de objetos en tiempo real, enfocada en **Seguridad Industrial y Maquinaria Pesada**.

Este proyecto ejecuta todo el pipeline de visión artificial (Pre-procesamiento, Inferencia y Post-procesamiento) en la **capa nativa (C++)** utilizando **ONNX Runtime** y **OpenCV**, maximizando el control sobre el hardware del dispositivo.

## 🚀 Características Técnicas (Nivel Avanzado)
Este proyecto cumple con los requisitos de la categoría "Avanzado":

* **🧠 Motor de Inferencia Nativo:** Uso de `ONNX Runtime C++ API` a través de JNI.
* **🎯 Modelo Personalizado:** YOLOv8 Nano (`yolov8n`) re-entrenado con dataset específico de construcción.
* **⚙️ Procesamiento en C++:** Implementación manual de **NMS (Non-Maximum Suppression)** para filtrar detecciones.
* **📓 Evidencia de Entrenamiento:** Se incluye el cuaderno de Jupyter con el proceso de Fine-Tuning y exportación.

## 📓 Evidencia de Entrenamiento y Reproducibilidad
El código utilizado para entrenar el modelo se encuentra disponible en el repositorio:
* **Archivo:** [`notebooks/notebooks.ipynb`](notebooks/notebooks.ipynb)
* **Proceso:** Descarga desde Roboflow API, entrenamiento con Ultralytics YOLOv8 y exportación a formato ONNX (Opset 12).

## 🏗️ Clases Detectadas
El modelo detecta 15 clases críticas:
1. `Excavator` (Excavadora)
2. `Dump Truck` (Volqueta)
3. `Front End Loader` (Cargadora)
4. `Hard Hat ON/OFF` (Casco)
5. `Safety Vest ON/OFF` (Chaleco)
6. `Gloves ON/OFF` (Guantes)
7. `Worker` (Trabajador)
... entre otras.

## 📂 Estructura del Código
* **Lógica C++:** [`app/src/main/cpp/native-lib.cpp`](app/src/main/cpp/native-lib.cpp) (Inferencia y NMS).
* **Modelo:** [`app/src/main/assets/yolov8n.onnx`](app/src/main/assets/yolov8n.onnx).
* **Entrenamiento:** [`notebooks/`](notebooks/).

## ⚙️ Notas sobre Rendimiento y Hardware
El código fuente incluye la implementación para aceleración por hardware (**NNAPI**) y soporte para modelos **FP16**.
> *Nota: Para la demostración en el dispositivo de prueba (Tecno Pop 8), se ha forzado la ejecución en **CPU con precisión FP32** para garantizar la estabilidad de los drivers, priorizando la robustez de la detección sobre la tasa de cuadros (FPS).*

## 🛠️ Requisitos de Compilación
1.  **Android Studio** Iguana+.
2.  **NDK** v26.x.
3.  **Librerías Externas (en C:/):**
    * OpenCV Android SDK (`C:/OpenCV`)
    * ONNX Runtime (`C:/ONNX`)

---
*Desarrollado para la materia de Sistemas Inteligentes - 2026*
