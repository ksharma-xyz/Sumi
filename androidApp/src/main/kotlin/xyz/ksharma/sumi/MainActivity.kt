package xyz.ksharma.sumi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import xyz.ksharma.sumi.preferences.ActivityHolder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        ActivityHolder.set(this)
    }

    override fun onDestroy() {
        ActivityHolder.clear()
        super.onDestroy()
    }
}
