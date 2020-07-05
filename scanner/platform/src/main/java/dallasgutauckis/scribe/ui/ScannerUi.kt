package dallasgutauckis.scribe.ui

import androidx.camera.view.PreviewView
import androidx.compose.Composable
import androidx.compose.MutableState
import androidx.compose.mutableStateOf
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.layout.Column
import androidx.ui.layout.padding
import androidx.ui.material.FloatingActionButton
import androidx.ui.material.Scaffold
import androidx.ui.unit.dp
import androidx.ui.viewinterop.AndroidView
import dallasgutauckis.scribe.presentation.ScannerPresentation
import dallasgutauckis.scribe.scanner.platform.R
import oolong.Dispatch
import oolong.Effect
import oolong.Oolong
import oolong.Render

class ScannerUi() {

    /**
     * Compose model representing the current state of the UI
     */
    private val uiModel =
        mutableStateOf<LastRender<ScannerPresentation.Props, ScannerPresentation.Msg>>(LastRender.NoValue())

    private val presentation = ScannerPresentation(scanImageFactory = { scan ->
        { dispatch ->
            // TODO Run ML
            val options = ObjectDetec

            dispatch(ScannerPresentation.Msg.ImageScanComplete(emptyList()))
        }
    })

    private val oolong =
        Oolong.runtime(
            init = presentation.init,
            update = presentation.update,
            view = presentation.view,
            render = composeRenderer(uiModel)
        )

    @Composable
    fun bindUi() {
        val lastRender = uiModel.value

        if (lastRender is LastRender.NoValue) {
            return
        } else if (lastRender is LastRender.Value<ScannerPresentation.Props, ScannerPresentation.Msg>) {
            val isCameraOn = lastRender.props.isCameraOn

            Scaffold(floatingActionButton = {
                FloatingActionButton({
                    if (isCameraOn) {
                        lastRender.props.stopCamera!!
                    } else {
                        lastRender.props.startCamera!!
                    }.invoke(lastRender.dispatch)
                }) {
                    Text(if (isCameraOn) "-" else "+")
                }
            }) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Hello, scanner!")

                    if (isCameraOn) {
                        Text("Camera's on!")

                        if (lastRender.props.isCameraPermissionGranted) {
                            AndroidView(R.layout.camera_preview) { view ->
                                (view as PreviewView)
                            }
                        }
                    }


                    if (lastRender.props.isScanning) Text("Scanning!")
                }
            }
        }
    }
}

sealed class LastRender<Props, Msg> {
    data class Value<Props, Msg>(val props: Props, val dispatch: Dispatch<Msg>) :
        LastRender<Props, Msg>()

    data class NoValue<Props, Msg>(val noop: Unit = Unit) : LastRender<Props, Msg>()
}

fun <Msg, Props> composeRenderer(uiModel: MutableState<LastRender<Props, Msg>>): Render<Msg, Props> =
    { props, dispatch ->
        uiModel.value = LastRender.Value(props, dispatch)
        Unit
    }
