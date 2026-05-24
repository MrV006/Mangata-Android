<?php
get_header();

global $wpdb;
$table_mangas = $wpdb->prefix . 'mangata_mangas';
$table_chapters = $wpdb->prefix . 'mangata_chapters';
$table_exams = $wpdb->prefix . 'mangata_exams';

// Handle recruitment exam upload form submit in PHP
$upload_msg = '';
if (isset($_POST['submit_exam'])) {
    if (!empty($_FILES['exam_file'])) {
        $username = sanitize_text_field($_POST['applicant_username']);
        $email = sanitize_email($_POST['applicant_email']);
        $password = sanitize_text_field($_POST['applicant_password']);

        // Check if user exists, else register
        $user_id = username_exists($username);
        if (!$user_id) {
            $user_id = email_exists($email);
        }
        if (!$user_id) {
            $user_id = wp_create_user($username, $password, $email);
        }

        if (is_wp_error($user_id)) {
            $upload_msg = '<div class="error-message">خطا در بررسی حساب کاربری: ' . $user_id->get_error_message() . '</div>';
        } else {
            require_once(ABSPATH . 'wp-admin/includes/file.php');
            $upload_overrides = array('test_form' => false);
            $moved = wp_handle_upload($_FILES['exam_file'], $upload_overrides);

            if ($moved && !isset($moved['error'])) {
                $file_path = $moved['file'];
                $file_url = $moved['url'];

                $wpdb->insert($table_exams, array(
                    'user_id' => $user_id,
                    'file_name' => basename($file_path),
                    'file_url' => $file_url,
                    'status' => 'Pending'
                ));
                $upload_msg = '<div class="success-message">فایل آزمون شما با موفقیت آپلود شد و به حساب ' . esc_html($username) . ' متصل شد.</div>';
            } else {
                $upload_msg = '<div class="error-message">خطا در بارگذاری فایل: ' . ($moved['error'] ?? 'خطای ناشناخته') . '</div>';
            }
        }
    } else {
        $upload_msg = '<div class="error-message">لطفاً فایل آزمون را انتخاب کنید.</div>';
    }
}

// Fetch mangas from real db table
$mangas = $wpdb->get_results("SELECT * FROM $table_mangas ORDER BY id DESC", ARRAY_A);
?>

