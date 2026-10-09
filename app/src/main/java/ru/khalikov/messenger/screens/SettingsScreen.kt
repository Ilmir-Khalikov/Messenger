package ru.khalikov.messenger.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ru.khalikov.messenger.R

@Composable
fun SettingsScreen(navController: NavController, onSignOut: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Настройки",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        SettingsItem(
            iconResId = R.drawable.ic_person,
            title = stringResource(R.string.profile_title),
            onClick = { navController.navigate("edit_profile") }
        )

//        SettingsItem(
//            icon = Icons.Default.DarkMode,
//            title = "Тёмная тема",
//            onClick = { /* TODO: Реализовать позже */ }
//        )
//
//        SettingsItem(
//            icon = Icons.Default.Info,
//            title = "О приложении",
//            onClick = { /* TODO: Реализовать позже */ }
//        )

        SettingsItem(
            iconResId = R.drawable.ic_logout,
            title = stringResource(R.string.log_out_button),
            onClick = onSignOut
        )
    }
}

@Composable
fun SettingsItem(iconResId: Int, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}