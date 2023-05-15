package com.example.facerecognitionlibrary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.ByteArrayOutputStream

class FaceRecognitionLibrary {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun chaquopyTest(context: Context) {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
            Log.d("mylog", "Python start success")
        }

        val py = Python.getInstance()
        val myscript = py.getModule("myscript")
        val obj = myscript.callAttr("main")
        Log.d("mylog", "myscript.py 코드 호출 성공")
    }

    fun recognizeFaceTest(context: Context, byteArr: ByteArray) {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
            Log.d("mylog", "Python start success")
        }

        val bitmapFromByteArray: Bitmap = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
//        Log.d("mylog", "byteArr를 다시 Bitmap으로 변환했습니다.")

        val py = Python.getInstance()
        val myscript = py.getModule("recognizeFace")
        val bytesObj = py.builtins.callAttr("bytes", byteArr)
        val obj = myscript.callAttr("main", bytesObj)
//        val obj = myscript.callAttr("main")
        Log.d("mylog", "recognizeFace.py 코드 호출 성공")
        Log.d("myLog", "유클리드 거리: $obj")
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun getFaceVector(context: Context, byteArr: ByteArray): DoubleArray {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
            Log.d("mylog", "Python start success")
        }

        val bitmapFromByteArray: Bitmap = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
//        Log.d("mylog", "byteArr를 다시 Bitmap으로 변환했습니다.")

        val py = Python.getInstance()
        val myscript = py.getModule("getFaceVector")
        val bytesObj = py.builtins.callAttr("bytes", byteArr)
        val obj = myscript.callAttr("main", bytesObj)
//        val obj = myscript.callAttr("main")
        Log.d("mylog", "getFaceVector.py 코드 호출 성공")
        Log.d("myLog", "128차원 특징 벡터: $obj")

//        val byteArray: ByteArray = bitmapToByteArray(image)

        return obj.toJava(DoubleArray::class.java)
    }

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

    fun opencvTest(context: Context) {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))

        }

        val py = Python.getInstance()
        val myscript = py.getModule("opencv_human_detection")
        val obj = myscript.callAttr("main")
        Log.d("mylog", "파이썬 코드 호출 성공")
    }

    fun initPython(context: Context) {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
            Log.d("myLog", "Python start success in $context")
        }
    }


    /*
    fun cameraTest(context: Context, textureView: TextureView) {
        // CameraExecutor를 초기화
        cameraExecutor = Executors.newSingleThreadExecutor()

        // 카메라 프로바이더를 설정합니다.
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(Runnable {
            // 사용 가능한 카메라 프로바이더를 가져옵니다.
            val cameraProvider = cameraProviderFuture.get()

            // Preview 객체를 만듭니다.
            val preview = Preview.Builder()
                .build()
                .also {
                    // 화면에 미리보기를 보여줄 TextureView를 지정합니다.
                    it.setSurfaceProvider(textureView.surfaceProvider)
                }

            // 사용 가능한 후면 카메라를 선택합니다.
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // 카메라 프로바이더에서 사용 가능한 카메라의 영상을 가져옵니다.
                cameraProvider.bindToLifecycle(
                    /* lifecycleOwner= */ null,
                    cameraSelector,
                    preview
                )

                // 카메라 영상 처리를 위한 OpenCV 로딩을 수행합니다.
                CoroutineScope(Dispatchers.IO).launch {
                    loadOpenCv(context)
                }

                // Face Cascade Classifier를 로딩합니다.
                val faceCascadeClassifier = loadCascadeClassifier(context, R.raw.haarcascade_frontalface_default)

                // 각 프레임마다 얼굴 랜드마크를 검출합니다.
                val imageAnalyzer = androidx.camera.core.ImageAnalysis.Builder()
                    .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, FaceAnalyzer(faceCascadeClassifier, cameraExecutor))
                    }

                // 사용 가능한 카메라에서 프리뷰와 분석을 위한 세션을 생성합니다.
                val camera = cameraProvider.bindToLifecycle(
                    /* lifecycleOwner= */ null,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )

            } catch (exc: Exception) {
                Log.e(TAG, "카메라 세션을 만들지 못했습니다.", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    */
}