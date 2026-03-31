package com.matedroid.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.matedroid.MainActivity

/**
 * Glance ActionCallback triggered when the user taps the sentry dot in the widget.
 * Opens the Sentry History screen via deep link.
 */
class SentryHistoryCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)
        val carId = prefs[CarWidget.CAR_ID_KEY] ?: return
        val exteriorColor = prefs[CarWidget.EXTERIOR_COLOR_KEY]

        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra("EXTRA_CAR_ID", carId)
            .putExtra("EXTRA_NAVIGATE_TO", "sentry_history")
        if (exteriorColor != null) {
            intent.putExtra("EXTRA_EXTERIOR_COLOR", exteriorColor)
        }
        context.startActivity(intent)
    }
}
