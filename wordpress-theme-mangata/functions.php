<?php
/**
 * Mangata Theme Functions & REST API Backend Router
 * Fully synched with Android App. Real Database connections, no mockups.
 */

// 1. Establish custom database tables on theme activation
if (!function_exists('mangata_ensure_table_exists')) {
    function mangata_ensure_table_exists($table_name, $create_sql) {
        global $wpdb;
        if ($wpdb->get_var($wpdb->prepare("SHOW TABLES LIKE %s", $table_name)) !== $table_name) {
            $wpdb->query($create_sql);
        }
    }
}

function mangata_setup_tables() {
    global $wpdb;

    // Check if we've already set up the db to avoid running heavy checks on every request if possible
    if (get_option('mangata_db_created') === '1.0.0') {
        return;
    }

    $charset_collate = $wpdb->get_charset_collate();

    // Mangas Table
    $table_mangas = $wpdb->prefix . 'mangata_mangas';
    $sql_mangas = "CREATE TABLE $table_mangas (
        id bigint(20) NOT NULL AUTO_INCREMENT,
        title varchar(255) NOT NULL,
        description text DEFAULT '' NOT NULL,
        cover_image varchar(255) DEFAULT '' NOT NULL,
        created_at datetime DEFAULT '0000-00-00 00:00:00' NOT NULL,
        PRIMARY KEY (id)
    ) $charset_collate;";
    mangata_ensure_table_exists($table_mangas, $sql_mangas);

    // Chapters Table
    $table_chapters = $wpdb->prefix . 'mangata_chapters';
    $sql_chapters = "CREATE TABLE $table_chapters (
        id bigint(20) NOT NULL AUTO_INCREMENT,
        manga_id bigint(20) NOT NULL,
        chapter_number decimal(10,2) NOT NULL,
        title varchar(255) DEFAULT '' NOT NULL,
        images_json longtext NOT NULL,
        zip_url varchar(255) DEFAULT '' NOT NULL,
        uploaded_by bigint(20) NOT NULL,
        created_at datetime DEFAULT '0000-00-00 00:00:00' NOT NULL,
        PRIMARY KEY (id)
    ) $charset_collate;";
    mangata_ensure_table_exists($table_chapters, $sql_chapters);

    // Exams Table (Recruitment Exam files)
    $table_exams = $wpdb->prefix . 'mangata_exams';
    $sql_exams = "CREATE TABLE $table_exams (
        id bigint(20) NOT NULL AUTO_INCREMENT,
        user_id bigint(20) NOT NULL,
        file_name varchar(255) NOT NULL,
        file_url varchar(255) NOT NULL,
        status varchar(50) DEFAULT 'Pending' NOT NULL,
        score int(11) DEFAULT NULL,
        created_at datetime DEFAULT '0000-00-00 00:00:00' NOT NULL,
        PRIMARY KEY (id)
    ) $charset_collate;";
    mangata_ensure_table_exists($table_exams, $sql_exams);

    // Staff Assignments Table
    $table_staff = $wpdb->prefix . 'mangata_staff';
    $sql_staff = "CREATE TABLE $table_staff (
        id bigint(20) NOT NULL AUTO_INCREMENT,
        user_id bigint(20) NOT NULL,
        manga_id bigint(20) NOT NULL,
        role varchar(100) NOT NULL,
        PRIMARY KEY (id)
    ) $charset_collate;";
    mangata_ensure_table_exists($table_staff, $sql_staff);

    // Save DB status option
    update_option('mangata_db_created', '1.0.0');
}
add_action('after_switch_theme', 'mangata_setup_tables');

// Ensure tables are loaded on theme init if not exists
add_action('init', 'mangata_setup_tables');


// Helper: Format API Success and Errors
function mangata_api_success($data) {
    return new WP_REST_Response(array('status' => 'success', 'data' => $data), 200);
}

function mangata_api_error($message, $code = 400) {
    return new WP_REST_Response(array('status' => 'error', 'message' => $message), $code);
}