<div class="container">
    <div class="card" style="background: linear-gradient(135deg, #1f1a3a, #121212); border: 1px solid #7c4dff;">
        <h1 style="color:#bb86fc; margin:0 0 10px 0;">خوش آمدید به پنل مانهواخوان مانگاتا (Mangata)</h1>
        <p style="margin:0; font-size:15px; color:#ccc;">سینک شده با دیتابیس `mrvir111_mangata_db` برای تضمین ارتباط با وبسایت mr-v.ir و اپلیکیشن اختصاصی ما.</p>
    </div>

    <!-- Active Manhwas -->
    <h2 id="manhwa" style="color:#ff7597; margin-top:40px; border-bottom: 1px solid #333; padding-bottom:10px;">🎨 مانهوا کارهای فعال تیم</h2>
    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px;">
        <?php if (!empty($mangas)): ?>
            <?php foreach ($mangas as $m): ?>
                <div class="card" style="display: flex; flex-direction: column; justify-content: space-between;">
                    <div>
                        <?php if ($m['cover_image']): ?>
                            <img src="<?php echo esc_url($m['cover_image']); ?>" style="width:100%; height:200px; object-fit:cover; border-radius:4px; margin-bottom:15px;" alt="<?php echo esc_attr($m['title']); ?>">
                        <?php endif; ?>
                        <h3 style="color:#fff; margin:0 0 10px 0;"><?php echo esc_html($m['title']); ?></h3>
                        <p style="color:#aaa; font-size:13px; line-height: 1.5;"><?php echo esc_html($m['description']); ?></p>
                    </div>
                    
                    <!-- Chapter Lists -->
                    <div style="margin-top:20px; border-top: 1px solid #333; padding-top:10px;">
                        <h4 style="color:#bb86fc; margin:0 0 10px 0; font-size:14px;">فصل‌های آپلود شده (فایل زیپ):</h4>
                        <?php
                        $chaps = $wpdb->get_results($wpdb->prepare("SELECT * FROM $table_chapters WHERE manga_id = %d ORDER BY chapter_number ASC", $m['id']), ARRAY_A);
                        if (!empty($chaps)): ?>
                            <ul style="padding:0; margin:0; list-style:none; display:flex; flex-direction:column; gap:5px;">
                                <?php foreach ($chaps as $c): ?>
                                    <li style="background:#252525; padding:8px 12px; border-radius:4px; display:flex; justify-content:space-between; align-items:center;">
                                        <span style="font-size:13px; font-weight:bold;">چپتر <?php echo esc_html($c['chapter_number']); ?> - <?php echo esc_html($c['title']); ?></span>
                                        <a href="?read_chapter=<?php echo esc_attr($c['id']); ?>" class="btn" style="padding:4px 8px; font-size:11px;">خوانش ریدر</a>
                                    </li>
                                <?php endforeach; ?>
                            </ul>
                        <?php else: ?>
                            <span style="font-size:12px; color:#666;">چپتری برای این کار قرار نگرفته است.</span>
                        <?php endif; ?>
                    </div>
                </div>
            <?php endforeach; ?>
        <?php else: ?>
            <div class="card" style="grid-column: 1 / -1; text-align:center;">
                <p style="color:#666; margin:0;">هنوز مانهوایی به همراه دیتابیس ثبت نشده است.</p>
            </div>
        <?php endif; ?>
    </div>

    <!-- Active Reader UI -->
    <?php
    if (isset($_GET['read_chapter'])):
        $chapter_id = intval($_GET['read_chapter']);
        $chap_db = $wpdb->get_row($wpdb->prepare("SELECT * FROM $table_chapters WHERE id = %d", $chapter_id), ARRAY_A);
        if ($chap_db):
            $manga_db = $wpdb->get_row($wpdb->prepare("SELECT title FROM $table_mangas WHERE id = %d", $chap_db['manga_id']), ARRAY_A);
            $imgs = json_decode($chap_db['images_json'], true);
            ?>
            <div id="reader" class="card" style="margin-top: 40px; border-color: #ff7597; background: #000; text-align: center;">
                <div style="display:flex; justify-content: space-between; align-items:center; margin-bottom: 20px;">
                    <h2 style="color:#ff7597; margin:0;"><?php echo esc_html($manga_db['title']); ?> - چپتر <?php echo esc_html($chap_db['chapter_number']); ?></h2>
                    <a href="?" class="btn" style="background:#444;">بستن ریدر X</a>
                </div>
                
                <div style="display:flex; flex-direction:column; gap:0px; max-width:800px; margin: 0 auto; background:#121212; padding:15px; border-radius:8px;">
                    <?php if (!empty($imgs)): ?>
                        <?php foreach ($imgs as $img_url): ?>
                            <img src="<?php echo esc_url($img_url); ?>" style="width:100%; height:auto; display:block; margin:0 auto;" alt="Manga Page">
                        <?php endforeach; ?>
                    <?php else: ?>
                        <p style="color:#888; padding:30px;">تصویر ریدر یافت نشد.</p>
                    <?php endif; ?>
                </div>
            </div>
        <?php endif; ?>
    <?php endif; ?>

    <!-- Recruitment Portal with real exam uploading -->
    <div id="recruitment" class="card" style="margin-top:40px; border-left: 5px solid #6200ee;">
        <h2 style="color:#bb86fc; margin:0 0 10px 0;">🤝 استخدام در تیم ترجمه و طراحی مانگاتا</h2>
        <p style="color:#aaa; font-size:14px; line-height:1.6; margin:0 0 20px 0;">
            با آپلود فایل آزمون یا پر کردن فرم، اطلاعات آزمون شما به صورت کامل و زنده به دیتابیس مشترک وب‌سایت و اپلیکیشن متصل می‌شود. مدیریت کل پنل می‌تواند آزمون شما را تصحیح کرده و نمره دهد.
        </p>

        <?php echo $upload_msg; ?>

        <form action="#recruitment" method="POST" enctype="multipart/form-data" style="max-width: 500px; background: #252525; padding: 20px; border-radius: 8px;">
            <label style="font-weight:bold; font-size:13px; color:#bb86fc;">نام کاربری متقاضی:</label>
            <input type="text" name="applicant_username" placeholder="مثلاً amirreza" required>

            <label style="font-weight:bold; font-size:13px; color:#bb86fc;">ایمیل متقاضی:</label>
            <input type="email" name="applicant_email" placeholder="مثلاً amirreza@example.com" required>

            <label style="font-weight:bold; font-size:13px; color:#bb86fc;">رمز عبور حساب کاربری:</label>
            <input type="password" name="applicant_password" placeholder="رمز عبور شما برای ورود به اپلیکیشن" required>

            <label style="font-weight:bold; font-size:13px; color:#bb86fc; display:block; margin-top:15px;">فایل پاسخ آزمون (PDF, ZIP, PNG):</label>
            <input type="file" name="exam_file" required style="background:transparent; border:none; padding: 0;">

            <button type="submit" name="submit_exam" class="btn" style="width:100%; margin-top:20px;">ارسال فایل آزمون استخدامی</button>
        </form>
    </div>

    <!-- Super Admin Panel inside the theme for checking exams and scoring them -->
    <?php
    $current_user = wp_get_current_user();
    if ($current_user && in_array('administrator', $current_user->roles)): ?>
        <div class="card" style="margin-top:40px; border: 1px dashed #4e342e; background:#1a1515;">
            <h2 style="color: #ff5722;">🛡️ پنل مدیریت کل مانگاتا (مخصوص ادمین برای تصحیح آزمون‌ها)</h2>
            <p style="color:#ccc; font-size:13px;">اطلاعات آزمون‌های بارگذاری شده مستقیماً از جدول `mangata_exams` متصل است.</p>

            <?php
            // Handle Grading
            if (isset($_POST['grade_exam'])) {
                $exam_id = intval($_POST['exam_id']);
                $score = intval($_POST['score']);
                $status = sanitize_text_field($_POST['status']);

                $wpdb->update($table_exams, array(
                    'status' => $status,
                    'score' => $score
                ), array('id' => $exam_id));

                echo '<div class="success-message">آزمون شماره ' . $exam_id . ' با موفقیت به وضعیت ' . esc_html($status) . ' و نمره ' . $score . ' تغییر یافت.</div>';
            }

            $all_exams = $wpdb->get_results("SELECT e.*, u.user_login as username, u.user_email as email FROM $table_exams e JOIN {$wpdb->users} u ON e.user_id = u.ID ORDER BY e.id DESC", ARRAY_A);
            if (!empty($all_exams)): ?>
                <table>
                    <thead>
                        <tr>
                            <th>آیدی کاربر</th>
                            <th>نام کاربری متقاضی</th>
                            <th>ایمیل</th>
                            <th>فایل آزمون</th>
                            <th>وضعیت فعلی</th>
                            <th>نمره</th>
                            <th>عملیات تصحیح آزمون</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($all_exams as $exam): ?>
                            <tr>
                                <td><?php echo esc_html($exam['user_id']); ?></td>
                                <td><?php echo esc_html($exam['username']); ?></td>
                                <td><?php echo esc_html($exam['email']); ?></td>
                                <td><a href="<?php echo esc_url($exam['file_url']); ?>" target="_blank" style="color:#ff7597; text-decoration:none; font-weight:bold;">دانلود/نمایش فایل</a></td>
                                <td>
                                    <span style="padding:4px 8px; border-radius:3px; background: <?php echo $exam['status'] === 'Accepted' ? '#1b5e20' : ($exam['status'] === 'Rejected' ? '#b71c1c' : '#bd9601'); ?>;">
                                        <?php echo esc_html($exam['status']); ?>
                                    </span>
                                </td>
                                <td><?php echo $exam['score'] !== null ? esc_html($exam['score']) . ' از 100' : 'هنوز داده نشده'; ?></td>
                                <td>
                                    <form action="" method="POST" style="display:flex; gap:10px; margin:0; padding:0; background:transparent; border:none; max-width:none;">
                                        <input type="hidden" name="exam_id" value="<?php echo esc_attr($exam['id']); ?>">
                                        <input type="number" name="score" placeholder="نمره (0-100)" min="0" max="100" required style="width:100px; margin:0;">
                                        <select name="status" style="width:150px; margin:0;">
                                            <option value="Accepted">تایید شد (قبولی)</option>
                                            <option value="Rejected">رد شد</option>
                                            <option value="Pending">در انتظار بررسی</option>
                                        </select>
                                        <button type="submit" name="grade_exam" class="btn" style="background:#ff5722; padding:5px 12px;">ثبت نمره</button>
                                    </form>
                                </td>
                            </tr>
                        <?php endforeach; ?>
                    </tbody>
                </table>
            <?php else: ?>
                <p style="color:#888;">هیچ آزمونی به دیتابیس ارسال نشده است.</p>
            <?php endif; ?>
        </div>
    <?php endif; ?>
</div>

<?php
get_footer();
