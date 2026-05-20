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
 * REST API Endpoint Integration for Android App
 * Enables the Android App to fetch real mangas and chapters from your site database (mr-v.ir)
 */
function register_mangata_rest_routes() {
    register_rest_route( 'mangata/v1', '/mangas', array(
        'methods'             => 'GET',
        'callback'            => 'get_mangata_dynamic_manga_list',
        'permission_callback' => '__return_true', // Public access for users reading on client
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
                'isPremium'      => false,
                'reviewsJson'    => '[]',
                'pagesJson'      => '["https://picsum.photos/id/1028/800/1200", "https://picsum.photos/id/1029/800/1200"]'
            )
        );
    }

    return $mangas;
}
