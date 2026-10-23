package com.example.aigamingagent

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

class GameControlService : AccessibilityService() {
    companion object {
        const val PREFS = "agent_prefs"
        const val KEY_ACTION = "action"
        const val KEY_INSTRUCTION = "instruction"
        const val KEY_RUNNING = "running"
        @Volatile var requestRun = false
    }

    private val handler = Handler(Looper.getMainLooper())
    private var lastActionAt = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        handler.post(tick)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // This prototype does not inspect game content.
    }

    override fun onInterrupt() {
        handler.removeCallbacks(tick)
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        super.onDestroy()
    }

    private val tick = object : Runnable {
        override fun run() {
            try {
                val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
                val enabled = prefs.getBoolean(KEY_RUNNING, false)
                val now = System.currentTimeMillis()
                if (enabled && now - lastActionAt >= 1500L) {
                    val action = prefs.getString(KEY_ACTION, "Tap repeatedly") ?: "Tap repeatedly"
                    performChosenGesture(action)
                    lastActionAt = now
                }
            } finally {
                handler.postDelayed(this, 300L)
            }
        }
    }

    private fun performChosenGesture(action: String) {
        val dm = resources.displayMetrics
        val w = dm.widthPixels.toFloat()
        val h = dm.heightPixels.toFloat()
        val path = Path()

        when (action) {
            "Swipe left" -> {
                path.moveTo(w * 0.78f, h * 0.58f)
                path.lineTo(w * 0.25f, h * 0.58f)
            }
            "Swipe right" -> {
                path.moveTo(w * 0.25f, h * 0.58f)
                path.lineTo(w * 0.78f, h * 0.58f)
            }
            else -> {
                path.moveTo(w * 0.5f, h * 0.58f)
                path.lineTo(w * 0.5f, h * 0.58f)
            }
        }

        val stroke = GestureDescription.StrokeDescription(path, 0, 100)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }
}
