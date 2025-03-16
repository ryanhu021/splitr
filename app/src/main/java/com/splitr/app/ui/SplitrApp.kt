package com.splitr.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.splitr.app.data.AppDatabase
import com.splitr.app.data.Item
import com.splitr.app.data.Receipt
import com.splitr.app.data.ReceiptWithItems
import com.splitr.app.data.ReceiptWithItemsAndUsers
import com.splitr.app.data.User
import com.splitr.app.ui.theme.DistributeReceiptViewModel
import com.splitr.app.ui.theme.ItemizedReceiptViewModel
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
            HomeScreen(
                onEditReceipt = { receiptId ->
                    navController.navigate(Routes.ItemizedReceipt(receiptId))
                },
                onScanReceipt = { navController.navigate(Routes.Camera) },
                onManageCollaborators = { navController.navigate(Routes.Collaborators()) }
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

@Composable
fun HomeScreen(
    onEditReceipt: (Int) -> Unit,
    onScanReceipt: () -> Unit,
    onManageCollaborators: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val receipts by viewModel.receiptList.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Splitr", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onScanReceipt
        ) {
            Text("Scan New Receipt")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onManageCollaborators
        ) {
            Text("Manage Collaborators")
        }

        LazyColumn {
            items(receipts) { receiptWithItems ->
                ReceiptItem(receiptWithItems.receipt, onEditReceipt)
            }
        }
    }
}

@Composable
fun CameraScreen(
    onTextRecognized: (String) -> Unit,
    onReceiptProcessed: (Int) -> Unit,
    viewModel: CameraViewModel = viewModel(),
//    homeViewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val textRecognitionResult by viewModel.textRecognitionResult.collectAsState()
    val parsedReceipt by viewModel.parsedReceipt.collectAsState()
    val parsedItems by viewModel.parsedItems.collectAsState()
    var imageCapture = remember { mutableStateOf<ImageCapture?>(null) }

    // Handle recognized text
    LaunchedEffect(textRecognitionResult) {
        textRecognitionResult?.let {
            if (it.isNotBlank()) {
                onTextRecognized(it)
                viewModel.clearResult()
            }
        }
    }

    if (hasCameraPermission) {

        Box(modifier = Modifier.fillMaxSize()) {
            // Camera preview
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        imageCapture.value = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .build()

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture.value
                            )
                        } catch (e: Exception) {
                            Log.e("CameraScreen", "Camera binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Button(
                    onClick = {
                        Log.e("CameraScreen", "Taking picture...")
                        Log.e("CameraScreen", "imageCapture: $imageCapture")
                        imageCapture.value?.takePicture(
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                    Log.e("CameraScreen", "Image captured")
                                    viewModel.processImage(imageProxy, onReceiptProcessed)
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    Log.e("CameraScreen", "Image capture failed", exception)
                                }
                            }
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Take Picture",
                        tint = Color.White,
                    )
                }
            }
        }

//        parsedReceipt?.let { receipt ->
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//            ) {
//                Text("Store: ${receipt.name}")
//                Text("Date: ${receipt.date}")
//                Text("Total: $${receipt.totalAmount}")
//
//                parsedItems.forEach { item ->
//                    Text("${item.name} - $${item.price} x ${item.quantity}")
//                }
//
//                Button(onClick = {
//                    homeViewModel.addReceipt(receipt, parsedItems)
//                    viewModel.clearResult()
//                }) {
//                    Text("Save Receipt")
//                }
//            }
//        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Camera permission is required to scan receipts")
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
            ) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
