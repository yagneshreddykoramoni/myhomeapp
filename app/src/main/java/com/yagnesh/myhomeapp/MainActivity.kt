package com.yagnesh.myhomeapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.text.input.VisualTransformation
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import com.google.firebase.storage.FirebaseStorage
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import com.google.firebase.storage.StorageReference
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.HttpURLConnection
import android.content.ActivityNotFoundException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.ai.client.generativeai.GenerativeModel
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.rememberCoroutineScope
import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.setValue

// ============== PROFILE PICTURE STATE MANAGER ==============
object ProfilePictureManager {
    private val _profilePictures = mutableStateMapOf<String, String>()

    fun update(userId: String, uri: String) {
        _profilePictures[userId] = uri
        // Also update the cache for backward compatibility
        ProfilePictureCache.put(userId, uri)
    }

    fun remove(userId: String) {
        _profilePictures.remove(userId)
        ProfilePictureCache.put(userId, "")
    }

    fun get(userId: String): String? = _profilePictures[userId] ?: ProfilePictureCache.get(userId)
}

class MainActivity : ComponentActivity() {
    private lateinit var sessionManager: SessionManager  // ADD THIS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        sessionManager = SessionManager(this)  // ADD THIS
        Log.d("MainActivity", "Firebase initialized")

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigationWithSession(sessionManager)  // CHANGED
                }
            }
        }
    }
}

// ============== DATA CLASSES ==============
data class Family(
    val familyNumber: String = "",
    val familyName: String = "",
    val creatorName: String = "",
    val memberCount: Int = 0
)

data class FamilyMember(
    val name: String = "",
    val role: String = "",
    val userId: String = ""
)

data class Expense(
    val amount: Double = 0.0,
    val category: String = "",
    val icon: Int = android.R.drawable.ic_menu_gallery
)

data class FileItem(
    val name: String = "",
    val type: String = ""
)

data class GroceryItem(
    val id: String = "",
    val name: String = "",
    var isChecked: Boolean = false,
    val imageUrl: String = "",  // For future: store image URL
    val addedBy: String = "",
    val addedAt: com.google.firebase.Timestamp? = null
)

data class BillReminder(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val dueDate: com.google.firebase.Timestamp? = null,
    val remindDaysBefore: Int = 1,
    val isReminderEnabled: Boolean = true,
    val isPaid: Boolean = false,
    val addedBy: String = "",
    val addedAt: com.google.firebase.Timestamp? = null,
    val category: String = ""  // Electricity, Mobile, LIC, Loan, etc.
)

data class MemberDetail(
    val userId: String = "",
    val name: String = "",
    val role: String = "",
    val relation: String = "Son",
    val profilePicture: String = "",
    val customSections: List<CustomSection> = emptyList(),
    val fileGroups: List<FileGroup> = emptyList()
)

data class FileGroup(
    val title: String,
    val files: List<MemberFile>
)

data class MemberFile(
    val id: String = "",
    val fileName: String = "",
    val fileType: String = "",
    val fileUri: String = "",
    val uploadedAt: Timestamp? = null,
    val notes: String = "",
    val uploadedBy: String = ""
)

// Add the type field to ExpenseItem
data class ExpenseItem(
    val id: String = "",
    val name: String = "",  // NEW
    val amount: Double = 0.0,
    val category: String = "",
    val type: String = "outgoing",  // NEW
    val date: Timestamp? = null,
    val addedBy: String = "",
    val addedByName: String = "",
    val notes: String = ""
)

// Add new ExpenseCategory class
data class ExpenseCategory(
    val id: String = "",
    val name: String = "",
    val createdAt: Timestamp? = null
)

data class ExpenseSummary(
    val thisWeek: Double = 0.0,
    val thisMonth: Double = 0.0,
    val topCategories: List<String> = emptyList()
)

data class LocationData(
    val userId: String = "",
    val userName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val batteryLevel: Int = 0,
    val isOnline: Boolean = false,
    val isNotificationEnabled: Boolean = true,
    val lastUpdated: com.google.firebase.Timestamp? = null
)

data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val dateOfBirth: String = "",
    val address: String = "",
    val profilePictureUri: String = "",
    val updatedAt: com.google.firebase.Timestamp? = null
)

data class CustomSection(
    val name: String = "",
    val value: String = ""
)

data class UserNickname(
    val targetUserId: String = "",
    val nickname: String = "",
    val setBy: String = ""  // userId who set this nickname
)

data class SavedReport(
    val id: String = "",
    val reportName: String = "",
    val reportType: String = "",
    val generatedAt: com.google.firebase.Timestamp? = null,
    val fileUrl: String = ""
)

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long
)

data class NeetiSukti(
    val teluguText: String = "",
    val englishTranslation: String = "",
    val date: String = "",
    val generatedAt: com.google.firebase.Timestamp? = null
)

// ============== PROFILE PICTURE CACHE ==============
object ProfilePictureCache {
    private val cache = mutableMapOf<String, String>()

    fun get(userId: String): String? = cache[userId]

    fun put(userId: String, uri: String) {
        cache[userId] = uri
    }

    fun clear() {
        cache.clear()
    }
}

// ============== USER SESSION ==============
object UserSession {
    var userId: String = ""
    var userName: String = ""
    var phoneNumber: String = ""
    var familyId: String = ""

    // ADD THESE TWO FUNCTIONS:
    fun updateFromSessionData(sessionData: SessionData) {
        userId = sessionData.userId
        userName = sessionData.userName
        phoneNumber = sessionData.phoneNumber
        familyId = sessionData.familyId
    }

    fun clear() {
        userId = ""
        userName = ""
        phoneNumber = ""
        familyId = ""
    }
}

// DataStore extension
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

// Session Manager class
class SessionManager(private val context: Context) {
    private val userIdKey = stringPreferencesKey("user_id")
    private val userNameKey = stringPreferencesKey("user_name")
    private val phoneNumberKey = stringPreferencesKey("phone_number")
    private val familyIdKey = stringPreferencesKey("family_id")

    suspend fun saveSession(userId: String, userName: String, phoneNumber: String, familyId: String) {
        context.dataStore.edit { preferences ->
            preferences[userIdKey] = userId
            preferences[userNameKey] = userName
            preferences[phoneNumberKey] = phoneNumber
            preferences[familyIdKey] = familyId
        }
    }

    val sessionData: Flow<SessionData> = context.dataStore.data.map { preferences ->
        SessionData(
            userId = preferences[userIdKey] ?: "",
            userName = preferences[userNameKey] ?: "",
            phoneNumber = preferences[phoneNumberKey] ?: "",
            familyId = preferences[familyIdKey] ?: ""
        )
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun isLoggedIn(): Boolean {
        val session = sessionData.first()
        return session.userId.isNotEmpty()
    }
}

data class SessionData(
    val userId: String = "",
    val userName: String = "",
    val phoneNumber: String = "",
    val familyId: String = ""
)

// ============== HELPER FUNCTIONS ==============
fun hashPassword(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

fun generateUniqueFamilyNumber(): String {
    val randomNum = Random.nextInt(100000, 999999)
    return "FAM-$randomNum"
}

// ============== APP STATE & NAVIGATION ==============
enum class AppScreen {
    WELCOME,
    REGISTER,
    VERIFY_REGISTER_OTP,
    LOGIN,
    FORGOT_PASSWORD,
    VERIFY_FORGOT_PASSWORD_OTP,
    RESET_PASSWORD,
    CREATE_OR_JOIN_FAMILY,
    CREATE_FAMILY,
    JOIN_FAMILY,
    DASHBOARD,
    FAMILY_MEMBERS,
    MEMBER_DETAIL,
    ADD_MEMBER_DATA,
    EDIT_PERSONAL_INFO,
    VIEW_MEMBER_FILES,
    EXPENSE_OVERVIEW,
    ADD_EXPENSE,
    EDIT_EXPENSE,
    REPORTS,
    ANALYTICS,
    GROCERY_LIST,
    BILL_REMINDERS,
    PROFILE,
    AI_BOTS,
    LIVE_LOCATIONS,
    AI_COOK
}

@Composable
fun AppNavigationWithSession(sessionManager: SessionManager) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf(AppScreen.WELCOME) }

    var phoneNumber by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var selectedMemberId by remember { mutableStateOf("") }
    var selectedExpenseId by remember { mutableStateOf("") }

    // CRITICAL FIX: This should check session and set screen BEFORE showing anything
    LaunchedEffect(Unit) {
        try {
            val sessionData = sessionManager.sessionData.first()

            // Check if user is logged in by checking if userId is not empty
            if (sessionData.userId.isNotEmpty()) {
                // User is logged in - update UserSession and go to Dashboard
                UserSession.updateFromSessionData(sessionData)
                Log.d("AppNavigation", "User is logged in, userId: ${sessionData.userId}")
                currentScreen = AppScreen.DASHBOARD
            } else {
                // User is not logged in - go to Welcome screen
                Log.d("AppNavigation", "User is not logged in")
                currentScreen = AppScreen.WELCOME
            }
        } catch (e: Exception) {
            Log.e("AppNavigation", "Error checking session: ${e.message}")
            currentScreen = AppScreen.WELCOME
        } finally {
            isLoading = false
        }
    }

    // ADD THIS LOADING SCREEN:
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = Color(0xFF7DDDD3))
                Text(
                    text = "Loading...",
                    color = Color(0xFF6B6B6B),
                    fontSize = 16.sp
                )
            }
        }
        return
    }

    // Handle Android back button (keep existing code)
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        currentScreen = when (currentScreen) {
            AppScreen.REGISTER -> AppScreen.WELCOME
            AppScreen.LOGIN -> AppScreen.WELCOME
            AppScreen.VERIFY_REGISTER_OTP -> AppScreen.REGISTER
            AppScreen.FORGOT_PASSWORD -> AppScreen.LOGIN
            AppScreen.VERIFY_FORGOT_PASSWORD_OTP -> AppScreen.FORGOT_PASSWORD
            AppScreen.RESET_PASSWORD -> AppScreen.LOGIN
            AppScreen.CREATE_OR_JOIN_FAMILY -> AppScreen.WELCOME
            AppScreen.CREATE_FAMILY -> AppScreen.CREATE_OR_JOIN_FAMILY
            AppScreen.JOIN_FAMILY -> AppScreen.CREATE_OR_JOIN_FAMILY
            AppScreen.FAMILY_MEMBERS -> AppScreen.DASHBOARD
            AppScreen.MEMBER_DETAIL -> AppScreen.FAMILY_MEMBERS
            AppScreen.ADD_MEMBER_DATA -> AppScreen.MEMBER_DETAIL
            AppScreen.EDIT_PERSONAL_INFO -> AppScreen.MEMBER_DETAIL
            AppScreen.VIEW_MEMBER_FILES -> AppScreen.MEMBER_DETAIL
            AppScreen.EXPENSE_OVERVIEW -> AppScreen.DASHBOARD
            AppScreen.ADD_EXPENSE -> AppScreen.EXPENSE_OVERVIEW
            AppScreen.EDIT_EXPENSE -> AppScreen.EXPENSE_OVERVIEW
            AppScreen.REPORTS -> AppScreen.EXPENSE_OVERVIEW
            AppScreen.ANALYTICS -> AppScreen.EXPENSE_OVERVIEW
            AppScreen.GROCERY_LIST -> AppScreen.DASHBOARD
            AppScreen.BILL_REMINDERS -> AppScreen.DASHBOARD
            AppScreen.PROFILE -> AppScreen.DASHBOARD
            AppScreen.AI_BOTS -> AppScreen.DASHBOARD
            AppScreen.AI_COOK -> AppScreen.AI_BOTS
            AppScreen.LIVE_LOCATIONS -> AppScreen.DASHBOARD
            else -> currentScreen
        }
    }
    when (currentScreen) {
        AppScreen.WELCOME -> {
            WelcomeScreen(
                onRegisterClick = { currentScreen = AppScreen.REGISTER },
                onLoginClick = { currentScreen = AppScreen.LOGIN }
            )
        }
        AppScreen.REGISTER -> {
            RegisterScreen(
                onBackClick = { currentScreen = AppScreen.WELCOME },
                onOtpSent = { phone, name, pass, verificationIdResult ->
                    phoneNumber = phone
                    userName = name
                    password = pass
                    verificationId = verificationIdResult
                    currentScreen = AppScreen.VERIFY_REGISTER_OTP
                },
                onLoginClick = { currentScreen = AppScreen.LOGIN }
            )
        }
        AppScreen.VERIFY_REGISTER_OTP -> {
            // Skip OTP verification, go directly to create/join family
            currentScreen = AppScreen.CREATE_OR_JOIN_FAMILY
        }
        AppScreen.LOGIN -> {
            LoginScreen(
                onBackClick = { currentScreen = AppScreen.WELCOME },
                onLoginSuccess = { hasFamilyId ->
                    if (hasFamilyId) {
                        currentScreen = AppScreen.DASHBOARD
                    } else {
                        currentScreen = AppScreen.CREATE_OR_JOIN_FAMILY
                    }
                },
                onRegisterClick = { currentScreen = AppScreen.REGISTER },
                onForgotPasswordClick = { currentScreen = AppScreen.FORGOT_PASSWORD }
            )
        }
        AppScreen.FORGOT_PASSWORD -> {
            ForgotPasswordScreen(
                onBackClick = { currentScreen = AppScreen.LOGIN },
                onOtpSent = { phone, verificationIdResult ->
                    phoneNumber = phone
                    verificationId = verificationIdResult
                    currentScreen = AppScreen.VERIFY_FORGOT_PASSWORD_OTP
                }
            )
        }
        AppScreen.VERIFY_FORGOT_PASSWORD_OTP -> {
            VerifyForgotPasswordOtpScreen(
                phoneNumber = phoneNumber,
                verificationId = verificationId,
                onBackClick = { currentScreen = AppScreen.FORGOT_PASSWORD },
                onVerificationSuccess = { currentScreen = AppScreen.RESET_PASSWORD }
            )
        }
        AppScreen.RESET_PASSWORD -> {
            ResetPasswordScreen(
                phoneNumber = phoneNumber,
                onBackClick = { currentScreen = AppScreen.LOGIN },
                onPasswordReset = { currentScreen = AppScreen.LOGIN }
            )
        }
        AppScreen.CREATE_OR_JOIN_FAMILY -> {
            CreateOrJoinFamilyScreen(
                onBackClick = { currentScreen = AppScreen.WELCOME },
                onCreateFamilyClick = { currentScreen = AppScreen.CREATE_FAMILY },
                onJoinFamilyClick = { currentScreen = AppScreen.JOIN_FAMILY }
            )
        }
        AppScreen.CREATE_FAMILY -> {
            CreateFamilyScreen(
                onBackClick = { currentScreen = AppScreen.CREATE_OR_JOIN_FAMILY },
                onFamilyCreated = { currentScreen = AppScreen.DASHBOARD }
            )
        }
        AppScreen.JOIN_FAMILY -> {
            JoinFamilyScreen(
                onBackClick = { currentScreen = AppScreen.CREATE_OR_JOIN_FAMILY },
                onFamilyJoined = { currentScreen = AppScreen.DASHBOARD }
            )
        }
        AppScreen.DASHBOARD -> {
            DashboardScreen(
                onNavigateToFamilyMembers = {
                    currentScreen = AppScreen.FAMILY_MEMBERS
                },
                onNavigateToExpenses = {
                    currentScreen = AppScreen.EXPENSE_OVERVIEW
                },
                onNavigateToAddExpense = {
                    currentScreen = AppScreen.ADD_EXPENSE
                },
                onNavigateToGroceryList = {  // ← ADD THIS
                    currentScreen = AppScreen.GROCERY_LIST
                },
                onNavigateToBillReminders = {
                    currentScreen = AppScreen.BILL_REMINDERS
                },
                onNavigateToProfile = {  // ← ADD THIS ENTIRE BLOCK
                    currentScreen = AppScreen.PROFILE
                },
                onNavigateToLiveLocations = { currentScreen = AppScreen.LIVE_LOCATIONS },  // ← NEW
                onNavigateToAIBots = { currentScreen = AppScreen.AI_BOTS }
            )
        }
        AppScreen.FAMILY_MEMBERS -> {
            FamilyMembersScreen(
                onBackClick = { currentScreen = AppScreen.DASHBOARD },
                onMemberClick = { memberId ->
                    selectedMemberId = memberId
                    currentScreen = AppScreen.MEMBER_DETAIL
                }
            )
        }
        AppScreen.MEMBER_DETAIL -> {
            MemberDetailScreen(
                memberId = selectedMemberId,
                onBackClick = { currentScreen = AppScreen.FAMILY_MEMBERS },
                onAddDataClick = { currentScreen = AppScreen.ADD_MEMBER_DATA },
                onEditPersonalInfoClick = { currentScreen = AppScreen.EDIT_PERSONAL_INFO },
                onViewFilesClick = { currentScreen = AppScreen.VIEW_MEMBER_FILES }
            )
        }
        AppScreen.ADD_MEMBER_DATA -> {
            AddMemberDataScreen(
                memberId = selectedMemberId,
                onBackClick = { currentScreen = AppScreen.MEMBER_DETAIL },
                onDataSaved = { currentScreen = AppScreen.MEMBER_DETAIL }
            )
        }
        AppScreen.EDIT_PERSONAL_INFO -> {
            EditPersonalInfoScreen(
                memberId = selectedMemberId,
                onBackClick = { currentScreen = AppScreen.MEMBER_DETAIL },
                onInfoSaved = { currentScreen = AppScreen.MEMBER_DETAIL }
            )
        }
        AppScreen.VIEW_MEMBER_FILES -> {
            ViewMemberFilesScreen(
                memberId = selectedMemberId,
                onBackClick = { currentScreen = AppScreen.MEMBER_DETAIL }
            )
        }
        AppScreen.EXPENSE_OVERVIEW -> {
            ExpenseOverviewScreen(
                onBackClick = { currentScreen = AppScreen.DASHBOARD },
                onAddExpenseClick = { currentScreen = AppScreen.ADD_EXPENSE },
                onEditExpenseClick = { expenseId ->
                    selectedExpenseId = expenseId
                    currentScreen = AppScreen.EDIT_EXPENSE
                },
                onReportsClick = { currentScreen = AppScreen.REPORTS },
                onAnalyticsClick = { currentScreen = AppScreen.ANALYTICS }
            )
        }
        AppScreen.ADD_EXPENSE -> {
            AddExpenseScreen(
                onBackClick = { currentScreen = AppScreen.EXPENSE_OVERVIEW },
                onExpenseSaved = { currentScreen = AppScreen.EXPENSE_OVERVIEW }
            )
        }
        AppScreen.EDIT_EXPENSE -> {
            EditExpenseScreen(
                expenseId = selectedExpenseId,
                onBackClick = { currentScreen = AppScreen.EXPENSE_OVERVIEW },
                onExpenseUpdated = { currentScreen = AppScreen.EXPENSE_OVERVIEW },
                onExpenseDeleted = { currentScreen = AppScreen.EXPENSE_OVERVIEW }
            )
        }
        AppScreen.REPORTS -> {
            ReportsScreen(
                onBackClick = { currentScreen = AppScreen.EXPENSE_OVERVIEW }
            )
        }
        AppScreen.ANALYTICS -> {  // ← ADD THIS ENTIRE BLOCK
            AnalyticsScreen(
                onBackClick = { currentScreen = AppScreen.EXPENSE_OVERVIEW }
            )
        }
        AppScreen.GROCERY_LIST -> {  // ← ADD THIS ENTIRE BLOCK
            GroceryListScreen(
                onBackClick = { currentScreen = AppScreen.DASHBOARD }
            )
        }
        AppScreen.BILL_REMINDERS -> {
            BillRemindersScreen(
                onBackClick = { currentScreen = AppScreen.DASHBOARD }
            )
        }
        AppScreen.PROFILE -> {  // ← ADD THIS ENTIRE BLOCK
            ProfileScreen(
                onBackClick = { currentScreen = AppScreen.DASHBOARD },
                onLogout = { currentScreen = AppScreen.WELCOME }
            )
        }
        AppScreen.AI_BOTS -> {  // ← ADD THIS ENTIRE BLOCK
            AIBotsScreen(
                onBackClick = { currentScreen = AppScreen.FAMILY_MEMBERS },
                onAICookClick = { currentScreen = AppScreen.AI_COOK }
            )
        }
        AppScreen.AI_COOK -> {  // ← ADD THIS ENTIRE BLOCK
            AICookScreen(
                onBackClick = { currentScreen = AppScreen.AI_BOTS }
            )
        }
        AppScreen.LIVE_LOCATIONS -> {
            LiveLocationsScreen(
                onBackClick = { currentScreen = AppScreen.DASHBOARD }
            )
        }
    }
}

