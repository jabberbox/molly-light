/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.thoughtcrime.securesms.conversation.v2

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.signal.core.ui.FixedRoundedCornerBottomSheetDialogFragment
import org.thoughtcrime.securesms.R

/**
 * LIGHT-STYLE PASS: half-screen attachment picker shown from the resting
 * bottom bar's plus icon, matching the Light reference's camera / gallery /
 * audio message sheet. Replaces the old in-place attachment keyboard
 * ([org.thoughtcrime.securesms.conversation.v2.keyboard.AttachmentKeyboardFragment])
 * for this new bottom-bar entry point.
 */
class AttachmentPickerBottomSheetDialogFragment : FixedRoundedCornerBottomSheetDialogFragment() {

  companion object {
    const val TAG = "AttachmentPickerBottomSheetDialogFragment"
    const val RESULT_KEY = "AttachmentPickerBottomSheetDialogFragmentResult"
    const val ACTION_RESULT = "Action"
  }

  enum class Action {
    CAMERA,
    GALLERY,
    AUDIO
  }

  override val peekHeightPercentage: Float = 0.5f

  // LIGHT-STYLE PASS: the reference sheet is a flush black rectangle with no
  // rounded corners, seamless against the black conversation behind it.
  override val cornerRadius: Int = 0

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

    // LIGHT-STYLE PASS: halfExpandedRatio interacts with the base class's
    // peekHeight (computed against the full display height, not the dialog's
    // actual available height) and silently produced a sheet shorter than
    // requested, floating above the screen bottom instead of flush against
    // it. Fixing the content height directly and expanding to it, with
    // collapse skipped, is the reliable way to get an always-flush sheet at
    // a specific fraction of the screen.
    dialog.behavior.isFitToContents = true
    dialog.behavior.skipCollapsed = true
    dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

    return dialog
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.attachment_picker_bottom_sheet, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val params = view.layoutParams
    params.height = (resources.displayMetrics.heightPixels * peekHeightPercentage).toInt()
    view.layoutParams = params

    view.findViewById<View>(R.id.attachment_picker_camera).setOnClickListener {
      setFragmentResult(RESULT_KEY, bundleOf(ACTION_RESULT to Action.CAMERA))
      dismiss()
    }

    view.findViewById<View>(R.id.attachment_picker_gallery).setOnClickListener {
      setFragmentResult(RESULT_KEY, bundleOf(ACTION_RESULT to Action.GALLERY))
      dismiss()
    }

    view.findViewById<View>(R.id.attachment_picker_audio).setOnClickListener {
      setFragmentResult(RESULT_KEY, bundleOf(ACTION_RESULT to Action.AUDIO))
      dismiss()
    }

    view.findViewById<View>(R.id.attachment_picker_dismiss).setOnClickListener {
      dismiss()
    }
  }
}
