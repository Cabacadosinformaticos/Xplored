package pt.iade.ei.xplored

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray

/**
 * Centralized session and user management for Xplored.
 *
 * - Stores a local list of known users (email, name, etc.)
 * - Persists login state for the currently active session
 * - Keeps users even after logout
 */
object SessionManager {
    // Two separate preference files (clean separation)
    private const val PREF_SESSION = "XploredSession"
    private const val PREF_USERS = "XploredUsers"

    // Keys for session-level info
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_USER_NAME = "userName"
    private const val KEY_USER_EMAIL = "userEmail"
    private const val KEY_USER_POINTS = "userPoints"
    private const val KEY_USER_ABOUT = "userAbout"

    // Keys for stored user list
    private const val KEY_USERS_LIST = "usersList"

    private const val KEY_USER_COUNTRY = "userCountry"

    // Helper accessors
    private fun sessionPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_SESSION, Context.MODE_PRIVATE)

    private fun usersPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_USERS, Context.MODE_PRIVATE)

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

    // Update "about" in the registered users list (and keep session in sync)
    fun updateUserAbout(context: Context, email: String, about: String) {
        val users = getRegisteredUsersArray(context)
        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.getString("email").trim().equals(email.trim(), ignoreCase = true)) {
                user.put("about", about) // create or update
                break
            }
        }
        usersPrefs(context).edit().putString(KEY_USERS_LIST, users.toString()).apply()
        saveUserAbout(context, about)
        Log.d("SessionDebug", "Updated about for $email")
    }

    fun getAboutForEmail(context: Context, email: String): String {
        if (email.isBlank()) return ""
        val users = getRegisteredUsersArray(context)
        for (i in 0 until users.length()) {
            val u = users.getJSONObject(i)
            if (u.getString("email").trim().equals(email.trim(), ignoreCase = true)) {
                return u.optString("about", "")
            }
        }
        return ""
    }

    /**
     * Clears only the session data (keeps all registered users).
     */
    fun clearSession(context: Context) {
        val prefs = sessionPrefs(context)
        prefs.edit().clear().apply()
        Log.d("SessionDebug", "Session cleared (users preserved)")
    }

    // ---------- REGISTERED USERS ----------
    fun getRegisteredUsersArray(context: Context): JSONArray {
        val json = usersPrefs(context).getString(KEY_USERS_LIST, "[]")
        return JSONArray(json)
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

    fun updateUserPoints(context: Context, email: String, newPoints: Int) {
        val users = getRegisteredUsersArray(context)
        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.getString("email").trim().equals(email.trim(), ignoreCase = true)) {
                user.put("points", newPoints)
                break
            }
        }
        usersPrefs(context).edit().putString(KEY_USERS_LIST, users.toString()).apply()
        saveUserPoints(context, newPoints)
        Log.d("SessionDebug", "Updated points for $email to $newPoints")
    }
}