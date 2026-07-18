/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.thoughtcrime.securesms.main

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.signal.core.ui.compose.DayNightPreviews
import org.signal.core.ui.compose.Previews
import org.signal.core.ui.compose.theme.colorAttribute
import org.thoughtcrime.securesms.R
import org.signal.core.ui.R as CoreUiR

// LIGHT-STYLE PASS: uniform icon size app-wide, derived from a caliper
// measurement against physical Light Phone III hardware (~5.6mm target).
private val NAV_ICON_SIZE = 32.dp

enum class MainNavigationListLocation(
  @StringRes val label: Int,
  @RawRes val icon: Int,
  @StringRes val contentDescription: Int = label
) {
  CHATS(
    label = R.string.ConversationListTabs__chats,
    icon = R.raw.chats_28
  ),
  ARCHIVE(
    label = R.string.ConversationListTabs__chats,
    icon = R.raw.chats_28
  ),
  CALLS(
    label = R.string.ConversationListTabs__calls,
    icon = R.raw.calls_28
  ),
  STORIES(
    label = R.string.ConversationListTabs__stories,
    icon = R.raw.stories_28
  );

  val isChatsTab: Boolean
    get() = this == CHATS || this == ARCHIVE
}

data class MainNavigationState(
  val chatsCount: Int = 0,
  val callsCount: Int = 0,
  val storiesCount: Int = 0,
  val storyFailure: Boolean = false,
  val isStoriesFeatureEnabled: Boolean = true,
  val currentListLocation: MainNavigationListLocation = MainNavigationListLocation.CHATS,
  val compact: Boolean = false
)

/**
 * Chats list bottom navigation bar.
 */
@Composable
fun MainNavigationBar(
  state: MainNavigationState,
  mainFloatingActionButtonsCallback: MainFloatingActionButtonsCallback,
  onDestinationSelected: (MainNavigationListLocation) -> Unit
) {
  NavigationBar(
    containerColor = colorAttribute(R.attr.navbar_container_color),
    contentColor = MaterialTheme.colorScheme.onSurface,
    modifier = Modifier.height(48.dp),
    windowInsets = WindowInsets(0, 0, 0, 0)
  ) {
    val entries = remember(state.isStoriesFeatureEnabled) {
      if (state.isStoriesFeatureEnabled) {
        MainNavigationListLocation.entries.filterNot { it == MainNavigationListLocation.ARCHIVE }
      } else {
        MainNavigationListLocation.entries.filterNot { it == MainNavigationListLocation.STORIES || it == MainNavigationListLocation.ARCHIVE }
      }
    }

    entries.forEach { destination ->

      val badgeCount = when (destination) {
        MainNavigationListLocation.ARCHIVE -> error("Not supported")
        MainNavigationListLocation.CHATS -> state.chatsCount
        MainNavigationListLocation.CALLS -> state.callsCount
        MainNavigationListLocation.STORIES -> state.storiesCount
      }

      val selected = state.currentListLocation == destination
      NavigationBarItem(
        colors = NavigationBarItemDefaults.colors(
          indicatorColor = colorAttribute(R.attr.navbar_active_indicator_color),
          selectedTextColor = MaterialTheme.colorScheme.onSurface,
        ),
        selected = selected,
        icon = {
          NavigationDestinationIcon(
            destination = destination,
            selected = selected
          )
        },
        // LIGHT-STYLE PASS: no text labels under the icons, matching the Light
        // reference's icon-only bottom bar.
        label = null,
        onClick = {
          onDestinationSelected(destination)
        },
        modifier = Modifier.drawNavigationBarBadge(count = badgeCount, compact = state.compact)
      )
    }

    // LIGHT-STYLE PASS: camera and the primary action (compose/new call) used to
    // float above the bar as separate FABs; folded into the same row here so the
    // whole bottom chrome reads as one flat icon strip, matching the Light
    // reference's single row of icons. Shown on Calls too (not just Chats) so
    // the bar stays a consistent 4 icons across both tabs -- tapping it opens
    // the exact same camera flow as the Chats tab's camera icon.
    if (state.currentListLocation == MainNavigationListLocation.CHATS || state.currentListLocation == MainNavigationListLocation.ARCHIVE || state.currentListLocation == MainNavigationListLocation.CALLS) {
      NavigationBarItem(
        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
        selected = false,
        icon = {
          // LIGHT-STYLE PASS: real Light SDK asset (light_ic_camera), sized to
          // NAV_ICON_SIZE to match the app-wide uniform icon scale.
          Icon(
            imageVector = ImageVector.vectorResource(R.drawable.light_ic_camera),
            contentDescription = stringResource(R.string.conversation_list_fragment__open_camera_description),
            modifier = Modifier.size(NAV_ICON_SIZE)
          )
        },
        label = null,
        onClick = {
          mainFloatingActionButtonsCallback.onCameraClick(MainNavigationListLocation.CHATS)
        }
      )
    }

    NavigationBarItem(
      colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
      selected = false,
      icon = {
        // LIGHT-STYLE PASS: real Light SDK asset (light_ic_compose, the
        // pencil-with-plus icon) for the chats compose action.
        val (icon, contentDescriptionId) = when (state.currentListLocation) {
          MainNavigationListLocation.ARCHIVE, MainNavigationListLocation.CHATS -> R.drawable.light_ic_compose to R.string.conversation_list_fragment__fab_content_description
          MainNavigationListLocation.CALLS -> R.drawable.symbol_phone_plus_24 to R.string.CallLogFragment__start_a_new_call
          MainNavigationListLocation.STORIES -> CoreUiR.drawable.symbol_camera_24 to R.string.conversation_list_fragment__open_camera_description
        }

        Icon(
          imageVector = ImageVector.vectorResource(icon),
          contentDescription = stringResource(contentDescriptionId),
          modifier = Modifier.size(NAV_ICON_SIZE)
        )
      },
      label = null,
      onClick = {
        when (state.currentListLocation) {
          MainNavigationListLocation.ARCHIVE, MainNavigationListLocation.CHATS -> mainFloatingActionButtonsCallback.onNewChatClick()
          MainNavigationListLocation.CALLS -> mainFloatingActionButtonsCallback.onNewCallClick()
          MainNavigationListLocation.STORIES -> mainFloatingActionButtonsCallback.onCameraClick(MainNavigationListLocation.STORIES)
        }
      }
    )
  }
}

