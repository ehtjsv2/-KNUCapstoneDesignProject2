package com.example.facerecognitionmodule

//import androidx.camera.core.internal.YuvToJpegProcessor

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.facerecognitionlibrary.FaceRecognitionLibrary
import com.example.facerecognitionmodule.databinding.ActivityMainBinding
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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    //DB생성
//    val db=DB(this)
//    val isCreate = db.createTable()
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
    private var enable = false
    private var test_var = false

    private lateinit var textViewMessage: TextView
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    companion object {
        private const val REQUEST_CODE_GENERAL_ORDER = 1
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val generalOrderButton: Button = findViewById(R.id.general_Order_Button)
        val simpleOrderButton: Button = findViewById(R.id.simple_Order_Button)
        //generalOrderButton.setOnClickListener {
        //    val intent = Intent(this, general_order_activity::class.java)
        //   startActivity(intent)
        // }

        generalOrderButton.setBackgroundColor(Color.GRAY)
        generalOrderButton.setOnClickListener(null)

        simpleOrderButton.setBackgroundColor(Color.GRAY)
        simpleOrderButton.setOnClickListener(null)
        /* 카메라 사용 */
        startCamera()

        binding.cameraCaptureButton.setOnClickListener {
            takePhoto()
        }

       // if (enable) {
        //    generalOrderButton.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))
         //   generalOrderButton.setOnClickListener {
         //       val intent = Intent(this, general_order_activity::class.java)
          //      startActivity(intent)
          //  }
        //} else {
        //    generalOrderButton.setBackgroundColor(Color.GRAY)
        //    generalOrderButton.setOnClickListener(null)
        //}
        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()


        //binding.generalOrderButton.setBackgroundResource(android.R.drawable.btn_default)


        binding.grayscaleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.grayView.visibility = View.VISIBLE
            } else {
                binding.grayView.visibility = View.INVISIBLE
            }
        }

        if(test_var){
            //stopCameraPreview()
            //displayTextOnCameraPreview()
        }

        /* image 변환 테스트 */
        val libTest = FaceRecognitionLibrary()
        val image: Bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.dohun006)
        byteArr = libTest.bitmapToByteArray(image)
        Log.d("mylog", "byteArr를 생성했습니다.")

        libTest.getFaceVector(this, byteArr)
    }


    private fun stopCameraPreview() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // 이미지 분석 및 카메라 프리뷰 중지
            cameraProvider.unbindAll()

            runOnUiThread {
                // 카메라 화면을 제거하기 위해 PreviewView를 숨기거나 제거
                binding.viewFinder.visibility = View.GONE

                // 액티비티 배경색을 흰색으로 설정
                window.decorView.setBackgroundColor(Color.WHITE)

                // 'ID-001 생성' 문구를 화면 중앙에 계속 표시하는 코드
                val toast = Toast.makeText(this, "ID-001 생성", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                val toastView = toast.view
                val toastMessage = toastView?.findViewById<TextView>(android.R.id.message)
                toastMessage?.gravity = Gravity.CENTER
                toast.show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_GENERAL_ORDER && resultCode == Activity.RESULT_OK) {
            val selectedDrinks = data?.getStringArrayListExtra("selectedDrinks")

            // 선택된 음료 정보를 처리하는 로직을 여기에 구현합니다.
            // 예를 들어, 주문 처리 등의 작업을 수행할 수 있습니다.
        }
    } //전달된 음료 받아오기

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
                    libTest.getFaceVector(baseContext, byteArr)
                    count = 0
                }
            })
    }


    private fun takePhotoAndFinish() {
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
                    libTest.getFaceVector(baseContext, byteArr)
                    count = 0

                    stopCameraPreview()

                    finish()
                }
            })
    }
    // viewFinder 설정 : Preview
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val simpleOrderButton = findViewById<Button>(R.id.simple_Order_Button)
        val generalOrderButton = findViewById<Button>(R.id.general_Order_Button)


        class MyImageAnalyzer : ImageAnalysis.Analyzer {
            override fun analyze(image: ImageProxy) {
                // Image 처리 코드 작성
                Log.d("CameraX-Debug", "analyze: got the frame at: " + image.imageInfo.timestamp)

                val format = image.format // 35(YUV_420_888)

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

                // Rotate RGB Mat
                val rotatedMat = Mat()
                Core.rotate(rgbMat, rotatedMat, Core.ROTATE_90_COUNTERCLOCKWISE)

                // Convert Mat to Bitmap
                val bmp2 =
                    Bitmap.createBitmap(rgbMat.cols(), rgbMat.rows(), Bitmap.Config.ARGB_8888)
                Utils.matToBitmap(rgbMat, bmp2)

                // *회전 방향 확인
                val display = windowManager.defaultDisplay
                val rotation = display.rotation

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
                } else if (count > 0) {
                    count = 0
                    Log.d("myLog", "${System.currentTimeMillis()}: count 초기화. $count")
                }

                if (numFaces > 0 && binding.grayscaleSwitch.isChecked() && count > 10) {
                    count = 0;
                    //takePhoto()
                    binding.grayscaleSwitch.isChecked = false;
                    enable = true
                    Log.d("myLog", "${System.currentTimeMillis()}: takePhoto() 호출됨");
                }

                runOnUiThread {
                    if (enable) {
                        generalOrderButton.setBackgroundColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.purple_500
                            )
                        )
                        generalOrderButton.setOnClickListener {
                            val intent = Intent(this@MainActivity, general_order_activity::class.java)
                            startActivity(intent)
                        }
                       simpleOrderButton.setBackgroundColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.purple_500
                            )
                        )
                        stopCameraPreview()
                    }
                }

                // Rotate the bitmap by 90 degrees
                val matrix = Matrix()
                matrix.postRotate(360f)

// Create a new rotated bitmap
                val rotatedBitmap = Bitmap.createBitmap(graybmp, 0, 0, graybmp.width, graybmp.height, matrix, true)


                // Display the bitmap or do further processing with it
                runOnUiThread {
                    binding.grayView.setImageBitmap(rotatedBitmap)
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
        //val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        //val filename = sdf.format(System.currentTimeMillis())
        //return "${filename}.jpg"
        return "guest.jpg"
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
