package ru.khalikov.messenger

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.khalikov.messenger.screens.HomeScreen
import ru.khalikov.messenger.screens.LoginScreen
import ru.khalikov.messenger.screens.SignupScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier, authViewModel : AuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login", builder = {
        composable(route = "login") {
            LoginScreen(modifier, navController, authViewModel)
        }
        composable(route = "signup") {
            SignupScreen(modifier, navController, authViewModel)
        }
        composable(route = "home") {
            HomeScreen(modifier, navController, authViewModel)
        }
    })
}