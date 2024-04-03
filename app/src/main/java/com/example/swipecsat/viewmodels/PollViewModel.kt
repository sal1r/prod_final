package com.example.swipecsat.viewmodels

import android.app.Application
import android.content.Context.MODE_PRIVATE
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.swipecsat.models.DetailQuestion
import com.example.swipecsat.models.EndPoll
import com.example.swipecsat.models.Poll
import com.example.swipecsat.models.PollItem
import com.example.swipecsat.models.SimpleQuestion
import com.example.swipecsat.models.createPoll
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject

class PollViewModel(application: Application) : AndroidViewModel(application) {
    val currentPoll = MutableLiveData<Poll>()
    val currentItemIndex = MutableLiveData<Int>()
    val questionsCount = MutableLiveData<Int>(0)
    val uId = MutableLiveData<String?>()
    val showHelper = MutableLiveData(true)

    fun nextItem() {
        if (currentItemIndex.value!! < currentPoll.value!!.items.size - 1) {
            currentItemIndex.value = currentItemIndex.value!! + 1
        }
    }

    fun sendAnswer(answer: Int) {
        if (currentPoll.value!!.items[currentItemIndex.value!!] !is SimpleQuestion) return

        CoroutineScope(Dispatchers.IO).launch {
            val client = okhttp3.OkHttpClient()

            val body = okhttp3.RequestBody
                .create("application/json".toMediaType(),
                    "{\"user_id\": \"${uId.value}\", " +
                            "\"question\": \"${(currentPoll.value!!.items[currentItemIndex.value!!] as SimpleQuestion).text}\", " +
                            "\"answer\": \"$answer\", " +
                            "\"feedback\": \"empty\"}")

            val request = okhttp3.Request.Builder()
                .url("http://158.160.98.205:8000/api/feedback/answer")
                .post(body)
                .build()

            client.newCall(request).execute()
        }

        CoroutineScope(Dispatchers.Main).launch {
            nextItem()
        }
    }

    fun loadQuestions(productName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = okhttp3.OkHttpClient().newCall(
                    okhttp3.Request.Builder()
                        .url("http://158.160.98.205:8000/api/question/get/random/$productName")
                        .build()
                ).execute()

                val items = mutableListOf<PollItem>()
                val decodedItems = JSONArray(
                    JSONObject(response.body!!.string()).getString("questions")
                )

                for (i in 0 until decodedItems.length()) {
                    val item = decodedItems.getString(i)
                    items.add(SimpleQuestion(this@PollViewModel, item))
                }
                items.add(DetailQuestion(this@PollViewModel))
                items.add(EndPoll(this@PollViewModel))

                withContext(Dispatchers.Main) {
                    questionsCount.value = items.count { it is SimpleQuestion }
                    createPoll(this@PollViewModel, items)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    questionsCount.value = 0
                    createPoll(this@PollViewModel, listOf(EndPoll(this@PollViewModel)))
                }
            }
        }
    }

    fun registerUser(gender: String, age: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    uId.value = it.result

                    val sp = getApplication<Application>().getSharedPreferences("regData", MODE_PRIVATE)
                    val spEdit = sp.edit()

                    spEdit.putString("uId", it.result)
                    spEdit.putString("gender", gender)
                    spEdit.putString("age", age)
                    spEdit.apply()
                }

                val client = okhttp3.OkHttpClient()

                val body = okhttp3.RequestBody
                    .create("application/json".toMediaType(),
                        "{\"id\": \"${it.result}\", \"gender\": \"$gender\", \"age\": $age}")

                val request = okhttp3.Request.Builder()
                    .url("http://158.160.98.205:8000/api/user/register")
                    .post(body)
                    .build()

                client.newCall(request).execute()
            }
        }
    }

    fun sendDetailAnswer(answer: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val client = okhttp3.OkHttpClient()

            val body = okhttp3.RequestBody
                .create("application/json".toMediaType(),
                    "{\"answer\": \"$answer\"}")

            val request = okhttp3.Request.Builder()
                .url("http://158.160.98.205:8000/api/question/detail/quest/")
                .post(body)
                .build()

            client.newCall(request).execute()
        }
        nextItem()
    }
}