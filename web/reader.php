<?php
/**
 * Standalone Mangata Portal - Web Manga Reader Screen
 * Fully responsive, modern, with live interactive panel customizer
 */

require_once __DIR__ . '/config.php';

$chapter_id = isset($_GET['chapter_id']) ? (int)$_GET['chapter_id'] : 0;

if ($chapter_id <= 0) {
    die("خطا: شناسه چپتر نامعتبر است.");
}

// Fetch chapter details
$stmt = $pdo->prepare("SELECT * FROM mangata_chapters WHERE id = ?");
$stmt->execute([$chapter_id]);
$chap = $stmt->fetch();

if (!$chap) {
    die("خطا: چپتر در پایگاه داده پیدا نشد.");
}

// Fetch manga title
$stmt = $pdo->prepare("SELECT title FROM mangata_mangas WHERE id = ?");
$stmt->execute([$chap['manga_id']]);
$manga_title = $stmt->fetchColumn() ?: "مانهوا";

$images = json_decode($chap['images_json'], true) ?: [];

// Find neighboring chapters for DB-connected navigation
$manga_id = (int)$chap['manga_id'];
$current_number = (float)$chap['chapter_number'];

// Prev Chapter
$stmt_prev = $pdo->prepare("SELECT id, chapter_number FROM mangata_chapters WHERE manga_id = ? AND chapter_number < ? ORDER BY chapter_number DESC LIMIT 1");
$stmt_prev->execute([$manga_id, $current_number]);
$prev_chap = $stmt_prev->fetch();

