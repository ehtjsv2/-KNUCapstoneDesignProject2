package com.example.facerecognitionlibrary

import android.content.Context
import android.graphics.*
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CoroutineScope
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Abstract class representing a face recognition library.
 * To use this library, you need to implement the abstract function `reachSeletedCount()`, which defines the action to
 * take when face extraction is successful.
 *
 * @param context The context of the Android application.
 * @param activity The reference to the current activity or AppCompatActivity.
 * @param threshold The threshold value for face detection count. Default value is 10.
 * @constructor Creates an instance of the FaceRecognitionLibrary.
 * @see bitmapToByteArray
 * @see getFaceVector
 * @see getFaceDistance
 * @see startCamera
 * @see reachSeletedCount
 */
abstract class FaceRecognitionLibrary(
    private val context: Context,
    private val activity: AppCompatActivity,
    private val threshold: Int = 10
) {
    private var count = 0
    private var enable = false
    private lateinit var byteArr: ByteArray


    /**
     * Convert Bitmap to ByteArray. return converted `ByteArray`
     * The converted ByteArray data may be transferred as a factor of a `getFaceVector()` function.
     *
     * @param bitmap Bitmap data to convert to ByteArray.
     * @return converted ByteArray
     * @see getFaceVector
     * */
    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    /**
     * Extract the face feature vector from the passed `ByteArray` data.
     * return face feature vector as a `DoubleArray`, or `null` if the extraction fails.
     * The ByteArray data must contain a face image.
     * @param context The context of the Android application.
     * @param byteArr The ByteArray data containing the face image.
     * @return The extracted face feature vector as a `DoubleArray`, or `null` if the extraction fails.
     * @see bitmapToByteArray
     */
    fun getFaceVector(context: Context, byteArr: ByteArray): DoubleArray? {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
            Log.d("mylog", "Python start success")
        }

//        val bitmapFromByteArray: Bitmap = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
//        Log.d("mylog", "byteArr를 다시 Bitmap으로 변환했습니다.")

        val py = Python.getInstance()
        val myscript = py.getModule("getFaceVector")
        val bytesObj = py.builtins.callAttr("bytes", byteArr)
        val obj = myscript.callAttr("main", bytesObj)
//        val obj = myscript.callAttr("main")
        Log.d("mylog", "getFaceVector.py 코드 호출 성공")
        Log.d("myLog", "128차원 특징 벡터: $obj")

//        val byteArray: ByteArray = bitmapToByteArray(image)

        return obj?.toJava(DoubleArray::class.java)
    }

    /**
     * Calculates the distance between two face feature vectors.
     * return The distance between the two face feature vectors as `Double`.
     * Each vector value can be obtained by the `getFaceVector()` function.
     * @param vector1 The first face feature vector.
     * @param vector2 The second face feature vector.
     * @return The distance between the two face feature vectors.
     * @throws IllegalArgumentException if the dimensions of the input vectors are not equal to 128.
     * @see getFaceVector
     */
    fun getFaceDistance(vector1: DoubleArray, vector2: DoubleArray): Double {
        require(vector1.size == vector2.size && vector1.size == 128) {
            "Vector dimensions should be 128."
        }

        var sum = 0.0
        for (i in vector1.indices) {
            val diff = vector1[i] - vector2[i]
            sum += diff * diff
        }

        return kotlin.math.sqrt(sum)
    }

    /**
     * Function for module initialization. You must call this before using the `getFaceVector()` function.
     * @param context The context of the Android application.
     * @param activity The reference to the current activity or AppCompatActivity.
     * @see getFaceVector
     */
    fun settingModule(context: Context, activity: AppCompatActivity) {
        val image: Bitmap = BitmapFactory.decodeResource(activity.resources, R.raw.dohun003)
        val byteArr = bitmapToByteArray(image)
        Log.d("myLog", "byteArr를 생성했습니다.")

        getFaceVector(context, byteArr)
    }

    /**
     * Starts the camera with the specified configuration.
     * @param context The context of the Android application.
     * @param activity The reference to the current activity or AppCompatActivity.
     * @param surfaceProvider The SurfaceProvider for displaying the camera preview.
     * @param messageView The TextView to display messages or status updates.
     */
    fun startCamera(
        context: Context, activity: AppCompatActivity,
        surfaceProvider: SurfaceProvider,
        messageView: TextView
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        class MyImageAnalyzer : ImageAnalysis.Analyzer {
            private var lastAnalyzedTimestamp = 0L // 1초 단위로 프레임 처리하기위한 변수
            override fun analyze(image: ImageProxy) {
                val currentTime = System.currentTimeMillis()
                val elapsedTime = currentTime - lastAnalyzedTimestamp
                if (elapsedTime >= 1000) {
                    // 프레임 처리 로직 작성
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
                            "CameraX-Debug",
                            "Failed to decode byte array into a Bitmap. " + "Bytes size: ${nv21.size}, image format: ${image.format}, " + "image dimensions: ${image.width} x ${image.height}."
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

                    // Rotate RGB Mat
                    val rotatedMat = Mat()
                    Core.rotate(rgbMat, rotatedMat, Core.ROTATE_90_COUNTERCLOCKWISE)

                    // Convert Mat to Bitmap
                    val bmp2 =
                        Bitmap.createBitmap(rgbMat.cols(), rgbMat.rows(), Bitmap.Config.ARGB_8888)
                    Utils.matToBitmap(rgbMat, bmp2)

                    // *회전 방향 확인
                    val display = activity.windowManager.defaultDisplay
                    val rotation = display.rotation


                    // Load cascade classifier file
                    val cascadeFile =
                        File(context.cacheDir, "haarcascade_frontalface_alt.xml")
                    val inputStream =
                        context.resources.openRawResource(R.raw.haarcascade_frontalface_alt)
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
                    Imgproc.cvtColor(rotatedMat, grayMat, Imgproc.COLOR_RGB2GRAY)
                    val graybmp =
                        Bitmap.createBitmap(grayMat.cols(), grayMat.rows(), Bitmap.Config.ARGB_8888)
                    Utils.matToBitmap(grayMat, graybmp)

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
                        messageView.post {
                            messageView.text = "얼굴 탐지 중입니다. $count"
                        }

                    } else if (count > 0) {
                        count = 0
                        Log.d("myLog", "${System.currentTimeMillis()}: count 초기화. $count")
                        messageView.post {
                            messageView.text = "카메라를 정면으로 바라보세요."
                        }
                    }
                    // 이미지를 90도 회전
                    val matrix = Matrix()
                    matrix.postRotate(-90f) // 90도 회전
                    val rotatedBitmap = Bitmap.createBitmap(
                        bmp, 0, 0, bmp.width, bmp.height, matrix, true
                    )

                    if (numFaces > 0 && count > threshold) { // count 가 임계값 넘었을 때
                        count = 0;
                        //takePhoto()
//                    binding.grayscaleSwitch.isChecked = false;
                        enable = true

                        byteArr = bitmapToByteArray(rotatedBitmap)
                        Log.d("myLog", "byteArr를 생성했습니다.")
                        val bitmapFromByteArray: Bitmap =
                            BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
//                    Log.d("mylog", "byteArr를 다시 Bitmap으로 변환했습니다.")
                        val new_vector = getFaceVector(context, byteArr)
//                        Log.d("myLog", "벡터를 생성했습니다.")

                        if (new_vector != null) {
                            reachSeletedCount(new_vector)
                        }


                    }
                    lastAnalyzedTimestamp = currentTime
                }
                image.close()
            }
        }

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            var preview = Preview.Builder().build().also {
                it.setSurfaceProvider(surfaceProvider)
            }

            // ImageCapture
            val imageCapture = ImageCapture.Builder().build()

            // ImageAnalysis
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().apply {
                    setAnalyzer(ContextCompat.getMainExecutor(context), MyImageAnalyzer())
                }

            // Select back camera as a default
            // val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    activity, cameraSelector, preview, imageCapture, imageAnalysis
                )

            } catch (exc: Exception) {
                Log.d("CameraX-Debug", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }


    /**
     * Checks if the similarity score between two face feature vectors indicates the same user.
     * return `true` if the similarity score is below the similarity threshold, indicating the same user. Otherwise, returns `false`.
     * @param similarity The similarity score between the two face feature vectors.
     * @param similarityThreshold The threshold value for considering two vectors as the same user.
     * Default value is 0.4.
     * @return `true` if the similarity score is below the similarity threshold, indicating the same user. Otherwise, returns `false`.
     */
    fun isSameUser(similarity: Double, similarityThreshold: Double = 0.4): Boolean {
        return similarity < similarityThreshold
    }

    /**
     * Abstract function that defines the action to perform when face extraction is successful.
     * You must implement this function in order to use the library.
     * @param vector Face feature vector extracted by `getFaceVector()` function
     * @see getFaceVector
     * @see startCamera
     */
    abstract fun reachSeletedCount(vector: DoubleArray)

}