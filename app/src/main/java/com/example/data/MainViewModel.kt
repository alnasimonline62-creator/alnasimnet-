package com.example.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// --- State Data Models ---

enum class UserStatus(val label: String, val icon: String, val description: String, val hexColor: Long) {
    ONLINE("متصل الآن", "🟢", "نشط ويبرمج أفكاراً جديدة", 0xFF4CAF50),
    BUSY("مشغول بالبرمجة", "🔴", "في قمة التركيز، يرجى الانتظار", 0xFFF44336),
    AWAY("خارج المكتب", "🟡", "يأخذ استراحة قصيرة لتناول الشاي/القهوة", 0xFFFFEB3B),
    VACATION("في إجازة", "✈️", "يستعيد طاقته ويستكشف العالم", 0xFF9C27B0)
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: String = SimpleDateFormat("hh:mm a", Locale("ar")).format(Date())
)

data class QuickNote(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val timestamp: String = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date()),
    val isPinned: Boolean = false
)

class MainViewModel : ViewModel() {

    // --- State Flows ---

    private val _status = MutableStateFlow(UserStatus.ONLINE)
    val status: StateFlow<UserStatus> = _status.asStateFlow()

    private val _currentTime = MutableStateFlow("")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _quickNotes = MutableStateFlow<List<QuickNote>>(emptyList())
    val quickNotes: StateFlow<List<QuickNote>> = _quickNotes.asStateFlow()

    init {
        updateTime()
        loadDefaultNotes()
        loadDefaultWelcomeMessage()
    }

    // --- Actions ---

    fun changeStatus(newStatus: UserStatus) {
        _status.value = newStatus
    }

    fun updateTime() {
        val formatter = SimpleDateFormat("yyyy/MM/dd • hh:mm a", Locale("ar"))
        _currentTime.value = formatter.format(Date())
    }

    private fun loadDefaultNotes() {
        _quickNotes.value = listOf(
            QuickNote(
                text = "🚀 تم إطلاق منصة 'نسيم العكيشي أونلاين'! مطورة بالكامل باستخدام Jetpack Compose و Material 3.",
                isPinned = true
            ),
            QuickNote(
                text = "💡 لا تتردد في الدردشة مع توأمي الرقمي! لديه معرفة شاملة بسيرتي الذاتية ومهاراتي وحالتي الفورية.",
                isPinned = false
            ),
            QuickNote(
                text = "☕ استهلاك القهوة في أعلى مستوياته اليوم! مبرمج بكل حب وشغف لإنتاج أفضل الواجهات.",
                isPinned = false
            )
        )
    }

    private fun loadDefaultWelcomeMessage() {
        _chatMessages.value = listOf(
            ChatMessage(
                text = "مرحباً بك! 👋 أنا التوأم الرقمي المعتمد على الذكاء الاصطناعي لنسيم العكيشي. يسعدني جداً الترحيب بك وإجابتك عن أي سؤال يخص مهارات نسيم البرمجية، سيرته المهنية، أو حالته الحالية!",
                isUser = false
            )
        )
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank() || _isChatLoading.value) return

        val userMsg = ChatMessage(text = text, isUser = true)
        val updatedMsgs = _chatMessages.value + userMsg
        _chatMessages.value = updatedMsgs

        _isChatLoading.value = true

        viewModelScope.launch {
            // Build the system prompt using current status and time for contextual richness in Arabic
            val systemPrompt = """
                أنت التوأم الرقمي والمساعد الذكي لـ "نسيم العكيشي" (Naseem Al-Ukaishi).
                مهمتك هي الترحيب بزوار لوحة التحكم الخاصة بنسيم العكيشي ومساعدتهم بلغة عربية فصحى راقية، ذكية، وودودة جداً.
                إليك معلومات تفصيلية عن نسيم لكي تستخدمها في إجاباتك:
                - الاسم الكامل: نسيم العكيشي (Naseem Al-Ukaishi)
                - البريد الإلكتروني للتواصل: www.naseem712392760@gmail.com
                - نبذة شخصية: مهندس برمجيات شغوف، صانع حلول رقمية، ومطور تطبيقات أندرويد متميزة وحلول ويب متكاملة وقابلة للتوسع.
                - الحالة الحالية لنسيم العكيشي: نسيم الآن في حالة '${_status.value.label}' (${_status.value.description}).
                - التوقيت الحالي في لوحة التحكم: ${_currentTime.value}
                - مهارات تقنية أساسية:
                  1. تطوير تطبيقات أندرويد أصلية (Native Android) باستخدام لغة Kotlin، وتقنية Jetpack Compose، ومعمارية MVVM.
                  2. تطوير الويب المتكامل باستخدام React و Node.js و TypeScript.
                  3. قواعد البيانات وإدارة البيانات (Room، SQLite، PostgreSQL).
                  4. دمج تقنيات الذكاء الاصطناعي التوليدي وموديلات Gemini.
                  5. تصميم واجهات مستخدم مذهلة وفق معايير Material Design 3 (UI/UX).
                - أبرز المشاريع المنجزة:
                  1. تطبيق 'نسيم العكيشي أونلاين': البوابة الذكية لعرض الهوية الرقمية والإنتاجية الفورية.
                  2. التوأم الرقمي المدعوم بالذكاء الاصطناعي: مساعد تفاعلي يفهم السياق والبيانات الفورية.
                  3. لوحات تحكم متقدمة ومشاريع ويب ذات أداء عالٍ.

                عندما تجيب:
                - تحدث دائماً بصفة التوأم الرقمي المساعد لنسيم العكيشي (مثال: "أنا التوأم الرقمي لنسيم العكيشي، يسعدني...")، أو تحدث بالنيابة عنه لخدمة الزائر.
                - التزم تماماً باللغة العربية الفصحى الودودة والمهنية، مع لمسة حماسية تجاه التقنية والبرمجة والابتكار.
                - اجعل الإجابات منسقة بشكل مذهل ومنظم (استخدم التعداد النقطي أو الفقرات القصيرة أو الكلمات البارزة).
                - لا تذكر تفاصيل تقنية داخلية من الكود البرمجي (مثل أسماء الكلاسات أو مسارات الملفات)، بل ركز تماماً على القيمة والمهارة والترحيب بالزائر.
                - إذا سألك الزائر عن البريد الإلكتروني أو كيفية التواصل، اذكر له البريد الإلكتروني مباشرة بكل فخر وسرور: www.naseem712392760@gmail.com
            """.trimIndent()

            // Map ChatMessage objects into Gemini-compatible Content objects for chat history
            // Limit to last 10 messages to keep latency low and fit within token budgets
            val apiHistory = updatedMsgs.dropLast(1).takeLast(10).map { msg ->
                Content(
                    parts = listOf(Part(text = msg.text)),
                    role = if (msg.isUser) "user" else "model"
                )
            }

            val responseText = RetrofitClient.askGemini(
                prompt = text,
                history = apiHistory,
                systemPrompt = systemPrompt
            )

            _chatMessages.value = _chatMessages.value + ChatMessage(text = responseText, isUser = false)
            _isChatLoading.value = false
        }
    }

    fun addQuickNote(text: String, isPinned: Boolean = false) {
        if (text.isBlank()) return
        val newNote = QuickNote(text = text, isPinned = isPinned)
        _quickNotes.value = (listOf(newNote) + _quickNotes.value).sortedWith(
            compareByDescending<QuickNote> { it.isPinned }.thenByDescending { it.timestamp }
        )
    }

    fun deleteQuickNote(id: String) {
        _quickNotes.value = _quickNotes.value.filter { it.id != id }
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
        loadDefaultWelcomeMessage()
    }
}
