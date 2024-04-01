package com.example.swipecsat.views

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.swipecsat.models.EndPoll
import com.example.swipecsat.models.Question
import com.example.swipecsat.ui.theme.backgroudColor
import com.example.swipecsat.ui.theme.greenBackgroundColor
import com.example.swipecsat.ui.theme.redBackgroundColor
import com.example.swipecsat.viewmodels.PollViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PollScreen(pollViewModel: PollViewModel) {
    var swipable by remember { mutableStateOf(true) }
    var background by remember { mutableStateOf(backgroudColor) }
    val coroutineScope = rememberCoroutineScope()
    val swipeableState = rememberSwipeableState(0)
    val swipeOffset = swipeableState.offset.value
    val swipeLimit = LocalConfiguration.current.screenWidthDp.toFloat() + 32f
    val anchors = mapOf(
        0f to 0,
        -swipeLimit to -1,
        swipeLimit to 1,
    )

    val swipeableModifier = Modifier.swipeable(
        state = swipeableState,
        anchors = anchors,
        thresholds = { _, _ -> FractionalThreshold(0.5f) },
        orientation = Orientation.Horizontal,
        enabled = swipable
    )

    LaunchedEffect(swipeableState.currentValue) {
        if (swipeableState.currentValue != 0) {
            pollViewModel.sendAnswer(when (swipeableState.currentValue) {
                -1 -> "Да"
                1 -> "Нет"
                else -> ""
            })

            coroutineScope.launch {
                background = backgroudColor
                swipeableState.animateTo(0)
            }

            pollViewModel.nextItem()
        }
    }

    LaunchedEffect(swipeOffset) {
        val fraction = abs(swipeOffset) / swipeLimit
        background =
            if (swipeOffset > 0) lerp(backgroudColor, greenBackgroundColor, fraction)
            else lerp(backgroudColor, redBackgroundColor, fraction)
    }

    val currentItemIndex by pollViewModel.currentItemIndex.observeAsState()
    val currentPoll by pollViewModel.currentPoll.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
        verticalArrangement = Arrangement.Center
    ) {
        val currentItem = currentPoll!!.items[currentItemIndex!!]

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${if(currentItemIndex!! < currentPoll!!.items.size - 1) currentItemIndex!! + 1
                          else currentPoll!!.items.size - 1}" +
                        "/${currentPoll!!.items.size - 1}",
                textAlign = TextAlign.Center,
                fontSize = MaterialTheme.typography.displayMedium.fontSize,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(64.dp))
        }
        Box(
            modifier = Modifier
                .then(swipeableModifier)
                .offset(swipeOffset.dp, 0.dp)
                .rotate(30f * (swipeOffset / swipeLimit))
                .alpha(1f - abs(swipeOffset) / swipeLimit / 2f)
        ) {
            when (currentItem) {
                is Question -> {
                    QuestionCard(pollViewModel, currentItem.text)
                    swipable = true
                }
                is EndPoll -> {
                    EndPollCard(pollViewModel)
                    swipable = false
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun QuestionCard(pollViewModel: PollViewModel, text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 0.dp)
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.shapes.large
            )
            .padding(16.dp, 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text,
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(64.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Button(
                modifier = Modifier.weight(2f),
                onClick = {
                pollViewModel.sendAnswer("Yes")
                pollViewModel.nextItem()
            }) {
                Text("Да")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                modifier = Modifier.weight(2f),
                onClick = {
                pollViewModel.sendAnswer("No")
                pollViewModel.nextItem()
            }) {
                Text("Нет")
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun EndPollCard(pollViewModel: PollViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 0.dp)
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.shapes.large
            )
            .padding(0.dp, 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Конец опроса",
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Спасибо что поучастовали, ваше мнение очень важно для нас!",
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            textAlign = TextAlign.Center
        )
    }
}