// ============== FAMILY MEMBERS SCREEN ==============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMembersScreen(
    onBackClick: () -> Unit,
    onMemberClick: (String) -> Unit,
) {
    var familyMembers by remember { mutableStateOf<List<FamilyMember>>(emptyList()) }
    var userNicknames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }  // ← NEW
    var isLoading by remember { mutableStateOf(true) }

    val db = FirebaseFirestore.getInstance()

    // Load family members and nicknames
    LaunchedEffect(Unit) {
        if (UserSession.familyId.isNotEmpty()) {
            // Load family members
            db.collection("families")
                .document(UserSession.familyId)
                .get()
                .addOnSuccessListener { document ->
                    val members = document.get("members") as? List<Map<String, Any>> ?: emptyList()
                    familyMembers = members.map { member ->
                        FamilyMember(
                            name = member["name"] as? String ?: "",
                            role = member["role"] as? String ?: "",
                            userId = member["userId"] as? String ?: ""
                        )
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }

            // ← NEW: Load user's nicknames for other members
            db.collection("users")
                .document(UserSession.userId)
                .collection("nicknames")
                .get()
                .addOnSuccessListener { snapshot ->
                    val nicknames = mutableMapOf<String, String>()
                    snapshot.documents.forEach { doc ->
                        val targetUserId = doc.getString("targetUserId") ?: ""
                        val nickname = doc.getString("nickname") ?: ""
                        if (targetUserId.isNotEmpty() && nickname.isNotEmpty()) {
                            nicknames[targetUserId] = nickname
                        }
                    }
                    userNicknames = nicknames
                }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))
            .padding(16.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBackClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF6A11CB)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF6A11CB)
                )
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_revert),
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Back",
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color(0xFF6A11CB)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "Family Members",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B6B6B),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF7DDDD3))
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Existing family members
                items(familyMembers) { member ->
                    // ← UPDATED: Pass nickname to card
                    val displayName = if (member.userId == UserSession.userId) {
                        "Me"
                    } else {
                        userNicknames[member.userId] ?: member.name
                    }

                    FamilyMemberCard(
                        member = member,
                        displayName = displayName,
                        onMemberClick = {
                            if (member.userId.isNotEmpty()) {
                                Log.d("FamilyMembers", "Clicked member: ${member.name}, userId: ${member.userId}")
                                onMemberClick(member.userId)
                            } else {
                                Log.e("FamilyMembers", "Member ${member.name} has no userId!")
                            }
                        }
                    )
                }
                // ← REMOVED: "+ NEW", "Add Member Data", and "AI Bots" buttons
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// ← UPDATED: FamilyMemberCard with profile picture
@Composable
fun FamilyMemberCard(
    member: FamilyMember,
    displayName: String,
    onMemberClick: () -> Unit = {}
) {
    var profilePictureUri by remember { mutableStateOf("") }

    LaunchedEffect(member.userId) {
        loadProfilePicture(member.userId) { uri ->
            profilePictureUri = uri
        }
    }

    Card(
        onClick = onMemberClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8E8E0)
        ),
        shape = RoundedCornerShape(40.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            ProfileImage(
                userId = member.userId,
                size = 60.dp,
                backgroundColor = Color(0xFFB8B8A8)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Name (showing nickname or "Me")
            Text(
                text = displayName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2D3748),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ============== MEMBER DETAIL SCREEN ==============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    memberId: String,
    onBackClick: () -> Unit,
    onAddDataClick: () -> Unit = {},
    onEditPersonalInfoClick: () -> Unit = {},
    onViewFilesClick: () -> Unit = {}
) {
    var memberDetail by remember { mutableStateOf<MemberDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var userNickname by remember { mutableStateOf("") }  // ← NEW
    var showNicknameDialog by remember { mutableStateOf(false) }  // ← NEW
    var nicknameInput by remember { mutableStateOf("") }  // ← NEW

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val isViewingOwnProfile = memberId == UserSession.userId  // ← NEW

    // Load member details
    LaunchedEffect(memberId) {
        Log.d("MemberDetail", "Loading member: $memberId")

        if (memberId.isEmpty()) {
            Log.e("MemberDetail", "Member ID is empty")
            isLoading = false
            return@LaunchedEffect
        }

        // Try to find the member in the current family
        db.collection("families")
            .document(UserSession.familyId)
            .get()
            .addOnSuccessListener { document ->
                val members = document.get("members") as? List<Map<String, Any>> ?: emptyList()
                val member = members.find { (it["userId"] as? String) == memberId }

                if (member != null) {
                    Log.d("MemberDetail", "Member found: ${member["name"]}")

                    // ← NEW: Load nickname for this member (if not viewing own profile)
                    if (!isViewingOwnProfile) {
                        db.collection("users")
                            .document(UserSession.userId)
                            .collection("nicknames")
                            .whereEqualTo("targetUserId", memberId)
                            .get()
                            .addOnSuccessListener { nicknameSnapshot ->
                                if (!nicknameSnapshot.isEmpty) {
                                    userNickname = nicknameSnapshot.documents[0].getString("nickname") ?: ""
                                }
                            }
                    }

                    db.collection("families")
                        .document(UserSession.familyId)
                        .collection("memberData")
                        .document(memberId)
                        .collection("personalDetails")
                        .document("info")
                        .get()
                        .addOnSuccessListener { detailsDoc ->
                            val relation = detailsDoc.getString("relation") ?: "Member"
                            val profilePicture = detailsDoc.getString("profilePicture") ?: ""

                            // Load custom sections
                            val sectionsData = detailsDoc.get("customSections") as? List<Map<String, String>> ?: emptyList()
                            val customSections = sectionsData.map { section ->
                                CustomSection(
                                    name = section["name"] as? String ?: "",
                                    value = section["value"] as? String ?: ""
                                )
                            }

                            db.collection("families")
                                .document(UserSession.familyId)
                                .collection("memberData")
                                .document(memberId)
                                .collection("files")
                                .get()
                                .addOnSuccessListener { filesSnapshot ->
                                    // Group files by title
                                    val filesMap = mutableMapOf<String, MutableList<MemberFile>>()

                                    filesSnapshot.documents.forEach { fileDoc ->
                                        val title = fileDoc.getString("title") ?: "Untitled"
                                        val file = MemberFile(
                                            id = fileDoc.id,
                                            fileName = fileDoc.getString("fileName") ?: "",
                                            fileType = fileDoc.getString("fileType") ?: "",
                                            fileUri = fileDoc.getString("fileUri") ?: "",
                                            uploadedAt = fileDoc.getTimestamp("uploadedAt"),
                                            notes = fileDoc.getString("notes") ?: "",
                                            uploadedBy = fileDoc.getString("uploadedBy") ?: memberId  // ← NEW
                                        )

                                        if (!filesMap.containsKey(title)) {
                                            filesMap[title] = mutableListOf()
                                        }
                                        filesMap[title]?.add(file)
                                    }

                                    val fileGroups = filesMap.map { (title, files) ->
                                        FileGroup(title = title, files = files)
                                    }

                                    memberDetail = MemberDetail(
                                        userId = memberId,
                                        name = member["name"] as? String ?: "Unknown",
                                        role = member["role"] as? String ?: "member",
                                        relation = relation,
                                        profilePicture = profilePicture,
                                        customSections = customSections,
                                        fileGroups = fileGroups
                                    )
                                    isLoading = false
                                }
                                .addOnFailureListener { e ->
                                    Log.e("MemberDetail", "Error loading files", e)
                                    memberDetail = MemberDetail(
                                        userId = memberId,
                                        name = member["name"] as? String ?: "Unknown",
                                        role = member["role"] as? String ?: "member",
                                        relation = relation,
                                        profilePicture = profilePicture,
                                        customSections = customSections,
                                        fileGroups = emptyList()
                                    )
                                    isLoading = false
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("MemberDetail", "Error loading member details", e)
                            memberDetail = MemberDetail(
                                userId = memberId,
                                name = member["name"] as? String ?: "Unknown",
                                role = member["role"] as? String ?: "member",
                                relation = "Member",
                                profilePicture = "",
                                customSections = emptyList(),
                                fileGroups = emptyList()
                            )
                            isLoading = false
                        }
                } else {
                    Log.e("MemberDetail", "Member not found in family")
                    isLoading = false
                }
            }
            .addOnFailureListener { e ->
                Log.e("MemberDetail", "Error loading member", e)
                isLoading = false
            }
    }

    // ← NEW: Nickname dialog
    if (showNicknameDialog) {
        AlertDialog(
            onDismissRequest = {
                showNicknameDialog = false
                nicknameInput = ""
            },
            title = { Text("Set Nickname") },
            text = {
                Column {
                    Text(
                        text = "Enter a nickname for ${memberDetail?.name}",
                        fontSize = 14.sp,
                        color = Color(0xFF6B6B6B),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = nicknameInput,
                        onValueChange = { nicknameInput = it },
                        label = { Text("Nickname") },
                        placeholder = { Text("e.g., Mom, Dad, Brother") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8E8E0),
                            unfocusedContainerColor = Color(0xFFE8E8E0),
                            focusedIndicatorColor = Color(0xFF7DDDD3),
                            unfocusedIndicatorColor = Color(0xFFB8B8A8),
                            cursorColor = Color(0xFF7DDDD3)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nicknameInput.isNotBlank()) {
                            // Save nickname to database
                            db.collection("users")
                                .document(UserSession.userId)
                                .collection("nicknames")
                                .document(memberId)
                                .set(hashMapOf(
                                    "targetUserId" to memberId,
                                    "nickname" to nicknameInput,
                                    "setBy" to UserSession.userId,
                                    "updatedAt" to Timestamp.now()
                                ))
                                .addOnSuccessListener {
                                    userNickname = nicknameInput
                                    showNicknameDialog = false
                                    nicknameInput = ""
                                    android.widget.Toast.makeText(
                                        context,
                                        "Nickname saved",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { e ->
                                    android.widget.Toast.makeText(
                                        context,
                                        "Failed to save: ${e.message}",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showNicknameDialog = false
                        nicknameInput = ""
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6A11CB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF7DDDD3))
        }
    } else if (memberDetail != null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F0))
                .padding(16.dp)
        ) {
            // Back button
            item {
                OutlinedButton(
                    onClick = onBackClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = Color(0xFF6A11CB)
                    )
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_revert),
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Back",
                        modifier = Modifier.padding(start = 8.dp),
                        color = Color(0xFF6A11CB)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Profile section
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar with profile picture
                    var memberProfileUri by remember { mutableStateOf("") }

                    LaunchedEffect(memberId) {
                        loadProfilePicture(memberId) { uri ->
                            memberProfileUri = uri
                        }
                    }

                    ProfileImage(
                        userId = memberId,
                        size = 120.dp,
                        backgroundColor = Color(0xFF7DDDD3),
                        iconSize = 60.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = memberDetail!!.name,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B6B6B)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ← UPDATED: Relation badge with edit icon for other members
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF7DDDD3))
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (isViewingOwnProfile) "Me" else (userNickname.ifEmpty { memberDetail!!.relation }),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        // ← NEW: Edit icon for other members
                        if (!isViewingOwnProfile) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    nicknameInput = userNickname
                                    showNicknameDialog = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_edit),
                                    contentDescription = "Edit Nickname",
                                    tint = Color(0xFF7DDDD3),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Personal Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8E8E0)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Personal Details",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3748)
                            )
                            // ← UPDATED: Only show Edit button for own profile
                            if (isViewingOwnProfile) {
                                OutlinedButton(
                                    onClick = onEditPersonalInfoClick,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF7DDDD3)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.5.dp,
                                        color = Color(0xFF7DDDD3)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_edit),
                                        contentDescription = "Edit",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Edit",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        if (memberDetail!!.customSections.isEmpty()) {
                            Text(
                                text = "No personal details added yet",
                                fontSize = 14.sp,
                                color = Color(0xFF9CA3AF),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            memberDetail!!.customSections.forEach { section ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${section.name}: ",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF6B6B6B)
                                    )
                                    Text(
                                        text = section.value,
                                        fontSize = 16.sp,
                                        color = Color(0xFF2D3748)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Stored Files Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8E8E0)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Stored Files",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3748)
                            )
                            OutlinedButton(
                                onClick = onViewFilesClick,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF7DDDD3)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.5.dp,
                                    color = Color(0xFF7DDDD3)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_view),
                                    contentDescription = "View All",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "View All",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        val totalFiles = memberDetail!!.fileGroups.sumOf { it.files.size }
                        if (totalFiles == 0) {
                            Text(
                                text = "No files uploaded yet",
                                fontSize = 14.sp,
                                color = Color(0xFF9CA3AF),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Text(
                                text = "$totalFiles file(s) stored",
                                fontSize = 16.sp,
                                color = Color(0xFF2D3748)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // ← UPDATED: Add Data button only for own profile
            if (isViewingOwnProfile) {
                item {
                    Button(
                        onClick = onAddDataClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7DDDD3)
                        ),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Text(
                            text = "Add Data",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// ============== ADD MEMBER DATA SCREEN - FIREBASE STORAGE (SIMPLE) ==============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberDataScreen(
    memberId: String,
    onBackClick: () -> Unit,
    onDataSaved: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // File picker launcher - uploads to Firebase Storage
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            // Get filename
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            var fileName = "file_${System.currentTimeMillis()}"
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = it.getString(nameIndex)
                    }
                }
            }
            selectedFileName = fileName

            android.widget.Toast.makeText(
                context,
                "Uploading file...",
                android.widget.Toast.LENGTH_SHORT
            ).show()

            // Upload to Firebase Storage
            try {
                val storage = com.google.firebase.storage.FirebaseStorage.getInstance()
                val timestamp = System.currentTimeMillis()
                val storageRef = storage.reference
                    .child("family_files")
                    .child(UserSession.familyId)
                    .child("${timestamp}_${fileName}")

                storageRef.putFile(uri)
                    .addOnSuccessListener {
                        // Get permanent download URL
                        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            selectedFileUri = downloadUri
                            android.widget.Toast.makeText(
                                context,
                                "File uploaded successfully!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        selectedFileUri = null
                        selectedFileName = ""
                        android.widget.Toast.makeText(
                            context,
                            "Upload failed: ${e.message}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        android.util.Log.e("FileUpload", "Upload error", e)
                    }
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    context,
                    "Error: ${e.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onDataSaved()
            },
            title = { Text("Success!") },
            text = { Text("File data has been saved successfully.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onDataSaved()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    )
                ) {
                    Text("OK")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))
            .padding(16.dp)
    ) {
        // Back button
        item {
            OutlinedButton(
                onClick = onBackClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF6A11CB)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF6A11CB)
                )
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_revert),
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Back",
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color(0xFF6A11CB)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Title
        item {
            Text(
                text = "Add Member Data",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B6B6B),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Title field
        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Title", color = Color(0xFF9CA3AF)) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE8E8E0),
                    unfocusedContainerColor = Color(0xFFE8E8E0),
                    focusedIndicatorColor = Color(0xFF7DDDD3),
                    unfocusedIndicatorColor = Color(0xFFB8B8A8),
                    cursorColor = Color(0xFF7DDDD3)
                ),
                shape = RoundedCornerShape(40.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Upload area
        item {
            Card(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8E8E0)
                ),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    color = Color(0xFF7DDDD3)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (selectedFileName.isEmpty()) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_upload),
                            contentDescription = "Upload",
                            tint = Color(0xFF7DDDD3),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tap to upload images, PDFs, documents...",
                            fontSize = 16.sp,
                            color = Color(0xFF2D3748),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_info_details),
                            contentDescription = "File",
                            tint = Color(0xFF7DDDD3),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = selectedFileName,
                            fontSize = 16.sp,
                            color = Color(0xFF2D3748),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to change file",
                            fontSize = 14.sp,
                            color = Color(0xFF718096),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Notes field
        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Notes", color = Color(0xFF9CA3AF)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE8E8E0),
                    unfocusedContainerColor = Color(0xFFE8E8E0),
                    focusedIndicatorColor = Color(0xFF7DDDD3),
                    unfocusedIndicatorColor = Color(0xFFB8B8A8),
                    cursorColor = Color(0xFF7DDDD3)
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 8
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Save button
        item {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        android.widget.Toast.makeText(context, "Please enter a title", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (selectedFileUri == null) {
                        android.widget.Toast.makeText(context, "Please select a file", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isUploading = true

                    // Get file extension
                    val fileExtension = if (selectedFileName.contains(".")) {
                        selectedFileName.substringAfterLast(".")
                    } else {
                        "bin"
                    }

                    // Rename file
                    val renamedFileName = if (fileExtension.isNotEmpty() && fileExtension != "bin") {
                        "$title.$fileExtension"
                    } else {
                        title
                    }

                    val fileData = hashMapOf(
                        "title" to title,
                        "fileName" to renamedFileName,
                        "fileType" to fileExtension,
                        "fileUri" to (selectedFileUri?.toString() ?: ""),
                        "notes" to notes,
                        "uploadedAt" to Timestamp.now(),
                        "uploadedBy" to UserSession.userId
                    )

                    // Save to Firestore
                    db.collection("families")
                        .document(UserSession.familyId)
                        .collection("memberData")
                        .document(memberId)
                        .collection("files")
                        .add(fileData)
                        .addOnSuccessListener {
                            isUploading = false
                            showSuccessDialog = true
                        }
                        .addOnFailureListener { e ->
                            isUploading = false
                            android.widget.Toast.makeText(
                                context,
                                "Failed to save: ${e.message}",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7DDDD3),
                    disabledContainerColor = Color(0xFFB8B8A8)
                ),
                shape = RoundedCornerShape(32.dp),
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Saving...",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Save",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ============== FIXED EDIT PERSONAL INFO SCREEN ==============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPersonalInfoScreen(
    memberId: String,
    onBackClick: () -> Unit,
    onInfoSaved: () -> Unit
) {
    var customSections by remember { mutableStateOf<List<CustomSection>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showAddSectionDialog by remember { mutableStateOf(false) }
    var showEditSectionDialog by remember { mutableStateOf(false) }
    var editingSectionIndex by remember { mutableStateOf(-1) }
    var newSectionName by remember { mutableStateOf("") }
    var newSectionValue by remember { mutableStateOf("") }

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // Load existing data - FIXED PATH
    LaunchedEffect(memberId) {
        if (memberId.isNotEmpty()) {
            // Load from the FAMILY collection, not user collection
            db.collection("families")
                .document(UserSession.familyId)
                .collection("memberData")
                .document(memberId)
                .collection("personalDetails")
                .document("info")
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Log.d("EditPersonalInfo", "Document exists, loading sections...")
                        // Load custom sections
                        val sectionsData = document.get("customSections") as? List<Map<String, String>> ?: emptyList()
                        Log.d("EditPersonalInfo", "Loaded ${sectionsData.size} sections")
                        customSections = sectionsData.map { section ->
                            CustomSection(
                                name = section["name"] ?: "",
                                value = section["value"] ?: ""
                            )
                        }
                    } else {
                        Log.d("EditPersonalInfo", "Document does not exist")
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    Log.e("EditPersonalInfo", "Failed to load", e)
                    isLoading = false
                }
        }
    }

    // Add section dialog
    if (showAddSectionDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddSectionDialog = false
                newSectionName = ""
                newSectionValue = ""
            },
            title = { Text("Add New Section") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newSectionName,
                        onValueChange = { newSectionName = it },
                        label = { Text("Section Name") },
                        placeholder = { Text("e.g., Aadhaar, Voter ID, Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8E8E0),
                            unfocusedContainerColor = Color(0xFFE8E8E0),
                            focusedIndicatorColor = Color(0xFF7DDDD3),
                            unfocusedIndicatorColor = Color(0xFFB8B8A8),
                            cursorColor = Color(0xFF7DDDD3)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newSectionValue,
                        onValueChange = { newSectionValue = it },
                        label = { Text("Value") },
                        placeholder = { Text("Enter value") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8E8E0),
                            unfocusedContainerColor = Color(0xFFE8E8E0),
                            focusedIndicatorColor = Color(0xFF7DDDD3),
                            unfocusedIndicatorColor = Color(0xFFB8B8A8),
                            cursorColor = Color(0xFF7DDDD3)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSectionName.isNotBlank() && newSectionValue.isNotBlank()) {
                            customSections = customSections + CustomSection(newSectionName, newSectionValue)
                            newSectionName = ""
                            newSectionValue = ""
                            showAddSectionDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showAddSectionDialog = false
                        newSectionName = ""
                        newSectionValue = ""
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6A11CB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Edit section dialog
    if (showEditSectionDialog && editingSectionIndex >= 0) {
        AlertDialog(
            onDismissRequest = {
                showEditSectionDialog = false
                editingSectionIndex = -1
                newSectionName = ""
                newSectionValue = ""
            },
            title = { Text("Edit Section") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newSectionName,
                        onValueChange = { newSectionName = it },
                        label = { Text("Section Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8E8E0),
                            unfocusedContainerColor = Color(0xFFE8E8E0),
                            focusedIndicatorColor = Color(0xFF7DDDD3),
                            unfocusedIndicatorColor = Color(0xFFB8B8A8),
                            cursorColor = Color(0xFF7DDDD3)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newSectionValue,
                        onValueChange = { newSectionValue = it },
                        label = { Text("Value") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8E8E0),
                            unfocusedContainerColor = Color(0xFFE8E8E0),
                            focusedIndicatorColor = Color(0xFF7DDDD3),
                            unfocusedIndicatorColor = Color(0xFFB8B8A8),
                            cursorColor = Color(0xFF7DDDD3)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSectionName.isNotBlank() && newSectionValue.isNotBlank()) {
                            customSections = customSections.toMutableList().apply {
                                this[editingSectionIndex] = CustomSection(newSectionName, newSectionValue)
                            }
                            newSectionName = ""
                            newSectionValue = ""
                            showEditSectionDialog = false
                            editingSectionIndex = -1
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showEditSectionDialog = false
                        editingSectionIndex = -1
                        newSectionName = ""
                        newSectionValue = ""
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6A11CB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onInfoSaved()
            },
            title = { Text("Success!") },
            text = { Text("Personal information has been saved successfully.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onInfoSaved()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF7DDDD3))
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F0))
                .padding(16.dp)
        ) {
            // Back button
            item {
                OutlinedButton(
                    onClick = onBackClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = Color(0xFF6A11CB)
                    )
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_revert),
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Back",
                        modifier = Modifier.padding(start = 8.dp),
                        color = Color(0xFF6A11CB)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Title
            item {
                Text(
                    text = "Edit Personal Information",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B6B6B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Show message if no sections
            if (customSections.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8E8E0)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No sections added yet.\nClick 'Add New Section' below to start.",
                                fontSize = 16.sp,
                                color = Color(0xFF6B6B6B),
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Custom sections
            items(customSections.size) { index ->
                val section = customSections[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = section.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B6B6B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = section.value,
                                fontSize = 16.sp,
                                color = Color(0xFF2D3748)
                            )
                        }

                        // Edit button
                        IconButton(
                            onClick = {
                                editingSectionIndex = index
                                newSectionName = section.name
                                newSectionValue = section.value
                                showEditSectionDialog = true
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_edit),
                                contentDescription = "Edit",
                                tint = Color(0xFF7DDDD3),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Delete button
                        IconButton(
                            onClick = {
                                customSections = customSections.filterIndexed { i, _ -> i != index }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_delete),
                                contentDescription = "Delete",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Add Section button
            item {
                OutlinedButton(
                    onClick = { showAddSectionDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF7DDDD3)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_input_add),
                        contentDescription = "Add",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add New Section",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Save button
            item {
                Button(
                    onClick = {
                        isSaving = true

                        val sectionsData = customSections.map { section ->
                            hashMapOf(
                                "name" to section.name,
                                "value" to section.value
                            )
                        }

                        val personalInfo = hashMapOf(
                            "customSections" to sectionsData,
                            "updatedAt" to Timestamp.now(),
                            "updatedBy" to UserSession.userId
                        )

                        Log.d("EditPersonalInfo", "Saving ${sectionsData.size} sections")

                        // Save to the correct path
                        db.collection("families")
                            .document(UserSession.familyId)
                            .collection("memberData")
                            .document(UserSession.userId)
                            .collection("personalDetails")
                            .document("info")
                            .set(personalInfo)
                            .addOnSuccessListener {
                                Log.d("EditPersonalInfo", "Successfully saved")
                                isSaving = false
                                showSuccessDialog = true
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditPersonalInfo", "Failed to save", e)
                                isSaving = false
                                android.widget.Toast.makeText(
                                    context,
                                    "Failed to save: ${e.message}",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3),
                        disabledContainerColor = Color(0xFFB8B8A8)
                    ),
                    shape = RoundedCornerShape(32.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Saving...",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Save",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ============== VIEW MEMBER FILES SCREEN ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewMemberFilesScreen(
    memberId: String,
    onBackClick: () -> Unit
) {
    var fileGroups by remember { mutableStateOf<List<FileGroup>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<MemberFile?>(null) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Load files grouped by title
    LaunchedEffect(memberId) {
        if (memberId.isNotEmpty()) {
            db.collection("families")
                .document(UserSession.familyId)
                .collection("memberData")
                .document(memberId)
                .collection("files")
                .get()
                .addOnSuccessListener { documents ->
                    val filesMap = mutableMapOf<String, MutableList<MemberFile>>()

                    documents.forEach { doc ->
                        val title = doc.getString("title") ?: "Untitled"
                        val file = MemberFile(
                            id = doc.id,
                            fileName = doc.getString("fileName") ?: "",
                            fileType = doc.getString("fileType") ?: "",
                            fileUri = doc.getString("fileUri") ?: "",
                            uploadedAt = doc.getTimestamp("uploadedAt"),
                            notes = doc.getString("notes") ?: "",
                            uploadedBy = doc.getString("uploadedBy") ?: memberId
                        )

                        if (!filesMap.containsKey(title)) {
                            filesMap[title] = mutableListOf()
                        }
                        filesMap[title]?.add(file)
                    }

                    fileGroups = filesMap.map { (title, files) ->
                        FileGroup(title = title, files = files)
                    }

                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && fileToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete File") },
            text = { Text("Are you sure you want to delete ${fileToDelete?.fileName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        fileToDelete?.let { file ->
                            // Delete from storage first
                            deleteFileFromStorage(context, file.fileUri)

                            // Then delete from Firestore
                            db.collection("families")
                                .document(UserSession.familyId)
                                .collection("memberData")
                                .document(memberId)
                                .collection("files")
                                .document(file.id)
                                .delete()
                                .addOnSuccessListener {
                                    showDeleteDialog = false
                                    fileToDelete = null
                                    isLoading = true

                                    // Re-fetch files
                                    db.collection("families")  // ← CORRECT
                                        .document(UserSession.familyId)
                                        .collection("memberData")
                                        .document(memberId)
                                        .collection("files")
                                        .get()
                                        .addOnSuccessListener { documents ->
                                            val filesMap = mutableMapOf<String, MutableList<MemberFile>>()

                                            documents.forEach { doc ->
                                                val title = doc.getString("title") ?: "Untitled"
                                                val newFile = MemberFile(
                                                    id = doc.id,
                                                    fileName = doc.getString("fileName") ?: "",
                                                    fileType = doc.getString("fileType") ?: "",
                                                    fileUri = doc.getString("fileUri") ?: "",
                                                    uploadedAt = doc.getTimestamp("uploadedAt"),
                                                    notes = doc.getString("notes") ?: "",
                                                    uploadedBy = doc.getString("uploadedBy") ?: memberId
                                                )

                                                if (!filesMap.containsKey(title)) {
                                                    filesMap[title] = mutableListOf()
                                                }
                                                filesMap[title]?.add(newFile)
                                            }

                                            fileGroups = filesMap.map { (title, files) ->
                                                FileGroup(title = title, files = files)
                                            }

                                            isLoading = false
                                        }
                                }
                                .addOnFailureListener { e ->
                                    android.widget.Toast.makeText(
                                        context,
                                        "Failed to delete: ${e.message}",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    showDeleteDialog = false
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF7DDDD3))
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F0))
                .padding(16.dp)
        ) {
            // Back button
            item {
                OutlinedButton(
                    onClick = onBackClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = Color(0xFF6A11CB)
                    )
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_revert),
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Back",
                        modifier = Modifier.padding(start = 8.dp),
                        color = Color(0xFF6A11CB)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Title
            item {
                Text(
                    text = "Stored Files",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B6B6B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (fileGroups.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8E8E0)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                                contentDescription = "No files",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No files uploaded yet",
                                fontSize = 18.sp,
                                color = Color(0xFF9CA3AF),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(fileGroups) { group ->
                    FileGroupCardWithExpandableNotes(
                        fileGroup = group,
                        onDeleteFile = { file ->
                            fileToDelete = file
                            showDeleteDialog = true
                        },
                        context = context,
                        isOwner = memberId == UserSession.userId
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ============== FILE GROUP CARD ==============

@Composable
fun FileGroupCardWithExpandableNotes(
    fileGroup: FileGroup,
    onDeleteFile: (MemberFile) -> Unit,
    context: android.content.Context,
    isOwner: Boolean
) {
    var isNotesExpanded by remember { mutableStateOf(false) }
    val notes = fileGroup.files.firstOrNull()?.notes ?: ""

    // Calculate if notes need expansion (more than 2 lines, roughly 80 characters)
    val needsExpansion = notes.length > 80

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8E8E0)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Title
            Text(
                text = fileGroup.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748)
            )

            // Notes below title (expandable)
            if (notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (needsExpansion && !isNotesExpanded) {
                        notes.take(80) + "..."
                    } else {
                        notes
                    },
                    fontSize = 14.sp,
                    color = Color(0xFF6B6B6B),
                    lineHeight = 20.sp
                )

                if (needsExpansion) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isNotesExpanded) "Show Less" else "More",
                        fontSize = 14.sp,
                        color = Color(0xFF7DDDD3),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { isNotesExpanded = !isNotesExpanded }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Files list
            fileGroup.files.forEach { file ->
                FileItemCardSimple(
                    file = file,
                    onDeleteFile = { onDeleteFile(file) },
                    onViewFile = {
                        openFile(context, file)
                    },
                    context = context,
                    showDeleteButton = isOwner
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // View button
                OutlinedButton(
                    onClick = {
                        if (fileGroup.files.isNotEmpty()) {
                            openFile(context, fileGroup.files[0])
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF7DDDD3)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_view),
                        contentDescription = "View",
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Download button
                OutlinedButton(
                    onClick = {
                        fileGroup.files.forEach { file ->
                            downloadFile(context, file)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF7DDDD3)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_download),
                        contentDescription = "Download",
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Share button
                OutlinedButton(
                    onClick = {
                        shareFiles(context, fileGroup)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF7DDDD3)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_share),
                        contentDescription = "Share",
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Delete button (only for owner)
                if (isOwner) {
                    OutlinedButton(
                        onClick = {
                            fileGroup.files.forEach { file ->
                                onDeleteFile(file)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 2.dp,
                            color = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_delete),
                            contentDescription = "Delete",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============== FILE ITEM CARD ==============

@Composable
fun FileItemCardSimple(
    file: MemberFile,
    onDeleteFile: () -> Unit,
    onViewFile: () -> Unit,
    context: android.content.Context,
    showDeleteButton: Boolean = true
) {
    Card(
        onClick = onViewFile,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (file.fileType.lowercase()) {
                            "pdf" -> Color(0xFFFFEBEE)
                            "jpg", "jpeg", "png", "gif", "image" -> Color(0xFFE3F2FD)
                            "docx", "doc", "word" -> Color(0xFFE8F5E9)
                            else -> Color(0xFFF5F5F5)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = when (file.fileType.lowercase()) {
                            "pdf" -> android.R.drawable.ic_menu_report_image
                            "jpg", "jpeg", "png", "gif", "image" -> android.R.drawable.ic_menu_gallery
                            else -> android.R.drawable.ic_menu_info_details
                        }
                    ),
                    contentDescription = file.fileName,
                    tint = when (file.fileType.lowercase()) {
                        "pdf" -> Color(0xFFD32F2F)
                        "jpg", "jpeg", "png", "gif", "image" -> Color(0xFF1976D2)
                        "docx", "doc", "word" -> Color(0xFF388E3C)
                        else -> Color(0xFF757575)
                    },
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // File name
            Text(
                text = file.fileName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2D3748),
                modifier = Modifier.weight(1f)
            )

            // View icon hint
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_view),
                contentDescription = "Tap to view",
                tint = Color(0xFF7DDDD3),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ============================================================================
// FIXED FILE OPERATION FUNCTIONS
// ============================================================================

// Open file - Download first, then open with local file URI OR in-app viewer
private fun openFile(context: Context, file: MemberFile) {
    try {
        if (!file.fileUri.startsWith("https://firebasestorage.googleapis.com")) {
            Toast.makeText(context, "This file uses old format. Please re-upload it.", Toast.LENGTH_LONG).show()
            return
        }

        downloadAndOpenFile(context, file)

    } catch (e: Exception) {
        Toast.makeText(context, "Unable to open file: ${e.message}", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
}



// Open PDF in-app using WebView with loading indicator
private fun downloadAndOpenFile(context: Context, file: MemberFile) {
    Thread {
        try {
            val cacheDir = File(context.cacheDir, "opened_files")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val url = URL(file.fileUri)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            val input = connection.inputStream
            val outputFile = File(cacheDir, file.fileName)
            val output = FileOutputStream(outputFile)

            input.copyTo(output)
            input.close()
            output.close()

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outputFile
            )

            Handler(Looper.getMainLooper()).post {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(fileUri, getMimeType(file.fileType))
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No app available to open this file type", Toast.LENGTH_LONG).show()
                }
            }

        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Failed to open file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }.start()
}


// Download file - works with Firebase Storage URLs
private fun downloadFile(context: android.content.Context, file: MemberFile) {
    try {
        // Check if it's a Firebase Storage URL
        if (file.fileUri.startsWith("https://firebasestorage.googleapis.com")) {
            // Download Firebase Storage file
            val uri = android.net.Uri.parse(file.fileUri)

            val downloadManager = context.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
            val request = android.app.DownloadManager.Request(uri).apply {
                setTitle(file.fileName)
                setDescription("Downloading ${file.fileName}")
                setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    android.os.Environment.DIRECTORY_DOWNLOADS,
                    file.fileName
                )
            }

            downloadManager.enqueue(request)

            android.widget.Toast.makeText(
                context,
                "Downloading ${file.fileName}...",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } else {
            // Legacy content:// URI
            android.widget.Toast.makeText(
                context,
                "This file uses old format. Please re-upload it.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "Download failed: ${e.message}",
            android.widget.Toast.LENGTH_LONG
        ).show()
        android.util.Log.e("FileDownload", "Error downloading file", e)
    }
}

// FIXED: Share files by downloading them first, then sharing the local copies
private fun shareFiles(context: android.content.Context, fileGroup: FileGroup) {
    try {
        // Filter out legacy URIs
        val validFiles = fileGroup.files.filter {
            it.fileUri.startsWith("https://firebasestorage.googleapis.com")
        }

        if (validFiles.isEmpty()) {
            android.widget.Toast.makeText(
                context,
                "These files use old format. Please re-upload them.",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return
        }

        android.widget.Toast.makeText(
            context,
            "Preparing files for sharing...",
            android.widget.Toast.LENGTH_SHORT
        ).show()

        // Use Thread instead of coroutine (no suspend function needed)
        Thread {
            try {
                val downloadedUris = mutableListOf<android.net.Uri>()

                // Create a cache directory for temporary files
                val cacheDir = java.io.File(context.cacheDir, "shared_files")
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }

                // Download each file
                for (file in validFiles) {
                    try {
                        val url = java.net.URL(file.fileUri)
                        val connection = url.openConnection() as java.net.HttpURLConnection
                        connection.connect()

                        val inputStream = connection.inputStream
                        val outputFile = java.io.File(cacheDir, file.fileName)
                        val outputStream = java.io.FileOutputStream(outputFile)

                        inputStream.copyTo(outputStream)

                        inputStream.close()
                        outputStream.close()

                        // Use FileProvider to get shareable URI
                        val fileUri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            outputFile
                        )
                        downloadedUris.add(fileUri)
                    } catch (e: Exception) {
                        android.util.Log.e("FileShare", "Error downloading file: ${file.fileName}", e)
                    }
                }

                // Switch to main thread for sharing using Handler
                val handler = android.os.Handler(android.os.Looper.getMainLooper())
                handler.post {
                    if (downloadedUris.isEmpty()) {
                        android.widget.Toast.makeText(
                            context,
                            "Failed to prepare files for sharing",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        return@post
                    }

                    if (downloadedUris.size == 1) {
                        // Single file
                        val file = validFiles[0]
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND)
                        intent.type = getMimeType(file.fileType)
                        intent.putExtra(android.content.Intent.EXTRA_STREAM, downloadedUris[0])
                        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, file.fileName)
                        intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)

                        val chooser = android.content.Intent.createChooser(intent, "Share ${file.fileName}")
                        chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(chooser)
                    } else {
                        // Multiple files
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND_MULTIPLE)
                        intent.type = "*/*"
                        intent.putParcelableArrayListExtra(
                            android.content.Intent.EXTRA_STREAM,
                            ArrayList(downloadedUris)
                        )
                        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, fileGroup.title)
                        intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)

                        val chooser = android.content.Intent.createChooser(intent, "Share files")
                        chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(chooser)
                    }
                }
            } catch (e: Exception) {
                val handler = android.os.Handler(android.os.Looper.getMainLooper())
                handler.post {
                    android.widget.Toast.makeText(
                        context,
                        "Failed to share files: ${e.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
                android.util.Log.e("FileShare", "Error in share operation", e)
            }
        }.start() // Start the thread
    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "Unable to share files: ${e.message}",
            android.widget.Toast.LENGTH_LONG
        ).show()
        android.util.Log.e("FileShare", "Error sharing files", e)
    }
}

// Delete file from Firebase Storage
private fun deleteFileFromStorage(context: android.content.Context, fileUri: String) {
    try {
        // Check if it's a Firebase Storage URL
        if (fileUri.startsWith("https://firebasestorage.googleapis.com")) {
            // Delete from Firebase Storage
            val storage = com.google.firebase.storage.FirebaseStorage.getInstance()
            val fileRef = storage.getReferenceFromUrl(fileUri)

            fileRef.delete()
                .addOnSuccessListener {
                    android.util.Log.d("FileDelete", "File deleted from Firebase Storage")
                }
                .addOnFailureListener { e ->
                    android.util.Log.w("FileDelete", "Failed to delete from storage: ${e.message}")
                }
        }
        // Legacy content:// URIs - don't try to delete (we don't own them)
    } catch (e: Exception) {
        android.util.Log.e("FileDelete", "Error deleting file from storage", e)
    }
}

// Get MIME type from file type/extension
fun getMimeType(fileExtension: String): String {
    return when (fileExtension.lowercase()) {
        "pdf" -> "application/pdf"
        "doc" -> "application/msword"
        "docx", "word" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xls" -> "application/vnd.ms-excel"
        "xlsx", "excel" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "ppt" -> "application/vnd.ms-powerpoint"
        "pptx", "powerpoint" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "image" -> "image/*"
        "txt", "text" -> "text/plain"
        "zip" -> "application/zip"
        "mp4" -> "video/mp4"
        "mp3" -> "audio/mpeg"
        else -> "*/*"
    }
}

// ============== EXPENSE OVERVIEW SCREEN ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseOverviewScreen(
    onBackClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onEditExpenseClick: (String) -> Unit = {},
    onReportsClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {}
) {
    var expenses by remember { mutableStateOf<List<ExpenseItem>>(emptyList()) }
    var summary by remember { mutableStateOf(ExpenseSummary()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCategoryDialog by remember { mutableStateOf(false) }  // ← NEW
    var categories by remember { mutableStateOf<List<ExpenseCategory>>(emptyList()) }  // ← NEW
    var newCategoryName by remember { mutableStateOf("") }  // ← NEW

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // ← NEW: Load categories
    LaunchedEffect(Unit) {
        if (UserSession.familyId.isNotEmpty()) {
            // Load categories
            db.collection("families")
                .document(UserSession.familyId)
                .collection("categories")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener

                    categories = snapshot?.documents?.mapNotNull { doc ->
                        ExpenseCategory(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            createdAt = doc.getTimestamp("createdAt")
                        )
                    } ?: emptyList()

                    // Add default categories if none exist
                    if (categories.isEmpty()) {
                        val defaultCategories = listOf(
                            "Groceries", "Dining", "Utilities", "Transportation",
                            "Healthcare", "Entertainment", "Shopping", "Education", "Other"
                        )
                        defaultCategories.forEach { categoryName ->
                            db.collection("families")
                                .document(UserSession.familyId)
                                .collection("categories")
                                .add(hashMapOf(
                                    "name" to categoryName,
                                    "createdAt" to Timestamp.now()
                                ))
                        }
                    }
                }

            // Load expenses with real-time updates
            db.collection("families")
                .document(UserSession.familyId)
                .collection("expenses")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ExpenseOverview", "Error loading expenses", error)
                        isLoading = false
                        return@addSnapshotListener
                    }

                    expenses = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            ExpenseItem(
                                id = doc.id,
                                name = doc.getString("name") ?: "",  // ← NEW
                                amount = doc.getDouble("amount") ?: 0.0,
                                category = doc.getString("category") ?: "",
                                type = doc.getString("type") ?: "outgoing",  // ← NEW
                                date = doc.getTimestamp("date"),
                                addedBy = doc.getString("addedBy") ?: "",
                                addedByName = doc.getString("addedByName") ?: "",
                                notes = doc.getString("notes") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()

                    // ← UPDATED: Calculate summary with incoming/outgoing
                    val now = System.currentTimeMillis()
                    val weekAgo = now - (7 * 24 * 60 * 60 * 1000)
                    val monthAgo = now - (30 * 24 * 60 * 60 * 1000)

                    val thisWeekExpenses = expenses.filter {
                        (it.date?.toDate()?.time ?: 0) >= weekAgo
                    }
                    val thisWeek = thisWeekExpenses.sumOf {
                        if (it.type == "incoming") it.amount else -it.amount
                    }

                    val thisMonthExpenses = expenses.filter {
                        (it.date?.toDate()?.time ?: 0) >= monthAgo
                    }
                    val thisMonth = thisMonthExpenses.sumOf {
                        if (it.type == "incoming") it.amount else -it.amount
                    }

                    val categoryTotals = expenses
                        .groupBy { it.category }
                        .mapValues { (_, items) -> items.sumOf { if (it.type == "incoming") it.amount else -it.amount } }
                        .toList()
                        .sortedByDescending { kotlin.math.abs(it.second) }
                        .take(3)
                        .map { it.first }

                    summary = ExpenseSummary(
                        thisWeek = thisWeek,
                        thisMonth = thisMonth,
                        topCategories = categoryTotals
                    )

                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    // ← NEW: Category Management Dialog
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Manage Categories") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Add new category
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        placeholder = { Text("New category name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8E8E0),
                            unfocusedContainerColor = Color(0xFFE8E8E0),
                            focusedIndicatorColor = Color(0xFF7DDDD3),
                            unfocusedIndicatorColor = Color(0xFFB8B8A8),
                            cursorColor = Color(0xFF7DDDD3)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (newCategoryName.isNotBlank()) {
                                        db.collection("families")
                                            .document(UserSession.familyId)
                                            .collection("categories")
                                            .add(hashMapOf(
                                                "name" to newCategoryName,
                                                "createdAt" to Timestamp.now()
                                            ))
                                            .addOnSuccessListener {
                                                newCategoryName = ""
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Category added",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_input_add),
                                    contentDescription = "Add",
                                    tint = Color(0xFF7DDDD3)
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Categories list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE8E8E0)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = category.name,
                                        fontSize = 16.sp,
                                        color = Color(0xFF2D3748)
                                    )
                                    IconButton(
                                        onClick = {
                                            db.collection("families")
                                                .document(UserSession.familyId)
                                                .collection("categories")
                                                .document(category.id)
                                                .delete()
                                                .addOnSuccessListener {
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "Category deleted",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(id = android.R.drawable.ic_menu_delete),
                                            contentDescription = "Delete",
                                            tint = Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCategoryDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    )
                ) {
                    Text("Done")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF7DDDD3))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with back button and settings icon
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onBackClick,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF6A11CB)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = Color(0xFF6A11CB)
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_revert),
                                contentDescription = "Back",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Back",
                                modifier = Modifier.padding(start = 8.dp),
                                color = Color(0xFF6A11CB)
                            )
                        }

                        // ← NEW: Settings icon for category management
                        IconButton(
                            onClick = { showCategoryDialog = true }
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_preferences),
                                contentDescription = "Category Settings",
                                tint = Color(0xFF6A11CB),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Title
                item {
                    Text(
                        text = "Expense Overview",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B6B6B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Summary Cards (updated with real-time data)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExpenseSummaryCard(
                            title = "This Week",
                            amount = summary.thisWeek,
                            icon = android.R.drawable.ic_menu_recent_history,
                            modifier = Modifier.weight(1f)
                        )

                        ExpenseSummaryCard(
                            title = "This Month",
                            amount = summary.thisMonth,
                            icon = android.R.drawable.ic_menu_month,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Expense List (updated to show name, not category)
                if (expenses.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8E8E0)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_info_details),
                                    contentDescription = "No expenses",
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No expenses added yet",
                                    fontSize = 18.sp,
                                    color = Color(0xFF9CA3AF),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(expenses) { expense ->
                        ExpenseItemCardUpdated(
                            expense = expense,
                            onEditClick = { onEditExpenseClick(expense.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Reports and Analytics Buttons
                item {
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onReportsClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7DDDD3)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_report_image),
                                contentDescription = "Reports",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reports",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = onAnalyticsClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6A11CB)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_sort_by_size),
                                contentDescription = "Analytics",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Analytics",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }

        // Floating Action Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            androidx.compose.material3.FloatingActionButton(
                onClick = onAddExpenseClick,
                containerColor = Color(0xFF7DDDD3),
                contentColor = Color.White,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_input_add),
                    contentDescription = "Add Expense",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// ← UPDATED: Show expense NAME, not category, no icons
@Composable
fun ExpenseItemCardUpdated(
    expense: ExpenseItem,
    onEditClick: () -> Unit = {}
) {
    val dateFormat = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
    val dateStr = expense.date?.toDate()?.let { dateFormat.format(it) } ?: ""

    val amountColor = if (expense.type == "incoming") Color(0xFF10B981) else Color(0xFFEF4444)
    val amountPrefix = if (expense.type == "incoming") "+" else "-"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8E8E0)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ← REMOVED: Icon/Image section

            // Name and Date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.name,  // ← CHANGED: Show name, not category
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2D3748)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateStr,
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF)
                )
                if (expense.category.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = expense.category,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            // Amount in Indian Rupees
            Text(
                text = "$amountPrefix₹${String.format("%.2f", kotlin.math.abs(expense.amount))}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Edit button
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_edit),
                    contentDescription = "Edit",
                    tint = Color(0xFF7DDDD3),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ExpenseSummaryCard(
    title: String,
    amount: Double,
    icon: Int,
    modifier: Modifier = Modifier
) {
    val amountColor = if (amount >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
    val amountPrefix = if (amount >= 0) "+" else ""

    Card(
        modifier = modifier.height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8E8E0)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color(0xFF6B6B6B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$amountPrefix₹${String.format("%.2f", kotlin.math.abs(amount))}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

// ============== ADD EXPENSE SCREEN ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onBackClick: () -> Unit,
    onExpenseSaved: () -> Unit
) {
    var name by remember { mutableStateOf("") }  // ← CHANGED: name instead of duplicate amount
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("outgoing") }  // ← NEW: incoming/outgoing
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showTypeMenu by remember { mutableStateOf(false) }  // ← NEW
    var showDatePicker by remember { mutableStateOf(false) }  // ← NEW
    var isSaving by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var categories by remember { mutableStateOf<List<ExpenseCategory>>(emptyList()) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Load categories
    LaunchedEffect(Unit) {
        if (UserSession.familyId.isNotEmpty()) {
            db.collection("families")
                .document(UserSession.familyId)
                .collection("categories")
                .addSnapshotListener { snapshot, _ ->
                    categories = snapshot?.documents?.mapNotNull { doc ->
                        ExpenseCategory(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            createdAt = doc.getTimestamp("createdAt")
                        )
                    } ?: emptyList()
                }
        }
    }

    // Format date
    val dateFormat = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
    val formattedDate = dateFormat.format(java.util.Date(selectedDate))

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onExpenseSaved()
            },
            title = { Text("Success!") },
            text = { Text("Expense has been added successfully.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onExpenseSaved()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    )
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Category selection dialog
    if (showCategoryMenu) {
        AlertDialog(
            onDismissRequest = { showCategoryMenu = false },
            title = { Text("Select Category") },
            text = {
                LazyColumn {
                    items(categories) { cat ->
                        Card(
                            onClick = {
                                category = cat.name
                                showCategoryMenu = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (category == cat.name) Color(0xFF7DDDD3) else Color(0xFFE8E8E0)
                            )
                        ) {
                            Text(
                                text = cat.name,
                                modifier = Modifier.padding(16.dp),
                                color = if (category == cat.name) Color.White else Color(0xFF2D3748)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCategoryMenu = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    )
                ) {
                    Text("Close")
                }
            }
        )
    }

    // ← NEW: Type selection dialog (Incoming/Outgoing)
    if (showTypeMenu) {
        AlertDialog(
            onDismissRequest = { showTypeMenu = false },
            title = { Text("Select Type") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Outgoing option (red)
                    Card(
                        onClick = {
                            type = "outgoing"
                            showTypeMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (type == "outgoing") Color(0xFFEF4444) else Color(0xFFFFE8E8)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.arrow_down_float),
                                contentDescription = "Outgoing",
                                tint = if (type == "outgoing") Color.White else Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Outgoing",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (type == "outgoing") Color.White else Color(0xFFEF4444)
                            )
                        }
                    }

                    // Incoming option (green)
                    Card(
                        onClick = {
                            type = "incoming"
                            showTypeMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (type == "incoming") Color(0xFF10B981) else Color(0xFFD1FAE5)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.arrow_up_float),
                                contentDescription = "Incoming",
                                tint = if (type == "incoming") Color.White else Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Incoming",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (type == "incoming") Color.White else Color(0xFF10B981)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTypeMenu = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    )
                ) {
                    Text("Close")
                }
            }
        )
    }

    // ← NEW: Calendar date picker dialog (past dates only)
    if (showDatePicker) {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = selectedDate

        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Select Date") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Simple date picker (in production, use MaterialDatePicker)
                    Text(
                        text = "Selected: $formattedDate",
                        fontSize = 16.sp,
                        color = Color(0xFF2D3748),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Day selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                calendar.add(java.util.Calendar.DAY_OF_MONTH, -1)
                                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                                    selectedDate = calendar.timeInMillis
                                }
                            }
                        ) {
                            Text("Previous Day")
                        }
                        OutlinedButton(
                            onClick = {
                                calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                                    selectedDate = calendar.timeInMillis
                                }
                            },
                            enabled = calendar.timeInMillis < System.currentTimeMillis()
                        ) {
                            Text("Next Day")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reset to today
                    Button(
                        onClick = {
                            selectedDate = System.currentTimeMillis()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7DDDD3)
                        )
                    ) {
                        Text("Today")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    )
                ) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))
            .padding(24.dp)
    ) {
        // Back button
        OutlinedButton(
            onClick = onBackClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF6A11CB)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color(0xFF6A11CB)
            )
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_revert),
                contentDescription = "Back",
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Back",
                modifier = Modifier.padding(start = 8.dp),
                color = Color(0xFF6A11CB)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = "Add Expense",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B6B6B),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ← CHANGED: Name field (not duplicate amount)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Expense Name", color = Color(0xFF9CA3AF)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8E8E0),
                unfocusedContainerColor = Color(0xFFE8E8E0),
                focusedIndicatorColor = Color(0xFF7DDDD3),
                unfocusedIndicatorColor = Color(0xFFB8B8A8),
                cursorColor = Color(0xFF7DDDD3)
            ),
            shape = RoundedCornerShape(28.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Category selector
        OutlinedTextField(
            value = category,
            onValueChange = { },
            placeholder = { Text("Select Category", color = Color(0xFF9CA3AF)) },
            label = { Text("Category") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCategoryMenu = true },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8E8E0),
                unfocusedContainerColor = Color(0xFFE8E8E0),
                focusedIndicatorColor = Color(0xFF7DDDD3),
                unfocusedIndicatorColor = Color(0xFFB8B8A8),
                cursorColor = Color(0xFF7DDDD3),
                disabledContainerColor = Color(0xFFE8E8E0),
                disabledIndicatorColor = Color(0xFFB8B8A8),
                disabledTextColor = Color(0xFF2D3748)
            ),
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            enabled = false,
            trailingIcon = {
                Icon(
                    painter = painterResource(id = android.R.drawable.arrow_down_float),
                    contentDescription = "Select",
                    tint = Color(0xFF7DDDD3)
                )
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Amount field
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
            placeholder = { Text("0.00", color = Color(0xFF9CA3AF)) },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8E8E0),
                unfocusedContainerColor = Color(0xFFE8E8E0),
                focusedIndicatorColor = Color(0xFF7DDDD3),
                unfocusedIndicatorColor = Color(0xFFB8B8A8),
                cursorColor = Color(0xFF7DDDD3)
            ),
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = {
                Text(
                    text = "₹",
                    fontSize = 20.sp,
                    color = Color(0xFF2D3748),
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ← NEW: Type selector (Incoming/Outgoing) - replaces duplicate amount field
        OutlinedTextField(
            value = if (type == "incoming") "Incoming" else "Outgoing",
            onValueChange = { },
            label = { Text("Type") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTypeMenu = true },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8E8E0),
                unfocusedContainerColor = Color(0xFFE8E8E0),
                focusedIndicatorColor = if (type == "incoming") Color(0xFF10B981) else Color(0xFFEF4444),
                unfocusedIndicatorColor = if (type == "incoming") Color(0xFF10B981) else Color(0xFFEF4444),
                disabledContainerColor = Color(0xFFE8E8E0),
                disabledIndicatorColor = if (type == "incoming") Color(0xFF10B981) else Color(0xFFEF4444),
                disabledTextColor = if (type == "incoming") Color(0xFF10B981) else Color(0xFFEF4444)
            ),
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            enabled = false,
            leadingIcon = {
                Icon(
                    painter = painterResource(
                        id = if (type == "incoming")
                            android.R.drawable.arrow_up_float
                        else
                            android.R.drawable.arrow_down_float
                    ),
                    contentDescription = type,
                    tint = if (type == "incoming") Color(0xFF10B981) else Color(0xFFEF4444)
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(id = android.R.drawable.arrow_down_float),
                    contentDescription = "Select",
                    tint = if (type == "incoming") Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Notes field
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            placeholder = { Text("Notes (optional)", color = Color(0xFF9CA3AF)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8E8E0),
                unfocusedContainerColor = Color(0xFFE8E8E0),
                focusedIndicatorColor = Color(0xFF7DDDD3),
                unfocusedIndicatorColor = Color(0xFFB8B8A8),
                cursorColor = Color(0xFF7DDDD3)
            ),
            shape = RoundedCornerShape(28.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ← NEW: Date field with calendar dialog
        OutlinedTextField(
            value = formattedDate,
            onValueChange = { },
            label = { Text("Date") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8E8E0),
                unfocusedContainerColor = Color(0xFFE8E8E0),
                focusedIndicatorColor = Color(0xFF7DDDD3),
                unfocusedIndicatorColor = Color(0xFFB8B8A8),
                cursorColor = Color(0xFF7DDDD3),
                disabledContainerColor = Color(0xFFE8E8E0),
                disabledIndicatorColor = Color(0xFFB8B8A8),
                disabledTextColor = Color(0xFF2D3748)
            ),
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            enabled = false,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_today),
                    contentDescription = "Date",
                    tint = Color(0xFF7DDDD3),
                    modifier = Modifier.size(24.dp)
                )
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Save button
        Button(
            onClick = {
                when {
                    name.isBlank() -> {
                        android.widget.Toast.makeText(context, "Please enter expense name", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    category.isBlank() -> {
                        android.widget.Toast.makeText(context, "Please select category", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    amount.isBlank() -> {
                        android.widget.Toast.makeText(context, "Please enter amount", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    amount.toDoubleOrNull() == null -> {
                        android.widget.Toast.makeText(context, "Please enter valid amount", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        isSaving = true

                        val expenseData = hashMapOf(
                            "name" to name,
                            "amount" to amount.toDouble(),
                            "category" to category,
                            "type" to type,  // ← NEW
                            "notes" to notes,
                            "date" to Timestamp(java.util.Date(selectedDate)),
                            "addedBy" to UserSession.userId,
                            "addedByName" to UserSession.userName
                        )

                        db.collection("families")
                            .document(UserSession.familyId)
                            .collection("expenses")
                            .add(expenseData)
                            .addOnSuccessListener {
                                isSaving = false
                                showSuccessDialog = true
                            }
                            .addOnFailureListener { e ->
                                isSaving = false
                                android.widget.Toast.makeText(
                                    context,
                                    "Failed to save: ${e.message}",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7DDDD3),
                disabledContainerColor = Color(0xFFB8B8A8)
            ),
            shape = RoundedCornerShape(32.dp),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Saving...",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            } else {
                Text(
                    text = "Save",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ============== EDIT EXPENSE SCREEN ==============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseScreen(
    expenseId: String,
    onBackClick: () -> Unit,
    onExpenseUpdated: () -> Unit,
    onExpenseDeleted: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("outgoing") }  // ← NEW: incoming/outgoing
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showTypeMenu by remember { mutableStateOf(false) }  // ← NEW
    var showDatePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var categories by remember { mutableStateOf<List<ExpenseCategory>>(emptyList()) }  // ← NEW: Dynamic categories

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // ← NEW: Load categories from database
    LaunchedEffect(Unit) {
        if (UserSession.familyId.isNotEmpty()) {
            db.collection("families")
                .document(UserSession.familyId)
                .collection("categories")
                .addSnapshotListener { snapshot, _ ->
                    categories = snapshot?.documents?.mapNotNull { doc ->
                        ExpenseCategory(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            createdAt = doc.getTimestamp("createdAt")
                        )
                    } ?: emptyList()
                }
        }
    }

    // Format date
    val dateFormat = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
    val formattedDate = dateFormat.format(java.util.Date(selectedDate))

    // Load existing expense data
    LaunchedEffect(expenseId) {
        if (expenseId.isNotEmpty()) {
            db.collection("families")
                .document(UserSession.familyId)
                .collection("expenses")
                .document(expenseId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        name = document.getString("name") ?: ""
                        amount = document.getDouble("amount")?.toString() ?: ""
                        category = document.getString("category") ?: ""
                        type = document.getString("type") ?: "outgoing"  // ← NEW: Load type
                        notes = document.getString("notes") ?: ""
                        selectedDate = document.getTimestamp("date")?.toDate()?.time ?: System.currentTimeMillis()
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    android.widget.Toast.makeText(
                        context,
                        "Failed to load expense: ${e.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    isLoading = false
                }
        }
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onExpenseUpdated()
            },
            title = { Text("Success!") },
            text = { Text("Expense has been updated successfully.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onExpenseUpdated()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Delete Expense",
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete this expense? This action cannot be undone.",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        db.collection("families")
                            .document(UserSession.familyId)
                            .collection("expenses")
                            .document(expenseId)
                            .delete()
                            .addOnSuccessListener {
                                showDeleteDialog = false
                                onExpenseDeleted()
                            }
                            .addOnFailureListener { e ->
                                android.widget.Toast.makeText(
                                    context,
                                    "Failed to delete: ${e.message}",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                showDeleteDialog = false
                            }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6A11CB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Category selection dialog
    if (showCategoryMenu) {
        AlertDialog(
            onDismissRequest = { showCategoryMenu = false },
            title = { Text("Select Category") },
            text = {
                LazyColumn(
                    modifier = Modifier.height(400.dp)
                ) {
                    items(categories) { cat ->
                        Card(
                            onClick = {
                                category = cat.name
                                showCategoryMenu = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (category == cat.name) Color(0xFF7DDDD3) else Color(0xFFE8E8E0)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = cat.name,
                                modifier = Modifier.padding(16.dp),
                                fontSize = 16.sp,
                                color = if (category == cat.name) Color.White else Color(0xFF2D3748)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCategoryMenu = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ← NEW: Type selection dialog (Incoming/Outgoing)
    if (showTypeMenu) {
        AlertDialog(
            onDismissRequest = { showTypeMenu = false },
            title = { Text("Select Type") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Outgoing option (red)
                    Card(
                        onClick = {
                            type = "outgoing"
                            showTypeMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (type == "outgoing") Color(0xFFEF4444) else Color(0xFFFFE8E8)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.arrow_down_float),
                                contentDescription = "Outgoing",
                                tint = if (type == "outgoing") Color.White else Color(0xFFEF4444),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Outgoing",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (type == "outgoing") Color.White else Color(0xFFEF4444)
                                )
                                Text(
                                    text = "Money spent",
                                    fontSize = 14.sp,
                                    color = if (type == "outgoing") Color.White.copy(alpha = 0.8f) else Color(0xFFEF4444).copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Incoming option (green)
                    Card(
                        onClick = {
                            type = "incoming"
                            showTypeMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (type == "incoming") Color(0xFF10B981) else Color(0xFFD1FAE5)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.arrow_up_float),
                                contentDescription = "Incoming",
                                tint = if (type == "incoming") Color.White else Color(0xFF10B981),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Incoming",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (type == "incoming") Color.White else Color(0xFF10B981)
                                )
                                Text(
                                    text = "Money received",
                                    fontSize = 14.sp,
                                    color = if (type == "incoming") Color.White.copy(alpha = 0.8f) else Color(0xFF10B981).copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTypeMenu = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ← NEW: Calendar date picker dialog (past dates only)
    if (showDatePicker) {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = selectedDate

        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Select Date") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Selected: $formattedDate",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Day selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                calendar.add(java.util.Calendar.DAY_OF_MONTH, -1)
                                selectedDate = calendar.timeInMillis
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF6A11CB)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6A11CB)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_media_previous),
                                contentDescription = "Previous",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Previous")
                        }

                        OutlinedButton(
                            onClick = {
                                calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                                    selectedDate = calendar.timeInMillis
                                } else {
                                    calendar.add(java.util.Calendar.DAY_OF_MONTH, -1)
                                    android.widget.Toast.makeText(
                                        context,
                                        "Cannot select future dates",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            enabled = calendar.timeInMillis < System.currentTimeMillis(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF6A11CB)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6A11CB)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_media_next),
                                contentDescription = "Next",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reset to today
                    Button(
                        onClick = {
                            selectedDate = System.currentTimeMillis()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7DDDD3)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_today),
                            contentDescription = "Today",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Today", fontSize = 16.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF7DDDD3))
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F0))
                .padding(24.dp)
        ) {
            // Back button
            OutlinedButton(
                onClick = onBackClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF6A11CB)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF6A11CB)
                )
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_revert),
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Back",
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color(0xFF6A11CB)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Edit Expense",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B6B6B),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Expense Name", color = Color(0xFF9CA3AF)) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE8E8E0),
                    unfocusedContainerColor = Color(0xFFE8E8E0),
                    focusedIndicatorColor = Color(0xFF7DDDD3),
                    unfocusedIndicatorColor = Color(0xFFB8B8A8),
                    cursorColor = Color(0xFF7DDDD3)
                ),
                shape = RoundedCornerShape(28.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Category selector
            OutlinedTextField(
                value = category,
                onValueChange = { },
                placeholder = { Text("Select Category", color = Color(0xFF9CA3AF)) },
                label = { Text("Category") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategoryMenu = true },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE8E8E0),
                    unfocusedContainerColor = Color(0xFFE8E8E0),
                    focusedIndicatorColor = Color(0xFF7DDDD3),
                    unfocusedIndicatorColor = Color(0xFFB8B8A8),
                    cursorColor = Color(0xFF7DDDD3),
                    disabledContainerColor = Color(0xFFE8E8E0),
                    disabledIndicatorColor = Color(0xFFB8B8A8),
                    disabledTextColor = Color(0xFF2D3748)
                ),
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                enabled = false,
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = android.R.drawable.arrow_down_float),
                        contentDescription = "Select",
                        tint = Color(0xFF7DDDD3)
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Amount field with Indian Rupees
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                placeholder = { Text("0.00", color = Color(0xFF9CA3AF)) },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE8E8E0),
                    unfocusedContainerColor = Color(0xFFE8E8E0),
                    focusedIndicatorColor = Color(0xFF7DDDD3),
                    unfocusedIndicatorColor = Color(0xFFB8B8A8),
                    cursorColor = Color(0xFF7DDDD3)
                ),
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = {
                    Text(
                        text = "₹",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748),
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ← NEW: Type selector (Incoming/Outgoing)
            OutlinedTextField(
                value = if (type == "incoming") "Incoming" else "Outgoing",
                onValueChange = { },
                label = { Text("Type") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTypeMenu = true },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE8E8E0),
                    unfocusedContainerColor = Color(0xFFE8E8E0),
                    focusedIndicatorColor = if (type == "incoming") Color(0xFF10B981) else Color(0xFFEF4444),
                    unfocusedIndicatorColor = if (type == "incoming") Color(0xFF10B981) else Color(0xFFEF4444),
                    disabledContainerColor = Color(0xFFE8E8E0),
                    disabledIndicatorColor = if (type == "incoming") Color(0xFF10B981) else Color(0xFFEF4444),
                    disabledTextColor = if (type == "incoming") Color(0xFF10B981) else Color(0xFFEF4444)
                ),
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                enabled = false,
                leadingIcon = {
                    Icon(
                        painter = painterResource(
                            id = if (type == "incoming")
                                android.R.drawable.arrow_up_float
                            else
                                android.R.drawable.arrow_down_float
                        ),
                        contentDescription = type,
                        tint = if (type == "incoming") Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = android.R.drawable.arrow_down_float),
                        contentDescription = "Select",
                        tint = if (type == "incoming") Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Notes field
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Notes (optional)", color = Color(0xFF9CA3AF)) },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE8E8E0),
                    unfocusedContainerColor = Color(0xFFE8E8E0),
                    focusedIndicatorColor = Color(0xFF7DDDD3),
                    unfocusedIndicatorColor = Color(0xFFB8B8A8),
                    cursorColor = Color(0xFF7DDDD3)
                ),
                shape = RoundedCornerShape(28.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ← UPDATED: Date field with calendar dialog
            OutlinedTextField(
                value = formattedDate,
                onValueChange = { },
                label = { Text("Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE8E8E0),
                    unfocusedContainerColor = Color(0xFFE8E8E0),
                    focusedIndicatorColor = Color(0xFF7DDDD3),
                    unfocusedIndicatorColor = Color(0xFFB8B8A8),
                    cursorColor = Color(0xFF7DDDD3),
                    disabledContainerColor = Color(0xFFE8E8E0),
                    disabledIndicatorColor = Color(0xFFB8B8A8),
                    disabledTextColor = Color(0xFF2D3748)
                ),
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                enabled = false,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_today),
                        contentDescription = "Date",
                        tint = Color(0xFF7DDDD3),
                        modifier = Modifier.size(24.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Update button
            Button(
                onClick = {
                    when {
                        name.isBlank() -> {
                            android.widget.Toast.makeText(context, "Please enter expense name", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        category.isBlank() -> {
                            android.widget.Toast.makeText(context, "Please select category", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        amount.isBlank() -> {
                            android.widget.Toast.makeText(context, "Please enter amount", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        amount.toDoubleOrNull() == null -> {
                            android.widget.Toast.makeText(context, "Please enter valid amount", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            isSaving = true

                            val expenseData = hashMapOf(
                                "name" to name,
                                "amount" to amount.toDouble(),
                                "category" to category,
                                "type" to type,  // ← NEW: Save type
                                "notes" to notes,
                                "date" to Timestamp(java.util.Date(selectedDate)),
                                "updatedAt" to Timestamp.now(),
                                "updatedBy" to UserSession.userId
                            )

                            db.collection("families")
                                .document(UserSession.familyId)
                                .collection("expenses")
                                .document(expenseId)
                                .update(expenseData as Map<String, Any>)
                                .addOnSuccessListener {
                                    isSaving = false
                                    showSuccessDialog = true
                                }
                                .addOnFailureListener { e ->
                                    isSaving = false
                                    android.widget.Toast.makeText(
                                        context,
                                        "Failed to update: ${e.message}",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7DDDD3),
                    disabledContainerColor = Color(0xFFB8B8A8)
                ),
                shape = RoundedCornerShape(32.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Updating...",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Update",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delete button
            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_delete),
                    contentDescription = "Delete",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Delete Expense",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFEF4444)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ============== REPORTS SCREEN ==============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onBackClick: () -> Unit
) {
    var expenses by remember { mutableStateOf<List<ExpenseItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var reportHistory by remember { mutableStateOf<List<SavedReport>>(emptyList()) }

    // Monthly Report dropdown state
    var showMonthlyDropdown by remember { mutableStateOf(false) }
    var showMonthlyViewDialog by remember { mutableStateOf(false) }

    // Yearly Report dropdown state
    var showYearlyDropdown by remember { mutableStateOf(false) }
    var showYearlyViewDialog by remember { mutableStateOf(false) }

    // View Report state
    var viewingReport by remember { mutableStateOf<String?>(null) } // "monthly" or "yearly"
    var filteredExpenses by remember { mutableStateOf<List<ExpenseItem>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("All Categories") }
    var selectedType by remember { mutableStateOf("All") } // All, Incoming, Outgoing
    var showCategoryFilter by remember { mutableStateOf(false) }
    var showTypeFilter by remember { mutableStateOf(false) }
    var categories by remember { mutableStateOf<List<String>>(listOf("All Categories")) }

    // Report History View Dialog
    var viewingHistoryReport by remember { mutableStateOf<SavedReport?>(null) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Load expenses and categories
    LaunchedEffect(Unit) {
        if (UserSession.familyId.isNotEmpty()) {
            // Load categories
            db.collection("families")
                .document(UserSession.familyId)
                .collection("categories")
                .get()
                .addOnSuccessListener { snapshot ->
                    val loadedCategories = mutableListOf("All Categories")
                    loadedCategories.addAll(
                        snapshot.documents.mapNotNull { doc ->
                            doc.getString("name")
                        }
                    )
                    categories = loadedCategories
                }

            // Load expenses
            db.collection("families")
                .document(UserSession.familyId)
                .collection("expenses")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }

                    expenses = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            ExpenseItem(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                amount = doc.getDouble("amount") ?: 0.0,
                                category = doc.getString("category") ?: "",
                                type = doc.getString("type") ?: "outgoing",
                                date = doc.getTimestamp("date"),
                                addedBy = doc.getString("addedBy") ?: "",
                                addedByName = doc.getString("addedByName") ?: "",
                                notes = doc.getString("notes") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()

                    isLoading = false
                }

            // Load report history
            db.collection("families")
                .document(UserSession.familyId)
                .collection("reportHistory")
                .orderBy("generatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener { snapshot, error ->
                    reportHistory = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            SavedReport(
                                id = doc.id,
                                reportName = doc.getString("reportName") ?: "",
                                reportType = doc.getString("reportType") ?: "",
                                generatedAt = doc.getTimestamp("generatedAt"),
                                fileUrl = doc.getString("fileUrl") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                }
        } else {
            isLoading = false
        }
    }

    // Filter expenses based on report type and filters
    LaunchedEffect(viewingReport, selectedCategory, selectedType, expenses) {
        if (viewingReport != null) {
            val now = System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance()

            val baseFiltered = if (viewingReport == "monthly") {
                // Current month
                calendar.timeInMillis = now
                val currentMonth = calendar.get(java.util.Calendar.MONTH)
                val currentYear = calendar.get(java.util.Calendar.YEAR)

                expenses.filter { expense ->
                    expense.date?.let { timestamp ->
                        calendar.timeInMillis = timestamp.toDate().time
                        calendar.get(java.util.Calendar.MONTH) == currentMonth &&
                                calendar.get(java.util.Calendar.YEAR) == currentYear
                    } ?: false
                }
            } else {
                // Current year
                calendar.timeInMillis = now
                val currentYear = calendar.get(java.util.Calendar.YEAR)

                expenses.filter { expense ->
                    expense.date?.let { timestamp ->
                        calendar.timeInMillis = timestamp.toDate().time
                        calendar.get(java.util.Calendar.YEAR) == currentYear
                    } ?: false
                }
            }

            // Apply category filter
            val categoryFiltered = if (selectedCategory == "All Categories") {
                baseFiltered
            } else {
                baseFiltered.filter { it.category == selectedCategory }
            }

            // Apply type filter
            filteredExpenses = when (selectedType) {
                "Incoming" -> categoryFiltered.filter { it.type == "incoming" }
                "Outgoing" -> categoryFiltered.filter { it.type == "outgoing" }
                else -> categoryFiltered
            }
        }
    }

    // Category Filter Dialog
    if (showCategoryFilter) {
        AlertDialog(
            onDismissRequest = { showCategoryFilter = false },
            title = { Text("Filter by Category") },
            text = {
                LazyColumn {
                    items(categories) { category ->
                        Card(
                            onClick = {
                                selectedCategory = category
                                showCategoryFilter = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedCategory == category)
                                    Color(0xFF7DDDD3)
                                else
                                    Color(0xFFE8E8E0)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = category,
                                modifier = Modifier.padding(16.dp),
                                fontSize = 16.sp,
                                color = if (selectedCategory == category) Color.White else Color(0xFF2D3748)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCategoryFilter = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Type Filter Dialog
    if (showTypeFilter) {
        AlertDialog(
            onDismissRequest = { showTypeFilter = false },
            title = { Text("Filter by Type") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Incoming", "Outgoing").forEach { type ->
                        Card(
                            onClick = {
                                selectedType = type
                                showTypeFilter = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedType == type)
                                    Color(0xFF7DDDD3)
                                else
                                    Color(0xFFE8E8E0)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = when(type) {
                                            "Incoming" -> android.R.drawable.arrow_up_float
                                            "Outgoing" -> android.R.drawable.arrow_down_float
                                            else -> android.R.drawable.ic_menu_view
                                        }
                                    ),
                                    contentDescription = type,
                                    tint = if (selectedType == type) Color.White else Color(0xFF2D3748),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = type,
                                    fontSize = 16.sp,
                                    color = if (selectedType == type) Color.White else Color(0xFF2D3748)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTypeFilter = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // View Report Dialog (for history items)
    if (viewingHistoryReport != null) {
        ViewReportDialog(
            report = viewingHistoryReport!!,
            expenses = expenses,
            onDismiss = { viewingHistoryReport = null },
            context = context
        )
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF7DDDD3))
        }
    } else if (viewingReport != null) {
        // View Report Screen
        ViewReportScreen(
            reportType = viewingReport!!,
            filteredExpenses = filteredExpenses,
            selectedCategory = selectedCategory,
            selectedType = selectedType,
            onCategoryFilterClick = { showCategoryFilter = true },
            onTypeFilterClick = { showTypeFilter = true },
            onBackClick = { viewingReport = null },
            onDownloadClick = {
                generateAndDownloadPDFReport(
                    context = context,
                    expenses = filteredExpenses,
                    reportType = viewingReport!!,
                    selectedCategory = selectedCategory,
                    selectedType = selectedType
                )
            },
            context = context
        )
    } else {
        // Main Reports Screen
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F0))
                .padding(16.dp)
        ) {
            // Back button
            item {
                OutlinedButton(
                    onClick = onBackClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = Color(0xFF6A11CB)
                    )
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_revert),
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Back",
                        modifier = Modifier.padding(start = 8.dp),
                        color = Color(0xFF6A11CB)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Title
            item {
                Text(
                    text = "Reports",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B6B6B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Monthly Report Button with Dropdown
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { showMonthlyDropdown = !showMonthlyDropdown },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7DDDD3)
                        ),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Text(
                            text = "Monthly Report",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(
                                id = if (showMonthlyDropdown)
                                    android.R.drawable.arrow_up_float
                                else
                                    android.R.drawable.arrow_down_float
                            ),
                            contentDescription = "Dropdown",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Dropdown Menu
                    androidx.compose.material3.DropdownMenu(
                        expanded = showMonthlyDropdown,
                        onDismissRequest = { showMonthlyDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(Color(0xFFE8E8E0), RoundedCornerShape(16.dp))
                    ) {
                        // View Report
                        androidx.compose.material3.DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_view),
                                        contentDescription = "View",
                                        tint = Color(0xFF2D3748),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "View Report",
                                        fontSize = 16.sp,
                                        color = Color(0xFF2D3748)
                                    )
                                }
                            },
                            onClick = {
                                showMonthlyDropdown = false
                                viewingReport = "monthly"
                                selectedCategory = "All Categories"
                                selectedType = "All"
                            }
                        )

                        // Download Report
                        androidx.compose.material3.DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_download),
                                        contentDescription = "Download",
                                        tint = Color(0xFF2D3748),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Download Report",
                                        fontSize = 16.sp,
                                        color = Color(0xFF2D3748)
                                    )
                                }
                            },
                            onClick = {
                                showMonthlyDropdown = false
                                generateAndDownloadPDFReport(
                                    context = context,
                                    expenses = expenses,
                                    reportType = "monthly",
                                    selectedCategory = "All Categories",
                                    selectedType = "All"
                                )
                            }
                        )

                        // Share Report
                        androidx.compose.material3.DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_share),
                                        contentDescription = "Share",
                                        tint = Color(0xFF2D3748),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Share Report",
                                        fontSize = 16.sp,
                                        color = Color(0xFF2D3748)
                                    )
                                }
                            },
                            onClick = {
                                showMonthlyDropdown = false
                                shareReport(context, expenses, "monthly", "All Categories", "All")
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Yearly Report Button with Dropdown
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { showYearlyDropdown = !showYearlyDropdown },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7DDDD3)
                        ),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Text(
                            text = "Yearly Report",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(
                                id = if (showYearlyDropdown)
                                    android.R.drawable.arrow_up_float
                                else
                                    android.R.drawable.arrow_down_float
                            ),
                            contentDescription = "Dropdown",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Dropdown Menu
                    androidx.compose.material3.DropdownMenu(
                        expanded = showYearlyDropdown,
                        onDismissRequest = { showYearlyDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(Color(0xFFE8E8E0), RoundedCornerShape(16.dp))
                    ) {
                        // View Report
                        androidx.compose.material3.DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_view),
                                        contentDescription = "View",
                                        tint = Color(0xFF2D3748),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "View Report",
                                        fontSize = 16.sp,
                                        color = Color(0xFF2D3748)
                                    )
                                }
                            },
                            onClick = {
                                showYearlyDropdown = false
                                viewingReport = "yearly"
                                selectedCategory = "All Categories"
                                selectedType = "All"
                            }
                        )

                        // Download Report
                        androidx.compose.material3.DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_download),
                                        contentDescription = "Download",
                                        tint = Color(0xFF2D3748),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Download Report",
                                        fontSize = 16.sp,
                                        color = Color(0xFF2D3748)
                                    )
                                }
                            },
                            onClick = {
                                showYearlyDropdown = false
                                generateAndDownloadPDFReport(
                                    context = context,
                                    expenses = expenses,
                                    reportType = "yearly",
                                    selectedCategory = "All Categories",
                                    selectedType = "All"
                                )
                            }
                        )

                        // Share Report
                        androidx.compose.material3.DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_share),
                                        contentDescription = "Share",
                                        tint = Color(0xFF2D3748),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Share Report",
                                        fontSize = 16.sp,
                                        color = Color(0xFF2D3748)
                                    )
                                }
                            },
                            onClick = {
                                showYearlyDropdown = false
                                shareReport(context, expenses, "yearly", "All Categories", "All")
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Report History Section
            item {
                Text(
                    text = "Report History",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Report History List
            if (reportHistory.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8E8E0)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_report_image),
                                contentDescription = "No reports",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No reports generated yet",
                                fontSize = 18.sp,
                                color = Color(0xFF9CA3AF),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(reportHistory) { report ->
                    ReportHistoryCardWithIcons(
                        report = report,
                        context = context,
                        expenses = expenses,
                        onViewClick = { viewingHistoryReport = report }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ============== VIEW REPORT SCREEN ==============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewReportScreen(
    reportType: String,
    filteredExpenses: List<ExpenseItem>,
    selectedCategory: String,
    selectedType: String,
    onCategoryFilterClick: () -> Unit,
    onTypeFilterClick: () -> Unit,
    onBackClick: () -> Unit,
    onDownloadClick: () -> Unit,
    context: android.content.Context
) {
    val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))
            .padding(16.dp)
    ) {
        // Back button
        OutlinedButton(
            onClick = onBackClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF6A11CB)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color(0xFF6A11CB)
            )
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_revert),
                contentDescription = "Back",
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Back",
                modifier = Modifier.padding(start = 8.dp),
                color = Color(0xFF6A11CB)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "${if (reportType == "monthly") "Monthly" else "Yearly"} Report",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B6B6B),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category Filter
            Card(
                onClick = onCategoryFilterClick,
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8E8E0)
                ),
                shape = RoundedCornerShape(32.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF7DDDD3))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedCategory,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D3748),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        painter = painterResource(id = android.R.drawable.arrow_down_float),
                        contentDescription = "Filter",
                        tint = Color(0xFF7DDDD3),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Type Filter
            Card(
                onClick = onTypeFilterClick,
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8E8E0)
                ),
                shape = RoundedCornerShape(32.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF7DDDD3))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedType,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D3748),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        painter = painterResource(id = android.R.drawable.arrow_down_float),
                        contentDescription = "Filter",
                        tint = Color(0xFF7DDDD3),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8E8E0)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                val totalIncoming = filteredExpenses.filter { it.type == "incoming" }.sumOf { it.amount }
                val totalOutgoing = filteredExpenses.filter { it.type == "outgoing" }.sumOf { it.amount }
                val netBalance = totalIncoming - totalOutgoing

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryItem("Total Incoming", totalIncoming, Color(0xFF10B981))
                    SummaryItem("Total Outgoing", totalOutgoing, Color(0xFFEF4444))
                }
                Spacer(modifier = Modifier.height(12.dp))
                SummaryItem(
                    "Net Balance",
                    netBalance,
                    if (netBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                    isBold = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Expenses Table Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF7DDDD3)
            ),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Date", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                Text("Name", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1.5f))
                Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                Text("Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(0.8f))
                Text("Amount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
        }

        // Expenses Table Content
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(filteredExpenses) { expense ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(0.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFE8E8E0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = expense.date?.toDate()?.let { dateFormat.format(it) } ?: "",
                            fontSize = 11.sp,
                            color = Color(0xFF6B6B6B),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = expense.name,
                            fontSize = 11.sp,
                            color = Color(0xFF2D3748),
                            modifier = Modifier.weight(1.5f)
                        )
                        Text(
                            text = expense.category,
                            fontSize = 11.sp,
                            color = Color(0xFF6B6B6B),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            painter = painterResource(
                                id = if (expense.type == "incoming")
                                    android.R.drawable.arrow_up_float
                                else
                                    android.R.drawable.arrow_down_float
                            ),
                            contentDescription = expense.type,
                            tint = if (expense.type == "incoming") Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier
                                .weight(0.8f)
                                .size(16.dp)
                        )
                        Text(
                            text = "₹${String.format("%.2f", expense.amount)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (expense.type == "incoming") Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Download Button
        Button(
            onClick = onDownloadClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7DDDD3)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_download),
                contentDescription = "Download",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Download Report",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SummaryItem(label: String, amount: Double, color: Color, isBold: Boolean = false) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF6B6B6B)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "₹${String.format("%.2f", kotlin.math.abs(amount))}",
            fontSize = if (isBold) 20.sp else 18.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
    }
}

// ============== REPORT HISTORY CARD WITH ICONS ==============

@Composable
fun ReportHistoryCardWithIcons(
    report: SavedReport,
    context: android.content.Context,
    expenses: List<ExpenseItem>,
    onViewClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8E8E0)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_report_image),
                    contentDescription = "Report",
                    tint = Color(0xFF2D3748),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = report.reportName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2D3748)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Three Icon Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // View Icon
                IconButton(
                    onClick = onViewClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7DDDD3))
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_view),
                        contentDescription = "View",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Download Icon
                IconButton(
                    onClick = {
                        generateAndDownloadPDFReport(
                            context = context,
                            expenses = expenses,
                            reportType = report.reportType,
                            selectedCategory = "All Categories",
                            selectedType = "All"
                        )
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7DDDD3))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_download),
                        contentDescription = "Download",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Share Icon
                IconButton(
                    onClick = {
                        shareReport(context, expenses, report.reportType, "All Categories", "All")
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7DDDD3))
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_share),
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// ============== VIEW REPORT DIALOG (for history items) ==============

@Composable
fun ViewReportDialog(
    report: SavedReport,
    expenses: List<ExpenseItem>,
    onDismiss: () -> Unit,
    context: android.content.Context
) {
    // Filter expenses based on report type
    val filteredExpenses = remember(report, expenses) {
        val calendar = java.util.Calendar.getInstance()
        val reportMonth = report.reportName.split(" ").getOrNull(0) // e.g., "November"
        val reportYear = report.reportName.split(" ").getOrNull(1)?.toIntOrNull() ?: calendar.get(java.util.Calendar.YEAR)

        if (report.reportType == "monthly" && reportMonth != null) {
            val monthMap = mapOf(
                "January" to 0, "February" to 1, "March" to 2, "April" to 3,
                "May" to 4, "June" to 5, "July" to 6, "August" to 7,
                "September" to 8, "October" to 9, "November" to 10, "December" to 11
            )
            val month = monthMap[reportMonth] ?: 0

            expenses.filter { expense ->
                expense.date?.let { timestamp ->
                    calendar.timeInMillis = timestamp.toDate().time
                    calendar.get(java.util.Calendar.MONTH) == month &&
                            calendar.get(java.util.Calendar.YEAR) == reportYear
                } ?: false
            }
        } else {
            expenses.filter { expense ->
                expense.date?.let { timestamp ->
                    calendar.timeInMillis = timestamp.toDate().time
                    calendar.get(java.util.Calendar.YEAR) == reportYear
                } ?: false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = report.reportName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                // Summary
                item {
                    val totalIncoming = filteredExpenses.filter { it.type == "incoming" }.sumOf { it.amount }
                    val totalOutgoing = filteredExpenses.filter { it.type == "outgoing" }.sumOf { it.amount }
                    val netBalance = totalIncoming - totalOutgoing

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8E8E0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Summary",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3748)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Incoming:", fontSize = 14.sp, color = Color(0xFF6B6B6B))
                                Text(
                                    "₹${String.format("%.2f", totalIncoming)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Outgoing:", fontSize = 14.sp, color = Color(0xFF6B6B6B))
                                Text(
                                    "₹${String.format("%.2f", totalOutgoing)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Net Balance:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D3748))
                                Text(
                                    "₹${String.format("%.2f", kotlin.math.abs(netBalance))}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (netBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Expense items
                items(filteredExpenses) { expense ->
                    val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E8E0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = expense.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2D3748)
                                )
                                Text(
                                    text = "₹${String.format("%.2f", expense.amount)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (expense.type == "incoming") Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = expense.category,
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                                Text(
                                    text = expense.date?.toDate()?.let { dateFormat.format(it) } ?: "",
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7DDDD3)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// ============== PDF GENERATION FUNCTION ==============

fun generateAndDownloadPDFReport(
    context: android.content.Context,
    expenses: List<ExpenseItem>,
    reportType: String,
    selectedCategory: String,
    selectedType: String
) {
    try {
        val calendar = java.util.Calendar.getInstance()
        val now = System.currentTimeMillis()
        calendar.timeInMillis = now

        // Filter expenses based on report type
        val filteredByTime = if (reportType == "monthly") {
            val currentMonth = calendar.get(java.util.Calendar.MONTH)
            val currentYear = calendar.get(java.util.Calendar.YEAR)

            expenses.filter { expense ->
                expense.date?.let { timestamp ->
                    calendar.timeInMillis = timestamp.toDate().time
                    calendar.get(java.util.Calendar.MONTH) == currentMonth &&
                            calendar.get(java.util.Calendar.YEAR) == currentYear
                } ?: false
            }
        } else {
            val currentYear = calendar.get(java.util.Calendar.YEAR)

            expenses.filter { expense ->
                expense.date?.let { timestamp ->
                    calendar.timeInMillis = timestamp.toDate().time
                    calendar.get(java.util.Calendar.YEAR) == currentYear
                } ?: false
            }
        }

        // Apply category filter
        val categoryFiltered = if (selectedCategory == "All Categories") {
            filteredByTime
        } else {
            filteredByTime.filter { it.category == selectedCategory }
        }

        // Apply type filter
        val finalFiltered = when (selectedType) {
            "Incoming" -> categoryFiltered.filter { it.type == "incoming" }
            "Outgoing" -> categoryFiltered.filter { it.type == "outgoing" }
            else -> categoryFiltered
        }

        calendar.timeInMillis = now
        val monthName = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(calendar.time)
        val year = calendar.get(java.util.Calendar.YEAR)

        val fileName = "Expense_Report_${if (reportType == "monthly") monthName else year}_${System.currentTimeMillis()}.pdf"

        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var currentPage = pdfDocument.startPage(pageInfo)
        var canvas = currentPage.canvas
        val paint = android.graphics.Paint()

        var yPos = 50f

        // Title
        paint.textSize = 24f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText(
            "${if (reportType == "monthly") "Monthly" else "Yearly"} Expense Report",
            50f,
            yPos,
            paint
        )
        yPos += 40f

        // Subtitle
        paint.textSize = 14f
        paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText(
            if (reportType == "monthly") monthName else year.toString(),
            50f,
            yPos,
            paint
        )
        yPos += 30f

        // Summary
        val totalIncoming = finalFiltered.filter { it.type == "incoming" }.sumOf { it.amount }
        val totalOutgoing = finalFiltered.filter { it.type == "outgoing" }.sumOf { it.amount }
        val netBalance = totalIncoming - totalOutgoing

        paint.textSize = 12f
        canvas.drawText("Total Incoming: ₹${String.format("%.2f", totalIncoming)}", 50f, yPos, paint)
        yPos += 20f
        canvas.drawText("Total Outgoing: ₹${String.format("%.2f", totalOutgoing)}", 50f, yPos, paint)
        yPos += 20f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText("Net Balance: ₹${String.format("%.2f", kotlin.math.abs(netBalance))}", 50f, yPos, paint)
        paint.typeface = android.graphics.Typeface.DEFAULT
        yPos += 30f

        // Table headers
        paint.textSize = 10f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText("Date", 50f, yPos, paint)
        canvas.drawText("Name", 120f, yPos, paint)
        canvas.drawText("Category", 250f, yPos, paint)
        canvas.drawText("Type", 350f, yPos, paint)
        canvas.drawText("Amount (₹)", 420f, yPos, paint)
        yPos += 15f
        paint.typeface = android.graphics.Typeface.DEFAULT

        // Table content
        val dateFormat = java.text.SimpleDateFormat("dd MMM yy", java.util.Locale.getDefault())
        paint.textSize = 9f

        finalFiltered.forEach { expense ->
            if (yPos > 800f) {
                pdfDocument.finishPage(currentPage)
                currentPage = pdfDocument.startPage(pageInfo)
                canvas = currentPage.canvas
                yPos = 50f
            }

            canvas.drawText(
                expense.date?.toDate()?.let { dateFormat.format(it) } ?: "",
                50f,
                yPos,
                paint
            )
            canvas.drawText(expense.name.take(15), 120f, yPos, paint)
            canvas.drawText(expense.category.take(12), 250f, yPos, paint)
            canvas.drawText(if (expense.type == "incoming") "IN" else "OUT", 350f, yPos, paint)
            canvas.drawText(String.format("%.2f", expense.amount), 420f, yPos, paint)
            yPos += 15f
        }

        pdfDocument.finishPage(currentPage)

        // Save file (API 24+ compatible code)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
            }
        } else {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            )
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = java.io.File(downloadsDir, fileName)
            java.io.FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
        }

        pdfDocument.close()

        android.widget.Toast.makeText(
            context,
            "Report downloaded to Downloads folder",
            android.widget.Toast.LENGTH_LONG
        ).show()

    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "Failed to generate PDF: ${e.message}",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}

// ============== SHARE REPORT FUNCTION ==============

fun shareReport(
    context: android.content.Context,
    expenses: List<ExpenseItem>,
    reportType: String,
    selectedCategory: String,
    selectedType: String
) {
    try {
        val calendar = java.util.Calendar.getInstance()
        val now = System.currentTimeMillis()
        calendar.timeInMillis = now

        // Filter expenses based on report type
        val filteredByTime = if (reportType == "monthly") {
            val currentMonth = calendar.get(java.util.Calendar.MONTH)
            val currentYear = calendar.get(java.util.Calendar.YEAR)

            expenses.filter { expense ->
                expense.date?.let { timestamp ->
                    calendar.timeInMillis = timestamp.toDate().time
                    calendar.get(java.util.Calendar.MONTH) == currentMonth &&
                            calendar.get(java.util.Calendar.YEAR) == currentYear
                } ?: false
            }
        } else {
            val currentYear = calendar.get(java.util.Calendar.YEAR)

            expenses.filter { expense ->
                expense.date?.let { timestamp ->
                    calendar.timeInMillis = timestamp.toDate().time
                    calendar.get(java.util.Calendar.YEAR) == currentYear
                } ?: false
            }
        }

        // Apply category filter
        val categoryFiltered = if (selectedCategory == "All Categories") {
            filteredByTime
        } else {
            filteredByTime.filter { it.category == selectedCategory }
        }

        // Apply type filter
        val finalFiltered = when (selectedType) {
            "Incoming" -> categoryFiltered.filter { it.type == "incoming" }
            "Outgoing" -> categoryFiltered.filter { it.type == "outgoing" }
            else -> categoryFiltered
        }

        calendar.timeInMillis = now
        val monthName = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(calendar.time)
        val year = calendar.get(java.util.Calendar.YEAR)

        val fileName = "Expense_Report_Share_${System.currentTimeMillis()}.pdf"

        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var currentPage = pdfDocument.startPage(pageInfo)
        var canvas = currentPage.canvas
        val paint = android.graphics.Paint()

        var yPos = 50f

        // Title
        paint.textSize = 24f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText(
            "${if (reportType == "monthly") "Monthly" else "Yearly"} Expense Report",
            50f,
            yPos,
            paint
        )
        yPos += 40f

        // Subtitle
        paint.textSize = 14f
        paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText(
            if (reportType == "monthly") monthName else year.toString(),
            50f,
            yPos,
            paint
        )
        yPos += 30f

        // Summary
        val totalIncoming = finalFiltered.filter { it.type == "incoming" }.sumOf { it.amount }
        val totalOutgoing = finalFiltered.filter { it.type == "outgoing" }.sumOf { it.amount }
        val netBalance = totalIncoming - totalOutgoing

        paint.textSize = 12f
        canvas.drawText("Total Incoming: ₹${String.format("%.2f", totalIncoming)}", 50f, yPos, paint)
        yPos += 20f
        canvas.drawText("Total Outgoing: ₹${String.format("%.2f", totalOutgoing)}", 50f, yPos, paint)
        yPos += 20f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText("Net Balance: ₹${String.format("%.2f", kotlin.math.abs(netBalance))}", 50f, yPos, paint)
        paint.typeface = android.graphics.Typeface.DEFAULT
        yPos += 30f

        // Table headers
        paint.textSize = 10f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText("Date", 50f, yPos, paint)
        canvas.drawText("Name", 120f, yPos, paint)
        canvas.drawText("Category", 250f, yPos, paint)
        canvas.drawText("Type", 350f, yPos, paint)
        canvas.drawText("Amount (₹)", 420f, yPos, paint)
        yPos += 15f
        paint.typeface = android.graphics.Typeface.DEFAULT

        // Table content
        val dateFormat = java.text.SimpleDateFormat("dd MMM yy", java.util.Locale.getDefault())
        paint.textSize = 9f

        finalFiltered.forEach { expense ->
            if (yPos > 800f) {
                pdfDocument.finishPage(currentPage)
                currentPage = pdfDocument.startPage(pageInfo)
                canvas = currentPage.canvas
                yPos = 50f
            }

            canvas.drawText(
                expense.date?.toDate()?.let { dateFormat.format(it) } ?: "",
                50f,
                yPos,
                paint
            )
            canvas.drawText(expense.name.take(15), 120f, yPos, paint)
            canvas.drawText(expense.category.take(12), 250f, yPos, paint)
            canvas.drawText(if (expense.type == "incoming") "IN" else "OUT", 350f, yPos, paint)
            canvas.drawText(String.format("%.2f", expense.amount), 420f, yPos, paint)
            yPos += 15f
        }

        pdfDocument.finishPage(currentPage)

        // Save to cache directory for sharing
        val cacheDir = context.cacheDir
        val file = java.io.File(cacheDir, fileName)
        java.io.FileOutputStream(file).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }

        pdfDocument.close()

        // Share using FileProvider
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Expense Report - ${if (reportType == "monthly") monthName else year}")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Report"))

    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "Failed to share report: ${e.message}",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}

// ============== HELPER FUNCTIONS ==============

data class ReportData(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val monthlyCount: Int = 0,
    val weeklyIncome: Double = 0.0,
    val weeklyExpense: Double = 0.0,
    val weeklyCount: Int = 0,
    val topCategories: List<String> = emptyList()
)

fun calculateReportData(expenses: List<ExpenseItem>): ReportData {
    val now = System.currentTimeMillis()
    val weekAgo = now - (7 * 24 * 60 * 60 * 1000)
    val monthAgo = now - (30 * 24 * 60 * 60 * 1000)

    val allIncome = expenses.filter { it.type == "incoming" }.sumOf { it.amount }
    val allExpense = expenses.filter { it.type == "outgoing" }.sumOf { it.amount }

    val monthlyExpenses = expenses.filter {
        (it.date?.toDate()?.time ?: 0) >= monthAgo
    }
    val monthlyIncome = monthlyExpenses.filter { it.type == "incoming" }.sumOf { it.amount }
    val monthlyExpense = monthlyExpenses.filter { it.type == "outgoing" }.sumOf { it.amount }

    val weeklyExpenses = expenses.filter {
        (it.date?.toDate()?.time ?: 0) >= weekAgo
    }
    val weeklyIncome = weeklyExpenses.filter { it.type == "incoming" }.sumOf { it.amount }
    val weeklyExpense = weeklyExpenses.filter { it.type == "outgoing" }.sumOf { it.amount }

    val categoryTotals = expenses
        .groupBy { it.category }
        .mapValues { (_, items) -> items.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
        .take(3)
        .map { it.first }

    return ReportData(
        totalIncome = allIncome,
        totalExpense = allExpense,
        monthlyIncome = monthlyIncome,
        monthlyExpense = monthlyExpense,
        monthlyCount = monthlyExpenses.size,
        weeklyIncome = weeklyIncome,
        weeklyExpense = weeklyExpense,
        weeklyCount = weeklyExpenses.size,
        topCategories = categoryTotals
    )
}

data class AnalyticsData(
    val weeklyData: List<Double> = List(7) { 0.0 },
    val monthlyData: List<Double> = List(12) { 0.0 },
    val total: Double = 0.0,
    val count: Int = 0
)

fun calculateAnalyticsData(expenses: List<ExpenseItem>): AnalyticsData {
    val calendar = java.util.Calendar.getInstance()

    // Calculate weekly data (last 7 days)
    val weeklyData = MutableList(7) { 0.0 }
    val now = System.currentTimeMillis()

    expenses.forEach { expense ->
        expense.date?.let { timestamp ->
            val expenseTime = timestamp.toDate().time
            val daysDiff = ((now - expenseTime) / (1000 * 60 * 60 * 24)).toInt()

            if (daysDiff in 0..6) {
                val amount = if (expense.type == "incoming") expense.amount else -expense.amount
                weeklyData[6 - daysDiff] += amount
            }
        }
    }

    // Calculate monthly data (last 12 months)
    val monthlyData = MutableList(12) { 0.0 }
    calendar.timeInMillis = now
    val currentMonth = calendar.get(java.util.Calendar.MONTH)
    val currentYear = calendar.get(java.util.Calendar.YEAR)

    expenses.forEach { expense ->
        expense.date?.let { timestamp ->
            calendar.timeInMillis = timestamp.toDate().time
            val expenseMonth = calendar.get(java.util.Calendar.MONTH)
            val expenseYear = calendar.get(java.util.Calendar.YEAR)

            if (expenseYear == currentYear) {
                val amount = if (expense.type == "incoming") expense.amount else -expense.amount
                monthlyData[expenseMonth] += amount
            }
        }
    }

    val total = expenses.sumOf {
        if (it.type == "incoming") it.amount else -it.amount
    }

    return AnalyticsData(
        weeklyData = weeklyData,
        monthlyData = monthlyData,
        total = total,
        count = expenses.size
    )
}

// ============== ANALYTICS SCREEN ==============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedFilter by remember { mutableStateOf("All Categories") }
    var selectedType by remember { mutableStateOf("All") }  // ← NEW: Type filter state
    var showDatePicker by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showTypeMenu by remember { mutableStateOf(false) }  // ← NEW: Type menu state
    var expenses by remember { mutableStateOf<List<ExpenseItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var categories by remember { mutableStateOf<List<String>>(listOf("All Categories")) }

    val db = FirebaseFirestore.getInstance()

    // Format selected date
    val dateFormat = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
    val formattedDate = dateFormat.format(java.util.Date(selectedDate))

    // Load expenses and categories with real-time listener
    LaunchedEffect(Unit) {
        if (UserSession.familyId.isNotEmpty()) {
            // Load categories
            db.collection("families")
                .document(UserSession.familyId)
                .collection("categories")
                .addSnapshotListener { snapshot, _ ->  // ← CHANGED: Real-time listener
                    val loadedCategories = mutableListOf("All Categories")
                    loadedCategories.addAll(
                        snapshot?.documents?.mapNotNull { doc ->
                            doc.getString("name")
                        } ?: emptyList()
                    )
                    categories = loadedCategories
                }

            // ← CHANGED: Real-time listener for expenses
            db.collection("families")
                .document(UserSession.familyId)
                .collection("expenses")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }

                    expenses = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            ExpenseItem(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                amount = doc.getDouble("amount") ?: 0.0,
                                category = doc.getString("category") ?: "",
                                type = doc.getString("type") ?: "outgoing",
                                date = doc.getTimestamp("date"),
                                addedBy = doc.getString("addedBy") ?: "",
                                addedByName = doc.getString("addedByName") ?: "",
                                notes = doc.getString("notes") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()

                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    // ← UPDATED: Filter expenses by category AND type
    val filteredExpenses = remember(expenses, selectedFilter, selectedType) {
        var filtered = when (selectedFilter) {
            "All Categories" -> expenses
            else -> expenses.filter { it.category == selectedFilter }
        }

        filtered = when (selectedType) {
            "Incoming" -> filtered.filter { it.type == "incoming" }
            "Outgoing" -> filtered.filter { it.type == "outgoing" }
            else -> filtered
        }

        filtered
    }

    // ← UPDATED: Calculate analytics with filtered data
    val analyticsData = remember(filteredExpenses) {
        calculateAnalyticsData(filteredExpenses)
    }

    // Filter Menu Dialog
    if (showFilterMenu) {
        AlertDialog(
            onDismissRequest = { showFilterMenu = false },
            title = { Text("Select Category") },
            text = {
                LazyColumn {
                    items(categories) { category ->
                        Card(
                            onClick = {
                                selectedFilter = category
                                showFilterMenu = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedFilter == category) Color(0xFF7DDDD3) else Color(0xFFE8E8E0)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = category,
                                modifier = Modifier.padding(16.dp),
                                fontSize = 16.sp,
                                color = if (selectedFilter == category) Color.White else Color(0xFF2D3748)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFilterMenu = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ← NEW: Type Filter Dialog
    if (showTypeMenu) {
        AlertDialog(
            onDismissRequest = { showTypeMenu = false },
            title = { Text("Filter by Type") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // All option
                    Card(
                        onClick = {
                            selectedType = "All"
                            showTypeMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedType == "All") Color(0xFF7DDDD3) else Color(0xFFE8E8E0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_view),
                                contentDescription = "All",
                                tint = if (selectedType == "All") Color.White else Color(0xFF2D3748),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "All",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedType == "All") Color.White else Color(0xFF2D3748)
                            )
                        }
                    }

                    // Incoming option
                    Card(
                        onClick = {
                            selectedType = "Incoming"
                            showTypeMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedType == "Incoming") Color(0xFF10B981) else Color(0xFFD1FAE5)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.arrow_up_float),
                                contentDescription = "Incoming",
                                tint = if (selectedType == "Incoming") Color.White else Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Incoming",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selectedType == "Incoming") Color.White else Color(0xFF10B981)
                                )
                                Text(
                                    text = "Money received",
                                    fontSize = 12.sp,
                                    color = if (selectedType == "Incoming") Color.White.copy(alpha = 0.8f) else Color(0xFF10B981).copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Outgoing option
                    Card(
                        onClick = {
                            selectedType = "Outgoing"
                            showTypeMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedType == "Outgoing") Color(0xFFEF4444) else Color(0xFFFFE8E8)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.arrow_down_float),
                                contentDescription = "Outgoing",
                                tint = if (selectedType == "Outgoing") Color.White else Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Outgoing",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selectedType == "Outgoing") Color.White else Color(0xFFEF4444)
                                )
                                Text(
                                    text = "Money spent",
                                    fontSize = 12.sp,
                                    color = if (selectedType == "Outgoing") Color.White.copy(alpha = 0.8f) else Color(0xFFEF4444).copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTypeMenu = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF7DDDD3))
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F0))
                .padding(16.dp)
        ) {
            // Back button
            item {
                OutlinedButton(
                    onClick = onBackClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = Color(0xFF6A11CB)
                    )
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_revert),
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Back",
                        modifier = Modifier.padding(start = 8.dp),
                        color = Color(0xFF6A11CB)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Title
            item {
                Text(
                    text = "Detailed Expense Analytics",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B6B6B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            // ← UPDATED: Three filter dropdowns (Date, Category, Type)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Date Selector
                        Card(
                            onClick = { /* Date picker would go here */ },
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8E8E0)
                            ),
                            shape = RoundedCornerShape(32.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF7DDDD3))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_today),
                                    contentDescription = "Date",
                                    tint = Color(0xFF7DDDD3),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formattedDate,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2D3748)
                                )
                            }
                        }

                        // Category Filter
                        Card(
                            onClick = { showFilterMenu = true },
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8E8E0)
                            ),
                            shape = RoundedCornerShape(32.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF7DDDD3))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = selectedFilter,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2D3748),
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    painter = painterResource(id = android.R.drawable.arrow_down_float),
                                    contentDescription = "Dropdown",
                                    tint = Color(0xFF7DDDD3),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // ← NEW: Type Filter Dropdown (full width)
                    Card(
                        onClick = { showTypeMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8E8E0)
                        ),
                        shape = RoundedCornerShape(32.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            when (selectedType) {
                                "Incoming" -> Color(0xFF10B981)
                                "Outgoing" -> Color(0xFFEF4444)
                                else -> Color(0xFF7DDDD3)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = when (selectedType) {
                                            "Incoming" -> android.R.drawable.arrow_up_float
                                            "Outgoing" -> android.R.drawable.arrow_down_float
                                            else -> android.R.drawable.ic_menu_view
                                        }
                                    ),
                                    contentDescription = selectedType,
                                    tint = when (selectedType) {
                                        "Incoming" -> Color(0xFF10B981)
                                        "Outgoing" -> Color(0xFFEF4444)
                                        else -> Color(0xFF7DDDD3)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = selectedType,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2D3748)
                                )
                            }
                            Icon(
                                painter = painterResource(id = android.R.drawable.arrow_down_float),
                                contentDescription = "Dropdown",
                                tint = when (selectedType) {
                                    "Incoming" -> Color(0xFF10B981)
                                    "Outgoing" -> Color(0xFFEF4444)
                                    else -> Color(0xFF7DDDD3)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Daily Expenses Chart
            item {
                AnalyticsChartCard(
                    title = "Daily Expenses",
                    data = analyticsData.weeklyData,
                    labels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
                    chartType = "line",
                    selectedType = selectedType
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Weekly Expenses Chart
            item {
                AnalyticsChartCard(
                    title = "Weekly Expenses",
                    data = analyticsData.weeklyData,
                    labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                    chartType = "bar",
                    selectedType = selectedType
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Monthly Expenses Chart
            item {
                AnalyticsChartCard(
                    title = "Monthly Expenses",
                    data = analyticsData.monthlyData,
                    labels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"),
                    chartType = "line",
                    selectedType = selectedType
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ← UPDATED: Summary Cards with Indian Rupees
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total This Month
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8E8E0)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Total This Month:",
                                fontSize = 14.sp,
                                color = Color(0xFF6B6B6B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "₹${String.format("%.2f", kotlin.math.abs(analyticsData.total))}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (analyticsData.total >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    painter = painterResource(
                                        id = if (analyticsData.total >= 0)
                                            android.R.drawable.arrow_up_float
                                        else
                                            android.R.drawable.arrow_down_float
                                    ),
                                    contentDescription = "Trend",
                                    tint = if (analyticsData.total >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Count
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8E8E0)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Total Transactions:",
                                fontSize = 14.sp,
                                color = Color(0xFF6B6B6B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${analyticsData.count}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3748)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Download Report Button
            item {
                Button(
                    onClick = {
                        // ← NEW: Generate PDF from entire analytics page
                        generateAnalyticsPDF(
                            context = context,
                            analyticsData = analyticsData,
                            selectedFilter = selectedFilter,
                            selectedType = selectedType,
                            filteredExpenses = filteredExpenses
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_download),
                        contentDescription = "Download",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Download Report",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ← UPDATED: Analytics Chart Card with Indian Rupees
@Composable
fun AnalyticsChartCard(
    title: String,
    data: List<Double>,
    labels: List<String>,
    chartType: String,
    selectedType: String = "All"
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8E8E0)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )

                // Type indicator
                if (selectedType != "All") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (selectedType) {
                                    "Incoming" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                    "Outgoing" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                    else -> Color(0xFF7DDDD3).copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = selectedType,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when (selectedType) {
                                "Incoming" -> Color(0xFF10B981)
                                "Outgoing" -> Color(0xFFEF4444)
                                else -> Color(0xFF7DDDD3)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Chart visualization
            if (chartType == "line") {
                SimpleLineChart(
                    data = data,
                    labels = labels,
                    selectedType = selectedType
                )
            } else {
                SimpleBarChart(
                    data = data,
                    labels = labels,
                    selectedType = selectedType
                )
            }
        }
    }
}

// ← UPDATED: Simple Line Chart with color based on type
@Composable
fun SimpleLineChart(
    data: List<Double>,
    labels: List<String>,
    selectedType: String = "All"
) {
    val maxValue = data.maxOfOrNull { kotlin.math.abs(it) } ?: 100.0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, value ->
                val absValue = kotlin.math.abs(value)
                val heightFraction = if (maxValue > 0) (absValue / maxValue).toFloat() else 0f
                val barColor = when {
                    selectedType == "Incoming" -> Color(0xFF10B981)
                    selectedType == "Outgoing" -> Color(0xFFEF4444)
                    value >= 0 -> Color(0xFF10B981)
                    else -> Color(0xFFEF4444)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight(fraction = if (heightFraction > 0) heightFraction else 0.05f)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(barColor)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Labels
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        labels.take(data.size).forEach { label ->
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF6B6B6B),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Indian Rupees label
    Text(
        text = "Amount in ₹",
        fontSize = 10.sp,
        color = Color(0xFF9CA3AF),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

// ← UPDATED: Simple Bar Chart with color based on type
@Composable
fun SimpleBarChart(
    data: List<Double>,
    labels: List<String>,
    selectedType: String = "All"
) {
    val maxValue = data.maxOfOrNull { kotlin.math.abs(it) } ?: 100.0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, value ->
                val absValue = kotlin.math.abs(value)
                val heightFraction = if (maxValue > 0) (absValue / maxValue).toFloat() else 0f
                val barColor = when {
                    selectedType == "Incoming" -> Color(0xFF10B981)
                    selectedType == "Outgoing" -> Color(0xFFEF4444)
                    value >= 0 -> Color(0xFF10B981)
                    else -> Color(0xFFEF4444)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .fillMaxHeight(fraction = if (heightFraction > 0) heightFraction else 0.05f)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(barColor)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Labels
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        labels.take(data.size).forEach { label ->
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF6B6B6B),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Indian Rupees label
    Text(
        text = "Amount in ₹",
        fontSize = 10.sp,
        color = Color(0xFF9CA3AF),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

// ← NEW: Generate Analytics PDF Function
fun generateAnalyticsPDF(
    context: android.content.Context,
    analyticsData: AnalyticsData,
    selectedFilter: String,
    selectedType: String,
    filteredExpenses: List<ExpenseItem>
) {
    try {
        val fileName = "Analytics_Report_${System.currentTimeMillis()}.pdf"
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var currentPage = pdfDocument.startPage(pageInfo)
        var canvas = currentPage.canvas
        val paint = android.graphics.Paint()

        var yPos = 50f

        // Title
        paint.textSize = 24f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText("Expense Analytics Report", 50f, yPos, paint)
        yPos += 40f

        // Filters Info
        paint.textSize = 12f
        paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText("Category: $selectedFilter", 50f, yPos, paint)
        yPos += 20f
        canvas.drawText("Type: $selectedType", 50f, yPos, paint)
        yPos += 30f

        // Summary Section
        paint.textSize = 16f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText("Summary", 50f, yPos, paint)
        yPos += 25f

        paint.textSize = 12f
        paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText("Total This Month: ₹${String.format("%.2f", kotlin.math.abs(analyticsData.total))}", 50f, yPos, paint)
        yPos += 20f
        canvas.drawText("Total Transactions: ${analyticsData.count}", 50f, yPos, paint)
        yPos += 30f

        // Weekly Data Section
        paint.textSize = 14f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText("Weekly Data", 50f, yPos, paint)
        yPos += 20f

        paint.textSize = 10f
        paint.typeface = android.graphics.Typeface.DEFAULT
        val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        weekDays.forEachIndexed { index, day ->
            if (index < analyticsData.weeklyData.size) {
                canvas.drawText("$day: ₹${String.format("%.2f", kotlin.math.abs(analyticsData.weeklyData[index]))}", 50f, yPos, paint)
                yPos += 15f
            }
        }

        yPos += 15f

        // Monthly Data Section
        paint.textSize = 14f
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        canvas.drawText("Monthly Data", 50f, yPos, paint)
        yPos += 20f

        paint.textSize = 10f
        paint.typeface = android.graphics.Typeface.DEFAULT
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        months.forEachIndexed { index, month ->
            if (index < analyticsData.monthlyData.size) {
                canvas.drawText("$month: ₹${String.format("%.2f", kotlin.math.abs(analyticsData.monthlyData[index]))}", 50f, yPos, paint)
                yPos += 15f

                // Start new page if needed
                if (yPos > 800f) {
                    pdfDocument.finishPage(currentPage)
                    currentPage = pdfDocument.startPage(pageInfo)
                    canvas = currentPage.canvas
                    yPos = 50f
                }
            }
        }

        yPos += 15f

        // Recent Transactions
        if (filteredExpenses.isNotEmpty()) {
            paint.textSize = 14f
            paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            canvas.drawText("Recent Transactions", 50f, yPos, paint)
            yPos += 20f

            paint.textSize = 10f
            paint.typeface = android.graphics.Typeface.DEFAULT

            val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())

            filteredExpenses.take(20).forEach { expense ->
                if (yPos > 800f) {
                    pdfDocument.finishPage(currentPage)
                    currentPage = pdfDocument.startPage(pageInfo)
                    canvas = currentPage.canvas
                    yPos = 50f
                }

                val dateStr = expense.date?.toDate()?.let { dateFormat.format(it) } ?: ""
                val typeIndicator = if (expense.type == "incoming") "↑" else "↓"
                canvas.drawText("$dateStr - ${expense.name} $typeIndicator ₹${String.format("%.2f", expense.amount)}", 50f, yPos, paint)
                yPos += 15f
            }
        }

        pdfDocument.finishPage(currentPage)

        // Save file (API 24+ compatible)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
            }
        } else {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            )
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = java.io.File(downloadsDir, fileName)
            java.io.FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
        }

        pdfDocument.close()

        android.widget.Toast.makeText(
            context,
            "Analytics report downloaded to Downloads folder",
            android.widget.Toast.LENGTH_LONG
        ).show()

    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "Failed to generate PDF: ${e.message}",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}

// ============== GROCERY LIST SCREEN ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(
    onBackClick: () -> Unit
) {
    var groceryItems by remember { mutableStateOf<List<GroceryItem>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<GroceryItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Reorderable state for drag-and-drop
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            // Update the list order in memory immediately for smooth UI
            groceryItems = groceryItems.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        },
        onDragEnd = { _, _ ->
            // Save the new order to Firebase after dragging ends
            groceryItems.forEachIndexed { index, item ->
                db.collection("families")
                    .document(UserSession.familyId)
                    .collection("groceryList")
                    .document(item.id)
                    .update("order", index)
                    .addOnFailureListener { e ->
                        Log.e("GroceryList", "Failed to update order", e)
                    }
            }
        }
    )

    // Load grocery items from Firebase
    LaunchedEffect(Unit) {
        if (UserSession.familyId.isNotEmpty()) {
            db.collection("families")
                .document(UserSession.familyId)
                .collection("groceryList")
                .orderBy("order", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("GroceryList", "Error loading items", error)
                        isLoading = false
                        return@addSnapshotListener
                    }

                    groceryItems = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            GroceryItem(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                isChecked = doc.getBoolean("isChecked") ?: false,
                                imageUrl = doc.getString("imageUrl") ?: "",
                                addedBy = doc.getString("addedBy") ?: "",
                                addedAt = doc.getTimestamp("addedAt")
                            )
                        } catch (e: Exception) {
                            Log.e("GroceryList", "Error parsing item", e)
                            null
                        }
                    } ?: emptyList()

                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    // Add Item Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                newItemName = ""
            },
            title = { Text("Add Grocery Item") },
            text = {
                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    placeholder = { Text("Item name", color = Color(0xFF9CA3AF)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE8E8E0),
                        unfocusedContainerColor = Color(0xFFE8E8E0),
                        focusedIndicatorColor = Color(0xFF7DDDD3),
                        unfocusedIndicatorColor = Color(0xFFB8B8A8),
                        cursorColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newItemName.isBlank()) {
                            android.widget.Toast.makeText(
                                context,
                                "Please enter an item name",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        isSaving = true

                        val itemData = hashMapOf(
                            "name" to newItemName,
                            "isChecked" to false,
                            "imageUrl" to "",
                            "addedBy" to UserSession.userId,
                            "addedAt" to Timestamp.now(),
                            "order" to groceryItems.size
                        )

                        db.collection("families")
                            .document(UserSession.familyId)
                            .collection("groceryList")
                            .add(itemData)
                            .addOnSuccessListener {
                                newItemName = ""
                                isSaving = false
                                showAddDialog = false
                            }
                            .addOnFailureListener { e ->
                                android.widget.Toast.makeText(
                                    context,
                                    "Failed to add item: ${e.message}",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                isSaving = false
                            }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Add")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showAddDialog = false
                        newItemName = ""
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6A11CB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                itemToDelete = null
            },
            title = {
                Text(
                    "Delete Item",
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${itemToDelete?.name}\"?",
                    fontSize = 16.sp,
                    color = Color(0xFF2D3748)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { item ->
                            db.collection("families")
                                .document(UserSession.familyId)
                                .collection("groceryList")
                                .document(item.id)
                                .delete()
                                .addOnSuccessListener {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Item deleted",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    showDeleteDialog = false
                                    itemToDelete = null
                                }
                                .addOnFailureListener { e ->
                                    android.widget.Toast.makeText(
                                        context,
                                        "Failed to delete: ${e.message}",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    showDeleteDialog = false
                                    itemToDelete = null
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6A11CB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F0))
                .padding(24.dp)
        ) {
            // Header with Back button and Share icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = Color(0xFF6A11CB)
                    )
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_revert),
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Back",
                        modifier = Modifier.padding(start = 8.dp),
                        color = Color(0xFF6A11CB)
                    )
                }

                // Share Icon
                IconButton(
                    onClick = {
                        val itemsText = groceryItems.joinToString("\n") { item ->
                            val status = if (item.isChecked) "✓" else "○"
                            "$status ${item.name}"
                        }

                        val shareText = "Grocery List:\n\n$itemsText"

                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Grocery List")
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Share Grocery List"))
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7DDDD3))
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_share),
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Groceries List",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B6B6B),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Grocery items card
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF7DDDD3))
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8E8E0)
                    ),
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    if (groceryItems.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_agenda),
                                    contentDescription = "Empty",
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No items yet",
                                    fontSize = 18.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap the + button to add items",
                                    fontSize = 14.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = reorderState.listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                                .reorderable(reorderState),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(groceryItems, key = { it.id }) { item ->
                                ReorderableItem(reorderState, key = item.id) { isDragging ->
                                    val elevation = androidx.compose.animation.core.animateDpAsState(
                                        if (isDragging) 8.dp else 0.dp
                                    )

                                    GroceryListItemCard(
                                        item = item,
                                        onCheckedChange = { checked ->
                                            // Update in Firebase
                                            db.collection("families")
                                                .document(UserSession.familyId)
                                                .collection("groceryList")
                                                .document(item.id)
                                                .update("isChecked", checked)
                                        },
                                        onLongPress = {
                                            itemToDelete = item
                                            showDeleteDialog = true
                                        },
                                        isDragging = isDragging,
                                        elevation = elevation.value,
                                        reorderState = reorderState
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button - MOVED FURTHER FROM EDGES
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp, end = 40.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            androidx.compose.material3.FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF7DDDD3),
                contentColor = Color.White,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_input_add),
                    contentDescription = "Add Item",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// Updated Grocery List Item Card with 2-line Hamburger on RIGHT side
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroceryListItemCard(
    item: GroceryItem,
    onCheckedChange: (Boolean) -> Unit,
    onLongPress: () -> Unit,
    isDragging: Boolean = false,
    elevation: androidx.compose.ui.unit.Dp = 0.dp,
    reorderState: org.burnoutcrew.reorderable.ReorderableLazyListState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) Color(0xFFD4F1E8) else Color.Transparent
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = { onCheckedChange(!item.isChecked) },
                    onLongClick = { onLongPress() }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Square Checkbox
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(
                        width = 2.dp,
                        color = if (item.isChecked) Color(0xFF7DDDD3) else Color(0xFF6B6B6B),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .background(
                        if (item.isChecked) Color(0xFF7DDDD3) else Color.Transparent
                    )
                    .clickable { onCheckedChange(!item.isChecked) },
                contentAlignment = Alignment.Center
            ) {
                if (item.isChecked) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = "Checked",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Item name
            Text(
                text = item.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2D3748),
                modifier = Modifier.weight(1f),
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                style = androidx.compose.ui.text.TextStyle(
                    color = if (item.isChecked) Color(0xFF9CA3AF) else Color(0xFF2D3748)
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 2-Line Hamburger Icon on RIGHT side - Drag handle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,  // ← CORRECT: horizontalAlignment
                verticalArrangement = Arrangement.spacedBy(4.dp),   // ← CORRECT: verticalArrangement
                modifier = Modifier
                    .size(32.dp)
                    .padding(top = 11.dp)
                    .detectReorder(reorderState)  // Drag detection
            ) {
                // First line
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(2.dp)
                        .background(Color(0xFFB0B0B0))
                )
                // Second line
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(2.dp)
                        .background(Color(0xFFB0B0B0))
                )
            }
        }
    }
}

// ============== UPDATED BILL REMINDERS SCREEN ==============
// Replace the existing BillRemindersScreen and BillReminderCard functions with these

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BillRemindersScreen(
    onBackClick: () -> Unit
) {
    var billReminders by remember { mutableStateOf<List<BillReminder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOptionsDialog by remember { mutableStateOf(false) }
    var selectedBill by remember { mutableStateOf<BillReminder?>(null) }

    // Form fields
    var billName by remember { mutableStateOf("") }
    var billAmount by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var frequency by remember { mutableStateOf("monthly") } // one-time, weekly, monthly, yearly
    var remindDaysBefore by remember { mutableStateOf(1) }
    var showFrequencyMenu by remember { mutableStateOf(false) }
    var showReminderMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    val dateFormat = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())

    // Load bill reminders from Firebase
    LaunchedEffect(Unit) {
        if (UserSession.familyId.isNotEmpty()) {
            db.collection("families")
                .document(UserSession.familyId)
                .collection("billReminders")
                .orderBy("dueDate", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("BillReminders", "Error loading reminders", error)
                        isLoading = false
                        return@addSnapshotListener
                    }

                    billReminders = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            BillReminder(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                amount = doc.getDouble("amount") ?: 0.0,
                                dueDate = doc.getTimestamp("dueDate"),
                                remindDaysBefore = doc.getLong("remindDaysBefore")?.toInt() ?: 1,
                                isReminderEnabled = doc.getBoolean("isReminderEnabled") ?: true,
                                isPaid = doc.getBoolean("isPaid") ?: false,
                                addedBy = doc.getString("addedBy") ?: "",
                                addedAt = doc.getTimestamp("addedAt"),
                                category = doc.getString("frequency") ?: "monthly"
                            )
                        } catch (e: Exception) {
                            Log.e("BillReminders", "Error parsing reminder", e)
                            null
                        }
                    } ?: emptyList()

                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    // Add/Edit Dialog
    if (showAddDialog || showEditDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                showEditDialog = false
                billName = ""
                billAmount = ""
                selectedDate = System.currentTimeMillis()
                frequency = "monthly"
                remindDaysBefore = 1
            },
            title = { Text(if (showAddDialog) "Add Bill Reminder" else "Edit Bill Reminder") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Bill Name
                    OutlinedTextField(
                        value = billName,
                        onValueChange = { billName = it },
                        label = { Text("Bill Name") },
                        placeholder = { Text("e.g., Electricity Bill") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8E8E0),
                            unfocusedContainerColor = Color(0xFFE8E8E0),
                            focusedIndicatorColor = Color(0xFF7DDDD3),
                            unfocusedIndicatorColor = Color(0xFFB8B8A8),
                            cursorColor = Color(0xFF7DDDD3)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Bill Amount
                    OutlinedTextField(
                        value = billAmount,
                        onValueChange = { billAmount = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Amount") },
                        placeholder = { Text("0.00") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8E8E0),
                            unfocusedContainerColor = Color(0xFFE8E8E0),
                            focusedIndicatorColor = Color(0xFF7DDDD3),
                            unfocusedIndicatorColor = Color(0xFFB8B8A8),
                            cursorColor = Color(0xFF7DDDD3)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = {
                            Text("₹", fontSize = 18.sp, color = Color(0xFF2D3748))
                        }
                    )

                    // Due Date
                    OutlinedTextField(
                        value = dateFormat.format(java.util.Date(selectedDate)),
                        onValueChange = { },
                        label = { Text("Due Date") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8E8E0),
                            unfocusedContainerColor = Color(0xFFE8E8E0),
                            focusedIndicatorColor = Color(0xFF7DDDD3),
                            unfocusedIndicatorColor = Color(0xFFB8B8A8),
                            disabledContainerColor = Color(0xFFE8E8E0),
                            disabledIndicatorColor = Color(0xFFB8B8A8),
                            disabledTextColor = Color(0xFF2D3748)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        enabled = false,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_today),
                                contentDescription = "Date",
                                tint = Color(0xFF7DDDD3)
                            )
                        }
                    )

                    // Frequency Dropdown
                    OutlinedTextField(
                        value = frequency.capitalize(),
                        onValueChange = { },
                        label = { Text("Frequency") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFrequencyMenu = true },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8E8E0),
                            unfocusedContainerColor = Color(0xFFE8E8E0),
                            focusedIndicatorColor = Color(0xFF7DDDD3),
                            unfocusedIndicatorColor = Color(0xFFB8B8A8),
                            disabledContainerColor = Color(0xFFE8E8E0),
                            disabledIndicatorColor = Color(0xFFB8B8A8),
                            disabledTextColor = Color(0xFF2D3748)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        enabled = false,
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = android.R.drawable.arrow_down_float),
                                contentDescription = "Dropdown",
                                tint = Color(0xFF7DDDD3)
                            )
                        }
                    )

                    // Remind Days Before Dropdown
                    OutlinedTextField(
                        value = "$remindDaysBefore day(s) before",
                        onValueChange = { },
                        label = { Text("Remind Me") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showReminderMenu = true },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8E8E0),
                            unfocusedContainerColor = Color(0xFFE8E8E0),
                            focusedIndicatorColor = Color(0xFF7DDDD3),
                            unfocusedIndicatorColor = Color(0xFFB8B8A8),
                            disabledContainerColor = Color(0xFFE8E8E0),
                            disabledIndicatorColor = Color(0xFFB8B8A8),
                            disabledTextColor = Color(0xFF2D3748)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        enabled = false,
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = android.R.drawable.arrow_down_float),
                                contentDescription = "Dropdown",
                                tint = Color(0xFF7DDDD3)
                            )
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (billName.isBlank()) {
                            android.widget.Toast.makeText(context, "Please enter bill name", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (billAmount.isBlank() || billAmount.toDoubleOrNull() == null) {
                            android.widget.Toast.makeText(context, "Please enter valid amount", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSaving = true

                        val billData = hashMapOf(
                            "name" to billName,
                            "amount" to billAmount.toDouble(),
                            "dueDate" to Timestamp(java.util.Date(selectedDate)),
                            "frequency" to frequency,
                            "remindDaysBefore" to remindDaysBefore,
                            "isReminderEnabled" to true,
                            "isPaid" to false,
                            "addedBy" to UserSession.userId,
                            "addedAt" to Timestamp.now()
                        )

                        if (showEditDialog && selectedBill != null) {
                            // Update existing bill
                            db.collection("families")
                                .document(UserSession.familyId)
                                .collection("billReminders")
                                .document(selectedBill!!.id)
                                .update(billData as Map<String, Any>)
                                .addOnSuccessListener {
                                    isSaving = false
                                    showEditDialog = false
                                    billName = ""
                                    billAmount = ""
                                    android.widget.Toast.makeText(context, "Bill updated", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    isSaving = false
                                    android.widget.Toast.makeText(context, "Failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            // Add new bill
                            db.collection("families")
                                .document(UserSession.familyId)
                                .collection("billReminders")
                                .add(billData)
                                .addOnSuccessListener {
                                    isSaving = false
                                    showAddDialog = false
                                    billName = ""
                                    billAmount = ""
                                    android.widget.Toast.makeText(context, "Bill added", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    isSaving = false
                                    android.widget.Toast.makeText(context, "Failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (showEditDialog) "Update" else "Add")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showAddDialog = false
                        showEditDialog = false
                        billName = ""
                        billAmount = ""
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6A11CB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Frequency Menu Dialog
    if (showFrequencyMenu) {
        AlertDialog(
            onDismissRequest = { showFrequencyMenu = false },
            title = { Text("Select Frequency") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("one-time", "weekly", "monthly", "yearly").forEach { freq ->
                        Card(
                            onClick = {
                                frequency = freq
                                showFrequencyMenu = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (frequency == freq) Color(0xFF7DDDD3) else Color(0xFFE8E8E0)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = freq.capitalize(),
                                modifier = Modifier.padding(16.dp),
                                fontSize = 16.sp,
                                color = if (frequency == freq) Color.White else Color(0xFF2D3748)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFrequencyMenu = false }) {
                    Text("Close", color = Color(0xFF6A11CB))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Reminder Days Menu Dialog
    if (showReminderMenu) {
        AlertDialog(
            onDismissRequest = { showReminderMenu = false },
            title = { Text("Remind Me") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { days ->
                        Card(
                            onClick = {
                                remindDaysBefore = days
                                showReminderMenu = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (remindDaysBefore == days) Color(0xFF7DDDD3) else Color(0xFFE8E8E0)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "$days day${if (days > 1) "s" else ""} before",
                                modifier = Modifier.padding(16.dp),
                                fontSize = 16.sp,
                                color = if (remindDaysBefore == days) Color.White else Color(0xFF2D3748)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReminderMenu = false }) {
                    Text("Close", color = Color(0xFF6A11CB))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = selectedDate

        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Select Due Date") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Selected: ${dateFormat.format(java.util.Date(selectedDate))}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                calendar.add(java.util.Calendar.DAY_OF_MONTH, -1)
                                selectedDate = calendar.timeInMillis
                            }
                        ) {
                            Text("Previous Day")
                        }
                        OutlinedButton(
                            onClick = {
                                calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                                selectedDate = calendar.timeInMillis
                            }
                        ) {
                            Text("Next Day")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            selectedDate = System.currentTimeMillis()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7DDDD3)
                        )
                    ) {
                        Text("Today")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    )
                ) {
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Options Dialog (Edit/Delete)
    if (showOptionsDialog && selectedBill != null) {
        AlertDialog(
            onDismissRequest = {
                showOptionsDialog = false
                selectedBill = null
            },
            title = { Text("Bill Options") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Edit option
                    Card(
                        onClick = {
                            billName = selectedBill?.name ?: ""
                            billAmount = selectedBill?.amount?.toString() ?: ""
                            selectedDate = selectedBill?.dueDate?.toDate()?.time ?: System.currentTimeMillis()
                            frequency = selectedBill?.category ?: "monthly"
                            remindDaysBefore = selectedBill?.remindDaysBefore ?: 1
                            showOptionsDialog = false
                            showEditDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8E8E0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_edit),
                                contentDescription = "Edit",
                                tint = Color(0xFF7DDDD3),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Edit",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2D3748)
                            )
                        }
                    }

                    // Delete option
                    Card(
                        onClick = {
                            showOptionsDialog = false
                            showDeleteDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFE8E8)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_delete),
                                contentDescription = "Delete",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Delete",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOptionsDialog = false
                        selectedBill = null
                    }
                ) {
                    Text("Cancel", color = Color(0xFF6A11CB))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && selectedBill != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                selectedBill = null
            },
            title = {
                Text(
                    "Delete Bill",
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${selectedBill?.name}\"? This action cannot be undone.",
                    fontSize = 16.sp,
                    color = Color(0xFF2D3748)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        db.collection("families")
                            .document(UserSession.familyId)
                            .collection("billReminders")
                            .document(selectedBill!!.id)
                            .delete()
                            .addOnSuccessListener {
                                android.widget.Toast.makeText(
                                    context,
                                    "Bill deleted",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                showDeleteDialog = false
                                selectedBill = null
                            }
                            .addOnFailureListener { e ->
                                android.widget.Toast.makeText(
                                    context,
                                    "Failed to delete: ${e.message}",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedBill = null
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6A11CB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F0))
                .padding(24.dp)
        ) {
            // Back button
            OutlinedButton(
                onClick = onBackClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF6A11CB)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF6A11CB)
                )
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_revert),
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Back",
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color(0xFF6A11CB)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Bill Reminders",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B6B6B),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Bill reminders list
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF7DDDD3))
                }
            } else if (billReminders.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8E8E0)
                    ),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_today),
                                contentDescription = "Empty",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No bill reminders yet",
                                fontSize = 18.sp,
                                color = Color(0xFF9CA3AF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap the + button to add bills",
                                fontSize = 14.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(billReminders) { bill ->
                        BillReminderCard(
                            bill = bill,
                            onToggleReminder = { enabled ->
                                db.collection("families")
                                    .document(UserSession.familyId)
                                    .collection("billReminders")
                                    .document(bill.id)
                                    .update("isReminderEnabled", enabled)
                                    .addOnSuccessListener {
                                        if (enabled) {
                                            // Send notification to all family members
                                            sendBillReminderNotification(bill, context)
                                        }
                                    }
                            },
                            onLongPress = {
                                selectedBill = bill
                                showOptionsDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button (same spacing as Grocery List)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp, end = 40.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            androidx.compose.material3.FloatingActionButton(
                onClick = {
                    billName = ""
                    billAmount = ""
                    selectedDate = System.currentTimeMillis()
                    frequency = "monthly"
                    remindDaysBefore = 1
                    showAddDialog = true
                },
                containerColor = Color(0xFF7DDDD3),
                contentColor = Color.White,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_input_add),
                    contentDescription = "Add Bill",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// Updated Bill Reminder Card with Long Press and Toggle
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BillReminderCard(
    bill: BillReminder,
    onToggleReminder: (Boolean) -> Unit,
    onLongPress: () -> Unit
) {
    val dateFormat = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
    val dueDateStr = bill.dueDate?.toDate()?.let { dateFormat.format(it) } ?: "No date"

    // Calculate if overdue
    val now = System.currentTimeMillis()
    val isOverdue = bill.dueDate != null &&
            bill.dueDate.toDate().time < now &&
            !bill.isPaid

    // Calculate days until/past due
    val daysDiff = bill.dueDate?.let { dueDate ->
        val diff = dueDate.toDate().time - now
        (diff / (1000 * 60 * 60 * 24)).toInt()
    } ?: 0

    val remindText = when {
        bill.isPaid -> "Paid"
        isOverdue -> "Overdue"
        daysDiff == 0 -> "Due today"
        daysDiff > 0 -> "Due in $daysDiff day${if (daysDiff > 1) "s" else ""}"
        else -> "Overdue by ${kotlin.math.abs(daysDiff)} day${if (kotlin.math.abs(daysDiff) > 1) "s" else ""}"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = { onLongPress() }
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                bill.isPaid -> Color(0xFFD1FAE5)
                isOverdue -> Color(0xFFFFE8E8)
                else -> Color(0xFFE8E8E0)
            }
        ),
        shape = RoundedCornerShape(32.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 3.dp,
            color = Color(0xFF7DDDD3)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bill.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${String.format("%.2f", bill.amount)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7DDDD3)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Due: $dueDateStr",
                        fontSize = 16.sp,
                        color = Color(0xFF6B6B6B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = remindText,
                        fontSize = 14.sp,
                        color = if (isOverdue) Color(0xFFDC2626) else Color(0xFF2D3748),
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Frequency: ${bill.category.capitalize()}",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }

                // Toggle switch
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (bill.isReminderEnabled && !bill.isPaid)
                                Color(0xFF7DDDD3)
                            else
                                Color(0xFF9CA3AF)
                        )
                        .clickable(enabled = !bill.isPaid) {
                            onToggleReminder(!bill.isReminderEnabled)
                        },
                    contentAlignment = if (bill.isReminderEnabled && !bill.isPaid)
                        Alignment.CenterEnd
                    else
                        Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

// Helper function to send notifications to all family members
fun sendBillReminderNotification(bill: BillReminder, context: android.content.Context) {
    val db = FirebaseFirestore.getInstance()

    // Get all family members
    db.collection("families")
        .document(UserSession.familyId)
        .get()
        .addOnSuccessListener { document ->
            val members = document.get("members") as? List<Map<String, Any>> ?: emptyList()

            members.forEach { member ->
                val userId = member["userId"] as? String ?: ""

                // Create notification for each member
                val notificationData = hashMapOf(
                    "type" to "bill_reminder",
                    "billId" to bill.id,
                    "billName" to bill.name,
                    "amount" to bill.amount,
                    "dueDate" to bill.dueDate,
                    "recipientUserId" to userId,
                    "sentAt" to Timestamp.now(),
                    "read" to false
                )

                db.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .add(notificationData)
                    .addOnSuccessListener {
                        Log.d("BillReminder", "Notification sent to $userId")
                    }
            }

            android.widget.Toast.makeText(
                context,
                "Reminders enabled for all family members",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
}

fun getTodayDateString(): String {
    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return dateFormat.format(java.util.Date())
}

fun generateNeetiSukti(
    onSuccess: (String, String) -> Unit,
    onError: (String) -> Unit
) {
    val apiKey = "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"

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = "https://api.groq.com/openai/v1/chat/completions"

            val prompt = """
Generate a single short motivational or inspirational quote in Telugu (one sentence only, maximum 15-20 words).
Then provide its English translation.

Format your response EXACTLY as follows (no extra text):
Telugu: [your Telugu quote here]
English: [English translation here]

Example format:
Telugu: జీవితంలో విజయం సాధించాలంటే, కృషి మరియు నమ్మకం అవసరం.
English: To achieve success in life, hard work and faith are necessary.

IMPORTANT:
- Keep the Telugu quote short, meaningful, and motivational
- Provide accurate English translation
- Follow the exact format shown above
- Do not add any extra explanations or text
            """.trimIndent()

            val jsonObject = JSONObject()
            jsonObject.put("model", "llama-3.3-70b-versatile")

            val messagesArray = JSONArray()
            val messageObject = JSONObject()
            messageObject.put("role", "user")
            messageObject.put("content", prompt)
            messagesArray.put(messageObject)

            jsonObject.put("messages", messagesArray)
            jsonObject.put("temperature", 0.8)
            jsonObject.put("max_tokens", 200)

            val jsonBody = jsonObject.toString()

            val client = OkHttpClient()
            val mediaType = "application/json".toMediaTypeOrNull()
            val requestBody = jsonBody.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val choices = jsonResponse.getJSONArray("choices")

                    if (choices.length() > 0) {
                        val message = choices.getJSONObject(0)
                            .getJSONObject("message")
                        val text = message.getString("content")

                        val teluguLine = text.substringAfter("Telugu:").substringBefore("English:").trim()
                        val englishLine = text.substringAfter("English:").trim()

                        withContext(Dispatchers.Main) {
                            onSuccess(teluguLine, englishLine)
                        }
                    } else {
                        throw Exception("No response from AI")
                    }
                } else {
                    throw Exception("Empty response body")
                }
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                throw Exception("API Error: ${response.code} - $errorBody")
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError(e.message ?: "Unknown error occurred")
                Log.e("NeetiSukti", "Error generating quote", e)
            }
        }
    }
}

fun checkAndGenerateDailyNeetiSukti(
    familyId: String,
    onQuoteLoaded: (NeetiSukti) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val today = getTodayDateString()

    db.collection("families")
        .document(familyId)
        .collection("neetiSukti")
        .document(today)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val neetiSukti = NeetiSukti(
                    teluguText = document.getString("teluguText") ?: "",
                    englishTranslation = document.getString("englishTranslation") ?: "",
                    date = document.getString("date") ?: "",
                    generatedAt = document.getTimestamp("generatedAt")
                )
                onQuoteLoaded(neetiSukti)
            } else {
                generateNeetiSukti(
                    onSuccess = { telugu, english ->
                        val neetiSuktiData = hashMapOf(
                            "teluguText" to telugu,
                            "englishTranslation" to english,
                            "date" to today,
                            "generatedAt" to com.google.firebase.Timestamp.now()
                        )

                        db.collection("families")
                            .document(familyId)
                            .collection("neetiSukti")
                            .document(today)
                            .set(neetiSuktiData)
                            .addOnSuccessListener {
                                val neetiSukti = NeetiSukti(
                                    teluguText = telugu,
                                    englishTranslation = english,
                                    date = today,
                                    generatedAt = com.google.firebase.Timestamp.now()
                                )
                                onQuoteLoaded(neetiSukti)
                                Log.d("NeetiSukti", "New quote generated and saved")
                            }
                    },
                    onError = { error ->
                        Log.e("NeetiSukti", "Error: $error")
                        val fallbackQuote = NeetiSukti(
                            teluguText = "ప్రతి రోజు కొత్త అవకాశాలతో నిండి ఉంటుంది.",
                            englishTranslation = "Every day is filled with new opportunities.",
                            date = today,
                            generatedAt = com.google.firebase.Timestamp.now()
                        )
                        onQuoteLoaded(fallbackQuote)
                    }
                )
            }
        }
        .addOnFailureListener { e ->
            Log.e("NeetiSukti", "Error checking quote", e)
            val fallbackQuote = NeetiSukti(
                teluguText = "ప్రతి రోజు కొత్త అవకాశాలతో నిండి ఉంటుంది.",
                englishTranslation = "Every day is filled with new opportunities.",
                date = today,
                generatedAt = com.google.firebase.Timestamp.now()
            )
            onQuoteLoaded(fallbackQuote)
        }
}

// Neeti Sukti Section Composable
@Composable
fun NeetiSuktiSection(neetiSukti: NeetiSukti, context: android.content.Context) {
    val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8E8E0)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "నేటి సూక్తి",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            val textToCopy = "నేటి సూక్తి\n\n${neetiSukti.teluguText}"
                            val clip = android.content.ClipData.newPlainText("Neeti Sukti", textToCopy)
                            clipboardManager.setPrimaryClip(clip)
                            android.widget.Toast.makeText(
                                context,
                                "నేటి సూక్తి కాపీ చేయబడింది!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_copy),
                            contentDescription = "Copy",
                            tint = Color(0xFF7DDDD3),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val shareText = "నేటి సూక్తి\n\n${neetiSukti.teluguText}"
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = android.content.Intent.createChooser(sendIntent, "నేటి సూక్తి షేర్ చేయండి")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_share),
                            contentDescription = "Share",
                            tint = Color(0xFF7DDDD3),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7DDDD3).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.btn_star_big_on),
                        contentDescription = "Quote",
                        tint = Color(0xFF7DDDD3),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = neetiSukti.teluguText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2D3748),
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFB8B8A8).copy(alpha = 0.3f))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = neetiSukti.englishTranslation,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF6B6B6B),
                        lineHeight = 20.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

// ============== DASHBOARD SCREEN ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToFamilyMembers: () -> Unit,
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToAddExpense: () -> Unit = {},
    onNavigateToGroceryList: () -> Unit = {},
    onNavigateToBillReminders: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToLiveLocations: () -> Unit = {},
    onNavigateToAIBots: () -> Unit = {}
) {
    val context = LocalContext.current
    var familyName by remember { mutableStateOf("Loading...") }
    var familyNumber by remember { mutableStateOf("") }
    var familyMembers by remember { mutableStateOf<List<FamilyMember>>(emptyList()) }
    var userNicknames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var recentExpenses by remember { mutableStateOf<List<ExpenseItem>>(emptyList()) }
    var groceryItems by remember { mutableStateOf<List<GroceryItem>>(emptyList()) }
    var billReminders by remember { mutableStateOf<List<BillReminder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var isSendingAlert by remember { mutableStateOf(false) }
    var neetiSukti by remember { mutableStateOf<NeetiSukti?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()

    BackHandler {
        showExitDialog = true
    }

    LaunchedEffect(Unit) {
        if (UserSession.familyId.isNotEmpty()) {
            // Load family data
            db.collection("families")
                .document(UserSession.familyId)
                .get()
                .addOnSuccessListener { document ->
                    familyName = document.getString("familyName") ?: "My Family"
                    familyNumber = document.getString("familyNumber") ?: ""

                    val members = document.get("members") as? List<Map<String, Any>> ?: emptyList()
                    familyMembers = members.map { member ->
                        FamilyMember(
                            name = member["name"] as? String ?: "",
                            role = member["role"] as? String ?: "",
                            userId = member["userId"] as? String ?: ""
                        )
                    }
                    isLoading = false
                }

            // Load user's nicknames
            db.collection("users")
                .document(UserSession.userId)
                .collection("nicknames")
                .get()
                .addOnSuccessListener { snapshot ->
                    val nicknames = mutableMapOf<String, String>()
                    snapshot.documents.forEach { doc ->
                        val targetUserId = doc.getString("targetUserId") ?: ""
                        val nickname = doc.getString("nickname") ?: ""
                        if (targetUserId.isNotEmpty() && nickname.isNotEmpty()) {
                            nicknames[targetUserId] = nickname
                        }
                    }
                    userNicknames = nicknames
                }

            // Load recent expenses (5 most recent)
            db.collection("families")
                .document(UserSession.familyId)
                .collection("expenses")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener { snapshot, error ->
                    recentExpenses = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            ExpenseItem(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                amount = doc.getDouble("amount") ?: 0.0,
                                category = doc.getString("category") ?: "",
                                type = doc.getString("type") ?: "outgoing",
                                date = doc.getTimestamp("date"),
                                addedBy = doc.getString("addedBy") ?: "",
                                addedByName = doc.getString("addedByName") ?: "",
                                notes = doc.getString("notes") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                }

            // Load grocery items (5 most recent)
            db.collection("families")
                .document(UserSession.familyId)
                .collection("groceryList")
                .orderBy("addedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener { snapshot, error ->
                    groceryItems = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            GroceryItem(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                isChecked = doc.getBoolean("isChecked") ?: false,
                                imageUrl = doc.getString("imageUrl") ?: "",
                                addedBy = doc.getString("addedBy") ?: "",
                                addedAt = doc.getTimestamp("addedAt")
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                }

            // Load bill reminders (5 nearest upcoming)
            db.collection("families")
                .document(UserSession.familyId)
                .collection("billReminders")
                .orderBy("dueDate", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .limit(5)
                .addSnapshotListener { snapshot, error ->
                    billReminders = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            BillReminder(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                amount = doc.getDouble("amount") ?: 0.0,
                                dueDate = doc.getTimestamp("dueDate"),
                                remindDaysBefore = doc.getLong("remindDaysBefore")?.toInt() ?: 1,
                                isReminderEnabled = doc.getBoolean("isReminderEnabled") ?: true,
                                isPaid = doc.getBoolean("isPaid") ?: false,
                                addedBy = doc.getString("addedBy") ?: "",
                                addedAt = doc.getTimestamp("addedAt"),
                                category = doc.getString("frequency") ?: "monthly"
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                }

            // NEW: Load or generate today's Neeti Sukti
            checkAndGenerateDailyNeetiSukti(UserSession.familyId) { quote ->
                neetiSukti = quote
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F0))
                .padding(16.dp)
        ) {
            // Header with hamburger menu and profile
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isDrawerOpen = true },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_sort_by_size),
                            contentDescription = "Menu",
                            tint = Color(0xFF6A11CB),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = familyName,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B6B6B)
                        )
                        Text(
                            text = familyNumber,
                            fontSize = 18.sp,
                            color = Color(0xFF2D3748)
                        )
                    }

                    var currentUserProfileUri by remember { mutableStateOf("") }

                    LaunchedEffect(UserSession.userId) {
                        loadProfilePicture(UserSession.userId) { uri ->
                            currentUserProfileUri = uri
                        }
                    }

                    Box(
                        modifier = Modifier.clickable {
                            onNavigateToProfile()  // This will navigate to profile page
                        }
                    ) {
                        ProfileImage(
                            userId = UserSession.userId,
                            size = 56.dp,
                            borderWidth = 2.dp,
                            borderColor = Color.White,
                            backgroundColor = Color(0xFF7DDDD3),
                            iconSize = 32.dp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // RED ALERT BUTTON (BIG)
            item {
                Card(
                    onClick = {
                        if (!isSendingAlert) {
                            isSendingAlert = true

                            db.collection("families")
                                .document(UserSession.familyId)
                                .get()
                                .addOnSuccessListener { document ->
                                    val members = document.get("members") as? List<Map<String, Any>> ?: emptyList()

                                    members.forEach { member ->
                                        val userId = member["userId"] as? String ?: ""

                                        if (userId != UserSession.userId) {
                                            db.collection("users")
                                                .document(userId)
                                                .get()
                                                .addOnSuccessListener { userDoc ->
                                                    val fcmToken = userDoc.getString("fcmToken")

                                                    if (fcmToken != null) {
                                                        val fcmRequest = hashMapOf(
                                                            "token" to fcmToken,
                                                            "title" to "🚨 RED ALERT",
                                                            "body" to "${UserSession.userName} pressed Red Alert and needs help!",
                                                            "type" to "red_alert",
                                                            "senderUserId" to UserSession.userId,
                                                            "senderName" to UserSession.userName,
                                                            "createdAt" to Timestamp.now()
                                                        )

                                                        db.collection("fcm_requests")
                                                            .add(fcmRequest)
                                                            .addOnSuccessListener {
                                                                Log.d("RedAlert", "FCM request created for $userId")
                                                            }
                                                    }

                                                    val notificationData = hashMapOf(
                                                        "type" to "red_alert",
                                                        "senderUserId" to UserSession.userId,
                                                        "senderName" to UserSession.userName,
                                                        "message" to "${UserSession.userName} pressed Red Alert and needs help!",
                                                        "sentAt" to Timestamp.now(),
                                                        "read" to false
                                                    )

                                                    db.collection("users")
                                                        .document(userId)
                                                        .collection("notifications")
                                                        .add(notificationData)
                                                        .addOnSuccessListener {
                                                            Log.d("RedAlert", "Notification sent to $userId")
                                                        }
                                                }
                                        }
                                    }

                                    isSendingAlert = false
                                    android.widget.Toast.makeText(
                                        context,
                                        "🚨 Red Alert sent to all family members!",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                                .addOnFailureListener { e ->
                                    isSendingAlert = false
                                    android.widget.Toast.makeText(
                                        context,
                                        "Failed to send alert: ${e.message}",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSendingAlert) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(40.dp)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_dialog_alert),
                                    contentDescription = "Red Alert",
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "RED ALERT",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // NEW: NEETI SUKTI SECTION (నేటి సూక్తి)
            item {
                neetiSukti?.let { quote ->
                    NeetiSuktiSection(neetiSukti = quote, context = context)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // FAMILY MEMBERS SECTION
            item {
                SectionCard(
                    title = "Family Members",
                    onViewAllClick = onNavigateToFamilyMembers
                ) {
                    if (familyMembers.isEmpty()) {
                        Text(
                            text = "No family members yet",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            items(familyMembers) { member ->
                                val displayName = if (member.userId == UserSession.userId) {
                                    "Me"
                                } else {
                                    userNicknames[member.userId] ?: member.name
                                }
                                MemberAvatar(
                                    member = member,
                                    displayName = displayName
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // RECENT EXPENSES SECTION
            item {
                SectionCard(
                    title = "Recent Expenses",
                    onViewAllClick = onNavigateToExpenses
                ) {
                    if (recentExpenses.isEmpty()) {
                        Text(
                            text = "No expenses yet",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            recentExpenses.forEach { expense ->
                                RecentExpenseItem(expense = expense)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // GROCERIES TO BUY SECTION
            item {
                SectionCard(
                    title = "Groceries To Buy",
                    onViewAllClick = onNavigateToGroceryList
                ) {
                    if (groceryItems.isEmpty()) {
                        Text(
                            text = "No grocery items yet",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            groceryItems.forEach { item ->
                                GroceryItemRow(item = item)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // BILL REMINDERS SECTION
            item {
                SectionCard(
                    title = "Bill Reminders",
                    onViewAllClick = onNavigateToBillReminders
                ) {
                    if (billReminders.isEmpty()) {
                        Text(
                            text = "No bill reminders yet",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            billReminders.forEach { bill ->
                                BillReminderRow(bill = bill)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Animated overlay
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isDrawerOpen = false }
            )
        }

        // Sliding drawer with animation
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.TopStart)
            ) {
                DrawerContent(
                    isDrawerOpen = isDrawerOpen,
                    onDrawerClose = { isDrawerOpen = false },
                    onNavigateToFamilyMembers = {
                        isDrawerOpen = false
                        onNavigateToFamilyMembers()
                    },
                    onNavigateToExpenses = {
                        isDrawerOpen = false
                        onNavigateToExpenses()
                    },
                    onNavigateToGroceryList = {
                        isDrawerOpen = false
                        onNavigateToGroceryList()
                    },
                    onNavigateToBillReminders = {
                        isDrawerOpen = false
                        onNavigateToBillReminders()
                    },
                    onNavigateToLiveLocations = {
                        isDrawerOpen = false
                        onNavigateToLiveLocations()
                    },
                    onNavigateToAIBots = {
                        isDrawerOpen = false
                        onNavigateToAIBots()
                    }
                )
            }
        }
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Exit App") },
                text = { Text("Are you sure you want to exit the app?") },
                confirmButton = {
                    Button(
                        onClick = {
                            // Exit the app
                            (context as? Activity)?.finishAffinity()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        )
                    ) {
                        Text("Exit")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showExitDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// ============== SUPPORTING COMPOSABLES (Keep your existing ones) ==============

@Composable
fun SectionCard(
    title: String,
    onViewAllClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8E8E0)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )

                TextButton(
                    onClick = onViewAllClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF7DDDD3)
                    )
                ) {
                    Text(
                        text = "View All",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_view),
                        contentDescription = "View All",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            content()
        }
    }
}

@Composable
fun MemberAvatar(member: FamilyMember, displayName: String = member.name) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ProfileImage(
            userId = member.userId,
            size = 60.dp,
            backgroundColor = Color(0xFFB8B8A8)
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = displayName,
            fontSize = 12.sp,
            color = Color(0xFF2D3748),
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 70.dp)
        )
    }
}

@Composable
fun RecentExpenseItem(expense: ExpenseItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = expense.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2D3748),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "₹${String.format("%.2f", expense.amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (expense.type == "incoming") Color(0xFF10B981) else Color(0xFFEF4444)
        )
    }
}

@Composable
fun GroceryItemRow(item: GroceryItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(
                    width = 2.dp,
                    color = if (item.isChecked) Color(0xFF7DDDD3) else Color(0xFF6B6B6B),
                    shape = RoundedCornerShape(4.dp)
                )
                .background(
                    if (item.isChecked) Color(0xFF7DDDD3) else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            if (item.isChecked) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = "Checked",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = item.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2D3748),
            textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
            style = androidx.compose.ui.text.TextStyle(
                color = if (item.isChecked) Color(0xFF9CA3AF) else Color(0xFF2D3748)
            )
        )
    }
}

@Composable
fun BillReminderRow(bill: BillReminder) {
    val dateFormat = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
    val dueDateStr = bill.dueDate?.toDate()?.let { dateFormat.format(it) } ?: "No date"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = bill.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2D3748),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "₹${String.format("%.2f", bill.amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEF4444)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = dueDateStr,
            fontSize = 12.sp,
            color = Color(0xFF6B6B6B)
        )
    }
}

@Composable
fun DrawerContent(
    isDrawerOpen: Boolean,
    onDrawerClose: () -> Unit,
    onNavigateToFamilyMembers: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToGroceryList: () -> Unit,
    onNavigateToBillReminders: () -> Unit,
    onNavigateToLiveLocations: () -> Unit,
    onNavigateToAIBots: () -> Unit
) {
    Column(  // ✅ Everything should be INSIDE this Column
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color(0xFFF5F5F0))
            .padding(vertical = 24.dp)
    ) {
        // Header with Menu title and close button
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Menu",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748)
            )
            IconButton(onClick = onDrawerClose) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                    contentDescription = "Close",
                    tint = Color(0xFF6A11CB),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Menu items
        DrawerMenuItem(android.R.drawable.ic_menu_myplaces, "Family Members Data", onNavigateToFamilyMembers)
        DrawerMenuItem(android.R.drawable.ic_menu_info_details, "Expenses", onNavigateToExpenses)
        DrawerMenuItem(android.R.drawable.ic_menu_agenda, "Groceries List", onNavigateToGroceryList)
        DrawerMenuItem(android.R.drawable.ic_menu_today, "Bill Reminders", onNavigateToBillReminders)
        DrawerMenuItem(android.R.drawable.ic_dialog_map, "Live Locations", onNavigateToLiveLocations)
        DrawerMenuItem(android.R.drawable.btn_star_big_on, "AI Bots", onNavigateToAIBots)

        Spacer(modifier = Modifier.weight(1f))

    }
}

// ============== DRAWER MENU ITEM (Keep this as is) ==============
@Composable
fun DrawerMenuItem(icon: Int, title: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8E8E0)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF7DDDD3)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2D3748)
            )
        }
    }
}

// ============== LIVE LOCATIONS SCREEN ==============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveLocationsScreen(
    onBackClick: () -> Unit
) {
    var memberLocations by remember { mutableStateOf<List<LocationData>>(emptyList()) }
    var userNicknames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }  // ← NEW
    var isLoading by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Load member locations AND nicknames
    LaunchedEffect(Unit) {
        if (UserSession.familyId.isNotEmpty()) {
            // ← NEW: Load user's nicknames for other members
            db.collection("users")
                .document(UserSession.userId)
                .collection("nicknames")
                .get()
                .addOnSuccessListener { snapshot ->
                    val nicknames = mutableMapOf<String, String>()
                    snapshot.documents.forEach { doc ->
                        val targetUserId = doc.getString("targetUserId") ?: ""
                        val nickname = doc.getString("nickname") ?: ""
                        if (targetUserId.isNotEmpty() && nickname.isNotEmpty()) {
                            nicknames[targetUserId] = nickname
                        }
                    }
                    userNicknames = nicknames
                }

            db.collection("families")
                .document(UserSession.familyId)
                .collection("memberLocations")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("LiveLocations", "Error loading locations", error)
                        isLoading = false
                        return@addSnapshotListener
                    }

                    memberLocations = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            LocationData(
                                userId = doc.getString("userId") ?: "",
                                userName = doc.getString("userName") ?: "",
                                latitude = doc.getDouble("latitude") ?: 0.0,
                                longitude = doc.getDouble("longitude") ?: 0.0,
                                batteryLevel = doc.getLong("batteryLevel")?.toInt() ?: 0,
                                isOnline = doc.getBoolean("isOnline") ?: false,
                                isNotificationEnabled = doc.getBoolean("isNotificationEnabled") ?: true,
                                lastUpdated = doc.getTimestamp("lastUpdated")
                            )
                        } catch (e: Exception) {
                            Log.e("LiveLocations", "Error parsing location", e)
                            null
                        }
                    } ?: emptyList()

                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))
            .padding(16.dp)
    ) {
        // Back button
        OutlinedButton(
            onClick = onBackClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF6A11CB)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color(0xFF6A11CB)
            )
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_revert),
                contentDescription = "Back",
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Back",
                modifier = Modifier.padding(start = 8.dp),
                color = Color(0xFF6A11CB)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "Live Locations",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B6B6B),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF7DDDD3))
            }
        } else if (memberLocations.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8E8E0)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_dialog_map),
                        contentDescription = "No locations",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No location data yet",
                        fontSize = 18.sp,
                        color = Color(0xFF9CA3AF),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Family members need to enable Live Location in their profile",
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(memberLocations) { location ->
                    // ← UPDATED: Pass nickname to card
                    val displayName = if (location.userId == UserSession.userId) {
                        "Me"
                    } else {
                        userNicknames[location.userId] ?: location.userName
                    }

                    LiveLocationMemberCard(
                        location = location,
                        displayName = displayName,  // ← NEW
                        context = context
                    )
                }
            }
        }
    }
}

// ← UPDATED: Live Location Member Card with displayName parameter
@Composable
fun LiveLocationMemberCard(
    location: LocationData,
    displayName: String,  // ← NEW parameter
    context: android.content.Context
) {
    Card(
        onClick = {
            if (location.isOnline && location.latitude != 0.0 && location.longitude != 0.0) {
                // Open in Google Maps
                val uri = "geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}($displayName)"
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri))
                intent.setPackage("com.google.android.apps.maps")

                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // If Google Maps not installed, use browser
                    val browserUri = "https://www.google.com/maps?q=${location.latitude},${location.longitude}"
                    val browserIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(browserUri))
                    context.startActivity(browserIntent)
                }
            } else {
                android.widget.Toast.makeText(
                    context,
                    "$displayName is currently offline",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8E8E0)
        ),
        shape = RoundedCornerShape(40.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Avatar
            var locationProfileUri by remember { mutableStateOf("") }

            LaunchedEffect(location.userId) {
                loadProfilePicture(location.userId) { uri ->
                    locationProfileUri = uri
                }
            }

            ProfileImage(
                userId = location.userId,
                size = 60.dp,
                backgroundColor = if (location.isOnline) Color(0xFF7DDDD3) else Color(0xFFB8B8A8)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Name (showing nickname or "Me")
            Text(
                text = displayName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2D3748),
                modifier = Modifier.weight(1f)
            )

            // Status badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (location.isOnline)
                            Color(0xFF10B981).copy(alpha = 0.2f)
                        else
                            Color(0xFFEF4444).copy(alpha = 0.2f)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (location.isOnline) "Online" else "Offline",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (location.isOnline) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }
        }
    }
}

// ============== LOGIN SCREEN ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }  // ← NEW: Password visibility toggle
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Get sessionManager from LocalContext
    val sessionManager = remember { SessionManager(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 20.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF6A11CB)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color(0xFF6A11CB)
            )
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_revert),
                contentDescription = "Back",
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Back",
                modifier = Modifier.padding(start = 8.dp),
                color = Color(0xFF6A11CB)
            )
        }

        Text(
            text = "Login",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(60.dp))

        // ← CHANGED: Phone number field only accepts numbers
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                if (it.all { char -> char.isDigit() }) {  // Only allow digits
                    phoneNumber = it
                }
            },
            placeholder = { Text("Phone number", color = Color(0xFF9CA3AF)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8E8E0),
                unfocusedContainerColor = Color(0xFFE8E8E0),
                focusedIndicatorColor = Color(0xFF7DDDD3),
                unfocusedIndicatorColor = Color(0xFFB8B8A8),
                cursorColor = Color(0xFF7DDDD3)
            ),
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)  // Number keyboard
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ← CHANGED: Password field with eye icon
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password", color = Color(0xFF9CA3AF)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8E8E0),
                unfocusedContainerColor = Color(0xFFE8E8E0),
                focusedIndicatorColor = Color(0xFF7DDDD3),
                unfocusedIndicatorColor = Color(0xFFB8B8A8),
                cursorColor = Color(0xFF7DDDD3)
            ),
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {  // ← NEW: Eye icon
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible)
                                android.R.drawable.ic_menu_view
                            else
                                android.R.drawable.ic_menu_close_clear_cancel
                        ),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Forgot password?",
            fontSize = 16.sp,
            color = Color(0xFF7DDDD3),
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onForgotPasswordClick() }
        )

        Spacer(modifier = Modifier.height(40.dp))

        if (errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFEE2E2)
                )
            ) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = {
                if (phoneNumber.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill in all fields"
                    return@Button
                }

                errorMessage = ""
                isLoading = true

                val formattedPhone = if (!phoneNumber.startsWith("+")) {
                    "+91$phoneNumber"
                } else {
                    phoneNumber
                }

                db.collection("users")
                    .whereEqualTo("phoneNumber", formattedPhone)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            isLoading = false
                            errorMessage = "No account found with this phone number"
                            return@addOnSuccessListener
                        }

                        val userDoc = documents.documents[0]
                        val storedPassword = userDoc.getString("password") ?: ""
                        val hashedInputPassword = hashPassword(password)

                        if (storedPassword == hashedInputPassword) {
                            val userId = userDoc.id
                            val userName = userDoc.getString("name") ?: ""
                            val familyId = userDoc.getString("familyId") ?: ""

                            UserSession.userId = userId
                            UserSession.userName = userName
                            UserSession.phoneNumber = formattedPhone
                            UserSession.familyId = familyId

                            // SAVE SESSION using coroutine scope
                            coroutineScope.launch {
                                sessionManager.saveSession(userId, userName, formattedPhone, familyId)
                                isLoading = false
                                onLoginSuccess(familyId.isNotEmpty())
                            }
                        } else {
                            isLoading = false
                            errorMessage = "Incorrect password"
                        }
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        errorMessage = "Login failed: ${e.message}"
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7DDDD3),
                disabledContainerColor = Color(0xFFB8B8A8)
            ),
            shape = RoundedCornerShape(28.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Login",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New here? ",
                fontSize = 16.sp,
                color = Color(0xFF6B6B6B)
            )
            Text(
                text = "Register now.",
                fontSize = 16.sp,
                color = Color(0xFF7DDDD3),
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onRegisterClick() }
            )
        }
    }
}

// ============== UPDATE CREATE FAMILY SCREEN ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFamilyScreen(
    onBackClick: () -> Unit,
    onFamilyCreated: () -> Unit
) {
    var familyName by remember { mutableStateOf("") }
    val creatorName = UserSession.userName
    var secretCode by remember { mutableStateOf("") }
    var familyNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isCreated by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.Start),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF6A11CB)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color(0xFF6A11CB)
            )
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_revert),
                contentDescription = "Back",
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Back",
                modifier = Modifier.padding(start = 8.dp),
                color = Color(0xFF6A11CB)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Create Family",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = familyName,
            onValueChange = { familyName = it },
            label = { Text("Family Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color(0xFF6A11CB),
                unfocusedIndicatorColor = Color(0xFFCBD5E0),
                focusedLabelColor = Color(0xFF6A11CB),
                unfocusedLabelColor = Color(0xFF718096)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isCreated
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = creatorName,
            onValueChange = { },
            label = { Text("Creator Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                disabledContainerColor = Color(0xFFF3F4F6),
                disabledIndicatorColor = Color(0xFFCBD5E0),
                disabledLabelColor = Color(0xFF9CA3AF),
                disabledTextColor = Color(0xFF6B7280)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = false
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ← NEW: Secret Code Field
        OutlinedTextField(
            value = secretCode,
            onValueChange = { secretCode = it },
            label = { Text("Secret Code") },
            placeholder = { Text("Enter a secret code", color = Color(0xFF9CA3AF)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color(0xFF6A11CB),
                unfocusedIndicatorColor = Color(0xFFCBD5E0),
                focusedLabelColor = Color(0xFF6A11CB),
                unfocusedLabelColor = Color(0xFF718096)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isCreated,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isCreated && familyNumber.isNotEmpty())
                    Color(0xFF7DDDD3)
                else
                    Color(0xFFE6E6FA)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Family Number",
                    fontSize = 16.sp,
                    color = if (isCreated) Color(0xFF2D3748) else Color(0xFF718096),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isCreated && familyNumber.isNotEmpty()) {
                    Text(
                        text = familyNumber,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Others can join using this family number.",
                        fontSize = 14.sp,
                        color = Color(0xFF2D3748),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Will be generated after creation",
                        fontSize = 18.sp,
                        color = Color(0xFFA0AEC0),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFEE2E2)
                )
            ) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = {
                if (familyName.isBlank()) {
                    errorMessage = "Please enter family name"
                    return@Button
                }

                if (creatorName.isBlank()) {
                    errorMessage = "Creator name not found. Please login again."
                    return@Button
                }

                // ← NEW: Validate secret code
                if (secretCode.isBlank()) {
                    errorMessage = "Please enter a secret code"
                    return@Button
                }

                if (secretCode.length < 4) {
                    errorMessage = "Secret code must be at least 4 characters"
                    return@Button
                }

                errorMessage = ""
                isLoading = true

                val newFamilyNumber = generateUniqueFamilyNumber()

                val familyData = hashMapOf(
                    "familyName" to familyName,
                    "creatorName" to creatorName,
                    "familyNumber" to newFamilyNumber,
                    "secretCode" to secretCode,
                    "createdAt" to Timestamp.now(),
                    "memberCount" to 1,
                    "members" to listOf(
                        hashMapOf(
                            "name" to creatorName,
                            "role" to "creator",
                            "joinedAt" to Timestamp.now(),
                            "userId" to UserSession.userId
                        )
                    )
                )

                db.collection("families")
                    .document(newFamilyNumber)
                    .set(familyData)
                    .addOnSuccessListener {
                        // Update user's familyId
                        db.collection("users")
                            .document(UserSession.userId)
                            .update("familyId", newFamilyNumber)
                            .addOnSuccessListener {
                                UserSession.familyId = newFamilyNumber
                                isLoading = false
                                isCreated = true
                                familyNumber = newFamilyNumber

                                // Navigate to dashboard after 2 seconds
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    onFamilyCreated()
                                }, 2000)
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                errorMessage = "Failed to update user: ${e.message}"
                            }
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        errorMessage = "Failed to create family: ${e.message}"
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading && !isCreated,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCreated) Color(0xFF10B981) else Color(0xFF6A11CB),
                disabledContainerColor = Color(0xFFCBD5E0)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = " Creating...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.White
                )
            } else if (isCreated) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_save),
                    contentDescription = "Created",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = " Family Created",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = "Create Family",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (!isCreated) {
            Text(
                text = "A unique family number will be generated for you",
                fontSize = 14.sp,
                color = Color(0xFF718096),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ============== WELCOME SCREEN ==============
@Composable
fun WelcomeScreen(
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo - simple icon without circle background
            Icon(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(180.dp),
                tint = Color.Unspecified  // IMPORTANT: This preserves the original colors of your logo
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "MY HOME",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your home vault\nfor every document.",
                fontSize = 18.sp,
                color = Color(0xFF6B6B6B),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(80.dp))

            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7DDDD3)
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Register",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF6A11CB)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    color = Color(0xFF6A11CB)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Login",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6A11CB)
                )
            }
        }
    }
}

