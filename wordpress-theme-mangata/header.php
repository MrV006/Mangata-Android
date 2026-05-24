<!DOCTYPE html>
<html <?php language_attributes(); ?>>
<head>
    <meta charset="<?php bloginfo('charset'); ?>">
    <meta name="viewport" content="width=device-width, initial-scale=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="<?php echo get_stylesheet_uri(); ?>">
    <?php wp_head(); ?>
</head>
<body <?php body_class(); ?>>
<header>
    <h1><a href="<?php echo esc_url(home_url('/')); ?>"><?php bloginfo('name'); ?> | مانهوا ریدر هوشمند</a></h1>
    <nav style="display: flex; gap: 15px;">
        <a href="<?php echo esc_url(home_url('/')); ?>" style="color:#bb86fc; text-decoration:none;">صفحه اصلی</a>
        <a href="#manhwa" style="color:#fff; text-decoration:none;">مانهواها</a>
        <a href="#recruitment" style="color:#fff; text-decoration:none;">استخدام تیم ترجمه</a>
    </nav>
</header>
