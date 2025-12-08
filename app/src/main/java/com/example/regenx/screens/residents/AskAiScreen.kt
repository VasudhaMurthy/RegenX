//package com.example.regenx.screens.residents
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.Send
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AskAiScreen(
//    navController: NavController,
//    viewModel: AskAiViewModel = viewModel()
//) {
//    val messages = viewModel.messages
//    val isLoading = viewModel.isLoading
//
//    var inputText by remember { mutableStateOf("") }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(text = "Ask AI") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = "Back"
//                        )
//                    }
//                },
//                actions = {
//                    if (messages.isNotEmpty()) {
//                        IconButton(onClick = { viewModel.clearChat() }) {
//                            Icon(
//                                imageVector = Icons.Default.Delete,
//                                contentDescription = "Clear chat"
//                            )
//                        }
//                    }
//                }
//            )
//        }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//        ) {
//            // Messages list
//            LazyColumn(
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxWidth()
//                    .padding(8.dp),
//                reverseLayout = true // latest at bottom
//            ) {
//                val reversed = messages.asReversed()
//                items(reversed) { msg ->
//                    ChatBubble(message = msg)
//                    Spacer(modifier = Modifier.height(8.dp))
//                }
//
//                if (messages.isEmpty()) {
//                    item {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(16.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                text = "Ask anything about waste, segregation, pickups, or the app!",
//                                style = MaterialTheme.typography.bodyMedium,
//                                textAlign = TextAlign.Center
//                            )
//                        }
//                    }
//                }
//            }
//
//            if (isLoading) {
//                LinearProgressIndicator(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp)
//                )
//            }
//
//            // Input bar
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(8.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                TextField(
//                    value = inputText,
//                    onValueChange = { inputText = it },
//                    modifier = Modifier.weight(1f),
//                    placeholder = { Text("Type your question...") },
//                    maxLines = 4,
//                    shape = RoundedCornerShape(24.dp)
//                )
//
//                Spacer(modifier = Modifier.width(8.dp))
//
//                IconButton(
//                    onClick = {
//                        val text = inputText.trim()
//                        if (text.isNotEmpty() && !isLoading) {
//                            viewModel.sendPrompt(text)
//                            inputText = ""
//                        }
//                    },
//                    enabled = inputText.isNotBlank() && !isLoading
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Send,
//                        contentDescription = "Send"
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ChatBubble(message: ChatMessage) {
//    val backgroundColor: Color
//    val alignment: Arrangement.Horizontal
//    val shape: RoundedCornerShape
//
//    if (message.isUser) {
//        backgroundColor = MaterialTheme.colorScheme.primary
//        alignment = Arrangement.End
//        shape = RoundedCornerShape(
//            topStart = 16.dp,
//            topEnd = 16.dp,
//            bottomStart = 16.dp,
//            bottomEnd = 0.dp
//        )
//    } else {
//        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
//        alignment = Arrangement.Start
//        shape = RoundedCornerShape(
//            topStart = 16.dp,
//            topEnd = 16.dp,
//            bottomStart = 0.dp,
//            bottomEnd = 16.dp
//        )
//    }
//
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = alignment
//    ) {
//        Box(
//            modifier = Modifier
//                .widthIn(max = 280.dp)
//                .clip(shape)
//                .background(backgroundColor)
//                .padding(12.dp)
//        ) {
//            Text(
//                text = message.text,
//                color = if (message.isUser)
//                    MaterialTheme.colorScheme.onPrimary
//                else
//                    MaterialTheme.colorScheme.onSurfaceVariant,
//                style = MaterialTheme.typography.bodyMedium
//            )
//        }
//    }
//}





package com.example.regenx.screens.residents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskAiScreen(
    navController: NavController,
    viewModel: AskAiViewModel = viewModel()
) {
    val messages = viewModel.messages
    val isLoading = viewModel.isLoading

    var inputText by remember { mutableStateOf("") }

    // 👋 Show ReXi welcome message once when screen is opened
    LaunchedEffect(Unit) {
        viewModel.addWelcomeMessage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Ask AI") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (messages.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearChat() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear chat"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Messages list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                reverseLayout = true
            ) {
                val reversed = messages.asReversed()
                items(reversed) { msg ->
                    ChatBubble(message = msg)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ask ReXi anything about home waste, recycling tricks or garbage collection issues.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            // Input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type your question...") },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val text = inputText.trim()
                        if (text.isNotEmpty() && !isLoading) {
                            viewModel.sendPrompt(text)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send"
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val backgroundColor: androidx.compose.ui.graphics.Color
    val alignment: Arrangement.Horizontal
    val shape: RoundedCornerShape

    if (message.isUser) {
        backgroundColor = MaterialTheme.colorScheme.primary
        alignment = Arrangement.End
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 0.dp
        )
    } else {
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        alignment = Arrangement.Start
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 0.dp,
            bottomEnd = 16.dp
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(backgroundColor)
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.isUser)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