/**
 * Draws badge over navigation bar item. We do this since they're required to be inside a row,
 * and things get really funky or clip weird if we try to use a normal composable.
 */
@Composable
private fun Modifier.drawNavigationBarBadge(count: Int, compact: Boolean): Modifier {
  return if (count <= 0) {
    this
  } else {
    val formatted = formatCount(count)
    val textMeasurer = rememberTextMeasurer()
    val color = colorResource(R.color.ConversationListTabs__unread)
    val textStyle = MaterialTheme.typography.labelMedium
    val textLayoutResult = remember(formatted) {
      textMeasurer.measure(formatted, textStyle)
    }

    var size by remember { mutableStateOf(IntSize.Zero) }

    val padding = with(LocalDensity.current) {
      4.dp.toPx()
    }

    val xOffsetExtra = with(LocalDensity.current) {
      4.dp.toPx()
    }

    val yOffset = with(LocalDensity.current) {
      if (compact) 6.dp.toPx() else 10.dp.toPx()
    }

    this
      .onSizeChanged {
        size = it
      }
      .drawWithContent {
        drawContent()

        val xOffset = size.width.toFloat() / 2f + xOffsetExtra
        val yRadius = size.height.toFloat() / 2f

        if (size != IntSize.Zero) {
          drawRoundRect(
            color = color,
            topLeft = Offset(xOffset, yOffset),
            size = Size(textLayoutResult.size.width.toFloat() + padding * 2, textLayoutResult.size.height.toFloat()),
            cornerRadius = CornerRadius(yRadius, yRadius)
          )

          drawText(
            textLayoutResult = textLayoutResult,
            color = Color.White,
            topLeft = Offset(xOffset + padding, yOffset)
          )
        }
      }
  }
}

/**
 * Navigation Rail for medium and large form factor devices.
 */
@Composable
fun MainNavigationRail(
  state: MainNavigationState,
  mainFloatingActionButtonsCallback: MainFloatingActionButtonsCallback,
  onDestinationSelected: (MainNavigationListLocation) -> Unit
) {
  NavigationRail(
    containerColor = colorAttribute(R.attr.navbar_container_color),
  ) {
    Spacer(modifier = Modifier.height(40.dp).weight(1f, fill = false))

    MainFloatingActionButtons(
      destination = state.currentListLocation,
      callback = mainFloatingActionButtonsCallback,
      modifier = Modifier.padding(vertical = 8.dp)
    )

    Spacer(modifier = Modifier.height(40.dp).weight(1f, fill = false))

    val entries = remember(state.isStoriesFeatureEnabled) {
      if (state.isStoriesFeatureEnabled) {
        MainNavigationListLocation.entries.filterNot { it == MainNavigationListLocation.ARCHIVE }
      } else {
        MainNavigationListLocation.entries.filterNot { it == MainNavigationListLocation.STORIES || it == MainNavigationListLocation.ARCHIVE }
      }
    }

    val selectedDestination = if (state.currentListLocation == MainNavigationListLocation.ARCHIVE) {
      MainNavigationListLocation.CHATS
    } else {
      state.currentListLocation
    }

    entries.forEachIndexed { idx, destination ->
      val selected = selectedDestination == destination

      Box {
        NavigationRailItem(
          colors = NavigationRailItemDefaults.colors(
            indicatorColor = colorAttribute(R.attr.navbar_active_indicator_color)
          ),
          modifier = Modifier.padding(bottom = if (MainNavigationListLocation.entries.lastIndex == idx) 0.dp else 16.dp),
          icon = {
            NavigationDestinationIcon(
              destination = destination,
              selected = selected
            )
          },
          label = {
            NavigationDestinationLabel(destination)
          },
          selected = selected,
          onClick = {
            onDestinationSelected(destination)
          }
        )

        NavigationRailCountIndicator(
          state = state,
          destination = destination
        )
      }
    }
  }
}