// Next Chapter
$stmt_next = $pdo->prepare("SELECT id, chapter_number FROM mangata_chapters WHERE manga_id = ? AND chapter_number > ? ORDER BY chapter_number ASC LIMIT 1");
$stmt_next->execute([$manga_id, $current_number]);
$next_chap = $stmt_next->fetch();
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo htmlspecialchars($manga_title); ?> - چپتر <?php echo (float)$chap['chapter_number']; ?> | ریدر فوق‌پیشرفته مانگاتا</title>
    <link rel="stylesheet" href="style.css">
    <style>
        :root {
            --bg-color: #0c0a0f;
            --card-bg: #121016;
            --text-color: #e2dff0;
            --accent-color: #ff7597;
            --secondary-color: #bb86fc;
            --container-width: 800px;
            --img-brightness: 100%;
        }
        
        /* Interactive dynamic classes via settings panel */
        body.theme-black {
            --bg-color: #000000;
            --card-bg: #111111;
            --text-color: #e2dff0;
            --accent-color: #ff7597;
            --secondary-color: #bb86fc;
        }
        body.theme-midnight {
            --bg-color: #0c0a0f;
            --card-bg: #121016;
            --text-color: #e2dff0;
            --accent-color: #ff7597;
            --secondary-color: #bb86fc;
        }
        body.theme-sepia {
            --bg-color: #faf0e6;
            --card-bg: #f3e6d5;
            --text-color: #2b231d;
            --accent-color: #ff5722;
            --secondary-color: #7c4dff;
        }
        body.theme-charcoal {
            --bg-color: #222225;
            --card-bg: #2d2d32;
            --text-color: #f2f2f5;
            --accent-color: #03dac6;
            --secondary-color: #bb86fc;
        }

        body {
            background-color: var(--bg-color) !important;
            color: var(--text-color) !important;
            transition: background-color 0.3s ease, color 0.3s ease;
            overflow-x: hidden;
        }

        /* Reading Progress Top Bar */
        .progress-bar-container {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 3px;
            background: rgba(255, 255, 255, 0.1);
            z-index: 2000;
        }
        .progress-bar-fill {
            height: 100%;
            width: 0%;
            background: linear-gradient(90deg, var(--accent-color), var(--secondary-color));
            transition: width 0.1s ease-out;
        }

        /* Top Header Navigation Overlay */
        .reader-header {
            background: var(--card-bg);
            border-bottom: 2px solid var(--accent-color);
            padding: 12px 20px;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            box-sizing: border-box;
            z-index: 1500;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 4px 20px rgba(0,0,0,0.5);
            transition: transform 0.3s cubic-bezier(0.1, 0.9, 0.2, 1);
        }
        .reader-header.hide {
            transform: translateY(-100%);
        }

        /* Wide screen immersive Webtoon canvas */
        .reader-container {
            max-width: var(--container-width);
            margin: 80px auto 0 auto;
            padding: 0;
            box-shadow: 0 0 40px rgba(0,0,0,0.8);
            background: rgba(0,0,0,0.2);
            transition: max-width 0.3s ease;
            position: relative;
        }

        /* Individual page loader integration */
        .img-wrapper {
            position: relative;
            width: 100%;
            min-height: 400px;
            background: rgba(0,0,0,0.15);
            overflow: hidden;
            display: flex;
            justify-content: center;
            align-items: center;
        }
        
        /* Neon loader animation */
        .img-loader {
            position: absolute;
            width: 40px;
            height: 40px;
            border: 3px solid rgba(255, 117, 151, 0.1);
            border-top-color: var(--accent-color);
            border-radius: 50%;
            animation: spin 1s infinite linear;
            z-index: 10;
        }
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        .manga-image {
            width: 100%;
            height: auto;
            display: block;
            margin: 0 auto;
            border-bottom: 2px solid #000;
            filter: brightness(var(--img-brightness));
            opacity: 0;
            transition: opacity 0.5s ease-out, filter 0.2s ease;
            z-index: 20;
        }
        .manga-image.loaded {
            opacity: 1;
        }

        /* Controls Panel Drawer */
        .settings-fab {
            position: fixed;
            bottom: 25px;
            right: 25px;
            width: 50px;
            height: 50px;
            border-radius: 50%;
            background: linear-gradient(135deg, var(--accent-color), var(--secondary-color));
            box-shadow: 0 4px 15px rgba(255, 117, 151, 0.4);
            display: flex;
            justify-content: center;
            align-items: center;
            cursor: pointer;
            z-index: 1600;
            border: none;
            transition: transform 0.2s;
        }
        .settings-fab:hover {
            transform: scale(1.1) rotate(45deg);
        }
        .scroll-top-fab {
            position: fixed;
            bottom: 25px;
            left: 25px;
            width: 50px;
            height: 50px;
            border-radius: 50%;
            background: rgba(30, 27, 36, 0.9);
            border: 1px solid var(--accent-color);
            box-shadow: 0 4px 15px rgba(0,0,0,0.5);
            display: flex;
            justify-content: center;
            align-items: center;
            cursor: pointer;
            z-index: 1600;
            opacity: 0;
            visibility: hidden;
            transition: opacity 0.3s, visibility 0.3s, transform 0.2s;
        }
        .scroll-top-fab.show {
            opacity: 1;
            visibility: visible;
        }
        .scroll-top-fab:hover {
            transform: scale(1.1);
        }

        /* Glassmorphic settings sidebar panel drawer */
        .settings-drawer {
            position: fixed;
            left: -320px;
            top: 0;
            width: 320px;
            height: 100%;
            background: rgba(20, 18, 24, 0.95);
            backdrop-filter: blur(15px);
            border-right: 2px solid var(--accent-color);
            box-shadow: 10px 0 30px rgba(0,0,0,0.6);
            z-index: 1800;
            box-sizing: border-box;
            padding: 30px 24px;
            transition: left 0.3s cubic-bezier(0.1, 0.9, 0.2, 1);
            display: flex;
            flex-direction: column;
            gap: 20px;
        }
        .settings-drawer.open {
            left: 0;
        }
        .drawer-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.5);
            z-index: 1700;
            display: none;
        }
        .drawer-overlay.open {
            display: block;
        }

        /* Theme selection buttons */
        .color-bubble {
            width: 38px;
            height: 38px;
            border-radius: 50%;
            cursor: pointer;
            border: 2px solid transparent;
            transition: transform 0.1s, border-color 0.2s;
        }
        .color-bubble:hover {
            transform: scale(1.1);
        }
        .color-bubble.active {
            border-color: var(--accent-color);
            box-shadow: 0 0 10px var(--accent-color);
        }

        /* Action Nav Buttons in reader */
        .btn-nav {
            background: linear-gradient(135deg, rgba(255,255,255,0.06), rgba(255,255,255,0.02));
            color: var(--text-color);
            border: 1px solid rgba(255,255,255,0.1);
            padding: 8px 16px;
            border-radius: 8px;
            cursor: pointer;
            text-decoration: none;
            font-size: 13px;
            font-weight: bold;
            display: flex;
            align-items: center;
            gap: 5px;
            transition: border-color 0.2s, background 0.2s;
        }
        .btn-nav:hover {
            border-color: var(--accent-color);
            background: rgba(255,117,151,0.1);
        }

        /* Completion Action Section */
        .reader-footer {
            background: var(--card-bg);
            padding: 40px 30px;
            text-align: center;
            border-top: 1px solid rgba(255,255,255,0.06);
            margin-top: 40px;
            border-radius: 12px;
            border: 1px solid rgba(255, 255, 255, 0.05);
            box-shadow: 0 -4px 20px rgba(0,0,0,0.4);
        }

        .fullscreen-prompt {
            position: absolute;
            top: 15px;
            left: 50%;
            transform: translateX(-50%);
            background: rgba(0,0,0,0.7);
            color: #ff7597;
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: bold;
            z-index: 1000;
            pointer-events: none;
            animation: pulse 2s infinite;
        }
        @keyframes pulse {
            0% { opacity: 0.7; }
            50% { opacity: 1; }
            100% { opacity: 0.7; }
        }
    </style>
