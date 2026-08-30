package com.statushdlite

import android.content.Context
import android.content.Intent

/**
 * Launches Termux if it is installed. Does not attempt to send it any
 * command — the user pastes the copied command themselves.
 */
object TermuxLauncher {

    private const val TERMUX_PACKAGE = "com.termux"

    /** Returns true if Termux was found and launched, false otherwise. */
    fun openTermux(context: Context): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(TERMUX_PACKAGE)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            true
        } else {
            false
        }
    }
}
