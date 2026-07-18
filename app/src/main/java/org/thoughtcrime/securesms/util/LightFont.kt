/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package org.thoughtcrime.securesms.util

import android.graphics.Typeface
import android.graphics.fonts.FontStyle
import android.graphics.fonts.SystemFonts
import android.os.Build
import org.signal.core.util.logging.Log
import kotlin.math.abs

/**
 * LIGHT-STYLE PASS: the Light Phone III's stock messaging app renders in
 * Akkurat, not the system default. The Light SDK ([com.thelightphone.sdk.ui.lightFontFamily])
 * doesn't bundle the font file itself (licensed), and instead looks it up
 * as an on-device system font at runtime -- LP3 hardware ships Akkurat
 * pre-installed. This mirrors that same lookup so Molly picks up the exact
 * same on-device font when running on LP3 hardware, with a graceful
 * fallback to the platform default everywhere else (emulators, other
 * devices).
 */
object LightFont {
  private val TAG = Log.tag(LightFont::class.java)

  private var attempted = false
  private var cached: Typeface? = null

  @Synchronized
  fun regular(): Typeface? {
    if (!attempted) {
      attempted = true
      cached = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) loadSystemAkkurat() else null
      if (cached == null) {
        Log.i(TAG, "Akkurat system font not found, falling back to default typeface")
      }
    }
    return cached
  }

  private fun loadSystemAkkurat(): Typeface? {
    return try {
      SystemFonts.getAvailableFonts()
        .filter { it.file?.name?.startsWith("Akkurat", ignoreCase = true) == true }
        .filter { it.style.slant == FontStyle.FONT_SLANT_UPRIGHT }
        .minByOrNull { abs(it.style.weight - FontStyle.FONT_WEIGHT_NORMAL) }
        ?.file
        ?.let { Typeface.Builder(it).build() }
    } catch (e: Exception) {
      Log.w(TAG, "Failed to load Akkurat system font", e)
      null
    }
  }
}