// ============== REGISTER SCREEN - UPDATED ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onOtpSent: (String, String, String, String) -> Unit,
    onLoginClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }  // ← NEW
    var confirmPasswordVisible by remember { mutableStateOf(false) }  // ← NEW
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val auth = Firebase.auth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 20.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF6A11CB)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color(0xFF6A11CB)
            )
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_revert),
                contentDescription = "Back",
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Back",
                modifier = Modifier.padding(start = 8.dp),
                color = Color(0xFF6A11CB)
            )
        }

        Text(
            text = "Register",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(60.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Name", color = Color(0xFF9CA3AF)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8E8E0),
                unfocusedContainerColor = Color(0xFFE8E8E0),
                focusedIndicatorColor = Color(0xFF7DDDD3),
                unfocusedIndicatorColor = Color(0xFFB8B8A8),
                cursorColor = Color(0xFF7DDDD3)
            ),
            shape = RoundedCornerShape(28.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ← CHANGED: Phone number field only accepts numbers
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                if (it.all { char -> char.isDigit() }) {
                    phoneNumber = it
                }
            },
            placeholder = { Text("Phone number", color = Color(0xFF9CA3AF)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8E8E0),
                unfocusedContainerColor = Color(0xFFE8E8E0),
                focusedIndicatorColor = Color(0xFF7DDDD3),
                unfocusedIndicatorColor = Color(0xFFB8B8A8),
                cursorColor = Color(0xFF7DDDD3)
            ),
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ← CHANGED: Password field with eye icon
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password", color = Color(0xFF9CA3AF)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8E8E0),
                unfocusedContainerColor = Color(0xFFE8E8E0),
                focusedIndicatorColor = Color(0xFF7DDDD3),
                unfocusedIndicatorColor = Color(0xFFB8B8A8),
                cursorColor = Color(0xFF7DDDD3)
            ),
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible)
                                android.R.drawable.ic_menu_view
                            else
                                android.R.drawable.ic_menu_close_clear_cancel
                        ),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ← CHANGED: Confirm password field with eye icon
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = { Text("Confirm password", color = Color(0xFF9CA3AF)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8E8E0),
                unfocusedContainerColor = Color(0xFFE8E8E0),
                focusedIndicatorColor = Color(0xFF7DDDD3),
                unfocusedIndicatorColor = Color(0xFFB8B8A8),
                cursorColor = Color(0xFF7DDDD3)
            ),
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (confirmPasswordVisible)
                                android.R.drawable.ic_menu_view
                            else
                                android.R.drawable.ic_menu_close_clear_cancel
                        ),
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(40.dp))

        if (errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFEE2E2)
                )
            ) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = {
                when {
                    name.isBlank() -> errorMessage = "Please enter your name"
                    phoneNumber.isBlank() -> errorMessage = "Please enter phone number"
                    phoneNumber.length < 10 -> errorMessage = "Phone number must be at least 10 digits"
                    password.isBlank() -> errorMessage = "Please enter password"
                    password.length < 6 -> errorMessage = "Password must be at least 6 characters"
                    password != confirmPassword -> errorMessage = "Passwords do not match"
                    else -> {
                        errorMessage = ""
                        isLoading = true

                        val formattedPhone = if (!phoneNumber.startsWith("+")) {
                            "+91$phoneNumber"
                        } else {
                            phoneNumber
                        }

                        val db = FirebaseFirestore.getInstance()
                        db.collection("users")
                            .whereEqualTo("phoneNumber", formattedPhone)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (!documents.isEmpty) {
                                    isLoading = false
                                    errorMessage = "Phone number already registered"
                                    return@addOnSuccessListener
                                }

                                val userId = java.util.UUID.randomUUID().toString()
                                val hashedPassword = hashPassword(password)

                                val userData = hashMapOf(
                                    "name" to name,
                                    "phoneNumber" to formattedPhone,
                                    "password" to hashedPassword,
                                    "createdAt" to Timestamp.now(),
                                    "familyId" to ""
                                )

                                db.collection("users")
                                    .document(userId)
                                    .set(userData)
                                    .addOnSuccessListener {
                                        Log.d("Register", "User registered successfully")
                                        UserSession.userId = userId
                                        UserSession.userName = name
                                        UserSession.phoneNumber = formattedPhone
                                        UserSession.familyId = ""
                                        isLoading = false

                                        onOtpSent(formattedPhone, name, password, "")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Register", "Registration failed", e)
                                        isLoading = false
                                        errorMessage = "Registration failed: ${e.message}"
                                    }
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                errorMessage = "Error checking phone number: ${e.message}"
                            }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7DDDD3),
                disabledContainerColor = Color(0xFFB8B8A8)
            ),
            shape = RoundedCornerShape(28.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Register",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                fontSize = 16.sp,
                color = Color(0xFF6B6B6B)
            )
            Text(
                text = "Login",
                fontSize = 16.sp,
                color = Color(0xFF7DDDD3),
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onLoginClick() }
            )
        }
    }
}

