package com.splitr.app.ui

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .height(70.dp)
                    .fillMaxWidth()
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    modifier = Modifier.padding(16.dp),
                    icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentRoute == Routes.Home.toString(),
                    onClick = {
                        navController.navigate(Routes.Home) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )

                NavigationBarItem(
                    modifier = Modifier.padding(16.dp),
                    icon = { Icon(Icons.Rounded.Add, contentDescription = "Camera") },
                    label = { Text("Camera") },
                    selected = currentRoute == Routes.Camera.toString(),
                    onClick = {
                        navController.navigate(Routes.Camera) {
                            launchSingleTop = true
                        }
                    }
                )

                NavigationBarItem(
                    modifier = Modifier.padding(16.dp),
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "People") },
                    label = { Text("People") },
                    selected = currentRoute == Routes.Collaborators().toString(),
                    onClick = {
                        navController.navigate(Routes.Collaborators()) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Routes.Home> {
                val viewModel: HomeViewModel = viewModel {
                    HomeViewModel(receiptDao)
                }
                HomeScreen(
                    onEditReceipt = { receiptId ->
                        navController.navigate(Routes.ItemizedReceipt(receiptId))
                    },
                    onViewBreakdown = { receiptId ->
                        navController.navigate(Routes.ReceiptBreakdown(receiptId))
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
                            navController.navigate(Routes.ReceiptBreakdown(details.receiptId))
                        },
                        onAddContributors = {
                            navController.navigate(Routes.Collaborators(details.receiptId))
                        },
                    )
                }
            }
            composable<Routes.ReceiptBreakdown> { backStackEntry ->
                val details: Routes.ReceiptBreakdown = backStackEntry.toRoute()
                val viewModel: ReceiptBreakdownViewModel = viewModel {
                    ReceiptBreakdownViewModel(receiptDao, details.receiptId)
                }
                val receiptWithItemsAndUsers by viewModel.receiptWithItemsAndUsers.collectAsState()

                receiptWithItemsAndUsers?.let {
                    ReceiptBreakdownScreen(
                        receiptWithItemsAndUsers = it,
                        onDone = { navController.navigate(Routes.Home) },
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
}