</head>
<body class="theme-midnight">

<!-- Top Progress Indicator Scrollbar -->
<div class="progress-bar-container">
    <div id="progressBar" class="progress-bar-fill"></div>
</div>

<!-- Header overlay -->
<div id="readerHeader" class="reader-header">
    <div style="display: flex; align-items: center; gap: 15px;">
        <a href="." class="btn-nav" style="border-radius: 50%; padding:0; width: 36px; height: 36px; justify-content:center;">↩</a>
        <div>
            <h2 style="color:var(--accent-color); margin:0; font-size:16px; font-weight:bold;"><?php echo htmlspecialchars($manga_title); ?></h2>
            <span style="color:var(--text-color); font-size:12px; opacity: 0.7;">چپتر <?php echo (float)$chap['chapter_number']; ?> <?php echo htmlspecialchars($chap['title'] ?: ''); ?></span>
        </div>
    </div>
    
    <!-- Top Nav Chapter Select list -->
    <div style="display:flex; gap:10px;">
        <?php if ($prev_chap): ?>
            <a href="?chapter_id=<?php echo $prev_chap['id']; ?>" class="btn-nav">◀ فصل قبلی</a>
        <?php endif; ?>
        
        <?php if ($next_chap): ?>
            <a href="?chapter_id=<?php echo $next_chap['id']; ?>" class="btn-nav" style="border-color: var(--accent-color); color: var(--accent-color);">فصل بعدی ▶</a>
        <?php endif; ?>
    </div>
</div>

