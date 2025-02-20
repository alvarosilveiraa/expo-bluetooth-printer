import android.os.Bundle
import expo.modules.interfaces.permissions.Permissions
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.CodedException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BluetoothPrinterHelpers {
  companion object {
    internal suspend fun askForPermissions(manager: Permissions, vararg args: String): Bundle {
      return suspendCoroutine {
        Permissions.askForPermissionsWithPermissionsManager(
          manager,
          object : Promise {
            override fun resolve(value: Any?) {
              it.resume(
                value as? Bundle
                  ?: throw ConversionException(Any::class.java, Bundle::class.java, "value returned by the permission promise is not a Bundle")
              )
            }

            override fun reject(code: String, message: String?, cause: Throwable?) {
              it.resumeWithException(CodedException(code, message, cause))
            }
          },
          *args
        )
      }
    }
  }
}
