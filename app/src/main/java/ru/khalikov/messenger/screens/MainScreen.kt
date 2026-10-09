package ru.khalikov.messenger.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.khalikov.messenger.AuthState
import ru.khalikov.messenger.AuthViewModel
import ru.khalikov.messenger.R

@Composable
fun MainScreen(
    navController: androidx.navigation.NavController,
    authViewModel: AuthViewModel
) {
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    val bottomNavController = rememberNavController()
    val items = listOf(
        BottomNavItem("chats", "Чаты", R.drawable.ic_chat),
        BottomNavItem("contacts", "Контакты", R.drawable.ic_group),
        BottomNavItem("settings", "Настройки", R.drawable.ic_settings)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(painterResource(id = item.iconResId), contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "chats",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("chats") {}
            composable("contacts") {}
            composable("settings") {
                SettingsScreen(
                    navController = navController,
                    onSignOut = { authViewModel.signout() }
                )
            }
        }
    }
}

data class BottomNavItem(val route: String, val title: String, val iconResId: Int)