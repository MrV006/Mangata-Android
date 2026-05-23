package com.example.data

import kotlinx.coroutines.flow.Flow

class MangaRepository(private val mangaDao: MangaDao) {

    val allMangas: Flow<List<MangaEntity>> = mangaDao.getAllMangas()
    val allBookmarks: Flow<List<Bookmark>> = mangaDao.getBookmarks()
    val readHistory: Flow<List<ReadHistory>> = mangaDao.getReadHistory()
    val userPurchases: Flow<List<UserPurchase>> = mangaDao.getPurchases()
    val allTeamMembers: Flow<List<TeamMember>> = mangaDao.getAllTeamMembers()

    fun isBookmarked(mangaId: Int): Flow<Boolean> = mangaDao.isBookmarked(mangaId)

    suspend fun toggleBookmark(mangaId: Int, bookmarked: Boolean) {
        if (bookmarked) {
            mangaDao.insertBookmark(Bookmark(mangaId))
        } else {
            mangaDao.deleteBookmark(mangaId)
        }
    }

    suspend fun insertBookmark(bookmark: Bookmark) {
        mangaDao.insertBookmark(bookmark)
    }

    suspend fun insertReadHistory(history: ReadHistory) {
        mangaDao.insertReadHistory(history)
    }

    fun getHistoryForManga(mangaId: Int): Flow<ReadHistory?> = mangaDao.getHistoryForManga(mangaId)

    suspend fun saveReadHistory(mangaId: Int, chapter: Int, progress: Float) {
        mangaDao.insertReadHistory(ReadHistory(mangaId, chapter, progress))
    }

    suspend fun deleteHistory(mangaId: Int) {
        mangaDao.deleteHistoryForManga(mangaId)
    }

    fun hasActivePurchase(sku: String): Flow<Boolean> = mangaDao.hasActivePurchase(sku)

    suspend fun addPurchase(purchase: UserPurchase) {
        mangaDao.insertPurchase(purchase)
    }

    suspend fun removePurchase(sku: String) {
        mangaDao.deletePurchase(sku)
    }

    suspend fun clearAllPurchases() {
        mangaDao.clearPurchases()
    }

    suspend fun insertTeamMember(member: TeamMember) {
        mangaDao.insertTeamMember(member)
    }

    suspend fun removeTeamMember(member: TeamMember) {
        mangaDao.deleteTeamMember(member)
    }

    suspend fun updateManga(manga: MangaEntity) {
        mangaDao.insertMangas(listOf(manga))
    }

    suspend fun deleteManga(id: Int) {
        mangaDao.deleteMangaById(id)
    }

    suspend fun insertAllMangas(mangas: List<MangaEntity>) {
        mangaDao.insertMangas(mangas)
    }

    // --- Advanced Features Repository ---

    val allUserAccounts: Flow<List<UserAccount>> = mangaDao.getAllUserAccounts()
    fun getUserAccountById(id: Int): Flow<UserAccount?> = mangaDao.getUserAccountById(id)
    suspend fun insertUserAccount(user: UserAccount) = mangaDao.insertUserAccount(user)

    val systemSettings: Flow<SystemSettingsEntity?> = mangaDao.getSystemSettings()
    suspend fun insertSystemSettings(settings: SystemSettingsEntity) = mangaDao.insertSystemSettings(settings)

    val allRecruitments: Flow<List<RecruitmentApplication>> = mangaDao.getAllRecruitments()
    suspend fun insertRecruitment(app: RecruitmentApplication) = mangaDao.insertRecruitment(app)
    suspend fun removeRecruitment(id: Int) = mangaDao.deleteRecruitment(id)

    val allStories: Flow<List<StoryEntity>> = mangaDao.getAllStories()
    suspend fun insertStory(story: StoryEntity) = mangaDao.insertStory(story)
    suspend fun removeStory(id: Int) = mangaDao.deleteStory(id)
    suspend fun pruneEndedStories(expiryTime: Long) = mangaDao.pruneStories(expiryTime)
    suspend fun clearAllStories() = mangaDao.clearAllStories()

