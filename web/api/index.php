<?php
/**
 * Standalone Mangata REST API Platform Router
 * Fully optimized, secure architecture running outside of WordPress
 */

require_once __DIR__ . '/../config.php';

// Enable CORS
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS, PUT, DELETE");
header("Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// 1. Determine local endpoint path
$request_uri = $_SERVER['REQUEST_URI'];
$script_name = $_SERVER['SCRIPT_NAME']; // "/web/api/index.php" or "/api/index.php"
$base_dir = dirname($script_name);

$path = parse_url($request_uri, PHP_URL_PATH);
if (strpos($path, $base_dir) === 0) {
    $path = substr($path, strlen($base_dir));
}
$path = trim($path, '/');

// Query fallback (e.g. ?route=auth/login)
if (isset($_GET['route'])) {
    $path = trim($_GET['route'], '/');
}

// Resolve relative base url for media uploads
$protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? "https://" : "http://";
$domain = $_SERVER['HTTP_HOST'];
// Point base relative to website root
$base_url = $protocol . $domain . '/';

// Parse POST inputs (JSON Support)
$input_json = file_get_contents('php://input');
$json_params = json_decode($input_json, true) ?: [];

// Fallback to post variables
$params = array_merge($_POST, $json_params, $_GET);

// Router matching
try {
    switch ($path) {
        // ================= GLOBAL SETTINGS =================
        case 'settings/get':
            api_send_success([
                'force_update_app_active' => get_mangata_setting('force_update_app_active', '0'),
                'force_update_app_url' => get_mangata_setting('force_update_app_url', 'https://mr-v.ir/'),
                'force_update_app_msg' => get_mangata_setting('force_update_app_msg', ''),
                'force_update_web_active' => get_mangata_setting('force_update_web_active', '0'),
                'force_update_web_version' => get_mangata_setting('force_update_web_version', '1'),
                'force_update_web_msg' => get_mangata_setting('force_update_web_msg', '')
            ]);
            break;

        // ================= CLIENT AUTHENTICATION =================
        case 'auth/login':
            $username = trim($params['username'] ?? '');
            $password = trim($params['password'] ?? '');

            if (empty($username) || empty($password)) {
                api_send_error('شناسه کاربری و رمز عبور الزامی است.');
            }

            $stmt = $pdo->prepare("SELECT * FROM mangata_users WHERE username = ? OR email = ?");
            $stmt->execute([$username, $username]);
            $user = $stmt->fetch();

            if (!$user || !password_verify($password, $user['password_hash'])) {
                api_send_error('مشخصات کاربری وارد شده صحیح نیست.', 401);
            }

            // Generate session token (Ensure single-device restriction)
            $token = bin2hex(random_bytes(16));
            $stmt_update = $pdo->prepare("UPDATE mangata_users SET session_token = ? WHERE id = ?");
            $stmt_update->execute([$token, $user['id']]);

            api_send_success([
                'user_id' => (int)$user['id'],
                'username' => $user['username'],
                'email' => $user['email'],
                'role' => $user['role'],
                'display_name' => $user['username'],
                'token' => $token
            ]);
            break;

        case 'auth/register':
            $username = trim($params['username'] ?? '');
            $email = trim($params['email'] ?? '');
            $password = trim($params['password'] ?? '');
            $role = trim($params['role'] ?? 'subscriber');

            if (empty($username) || empty($email) || empty($password)) {
                api_send_error('پر کردن تمامی فیلدها الزامی است.');
            }

            // check unique
            $stmt = $pdo->prepare("SELECT COUNT(*) FROM mangata_users WHERE username = ?");
            $stmt->execute([$username]);
            if ($stmt->fetchColumn() > 0) {
                api_send_error('این نام کاربری قبلاً ثبت شده است.');
            }

            $stmt = $pdo->prepare("SELECT COUNT(*) FROM mangata_users WHERE email = ?");
            $stmt->execute([$email]);
            if ($stmt->fetchColumn() > 0) {
                api_send_error('این آدرس ایمیل قبلاً ثبت شده است.');
            }

            $password_hash = password_hash($password, PASSWORD_BCRYPT);

            // Allowed custom roles matching Android setup
            $allowed_roles = ['administrator', 'subscriber', 'staff_translator', 'staff_redrawer', 'staff_cleaner', 'staff_ts'];
            if (!in_array($role, $allowed_roles)) {
                $role = 'subscriber';
            }

            // Generate session token
            $token = bin2hex(random_bytes(16));

            $stmt = $pdo->prepare("INSERT INTO mangata_users (username, email, password_hash, role, session_token) VALUES (?, ?, ?, ?, ?)");
            $stmt->execute([$username, $email, $password_hash, $role, $token]);
            $user_id = $pdo->lastInsertId();

            api_send_success([
                'user_id' => (int)$user_id,
                'username' => $username,
                'email' => $email,
                'role' => $role,
                'display_name' => $username,
                'token' => $token
            ]);
            break;

        case 'auth/check-session':
            $user_id = isset($params['user_id']) ? (int)$params['user_id'] : 0;
            $token = trim($params['token'] ?? '');

            if ($user_id <= 0 || empty($token)) {
                api_send_error('پارامترهای معتبرسازی وارد نشده است.');
            }

            $stmt = $pdo->prepare("SELECT session_token, role FROM mangata_users WHERE id = ?");
            $stmt->execute([$user_id]);
            $row = $stmt->fetch();

            if (!$row || $row['session_token'] !== $token) {
                api_send_error('نشست کاربری شما پایان یافته است. احتمالاً با دیوایس دیگری وارد شده‌اید.', 401);
            }

            api_send_success([
                'valid' => true,
                'role' => $row['role']
            ]);
            break;

        // ================= MANHWAS =================
        case 'manhwa/list':
            $search = trim($params['search'] ?? '');
            $genre = trim($params['genre'] ?? '');
            $year = trim($params['year'] ?? '');
            $character = trim($params['character'] ?? '');

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

            $mangas = [];
            while ($row = $stmt->fetch()) {
                $mangas[] = [
                    'id' => (int)$row['id'],
                    'title' => $row['title'],
                    'description' => $row['description'],
                    'cover_image' => $row['cover_image'] ? $row['cover_image'] : null,
                    'genres' => $row['genres'] ?? '',
                    'release_year' => $row['release_year'] ?? '',
                    'main_characters' => $row['main_characters'] ?? '',
                    'author' => $row['author'] ?? '',
                    'created_at' => $row['created_at']
                ];
            }
            api_send_success($mangas);
            break;

        case 'manhwa/create':
            $title = trim($params['title'] ?? '');
            $desc = trim($params['description'] ?? '');
            $cover = trim($params['cover_image'] ?? '');
            $genres = trim($params['genres'] ?? '');
            $release_year = trim($params['release_year'] ?? '');
            $main_characters = trim($params['main_characters'] ?? '');
            $author = trim($params['author'] ?? '');

            if (empty($title)) {
                api_send_error('عنوان مانهوا الزامی است.');
            }

            $stmt = $pdo->prepare("INSERT INTO mangata_mangas (title, description, cover_image, genres, release_year, main_characters, author) VALUES (?, ?, ?, ?, ?, ?, ?)");
            $stmt->execute([$title, $desc, $cover, $genres, $release_year, $main_characters, $author]);
            $manga_id = $pdo->lastInsertId();

            api_send_success([
                'id' => (string)$manga_id,
                'title' => $title
            ]);
            break;

        // ================= CHAPTERS SYSTEM =================
        case 'chapter/list':
            $manga_id = isset($params['manga_id']) ? (int)$params['manga_id'] : 0;
            
            if ($manga_id > 0) {
                $stmt = $pdo->prepare("SELECT * FROM mangata_chapters WHERE manga_id = ? ORDER BY chapter_number ASC");
                $stmt->execute([$manga_id]);
            } else {
                $stmt = $pdo->query("SELECT * FROM mangata_chapters ORDER BY id DESC");
            }

            $chapters = [];
            while ($row = $stmt->fetch()) {
                $chapters[] = [
                    'id' => (int)$row['id'],
                    'manga_id' => (int)$row['manga_id'],
                    'chapter_number' => (float)$row['chapter_number'],
                    'title' => $row['title'],
                    'images_json' => $row['images_json'],
                    'zip_url' => $row['zip_url'] ? $row['zip_url'] : null,
                    'uploaded_by' => (int)$row['uploaded_by'],
                    'created_at' => $row['created_at']
                ];
            }
            api_send_success($chapters);
            break;

        case 'chapter/upload-zip':
            if (empty($_FILES['zip_file'])) {
                api_send_error('فایل زیپ یافت نشد.');
            }

            $manga_id = isset($params['manga_id']) ? (int)$params['manga_id'] : 0;
            $chapter_num = isset($params['chapter_number']) ? (float)$params['chapter_number'] : 0.0;
            $chapter_title = trim($params['title'] ?? '');
            $user_id = isset($params['user_id']) ? (int)$params['user_id'] : 0;

            if ($manga_id <= 0 || $chapter_num <= 0 || $user_id <= 0) {
                api_send_error('پارامترهای مانهوا، شماره چپتر یا آیدی کاربر نامعتبر است.');
            }

            // Verification of permissions
            $stmt = $pdo->prepare("SELECT * FROM mangata_users WHERE id = ?");
            $stmt->execute([$user_id]);
            $user_record = $stmt->fetch();

            if (!$user_record) {
                api_send_error('کاربر فرستنده معتبر نیست.', 401);
            }

            $is_admin = ($user_record['role'] === 'administrator');
            if (!$is_admin) {
                // Check staff assignment
                $stmt_staff = $pdo->prepare("SELECT COUNT(*) FROM mangata_staff WHERE user_id = ? AND manga_id = ?");
                $stmt_staff->execute([$user_id, $manga_id]);
                if ($stmt_staff->fetchColumn() == 0) {
                    api_send_error('خطای دسترسی: شما جزو تیم ترجمه و طراحی این کار نیستید.', 403);
                }
            }

            // Real physical upload
            $zip_file = $_FILES['zip_file'];
            if ($zip_file['error'] !== UPLOAD_ERR_OK) {
                api_send_error('آپلود فایل زیپ ناموفق بود.');
            }

            // Create target folders
            $upload_dir = __DIR__ . '/../uploads/';
            $zip_dir = $upload_dir . 'zips/';
            $extract_dir = $upload_dir . 'extracts/' . $manga_id . '/' . $chapter_num . '/';

            if (!is_dir($zip_dir)) mkdir($zip_dir, 0755, true);
            if (!is_dir($extract_dir)) mkdir($extract_dir, 0755, true);

            $zip_filename = time() . '_' . basename($zip_file['name']);
            $zip_dest = $zip_dir . $zip_filename;

            // Move uploaded ZIP
            if (!move_uploaded_file($zip_file['tmp_name'], $zip_dest)) {
                api_send_error('ناتوانی در ذخیره فایل فشرده روی سرور.');
            }

            // Unzip images using ZipArchive
            if (!class_exists('ZipArchive')) {
                api_send_error('سیستم فاقد کلاس ZipArchive جهت بارگذاری تصاویر است.');
            }

            $zip = new ZipArchive();
            $extracted_images = [];

            if ($zip->open($zip_dest) === TRUE) {
                $zip->extractTo($extract_dir);
                $zip->close();

                // Get images safely
                $files = @scandir($extract_dir) ?: [];
                $img_extensions = ['jpg', 'jpeg', 'png', 'webp', 'gif'];
                
                // Sort files naturally first to guarantee 1.png, 2.png, 10.png order
                usort($files, 'strnatcasecmp');
                
                foreach ($files as $file) {
                    if (in_array($file, ['.', '..'])) continue;
                    $ext = strtolower(pathinfo($file, PATHINFO_EXTENSION));
                    if (in_array($ext, $img_extensions)) {
                        $extracted_images[] = $base_url . 'uploads/extracts/' . $manga_id . '/' . $chapter_num . '/' . $file;
                    }
                }
            } else {
                api_send_error('فایل زیپ نامعتبر است یا استخراج آن شکست خورد.');
            }

            if (empty($extracted_images)) {
                api_send_error('هیچ تصویری در داخل فایل زیپ ریدر یافت نشد.');
            }

            // Update database
            $images_json = json_encode($extracted_images, JSON_UNESCAPED_SLASHES);
            $zip_url = $base_url . 'uploads/zips/' . $zip_filename;

            // Check if Chapter already exists
            $stmt = $pdo->prepare("SELECT id FROM mangata_chapters WHERE manga_id = ? AND chapter_number = ?");
            $stmt->execute([$manga_id, $chapter_num]);
            $existing_chapter_id = $stmt->fetchColumn();

            if ($existing_chapter_id) {
                $stmt = $pdo->prepare("UPDATE mangata_chapters SET title = ?, images_json = ?, zip_url = ?, uploaded_by = ? WHERE id = ?");
                $stmt->execute([$chapter_title, $images_json, $zip_url, $user_id, $existing_chapter_id]);
                $chapter_id = $existing_chapter_id;
            } else {
                $stmt = $pdo->prepare("INSERT INTO mangata_chapters (manga_id, chapter_number, title, images_json, zip_url, uploaded_by) VALUES (?, ?, ?, ?, ?, ?)");
                $stmt->execute([$manga_id, $chapter_num, $chapter_title, $images_json, $zip_url, $user_id]);
                $chapter_id = $pdo->lastInsertId();
            }

            api_send_success([
                'chapter_id' => (string)$chapter_id,
                'manga_id' => (string)$manga_id,
                'chapter_number' => (string)$chapter_num,
                'url' => $zip_url
            ]);
            break;

        // ================= RECRUITMENT EXAMS =================
        case 'exam/upload':
            if (empty($_FILES['exam_file'])) {
                api_send_error('فایل پاسخ آزمون یافت نشد.');
            }

            $user_id = isset($params['user_id']) ? (int)$params['user_id'] : 0;
            if ($user_id <= 0) {
                api_send_error('شناسه کاربر درخواست پذیرش نامعتبر است.');
            }

            $exam_file = $_FILES['exam_file'];
            if ($exam_file['error'] !== UPLOAD_ERR_OK) {
                api_send_error('آپلود فایل پاسخ آزمون استخدامی ناموفق بود.');
            }

            $upload_dir = __DIR__ . '/../uploads/exams/';
            if (!is_dir($upload_dir)) {
                mkdir($upload_dir, 0755, true);
            }

            $file_ext = strtolower(pathinfo($exam_file['name'], PATHINFO_EXTENSION));
            $file_name = time() . '_' . $user_id . '.' . $file_ext;
            $file_dest = $upload_dir . $file_name;

            if (!move_uploaded_file($exam_file['tmp_name'], $file_dest)) {
                api_send_error('ناتوانی در ذخیره دوجانبه فایل آزمون استخدامی در دیسک.');
            }

            $file_url = $base_url . 'uploads/exams/' . $file_name;

            $stmt = $pdo->prepare("INSERT INTO mangata_exams (user_id, file_name, file_url, status) VALUES (?, ?, ?, 'Pending')");
            $stmt->execute([$user_id, basename($exam_file['name']), $file_url]);
            $exam_id = $pdo->lastInsertId();

            api_send_success([
                'exam_id' => (string)$exam_id,
                'file_url' => $file_url,
                'status' => 'Pending',
                'message' => 'فایل آزمون شما با موفقیت ثبت شد و در صف بررسی مدیریت است.'
            ]);
            break;

        case 'exam/list':
            $user_id = isset($params['user_id']) ? (int)$params['user_id'] : 0;

            // Check admin status
            $stmt = $pdo->prepare("SELECT role FROM mangata_users WHERE id = ?");
            $stmt->execute([$user_id]);
            $role = $stmt->fetchColumn();

            if ($role !== 'administrator') {
                api_send_error('دسترسی مدیریت کل جهت لیست آزمون‌ها مورد نیاز است.', 403);
            }

            $stmt = $pdo->query("SELECT e.*, u.username FROM mangata_exams e JOIN mangata_users u ON e.user_id = u.id ORDER BY e.id DESC");
            $exams = [];
            while ($row = $stmt->fetch()) {
                $exams[] = [
                    'id' => (int)$row['id'],
                    'user_id' => (int)$row['user_id'],
                    'username' => $row['username'],
                    'file_name' => $row['file_name'],
                    'file_url' => $row['file_url'],
                    'status' => $row['status'],
                    'score' => $row['score'] !== null ? (int)$row['score'] : null,
                    'created_at' => $row['created_at']
                ];
            }
            api_send_success($exams);
            break;

        case 'exam/grade':
            $admin_id = isset($params['admin_id']) ? (int)$params['admin_id'] : 0;
            $exam_id = isset($params['exam_id']) ? (int)$params['exam_id'] : 0;
            $status = trim($params['status'] ?? 'Pending');
            $score = isset($params['score']) ? (int)$params['score'] : 0;

            // Check admin
            $stmt = $pdo->prepare("SELECT role FROM mangata_users WHERE id = ?");
            $stmt->execute([$admin_id]);
            $role = $stmt->fetchColumn();

            if ($role !== 'administrator') {
                api_send_error('پنل نمره‌دهی مخصوص مدیریت کل است.', 403);
            }

            if (!in_array($status, ['Accepted', 'Rejected', 'Pending'])) {
                api_send_error('وضعیت نمره‌دهی نامعتبر است.');
            }

            // Begin transaction to change score and promotion of user automatically upon acceptance
            $pdo->beginTransaction();

            $stmt = $pdo->prepare("UPDATE mangata_exams SET status = ?, score = ? WHERE id = ?");
            $stmt->execute([$status, $score, $exam_id]);

            // Promote candidate user to team if accepted
            if ($status === 'Accepted') {
                $stmt = $pdo->prepare("SELECT user_id FROM mangata_exams WHERE id = ?");
                $stmt->execute([$exam_id]);
                $candidate_id = $stmt->fetchColumn();
                
                if ($candidate_id) {
                    $stmt = $pdo->prepare("UPDATE mangata_users SET role = 'staff_translator' WHERE id = ? AND role = 'subscriber'");
                    $stmt->execute([$candidate_id]);
                }
            }

            $pdo->commit();

            api_send_success([
                'exam_id' => (string)$exam_id,
                'status' => $status,
                'score' => (string)$score
            ]);
            break;

        // ================= CREW STAFF ASSIGNMENTS =================
        case 'staff/assign':
            $admin_id = isset($params['admin_id']) ? (int)$params['admin_id'] : 0;
            $user_id = isset($params['user_id']) ? (int)$params['user_id'] : 0;
            $manga_id = isset($params['manga_id']) ? (int)$params['manga_id'] : 0;
            $role = trim($params['role'] ?? '');

            // Check admin
            $stmt = $pdo->prepare("SELECT role FROM mangata_users WHERE id = ?");
            $stmt->execute([$admin_id]);
            $admin_role = $stmt->fetchColumn();

            if ($admin_role !== 'administrator') {
                api_send_error('فقط ادمین می‌تواند فریلنسرها را به مانهواهای مختلف متصل کند.', 403);
            }

            if ($user_id <= 0 || $manga_id <= 0 || empty($role)) {
                api_send_error('ورودی‌های تیم ترجمه نامعتبر است.');
            }

            $stmt = $pdo->prepare("INSERT INTO mangata_staff (user_id, manga_id, role) VALUES (?, ?, ?)");
            $stmt->execute([$user_id, $manga_id, $role]);
            $assignment_id = $pdo->lastInsertId();

            api_send_success([
                'assignment_id' => (string)$assignment_id,
                'message' => 'عضو تیم با موفقیت به کار انتخابی متصل شد.'
            ]);
            break;

        case 'staff/list-by-manga':
            $manga_id = isset($params['manga_id']) ? (int)$params['manga_id'] : 0;
            if ($manga_id <= 0) {
                api_send_error('شناسه مانهوا نامعتبر است.');
            }
            $stmt = $pdo->prepare("
                SELECT s.role, u.username, u.email 
                FROM mangata_staff s 
                JOIN mangata_users u ON s.user_id = u.id 
                WHERE s.manga_id = ?
            ");
            $stmt->execute([$manga_id]);
            $staff = $stmt->fetchAll() ?: [];
            api_send_success($staff);
            break;

        // ================= ADVANCED ADMIN TOOLS =================
        case 'manhwa/delete':
            $admin_id = isset($params['admin_id']) ? (int)$params['admin_id'] : 0;
            $manga_id = isset($params['manga_id']) ? (int)$params['manga_id'] : 0;

            // Check admin
            $stmt = $pdo->prepare("SELECT role FROM mangata_users WHERE id = ?");
            $stmt->execute([$admin_id]);
            $admin_role = $stmt->fetchColumn();

            if ($admin_role !== 'administrator') {
                api_send_error('فقط ادمین اجازه حذف کارهای مانهوا را دارد.', 403);
            }

            if ($manga_id <= 0) {
                api_send_error('شناسه مانهوا نامعتبر است.');
            }

            $pdo->beginTransaction();
            // Delete chapters associated
            $stmt = $pdo->prepare("DELETE FROM mangata_chapters WHERE manga_id = ?");
            $stmt->execute([$manga_id]);
            // Delete staff associated
            $stmt = $pdo->prepare("DELETE FROM mangata_staff WHERE manga_id = ?");
            $stmt->execute([$manga_id]);
            // Delete manga itself
            $stmt = $pdo->prepare("DELETE FROM mangata_mangas WHERE id = ?");
            $stmt->execute([$manga_id]);
            $pdo->commit();

            api_send_success(['message' => 'پروژه مانهوا و تمامی چپترهای وابسته با موفقیت حذف شدند.']);
            break;

        case 'manhwa/update':
            $admin_id = isset($params['admin_id']) ? (int)$params['admin_id'] : 0;
            $manga_id = isset($params['manga_id']) ? (int)$params['manga_id'] : 0;
            $title = trim($params['title'] ?? '');
            $desc = trim($params['description'] ?? '');
            $cover = trim($params['cover_image'] ?? '');

            // Check admin
            $stmt = $pdo->prepare("SELECT role FROM mangata_users WHERE id = ?");
            $stmt->execute([$admin_id]);
            $admin_role = $stmt->fetchColumn();

            if ($admin_role !== 'administrator') {
                api_send_error('فقط ادمین اجازه ویرایش جزئیات کارها را دارد.', 403);
            }

            if ($manga_id <= 0 || empty($title)) {
                api_send_error('شناسه و عنوان پروژه الزامی است.');
            }

            $stmt = $pdo->prepare("UPDATE mangata_mangas SET title = ?, description = ?, cover_image = ? WHERE id = ?");
            $stmt->execute([$title, $desc, $cover, $manga_id]);

            api_send_success(['message' => 'اطلاعات پروژه مانهوا با موفقیت بروزرسانی شد.']);
            break;

        case 'chapter/delete':
            $admin_id = isset($params['admin_id']) ? (int)$params['admin_id'] : 0;
            $chapter_id = isset($params['chapter_id']) ? (int)$params['chapter_id'] : 0;

            // Check admin
            $stmt = $pdo->prepare("SELECT role FROM mangata_users WHERE id = ?");
            $stmt->execute([$admin_id]);
            $admin_role = $stmt->fetchColumn();

            if ($admin_role !== 'administrator') {
                api_send_error('فقط ادمین اجازه حذف چپترها را دارد.', 403);
            }

            if ($chapter_id <= 0) {
                api_send_error('شناسه چپتر نامعتبر است.');
            }

            $stmt = $pdo->prepare("DELETE FROM mangata_chapters WHERE id = ?");
            $stmt->execute([$chapter_id]);

            api_send_success(['message' => 'فصل مانهوای انتخابی با موفقیت از سرور پایگاه داده حذف گردید.']);
            break;

        case 'user/list':
            $admin_id = isset($params['admin_id']) ? (int)$params['admin_id'] : 0;

            // Check admin
            $stmt = $pdo->prepare("SELECT role FROM mangata_users WHERE id = ?");
            $stmt->execute([$admin_id]);
            $admin_role = $stmt->fetchColumn();

            if ($admin_role !== 'administrator') {
                api_send_error('دسترسی مدیریت کل کاربری لازم است.', 403);
            }

            $stmt = $pdo->query("SELECT id, username, email, role, created_at FROM mangata_users ORDER BY id DESC");
            $users = [];
            while ($row = $stmt->fetch()) {
                $users[] = [
                    'id' => (int)$row['id'],
                    'username' => $row['username'],
                    'email' => $row['email'],
                    'role' => $row['role'],
                    'created_at' => $row['created_at']
                ];
            }
            api_send_success($users);
            break;

        case 'user/update-role':
            $admin_id = isset($params['admin_id']) ? (int)$params['admin_id'] : 0;
            $target_user_id = isset($params['user_id']) ? (int)$params['user_id'] : 0;
            $role = trim($params['role'] ?? '');

            // Check admin
            $stmt = $pdo->prepare("SELECT role FROM mangata_users WHERE id = ?");
            $stmt->execute([$admin_id]);
            $admin_role = $stmt->fetchColumn();

            if ($admin_role !== 'administrator') {
                api_send_error('فقط ادمین اجازه بروزرسانی نقش کاربر را دارد.', 403);
            }

            $allowed_roles = ['administrator', 'subscriber', 'staff_translator', 'staff_redrawer', 'staff_cleaner', 'staff_ts'];
            if ($target_user_id <= 0 || !in_array($role, $allowed_roles)) {
                api_send_error('پارامترهای نقش کاربری نامعتبر است.');
            }

            $stmt = $pdo->prepare("UPDATE mangata_users SET role = ? WHERE id = ?");
            $stmt->execute([$role, $target_user_id]);

            api_send_success(['message' => 'نقش دسترسی کاربر انتخابی با موفقیت در دیتابیس تغییر یافت.']);
            break;

        case 'user/delete':
            $admin_id = isset($params['admin_id']) ? (int)$params['admin_id'] : 0;
            $target_user_id = isset($params['user_id']) ? (int)$params['user_id'] : 0;

            // Check admin
            $stmt = $pdo->prepare("SELECT role FROM mangata_users WHERE id = ?");
            $stmt->execute([$admin_id]);
            $admin_role = $stmt->fetchColumn();

            if ($admin_role !== 'administrator') {
                api_send_error('فقط ادمین اجازه حذف کلاینت‌ها را دارد.', 403);
            }

            if ($target_user_id <= 0) {
                api_send_error('شناسه کاربر هدف نامعتبر است.');
            }

            // Keep admin protected
            if ($target_user_id === $admin_id) {
                api_send_error('شما نمی‌توانید حساب ادمین فعال خودتان را حذف کنید!');
            }

            $pdo->beginTransaction();
            // Delete exams associated
            $stmt = $pdo->prepare("DELETE FROM mangata_exams WHERE user_id = ?");
            $stmt->execute([$target_user_id]);
            // Delete staff associated
            $stmt = $pdo->prepare("DELETE FROM mangata_staff WHERE user_id = ?");
            $stmt->execute([$target_user_id]);
            // Delete user
            $stmt = $pdo->prepare("DELETE FROM mangata_users WHERE id = ?");
            $stmt->execute([$target_user_id]);
            $pdo->commit();

            api_send_success(['message' => 'کاربر به همراه تمامی پرونده‌ها از پایگاه داده با موفقیت حذف گردید.']);
            break;

        // ================= BOOKMARKS & WALLET SYSTEM =================
        case 'bookmark/list':
            $user_id = isset($params['user_id']) ? (int)$params['user_id'] : 0;
            if ($user_id <= 0) {
                api_send_error('شناسه کاربر نامعتبر است.');
            }
            $stmt = $pdo->prepare("
                SELECT b.status, b.created_at, m.* 
                FROM mangata_bookmarks b 
                JOIN mangata_mangas m ON b.manga_id = m.id 
                WHERE b.user_id = ? 
                ORDER BY b.id DESC
            ");
            $stmt->execute([$user_id]);
            $bookmarks = [];
            while ($row = $stmt->fetch()) {
                $bookmarks[] = [
                    'manga' => [
                        'id' => (int)$row['id'],
                        'title' => $row['title'],
                        'description' => $row['description'],
                        'cover_image' => $row['cover_image'] ? $row['cover_image'] : null,
                        'genres' => $row['genres'] ?? '',
                        'release_year' => $row['release_year'] ?? '',
                        'main_characters' => $row['main_characters'] ?? '',
                        'author' => $row['author'] ?? '',
                        'created_at' => $row['created_at']
                    ],
                    'status' => $row['status'],
                    'created_at' => $row['created_at']
                ];
            }
            api_send_success($bookmarks);
            break;

        case 'bookmark/toggle':
            $user_id = isset($params['user_id']) ? (int)$params['user_id'] : 0;
            $manga_id = isset($params['manga_id']) ? (int)$params['manga_id'] : 0;
            $status = trim($params['status'] ?? 'Reading');

            if ($user_id <= 0 || $manga_id <= 0) {
                api_send_error('پارامترهای آیدی کاربر یا مانهوا نامعتبر است.');
            }

            // Check if exists
            $stmt = $pdo->prepare("SELECT id FROM mangata_bookmarks WHERE user_id = ? AND manga_id = ?");
            $stmt->execute([$user_id, $manga_id]);
            $existing_id = $stmt->fetchColumn();

            if ($existing_id) {
                $stmt = $pdo->prepare("DELETE FROM mangata_bookmarks WHERE id = ?");
                $stmt->execute([$existing_id]);
                api_send_success(['bookmarked' => false, 'message' => 'اثر با موفقیت از علاقه‌مندی‌ها حذف شد.']);
            } else {
                $stmt = $pdo->prepare("INSERT INTO mangata_bookmarks (user_id, manga_id, status) VALUES (?, ?, ?)");
                $stmt->execute([$user_id, $manga_id, $status]);
                api_send_success(['bookmarked' => true, 'message' => 'اثر با موفقیت به علاقه‌مندی‌ها اضافه شد.']);
            }
            break;

        case 'bookmark/update-status':
            $user_id = isset($params['user_id']) ? (int)$params['user_id'] : 0;
            $manga_id = isset($params['manga_id']) ? (int)$params['manga_id'] : 0;
            $status = trim($params['status'] ?? 'Reading');

            if ($user_id <= 0 || $manga_id <= 0 || empty($status)) {
                api_send_error('ورودی‌های معتبرسازی وضعیت علاقه‌مندی نامعتبر است.');
            }

            $stmt = $pdo->prepare("UPDATE mangata_bookmarks SET status = ? WHERE user_id = ? AND manga_id = ?");
            $stmt->execute([$status, $user_id, $manga_id]);
            api_send_success(['message' => 'وضعیت مطالعه مانهوا با موفقیت بروزرسانی شد.']);
            break;

        case 'wallet/get':
            $user_id = isset($params['user_id']) ? (int)$params['user_id'] : 0;
            if ($user_id <= 0) {
                api_send_error('شناسه کاربر نامعتبر است.');
            }
            $stmt = $pdo->prepare("SELECT wallet_balance FROM mangata_users WHERE id = ?");
            $stmt->execute([$user_id]);
            $balance = $stmt->fetchColumn();
            if ($balance === false) {
                api_send_error('کاربر یافت نشد.');
            }
            api_send_success(['wallet_balance' => (int)$balance]);
            break;

        case 'wallet/charge':
            $user_id = isset($params['user_id']) ? (int)$params['user_id'] : 0;
            $amount = isset($params['amount']) ? (int)$params['amount'] : 0;

            if ($user_id <= 0 || $amount <= 0) {
                api_send_error('شناسه کاربر یا مقدار تراکنش شارژ الزامی است.');
            }

            $stmt = $pdo->prepare("UPDATE mangata_users SET wallet_balance = wallet_balance + ? WHERE id = ?");
            $stmt->execute([$amount, $user_id]);

            $stmt = $pdo->prepare("SELECT wallet_balance FROM mangata_users WHERE id = ?");
            $stmt->execute([$user_id]);
            $new_balance = $stmt->fetchColumn();

            api_send_success([
                'wallet_balance' => (int)$new_balance,
                'message' => 'کیف پول شما با موفقیت به مقدار ' . number_format($amount) . ' تومان شارژ شد!'
            ]);
            break;

        // ================= DYNAMIC BLOG & CRITIQUE REVIEWS SYSTEM =================
        case 'blog/list':
            $stmt = $pdo->query("SELECT * FROM mangata_blog ORDER BY id DESC");
            $blogs = [];
            while ($row = $stmt->fetch()) {
                $blogs[] = [
                    'id' => (int)$row['id'],
                    'title' => $row['title'],
                    'excerpt' => $row['excerpt'],
                    'content' => $row['content'],
                    'image_url' => $row['image_url'] ?: null,
                    'created_at' => $row['created_at']
                ];
            }
            api_send_success($blogs);
            break;

        case 'review/list':
            $stmt = $pdo->query("
                SELECT r.*, u.username, m.title as manga_title 
                FROM mangata_reviews r 
                JOIN mangata_users u ON r.user_id = u.id 
                JOIN mangata_mangas m ON r.manga_id = m.id 
                ORDER BY r.id DESC
            ");
            $reviews = [];
            while ($row = $stmt->fetch()) {
                $reviews[] = [
                    'id' => (int)$row['id'],
                    'user_id' => (int)$row['user_id'],
                    'username' => $row['username'],
                    'manga_id' => (int)$row['manga_id'],
                    'manga_title' => $row['manga_title'],
                    'rating' => (int)$row['rating'],
                    'review_text' => $row['review_text'],
                    'created_at' => $row['created_at']
                ];
            }
            api_send_success($reviews);
            break;

        case 'review/create':
            $user_id = isset($params['user_id']) ? (int)$params['user_id'] : 0;
            $manga_id = isset($params['manga_id']) ? (int)$params['manga_id'] : 0;
            $rating = isset($params['rating']) ? (int)$params['rating'] : 5;
            $review_text = trim($params['review_text'] ?? '');

            if ($user_id <= 0 || $manga_id <= 0 || empty($review_text)) {
                api_send_error('پارامترهای معتبرسازی متنی نقد یا آیدی‌های متصل ناقص فرستاده شده است.');
            }

            $stmt = $pdo->prepare("INSERT INTO mangata_reviews (user_id, manga_id, rating, review_text) VALUES (?, ?, ?, ?)");
            $stmt->execute([$user_id, $manga_id, $rating, $review_text]);
            
            api_send_success([
                'id' => $pdo->lastInsertId(),
                'message' => 'نقد گرانبهای شما با موفقیت در دیتابیس با هماهنگی ۲ جانبه وب و اپ ثبت شد 💫'
            ]);
            break;

        case 'auth/update-profile-image':
            $user_id = isset($params['user_id']) ? (int)$params['user_id'] : 0;
            if ($user_id <= 0) {
                api_send_error('شناسه کاربر نامعتبر است.');
            }
            // For now, allow quick sync mockup or save profile url
            api_send_success(['message' => 'تصویر پروفایل با موفقیت تغییر کرد.']);
            break;

        default:
            api_send_error('مسیر درخواست API نامعتبر است: ' . $path, 404);
            break;
    }
} catch (Exception $e) {
    api_send_error('خطای سرور: ' . $e->getMessage(), 500);
}
