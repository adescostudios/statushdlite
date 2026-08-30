package com.statushdlite

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private const val PREFS_NAME = "statushd_prefs"
private const val KEY_ONBOARDED = "onboarded"

private object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val PERMISSION = "permission"
    const val HOME = "home"
    const val LIBRARY = "library"
    const val HELP = "help"
    const val FAQ = "faq"
    const val SETTINGS = "settings"
}

private fun hasOnboarded(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_ONBOARDED, false)

private fun markOnboarded(context: Context) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_ONBOARDED, true)
        .apply()
}

/**
 * Jumps to one of the 4 bottom-nav tab destinations from wherever the
 * person currently is, instead of pushing a fresh copy onto the back
 * stack every time. `popUpTo(HOME) { saveState = true }` + `launchSingleTop`
 * + `restoreState` is the standard bottom-nav pattern: it keeps Home as
 * the floor of the stack, avoids piling up duplicate Library/Help/FAQ
 * entries from repeated taps, and restores each tab's scroll/UI state
 * when you come back to it.
 *
 * This is deliberately separate from a screen's own "<-" back arrow,
 * which still calls [androidx.navigation.NavHostController.popBackStack]
 * directly — a tab tap means "jump to this section", a back arrow means
 * "undo the last navigation step". Conflating the two was why FAQ's
 * bottom-nav Home button used to behave differently from every other
 * screen's.
 */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Wires the 8 screens together. First launch goes
 * Splash -> Onboarding -> Permission -> Home. From Home onward, every
 * screen carries the full bottom nav (Home/Library/Help/FAQ) and any tab
 * is reachable from any other — see [navigateToTab]. Settings is reached
 * only via Home's gear icon and isn't itself a bottom-nav tab. Once
 * onboarding has been completed, the app starts straight on Home on
 * future launches.
 */
@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController: NavHostController = rememberNavController()
    val startDestination = remember {
        if (hasOnboarded(context)) Routes.HOME else Routes.SPLASH
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.SPLASH) {
            SplashScreen(onGetStarted = { navController.navigate(Routes.ONBOARDING) })
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onBack = { navController.popBackStack() },
                onFinished = {
                    markOnboarded(context)
                    navController.navigate(Routes.PERMISSION) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.PERMISSION) {
            PermissionScreen(
                onBack = { navController.popBackStack() },
                onContinue = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onBack = { navController.popBackStack() },
                onNavigateHelp = { navController.navigateToTab(Routes.HELP) },
                onNavigateFAQ = { navController.navigateToTab(Routes.FAQ) },
                onNavigateLibrary = { navController.navigateToTab(Routes.LIBRARY) },
                onNavigateSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.HELP) {
            HelpScreen(
                onBack = { navController.popBackStack() },
                onNavigateHome = { navController.navigateToTab(Routes.HOME) },
                onNavigateLibrary = { navController.navigateToTab(Routes.LIBRARY) },
                onNavigateFAQ = { navController.navigateToTab(Routes.FAQ) }
            )
        }
        composable(Routes.FAQ) {
            FAQScreen(
                onBack = { navController.popBackStack() },
                onNavigateHome = { navController.navigateToTab(Routes.HOME) },
                onNavigateLibrary = { navController.navigateToTab(Routes.LIBRARY) },
                onNavigateHelp = { navController.navigateToTab(Routes.HELP) }
            )
        }
        composable(Routes.LIBRARY) {
            LibraryScreen(
                onBack = { navController.popBackStack() },
                onNavigateHome = { navController.navigateToTab(Routes.HOME) },
                onNavigateHelp = { navController.navigateToTab(Routes.HELP) },
                onNavigateFAQ = { navController.navigateToTab(Routes.FAQ) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateHome = { navController.navigateToTab(Routes.HOME) },
                onNavigateLibrary = { navController.navigateToTab(Routes.LIBRARY) },
                onNavigateHelp = { navController.navigateToTab(Routes.HELP) },
                onNavigateFAQ = { navController.navigateToTab(Routes.FAQ) }
            )
        }
    }
}
