<?php
/**
 * Functions of Mangata WordPress Theme
 */

function mangata_theme_setup() {
    // Add support for Featured Images
    add_theme_support( 'post-thumbnails' );
    // Automatic title tag support
    add_theme_support( 'title-tag' );
}
add_action( 'after_setup_theme', 'mangata_theme_setup' );

// Enqueue styles
function mangata_enqueue_styles() {
    wp_enqueue_style( 'mangata-main-style', get_stylesheet_uri() );
}
add_action( 'wp_enqueue_scripts', 'mangata_enqueue_styles' );

/**
 * Register Main Custom Post Type: Manga
 */
function mangata_register_manga_cpt() {
    $labels = array(
        'name'                  => 'مانهوا و مانگاها',
        'singular_name'         => 'مانگا',
        'menu_name'             => 'مانگا و مانهوا M',
        'add_new'               => 'افزودن اثر جدید',
        'all_items'             => 'گالری آثار',
        'add_new_item'          => 'افزودن عنوان جدید',
        'edit_item'             => 'ویرایش عنوان مانهوا',
        'new_item'              => 'عنوان جدید',
        'view_item'             => 'مشاهده در سایت',
        'search_items'          => 'جستجوی کارها',
        'not_found'             => 'اثری پیدا نشد',
    );

    $args = array(
        'labels'             => $labels,
        'public'             => true,
        'has_archive'        => true,
        'menu_icon'          => 'dashicons-book-alt',
        'supports'           => array( 'title', 'editor', 'thumbnail', 'excerpt', 'custom-fields' ),
        'show_in_rest'       => true, // Required for REST API query integration
        'rewrite'            => array('slug' => 'manga'),
    );

    register_post_type( 'manga', $args );
}
add_action( 'init', 'mangata_register_manga_cpt' );

/**
 * Register Custom Meta Box for Manga Details
 */
function mangata_add_manga_metabox() {
    add_meta_box(
        'mangata_manga_details', // Unique ID
        'جزئیات تخصصی مانهوا',    // Box title
        'mangata_manga_metabox_html', // Content callback
        'manga',                  // Post type
        'normal',                 // Context
        'high'                    // Priority
    );
}
add_action( 'add_meta_boxes', 'mangata_add_manga_metabox' );

function mangata_admin_scripts() {
    global $post_type;
    if( 'manga' == $post_type ) {
        wp_enqueue_media();
    }
}
add_action( 'admin_enqueue_scripts', 'mangata_admin_scripts' );

function mangata_manga_metabox_html( $post ) {
    // Nonce field to validate form request came from current site
    wp_nonce_field( basename( __FILE__ ), 'mangata_manga_nonce' );

    // Get current values
    $title_en = get_post_meta( $post->ID, '_manga_title_en', true );
    $type = get_post_meta( $post->ID, '_manga_type', true );
    $status = get_post_meta( $post->ID, '_manga_status', true );
    $rating = get_post_meta( $post->ID, '_manga_rating', true );
    $genres = get_post_meta( $post->ID, '_manga_genres', true );
    $author = get_post_meta( $post->ID, '_manga_author', true );
    $translator = get_post_meta( $post->ID, '_manga_translator_team', true );
    $chapters = get_post_meta( $post->ID, '_manga_chapters_count', true );
    $is_premium = get_post_meta( $post->ID, '_manga_is_premium', true );
    $pages_json = get_post_meta( $post->ID, '_manga_pages_json', true );

    // Build the form
    ?>
    <style>
        .mangata-meta-row { margin-bottom: 12px; }
        .mangata-meta-row label { display: inline-block; width: 140px; font-weight: bold; }
        .mangata-meta-row input[type="text"], .mangata-meta-row input[type="number"], .mangata-meta-row select { width: 50%; }
        .mangata-meta-row textarea { width: 100%; height: 80px; direction: ltr; }
    </style>
    
    <div class="mangata-meta-row">
        <label>عنوان انگلیسی (Title EN)</label>
        <input type="text" name="manga_title_en" value="<?php echo esc_attr( $title_en ); ?>" />
    </div>
    <div class="mangata-meta-row">
        <label>نوع (Type)</label>
        <select name="manga_type">
            <option value="مانهوا" <?php selected( $type, 'مانهوا' ); ?>>مانهوا</option>
            <option value="مانگا" <?php selected( $type, 'مانگا' ); ?>>مانگا</option>
            <option value="مانهوا" <?php selected( $type, 'مانهوا' ); ?>>مانهوا</option>
        </select>
    </div>
    <div class="mangata-meta-row">
        <label>وضعیت (Status)</label>
        <select name="manga_status">
            <option value="در حال انتشار" <?php selected( $status, 'در حال انتشار' ); ?>>در حال انتشار</option>
            <option value="پایان یافته" <?php selected( $status, 'پایان یافته' ); ?>>پایان یافته</option>
        </select>
    </div>
    <div class="mangata-meta-row">
        <label>امتیاز (Rating)</label>
        <input type="number" step="0.1" max="10" name="manga_rating" value="<?php echo esc_attr( $rating ); ?>" />
    </div>
    <div class="mangata-meta-row">
        <label>ژانرها (Genres)</label>
        <input type="text" name="manga_genres" value="<?php echo esc_attr( $genres ); ?>" placeholder="اکشن, فانتزی, ..." />
    </div>
    <div class="mangata-meta-row">
        <label>نویسنده/طراح (Author)</label>
        <input type="text" name="manga_author" value="<?php echo esc_attr( $author ); ?>" />
    </div>
    <div class="mangata-meta-row">
        <label>تیم ترجمه (Translator Team)</label>
        <input type="text" name="manga_translator_team" value="<?php echo esc_attr( $translator ); ?>" />
    </div>
    <div class="mangata-meta-row">
        <label>تعداد فصل‌ها (Chapters Count)</label>
        <input type="number" name="manga_chapters_count" value="<?php echo esc_attr( $chapters ); ?>" />
    </div>
    <div class="mangata-meta-row">
        <label>عنوان پولی (Premium?)</label>
        <input type="checkbox" name="manga_is_premium" value="1" <?php checked( $is_premium, '1' ); ?> /> بله، خرید سکه‌ای/پولی است
    </div>
    <div class="mangata-meta-row">
        <label>لینک صفحات چپتر (JSON Array)</label><br/>
        <small>آدرس تصاویر فصول را داخل یک آرایه جیسون وارد کنید. (مانند: ["url1", "url2"]). اگر هاست دانلود ندارید، می‌توانید با زدن دکمه زیر مستقیما در وردپرس آپلود کنید.</small><br/>
        <textarea id="mangata_pages_json_id" name="manga_pages_json" placeholder='["https://example.com/page1.jpg", "https://example.com/page2.jpg"]'><?php echo esc_textarea( $pages_json ); ?></textarea>
        <br/>
        <button type="button" class="button button-primary" id="mangata_upload_images_btn">آپلود/انتخاب عکس‌ها (ایجاد خودکار لیست)</button>
    </div>
    <p><em>نکته: عکس کاور و بنر را از طریق باکس "تصویر شاخص" (Featured Image) در سایدبار وردپرس آپلود کنید. تصویر شاخص به عنوان کاور و بنر به صورت اتوماتیک در اپلیکیشن تنظیم می‌شود.</em></p>
    
    <script>
    jQuery(document).ready(function($){
        var mediaUploader;
        $('#mangata_upload_images_btn').click(function(e) {
            e.preventDefault();
            if (mediaUploader) {
                mediaUploader.open();
                return;
            }
            mediaUploader = wp.media.frames.file_frame = wp.media({
                title: 'انتخاب یا آپلود صفحات مانهوا',
                button: { text: 'افزودن به لیست' },
                multiple: true
            });
            mediaUploader.on('select', function() {
                var selection = mediaUploader.state().get('selection');
                var urls = [];
                selection.map(function(attachment) {
                    var attachmentJson = attachment.toJSON();
                    urls.push(attachmentJson.url);
                });
                
                // Merge with existing if valid JSON
                var existing = $('#mangata_pages_json_id').val();
                var currentUrls = [];
                try {
                    if (existing.trim() !== '') {
                        currentUrls = JSON.parse(existing);
                    }
                } catch(err) {
                    console.log("Existing data is not valid JSON.", err);
                }
                
                var mergedUrls = currentUrls.concat(urls);
                $('#mangata_pages_json_id').val(JSON.stringify(mergedUrls, null, 2));
            });
            mediaUploader.open();
        });
    });
    </script>
    <?php
}

