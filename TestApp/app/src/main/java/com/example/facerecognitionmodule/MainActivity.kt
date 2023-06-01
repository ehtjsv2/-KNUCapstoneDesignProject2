package com.example.facerecognitionmodule

//import androidx.camera.core.internal.YuvToJpegProcessor

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.facerecognitionlibrary.FaceRecognitionLibrary
import com.example.facerecognitionmodule.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.io.File
import java.lang.Runnable
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
    private lateinit var matchedIDView: TextView
    private lateinit var createdIDView: TextView
    private lateinit var messageView: TextView
    private lateinit var generalOrderButton: Button
    private lateinit var simpleOrderButton: Button
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var libTest: FaceRecognitionLibrary

    private lateinit var db: DB
    private var new_id: String? = null
    var closestUser: User? = null

    var closestDistance = Double.MAX_VALUE

    // 타이머와 핸들러 선언
    private var buttonTimer: Runnable? = null
    private val timerHandler = Handler(Looper.getMainLooper())

    // 버튼 활성화 시간 (단위: 밀리초)
    private val BUTTON_TIMEOUT = 5000L
//    private var isCameraRunning = false

    private val isCameraRunning = MutableLiveData<Boolean>()
    private var cameraJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        /* DB 생성 */
//        val db = DB(this)
        db = DB(this)
        val isCreate = db.createTable()
        Log.d("dbTest", "[MainActivity]  isCreate = $isCreate")

        /* sizeOfUser 테스트 */
//        val size = db.sizeOfUser()
//        Log.d("dbTest", "[MainActivity]  size = $size")

        /* selectAllUser 테스트 */
        val list = db.selectAllUser()
        for (user in list) {
            Log.d("dbTest", "[MainActivity]  userId = ${user.ID} , vector[0] = ${user.vector[0]}")
        }

        generalOrderButton = binding.generalOrderButton
        simpleOrderButton = binding.simpleOrderButton
        matchedIDView = binding.matchedIDView
        createdIDView = binding.createdIDView
        messageView = binding.messageView

        generalOrderButton.isEnabled = false
        simpleOrderButton.isEnabled = false



        class FaceRecognitionLibrary2 : FaceRecognitionLibrary(this, this, 5) {
            override fun reachSeletedCount(vector: DoubleArray) {
                Log.d("myLog", "count가 임계값에 도달했음. vector는 얼굴벡터를 담고있음")
                // 벡터 비교 수행
                val list = db.selectAllUser()
                closestDistance = Double.MAX_VALUE
                closestUser = null

                for (user in list) {
                    val distance = libTest.getFaceDistance(vector, user.vector)

                    if (distance < closestDistance) {
                        closestUser = user
                        closestDistance = distance
                    }
                }

                Log.d("myLog", "closestDistance: $closestDistance")

                /* 매칭되는 ID가 있는 경우 */
                if (libTest.isSameUser(closestDistance)) {

                    runOnUiThread {
                        // 카메라 프리뷰 작동 중지
                        stopCameraPreview()
                        messageView.visibility = View.INVISIBLE

                        // "ID-XXX님 어서오세요" 텍스트를 상단에 표시
                        matchedIDView.text = "${closestUser?.ID}님 어서오세요."
                        matchedIDView.visibility = View.VISIBLE

                        // 일반 주문, 간단 주문 활성화
                        generalOrderButton.isEnabled = true
                        simpleOrderButton.isEnabled = true

//                            generalOrderButton.setBackgroundColor(
//                                ContextCompat.getColor(
//                                    this@MainActivity,
//                                    R.color.purple_500
//                                )
//                            )

                        generalOrderButton.setOnClickListener {
                            val intent = Intent(
                                this@MainActivity, general_order_activity::class.java
                            )
                            intent.putExtra("id", "${closestUser?.ID}")
                            startActivity(intent)
                        }

                        simpleOrderButton.setOnClickListener {
                            val intent = Intent(
                                this@MainActivity, simple_order_activity::class.java
                            )
                            intent.putExtra("id", "${closestUser?.ID}")
                            startActivity(intent)
                        }

//                            simpleOrderButton.setBackgroundColor(
//                                ContextCompat.getColor(
//                                    this@MainActivity,
//                                    R.color.purple_500
//                                )
//                            )

                        // 일정 시간 후에 카메라 작동 및 버튼 비활성화
//                            timerHandler.postDelayed({
//                                startCamera()
//                                generalOrderButton.isEnabled = false
//                                simpleOrderButton.isEnabled = false
//                            }, BUTTON_TIMEOUT)
                    }

                }
                /* 매칭되는 ID가 없는 경우 */
                else {
                    // 새로운 ID 생성
                    new_id = db.createID(vector)

                    runOnUiThread {
                        // 카메라 프리뷰 작동 중지
                        stopCameraPreview()
                        messageView.visibility = View.INVISIBLE

                        // "ID-XXX 생성" 텍스트를 카메라 프리뷰에 출력
                        createdIDView.text = "${new_id} 생성"
                        createdIDView.visibility = View.VISIBLE

                        // 일반 주문 활성화
                        generalOrderButton.isEnabled = true

                        generalOrderButton.setOnClickListener {
                            val intent = Intent(
                                this@MainActivity, general_order_activity::class.java
                            )
                            intent.putExtra("id", "$new_id")
                            startActivity(intent)
                        }

                    }

                }
            }

        }
        /* image 변환 테스트 */
        libTest = FaceRecognitionLibrary2()

        libTest.settingModule(this, this)
        /* 카메라 사용 */
        libTest.startCamera(this, this, binding.viewFinder.surfaceProvider, binding.messageView)

        // LiveData를 관찰하여 값의 변경을 감지하는 Observer 생성

        val cameraRunningObserver = Observer<Boolean> { isRunning ->
            Log.d("myLog", "isCameraRunning 변경 감지: $isRunning")
            // isCameraRunning 값이 변경되었을 때 수행할 동작을 여기에 작성
//            if (isRunning) {
//                // 카메라가 실행 중인 경우
//                // ...
//            } else {
//                // 카메라가 실행 중이 아닌 경우
//                // ...
//            }

//            cameraJob?.cancel()

            if (!isRunning) {
                cameraJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(10000) // 10초 동안 대기
                    generalOrderButton.isEnabled = false
                    simpleOrderButton.isEnabled = false
                    libTest.startCamera(baseContext, this@MainActivity, binding.viewFinder.surfaceProvider, binding.messageView)
                    runOnUiThread {
                        binding.createdIDView.visibility = View.INVISIBLE
                        binding.matchedIDView.visibility = View.INVISIBLE
                        binding.viewFinder.visibility = View.VISIBLE
                        messageView.text = "카메라를 정면으로 바라보세요."
                        messageView.visibility = View.VISIBLE
                    }
                    Log.d("myLog", "카메라 재시작")
                }
            }
        }
        // Observer를 LiveData에 연결

        isCameraRunning.observe(this, cameraRunningObserver)
        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()


    }


    private fun stopCameraPreview() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // 이미지 분석 및 카메라 프리뷰 중지
            cameraProvider.unbindAll()
