package com.statushdlite

import android.content.Context

/**
 * Persists the "default" conversion settings that Home starts with —
 * separate from whatever the person is fiddling with on Home in a given
 * session. Settings screen reads/writes this; Home reads it once on
 * first composition so a fresh app launch (or a freshly (re)created Home
 * instance) picks up whatever was last saved as the default.
 */
object AppPreferences {

    private const val PREFS_NAME = "statushd_settings"
    private const val KEY_RES_WIDTH = "default_res_width"
    private const val KEY_RES_HEIGHT = "default_res_height"
    private const val KEY_BITRATE = "default_bitrate"
    private const val KEY_FORMAT = "default_format"
    private const val KEY_USE_ADVANCED = "default_use_advanced"

    /** The app's out-of-the-box values — matches [ConversionSettings]'s
     *  own constructor defaults. Both the "nothing saved yet" fallback and
     *  the target of "Reset to Defaults" on the Settings screen. */
    val FactoryDefaults = ConversionSettings()

    fun loadDefaultSettings(context: Context): ConversionSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val width = prefs.getInt(KEY_RES_WIDTH, FactoryDefaults.resolution.width)
        val height = prefs.getInt(KEY_RES_HEIGHT, FactoryDefaults.resolution.height)
        val resolution = ResolutionPresets.find { it.width == width && it.height == height }
            ?: FactoryDefaults.resolution
        val bitrate = prefs.getFloat(KEY_BITRATE, FactoryDefaults.bitrateMbps.toFloat()).toDouble()
        val formatName = prefs.getString(KEY_FORMAT, FactoryDefaults.format.name)
        val format = OutputFormat.entries.find { it.name == formatName } ?: FactoryDefaults.format
        val useAdvanced = prefs.getBoolean(KEY_USE_ADVANCED, FactoryDefaults.useAdvanced)
        return ConversionSettings(
            useAdvanced = useAdvanced,
            resolution = resolution,
            bitrateMbps = bitrate,
            format = format
        )
    }

    fun saveDefaultSettings(context: Context, settings: ConversionSettings) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_RES_WIDTH, settings.resolution.width)
            .putInt(KEY_RES_HEIGHT, settings.resolution.height)
            .putFloat(KEY_BITRATE, settings.bitrateMbps.toFloat())
            .putString(KEY_FORMAT, settings.format.name)
            .putBoolean(KEY_USE_ADVANCED, settings.useAdvanced)
            .apply()
    }

    /** Wipes stored overrides so [loadDefaultSettings] falls back to
     *  [FactoryDefaults] again. */
    fun resetToFactoryDefaults(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