function mangata_save_manga_meta( $post_id ) {
    // Check if simple validation passed
    if ( ! isset( $_POST['mangata_manga_nonce'] ) || ! wp_verify_nonce( $_POST['mangata_manga_nonce'], basename( __FILE__ ) ) ) {
        return $post_id;
    }
    // Check autosave
    if ( defined( 'DOING_AUTOSAVE' ) && DOING_AUTOSAVE ) {
        return $post_id;
    }
    // Check user permissions
    if ( ! current_user_can( 'edit_post', $post_id ) ) {
        return $post_id;
    }

    // Array of fields to save
    $fields = array(
        'manga_title_en',
        'manga_type',
        'manga_status',
        'manga_rating',
        'manga_genres',
        'manga_author',
        'manga_translator_team',
        'manga_chapters_count',
        'manga_pages_json'
    );

    foreach ( $fields as $field ) {
        if ( isset( $_POST[$field] ) ) {
            // Using sanitize_text_field but textarea for json might need different sanitization
            if ($field === 'manga_pages_json') {
                update_post_meta( $post_id, '_' . $field, wp_unslash($_POST[$field]) );
            } else {
                update_post_meta( $post_id, '_' . $field, sanitize_text_field( $_POST[$field] ) );
            }
        }
    }

    // Checkbox mapping
    $is_premium = isset( $_POST['manga_is_premium'] ) ? '1' : '0';
    update_post_meta( $post_id, '_manga_is_premium', $is_premium );
}
add_action( 'save_post', 'mangata_save_manga_meta' );

/**
 * REST API Endpoint Integration for Android App
 * Enables the Android App to fetch real mangas and chapters from your site database (mr-v.ir)
 */
function register_mangata_rest_routes() {
    register_rest_route( 'mangata/v1', '/mangas', array(
        'methods'             => 'GET',
        'callback'            => 'get_mangata_dynamic_manga_list',
        'permission_callback' => '__return_true', // Public access for users reading on client
    ) );

    register_rest_route( 'mangata/v1', '/login', array(
        'methods'             => 'POST',
        'callback'            => 'mangata_rest_login',
        'permission_callback' => '__return_true',
    ) );

    register_rest_route( 'mangata/v1', '/register', array(
        'methods'             => 'POST',
        'callback'            => 'mangata_rest_register',
        'permission_callback' => '__return_true',
    ) );

    register_rest_route( 'mangata/v1', '/sync', array(
        'methods'             => 'POST',
        'callback'            => 'mangata_rest_sync',
        'permission_callback' => '__return_true',
    ) );

    register_rest_route( 'mangata/v1', '/purchase', array(
        'methods'             => 'POST',
        'callback'            => 'mangata_rest_purchase',
        'permission_callback' => '__return_true',
    ) );

    register_rest_route( 'mangata/v1', '/admin/get-settings', array(
        'methods'             => 'GET',
        'callback'            => 'mangata_get_admin_settings',
        'permission_callback' => '__return_true',
    ) );

    register_rest_route( 'mangata/v1', '/admin/update-settings', array(
        'methods'             => 'POST',
        'callback'            => 'mangata_update_admin_settings',
        'permission_callback' => '__return_true',
    ) );

    register_rest_route( 'mangata/v1', '/admin/get-users', array(
        'methods'             => 'GET',
        'callback'            => 'mangata_get_admin_users',
        'permission_callback' => '__return_true',
    ) );

    register_rest_route( 'mangata/v1', '/admin/update-user', array(
        'methods'             => 'POST',
        'callback'            => 'mangata_update_admin_user',
        'permission_callback' => '__return_true',
    ) );

    register_rest_route( 'mangata/v1', '/admin/get-recruitments', array(
        'methods'             => 'GET',
        'callback'            => 'mangata_get_admin_recruitments',
        'permission_callback' => '__return_true',
    ) );

    register_rest_route( 'mangata/v1', '/admin/add-recruitment', array(
        'methods'             => 'POST',
        'callback'            => 'mangata_add_admin_recruitment',
        'permission_callback' => '__return_true',
    ) );

    register_rest_route( 'mangata/v1', '/admin/update-recruitment', array(
        'methods'             => 'POST',
        'callback'            => 'mangata_update_admin_recruitment',
        'permission_callback' => '__return_true',
    ) );

    register_rest_route( 'mangata/v1', '/admin/save-manga', array(
        'methods'             => 'POST',
        'callback'            => 'mangata_save_manga',
        'permission_callback' => '__return_true',
    ) );

    register_rest_route( 'mangata/v1', '/admin/delete-manga', array(
        'methods'             => 'POST',
        'callback'            => 'mangata_delete_manga',
        'permission_callback' => '__return_true',
    ) );
}
add_action( 'rest_api_init', 'register_mangata_rest_routes' );

