package dallasgutauckis.scribe

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.ui.core.setContent
import dallasgutauckis.scribe.ui.ScannerUi
import java.net.URI

class MainActivity : AppCompatActivity() {

    private val navigation: Navigation = { directive ->
        when (directive) {
            is Directive.Deeplink -> {
                // Do URI Matching
                null
            }

            is Directive.DirectIntent -> Destination.Scanner
            is Directive.PassiveIntent -> TODO()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            when (navigation.destination(intent)) {
                Destination.Scanner -> ScannerUi().bindUi()
                null -> TODO()
            }
        }
    }
}

sealed class Destination {
    object Scanner : Destination()
}

sealed class Directive {
    /**
     * Used for cases where the destination is only a deep link (e.g. seatgeek://event/1234)
     */
    data class Deeplink(val uri: URI) : Directive()

    /**
     * Used for intents where the component (com.<x>.<y>.<z>) is specified specifically
     */
    data class DirectIntent(val intent: Intent) : Directive()

    /**
     * Used for intents like ACTION_VIEW for PDFs
     */
    data class PassiveIntent(val intent: Intent) : Directive()
}

typealias Navigation = (Directive) -> Destination?

fun Navigation.parse(intent: Intent): Directive {
    return when {
        intent.data != null -> Directive.Deeplink(URI.create(intent.data.toString()))
        intent.component != null -> Directive.DirectIntent(intent)
        else -> Directive.PassiveIntent(intent)
    }
}

fun Navigation.destination(intent: Intent): Destination? {
    return this(parse(intent))
}