// ============== FORGOT PASSWORD SCREEN - UPDATED ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onOtpSent: (String, String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val auth = Firebase.auth
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))  // CHANGED: Consistent background
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Forgot Password",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748),  // CHANGED: Consistent dark text
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter your phone number to receive an OTP",
            fontSize = 16.sp,
            color = Color(0xFF6B6B6B),  // CHANGED: Consistent gray
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(60.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            placeholder = { Text("Phone number", color = Color(0xFF9CA3AF)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8E8E0),  // CHANGED
                unfocusedContainerColor = Color(0xFFE8E8E0),
                focusedIndicatorColor = Color(0xFF7DDDD3),
                unfocusedIndicatorColor = Color(0xFFB8B8A8),
                cursorColor = Color(0xFF7DDDD3)
            ),
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(modifier = Modifier.height(40.dp))

        if (errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFEE2E2)
                )
            ) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = {
                if (phoneNumber.isBlank()) {
                    errorMessage = "Please enter phone number"
                    return@Button
                }

                errorMessage = ""
                isLoading = true

                val formattedPhone = if (!phoneNumber.startsWith("+")) {
                    "+91$phoneNumber"
                } else {
                    phoneNumber
                }

                db.collection("users")
                    .whereEqualTo("phoneNumber", formattedPhone)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            isLoading = false
                            errorMessage = "No account found with this phone number"
                            return@addOnSuccessListener
                        }

                        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                isLoading = false
                            }

                            override fun onVerificationFailed(e: FirebaseException) {
                                isLoading = false
                                errorMessage = "Verification failed: ${e.message}"
                            }

                            override fun onCodeSent(
                                verificationId: String,
                                token: PhoneAuthProvider.ForceResendingToken
                            ) {
                                isLoading = false
                                onOtpSent(formattedPhone, verificationId)
                            }
                        }

                        val options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(formattedPhone)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(context as ComponentActivity)
                            .setCallbacks(callbacks)
                            .build()

                        PhoneAuthProvider.verifyPhoneNumber(options)
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        errorMessage = "Error: ${e.message}"
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7DDDD3),
                disabledContainerColor = Color(0xFFB8B8A8)
            ),
            shape = RoundedCornerShape(28.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Send OTP",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onBackClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF6A11CB)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color(0xFF6A11CB)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(text = "Back to Login", color = Color(0xFF6A11CB))
        }
    }
}

