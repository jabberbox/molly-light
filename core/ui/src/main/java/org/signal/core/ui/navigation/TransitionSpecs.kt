/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.signal.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent

/**
 * A collection of [TransitionSpecs] for setting up nav3 navigation.
 */
object TransitionSpecs {

  /**
   * Screens slide in from the right and slide out from the left.
   */
  object HorizontalSlide {
    private const val DURATION = 200

    val transitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
      (
        slideInHorizontally(
          initialOffsetX = { it },
          animationSpec = tween(DURATION)
        ) + fadeIn(animationSpec = tween(DURATION))
        ) togetherWith
        (
          slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(DURATION)
          ) + fadeOut(animationSpec = tween(DURATION))
          )
    }

    val popTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
      (
        slideInHorizontally(
          initialOffsetX = { -it },
          animationSpec = tween(DURATION)
        ) + fadeIn(animationSpec = tween(DURATION))
        ) togetherWith
        (
          slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(DURATION)
          ) + fadeOut(animationSpec = tween(DURATION))
          )
    }

    val predictivePopTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.(@NavigationEvent.SwipeEdge Int) -> ContentTransform = {
      (
        slideInHorizontally(
          initialOffsetX = { -it },
          animationSpec = tween(DURATION)
        ) + fadeIn(animationSpec = tween(DURATION))
        ) togetherWith
        (
          slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(DURATION)
          ) + fadeOut(animationSpec = tween(DURATION))
          )
    }
  }

  /**
   * Screens slide in from the bottom and slide out to the bottom, like a sheet.
   */
  object VerticalSlide {
    private const val DURATION = 300

    val transitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
      slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(DURATION)
      ) + fadeIn(animationSpec = tween(DURATION)) togetherWith
        fadeOut(animationSpec = tween(DURATION))
    }

    val popTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
      fadeIn(animationSpec = tween(DURATION)) togetherWith
        slideOutVertically(
          targetOffsetY = { it },
          animationSpec = tween(DURATION)
        ) + fadeOut(animationSpec = tween(DURATION))
    }

    val predictivePopTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.(@NavigationEvent.SwipeEdge Int) -> ContentTransform = {
      fadeIn(animationSpec = tween(DURATION)) togetherWith
        slideOutVertically(
          targetOffsetY = { it },
          animationSpec = tween(DURATION)
        ) + fadeOut(animationSpec = tween(DURATION))
    }
  }

  /**
   * No enter/exit animation.
   */
  object None {
    val metadata: Map<String, Any> =
      NavDisplay.transitionSpec { EnterTransition.None togetherWith ExitTransition.None } +
        NavDisplay.popTransitionSpec { EnterTransition.None togetherWith ExitTransition.None } +
        NavDisplay.predictivePopTransitionSpec { EnterTransition.None togetherWith ExitTransition.None }

    // LIGHT-PERF PASS: same no-op transition as `metadata` above, but shaped as direct
    // NavDisplay transitionSpec/popTransitionSpec/predictivePopTransitionSpec lambdas
    // (like HorizontalSlide's) rather than per-entry metadata, for call sites that wire
    // NavDisplay directly. The Compose AnimatedContent slide+fade transition runs on the
    // Choreographer animation callback and was measured (via `dumpsys gfxinfo framestats`
    // on real hardware) taking 45-386ms per frame instead of its ~16ms budget, stalling
    // the traversal phase and dominating the visible lag when opening/closing a
    // conversation. Skipping it entirely is the direct fix rather than trying to make an
    // already-instant-feeling navigation model animate faster.
    val transitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
      EnterTransition.None togetherWith ExitTransition.None
    }

    val popTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
      EnterTransition.None togetherWith ExitTransition.None
    }

    val predictivePopTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.(@NavigationEvent.SwipeEdge Int) -> ContentTransform = {
      EnterTransition.None togetherWith ExitTransition.None
    }
  }
}
