<?php
/**
 * Standalone Mangata Portal - Web Core Frontend Dashboard
 * Connected directly to mrvir111_mangata_db database
 */

require_once __DIR__ . '/config.php';

// Handle user login internally for the web platform
$login_msg = '';
if (isset($_POST['web_login'])) {
    $username = trim($_POST['username'] ?? '');
    $password = trim($_POST['password'] ?? '');

    if (!empty($username) && !empty($password)) {
        $stmt = $pdo->prepare("SELECT * FROM mangata_users WHERE username = ? OR email = ?");
        $stmt->execute([$username, $username]);
        $user = $stmt->fetch();

        if ($user && password_verify($password, $user['password_hash'])) {
            $_SESSION['user_id'] = $user['id'];
            $_SESSION['username'] = $user['username'];
            $_SESSION['user_role'] = $user['role'];
            $login_msg = '<div class="success-message">خوش آمدید، ' . htmlspecialchars($user['username']) . '! ورود موفقیت‌آمیز بود.</div>';
        } else {
            $login_msg = '<div class="error-message">نام کاربری یا رمز عبور اشتباه است.</div>';
        }
    } else {
        $login_msg = '<div class="error-message">لطفاً تمامی فیلدها را پر کنید.</div>';
    }
}

// Handle user logout
if (isset($_GET['logout'])) {
    session_destroy();
    header("Location: " . strtok($_SERVER["REQUEST_URI"], '?'));
    exit;
}

// Handle recruitment exam upload form submit in pure PHP
$upload_msg = '';
if (isset($_POST['submit_exam'])) {
    $username = trim($_POST['applicant_username'] ?? '');
    $email = trim($_POST['applicant_email'] ?? '');
    $password = trim($_POST['applicant_password'] ?? '');

    if (!empty($username) && !empty($email) && !empty($password) && !empty($_FILES['exam_file'])) {
        // Create user if they don't exist
        $stmt = $pdo->prepare("SELECT id FROM mangata_users WHERE username = ? OR email = ?");
        $stmt->execute([$username, $email]);
        $candidate_id = $stmt->fetchColumn();

        $allowed_to_upload = true;

        if (!$candidate_id) {
            $password_hash = password_hash($password, PASSWORD_BCRYPT);
            try {
                $stmt = $pdo->prepare("INSERT INTO mangata_users (username, email, password_hash, role) VALUES (?, ?, ?, 'subscriber')");
                $stmt->execute([$username, $email, $password_hash]);
                $candidate_id = $pdo->lastInsertId();
            } catch (Exception $e) {
                $upload_msg = '<div class="error-message">خطا در فرآیند ثبت‌نام متقاضی جدید: ' . $e->getMessage() . '</div>';
                $allowed_to_upload = false;
            }
        }

        if ($allowed_to_upload && $candidate_id) {
            $exam_file = $_FILES['exam_file'];
            if ($exam_file['error'] === UPLOAD_ERR_OK) {
                $upload_dir = __DIR__ . '/uploads/exams/';
                if (!is_dir($upload_dir)) {
                    mkdir($upload_dir, 0755, true);
                }

                $file_ext = strtolower(pathinfo($exam_file['name'], PATHINFO_EXTENSION));
                $safe_file_name = time() . '_' . $candidate_id . '.' . $file_ext;
                $file_dest = $upload_dir . $safe_file_name;

                if (move_uploaded_file($exam_file['tmp_name'], $file_dest)) {
                    $protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? "https://" : "http://";
                    $file_url = $protocol . $_SERVER['HTTP_HOST'] . '/uploads/exams/' . $safe_file_name;

                    $stmt = $pdo->prepare("INSERT INTO mangata_exams (user_id, file_name, file_url, status) VALUES (?, ?, ?, 'Pending')");
                    $stmt->execute([$candidate_id, basename($exam_file['name']), $file_url]);

                    $upload_msg = '<div class="success-message">فایل آزمون استخدامی شما با نام ' . htmlspecialchars($exam_file['name']) . ' با موفقیت ثبت گردید و در صف بررسی نمرات قرار گرفت! می‌توانید با همین مشخصات وارد اپلیکیشن اندروید نیز شوید.</div>';
                } else {
                    $upload_msg = '<div class="error-message">خطا در ذخیره‌سازی فایل روی حافظه سرور رخ داد.</div>';
                }
            } else {
                $upload_msg = '<div class="error-message">خطای آپلود فایل استخدامی: ' . $exam_file['error'] . '</div>';
            }
        }
    } else {
        $upload_msg = '<div class="error-message">لطفاً کلیه فیلدها و فایل مربوطه را به صورت همزمان ارسال نمایید.</div>';
    }
}

