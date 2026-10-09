package ru.khalikov.messenger

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.khalikov.messenger.screens.LoginScreen
import ru.khalikov.messenger.screens.MainScreen
import ru.khalikov.messenger.screens.SignupScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier, authViewModel : AuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login", builder = {
        composable(route = "login") {
            LoginScreen(navController, authViewModel)
        }
        composable(route = "signup") {
            SignupScreen(navController, authViewModel)
        }
        composable(route = "main") {
            MainScreen(navController, authViewModel)
        }
    })
}