// ============== VERIFY FORGOT PASSWORD OTP SCREEN - UPDATED ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyForgotPasswordOtpScreen(
    phoneNumber: String,
    verificationId: String,
    onBackClick: () -> Unit,
    onVerificationSuccess: () -> Unit
) {
    var otp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val auth = Firebase.auth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))  // CHANGED: Consistent background
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Verify OTP",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748),  // CHANGED: Consistent dark text
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter the OTP sent to\n$phoneNumber",
            fontSize = 16.sp,
            color = Color(0xFF6B6B6B),  // CHANGED: Consistent gray
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(60.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 6) otp = it },
            placeholder = { Text("Enter 6-digit OTP", color = Color(0xFF9CA3AF)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8E8E0),  // CHANGED
                unfocusedContainerColor = Color(0xFFE8E8E0),
                focusedIndicatorColor = Color(0xFF7DDDD3),
                unfocusedIndicatorColor = Color(0xFFB8B8A8),
                cursorColor = Color(0xFF7DDDD3)
            ),
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(40.dp))

        if (errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFEE2E2)
                )
            ) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = {
                if (otp.length != 6) {
                    errorMessage = "Please enter 6-digit OTP"
                    return@Button
                }

                errorMessage = ""
                isLoading = true

                val credential = PhoneAuthProvider.getCredential(verificationId, otp)

                auth.signInWithCredential(credential)
                    .addOnSuccessListener {
                        Log.d("VerifyOTP", "OTP verified successfully")
                        isLoading = false
                        onVerificationSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e("VerifyOTP", "Verification failed", e)
                        isLoading = false
                        errorMessage = "Invalid OTP. Please try again."
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7DDDD3),
                disabledContainerColor = Color(0xFFB8B8A8)
            ),
            shape = RoundedCornerShape(28.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Verify",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onBackClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF6A11CB)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color(0xFF6A11CB)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(text = "Back", color = Color(0xFF6A11CB))
        }
    }
}

