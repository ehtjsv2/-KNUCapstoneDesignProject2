package com.example.facerecognitionmodule

import android.app.Activity
import android.content.Context
import android.database.Observable
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
import androidx.core.content.ContentProviderCompat.requireContext
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import java.io.FileOutputStream
import org.opencv.core.Point

class MainActivity : AppCompatActivity() {
    // ViewBinding
    lateinit private var binding: ActivityMainBinding

    private var grayView: ImageView? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageAnalysis: ImageAnalysis

    private lateinit var byteArr: ByteArray
    private var isPhotoTaken = false
    private var count = 0


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
        val libTest = FaceRecognitionLibrary()
        val image: Bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.dohun006)
        byteArr = libTest.bitmapToByteArray(image)
        Log.d("mylog", "byteArr를 생성했습니다.")

        libTest.recognizeFace(this, byteArr)

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
                    Log.d("myLog", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)

                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
                    Log.d("myLog", msg)

                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    val libTest = FaceRecognitionLibrary()
                    byteArr = libTest.bitmapToByteArray(bitmap)
                    Log.d("mylog", "byteArr를 생성했습니다.")
                    val bitmapFromByteArray: Bitmap =
                        BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
//                    Log.d("mylog", "byteArr를 다시 Bitmap으로 변환했습니다.")
                    libTest.recognizeFace(baseContext, byteArr)
                    count = 0
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

                val format = image.format // 35(YUV_420_888)

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

                OpenCVLoader.initDebug();

                // Create Mat object
                val yuvMat = Mat(image.height + image.height / 2, image.width, CvType.CV_8UC1)
                yuvMat.put(0, 0, nv21)

                // Convert YUV to RGB
                val rgbMat = Mat(image.height, image.width, CvType.CV_8UC3)
                Imgproc.cvtColor(yuvMat, rgbMat, Imgproc.COLOR_YUV2RGB_NV21)

                // Convert Mat to Bitmap
                val bmp2 =
                    Bitmap.createBitmap(rgbMat.cols(), rgbMat.rows(), Bitmap.Config.ARGB_8888)
                Utils.matToBitmap(rgbMat, bmp2)

                // *회전 방향 확인
                val display = windowManager.defaultDisplay
                val rotation = display.rotation
//                Log.d("myLog", "Device orientation: $rotation")

                // rotate image
//                val bmpMat = Mat()
//                Utils.bitmapToMat(bmp2, bmpMat)
//
//                // 회전 매트릭스 생성
//                val center = Point(bmp2.width / 2.0, bmp2.height / 2.0)
//                val angle = 90.0
//                val scale = 1.0
//                val rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, scale)
//
//                // 이미지 회전
//                val rotatedMat = Mat()
//                Imgproc.warpAffine(bmpMat, rotatedMat, rotationMatrix, bmpMat.size())
//
//                // 회전된 이미지를 다시 Bitmap으로 변환하여 적용
//                Utils.matToBitmap(rotatedMat, bmp2)

                // Load cascade classifier file
                val cascadeFile =
                    File(applicationContext.cacheDir, "haarcascade_frontalface_alt.xml")
                val inputStream = resources.openRawResource(R.raw.haarcascade_frontalface_alt)
                val outputStream = FileOutputStream(cascadeFile)

                // 파일 복사
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }

                // 파일 로드
                val cascadeClassifier = CascadeClassifier(cascadeFile.absolutePath)

                // grayscale 매트릭스로 변환
                val grayMat = Mat()
                Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY)
                val graybmp =
                    Bitmap.createBitmap(grayMat.cols(), grayMat.rows(), Bitmap.Config.ARGB_8888)
                Utils.matToBitmap(grayMat, graybmp)


//                val bmpMat = Mat()
//                Utils.bitmapToMat(graybmp, bmpMat)
//
//                // 회전 매트릭스 생성
//                val center = Point(graybmp.width / 2.0, graybmp.height / 2.0)
//                val angle = 180.0
//                val scale = 1.0
//                val rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, scale)
//
//                // 이미지 회전
//                val rotatedMat = Mat()
//                Imgproc.warpAffine(bmpMat, rotatedMat, rotationMatrix, bmpMat.size())
//
//                // 회전된 이미지를 다시 Bitmap으로 변환하여 적용
//                Utils.matToBitmap(rotatedMat, graybmp)


                // Detect faces
                val faces = MatOfRect()
//                cascadeClassifier.detectMultiScale(rgbMat, faces)
                cascadeClassifier.detectMultiScale(grayMat, faces)

                var numFaces = faces.toArray().size
//                Log.d("myLog", "Number of detected faces: $numFaces")

                // Draw rectangles on bitmap for detected faces
                val canvas = Canvas(graybmp)
                faces.toList().forEach { face ->
                    val rect = Rect(face.x, face.y, face.x + face.width, face.y + face.height)
                    canvas.drawRect(rect, Paint().apply {
                        color = Color.RED
                        strokeWidth = 5f
                        style = Paint.Style.STROKE
                    })
                }

                if (numFaces > 0) {
                    count++
                    Log.d("myLog", "${System.currentTimeMillis()}: 얼굴이 탐지되었습니다. $count")
                } else if (count > 0) {
                    count = 0
                    Log.d("myLog", "${System.currentTimeMillis()}: count 초기화. $count")
                }

                if (numFaces > 0 && binding.grayscaleSwitch.isChecked && count > 10) {
                    takePhoto()
                    count = 0
                    Log.d("myLog", "${System.currentTimeMillis()}: takePhoto() 호출됨")
                }

                // Display the bitmap or do further processing with it
                runOnUiThread {
                    binding.grayView.setImageBitmap(graybmp)
                }


//                if (bmp != null) {
//                    // 이미지를 grayscale로 변환
//                    val grayBitmap =
//                        Bitmap.createBitmap(bmp.width, bmp.height, Bitmap.Config.ARGB_8888)
//                    val canvas = Canvas(grayBitmap)
//                    val paint = Paint()
//                    val cm = ColorMatrix(
//                        floatArrayOf(
//                            0.33f, 0.33f, 0.33f, 0f, 0f,
//                            0.33f, 0.33f, 0.33f, 0f, 0f,
//                            0.33f, 0.33f, 0.33f, 0f, 0f,
//                            0f, 0f, 0f, 1f, 0f
//                        )
//                    )
//                    paint.colorFilter = ColorMatrixColorFilter(cm)
//                    canvas.drawBitmap(bmp, 0f, 0f, paint)
//
//                    // grayscale 이미지를 grayView에 적용
//                    runOnUiThread {
//                        binding.grayView.setImageBitmap(grayBitmap)
//                    }
//                }

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