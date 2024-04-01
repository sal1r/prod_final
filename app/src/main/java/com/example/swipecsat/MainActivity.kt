package com.example.swipecsat

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModelProvider
import com.example.swipecsat.models.EndPoll
import com.example.swipecsat.models.PollItem
import com.example.swipecsat.models.Question
import com.example.swipecsat.models.createPoll
import com.example.swipecsat.ui.theme.SwipeCSATTheme
import com.example.swipecsat.viewmodels.PollViewModel
import com.example.swipecsat.views.PollScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray

class MainActivity : ComponentActivity() {
    private var appInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pollViewModel = ViewModelProvider(this)[PollViewModel::class.java]

//        CoroutineScope(Dispatchers.IO).launch {
//            val client = okhttp3.OkHttpClient()
//
//            val body = okhttp3.RequestBody
//                .create("application/json".toMediaType(),
//                    "{\"hashed_password\": \"123\", \"gender\": \"male\", \"age\": 10}")
//
//            val request = okhttp3.Request.Builder()
//                .url("http://158.160.98.205:8000/api/register")
//                .post(body)
//                .build()
//
//            val response = client.newCall(request).execute()
//            val bod = response.body?.string()
//            Log.d("test", bod ?: "No body")
//        }

        if (!appInitialized) {
            checkPermissions(this, applicationContext)

            val extras = intent.extras
            if (extras != null) {
                val items = mutableListOf<PollItem>()
                val decodedItems = JSONArray(extras.getString("items"))

                for (i in 0 until decodedItems.length()) {
                    val item = decodedItems.getString(i)
                    items.add(Question(pollViewModel, item))
                }
                items.add(EndPoll(pollViewModel))

                createPoll(pollViewModel, items)
            } else {
                createPoll(
                    pollViewModel,
                    listOf(
                        Question(pollViewModel, "Вас устроил вкус кофе?"),
                        Question(pollViewModel, "Соответсвует ли цена нашему кофе?"),
                        Question(pollViewModel, "Хорошая ли подача?"),
                        EndPoll(pollViewModel)
                    )
                )
            }

            appInitialized = true
        }

        setContent {
            SwipeCSATTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PollScreen(pollViewModel)
                }
            }
        }
    }
}