// Handle grading exams from Super Admin
$admin_msg = '';
if (isset($_POST['grade_exam']) && is_admin()) {
    $exam_id = (int)$_POST['exam_id'];
    $score = (int)$_POST['score'];
    $status = $_POST['status'];

    if (in_array($status, ['Accepted', 'Rejected', 'Pending'])) {
        $pdo->beginTransaction();
        
        $stmt = $pdo->prepare("UPDATE mangata_exams SET status = ?, score = ? WHERE id = ?");
        $stmt->execute([$status, $score, $exam_id]);

        if ($status === 'Accepted') {
            $stmt = $pdo->prepare("SELECT user_id FROM mangata_exams WHERE id = ?");
            $stmt->execute([$exam_id]);
            $cand_id = $stmt->fetchColumn();

            if ($cand_id) {
                $stmt = $pdo->prepare("UPDATE mangata_users SET role = 'staff_translator' WHERE id = ? AND role = 'subscriber'");
                $stmt->execute([$cand_id]);
            }
        }

        $pdo->commit();
        $admin_msg = '<div class="success-message">نمره‌دهی آزمون شماره ' . $exam_id . ' با موفقیت به وضعیت ' . htmlspecialchars($status) . ' و نمره ' . $score . ' ارتقا یافت.</div>';
    }
}

// Handle manhwa creation from Super Admin
if (isset($_POST['create_manhwa_web']) && is_admin()) {
    $title = trim($_POST['manga_title'] ?? '');
    $desc = trim($_POST['manga_desc'] ?? '');
    $cover = trim($_POST['manga_cover'] ?? '');

    if (!empty($title)) {
        $stmt = $pdo->prepare("INSERT INTO mangata_mangas (title, description, cover_image) VALUES (?, ?, ?)");
        $stmt->execute([$title, $desc, $cover]);
        $admin_msg = '<div class="success-message font-bold">پروژه مانهوا جدید «' . htmlspecialchars($title) . '» با موفقیت به صورت زنده ایجاد و سینک گردید.</div>';
    }
}

// Handle staff crew assignment from Super Admin
if (isset($_POST['assign_staff_web']) && is_admin()) {
    $m_id = (int)($_POST['assign_manga_id'] ?? 0);
    $u_id = (int)($_POST['assign_user_id'] ?? 0);
    $role = trim($_POST['assign_role'] ?? '');

    if ($m_id > 0 && $u_id > 0 && !empty($role)) {
        $stmt = $pdo->prepare("INSERT INTO mangata_staff (user_id, manga_id, role) VALUES (?, ?, ?)");
        $stmt->execute([$u_id, $m_id, $role]);
        $admin_msg = '<div class="success-message">عضو تیم استخدام‌شده با آیدی کاربری ' . $u_id . ' به مانهوا به عنوان ' . htmlspecialchars($role) . ' متصل شد.</div>';
    }
}