function get_mangata_dynamic_manga_list() {
    $args = array(
        'post_type'      => 'manga',
        'posts_per_page' => -1,
        'post_status'    => 'publish'
    );

    $query = new WP_Query($args);
    $mangas = array();

    if ($query->have_posts()) {
        while ($query->have_posts()) {
            $query->the_post();
            $id = get_the_ID();
            
            // Collect metadata fields or fallback to realistic mockups
            $type = get_post_meta($id, '_manga_type', true) ?: 'مانهوا';
            $rating = get_post_meta($id, '_manga_rating', true) ?: '4.9';
            $status = get_post_meta($id, '_manga_status', true) ?: 'در حال انتشار';
            $genres = get_post_meta($id, '_manga_genres', true) ?: 'اکشن, ماجراجویی, فانتزی';
            $author = get_post_meta($id, '_manga_author', true) ?: 'نویسنده مانهوا';
            $trans_team = get_post_meta($id, '_manga_translator_team', true) ?: 'کادر مانگاتا';
            $chapters_count = get_post_meta($id, '_manga_chapters_count', true) ?: '150';
            $is_premium = get_post_meta($id, '_manga_is_premium', true) ?: 'false';
            
            // Get post thumbnail or a default image
            $cover_url = get_the_post_thumbnail_url($id, 'medium');
            if (empty($cover_url)) {
                $cover_url = get_post_meta($id, '_manga_cover_url', true);
            }
            if (empty($cover_url)) {
                $cover_url = 'https://picsum.photos/id/1025/400/600';
            }

            $banner_url = get_the_post_thumbnail_url($id, 'large');
            if (empty($banner_url)) {
                $banner_url = get_post_meta($id, '_manga_banner_url', true);
            }
            if (empty($banner_url)) {
                $banner_url = 'https://picsum.photos/id/1025/1200/600';
            }

            // Custom serialized pages representing comic cells
            $pages_raw = get_post_meta($id, '_manga_pages_json', true) ?: '["https://picsum.photos/id/1015/800/1200", "https://picsum.photos/id/1016/800/1200"]';

            $mangas[] = array(
                'id'             => $id,
                'titleFa'        => get_the_title(),
                'titleEn'        => get_post_meta($id, '_manga_title_en', true) ?: 'Solo Leveling',
                'descriptionFa'  => get_the_content(),
                'type'           => $type,
                'coverUrl'       => $cover_url,
                'bannerUrl'      => $banner_url,
                'rating'         => floatval($rating),
                'status'         => $status,
                'genres'         => $genres,
                'author'         => $author,
                'translatorTeam' => $trans_team,
                'chaptersCount'  => intval($chapters_count),
                'isPremium'      => ($is_premium === 'true' || $is_premium === '1'),
                'reviewsJson'    => '[]',
                'pagesJson'      => $pages_raw
            );
        }
        wp_reset_postdata();
    }

    // Fallback seed inside server so client never remains completely empty
    if (empty($mangas)) {
        return array(
            array(
                'id'             => 1001,
                'titleFa'        => 'پادشاه مبارزان (مانهوا اختصاصی)',
                'titleEn'        => 'The King of Fighters',
                'descriptionFa'  => 'دنیایی فانتزی که در آن ماجراجویان سطح قوی‌ترین رزمی کاران تاریخ را به چالش می‌کشند.',
                'type'           => 'مانهوا',
                'coverUrl'       => 'https://picsum.photos/id/1027/400/600',
                'bannerUrl'      => 'https://picsum.photos/id/1027/1200/600',
                'rating'         => 4.9,
                'status'         => 'در حال انتشار',
                'genres'         => 'اکشن, فانتزی, حماسی',
                'author'         => 'کره‌ای تبار',
                'translatorTeam' => 'دپارتمان وب‌تون مانگاتا',
                'chaptersCount'  => 230,
                'isPremium'      => true,
                'reviewsJson'    => '[]',
                'pagesJson'      => '["https://picsum.photos/id/1028/800/1200", "https://picsum.photos/id/1029/800/1200"]'
            )
        );
    }

    return $mangas;
}

/**
 * Handle user Login via REST API
 */
function mangata_rest_login($request) {
    $params = $request->get_json_params();
    $username = isset($params['username']) ? sanitize_text_field($params['username']) : '';
    $password = isset($params['password']) ? $params['password'] : '';

    if (empty($username) || empty($password)) {
        return array('error' => 'لطفا نام کاربری و رمز عبور را وارد کنید.');
    }

    $user = get_user_by('login', $username);

    if (!$user) {
        $user = get_user_by('email', $username);
    }

    if (!$user || !wp_check_password($password, $user->user_pass, $user->ID)) {
        return array('error' => 'نام کاربری یا رمز عبور اشتباه است.');
    }

    $user_id = $user->ID;

    // Trigger promotion check for "Mr.V"
    if (strtolower($username) === 'mr.v') {
        update_user_meta($user_id, 'mangata_role', 'SUPER_ADMIN');
        update_user_meta($user_id, 'mangata_sub_role', 'مدیر کل');
        $wp_user = new WP_User($user_id);
        $wp_user->set_role('administrator');
    }

    $role = get_user_meta($user_id, 'mangata_role', true) ?: 'NORMAL_USER';
    $sub_role = get_user_meta($user_id, 'mangata_sub_role', true) ?: 'کاربر عادی';
    $wallet_rial = intval(get_user_meta($user_id, 'mangata_wallet_rial', true) ?: 0);
    $wallet_gift = intval(get_user_meta($user_id, 'mangata_wallet_gift_chapters', true) ?: 0);
    $purchased_json = get_user_meta($user_id, 'mangata_purchased_chapters_json', true) ?: '[]';
    $bookmarks_json = get_user_meta($user_id, 'mangata_bookmarks_json', true) ?: '[]';
    $read_history_json = get_user_meta($user_id, 'mangata_read_history_json', true) ?: '[]';

    return array(
        'id'                  => $user_id,
        'username'            => $user->user_login,
        'displayName'         => $user->display_name ?: $user->user_login,
        'role'                => $role,
        'subRole'             => $sub_role,
        'walletRial'          => $wallet_rial,
        'walletGiftChapters'  => $wallet_gift,
        'purchasedChaptersJson' => $purchased_json,
        'bookmarksJson'       => $bookmarks_json,
        'readHistoryJson'     => $read_history_json,
        'error'               => null
    );
}

/**
 * Handle user Registration via REST API
 */
function mangata_rest_register($request) {
    $params = $request->get_json_params();
    $username = isset($params['username']) ? sanitize_text_field($params['username']) : '';
    $displayName = isset($params['displayName']) ? sanitize_text_field($params['displayName']) : '';
    $password = isset($params['password']) ? $params['password'] : '';

    if (empty($username) || empty($password)) {
        return array('error' => 'نام کاربری و رمز عبور الزامی است.');
    }

    if (username_exists($username) || email_exists($username . '@mr-v.ir')) {
        return array('error' => 'این نام کاربری از قبل ثبت شده است.');
    }

    $user_id = wp_create_user($username, $password, $username . '@mr-v.ir');

    if (is_wp_error($user_id)) {
        return array('error' => $user_id->get_error_message());
    }

    if (!empty($displayName)) {
        wp_update_user(array('ID' => $user_id, 'display_name' => $displayName));
    }

    // Role assignment logic:
    // First person to register with "Mr.V" becomes SUPER_ADMIN (مدیر کل) of WP and entire suite
    $is_mrv = (strtolower($username) === 'mr.v');
    $role = $is_mrv ? 'SUPER_ADMIN' : 'NORMAL_USER';
    $sub_role = $is_mrv ? 'مدیر کل' : 'کاربر عادی';

    update_user_meta($user_id, 'mangata_role', $role);
    update_user_meta($user_id, 'mangata_sub_role', $sub_role);
    update_user_meta($user_id, 'mangata_wallet_rial', 0);
    update_user_meta($user_id, 'mangata_wallet_gift_chapters', 0);
    update_user_meta($user_id, 'mangata_purchased_chapters_json', '[]');
    update_user_meta($user_id, 'mangata_bookmarks_json', '[]');
    update_user_meta($user_id, 'mangata_read_history_json', '[]');

    if ($is_mrv) {
        $wp_user = new WP_User($user_id);
        $wp_user->set_role('administrator');
    }

    $final_user = get_userdata($user_id);

    return array(
        'id'                  => $user_id,
        'username'            => $final_user->user_login,
        'displayName'         => $final_user->display_name ?: $final_user->user_login,
        'role'                => $role,
        'subRole'             => $sub_role,
        'walletRial'          => 0,
        'walletGiftChapters'  => 0,
        'purchasedChaptersJson' => '[]',
        'bookmarksJson'       => '[]',
        'readHistoryJson'     => '[]',
        'error'               => null
    );
}