//            isCameraRunning = false
            isCameraRunning.value = false

            runOnUiThread {
                // 카메라 화면을 제거하기 위해 PreviewView를 숨기거나 제거
                binding.viewFinder.visibility = View.GONE

                // 액티비티 배경색을 흰색으로 설정
//                window.decorView.setBackgroundColor(Color.WHITE)

//                // 'ID-001 생성' 문구를 화면 중앙에 계속 표시하는 코드
//                val toast = Toast.makeText(this, "${closestUser?.ID}. 생성", Toast.LENGTH_SHORT)
//                toast.setGravity(Gravity.CENTER, 0, 0)
//                val toastView = toast.view
//                val toastMessage = toastView?.findViewById<TextView>(android.R.id.message)
//                toastMessage?.gravity = Gravity.CENTER
//                toast.show()
            }
        }, ContextCompat.getMainExecutor(this))
    }


    /*
    // viewFinder 설정 : Preview
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//        val simpleOrderButton = findViewById<Button>(R.id.simple_Order_Button)
//        val generalOrderButton = findViewById<Button>(R.id.general_Order_Button)

//        isCameraRunning = true
        isCameraRunning.value = true

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
                    runOnUiThread {
                        messageView.text = "얼굴 탐지 중입니다. $count"
                    }

                } else if (count > 0) {
                    count = 0
                    Log.d("myLog", "${System.currentTimeMillis()}: count 초기화. $count")
                    runOnUiThread {
                        messageView.text = "카메라를 정면으로 바라보세요."
                    }
                }

                // 이미지를 90도 회전
                val matrix = Matrix()
                matrix.postRotate(-90f) // 90도 회전
                val rotatedBitmap = Bitmap.createBitmap(
                    bmp, 0, 0, bmp.width, bmp.height, matrix, true
                )

//                if (numFaces > 0 && binding.grayscaleSwitch.isChecked() && count > 10) {
                if (numFaces > 0 && count > 10) {
                    count = 0;
                    //takePhoto()
//                    binding.grayscaleSwitch.isChecked = false;
                    enable = true

                    byteArr = libTest.bitmapToByteArray(rotatedBitmap)
                    Log.d("myLog", "byteArr를 생성했습니다.")
                    val bitmapFromByteArray: Bitmap =
                        BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
//                    Log.d("mylog", "byteArr를 다시 Bitmap으로 변환했습니다.")
                    val new_vector = libTest.getFaceVector(baseContext, byteArr)

                    // 벡터 비교 수행
                    val size = db.sizeOfUser()

                    val list = db.selectAllUser()
                    closestDistance = Double.MAX_VALUE
                    closestUser = null

                    for (user in list) {
                        val distance = libTest.getFaceDistance(new_vector, user.vector)

                        if (distance < closestDistance) {
                            closestUser = user
                            closestDistance = distance
                        }
                    }

                    Log.d("myLog", "closestDistance: $closestDistance")

                    /* 매칭되는 ID가 있는 경우 */
                    if (closestDistance < 0.4) {

                        runOnUiThread {
                            // 카메라 프리뷰 작동 중지
                            stopCameraPreview()
                            messageView.visibility = View.INVISIBLE

                            // "ID-XXX님 어서오세요" 텍스트를 상단에 표시
                            matchedIDView.text = "${closestUser?.ID}님 어서오세요."
                            matchedIDView.visibility = View.VISIBLE

                            // 일반 주문, 간단 주문 활성화
                            generalOrderButton.isEnabled = true
                            simpleOrderButton.isEnabled = true

//                            generalOrderButton.setBackgroundColor(
//                                ContextCompat.getColor(
//                                    this@MainActivity,
//                                    R.color.purple_500
//                                )
//                            )

                            generalOrderButton.setOnClickListener {
                                val intent = Intent(
                                    this@MainActivity, general_order_activity::class.java
                                )
                                intent.putExtra("id", "${closestUser?.ID}")
                                startActivity(intent)
                            }

                            simpleOrderButton.setOnClickListener {
                                val intent = Intent(
                                    this@MainActivity, simple_order_activity::class.java
                                )
                                intent.putExtra("id", "${closestUser?.ID}")
                                startActivity(intent)
                            }

//                            simpleOrderButton.setBackgroundColor(
//                                ContextCompat.getColor(
//                                    this@MainActivity,
//                                    R.color.purple_500
//                                )
//                            )

                            // 일정 시간 후에 카메라 작동 및 버튼 비활성화
//                            timerHandler.postDelayed({
//                                startCamera()
//                                generalOrderButton.isEnabled = false
//                                simpleOrderButton.isEnabled = false
//                            }, BUTTON_TIMEOUT)
                        }

                    }
                    /* 매칭되는 ID가 없는 경우 */
                    else {
                        // 새로운 ID 생성
                        new_id = db.createID(new_vector)

                        runOnUiThread {
                            // 카메라 프리뷰 작동 중지
                            stopCameraPreview()
                            messageView.visibility = View.INVISIBLE

                            // "ID-XXX 생성" 텍스트를 카메라 프리뷰에 출력
                            createdIDView.text = "${new_id} 생성"
                            createdIDView.visibility = View.VISIBLE

                            // 일반 주문 활성화
                            generalOrderButton.isEnabled = true

                            generalOrderButton.setOnClickListener {
                                val intent = Intent(
                                    this@MainActivity, general_order_activity::class.java
                                )
                                intent.putExtra("id", "$new_id")
                                startActivity(intent)
                            }

                        }

                    }
                }
                image.close()
            }
        }

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            var preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            // ImageCapture
            imageCapture = ImageCapture.Builder().build()

            // ImageAnalysis
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().apply {
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
                    this, cameraSelector, preview, imageCapture, imageAnalysis
                )

            } catch (exc: Exception) {
                Log.d("CameraX-Debug", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }
    */

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
