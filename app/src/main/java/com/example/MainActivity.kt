package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Force Right-to-Left (RTL) layout direction to provide a perfect Arabic user experience
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color(0xFF0F172A) // Slate 900: Beautiful deep cyber-arabic canvas
                    ) { innerPadding ->
                        DashboardScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // State bindings
    val status by viewModel.status.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val quickNotes by viewModel.quickNotes.collectAsState()

    // Screen states
    var chatInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var noteIsPinned by remember { mutableStateOf(false) }

    // Feedback states (Contact Hub)
    var senderName by remember { mutableStateOf("") }
    var senderTopic by remember { mutableStateOf("") }
    var feedbackMessage by remember { mutableStateOf("") }
    var isSendingFeedback by remember { mutableStateOf(false) }

    // Auto-update time helper loop
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateTime()
            delay(10000) // Update every 10 seconds
        }
    }

    // Scroll state for chat
    val chatScrollState = rememberLazyListState()
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            chatScrollState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Main layout
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // --- 1. Header Block with Liveness Pulse ---
        item {
            HeaderSection(
                status = status,
                currentTime = currentTime,
                onRefreshTime = { viewModel.updateTime() }
            )
        }

        // --- 2. Live Status Selector ---
        item {
            StatusSelectorSection(
                currentStatus = status,
                onStatusChange = { newStatus ->
                    viewModel.changeStatus(newStatus)
                    Toast.makeText(
                        context,
                        "تم تغيير حالة نسيم الفورية إلى: ${newStatus.label}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        // --- 3. Custom Chat with AI Twin ---
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), // Slate 800
                border = BorderStroke(1.dp, Color(0xFF334155)), // Slate 700
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_twin_chat_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Chat Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981)) // Emerald green dot
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "التوأم الرقمي الذكي لنسيم",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Clear chat button
                        IconButton(
                            onClick = {
                                viewModel.clearChat()
                                Toast.makeText(context, "تم مسح الدردشة بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("chat_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "مسح المحادثة",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Text(
                        text = "اسأل التوأم الذكي عن مهارات نسيم، مشاريعه، تواصله، أو وضعه الحالي وسيجيبك فوراً بلغة ذكية.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chat Scroll Box
                    Box(
                        modifier = Modifier
                            .height(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F172A)) // Slate 900 background for messages
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        if (chatMessages.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ابدأ المحادثة الآن...",
                                    color = Color(0xFF64748B),
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                state = chatScrollState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(chatMessages) { message ->
                                    ChatBubble(message = message)
                                }

                                if (isChatLoading) {
                                    item {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.Start,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFF334155))
                                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(14.dp),
                                                        strokeWidth = 2.dp,
                                                        color = Color(0xFF10B981)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "يفكر التوأم الرقمي...",
                                                        color = Color(0xFFE2E8F0),
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick prompt chips
                    Text(
                        text = "مواضيع مقترحة لبدء الحوار:",
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    val quickPrompts = listOf(
                        "ما هي خبرات نسيم؟",
                        "كيف أتواصل معه؟",
                        "ماذا يبرمج حالياً؟",
                        "ما اللغات البرمجية المفضلة لديه؟"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickPrompts) { prompt ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF334155))
                                    .clickable {
                                        viewModel.sendChatMessage(prompt)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = prompt,
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = {
                                Text(
                                    text = "اطرح سؤالاً على التوأم المساعد...",
                                    style = TextStyle(textDirection = TextDirection.ContentOrRtl)
                                )
                            },
                            textStyle = TextStyle(
                                textDirection = TextDirection.ContentOrRtl,
                                color = Color.White,
                                fontSize = 14.sp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedPlaceholderColor = Color(0xFF64748B),
                                unfocusedPlaceholderColor = Color(0xFF64748B)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_text_field"),
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (chatInput.isNotBlank()) {
                                    viewModel.sendChatMessage(chatInput)
                                    chatInput = ""
                                }
                            })
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Send Button
                        Button(
                            onClick = {
                                if (chatInput.isNotBlank()) {
                                    viewModel.sendChatMessage(chatInput)
                                    chatInput = ""
                                }
                            },
                            enabled = chatInput.isNotBlank() && !isChatLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                disabledContainerColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("chat_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "إرسال",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // --- 4. Custom Quick Notes bulletin board ---
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bulletin_notes_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "جدار الإشعارات والملاحظات السريعة",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "أضف ملاحظة أو تصفح الإعلانات الفورية لمتابعة سير العمل.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Form to add note
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        placeholder = {
                            Text(
                                text = "اكتب إعلاناً أو ملاحظة سريعة هنا...",
                                style = TextStyle(textDirection = TextDirection.ContentOrRtl)
                            )
                        },
                        textStyle = TextStyle(
                            textDirection = TextDirection.ContentOrRtl,
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_input_text_field"),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pin Switch
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = noteIsPinned,
                                onCheckedChange = { noteIsPinned = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFF59E0B)) // Amber/Gold
                            )
                            Text(
                                text = "تثبيت في الأعلى 📌",
                                color = Color(0xFFE2E8F0),
                                fontSize = 13.sp
                            )
                        }

                        // Add Button
                        Button(
                            onClick = {
                                if (noteInput.isNotBlank()) {
                                    viewModel.addQuickNote(noteInput, noteIsPinned)
                                    noteInput = ""
                                    noteIsPinned = false
                                    Toast.makeText(context, "تمت إضافة الملاحظة بنجاح", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = noteInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_note_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "أضف")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Scrolling List of Notes
                    if (quickNotes.isEmpty()) {
                        Text(
                            text = "لا توجد ملاحظات حالية.",
                            color = Color(0xFF64748B),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            quickNotes.forEach { note ->
                                NoteItem(
                                    note = note,
                                    onDelete = { viewModel.deleteQuickNote(note.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 5. Professional Skill Showcase Map ---
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "خريطة المهارات البرمجية لنسيم العكيشي",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "تقييم تقريبي لمستويات الأداء والخبرة العملية في هندسة البرمجيات.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    SkillProgressItem(title = "تطوير تطبيقات الأندرويد الأصلية (Kotlin, Compose, MVVM)", percentage = 0.95f, color = Color(0xFF3DDF84))
                    Spacer(modifier = Modifier.height(12.dp))
                    SkillProgressItem(title = "تطوير تطبيقات الويب المتكاملة (React, Node.js, TypeScript)", percentage = 0.90f, color = Color(0xFF3B82F6))
                    Spacer(modifier = Modifier.height(12.dp))
                    SkillProgressItem(title = "دمج تقنيات الذكاء الاصطناعي التوليدي وموديلات (Gemini APIs)", percentage = 0.92f, color = Color(0xFF8B5CF6))
                    Spacer(modifier = Modifier.height(12.dp))
                    SkillProgressItem(title = "قواعد البيانات والذاكرة المحلية (SQLite, PostgreSQL, Room)", percentage = 0.88f, color = Color(0xFFF59E0B))
                }
            }
        }

        // --- 6. Direct Contact Hub & Feedback Form ---
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_hub_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "روابط الاتصال السريع والرسائل المباشرة",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "تواصل مع نسيم العكيشي مباشرة أو اترك رسالة سريعة من خلال هذه اللوحة الفورية.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Email display row
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F172A))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "البريد الإلكتروني المهني",
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "www.naseem712392760@gmail.com",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Copy Button
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Nasim Email", "www.naseem712392760@gmail.com")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "تم نسخ البريد الإلكتروني إلى الحافظة! 📋", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("copy_email_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "نسخ",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("نسخ", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "منصات التواصل الاجتماعي والمواقع المهنية:",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Row of social media placeholders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val socialPlatforms = listOf(
                            Triple("GitHub", Icons.Default.Code, "https://github.com"),
                            Triple("LinkedIn", Icons.Default.Person, "https://linkedin.com"),
                            Triple("WhatsApp", Icons.Default.Call, "https://wa.me"),
                            Triple("Twitter / X", Icons.Default.Share, "https://x.com")
                        )

                        socialPlatforms.forEach { (name, icon, url) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(BorderStroke(1.dp, Color(0xFF334155)), RoundedCornerShape(12.dp))
                                    .clickable {
                                        Toast.makeText(
                                            context,
                                            "جاري الانتقال إلى حساب نسيم العكيشي على $name: $url",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = name,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = name,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "نموذج الرسائل الفورية:",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Sender Name
                    OutlinedTextField(
                        value = senderName,
                        onValueChange = { senderName = it },
                        placeholder = {
                            Text(
                                text = "اسمك الكريم...",
                                style = TextStyle(textDirection = TextDirection.ContentOrRtl)
                            )
                        },
                        textStyle = TextStyle(
                            textDirection = TextDirection.ContentOrRtl,
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        maxLines = 1
                    )

                    // Sender Topic
                    OutlinedTextField(
                        value = senderTopic,
                        onValueChange = { senderTopic = it },
                        placeholder = {
                            Text(
                                text = "عنوان الرسالة / الموضوع...",
                                style = TextStyle(textDirection = TextDirection.ContentOrRtl)
                            )
                        },
                        textStyle = TextStyle(
                            textDirection = TextDirection.ContentOrRtl,
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        maxLines = 1
                    )

                    // Message
                    OutlinedTextField(
                        value = feedbackMessage,
                        onValueChange = { feedbackMessage = it },
                        placeholder = {
                            Text(
                                text = "اكتب رسالتك لنسيم العكيشي هنا بالتفصيل...",
                                style = TextStyle(textDirection = TextDirection.ContentOrRtl)
                            )
                        },
                        textStyle = TextStyle(
                            textDirection = TextDirection.ContentOrRtl,
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .padding(bottom = 12.dp),
                        maxLines = 4
                    )

                    // Submit Message
                    Button(
                        onClick = {
                            if (senderName.isNotBlank() && feedbackMessage.isNotBlank()) {
                                isSendingFeedback = true
                                coroutineScope.launch {
                                    delay(1500) // Simulate elegant networking delay
                                    isSendingFeedback = false
                                    Toast.makeText(
                                        context,
                                        "شكراً لك يا ${senderName}! تم إرسال رسالتك الفورية بنجاح إلى نسيم العكيشي.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    senderName = ""
                                    senderTopic = ""
                                    feedbackMessage = ""
                                }
                            }
                        },
                        enabled = senderName.isNotBlank() && feedbackMessage.isNotBlank() && !isSendingFeedback,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            disabledContainerColor = Color(0xFF475569)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("send_feedback_button")
                    ) {
                        if (isSendingFeedback) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جاري تشفير وإرسال الرسالة...")
                        } else {
                            Icon(imageVector = Icons.Default.Email, contentDescription = "أرسل")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إرسال رسالة مباشرة", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    status: UserStatus,
    currentTime: String,
    onRefreshTime: () -> Unit
) {
    // Elegant pulsing animation for the active online status liveness
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row containing the Status indicator & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Time Area
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .clickable { onRefreshTime() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "تحديث الوقت",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currentTime.ifBlank { "جاري تحميل التوقيت..." },
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Status Indicator Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(status.hexColor).copy(alpha = 0.15f))
                        .border(
                            BorderStroke(
                                1.dp,
                                Color(status.hexColor).copy(alpha = alphaPulse)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = status.icon,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Text(
                            text = status.label,
                            color = Color(status.hexColor),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Branding Head
            Text(
                text = "نسيم العكيشي أونلاين 🌐",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "البوابة الرقمية الموحدة والتوأم الذكي لنسيم العكيشي",
                color = Color(0xFF10B981), // Emerald Cyan primary
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "مرحباً بك في المساحة الخاصة بي! حالتي الحالية هي: ${status.description}.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun StatusSelectorSection(
    currentStatus: UserStatus,
    onStatusChange: (UserStatus) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "اختر الحالة الافتراضية لنسيم العكيشي حالياً:",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserStatus.values().forEach { stat ->
                    val isSelected = currentStatus == stat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(stat.hexColor).copy(alpha = 0.2f) else Color(0xFF0F172A))
                            .border(
                                BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(stat.hexColor) else Color(0xFF334155)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onStatusChange(stat) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = stat.icon, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stat.label,
                                color = if (isSelected) Color(stat.hexColor) else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .widthIn(max = 260.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 2.dp,
                        bottomEnd = if (message.isUser) 2.dp else 16.dp
                    )
                )
                .background(
                    if (message.isUser) {
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF0D9488), Color(0xFF0F766E)) // Dark Teal gradients
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1E293B), Color(0xFF334155)) // Deep slate gradients
                        )
                    }
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.timestamp,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun NoteItem(
    note: QuickNote,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A))
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (note.isPinned) Color(0xFFF59E0B) else Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (note.isPinned) {
                        Text(
                            text = "📌 مثبت للأعلى",
                            color = Color(0xFFF59E0B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = note.timestamp,
                        color = Color(0xFF64748B),
                        fontSize = 9.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = note.text,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete Action
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "حذف الملاحظة",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun SkillProgressItem(
    title: String,
    percentage: Float,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color(0xFFE2E8F0),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${(percentage * 100).toInt()}%",
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = percentage,
            color = color,
            trackColor = Color(0xFF334155),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
        )
    }
}
