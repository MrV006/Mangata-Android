<?php
/**
 * Standalone Mangata Portal - Web Manga Reader Screen
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
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo htmlspecialchars($manga_title); ?> - چپتر <?php echo (float)$chap['chapter_number']; ?> | ریدر مانگاتا</title>
    <link rel="stylesheet" href="style.css">
    <style>
        body {
            background-color: #0c0a0f !important;
            margin: 0;
            padding: 0;
        }
        .reader-container {
            max-width: 800px;
            margin: 0 auto;
            padding: 0 10px;
            box-shadow: 0 0 30px rgba(0,0,0,0.8);
            background: #121016;
        }
        .reader-header {
            background: #121016;
            border-bottom: 1px solid #ff7597;
            padding: 15px 20px;
            position: sticky;
            top: 0;
            z-index: 100;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .manga-image {
            width: 100%;
            height: auto;
            display: block;
            margin: 0 auto;
            border-bottom: 2px solid #000;
        }
        .reader-footer {
            background: #121016;
            padding: 30px 20px;
            text-align: center;
            border-top: 1px solid #333;
            margin-top: 20px;
        }
    </style>
</head>
<body>

<div class="reader-header">
    <div>
        <h2 style="color:#ff7597; margin:0; font-size:18px;"><?php echo htmlspecialchars($manga_title); ?></h2>
        <span style="color:#aaa; font-size:13px;">چپتر <?php echo (float)$chap['chapter_number']; ?> <?php echo htmlspecialchars($chap['title'] ?: ''); ?></span>
    </div>
    <a href="." class="btn btn-sm" style="background:#444; color:#fff; font-weight:bold;">بازگشت به سایت اصلی ↩</a>
</div>

<div class="reader-container">
    <?php if (!empty($images)): ?>
        <?php foreach ($images as $img_url): ?>
            <img src="<?php echo htmlspecialchars($img_url); ?>" class="manga-image" alt="Manga Page" loading="lazy">
        <?php endforeach; ?>
    <?php else: ?>
        <div style="text-align:center; padding: 100px 20px; color:#888;">
            <p style="font-size:18px; margin-bottom:20px;">تصویر ریدر بارگذاری مانهوا یافت نشد.</p>
            <p style="font-size:13px;">این خطا ممکن است ناشی از خالی بودن فایل فشرده چپتر یا تصاویر باشد.</p>
        </div>
    <?php endif; ?>
</div>

<div class="reader-footer">
    <h4 style="color:#bb86fc; margin:0 0 10px 0;">خوانش چپتر به پایان رسید</h4>
    <p style="color:#666; font-size:12px; margin-bottom:20px;">با تشکر از همراهی شما با رسانه ترجمه و انتشار مانگاتا</p>
    <a href="." class="btn" style="background: linear-gradient(135deg, #6200ee, #7c4dff); font-weight:bold;">بازگشت به مانهواهای فعال</a>
</div>

</body>
</html>