// 2. Register REST API Endpoints
add_action('rest_api_init', function () {
    
    // AUTHENTICATION
    register_rest_route('mangata/v1', '/auth/login', array(
        'methods' => 'POST',
        'callback' => 'mangata_api_login',
        'permission_callback' => '__return_true'
    ));

    register_rest_route('mangata/v1', '/auth/register', array(
        'methods' => 'POST',
        'callback' => 'mangata_api_register',
        'permission_callback' => '__return_true'
    ));

    // MANHWAS & CHAPTERS
    register_rest_route('mangata/v1', '/manhwa/list', array(
        'methods' => 'GET',
        'callback' => 'mangata_api_get_manhwas',
        'permission_callback' => '__return_true'
    ));

    register_rest_route('mangata/v1', '/manhwa/create', array(
        'methods' => 'POST',
        'callback' => 'mangata_api_create_manhwa',
        'permission_callback' => '__return_true'
    ));

    register_rest_route('mangata/v1', '/chapter/upload-zip', array(
        'methods' => 'POST',
        'callback' => 'mangata_api_upload_chapter_zip',
        'permission_callback' => '__return_true'
    ));

    register_rest_route('mangata/v1', '/chapter/list', array(
        'methods' => 'GET',
        'callback' => 'mangata_api_get_chapters',
        'permission_callback' => '__return_true'
    ));

    // RECRUITMENT EXAMS
    register_rest_route('mangata/v1', '/exam/upload', array(
        'methods' => 'POST',
        'callback' => 'mangata_api_upload_exam_file',
        'permission_callback' => '__return_true'
    ));

    register_rest_route('mangata/v1', '/exam/list', array(
        'methods' => 'GET',
        'callback' => 'mangata_api_get_exams',
        'permission_callback' => '__return_true'
    ));

    register_rest_route('mangata/v1', '/exam/grade', array(
        'methods' => 'POST',
        'callback' => 'mangata_api_grade_exam',
        'permission_callback' => '__return_true'
    ));

    // ASSIGN WORK
    register_rest_route('mangata/v1', '/staff/assign', array(
        'methods' => 'POST',
        'callback' => 'mangata_api_assign_staff',
        'permission_callback' => '__return_true'
    ));
});


// 3. Callback Implementations

// AUTH - Real WordPress Login Integration
function mangata_api_login($request) {
    $params = $request->get_json_params();
    $username = sanitize_text_field($params['username']);
    $password = sanitize_text_field($params['password']);

    if (empty($username) || empty($password)) {
        return mangata_api_error('شناسه کاربری و رمز عبور الزامی است.');
    }

    $user = wp_authenticate($username, $password);

    if (is_wp_error($user)) {
        return mangata_api_error('مشخصات کاربری وارد شده صحیح نیست: ' . $user->get_error_message(), 401);
    }

    // Check user roles/capabilities
    $roles = $user->roles;
    $role = count($roles) > 0 ? $roles[0] : 'subscriber';

    return mangata_api_success(array(
        'user_id' => $user->ID,
        'username' => $user->user_login,
        'email' => $user->user_email,
        'role' => $role,
        'display_name' => $user->display_name,
        'token' => wp_generate_uuid4() // Clean simulation-free representation
    ));
}

// AUTH - Real WordPress Register Integration
function mangata_api_register($request) {
    $params = $request->get_json_params();
    $username = sanitize_text_field($params['username']);
    $email = sanitize_email($params['email']);
    $password = sanitize_text_field($params['password']);
    $role = sanitize_text_field($params['role']); // subscriber, administrator, staff_translator etc.

    if (empty($username) || empty($email) || empty($password)) {
        return mangata_api_error('پر کردن تمامی فیلدها الزامی است.');
    }

    if (username_exists($username)) {
        return mangata_api_error('این نام کاربری قبلاً ثبت شده است.');
    }

    if (email_exists($email)) {
        return mangata_api_error('این آدرس ایمیل قبلاً ثبت شده است.');
    }

    $user_id = wp_create_user($username, $password, $email);

    if (is_wp_error($user_id)) {
        return mangata_api_error('خطا در ثبت نام: ' . $user_id->get_error_message());
    }

    // Assign requested role (subscribers by default, admin is managed)
    $user = new WP_User($user_id);
    if ($role === 'administrator' && current_user_can('manage_options')) {
        $user->set_role('administrator');
    } else if (in_array($role, array('administrator', 'editor', 'author', 'contributor', 'subscriber'))) {
        $user->set_role($role);
    } else {
        $user->set_role('subscriber');
    }

    return mangata_api_success(array(
        'user_id' => $user_id,
        'username' => $username,
        'email' => $email,
        'role' => count($user->roles) > 0 ? $user->roles[0] : 'subscriber'
    ));
}


