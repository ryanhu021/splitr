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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel

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