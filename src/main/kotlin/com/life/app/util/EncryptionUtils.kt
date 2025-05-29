package com.life.app.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Base64

/**
 * Utility class for handling encryption and decryption of sensitive data.
 */
@Singleton
class EncryptionUtils @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val MASTER_KEY_ALIAS = "life_master_key"
        private const val ENCRYPTED_PREFS_FILE = "life_encrypted_prefs"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val GCM_TAG_LENGTH = 128
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedSharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Store an encrypted value in SharedPreferences.
     */
    fun storeEncryptedValue(key: String, value: String) {
        encryptedSharedPreferences.edit().putString(key, value).apply()
    }

    /**
     * Retrieve a decrypted value from SharedPreferences.
     */
    fun getDecryptedValue(key: String, defaultValue: String = ""): String {
        return encryptedSharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    /**
     * Encrypt a string using the Android Keystore.
     */
    fun encrypt(plainText: String): String {
        try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            
            // Combine IV and encrypted data
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
            
            return Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            // In a real app, you would log this error and handle it appropriately
            return ""
        }
    }

    /**
     * Decrypt a string using the Android Keystore.
     */
    fun decrypt(encryptedText: String): String {
        try {
            val secretKey = getOrCreateSecretKey()
            val combined = Base64.decode(encryptedText, Base64.DEFAULT)
            
            // Extract IV from the combined data
            val iv = ByteArray(12) // GCM IV length is 12 bytes
            System.arraycopy(combined, 0, iv, 0, iv.size)
            
            // Extract encrypted data
            val encryptedBytes = ByteArray(combined.size - iv.size)
            System.arraycopy(combined, iv.size, encryptedBytes, 0, encryptedBytes.size)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            // In a real app, you would log this error and handle it appropriately
            return ""
        }
    }

    /**
     * Get or create a secret key for encryption/decryption.
     */
    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        
        if (keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            val entry = keyStore.getEntry(MASTER_KEY_ALIAS, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        }
        
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
}