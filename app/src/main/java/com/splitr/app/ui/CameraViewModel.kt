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

class CameraViewModel : ViewModel() {

    private val _receipt = MutableStateFlow<Receipt?>(null)
    val receipt: StateFlow<Receipt?> = _receipt

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items

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
                extractReceiptData(parsedText)
            }
            .addOnFailureListener { e ->
                Log.e("CameraViewModel", "Text recognition failed: ${e.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun extractReceiptData(parsedText: String) {
        val products = getTuples(parsedText)
        if (products.isEmpty()) return

        val receiptName = "Scanned Receipt"
        val receiptDate = Calendar.getInstance().time.toString()
        val totalAmount = products.sumOf { it.first }

        _receipt.value = Receipt(
            name = receiptName,
            date = receiptDate,
            totalAmount = totalAmount
        )

        _items.value = products.map { (price, name) ->
            Item(
                receiptId = 0, // Will be assigned when saved to the database
                name = name,
                price = price,
                quantity = 1
            )
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
        _receipt.value = null
        _items.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        recognizer.close()
    }
}