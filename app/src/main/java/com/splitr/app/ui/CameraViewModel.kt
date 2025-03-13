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
import com.splitr.app.data.Receipt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.file.Files.find
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

class CameraViewModel(
    private val database: AppDatabase
) : ViewModel() {
    private val _textRecognitionResult = MutableStateFlow<String?>(null)
    val textRecognitionResult: StateFlow<String?> = _textRecognitionResult

    private val recognizer: TextRecognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    fun processImage(imageProxy: ImageProxy) {
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
                val parsedText = visionText.text
                _textRecognitionResult.value = parsedText
                saveReceiptToDatabase(parsedText)
            }
            .addOnFailureListener { e ->
                Log.e("CameraViewModel", "Text recognition failed: ${e.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun saveReceiptToDatabase(parsedText: String) {
        val products = getTuples(parsedText)
        if (products.isEmpty()) return

        val receiptName = "Scanned Receipt"
        val receiptDate = Calendar.getInstance().time.toString()  // Example timestamp
        val totalAmount = products.sumOf { it.first }

        viewModelScope.launch {
            val receiptId = database.receiptDao().insertReceipt(
                Receipt(
                    name = receiptName,
                    date = receiptDate,
                    totalAmount = totalAmount
                )
            ).toInt()

            Log.e("Receipt", "Inserted Receipt - ID: $receiptId, Name: $receiptName, Date: $receiptDate, Total: $totalAmount")

            products.forEachIndexed { index, (price, name) ->
                if (name.isNotBlank()) {
                    val item = Item(
                        receiptId = receiptId,
                        name = name,
                        price = price,
                        quantity = 1
                    )
//                    database.itemDao().insertItem(item)
                    Log.e("Item", "Item #$index - Name: ${item.name}, Price: ${item.price}, Quantity: ${item.quantity}, Receipt ID: ${item.receiptId}")
                } else {
                    Log.e("Item", "Skipping empty item at index $index")
                }
            }
        }
    }

    // Extracts (price, product_name) pairs from parsed receipt text
    private fun getTuples(text: String): List<Pair<Double, String>> {
        val products = mutableListOf<Pair<Double, String>>()
        var produsCrt = 0
        var pretCrt = 0
        var numeCrt = 0
        var started = false

        val lines = text.split("\n")
        for (line in lines) {
            if ("total" in line.lowercase(Locale.getDefault()) || "*" in line.lowercase(Locale.getDefault())) {
                break
            }

            if ("x " in line.lowercase(Locale.getDefault())) {
                val trimmedLine = line.drop(line.lowercase(Locale.getDefault()).indexOf("x ") + 2).trim()
                if (trimmedLine.isEmpty()) continue

                val words = trimmedLine.split(' ')
                if (words.isNotEmpty()) {
                    words[0].toDoubleOrNull()?.let { nr ->
                        if (pretCrt == produsCrt) {
                            products.add(Pair(nr, ""))
                            produsCrt++
                            pretCrt++
                        } else {
                            products[pretCrt] = Pair(nr, products[pretCrt].second)
                            pretCrt++
                        }
                        started = true
                    }
                }
            } else if (started
                && !line[0].isDigit()
                && "discount" !in line.lowercase(Locale.getDefault())
                && "total" !in line.lowercase(Locale.getDefault())
                && "lei" != line.lowercase(Locale.getDefault())
                && "lel" != line.lowercase(Locale.getDefault())
            ) {
                if (numeCrt == produsCrt) {
                    products.add(Pair(0.0, line))
                    produsCrt++
                    numeCrt++
                } else {
                    products[numeCrt] = Pair(products[numeCrt].first, line)
                    numeCrt++
                }
            }
        }
        return products
    }

    fun clearResult() {
        _textRecognitionResult.value = null
    }

    override fun onCleared() {
        super.onCleared()
        recognizer.close()
    }
}