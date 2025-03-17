package com.splitr.app.ui

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.splitr.app.data.Item
import com.splitr.app.data.Parser
import com.splitr.app.data.ParserResult
import com.splitr.app.data.Receipt
import com.splitr.app.data.ReceiptDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CameraViewModel(
    private val receiptDao: ReceiptDao,
) : ViewModel() {
    // TODO: remove these
    private val _textRecognitionResult = MutableStateFlow<String?>(null)
    val textRecognitionResult: StateFlow<String?> = _textRecognitionResult

    private val _parsedReceipt = MutableStateFlow<Receipt?>(null)
    val parsedReceipt: StateFlow<Receipt?> = _parsedReceipt

    private val _parsedItems = MutableStateFlow<List<Item>>(emptyList())
    val parsedItems: StateFlow<List<Item>> = _parsedItems

    private val recognizer: TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    fun processImage(imageProxy: ImageProxy, onReceiptProcessed: (Int) -> Unit) {
        Log.e("CameraViewModel", "Processing image")
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                Log.e("CameraViewModel", "Text recognition successful")
                val parsedText = ""

                // parse receipt details and items
                var parserResult = ParserResult("", "", emptyList())
                try {
                    parserResult = Parser.parseReceipt(visionText, -1)
                } catch (e: Exception) {
                    Log.e("CameraViewModel", "Error parsing receipt data: ${e.message}")
                }

                // create and insert the receipt
                val receipt = Receipt(
                    name = parserResult.name,
                    date = parserResult.date,
                )
                // Launch coroutine to insert receipt and items
                viewModelScope.launch {
                    Log.e("CameraViewModel", "Inserting receipt and items")
                    try {
                        val receiptId = receiptDao.insertReceipt(receipt).toInt()

                        // associate parsed items with the newly created receipt ID
                        val itemsWithReceiptId = parserResult.items.map { item ->
                            Item(
                                receiptId = receiptId,
                                name = item.name,
                                price = item.price,
                                quantity = item.quantity
                            )
                        }

                        // insert items into the database
                        receiptDao.insertItemsAndUpdateTotal(itemsWithReceiptId)

                        _parsedReceipt.value = receipt.copy(id = receiptId)
                        _parsedItems.value = itemsWithReceiptId

                        Log.e("Parsed receipt", _parsedReceipt.value.toString())
                        Log.e("Parsed items", _parsedItems.value.toString())
                        // Navigate to ItemizedReceipt screen
                        onReceiptProcessed(receiptId)
                    } catch (e: Exception) {
                        Log.e("ReceiptProcessing", "Error processing receipt: ${e.message}")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("CameraViewModel", "Text recognition failed: ${e.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    fun clearResult() {
        _textRecognitionResult.value = null
        _parsedReceipt.value = null
        _parsedItems.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        recognizer.close()
    }
}