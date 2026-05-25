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
        created_at datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
        PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

    // 2. Mangas Table
    $pdo->exec("CREATE TABLE IF NOT EXISTS mangata_mangas (
        id bigint(20) NOT NULL AUTO_INCREMENT,
        title varchar(255) NOT NULL,
        description text NOT NULL,
        cover_image varchar(255) DEFAULT '' NOT NULL,
        created_at datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
        PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

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
    return isset($_SESSION['user_id']);
}

function get_current_user_id() {
    return $_SESSION['user_id'] ?? null;
}

function is_admin() {
    return isset($_SESSION['user_role']) && $_SESSION['user_role'] === 'administrator';
}
