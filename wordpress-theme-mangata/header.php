<!DOCTYPE html>
<html <?php language_attributes(); ?>>
<head>
    <meta charset="<?php bloginfo( 'charset' ); ?>">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php wp_title('|', true, 'right'); bloginfo('name'); ?></title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <?php wp_head(); ?>
</head>
<body <?php body_class(); ?>>

<header class="mangata-header">
    <div class="brand-wrapper">
        <a href="<?php echo esc_url( home_url( '/' ) ); ?>" style="display: flex; align-items: center; gap: 12px;">
            <div class="brand-logo-m">M</div>
            <div class="brand-title">مانگاتا</div>
        </a>
    </div>

    <nav>
        <ul class="nav-links">
            <li><a href="<?php echo esc_url( home_url( '/' ) ); ?>" class="active-nav"><i class="fa-solid fa-book-open" style="margin-left: 6px;"></i>کتابخانه</a></li>
            <li><a href="<?php echo esc_url( home_url( '/#collaborators' ) ); ?>"><i class="fa-solid fa-users-gear" style="margin-left: 6px;"></i>تیم ترجمه</a></li>
            <li><a href="<?php echo esc_url( home_url( '/#vippricing' ) ); ?>" class="vip-badge-btn"><i class="fa-solid fa-crown" style="margin-left: 6px;"></i>ارتقای عضویت VIP</a></li>
        </ul>
    </nav>
</header>