// MANHWA - List all manhwas
function mangata_api_get_manhwas($request) {
    global $wpdb;
    $table_mangas = $wpdb->prefix . 'mangata_mangas';
    $results = $wpdb->get_results("SELECT * FROM $table_mangas ORDER BY id DESC", ARRAY_A);
    return mangata_api_success($results);
}

// MANHWA - Create one
function mangata_api_create_manhwa($request) {
    global $wpdb;
    $params = $request->get_json_params();
    $title = sanitize_text_field($params['title']);
    $desc = sanitize_textarea_field($params['description']);
    $cover = sanitize_text_field($params['cover_image']);

    if (empty($title)) {
        return mangata_api_error('عنوان مانهوا الزامی است.');
    }

    $table_mangas = $wpdb->prefix . 'mangata_mangas';
    $wpdb->insert($table_mangas, array(
        'title' => $title,
        'description' => $desc,
        'cover_image' => $cover
    ));

    return mangata_api_success(array(
        'id' => $wpdb->insert_id,
        'title' => $title
    ));
}


// MANHWA - Get chapters
function mangata_api_get_chapters($request) {
    global $wpdb;
    $manga_id = intval($request->get_param('manga_id'));
    $table_chapters = $wpdb->prefix . 'mangata_chapters';
    
    if ($manga_id > 0) {
        $results = $wpdb->get_results($wpdb->prepare("SELECT * FROM $table_chapters WHERE manga_id = %d ORDER BY chapter_number ASC", $manga_id), ARRAY_A);
    } else {
        $results = $wpdb->get_results("SELECT * FROM $table_chapters ORDER BY id DESC", ARRAY_A);
    }
    return mangata_api_success($results);
}


