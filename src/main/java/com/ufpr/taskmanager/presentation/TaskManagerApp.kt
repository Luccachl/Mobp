package com.ufpr.taskmanager.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.ufpr.taskmanager.presentation.navigation.AppNavigation
import com.ufpr.taskmanager.presentation.navigation.Screen
import com.ufpr.taskmanager.presentation.viewmodel.AuthenticationViewModel
import com.ufpr.taskmanager.presentation.viewmodel.TaskListViewModel
import com.ufpr.taskmanager.presentation.viewmodel.UserSessionStatus

@Composable
fun TaskManagerApp(
    authViewModel: AuthenticationViewModel,
    taskListViewModel: TaskListViewModel
) {
    val navController = rememberNavController()
    val authState by authViewModel.sessionState.collectAsState()

    val startDestination = when (authState) {
        is UserSessionStatus.Active -> Screen.Dashboard.route
        is UserSessionStatus.Inactive, is UserSessionStatus.AuthError -> Screen.Login.route
        else -> Screen.Login.route
    }

    LaunchedEffect(authState) {
        when (authState) {
            is UserSessionStatus.Active -> {
                if (navController.currentDestination?.route?.startsWith(Screen.Login.route) == true ||
                    navController.currentDestination?.route?.startsWith(Screen.Register.route) == true) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is UserSessionStatus.Inactive -> {
                if (navController.currentDestination?.route != Screen.Login.route) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            else -> {}
        }
    }

    AppNavigation(
        navController = navController,
        authViewModel = authViewModel,
        taskListViewModel = taskListViewModel,
        startDestination = startDestination
    )
}