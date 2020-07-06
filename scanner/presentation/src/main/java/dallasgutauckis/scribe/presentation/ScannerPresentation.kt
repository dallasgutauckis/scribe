package dallasgutauckis.scribe.presentation

import oolong.*
import oolong.effect.none
import javax.smartcardio.Card

interface Presentation<Model, Msg, Props> {
    val init: Init<Model, Msg>
    val update: Update<Model, Msg>
    val view: View<Model, Props>
}

typealias ScanImageEffectFactory = (ScannerPresentation.Msg.Scan) -> Effect<ScannerPresentation.Msg>

class ScannerPresentation(
    private val isCameraAllowed: Boolean,
    private val scanImageFactory: ScanImageEffectFactory)
    :  Presentation<ScannerPresentation.Model, ScannerPresentation.Msg, ScannerPresentation.Props> {
    data class Model(
        val isCameraOn: Boolean = false,
        val isCameraAllowed: Boolean = false,
        val isScanning: Boolean = false
    )

    data class Props(
        val isCameraOn: Boolean = false,
        val isScanning: Boolean = false,
        val isCameraPermissionGranted: Boolean = false,
        val startCamera: ((Dispatch<Msg>) -> Unit)? = null,
        val stopCamera: ((Dispatch<Msg>) -> Unit)? = null
    )

    sealed class Msg {
        object StartCamera : Msg()
        object StopCamera : Msg()
        object CameraPermissionAllowed : Msg()
        object CameraPermissionDenied : Msg()

        data class Scan(val image: String) : Msg()
        data class ImageScanComplete(val cards: List<Card>) : Msg()
    }

    override val init: Init<Model, Msg> = {
        next(Model(isCameraAllowed = isCameraAllowed), none())
    }

    override val update: Update<Model, Msg> = { msg, model ->
        when (msg) {
            Msg.StartCamera -> next(model.copy(isCameraOn = true), none())
            Msg.StopCamera -> next(model.copy(isCameraOn = false), none())
            is Msg.Scan -> next(model.copy(isScanning = true), scanImageFactory(msg))
            is Msg.ImageScanComplete -> next(model.copy(isScanning = false), none())
            Msg.CameraPermissionAllowed -> next(model.copy(isCameraAllowed = true), none())
            Msg.CameraPermissionDenied -> next(model.copy(isCameraAllowed = false), none())
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
