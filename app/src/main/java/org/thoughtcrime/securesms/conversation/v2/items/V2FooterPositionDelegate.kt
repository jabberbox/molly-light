/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.thoughtcrime.securesms.conversation.v2.items

import android.view.View
import android.widget.Space
import org.signal.core.ui.view.Stub
import org.signal.core.util.BidiUtil
import org.signal.core.util.dp
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.emoji.EmojiTextView
import org.thoughtcrime.securesms.util.ViewUtil
import org.thoughtcrime.securesms.util.padding
import org.thoughtcrime.securesms.util.visible
import kotlin.math.max

/**
 * Logical delegate for determining the footer position for a particular conversation item.
 */
class V2FooterPositionDelegate private constructor(
  private val root: V2ConversationItemLayout,
  private val footerViews: List<View>,
  private val bodyContainer: View,
  private val body: EmojiTextView,
  private val footerSpacer: Space?,
  private val thumbnailView: Stub<V2ConversationItemThumbnail>?
) : V2ConversationItemLayout.OnMeasureListener {

  constructor(binding: V2ConversationItemTextOnlyBindingBridge) : this(
    binding.root,
    listOfNotNull(
      binding.footerDate,
      binding.deliveryStatus,
      binding.footerExpiry,
      binding.footerSpace,
      binding.footerPinned,
      binding.footerStarred
    ),
    binding.bodyWrapper,
    binding.body,
    binding.footerSpace,
    null
  )

  constructor(binding: V2ConversationItemMediaBindingBridge) : this(
    binding.textBridge.root,
    listOfNotNull(
      binding.textBridge.footerDate,
      binding.textBridge.deliveryStatus,
      binding.textBridge.footerExpiry,
      binding.textBridge.footerSpace,
      binding.textBridge.footerPinned
    ),
    binding.textBridge.bodyWrapper,
    binding.textBridge.body,
    binding.textBridge.footerSpace,
    binding.thumbnailStub
  )

  private val gutters = 48.dp + 16.dp
  private val horizontalFooterPadding = root.context.resources.getDimensionPixelOffset(R.dimen.message_bubble_horizontal_padding)

  private var displayState: DisplayState = DisplayState.NONE

  override fun onPreMeasure() {
    displayTuckedIntoBody()
  }

  override fun onPostMeasure(): Boolean {
    val maxWidth = if (thumbnailView?.isVisible == true) {
      thumbnailView.get().layoutParams.width
    } else {
      root.measuredWidth - gutters
    }

    val lastLineWidth = body.lastLineWidth
    val footerWidth = getFooterWidth()

    if (footerViews.all { !it.visible }) {
      return false
    }

    if (body.isJumbomoji || BidiUtil.hasMixedTextDirection(body.text)) {
      displayUnderneathBody()
      return true
    }

    val availableTuck = bodyContainer.measuredWidth - lastLineWidth - (horizontalFooterPadding * 2)
    if (body.lineCount > 1 && availableTuck > footerWidth) {
      return false
    }

    // LIGHT-STYLE PASS: previously, single-line messages with room to spare
    // tucked the footer (checkmark/pin/star) into the end of the line by
    // reserving right-side padding on the body text. Since that padding varies
    // with whatever footer content happens to be visible per-message (often
    // nothing at all now that the plain timestamp lives in the cluster header),
    // it made sent/received text drift left or right inconsistently between
    // messages. Footer content always renders on its own row underneath now,
    // so every message's text lines up flush regardless of its footer.
    displayUnderneathBody()
    return true
  }

  private fun displayUnderneathBody() {
    if (displayState == DisplayState.UNDERNEATH) {
      return
    }

    val horizontalPadding = max(0, getFooterWidth() - (footerSpacer?.measuredWidth ?: 0) - body.measuredWidth)
    val (left, right) = if (bodyContainer.layoutDirection == View.LAYOUT_DIRECTION_LTR) {
      0 to horizontalPadding
    } else {
      horizontalPadding to 0
    }

    body.padding(right = right, left = left, bottom = footerViews.first().measuredHeight)
    displayState = DisplayState.UNDERNEATH
  }

  private fun displayTuckedIntoBody() {
    if (displayState == DisplayState.TUCKED) {
      return
    }

    body.padding(right = 0, left = 0, bottom = 0)
    displayState = DisplayState.TUCKED
  }

  private fun getFooterWidth(): Int {
    return footerViews.sumOf { it.measuredWidth + ViewUtil.getLeftMargin(it) + ViewUtil.getRightMargin(it) }
  }

  private enum class DisplayState {
    NONE,
    UNDERNEATH,
    TUCKED
  }
}
