package dallasgutauckis.scribe.scanner.uno

import android.graphics.ImageFormat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import dallasgutauckis.scribe.scanner.interfaces.Logger
import dallasgutauckis.scribe.scanner.interfaces.Scanner
import java.nio.ByteBuffer

class UnoScanner(val logger: Logger? = null) : Scanner<UnoCard> {
    private val objectDetector by lazy {
        ObjectDetection.getClient(
            ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableMultipleObjects()
                .enableClassification()
                .build()
        )
    }

    /**
     * Image will be in NV21 format
     */
    override fun scan(
        byteBuffer: ByteBuffer,
        width: Int,
        height: Int,
        rotation: Int
    ): Collection<UnoCard> {
        return objectDetector.process(
            InputImage.fromByteBuffer(
                byteBuffer,
                width,
                height,
                rotation,
                ImageFormat.NV21
            )
        )
            .continueWith {
                if (it.isSuccessful) it.result else emptyList()
            }.result
            ?.filter {
                it.labels
                    .onEach {
                        logger?.log("text: ${it.text}; confidence: ${it.confidence}; index: ${it.index}")
                    }
                    .any { label -> label.text.equals("card", true) }
            }?.map {
                // Do UNO card detection
                UnoCard.ColoredCard.DrawTwo(UnoCard.ColoredCard.Color.BLUE)
            } ?: emptyList()
    }

}

sealed class UnoCard {
    sealed class ColoredCard(open val color: Color) : UnoCard() {
        enum class Color {
            YELLOW, RED, GREEN, BLUE
        }

        data class Zero(override val color: Color) : ColoredCard(color)
        data class One(override val color: Color) : ColoredCard(color)
        data class Two(override val color: Color) : ColoredCard(color)
        data class Three(override val color: Color) : ColoredCard(color)
        data class Four(override val color: Color) : ColoredCard(color)
        data class Five(override val color: Color) : ColoredCard(color)
        data class Six(override val color: Color) : ColoredCard(color)
        data class Seven(override val color: Color) : ColoredCard(color)
        data class Eight(override val color: Color) : ColoredCard(color)
        data class Nine(override val color: Color) : ColoredCard(color)
        data class Reverse(override val color: Color) : ColoredCard(color)
        data class Skip(override val color: Color) : ColoredCard(color)
        data class DrawTwo(override val color: Color) : ColoredCard(color)
    }

    object Wild : UnoCard()
    object DrawFour : UnoCard()
}

val UnoCard.scoreValue: Int
    get() = when (this) {
        is UnoCard.ColoredCard.Zero -> 0
        is UnoCard.ColoredCard.One -> 1
        is UnoCard.ColoredCard.Two -> 2
        is UnoCard.ColoredCard.Three -> 3
        is UnoCard.ColoredCard.Four -> 4
        is UnoCard.ColoredCard.Five -> 5
        is UnoCard.ColoredCard.Six -> 6
        is UnoCard.ColoredCard.Seven -> 7
        is UnoCard.ColoredCard.Eight -> 8
        is UnoCard.ColoredCard.Nine -> 9
        is UnoCard.ColoredCard.Reverse -> 20
        is UnoCard.ColoredCard.Skip -> 20
        is UnoCard.ColoredCard.DrawTwo -> 20
        UnoCard.Wild -> 50
        UnoCard.DrawFour -> 50
    }

fun Collection<UnoCard>.score(): Int = this.sumBy { it.scoreValue }
