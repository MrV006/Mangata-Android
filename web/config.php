<?php
/**
 * Standalone Mangata Platform - Database Configuration & Self-Healing Schemas
 * Connected to mrvir111_mangata_db database on mr-v.ir
 */

session_start();

// Database connection details
$db_host = 'localhost';
$db_name = 'mrvir111_mangata_db';
$db_user = 'mrvir111_MrV';
$db_pass = 'gB3(td@~iji9H2~d';

try {
    $pdo = new PDO("mysql:host=$db_host;dbname=$db_name;charset=utf8mb4", $db_user, $db_pass, [
        PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES   => false,
    ]);
} catch (PDOException $e) {
    die("خطا در اتصال به پایگاه داده: " . $e->getMessage());
}

// Automatically create tables if they do not exist (Self-healing system)
function mangata_init_database($pdo) {
    // 1. Users Table
    $pdo->exec("CREATE TABLE IF NOT EXISTS mangata_users (
        id bigint(20) NOT NULL AUTO_INCREMENT,
        username varchar(50) NOT NULL UNIQUE,
        email varchar(100) NOT NULL UNIQUE,
        password_hash varchar(255) NOT NULL,
        role varchar(50) DEFAULT 'subscriber' NOT NULL,
        session_token varchar(255) DEFAULT NULL,
        wallet_balance bigint(20) DEFAULT 2800 NOT NULL,
        created_at datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
        PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

    // Add session_token column to mangata_users if not exists (for existing database migration)
    try {
        $pdo->exec("ALTER TABLE mangata_users ADD COLUMN session_token varchar(255) DEFAULT NULL;");
    } catch (Exception $e) {
        // Safe to ignore if already exists
    }

    // Add wallet_balance column to mangata_users if not exists
    try {
        $pdo->exec("ALTER TABLE mangata_users ADD COLUMN wallet_balance bigint(20) DEFAULT 2800 NOT NULL;");
    } catch (Exception $e) {
        // Safe to ignore if already exists
    }

    // 2. Mangas Table
    $pdo->exec("CREATE TABLE IF NOT EXISTS mangata_mangas (
        id bigint(20) NOT NULL AUTO_INCREMENT,
        title varchar(255) NOT NULL,
        description text NOT NULL,
        cover_image varchar(255) DEFAULT '' NOT NULL,
        genres varchar(255) DEFAULT '' NOT NULL,
        release_year varchar(50) DEFAULT '' NOT NULL,
        main_characters text DEFAULT NULL,
        author varchar(255) DEFAULT '' NOT NULL,
        created_at datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
        PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

    // Alter table to add columns for existing database
    $cols = [
        'genres' => "varchar(255) DEFAULT '' NOT NULL",
        'release_year' => "varchar(50) DEFAULT '' NOT NULL",
        'main_characters' => "text DEFAULT NULL",
        'author' => "varchar(255) DEFAULT '' NOT NULL"
    ];
    foreach ($cols as $col_name => $col_definition) {
        try {
            $pdo->exec("ALTER TABLE mangata_mangas ADD COLUMN $col_name $col_definition;");
        } catch (Exception $e) {
            // Safe to ignore if column already exists
        }
    }

    // 3. Chapters Table
    $pdo->exec("CREATE TABLE IF NOT EXISTS mangata_chapters (
        id bigint(20) NOT NULL AUTO_INCREMENT,
        manga_id bigint(20) NOT NULL,
        chapter_number decimal(10,2) NOT NULL,
        title varchar(255) DEFAULT '' NOT NULL,
        images_json longtext NOT NULL,
        zip_url varchar(255) DEFAULT '' NOT NULL,
        uploaded_by bigint(20) NOT NULL,
        created_at datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
        PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

    // 4. Exams Table
    $pdo->exec("CREATE TABLE IF NOT EXISTS mangata_exams (
        id bigint(20) NOT NULL AUTO_INCREMENT,
        user_id bigint(20) NOT NULL,
        file_name varchar(255) NOT NULL,
        file_url varchar(255) NOT NULL,
        status varchar(50) DEFAULT 'Pending' NOT NULL,
        score int(11) DEFAULT NULL,
        created_at datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
        PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

    // 5. Staff Assignments Table
    $pdo->exec("CREATE TABLE IF NOT EXISTS mangata_staff (
        id bigint(20) NOT NULL AUTO_INCREMENT,
        user_id bigint(20) NOT NULL,
        manga_id bigint(20) NOT NULL,
        role varchar(100) NOT NULL,
        PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

    // 6. Settings Table for global options (e.g. force updates)
    $pdo->exec("CREATE TABLE IF NOT EXISTS mangata_settings (
        setting_key varchar(100) NOT NULL UNIQUE,
        setting_value text NOT NULL,
        PRIMARY KEY (setting_key)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

    // 7. Bookmarks Table
    $pdo->exec("CREATE TABLE IF NOT EXISTS mangata_bookmarks (
        id bigint(20) NOT NULL AUTO_INCREMENT,
        user_id bigint(20) NOT NULL,
        manga_id bigint(20) NOT NULL,
        status varchar(50) DEFAULT 'Reading' NOT NULL,
        created_at datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
        PRIMARY KEY (id),
        UNIQUE KEY user_manga (user_id, manga_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

    // Seed default settings if empty
    $default_settings = [
        'force_update_app_active' => '0',
        'force_update_app_url' => 'https://mr-v.ir/',
        'force_update_app_msg' => 'نسخه جدید و حیاتی اپلیکیشن مانگاتا آماده دریافت است. لطفا جهت دسترسی مجدد به امکانات برنامه آن را به روز رسانی کنید.',
        'force_update_web_active' => '0',
        'force_update_web_version' => '1',
        'force_update_web_msg' => 'به‌روزرسانی مهمی برای وب‌سایت مانگاتا در دیتابیس ثبت شده است. جهت ارتقاء ثبات سیستم، تمیزکننده عمیق کش و دارایی‌ها را اجرا کنید.'
    ];
    foreach ($default_settings as $s_key => $s_val) {
        $stmt_set = $pdo->prepare("SELECT COUNT(*) FROM mangata_settings WHERE setting_key = ?");
        $stmt_set->execute([$s_key]);
        if ($stmt_set->fetchColumn() == 0) {
            $ins_set = $pdo->prepare("INSERT INTO mangata_settings (setting_key, setting_value) VALUES (?, ?)");
            $ins_set->execute([$s_key, $s_val]);
        }
    }

    // Seed default admin account if none exists
    $stmt = $pdo->query("SELECT COUNT(*) FROM mangata_users WHERE role = 'administrator'");
    if ($stmt->fetchColumn() == 0) {
        // Create default admin: admin / admin123
        $admin_user = 'admin';
        $admin_email = 'admin@mr-v.ir';
        $admin_pass_hash = password_hash('admin123', PASSWORD_BCRYPT);
        
        $insert = $pdo->prepare("INSERT INTO mangata_users (username, email, password_hash, role) VALUES (?, ?, ?, 'administrator')");
        $insert->execute([$admin_user, $admin_email, $admin_pass_hash]);
    }
}

// Call initializer
mangata_init_database($pdo);

// Helper Response Formatters for API
function api_send_success($data) {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(['status' => 'success', 'data' => $data], JSON_UNESCAPED_UNICODE);
    exit;
}

function api_send_error($message, $code = 400) {
    header('Content-Type: application/json; charset=utf-8');
    http_response_code($code);
    echo json_encode(['status' => 'error', 'message' => $message], JSON_UNESCAPED_UNICODE);
    exit;
}

// Session Helpers for Web Dashboard Login
function is_logged_in() {
    global $pdo;
    if (!isset($_SESSION['user_id'])) {
        return false;
    }
    if (isset($_SESSION['session_token'])) {
        try {
            $stmt = $pdo->prepare("SELECT session_token FROM mangata_users WHERE id = ?");
            $stmt->execute([$_SESSION['user_id']]);
            $db_token = $stmt->fetchColumn();
            if ($db_token !== $_SESSION['session_token']) {
                // Another device has logged in! Invalidate session.
                unset($_SESSION['user_id']);
                unset($_SESSION['user_role']);
                unset($_SESSION['session_token']);
                return false;
            }
        } catch (Exception $e) {
            // Decouple network/db issues gracefully
        }
    }
    return true;
}

function get_current_user_id() {
    return $_SESSION['user_id'] ?? null;
}

function is_admin() {
    return isset($_SESSION['user_role']) && $_SESSION['user_role'] === 'administrator';
}

function get_mangata_setting($key, $default = '') {
    global $pdo;
    try {
        $stmt = $pdo->prepare("SELECT setting_value FROM mangata_settings WHERE setting_key = ?");
        $stmt->execute([$key]);
        $val = $stmt->fetchColumn();
        return $val !== false ? $val : $default;
    } catch (Exception $e) {
        return $default;
    }
}

function set_mangata_setting($key, $value) {
    global $pdo;
    try {
        $stmt = $pdo->prepare("INSERT INTO mangata_settings (setting_key, setting_value) VALUES (?, ?) ON DUPLICATE KEY UPDATE setting_value = ?");
        return $stmt->execute([$key, $value, $value]);
    } catch (Exception $e) {
        return false;
    }
}