// Fetch all mangas
$stmt = $pdo->query("SELECT * FROM mangata_mangas ORDER BY id DESC");
$mangas = $stmt->fetchAll();
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>مانگاتا | پلتفرم هوشمند ترجمه، مدیریت و خوانش مانهوا</title>
    <link rel="stylesheet" href="style.css">
    <style>
        /* Modern Glass & Neon details styling inside custom application */
        header {
            box-shadow: 0 4px 20px rgba(0,0,0,0.5);
        }
        .hero {
            background: linear-gradient(135deg, rgba(98,0,238,0.2) 0%, rgba(255,117,151,0.1) 100%), #1e1e1e;
            padding: 50px 30px;
            text-align: center;
            border-radius: 12px;
            margin-bottom: 30px;
            border: 1px solid rgba(255,255,255,0.05);
            position: relative;
            overflow: hidden;
        }
        .hero::after {
            content: '';
            position: absolute;
            top: -50%;
            left: -50%;
            width: 200%;
            height: 200%;
            background: radial-gradient(circle, rgba(124,77,255,0.08) 0%, transparent 70%);
            pointer-events: none;
        }
        .manhwa-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
            gap: 24px;
        }
        .card-manga {
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            overflow: hidden;
            border: 1px solid rgba(255,255,255,0.04);
        }
        .card-manga:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 24px rgba(124,77,255,0.2);
            border-color: rgba(124,77,255,0.3);
        }
        .btn-sm {
            padding: 6px 12px;
            font-size: 12px;
        }
        .panel-flex {
            display: grid;
            grid-template-columns: 1fr;
            gap: 20px;
        }
        @media(min-width: 768px) {
            .panel-flex {
                grid-template-columns: 2fr 1fr;
            }
        }
        .badge {
            background: #ff7597;
            color: #000;
            padding: 2px 6px;
            border-radius: 10px;
            font-size: 11px;
            font-weight: bold;
        }
    </style>
</head>
<body>

<header>
    <h1><a href="." style="color:#bb86fc; font-weight:bold;">MANGATA | مانگاتا</a></h1>
    <nav style="display: flex; align-items: center; gap: 20px;">
        <a href="." style="color:#bb86fc; text-decoration:none; font-weight:bold;">صفحه اصلی</a>
        <a href="#manhwa" style="color:#fff; text-decoration:none;">لیست کارهای فعال</a>
        <a href="#recruitment" style="color:#fff; text-decoration:none;">فرصت‌های استخدام</a>
        <?php if (!is_logged_in()): ?>
            <a href="#auth" class="btn btn-sm" style="background:#03dac6; color:#000; font-weight:bold;">ورود اعضا</a>
        <?php else: ?>
            <span style="color:#03dac6; font-size:14px;">کاربر: <strong><?php echo htmlspecialchars($_SESSION['username']); ?></strong> (<?php echo htmlspecialchars($_SESSION['user_role']); ?>)</span>
            <a href="?logout=1" class="btn btn-sm" style="background:#b71c1c;">خروج</a>
        <?php endif; ?>
    </nav>
</header>