<!-- Immersive click hint -->
<div class="container" style="padding:0; position:relative;">
    <div class="fullscreen-prompt">💡 برای مطالعه در تم تمام‌صفحه سینمایی، یک بار روی صفحه کلیک کنید.</div>

    <!-- Manga Pages Reading Board -->
    <div class="reader-container" id="mangaReadingBoard">
        <?php if (!empty($images)): ?>
            <?php foreach ($images as $index => $img_url): ?>
                <div class="img-wrapper">
                    <!-- Loading Placeholder animation -->
                    <div class="img-loader"></div>
                    <img 
                        src="placeholder_loading" 
                        data-src="<?php echo htmlspecialchars($img_url); ?>" 
                        class="manga-image" 
                        alt="صفحه مانهوا شماره <?php echo $index + 1; ?>"
                    >
                </div>
            <?php endforeach; ?>
        <?php else: ?>
            <div style="text-align:center; padding: 120px 20px; color:#888;">
                <p style="font-size:18px; margin-bottom:20px; color:var(--accent-color); font-weight:bold;">تصویر ریدر بارگذاری مانهوا یافت نشد.</p>
                <p style="font-size:13px;">این خطا ممکن است ناشی از خالی بودن فایل فشرده چپتر یا عدم تطابق فرمت عکس‌ها در سرور باشد.</p>
                <a href="." class="btn btn-sm" style="margin-top: 20px;">بازگشت به مانهواهای فعال</a>
            </div>
        <?php endif; ?>
        
        <!-- Interactive Completion segment card -->
        <div class="reader-footer">
            <div style="font-size: 40px; margin-bottom:15px; color:#4caf50;">✅</div>
            <h4 style="color:var(--accent-color); margin:0 0 10px 0; font-size: 20px; font-weight:bold;">این چپتر با موفقیت به پایان رسید!</h4>
            <p style="color:#888; font-size:13px; margin-bottom:30px; max-width: 500px; margin-left:auto; margin-right:auto;">با تشکر از اینکه مانهوای محبوب خود را با بالاترین کیفیت در رسانه انتشار مستقل مانگاتا دنبال می‌کنید.</p>
            
            <div style="display:flex; justify-content:center; gap:15px;">
                <a href="." class="btn" style="background:#555;">امکانات صفحه اصلی</a>
                <?php if ($next_chap): ?>
                    <a href="?chapter_id=<?php echo $next_chap['id']; ?>" class="btn" style="background: linear-gradient(135deg, #6200ee, var(--accent-color)); font-weight:bold;">مطالعه چپتر بعدی (<?php echo (float)$next_chap['chapter_number']; ?>) 👁️</a>
                <?php else: ?>
                    <span style="color:#9e9e9e; font-size:13px; font-weight:bold; align-self:center;">✨ شما آخرین چپتر منتشر شده مانگاتا را به پایان رساندید!</span>
                <?php endif; ?>
            </div>
        </div>
    </div>
</div>

<!-- Navigation and control widgets floating FABs -->
<button class="settings-fab" id="settingsToggle" title="تنظیمات نمایشی صفحه">⚙️</button>
<button class="scroll-top-fab" id="scrollTopBtn" title="پرش سریع به بالا">▲</button>

