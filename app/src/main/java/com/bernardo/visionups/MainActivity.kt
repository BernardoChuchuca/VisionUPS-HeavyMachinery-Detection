package com.bernardo.visionups

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.bernardo.visionups.databinding.ActivityMainBinding
import java.nio.ByteBuffer
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var isProcessing = false // Semáforo para no saturar

    // Nombres de las 80 clases de COCO (YOLO estándar)
    private val classNames = listOf(
        "Dump Truck",       // 0 - Volqueta
        "Excavator",        // 1 - Excavadora
        "Front End Loader", // 2 - Cargadora Frontal
        "Gloves ON",        // 3 - Guantes puestos
        "Gloves-OFF",       // 4 - Sin guantes (¡Alerta de seguridad!)
        "Hard Hat OFF",     // 5 - Sin casco
        "Hard Hat ON",      // 6 - Con casco
        "Ladder",           // 7 - Escalera
        "Safety Vest OFF",  // 8 - Sin chaleco
        "Safety Vest ON",   // 9 - Con chaleco
        "Skid Steer",       // 10 - Minicargadora (Bobcat)
        "Tractor Trailer",  // 11 - Trailer
        "Trailer",          // 12 - Remolque
        "Vehicle",          // 13 - Vehículo genérico
        "Worker"            // 14 - Trabajador
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initYolo()
        } else {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) initYolo() else Toast.makeText(this, "Sin permisos no funciono :(", Toast.LENGTH_LONG).show()
            }.launch(Manifest.permission.CAMERA)
        }
    }

    private fun initYolo() {
        // 1. Cargar el modelo desde C++
        if (initModel(assets)) {
            Toast.makeText(this, "IA Cargada Correctamente", Toast.LENGTH_SHORT).show()
            binding.tvDebug.text = "IA Lista. Buscando objetos..."
            startCamera()
        } else {
            binding.tvDebug.text = "ERROR: No se encontró yolov8n.onnx"
            Toast.makeText(this, "Falta el archivo yolov8n.onnx en assets", Toast.LENGTH_LONG).show()
        }
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val cameraProvider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(binding.viewFinder.surfaceProvider) }

            val analyzer = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            // VARIABLES PARA FPS
            var lastTime = System.currentTimeMillis()
            var frameCount = 0
            var fps = 0.0

            analyzer.setAnalyzer(cameraExecutor) { image ->
                if (isProcessing) {
                    image.close()
                    return@setAnalyzer
                }
                isProcessing = true

                // CALCULO DE FPS
                val currentTime = System.currentTimeMillis()
                frameCount++
                if (currentTime - lastTime >= 1000) { // Si pasó 1 segundo
                    fps = frameCount.toDouble()
                    frameCount = 0
                    lastTime = currentTime
                }

                try {
                    val buffer = image.planes[0].buffer
                    val resultString = detect(buffer, image.width, image.height, image.planes[0].rowStride)
                    val detections = parseResults(resultString)

                    runOnUiThread {
                        binding.overlay.setResults(detections)

                        // ACTUALIZAR TEXTO CON FPS
                        // Agregamos el dato de FPS al texto de depuración
                        binding.tvDebug.text = "FPS: ${fps.toInt()} | Objetos: ${detections.size}"

                        // Opcional: Cambiar color si los FPS son bajos (menos de 10 es lento)
                        if (fps < 10) binding.tvDebug.setTextColor(android.graphics.Color.RED)
                        else binding.tvDebug.setTextColor(android.graphics.Color.GREEN)

                        isProcessing = false
                    }
                } catch (e: Exception) {
                    isProcessing = false
                } finally {
                    image.close()
                }
            }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analyzer)

        }, ContextCompat.getMainExecutor(this))
    }

    private fun parseResults(data: String): List<OverlayView.DetectionResult> {
        val list = ArrayList<OverlayView.DetectionResult>()
        if (data.isEmpty()) return list

        val scaleX = binding.overlay.width.toFloat() / 320f
        val scaleY = binding.overlay.height.toFloat() / 320f

        data.split("|").filter { it.isNotEmpty() }.forEach {
            val v = it.split(",")
            if (v.size == 6) {
                val cls = v[0].toInt()
                val score = v[1].toFloat()
                val x = v[2].toFloat(); val y = v[3].toFloat()
                val w = v[4].toFloat(); val h = v[5].toFloat()

                val rect = RectF(
                    (x - w/2) * scaleX, (y - h/2) * scaleY,
                    (x + w/2) * scaleX, (y + h/2) * scaleY
                )

                val label = if (cls in classNames.indices) classNames[cls] else "Obj"
                list.add(OverlayView.DetectionResult(rect, "$label ${(score*100).toInt()}%"))
            }
        }
        return list
    }

    // Funciones Nativas
    external fun initModel(mgr: android.content.res.AssetManager): Boolean
    external fun detect(buffer: ByteBuffer, w: Int, h: Int, stride: Int): String

    companion object { init { System.loadLibrary("visionups") } }
}