/**
 * Sync user balance + dynamic local purchases with WordPress
 */
function mangata_rest_sync($request) {
    $params = $request->get_json_params();
    $username = isset($params['username']) ? sanitize_text_field($params['username']) : '';
    $wallet_rial_client = isset($params['walletRial']) ? intval($params['walletRial']) : 0;
    $wallet_gift_client = isset($params['walletGiftChapters']) ? intval($params['walletGiftChapters']) : 0;
    $purchased_client_json = isset($params['purchasedChaptersJson']) ? $params['purchasedChaptersJson'] : '[]';

    if (empty($username)) {
        return array('error' => 'کاربر مشخص نشده است.');
    }

    $user = get_user_by('login', $username);
    if (!$user) {
        return array('error' => 'نام کاربری یافت نشد.');
    }

    $user_id = $user->ID;

    // Check custom role of Mr.V to guarantee matching superadmin settings
    if (strtolower($username) === 'mr.v') {
        update_user_meta($user_id, 'mangata_role', 'SUPER_ADMIN');
        update_user_meta($user_id, 'mangata_sub_role', 'مدیر کل');
        $wp_user = new WP_User($user_id);
        $wp_user->set_role('administrator');
    }

    // Merge wallets: Server balance & Android balance must be synced.
    // In dual payment options, we take the highest balance in either side or sync
    $wallet_server = intval(get_user_meta($user_id, 'mangata_wallet_rial', true) ?: 0);
    $wallet_gift_server = intval(get_user_meta($user_id, 'mangata_wallet_gift_chapters', true) ?: 0);
    
    $final_wallet = max($wallet_server, $wallet_rial_client);
    $final_gift = max($wallet_gift_server, $wallet_gift_client);

    update_user_meta($user_id, 'mangata_wallet_rial', $final_wallet);
    update_user_meta($user_id, 'mangata_wallet_gift_chapters', $final_gift);

    // Merge list of bought chapters safely
    $srv_purchased_raw = get_user_meta($user_id, 'mangata_purchased_chapters_json', true) ?: '[]';
    $srv_purchased = json_decode($srv_purchased_raw, true) ?: array();
    $clt_purchased = json_decode($purchased_client_json, true) ?: array();

    // Union merge
    $merged = array();
    foreach ($srv_purchased as $item) {
        $key = $item['mangaId'] . '-' . $item['chapterNumber'];
        $merged[$key] = $item;
    }
    foreach ($clt_purchased as $item) {
        $key = $item['mangaId'] . '-' . $item['chapterNumber'];
        $merged[$key] = $item;
    }

    $final_purchased_list = array_values($merged);
    $final_purchased_json = json_encode($final_purchased_list);
    update_user_meta($user_id, 'mangata_purchased_chapters_json', $final_purchased_json);

    // Merge Bookmarks
    $srv_bookmarks_raw = get_user_meta($user_id, 'mangata_bookmarks_json', true) ?: '[]';
    $clt_bookmarks_raw = isset($params['bookmarksJson']) ? $params['bookmarksJson'] : '[]';
    $srv_bookmarks = json_decode($srv_bookmarks_raw, true) ?: array();
    $clt_bookmarks = json_decode($clt_bookmarks_raw, true) ?: array();

    $merged_bookmarks = array();
    foreach ($srv_bookmarks as $b) {
        if (isset($b['mangaId'])) {
            $merged_bookmarks[$b['mangaId']] = $b;
        }
    }
    foreach ($clt_bookmarks as $b) {
        if (isset($b['mangaId'])) {
            if (!isset($merged_bookmarks[$b['mangaId']]) || 
                (isset($b['bookmarkedAt']) && isset($merged_bookmarks[$b['mangaId']]['bookmarkedAt']) && $b['bookmarkedAt'] > $merged_bookmarks[$b['mangaId']]['bookmarkedAt'])) {
                $merged_bookmarks[$b['mangaId']] = $b;
            }
        }
    }
    $final_bookmarks_json = json_encode(array_values($merged_bookmarks));
    update_user_meta($user_id, 'mangata_bookmarks_json', $final_bookmarks_json);

    // Merge Read History
    $srv_history_raw = get_user_meta($user_id, 'mangata_read_history_json', true) ?: '[]';
    $clt_history_raw = isset($params['readHistoryJson']) ? $params['readHistoryJson'] : '[]';
    $srv_history = json_decode($srv_history_raw, true) ?: array();
    $clt_history = json_decode($clt_history_raw, true) ?: array();

    $merged_history = array();
    foreach ($srv_history as $h) {
        if (isset($h['mangaId'])) {
            $merged_history[$h['mangaId']] = $h;
        }
    }
    foreach ($clt_history as $h) {
        if (isset($h['mangaId'])) {
            if (!isset($merged_history[$h['mangaId']]) ||
                (isset($h['lastReadTimestamp']) && isset($merged_history[$h['mangaId']]['lastReadTimestamp']) && $h['lastReadTimestamp'] > $merged_history[$h['mangaId']]['lastReadTimestamp'])) {
                $merged_history[$h['mangaId']] = $h;
            }
        }
    }
    $final_history_json = json_encode(array_values($merged_history));
    update_user_meta($user_id, 'mangata_read_history_json', $final_history_json);

    $role = get_user_meta($user_id, 'mangata_role', true) ?: 'NORMAL_USER';
    $sub_role = get_user_meta($user_id, 'mangata_sub_role', true) ?: 'کاربر عادی';

    return array(
        'id'                  => $user_id,
        'username'            => $user->user_login,
        'displayName'         => $user->display_name ?: $user->user_login,
        'role'                => $role,
        'subRole'             => $sub_role,
        'walletRial'          => $final_wallet,
        'walletGiftChapters'  => $final_gift,
        'purchasedChaptersJson' => $final_purchased_json,
        'bookmarksJson'       => $final_bookmarks_json,
        'readHistoryJson'     => $final_history_json,
        'error'               => null
    );
}

/**
 * Sync active chapter purchases
 */