// ============== RESET PASSWORD SCREEN - UPDATED ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    phoneNumber: String,
    onBackClick: () -> Unit,
    onPasswordReset: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))  // CHANGED: Consistent background
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Reset Password",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748),  // CHANGED: Consistent dark text
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(60.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            placeholder = { Text("New Password", color = Color(0xFF9CA3AF)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8E8E0),  // CHANGED
                unfocusedContainerColor = Color(0xFFE8E8E0),
                focusedIndicatorColor = Color(0xFF7DDDD3),
                unfocusedIndicatorColor = Color(0xFFB8B8A8),
                cursorColor = Color(0xFF7DDDD3)
            ),
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = { Text("Confirm Password", color = Color(0xFF9CA3AF)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8E8E0),  // CHANGED
                unfocusedContainerColor = Color(0xFFE8E8E0),
                focusedIndicatorColor = Color(0xFF7DDDD3),
                unfocusedIndicatorColor = Color(0xFFB8B8A8),
                cursorColor = Color(0xFF7DDDD3)
            ),
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(40.dp))

        if (errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFEE2E2)
                )
            ) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = {
                when {
                    newPassword.isBlank() -> errorMessage = "Please enter new password"
                    newPassword.length < 6 -> errorMessage = "Password must be at least 6 characters"
                    newPassword != confirmPassword -> errorMessage = "Passwords do not match"
                    else -> {
                        errorMessage = ""
                        isLoading = true

                        val hashedPassword = hashPassword(newPassword)

                        db.collection("users")
                            .whereEqualTo("phoneNumber", phoneNumber)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (documents.isEmpty) {
                                    isLoading = false
                                    errorMessage = "User not found"
                                    return@addOnSuccessListener
                                }

                                val userId = documents.documents[0].id

                                db.collection("users")
                                    .document(userId)
                                    .update("password", hashedPassword)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        onPasswordReset()
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = "Failed to reset password: ${e.message}"
                                    }
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                errorMessage = "Error: ${e.message}"
                            }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7DDDD3),
                disabledContainerColor = Color(0xFFB8B8A8)
            ),
            shape = RoundedCornerShape(28.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Reset Password",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onBackClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF6A11CB)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color(0xFF6A11CB)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(text = "Cancel", color = Color(0xFF6A11CB))
        }
    }
}

