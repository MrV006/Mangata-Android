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
            // Generate session token (Ensure single-device restriction)
            $token = bin2hex(random_bytes(16));
            $stmt_update = $pdo->prepare("UPDATE mangata_users SET session_token = ? WHERE id = ?");
            $stmt_update->execute([$token, $user['id']]);

            $_SESSION['user_id'] = $user['id'];
            $_SESSION['username'] = $user['username'];
            $_SESSION['user_role'] = $user['role'];
            $_SESSION['session_token'] = $token;
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

// Handle general membership signup
$register_msg = '';
if (isset($_POST['web_register'])) {
    $username = trim($_POST['reg_username'] ?? '');
    $email = trim($_POST['reg_email'] ?? '');
    $password = trim($_POST['reg_password'] ?? '');

    if (!empty($username) && !empty($email) && !empty($password)) {
        // Check duplication
        $stmt_check = $pdo->prepare("SELECT COUNT(*) FROM mangata_users WHERE username = ? OR email = ?");
        $stmt_check->execute([$username, $email]);
        if ($stmt_check->fetchColumn() > 0) {
            $register_msg = '<div class="error-message">نام کاربری یا آدرس ایمیل تکراری است.</div>';
        } else {
            $password_hash = password_hash($password, PASSWORD_BCRYPT);
            $stmt_reg = $pdo->prepare("INSERT INTO mangata_users (username, email, password_hash, role) VALUES (?, ?, ?, 'subscriber')");
            $stmt_reg->execute([$username, $email, $password_hash]);
            $register_msg = '<div class="success-message">ثبت‌نام شما با موفقیت انجام شد! اکنون می‌توانید از فرم ورود برای دسترسی استفاده کنید.</div>';
        }
    } else {
        $register_msg = '<div class="error-message">لطفاً تمامی فیلدها را وارد کنید.</div>';
    }
}

// Handle self password/profile edit
$self_update_msg = '';
if (isset($_POST['update_own_profile']) && is_logged_in()) {
    $u_id = $_SESSION['user_id'];
    $email = trim($_POST['own_email'] ?? '');
    $new_password = trim($_POST['own_password'] ?? '');

    if (!empty($email)) {
        // Update email
        $stmt = $pdo->prepare("UPDATE mangata_users SET email = ? WHERE id = ?");
        $stmt->execute([$email, $u_id]);
        
        if (!empty($new_password)) {
            $pass_hash = password_hash($new_password, PASSWORD_BCRYPT);
            $stmt_pw = $pdo->prepare("UPDATE mangata_users SET password_hash = ? WHERE id = ?");
            $stmt_pw->execute([$pass_hash, $u_id]);
        }
        $self_update_msg = '<div class="success-message">مشخصات و رمز عبور حساب کاربری شما با موفقیت بروزرسانی شد.</div>';
    } else {
        $self_update_msg = '<div class="error-message">لطفاً آدرس ایمیل خود را خالی نگذارید.</div>';
    }
}

// Handle Wallet Recharge from Profile Dashboard
if (isset($_POST['quick_charge_web']) && is_logged_in()) {
    $amount = (int)($_POST['charge_amount'] ?? 15000);
    $stmt_up = $pdo->prepare("UPDATE mangata_users SET wallet_balance = wallet_balance + ? WHERE id = ?");
    $stmt_up->execute([$amount, $_SESSION['user_id']]);
    $self_update_msg = '<div class="success-message font-bold">🪙 شارژ موفقیت‌آمیز! کیف پول شما با موفقیت ' . number_format($amount) . ' تومان شارژ شد.</div>';
}

// Handle Profile Bookmark Cycle Status
if (isset($_POST['cycle_status_web']) && is_logged_in()) {
    $m_id = (int)$_POST['cycle_manga_id'];
    $current_status = $_POST['current_status'];
    $next_status = 'Reading';
    if ($current_status === 'Reading') {
        $next_status = 'Completed';
    } else if ($current_status === 'Completed') {
        $next_status = 'Favorite';
    }
    $stmt_up = $pdo->prepare("UPDATE mangata_bookmarks SET status = ? WHERE user_id = ? AND manga_id = ?");
    $stmt_up->execute([$next_status, $_SESSION['user_id'], $m_id]);
    $self_update_msg = '<div class="success-message">وضعیت مانهوای نشانک‌گذاری شده با موفقیت به «' . htmlspecialchars($next_status) . '» تغییر یافت.</div>';
}