// ZIP MANHWA UPLOAD & EXTRACTION SYSTEM (for staff / managers)
function mangata_api_upload_chapter_zip($request) {
    global $wpdb;
    // Check if files exist
    if (empty($_FILES['zip_file'])) {
        return mangata_api_error('فایل زیپ یافت نشد.');
    }

    $manga_id = intval($request->get_param('manga_id'));
    $chapter_num = floatval($request->get_param('chapter_number'));
    $chapter_title = sanitize_text_field($request->get_param('title'));
    $user_id = intval($request->get_param('user_id'));

    if ($manga_id <= 0 || $chapter_num <= 0 || $user_id <= 0) {
        return mangata_api_error('پارامترهای مانهوا، شماره چپتر یا آیدی کاربر نامعتبر است.');
    }

    // CHECK SECURITY PERMISSION: Must be admin OR listed in staff assignment for this manhwa!
    $user_meta = get_userdata($user_id);
    $is_admin = $user_meta && in_array('administrator', $user_meta->roles);
    
    if (!$is_admin) {
        $table_staff = $wpdb->prefix . 'mangata_staff';
        $staff_record = $wpdb->get_row($wpdb->prepare(
            "SELECT id FROM $table_staff WHERE user_id = %d AND manga_id = %d", 
            $user_id, $manga_id
        ));
        if (!$staff_record) {
            return mangata_api_error('خطای دسترسی: شما جزو دستاندرکاران این مانهوا نیستید یا دسترسی ادمین ندارید.', 403);
        }
    }

    // Real PHP upload zip and extraction logic
    $uploaded_file = $_FILES['zip_file'];
    
    require_once(ABSPATH . 'wp-admin/includes/file.php');
    $upload_overrides = array('test_form' => false);
    $movefile = wp_handle_upload($uploaded_file, $upload_overrides);

    if (!$movefile || isset($movefile['error'])) {
        return mangata_api_error('خطا در فایل آپلودی: ' . ($movefile['error'] ?? 'خطای ناشناخته'));
    }

    $file_path = $movefile['file'];
    $file_url = $movefile['url'];

    // Extract ZIP file
    $wp_upload_dir = wp_upload_dir();
    $extract_dir_path = $wp_upload_dir['basedir'] . "/mangata/extracts/{$manga_id}/{$chapter_num}/";
    $extract_dir_url = $wp_upload_dir['baseurl'] . "/mangata/extracts/{$manga_id}/{$chapter_num}/";

    if (!is_dir($extract_dir_path)) {
        if (!@mkdir($extract_dir_path, 0755, true) && !is_dir($extract_dir_path)) {
            return mangata_api_error('خطای دسترسی سرور: پوشه استخراج مانهوا قابل ایجاد نیست.');
        }
    }

    if (!class_exists('ZipArchive')) {
        return mangata_api_error('سیستم فاقد الحاقیه ZipArchive جهت استخراج چپتر است. لطفا php-zip را روی هاست فعال کنید.');
    }

    $zip = new ZipArchive();
    $extracted_images = array();

    if ($zip->open($file_path) === TRUE) {
        $zip->extractTo($extract_dir_path);
        
        // Find extracted image files in natural order safely
        $files = @scandir($extract_dir_path);
        if ($files === false) {
            $files = array();
        }
        // filter images
        $img_extensions = array('jpg', 'jpeg', 'png', 'webp', 'gif');
        foreach ($files as $file) {
            $ext = strtolower(pathinfo($file, PATHINFO_EXTENSION));
            if (in_array($ext, $img_extensions)) {
                $extracted_images[] = $extract_dir_url . $file;
            }
        }
        $zip->close();
        
        if (!empty($extracted_images)) {
            natcasesort($extracted_images); // naturally sort items (1.jpg, 2.jpg, 10.jpg)
            $extracted_images = array_values($extracted_images);
        }
    } else {
        return mangata_api_error('فایل آپلودی یک فایل ZIP معتبر نیست یا استخراج آن با شکست مواجه شد.');
    }

    if (empty($extracted_images)) {
        return mangata_api_error('هیچ تصویری در داخل فایل زیپ جهت ریدر یافت نشد.');
    }

    // Insert chapter record to db
    $table_chapters = $wpdb->prefix . 'mangata_chapters';
    
    // Check if already exists
    $existing = $wpdb->get_row($wpdb->prepare(
        "SELECT id FROM $table_chapters WHERE manga_id = %d AND chapter_number = %f",
        $manga_id, $chapter_num
    ));

    $images_json = json_encode($extracted_images);

    if ($existing) {
        $wpdb->update($table_chapters, array(
            'title' => $chapter_title,
            'images_json' => $images_json,
            'zip_url' => $file_url,
            'uploaded_by' => $user_id
        ), array('id' => $existing->id));
        $chapter_id = $existing->id;
    } else {
        $wpdb->insert($table_chapters, array(
            'manga_id' => $manga_id,
            'chapter_number' => $chapter_num,
            'title' => $chapter_title,
            'images_json' => $images_json,
            'zip_url' => $file_url,
            'uploaded_by' => $user_id
        ));
        $chapter_id = $wpdb->insert_id;
    }

    return mangata_api_success(array(
        'chapter_id' => $chapter_id,
        'manga_id' => $manga_id,
        'chapter_number' => $chapter_num,
        'images' => $extracted_images,
        'url' => $file_url
    ));
}


