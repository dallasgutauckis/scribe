package dallasgutauckis.scribe.ui

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.Composable
import androidx.compose.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.layout.Column
import androidx.ui.layout.padding
import androidx.ui.material.FloatingActionButton
import androidx.ui.material.Scaffold
import androidx.ui.unit.dp
import androidx.ui.viewinterop.AndroidView
import dallasgutauckis.scribe.common.presentation.permissioning.Permission
import dallasgutauckis.scribe.common.presentation.permissioning.PermissionCallback
import dallasgutauckis.scribe.common.presentation.permissioning.PermissionProvider
import dallasgutauckis.scribe.presentation.ScannerPresentation
import dallasgutauckis.scribe.scanner.platform.R
import oolong.Oolong
import java.io.File
import java.util.concurrent.ExecutorService


class ScannerUi(
    cameraPermissionProvider: PermissionProvider<Permission.Camera>,
    private val context: Context
) {

    private lateinit var dispatch: (msg: ScannerPresentation.Msg) -> Unit

    private val cameraPermissionCallback = object : PermissionCallback {
        override fun onPermissionGranted(permission: Permission) {
            dispatch(ScannerPresentation.Msg.CameraPermissionAllowed)
        }

        override fun onPermissionDenied(permission: Permission) {
            dispatch(ScannerPresentation.Msg.CameraPermissionDenied)
        }

    }

    init {
        if (!cameraPermissionProvider.hasPermission()) {
            cameraPermissionProvider.requestPermission(cameraPermissionCallback)
        }
    }

    /**
     * Compose model representing the current state of the UI
     */
    private val uiModel =
        mutableStateOf(ScannerPresentation.Props())

    /**
     * Presentation class
     */
    private val presentation = ScannerPresentation(
        scanImageFactory = { scan ->
            { dispatch ->
                // TODO Run ML
                scan.image
//            UnoScanner.scan()

                dispatch(ScannerPresentation.Msg.ImageScanComplete(emptyList()))
            }
        },
        isCameraAllowed = cameraPermissionProvider.hasPermission()
    )

    private val oolong = Oolong.runtime(
        init = presentation.init,
        update = presentation.update,
        view = presentation.view,
        render = { props, dispatch ->
            this.dispatch = dispatch
            this.uiModel.value = props

            if (props.isCameraOn) {
                if (!isCameraOn) {
                    isCameraOn = true
                    startCamera()
                }
            } else {
                if (isCameraOn) {
                    stopCamera()
                }
            }
            Unit
        }
    )

    /* Camera */
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null

    private lateinit var cameraExecutor: ExecutorService

    private var isCameraOn = false

    private var viewFinder: PreviewView? = null

    private val outputDirectory: File by lazy {
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, "Scanner").apply { mkdirs() }
        }
        if (mediaDir != null && mediaDir.exists())
            mediaDir else context.filesDir
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    private fun startCamera() {
        Log.v(TAG, "camera starting")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder().build()

            // Select back camera
            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                preview!!.setSurfaceProvider(viewFinder!!.createSurfaceProvider())
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun stopCamera() {}

    private val lifecycleOwner = LifecycleOwner { lifecycle }

    private val lifecycle = object : Lifecycle() {
        override fun addObserver(observer: LifecycleObserver) = Unit

        override fun removeObserver(observer: LifecycleObserver) = Unit

        override fun getCurrentState(): State = State.RESUMED
    }
    /* End Camera */

    @Composable
    fun ui() {
        val props = uiModel.value
        val isCameraOn = props.isCameraOn

        val fabAction = (if (isCameraOn) props.stopCamera else props.startCamera)

        Scaffold(floatingActionButton = {
            if (fabAction != null) {
                FloatingActionButton(
                    onClick = {
                        fabAction(dispatch)
                    }, icon = {
                        Text(if (isCameraOn) "-" else "+")
                    })
            }
        }) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Hello, scanner!")

                if (isCameraOn) {
                    Text("Camera's on!")

                    if (props.isCameraOn)
                        AndroidView(R.layout.camera_preview) { view ->
                            viewFinder = (view as PreviewView)
                        }
                }


                if (props.isScanning)
                    Text("Scanning!")
            }
        }
    }
}