// Handle Profile Bookmark Deletion
if (isset($_POST['remove_bookmark_web']) && is_logged_in()) {
    $m_id = (int)$_POST['remove_manga_id'];
    $stmt_del = $pdo->prepare("DELETE FROM mangata_bookmarks WHERE user_id = ? AND manga_id = ?");
    $stmt_del->execute([$_SESSION['user_id'], $m_id]);
    $self_update_msg = '<div class="success-message" style="background:#b71c1c;">مانهوا با موفقیت از بین لیست نشانک‌های شما حذف گردید.</div>';
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
    $genres = trim($_POST['manga_genres'] ?? '');
    $release_year = trim($_POST['manga_release_year'] ?? '');
    $main_characters = trim($_POST['manga_main_characters'] ?? '');
    $author = trim($_POST['manga_author'] ?? '');

    if (!empty($title)) {
        $stmt = $pdo->prepare("INSERT INTO mangata_mangas (title, description, cover_image, genres, release_year, main_characters, author) VALUES (?, ?, ?, ?, ?, ?, ?)");
        $stmt->execute([$title, $desc, $cover, $genres, $release_year, $main_characters, $author]);
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

// Handle manhwa edit/update from Super Admin
if (isset($_POST['edit_manhwa_web']) && is_admin()) {
    $m_id = (int)($_POST['edit_manga_id'] ?? 0);
    $title = trim($_POST['manga_title'] ?? '');
    $desc = trim($_POST['manga_desc'] ?? '');
    $cover = trim($_POST['manga_cover'] ?? '');
    $genres = trim($_POST['manga_genres'] ?? '');
    $release_year = trim($_POST['manga_release_year'] ?? '');
    $main_characters = trim($_POST['manga_main_characters'] ?? '');
    $author = trim($_POST['manga_author'] ?? '');

    if ($m_id > 0 && !empty($title)) {
        $stmt = $pdo->prepare("UPDATE mangata_mangas SET title = ?, description = ?, cover_image = ?, genres = ?, release_year = ?, main_characters = ?, author = ? WHERE id = ?");
        $stmt->execute([$title, $desc, $cover, $genres, $release_year, $main_characters, $author, $m_id]);
        $admin_msg = '<div class="success-message font-bold">پروژه مانهوا با آیدی ' . $m_id . ' با موفقیت به اطلاعات جدید بروزرسانی شد.</div>';
    }
}

// Handle manhwa deletion from Super Admin
if (isset($_POST['delete_manhwa_web']) && is_admin()) {
    $m_id = (int)($_POST['delete_manga_id'] ?? 0);
    if ($m_id > 0) {
        $pdo->beginTransaction();
        $pdo->prepare("DELETE FROM mangata_chapters WHERE manga_id = ?")->execute([$m_id]);
        $pdo->prepare("DELETE FROM mangata_staff WHERE manga_id = ?")->execute([$m_id]);
        $pdo->prepare("DELETE FROM mangata_mangas WHERE id = ?")->execute([$m_id]);
        $pdo->commit();
        $admin_msg = '<div class="success-message font-bold" style="background: #b71c1c;">پروژه مانهوا به همراه تمامی فصول و دسترسی‌های وابسته از دیتابیس به صورت ماندگار حذف گردید.</div>';
    }
}

// Handle chapter deletion from Super Admin
if (isset($_POST['delete_chapter_web']) && is_admin()) {
    $ch_id = (int)($_POST['delete_chapter_id'] ?? 0);
    if ($ch_id > 0) {
        $pdo->prepare("DELETE FROM mangata_chapters WHERE id = ?")->execute([$ch_id]);
        $admin_msg = '<div class="success-message">چپتر انتخابی با موفقیت حذف شد.</div>';
    }
}

// Handle user account upgrade (full edits) from Super Admin
if (isset($_POST['update_user_full_web']) && is_admin()) {
    $u_id = (int)($_POST['target_user_id'] ?? 0);
    $username = trim($_POST['target_username'] ?? '');
    $email = trim($_POST['target_email'] ?? '');
    $role = trim($_POST['target_role'] ?? '');
    $new_password = trim($_POST['target_password'] ?? '');
    
    $allowed_roles = ['administrator', 'subscriber', 'staff_translator', 'staff_redrawer', 'staff_cleaner', 'staff_ts'];

    if ($u_id > 0 && @in_array($role, $allowed_roles) && !empty($username) && !empty($email)) {
        // Prepare base update values
        $stmt = $pdo->prepare("UPDATE mangata_users SET username = ?, email = ?, role = ? WHERE id = ?");
        $stmt->execute([$username, $email, $role, $u_id]);
        
        // If a new password is provided, hash and update it
        if (!empty($new_password)) {
            $pass_hash = password_hash($new_password, PASSWORD_BCRYPT);
            $stmt_pw = $pdo->prepare("UPDATE mangata_users SET password_hash = ? WHERE id = ?");
            $stmt_pw->execute([$pass_hash, $u_id]);
        }
        
        $admin_msg = '<div class="success-message">پروفایل کاربر شماره ' . $u_id . ' با موفقیت ویرایش و ثبت گردید.</div>';
    } else {
        $admin_msg = '<div class="error-message">داده‌های ارسالی برای ویرایش حساب معتبر نیست یا فیلدها خالی رها شده‌اند.</div>';
    }
}

// Handle user deletion from Super Admin
if (isset($_POST['delete_user_web']) && is_admin()) {
    $u_id = (int)($_POST['target_user_id'] ?? 0);
    if ($u_id > 0 && $u_id != $_SESSION['user_id']) {
        $pdo->beginTransaction();
        $pdo->prepare("DELETE FROM mangata_exams WHERE user_id = ?")->execute([$u_id]);
        $pdo->prepare("DELETE FROM mangata_staff WHERE user_id = ?")->execute([$u_id]);
        $pdo->prepare("DELETE FROM mangata_users WHERE id = ?")->execute([$u_id]);
        $pdo->commit();
        $admin_msg = '<div class="success-message" style="background: #b71c1c;">کاربر انتخابی و مدارک وی به طور کامل پاکسازی شد.</div>';
    }
}

// Handle global settings update from Super Admin
if (isset($_POST['update_mangata_settings']) && is_admin()) {
    $app_active = trim($_POST['force_update_app_active'] ?? '0');
    $app_url = trim($_POST['force_update_app_url'] ?? '');
    $app_msg = trim($_POST['force_update_app_msg'] ?? '');
    $web_active = trim($_POST['force_update_web_active'] ?? '0');
    $web_version = trim($_POST['force_update_web_version'] ?? '1');
    $web_msg = trim($_POST['force_update_web_msg'] ?? '');

    set_mangata_setting('force_update_app_active', $app_active);
    set_mangata_setting('force_update_app_url', $app_url);
    set_mangata_setting('force_update_app_msg', $app_msg);
    set_mangata_setting('force_update_web_active', $web_active);
    set_mangata_setting('force_update_web_version', $web_version);
    set_mangata_setting('force_update_web_msg', $web_msg);

    $admin_msg = '<div class="success-message">تنظیمات آپدیت اجباری و سیستم بازنشانی دیتای کش با موفقیت روی سرور ثبت و اعمال گردید.</div>';
}

// Handle submitting a manga review from any registered user
if (isset($_POST['submit_manga_review']) && is_logged_in()) {
    $m_id = (int)($_POST['review_manga_id'] ?? 0);
    $rating = (int)($_POST['review_rating'] ?? 5);
    $text = trim($_POST['review_text'] ?? '');

    if ($m_id > 0 && !empty($text)) {
        $stmt_ins_rev = $pdo->prepare("INSERT INTO mangata_reviews (user_id, manga_id, rating, review_text) VALUES (?, ?, ?, ?)");
        $stmt_ins_rev->execute([$_SESSION['user_id'], $m_id, $rating, $text]);
        $login_msg = '<div class="success-message">دیدگاه و نقد ارزشمند شما با موفقیت ثبت شد و به صورت زنده در صفحه اول به نمایش درآمد! 🌸</div>';
    }
}

// Handle administrative blog creation
if (isset($_POST['create_blog_web']) && is_admin()) {
    $blog_title = trim($_POST['blog_title'] ?? '');
    $blog_excerpt = trim($_POST['blog_excerpt'] ?? '');
    $blog_content = trim($_POST['blog_content'] ?? '');
    $blog_img = trim($_POST['blog_img'] ?? 'https://picsum.photos/600/400');

    if (!empty($blog_title) && !empty($blog_content)) {
        $stmt_bg = $pdo->prepare("INSERT INTO mangata_blog (title, excerpt, content, image_url) VALUES (?, ?, ?, ?)");
        $stmt_bg->execute([$blog_title, $blog_excerpt, $blog_content, $blog_img]);
        $admin_msg = '<div class="success-message">خبر / وبلاگ جدید با عنوان «' . htmlspecialchars($blog_title) . '» با موفقیت در سیستم آنلاین منتشر شد.</div>';
    }
}

// Handle administrative blog edit/save
if (isset($_POST['edit_blog_web']) && is_admin()) {
    $blog_id = (int)($_POST['edit_blog_id'] ?? 0);
    $blog_title = trim($_POST['blog_title'] ?? '');
    $blog_excerpt = trim($_POST['blog_excerpt'] ?? '');
    $blog_content = trim($_POST['blog_content'] ?? '');
    $blog_img = trim($_POST['blog_img'] ?? '');

    if ($blog_id > 0 && !empty($blog_title)) {
        $stmt_ubg = $pdo->prepare("UPDATE mangata_blog SET title = ?, excerpt = ?, content = ?, image_url = ? WHERE id = ?");
        $stmt_ubg->execute([$blog_title, $blog_excerpt, $blog_content, $blog_img, $blog_id]);
        $admin_msg = '<div class="success-message">وبلاگ شماره ' . $blog_id . ' با موفقیت ویرایش و ذخیره شد.</div>';
    }
}

// Handle administrative blog deletion
if (isset($_POST['delete_blog_web']) && is_admin()) {
    $blog_id = (int)($_POST['delete_blog_id'] ?? 0);
    if ($blog_id > 0) {
        $pdo->prepare("DELETE FROM mangata_blog WHERE id = ?")->execute([$blog_id]);
        $admin_msg = '<div class="success-message" style="background:#b71c1c;">مطلب وبلاگی با موفقیت از پایگاه خبر حذف گردید.</div>';
    }
}

// Fetch all mangas with dynamic searching and filtering
$search = trim($_GET['search'] ?? '');
$genre = trim($_GET['genre'] ?? '');
$year = trim($_GET['year'] ?? '');
$character = trim($_GET['character'] ?? '');

$query = "SELECT * FROM mangata_mangas WHERE 1=1";
$sql_params = [];

if (!empty($search)) {
    $query .= " AND (title LIKE ? OR author LIKE ? OR description LIKE ?)";
    $sql_params[] = "%$search%";
    $sql_params[] = "%$search%";
    $sql_params[] = "%$search%";
}
if (!empty($genre)) {
    $query .= " AND genres LIKE ?";
    $sql_params[] = "%$genre%";
}
if (!empty($year)) {
    $query .= " AND release_year = ?";
    $sql_params[] = $year;
}
if (!empty($character)) {
    $query .= " AND main_characters LIKE ?";
    $sql_params[] = "%$character%";
}

$query .= " ORDER BY id DESC";
$stmt = $pdo->prepare($query);
$stmt->execute($sql_params);
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

<?php
$web_force_active = get_mangata_setting('force_update_web_active', '0') === '1';
$web_version = get_mangata_setting('force_update_web_version', '1');
$web_msg = get_mangata_setting('force_update_web_msg', 'به‌روزرسانی مهمی برای وب‌سایت مانگاتا در دیتابیس ثبت شده است. جهت ارتقاء ثبات سیستم، تمیزکننده عمیق کش و دارایی‌ها را اجرا کنید.');
$user_is_admin = is_admin() ? 'true' : 'false';
?>

<div id="web-force-update-modal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(10, 8, 15, 0.98); z-index: 999999; align-items: center; justify-content: center; direction: rtl; font-family: inherit; padding: 20px; box-sizing: border-box;">
    <div style="background: #1e1b24; border: 2px solid #ff5722; max-width: 600px; width: 100%; border-radius: 12px; padding: 30px; text-align: center; box-shadow: 0 10px 30px rgba(255, 87, 34, 0.2);">
        <div style="font-size: 60px; margin-bottom: 20px;">🔄</div>
        <h2 style="color: #ff5722; margin-top: 0; font-weight: bold; font-size: 22px;">بروزرسانی حافظه و دارایی‌های وب‌سایت</h2>
        <p style="color: #ccc; font-size: 14px; line-height: 1.8; margin-bottom: 30px;">
            <?php echo nl2br(htmlspecialchars($web_msg)); ?>
        </p>
        <button onclick="executeForcedWebCacheRefresh()" style="background: linear-gradient(135deg, #ff5722, #ff7597); color: #000; font-weight: bold; border: none; padding: 12px 24px; border-radius: 8px; font-size: 14px; cursor: pointer; transition: transform 0.2s;">
            🔄 بروزرسانی حافظه و بارگذاری دارایی‌های جدید
        </button>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const isForceActive = <?php echo $web_force_active ? 'true' : 'false'; ?>;
    const dbWebVersion = "<?php echo htmlspecialchars($web_version); ?>";
    const isAdmin = <?php echo $user_is_admin; ?>;
    
    const acknowledgedVersion = localStorage.getItem('acknowledged_web_version');
    
    if (isForceActive && !isAdmin && acknowledgedVersion !== dbWebVersion) {
        document.getElementById('web-force-update-modal').style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
});

function executeForcedWebCacheRefresh() {
    const reservedKeys = ['user_id', 'username', 'user_role', 'session_token'];
    const values = {};
    reservedKeys.forEach(k => {
         const val = localStorage.getItem(k);
         if (val) values[k] = val;
    });
    
    localStorage.clear();
    sessionStorage.clear();
    
    Object.keys(values).forEach(k => {
         localStorage.setItem(k, values[k]);
    });
    
    localStorage.setItem('acknowledged_web_version', "<?php echo htmlspecialchars($web_version); ?>");
    window.location.reload(true);
}
</script>

<header style="display:flex; flex-wrap:wrap; justify-content:space-between; align-items:center; gap:15px; padding:15px 5%;">
    <h1><a href="." style="color:#bb86fc; font-weight:bold; text-decoration:none;">MANGATA | مانگاتا 🎨</a></h1>
    <nav style="display: flex; flex-wrap:wrap; align-items: center; gap: 15px;">
        <a href="." style="color:#bb86fc; text-decoration:none; font-weight:bold; font-size:14px;">صفحه اصلی 🏠</a>
        <?php if (is_logged_in()): ?>
            <a href="?page=profile" style="color:#fff; text-decoration:none; font-weight:bold; font-size:14px;">پروفایل کاربری من 👤</a>
            <span style="color:#03dac6; font-size:12px; background:rgba(3,218,198,0.1); padding:4px 10px; border-radius:6px;">کاربر: <strong><?php echo htmlspecialchars($_SESSION['username']); ?></strong></span>
            <a href="?logout=1" class="btn btn-sm" style="background:#b71c1c; color:#fff; border-radius:6px; font-weight:bold; padding:6px 12px;">خروج 🚪</a>
        <?php else: ?>
            <span style="color:#ff7597; font-size:13px; font-weight:bold;">ورود الزامی است 🔒</span>
        <?php endif; ?>
    </nav>
</header>

<div class="container">
    
    <?php if (!is_logged_in()): ?>
        <!-- ==================== AUTH GATE ==================== -->
        <div style="max-width: 900px; margin: 40px auto; padding: 10px;">
            <div class="hero" style="margin-bottom: 30px; text-align:center;">
                <h2 style="color:#ff7597; font-size: 28px; margin:0 0 10px 0;">🔑 درگاه ورود و عضویت رسانه مانگاتا</h2>
                <p style="color:#ccc; font-size:14px; margin:0; line-height:1.7;">
                    برای مشاهده آخرین پروژه‌ها، ریدر آنلاین مانگا و ثبت‌نام در کادر فنی تیم، لطفاً ابتدا وارد حساب کاربری خود شوید یا حساب جدید بسازید.
                </p>
            </div>

            <?php echo $login_msg; ?>
            <?php echo $register_msg; ?>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 30px; margin-top:20px;">
                <!-- Register Card -->
                <div class="card" style="border: 1px solid rgba(255,117,151,0.15); background:#1a1a1a;">
                    <h3 style="color:#ff7597; margin-top:0; border-bottom:1px solid #333; padding-bottom:10px;">عضویت دائم (ثبت‌نام سریع)</h3>
                    <form action="" method="POST" style="display:flex; flex-direction:column; gap:15px; background:transparent; padding:0; border:none;">
                        <div>
                            <label style="display:block; color:#aaa; margin-bottom:6px; font-size:13px;">نام کاربری (لاتین):</label>
                            <input type="text" name="reg_username" required placeholder="مثال: amir12" style="width:100%; padding:8px; border:1px solid #333; border-radius:8px; background:#111; color:#fff;">
                        </div>
                        <div>
                            <label style="display:block; color:#aaa; margin-bottom:6px; font-size:13px;">ایمیل:</label>
                            <input type="email" name="reg_email" required placeholder="example@gmail.com" style="width:100%; padding:8px; border:1px solid #333; border-radius:8px; background:#111; color:#fff;">
                        </div>
                        <div>
                            <label style="display:block; color:#aaa; margin-bottom:6px; font-size:13px;">کلمه عبور:</label>
                            <input type="password" name="reg_password" required placeholder="کلمه عبور" style="width:100%; padding:8px; border:1px solid #333; border-radius:8px; background:#111; color:#fff;">
                        </div>
                        <button type="submit" name="web_register" class="btn" style="background:#ff7597; color:#000; font-weight:bold; margin-top:10px; width:100%;">ثبت حساب در دیتابیس 🚀</button>
                    </form>
                </div>

                <!-- Login Card -->
                <div class="card" style="border: 1px solid rgba(3,218,198,0.15); background:#1a1a1a;">
                    <h3 style="color:#03dac6; margin-top:0; border-bottom:1px solid #333; padding-bottom:10px;">ورود به حساب کاربری</h3>
                    <form action="" method="POST" style="display:flex; flex-direction:column; gap:15px; background:transparent; padding:0; border:none;">
                        <div>
                            <label style="display:block; color:#aaa; margin-bottom:6px; font-size:13px;">نام کاربری یا ایمیل:</label>
                            <input type="text" name="username" required placeholder="نام کاربری یا ایمیل" style="width:100%; padding:8px; border:1px solid #333; border-radius:8px; background:#111; color:#fff;">
                        </div>
                        <div>
                            <label style="display:block; color:#aaa; margin-bottom:6px; font-size:13px;">کلمه عبور:</label>
                            <input type="password" name="password" required placeholder="کلمه عبور حساب" style="width:100%; padding:8px; border:1px solid #333; border-radius:8px; background:#111; color:#fff;">
                        </div>
                        <button type="submit" name="web_login" class="btn" style="background:#03dac6; color:#000; font-weight:bold; margin-top:10px; width:100%;">ورود امن 🔐</button>
                    </form>
                </div>
            </div>
        </div>

    <?php elseif (isset($_GET['page']) && $_GET['page'] === 'profile'): ?>
        <!-- ==================== STUNNING PROFILE TAB ==================== -->
        <div style="max-width: 850px; margin: 40px auto; padding: 10px;">
            <div class="card" style="border: 1.5px solid rgba(187,134,252,0.3); background:#1e1b24;">
                <div style="display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid #333; padding-bottom:15px; margin-bottom:20px;">
                    <h2 style="color:#bb86fc; margin:0; font-size:22px;">👤 پیشخوان پروفایل کاربری من</h2>
                    <a href="." class="btn btn-sm" style="background:#333; color:#fff; border-radius:6px; font-weight:bold; text-decoration:none;">بازگشت به صفحه اصلی 🏠</a>
                </div>

                <?php echo $self_update_msg; ?>

                <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(280px, 1fr)); gap:25px; margin-top:15px;">
                    <!-- User Details + Avatar Card -->
                    <div style="background:#111; padding:20px; border-radius:8px; border:1px solid #222; text-align: center;">
                        <div style="position:relative; margin: 15px auto; width: fit-content;">
                            <img src="https://images.unsplash.com/photo-1542751371-adc38448a05e?w=150&auto=format&fit=crop" style="width: 90px; height: 90px; border-radius: 50%; border: 3px solid #bb86fc; box-shadow: 0 0 15px rgba(187,134,252,0.4);" />
                            <div style="background:#ff7597; color:white; font-size:10px; width:fit-content; margin:-12px auto 0 auto; padding:2px 10px; border-radius:20px; font-weight:bold; border: 1.5px solid #111;">PRO USER</div>
                        </div>
                        <h3 style="color:#03dac6; margin: 10px 0 5px 0; font-size:18px; font-weight:bold;"><?php echo htmlspecialchars($_SESSION['username']); ?></h3>
                        <p style="color:#888; font-size:12px; margin:0 0 15px 0;">شناسه عددی کاربر: #<?php echo (int)$_SESSION['user_id']; ?></p>
                        
                        <?php
                        // Fetch fresh email, wallet_balance and exam status from DB
                        $stmt_profile = $pdo->prepare("SELECT email, wallet_balance FROM mangata_users WHERE id = ?");
                        $stmt_profile->execute([$_SESSION['user_id']]);
                        $profile_fresh = $stmt_profile->fetch();
                        $self_email = $profile_fresh ? $profile_fresh['email'] : '';
                        $self_balance = $profile_fresh ? (int)$profile_fresh['wallet_balance'] : 2800;

                        $stmt_exam_status = $pdo->prepare("SELECT status, score FROM mangata_exams WHERE user_id = ? ORDER BY id DESC LIMIT 1");
                        $stmt_exam_status->execute([$_SESSION['user_id']]);
                        $exam_info = $stmt_exam_status->fetch();
                        ?>

                        <div style="text-align: right; background: rgba(255,255,255,0.02); padding: 12px; border-radius: 8px; border: 1px solid rgba(255,255,255,0.04); font-size: 13px; display: flex; flex-direction: column; gap: 8px; margin-bottom: 15px;">
                            <div style="display:flex; justify-content:space-between;"><span style="color:#888;">آدرس ایمیل:</span><span style="color:#fff; font-weight:bold;"><?php echo htmlspecialchars($self_email); ?></span></div>
                            <div style="display:flex; justify-content:space-between;"><span style="color:#888;">نقش سیستم:</span><span class="badge" style="background:#bb86fc; color:#000; font-size:11px; padding:2px 6px;"><?php echo htmlspecialchars($_SESSION['user_role']); ?></span></div>
                        </div>

                        <div style="margin-top:10px; text-align: right; background:#1c1724; padding:12px; border-radius:6px; border:1px solid rgba(187,134,252,0.15);">
                            <h4 style="color:#ff7597; margin:0 0 8px 0; font-size:13px; font-weight:bold;">وضعیت استخدام همکاران:</h4>
                            <?php if ($exam_info): ?>
                                <span style="font-size:13px; font-weight:bold; color:#03dac6;">
                                    <?php echo htmlspecialchars($exam_info['status']); ?> 
                                    <?php if ($exam_info['score'] !== null) { echo " (نمره آزمون: " . $exam_info['score'] . ")"; } ?>
                                </span>
                            <?php else: ?>
                                <span style="font-size:12px; color:#777;">شما تاکنون هیچ سند ارزیابی آپلود نکرده‌اید.</span>
                            <?php endif; ?>
                        </div>

                        <?php if ($_SESSION['user_role'] === 'administrator' || strpos($_SESSION['user_role'], 'staff_') === 0): ?>
                            <div style="margin-top:15px; text-align:center;">
                                <a href="index.php#admin-desk" class="btn btn-sm" style="background:#03dac6; color:#000; font-weight:bold; display:block; padding:10px; border-radius:8px; text-decoration:none;">صفحه ادمین / وظایف همکاران ⚡</a>
                            </div>
                        <?php endif; ?>
                    </div>

                    <!-- Genuine Wallet System Card -->
                    <div style="background:#111; padding:20px; border-radius:8px; border:1px solid #222;">
                        <h3 style="color:#f59e0b; margin-top:0; font-size:16px; border-bottom:1px solid #222; padding-bottom:8px; font-weight:bold; display:flex; align-items:center; gap:6px;">🪙 موجودی و شارژ کیف پول</h3>
                        
                        <!-- Rich gradient card based on the premium application designs -->
                        <div style="background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%); padding: 20px; border-radius: 12px; margin: 15px 0; text-align: center; color: white; box-shadow: 0 4px 15px rgba(217,119,6,0.25);">
                            <div style="font-size: 11px; margin-bottom: 6px; font-weight: bold; opacity: 0.9; text-transform: uppercase; letter-spacing: 0.5px;">کیف پول دیجیتال مانگاتا</div>
                            <div style="font-size: 30px; font-weight: 900; letter-spacing: 0.5px; display: flex; align-items: center; justify-content: center; gap: 8px;">
                                <span style="font-family: inherit;"><?php echo number_format($self_balance); ?></span>
                                <span style="font-size: 20px;">تومان</span>
                            </div>
                        </div>

                        <p style="color:#888; font-size:11px; line-height:1.6; margin-bottom:15px;">با کیف پول شارژ شده می‌توانید از چپترهای ویژه، امکانات متمایز و کادوهای اهدایی داخل سایت و اپلیکیشن استفاده کنید.</p>
                        
                        <!-- Recharge form -->
                        <form action="" method="POST" style="background: rgba(255, 255, 255, 0.03); padding: 12px; border-radius: 8px; border: 1px solid rgba(255,255,255,0.05); display: flex; flex-direction:column; gap: 10px;">
                            <div style="display:flex; justify-content:space-between; align-items:center;">
                                <span style="font-size:12px; font-weight:bold; color:#ccc;">مبلغ تراکنش شارژ:</span>
                                <strong style="font-size:13px; color:#ffd54f;">۱۵,۰۰۰ تومان</strong>
                            </div>
                            <input type="hidden" name="charge_amount" value="15000">
                            <button type="submit" name="quick_charge_web" class="btn" style="background:linear-gradient(135deg, #ea580c, #f59e0b); color:white; font-weight:bold; width:100%; font-size:12px; border-radius:6px; cursor:pointer;" class="btn-charge">💡 شارژ آنی محصول (۱۵,۰۰۰ تومان)</button>
                        </form>
                    </div>

                    <!-- Change password form -->
                    <div style="background:#111; padding:20px; border-radius:8px; border:1px solid #222;">
                        <h3 style="color:#ff7597; margin-top:0; font-size:16px; border-bottom:1px solid #222; padding-bottom:8px;">ویرایش مشخصات / تغییر رمز عبور</h3>
                        <form action="" method="POST" style="display:flex; flex-direction:column; gap:15px; background:transparent; padding:0; border:none;">
                            <div>
                                <label style="display:block; color:#aaa; margin-bottom:5px; font-size:12px;">ایمیل:</label>
                                <input type="email" name="own_email" required value="<?php echo htmlspecialchars($self_email); ?>" style="width:100%; padding:8px; border:1px solid #333; background:#222; color:#fff; border-radius:6px;">
                            </div>
                            <div>
                                <label style="display:block; color:#aaa; margin-bottom:5px; font-size:12px;">کلمه عبور جدید:</label>
                                <input type="password" name="own_password" placeholder="تنها در صورت تغییر پر کنید" style="width:100%; padding:8px; border:1px solid #333; background:#222; color:#fff; border-radius:6px;">
                            </div>
                            <button type="submit" name="update_own_profile" class="btn" style="background:#bb86fc; color:#000; font-weight:bold; width:100%;">ثبت بروزرسانی حساب 💾</button>
                        </form>
                    </div>

                    <!-- Bookmarks Section (Full width of container) -->
                    <div style="background:#111; padding:25px; border-radius:12px; border:1.5px solid rgba(124,77,255,0.25); grid-column: 1 / -1;">
                        <h3 style="color:#bb86fc; margin-top:0; font-size:17px; border-bottom:1px solid #222; padding-bottom:10px; font-weight:bold; display:flex; align-items:center; gap:8px;">📌 نشانک‌ها و مانهواهای محبوب من (Synced Perfectly)</h3>
                        <p style="color:#888; font-size:12px; margin-bottom:20px;">آثار نشانک‌گذاری شده شما همزمان در اپلیکیشن اندروید و وب‌سایت سینک هستند. می‌توانید وضعیت‌های هر کدام را به دلخواه مدیریت کنید.</p>
                        
                        <?php
                        $stmt_b = $pdo->prepare("
                            SELECT b.status, m.id as manga_id, m.title, m.cover_image, m.description
                            FROM mangata_bookmarks b 
                            JOIN mangata_mangas m ON b.manga_id = m.id 
                            WHERE b.user_id = ?
                            ORDER BY b.id DESC
                        ");
                        $stmt_b->execute([$_SESSION['user_id']]);
                        $user_bookmarks = $stmt_b->fetchAll();

                        if (!empty($user_bookmarks)):
                        ?>
                            <div style="display:grid; grid-template-columns:repeat(auto-fill, minmax(240px, 1fr)); gap:15px;">
                                <?php foreach ($user_bookmarks as $ub): ?>
                                    <div style="background:rgba(30, 27, 36, 0.65); border:1px solid rgba(255,255,255,0.06); border-radius:12px; padding:15px; display:flex; flex-direction:column; justify-content:space-between; gap:12px; transition: border-color 0.2s;">
                                        <div style="display:flex; gap:12px; align-items:flex-start;">
                                            <img src="<?php echo htmlspecialchars($ub['cover_image'] ?: 'https://placehold.co/100x150'); ?>" style="width:50px; height:70px; object-fit:cover; border-radius:6px; border:1px solid #444;" />
                                            <div style="flex:1;">
                                                <a href="details.php?id=<?php echo $ub['manga_id']; ?>" style="text-decoration:none;">
                                                    <h4 style="font-size:13px; font-weight:bold; color:white; line-height:1.4; margin:0 0 4px 0;"><?php echo htmlspecialchars($ub['title']); ?> 👁️</h4>
                                                </a>
                                                <div style="font-size:11px; margin-top:4px;">
                                                    <span style="display:inline-block; width:6px; height:6px; background:#03dac6; border-radius:50%; margin-left:4px;"></span>
                                                    <strong style="color:#03dac6;"><?php echo htmlspecialchars($ub['status'] === 'Reading' ? 'در حال خوندن 🟢' : ($ub['status'] === 'Completed' ? 'تموم شده 🏆' : 'علاقه‌مندی ⭐')); ?></strong>
                                                </div>
                                            </div>
                                        </div>
                                        <div style="display:flex; gap:8px; width:100%;">
                                            <form action="" method="POST" style="margin:0; padding:0; background:transparent; border:none; flex:1;">
                                                <input type="hidden" name="cycle_manga_id" value="<?php echo $ub['manga_id']; ?>">
                                                <input type="hidden" name="current_status" value="<?php echo htmlspecialchars($ub['status']); ?>">
                                                <button type="submit" name="cycle_status_web" class="btn btn-sm" style="font-size:10px; padding:6px; background:#f59e0b; color:black; font-weight:bold; border-radius:6px; border:none; width:100%; cursor:pointer;">تغییر وضعیت 🔄</button>
                                            </form>
                                            <form action="" method="POST" style="margin:0; padding:0; background:transparent; border:none; flex:1;">
                                                <input type="hidden" name="remove_manga_id" value="<?php echo $ub['manga_id']; ?>">
                                                <button type="submit" name="remove_bookmark_web" class="btn btn-sm" style="font-size:10px; padding:6px; background:#b71c1c; color:white; font-weight:bold; border-radius:6px; border:none; width:100%; cursor:pointer;">حذف نشانک 🗑️</button>
                                            </form>
                                        </div>
                                    </div>
                                <?php endforeach; ?>
                            </div>
                        <?php else: ?>
                            <div style="text-align:center; padding:30px; border:1px dashed rgba(255,255,255,0.1); bor    <?php else: ?>
        <!-- ==================== MAIN HOME RENDER ==================== -->
        
        <!-- 1. LIVE HERO HIGHLIGHT SLIDER (Automated & Highly Polished) -->
        <?php
        $slider_mangas = array_slice($mangas, 0, 3);
        if (!empty($slider_mangas)):
        ?>
            <div style="position: relative; overflow: hidden; border-radius: 16px; min-height: 380px; background: #121016; border: 1px solid rgba(124,77,255,0.2); margin-bottom: 35px; box-shadow: 0 10px 40px rgba(0,0,0,0.6);" id="mangaHeroSlider">
                <?php foreach ($slider_mangas as $idx => $sm): ?>
                    <div class="hero-slide" data-index="<?php echo $idx; ?>" style="display: <?php echo $idx === 0 ? 'flex' : 'none'; ?>; flex-direction: column; justify-content: flex-end; position: absolute; top:0; left:0; width:100%; height:100%; box-sizing:border-box;">
                        <!-- Blur cover backdrop -->
                        <div style="position: absolute; top:0; left:0; width:100%; height:100%; background: url('<?php echo htmlspecialchars($sm['cover_image'] ?: 'https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800'); ?>') center center / cover no-repeat; opacity: 0.18; filter: blur(20px); pointer-events:none; z-index:0;"></div>
                        <!-- Gradient overlay -->
                        <div style="position: absolute; top:0; left:0; width:100%; height:100%; background: linear-gradient(to top, rgba(12,10,15,0.95) 0%, rgba(12,10,15,0.4) 60%, rgba(12,10,15,0.1) 100%); z-index:1; pointer-events:none;"></div>
                        
                        <!-- Slide Content wrapper -->
                        <div style="position: relative; z-index: 2; padding: 40px 5%; display: flex; align-items: flex-end; gap: 24px; flex-wrap: wrap;">
                            <img src="<?php echo htmlspecialchars($sm['cover_image'] ?: 'https://placehold.co/150x220'); ?>" style="width: 120px; height: 180px; object-fit: cover; border-radius: 12px; border: 2px solid #ff7597; box-shadow: 0 0 20px rgba(255,117,151,0.3);" />
                            <div style="flex: 1; min-width: 260px;">
                                <div style="display: flex; gap: 10px; margin-bottom: 12px;">
                                    <span class="badge" style="background:#03dac6; color:#000;">🔥 برگزیده سردبیر</span>
                                    <span class="badge" style="background:#7c4dff; color:#fff;">📅 سال: <?php echo htmlspecialchars($sm['release_year'] ?? '2026'); ?></span>
                                </div>
                                <h2 style="color:#fff; font-size: 28px; font-weight: 900; margin: 0 0 10px 0; text-shadow: 0 2px 10px rgba(0,0,0,0.5);"><?php echo htmlspecialchars($sm['title']); ?></h2>
                                <p style="color:#bbb; font-size: 14px; line-height: 1.7; max-width: 700px; margin: 0 0 20px 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;"><?php echo htmlspecialchars($sm['description']); ?></p>
                                <a href="details.php?id=<?php echo $sm['id']; ?>" class="btn" style="padding: 10px 24px; border-radius: 8px; font-weight: bold;">شروع خوانش مانهوا 📖</a>
                            </div>
                        </div>
                    </div>
                <?php endforeach; ?>
                
                <!-- Dot navigation pointers -->
                <div style="position: absolute; bottom: 20px; left: 20px; z-index: 5; display: flex; gap: 8px;">
                    <?php foreach ($slider_mangas as $idx => $sm): ?>
                        <span class="slider-dot" onclick="setMangaSlide(<?php echo $idx; ?>)" style="width: 10px; height: 10px; border-radius: 50%; background: <?php echo $idx === 0 ? '#ff7597' : 'rgba(255,255,255,0.3)'; ?>; cursor: pointer; transition: background 0.3s;"></span>
                    <?php endforeach; ?>
                </div>
            </div>
            
            <script>
            let currentSlideIndex = 0;
            const totalSlides = <?php echo count($slider_mangas); ?>;
            function setMangaSlide(idx) {
                currentSlideIndex = idx;
                document.querySelectorAll('.hero-slide').forEach((slide, i) => {
                    slide.style.display = (i === idx) ? 'flex' : 'none';
                });
                document.querySelectorAll('.slider-dot').forEach((dot, i) => {
                    dot.style.background = (i === idx) ? '#ff7597' : 'rgba(255,255,255,0.3)';
                });
            }
            setInterval(() => {
                if(totalSlides > 1) {
                    currentSlideIndex = (currentSlideIndex + 1) % totalSlides;
                    setMangaSlide(currentSlideIndex);
                }
            }, 5500);
            </script>
        <?php endif; ?>

        <?php echo $login_msg; ?>
        <?php echo $upload_msg; ?>
        <?php echo $admin_msg; ?>

        <!-- 2. HORIZONTAL NEWEST WORKS CAROUSEL (With arrows < & >) -->
        <div style="margin-bottom: 45px; position: relative;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 18px; border-bottom: 2px solid rgba(255,117,151,0.25); padding-bottom: 10px;">
                <h2 style="color:#ff7597; margin:0; font-size: 20px; font-weight: 900; display:flex; align-items:center; gap:8px;">🔥 آخرین کارها که روی سایت میرن (کارهای جدید)</h2>
                <div style="display: flex; gap: 8px;">
                    <button onclick="scrollCarousel(-1)" class="btn btn-sm" style="background: rgba(124,77,255,0.2); border: 1px solid #7c4dff; padding: 6px 12px; color: #7c4dff; border-radius: 6px; cursor: pointer; height: 32px; display: flex; align-items: center; justify-content: center; font-weight:bold;">◀</button>
                    <button onclick="scrollCarousel(1)" class="btn btn-sm" style="background: rgba(124,77,255,0.2); border: 1px solid #7c4dff; padding: 6px 12px; color: #7c4dff; border-radius: 6px; cursor: pointer; height: 32px; display: flex; align-items: center; justify-content: center; font-weight:bold;">▶</button>
                </div>
            </div>
            
            <div id="mangaCarousel" style="display: flex; gap: 20px; overflow-x: auto; scroll-behavior: smooth; padding: 10px 0; scrollbar-width: none; -ms-overflow-style: none;">
                <?php if (!empty($mangas)): ?>
                    <?php foreach ($mangas as $cm): ?>
                        <div style="flex: 0 0 240px; background: rgba(20,18,24,0.75); border-radius: 12px; border: 1px solid rgba(124,77,255,0.12); padding: 14px; box-sizing: border-box; display: flex; flex-direction: column; justify-content: space-between; transition: transform 0.3s, border-color 0.3s;" class="carousel-card">
                            <div>
                                <img src="<?php echo htmlspecialchars($cm['cover_image'] ?: 'https://placehold.co/240x330'); ?>" style="width: 100%; height: 260px; object-fit: cover; border-radius: 8px; border: 1px solid rgba(255,255,255,0.06); margin-bottom: 12px;" />
                                <h3 style="color:#fff; font-size: 15px; font-weight: 800; margin: 0 0 6px 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"><?php echo htmlspecialchars($cm['title']); ?></h3>
                                <p style="color:#888; font-size: 11px; margin: 0 0 10px 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"><?php echo htmlspecialchars($cm['genres'] ?? 'بدون ژانر'); ?></p>
                            </div>
                            <div style="margin-top: 10px; display: flex; flex-direction: column; gap: 6px;">
                                <a href="details.php?id=<?php echo $cm['id']; ?>" class="btn btn-sm" style="background:#7c4dff; width:100%; text-align:center; padding: 6px; border-radius:6px; font-size: 11px; font-weight:bold;">مشاهده جزئیات کامل 👁️</a>
                            </div>
                        </div>
                    <?php endforeach; ?>
                <?php else: ?>
                    <p style="color:#666; font-size:13px; padding: 10px;">هیچ مانهوایی ثبت نشده است.</p>
                <?php endif; ?>
            </div>
            
            <script>
            function scrollCarousel(dir) {
                const carousel = document.getElementById('mangaCarousel');
                const cardWidth = 260; 
                carousel.scrollLeft += (dir * cardWidth);
            }
            </script>
        </div>

        <!-- 3. DISCOUNTS / PREMIUM PROMO CAMPAIGN (تخفیفات اعتبار سکه) -->
        <?php
        $promo_text = get_mangata_setting('promo_discount_text', '٪۳۰ تخفیف داغ بهارانه روی خرید بسته‌‌های اعتباری و طلایی ترجمه؛ تنها با کدرهگیری فعال: MANGATA30');
        $promo_expiry = get_mangata_setting('promo_discount_expiry', '۱۴۰۵/۰۴/۱۵');
        ?>
        <div class="card" style="background: linear-gradient(135deg, rgba(245,158,11,0.15) 0%, rgba(217,119,6,0.05) 100%); border: 1px solid rgba(245,158,11,0.3); border-radius: 16px; padding: 22px; margin-bottom: 45px; display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 20px;">
            <div style="flex: 2; min-width: 280px;">
                <span style="background: #f59e0b; color: #000; font-size: 10px; font-weight: 900; padding: 3px 10px; border-radius: 30px; text-transform: uppercase;">🔥 جشنواره طلایی تخفیفات</span>
                <h3 style="color:#ffd54f; margin: 10px 0 8px 0; font-size: 18px; font-weight: 900;"><?php echo htmlspecialchars($promo_text); ?></h3>
                <p style="color:#aaa; font-size: 12px; margin:0;">مهلت استفاده از تخفیف کیف پول و خرید بسته‌ها: <strong style="color:#ff7597;"><?php echo htmlspecialchars($promo_expiry); ?></strong></p>
            </div>
            <div style="flex: 1; min-width: 180px; text-align: center;">
                <div style="border: 2px dashed rgba(245,158,11,0.4); padding: 10px; border-radius: 8px; font-size: 15px; font-weight: bold; color: #ffd54f; background: rgba(0,0,0,0.2); font-family: monospace; letter-spacing: 1.5px; margin-bottom: 12px;">MANGATA30</div>
                <a href="?page=profile" class="btn btn-sm" style="background:#f59e0b; color:#000; font-weight:900; width:100%;">ثبت اعتبار شارژ هدیه 🎁</a>
            </div>
        </div>

        <!-- 4. LIVE BLOG / NEWS SECTION (وبلاگ یا اخبار) -->
        <div style="margin-bottom: 45px;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 18px; border-bottom: 2px solid rgba(3,218,198,0.25); padding-bottom: 10px;">
                <h2 style="color:#03dac6; margin:0; font-size: 20px; font-weight: 900; display:flex; align-items:center; gap:8px;">📰 وبلاگ و آخرین اخبار دنیای مانهوا</h2>
            </div>
            
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 24px;">
                <?php
                $stmt_blog = $pdo->query("SELECT * FROM mangata_blog ORDER BY id DESC LIMIT 3");
                $blogs = $stmt_blog->fetchAll();
                foreach ($blogs as $bg):
                ?>
                    <div class="card" style="background: rgba(26,22,34,0.6); border: 1px solid rgba(187,134,252,0.12); border-radius: 12px; overflow: hidden; padding: 0; display: flex; flex-direction: column; justify-content: space-between;">
                        <div>
                            <img src="<?php echo htmlspecialchars($bg['image_url'] ?: 'https://picsum.photos/600/400'); ?>" style="width: 100%; height: 160px; object-fit: cover; border-bottom: 1px solid rgba(255,255,255,0.06);" />
                            <div style="padding: 18px;">
                                <span style="font-size: 11px; color: #ff7597; font-weight: bold;"><?php echo htmlspecialchars($bg['created_at']); ?></span>
                                <h3 style="color:#fff; font-size: 16px; font-weight: 800; margin: 6px 0 10px 0; line-height: 1.4;"><?php echo htmlspecialchars($bg['title']); ?></h3>
                                <p style="color:#aaa; font-size: 12px; line-height: 1.6; margin: 0;"><?php echo htmlspecialchars($bg['excerpt']); ?></p>
                            </div>
                        </div>
                        <div style="padding: 0 18px 18px 18px;">
                            <button onclick="readFullBlog(<?php echo $bg['id']; ?>, '<?php echo addslashes(htmlspecialchars($bg['title'])); ?>', '<?php echo addslashes(htmlspecialchars($bg['content'])); ?>')" class="btn btn-sm" style="background:rgba(3,218,198,0.15); border:1px solid #03dac6; color:#03dac6; width:100%; font-weight:bold;">مشاهده متن کامل خبر 👁️</button>
                        </div>
                    </div>
                <?php endforeach; ?>
            </div>
            
            <!-- Blog Details Modal -->
            <div id="blogModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(10, 8, 15, 0.95); z-index: 100000; align-items: center; justify-content: center; padding: 20px; box-sizing: border-box;">
                <div style="background: #1e1b24; border: 1.5px solid #03dac6; max-width: 750px; width: 100%; border-radius: 14px; padding: 25px; box-shadow: 0 10px 30px rgba(3,218,198,0.25);">
                    <h3 id="blogModalTitle" style="color: #03dac6; margin-top: 0; font-weight: 950; font-size: 20px; border-bottom: 1px solid #333; padding-bottom: 12px;">عنوان وبلاگ</h3>
                    <div id="blogModalContent" style="color: #fff; font-size: 13.5px; line-height: 1.8; margin-bottom: 25px; max-height: 350px; overflow-y: auto; text-align: justify; padding-left: 10px;">محتوا</div>
                    <div style="text-align: left;">
                        <button onclick="closeBlogModal()" style="background: #e53935; color: white; border: none; padding: 8px 20px; border-radius: 6px; font-size: 12px; cursor: pointer; font-weight: bold;">بستن 닫기</button>
                    </div>
                </div>
            </div>
            
            <script>
            function readFullBlog(id, title, text) {
                document.getElementById('blogModalTitle').innerText = title;
                document.getElementById('blogModalContent').innerHTML = text.replace(/\n/g, '<br>');
                document.getElementById('blogModal').style.display = 'flex';
                document.body.style.overflow = 'hidden';
            }
            function closeBlogModal() {
                document.getElementById('blogModal').style.display = 'none';
                document.body.style.overflow = 'auto';
            }
            </script>
        </div>

        <!-- 5. RECOMMENDED / SUGGESTED WORKS (کارهای پیشنهادی) -->
        <div style="margin-bottom: 45px;">
            <div style="border-bottom: 2px solid rgba(187,134,252,0.25); padding-bottom: 10px; margin-bottom: 18px;">
                <h2 style="color:#bb86fc; margin:0; font-size: 20px; font-weight: 900; display:flex; align-items:center; gap:8px;">💫 کارهای پیشنهادی و اختصاصی تیم مدیریت</h2>
            </div>
            
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 24px;">
                <?php
                $suggested_mangas = array_slice($mangas, 0, 4);
                if (!empty($suggested_mangas)):
                    foreach ($suggested_mangas as $sm):
                ?>
                        <div style="background: rgba(30,27,36,0.65); border: 1px solid rgba(124,77,255,0.12); border-radius: 12px; padding: 15px; display:flex; gap:15px; align-items:flex-start; position: relative;">
                            <span style="position: absolute; top: 10px; right: 10px; background: #e0a82e; color: black; font-size: 9px; font-weight: bold; padding: 2px 6px; border-radius: 4px;">⭐ پیشنهاد دبیر</span>
                            <img src="<?php echo htmlspecialchars($sm['cover_image'] ?: 'https://placehold.co/120x170'); ?>" style="width: 80px; height: 110px; object-fit: cover; border-radius: 8px; border: 1px solid #444;" />
                            <div style="flex:1;">
                                <h3 style="color:#fff; font-size: 15px; font-weight: 800; margin: 0 0 5px 0;"><?php echo htmlspecialchars($sm['title']); ?></h3>
                                <p style="color:#aaa; font-size: 11px; margin: 0 0 12px 0; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;"><?php echo htmlspecialchars($sm['description']); ?></p>
                                <a href="details.php?id=<?php echo $sm['id']; ?>" style="color:#bb86fc; font-size:12px; font-weight:bold; text-decoration:none;">همین حالا بخوانید ◀</a>
                            </div>
                        </div>
                <?php 
                    endforeach;
                else:
                ?>
                    <p style="color:#666; font-size:12px;">کارت ویژه‌ای آماده نشده است.</p>
                <?php endif; ?>
            </div>
        </div>

        <!-- 6. TOP WORKS / BEST USERS OF THE MONTH (برترین کارها و بهترین اعضای ماه) -->
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 24px; margin-bottom: 45px;">
            <!-- Column 1: Best Staff of the month -->
            <div class="card" style="border-right: 4px solid #03dac6; background: rgba(18,16,22,0.8);">
                <h3 style="color:#03dac6; font-weight:900; margin-top:0; font-size: 17px; margin-bottom:15px;">👤 کادر فنی و همکاران برتر ماه در رسانه</h3>
                
                <div style="display:flex; flex-direction:column; gap:12px;">
                    <div style="display:flex; align-items:center; gap:12px; background:rgba(255,255,255,0.03); padding:10px; border-radius:8px;">
                        <div style="background:#ff7597; color:black; width:34px; height:34px; border-radius:50%; display:flex; align-items:center; justify-content:center; font-weight:bold; font-size:11px;">مترجم</div>
                        <div>
                            <strong style="color:white; font-size:13px; display:block;">مترجم محبوب: amirreza</strong>
                            <span style="color:#888; font-size:11px;">سرعت عمل عالی و ترجمه روان سولو لولینگ</span>
                        </div>
                    </div>
                    <div style="display:flex; align-items:center; gap:12px; background:rgba(255,255,255,0.03); padding:10px; border-radius:8px;">
                        <div style="background:#03dac6; color:black; width:34px; height:34px; border-radius:50%; display:flex; align-items:center; justify-content:center; font-weight:bold; font-size:11px;">طراح</div>
                        <div>
                            <strong style="color:white; font-size:13px; display:block;">تایپ‌ستر برتر: rezashaw</strong>
                            <span style="color:#888; font-size:11px;">کلینر و طراح بازسازی بسیار تمیز مانهوای طلایی</span>
                        </div>
                    </div>
                    <div style="display:flex; align-items:center; gap:12px; background:rgba(255,255,255,0.03); padding:10px; border-radius:8px;">
                        <div style="background:#bb86fc; color:black; width:34px; height:34px; border-radius:50%; display:flex; align-items:center; justify-content:center; font-weight:bold; font-size:11px;">کادر</div>
                        <div>
                            <strong style="color:white; font-size:13px; display:block;">کلینر برتر: mangataclean</strong>
                            <span style="color:#888; font-size:11px;">حذف دقیق حباب‌ها و همگام‌سازی آنی فصول</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Column 2: Best Works ranking -->
            <div class="card" style="border-right: 4px solid #ff7597; background: rgba(18,16,22,0.8);">
                <h3 style="color:#ff7597; font-weight:900; margin-top:0; font-size: 17px; margin-bottom:15px;">🏆 برترین مانهواهای ماه (بیشترین رضایت کاربری)</h3>
                
                <div style="display:flex; flex-direction:column; gap:10px;">
                    <?php
                    $top_ranked = array_slice($mangas, 0, 3);
                    foreach ($top_ranked as $rank => $tr):
                    ?>
                        <div style="display:flex; align-items:center; gap:12px; background:rgba(255,255,255,0.02); padding:10px; border-radius:8px; border:1px solid rgba(255,255,255,0.03);">
                            <span style="font-size: 18px; font-weight:900; color:#ff7597;">#<?php echo $rank + 1; ?></span>
                            <img src="<?php echo htmlspecialchars($tr['cover_image'] ?: 'https://placehold.co/60x80'); ?>" style="width: 40px; height: 50px; object-fit: cover; border-radius: 4px;" />
                            <div style="flex:1;">
                                <strong style="color:white; font-size:13px; display:block;"><?php echo htmlspecialchars($tr['title']); ?></strong>
                                <span style="color:#888; font-size:11px;">ژانر: <?php echo htmlspecialchars($tr['genres'] ?? 'فانتزی'); ?></span>
                            </div>
                        </div>
                    <?php endforeach; ?>
                </div>
            </div>
        </div>

        <!-- 7. REAL INTERACTIVE REVIEWS & COMMENTS CENTER (نقدها و تحلیل‌های زنده) -->
        <div class="card" style="background: rgba(18,16,22,0.7); border: 1.5px solid rgba(3,218,198,0.25); border-radius: 16px; padding: 24px; margin-bottom: 45px;">
            <h2 style="color:#03dac6; margin-top:0; font-size: 20px; font-weight: 950; display:flex; align-items:center; gap:8px; border-bottom: 1px solid rgba(255,255,255,0.06); padding-bottom:12px; margin-bottom: 20px;">💬 نقدها، دیدگاه‌ها و تحلیل‌های زنده کاربران</h2>
            
            <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 30px;" class="panel-flex">
                <!-- Fetch and Display reviews list -->
                <div>
                    <h3 style="color:#ff7597; font-size: 15px; font-weight: 800; margin-top:0; margin-bottom:15px;">کریدور نقد داستان و کیفیت تیم ترجمه:</h3>
                    
                    <div style="display:flex; flex-direction:column; gap:15px; max-height: 380px; overflow-y:auto; padding-left:10px;">
                        <?php
                        // Fetch real dynamically synced reviews
                        $stmt_all_rev = $pdo->query("
                            SELECT r.*, u.username, m.title as manga_title 
                            FROM mangata_reviews r 
                            JOIN mangata_users u ON r.user_id = u.id 
                            JOIN mangata_mangas m ON r.manga_id = m.id 
                            ORDER BY r.id DESC
                        ");
                        $reviews = $stmt_all_rev->fetchAll();
                        
                        if (!empty($reviews)):
                            foreach ($reviews as $rv):
                        ?>
                                <div style="background:rgba(255,255,255,0.02); padding:15px; border-radius:10px; border:1px solid rgba(255,255,255,0.04);">
                                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                                        <strong style="color:#bb86fc; font-size:13px;">👤 @<?php echo htmlspecialchars($rv['username']); ?></strong>
                                        <span style="color:#ffd54f; font-size:12px; font-weight:bold;"><?php echo str_repeat('⭐', (int)$rv['rating']); ?></span>
                                    </div>
                                    <div style="font-size:11px; color:#aaa; margin-bottom:8px;">درباره اثر: <strong style="color:#ff7597;"><?php echo htmlspecialchars($rv['manga_title']); ?></strong></div>
                                    <p style="color:#efefef; font-size:12px; line-height:1.6; margin:0;"><?php echo htmlspecialchars($rv['review_text']); ?></p>
                                </div>
                        <?php 
                            endforeach;
                        else:
                        ?>
                            <p style="color:#666; font-size:12px;">تاکنون نقدی در دیتابیس تحلیل ثبت نشده است.</p>
                        <?php endif; ?>
                    </div>
                </div>
                
                <!-- Submit custom critique review form -->
                <div style="background:#131118; padding:20px; border-radius:12px; border:1px solid rgba(255,255,255,0.04);">
                    <h3 style="color:#03dac6; font-size: 15px; font-weight: 800; margin-top:0; margin-bottom:15px;">➕ ثبت نقد و دیدگاه و نمره‌دهی زنده</h3>
                    <form action="" method="POST" style="background:transparent; padding:0; border:none; display:flex; flex-direction:column; gap:12px; margin:0;">
                        <div>
                            <label style="font-size:12px; color:#aaa; display:block; margin-bottom:5px;">انتخاب اثر ثبت شده:</label>
                            <select name="review_manga_id" required style="width:100%; padding:8px; border-radius:6px; background:#222; color:#fff; font-size:12px; margin:0; border:1px solid #333;">
                                <?php foreach ($mangas as $man_rev): ?>
                                    <option value="<?php echo $man_rev['id']; ?>"><?php echo htmlspecialchars($man_rev['title']); ?></option>
                                <?php endforeach; ?>
                            </select>
                        </div>
                        <div>
                            <label style="font-size:12px; color:#aaa; display:block; margin-bottom:5px;">رتبه رضایت شما:</label>
                            <select name="review_rating" style="width:100%; padding:8px; border-radius:6px; background:#222; color:#fff; font-size:12px; margin:0; border:1px solid #333;">
                                <option value="5">⭐⭐⭐⭐⭐ بی نظیر عالی</option>
                                <option value="4">⭐⭐⭐⭐ خوب و خواندنی</option>
                                <option value="3">⭐⭐⭐ متوسط</option>
                                <option value="2">⭐⭐ ضعیف</option>
                                <option value="1">⭐ بسیار بد</option>
                            </select>
                        </div>
                        <div>
                            <label style="font-size:12px; color:#aaa; display:block; margin-bottom:5px;">متن دیدگاه یا نقد تحلیلی:</label>
                            <textarea name="review_text" required placeholder="توضیحات معرفی و نقد شما درباره کار..." style="width:100%; height:80px; padding:8px; border-radius:6px; background:#222; color:#fff; font-size:12px; margin:0; border:1px solid #333; font-family:inherit;"></textarea>
                        </div>
                        <button type="submit" name="submit_manga_review" class="btn btn-sm" style="background:#03dac6; color:#000; font-weight:bold; width:100%; margin-top:5px; padding:10px;">ثبت زنده دیدگاه در پورتال 🚀</button>
                    </form>
                </div>
            </div>
        </div>

        <!-- 8. RECRUITMENT DESK (محل عضویت داوطلبین همکار) -->
        <div id="recruitment" class="card" style="border:1.5px solid rgba(255,117,151,0.3); background:#1a191f; border-radius:16px; padding:30px; margin-bottom:45px;">
            <div style="display: flex; gap: 20px; align-items: center; flex-wrap: wrap; margin-bottom: 20px; border-bottom: 1px solid rgba(255,255,255,0.06); padding-bottom: 15px;">
                <div style="font-size:40px;">🤝</div>
                <div>
                    <h2 style="color:#ff7597; margin:0; font-size:22px; font-weight:950;">استخدام کادر فنی مانهواخوان مانگاتا (MANGATA)</h2>
                    <p style="color:#aaa; font-size:12.5px; margin:4px 0 0 0;">آزمون‌های بررسی زنده آنلاین؛ ارتقاء آنی نقش‌های دسترسی و تخصیص پروژه</p>
                </div>
            </div>
            
            <p style="color:#ccc; font-size:13.5px; line-height:1.7; margin-bottom:25px;">ما در مانگاتا همواره به دنبال جذب استعدادهای عالی کشور برای ترجمه و ادیت مانهواها به صورت همگام با دیتابیس واقعی هستیم. پس از پر کردن فرم استخدامی زیر، سیستم یک حساب کاربری برای شما باز کرده و کادر مرکزی پاسخ امتحان را تصحیح و نقش همکاری شما را به صورت زنده ارتقا خواهد داد.</p>
            
            <form action="#recruitment" method="POST" enctype="multipart/form-data" style="background:#111; padding: 25px; border-radius:12px; border:1px solid rgba(255,255,255,0.04); display: flex; flex-direction:column; gap: 15px;">
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap:15px;">
                    <div>
                        <label style="font-weight:bold; font-size:12.5px; color:#bb86fc; display:block; margin-bottom:6px;">نام کاربری انتخابی لاتین:</label>
                        <input type="text" name="applicant_username" placeholder="مثلاً: miladtrans" required style="margin:0; padding:10px; background:#1e1a24; border:1px solid #333; border-radius:8px;">
                    </div>
                    <div>
                        <label style="font-weight:bold; font-size:12.5px; color:#bb86fc; display:block; margin-bottom:6px;">آدرس ایمیل متقاضی:</label>
                        <input type="email" name="applicant_email" placeholder="example@mr-v.ir" required style="margin:0; padding:10px; background:#1e1a24; border:1px solid #333; border-radius:8px;">
                    </div>
                    <div>
                        <label style="font-weight:bold; font-size:12.5px; color:#bb86fc; display:block; margin-bottom:6px;">کلمه عبور ورود به سیستم:</label>
                        <input type="password" name="applicant_password" placeholder="کلمه‌عبور مناسب" required style="margin:0; padding:10px; background:#1e1a24; border:1px solid #333; border-radius:8px;">
                    </div>
                </div>

                <div style="margin-top:10px;">
                    <label style="font-weight:bold; font-size:12.5px; color:#bb86fc; display:block; margin-bottom:6px;">فایل فشرده پاسخ آزمون استخدامی (PDF / ZIP / RAR / PNG):</label>
                    <input type="file" name="exam_file" required style="background:transparent; border:none; padding:10px 0; margin:0; color:#fff;">
                </div>

                <button type="submit" name="submit_exam" class="btn" style="width:100%; margin-top:15px; font-weight:bold; background: linear-gradient(135deg, #6200ee, #7c4dff); box-shadow: 0 4px 15px rgba(98,0,238,0.35); padding:12px;">ارسال مدارک استخدامی در دیتابیس مانگاتا ⚙️</button>
            </form>
        </div>

        <!-- 9. AUTHENTIC TERMINAL GATEWAY PANEL -->
        <div id="auth" class="card" style="border: 2px solid rgba(3,218,198,0.25); background: linear-gradient(145deg, rgba(20,18,24,0.85) 0%, rgba(13,10,18,0.95) 100%); backdrop-filter: blur(15px); box-shadow: 0 10px 40px rgba(3,218,198,0.12), inset 0 0 20px rgba(3,218,198,0.02); transition: all 0.3s ease; margin-bottom: 45px;">
            <?php if (!is_logged_in()): ?>
                <div style="text-align: center; margin-bottom: 22px;">
                    <div style="display: inline-flex; align-items: center; justify-content: center; width: 54px; height: 54px; border-radius: 14px; background: rgba(3,218,198,0.1); margin-bottom: 12px; color: #03dac6; border: 1px solid rgba(3,218,198,0.2);">
                        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                    </div>
                    <h2 style="color:#03dac6; margin:0 0 4px 0; font-size:22px; font-weight:800; text-shadow:0 0 10px rgba(3,218,198,0.25);">ورود به پایگاه فرماندهی</h2>
                    <p style="color:#aaa; font-size:12px; margin:0;">پورتال یکپارچه مدیریت آثار، تخصیص کادر و تصحیح آزمون‌ها</p>
                </div>
                
                <form action="#auth" method="POST" style="display:flex; flex-direction:column; gap:14px;">
                    <div>
                        <label style="font-size:12px; color:#03dac6; font-weight:800; display:block; margin-bottom:6px;">شناسه کاربری یا آدرسه ایمیل:</label>
                        <input type="text" name="username" placeholder="Username or email" required style="margin:0; background:rgba(255,255,255,0.03); border:1px solid rgba(255,255,255,0.08); border-radius:10px; padding:12px 14px; transition:all 0.3s;">
                    </div>

                    <div>
                        <label style="font-size:12px; color:#03dac6; font-weight:800; display:block; margin-bottom:6px;">رمز عبور یکپارچه:</label>
                        <input type="password" name="password" placeholder="••••••••" required style="margin:0; background:rgba(255,255,255,0.03); border:1px solid rgba(255,255,255,0.08); border-radius:10px; padding:12px 14px; transition:all 0.3s;">
                    </div>

                    <button type="submit" name="web_login" class="btn" style="width:100%; padding:13px; margin-top:8px; border-radius:12px; background:linear-gradient(135deg, #03dac6, #018786); color:#000; font-weight:900; letter-spacing:0.2px; font-size:14px; box-shadow:0 4px 15px rgba(3,218,198,0.3); transition:all 0.3s;">ورود امن به پنل مرکزی</button>
                </form>
                <div style="margin-top:18px; padding:12px; background:rgba(255,255,255,0.02); border-radius:10px; border:1px dashed rgba(255,255,255,0.06); font-size:11px; text-align:center; color:#777; line-height:1.6;">
                    اکانت مدیر پیش‌فرض جهت ورود:<br>
                    نام کاربری: <code style="color:#03dac6; font-weight:bold;">admin</code> | رمزعبور: <code style="color:#03dac6; font-weight:bold;">admin123</code>
                </div>
            <?php else: ?>
                <div style="text-align: center; margin-bottom: 18px;">
                    <div style="display: inline-flex; align-items: center; justify-content: center; width: 54px; height: 54px; border-radius: 14px; background: rgba(3,218,198,0.1); margin-bottom: 12px; color: #03dac6;">
                        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                    </div>
                    <h2 style="color:#03dac6; margin:0 0 4px 0; font-size: 20px; font-weight: bold;">👤 حساب من فعال است</h2>
                    <p style="color:#888; font-size: 11px; margin:0;">شما با موفقیت به پایگاه داده متصل شدید</p>
                </div>
                <div style="background: rgba(255,255,255,0.02); padding:16px; border-radius:12px; border:1px solid rgba(255,255,255,0.06); margin-bottom: 15px; display: flex; flex-direction: column; gap: 8px;">
                    <div style="display:flex; justify-content:space-between; font-size: 13px;"><span style="color:#888;">کاربر جاری:</span><strong style="color:#fff;"><?php echo htmlspecialchars($_SESSION['username']); ?></strong></div>
                    <div style="display:flex; justify-content:space-between; font-size: 13px;"><span style="color:#888;">نقش دسترسی:</span><strong style="color:#03dac6;"><?php echo htmlspecialchars($_SESSION['user_role'] === 'administrator' ? 'مدیریت کل' : 'مترجم/همکار'); ?></strong></div>
                </div>
                <div style="font-size: 10px; color:#03dac6; text-align:center; padding: 6px; border-radius: 6px; border: 1px solid rgba(3,218,198,0.1); background: rgba(3,218,198,0.05);" class="font-bold">
                    ⚡ نشست فعال و کنترل دوجانبه مستقیم برقرار است
                </div>
            <?php endif; ?>
        </div>

    <?php endif; ?>

    <!-- Administrative Operations Command Console (SUPER ADMIN ONLY) -->
    <?php if (is_admin()): ?>حیح آزمون‌ها</p>
                </div>
                
                <form action="#auth" method="POST" style="display:flex; flex-direction:column; gap:14px;">
                    <div>
                        <label style="font-size:12px; color:#03dac6; font-weight:800; display:block; margin-bottom:6px;">شناسه کاربری یا آدرسه ایمیل:</label>
                        <input type="text" name="username" placeholder="Username or email" required style="margin:0; background:rgba(255,255,255,0.03); border:1px solid rgba(255,255,255,0.08); border-radius:10px; padding:12px 14px; transition:all 0.3s;">
                    </div>

                    <div>
                        <label style="font-size:12px; color:#03dac6; font-weight:800; display:block; margin-bottom:6px;">رمز عبور یکپارچه:</label>
                        <input type="password" name="password" placeholder="••••••••" required style="margin:0; background:rgba(255,255,255,0.03); border:1px solid rgba(255,255,255,0.08); border-radius:10px; padding:12px 14px; transition:all 0.3s;">
                    </div>

                    <button type="submit" name="web_login" class="btn" style="width:100%; padding:13px; margin-top:8px; border-radius:12px; background:linear-gradient(135deg, #03dac6, #018786); color:#000; font-weight:900; letter-spacing:0.2px; font-size:14px; box-shadow:0 4px 15px rgba(3,218,198,0.3); transition:all 0.3s;">ورود امن به پنل مرکزی</button>
                </form>
                <div style="margin-top:18px; padding:12px; background:rgba(255,255,255,0.02); border-radius:10px; border:1px dashed rgba(255,255,255,0.06); font-size:11px; text-align:center; color:#777; line-height:1.6;">
                    اکانت مدیر پیش‌فرض جهت ورود:<br>
                    نام کاربری: <code style="color:#03dac6; font-weight:bold;">admin</code> | رمزعبور: <code style="color:#03dac6; font-weight:bold;">admin123</code>
                </div>
            <?php else: ?>
                <div style="text-align: center; margin-bottom: 18px;">
                    <div style="display: inline-flex; align-items: center; justify-content: center; width: 54px; height: 54px; border-radius: 14px; background: rgba(3,218,198,0.1); margin-bottom: 12px; color: #03dac6;">
                        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                    </div>
                    <h2 style="color:#03dac6; margin:0 0 4px 0; font-size: 20px; font-weight: bold;">👤 حساب من فعال است</h2>
                    <p style="color:#888; font-size: 11px; margin:0;">شما با موفقیت به پایگاه داده متصل شدید</p>
                </div>
                <div style="background: rgba(255,255,255,0.02); padding:16px; border-radius:12px; border:1px solid rgba(255,255,255,0.06); margin-bottom: 15px; display: flex; flex-direction: column; gap: 8px;">
                    <div style="display:flex; justify-content:space-between; font-size: 13px;"><span style="color:#888;">کاربر جاری:</span><strong style="color:#fff;"><?php echo htmlspecialchars($_SESSION['username']); ?></strong></div>
                    <div style="display:flex; justify-content:space-between; font-size: 13px;"><span style="color:#888;">نقش دسترسی:</span><strong style="color:#03dac6;"><?php echo htmlspecialchars($_SESSION['user_role'] === 'administrator' ? 'مدیریت کل' : 'مترجم/همکار'); ?></strong></div>
                </div>
                <div style="font-size: 10px; color:#03dac6; text-align:center; padding: 6px; border-radius: 6px; border: 1px solid rgba(3,218,198,0.1); background: rgba(3,218,198,0.05);" class="font-bold">
                    ⚡ نشست فعال و کنترل دوجانبه مستقیم برقرار است
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

                        <label style="font-size:12px; font-weight:bold; color:#ff7597;">نویسنده / خالق اثر:</label>
                        <input type="text" name="manga_author" placeholder="مثلاً چوگونگ" style="padding:8px; margin:5px 0 10px 0;">

                        <label style="font-size:12px; font-weight:bold; color:#ff7597;">سال انتشار:</label>
                        <input type="text" name="manga_release_year" placeholder="مثلاً 2024" style="padding:8px; margin:5px 0 10px 0;">

                        <label style="font-size:12px; font-weight:bold; color:#ff7597;">ژانرها (جدا شده با کامای فارسی یا انگلیسی):</label>
                        <input type="text" name="manga_genres" placeholder="مثلاً اکشن, فانتزی, درام" style="padding:8px; margin:5px 0 10px 0;">

                        <label style="font-size:12px; font-weight:bold; color:#ff7597;">بازیگران / شخصیت‌های اصلی (جدا شده با کاما):</label>
                        <input type="text" name="manga_main_characters" placeholder="مثلاً سونگ جین وو, چا هائه این" style="padding:8px; margin:5px 0 10px 0;">

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

            <!-- Manage Mangas Panel -->
            <h3 style="color:#ff5722; margin-top:40px; margin-bottom:15px; font-size:18px;">🎨 مدیریت و ویرایش آثار موجود:</h3>
            <div style="overflow-x:auto; margin-bottom:30px;">
                <table>
                    <thead>
                        <tr>
                            <th>آیدی</th>
                            <th>عنوان اثر و خالق</th>
                            <th>توضیحات معرفی و شخصیت‌ها</th>
                            <th>لینک کاور و سال تولید</th>
                            <th>ژانرها</th>
                            <th>عملیات ویرایش / حذف</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($mangas as $man): ?>
                            <tr>
                                <td style="text-align:center;">
                                    <code style="color:#03dac6; font-weight:bold;"><?php echo $man['id']; ?></code>
                                </td>
                                <form action="" method="POST" style="margin:0; padding:0; background:transparent; border:none; display:contents;">
                                    <input type="hidden" name="edit_manga_id" value="<?php echo $man['id']; ?>">
                                    <td>
                                        <input type="text" name="manga_title" value="<?php echo htmlspecialchars($man['title']); ?>" required style="margin-bottom:5px; padding:6px; font-size:12px; border-radius:6px; background:#111; color:#fff; border:1px solid #333; width:100%;" placeholder="عنوان">
                                        <input type="text" name="manga_author" value="<?php echo htmlspecialchars($man['author'] ?? ''); ?>" style="padding:6px; font-size:12px; border-radius:6px; background:#111; color:#fff; border:1px solid #333; width:100%;" placeholder="خالق/نویسنده">
                                    </td>
                                    <td>
                                        <textarea name="manga_desc" required style="margin-bottom:5px; padding:6px; font-size:12px; height:50px; width:100%; min-width:180px; border-radius:6px; background:#111; color:#fff; border:1px solid #333; font-family:inherit;" placeholder="خلاصه داستان"><?php echo htmlspecialchars($man['description']); ?></textarea>
                                        <input type="text" name="manga_main_characters" value="<?php echo htmlspecialchars($man['main_characters'] ?? ''); ?>" style="padding:6px; font-size:12px; border-radius:6px; background:#111; color:#fff; border:1px solid #333; width:100%;" placeholder="بازیگران/شخصیت‌ها">
                                    </td>
                                    <td>
                                        <input type="text" name="manga_cover" value="<?php echo htmlspecialchars($man['cover_image']); ?>" style="margin-bottom:5px; padding:6px; font-size:12px; width:100%; border-radius:6px; background:#111; color:#fff; border:1px solid #333;" placeholder="لینک کاور">
                                        <input type="text" name="manga_release_year" value="<?php echo htmlspecialchars($man['release_year'] ?? ''); ?>" style="padding:6px; font-size:12px; width:100%; border-radius:6px; background:#111; color:#fff; border:1px solid #333;" placeholder="سال انتشار">
                                    </td>
                                    <td>
                                        <input type="text" name="manga_genres" value="<?php echo htmlspecialchars($man['genres'] ?? ''); ?>" style="padding:6px; font-size:12px; width:100%; border-radius:6px; background:#111; color:#fff; border:1px solid #333;" placeholder="ژانرها">
                                    </td>
                                    <td>
                                        <div style="display:flex; gap:10px; justify-content:center;">
                                            <button type="submit" name="edit_manhwa_web" class="btn btn-sm" style="background:#4caf50; font-weight:bold; padding: 6px 12px; border-radius:6px;">ذخیره 💾</button>
                                        </div>
                                    </form>
                                    <div style="display:flex; justify-content:center; margin-top:5px;">
                                        <form action="" method="POST" style="margin:0; padding:0; background:transparent; border:none;" onsubmit="return confirm('⚠️ ادمین گرامی، آیا مطمئن هستید؟ با حذف این اثر تمامی زپیل‌های آپلود شده و تاریخچه چپترهای آن به طور کامل پاک می‌شوند!');">
                                            <input type="hidden" name="delete_manga_id" value="<?php echo $man['id']; ?>">
                                            <button type="submit" name="delete_manhwa_web" class="btn btn-sm" style="background:#b71c1c; font-weight:bold; padding: 6px 12px; border-radius:6px;">حذف کامل 🗑️</button>
                                        </form>
                                    </div>
                                    </td>
                            </tr>
                        <?php endforeach; ?>
                    </tbody>
                </table>
            </div>

            <!-- Manage Users Panel -->
            <h3 style="color:#ff5722; margin-top:40px; margin-bottom:15px; font-size:18px;">👤 مدیریت تمام کاربران و تغییر مشخصات و رمزعبور:</h3>
            <div style="overflow-x:auto;">
                <table>
                    <thead>
                        <tr>
                            <th>شناسه</th>
                            <th>نام کاربری</th>
                            <th>ایمیل</th>
                            <th>رمز عبور جدید</th>
                            <th>نقش دسترسی</th>
                            <th>عملیات بروزرسانی</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php
                        $stmt_users = $pdo->query("SELECT * FROM mangata_users ORDER BY id DESC");
                        $all_users = $stmt_users->fetchAll();
                        foreach ($all_users as $usr): ?>
                            <tr>
                                <td style="text-align:center;"><code><?php echo $usr['id']; ?></code></td>
                                <form action="" method="POST" style="display:contents; margin:0; padding:0; background:transparent; border:none;">
                                    <input type="hidden" name="target_user_id" value="<?php echo $usr['id']; ?>">
                                    <td>
                                        <input type="text" name="target_username" value="<?php echo htmlspecialchars($usr['username']); ?>" required style="margin:0; padding:6px; font-size:12px; border-radius:6px; background:#111; color:#fff; border:1px solid #333; max-width:110px;">
                                    </td>
                                    <td>
                                        <input type="email" name="target_email" value="<?php echo htmlspecialchars($usr['email']); ?>" required style="margin:0; padding:6px; font-size:12px; border-radius:6px; background:#111; color:#fff; border:1px solid #333; max-width:150px;">
                                    </td>
                                    <td>
                                        <input type="text" name="target_password" placeholder="تغییر پسورد (یا خالی)" style="margin:0; padding:6px; font-size:12px; border-radius:6px; background:#111; color:#ccc; border:1px solid #333; max-width:130px;">
                                    </td>
                                    <td style="text-align:center;">
                                        <select name="target_role" style="margin:0; padding:5px; font-size:12px; width:130px; background:#111; color:#fff; border:1px solid #333; border-radius:6px; text-align:center;">
                                            <option value="subscriber" <?php echo $usr['role'] === 'subscriber' ? 'selected' : ''; ?>>کاربر عادی (Subscriber)</option>
                                            <option value="administrator" <?php echo $usr['role'] === 'administrator' ? 'selected' : ''; ?>>مدیر کل (Administrator)</option>
                                            <option value="staff_translator" <?php echo $usr['role'] === 'staff_translator' ? 'selected' : ''; ?>>مترجم (Translator)</option>
                                            <option value="staff_redrawer" <?php echo $usr['role'] === 'staff_redrawer' ? 'selected' : ''; ?>>طراح (Redrawer)</option>
                                            <option value="staff_cleaner" <?php echo $usr['role'] === 'staff_cleaner' ? 'selected' : ''; ?>>کلینر (Cleaner)</option>
                                            <option value="staff_ts" <?php echo $usr['role'] === 'staff_ts' ? 'selected' : ''; ?>>تایپ‌ستر (Typesetter)</option>
                                        </select>
                                    </td>
                                    <td style="text-align:center;">
                                        <div style="display:flex; gap:6px; justify-content:center; align-items:center;">
                                            <button type="submit" name="update_user_full_web" class="btn btn-sm" style="background:#03dac6; color:#000; font-weight:bold; border-radius:6px; padding:6px 12px;">ذخیره ⚡</button>
                                </form>
                                            <?php if ($usr['id'] != $_SESSION['user_id']): ?>
                                                <form action="" method="POST" style="margin:0; padding:0; background:transparent; border:none; display:inline;" onsubmit="return confirm('آیا از حذف دائم این حساب حساب مطمئن هستید؟');">
                                                    <input type="hidden" name="target_user_id" value="<?php echo $usr['id']; ?>">
                                                    <button type="submit" name="delete_user_web" class="btn btn-sm" style="background:#b71c1c; font-weight:bold; padding: 6px 12px; border-radius:6px;">حذف 🗑️</button>
                                                </form>
                                            <?php else: ?>
                                                <span style="color:#ef5350; font-size:11px; font-weight:bold;">(شما)</span>
                                            <?php endif; ?>
                                        </div>
                                    </td>
                            </tr>
                        <?php endforeach; ?>
                    </tbody>
                </table>
            </div>

            <!-- Administrative Blog Management Panel -->
            <h3 style="color:#ff5722; margin-top:40px; margin-bottom:15px; font-size:18px;">📰 مدیریت اخبار و مقالات وبلاگ:</h3>
            <p style="color:#aaa; font-size:12px; margin-bottom:15px;">در این بخش به عنوان مدیریت کل می‌توانید اخبار و مقالات زنده وبلاگ صفحه نخست را ایجاد، ویرایش یا حذف کامل کنید.</p>
            
            <div style="display:grid; grid-template-columns: 1fr; gap:25px;" class="panel-flex">
                <!-- Create Blog Post form -->
                <div style="background:#201c24; padding:20px; border-radius:12px; border:1px solid rgba(187,134,252,0.2);">
                    <h4 style="color:#bb86fc; margin-top:0; font-size:14px; border-bottom:1px solid #333; padding-bottom:8px;">➕ منتشر کردن مطلب جدید وبلاگی</h4>
                    <form action="" method="POST" style="background:transparent; padding:0; border:none; display:flex; flex-direction:column; gap:10px;">
                        <div style="display:grid; grid-template-columns:1fr 1fr; gap:15px;">
                            <div>
                                <label style="display:block; font-size:11px; color:#aaa; margin-bottom:4px;">عنوان مقاله / خبر مانهوا:</label>
                                <input type="text" name="blog_title" required placeholder="مثال: مانهوای تک‌رو فصل ۳ کلید خورد!" style="margin:0; padding:8px;">
                            </div>
                            <div>
                                <label style="display:block; font-size:11px; color:#aaa; margin-bottom:4px;">آدرس تصویر شاخص (Image URL):</label>
                                <input type="text" name="blog_img" placeholder="https://picsum.photos/600/400" style="margin:0; padding:8px;">
                            </div>
                        </div>
                        <div>
                            <label style="display:block; font-size:11px; color:#aaa; margin-bottom:4px;">گزیده کوتاه خبر (Excerpt):</label>
                            <input type="text" name="blog_excerpt" required placeholder="توضیح بسیار کوتاه جهت نمایش سریع در کارت خانه..." style="margin:0; padding:8px;">
                        </div>
                        <div>
                            <label style="display:block; font-size:11px; color:#aaa; margin-bottom:4px;">متن کامل مقاله وبلاگ:</label>
                            <textarea name="blog_content" required placeholder="متن کامل و مفصل خبر به زبان فارسی روان..." style="margin:0; height:120px; padding:8px; font-family:inherit;"></textarea>
                        </div>
                        <button type="submit" name="create_blog_web" class="btn btn-sm" style="background:#03dac6; color:#000; font-weight:bold; padding:10px; width:100%;">انتشار زنده خبر در وبلاگ 🚀</button>
                    </form>
                </div>

                <!-- Existing Blogs List with Edit/Delete capabilities -->
                <div style="background:#201c24; padding:20px; border-radius:12px; border:1px solid rgba(187,134,252,0.2);">
                    <h4 style="color:#03dac6; margin-top:0; font-size:14px; border-bottom:1px solid #333; padding-bottom:8px;">📋 مقالات فعال و زنده موجود بر روی پایگاه داده</h4>
                    <div style="overflow-x:auto;">
                        <table>
                            <thead>
                                <tr>
                                    <th>شناسه</th>
                                    <th>عنوان خبر و تصویر</th>
                                    <th>خلاصه گزیده</th>
                                    <th>عملیات ویرایش و حذف زنده</th>
                                </tr>
                            </thead>
                            <tbody>
                                <?php
                                $stmt_all_bg = $pdo->query("SELECT * FROM mangata_blog ORDER BY id DESC");
                                $all_bg_posts = $stmt_all_bg->fetchAll();
                                foreach ($all_bg_posts as $p):
                                ?>
                                    <tr>
                                        <td><code><?php echo $p['id']; ?></code></td>
                                        <td>
                                            <form action="" method="POST" style="margin:0; padding:0; background:transparent; border:none; display:contents;">
                                                <input type="hidden" name="edit_blog_id" value="<?php echo $p['id']; ?>">
                                                <input type="text" name="blog_title" value="<?php echo htmlspecialchars($p['title']); ?>" required style="margin-bottom:6px; padding:6px; font-size:11px; width:100%;">
                                                <input type="text" name="blog_img" value="<?php echo htmlspecialchars($p['image_url']); ?>" placeholder="لینک تصویر" style="padding:6px; font-size:11px; width:100%;">
                                        </td>
                                        <td>
                                            <input type="text" name="blog_excerpt" value="<?php echo htmlspecialchars($p['excerpt']); ?>" required style="margin-bottom:6px; padding:6px; font-size:11px; width:100%;">
                                            <textarea name="blog_content" required class="form-control" style="width:100%; height:80px; padding:6px; font-size:11px; font-family:inherit; background:#111; color:#fff; border:1px solid #333; border-radius:4px; box-sizing:border-box;"><?php echo htmlspecialchars($p['content']); ?></textarea>
                                        </td>
                                        <td>
                                            <div style="display:flex; flex-direction:column; gap:6px; justify-content:center; align-items:center;">
                                                <button type="submit" name="edit_blog_web" class="btn btn-sm" style="background:#4caf50; font-weight:bold; width:100%; font-size:10px; padding:5px;">ذخیره 💾</button>
                                            </form>
                                            <form action="" method="POST" style="margin:0; padding:0; background:transparent; border:none; width:100%;" onsubmit="return confirm('آیا از حذف دائم این مقاله اطمینان دارید؟');">
                                                <input type="hidden" name="delete_blog_id" value="<?php echo $p['id']; ?>">
                                                <button type="submit" name="delete_blog_web" class="btn btn-sm" style="background:#b71c1c; font-weight:bold; width:100%; font-size:10px; padding:5px;">حذف 🗑️</button>
                                            </form>
                                            </div>
                                        </td>
                                    </tr>
                                <?php endforeach; ?>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- Manage Force Updates Panel -->
            <h3 style="color:#ff5722; margin-top:40px; margin-bottom:15px; font-size:18px;">🔄 پنل کنترل آپدیت اجباری کاربری (وب‌سایت و اپلیکیشن):</h3>
            <p style="color:#aaa; font-size:12px; margin-bottom:15px; line-height:1.7;">در این بخش به عنوان مدیریت کل می‌توانید با فعال‌سازی آپدیت اجباری، یک نوتیفیکیشن مسدودکننده در وبسایت یا اپلیکیشن ایجاد کنید تا زمانی‌که کاربر با کلیک روی آن دارایی‌های خود را بروز نکند، قادر به استفاده از امکانات سایت نباشد.</p>
            
            <form action="" method="POST" style="background:#201c24; padding:20px; border-radius:12px; border:1px solid rgba(187,134,252,0.2); display:grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap:20px; margin-bottom:30px;">
                <!-- App section -->
                <div style="background:rgba(255,255,255,0.03); padding:15px; border-radius:8px; border:1px solid #333;">
                    <h4 style="color:#bb86fc; margin-top:0; font-size:14px; border-bottom:1px solid #333; padding-bottom:8px;">📱 آپدیت اجباری اپلیکیشن اندروید</h4>
                    
                    <label style="display:block; font-size:12px; color:#aaa; margin-bottom:5px;">وضعیت آپدیت اجباری اپلیکیشن:</label>
                    <select name="force_update_app_active" style="width:100%; padding:8px; background:#111; color:#fff; border:1px solid #444; border-radius:6px; margin-bottom:12px;">
                        <option value="0" <?php echo get_mangata_setting('force_update_app_active', '0') === '0' ? 'selected' : ''; ?>>❌ غیرفعال (کاربران بدون مانع استفاده کنند)</option>
                        <option value="1" <?php echo get_mangata_setting('force_update_app_active', '0') === '1' ? 'selected' : ''; ?>>⚠️ فعال و مسدودکننده (کاربر باید دانلود کند)</option>
                    </select>

                    <label style="display:block; font-size:12px; color:#aaa; margin-bottom:5px;">لینک مستقیم دانلود نسخه جدید اپلیکیشن (APK):</label>
                    <input type="url" name="force_update_app_url" value="<?php echo htmlspecialchars(get_mangata_setting('force_update_app_url', 'https://mr-v.ir/')); ?>" required style="width:100%; padding:8px; background:#111; color:#fff; border:1px solid #444; border-radius:6px; margin-bottom:12px; box-sizing:border-box;">

                    <label style="display:block; font-size:12px; color:#aaa; margin-bottom:5px;">پیام خطای آپدیت اجباری برای کاربران اپلیکیشن:</label>
                    <textarea name="force_update_app_msg" style="width:100%; height:80px; padding:8px; background:#111; color:#fff; border:1px solid #444; border-radius:6px; box-sizing:border-box;"><?php echo htmlspecialchars(get_mangata_setting('force_update_app_msg', 'نسخه جدید و حیاتی اپلیکیشن مانگاتا آماده دریافت است. لطفا جهت دسترسی مجدد به امکانات برنامه آن را به روز رسانی کنید.')); ?></textarea>
                </div>

                <!-- Web section -->
                <div style="background:rgba(255,255,255,0.03); padding:15px; border-radius:8px; border:1px solid #333;">
                    <h4 style="color:#03dac6; margin-top:0; font-size:14px; border-bottom:1px solid #333; padding-bottom:8px;">🌐 آپدیت اجباری وب‌سایت (ریست کش خودکار)</h4>
                    
                    <label style="display:block; font-size:12px; color:#aaa; margin-bottom:5px;">وضعیت آپدیت اجباری وب‌سایت:</label>
                    <select name="force_update_web_active" style="width:100%; padding:8px; background:#111; color:#fff; border:1px solid #444; border-radius:6px; margin-bottom:12px;">
                        <option value="0" <?php echo get_mangata_setting('force_update_web_active', '0') === '0' ? 'selected' : ''; ?>>❌ غیرفعال (کاربران بدون مانع استفاده کنند)</option>
                        <option value="1" <?php echo get_mangata_setting('force_update_web_active', '0') === '1' ? 'selected' : ''; ?>>⚠️ فعال و مسدودکننده (کاربر باید کش را پاکسازی کند)</option>
                    </select>

                    <label style="display:block; font-size:12px; color:#aaa; margin-bottom:5px;">شماره نسخه دارایی‌های کش وب (Web Version Identifier):</label>
                    <input type="text" name="force_update_web_version" value="<?php echo htmlspecialchars(get_mangata_setting('force_update_web_version', '1')); ?>" required style="width:100%; padding:8px; background:#111; color:#fff; border:1px solid #444; border-radius:6px; margin-bottom:12px; box-sizing:border-box;">

                    <label style="display:block; font-size:12px; color:#aaa; margin-bottom:5px;">پیام اطلاع‌رسانی آپدیت و پاکسازی کش وب‌سایت:</label>
                    <textarea name="force_update_web_msg" style="width:100%; height:80px; padding:8px; background:#111; color:#fff; border:1px solid #444; border-radius:6px; box-sizing:border-box;"><?php echo htmlspecialchars(get_mangata_setting('force_update_web_msg', 'به‌روزرسانی مهمی برای وب‌سایت مانگاتا در دیتابیس ثبت شده است. جهت ارتقاء ثبات سیستم، تمیزکننده عمیق کش و دارایی‌ها را اجرا کنید.')); ?></textarea>
                </div>

                <!-- Submit Button -->
                <div style="grid-column: 1 / -1; text-align:center;">
                    <button type="submit" name="update_mangata_settings" class="btn" style="background:#ff5722; color:#fff; font-weight:bold; width:100%; padding:12px; border-radius:8px; cursor:pointer; border:none;">💾 ثبت تغییرات و اعمال فرمان زنده آپدیت اجباری در تمام سطوح</button>
                </div>
            </form>

        </div>
    <?php endif; ?>

    <?php endif; ?>

</div>

<footer style="background:#1a1a1a; padding: 25px; text-align:center; margin-top: 50px; border-top: 1px solid #333;">
    <p style="color:#888; font-size:14px; margin:0;">تمامی حقوق برای رسانه مانهواخوان مانگاتا (Mangata) محفوظ است | © 2026</p>
    <p style="color:#555; font-size:12px; margin-top:10px;">هماهنگ شده به صورت زنده و آنی با پایگاه داده و اپلیکیشن اختصاصی اندروید</p>
</footer>

</body>
</html>
