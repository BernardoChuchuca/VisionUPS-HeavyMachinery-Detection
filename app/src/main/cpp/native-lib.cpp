#include <jni.h>
#include <string>
#include <vector>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include <opencv2/dnn.hpp> // IMPORTANTE: Necesario para NMS
#include <onnxruntime_cxx_api.h>

#define LOG_TAG "VisionUPS_NMS"

// Variables Globales
Ort::Env ortEnv(ORT_LOGGING_LEVEL_WARNING, "Yolo");
Ort::Session* session = nullptr;
const int INPUT_SIZE = 640; // Aseguramos que sea 640 para tu modelo nuevo

// Configuración del NMS (Ajusta si es necesario)
const float CONFIDENCE_THRESHOLD = 0.25f; // Mínimo para ser considerado
const float NMS_THRESHOLD = 0.45f;        // Cuánto se pueden encimar (45%)

// Función auxiliar para leer assets
std::vector<char> load_model_file(AAssetManager* mgr, const char* name) {
    AAsset* asset = AAssetManager_open(mgr, name, AASSET_MODE_BUFFER);
    if (!asset) return {};
    size_t length = AAsset_getLength(asset);
    std::vector<char> buffer(length);
    AAsset_read(asset, buffer.data(), length);
    AAsset_close(asset);
    return buffer;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_bernardo_visionups_MainActivity_initModel(JNIEnv* env, jobject, jobject assetManager) {
    if (session) return JNI_TRUE;

    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);
    auto modelData = load_model_file(mgr, "yolov8n.onnx");

    if (modelData.empty()) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "❌ No se encontró yolov8n.onnx");
        return JNI_FALSE;
    }

    Ort::SessionOptions options;
    options.SetIntraOpNumThreads(1);
    options.SetGraphOptimizationLevel(GraphOptimizationLevel::ORT_ENABLE_BASIC);

    try {
        session = new Ort::Session(ortEnv, modelData.data(), modelData.size(), options);
        return JNI_TRUE;
    } catch (const std::exception& e) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "❌ Error sesión: %s", e.what());
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bernardo_visionups_MainActivity_detect(JNIEnv* env, jobject, jobject buffer, jint width, jint height, jint stride) {
    if (!session) return env->NewStringUTF("");

    // 1. PREPROCESAMIENTO
    uint8_t* srcData = (uint8_t*)env->GetDirectBufferAddress(buffer);
    cv::Mat frame(height, width, CV_8UC4, srcData, stride);
    cv::Mat blob;

    cv::resize(frame, blob, cv::Size(INPUT_SIZE, INPUT_SIZE), 0, 0, cv::INTER_NEAREST);
    cv::cvtColor(blob, blob, cv::COLOR_RGBA2RGB);
    blob.convertTo(blob, CV_32F, 1.0 / 255.0);

    std::vector<float> inputValues;
    std::vector<cv::Mat> channels(3);
    cv::split(blob, channels);
    for (int i = 0; i < 3; ++i) {
        inputValues.insert(inputValues.end(), (float*)channels[i].data, (float*)channels[i].data + INPUT_SIZE * INPUT_SIZE);
    }

    // 2. INFERENCIA
    std::vector<int64_t> inputShape = {1, 3, INPUT_SIZE, INPUT_SIZE};
    auto memoryInfo = Ort::MemoryInfo::CreateCpu(OrtArenaAllocator, OrtMemTypeDefault);
    Ort::Value inputTensor = Ort::Value::CreateTensor<float>(memoryInfo, inputValues.data(), inputValues.size(), inputShape.data(), inputShape.size());

    const char* inputNames[] = {"images"};
    const char* outputNames[] = {"output0"};

    try {
        auto outputs = session->Run(Ort::RunOptions{nullptr}, inputNames, &inputTensor, 1, outputNames, 1);
        float* rawData = outputs[0].GetTensorMutableData<float>();
        auto shape = outputs[0].GetTensorTypeAndShapeInfo().GetShape();

        int rows = shape[1]; // 84 (4 coords + 80 clases)
        int cols = shape[2]; // 8400 predicciones

        // Vectores para almacenar candidatos ANTES del NMS
        std::vector<int> classIds;
        std::vector<float> confidences;
        std::vector<cv::Rect> boxes;

        // 3. RECOLECCIÓN DE CANDIDATOS
        for (int i = 0; i < cols; i++) {
            float maxScore = 0.0f;
            int maxClass = -1;

            // Buscar la mejor clase
            for (int c = 4; c < rows; c++) {
                float score = rawData[c * cols + i];
                if (score > maxScore) {
                    maxScore = score;
                    maxClass = c - 4;
                }
            }

            if (maxScore > CONFIDENCE_THRESHOLD) {
                float cx = rawData[0 * cols + i];
                float cy = rawData[1 * cols + i];
                float w = rawData[2 * cols + i];
                float h = rawData[3 * cols + i];

                // NMS de OpenCV necesita formato: Top-Left X, Top-Left Y, Width, Height
                int left = (int)(cx - w / 2);
                int top = (int)(cy - h / 2);
                int width = (int)w;
                int height = (int)h;

                classIds.push_back(maxClass);
                confidences.push_back(maxScore);
                boxes.push_back(cv::Rect(left, top, width, height));
            }
        }

        // 4. APLICAR NMS (La magia que elimina duplicados)
        std::vector<int> indices;
        // Esta función de OpenCV hace todo el trabajo matemático difícil
        cv::dnn::NMSBoxes(boxes, confidences, CONFIDENCE_THRESHOLD, NMS_THRESHOLD, indices);

        // 5. CONSTRUIR RESULTADO FINAL (Solo los ganadores)
        std::string result = "";
        for (int idx : indices) {
            cv::Rect box = boxes[idx];
            int cls = classIds[idx];
            float conf = confidences[idx];

            // Recuperamos el centro X e Y porque tu código Java ya está configurado para eso
            // (Mantenemos compatibilidad con tu MainActivity actual)
            float finalCx = box.x + box.width / 2.0f;
            float finalCy = box.y + box.height / 2.0f;
            float finalW = (float)box.width;
            float finalH = (float)box.height;

            result += std::to_string(cls) + "," + std::to_string(conf) + "," +
                      std::to_string(finalCx) + "," + std::to_string(finalCy) + "," +
                      std::to_string(finalW) + "," + std::to_string(finalH) + "|";
        }

        return env->NewStringUTF(result.c_str());

    } catch (const std::exception& e) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Error: %s", e.what());
        return env->NewStringUTF("");
    } catch (...) {
        return env->NewStringUTF("");
    }
}