function mangata_rest_purchase($request) {
    $params = $request->get_json_params();
    $userId = isset($params['userId']) ? intval($params['userId']) : 0;
    $mangaId = isset($params['mangaId']) ? intval($params['mangaId']) : 0;
    $chapterNumber = isset($params['chapterNumber']) ? intval($params['chapterNumber']) : 0;
    $price = isset($params['price']) ? intval($params['price']) : 0;
    $isGiftUse = isset($params['isGiftUse']) ? (bool)$params['isGiftUse'] : false;

    if ($userId <= 0) {
        return array('success' => false, 'errorMessage' => 'شناسه کاربر نامعتبر است.');
    }

    $user = get_userdata($userId);
    if (!$user) {
        return array('success' => false, 'errorMessage' => 'کاربر یافت نشد.');
    }

    $wallet_rial = intval(get_user_meta($userId, 'mangata_wallet_rial', true) ?: 0);
    $wallet_gift = intval(get_user_meta($userId, 'mangata_wallet_gift_chapters', true) ?: 0);
    $purchased_raw = get_user_meta($userId, 'mangata_purchased_chapters_json', true) ?: '[]';
    $purchased = json_decode($purchased_raw, true) ?: array();

    // Check if duplicate purchase
    $already = false;
    foreach ($purchased as $item) {
        if ($item['mangaId'] == $mangaId && $item['chapterNumber'] == $chapterNumber) {
            $already = true;
            break;
        }
    }

    if ($already) {
        return array(
            'success'            => true,
            'walletRial'         => $wallet_rial,
            'walletGiftChapters' => $wallet_gift,
            'purchasedChaptersJson' => $purchased_raw,
            'errorMessage'       => null
        );
    }

    if ($isGiftUse) {
        if ($wallet_gift < 1) {
            return array('success' => false, 'errorMessage' => 'اعتبار چپتر هدیه کافی نیست.');
        }
        $wallet_gift--;
    } else {
        if ($wallet_rial < $price) {
            return array('success' => false, 'errorMessage' => 'مانده ریالی کیف پول کافی نیست.');
        }
        $wallet_rial -= $price;
    }

    $purchased[] = array(
        'id'            => count($purchased) + 1,
        'userId'        => $userId,
        'mangaId'       => $mangaId,
        'chapterNumber' => $chapterNumber,
        'costRial'      => $isGiftUse ? 0 : $price
    );

    $new_purchased_raw = json_encode($purchased);

    update_user_meta($userId, 'mangata_wallet_rial', $wallet_rial);
    update_user_meta($userId, 'mangata_wallet_gift_chapters', $wallet_gift);
    update_user_meta($userId, 'mangata_purchased_chapters_json', $new_purchased_raw);

    return array(
        'success'               => true,
        'walletRial'            => $wallet_rial,
        'walletGiftChapters'    => $wallet_gift,
        'purchasedChaptersJson' => $new_purchased_raw,
        'errorMessage'          => null
    );
}

/**
 * Catch Front-end Form Submissions for Login, Register, and Add Manga
 */