fun ItemizedReceiptScreen(
    receiptWithItems: ReceiptWithItems,
    onDoneClick: () -> Unit,
    onNext: (Int) -> Unit,
    viewModel: ItemizedReceiptViewModel = viewModel()
) {
    var editableItems by remember { mutableStateOf(receiptWithItems.items) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Receipt Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Date: ${receiptWithItems.receipt.date}")

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                LazyColumn {
                    items(editableItems) { item ->
                        ItemRow(item) { updatedItem ->
                            editableItems = editableItems.map {
                                if (it.id == updatedItem.id) updatedItem else it
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                "Total",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${'$'}${receiptWithItems.receipt.totalAmount}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { /* TODO: Implement collaborator edit action */
                onNext(receiptWithItems.receipt.id)
            }
        ) {
            Text("Next")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            onClick = {
                viewModel.deleteReceipt(receiptWithItems.receipt)
                onDoneClick()
            }
        ) {
            Text("Delete Receipt", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Button to save changes
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                viewModel.updateItems(editableItems)
                onDoneClick()
            }
        ) {
            Text("Done")
        }
    }
}


@Composable
fun ReceiptItem(
    receipt: Receipt,
    onEditReceipt: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(receipt.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(receipt.date)
                Text("Total: ${'$'}${receipt.totalAmount}")
            }
            IconButton(onClick = { onEditReceipt(receipt.id) }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Receipt", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun ItemRow(
    item: Item,
    onItemChange: (Item) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(2f)) {
            TextField(
                value = item.name,
                onValueChange = { onItemChange(item.copy(name = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Item Name") },
                textStyle = TextStyle.Default.copy(fontWeight = FontWeight.Medium)
            )
//            TextField(
//                value = item.quantity.toString(),
//                onValueChange = {
//                    onItemChange(item.copy(quantity = it.toIntOrNull() ?: 0))
//                },
//                modifier = Modifier.fillMaxWidth(),
//                label = { Text("Quantity") },
//                textStyle = TextStyle.Default.copy(fontSize = 14.sp)
//            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        TextField(
            value = "${'$'}${item.price}",
            onValueChange = {
                val priceString = it.removePrefix("$")
                priceString.toDoubleOrNull()?.let { price ->
                    onItemChange(item.copy(price = price))
                }
            },
            modifier = Modifier.weight(1f),
            label = { Text("Price") },
            textStyle = TextStyle.Default.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun CollaboratorsScreen(
    receiptId: Int?,
    onBack: () -> Unit,
    viewModel: CollaboratorsViewModel = viewModel()
) {
    val collaborators by viewModel.collaborators.collectAsState()
    val selectedCollaborators by viewModel.selectedCollaborators.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newCollaboratorName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text("Manage Collaborators", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))
        if (receiptId != null) {
            Button(
                onClick = { onBack() },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text("Back", fontSize = 24.sp)
            }
            if (selectedCollaborators.isNotEmpty()) {
                Text("Selected Collaborators", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(selectedCollaborators) { user ->
                        CollaboratorItem(
                            user = user,
                            onClick = {
                                viewModel.toggleSelectedCollaboratorSelection(
                                    user,
                                    receiptId
                                )
                            },
                            onDelete = { viewModel.deleteCollaborator(user) }
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            Text("Available Collaborators", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }
        // Collaborators List
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(collaborators.filter { it !in selectedCollaborators }) { user ->
                CollaboratorItem(
                    user = user,
                    onClick = { viewModel.toggleSelectedCollaboratorSelection(user, receiptId) },
                    onDelete = { viewModel.deleteCollaborator(user) }
                )
            }
        }

        // "+" Button for Adding Collaborators
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
            ) {
                Text("+", fontSize = 24.sp)
            }
        }

        // Add Collaborator Dialog
        if (showAddDialog) {
            AddCollaboratorDialog(
                newCollaboratorName = newCollaboratorName,
                onNameChange = { newCollaboratorName = it },
                onAddClick = {
                    if (newCollaboratorName.isNotBlank()) {
                        viewModel.addCollaborator(User(name = newCollaboratorName.trim()))
                        newCollaboratorName = "" // Clear input field
                        showAddDialog = false  // Close the dialog
                    }
                },
                onDismiss = {
                    showAddDialog = false
                    newCollaboratorName = ""
                }
            )
        }
    }
}

@Composable
fun DistributeReceiptScreen(
    receiptWithItemsAndUsers: ReceiptWithItemsAndUsers,
    onDone: () -> Unit,
    onAddContributors: () -> Unit,
    viewModel: DistributeReceiptViewModel = viewModel()
) {
    // State to hold the collaborators selected in this screen
    val receiptContributors = viewModel.receiptContributors.collectAsState()

    LaunchedEffect(receiptWithItemsAndUsers.receipt.id) {
        viewModel.loadReceipt(receiptWithItemsAndUsers.receipt.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Distribute Receipt",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Show basic receipt information
        Text("Receipt: ${receiptWithItemsAndUsers.receipt.name}")
        Text("Date: ${receiptWithItemsAndUsers.receipt.date}")
        Spacer(modifier = Modifier.height(16.dp))

        // Display the list of items for context
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(receiptWithItemsAndUsers.itemsWithUsers) { itemWithUsers ->
                    Text("${itemWithUsers.item.name} - ${itemWithUsers.users}")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section for collaborator selection.
        // (You might later integrate a selection component here.)
        Text(
            "Selected Collaborators:",
            fontWeight = FontWeight.Bold
        )
        if (receiptContributors.value.isNotEmpty()) {
            Column(modifier = Modifier.padding(8.dp)) {
                receiptContributors.value.forEach { collaborator ->
                    Text(collaborator.name)
                }
            }
        } else {
            Text("No collaborators selected.", modifier = Modifier.padding(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to finalize distribution.
        Button(
            onClick = {
                onAddContributors()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Contributors")
        }

        Button(
            onClick = {
                onDone()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}


@Composable
fun AddCollaboratorDialog(
    newCollaboratorName: String,
    onNameChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Collaborator") },
        text = {
            TextField(
                value = newCollaboratorName,
                onValueChange = onNameChange,
                label = { Text("Collaborator Name") }
            )
        },
        confirmButton = {
            Button(onClick = onAddClick) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CollaboratorItem(
    user: User,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(user.name, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Collaborator",
                    tint = Color.Red
                )
            }
        }
    }
}