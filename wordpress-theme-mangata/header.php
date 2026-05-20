<!DOCTYPE html>
<html <?php language_attributes(); ?>>
<head>
    <meta charset="<?php bloginfo( 'charset' ); ?>">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php wp_title('|', true, 'right'); bloginfo('name'); ?></title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        /* Anti-Piracy Selection Block */
        body, img, div, p, span, a {
            -webkit-user-select: none;
            -moz-user-select: none;
            -ms-user-select: none;
            user-select: none;
            -webkit-user-drag: none;
        }
    </style>
    <script>
        // Anti-Piracy: Right-click protection
        document.addEventListener('contextmenu', function(e) {
            e.preventDefault();
            alert('راست کلیک در وب‌سایت مانگاتا مسدود شده است! کپی‌برداری مجاز نمی‌باشد.');
        });

        // Anti-Piracy: Image drag prevention
        document.addEventListener('dragstart', function(e) {
            if (e.target.nodeName === 'IMG') {
                e.target.style.filter = "blur(10px)";
                setTimeout(function() { e.target.style.filter = "none"; }, 500);
                e.preventDefault();
            }
        });

        // Anti-Piracy: DevTools keylock combination prevention
        document.addEventListener('keydown', function(e) {
            // Disable F12
            if (e.keyCode === 123) {
                e.preventDefault();
                return false;
            }
            // Disable Ctrl+Shift+I (Inspect element)
            if (e.ctrlKey && e.shiftKey && e.keyCode === 73) {
                e.preventDefault();
                return false;
            }
            // Disable Ctrl+Shift+C (Inspect device)
            if (e.ctrlKey && e.shiftKey && e.keyCode === 67) {
                e.preventDefault();
                return false;
            }
            // Disable Ctrl+u / Ctrl+s
            if (e.ctrlKey && (e.keyCode === 85 || e.keyCode === 83)) {
                e.preventDefault();
                return false;
            }
        });

        // Anti-Piracy: Continuous debug loop to disrupt chrome devtools inspect
        setInterval(function() {
            (function() {
                var before = new Date().getTime();
                debugger;
                var after = new Date().getTime();
                if (after - before > 100) {
                    document.body.innerHTML = "<div style='display:flex;justify-content:center;align-items:center;height:100vh;background:#06080c;color:#ff5252;font-family:sans-serif;font-weight:bold;font-size:1.5rem;direction:rtl;text-align:center;'>دسترسی به ابزارهای بازرسی (Inspect Element) مسدود شد!<br>کپی‌برداری غیرمجاز از پنل‌های مانهوا پیگرد قانونی دارد.</div>";
                }
            })();
        }, 1000);
    </script>
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