function mangata_handle_frontend_actions() {
    if ( isset($_POST['mangata_web_action']) ) {
        $action = sanitize_text_field($_POST['mangata_web_action']);
        
        if ( $action === 'login' ) {
            $username = isset($_POST['username']) ? sanitize_text_field($_POST['username']) : '';
            $password = isset($_POST['password']) ? $_POST['password'] : '';
            
            if (empty($username) || empty($password)) {
                wp_redirect(add_query_arg('mangata_error', urlencode('لطفا نام کاربری و رمز عبور را وارد کنید.'), home_url('/')));
                exit;
            }
            
            $creds = array(
                'user_login'    => $username,
                'user_password' => $password,
                'remember'      => true
            );
            
            $user = wp_signon($creds, false);
            
            if (is_wp_error($user)) {
                wp_redirect(add_query_arg('mangata_error', urlencode($user->get_error_message()), home_url('/')));
                exit;
            }
            
            // Promote Mr.V automatically to administrator / SUPER_ADMIN if custom meta is not set
            if (strtolower($username) === 'mr.v') {
                update_user_meta($user->ID, 'mangata_role', 'SUPER_ADMIN');
                update_user_meta($user->ID, 'mangata_sub_role', 'مدیر کل');
                $wp_user = new WP_User($user->ID);
                $wp_user->set_role('administrator');
            }
            
            wp_redirect(add_query_arg('mangata_success', urlencode('با موفقیت وارد شدید!'), home_url('/')));
            exit;
        }
        
        if ( $action === 'register' ) {
            $username = isset($_POST['username']) ? sanitize_text_field($_POST['username']) : '';
            $display_name = isset($_POST['display_name']) ? sanitize_text_field($_POST['display_name']) : '';
            $password = isset($_POST['password']) ? $_POST['password'] : '';
            
            if (empty($username) || empty($password)) {
                wp_redirect(add_query_arg('mangata_error', urlencode('نام کاربری و رمز عبور الزامی است.'), home_url('/')));
                exit;
            }
            
            if (username_exists($username) || email_exists($username . '@mr-v.ir')) {
                wp_redirect(add_query_arg('mangata_error', urlencode('این نام کاربری از قبل ثبت شده است.'), home_url('/')));
                exit;
            }
            
            $user_id = wp_create_user($username, $password, $username . '@mr-v.ir');
            
            if (is_wp_error($user_id)) {
                wp_redirect(add_query_arg('mangata_error', urlencode($user_id->get_error_message()), home_url('/')));
                exit;
            }
            
            if (!empty($display_name)) {
                wp_update_user(array('ID' => $user_id, 'display_name' => $display_name));
            }
            
            $is_mrv = (strtolower($username) === 'mr.v');
            $role = $is_mrv ? 'SUPER_ADMIN' : 'NORMAL_USER';
            $sub_role = $is_mrv ? 'مدیر کل' : 'کاربر عادی';
            
            update_user_meta($user_id, 'mangata_role', $role);
            update_user_meta($user_id, 'mangata_sub_role', $sub_role);
            update_user_meta($user_id, 'mangata_wallet_rial', 150000); // 150,000 Rials signup bonus for user testing
            update_user_meta($user_id, 'mangata_wallet_gift_chapters', 5); // 5 gift chapters signup bonus
            update_user_meta($user_id, 'mangata_purchased_chapters_json', '[]');
            
            if ($is_mrv) {
                $wp_user = new WP_User($user_id);
                $wp_user->set_role('administrator');
            }
            
            // Sign in this automatic new customer
            $creds = array(
                'user_login'    => $username,
                'user_password' => $password,
                'remember'      => true
            );
            wp_signon($creds, false);
            
            wp_redirect(add_query_arg('mangata_success', urlencode('ثبت‌نام با موفقیت انجام شد.'), home_url('/')));
            exit;
        }
        
        if ( $action === 'batch_upload_zip' ) {
            if (!is_user_logged_in()) {
                wp_redirect(add_query_arg('mangata_error', urlencode('برای دسترسی ابتدا وارد شوید.'), home_url('/')));
                exit;
            }
            
            $user_id = get_current_user_id();
            $user_role = get_user_meta($user_id, 'mangata_role', true) ?: 'NORMAL_USER';
            if ($user_role !== 'SUPER_ADMIN' && !current_user_can('manage_options')) {
                wp_redirect(add_query_arg('mangata_error', urlencode('شما دسترسی لازم برای این بخش را ندارید.'), home_url('/')));
                exit;
            }

            if (!isset($_FILES['manga_zip']) || $_FILES['manga_zip']['error'] !== UPLOAD_ERR_OK) {
                wp_redirect(add_query_arg('mangata_error', urlencode('خطا در بارگذاری فایل زیپ.'), home_url('/')));
                exit;
            }

            $file_tmp = $_FILES['manga_zip']['tmp_name'];
            $file_name = sanitize_file_name($_FILES['manga_zip']['name']);
            
            // Generate a unique folder name for extraction
            $upload_dir = wp_upload_dir();
            $batch_folder_name = 'mangata_batch_' . time() . '_' . wp_generate_password(6, false);
            $target_dir = $upload_dir['basedir'] . '/mangata_batches/' . $batch_folder_name;
            $target_url = $upload_dir['baseurl'] . '/mangata_batches/' . $batch_folder_name;

            if ( !file_exists($target_dir) ) {
                wp_mkdir_p($target_dir);
            }

            if (class_exists('ZipArchive')) {
                $zip = new ZipArchive();
                if ($zip->open($file_tmp) === TRUE) {
                    $zip->extractTo($target_dir);
                    $zip->close();

                    // Recursively gather image files
                    $images = [];
                    $iterator = new RecursiveIteratorIterator(new RecursiveDirectoryIterator($target_dir));
                    foreach ($iterator as $file) {
                        if ($file->isFile()) {
                            $ext = strtolower(pathinfo($file->getPathname(), PATHINFO_EXTENSION));
                            if (in_array($ext, array('jpg', 'jpeg', 'png', 'webp', 'gif'))) {
                                // Calculate relative path from target_dir to support subfolders inside ZIP
                                $rel_path = str_replace('\\', '/', substr($file->getPathname(), strlen($target_dir)));
                                $rel_path = ltrim($rel_path, '/');
                                $images[] = $target_url . '/' . $rel_path;
                            }
                        }
                    }

                    // Sort files naturally (like 1.jpg, 2.jpg, 10.jpg)
                    natsort($images);
                    $images = array_values($images);

                    if (empty($images)) {
                        wp_redirect(add_query_arg('mangata_error', urlencode('فایل زیپ فاقد هرگونه تصویر معتبر بود.'), home_url('/')));
                        exit;
                    }

                    $json_array = json_encode($images, JSON_UNESCAPED_SLASHES);
                    
                    // Set option so administrative user can copy-paste it immediately!
                    update_option('mangata_last_uploaded_batch_json', $json_array);
                    update_option('mangata_last_uploaded_batch_name', $file_name);
                    
                    wp_redirect(add_query_arg('mangata_success', urlencode('بارگذاری دسته‌ای موفقیت‌آمیز بود! تصاویر استخراج و مپ شدند.'), home_url('/') . '#batch-uploader-console'));
                    exit;
                } else {
                    wp_redirect(add_query_arg('mangata_error', urlencode('باز کردن زیپ با خطا مواجه شد.'), home_url('/')));
                    exit;
                }
            } else {
                wp_redirect(add_query_arg('mangata_error', urlencode('سرویس زیپ در سرور فعال نیست (ZipArchive not found).'), home_url('/')));
                exit;
            }
        }

        if ( $action === 'add_manga' ) {
            if (!is_user_logged_in()) {
                wp_redirect(add_query_arg('mangata_error', urlencode('برای افزودن اثر ابتدا وارد شوید.'), home_url('/')));
                exit;
            }
            
            $user_id = get_current_user_id();
            $user_role = get_user_meta($user_id, 'mangata_role', true) ?: 'NORMAL_USER';
            
            if ($user_role !== 'SUPER_ADMIN' && !current_user_can('manage_options')) {
                wp_redirect(add_query_arg('mangata_error', urlencode('شما دسترسی لازم برای این بخش را ندارید.'), home_url('/')));
                exit;
            }
            
            $title_fa = isset($_POST['title_fa']) ? sanitize_text_field($_POST['title_fa']) : '';
            $title_en = isset($_POST['title_en']) ? sanitize_text_field($_POST['title_en']) : '';
            $description = isset($_POST['description']) ? wp_kses_post($_POST['description']) : '';
            $type = isset($_POST['manga_type']) ? sanitize_text_field($_POST['manga_type']) : 'مانهوا';
            $status = isset($_POST['manga_status']) ? sanitize_text_field($_POST['manga_status']) : 'در حال انتشار';
            $rating = isset($_POST['rating']) ? floatval($_POST['rating']) : 5.0;
            $genres = isset($_POST['genres']) ? sanitize_text_field($_POST['genres']) : '';
            $author = isset($_POST['author']) ? sanitize_text_field($_POST['author']) : '';
            $translator = isset($_POST['translator']) ? sanitize_text_field($_POST['translator']) : '';
            $chapters_count = isset($_POST['chapters_count']) ? intval($_POST['chapters_count']) : 10;
            $is_premium = isset($_POST['is_premium']) ? '1' : '0';
            $cover_url = isset($_POST['cover_url']) ? esc_url_raw($_POST['cover_url']) : '';
            $banner_url = isset($_POST['banner_url']) ? esc_url_raw($_POST['banner_url']) : '';
            $pages_raw = isset($_POST['pages_json']) ? wp_unslash($_POST['pages_json']) : '[]';
            
            if (empty($title_fa)) {
                wp_redirect(add_query_arg('mangata_error', urlencode('وارد کردن عنوان فارسی الزامی است.'), home_url('/')));
                exit;
            }
            
            // Validate JSON
            $json_test = json_decode($pages_raw);
            if (json_last_error() !== JSON_ERROR_NONE) {
                $pages_raw = '["https://picsum.photos/id/1015/800/1200", "https://picsum.photos/id/1016/800/1200"]';
            }
            
            $post_data = array(
                'post_title'    => $title_fa,
                'post_content'  => $description,
                'post_status'   => 'publish',
                'post_type'     => 'manga'
            );
            
            $new_post_id = wp_insert_post($post_data);
            
            if (is_wp_error($new_post_id)) {
                wp_redirect(add_query_arg('mangata_error', urlencode($new_post_id->get_error_message()), home_url('/')));
                exit;
            }
            
            // Set fields as post metas to make it 100% compatible with WP Custom Fields
            update_post_meta( $new_post_id, '_manga_title_en', $title_en );
            update_post_meta( $new_post_id, '_manga_type', $type );
            update_post_meta( $new_post_id, '_manga_status', $status );
            update_post_meta( $new_post_id, '_manga_rating', $rating );
            update_post_meta( $new_post_id, '_manga_genres', $genres );
            update_post_meta( $new_post_id, '_manga_author', $author );
            update_post_meta( $new_post_id, '_manga_translator_team', $translator );
            update_post_meta( $new_post_id, '_manga_chapters_count', $chapters_count );
            update_post_meta( $new_post_id, '_manga_is_premium', $is_premium );
            update_post_meta( $new_post_id, '_manga_pages_json', $pages_raw );
            
            if (!empty($cover_url)) {
                update_post_meta($new_post_id, '_manga_cover_url', $cover_url);
            }
            if (!empty($banner_url)) {
                update_post_meta($new_post_id, '_manga_banner_url', $banner_url);
            }
            
            wp_redirect(add_query_arg('mangata_success', urlencode('مانهوا با موفقیت اضافه شد!'), home_url('/')));
            exit;
        }
        
        if ( $action === 'charge_wallet' ) {
            if (!is_user_logged_in()) {
                wp_redirect(add_query_arg('mangata_error', urlencode('برای این کار ابتدا وارد شوید.'), home_url('/')));
                exit;
            }
            
            $user_id = get_current_user_id();
            $charge_amount = isset($_POST['amount']) ? intval($_POST['amount']) : 0;
            if ($charge_amount > 0) {
                $current_wallet = intval(get_user_meta($user_id, 'mangata_wallet_rial', true) ?: 0);
                update_user_meta($user_id, 'mangata_wallet_rial', $current_wallet + $charge_amount);
                wp_redirect(add_query_arg('mangata_success', urlencode('کیف پول شما با موفقیت ' . number_format($charge_amount) . ' ریال شارژ شد!'), home_url('/')));
                exit;
            } else {
                wp_redirect(add_query_arg('mangata_error', urlencode('مبلغ شارژ اشتباه است.'), home_url('/')));
                exit;
            }
        }

        if ( $action === 'web_toggle_bookmark' ) {
            if (!is_user_logged_in()) {
                wp_redirect(add_query_arg('mangata_error', urlencode('برای نشان کردن ابتدا وارد شوید.'), home_url('/')));
                exit;
            }
            $user_id = get_current_user_id();
            $manga_id = isset($_POST['manga_id']) ? intval($_POST['manga_id']) : 0;
            if ($manga_id > 0) {
                $bookmarks_raw = get_user_meta($user_id, 'mangata_bookmarks_json', true) ?: '[]';
                $bookmarks = json_decode($bookmarks_raw, true) ?: array();
                
                $found_index = -1;
                foreach ($bookmarks as $idx => $b) {
                    if ($b['mangaId'] == $manga_id) {
                        $found_index = $idx;
                        break;
                    }
                }
                
                if ($found_index !== -1) {
                    // Remove
                    unset($bookmarks[$found_index]);
                    $bookmarks = array_values($bookmarks);
                    $msg = 'اثر با موفقیت از مانهواهای نشان‌شده شما حذف گردید.';
                } else {
                    // Add
                    $bookmarks[] = array(
                        'mangaId' => $manga_id,
                        'bookmarkedAt' => time() * 1000
                    );
                    $msg = 'اثر با موفقیت به مانهواهای نشان‌شده شما اضافه گردید!';
                }
                
                update_user_meta($user_id, 'mangata_bookmarks_json', json_encode($bookmarks));
                wp_redirect(add_query_arg('mangata_success', urlencode($msg), home_url('/')));
                exit;
            }
            wp_redirect(home_url('/'));
            exit;
        }

        if ( $action === 'web_save_progress' ) {
            if (!is_user_logged_in()) {
                wp_redirect(add_query_arg('mangata_error', urlencode('برای ذخیره پیشرفت خوانش ابتدا وارد شوید.'), home_url('/')));
                exit;
            }
            $user_id = get_current_user_id();
            $manga_id = isset($_POST['manga_id']) ? intval($_POST['manga_id']) : 0;
            $chapter = isset($_POST['current_chapter']) ? intval($_POST['current_chapter']) : 1;
            $percent = isset($_POST['scroll_percent']) ? floatval($_POST['scroll_percent']) : 100.0;
            if ($manga_id > 0) {
                $history_raw = get_user_meta($user_id, 'mangata_read_history_json', true) ?: '[]';
                $history = json_decode($history_raw, true) ?: array();
                
                $found_index = -1;
                foreach ($history as $idx => $h) {
                    if ($h['mangaId'] == $manga_id) {
                        $found_index = $idx;
                        break;
                    }
                }
                
                $record = array(
                    'mangaId' => $manga_id,
                    'currentChapter' => $chapter,
                    'scrollPercent' => $percent,
                    'lastReadTimestamp' => time() * 1000
                );
                
                if ($found_index !== -1) {
                    $history[$found_index] = $record;
                } else {
                    $history[] = $record;
                }
                
                update_user_meta($user_id, 'mangata_read_history_json', json_encode($history));
                wp_redirect(add_query_arg('mangata_success', urlencode('پیشرفت خواندن این چپتر با دیتابیس همگام‌سازی و ذخیره شد!'), home_url('/')));
                exit;
            }
            wp_redirect(home_url('/'));
            exit;
        }
    }
    
    // Check logout action
    if ( isset($_GET['action']) && $_GET['action'] === 'mangata_logout' ) {
        wp_logout();
        wp_redirect(home_url('/'));
        exit;
    }
}
add_action('init', 'mangata_handle_frontend_actions');

