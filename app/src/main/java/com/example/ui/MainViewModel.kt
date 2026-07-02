package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Paint
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.Assessment
import com.example.data.BehaviorAssessment
import com.example.data.Attendance
import com.example.data.HealthTalk
import com.example.data.LoggedInUser
import com.example.data.Member
import com.example.data.SyncResult
import com.example.data.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface Screen {
    object Login : Screen
    object Dashboard : Screen
    object RegisterMember : Screen
    object MarkAttendance : Screen
    object RecordHealthTalk : Screen
    object MemberDirectory : Screen
    object AssessMember : Screen
    object Reports : Screen
    object SyncSettings : Screen
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository.getInstance(application)
    private val sharedPrefs = application.getSharedPreferences("zac_club_prefs", Context.MODE_PRIVATE)

    // Current screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Currently logged-in user
    private val _currentUser = MutableStateFlow<LoggedInUser?>(null)
    val currentUser: StateFlow<LoggedInUser?> = _currentUser.asStateFlow()

    // Database Flows
    val members: StateFlow<List<Member>> = repository.allMembers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val attendanceRecords: StateFlow<List<Attendance>> = repository.allAttendance.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val healthTalks: StateFlow<List<HealthTalk>> = repository.allHealthTalks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val assessments: StateFlow<List<Assessment>> = repository.allAssessments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val behaviorAssessments: StateFlow<List<BehaviorAssessment>> = repository.allBehaviorAssessments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Google Sheets Web App URL state
    private val _webAppUrl = MutableStateFlow(sharedPrefs.getString("web_app_url", "") ?: "")
    val webAppUrl: StateFlow<String> = _webAppUrl.asStateFlow()

    // Sync State
    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Form inputs: Register Member
    val regFullName = MutableStateFlow("")
    val regDob = MutableStateFlow("") // yyyy-MM-dd
    val regGender = MutableStateFlow("Male")
    val regCommunity = MutableStateFlow("")
    val regReligion = MutableStateFlow("")
    val regSchoolStatus = MutableStateFlow("Currently in school")
    val regSchoolName = MutableStateFlow("")
    val regClassYear = MutableStateFlow("")
    val regOccupation = MutableStateFlow("")
    val regGuardianName = MutableStateFlow("")
    val regRelationship = MutableStateFlow("")
    val regContactNumber = MutableStateFlow("")
    val regParentAware = MutableStateFlow("Yes")
    val regConsentSigned = MutableStateFlow("Yes")
    val regHealthConditionKnown = MutableStateFlow("No")
    val regHealthConditionDetails = MutableStateFlow("")
    val regVisitedChpsLast6Months = MutableStateFlow("No")
    val regNhisCard = MutableStateFlow("No")
    val regOathTaken = MutableStateFlow("Yes")
    val regMembershipCardIssued = MutableStateFlow("Yes")
    val regMembershipTier = MutableStateFlow("Active")

    private val _nextMemberId = MutableStateFlow("ZAC-0001")
    val nextMemberId: StateFlow<String> = _nextMemberId.asStateFlow()

    // Form inputs: Attendance Session Info
    val attSearchQuery = MutableStateFlow("")
    val attDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val attSessionType = MutableStateFlow("Weekly Meeting")
    val attStatus = MutableStateFlow("Present")

    // Form inputs: Health Talk Info
    val talkDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val talkTopic = MutableStateFlow("Sexual and reproductive health")
    val talkLocation = MutableStateFlow("")
    val talkParticipantsCount = MutableStateFlow("")
    val talkKeyIssues = MutableStateFlow("")
    val talkActionPoints = MutableStateFlow("")

    // Directory State: search and filters
    val dirSearchQuery = MutableStateFlow("")
    val dirAgeGroupFilter = MutableStateFlow("All")
    val dirCommunityFilter = MutableStateFlow("All")
    val dirSchoolStatusFilter = MutableStateFlow("All")
    val dirMembershipTierFilter = MutableStateFlow("All")

    // Form inputs: Assessment Info
    val assessMemberId = MutableStateFlow("")
    val assessMemberName = MutableStateFlow("")
    val assessHygiene = MutableStateFlow(5) // 1 to 10
    val assessCharacter = MutableStateFlow(5)
    val assessBehaviorSchool = MutableStateFlow(5)
    val assessBehaviorHome = MutableStateFlow(5)
    val assessDressing = MutableStateFlow(5)
    val assessContribution = MutableStateFlow(5)
    val assessComments = MutableStateFlow("")
    val assessPeriodType = MutableStateFlow("Weekly") // Weekly, Monthly, Quarterly, Mid-Year, Annual
    val assessPeriodLabel = MutableStateFlow("")

    // Behavior questionnaire form inputs
    val behaviorMemberId = MutableStateFlow("")
    val behaviorMemberName = MutableStateFlow("")
    val behaviorMotherObedience = MutableStateFlow(5) // 1 to 10
    val behaviorMotherHelpfulness = MutableStateFlow(5)
    val behaviorMotherCommunication = MutableStateFlow(5)
    val behaviorMotherRuleCompliance = MutableStateFlow(5)
    val behaviorTeacherPunctuality = MutableStateFlow(5)
    val behaviorTeacherAttentiveness = MutableStateFlow(5)
    val behaviorTeacherRespect = MutableStateFlow(5)
    val behaviorTeacherHomework = MutableStateFlow(5)
    val behaviorComments = MutableStateFlow("")

    // Selected member for detail popup
    private val _selectedMember = MutableStateFlow<Member?>(null)
    val selectedMember: StateFlow<Member?> = _selectedMember.asStateFlow()

    // Login screen states
    val loginUsername = MutableStateFlow("")
    val loginPassword = MutableStateFlow("")
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Report generation states
    private val _pdfGenerationStatus = MutableStateFlow<String?>(null)
    val pdfGenerationStatus: StateFlow<String?> = _pdfGenerationStatus.asStateFlow()

    init {
        updateNextMemberId()
    }

    val isEditingMember = MutableStateFlow(false)
    val editingMemberId = MutableStateFlow<String?>(null)

    fun startEditingMember(member: Member) {
        isEditingMember.value = true
        editingMemberId.value = member.memberId
        
        regFullName.value = member.fullName
        regDob.value = member.dob
        regGender.value = member.gender
        regCommunity.value = member.community
        regReligion.value = member.religion
        regSchoolStatus.value = member.schoolStatus
        regSchoolName.value = member.schoolName
        regClassYear.value = member.classYear
        regOccupation.value = member.occupation
        regGuardianName.value = member.guardianName
        regRelationship.value = member.relationship
        regContactNumber.value = member.contactNumber
        regParentAware.value = member.parentAware
        regConsentSigned.value = member.consentSigned
        regHealthConditionKnown.value = member.healthConditionKnown
        regHealthConditionDetails.value = member.healthConditionDetails
        regVisitedChpsLast6Months.value = member.visitedChpsLast6Months
        regNhisCard.value = member.nhisCard
        regOathTaken.value = member.oathTaken
        regMembershipCardIssued.value = member.membershipCardIssued
        regMembershipTier.value = member.membershipTier
        
        navigateTo(Screen.RegisterMember)
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        // Reset lte messages
        _syncStatus.value = null
        _pdfGenerationStatus.value = null
        if (screen == Screen.RegisterMember) {
            if (!isEditingMember.value) {
                resetRegisterForm()
                updateNextMemberId()
            }
        } else {
            isEditingMember.value = false
            editingMemberId.value = null
        }
    }

    fun updateNextMemberId() {
        viewModelScope.launch {
            _nextMemberId.value = repository.getNextMemberId()
        }
    }

    fun login() {
        _loginError.value = null
        val u = loginUsername.value.trim().lowercase()
        val p = loginPassword.value

        val user = when {
            u == "admin" && p == "admin123" -> LoggedInUser("admin", UserRole.ADMINISTRATOR)
            u == "facilitator" && p == "fac123" -> LoggedInUser("facilitator", UserRole.FACILITATOR)
            u == "viewer" && p == "view123" -> LoggedInUser("viewer", UserRole.VIEWER)
            else -> null
        }

        if (user != null) {
            _currentUser.value = user
            navigateTo(Screen.Dashboard)
            loginUsername.value = ""
            loginPassword.value = ""
        } else {
            _loginError.value = "Invalid credentials. Use 'admin/admin123', 'facilitator/fac123', or 'viewer/view123'."
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = Screen.Login
    }

    fun saveWebAppUrl(url: String) {
        _webAppUrl.value = url
        sharedPrefs.edit().putString("web_app_url", url).apply()
        _syncStatus.value = "Web App URL updated."
    }

    // Member ID automatic logic
    fun calculateAge(dobString: String): Int {
        return try {
            val parts = dobString.split("-")
            if (parts.size == 3) {
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                val today = Calendar.getInstance()
                var age = today.get(Calendar.YEAR) - year
                if (today.get(Calendar.MONTH) + 1 < month ||
                    (today.get(Calendar.MONTH) + 1 == month && today.get(Calendar.DAY_OF_MONTH) < day)) {
                    age--
                }
                age.coerceAtLeast(0)
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    fun registerMember(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUserRole = _currentUser.value?.role
        if (currentUserRole == UserRole.VIEWER) {
            onError("Access Denied: Viewers cannot register members.")
            return
        }

        val name = regFullName.value.trim()
        val dob = regDob.value.trim()
        val community = regCommunity.value.trim()
        val religion = regReligion.value.trim()
        val contact = regContactNumber.value.trim()
        val guardian = regGuardianName.value.trim()
        val rel = regRelationship.value.trim()

        if (name.isBlank() || dob.isBlank() || community.isBlank() || contact.isBlank() || guardian.isBlank()) {
            onError("Please fill in all required fields (Name, DOB, Community, Contact, Guardian)")
            return
        }

        viewModelScope.launch {
            try {
                val age = calculateAge(dob)
                val ageGroup = if (age <= 14) "Junior (10-14)" else "Senior (15-19)"
                val memberId = repository.getNextMemberId()
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                val m = Member(
                    memberId = memberId,
                    registrationDate = todayStr,
                    fullName = name,
                    dob = dob,
                    age = age,
                    gender = regGender.value,
                    ageGroup = ageGroup,
                    community = community,
                    religion = religion,
                    schoolStatus = regSchoolStatus.value,
                    schoolName = if (regSchoolStatus.value == "Currently in school") regSchoolName.value.trim() else "",
                    classYear = if (regSchoolStatus.value == "Currently in school") regClassYear.value.trim() else "",
                    occupation = if (regSchoolStatus.value != "Currently in school") regOccupation.value.trim() else "",
                    guardianName = guardian,
                    relationship = rel,
                    contactNumber = contact,
                    parentAware = regParentAware.value,
                    consentSigned = regConsentSigned.value,
                    healthConditionKnown = regHealthConditionKnown.value,
                    healthConditionDetails = if (regHealthConditionKnown.value == "Yes") regHealthConditionDetails.value.trim() else "",
                    visitedChpsLast6Months = regVisitedChpsLast6Months.value,
                    nhisCard = regNhisCard.value,
                    registeredBy = _currentUser.value?.username ?: "Unknown",
                    oathTaken = regOathTaken.value,
                    membershipCardIssued = regMembershipCardIssued.value,
                    membershipTier = regMembershipTier.value
                )

                repository.registerMember(m)
                resetRegisterForm()
                onSuccess()
            } catch (e: Exception) {
                onError("Failed to register member: ${e.message}")
            }
        }
    }

    fun saveEditedMember(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUserRole = _currentUser.value?.role
        if (currentUserRole == UserRole.VIEWER) {
            onError("Access Denied: Viewers cannot edit members.")
            return
        }

        val name = regFullName.value.trim()
        val dob = regDob.value.trim()
        val community = regCommunity.value.trim()
        val religion = regReligion.value.trim()
        val contact = regContactNumber.value.trim()
        val guardian = regGuardianName.value.trim()
        val rel = regRelationship.value.trim()

        if (name.isBlank() || dob.isBlank() || community.isBlank() || contact.isBlank() || guardian.isBlank()) {
            onError("Please fill in all required fields (Name, DOB, Community, Contact, Guardian)")
            return
        }

        val editId = editingMemberId.value
        if (editId == null) {
            onError("Error: No member selected for editing.")
            return
        }

        viewModelScope.launch {
            try {
                val existingMember = members.value.find { it.memberId == editId }
                if (existingMember == null) {
                    onError("Error: Member not found in database.")
                    return@launch
                }

                val age = calculateAge(dob)
                val ageGroup = if (age <= 14) "Junior (10-14)" else "Senior (15-19)"

                val updated = existingMember.copy(
                    fullName = name,
                    dob = dob,
                    age = age,
                    gender = regGender.value,
                    ageGroup = ageGroup,
                    community = community,
                    religion = religion,
                    schoolStatus = regSchoolStatus.value,
                    schoolName = if (regSchoolStatus.value == "Currently in school") regSchoolName.value.trim() else "",
                    classYear = if (regSchoolStatus.value == "Currently in school") regClassYear.value.trim() else "",
                    occupation = if (regSchoolStatus.value != "Currently in school") regOccupation.value.trim() else "",
                    guardianName = guardian,
                    relationship = rel,
                    contactNumber = contact,
                    parentAware = regParentAware.value,
                    consentSigned = regConsentSigned.value,
                    healthConditionKnown = regHealthConditionKnown.value,
                    healthConditionDetails = if (regHealthConditionKnown.value == "Yes") regHealthConditionDetails.value.trim() else "",
                    visitedChpsLast6Months = regVisitedChpsLast6Months.value,
                    nhisCard = regNhisCard.value,
                    oathTaken = regOathTaken.value,
                    membershipCardIssued = regMembershipCardIssued.value,
                    membershipTier = regMembershipTier.value,
                    isSynced = false
                )

                repository.updateMember(updated)
                isEditingMember.value = false
                editingMemberId.value = null
                resetRegisterForm()
                onSuccess()
            } catch (e: Exception) {
                onError("Failed to update member details: ${e.message}")
            }
        }
    }

    private fun resetRegisterForm() {
        regFullName.value = ""
        regDob.value = ""
        regGender.value = "Male"
        regCommunity.value = ""
        regReligion.value = ""
        regSchoolStatus.value = "Currently in school"
        regSchoolName.value = ""
        regClassYear.value = ""
        regOccupation.value = ""
        regGuardianName.value = ""
        regRelationship.value = ""
        regContactNumber.value = ""
        regParentAware.value = "Yes"
        regConsentSigned.value = "Yes"
        regHealthConditionKnown.value = "No"
        regHealthConditionDetails.value = ""
        regVisitedChpsLast6Months.value = "No"
        regNhisCard.value = "No"
        regOathTaken.value = "Yes"
        regMembershipCardIssued.value = "Yes"
        regMembershipTier.value = "Active"
    }

    fun logAttendance(member: Member, status: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUserRole = _currentUser.value?.role
        if (currentUserRole == UserRole.VIEWER) {
            onError("Access Denied: Viewers cannot mark attendance.")
            return
        }
        viewModelScope.launch {
            try {
                val att = Attendance(
                    date = attDate.value,
                    memberId = member.memberId,
                    memberName = member.fullName,
                    sessionType = attSessionType.value,
                    status = status,
                    facilitator = _currentUser.value?.username ?: "Unknown"
                )
                
                var isSynced = false
                val url = _webAppUrl.value
                if (url.isNotBlank()) {
                    try {
                        isSynced = repository.recordAttendanceOnline(att, url)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                val finalAtt = att.copy(isSynced = isSynced)
                repository.recordAttendance(finalAtt)
                
                onSuccess()
            } catch (e: Exception) {
                onError("Failed to record attendance: ${e.message}")
            }
        }
    }

    fun saveHealthTalk(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUserRole = _currentUser.value?.role
        if (currentUserRole == UserRole.VIEWER) {
            onError("Access Denied: Viewers cannot record health education sessions.")
            return
        }
        val topic = talkTopic.value
        val loc = talkLocation.value.trim()
        val partsCount = talkParticipantsCount.value.trim().toIntOrNull() ?: 0
        val issues = talkKeyIssues.value.trim()
        val actions = talkActionPoints.value.trim()

        if (loc.isBlank() || partsCount <= 0 || issues.isBlank()) {
            onError("Please fill in location, participant count (>0), and key issues.")
            return
        }

        viewModelScope.launch {
            try {
                val talk = HealthTalk(
                    date = talkDate.value,
                    topic = topic,
                    location = loc,
                    participantsCount = partsCount,
                    keyIssues = issues,
                    actionPoints = actions,
                    facilitator = _currentUser.value?.username ?: "Unknown"
                )
                repository.recordHealthTalk(talk)
                resetHealthTalkForm()
                onSuccess()
            } catch (e: Exception) {
                onError("Failed to save health talk: ${e.message}")
            }
        }
    }

    private fun resetHealthTalkForm() {
        talkLocation.value = ""
        talkParticipantsCount.value = ""
        talkKeyIssues.value = ""
        talkActionPoints.value = ""
    }

    fun syncData() {
        val currentUserRole = _currentUser.value?.role
        if (currentUserRole == UserRole.VIEWER) {
            _syncStatus.value = "Access Denied: Viewers cannot perform cloud synchronization."
            return
        }
        _isSyncing.value = true
        _syncStatus.value = "Syncing with Google Sheets..."
        viewModelScope.launch {
            val result = repository.syncWithGoogleSheets(_webAppUrl.value)
            _syncStatus.value = result.message
            _isSyncing.value = false
        }
    }

    fun selectMember(member: Member?) {
        _selectedMember.value = member
    }

    // Dynamic Filtered Directory List
    @Suppress("UNCHECKED_CAST")
    val filteredMembers: StateFlow<List<Member>> = combine(
        members,
        dirSearchQuery,
        dirAgeGroupFilter,
        dirCommunityFilter,
        dirSchoolStatusFilter,
        dirMembershipTierFilter
    ) { flows: Array<Any> ->
        val list = flows[0] as List<Member>
        val query = flows[1] as String
        val ageGrp = flows[2] as String
        val comm = flows[3] as String
        val school = flows[4] as String
        val tier = flows[5] as String

        list.filter { m ->
            val matchesQuery = m.fullName.contains(query, ignoreCase = true) || m.memberId.contains(query, ignoreCase = true)
            val matchesAge = ageGrp == "All" || m.ageGroup.contains(ageGrp, ignoreCase = true)
            val matchesComm = comm == "All" || m.community.equals(comm, ignoreCase = true)
            val matchesSchool = school == "All" || m.schoolStatus.equals(school, ignoreCase = true)
            val matchesTier = tier == "All" || m.membershipTier.equals(tier, ignoreCase = true)

            matchesQuery && matchesAge && matchesComm && matchesSchool && matchesTier
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Calculate dynamically: Member attendance statistics
    fun getMemberAttendanceStats(memberId: String): MemberAttendanceStats {
        val allAtt = attendanceRecords.value.filter { it.memberId == memberId }
        val sessionsConducted = allAtt.size
        val attended = allAtt.count { it.status.lowercase() == "present" }
        val percentage = if (sessionsConducted > 0) (attended.toFloat() / sessionsConducted * 100).toInt() else 0
        return MemberAttendanceStats(attended, sessionsConducted, percentage)
    }

    // PDF REPORT GENERATOR using Android native PdfDocument
    fun generatePdfReport(context: Context, periodType: String, onFinished: (File?) -> Unit) {
        _pdfGenerationStatus.value = "Generating PDF..."
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pdfDocument = android.graphics.pdf.PdfDocument()
                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                val paint = Paint()
                val textPaint = Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 12f
                    isAntiAlias = true
                }

                val titlePaint = Paint().apply {
                    color = android.graphics.Color.rgb(0, 121, 107) // TealPrimary
                    textSize = 20f
                    isFakeBoldText = true
                    isAntiAlias = true
                }

                val headerPaint = Paint().apply {
                    color = android.graphics.Color.rgb(0, 77, 64)
                    textSize = 14f
                    isFakeBoldText = true
                    isAntiAlias = true
                }

                val borderPaint = Paint().apply {
                    color = android.graphics.Color.LTGRAY
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }

                var y = 50f

                // Draw Header
                canvas.drawText("ZONGOIRE ADOLESCENTS' HEALTH CLUB SYSTEM", 40f, y, titlePaint)
                y += 25f
                canvas.drawText("Official Program Report", 40f, y, headerPaint)
                y += 20f

                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                textPaint.textSize = 10f
                textPaint.color = android.graphics.Color.GRAY
                canvas.drawText("Generated on: $dateStr | Scope: $periodType", 40f, y, textPaint)
                y += 25f

                // Draw solid teal line separator
                paint.color = android.graphics.Color.rgb(0, 121, 107)
                canvas.drawRect(40f, y, 555f, y + 3f, paint)
                y += 25f

                // General Stats Header
                textPaint.textSize = 12f
                textPaint.color = android.graphics.Color.BLACK
                canvas.drawText("CLUB OVERVIEW & SUMMARY STATISTICS", 40f, y, headerPaint)
                y += 20f

                val mList = members.value
                val totalM = mList.size
                val juniorM = mList.count { it.ageGroup.contains("Junior") }
                val seniorM = mList.count { it.ageGroup.contains("Senior") }
                val activeM = mList.count { it.membershipTier.lowercase() == "active" }
                val starM = mList.count { it.membershipTier.lowercase() == "star" }

                canvas.drawText("Total Registered Members: $totalM", 50f, y, textPaint)
                y += 18f
                canvas.drawText("Junior Members (10-14): $juniorM", 50f, y, textPaint)
                y += 18f
                canvas.drawText("Senior Members (15-19): $seniorM", 50f, y, textPaint)
                y += 18f
                canvas.drawText("Membership Tiers: Active ($activeM) | Star ($starM) | Associate (${totalM - activeM - starM})", 50f, y, textPaint)
                y += 25f

                // Divider line
                canvas.drawLine(40f, y, 555f, y, borderPaint)
                y += 20f

                // Attendance Section
                canvas.drawText("ATTENDANCE SUMMARY", 40f, y, headerPaint)
                y += 20f

                val attList = attendanceRecords.value
                val totalAttLogs = attList.size
                val presentLogs = attList.count { it.status.lowercase() == "present" }
                val overallAttRate = if (totalAttLogs > 0) (presentLogs.toFloat() / totalAttLogs * 100).toInt() else 0

                canvas.drawText("Total Attendance Records logged: $totalAttLogs", 50f, y, textPaint)
                y += 18f
                canvas.drawText("Present logs: $presentLogs | Absent logs: ${totalAttLogs - presentLogs}", 50f, y, textPaint)
                y += 18f
                canvas.drawText("Overall Club Attendance Rate: $overallAttRate%", 50f, y, textPaint)
                y += 25f

                // Divider line
                canvas.drawLine(40f, y, 555f, y, borderPaint)
                y += 20f

                // Health Talks Section
                canvas.drawText("HEALTH EDUCATION ACTIVITIES DELIVERED", 40f, y, headerPaint)
                y += 20f

                val talksList = healthTalks.value
                canvas.drawText("Total Health Talks Conducted: ${talksList.size}", 50f, y, textPaint)
                y += 18f

                if (talksList.isNotEmpty()) {
                    val topicsCount = talksList.groupBy { it.topic }.mapValues { it.value.size }
                    canvas.drawText("Topics Distribution:", 50f, y, textPaint)
                    y += 18f
                    topicsCount.forEach { (topic, count) ->
                        if (y < 780f) {
                            canvas.drawText("  • $topic: $count session(s)", 60f, y, textPaint)
                            y += 15f
                        }
                    }
                } else {
                    canvas.drawText("  No health education talks recorded yet.", 60f, y, textPaint)
                    y += 15f
                }

                y += 25f
                canvas.drawText("CONFIDENTIAL HEALTH OVERVIEW (Authorized Facilitators Only)", 40f, y, headerPaint)
                y += 20f
                val knownConditions = mList.count { it.healthConditionKnown.lowercase() == "yes" }
                canvas.drawText("Members with known health conditions tracked by facilitators: $knownConditions", 50f, y, textPaint)
                y += 18f
                val nhisHolders = mList.count { it.nhisCard.lowercase() == "yes" }
                canvas.drawText("Members with valid National Health Insurance Scheme (NHIS) card: $nhisHolders", 50f, y, textPaint)
                y += 40f

                // Footer sign-off
                canvas.drawText("Certified by Zongoire CHPS Facilitator Team", 40f, y, textPaint)
                y += 15f
                canvas.drawLine(40f, y, 240f, y, borderPaint)

                pdfDocument.finishPage(page)

                // Save PDF to cache/files directory
                val dir = File(context.cacheDir, "reports")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, "ZAC_Club_Report_${periodType.replace(" ", "_")}.pdf")

                val out = FileOutputStream(file)
                pdfDocument.writeTo(out)
                out.flush()
                out.close()
                pdfDocument.close()

                _pdfGenerationStatus.value = "PDF Generated: ${file.name}"
                onFinished(file)

            } catch (e: Exception) {
                e.printStackTrace()
                _pdfGenerationStatus.value = "Failed to generate PDF: ${e.message}"
                onFinished(null)
            }
        }
    }

    fun saveAssessment(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUserRole = _currentUser.value?.role
        if (currentUserRole == UserRole.VIEWER) {
            onError("Access Denied: Viewers cannot record assessments.")
            return
        }
        val memberId = assessMemberId.value
        val name = assessMemberName.value
        if (memberId.isBlank() || name.isBlank()) {
            onError("Please select a member to assess.")
            return
        }
        val label = assessPeriodLabel.value.trim()
        if (label.isBlank()) {
            onError("Please enter a period label (e.g. Week 27, 2026).")
            return
        }

        viewModelScope.launch {
            try {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val assessment = Assessment(
                    date = todayStr,
                    memberId = memberId,
                    memberName = name,
                    hygiene = assessHygiene.value,
                    character = assessCharacter.value,
                    behaviorSchool = assessBehaviorSchool.value,
                    behaviorHome = assessBehaviorHome.value,
                    dressing = assessDressing.value,
                    contribution = assessContribution.value,
                    comments = assessComments.value,
                    assessedBy = _currentUser.value?.username ?: "Unknown",
                    periodType = assessPeriodType.value,
                    periodLabel = label
                )
                repository.recordAssessment(assessment)
                resetAssessmentForm()
                onSuccess()
            } catch (e: Exception) {
                onError("Failed to record assessment: ${e.message}")
            }
        }
    }

    fun resetAssessmentForm() {
        assessMemberId.value = ""
        assessMemberName.value = ""
        assessHygiene.value = 5
        assessCharacter.value = 5
        assessBehaviorSchool.value = 5
        assessBehaviorHome.value = 5
        assessDressing.value = 5
        assessContribution.value = 5
        assessComments.value = ""
        assessPeriodLabel.value = ""
    }

    fun submitBehaviorAssessment(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val memberId = behaviorMemberId.value
        val name = behaviorMemberName.value
        if (memberId.isBlank() || name.isBlank()) {
            onError("Please select a member to assess.")
            return
        }

        viewModelScope.launch {
            try {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val assessment = BehaviorAssessment(
                    date = todayStr,
                    memberId = memberId,
                    memberName = name,
                    motherObedience = behaviorMotherObedience.value,
                    motherHelpfulness = behaviorMotherHelpfulness.value,
                    motherCommunication = behaviorMotherCommunication.value,
                    motherRuleCompliance = behaviorMotherRuleCompliance.value,
                    teacherPunctuality = behaviorTeacherPunctuality.value,
                    teacherAttentiveness = behaviorTeacherAttentiveness.value,
                    teacherRespect = behaviorTeacherRespect.value,
                    teacherHomework = behaviorTeacherHomework.value,
                    comments = behaviorComments.value,
                    assessedBy = _currentUser.value?.username ?: "Unknown"
                )
                repository.recordBehaviorAssessment(assessment)
                resetBehaviorAssessmentForm()
                onSuccess()
            } catch (e: Exception) {
                onError("Failed to record behavior assessment: ${e.message}")
            }
        }
    }

    fun resetBehaviorAssessmentForm() {
        behaviorMemberId.value = ""
        behaviorMemberName.value = ""
        behaviorMotherObedience.value = 5
        behaviorMotherHelpfulness.value = 5
        behaviorMotherCommunication.value = 5
        behaviorMotherRuleCompliance.value = 5
        behaviorTeacherPunctuality.value = 5
        behaviorTeacherAttentiveness.value = 5
        behaviorTeacherRespect.value = 5
        behaviorTeacherHomework.value = 5
        behaviorComments.value = ""
    }
}

data class MemberAttendanceStats(
    val sessionsAttended: Int,
    val sessionsConducted: Int,
    val attendancePercentage: Int
)
