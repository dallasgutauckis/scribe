package dallasgutauckis.scribe.presentation

import oolong.*
import oolong.effect.none
import javax.smartcardio.Card

interface Presentation<Model, Msg, Props> {
    val init: Init<Model, Msg>
    val update: Update<Model, Msg>
    val view: View<Model, Props>
}

object ScannerPresentation : Presentation<ScannerPresentation.Model, ScannerPresentation.Msg, ScannerPresentation.Props> {
    data class Model(
        val isCameraOn: Boolean = false,
        val isScanning: Boolean = false
    )

    data class Props(
        val isCameraOn: Boolean = false,
        val isScanning: Boolean = false,
        val startCamera: ((Dispatch<Msg>) -> Unit)? = null,
        val stopCamera: ((Dispatch<Msg>) -> Unit)? = null
    )

    sealed class Msg {
        object StartCamera : Msg()
        object StopCamera : Msg()
        object Scan : Msg()
        data class ImageScanComplete(val cards: List<Card>) : Msg()
    }

    private val scanPhoto: Effect<Msg> = { dispatch ->
        dispatch(Msg.ImageScanComplete(emptyList()))
    }

    override val init: Init<Model, Msg> = {
        next(Model(), none())
    }

    override val update: Update<Model, Msg> = { msg, model ->
        when (msg) {
            Msg.StartCamera -> next(model.copy(isCameraOn = true), none())
            Msg.StopCamera -> next(model.copy(isCameraOn = false), none())
            Msg.Scan -> next(model.copy(isScanning = true), scanPhoto)
            is Msg.ImageScanComplete -> next(model.copy(isScanning = false), none())
        }
    }

    override val view: View<Model, Props> = {
        Props(
            isCameraOn = it.isCameraOn,
            isScanning = it.isScanning,
            startCamera = { dispatch -> dispatch(Msg.StartCamera) },
            stopCamera = { dispatch -> dispatch(Msg.StopCamera) }
        )
    }
}

/*
 * This function was created to make the implementations for this less dependent on Pair specifically and make the functionality easily replaceable
 */
fun <Model, Msg> next(model: Model, msg: Effect<Msg>): Next<Model, Msg> = model to msg

/**
 * Useful for situations where the View model and the props would be the same (basic screens and
 * screens without usable components, e.g. informational screens)
 */
fun <T> identity(): View<T, T> = { it }