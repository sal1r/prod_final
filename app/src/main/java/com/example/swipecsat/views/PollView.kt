package com.example.swipecsat.views

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swipecsat.R
import com.example.swipecsat.models.DetailQuestion
import com.example.swipecsat.models.EndPoll
import com.example.swipecsat.models.SimpleQuestion
import com.example.swipecsat.ui.theme.backgroudColor
import com.example.swipecsat.ui.theme.greenBackgroundColor
import com.example.swipecsat.ui.theme.primaryTextColor
import com.example.swipecsat.ui.theme.redBackgroundColor
import com.example.swipecsat.ui.theme.secondaryTextColor
import com.example.swipecsat.viewmodels.PollViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PollScreen(pollViewModel: PollViewModel) {
    var swipable by remember { mutableStateOf(true) }
    var showCounter by remember { mutableStateOf(true) }
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
                1 -> 1
                else -> 0
            })

            coroutineScope.launch {
                background = backgroudColor
                swipeableState.animateTo(0)
            }
        }
    }

    LaunchedEffect(swipeOffset) {
        val fraction = abs(swipeOffset) / swipeLimit
        background =
            if (swipeOffset > 0) lerp(backgroudColor, greenBackgroundColor, fraction)
            else lerp(backgroudColor, redBackgroundColor, fraction)
        if (pollViewModel.showHelper.value!!) {
            if (swipeOffset != 0f) {
                pollViewModel.showHelper.value = false
            }
        }
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

        if (showCounter) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(256.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${if(currentItemIndex!! < currentPoll!!.items.size - 1) currentItemIndex!! + 1
                    else pollViewModel.questionsCount.value!!}" +
                            "/${pollViewModel.questionsCount.value!!}",
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.typography.displayMedium.fontSize,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(64.dp))
            }
        }

        Box(
            modifier = Modifier
                .then(swipeableModifier)
                .offset(swipeOffset.dp, 0.dp)
                .rotate(abs(swipeOffset) * swipeOffset / 7000f)
//                .alpha(1f - abs(swipeOffset) / swipeLimit / 1.5f)
        ) {
            when (currentItem) {
                is SimpleQuestion -> {
                    SimpleQuestionCard(pollViewModel, currentItem.text)
                    LaunchedEffect(currentItem) {
                        coroutineScope.launch {
                            swipable = true
                            showCounter = true
                        }
                    }
                }
                is DetailQuestion -> {
                    DetailQuestionCard(pollViewModel)
                    LaunchedEffect(currentItem) {
                        coroutineScope.launch {
                            swipable = false
                            showCounter = false
                        }
                    }
                }
                is EndPoll -> {
                    EndPollCard(pollViewModel)
                    LaunchedEffect(currentItem) {
                        coroutineScope.launch {
                            swipable = false
                            showCounter = false
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleQuestionCard(pollViewModel: PollViewModel, text: String) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(3f)
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
            if (pollViewModel.showHelper.value!!) {
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Свайп вправо - да",
                        fontSize = 30.sp,
                        color = primaryTextColor
                    )
                    Image(
                        modifier = Modifier.size(36.dp),
                        painter = painterResource(R.drawable.arrow_right),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(primaryTextColor)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier.size(36.dp),
                        painter = painterResource(R.drawable.arrow_left),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(primaryTextColor)
                    )
                    Text(
                        " Свайп влево - нет",
                        fontSize = 30.sp,
                        color = primaryTextColor
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        Spacer(modifier = Modifier.weight(1f))
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
            .padding(0.dp, 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Конец опроса",
            fontSize = MaterialTheme.typography.displaySmall.fontSize,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .padding(8.dp, 0.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.onBackground,
                            Color.Transparent
                        )
                    )
                )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Image(
            modifier = Modifier.size(64.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
            painter = painterResource(R.drawable.heart),
            contentDescription = null
        )
        Spacer(modifier = Modifier.height(24.dp))

        val annotatedText = buildAnnotatedString {
            append("Спасибо что уделили нам время, ваше мнение очень важно для нас!" +
                    " Вот ваш промокод на скидку 10%: ")
            appendInlineContent("promocode")
        }

        val inlineContentMap = mapOf(
            "promocode" to InlineTextContent(
                Placeholder(
                    width = 104.sp,
                    height = 24.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                Text(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.shapes.small
                        )
                        .padding(4.dp, 0.dp),
                    text = "34TRF7",
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                )
            }
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp),
            text = annotatedText,
            inlineContent = inlineContentMap,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize
        )
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun DetailQuestionCard(viewModel: PollViewModel) {
    var answer by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 32.dp)
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.shapes.large
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Поделитесь своим мнением",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Вы можете оставить свои пожелания и поделиться своим опытом пользования нашими услугами в форме ниже",
            color = secondaryTextColor,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(64.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().padding(16.dp, 0.dp),
            value = answer,
            onValueChange = { answer = it },
            maxLines = 12,
            minLines = 12,
            label = { Text(text = "Ваше мнение") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.onBackground
            ),
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.padding(16.dp, 0.dp)
        ) {
            Button(
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.shapes.medium
                ),
                onClick = { viewModel.sendDetailAnswer(answer) }
            ) {
                Text(
                    text = "Отправить",
                    color = MaterialTheme.colorScheme.background
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.shapes.medium
                ),
                onClick = { viewModel.nextItem() }
            ) {
                Text(
                    text = "Пропустить",
                    color = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}