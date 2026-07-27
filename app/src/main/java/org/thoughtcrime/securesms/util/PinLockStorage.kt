package org.thoughtcrime.securesms.util

import android.content.Context
import java.security.MessageDigest
import java.security.SecureRandom

object PinLockStorage {
    private const val KEY_PIN_HASH = "pin_lock_hash"
    private const val KEY_PIN_SALT = "pin_lock_salt"

    fun setPin(context: Context, pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        SecurePreferenceManager.getSecurePreferences(context).edit()
            .putString(KEY_PIN_HASH, hash(pin, salt).toHex())
            .putString(KEY_PIN_SALT, salt.toHex())
            .apply()
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        val prefs = SecurePreferenceManager.getSecurePreferences(context)
        val storedHash = prefs.getString(KEY_PIN_HASH, null)?.fromHex() ?: return false
        val salt = prefs.getString(KEY_PIN_SALT, null)?.fromHex() ?: return false
        return MessageDigest.isEqual(hash(pin, salt), storedHash)
    }

    fun hasPin(context: Context): Boolean =
        SecurePreferenceManager.getSecurePreferences(context).contains(KEY_PIN_HASH)

    fun clearPin(context: Context) {
        SecurePreferenceManager.getSecurePreferences(context).edit()
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_SALT)
            .apply()
    }

    private fun hash(pin: String, salt: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").apply { update(salt) }.digest(pin.toByteArray())

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.fromHex(): ByteArray {
        val result = ByteArray(length / 2)
        for (i in result.indices) {
            result[i] = ((Character.digit(this[i * 2], 16) shl 4) + Character.digit(this[i * 2 + 1], 16)).toByte()
        }
        return result
    }
}
