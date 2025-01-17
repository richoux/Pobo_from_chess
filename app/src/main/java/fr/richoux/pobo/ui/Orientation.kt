package fr.richoux.pobo.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

// Disable landscape orientation, from https://stackoverflow.com/questions/69079267/disable-landscape-mode-in-compose
@Composable
fun LockScreenOrientation(orientation: Int) {
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
    val originalOrientation = activity.requestedOrientation
    activity.requestedOrientation = orientation
    onDispose {
      activity.requestedOrientation = originalOrientation
    }
  }
}

fun Context.findActivity(): Activity? = when (this) {
  is Activity -> this
  is ContextWrapper -> baseContext.findActivity()
  else -> null
}