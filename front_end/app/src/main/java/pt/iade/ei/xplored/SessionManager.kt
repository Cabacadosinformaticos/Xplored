package pt.iade.ei.xplored

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Centralized session management for Xplored.
 *
 * - Persists the login state and the data of the active user
 * - Accounts live on the backend; there is no local user list anymore
 */
object SessionManager {
    private const val PREF_SESSION = "XploredSession"

    // Keys for session-level info
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_USER_NAME = "userName"
    private const val KEY_USER_EMAIL = "userEmail"
    private const val KEY_USER_POINTS = "userPoints"
    private const val KEY_USER_ABOUT = "userAbout"
    private const val KEY_USER_COUNTRY = "userCountry"
    private const val KEY_USER_ROLE = "userRole"

    // Helper accessor
    private fun sessionPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_SESSION, Context.MODE_PRIVATE)

    // ---------- SESSION STATE ----------

    fun setLoggedIn(context: Context, value: Boolean) {
        sessionPrefs(context).edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()
        Log.d("SessionDebug", "setLoggedIn = $value")
    }

    fun isLoggedIn(context: Context): Boolean =
        sessionPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)

    fun saveUserData(context: Context, name: String, email: String) {
        sessionPrefs(context).edit()
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .apply()
        Log.d("SessionDebug", "saveUserData: name=$name, email=$email")
    }

    fun getUserName(context: Context): String =
        sessionPrefs(context).getString(KEY_USER_NAME, "") ?: ""

    fun getUserEmail(context: Context): String =
        sessionPrefs(context).getString(KEY_USER_EMAIL, "") ?: ""

    fun saveUserPoints(context: Context, points: Int) {
        sessionPrefs(context).edit().putInt(KEY_USER_POINTS, points).apply()
        Log.d("SessionDebug", "saveUserPoints = $points")
    }

    fun getUserPoints(context: Context): Int =
        sessionPrefs(context).getInt(KEY_USER_POINTS, 0)

    // Store "Sobre" (about) for the active session
    fun saveUserAbout(context: Context, about: String) {
        sessionPrefs(context).edit()
            .putString(KEY_USER_ABOUT, about)
            .apply()
        Log.d("SessionDebug", "saveUserAbout = $about")
    }

    // Read "Sobre" (about) from the active session
    fun getUserAbout(context: Context): String =
        sessionPrefs(context).getString(KEY_USER_ABOUT, "") ?: ""

    // The local user list was removed; "about" now lives in the session.
    // Kept because ProfileActivity still calls it with the active user's email.
    fun getAboutForEmail(context: Context, email: String): String =
        if (email.trim().equals(getUserEmail(context).trim(), ignoreCase = true)) getUserAbout(context) else ""

    /**
     * Clears the session data (the backend keeps the account).
     */
    fun clearSession(context: Context) {
        val prefs = sessionPrefs(context)
        prefs.edit().clear().apply()
        Log.d("SessionDebug", "Session cleared")
    }

    fun logoutUser(context: Context) {
        Log.d("SessionDebug", "Performing logout...")
        clearSession(context)
        setLoggedIn(context, false)
        Log.d("SessionDebug", "Logout successful - user session cleared.")
    }

    fun saveUserCountry(context: Context, country: String) {
        sessionPrefs(context).edit().putString(KEY_USER_COUNTRY, country).apply()
    }

    fun getUserCountry(context: Context): String =
        sessionPrefs(context).getString(KEY_USER_COUNTRY, "") ?: ""

    // Role of the active user ("USER", "BUSINESS" or "ADMIN"), needed for moderation.
    fun saveUserRole(context: Context, role: String) {
        sessionPrefs(context).edit().putString(KEY_USER_ROLE, role).apply()
    }

    fun getUserRole(context: Context): String =
        sessionPrefs(context).getString(KEY_USER_ROLE, "") ?: ""
}