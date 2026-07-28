package org.thoughtcrime.securesms.biometric

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.thoughtcrime.securesms.ScreenLockController
import org.thoughtcrime.securesms.util.PinLockStorage

class PinLockDialogFragment : DialogFragment() {

    interface Listener {
        fun onSuccess()
        fun onCancel()
    }

    private val enteredDigits = StringBuilder()
    private var dotsView: TextView? = null
    var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            it.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            ScreenLockController.showWhenLocked(it)
        }
        dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                listener?.onCancel()
                true
            } else {
                false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val dp = requireContext().resources.displayMetrics.density

        val MATCH = LinearLayout.LayoutParams.MATCH_PARENT
        val WRAP = LinearLayout.LayoutParams.WRAP_CONTENT

        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(MATCH, MATCH)
        }

        root.addView(View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH, 0, 1f)
        })

        root.addView(TextView(requireContext()).apply {
            text = "Enter PIN"
            setTextColor(Color.WHITE)
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, (16 * dp).toInt())
            layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        })

        val dots = TextView(requireContext()).apply {
            text = "○  ○  ○  ○"
            setTextColor(Color.WHITE)
            textSize = 28f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, (16 * dp).toInt())
            layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        }
        dotsView = dots
        root.addView(dots)

        root.addView(buildKeypad())

        root.addView(View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH, 0, 1f)
        })

        return root
    }

    private fun buildKeypad(): LinearLayout {
        val MATCH = LinearLayout.LayoutParams.MATCH_PARENT

        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "⌫")
        )

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(MATCH, 0, 3f)
        }

        rows.forEach { row ->
            val rowLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(MATCH, 0, 1f)
            }
            row.forEach { key ->
                rowLayout.addView(TextView(requireContext()).apply {
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
                if (enteredDigits.length == 4) verify()
            }
        }
    }

    private fun updateDots() {
        val n = enteredDigits.length
        dotsView?.text = (1..4).joinToString("  ") { if (it <= n) "●" else "○" }
    }

    private fun verify() {
        val ctx = requireContext()
        if (PinLockStorage.verifyPin(ctx, enteredDigits.toString())) {
            ScreenLockController.lockScreenAtStart = false
            dismissAllowingStateLoss()
            listener?.onSuccess()
        } else {
            enteredDigits.clear()
            dotsView?.setTextColor(Color.RED)
            dotsView?.postDelayed({
                dotsView?.text = "○  ○  ○  ○"
                dotsView?.setTextColor(Color.WHITE)
            }, 400)
        }
    }

    companion object {
        private const val TAG = "PinLockDialogFragment"

        @JvmStatic
        fun show(manager: FragmentManager, listener: Listener) {
            val existing = manager.findFragmentByTag(TAG) as? PinLockDialogFragment
            if (existing != null) {
                existing.listener = listener
                return
            }
            PinLockDialogFragment().apply {
                this.listener = listener
                isCancelable = false
            }.show(manager, TAG)
        }
    }
}
