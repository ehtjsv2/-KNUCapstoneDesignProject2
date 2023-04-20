package com.example.facerecognitionmodule

import android.app.Activity
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat

import com.example.facerecognitionlibrary.FaceRecognitionLibrary

import com.example.facerecognitionmodule.databinding.ActivityMainBinding
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import org.opencv.android.Utils

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.impl.ImageAnalysisConfig
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Bitmap.Config.RGB_565
//import androidx.camera.core.internal.YuvToJpegProcessor
import java.io.ByteArrayOutputStream

import androidx.camera.*

class MainActivity : AppCompatActivity() {
    // ViewBinding
    lateinit private var binding: ActivityMainBinding

    private var grayView: ImageView? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageAnalysis: ImageAnalysis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        /* 카메라 사용 */
        startCamera()

        binding.cameraCaptureButton.setOnClickListener {
            takePhoto()
        }

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.grayscaleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.grayView.visibility = View.VISIBLE
            } else {
                binding.grayView.visibility = View.INVISIBLE
            }
        }


        /* library 사용 예시 */
//        val faceRecog = FaceRecognitionLibrary()
//        faceRecog.showToast(this, "test message")

        /* chaquopy 테스트 */
//        val test = FaceRecognitionLibrary()
//        test.chaquopyTest(this)

        /* image 변환 테스트 */
//        val libTest = FaceRecognitionLibrary()
//        val image: Bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.face1)
//        val byteArr: ByteArray = libTest.bitmapToByteArray(image)
//        Log.d("mylog", "byteArr를 생성했습니다.")
//        val bitmapFromByteArray: Bitmap = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
//        Log.d("mylog", "byteArr를 다시 Bitmap으로 변환했습니다.")

        /* openCV 테스트 */
//        val libTest = FaceRecognitionLibrary()
//        libTest.opencvTest(this)


    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            newJpgFileName()
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.d("CameraX-Debug", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
                    Log.d("CameraX-Debug", msg)
                }
            })
    }

    // viewFinder 설정 : Preview
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        class MyImageAnalyzer : ImageAnalysis.Analyzer {
            override fun analyze(image: ImageProxy) {
                // Image 처리 코드 작성
                Log.d("CameraX-Debug", "analyze: got the frame at: " + image.imageInfo.timestamp)

//                val buffer = image.planes[0].buffer
//                val bytes = ByteArray(buffer.capacity())
//                Log.d("CameraX-Debug", "Buffer remaining: ${buffer.remaining()}")
//                buffer.get(bytes)
//                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

//                val buffer = image.planes[0].buffer
//                var bytes = ByteArray(buffer.remaining())
//                buffer.get(bytes)
//                val yuvImage = YuvImage(bytes, image.format, image.width, image.height, null)
//                val outputStream = ByteArrayOutputStream()
//                yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, outputStream)
//                val jpegArray = outputStream.toByteArray()
//                val bmp = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.size)

                val yBuffer = image.planes[0].buffer // Y
                val uBuffer = image.planes[1].buffer // U
                val vBuffer = image.planes[2].buffer // V

                val ySize = yBuffer.remaining()
                val uSize = uBuffer.remaining()
                val vSize = vBuffer.remaining()

                val nv21 = ByteArray(ySize + uSize + vSize)

                yBuffer.get(nv21, 0, ySize)
                vBuffer.get(nv21, ySize, vSize)
                uBuffer.get(nv21, ySize + vSize, uSize)

                val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)

                val out = ByteArrayOutputStream()
                yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)

                val imageBytes = out.toByteArray()
                val bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)


                if (bmp == null) {
                    Log.e(
                        "CameraX-Debug", "Failed to decode byte array into a Bitmap. " +
                                "Bytes size: ${nv21.size}, image format: ${image.format}, " +
                                "image dimensions: ${image.width} x ${image.height}."
                    )
                    return
                }

                if (bmp != null) {
                    // 이미지를 grayscale로 변환
                    val grayBitmap =
                        Bitmap.createBitmap(bmp.width, bmp.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(grayBitmap)
                    val paint = Paint()
                    val cm = ColorMatrix(
                        floatArrayOf(
                            0.33f, 0.33f, 0.33f, 0f, 0f,
                            0.33f, 0.33f, 0.33f, 0f, 0f,
                            0.33f, 0.33f, 0.33f, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    paint.colorFilter = ColorMatrixColorFilter(cm)
                    canvas.drawBitmap(bmp, 0f, 0f, paint)

                    // grayscale 이미지를 grayView에 적용
                    runOnUiThread {
                        binding.grayView.setImageBitmap(grayBitmap)
                    }
                }

                image.close()

            }
        }

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // ImageCapture
            imageCapture = ImageCapture.Builder()
                .build()

            // ImageAnalysis
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .apply {
                    setAnalyzer(ContextCompat.getMainExecutor(this@MainActivity), MyImageAnalyzer())
                }

            // Select back camera as a default
            // val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalysis
                )

            } catch (exc: Exception) {
                Log.d("CameraX-Debug", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }


    private fun newJpgFileName(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.jpg"
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir
        else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}