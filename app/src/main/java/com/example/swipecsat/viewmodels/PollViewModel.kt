package com.example.swipecsat.viewmodels

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.swipecsat.models.Poll
import com.example.swipecsat.ui.theme.backgroudColor

class PollViewModel(application: Application) : AndroidViewModel(application) {
    val currentPoll = MutableLiveData<Poll>()
    val currentItemIndex = MutableLiveData<Int>()

    fun nextItem() {
        if (currentItemIndex.value!! < currentPoll.value!!.items.size - 1) {
            currentItemIndex.value = currentItemIndex.value!! + 1
        }
    }

    fun sendAnswer(answer: String) {
        Unit
    }
}