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
    }
}