@Composable
private fun BoxScope.NavigationRailCountIndicator(
  state: MainNavigationState,
  destination: MainNavigationListLocation
) {
  val count = remember(state, destination) {
    when (destination) {
      MainNavigationListLocation.ARCHIVE -> error("Not supported")
      MainNavigationListLocation.CHATS -> state.chatsCount
      MainNavigationListLocation.CALLS -> state.callsCount
      MainNavigationListLocation.STORIES -> state.storiesCount
    }
  }

  if (count > 0) {
    Box(
      modifier = Modifier
        .padding(start = 42.dp)
        .height(16.dp)
        .defaultMinSize(minWidth = 16.dp)
        .background(color = colorResource(R.color.ConversationListTabs__unread), shape = RoundedCornerShape(percent = 50))
        .align(Alignment.TopStart)
    ) {
      Text(
        text = formatCount(count),
        style = MaterialTheme.typography.labelMedium,
        color = Color.White,
        modifier = Modifier
          .align(Alignment.Center)
          .padding(horizontal = 4.dp)
      )
    }
  }
}

@Composable
private fun NavigationDestinationIcon(
  destination: MainNavigationListLocation,
  selected: Boolean
) {
  // LIGHT-STYLE PASS: replaced the two-state Lottie animation with a plain
  // static icon -- matches Light's preference for instant, unanimated state
  // changes, and lets the calls-tab icon be the real Light SDK asset
  // (light_ic_call) rather than Signal's own animated glyph. No authentic
  // Light asset exists for a chats/message-list icon, so that one keeps
  // Signal's own plain chat-bubble outline. Selection is still conveyed by
  // the existing indicator-pill background, so no selected/unselected icon
  // variant is needed.
  val icon = when (destination) {
    MainNavigationListLocation.CHATS, MainNavigationListLocation.ARCHIVE -> R.drawable.symbol_chat_24
    MainNavigationListLocation.CALLS -> R.drawable.light_ic_call
    MainNavigationListLocation.STORIES -> CoreUiR.drawable.symbol_camera_24
  }

  Icon(
    imageVector = ImageVector.vectorResource(icon),
    contentDescription = null,
    tint = MaterialTheme.colorScheme.onSurface,
    modifier = Modifier.size(NAV_ICON_SIZE)
  )
}

@Composable
private fun NavigationDestinationLabel(destination: MainNavigationListLocation) {
  Text(stringResource(destination.label))
}

@Composable
private fun formatCount(count: Int): String {
  if (count > 99) {
    return stringResource(R.string.ConversationListTabs__99p)
  }
  return count.toString()
}

@DayNightPreviews
@Preview(device = "spec:parent=pixel_7,orientation=landscape")
@Composable
private fun MainNavigationRailPreview() {
  Previews.Preview {
    var selected by remember { mutableStateOf(MainNavigationListLocation.CHATS) }

    MainNavigationRail(
      state = MainNavigationState(
        chatsCount = 500,
        callsCount = 10,
        storiesCount = 5,
        currentListLocation = selected
      ),
      mainFloatingActionButtonsCallback = MainFloatingActionButtonsCallback.Empty,
      onDestinationSelected = { selected = it }
    )
  }
}

@DayNightPreviews
@Composable
private fun MainNavigationBarPreview() {
  Previews.Preview {
    var selected by remember { mutableStateOf(MainNavigationListLocation.CHATS) }

    MainNavigationBar(
      state = MainNavigationState(
        chatsCount = 500,
        callsCount = 10,
        storiesCount = 5,
        currentListLocation = selected,
        compact = false
      ),
      mainFloatingActionButtonsCallback = MainFloatingActionButtonsCallback.Empty,
      onDestinationSelected = { selected = it }
    )
  }
}
