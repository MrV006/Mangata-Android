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
            $cover_url = get_the_post_thumbnail_url($id, 'medium') ?: 'https://picsum.photos/id/1025/400/600';
            $banner_url = get_the_post_thumbnail_url($id, 'large') ?: 'https://picsum.photos/id/1025/1200/600';

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

    return array(
        'id'                  => $user_id,
        'username'            => $user->user_login,
        'displayName'         => $user->display_name ?: $user->user_login,
        'role'                => $role,
        'subRole'             => $sub_role,
        'walletRial'          => $wallet_rial,
        'walletGiftChapters'  => $wallet_gift,
        'purchasedChaptersJson' => $purchased_json,
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