// ============== CREATE OR JOIN FAMILY SCREEN - UPDATED ==============
@Composable
fun CreateOrJoinFamilyScreen(
    onBackClick: () -> Unit,
    onCreateFamilyClick: () -> Unit,
    onJoinFamilyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))  // Already consistent
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.Start),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF6A11CB)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color(0xFF6A11CB)
            )
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_revert),
                contentDescription = "Back",
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Logout",
                modifier = Modifier.padding(start = 8.dp),
                color = Color(0xFF6A11CB)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Create or Join Family",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You can belong to only one family",
            fontSize = 16.sp,
            color = Color(0xFF718096),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(60.dp))

        Card(
            onClick = onCreateFamilyClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8E8E0)  // CHANGED: Consistent card color
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_add),
                    contentDescription = "Create Family",
                    tint = Color(0xFF7DDDD3),  // CHANGED: Teal accent
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Create New Family",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2D3748)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Start a new family group",
                    fontSize = 14.sp,
                    color = Color(0xFF718096),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            onClick = onJoinFamilyClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8E8E0)  // CHANGED: Consistent card color
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_share),
                    contentDescription = "Join Family",
                    tint = Color(0xFF7DDDD3),  // CHANGED: Teal accent
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Join Existing Family",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2D3748)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Join an existing family group",
                    fontSize = 14.sp,
                    color = Color(0xFF718096),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ============== JOIN FAMILY SCREEN ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinFamilyScreen(
    onBackClick: () -> Unit,
    onFamilyJoined: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var families by remember { mutableStateOf<List<Family>>(emptyList()) }
    var filteredFamilies by remember { mutableStateOf<List<Family>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        db.collection("families")
            .get()
            .addOnSuccessListener { documents ->
                val familyList = documents.mapNotNull { doc ->
                    try {
                        Family(
                            familyNumber = doc.getString("familyNumber") ?: "",
                            familyName = doc.getString("familyName") ?: "",
                            creatorName = doc.getString("creatorName") ?: "",
                            memberCount = doc.getLong("memberCount")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                families = familyList
                filteredFamilies = familyList
                isLoading = false
            }
            .addOnFailureListener { e ->
                errorMessage = "Failed to load families: ${e.message}"
                isLoading = false
            }
    }

    LaunchedEffect(searchQuery) {
        filteredFamilies = if (searchQuery.isBlank()) {
            families
        } else {
            families.filter { family ->
                family.familyName.contains(searchQuery, ignoreCase = true) ||
                        family.familyNumber.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF6A11CB)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color(0xFF6A11CB)
            )
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_revert),
                contentDescription = "Back",
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Back",
                modifier = Modifier.padding(start = 8.dp),
                color = Color(0xFF6A11CB)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Join Family",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_search),
                        contentDescription = "Search",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Search families...",
                        color = Color(0xFF9CA3AF)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color(0xFF6A11CB),
                unfocusedIndicatorColor = Color(0xFFE5E7EB),
                cursorColor = Color(0xFF6A11CB)
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF6A11CB)
                )
            }
        } else if (errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFEE2E2)
                )
            ) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else if (filteredFamilies.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isBlank()) "No families available" else "No families found",
                    color = Color(0xFF9CA3AF),
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredFamilies) { family ->
                    JoinFamilyCard(
                        family = family,
                        onFamilyJoined = onFamilyJoined
                    )
                }
            }
        }
    }
}

@Composable
fun JoinFamilyCard(
    family: Family,
    onFamilyJoined: () -> Unit
) {
    var isRequesting by remember { mutableStateOf(false) }
    var requestSent by remember { mutableStateOf(false) }
    var showSecretCodeDialog by remember { mutableStateOf(false) }  // ← NEW
    var enteredSecretCode by remember { mutableStateOf("") }  // ← NEW
    var secretCodeError by remember { mutableStateOf("") }  // ← NEW

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // ← NEW: Secret Code Verification Dialog
    if (showSecretCodeDialog) {
        AlertDialog(
            onDismissRequest = {
                showSecretCodeDialog = false
                enteredSecretCode = ""
                secretCodeError = ""
            },
            title = {
                Text(
                    "Enter Secret Code",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter the secret code for ${family.familyName}",
                        fontSize = 16.sp,
                        color = Color(0xFF6B6B6B)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = enteredSecretCode,
                        onValueChange = {
                            enteredSecretCode = it
                            secretCodeError = ""
                        },
                        placeholder = { Text("Secret Code", color = Color(0xFF9CA3AF)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8E8E0),
                            unfocusedContainerColor = Color(0xFFE8E8E0),
                            focusedIndicatorColor = Color(0xFF7DDDD3),
                            unfocusedIndicatorColor = Color(0xFFB8B8A8),
                            cursorColor = Color(0xFF7DDDD3)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    if (secretCodeError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = secretCodeError,
                            color = Color(0xFFEF4444),
                            fontSize = 14.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredSecretCode.isBlank()) {
                            secretCodeError = "Please enter secret code"
                            return@Button
                        }

                        isRequesting = true

                        // Verify secret code
                        db.collection("families")
                            .document(family.familyNumber)
                            .get()
                            .addOnSuccessListener { doc ->
                                val storedSecretCode = doc.getString("secretCode") ?: ""

                                if (enteredSecretCode == storedSecretCode) {
                                    // Secret code is correct - join family directly
                                    db.collection("families")
                                        .document(family.familyNumber)
                                        .get()
                                        .addOnSuccessListener { familyDoc ->
                                            val members = familyDoc.get("members") as? MutableList<HashMap<String, Any>> ?: mutableListOf()
                                            members.add(
                                                hashMapOf(
                                                    "name" to UserSession.userName,
                                                    "role" to "member",
                                                    "userId" to UserSession.userId,
                                                    "joinedAt" to Timestamp.now()
                                                )
                                            )

                                            db.collection("families")
                                                .document(family.familyNumber)
                                                .update(
                                                    mapOf(
                                                        "members" to members,
                                                        "memberCount" to members.size
                                                    )
                                                )
                                                .addOnSuccessListener {
                                                    // Update user's familyId
                                                    db.collection("users")
                                                        .document(UserSession.userId)
                                                        .update("familyId", family.familyNumber)
                                                        .addOnSuccessListener {
                                                            UserSession.familyId = family.familyNumber
                                                            isRequesting = false
                                                            requestSent = true
                                                            showSecretCodeDialog = false
                                                            enteredSecretCode = ""

                                                            android.widget.Toast.makeText(
                                                                context,
                                                                "Successfully joined family!",
                                                                android.widget.Toast.LENGTH_SHORT
                                                            ).show()

                                                            // Navigate to dashboard
                                                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                                                onFamilyJoined()
                                                            }, 1000)
                                                        }
                                                }
                                        }
                                } else {
                                    // Secret code is incorrect
                                    isRequesting = false
                                    secretCodeError = "Incorrect secret code"
                                }
                            }
                            .addOnFailureListener { e ->
                                isRequesting = false
                                secretCodeError = "Error: ${e.message}"
                            }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isRequesting
                ) {
                    if (isRequesting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Join")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSecretCodeDialog = false
                        enteredSecretCode = ""
                        secretCodeError = ""
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6A11CB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = family.familyName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2D3748)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = family.familyNumber,
                    fontSize = 16.sp,
                    color = Color(0xFF718096)
                )
            }

            Button(
                onClick = {
                    // ← CHANGED: Show secret code dialog instead of sending request
                    showSecretCodeDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (requestSent) Color(0xFF10B981) else Color(0xFF7DDDD3),
                    disabledContainerColor = Color(0xFFE5E7EB)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isRequesting && !requestSent,
                modifier = Modifier.height(48.dp)
            ) {
                if (isRequesting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = if (requestSent) "Joined!" else "Join Family",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (requestSent) Color.White else Color(0xFF2D3748)
                    )
                }
            }
        }
    }
}