/**
 * REST Admin Implementations
 */
function mangata_get_admin_settings() {
    $settings = get_option('mangata_system_settings', array());
    $defaults = array(
        'id' => 1,
        'baseChapterPrice' => 450,
        'discountPercent50' => 20,
        'discountPercent100' => 40,
        'defaultStaffRewardChapters' => 5,
        'minChaptersForStoryToken' => 40,
        'storyTokensAwarded' => 2,
        'maxVideoStoryDurationSeconds' => 30,
        'shareCleanerPct' => 30,
        'shareEditorPct' => 30,
        'shareTranslatorPct' => 20,
        'sharePlatformPct' => 20,
        'isTranslatorTestUploaded' => true,
        'isCleanerTestUploaded' => false,
        'isTypistTestUploaded' => false,
        'requiredVersion' => 2,
        'featuredMangaIdsJson' => '[]',
        'startsFromZeroMangaIdsJson' => '[]'
    );
    
    $settings_merged = array_merge($defaults, (array)$settings);
    // Correct type conversions for JSON output
    $settings_merged['id'] = intval($settings_merged['id']);
    $settings_merged['baseChapterPrice'] = intval($settings_merged['baseChapterPrice']);
    $settings_merged['discountPercent50'] = intval($settings_merged['discountPercent50']);
    $settings_merged['discountPercent100'] = intval($settings_merged['discountPercent100']);
    $settings_merged['defaultStaffRewardChapters'] = intval($settings_merged['defaultStaffRewardChapters']);
    $settings_merged['minChaptersForStoryToken'] = intval($settings_merged['minChaptersForStoryToken']);
    $settings_merged['storyTokensAwarded'] = intval($settings_merged['storyTokensAwarded']);
    $settings_merged['maxVideoStoryDurationSeconds'] = intval($settings_merged['maxVideoStoryDurationSeconds']);
    $settings_merged['shareCleanerPct'] = intval($settings_merged['shareCleanerPct']);
    $settings_merged['shareEditorPct'] = intval($settings_merged['shareEditorPct']);
    $settings_merged['shareTranslatorPct'] = intval($settings_merged['shareTranslatorPct']);
    $settings_merged['sharePlatformPct'] = intval($settings_merged['sharePlatformPct']);
    $settings_merged['isTranslatorTestUploaded'] = filter_var($settings_merged['isTranslatorTestUploaded'], FILTER_VALIDATE_BOOLEAN);
    $settings_merged['isCleanerTestUploaded'] = filter_var($settings_merged['isCleanerTestUploaded'], FILTER_VALIDATE_BOOLEAN);
    $settings_merged['isTypistTestUploaded'] = filter_var($settings_merged['isTypistTestUploaded'], FILTER_VALIDATE_BOOLEAN);
    $settings_merged['requiredVersion'] = intval($settings_merged['requiredVersion']);
    $settings_merged['featuredMangaIdsJson'] = strval($settings_merged['featuredMangaIdsJson']);
    $settings_merged['startsFromZeroMangaIdsJson'] = strval($settings_merged['startsFromZeroMangaIdsJson']);
    
    return $settings_merged;
}

function mangata_update_admin_settings($request) {
    $params = $request->get_json_params();
    $settings = get_option('mangata_system_settings', array());
    foreach ($params as $key => $val) {
        if ($key !== 'id') {
            if (is_bool($val)) {
                $settings[$key] = $val;
            } elseif (is_numeric($val)) {
                $settings[$key] = intval($val);
            } else {
                $settings[$key] = sanitize_text_field($val);
            }
        }
    }
    update_option('mangata_system_settings', $settings);
    return mangata_get_admin_settings();
}

