package com.statushdlite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.statushdlite.ui.theme.StatusHDLiteTheme

/**
 * Single entry point. All screens and navigation live in [AppNavigation];
 * this class just hosts the Compose tree.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StatusHDLiteTheme {
                AppNavigation()
            }
        }
    }
}
