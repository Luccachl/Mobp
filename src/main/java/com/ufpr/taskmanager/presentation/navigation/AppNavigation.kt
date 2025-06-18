package com.ufpr.taskmanager.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ufpr.taskmanager.presentation.ui.auth.LoginScreen
import com.ufpr.taskmanager.presentation.ui.auth.RegisterScreen
import com.ufpr.taskmanager.presentation.ui.dashboard.DashboardScreen
import com.ufpr.taskmanager.presentation.ui.profile.ProfileScreen
import com.ufpr.taskmanager.presentation.ui.task.AddEditTaskScreen
import com.ufpr.taskmanager.presentation.ui.task.TaskDetailScreen
import com.ufpr.taskmanager.presentation.ui.task.TaskListScreen
import com.ufpr.taskmanager.presentation.viewmodel.AuthenticationViewModel
import com.ufpr.taskmanager.presentation.viewmodel.TaskListViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthenticationViewModel,
    taskListViewModel: TaskListViewModel,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(navController, authViewModel)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController, authViewModel)
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController, taskListViewModel, authViewModel)
        }
        composable(Screen.TaskList.route) {
            TaskListScreen(navController, authViewModel, taskListViewModel)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController, authViewModel)
        }
        composable(
            route = Screen.TaskDetail.route + "/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            TaskDetailScreen(navController, taskListViewModel, taskId)
        }
        composable(
            route = Screen.AddEditTask.route + "?taskId={taskId}",
            arguments = listOf(navArgument("taskId") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            AddEditTaskScreen(navController, taskListViewModel, taskId)
        }
    }
}