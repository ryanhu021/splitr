package com.splitr.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.splitr.app.ui.HomeScreen
import com.splitr.app.ui.theme.SplitrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplitrTheme {
                val navController = rememberNavController()
                HomeScreen(navController = navController)
            }
        }
    }
}