    fun getPurchasedChapters(userId: Int): Flow<List<ChapterPurchaseRecord>> = mangaDao.getPurchasedChapters(userId)
    fun isChapterUnlocked(userId: Int, mangaId: Int, chapterNumber: Int): Flow<Boolean> = mangaDao.isChapterUnlocked(userId, mangaId, chapterNumber)
    suspend fun insertChapterPurchase(record: ChapterPurchaseRecord) = mangaDao.insertChapterPurchase(record)

    val allSupportTickets: Flow<List<SupportTicket>> = mangaDao.getAllSupportTickets()
    fun getSupportTicketsByUserId(userId: Int): Flow<List<SupportTicket>> = mangaDao.getSupportTicketsByUserId(userId)
    suspend fun insertSupportTicket(ticket: SupportTicket) = mangaDao.insertSupportTicket(ticket)

    // Chapter Work tracking
    val allChapterWorks: Flow<List<ChapterWork>> = mangaDao.getAllChapterWorks()
    fun getChapterWorkByMangaAndNumber(mangaId: Int, chapterNumber: Int) = mangaDao.getChapterWorkByMangaAndNumber(mangaId, chapterNumber)
    suspend fun getChapterWorkByMangaAndNumberOneShot(mangaId: Int, chapterNumber: Int) = mangaDao.getChapterWorkByMangaAndNumberOneShot(mangaId, chapterNumber)
    suspend fun insertChapterWork(work: ChapterWork) = mangaDao.insertChapterWork(work)

