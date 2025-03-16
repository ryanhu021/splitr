package com.splitr.app.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.splitr.app.data.AppDatabase
import kotlinx.serialization.Serializable

sealed class Routes {
    @Serializable
    data object Home

    @Serializable
    data object Camera

    @Serializable
    data class ItemizedReceipt(
        val receiptId: Int
    )

    @Serializable
    data class DistributeReceipt(
        val receiptId: Int
    )

    @Serializable
    data class ReceiptBreakdown(
        val receiptId: Int
    )

    @Serializable
    data class Collaborators(
        val receiptId: Int? = null
    )
}

@Composable
fun SplitrApp() {
    val navController = rememberNavController()

    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val receiptDao = remember { database.receiptDao() }


    NavHost(
        navController = navController,
        startDestination = Routes.Home
    ) {
        composable<Routes.Home> {
            val viewModel: HomeViewModel = viewModel {
                HomeViewModel(receiptDao)
            }
            HomeScreen(
                onEditReceipt = { receiptId ->
                    navController.navigate(Routes.ItemizedReceipt(receiptId))
                },
                onScanReceipt = { navController.navigate(Routes.Camera) },
                onManageCollaborators = { navController.navigate(Routes.Collaborators()) },
                viewModel
            )
        }
        composable<Routes.ItemizedReceipt> { backstackEntry ->
            val details: Routes.ItemizedReceipt = backstackEntry.toRoute()
            val viewModel: ItemizedReceiptViewModel = viewModel {
                ItemizedReceiptViewModel(receiptDao, details.receiptId)
            }
            val receiptWithItems by viewModel.receiptWithItems.collectAsState()

            receiptWithItems?.let {
                ItemizedReceiptScreen(
                    receiptWithItems = it,
                    onDoneClick = {
                        navController.navigateUp()
                    },
                    onNext = { receiptId ->
                        navController.navigate(Routes.DistributeReceipt(receiptId))
                    }
                )
            }
        }
        composable<Routes.DistributeReceipt> { backStackEntry ->
            val details: Routes.DistributeReceipt = backStackEntry.toRoute()
            val viewModel: DistributeReceiptViewModel = viewModel {
                DistributeReceiptViewModel(receiptDao, details.receiptId)
            }
            val receiptWithItemsAndUsers by viewModel.receiptWithItemsAndUsers.collectAsState()

            receiptWithItemsAndUsers?.let {
                DistributeReceiptScreen(
                    receiptWithItemsAndUsers = it,
                    onDone = { // TODO: Aggregate total page
                        navController.navigate(Routes.Home)
                    },
                    onViewBreakdown = {
                        // TODO: Navigate to breakdown screen
                    },
                    onAddContributors = {
                        navController.navigate(Routes.Collaborators(details.receiptId))
                    },
                )
            }
        }
        composable<Routes.Camera> {
            val viewModel: CameraViewModel = viewModel {
                CameraViewModel(receiptDao)
            }

            CameraScreen(
                viewModel = viewModel,
                onTextRecognized = { recognizedText ->
                    Log.e("CameraScreen", recognizedText)
                    println(recognizedText)
                },
                onReceiptProcessed = { receiptId ->
                    navController.navigate(Routes.ItemizedReceipt(receiptId))
                }
            )
        }

        composable<Routes.Collaborators> { backStackEntry ->
            val details: Routes.Collaborators = backStackEntry.toRoute()
            val viewModel: CollaboratorsViewModel = viewModel {
                CollaboratorsViewModel(receiptDao, details.receiptId)
            }

            CollaboratorsScreen(
                details.receiptId,
                onBack = { navController.navigateUp() },
                viewModel
            )
        }
    }
}