package xyz.ksharma.sumi.preferences

import android.app.Activity
import java.lang.ref.WeakReference

object ActivityHolder {
    private var ref: WeakReference<Activity>? = null

    fun set(activity: Activity) {
        ref = WeakReference(activity)
    }

    fun clear() {
        ref = null
    }

    fun get(): Activity? = ref?.get()
}