    suspend fun seedDatabase() {
        // Only seed if empty
        val seedMangas = listOf(
            MangaEntity(
                id = 1,
                titleFa = "سولو لولینگ (تک‌رو)",
                titleEn = "Solo Leveling",
                descriptionFa = "در دنیایی مانهوایی که سیاه‌چاله‌هایی مخوف پدیدار شده و انسان‌ها قدرتی ماورایی یافته‌اند، سونگ جین‌وو ضعیف‌ترین شکارچی جهان است. پس از یک حادثه‌ی تلخ در سیاهچاله دوقلو، او قدرتی ناشناخته به نام سیستم کسب می‌کند که به او اجازه می‌دهد سطح خود را مانند یک بازی افزایش دهد...",
                type = "مانهوا",
                coverUrl = "https://picsum.photos/id/1025/400/600",
                bannerUrl = "https://picsum.photos/id/1025/1200/600",
                rating = 4.9,
                status = "پایان یافته",
                genres = "اکشن, ماجراجویی, فانتزی, گیمینگ",
                author = "Chugong",
                translatorTeam = "مانگاتا آلفا",
                chaptersCount = 179,
                isPremium = false,
                reviewsJson = """[
                    {"author": "امیر شریفی", "text": "بهترین مانهوای تاریخ که دنیای مانهواخوانی رو تکون داد. طراحی آرت در سطح جهانیه.", "rating": 5},
                    {"author": "سارا کریمی", "text": "آرت‌استایل شاهکاره و پیشرفت سطح جین‌وو واقعا جذاب و هیجان انگیزه.", "rating": 5},
                    {"author": "نوید", "text": "امیدوارم انیمه‌اش هم بتونه مثل خود وب‌تون جذاب باشه. صد در صد پیشنهادش می‌کنم.", "rating": 5}
                ]""",
                pagesJson = """[
                    "https://picsum.photos/id/1015/800/1200",
                    "https://picsum.photos/id/1016/800/1200",
                    "https://picsum.photos/id/1018/800/1200",
                    "https://picsum.photos/id/1019/800/1200",
                    "https://picsum.photos/id/1020/800/1200"
                ]"""
            ),
            MangaEntity(
                id = 2,
                titleFa = "برج خدا",
                titleEn = "Tower of God",
                descriptionFa = "برجی باشکوه که هر چیزی در این دنیا در بالاترین نقطه آن قرار دارد. بم، پسری که تمام عمرش را در تنهایی و تاریکی زیر برج گذرانده، برای دیدن دوباره دوست خود، ریچل، وارد برج می‌شود تا با چالش‌های خطرناک هر طبقه روبرو شود...",
                type = "مانهوا",
                coverUrl = "https://picsum.photos/id/1027/400/600",
                bannerUrl = "https://picsum.photos/id/1027/1200/600",
                rating = 4.8,
                status = "در حال انتشار",
                genres = "ماجراجویی, معمایی, فانتزی, درام",
                author = "SIU",
                translatorTeam = "مانگاتا پلاس",
                chaptersCount = 590,
                isPremium = true, // VIP preview
                reviewsJson = """[
                    {"author": "پارسا", "text": "جهان‌سازی بمب‌ترین چیزیه که تا حالا دیدم. برج خدا پادشاه عمق داستان‌سراییه.", "rating": 5},
                    {"author": "مهسا تهرانی", "text": "شخصیت‌پردازی‌ها به قدری عمیقه که از ریچل متنفر میشید و عاشق بم می‌شید.", "rating": 4.8},
                    {"author": "اردلان", "text": "طراحی فصل‌های جدید خیلی پیشرفت کرده. مبارزات فکری بی‌نظیری داره.", "rating": 5}
                ]""",
                pagesJson = """[
                    "https://picsum.photos/id/1028/800/1200",
                    "https://picsum.photos/id/1029/800/1200",
                    "https://picsum.photos/id/1031/800/1200",
                    "https://picsum.photos/id/1032/800/1200",
                    "https://picsum.photos/id/1033/800/1200"
                ]"""
            ),
            MangaEntity(
                id = 3,
                titleFa = "وان پیس (تکه‌ای از بهشت)",
                titleEn = "One Piece",
                descriptionFa = "گل دی. راجر، پادشاه دزدان دریایی، قبل از اعدام از گنج بزرگ خود، وان پیس، پرده برداشت. سال‌ها بعد، لوفی با خوردن میوه شیطانی پلاستیکی، گروه کلاه غوغا را تشکیل می‌دهد تا بزرگترین ماجراجویی اقیانوس را خلق کند...",
                type = "مانگا",
                coverUrl = "https://picsum.photos/id/1035/400/600",
                bannerUrl = "https://picsum.photos/id/1035/1200/600",
                rating = 4.9,
                status = "در حال انتشار",
                genres = "شونن, کمدی, اکشن, ماجراجویی",
                author = "Eiichiro Oda",
                translatorTeam = "مانگاتا نوستالژی",
                chaptersCount = 1110,
                isPremium = false,
                reviewsJson = """[
                    {"author": "علیرضا", "text": "شاهکار بزرگ اودا که بیش از ۲۵ ساله داره می‌درخشه. حتما با ترجمه روان مانگاتا بخونید.", "rating": 5},
                    {"author": "محدثه", "text": "داستان‌های حماسی لوفی و اراده‌ی زورو همیشه به من انگیزه میده.", "rating": 5},
                    {"author": "صادق", "text": "بهترین مانگای ماجراجویی تاریخ بشر بدون شک. مگه میشه لوفی و ملواناش رو دوست نداشت؟", "rating": 5}
                ]""",
                pagesJson = """[
                    "https://picsum.photos/id/1036/800/1200",
                    "https://picsum.photos/id/1037/800/1200",
                    "https://picsum.photos/id/1038/800/1200",
                    "https://picsum.photos/id/1039/800/1200",
                    "https://picsum.photos/id/1040/800/1200"
                ]"""
            ),
            MangaEntity(
                id = 4,
                titleFa = "خانه شیرین",
                titleEn = "Sweet Home",
                descriptionFa = "هیون‌سو، یک پسر دبیرستانی گوشه‌گیر که خانواده‌اش را در یک تصادف از دست داده، به یک مجتمع مسکونی ارزان‌قیمت نقل مکان می‌کند. ناگهان انسان‌ها بر اساس امیال پنهان خود تبدیل به هیولاهایی ترسناک می‌شوند و او باید برای رهایی بجنگد...",
                type = "مانهوا",
                coverUrl = "https://picsum.photos/id/1043/400/600",
                bannerUrl = "https://picsum.photos/id/1043/1200/600",
                rating = 4.7,
                status = "پایان یافته",
                genres = "ترسناک, روانشناختی, بقا, درام",
                author = "Kim Carnby",
                translatorTeam = "مانگاتا ترسناک",
                chaptersCount = 140,
                isPremium = true,
                reviewsJson = """[
                    {"author": "رضا", "text": "ترس و استرس مانهوا عجیبه. سریال نتفلیکسش هم قشنگ بود ولی خود مانهوا یه چیز دیگه‌ست.", "rating": 5},
                    {"author": "نیلوفر", "text": "تحول شخصیت هیون‌سو بی‌نظیر بود. مانیفست طمع آدم‌هاست این اثر.", "rating": 5},
                    {"author": "حامد", "text": "از نظر روانشناختی خیلی عمیقه. آرت‌استایل تاریکش کاملا حس تنهایی رو منتقل میکنه.", "rating": 4.5}
                ]""",
                pagesJson = """[
                    "https://picsum.photos/id/1044/800/1200",
                    "https://picsum.photos/id/1045/800/1200",
                    "https://picsum.photos/id/1047/800/1200",
                    "https://picsum.photos/id/1048/800/1200",
                    "https://picsum.photos/id/1049/800/1200"
                ]"""
            ),
            MangaEntity(
                id = 5,
                titleFa = "حرامزاده",
                titleEn = "Bastard",
                descriptionFa = "جین سون، پسری بیمار و ضعیف است که چشمی مصنوعی و مفاصلی فلزی دارد. او با پدر مهربان و ثروتمندش زندگی می‌کند که رئیس یک شرکت هولدینگ است؛ اما در پشت این نقاب مهربانی، پدرش یک قاتل زنجیره‌ای روانی است و جین شریک اجباری جنایات اوست...",
                type = "مانهوا",
                coverUrl = "https://picsum.photos/id/1051/400/600",
                bannerUrl = "https://picsum.photos/id/1051/1200/600",
                rating = 4.8,
                status = "پایان یافته",
                genres = "روانشناختی, معمایی, درام, جنایی",
                author = "Hwang Young-chan",
                translatorTeam = "مانگاتا دارک",
                chaptersCount = 93,
                isPremium = false,
                reviewsJson = """[
                    {"author": "ثنا", "text": "تنها مانهوایی که نفس ادم رو توی سینه حبس میکنه. تقابل پدر و پسر دیوانه‌کننده‌ست.", "rating": 5},
                    {"author": "کسری", "text": "کیم کارنبی واقعا یه نابغه‌ست. کلینر این مانهوا کارش حرف نداشته.", "rating": 5},
                    {"author": "بهروز", "text": "تایپوگرافی و ادیت فارسی این کار توی برنامه شما خیلی تمیز در اومده دست مریزاد.", "rating": 5}
                ]""",
                pagesJson = """[
                    "https://picsum.photos/id/1052/800/1200",
                    "https://picsum.photos/id/1053/800/1200",
                    "https://picsum.photos/id/1054/800/1200",
                    "https://picsum.photos/id/1055/800/1200",
                    "https://picsum.photos/id/1056/800/1200"
                ]"""
            )
        )
        mangaDao.insertMangas(seedMangas)

        // Seed Team Members
        val seedTeam = listOf(
            TeamMember(name = "امیررضا", roleFa = "مدیر کل و موسس", levelCode = 1, assignedWorks = "مدیریت زیرساخت", rating = 5.0),
            TeamMember(name = "مهدی خسروی", roleFa = "سرپرست مترجمان", levelCode = 2, assignedWorks = "سولو لولینگ, برج خدا", rating = 4.9),
            TeamMember(name = "تینا مهدوی", roleFa = "تایپیست و کارشناس فونت", levelCode = 3, assignedWorks = "برج خدا, خانه شیرین", rating = 4.8),
            TeamMember(name = "سینا زارع", roleFa = "کلینر و ادیتور تصاویر", levelCode = 3, assignedWorks = "وان پیس, حرامزاده", rating = 4.7),
            TeamMember(name = "نازنین راد", roleFa = "مترجم زبان کره‌ای", levelCode = 3, assignedWorks = "سولو لولینگ", rating = 4.9)
        )
        seedTeam.forEach { mangaDao.insertTeamMember(it) }

        // Seed system settings
        val defaultSettings = SystemSettingsEntity()
        mangaDao.insertSystemSettings(defaultSettings)

        // Seed user accounts (Super Admin, Dept Admin, Staff, and Readers)
        val seedUsers = listOf(
            UserAccount(id = 1, username = "god_admin", displayName = "امیررضا (مدیر کل)", role = "SUPER_ADMIN", subRole = "مدیر کل", walletRial = 50000, walletGiftChapters = 999, chaptersContributedLastMonth = 0, chaptersContributedThisMonth = 0, storyTokens = 10),
            UserAccount(id = 2, username = "dept_editor", displayName = "مهدی خسروی", role = "DEPT_ADMIN", subRole = "مدیر ترجمه", walletRial = 15000, walletGiftChapters = 20, chaptersContributedLastMonth = 45, chaptersContributedThisMonth = 0, storyTokens = 2),
            UserAccount(id = 3, username = "staff_cleaner", displayName = "سینا زارع", role = "STAFF", subRole = "کلینر", walletRial = 2400, walletGiftChapters = 12, chaptersContributedLastMonth = 52, chaptersContributedThisMonth = 0, storyTokens = 2),
            UserAccount(id = 4, username = "staff_editor", displayName = "تینا مهدوی", role = "STAFF", subRole = "تایپیست/ادیتور", walletRial = 3600, walletGiftChapters = 8, chaptersContributedLastMonth = 41, chaptersContributedThisMonth = 0, storyTokens = 2),
            UserAccount(id = 5, username = "staff_translator", displayName = "نازنین راد", role = "STAFF", subRole = "مترجم", walletRial = 1200, walletGiftChapters = 5, chaptersContributedLastMonth = 38, chaptersContributedThisMonth = 0, storyTokens = 0),
            UserAccount(id = 6, username = "guest_user", displayName = "کاربر مهمان", role = "NORMAL_USER", subRole = "کاربر عادی", walletRial = 4800, walletGiftChapters = 3, chaptersContributedLastMonth = 0, chaptersContributedThisMonth = 0, storyTokens = 0)
        )
        seedUsers.forEach { mangaDao.insertUserAccount(it) }

        // Seed initial chapter works for realistic dashboard statistics
        val seedChapterWorks = listOf(
            ChapterWork(mangaId = 1, mangaTitle = "سولو لولینگ (تک‌رو)", chapterNumber = 1, translatorId = 5, translatorName = "نازنین راد", cleanerId = 3, cleanerName = "سینا زارع", editorId = 4, editorName = "تینا مهدوی", revenueEarned = 10000, translatorPaid = 3500, cleanerPaid = 2000, editorPaid = 2500, platformEarned = 2000),
            ChapterWork(mangaId = 1, mangaTitle = "سولو لولینگ (تک‌رو)", chapterNumber = 2, translatorId = 5, translatorName = "نازنین راد", cleanerId = 3, cleanerName = "سینا زارع", editorId = 4, editorName = "تینا مهدوی", revenueEarned = 15000, translatorPaid = 5250, cleanerPaid = 3000, editorPaid = 3750, platformEarned = 3000),
            ChapterWork(mangaId = 2, mangaTitle = "برج خدا", chapterNumber = 1, translatorId = 2, translatorName = "مهدی خسروی", cleanerId = 3, cleanerName = "سینا زارع", editorId = 4, editorName = "تینا مهدوی", revenueEarned = 12000, translatorPaid = 4200, cleanerPaid = 2400, editorPaid = 3000, platformEarned = 2400)
        )
        seedChapterWorks.forEach { mangaDao.insertChapterWork(it) }
    }
}
