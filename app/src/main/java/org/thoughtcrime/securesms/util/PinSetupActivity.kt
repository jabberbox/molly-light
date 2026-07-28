package org.thoughtcrime.securesms.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.thoughtcrime.securesms.ScreenLockController

class PinSetupActivity : AppCompatActivity() {

    private val enteredDigits = StringBuilder()
    private var firstPin: String? = null
    private lateinit var titleView: TextView
    private lateinit var dotsView: TextView

    private val isDisableMode get() = intent.getBooleanExtra(EXTRA_DISABLE_MODE, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dp = resources.displayMetrics.density
        val MATCH = LinearLayout.LayoutParams.MATCH_PARENT
        val WRAP = LinearLayout.LayoutParams.WRAP_CONTENT

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(MATCH, MATCH)
        }

        // Back button row pinned to top
        root.addView(TextView(this).apply {
            text = "← Back"
            setTextColor(Color.GRAY)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding((24 * dp).toInt(), (56 * dp).toInt(), (24 * dp).toInt(), (24 * dp).toInt())
            setOnClickListener { finish() }
            layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        })

        // Top spacer
        root.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH, 0, 1f)
        })

        titleView = TextView(this).apply {
            text = if (isDisableMode) "Enter current PIN" else "Set a PIN"
            setTextColor(Color.WHITE)
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, (16 * dp).toInt())
            layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        }
        root.addView(titleView)

        dotsView = TextView(this).apply {
            text = "○  ○  ○  ○"
            setTextColor(Color.WHITE)
            textSize = 28f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, (16 * dp).toInt())
            layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        }
        root.addView(dotsView)

        // Keypad gets weight=3, bottom spacer weight=1 — always proportional
        root.addView(buildKeypad())

        root.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH, 0, 1f)
        })

        setContentView(root)
    }

    private fun buildKeypad(): LinearLayout {
        val MATCH = LinearLayout.LayoutParams.MATCH_PARENT

        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "⌫")
        )

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(MATCH, 0, 3f)
        }

        rows.forEach { row ->
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(MATCH, 0, 1f)
            }
            row.forEach { key ->
                rowLayout.addView(TextView(this).apply {
                    text = key
                    textSize = 28f
                    gravity = Gravity.CENTER
                    setTextColor(if (key.isEmpty()) Color.TRANSPARENT else Color.WHITE)
                    setOnClickListener { onKey(key) }
                    layoutParams = LinearLayout.LayoutParams(0, MATCH, 1f)
                })
            }
            container.addView(rowLayout)
        }
        return container
    }

    private fun onKey(key: String) {
        when (key) {
            "" -> Unit
            "⌫" -> if (enteredDigits.isNotEmpty()) {
                enteredDigits.deleteCharAt(enteredDigits.length - 1)
                updateDots()
            }
            else -> if (enteredDigits.length < 4) {
                enteredDigits.append(key)
                updateDots()
                if (enteredDigits.length == 4) handleComplete()
            }
        }
    }

    private fun updateDots() {
        val n = enteredDigits.length
        dotsView.text = (1..4).joinToString("  ") { if (it <= n) "●" else "○" }
    }

    private fun handleComplete() {
        val pin = enteredDigits.toString()
        when {
            isDisableMode -> {
                if (PinLockStorage.verifyPin(this, pin)) {
                    PinLockStorage.clearPin(this)
                    TextSecurePreferences.setPinLockEnabled(this, false)
                    if (!TextSecurePreferences.isBiometricScreenLockEnabled(this)) {
                        ScreenLockController.enableAutoLock(false)
                        ScreenLockController.lockImmediately = false
                    }
                    finish()
                } else {
                    showError()
                }
            }
            firstPin == null -> {
                firstPin = pin
                enteredDigits.clear()
                titleView.text = "Confirm PIN"
                dotsView.text = "○  ○  ○  ○"
            }
            pin == firstPin -> {
                PinLockStorage.setPin(this, pin)
                TextSecurePreferences.setPinLockEnabled(this, true)
                ScreenLockController.enableAutoLockSilently(true)
                ScreenLockController.lockImmediately = true
                finish()
            }
            else -> {
                firstPin = null
                enteredDigits.clear()
                titleView.text = "PINs didn't match. Try again"
                showError()
            }
        }
    }

    private fun showError() {
        enteredDigits.clear()
        dotsView.setTextColor(Color.RED)
        dotsView.postDelayed({
            dotsView.text = "○  ○  ○  ○"
            dotsView.setTextColor(Color.WHITE)
        }, 400)
    }

    companion object {
        private const val EXTRA_DISABLE_MODE = "disable_mode"

        @JvmStatic
        fun getIntentForSetup(context: Context): Intent =
            Intent(context, PinSetupActivity::class.java)

        @JvmStatic
        fun getIntentForDisable(context: Context): Intent =
            Intent(context, PinSetupActivity::class.java)
                .putExtra(EXTRA_DISABLE_MODE, true)
    }
}
