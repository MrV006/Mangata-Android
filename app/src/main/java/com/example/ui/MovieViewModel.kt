package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.*
import com.example.myket.MyketBillingHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieViewModel(application: Application) : AndroidViewModel(application) {

    private val LOG_TAG = "MangaViewModel"
    private val database = MangaDatabase.getDatabase(application)
    private val repository = MangaRepository(database.mangaDao())
    val myketHelper = MyketBillingHelper(application)

    // Base context/Prefs for domain setting
    private val sharedPrefs = application.getSharedPreferences("mangata_prefs", android.content.Context.MODE_PRIVATE)

    // Domain setting state
    private val _siteDomain = MutableStateFlow(sharedPrefs.getString("domain_url", "https://mangata.site") ?: "https://mangata.site")
    val siteDomain: StateFlow<String> = _siteDomain.asStateFlow()

    private val _isDomainConnected = MutableStateFlow(true)
    val isDomainConnected: StateFlow<Boolean> = _isDomainConnected.asStateFlow()

    // UI state streams
    val mangas: StateFlow<List<MangaEntity>> = repository.allMangas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<Bookmark>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val readHistory: StateFlow<List<ReadHistory>> = repository.readHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPurchases: StateFlow<List<UserPurchase>> = repository.userPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teamMembers: StateFlow<List<TeamMember>> = repository.allTeamMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Advanced features streams
    private val _currentUserId = MutableStateFlow(6)
    val currentUserId: StateFlow<Int> = _currentUserId.asStateFlow()

    val userAccounts: StateFlow<List<UserAccount>> = repository.allUserAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUserAccount: StateFlow<UserAccount?> = combine(userAccounts, _currentUserId) { accounts, id ->
        accounts.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val systemSettings: StateFlow<SystemSettingsEntity> = repository.systemSettings
        .map { it ?: SystemSettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SystemSettingsEntity())

    val recruitmentApps: StateFlow<List<RecruitmentApplication>> = repository.allRecruitments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stories: StateFlow<List<StoryEntity>> = repository.allStories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _serverVersionCode = MutableStateFlow(2) // Defaults to app version code 2
    val serverVersionCode: StateFlow<Int> = _serverVersionCode.asStateFlow()

    // Featured Slider IDs
    private val _featuredMangaIds = MutableStateFlow(listOf(1, 2, 3))
    val featuredMangaIds: StateFlow<List<Int>> = _featuredMangaIds.asStateFlow()

    // Starts from zero configuration map per manga ID
    private val _mangaStartsFromZero = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val mangaStartsFromZero: StateFlow<Map<Int, Boolean>> = _mangaStartsFromZero.asStateFlow()

    // Simulating Workflow Upload states
    data class UploadWorkflowState(
        val translatorWordFile: String? = null,
        val cleanerZipFile: String? = null,
        val typesetterZipFile: String? = null,
        val isWordDraftDeleted: Boolean = false,
        val isCleanerZipDraftDeleted: Boolean = false,
        val hasFinishedCompilation: Boolean = false,
        val outputZipFileName: String? = null,
        val chosenSequenceStart: Int = 1, // 0 or 1
        val chapterToPublish: Int = 0
    )

    private val _uploadWorkflow = MutableStateFlow<Map<Int, UploadWorkflowState>>(emptyMap())
    val uploadWorkflow: StateFlow<Map<Int, UploadWorkflowState>> = _uploadWorkflow.asStateFlow()

    fun getPurchasedChapters(userId: Int): Flow<List<ChapterPurchaseRecord>> = repository.getPurchasedChapters(userId)
    fun isChapterUnlocked(userId: Int, mangaId: Int, chapterNumber: Int): Flow<Boolean> = repository.isChapterUnlocked(userId, mangaId, chapterNumber)

    // Active VIP status based on active purchases in the database (to unlock advanced early chapters)
    val isVipActive: StateFlow<Boolean> = repository.userPurchases
        .map { list -> list.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Currently Selected Tab (0 = Home, 1 = Bookmarks, 2 = Team/Collaborators, 3 = Store, 4 = Settings/Domain Connection)
    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    // Currently Selected Manga for Detailed View Panel
    private val _selectedManga = MutableStateFlow<MangaEntity?>(null)
    val selectedManga: StateFlow<MangaEntity?> = _selectedManga.asStateFlow()

    // Gemini AI Summary State (Idle, Loading, Success, Error)
    private val _aiSummaryState = MutableStateFlow<AiSummaryState>(AiSummaryState.Idle)
    val aiSummaryState: StateFlow<AiSummaryState> = _aiSummaryState.asStateFlow()

    // Active Manga Reader state
    private val _readingManga = MutableStateFlow<MangaEntity?>(null)
    val readingManga: StateFlow<MangaEntity?> = _readingManga.asStateFlow()

    private val _activeChapter = MutableStateFlow(1)
    val activeChapter: StateFlow<Int> = _activeChapter.asStateFlow()

    // Reader UI configurations
    private val _isVerticalWebtoonMode = MutableStateFlow(true)
    val isVerticalWebtoonMode: StateFlow<Boolean> = _isVerticalWebtoonMode.asStateFlow()

    // Myket billing screen state
    private val _showMyketBillingDialog = MutableStateFlow(false)
    val showMyketBillingDialog: StateFlow<Boolean> = _showMyketBillingDialog.asStateFlow()

    private val _selectedSkuToBuy = MutableStateFlow<MyketBillingHelper.SkuDetails?>(null)
    val selectedSkuToBuy: StateFlow<MyketBillingHelper.SkuDetails?> = _selectedSkuToBuy.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        // Initialize setup and seeds
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val current = database.mangaDao().getAllMangas().first()
                if (current.isEmpty()) {
                    Log.d(LOG_TAG, "Seeding database with popular mangas and team members...")
                    repository.seedDatabase()
                }
            }

            // Bind to Myket billing service for IAP memberships
            myketHelper.startSetup { result ->
                if (result.isSuccess) {
                    Log.d(LOG_TAG, "Myket Billing Service Connected inside MangaViewModel.")
                    queryMyketOwnedPurchases()
                } else {
                    Log.e(LOG_TAG, "Failed to connect to Myket service: ${result.message}")
                }
            }
        }
    }

    private fun queryMyketOwnedPurchases() {
        viewModelScope.launch {
            val dbPurchases = userPurchases.first()
            val ownedSkusFromDb = dbPurchases.map { it.sku }

            myketHelper.queryInventory(
                skus = listOf(myketHelper.SKU_VIP_1MONTH, myketHelper.SKU_VIP_3MONTH, myketHelper.SKU_VIP_LIFETIME),
                currentlyOwnedSkus = ownedSkusFromDb
            ) { result, purchases ->
                if (result.isSuccess) {
                    Log.d(LOG_TAG, "Myket query returned ${purchases.size} verified purchases.")
                    viewModelScope.launch(Dispatchers.IO) {
                        purchases.forEach { purchase ->
                            val alreadySaved = ownedSkusFromDb.contains(purchase.sku)
                            if (!alreadySaved) {
                                val skuName = myketHelper.skuDetailsMap[purchase.sku]?.title ?: "اشتراک ویژه"
                                repository.addPurchase(
                                    UserPurchase(
                                        sku = purchase.sku,
                                        purchaseTime = purchase.purchaseTime,
                                        token = purchase.purchaseToken,
                                        orderId = purchase.orderId,
                                        skuNameFa = skuName
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        _activeTab.value = tabIndex
        if (tabIndex != 0) {
            _selectedManga.value = null
        }
    }

    fun selectManga(manga: MangaEntity?) {
        _selectedManga.value = manga
        if (manga != null) {
            loadAiStorySummary(manga)
        } else {
            _aiSummaryState.value = AiSummaryState.Idle
        }
    }

    fun updateSiteDomain(domain: String) {
        _siteDomain.value = domain
        sharedPrefs.edit().putString("domain_url", domain).apply()
        // Simulate dynamic testing of domain REST API structure
        viewModelScope.launch {
            _isDomainConnected.value = false
            kotlinx.coroutines.delay(1200) // Realistic server ping check
            _isDomainConnected.value = true
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleBookmark(id: Int, isFav: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleBookmark(id, isFav)
        }
    }

    fun openReader(manga: MangaEntity, chapter: Int = 1) {
        // VIP Check: Early chapters (e.g., chapter > 3) are locked for non-VIPs
        if (chapter > 3 && manga.isPremium && !isVipActive.value) {
            triggerMyketPurchase(myketHelper.SKU_VIP_1MONTH)
            return
        }
        _readingManga.value = manga
        _activeChapter.value = chapter
    }

    fun closeReader() {
        _readingManga.value = null
    }

    fun setChapter(chapter: Int) {
        val manga = _readingManga.value ?: return
        if (chapter > 3 && manga.isPremium && !isVipActive.value) {
            triggerMyketPurchase(myketHelper.SKU_VIP_1MONTH)
            return
        }
        if (chapter in 1..manga.chaptersCount) {
            _activeChapter.value = chapter
            saveReadingProgress(manga.id, chapter, 0.0f)
        }
    }

    fun toggleReaderDirection() {
        _isVerticalWebtoonMode.value = !_isVerticalWebtoonMode.value
    }

    fun saveReadingProgress(mangaId: Int, chapter: Int, progress: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveReadHistory(mangaId, chapter, progress)
        }
    }

    fun isBookmarked(mangaId: Int): Flow<Boolean> {
        return repository.isBookmarked(mangaId)
    }

    fun getHistoryForManga(mangaId: Int): Flow<ReadHistory?> {
        return repository.getHistoryForManga(mangaId)
    }

    fun triggerMyketPurchase(sku: String) {
        val details = myketHelper.skuDetailsMap[sku]
        if (details != null) {
            _selectedSkuToBuy.value = details
            _showMyketBillingDialog.value = true
        }
    }

    fun closeMyketCheckout() {
        _showMyketBillingDialog.value = false
        _selectedSkuToBuy.value = null
    }

    fun completeSimulatedPurchase(sku: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mockPurchase = myketHelper.generateSuccessfulPurchase(
                    sku = sku,
                    developerPayload = "payload_${System.currentTimeMillis()}"
                )

                val skuDetails = myketHelper.skuDetailsMap[sku]
                val farsiTitle = skuDetails?.title ?: "اشتراک ویژه مانگاتا"

                repository.addPurchase(
                    UserPurchase(
                        sku = sku,
                        purchaseTime = mockPurchase.purchaseTime,
                        token = mockPurchase.purchaseToken,
                        orderId = mockPurchase.orderId,
                        skuNameFa = farsiTitle
                    )
                )

                withContext(Dispatchers.Main) {
                    closeMyketCheckout()
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error writing purchase record to DB: ${e.message}")
            }
        }
    }

    fun cancelVipSubscription() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllPurchases()
        }
    }

    // Role level and translation collaborator assignments
    fun addNewTeamMember(name: String, role: String, works: String, level: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTeamMember(
                TeamMember(
                    name = name,
                    roleFa = role,
                    levelCode = level,
                    assignedWorks = works
                )
            )
        }
    }

    fun deleteMember(member: TeamMember) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeTeamMember(member)
        }
    }

    // --- Advanced Features Interactive Methods ---

    fun switchUser(userId: Int) {
        _currentUserId.value = userId
    }

    fun updateSystemSettings(settings: SystemSettingsEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertSystemSettings(settings)
        }
    }

    fun payWalletTopup(amount: Long) {
        val user = currentUserAccount.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertUserAccount(user.copy(walletRial = user.walletRial + amount))
        }
    }

    fun awardGiftChapters(userId: Int, count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            userAccounts.value.find { it.id == userId }?.let { targetUser ->
                repository.insertUserAccount(targetUser.copy(walletGiftChapters = targetUser.walletGiftChapters + count))
            }
        }
    }

    fun purchaseSingleChapter(mangaId: Int, chapterNumber: Int, useGiftPoints: Boolean, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val user = currentUserAccount.value ?: return
        val settings = systemSettings.value
        val cost = settings.baseChapterPrice

        viewModelScope.launch(Dispatchers.IO) {
            if (useGiftPoints) {
                if (user.walletGiftChapters >= 1) {
                    val updatedUser = user.copy(walletGiftChapters = user.walletGiftChapters - 1)
                    repository.insertUserAccount(updatedUser)
                    repository.insertChapterPurchase(
                        ChapterPurchaseRecord(
                            userId = user.id,
                            mangaId = mangaId,
                            chapterNumber = chapterNumber,
                            purchaseTime = System.currentTimeMillis()
                        )
                    )
                    withContext(Dispatchers.Main) { onSuccess() }
                } else {
                    withContext(Dispatchers.Main) { onError("شما چپتر هدیه کافی در کیف پول ندارید.") }
                }
            } else {
                if (user.walletRial >= cost) {
                    val updatedUser = user.copy(walletRial = user.walletRial - cost)
                    repository.insertUserAccount(updatedUser)

                    val cleanerShare = (cost * settings.shareCleanerPct / 100).toLong()
                    val editorShare = (cost * settings.shareEditorPct / 100).toLong()
                    val translatorShare = (cost * settings.shareTranslatorPct / 100).toLong()
                    val platformShare = (cost * settings.sharePlatformPct / 100).toLong()

                    userAccounts.value.forEach { account ->
                        var modifiedUser = account
                        var updated = false
                        if (account.id == 3) {
                            modifiedUser = account.copy(walletRial = account.walletRial + cleanerShare)
                            updated = true
                        } else if (account.id == 4) {
                            modifiedUser = account.copy(walletRial = account.walletRial + editorShare)
                            updated = true
                        } else if (account.id == 5) {
                            modifiedUser = account.copy(walletRial = account.walletRial + translatorShare)
                            updated = true
                        } else if (account.id == 1) {
                            modifiedUser = account.copy(walletRial = account.walletRial + platformShare)
                            updated = true
                        }

                        if (updated) {
                            repository.insertUserAccount(modifiedUser)
                        }
                    }

                    repository.insertChapterPurchase(
                        ChapterPurchaseRecord(
                            userId = user.id,
                            mangaId = mangaId,
                            chapterNumber = chapterNumber,
                            purchaseTime = System.currentTimeMillis()
                        )
                    )
                    withContext(Dispatchers.Main) { onSuccess() }
                } else {
                    withContext(Dispatchers.Main) { onError("کیف پول ریالی شما موجودی کافی ندارد.") }
                }
            }
        }
    }

    fun getBulkChaptersPrice(count: Int): Int {
        val settings = systemSettings.value
        val rawCost = settings.baseChapterPrice * count
        return if (count >= 100) {
            rawCost * (100 - settings.discountPercent100) / 100
        } else if (count >= 50) {
            rawCost * (100 - settings.discountPercent50) / 100
        } else {
            rawCost
        }
    }

    fun purchaseBulkChapters(mangaId: Int, startChapter: Int, count: Int, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val user = currentUserAccount.value ?: return
        val discountedCost = getBulkChaptersPrice(count)

        viewModelScope.launch(Dispatchers.IO) {
            if (user.walletRial >= discountedCost) {
                val updatedUser = user.copy(walletRial = user.walletRial - discountedCost)
                repository.insertUserAccount(updatedUser)

                val settings = systemSettings.value
                val cleanerShare = (discountedCost * settings.shareCleanerPct / 100).toLong()
                val editorShare = (discountedCost * settings.shareEditorPct / 100).toLong()
                val translatorShare = (discountedCost * settings.shareTranslatorPct / 100).toLong()
                val platformShare = (discountedCost * settings.sharePlatformPct / 100).toLong()

                userAccounts.value.forEach { account ->
                    var modifiedUser = account
                    var updated = false
                    if (account.id == 3) {
                        modifiedUser = account.copy(walletRial = account.walletRial + cleanerShare)
                        updated = true
                    } else if (account.id == 4) {
                        modifiedUser = account.copy(walletRial = account.walletRial + editorShare)
                        updated = true
                    } else if (account.id == 5) {
                        modifiedUser = account.copy(walletRial = account.walletRial + translatorShare)
                        updated = true
                    } else if (account.id == 1) {
                        modifiedUser = account.copy(walletRial = account.walletRial + platformShare)
                        updated = true
                    }

                    if (updated) {
                        repository.insertUserAccount(modifiedUser)
                    }
                }

                for (ch in startChapter until (startChapter + count)) {
                    repository.insertChapterPurchase(
                        ChapterPurchaseRecord(
                            userId = user.id,
                            mangaId = mangaId,
                            chapterNumber = ch,
                            purchaseTime = System.currentTimeMillis()
                        )
                    )
                }
                withContext(Dispatchers.Main) { onSuccess() }
            } else {
                withContext(Dispatchers.Main) { onError("کیف پول ریالی شما موجودی کافی ندارد.") }
            }
        }
    }

    fun addStaffContribution(staffId: Int, countAdded: Int = 1) {
        viewModelScope.launch(Dispatchers.IO) {
            userAccounts.value.find { it.id == staffId }?.let { staff ->
                val rewardRate = staff.customRewardRate ?: systemSettings.value.defaultStaffRewardChapters
                val rewardedChapters = countAdded * rewardRate
                val updatedStaff = staff.copy(
                    walletGiftChapters = staff.walletGiftChapters + rewardedChapters,
                    chaptersContributedThisMonth = staff.chaptersContributedThisMonth + countAdded
                )
                repository.insertUserAccount(updatedStaff)
            }
        }
    }

    fun postStory(mediaUrl: String, caption: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val user = currentUserAccount.value ?: return
        if (user.role == "SUPER_ADMIN" || user.storyTokens > 0) {
            viewModelScope.launch(Dispatchers.IO) {
                val updatedUser = if (user.role == "SUPER_ADMIN") user else user.copy(storyTokens = user.storyTokens - 1)
                repository.insertUserAccount(updatedUser)

                val story = StoryEntity(
                    staffId = user.id,
                    staffName = user.displayName,
                    staffRole = user.subRole,
                    mediaUrl = mediaUrl,
                    caption = caption,
                    uploadTime = System.currentTimeMillis()
                )
                repository.insertStory(story)
                withContext(Dispatchers.Main) { onSuccess() }
            }
        } else {
            onError("شما توکن ارسال استوری کافی ندارید! (ارسال استوری نیاز به حداقل ۴۰ فعالیت در ماه قبل دارد)")
        }
    }

    fun deleteStory(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeStory(id)
        }
    }

    fun simulateNewMonth() {
        val settings = systemSettings.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllStories()

            userAccounts.value.forEach { account ->
                val qualified = account.chaptersContributedThisMonth >= settings.minChaptersForStoryToken
                val awardedTokens = if (qualified) settings.storyTokensAwarded else 0

                val refreshed = account.copy(
                    storyTokens = awardedTokens,
                    chaptersContributedLastMonth = account.chaptersContributedThisMonth,
                    chaptersContributedThisMonth = 0
                )
                repository.insertUserAccount(refreshed)
            }
        }
    }

    fun applyForRecruitment(fullName: String, messengerId: String, specialty: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val app = RecruitmentApplication(
                fullName = fullName,
                messengerId = messengerId,
                specialty = specialty,
                testFileName = "فایل_خام_تست_${specialty}.zip",
                uploadedWorkName = "پاسخ_تست_کاربر_${fullName}.zip",
                status = "PENDING",
                dateSubmitted = "امروز"
            )
            repository.insertRecruitment(app)
            withContext(Dispatchers.Main) { onSuccess() }
        }
    }

    fun reviewRecruitment(app: RecruitmentApplication, approve: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val newStatus = if (approve) "APPROVED" else "REJECTED"
            repository.insertRecruitment(app.copy(status = newStatus))

            if (approve) {
                val guest = userAccounts.value.find { it.id == 6 }
                if (guest != null) {
                    val upgraded = guest.copy(
                        displayName = app.fullName,
                        role = "STAFF",
                        subRole = app.specialty,
                        storyTokens = 2
                    )
                    repository.insertUserAccount(upgraded)
                }
            }
        }
    }

    fun updateServerVersionCode(code: Int) {
        _serverVersionCode.value = code
    }

    fun toggleFeaturedManga(mangaId: Int) {
        val currentList = _featuredMangaIds.value.toMutableList()
        if (currentList.contains(mangaId)) {
            currentList.remove(mangaId)
        } else {
            currentList.add(mangaId)
        }
        _featuredMangaIds.value = currentList
    }

    fun setMangaStartsFromZero(mangaId: Int, startsFromZero: Boolean) {
        val currentMap = _mangaStartsFromZero.value.toMutableMap()
        currentMap[mangaId] = startsFromZero
        _mangaStartsFromZero.value = currentMap
    }

    fun uploadWorkflowProgress(mangaId: Int, fileType: String, fileName: String) {
        val currentMap = _uploadWorkflow.value.toMutableMap()
        val currentState = currentMap[mangaId] ?: UploadWorkflowState()
        val newState = when (fileType) {
            "WORD" -> currentState.copy(translatorWordFile = fileName, isWordDraftDeleted = false)
            "CLEANER_ZIP" -> currentState.copy(cleanerZipFile = fileName, isCleanerZipDraftDeleted = false)
            "EDITOR_ZIP" -> currentState.copy(typesetterZipFile = fileName, hasFinishedCompilation = false)
            else -> currentState
        }
        currentMap[mangaId] = newState
        _uploadWorkflow.value = currentMap
    }

    fun approveAndPublishWorkflow(mangaId: Int, chapterNumber: Int, sequenceStart: Int) {
        val currentMap = _uploadWorkflow.value.toMutableMap()
        val currentState = currentMap[mangaId] ?: UploadWorkflowState()
        
        val newState = currentState.copy(
            isWordDraftDeleted = true,
            isCleanerZipDraftDeleted = true,
            hasFinishedCompilation = true,
            outputZipFileName = "compiled_chapter_${chapterNumber}_images_sequenced.zip",
            chosenSequenceStart = sequenceStart,
            chapterToPublish = chapterNumber
        )
        currentMap[mangaId] = newState
        _uploadWorkflow.value = currentMap

        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.allMangas.first()
            val manga = list.find { it.id == mangaId } ?: return@launch
            
            val updatedManga = manga.copy(
                chaptersCount = maxOf(manga.chaptersCount + 1, chapterNumber),
                pagesJson = """[
                    "https://picsum.photos/id/1010/800/1200",
                    "https://picsum.photos/id/1011/800/1200",
                    "https://picsum.photos/id/1012/800/1200",
                    "https://picsum.photos/id/1013/800/1200"
                ]"""
            )
            repository.updateManga(updatedManga)
            
            val user = currentUserAccount.value
            if (user != null && user.role == "STAFF") {
                repository.insertUserAccount(user.copy(walletGiftChapters = user.walletGiftChapters + 5))
            }
        }
    }

    fun deleteOrReplaceChapter(mangaId: Int, chapterNumber: Int, isReplace: Boolean, customPages: List<String> = emptyList()) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.allMangas.first()
            val manga = list.find { it.id == mangaId } ?: return@launch
            val updatedManga = if (isReplace) {
                val mockPages = if (customPages.isNotEmpty()) customPages else listOf(
                    "https://picsum.photos/id/1021/800/1200",
                    "https://picsum.photos/id/1022/800/1200",
                    "https://picsum.photos/id/1023/800/1200"
                )
                manga.copy(pagesJson = org.json.JSONArray(mockPages).toString())
            } else {
                manga.copy(chaptersCount = maxOf(1, manga.chaptersCount - 1))
            }
            repository.updateManga(updatedManga)
        }
    }

    fun updateMangaDetails(mangaId: Int, titleFa: String, titleEn: String, descriptionFa: String, coverUrl: String, bannerUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.allMangas.first()
            val manga = list.find { it.id == mangaId } ?: return@launch
            val updatedManga = manga.copy(
                titleFa = titleFa,
                titleEn = titleEn,
                descriptionFa = descriptionFa,
                coverUrl = coverUrl,
                bannerUrl = bannerUrl
            )
            repository.updateManga(updatedManga)
        }
    }

    private fun loadAiStorySummary(manga: MangaEntity) {
        _aiSummaryState.value = AiSummaryState.Loading
        viewModelScope.launch {
            try {
                val summary = withContext(Dispatchers.IO) {
                    GeminiClient.getSummary(
                        "مانهوا/مانگا: ${manga.titleFa} \n" +
                        "توضیحات کلی: ${manga.descriptionFa} \n" +
                        "نظرات خوانندگان: ${manga.reviewsJson} \n" +
                        "لطفا تحلیل نهایی و جذابیت‌های کلیدی آرت و روایت داستان را با قلم حماسی ترجمه مجدد و ارزیابی عمیق به زبان فارسی ارائه بده."
                    )
                }
                _aiSummaryState.value = AiSummaryState.Success(summary)
            } catch (e: Exception) {
                _aiSummaryState.value = AiSummaryState.Error("خطایی در اتصال با جمینای رخ داد.")
            }
        }
    }
}

sealed class AiSummaryState {
    object Idle : AiSummaryState()
    object Loading : AiSummaryState()
    data class Success(val summary: String) : AiSummaryState()
    data class Error(val message: String) : AiSummaryState()
}