<div class="container">
    
    <!-- Hero Header -->
    <div class="hero">
        <h2 style="color:#ff7597; font-size: 32px; margin:0 0 15px 0;">قدرتمندترین پورتال اختصاصی و زنده مانگاتا</h2>
        <p style="color:#ccc; font-size:16px; max-width: 800px; margin: 0 auto; line-height:1.7;">
            از وردپرس خارج شدیم! اکنون با یک معماری فوق‌العاده سریع و اختصاصی PHP مجهز به پایگاه‌داده واقعی <code style="background:#000; padding:2px 8px; border-radius:4px; color:#bb86fc;">mrvir111_mangata_db</code> به صورت تمام سینک با اپلیکیشن اندروید در خدمت شماییم.
        </p>
    </div>

    <?php echo $login_msg; ?>
    <?php echo $upload_msg; ?>
    <?php echo $admin_msg; ?>

    <!-- Active Manhwas Section -->
    <h2 id="manhwa" style="color:#ff7597; margin-top:40px; border-bottom: 2px solid #ff7597; padding-bottom:10px;">🎨 مانهوا‌های در حال انتشار پلتفرم</h2>
    
    <div class="manhwa-grid">
        <?php if (!empty($mangas)): ?>
            <?php foreach ($mangas as $m): ?>
                <div class="card card-manga">
                    <div>
                        <?php 
                        $cover = !empty($m['cover_image']) ? $m['cover_image'] : 'https://placehold.co/300x450/1e1e1e/7c4dff?text=No+Cover';
                        ?>
                        <img src="<?php echo htmlspecialchars($cover); ?>" style="width:100%; height:280px; object-fit:cover; border-radius:6px; margin-bottom:15px; border: 1px solid #333;" alt="<?php echo htmlspecialchars($m['title']); ?>">
                        <h3 style="color:#fff; margin:0 0 10px 0; font-size:18px;"><?php echo htmlspecialchars($m['title']); ?></h3>
                        <p style="color:#aaa; font-size:13px; line-height: 1.6; margin-bottom:15px;"><?php echo htmlspecialchars($m['description']); ?></p>
                    </div>
                    
                    <div style="margin-top:20px; border-top: 1px solid #2d2d2d; padding-top:12px;">
                        <h4 style="color:#bb86fc; margin:0 0 12px 0; font-size:14px;">چپترهای فعال در ریدر:</h4>
                        <?php
                        $stmt_ch = $pdo->prepare("SELECT * FROM mangata_chapters WHERE manga_id = ? ORDER BY chapter_number ASC");
                        $stmt_ch->execute([$m['id']]);
                        $chaps = $stmt_ch->fetchAll();

                        if (!empty($chaps)): ?>
                            <div style="display:flex; flex-direction:column; gap:8px;">
                                <?php foreach ($chaps as $c): ?>
                                    <div style="background:#252525; padding:8px 12px; border-radius:6px; display:flex; justify-content:space-between; align-items:center;">
                                        <span style="font-size:12px; font-weight:bold; color:#e0e0e0;">چپتر <?php echo (float)$c['chapter_number']; ?> - <?php echo htmlspecialchars($c['title']); ?></span>
                                        <a href="reader.php?chapter_id=<?php echo $c['id']; ?>" class="btn btn-sm" style="padding: 4px 10px; font-size:11px; font-weight:bold; background:#7c4dff;">خوانش ریدر 👁️</a>
                                    </div>
                                <?php endforeach; ?>
                            </div>
                        <?php else: ?>
                            <span style="font-size:12px; color:#555;">هنوز چپتری برای این کار قرار نگرفته است.</span>
                        <?php endif; ?>
                    </div>
                </div>
            <?php endforeach; ?>
        <?php else: ?>
            <div class="card" style="grid-column: 1 / -1; text-align:center; padding: 40px;">
                <p style="color:#666; margin:0; font-size: 16px;">هنوز هیچ مانهوایی به همراه دیتابیس ثبت نشده است.</p>
            </div>
        <?php endif; ?>
    </div>

    <!-- Dual Layout: Recruitment Portal & Control Auth Desk -->
    <div class="panel-flex" style="margin-top:40px;">
        
        <!-- Recruitment Form -->
        <div id="recruitment" class="card" style="border-right: 5px solid #ff7597; background: #1a1a1a;">
            <h2 style="color:#ff7597; margin-top:0;">🤝 استخدام در تیم ترجمه و طراحی مانگاتا</h2>
            <p style="color:#b3b3b3; font-size:14px; line-height:1.7; margin-bottom:20px;">
                امتحان‌های ورودی بلافاصله با اپلیکیشن و حساب‌های سرور یکپارچه می‌شوند. با پر کردن فرم زیر یک حساب کاربری برای شما ثبت گردیده که با آن قادر خواهید بود وارد اپلیکیشن اندروید شده و کار در تیم ترجمه، طراحی، کلینری یا تایپ را شروع کنید.
            </p>

            <form action="#recruitment" method="POST" enctype="multipart/form-data" style="background:#252525; padding: 25px; border-radius:8px; border:1px solid #333;">
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap:15px;">
                    <div>
                        <label style="font-weight:bold; font-size:13px; color:#bb86fc; display:block; margin-bottom:5px;">نام کاربری انتخابی:</label>
                        <input type="text" name="applicant_username" placeholder="نام انگلیسی (مثلاً amirreza)" required style="margin:0;">
                    </div>
                    <div>
                        <label style="font-weight:bold; font-size:13px; color:#bb86fc; display:block; margin-bottom:5px;">آدرس ایمیل:</label>
                        <input type="email" name="applicant_email" placeholder="applicant@example.com" required style="margin:0;">
                    </div>
                </div>

                <div style="margin-top:15px;">
                    <label style="font-weight:bold; font-size:13px; color:#bb86fc; display:block; margin-bottom:5px;">رمز ورود به پنل و اپلیکیشن:</label>
                    <input type="password" name="applicant_password" placeholder="کلمه‌عبور ایمن" required style="margin:0;">
                </div>

                <div style="margin-top:15px;">
                    <label style="font-weight:bold; font-size:13px; color:#bb86fc; display:block; margin-bottom:5px;">فایل پاسخ آزمون استخدامی (PDF, ZIP, PNG):</label>
                    <input type="file" name="exam_file" required style="background:transparent; border:none; padding:0; margin:0;">
                </div>

                <button type="submit" name="submit_exam" class="btn" style="width:100%; margin-top:20px; font-weight:bold; background: linear-gradient(135deg, #6200ee, #7c4dff); box-shadow: 0 4px 10px rgba(98,0,238,0.4);">ارسال و ثبت اطلاعات آزمون متقاضی</button>
            </form>
        </div>

        <!-- Auth Terminal or User panel -->
        <div id="auth" class="card" style="border-top: 5px solid #03dac6; background: #1a1a1a;">
            <?php if (!is_logged_in()): ?>
                <h2 style="color:#03dac6; margin-top:0;">🔐 ورود به پایگاه فرماندهی</h2>
                <p style="color:#ccc; font-size:13px; margin-bottom:20px;">مخصوص مدیران کل و یا مترجمین جهت پیگیری وضعیت کارهای تیمی.</p>
                
                <form action="#auth" method="POST">
                    <label style="font-size:13px; color:#03dac6; font-weight:bold;">شناسه کاربری یا ایمیل:</label>
                    <input type="text" name="username" placeholder="username" required>

                    <label style="font-size:13px; color:#03dac6; font-weight:bold; display:block; margin-top:10px;">رمز عبور:</label>
                    <input type="password" name="password" placeholder="••••••••" required>

                    <button type="submit" name="web_login" class="btn" style="width:100%; margin-top:15px; background:#03dac6; color:#000; font-weight:bold;">ورود امن به پنل کاربری</button>
                </form>
                <div style="margin-top: 15px; padding: 10px; background:#222; border-radius:4px; font-size:11px; text-align:center; color:#888;">
                    اکانت مدیر پیش‌فرض جهت تست اولیه:<br>
                    نام کاربری: <code style="color:#fff;">admin</code> | رمزعبور: <code style="color:#fff;">admin123</code>
                </div>
            <?php else: ?>
                <h2 style="color:#03dac6; margin-top:0;">👤 حساب من</h2>
                <div style="background:#252525; padding:15px; border-radius:6px; border:1px solid #333;">
                    <p style="margin:5px 0;">کاربر جاری: <strong style="color:#fff;"><?php echo htmlspecialchars($_SESSION['username']); ?></strong></p>
                    <p style="margin:5px 0;">نقش دسترسی: <strong style="color:#03dac6;"><?php echo htmlspecialchars($_SESSION['user_role']); ?></strong></p>
                </div>
                <div style="margin-top:-5px; font-size: 11px; color:#888; text-align:center;" class="font-bold">
                    [اتصال دوجانبه مستقیم برقرار است]
                </div>
            <?php endif; ?>
        </div>

    </div>

    <!-- Administrative Operations Command Console (SUPER ADMIN ONLY) -->
    <?php if (is_admin()): ?>
        <div class="card" style="margin-top:40px; border: 2px dashed #ff5722; background: #1a1313; padding: 25px;">
            <h2 style="color: #ff5722; margin-top:0; font-weight:bold;">🛡️ پنل مدیریت فرماندهی مانگاتا (مخصوص ادمین)</h2>
            <p style="color:#ccc; font-size:13px; margin-bottom:25px;">در این بخش می‌توانید آزمون‌های داوطلبین را تصحیح، مانهوا ایجاد یا ویرایش و وظایف ترجمه را واگذار کنید.</p>

            <!-- Grid for Administrative mini operations Forms -->
            <div style="display:grid; grid-template-columns:1fr; gap:25px; margin-bottom:30px;" class="panel-flex">
                
                <!-- Create manga -->
                <div style="background:#252525; padding:20px; border-radius:8px; border: 1px solid #444;">
                    <h3 style="color:#bb86fc; margin-top:0; font-size:16px;">➕ ایجاد مانهوای کار جدید</h3>
                    <form action="" method="POST">
                        <label style="font-size:12px; font-weight:bold; color:#ff7597;">عنوان مانهوا:</label>
                        <input type="text" name="manga_title" placeholder="مثلاً بازگشت پادشاه ترجمه" required style="padding:8px; margin:5px 0 10px 0;">

                        <label style="font-size:12px; font-weight:bold; color:#ff7597;">توضیحات معرفی پروژه:</label>
                        <textarea name="manga_desc" placeholder="توضیحات کوتاه مانهوا..." required style="height:80px; padding:8px; margin:5px 0 10px 0;"></textarea>

                        <label style="font-size:12px; font-weight:bold; color:#ff7597;">لینک تصویر آدرس کاور:</label>
                        <input type="text" name="manga_cover" placeholder="https://image-url" style="padding:8px; margin:5px 0 10px 0;">

                        <button type="submit" name="create_manhwa_web" class="btn btn-sm" style="background:#ff5722; width:100%; font-weight:bold;">ثبت و همگام‌سازی مانهوا</button>
                    </form>
                </div>

                <!-- Crew assignment -->
                <div style="background:#252525; padding:20px; border-radius:8px; border:1px solid #444;">
                    <h3 style="color:#bb86fc; margin-top:0; font-size:16px;">👤 ایجاد شراکت و فریلنسر چپتر</h3>
                    <form action="" method="POST">
                        <label style="font-size:12px; font-weight:bold; color:#ff7597;">آیدی عددی مانهوا پروژه:</label>
                        <input type="number" name="assign_manga_id" required style="padding:8px; margin:5px 0 10px 0;">

                        <label style="font-size:12px; font-weight:bold; color:#ff7597;">آیدی عددی طراح/مترجم استخدام شده:</label>
                        <input type="number" name="assign_user_id" required style="padding:8px; margin:5px 0 10px 0;">

                        <label style="font-size:12px; font-weight:bold; color:#ff7597;">سمت انتخابی ترجمه:</label>
                        <select name="assign_role" style="padding:8px; margin:5px 0 10px 0;">
                            <option value="Translator">مترجم متن (Translator)</option>
                            <option value="Cleaner">کلینر حباب‌ها (Cleaner)</option>
                            <option value="Redrawer">طراح بازسازی پس‌زمینه (Redrawer)</option>
                            <option value="Typesetter">تایپ‌ستر هوشمند (Typesetter)</option>
                        </select>

                        <button type="submit" name="assign_staff_web" class="btn btn-sm" style="background:#ff5722; width:100%; font-weight:bold;">ثبت وظایف در دیتابیس کارهای فعال</button>
                    </form>
                </div>

            </div>

            <!-- Recruitment Exams Administration Grade Table -->
            <h3 style="color:#ff5722; margin-top:30px; margin-bottom:15px; font-size:18px;">📋 امتحانات استخدامی داوطلبین جدید:</h3>
            <?php
            $stmt_all_ex = $pdo->query("SELECT e.*, u.username as applicant_user, u.email as applicant_email FROM mangata_exams e JOIN mangata_users u ON e.user_id = u.id ORDER BY e.id DESC");
            $exams = $stmt_all_ex->fetchAll();

            if (!empty($exams)): ?>
                <div style="overflow-x:auto;">
                    <table>
                        <thead>
                            <tr>
                                <th>شناسه متقاضی</th>
                                <th>نام کاربری</th>
                                <th>ایمیل تاییدیه</th>
                                <th>فایل ارسال شبیه‌سازی</th>
                                <th>وضعیت تصحیح</th>
                                <th>نمره ثبت شده</th>
                                <th>اقدام تصحیح زنده</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php foreach ($exams as $ex): ?>
                                <tr>
                                    <td><?php echo (int)$ex['user_id']; ?></td>
                                    <td><strong><?php echo htmlspecialchars($ex['applicant_user']); ?></strong></td>
                                    <td><?php echo htmlspecialchars($ex['applicant_email']); ?></td>
                                    <td><a href="<?php echo htmlspecialchars($ex['file_url']); ?>" target="_blank" style="color:#ff7597; font-weight:bold; text-decoration:none;">دانلود آزمون 📥</a></td>
                                    <td>
                                        <span class="badge" style="background: <?php echo $ex['status'] === 'Accepted' ? '#1b5e20' : ($ex['status'] === 'Rejected' ? '#b71c1c' : '#bd9601'); ?>; color:#fff;">
                                            <?php echo htmlspecialchars($ex['status']); ?>
                                        </span>
                                    </td>
                                    <td><?php echo $ex['score'] !== null ? htmlspecialchars($ex['score']) . ' از 100' : 'در انتظار نمره'; ?></td>
                                    <td>
                                        <form action="" method="POST" style="display:flex; gap:10px; margin:0; padding:0; background:transparent; border:none; max-width:none;">
                                            <input type="hidden" name="exam_id" value="<?php echo $ex['id']; ?>">
                                            <input type="number" name="score" placeholder="نمره (0-100)" min="0" max="100" required style="width:100px; margin:0; padding:5px;">
                                            <select name="status" style="width:150px; margin:0; padding:5px;">
                                                <option value="Accepted">قبولی متقاضی ✅</option>
                                                <option value="Rejected">رد درخواست ❌</option>
                                                <option value="Pending">در انتظار نمرات ⏳</option>
                                            </select>
                                            <button type="submit" name="grade_exam" class="btn btn-sm" style="background:#ff5722; padding:6px 15px; font-weight:bold;">ثبت نمرات</button>
                                        </form>
                                    </td>
                                </tr>
                            <?php endforeach; ?>
                        </tbody>
                    </table>
                </div>
            <?php else: ?>
                <p style="color:#888;">هیچ امتحان ارسالی در سیستم موجود نیست.</p>
            <?php endif; ?>

        </div>
    <?php endif; ?>

</div>

<footer style="background:#1a1a1a; padding: 25px; text-align:center; margin-top: 50px; border-top: 1px solid #333;">
    <p style="color:#888; font-size:14px; margin:0;">تمامی حقوق برای رسانه مانهواخوان مانگاتا (Mangata) محفوظ است | © 2026</p>
    <p style="color:#555; font-size:12px; margin-top:10px;">هماهنگ شده به صورت زنده و آنی با پایگاه داده و اپلیکیشن اختصاصی اندروید</p>
</footer>

</body>
</html>
