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

            api_send_success([
                'user_id' => (int)$user['id'],
                'username' => $user['username'],
                'email' => $user['email'],
                'role' => $user['role'],
                'display_name' => $user['username'],
                'token' => bin2hex(random_bytes(16))
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

            $stmt = $pdo->prepare("INSERT INTO mangata_users (username, email, password_hash, role) VALUES (?, ?, ?, ?)");
            $stmt->execute([$username, $email, $password_hash, $role]);
            $user_id = $pdo->lastInsertId();

            api_send_success([
                'user_id' => (int)$user_id,
                'username' => $username,
                'email' => $email,
                'role' => $role,
                'display_name' => $username,
                'token' => bin2hex(random_bytes(16))
            ]);
            break;

        // ================= MANHWAS =================
        case 'manhwa/list':
            $stmt = $pdo->query("SELECT * FROM mangata_mangas ORDER BY id DESC");
            $mangas = [];
            while ($row = $stmt->fetch()) {
                $mangas[] = [
                    'id' => (int)$row['id'],
                    'title' => $row['title'],
                    'description' => $row['description'],
                    'cover_image' => $row['cover_image'] ? $row['cover_image'] : null,
                    'created_at' => $row['created_at']
                ];
            }
            api_send_success($mangas);
            break;

        case 'manhwa/create':
            $title = trim($params['title'] ?? '');
            $desc = trim($params['description'] ?? '');
            $cover = trim($params['cover_image'] ?? '');

            if (empty($title)) {
                api_send_error('عنوان مانهوا الزامی است.');
            }

            $stmt = $pdo->prepare("INSERT INTO mangata_mangas (title, description, cover_image) VALUES (?, ?, ?)");
            $stmt->execute([$title, $desc, $cover]);
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
                
                foreach ($files as $file) {
                    if (in_array($file, ['.', '..'])) continue;
                    $ext = strtolower(pathinfo($file, PATHINFO_EXTENSION));
                    if (in_array($ext, $img_extensions)) {
                        $extracted_images[] = $base_url . 'uploads/extracts/' . $manga_id . '/' . $chapter_num . '/' . $file;
                    }
                }

                if (!empty($extracted_images)) {
                    natcasesort($extracted_images);
                    $extracted_images = array_values($extracted_images);
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

        default:
            api_send_error('مسیر درخواست API نامعتبر است: ' . $path, 404);
            break;
    }
} catch (Exception $e) {
    api_send_error('خطای سرور: ' . $e->getMessage(), 500);
}
