package com.example.swipecsat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.swipecsat.ui.theme.SwipeCSATTheme
import com.example.swipecsat.viewmodels.PollViewModel
import com.example.swipecsat.views.PollScreen
import com.example.swipecsat.views.RegistrationScreen


class MainActivity : ComponentActivity() {
    private var appInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pollViewModel = ViewModelProvider(this)[PollViewModel::class.java]

        if (!appInitialized) {
            checkPermissions(this, applicationContext)

//            val sp = getSharedPreferences("MySharedPref", MODE_PRIVATE)
//            val spEdit = sp.edit()
//
//            var uId = sp.getString("uId", "")
//
//            if (uId != "") {
//                pollViewModel.uId.value = uId
//            } else {
//                uId = Random(System.currentTimeMillis()).nextLong().toString()
//                spEdit.putString("uId", uId)
//                spEdit.apply()
//            }

            val productName = intent.extras?.getString("productName")
//            Toast.makeText(this, productName ?: "Товар", Toast.LENGTH_SHORT).show()
//            Toast.makeText(this, intent.extras?.getString("test") ?: "", Toast.LENGTH_SHORT).show()
            pollViewModel.loadQuestions(productName ?: "Товар")


            val sp = getSharedPreferences("regData", MODE_PRIVATE)

            val currentuId = sp.getString("uId", null)
            if (currentuId != null) {
                pollViewModel.uId.value = currentuId
            }

            appInitialized = true
        }

        setContent {
            SwipeCSATTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val currentPoll = pollViewModel.currentPoll.observeAsState()
                    val uId = pollViewModel.uId.observeAsState()

                    if (uId.value == null) {
                        RegistrationScreen(pollViewModel)
                    } else {
                        if (currentPoll.value != null) {
                            PollScreen(pollViewModel)
                        }
                    }
                }
            }
        }
    }
}

