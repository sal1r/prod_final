package com.example.swipecsat.models

import com.example.swipecsat.viewmodels.PollViewModel

fun createPoll(
    viewModel: PollViewModel,
    items: List<PollItem>
) {
    viewModel.currentPoll.value = Poll(items)
    viewModel.currentItemIndex.value = 0
}

data class Poll(val items: List<PollItem>)

data class Question(
    private val viewModel: PollViewModel,
    val text: String
) : PollItem(viewModel)

data class EndPoll(
    private val viewModel: PollViewModel,
) : PollItem(viewModel) {

}

abstract class PollItem(
    private val viewModel: PollViewModel,
) {
    fun next() {
        viewModel.nextItem()
    }
}