function mangata_get_admin_users() {
    $wp_users = get_users();
    $accounts = array();
    foreach ($wp_users as $u) {
        $role = get_user_meta($u->ID, 'mangata_role', true) ?: 'NORMAL_USER';
        $sub_role = get_user_meta($u->ID, 'mangata_sub_role', true) ?: 'کاربر عادی';
        $wallet_rial = intval(get_user_meta($u->ID, 'mangata_wallet_rial', true) ?: 0);
        $wallet_gift = intval(get_user_meta($u->ID, 'mangata_wallet_gift_chapters', true) ?: 0);
        $contrib_last_month = intval(get_user_meta($u->ID, 'mangata_contrib_last', true) ?: 0);
        $contrib_this_month = intval(get_user_meta($u->ID, 'mangata_contrib_this', true) ?: 0);
        $story_tokens = intval(get_user_meta($u->ID, 'mangata_story_tokens', true) ?: 0);
        $rate = get_user_meta($u->ID, 'mangata_reward_rate', true);
        $custom_rate = ($rate !== '') ? intval($rate) : null;

        $accounts[] = array(
            'id' => $u->ID,
            'username' => $u->user_login,
            'displayName' => $u->display_name ?: $u->user_login,
            'role' => $role,
            'subRole' => $sub_role,
            'walletRial' => $wallet_rial,
            'walletGiftChapters' => $wallet_gift,
            'chaptersContributedLastMonth' => $contrib_last_month,
            'chaptersContributedThisMonth' => $contrib_this_month,
            'storyTokens' => $story_tokens,
            'customRewardRate' => $custom_rate,
            'password' => '123456'
        );
    }
    return $accounts;
}

function mangata_update_admin_user($request) {
    $params = $request->get_json_params();
    $user_id = isset($params['id']) ? intval($params['id']) : 0;
    if ($user_id > 0) {
        if (isset($params['role'])) {
            update_user_meta($user_id, 'mangata_role', sanitize_text_field($params['role']));
        }
        if (isset($params['subRole'])) {
            update_user_meta($user_id, 'mangata_sub_role', sanitize_text_field($params['subRole']));
        }
        if (isset($params['walletRial'])) {
            update_user_meta($user_id, 'mangata_wallet_rial', intval($params['walletRial']));
        }
        if (isset($params['walletGiftChapters'])) {
            update_user_meta($user_id, 'mangata_wallet_gift_chapters', intval($params['walletGiftChapters']));
        }
        if (isset($params['customRewardRate'])) {
            update_user_meta($user_id, 'mangata_reward_rate', intval($params['customRewardRate']));
        }
        return array('success' => true);
    }
    return array('success' => false, 'error' => 'کاربر پیدا نشد.');
}

function mangata_get_admin_recruitments() {
    $res = get_option('mangata_recruitment_list', array());
    if (!is_array($res)) {
        $res = array();
    }
    return $res;
}

function mangata_add_admin_recruitment($request) {
    $params = $request->get_json_params();
    $list = get_option('mangata_recruitment_list', array()) ?: array();
    
    $new_id = time() + rand(100, 999);
    $app = array(
        'id' => $new_id,
        'fullName' => isset($params['fullName']) ? sanitize_text_field($params['fullName']) : 'نامشخص',
        'messengerId' => isset($params['messengerId']) ? sanitize_text_field($params['messengerId']) : '',
        'specialty' => isset($params['specialty']) ? sanitize_text_field($params['specialty']) : 'مترجم',
        'testFileName' => isset($params['testFileName']) ? sanitize_text_field($params['testFileName']) : '',
        'uploadedWorkName' => isset($params['uploadedWorkName']) ? sanitize_text_field($params['uploadedWorkName']) : '',
        'status' => 'PENDING',
        'dateSubmitted' => current_time('mysql')
    );
    
    $list[] = $app;
    update_option('mangata_recruitment_list', $list);
    return $app;
}

function mangata_update_admin_recruitment($request) {
    $params = $request->get_json_params();
    $app_id = isset($params['id']) ? intval($params['id']) : 0;
    $status = isset($params['status']) ? sanitize_text_field($params['status']) : 'PENDING';
    
    $list = get_option('mangata_recruitment_list', array()) ?: array();
    $updated = false;
    foreach ($list as &$item) {
        if (intval($item['id']) === $app_id) {
            $item['status'] = $status;
            $updated = true;
            break;
        }
    }
    if ($updated) {
        update_option('mangata_recruitment_list', $list);
        return array('success' => true);
    }
    return array('success' => false, 'error' => 'درخواست پیدا نشد.');
}

function mangata_save_manga($request) {
    $params = $request->get_json_params();
    $manga_id = isset($params['id']) ? intval($params['id']) : 0;
    
    $post_data = array(
        'post_title'   => sanitize_text_field($params['titleFa']),
        'post_content' => sanitize_textarea_field($params['descriptionFa']),
        'post_status'  => 'publish',
        'post_type'    => 'manga'
    );
    
    if ($manga_id > 0) {
        $post_data['ID'] = $manga_id;
        $id = wp_update_post($post_data);
    } else {
        $id = wp_insert_post($post_data);
    }
    
    if (is_wp_error($id)) {
        return array('success' => false, 'error' => $id->get_error_message());
    }
    
    // Update fields
    update_post_meta($id, '_manga_title_en', sanitize_text_field($params['titleEn']));
    update_post_meta($id, '_manga_cover_url', esc_url_raw($params['coverUrl']));
    update_post_meta($id, '_manga_banner_url', esc_url_raw($params['bannerUrl']));
    update_post_meta($id, '_manga_type', sanitize_text_field(isset($params['type']) ? $params['type'] : 'مانهوا'));
    update_post_meta($id, '_manga_status', sanitize_text_field(isset($params['status']) ? $params['status'] : 'در حال انتشار'));
    update_post_meta($id, '_manga_genres', sanitize_text_field(isset($params['genres']) ? $params['genres'] : 'فانتزی, اکشن'));
    update_post_meta($id, '_manga_author', sanitize_text_field(isset($params['author']) ? $params['author'] : 'نامشخص'));
    update_post_meta($id, '_manga_translator_team', sanitize_text_field(isset($params['translatorTeam']) ? $params['translatorTeam'] : 'تیم مانگاتا'));
    update_post_meta($id, '_manga_chapters_count', intval(isset($params['chaptersCount']) ? $params['chaptersCount'] : 10));
    update_post_meta($id, '_manga_is_premium', sanitize_text_field(isset($params['isPremium']) && $params['isPremium'] ? 'true' : 'false'));
    if (isset($params['pagesJson'])) {
        update_post_meta($id, '_manga_pages_json', $params['pagesJson']);
    }
    
    return array('success' => true, 'id' => $id);
}

function mangata_delete_manga($request) {
    $params = $request->get_json_params();
    $manga_id = isset($params['id']) ? intval($params['id']) : 0;
    if ($manga_id > 0) {
        wp_delete_post($manga_id, true);
        return array('success' => true);
    }
    return array('success' => false, 'error' => 'شناسه نامعتبر است.');
}

