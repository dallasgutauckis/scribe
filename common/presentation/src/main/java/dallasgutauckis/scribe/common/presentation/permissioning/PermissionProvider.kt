package dallasgutauckis.scribe.common.presentation.permissioning

interface PermissionProvider<Permission : dallasgutauckis.scribe.common.presentation.permissioning.Permission> {
    val permission: Permission
    fun hasPermission(): Boolean
    fun requestPermission(callback: PermissionCallback)
}


interface PermissionCallback {
    fun onPermissionGranted(permission: Permission)
    fun onPermissionDenied(permission: Permission)
}

sealed class Permission {
    object Camera : Permission()
}
