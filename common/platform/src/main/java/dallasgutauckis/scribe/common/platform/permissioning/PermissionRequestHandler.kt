package dallasgutauckis.scribe.common.platform.permissioning

import android.Manifest
import android.content.pm.PackageManager
import dallasgutauckis.scribe.common.presentation.permissioning.Permission
import dallasgutauckis.scribe.common.presentation.permissioning.PermissionCallback
import dallasgutauckis.scribe.common.presentation.permissioning.PermissionProvider
import java.util.concurrent.atomic.AtomicInteger

interface PermissionRequestHandler {
    fun requestPermissions(
        permissions: Array<Permission>,
        callback: PermissionCallback
    )

    fun hasPermission(permission: String): Boolean
}

val Permission.androidName: String
    get() = when (this) {
        Permission.Camera -> Manifest.permission.CAMERA
    }

class PermissionRequestDelegateManager {

    private val permissionRequests = mutableMapOf<Int, PermissionCallback>()

    private val nextRequestCode = AtomicInteger()

    fun addPermissionCallback(callback: PermissionCallback): Int = nextRequestCode.getAndIncrement()
        .also { permissionRequests[it] = callback }

    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: Array<Int>
    ): Boolean {
        val callback = permissionRequests.remove(requestCode)

        return callback != null && permissions
            .mapIndexed { index, androidName ->
                when (androidName) {
                    Permission.Camera.androidName -> Permission.Camera
                    else -> null
                } to when (grantResults[index]) {
                    PackageManager.PERMISSION_GRANTED -> callback::onPermissionGranted!!
                    PackageManager.PERMISSION_DENIED -> callback::onPermissionDenied!!
                    else -> null
                }
            }
            .toMap()
            .filterKeys { it != null }
            .onEach { it.value!!.invoke(it.key!!) }
            .any()
    }

}


class PermissionRequestManager(private val permissionRequestHandler: PermissionRequestHandler) {

    val camera: PermissionProvider<Permission.Camera> =
        PermissionProviderImpl(
            Permission.Camera,
            permissionRequestHandler
        )

    class PermissionProviderImpl<T : Permission>(
        override val permission: T,
        private val permissionRequestHandler: PermissionRequestHandler
    ) : PermissionProvider<T> {
        override fun hasPermission(): Boolean {
            return permissionRequestHandler.hasPermission(permission.androidName)
        }

        override fun requestPermission(callback: PermissionCallback) {
            permissionRequestHandler.requestPermissions(
                listOf(permission).toTypedArray(),
                callback
            )
        }

    }
}
