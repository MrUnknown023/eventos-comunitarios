package com.dsm.catedra2.eventos_comunitarios.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dsm.catedra2.eventos_comunitarios.view.EventDetailScreen
import com.dsm.catedra2.eventos_comunitarios.view.EventListScreen
import com.dsm.catedra2.eventos_comunitarios.viewmodel.CommentViewModel
import com.dsm.catedra2.eventos_comunitarios.viewmodel.EventViewModel

sealed class Screen(val route: String) {
    object EventList   : Screen("event_list")
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
}

@Composable
fun AppNavGraph(
    navController    : NavHostController = rememberNavController(),
    eventViewModel   : EventViewModel,
    commentViewModel : CommentViewModel,
    currentUserId    : String,
    currentUserEmail : String,
    currentUserRole  : String,
    onLogoutClick    : () -> Unit
) {
    NavHost(
        navController    = navController,
        startDestination = Screen.EventList.route
    ) {
        composable(Screen.EventList.route) {
            EventListScreen(
                viewModel        = eventViewModel,
                commentViewModel = commentViewModel,
                currentUserId    = currentUserId,
                currentUserEmail = currentUserEmail,
                currentUserRole  = currentUserRole,
                onLogoutClick    = onLogoutClick,
                onEventClick     = { eventId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                }
            )
        }

        composable(
            route     = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            EventDetailScreen(
                eventId          = eventId,
                eventViewModel   = eventViewModel,
                commentViewModel = commentViewModel,
                currentUserId    = currentUserId,
                currentUserEmail = currentUserEmail,
                onBack           = { navController.popBackStack() }
            )
        }
    }
}