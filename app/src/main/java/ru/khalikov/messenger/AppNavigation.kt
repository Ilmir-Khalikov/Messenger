package ru.khalikov.messenger

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.khalikov.messenger.screens.LoginScreen
import ru.khalikov.messenger.screens.MainScreen
import ru.khalikov.messenger.screens.ProfileEditorScreen
import ru.khalikov.messenger.screens.SignupScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable(route = "login") {
            LoginScreen(navController, authViewModel)
        }
        composable(route = "signup") {
            SignupScreen(navController, authViewModel)
        }
        composable(
            route = "profile/{mode}",
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val isSetup = backStackEntry.arguments?.getString("mode") == "setup"
            ProfileEditorScreen(
                navController = navController,
                authViewModel = authViewModel,
                isSetup = isSetup
            )
        }
        composable(route = "main") {
            MainScreen(navController, authViewModel)
        }
    }
}
