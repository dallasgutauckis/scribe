package dallasgutauckis.scribe

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.ui.core.setContent
import dallasgutauckis.scribe.common.platform.permissioning.PermissionRequestDelegateManager
import dallasgutauckis.scribe.common.platform.permissioning.PermissionRequestHandler
import dallasgutauckis.scribe.common.platform.permissioning.PermissionRequestManager
import dallasgutauckis.scribe.common.platform.permissioning.androidName
import dallasgutauckis.scribe.common.presentation.permissioning.Permission
import dallasgutauckis.scribe.common.presentation.permissioning.PermissionCallback
import dallasgutauckis.scribe.ui.ScannerUi
import java.io.File
import java.net.URI


class MainActivity : ComponentActivity(), PermissionRequestHandler {

    private val permissionRequestDelegateManager = PermissionRequestDelegateManager()

    private val permissionRequestManager = PermissionRequestManager(this)

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
                Destination.Scanner -> ScannerUi(permissionRequestManager.camera, this).ui()
                null -> TODO()
            }
        }
    }

    /**
     * Called by Android platform and delivers camera permissions to the relevant requester in the [permissionRequests]
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (!permissionRequestDelegateManager.handlePermissionResult(
                requestCode,
                permissions,
                grantResults.toTypedArray()
            )
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun requestPermissions(
        permissions: Array<Permission>,
        callback: PermissionCallback
    ) {
        requestPermissions(
            permissions.map { it.androidName }.toTypedArray(),
            permissionRequestDelegateManager.addPermissionCallback(callback)
        )
    }

    override fun hasPermission(permission: String): Boolean {
        return when (val result = checkSelfPermission(permission)) {
            PackageManager.PERMISSION_GRANTED -> true
            PackageManager.PERMISSION_DENIED -> false
            else -> throw IllegalArgumentException("Unexpected value returned when checking permission ($permission): $result")
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