<!-- Live appearance sidebar drawer configs menu -->
<div class="drawer-overlay" id="drawerOverlay"></div>
<div class="settings-drawer" id="settingsDrawer">
    <h3 style="color:var(--accent-color); margin:0 0 10px 0; border-bottom: 2px solid var(--accent-color); padding-bottom:10px; font-weight:bold;">🛠️ سفارشی‌سازی ریدر</h3>
    
    <!-- Theme Choice control -->
    <div>
        <label style="font-weight:bold; font-size:13px; display:block; margin-bottom:8px; color:var(--secondary-color);">اتمسفر محیط کاربری:</label>
        <div style="display:flex; gap:12px; margin-top:5px;">
            <div class="color-bubble" data-theme="midnight" style="background:#0c0a0f;" title="Midnight"></div>
            <div class="color-bubble" data-theme="black" style="background:#000000;" title="خلاء مطلق"></div>
            <div class="color-bubble" data-theme="sepia" style="background:#faf0e6;" title="Cozy Sepia"></div>
            <div class="color-bubble" data-theme="charcoal" style="background:#222225;" title="Dim Charcoal"></div>
        </div>
    </div>

    <!-- Width control sliders -->
    <div style="margin-top:10px;">
        <label style="font-weight:bold; font-size:13px; display:block; margin-bottom:8px; color:var(--secondary-color);">پهنای بورد تصاویر مانهوا:</label>
        <div style="display:flex; flex-direction:column; gap:8px;">
            <button class="btn-nav" id="btnWidthNarrow" style="justify-content:center;">عریض باریک وب‌تون (600px)</button>
            <button class="btn-nav" id="btnWidthMedium" style="justify-content:center; border-color:var(--accent-color);">متعادل متوازن (800px)</button>
            <button class="btn-nav" id="btnWidthWide" style="justify-content:center;">عریض تبلت سینمایی (1000px)</button>
        </div>
    </div>

    <!-- Exposure slider filters overlay -->
    <div style="margin-top:10px;">
        <label style="font-weight:bold; font-size:13px; display:block; margin-bottom:4px; color:var(--secondary-color); display:flex; justify-content:space-between;">
            <span>فیلتر نور صفحه‌های تصویر:</span>
            <span id="brightnessVal">100%</span>
        </label>
        <input type="range" id="brightnessInput" min="40" max="100" value="100" style="margin:0; padding:0; accent-color: var(--accent-color);">
        <span style="font-size:11px; color:#555;">کاهش خستگی چشم زمان مطالعه در اتاق‌های تاریک</span>
    </div>

    <!-- Instructions segment -->
    <div style="margin-top:auto; font-size:11px; text-align:center; color:#555; border-top: 1px dotted rgba(255,255,255,0.1); padding-top:10px;">
        حالت مطالعه انتخابی شما در حافظه وب‌گرد کلینت ذخیره خواهد شد.
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    // 1. Double tap/single tap overlay togglers
    const header = document.getElementById('readerHeader');
    document.addEventListener('click', function(e) {
        // Toggle header visibility if we clicked empty space/image not on buttons/drawers
        if (!e.target.closest('#readerHeader') && 
            !e.target.closest('#settingsDrawer') && 
            !e.target.closest('#settingsToggle') && 
            !e.target.closest('#scrollTopBtn') && 
            !e.target.closest('.btn-nav') && 
            !e.target.closest('.btn')
        ) {
            header.classList.toggle('hide');
        }
    });

    // 2. Intelligent LazyLoading for Manga high-res pages
    const mangaBoard = document.getElementById('mangaReadingBoard');
    const imagesToLoad = mangaBoard.querySelectorAll('.manga-image');
    
    const lazyImageLoad = function() {
        imagesToLoad.forEach(function(img) {
            const rect = img.getBoundingClientRect();
            // Trigger 400px before image scrolls into viewport
            if (rect.top <= window.innerHeight + 400 && rect.bottom >= -400 && img.src === 'placeholder_loading' || img.getAttribute('src') === 'placeholder_loading') {
                const realSrc = img.getAttribute('data-src');
                img.src = realSrc;
                img.addEventListener('load', function() {
                    img.classList.add('loaded');
                    const loader = img.previousElementSibling;
                    if (loader && loader.classList.contains('img-loader')) {
                        loader.style.display = 'none';
                    }
                });
            }
        });
    };

    window.addEventListener('scroll', lazyImageLoad);
    window.addEventListener('resize', lazyImageLoad);
    // Initial load check
    lazyImageLoad();

    // 3. Real-time Scroll progress percentage calculations
    const progressBar = document.getElementById('progressBar');
    const scrollTopBtn = document.getElementById('scrollTopBtn');
    
    window.addEventListener('scroll', function() {
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const docHeight = document.documentElement.scrollHeight - window.innerHeight;
        const pct = docHeight > 0 ? (scrollTop / docHeight) * 100 : 0;
        progressBar.style.width = pct + '%';

        // Floating scrollTop show
        if (scrollTop > 400) {
            scrollTopBtn.classList.add('show');
        } else {
            scrollTopBtn.classList.remove('show');
        }
    });

    // Smooth scrollTop tap
    scrollTopBtn.addEventListener('click', function() {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    });

    // 4. Floating appearance drawer actions
    const drawer = document.getElementById('settingsDrawer');
    const overlay = document.getElementById('drawerOverlay');
    const toggle = document.getElementById('settingsToggle');

    function openSettings() {
        drawer.classList.add('open');
        overlay.classList.add('open');
    }
    function closeSettings() {
        drawer.classList.remove('open');
        overlay.classList.remove('open');
    }

    toggle.addEventListener('click', function(e) {
        e.stopPropagation();
        drawer.classList.contains('open') ? closeSettings() : openSettings();
    });
    overlay.addEventListener('click', closeSettings);

    // 5. App Theme changer presets
    const bubbles = document.querySelectorAll('.color-bubble');
    const body = document.body;

    // Load presets of theme from Storage
    const cachedTheme = localStorage.getItem('mangata_reader_theme') || 'midnight';
    body.className = '';
    body.classList.add('theme-' + cachedTheme);
    document.querySelector(`[data-theme="${cachedTheme}"]`)?.classList.add('active');

    bubbles.forEach(function(b) {
        b.addEventListener('click', function() {
            bubbles.forEach(el => el.classList.remove('active'));
            const th = b.getAttribute('data-theme');
            body.className = '';
            body.classList.add('theme-' + th);
            b.classList.add('active');
            localStorage.setItem('mangata_reader_theme', th);
        });
    });

    // 6. Board Width customization buttons
    const btnNarrow = document.getElementById('btnWidthNarrow');
    const btnMedium = document.getElementById('btnWidthMedium');
    const btnWide = document.getElementById('btnWidthWide');

    function selectWidthBtn(activeBtn) {
        [btnNarrow, btnMedium, btnWide].forEach(btn => btn.style.borderColor = 'rgba(255,255,255,0.1)');
        activeBtn.style.borderColor = 'var(--accent-color)';
    }

    // Load width presets from client storage
    const cachedWidth = localStorage.getItem('mangata_reader_width') || '800';
    document.documentElement.style.setProperty('--container-width', cachedWidth + 'px');
    if (cachedWidth === '600') selectWidthBtn(btnNarrow);
    else if (cachedWidth === '1000') selectWidthBtn(btnWide);
    else selectWidthBtn(btnMedium);

    btnNarrow.addEventListener('click', function() {
        document.documentElement.style.setProperty('--container-width', '600px');
        selectWidthBtn(btnNarrow);
        localStorage.setItem('mangata_reader_width', '600');
    });

    btnMedium.addEventListener('click', function() {
        document.documentElement.style.setProperty('--container-width', '800px');
        selectWidthBtn(btnMedium);
        localStorage.setItem('mangata_reader_width', '800');
    });

    btnWide.addEventListener('click', function() {
        document.documentElement.style.setProperty('--container-width', '1000px');
        selectWidthBtn(btnWide);
        localStorage.setItem('mangata_reader_width', '1000');
    });

    // 7. Live image brightness adjuster input slider
    const brightInput = document.getElementById('brightnessInput');
    const brightValText = document.getElementById('brightnessVal');

    const cachedBrightness = localStorage.getItem('mangata_reader_brightness') || '100';
    brightInput.value = cachedBrightness;
    brightValText.textContent = cachedBrightness + '%';
    document.documentElement.style.setProperty('--img-brightness', (cachedBrightness / 100));

    brightInput.addEventListener('input', function() {
        const val = brightInput.value;
        brightValText.textContent = val + '%';
        document.documentElement.style.setProperty('--img-brightness', (val / 100));
        localStorage.setItem('mangata_reader_brightness', val);
    });
});
</script>

</body>
</html>