// ============== PROFILE SCREEN ==============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit
) {
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editField by remember { mutableStateOf("") }
    var editValue by remember { mutableStateOf("") }
    var showPhotoOptions by remember { mutableStateOf(false) }
    var familyExpanded by remember { mutableStateOf(false) }
    var familyData by remember { mutableStateOf<Family?>(null) }
    var familyMembers by remember { mutableStateOf<List<FamilyMember>>(emptyList()) }
    var showExitDialog by remember { mutableStateOf(false) }
    var exitConfirmationText by remember { mutableStateOf("") }
    var isExiting by remember { mutableStateOf(false) }
    var familySecretCode by remember { mutableStateOf("") }
    var isCreator by remember { mutableStateOf(false) }
    var showSecretCodeEditDialog by remember { mutableStateOf(false) }
    var newSecretCode by remember { mutableStateOf("") }
    var showDeleteMemberDialog by remember { mutableStateOf(false) }
    var memberToDelete by remember { mutableStateOf<FamilyMember?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // ← NEW: Live Location states
    var isLiveLocationEnabled by remember { mutableStateOf(false) }
    var showLocationPermissionDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }

    // ← NEW: Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            // Permission granted, enable live location
            isLiveLocationEnabled = true

            // Update in Firebase
            db.collection("families")
                .document(UserSession.familyId)
                .collection("memberLocations")
                .document(UserSession.userId)
                .set(hashMapOf(
                    "userId" to UserSession.userId,
                    "userName" to UserSession.userName,
                    "isOnline" to true,
                    "latitude" to 0.0,
                    "longitude" to 0.0,
                    "batteryLevel" to 100,
                    "isNotificationEnabled" to true,
                    "lastUpdated" to Timestamp.now()
                ))
                .addOnSuccessListener {
                    // Start location service
                    val intent = android.content.Intent(context, LocationService::class.java)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }

                    android.widget.Toast.makeText(
                        context,
                        "Live Location enabled",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            // Permission denied
            isLiveLocationEnabled = false
            android.widget.Toast.makeText(
                context,
                "Location permission is required for Live Location",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    // REPLACE ENTIRE IMAGE PICKER LAUNCHER WITH:
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            android.widget.Toast.makeText(
                context,
                "Uploading profile picture...",
                android.widget.Toast.LENGTH_SHORT
            ).show()

            // Upload to Firebase Storage
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
                .child("profile_pictures/${UserSession.userId}.jpg")

            storageRef.putFile(uri)
                .addOnSuccessListener {
                    // Get download URL
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        // Save download URL to Firestore
                        db.collection("users")
                            .document(UserSession.userId)
                            .update(
                                "profilePictureUri", downloadUri.toString(),
                                "updatedAt", Timestamp.now()
                            )
                            .addOnSuccessListener {
                                // ADD THIS LINE for instant update:
                                ProfilePictureManager.update(UserSession.userId, downloadUri.toString())

                                android.widget.Toast.makeText(
                                    context,
                                    "Profile picture updated",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                showPhotoOptions = false
                                isLoading = true
                            }
                            .addOnFailureListener { e ->
                                android.widget.Toast.makeText(
                                    context,
                                    "Failed to save: ${e.message}",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    android.widget.Toast.makeText(
                        context,
                        "Upload failed: ${e.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    // Load user profile and family data (existing code with location status)
    LaunchedEffect(isLoading) {
        if (UserSession.userId.isNotEmpty()) {
            db.collection("users")
                .document(UserSession.userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userProfile = UserProfile(
                            userId = document.id,
                            name = document.getString("name") ?: "",
                            phoneNumber = document.getString("phoneNumber") ?: "",
                            email = document.getString("email") ?: "",
                            dateOfBirth = document.getString("dateOfBirth") ?: "",
                            address = document.getString("address") ?: "",
                            profilePictureUri = document.getString("profilePictureUri") ?: "",
                            updatedAt = document.getTimestamp("updatedAt")
                        )

                        // Load family data if user has a family
                        if (UserSession.familyId.isNotEmpty()) {
                            db.collection("families")
                                .document(UserSession.familyId)
                                .get()
                                .addOnSuccessListener { familyDoc ->
                                    if (familyDoc.exists()) {
                                        familyData = Family(
                                            familyNumber = familyDoc.getString("familyNumber") ?: "",
                                            familyName = familyDoc.getString("familyName") ?: "",
                                            creatorName = familyDoc.getString("creatorName") ?: "",
                                            memberCount = familyDoc.getLong("memberCount")?.toInt() ?: 0
                                        )

                                        familySecretCode = familyDoc.getString("secretCode") ?: ""

                                        val members = familyDoc.get("members") as? List<Map<String, Any>> ?: emptyList()
                                        familyMembers = members.map { member ->
                                            FamilyMember(
                                                name = member["name"] as? String ?: "",
                                                role = member["role"] as? String ?: "",
                                                userId = member["userId"] as? String ?: ""
                                            )
                                        }

                                        isCreator = familyMembers.find { it.userId == UserSession.userId }?.role == "creator"
                                    }

                                    // ← NEW: Load live location status
                                    db.collection("families")
                                        .document(UserSession.familyId)
                                        .collection("memberLocations")
                                        .document(UserSession.userId)
                                        .get()
                                        .addOnSuccessListener { locationDoc ->
                                            if (locationDoc.exists()) {
                                                isLiveLocationEnabled = locationDoc.getBoolean("isOnline") ?: false
                                            }
                                        }
                                }
                        }
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    Log.e("Profile", "Error loading profile", e)
                    isLoading = false
                }
        }
    }

    // Photo options dialog
    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title = { Text("Profile Photo") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Change Photo option
                    Card(
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8E8E0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                                contentDescription = "Change Photo",
                                tint = Color(0xFF7DDDD3),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Change Photo",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2D3748)
                            )
                        }
                    }

                    // Delete Photo option (only if photo exists)
                    if (userProfile?.profilePictureUri?.isNotEmpty() == true) {
                        Card(
                            onClick = {
                                // Delete profile picture
                                val storage = FirebaseStorage.getInstance()
                                val storageRef = storage.reference
                                    .child("profile_pictures/${UserSession.userId}.jpg")

                                storageRef.delete()
                                    .addOnSuccessListener {
                                        // Then remove from Firestore
                                        db.collection("users")
                                            .document(UserSession.userId)
                                            .update("profilePictureUri", "", "updatedAt", Timestamp.now())
                                            .addOnSuccessListener {
                                                // ADD THIS LINE for instant update:
                                                ProfilePictureManager.remove(UserSession.userId)

                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Profile picture removed",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                                showPhotoOptions = false
                                                isLoading = true
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        android.widget.Toast.makeText(
                                            context,
                                            "Failed to delete: ${e.message}",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnSuccessListener {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Profile picture removed",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                        showPhotoOptions = false
                                        isLoading = true
                                    }
                                    .addOnFailureListener { e ->
                                        android.widget.Toast.makeText(
                                            context,
                                            "Failed to remove: ${e.message}",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFE8E8)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_delete),
                                    contentDescription = "Delete Photo",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Delete Photo",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhotoOptions = false }) {
                    Text("Cancel", color = Color(0xFF6A11CB))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Edit dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit $editField") },
            text = {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    label = { Text(editField) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = editField != "Address",
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE8E8E0),
                        unfocusedContainerColor = Color(0xFFE8E8E0),
                        focusedIndicatorColor = Color(0xFF7DDDD3),
                        unfocusedIndicatorColor = Color(0xFFB8B8A8),
                        cursorColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Update field in Firebase
                        val fieldMap = mapOf(
                            "Name" to "name",
                            "Email" to "email",
                            "Date of Birth" to "dateOfBirth",
                            "Address" to "address"
                        )

                        val dbField = fieldMap[editField] ?: return@Button

                        db.collection("users")
                            .document(UserSession.userId)
                            .update(
                                dbField, editValue,
                                "updatedAt", Timestamp.now()
                            )
                            .addOnSuccessListener {
                                // Update UserSession if name was changed
                                if (editField == "Name") {
                                    UserSession.userName = editValue

                                    // Also update name in family members list
                                    if (UserSession.familyId.isNotEmpty()) {
                                        db.collection("families")
                                            .document(UserSession.familyId)
                                            .get()
                                            .addOnSuccessListener { doc ->
                                                val members = doc.get("members") as? MutableList<HashMap<String, Any>> ?: mutableListOf()
                                                val updatedMembers = members.map { member ->
                                                    if (member["userId"] == UserSession.userId) {
                                                        member["name"] = editValue
                                                    }
                                                    member
                                                }

                                                db.collection("families")
                                                    .document(UserSession.familyId)
                                                    .update("members", updatedMembers)
                                            }
                                    }
                                }

                                showEditDialog = false
                                isLoading = true
                            }
                            .addOnFailureListener { e ->
                                android.widget.Toast.makeText(
                                    context,
                                    "Failed to update: ${e.message}",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showEditDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6A11CB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Secret Code Edit Dialog
    if (showSecretCodeEditDialog) {
        AlertDialog(
            onDismissRequest = {
                showSecretCodeEditDialog = false
                newSecretCode = ""
            },
            title = { Text("Edit Secret Code") },
            text = {
                Column {
                    Text(
                        text = "Enter a new secret code for your family",
                        fontSize = 14.sp,
                        color = Color(0xFF6B6B6B),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = newSecretCode,
                        onValueChange = { newSecretCode = it },
                        label = { Text("New Secret Code") },
                        placeholder = { Text("At least 4 characters") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8E8E0),
                            unfocusedContainerColor = Color(0xFFE8E8E0),
                            focusedIndicatorColor = Color(0xFF7DDDD3),
                            unfocusedIndicatorColor = Color(0xFFB8B8A8),
                            cursorColor = Color(0xFF7DDDD3)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSecretCode.length < 4) {
                            android.widget.Toast.makeText(
                                context,
                                "Secret code must be at least 4 characters",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        // Update secret code in database
                        db.collection("families")
                            .document(UserSession.familyId)
                            .update("secretCode", newSecretCode)
                            .addOnSuccessListener {
                                familySecretCode = newSecretCode
                                showSecretCodeEditDialog = false
                                newSecretCode = ""
                                android.widget.Toast.makeText(
                                    context,
                                    "Secret code updated successfully",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                android.widget.Toast.makeText(
                                    context,
                                    "Failed to update: ${e.message}",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7DDDD3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSecretCodeEditDialog = false
                        newSecretCode = ""
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6A11CB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Delete Member Confirmation Dialog
    if (showDeleteMemberDialog && memberToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteMemberDialog = false
                memberToDelete = null
            },
            title = {
                Text(
                    "Remove Member",
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove ${memberToDelete?.name} from the family? This action cannot be undone.",
                    fontSize = 16.sp,
                    color = Color(0xFF2D3748)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        memberToDelete?.let { member ->
                            // Remove member from family
                            db.collection("families")
                                .document(UserSession.familyId)
                                .get()
                                .addOnSuccessListener { doc ->
                                    val members = doc.get("members") as? MutableList<HashMap<String, Any>> ?: mutableListOf()
                                    val updatedMembers = members.filter {
                                        it["userId"] != member.userId
                                    }

                                    db.collection("families")
                                        .document(UserSession.familyId)
                                        .update(
                                            "members", updatedMembers,
                                            "memberCount", updatedMembers.size
                                        )
                                        .addOnSuccessListener {
                                            // Remove familyId from user's profile
                                            db.collection("users")
                                                .document(member.userId)
                                                .update("familyId", "")
                                                .addOnSuccessListener {
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "${member.name} has been removed from the family",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()

                                                    showDeleteMemberDialog = false
                                                    memberToDelete = null

                                                    // Reload profile data
                                                    isLoading = true
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            android.widget.Toast.makeText(
                                                context,
                                                "Failed to remove member: ${e.message}",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                            showDeleteMemberDialog = false
                                            memberToDelete = null
                                        }
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Remove", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteMemberDialog = false
                        memberToDelete = null
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6A11CB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    "Logout",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to logout?",
                    fontSize = 16.sp,
                    color = Color(0xFF6B6B6B)
                )
            },
            confirmButton = {
                // In the logout confirmation dialog:
                Button(
                    onClick = {
                        coroutineScope.launch {
                            // Clear session data
                            sessionManager.clearSession()
                            UserSession.clear()

                            // Clear profile picture cache
                            ProfilePictureCache.clear()

                            // Stop any services if running
                            val intent = android.content.Intent(context, LocationService::class.java)
                            context.stopService(intent)

                            // Navigate to Welcome screen
                            showLogoutDialog = false
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Logout", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6A11CB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Exit Family Confirmation Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = {
                showExitDialog = false
                exitConfirmationText = ""
            },
            title = {
                Text(
                    "Exit From Family",
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to exit from ${familyData?.familyName}? This action cannot be undone.",
                        fontSize = 16.sp,
                        color = Color(0xFF2D3748)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Type \"exit\" to confirm:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B6B6B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exitConfirmationText,
                        onValueChange = { exitConfirmationText = it },
                        placeholder = { Text("Type exit", color = Color(0xFF9CA3AF)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8E8E0),
                            unfocusedContainerColor = Color(0xFFE8E8E0),
                            focusedIndicatorColor = Color(0xFFEF4444),
                            unfocusedIndicatorColor = Color(0xFFB8B8A8),
                            cursorColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (exitConfirmationText.lowercase() == "exit") {
                            isExiting = true

                            // Get current user's role
                            val currentMember = familyMembers.find { it.userId == UserSession.userId }
                            val isCreator = currentMember?.role == "creator"
                            val otherMembersExist = familyMembers.size > 1

                            if (isCreator && !otherMembersExist) {
                                // Delete entire family if creator and no other members
                                db.collection("families")
                                    .document(UserSession.familyId)
                                    .delete()
                                    .addOnSuccessListener {
                                        // Update user's familyId to empty
                                        db.collection("users")
                                            .document(UserSession.userId)
                                            .update("familyId", "")
                                            .addOnSuccessListener {
                                                UserSession.familyId = ""
                                                isExiting = false
                                                showExitDialog = false
                                                exitConfirmationText = ""

                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Family deleted successfully",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()

                                                // Navigate back
                                                onBackClick()
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        isExiting = false
                                        android.widget.Toast.makeText(
                                            context,
                                            "Failed to exit: ${e.message}",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                // Remove user from family members list
                                db.collection("families")
                                    .document(UserSession.familyId)
                                    .get()
                                    .addOnSuccessListener { doc ->
                                        val members = doc.get("members") as? MutableList<HashMap<String, Any>> ?: mutableListOf()
                                        val updatedMembers = members.filter {
                                            it["userId"] != UserSession.userId
                                        }

                                        db.collection("families")
                                            .document(UserSession.familyId)
                                            .update(
                                                "members", updatedMembers,
                                                "memberCount", updatedMembers.size
                                            )
                                            .addOnSuccessListener {
                                                // Update user's familyId to empty
                                                db.collection("users")
                                                    .document(UserSession.userId)
                                                    .update("familyId", "")
                                                    .addOnSuccessListener {
                                                        UserSession.familyId = ""
                                                        isExiting = false
                                                        showExitDialog = false
                                                        exitConfirmationText = ""

                                                        android.widget.Toast.makeText(
                                                            context,
                                                            "Exited from family successfully",
                                                            android.widget.Toast.LENGTH_SHORT
                                                        ).show()

                                                        // Navigate back
                                                        onBackClick()
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                isExiting = false
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Failed to exit: ${e.message}",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                            }
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                "Please type 'exit' to confirm",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444),
                        disabledContainerColor = Color(0xFFB8B8A8)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isExiting && exitConfirmationText.lowercase() == "exit"
                ) {
                    if (isExiting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isExiting) "Exiting..." else "Exit Family")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showExitDialog = false
                        exitConfirmationText = ""
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6A11CB)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isExiting
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF7DDDD3))
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F0))
                .padding(16.dp)
        ) {
            // Back button (existing)
            item {
                OutlinedButton(
                    onClick = onBackClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6A11CB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = Color(0xFF6A11CB)
                    )
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_revert),
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Back",
                        modifier = Modifier.padding(start = 8.dp),
                        color = Color(0xFF6A11CB)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Title (existing)
            item {
                Text(
                    text = "Profile",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B6B6B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Profile Photo Section (existing)
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.size(140.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF7DDDD3)
                            ),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 4.dp,
                                color = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (userProfile?.profilePictureUri?.isNotEmpty() == true) {
                                    // Display actual image using Coil
                                    coil.compose.AsyncImage(
                                        model = if (userProfile?.profilePictureUri?.startsWith("content://") == true) {
                                            android.net.Uri.parse(userProfile?.profilePictureUri)
                                        } else {
                                            userProfile?.profilePictureUri
                                        },
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        error = painterResource(id = android.R.drawable.ic_menu_gallery),
                                        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                                    )
                                } else {
                                    // Show default icon
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_myplaces),
                                        contentDescription = userProfile?.name ?: "User",
                                        tint = Color.White,
                                        modifier = Modifier.size(70.dp)
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(Color(0xFF7DDDD3))
                                .clickable {
                                    if (userProfile?.profilePictureUri?.isNotEmpty() == true) {
                                        showPhotoOptions = true
                                    } else {
                                        imagePickerLauncher.launch("image/*")
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_edit),
                                contentDescription = "Edit Photo",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = userProfile?.name ?: "User",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = userProfile?.phoneNumber ?: "",
                        fontSize = 16.sp,
                        color = Color(0xFF6B6B6B)
                    )

                    // ← NEW: Live Location Toggle
                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8E8E0)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_dialog_map),
                                    contentDescription = "Live Location",
                                    tint = if (isLiveLocationEnabled) Color(0xFF10B981) else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Live Location",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2D3748)
                                    )
                                    Text(
                                        text = if (isLiveLocationEnabled) "Sharing location" else "Location off",
                                        fontSize = 12.sp,
                                        color = if (isLiveLocationEnabled) Color(0xFF10B981) else Color(0xFF9CA3AF)
                                    )
                                }
                            }

                            // Toggle switch
                            Box(
                                modifier = Modifier
                                    .width(52.dp)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isLiveLocationEnabled)
                                            Color(0xFF10B981)
                                        else
                                            Color(0xFF9CA3AF)
                                    )
                                    .clickable {
                                        if (!isLiveLocationEnabled) {
                                            // ← STRICT: Only enable if user clicks - request permission
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        } else {
                                            // ← STRICT: Disable and update Firebase immediately
                                            isLiveLocationEnabled = false

                                            // Update in Firebase - set isOnline to FALSE
                                            db.collection("families")
                                                .document(UserSession.familyId)
                                                .collection("memberLocations")
                                                .document(UserSession.userId)
                                                .update("isOnline", false)  // ← CRITICAL: Set to FALSE
                                                .addOnSuccessListener {
                                                    // Stop location service
                                                    val intent = android.content.Intent(context, LocationService::class.java)
                                                    context.stopService(intent)

                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "Live Location disabled",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                .addOnFailureListener { e ->
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "Failed to disable: ${e.message}",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                    },
                                contentAlignment = if (isLiveLocationEnabled)
                                    Alignment.CenterEnd
                                else
                                    Alignment.CenterStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }

// ← UPDATED: Location permission launcher with STRICT enable
                    val locationPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        val fineLocationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                        val coarseLocationGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

                        if (fineLocationGranted || coarseLocationGranted) {
                            // ← STRICT: Permission granted, NOW enable
                            isLiveLocationEnabled = true

                            // Update in Firebase - set isOnline to TRUE
                            db.collection("families")
                                .document(UserSession.familyId)
                                .collection("memberLocations")
                                .document(UserSession.userId)
                                .set(hashMapOf(
                                    "userId" to UserSession.userId,
                                    "userName" to UserSession.userName,
                                    "isOnline" to true,  // ← CRITICAL: Set to TRUE only when user enables
                                    "latitude" to 0.0,
                                    "longitude" to 0.0,
                                    "batteryLevel" to 100,
                                    "isNotificationEnabled" to true,
                                    "lastUpdated" to Timestamp.now()
                                ))
                                .addOnSuccessListener {
                                    // Start location service
                                    val intent = android.content.Intent(context, LocationService::class.java)
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        context.startForegroundService(intent)
                                    } else {
                                        context.startService(intent)
                                    }

                                    android.widget.Toast.makeText(
                                        context,
                                        "Live Location enabled",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            // ← STRICT: Permission denied, keep disabled
                            isLiveLocationEnabled = false
                            android.widget.Toast.makeText(
                                context,
                                "Location permission is required for Live Location",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }

// ← UPDATED: Load location status STRICTLY from Firebase
                    LaunchedEffect(isLoading) {
                        if (UserSession.userId.isNotEmpty()) {
                            db.collection("users")
                                .document(UserSession.userId)
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        userProfile = UserProfile(
                                            userId = document.id,
                                            name = document.getString("name") ?: "",
                                            phoneNumber = document.getString("phoneNumber") ?: "",
                                            email = document.getString("email") ?: "",
                                            dateOfBirth = document.getString("dateOfBirth") ?: "",
                                            address = document.getString("address") ?: "",
                                            profilePictureUri = document.getString("profilePictureUri") ?: "",
                                            updatedAt = document.getTimestamp("updatedAt")
                                        )

                                        // Load family data if user has a family
                                        if (UserSession.familyId.isNotEmpty()) {
                                            db.collection("families")
                                                .document(UserSession.familyId)
                                                .get()
                                                .addOnSuccessListener { familyDoc ->
                                                    if (familyDoc.exists()) {
                                                        familyData = Family(
                                                            familyNumber = familyDoc.getString("familyNumber") ?: "",
                                                            familyName = familyDoc.getString("familyName") ?: "",
                                                            creatorName = familyDoc.getString("creatorName") ?: "",
                                                            memberCount = familyDoc.getLong("memberCount")?.toInt() ?: 0
                                                        )

                                                        familySecretCode = familyDoc.getString("secretCode") ?: ""

                                                        val members = familyDoc.get("members") as? List<Map<String, Any>> ?: emptyList()
                                                        familyMembers = members.map { member ->
                                                            FamilyMember(
                                                                name = member["name"] as? String ?: "",
                                                                role = member["role"] as? String ?: "",
                                                                userId = member["userId"] as? String ?: ""
                                                            )
                                                        }

                                                        isCreator = familyMembers.find { it.userId == UserSession.userId }?.role == "creator"
                                                    }

                                                    // ← STRICT: Load location status - ONLY from isOnline field
                                                    db.collection("families")
                                                        .document(UserSession.familyId)
                                                        .collection("memberLocations")
                                                        .document(UserSession.userId)
                                                        .get()
                                                        .addOnSuccessListener { locationDoc ->
                                                            if (locationDoc.exists()) {
                                                                // ← CRITICAL: Read isOnline field directly
                                                                isLiveLocationEnabled = locationDoc.getBoolean("isOnline") ?: false
                                                            } else {
                                                                // ← CRITICAL: Document doesn't exist = disabled
                                                                isLiveLocationEnabled = false
                                                            }
                                                        }
                                                        .addOnFailureListener {
                                                            // ← CRITICAL: Error loading = disabled
                                                            isLiveLocationEnabled = false
                                                        }
                                                }
                                        }
                                    }
                                    isLoading = false
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Profile", "Error loading profile", e)
                                    isLoading = false
                                }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // User Info Card (without "Personal Information" heading)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8E8E0)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = Color(0xFF7DDDD3)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Name field
                        ProfileInfoItem(
                            label = "Name",
                            value = userProfile?.name ?: "Not set",
                            onEdit = {
                                editField = "Name"
                                editValue = userProfile?.name ?: ""
                                showEditDialog = true
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Email field
                        ProfileInfoItem(
                            label = "Email",
                            value = userProfile?.email?.ifEmpty { "Not set" } ?: "Not set",
                            onEdit = {
                                editField = "Email"
                                editValue = userProfile?.email ?: ""
                                showEditDialog = true
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Date of Birth field
                        ProfileInfoItem(
                            label = "Date of Birth",
                            value = userProfile?.dateOfBirth?.ifEmpty { "Not set" } ?: "Not set",
                            onEdit = {
                                editField = "Date of Birth"
                                editValue = userProfile?.dateOfBirth ?: ""
                                showEditDialog = true
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Address field
                        ProfileInfoItem(
                            label = "Address",
                            value = userProfile?.address?.ifEmpty { "Not set" } ?: "Not set",
                            onEdit = {
                                editField = "Address"
                                editValue = userProfile?.address ?: ""
                                showEditDialog = true
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Account Settings Section
            item {
                Text(
                    text = "Account Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Family Details (Expandable) - WITH SECRET CODE INSIDE
            item {
                Card(
                    onClick = { familyExpanded = !familyExpanded },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8E8E0)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF7DDDD3)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_myplaces),
                                        contentDescription = "Family",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = "Family Details",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2D3748)
                                )
                            }

                            Icon(
                                painter = painterResource(
                                    id = if (familyExpanded)
                                        android.R.drawable.arrow_up_float
                                    else
                                        android.R.drawable.arrow_down_float
                                ),
                                contentDescription = if (familyExpanded) "Collapse" else "Expand",
                                tint = Color(0xFF7DDDD3),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Expanded content
                        androidx.compose.animation.AnimatedVisibility(visible = familyExpanded && familyData != null) {
                            Column {
                                Spacer(modifier = Modifier.height(20.dp))

                                // Family Number
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Family Number:",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF6B6B6B)
                                    )
                                    Text(
                                        text = familyData?.familyNumber ?: "",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2D3748)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Family Name
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Family Name:",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF6B6B6B)
                                    )
                                    Text(
                                        text = familyData?.familyName ?: "",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2D3748)
                                    )
                                }

                                // Secret Code (only for creators)
                                if (isCreator && familySecretCode.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Secret Code:",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF6B6B6B)
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = familySecretCode,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2D3748)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(
                                                onClick = {
                                                    newSecretCode = ""
                                                    showSecretCodeEditDialog = true
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = android.R.drawable.ic_menu_edit),
                                                    contentDescription = "Edit Secret Code",
                                                    tint = Color(0xFF7DDDD3),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Divider
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFFB8B8A8))
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Family Members Title
                                Text(
                                    text = "Family Members:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF6B6B6B)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Family Members List with Delete functionality
                                familyMembers.forEach { member ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF7DDDD3)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = android.R.drawable.ic_menu_myplaces),
                                                    contentDescription = member.name,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column {
                                                Text(
                                                    text = member.name,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF2D3748)
                                                )
                                                if (member.role == "creator") {
                                                    Text(
                                                        text = "Family Creator",
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF7DDDD3),
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }

                                        // DELETE ICON (only for non-creator members, and only visible to creators)
                                        if (isCreator && member.role != "creator") {
                                            IconButton(
                                                onClick = {
                                                    memberToDelete = member
                                                    showDeleteMemberDialog = true
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = android.R.drawable.ic_menu_delete),
                                                    contentDescription = "Delete Member",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Exit From Family Button
                                Button(
                                    onClick = { showExitDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFEF4444)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                                        contentDescription = "Exit",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Exit From Family",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Empty state when not expanded or no family
                        if (familyExpanded && familyData == null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "You are not part of any family",
                                fontSize = 14.sp,
                                color = Color(0xFF9CA3AF),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Logout Button
            item {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6A11CB)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_revert),
                        contentDescription = "Logout",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Logout",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

fun loadProfilePicture(userId: String, onResult: (String) -> Unit) {
    // REPLACE THE ENTIRE FUNCTION with:
    if (userId.isEmpty()) {
        onResult("")
        return
    }

    // Check local state first (fastest)
    ProfilePictureManager.get(userId)?.let {
        onResult(it)
        return
    }

    // Check Firebase (with real-time listener)
    val db = FirebaseFirestore.getInstance()

    db.collection("users")
        .document(userId)
        .addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener

            val uri = snapshot?.getString("profilePictureUri") ?: ""
            ProfilePictureManager.update(userId, uri)
            onResult(uri)
        }
}

@Composable
fun ProfileImage(
    userId: String,
    size: Dp = 60.dp,
    backgroundColor: Color = Color(0xFFB8B8A8),
    iconSize: Dp = 32.dp,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    contentScale: ContentScale = ContentScale.Crop,
    showLoading: Boolean = false
) {
    // This will automatically recompose when ProfilePictureManager changes
    val profilePictureUri by remember(userId) {
        derivedStateOf { ProfilePictureManager.get(userId) ?: "" }
    }

    var isLoading by remember { mutableStateOf(true) }

    // Load from Firebase on first composition
    LaunchedEffect(userId) {
        loadProfilePicture(userId) {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(borderWidth, borderColor, CircleShape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading && showLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(size * 0.4f)
            )
        } else if (profilePictureUri.isNotEmpty()) {
            AsyncImage(
                model = if (profilePictureUri.startsWith("content://")) {
                    android.net.Uri.parse(profilePictureUri)
                } else {
                    profilePictureUri
                },
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                error = painterResource(id = android.R.drawable.ic_menu_myplaces),
                placeholder = painterResource(id = android.R.drawable.ic_menu_myplaces)
            )
        } else {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_myplaces),
                contentDescription = "Default Avatar",
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

// Profile Info Item Component
@Composable
fun ProfileInfoItem(
    label: String,
    value: String,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B6B6B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2D3748)
            )
        }
        IconButton(onClick = onEdit) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_edit),
                contentDescription = "Edit $label",
                tint = Color(0xFF7DDDD3),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
// ============== AI BOTS SCREEN ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIBotsScreen(
    onBackClick: () -> Unit,
    onAICookClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))
            .padding(16.dp)
    ) {
        // Back button
        OutlinedButton(
            onClick = onBackClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF6A11CB)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color(0xFF6A11CB)
            )
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_revert),
                contentDescription = "Back",
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Back",
                modifier = Modifier.padding(start = 8.dp),
                color = Color(0xFF6A11CB)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "AI Bots",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B6B6B),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // AI Bots List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI Cook Bot
            item {
                AIBotCard(
                    botName = "AI Cook",
                    onClick = onAICookClick
                )
            }

            // Add more AI bots here in the future
            // item {
            //     AIBotCard(
            //         botName = "AI Assistant",
            //         onClick = { /* Navigate to AI Assistant */ }
            //     )
            // }
        }
    }
}

// AI Bot Card Component (styled like Family Member cards)
@Composable
fun AIBotCard(
    botName: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8E8E0)
        ),
        shape = RoundedCornerShape(40.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AI Bot Avatar with AI logo
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF7DDDD3)),
                contentAlignment = Alignment.Center
            ) {
                // AI logo representation (using star icon as AI symbol)
                Icon(
                    painter = painterResource(id = android.R.drawable.btn_star_big_on),
                    contentDescription = botName,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Bot Name
            Text(
                text = botName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2D3748),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ============== AI COOK SCREEN (WITH SHARE FEATURE) ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AICookScreen(
    onBackClick: () -> Unit
) {
    var userMessage by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showLanguageSettings by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("Telugu") }

    val context = LocalContext.current

    // Language options
    val languages = listOf(
        "Telugu", "Hindi", "English", "Kannada", "Tamil", "Malayalam", "Marathi"
    )

    // Description in different languages
    val descriptions = mapOf(
        "Telugu" to "మీరు వంట చేయాలనుకునే వంటకం పేరు మాత్రమే పంపండి. AI మీకు ఈ క్రింది విధంగా సమాధానం ఇస్తుంది:\n\n1. వంటకానికి అవసరమైన పదార్థాలు మరియు వాటి పరిమాణాలు\n2. వంట చేసే విధానం దశల వారీగా",
        "Hindi" to "आप केवल उस व्यंजन का नाम भेजें जो आप बनाना चाहते हैं। AI आपको इस पैटर्न में जवाब देगा:\n\n1. व्यंजन बनाने के लिए आवश्यक सामग्री और उनकी मात्रा\n2. खाना पकाने की प्रक्रिया को चरण-दर-चरण समझाना",
        "English" to "Just send the name of the dish you want to cook. AI will reply in this pattern:\n\n1. List of all ingredients with their quantities\n2. Step-by-step cooking instructions",
        "Kannada" to "ನೀವು ಅಡುಗೆ ಮಾಡಲು ಬಯಸುವ ಖಾದ್ಯದ ಹೆಸರನ್ನು ಮಾತ್ರ ಕಳುಹಿಸಿ. AI ಈ ಮಾದರಿಯಲ್ಲಿ ಉತ್ತರಿಸುತ್ತದೆ:\n\n1. ಖಾದ್ಯಕ್ಕೆ ಅಗತ್ಯವಿರುವ ಎಲ್ಲಾ ಪದಾರ್ಥಗಳು ಮತ್ತು ಅವುಗಳ ಪ್ರಮಾಣಗಳು\n2. ಅಡುಗೆ ಪ್ರಕ್ರಿಯೆಯನ್ನು ಹಂತ ಹಂತವಾಗಿ ವಿವರಿಸುವುದು",
        "Tamil" to "நீங்கள் சமைக்க விரும்பும் உணவின் பெயரை மட்டும் அனுப்பவும். AI இந்த முறையில் பதிலளிக்கும்:\n\n1. உணவிற்கு தேவையான அனைத்து பொருட்கள் மற்றும் அவற்றின் அளவுகள்\n2. சமையல் செயல்முறையை படிப்படியாக விளக்குதல்",
        "Malayalam" to "നിങ്ങൾ പാചകം ചെയ്യാൻ ആഗ്രഹിക്കുന്ന വിഭവത്തിന്റെ പേര് മാത്രം അയയ്ക്കുക. AI ഈ പാറ്റേണിൽ മറുപടി നൽകും:\n\n1. വിഭവത്തിന് ആവശ്യമായ എല്ലാ ചേരുവകളും അവയുടെ അളവും\n2. പാചക പ്രക്രിയ ഘട്ടം ഘട്ടമായി വിശദീകരിക്കൽ",
        "Marathi" to "तुम्हाला शिजवायच्या पदार्थाचे नाव पाठवा. AI या पॅटर्नमध्ये उत्तर देईल:\n\n1. पदार्थासाठी आवश्यक सर्व साहित्य आणि त्यांचे प्रमाण\n2. स्वयंपाक प्रक्रिया चरण-दर-चरण स्पष्टीकरण"
    )

    // Language Settings Dialog
    if (showLanguageSettings) {
        AlertDialog(
            onDismissRequest = { showLanguageSettings = false },
            title = {
                Text(
                    "Language Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Select your preferred language:",
                        fontSize = 14.sp,
                        color = Color(0xFF6B6B6B),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    languages.forEach { language ->
                        Card(
                            onClick = {
                                selectedLanguage = language
                                showLanguageSettings = false
                                android.widget.Toast.makeText(
                                    context,
                                    "Language changed to $language",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedLanguage == language)
                                    Color(0xFF7DDDD3)
                                else
                                    Color(0xFFE8E8E0)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (selectedLanguage == language) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_save),
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Text(
                                    text = language,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedLanguage == language) Color.White else Color(0xFF2D3748)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageSettings = false }) {
                    Text("Close", color = Color(0xFF6A11CB))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))
            .padding(16.dp)
    ) {
        // Header with Back button and Settings icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBackClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF6A11CB)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF6A11CB)
                )
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_revert),
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Back",
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color(0xFF6A11CB)
                )
            }

            // Settings icon
            IconButton(
                onClick = { showLanguageSettings = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_preferences),
                    contentDescription = "Settings",
                    tint = Color(0xFF6A11CB),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title with AI logo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF7DDDD3)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.btn_star_big_on),
                    contentDescription = "AI Cook",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "AI Cook",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B6B6B)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Language indicator
        Text(
            text = "Language: $selectedLanguage",
            fontSize = 14.sp,
            color = Color(0xFF7DDDD3),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Chat messages area
        if (chatMessages.isEmpty()) {
            // Welcome message with description in selected language
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8E8E0)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.btn_star_big_on),
                                contentDescription = "AI Cook",
                                tint = Color(0xFF7DDDD3),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = when (selectedLanguage) {
                                    "Telugu" -> "నమస్కారం! నేను AI Cook"
                                    "Hindi" -> "नमस्ते! मैं AI Cook हूं"
                                    "Kannada" -> "ನಮಸ್ಕಾರ! ನಾನು AI Cook"
                                    "Tamil" -> "வணக்கம்! நான் AI Cook"
                                    "Malayalam" -> "നമസ്കാരം! ഞാൻ AI Cook"
                                    "Marathi" -> "नमस्कार! मी AI Cook आहे"
                                    else -> "Hello! I'm AI Cook"
                                },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3748),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = descriptions[selectedLanguage] ?: descriptions["English"]!!,
                                fontSize = 16.sp,
                                color = Color(0xFF6B6B6B),
                                textAlign = TextAlign.Start,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Chat messages list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                reverseLayout = false
            ) {
                items(chatMessages.size) { index ->
                    val message = chatMessages[index]
                    // Find the corresponding user message (dish name) for AI replies
                    val dishName = if (!message.isFromUser && index > 0) {
                        chatMessages[index - 1].text
                    } else {
                        ""
                    }
                    AICookMessageBubble(
                        message = message,
                        dishName = dishName,
                        context = context
                    )
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF7DDDD3)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.btn_star_big_on),
                                    contentDescription = "AI",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE8E8E0)
                                ),
                                shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF7DDDD3),
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = when (selectedLanguage) {
                                            "Telugu" -> "వంట సూచనలు తయారు చేస్తోంది..."
                                            "Hindi" -> "रेसिपी तैयार कर रहा है..."
                                            "Kannada" -> "ಪಾಕವಿಧಾನವನ್ನು ತಯಾರಿಸುತ್ತಿದೆ..."
                                            "Tamil" -> "சமையல் முறையை தயாரிக்கிறது..."
                                            "Malayalam" -> "പാചകക്കുറിപ്പ് തയ്യാറാക്കുന്നു..."
                                            "Marathi" -> "पाककृती तयार करत आहे..."
                                            else -> "Preparing recipe..."
                                        },
                                        fontSize = 14.sp,
                                        color = Color(0xFF6B6B6B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input area
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = userMessage,
                onValueChange = { userMessage = it },
                placeholder = {
                    Text(
                        when (selectedLanguage) {
                            "Telugu" -> "వంటకం పేరు పంపండి..."
                            "Hindi" -> "व्यंजन का नाम भेजें..."
                            "Kannada" -> "ಖಾದ್ಯದ ಹೆಸರು ಕಳುಹಿಸಿ..."
                            "Tamil" -> "உணவின் பெயரை அனுப்பவும்..."
                            "Malayalam" -> "വിഭവത്തിന്റെ പേര് അയയ്ക്കുക..."
                            "Marathi" -> "पदार्थाचे नाव पाठवा..."
                            else -> "Send dish name..."
                        },
                        color = Color(0xFF9CA3AF)
                    )
                },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE8E8E0),
                    unfocusedContainerColor = Color(0xFFE8E8E0),
                    focusedIndicatorColor = Color(0xFF7DDDD3),
                    unfocusedIndicatorColor = Color(0xFFB8B8A8),
                    cursorColor = Color(0xFF7DDDD3)
                ),
                shape = RoundedCornerShape(28.dp),
                maxLines = 3
            )

            // Send button
            Button(
                onClick = {
                    if (userMessage.isNotBlank()) {
                        val dishName = userMessage

                        // Add user message
                        chatMessages = chatMessages + ChatMessage(
                            text = dishName,
                            isFromUser = true,
                            timestamp = System.currentTimeMillis()
                        )

                        userMessage = ""
                        isLoading = true

                        // Call Groq AI API
                        getAICookResponse(dishName, selectedLanguage, context) { response ->
                            chatMessages = chatMessages + ChatMessage(
                                text = response,
                                isFromUser = false,
                                timestamp = System.currentTimeMillis()
                            )
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.size(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7DDDD3)
                ),
                shape = CircleShape,
                enabled = !isLoading && userMessage.isNotBlank(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_send),
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// AI Cook Message Bubble Component (WITH SHARE BUTTON)
@Composable
fun AICookMessageBubble(
    message: ChatMessage,
    dishName: String,
    context: android.content.Context
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
        ) {
            if (!message.isFromUser) {
                // AI avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7DDDD3)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.btn_star_big_on),
                        contentDescription = "AI",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Card(
                modifier = Modifier.widthIn(max = 320.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isFromUser) Color(0xFF7DDDD3) else Color(0xFFE8E8E0)
                ),
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (message.isFromUser) 20.dp else 4.dp,
                    bottomEnd = if (message.isFromUser) 4.dp else 20.dp
                )
            ) {
                Text(
                    text = message.text,
                    fontSize = 15.sp,
                    color = if (message.isFromUser) Color.White else Color(0xFF2D3748),
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 22.sp
                )
            }

            if (message.isFromUser) {
                Spacer(modifier = Modifier.width(8.dp))
                // User avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6A11CB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_myplaces),
                        contentDescription = "User",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Share button for AI replies
        if (!message.isFromUser && dishName.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, top = 4.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = {
                        shareRecipe(context, dishName, message.text)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_share),
                        contentDescription = "Share Recipe",
                        tint = Color(0xFF7DDDD3),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Function to share recipe
fun shareRecipe(context: android.content.Context, dishName: String, recipe: String) {
    val shareText = "📖 $dishName Recipe\n\n$recipe\n\n✨ Shared from AI Cook"

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    val chooserIntent = Intent.createChooser(shareIntent, "Share Recipe via")
    context.startActivity(chooserIntent)
}

// Function to get AI response using Groq API (WITHOUT YouTube)
fun getAICookResponse(
    dishName: String,
    language: String,
    context: android.content.Context,
    onResponse: (String) -> Unit
) {
    val apiKey = "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"

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = "https://api.groq.com/openai/v1/chat/completions"

            val prompt = """
You are an expert cooking assistant. The user has asked for a recipe for "$dishName" in $language language.

Please provide the response STRICTLY in this format and ONLY in $language language:

**🥘 Ingredients:**
- [Ingredient 1 with quantity]
- [Ingredient 2 with quantity]
- [Continue with all ingredients]

**👨‍🍳 Cooking Instructions:**
1. [First step in detail]
2. [Second step in detail]
3. [Continue with all steps]

IMPORTANT:
- Write EVERYTHING in $language language only
- Be detailed and clear
- Include all ingredients with proper quantities
- Do NOT include any video links or external references
            """.trimIndent()

            // Build JSON request (OpenAI-compatible format)
            val jsonObject = JSONObject()
            jsonObject.put("model", "llama-3.3-70b-versatile")

            val messagesArray = JSONArray()
            val messageObject = JSONObject()
            messageObject.put("role", "user")
            messageObject.put("content", prompt)
            messagesArray.put(messageObject)

            jsonObject.put("messages", messagesArray)
            jsonObject.put("temperature", 0.7)
            jsonObject.put("max_tokens", 2000)

            val jsonBody = jsonObject.toString()

            // Make HTTP request
            val client = OkHttpClient()
            val mediaType = "application/json".toMediaTypeOrNull()
            val requestBody = jsonBody.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val choices = jsonResponse.getJSONArray("choices")

                    if (choices.length() > 0) {
                        val message = choices.getJSONObject(0)
                            .getJSONObject("message")
                        val text = message.getString("content")

                        withContext(Dispatchers.Main) {
                            onResponse(text)
                        }
                    } else {
                        throw Exception("No response from AI")
                    }
                } else {
                    throw Exception("Empty response body")
                }
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                throw Exception("API Error: ${response.code} - $errorBody")
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                val errorMessage = when (language) {
                    "Telugu" -> "క్ష‌మించండి, ఏదో తప్పు జరిగింది. దయచేసి మళ్లీ ప్రయత్నించండి.\n\nలోపం: ${e.message}"
                    "Hindi" -> "क्षमा करें, कुछ गलत हो गया। कृपया पुन: प्रयास करें।\n\nत्रुटि: ${e.message}"
                    "Kannada" -> "ಕ್ಷಮಿಸಿ, ಏನೋ ತಪ್ಪಾಗಿದೆ. ದಯವಿಟ್ಟು ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ.\n\nದೋಷ: ${e.message}"
                    "Tamil" -> "மன்னிக்கவும், ஏதோ தவறு ஏற்பட்டது. மீண்டும் முயற்சிக்கவும்.\n\nபிழை: ${e.message}"
                    "Malayalam" -> "ക്ഷമിക്കണം, എന്തോ തെറ്റ് സംഭവിച്ചു. വീണ്ടും ശ്രമിക്കുക.\n\nപിശക്: ${e.message}"
                    "Marathi" -> "माफ करा, काहीतरी चूक झाली. कृपया पुन्हा प्रयत्न करा.\n\nत्रुटी: ${e.message}"
                    else -> "Sorry, something went wrong. Please try again.\n\nError: ${e.message}"
                }
                onResponse(errorMessage)
                android.util.Log.e("AICook", "Error getting AI response", e)
            }
        }
    }
}

// ============== LOCATION SERVICE ==============

class LocationService : android.app.Service() {
    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient
    private lateinit var locationCallback: com.google.android.gms.location.LocationCallback
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this)

        // Create notification channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "location_service",
                "Location Service",
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(android.app.NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        // Start foreground service
        val notification = androidx.core.app.NotificationCompat.Builder(this, "location_service")
            .setContentTitle("Live Location Active")
            .setContentText("Sharing your location with family")
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)

        // Start location updates
        startLocationUpdates()

        return START_STICKY
    }

    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        // Check if permission is granted
        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
            androidx.core.app.ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, stop service
            Log.e("LocationService", "Location permission not granted")
            stopSelf()
            return
        }

        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            30000L // 30 seconds
        ).apply {
            setMinUpdateIntervalMillis(15000L) // 15 seconds
            setWaitForAccurateLocation(false)
        }.build()

        locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateLocationInFirebase(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            android.os.Looper.getMainLooper()
        )
    }

    private fun updateLocationInFirebase(location: android.location.Location) {
        if (UserSession.familyId.isNotEmpty() && UserSession.userId.isNotEmpty()) {
            // Get battery level
            val batteryManager = getSystemService(android.content.Context.BATTERY_SERVICE) as android.os.BatteryManager
            val batteryLevel = batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)

            val locationData = hashMapOf(
                "userId" to UserSession.userId,
                "userName" to UserSession.userName,
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "batteryLevel" to batteryLevel,
                "isOnline" to true,
                "isNotificationEnabled" to true,
                "lastUpdated" to com.google.firebase.Timestamp.now()
            )

            db.collection("families")
                .document(UserSession.familyId)
                .collection("memberLocations")
                .document(UserSession.userId)
                .set(locationData)
                .addOnSuccessListener {
                    Log.d("LocationService", "Location updated: ${location.latitude}, ${location.longitude}")
                }
                .addOnFailureListener { e ->
                    Log.e("LocationService", "Failed to update location", e)
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: android.content.Intent?): android.os.IBinder? = null
}

// ============== PREVIEWS ==============
@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    MaterialTheme {
        WelcomeScreen(
            onRegisterClick = {},
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    MaterialTheme {
        DashboardScreen(onNavigateToFamilyMembers = {})
    }
}