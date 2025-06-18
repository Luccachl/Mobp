package com.ufpr.taskmanager.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object Dashboard : Screen("dashboard_screen") // Nova tela
    object TaskList : Screen("task_list_screen")
    object TaskDetail : Screen("task_detail_screen") // Nova tela
    object AddEditTask : Screen("add_edit_task_screen")
    object Profile : Screen("profile_screen") // Nova tela

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}