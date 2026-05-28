<?php
/**
 * Dedicated Manhwa details page - Mangata Web
 * RTL compatible, fully responsive with glassmorphic visuals
 */

require_once __DIR__ . '/config.php';

if (!is_logged_in()) {
    header("Location: index.php");
    exit;
}

$manga_id = isset($_GET['id']) ? (int)$_GET['id'] : 0;

if ($manga_id <= 0) {
    header("Location: index.php");
    exit;
}

// Fetch Manga Details
$stmt = $pdo->prepare("SELECT * FROM mangata_mangas WHERE id = ?");
$stmt->execute([$manga_id]);
$manga = $stmt->fetch();

if (!$manga) {
    die("خطا: اثر مورد نظر یافت نشد یا از روی دیتابیس حذف گردیده است.");
}

// Fetch ALL chapters for this Manga
$stmt_ch = $pdo->prepare("SELECT * FROM mangata_chapters WHERE manga_id = ? ORDER BY chapter_number DESC");
$stmt_ch->execute([$manga_id]);
$chapters = $stmt_ch->fetchAll();

// Fetch Allocated Staff
$stmt_staff = $pdo->prepare("
    SELECT s.*, u.username, u.email 
    FROM mangata_staff s 
    JOIN mangata_users u ON s.user_id = u.id 
    WHERE s.manga_id = ?
");
$stmt_staff->execute([$manga_id]);
$allocated_staff = $stmt_staff->fetchAll();
$allocated_staff = $allocated_staff ?: [];

// Check bookmark info
$is_bookmarked = false;
$bookmark_status = '';
if (is_logged_in()) {
    $stmt_b = $pdo->prepare("SELECT status FROM mangata_bookmarks WHERE user_id = ? AND manga_id = ?");
    $stmt_b->execute([$_SESSION['user_id'], $manga_id]);
    $bookmark_row = $stmt_b->fetch();
    if ($bookmark_row) {
        $is_bookmarked = true;
        $bookmark_status = $bookmark_row['status'];
    }
}

// Handle Bookmark POST Toggle
if (isset($_POST['toggle_bookmark_action']) && is_logged_in()) {
    if ($is_bookmarked) {
        $stmt_del = $pdo->prepare("DELETE FROM mangata_bookmarks WHERE user_id = ? AND manga_id = ?");
        $stmt_del->execute([$_SESSION['user_id'], $manga_id]);
        $is_bookmarked = false;
    } else {
        $stmt_ins = $pdo->prepare("INSERT INTO mangata_bookmarks (user_id, manga_id, status) VALUES (?, ?, 'Reading')");
        $stmt_ins->execute([$_SESSION['user_id'], $manga_id]);
        $is_bookmarked = true;
        $bookmark_status = 'Reading';
    }
    header("Location: details.php?id=" . $manga_id);
    exit;
}

if (isset($_POST['cycle_bookmark_status_action']) && is_logged_in() && $is_bookmarked) {
    $next_status = 'Reading';
    if ($bookmark_status === 'Reading') {
        $next_status = 'Completed';
    } else if ($bookmark_status === 'Completed') {
        $next_status = 'Favorite';
    }
    $stmt_up = $pdo->prepare("UPDATE mangata_bookmarks SET status = ? WHERE user_id = ? AND manga_id = ?");
    $stmt_up->execute([$next_status, $_SESSION['user_id'], $manga_id]);
    header("Location: details.php?id=" . $manga_id);
    exit;
}
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo htmlspecialchars($manga['title']); ?> | مانگاتا</title>
    <link rel="stylesheet" href="style.css">
    <style>
        body {
            background-color: #0c0a0f;
            color: #e0e0e0;
            font-family: inherit;
        }
        .details-header {
            position: relative;
            background: linear-gradient(180deg, rgba(20,18,24,0.4) 0%, #0c0a0f 100%), 
                        url('<?php echo htmlspecialchars($manga['cover_image'] ?: "https://placehold.co/1200x500/121214/ffffff?text=" . urlencode($manga['title'])); ?>');
            background-size: cover;
            background-position: center;
            min-height: 380px;
            display: flex;
            align-items: flex-end;
            padding: 40px 20px;
            border-bottom: 1px solid rgba(255,117,151,0.15);
            margin-bottom: 30px;
        }
        .details-header-overlay {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(12, 10, 15, 0.7);
            backdrop-filter: blur(15px);
            z-index: 1;
        }
        .details-content-wrapper {
            position: relative;
            z-index: 10;
            display: flex;
            flex-direction: column;
            gap: 30px;
            width: 100%;
            max-width: 1100px;
            margin: 0 auto;
        }
        @media(min-width: 768px) {
            .details-content-wrapper {
                flex-direction: row;
                align-items: flex-end;
            }
        }
        .details-cover {
            width: 180px;
            height: 260px;
            border-radius: 12px;
            object-fit: cover;
            border: 2.5px solid #ff7597;
            box-shadow: 0 10px 30px rgba(255, 117, 151, 0.25);
            align-self: center;
        }
        .details-meta {
            flex: 1;
            text-shadow: 0 2px 8px rgba(0,0,0,0.8);
        }
        .badge-row {
            display: flex;
            gap: 10px;
            margin-bottom: 15px;
            flex-wrap: wrap;
        }
        .badge-meta {
            font-size: 11px;
            font-weight: 800;
            padding: 5px 12px;
            border-radius: 20px;
        }
        .badge-meta.accent {
            background: rgba(255, 117, 151, 0.2);
            color: #ff7597;
            border: 1px solid rgba(255, 117, 151, 0.4);
        }
        .badge-meta.cyan {
            background: rgba(3, 218, 198, 0.2);
            color: #03dac6;
            border: 1px solid rgba(3, 218, 198, 0.4);
        }
        .badge-meta.orange {
            background: rgba(255, 179, 0, 0.2);
            color: #ffb300;
            border: 1px solid rgba(255, 179, 0, 0.4);
        }
        .section-box {
            background: rgba(22, 20, 31, 0.85);
            border: 1px solid rgba(255,255,255,0.04);
            border-radius: 16px;
            padding: 24px;
            margin-bottom: 25px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.25);
            backdrop-filter: blur(5px);
        }
        .staff-avatar-row {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
            gap: 15px;
            margin-top: 15px;
        }
        .staff-card {
            background: #141218;
            border: 1px solid rgba(255,255,255,0.05);
            border-radius: 10px;
            padding: 12px 15px;
            display: flex;
            align-items: center;
            gap: 12px;
            transition: border-color 0.2s;
        }
        .staff-card:hover {
            border-color: #03dac6;
        }
        .staff-avatar-bubble {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            background: #2e2a3a;
            color: #03dac6;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 900;
            font-size: 14px;
        }
        .chapter-table-list {
            display: flex;
            flex-direction: column;
            gap: 10px;
            margin-top: 15px;
        }
        .chapter-item-row {
            background: #16141f;
            border-radius: 10px;
            padding: 12px 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            border: 1px solid rgba(255,255,255,0.03);
            transition: all 0.2s;
        }
        .chapter-item-row:hover {
            border-color: #ff7597;
            transform: translateX(-4px);
        }
    </style>
</head>
<body>

<header>
    <h1><a href="." style="color:#bb86fc; font-weight:bold; text-decoration:none;">MANGATA | مانگاتا</a></h1>
    <nav style="display: flex; align-items: center; gap: 20px;">
        <a href="." style="color:#fff; text-decoration:none;">صفحه اصلی</a>
        <a href="." style="color:#bb86fc; text-decoration:none; font-weight:bold;">لیست داستان‌ها</a>
        <?php if (is_logged_in()): ?>
            <span style="color:#03dac6; font-size:14px;">کادر فعال: <strong><?php echo htmlspecialchars($_SESSION['username']); ?></strong></span>
        <?php endif; ?>
    </nav>
</header>

<div class="details-header">
    <div class="details-header-overlay"></div>
    <div class="details-content-wrapper">
        <img class="details-cover" src="<?php echo htmlspecialchars($manga['cover_image'] ?: "https://placehold.co/300x450/121214/ffffff?text=No+Cover"); ?>" alt="<?php echo htmlspecialchars($manga['title']); ?>">
        
        <div class="details-meta">
            <h1 style="color:#fff; margin:0 0 10px 0; font-size:32px; font-weight:900; text-shadow: 0 4px 15px rgba(0,0,0,0.6);"><?php echo htmlspecialchars($manga['title']); ?></h1>
            <p style="color:#aaa; font-size:14px; margin:0 0 15px 0;">تاریخ انتشار در پلتفرم مانگاتا: <?php echo date('Y-m-d', strtotime($manga['created_at'])); ?></p>
            
            <div class="badge-row">
                <?php if (!empty($manga['author'])): ?>
                    <span class="badge-meta accent">✍️ نویسنده: <?php echo htmlspecialchars($manga['author']); ?></span>
                <?php endif; ?>
                <?php if (!empty($manga['release_year'])): ?>
                    <span class="badge-meta cyan">📅 سال انتشار: <?php echo htmlspecialchars($manga['release_year']); ?></span>
                <?php endif; ?>
                <?php if (!empty($manga['genres'])): ?>
                    <?php 
                    $genres_array = explode(',', $manga['genres']);
                    foreach ($genres_array as $g): $g = trim($g); if (!empty($g)): ?>
                        <span class="badge-meta orange">🏷️ ژانر: <?php echo htmlspecialchars($g); ?></span>
                    <?php endif; endforeach; ?>
                <?php endif; ?>
            </div>
            <?php if (!empty($manga['main_characters'])): ?>
                <p style="color:#03dac6; font-size:13px; margin:12px 0 0 0; font-weight:bold; text-shadow:0 2px 4px rgba(0,0,0,0.6);">🌟 شخصیت‌های اصلی: <span style="color:#e0e0e0; font-weight:normal;"><?php echo htmlspecialchars($manga['main_characters']); ?></span></p>
            <?php endif; ?>

            <?php if (is_logged_in()): ?>
                <div style="margin-top:20px; display:flex; gap:12px; align-items:center; flex-wrap:wrap; z-index:100; position:relative;">
                    <form action="" method="POST" style="margin:0; padding:0; background:transparent; border:none; display:inline-block;">
                        <button type="submit" name="toggle_bookmark_action" class="btn" style="background:<?php echo $is_bookmarked ? '#b71c1c' : '#7c4dff'; ?>; color:white; font-weight:bold; padding:10px 18px; border-radius:30px; border:none; font-size:13px; cursor:pointer;" class="btn-bookmark">
                            <?php echo $is_bookmarked ? '❌ حذف از نشانک‌ها' : '📌 اضافه به نشانک‌ها (علاقه‌مندی)'; ?>
                        </button>
                    </form>
                    <?php if ($is_bookmarked): ?>
                        <form action="" method="POST" style="margin:0; padding:0; background:transparent; border:none; display:inline-block;">
                            <button type="submit" name="cycle_bookmark_status_action" class="btn" style="background:#f59e0b; color:black; font-weight:bold; padding:10px 18px; border-radius:30px; border:none; font-size:13px; cursor:pointer;">
                                🔄 وضعیت: <?php echo htmlspecialchars($bookmark_status === 'Reading' ? 'در حال خواندن 🟢' : ($bookmark_status === 'Completed' ? 'خوانده شده 🏆' : 'علاقه‌مندی ⭐')); ?>
                            </button>
                        </form>
                    <?php endif; ?>
                </div>
            <?php endif; ?>
        </div>
    </div>
</div>

<div class="container" style="max-width: 1100px; padding: 0 15px;">
    
    <!-- Synopsis -->
    <div class="section-box">
        <h3 style="color:#ff7597; margin-top:0; font-size:18px; font-weight:bold; display:flex; align-items:center; gap:8px;">
            <span>ℹ️</span> خلاصه داستان و زیربنای اثر
        </h3>
        <p style="color:#ccc; font-size:15px; line-height:1.7; margin:15px 0 0 0; text-align:justify;">
            <?php echo nl2br(htmlspecialchars($manga['description'])); ?>
        </p>
    </div>

    <!-- Translation staff active -->
    <?php if (!empty($allocated_staff)): ?>
    <div class="section-box">
        <h3 style="color:#03dac6; margin-top:0; font-size:18px; font-weight:bold; display:flex; align-items:center; gap:8px;">
            <span>👥</span> عوامل فنی و ترجمه تخصصی مانگاتا
        </h3>
        <p style="color:#777; font-size:12px; margin:5px 0 0 0;">افرادی که برای ترجمه، ویرایش، کلین و هماهنگی بصری این اثر در دیتابیس ثبت شده‌اند.</p>
        
        <div class="staff-avatar-row">
            <?php foreach ($allocated_staff as $staff): ?>
                <div class="staff-card">
                    <div class="staff-avatar-bubble">
                        <?php echo mb_substr(htmlspecialchars($staff['username']), 0, 1, 'utf-8'); ?>
                    </div>
                    <div>
                        <h4 style="color:#fff; margin:0 0 3px 0; font-size:13px; font-weight:bold;"><?php echo htmlspecialchars($staff['username']); ?></h4>
                        <span style="color:#9e9e9e; font-size:10px; font-weight:bold;"><?php echo htmlspecialchars($staff['role']); ?></span>
                    </div>
                </div>
            <?php endforeach; ?>
        </div>
    </div>
    <?php endif; ?>

    <!-- Chapters list table -->
    <div class="section-box">
        <div style="display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid rgba(255,255,255,0.05); padding-bottom:12px; margin-bottom:15px;">
            <h3 style="color:#ffb300; margin:0; font-size:18px; font-weight:bold; display:flex; align-items:center; gap:8px;">
                <span>📖</span> چپترهای آنلاین پیاده‌سازی شده در ریدر
            </h3>
            <span style="color:#888; font-size:13px; font-weight:bold;"><?php echo count($chapters); ?> چپتر در دسترس</span>
        </div>

        <div class="chapter-table-list">
            <?php if (!empty($chapters)): ?>
                <?php foreach ($chapters as $index => $c): ?>
                    <div class="chapter-item-row">
                        <div>
                            <span style="color:#fff; font-weight:900; font-size:15px;">چپتر <?php echo (float)$c['chapter_number']; ?></span>
                            <?php if ($c['title']): ?>
                                <span style="color:#888; font-size:13px; margin-right:10px;">- <?php echo htmlspecialchars($c['title']); ?></span>
                            <?php endif; ?>
                        </div>
                        
                        <div style="display:flex; gap:10px; align-items:center;">
                            <span style="color:#555; font-size:11px; margin-left:10px; font-weight:bold;"><?php echo date('Y/m/d', strtotime($c['created_at'])); ?></span>
                            <a href="reader.php?chapter_id=<?php echo $c['id']; ?>" class="btn btn-sm" style="background:#ff7597; color:#000; font-weight:bold; padding:6px 16px; border-radius:6px; text-decoration:none;">مطالعه آنلاین 👁️</a>
                        </div>
                    </div>
                <?php endforeach; ?>
            <?php else: ?>
                <div style="text-align:center; padding: 40px; color:#555;">
                    <p style="margin:0; font-size:14px;">هنوز هیچ چپتری برای این اثر آپلود نشده است. ⌛</p>
                </div>
            <?php endif; ?>
        </div>
    </div>

</div>

<footer style="margin-top:60px; text-align:center; padding:30px; border-top:1px solid rgba(255,255,255,0.05); color:#444; font-size:12px;">
    MANGATA MANHWA PLATFORM &copy; 2026 - طراحی بومی و مدرن مبتنی بر دیتابیس سینک کامل اندروید
</footer>

</body>
</html>