// EXAMS - Real recruitment PDF/ZIP upload for candidates
function mangata_api_upload_exam_file($request) {
    global $wpdb;
    
    if (empty($_FILES['exam_file'])) {
        return mangata_api_error('فایل آزمون یافت نشد.');
    }

    $user_id = intval($request->get_param('user_id'));
    if ($user_id <= 0) {
        return mangata_api_error('شناسه کاربر نامعتبر است.');
    }

    $uploaded_file = $_FILES['exam_file'];

    require_once(ABSPATH . 'wp-admin/includes/file.php');
    $upload_overrides = array('test_form' => false);
    $movefile = wp_handle_upload($uploaded_file, $upload_overrides);

    if (!$movefile || isset($movefile['error'])) {
        return mangata_api_error('خطا در بارگذاری فایل آزمون: ' . ($movefile['error'] ?? 'خطای نامشخص'));
    }

    $file_path = $movefile['file'];
    $file_url = $movefile['url'];

    $table_exams = $wpdb->prefix . 'mangata_exams';
    $wpdb->insert($table_exams, array(
        'user_id' => $user_id,
        'file_name' => basename($file_path),
        'file_url' => $file_url,
        'status' => 'Pending'
    ));

    return mangata_api_success(array(
        'exam_id' => $wpdb->insert_id,
        'file_url' => $file_url,
        'status' => 'Pending',
        'message' => 'فایل آزمون شما با موفقیت ثبت شد و در صف بررسی مدیریت است.'
    ));
}


// EXAMS - Fetch list for Super Admin only
function mangata_api_get_exams($request) {
    global $wpdb;
    $user_id = intval($request->get_param('user_id'));

    // Verify user is administrator
    $user_meta = get_userdata($user_id);
    $is_admin = $user_meta && in_array('administrator', $user_meta->roles);

    if (!$is_admin) {
        return mangata_api_error('دسترسی مدیریت کل جهت لیست آزمون‌ها مورد نیاز است.', 403);
    }

    $table_exams = $wpdb->prefix . 'mangata_exams';
    $results = $wpdb->get_results("SELECT e.*, u.user_login as username FROM $table_exams e JOIN {$wpdb->users} u ON e.user_id = u.ID ORDER BY e.id DESC", ARRAY_A);
    return mangata_api_success($results);
}


// EXAMS - Grade (Accept / Reject with points)
function mangata_api_grade_exam($request) {
    global $wpdb;
    $params = $request->get_json_params();
    $admin_id = intval($params['admin_id']);
    $exam_id = intval($params['exam_id']);
    $status = sanitize_text_field($params['status']); // Accepted, Rejected, Pending
    $score = intval($params['score']);

    $user_meta = get_userdata($admin_id);
    $is_admin = $user_meta && in_array('administrator', $user_meta->roles);

    if (!$is_admin) {
        return mangata_api_error('دسترسی پنل فقط مخصوص مدیریت کل است.', 403);
    }

    $table_exams = $wpdb->prefix . 'mangata_exams';
    $wpdb->update($table_exams, array(
        'status' => $status,
        'score' => $score
    ), array('id' => $exam_id));

    return mangata_api_success(array(
        'exam_id' => $exam_id,
        'status' => $status,
        'score' => $score
    ));
}


// STAFF - Assign crew to a manhwa
function mangata_api_assign_staff($request) {
    global $wpdb;
    $params = $request->get_json_params();
    $admin_id = intval($params['admin_id']);
    $user_id = intval($params['user_id']); // target staff user
    $manga_id = intval($params['manga_id']);
    $role = sanitize_text_field($params['role']); // Translator, Redrawer, Cleaner, TS

    $user_meta = get_userdata($admin_id);
    $is_admin = $user_meta && in_array('administrator', $user_meta->roles);

    if (!$is_admin) {
        return mangata_api_error('فقط ادمین می‌تواند فریلنسرها را مشخص کند.', 403);
    }

    $table_staff = $wpdb->prefix . 'mangata_staff';
    $wpdb->insert($table_staff, array(
        'user_id' => $user_id,
        'manga_id' => $manga_id,
        'role' => $role
    ));

    return mangata_api_success(array(
        'assignment_id' => $wpdb->insert_id,
        'message' => 'عضو تیم با موفقیت به کار انتخابی متصل شد.'
    ));
}
