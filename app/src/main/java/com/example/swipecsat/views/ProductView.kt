package com.example.swipecsat.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.swipecsat.ui.theme.SwipeCSATTheme

@Composable
fun ProductIsOrdered() {

}

@Preview
@Composable
fun ProductIsOrderedPreview() {
    SwipeCSATTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ProductIsOrdered()
        }
    }
}