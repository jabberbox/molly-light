package org.thoughtcrime.securesms.components.settings.app.appearance

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.signal.core.ui.compose.ComposeFragment
import org.signal.core.ui.compose.DayNightPreviews
import org.signal.core.ui.compose.Previews
import org.signal.core.ui.compose.Rows
import org.signal.core.ui.compose.Scaffolds
import org.signal.core.ui.compose.SignalIcons
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.compose.rememberStatusBarColorNestedScrollModifier
import org.thoughtcrime.securesms.keyvalue.SettingsValues

/**
 * Allows the user to change language from application settings.
 */
class AppearanceSettingsFragment : ComposeFragment() {

  private val viewModel: AppearanceSettingsViewModel by viewModels()

  @Composable
  override fun FragmentContent() {
    val callbacks = remember { Callbacks() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    AppearanceSettingsScreen(
      state = state,
      callbacks = callbacks
    )
  }

  private inner class Callbacks : AppearanceSettingsCallbacks {
    override fun onNavigationClick() {
      requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onLanguageSelected(selection: String) {
      MaterialAlertDialogBuilder(requireContext())
        .setMessage(R.string.preferences_language_change_confirmation_message)
        .setPositiveButton(android.R.string.ok) { _, _ -> viewModel.setLanguage(selection) }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
    }
  }
}

interface AppearanceSettingsCallbacks {
  fun onNavigationClick() = Unit
  fun onLanguageSelected(selection: String) = Unit

  object Empty : AppearanceSettingsCallbacks
}

@Composable
private fun AppearanceSettingsScreen(
  state: AppearanceSettingsState,
  callbacks: AppearanceSettingsCallbacks
) {
  Scaffolds.Settings(
    title = stringResource(R.string.preferences__appearance),
    onNavigationClick = callbacks::onNavigationClick,
    navigationIcon = SignalIcons.ArrowStart.imageVector
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .padding(paddingValues)
        .then(rememberStatusBarColorNestedScrollModifier())
    ) {
      item {
        Rows.RadioListRow(
          text = stringResource(R.string.preferences__language),
          labels = stringArrayResource(R.array.language_entries),
          values = stringArrayResource(R.array.language_values),
          selectedValue = state.language,
          onSelected = callbacks::onLanguageSelected
        )
      }

      // LIGHT-STYLE PASS: theme, dynamic colors, chat color/wallpaper, app icon,
      // message font size, and navigation bar size all removed from this screen.
      // The whole point of this reskin is one fixed, deliberately minimal look;
      // molly_background_light and molly_background_dark are both literally
      // #000000 already (see core/ui molly_colors.xml), so the theme picker in
      // particular had become a no-op that would just confuse someone who picked
      // "Light" and saw no change. Leaving these in as live-but-pointless (or
      // actively off-brand, for app icon and dynamic colors) settings didn't fit
      // the scope of the project. Language stays since it's a functional/
      // accessibility setting, not a look-and-feel one.
    }
  }
}

@DayNightPreviews
@Composable
private fun AppearanceSettingsScreenPreview() {
  Previews.Preview {
    AppearanceSettingsScreen(
      state = AppearanceSettingsState(
        theme = SettingsValues.Theme.SYSTEM,
        dynamicColors = true,
        messageFontSize = 0,
        language = "en-US",
        isCompactNavigationBar = false
      ),
      callbacks = AppearanceSettingsCallbacks.Empty
    )
  }
}
