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
import com.splitr.app.data.AppDatabase
import com.splitr.app.data.Item
import com.splitr.app.data.Parser
import com.splitr.app.data.ParserResult
import com.splitr.app.data.Receipt
import com.splitr.app.data.ReceiptDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.file.Files.find
import java.util.regex.Pattern

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
                val parserResult = Parser.parseReceipt(visionText, -1)

                // create and insert the receipt
                val receipt = Receipt(
                    name = parserResult.name,
                    date = parserResult.date,
                    totalAmount = parserResult.totalAmount
                )
                // Launch coroutine to insert receipt and items
                viewModelScope.launch {
                    Log.e("CameraViewModel", "Inserting receipt and items")
                    try {
                        val receiptId = receiptDao.insertReceipt(receipt).toInt()

                        // associate parsed items with the newly created receipt ID
                        val itemsWithReceiptId = parserResult.items.map { item ->
                            item.copy(receiptId = receiptId)
                        }

                        // insert items into the database
                        receiptDao.insertItems(itemsWithReceiptId)

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

    /**
     * Parses recognized text into Receipt and Item objects
     */
    private fun parseReceiptData(text: String) {
        val lines = text.lines().map { it.trim() }

        // Extract name
        val storeName = lines.firstOrNull { it.isNotBlank() } ?: "Unknown Store"

        // Extract date
        val datePattern = Pattern.compile("\\d{2}/\\d{2}/\\d{4}|\\d{4}-\\d{2}-\\d{2}")
        val date = lines.firstOrNull { datePattern.matcher(it).find() } ?: "Unknown Date"

        // Extract total amount
        val totalAmountPattern = Pattern.compile("(?i)(total|amount)\\s*[:$]*\\s*([\\d.]+)")
        val totalAmount = lines
            .firstOrNull { totalAmountPattern.matcher(it).find() }
            ?.let {
                totalAmountPattern.matcher(it).apply { find() }.group(2)?.toDoubleOrNull() ?: 0.0
            } ?: 0.0

        // Extract items
        val itemPattern = Pattern.compile("(.+?)\\s+(\\d+(\\.\\d{2})?)\\s*x\\s*(\\d+)")
        val items = lines.mapNotNull { line ->
            itemPattern.matcher(line).takeIf { it.find() }?.let {
                Item(
                    name = it.group(1).trim(),
                    price = it.group(2).toDouble(),
                    quantity = it.group(4).toInt(),
                    receiptId = 0 // Will be assigned when saved
                )
            }
        }

        _parsedReceipt.value = Receipt(
            name = storeName,
            date = date,
            totalAmount = totalAmount
        )

        _parsedItems.value = items
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