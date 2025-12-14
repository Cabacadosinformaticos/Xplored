package pt.iade.ei.xplored

import android.content.Context
import com.google.android.gms.maps.GoogleMap

object SettingsManager {
    private const val PREF_SETTINGS = "XploredSettings"

    // Keys
    private const val KEY_MAP_TYPE = "mapType"
    private const val KEY_GHOST_MODE = "ghostMode"
    private const val KEY_WIFI_ONLY = "wifiOnly"
    private const val KEY_LOW_QUALITY_IMG = "lowQualityImg"
    private const val KEY_NOTIF_NEARBY = "notifNearby"
    private const val KEY_NOTIF_PROMO = "notifPromo"
    private const val KEY_BIOMETRIC = "biometric"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE)

    // Map Settings
    fun setMapType(context: Context, type: Int) = prefs(context).edit().putInt(KEY_MAP_TYPE, type).apply()
    fun getMapType(context: Context): Int = prefs(context).getInt(KEY_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL)

    // Privacy & Data
    fun setGhostMode(context: Context, enabled: Boolean) = prefs(context).edit().putBoolean(KEY_GHOST_MODE, enabled).apply()
    fun isGhostMode(context: Context): Boolean = prefs(context).getBoolean(KEY_GHOST_MODE, false)

    fun setWifiOnly(context: Context, enabled: Boolean) = prefs(context).edit().putBoolean(KEY_WIFI_ONLY, enabled).apply()
    fun isWifiOnly(context: Context): Boolean = prefs(context).getBoolean(KEY_WIFI_ONLY, false)

    fun setLowQualityImages(context: Context, enabled: Boolean) = prefs(context).edit().putBoolean(KEY_LOW_QUALITY_IMG, enabled).apply()
    fun isLowQualityImages(context: Context): Boolean = prefs(context).getBoolean(KEY_LOW_QUALITY_IMG, false)

    // Notifications
    fun setNearbyNotifs(context: Context, enabled: Boolean) = prefs(context).edit().putBoolean(KEY_NOTIF_NEARBY, enabled).apply()
    fun isNearbyNotifs(context: Context): Boolean = prefs(context).getBoolean(KEY_NOTIF_NEARBY, true)

    fun setPromoNotifs(context: Context, enabled: Boolean) = prefs(context).edit().putBoolean(KEY_NOTIF_PROMO, enabled).apply()
    fun isPromoNotifs(context: Context): Boolean = prefs(context).getBoolean(KEY_NOTIF_PROMO, true)

    // Security
    fun setBiometric(context: Context, enabled: Boolean) = prefs(context).edit().putBoolean(KEY_BIOMETRIC, enabled).apply()
    fun isBiometric(context: Context): Boolean = prefs(context).getBoolean(KEY_BIOMETRIC, false)
}