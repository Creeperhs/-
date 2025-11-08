import android.view.KeyEvent
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ShortcutHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "android") return
        hook("interceptSystemKeysAndShortcutsOld", lpparam.classLoader)
        hook("interceptSystemKeysAndShortcutsNew", lpparam.classLoader)
    }

    private fun hook(method: String, cl: ClassLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                "com.android.server.policy.PhoneWindowManager",
                cl,
                method,
                android.os.IBinder::class.java,
                KeyEvent::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val e = param.args[1] as KeyEvent
                        
                        // alt-tab
                        if (e.keyCode == KeyEvent.KEYCODE_TAB &&
                            e.metaState and KeyEvent.META_ALT_ON != 0
                        ) {
                            param.result = true
                            return
                        }
                        
                        // win/meta
                        if (e.keyCode == KeyEvent.KEYCODE_META_LEFT ||
                            e.keyCode == KeyEvent.KEYCODE_META_RIGHT
                        ) {
                            param.result = true
                            return
                        }
                    }
                }
            )
        } catch (_: Throwable) {}
    }
}
