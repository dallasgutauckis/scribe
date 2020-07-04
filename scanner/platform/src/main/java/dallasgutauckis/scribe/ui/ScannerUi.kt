package dallasgutauckis.scribe.ui

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
import dallasgutauckis.scribe.presentation.ScannerPresentation
import oolong.Dispatch
import oolong.Oolong
import oolong.Render

class ScannerUi {
    /**
     * Compose model representing the current state of the UI
     */
    private val uiModel =
        mutableStateOf<LastRender<ScannerPresentation.Props, ScannerPresentation.Msg>>(LastRender.NoValue())

    private val oolong =
        Oolong.runtime(
            init = ScannerPresentation.init,
            update = ScannerPresentation.update,
            view = ScannerPresentation.view,
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
                    }

                    if (lastRender.props.isScanning) {
                        Text("Scanning!")
                    }
                }
            }
        }
    }
}

sealed class LastRender<Props, Msg> {
    data class Value<Props, Msg>(val props: Props, val dispatch: Dispatch<Msg>) :
        LastRender<Props, Msg>()

    data class NoValue<Props, Msg>(val sentinelValue: Unit = Unit) : LastRender<Props, Msg>()
}

fun <Msg, Props> composeRenderer(uiModel: MutableState<LastRender<Props, Msg>>): Render<Msg, Props> =
    { props, dispatch ->
        uiModel.value = LastRender.Value(props, dispatch)
        Unit
    }
