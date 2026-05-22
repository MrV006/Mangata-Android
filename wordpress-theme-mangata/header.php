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

    <nav style="display: flex; align-items: center; gap: 20px;">
        <ul class="nav-links" style="margin-bottom: 0;">
            <li><a href="<?php echo esc_url( home_url( '/' ) ); ?>" class="active-nav"><i class="fa-solid fa-book-open" style="margin-left: 6px;"></i>کتابخانه</a></li>
            <li><a href="<?php echo esc_url( home_url( '/#collaborators' ) ); ?>"><i class="fa-solid fa-users-gear" style="margin-left: 6px;"></i>تیم ترجمه</a></li>
            <li><a href="<?php echo esc_url( home_url( '/#vippricing' ) ); ?>" class="vip-badge-btn"><i class="fa-solid fa-crown" style="margin-left: 6px;"></i>ارتقای عضویت VIP</a></li>
        </ul>

        <div class="user-action-shelf" style="display: flex; align-items: center; gap: 12px;">
            <?php if ( is_user_logged_in() ) : 
                $curr_user = wp_get_current_user();
                $u_id = $curr_user->ID;
                $display_name = $curr_user->display_name ?: $curr_user->user_login;
                $role = get_user_meta($u_id, 'mangata_role', true) ?: 'NORMAL_USER';
                $sub_role = get_user_meta($u_id, 'mangata_sub_role', true) ?: 'کاربر عادی';
                $wallet_rial = intval(get_user_meta($u_id, 'mangata_wallet_rial', true) ?: 0);
                $wallet_gift = intval(get_user_meta($u_id, 'mangata_wallet_gift_chapters', true) ?: 0);
                $is_admin = ($role === 'SUPER_ADMIN' || current_user_can('manage_options') || strtolower($curr_user->user_login) === 'mr.v');
            ?>
                <!-- Logged In User Pill -->
                <div class="user-logged-pill" style="background-color: #1A1D24; border: 1.5px solid #2D3139; padding: 6px 14px; border-radius: 12px; display: flex; align-items: center; gap: 10px;">
                    <div style="display: flex; flex-direction: column; text-align: right;">
                        <span style="font-size: 13px; font-weight: bold; color: #ffffff;"><?php echo esc_html($display_name); ?></span>
                        <span style="font-size: 10px; color: #9AA0A6;"><?php echo esc_html($sub_role); ?></span>
                    </div>
                    <div class="wallet-badge" style="background-color: #0d1e3d; border: 1px solid #0052cc; color: #80b3ff; font-size: 11px; padding: 3px 8px; border-radius: 6px; font-weight: bold; display: flex; align-items: center; gap: 4px;">
                        <span><?php echo number_format($wallet_rial); ?></span> <span style="font-size: 8px;">ریال</span>
                    </div>
                    <?php if ($wallet_gift > 0) : ?>
                        <div class="gift-badge" style="background-color: #3b111a; border: 1px solid #cc0033; color: #ff8093; font-size: 11px; padding: 3px 8px; border-radius: 6px; font-weight: bold; display: flex; align-items: center; gap: 4px;">
                            <span><?php echo esc_html($wallet_gift); ?></span> <span style="font-size: 8px;">هدیه</span>
                        </div>
                    <?php endif; ?>
                    
                    <?php if ( $is_admin ) : ?>
                        <a href="#mangata-web-admin" class="admin-link-btn" style="background: linear-gradient(135deg, #00C6FF, #0072FF); color: #ffffff; font-size: 12px; padding: 5px 12px; border-radius: 8px; font-weight: bold;"><i class="fa-solid fa-gauge-high"></i> پنل مدیریت</a>
                    <?php endif; ?>
                    
                    <a href="?action=mangata_logout" class="logout-btn" style="color: #ff5252; font-size: 12px; font-weight: bold; padding: 4px; margin-right: 6px;" title="خروج"><i class="fa-solid fa-power-off"></i></a>
                </div>
            <?php else : ?>
                <!-- Login/Register Buttons -->
                <button onclick="document.getElementById('mangata-login-modal').style.display='flex';" style="background-color: #1A1D24; color: #ffffff; border: 1.5px solid #2D3139; padding: 8px 18px; border-radius: 10px; font-weight: bold; font-size: 13px; cursor: pointer; transition: all 0.2s;"><i class="fa-solid fa-right-to-bracket" style="margin-left: 6px;"></i>ورود به حساب</button>
                <button onclick="document.getElementById('mangata-register-modal').style.display='flex';" style="background: linear-gradient(135deg, #00C6FF, #0072FF); color: #ffffff; border: none; padding: 8px 18px; border-radius: 10px; font-weight: bold; font-size: 13px; cursor: pointer; transition: all 0.2s; box-shadow: 0 4px 10px rgba(0, 114, 255, 0.2);"><i class="fa-solid fa-user-plus" style="margin-left: 6px;"></i>ثبت‌نام جدید</button>
            <?php endif; ?>
        </div>
    </nav>
</header>

<!-- Dialog Modals -->
<div id="mangata-login-modal" class="mangata-modal-overlay" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(6, 8, 12, 0.9); justify-content: center; align-items: center; z-index: 99999; backdrop-filter: blur(10px); transition: all 0.3s ease;">
    <div class="mangata-modal-card" style="background-color: #16191E; border: 1.5px solid #2D3139; width: 90%; max-width: 420px; padding: 30px; border-radius: 16px; position: relative; box-shadow: 0 10px 30px rgba(0,0,0,0.5);">
        <button onclick="document.getElementById('mangata-login-modal').style.display='none';" class="modal-close-btn" style="position: absolute; top: 15px; left: 15px; background: none; border: none; color: #9AA0A6; font-size: 18px; cursor: pointer;"><i class="fa-solid fa-xmark"></i></button>
        <div style="text-align: center; margin-bottom: 25px;">
            <div class="brand-logo-m" style="margin: 0 auto 12px auto; width: 44px; height: 44px; font-size: 24px;">M</div>
            <h3 style="color: #ffffff; font-size: 18px; font-weight: 800;">خوش آمدید! ورود به حساب کاربری</h3>
            <p style="color: #9AA0A6; font-size: 11px; margin-top: 5px;">مشخصات خود را وارد کرده و وارد وب‌تون‌خوان مانگاتا شوید.</p>
        </div>
        <form action="<?php echo esc_url( home_url('/') ); ?>" method="POST" style="display: flex; flex-direction: column; gap: 15px;">
            <input type="hidden" name="mangata_web_action" value="login">
            <div style="display: flex; flex-direction: column; gap: 6px;">
                <label style="color: #BDC1C6; font-size: 12px; font-weight: bold;">نام کاربری</label>
                <input type="text" name="username" required placeholder="مثال: amirreza" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px 12px; color: #ffffff; direction: ltr; font-weight: bold; outline: none; transition: border-color 0.2s;" onfocus="this.style.borderColor='#0072FF'" onblur="this.style.borderColor='#2D3139'">
            </div>
            <div style="display: flex; flex-direction: column; gap: 6px;">
                <label style="color: #BDC1C6; font-size: 12px; font-weight: bold;">رمز عبور حساب</label>
                <input type="password" name="password" required placeholder="••••••••" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px 12px; color: #ffffff; direction: ltr; outline: none; transition: border-color 0.2s;" onfocus="this.style.borderColor='#0072FF'" onblur="this.style.borderColor='#2D3139'">
            </div>
            <button type="submit" style="background: linear-gradient(135deg, #00C6FF, #0072FF); color: #ffffff; border: none; padding: 12px; border-radius: 10px; font-weight: bold; font-size: 14px; margin-top: 10px; cursor: pointer; transition: all 0.25s;"><i class="fa-solid fa-right-to-bracket" style="margin-left: 6px;"></i>ورود ایمن به حساب</button>
            <p style="text-align: center; color: #9AA0A6; font-size: 11px; margin-top: 10px;">عضو نیستید؟ <a href="#" onclick="document.getElementById('mangata-login-modal').style.display='none'; document.getElementById('mangata-register-modal').style.display='flex';" style="color: #00C6FF; font-weight: bold;">ایجاد حساب جدید</a></p>
        </form>
    </div>
</div>

<div id="mangata-register-modal" class="mangata-modal-overlay" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(6, 8, 12, 0.9); justify-content: center; align-items: center; z-index: 99999; backdrop-filter: blur(10px); transition: all 0.3s ease;">
    <div class="mangata-modal-card" style="background-color: #16191E; border: 1.5px solid #2D3139; width: 90%; max-width: 420px; padding: 30px; border-radius: 16px; position: relative; box-shadow: 0 10px 30px rgba(0,0,0,0.5);">
        <button onclick="document.getElementById('mangata-register-modal').style.display='none';" class="modal-close-btn" style="position: absolute; top: 15px; left: 15px; background: none; border: none; color: #9AA0A6; font-size: 18px; cursor: pointer;"><i class="fa-solid fa-xmark"></i></button>
        <div style="text-align: center; margin-bottom: 20px;">
            <div class="brand-logo-m" style="margin: 0 auto 12px auto; width: 44px; height: 44px; font-size: 24px;">M</div>
            <h3 style="color: #ffffff; font-size: 18px; font-weight: 800;">عضویت و ثبت نام در مانگاتا</h3>
            <p style="color: #9AA0A6; font-size: 11px; margin-top: 5px;">حساب خود را بسازید تا اعتبار رایگان هدیه عضویت جدید را دریافت کنید.</p>
        </div>
        <form action="<?php echo esc_url( home_url('/') ); ?>" method="POST" style="display: flex; flex-direction: column; gap: 14px;">
            <input type="hidden" name="mangata_web_action" value="register">
            <div style="display: flex; flex-direction: column; gap: 4px;">
                <label style="color: #BDC1C6; font-size: 12px; font-weight: bold;">نام کاربری (انگلیسی)</label>
                <input type="text" name="username" required placeholder="مثال: mr.v" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px 12px; color: #ffffff; direction: ltr; font-weight: bold; outline: none; transition: border-color 0.2s;" onfocus="this.style.borderColor='#0072FF'" onblur="this.style.borderColor='#2D3139'">
                <small style="color: #80b3ff; font-size: 9px;">کلمه "Mr.V" بلافاصله با عنوان مدیر کل و SUPER_ADMIN متصل خواهد شد.</small>
            </div>
            <div style="display: flex; flex-direction: column; gap: 4px;">
                <label style="color: #BDC1C6; font-size: 12px; font-weight: bold;">نام نمایشی (فارسی/انگلیسی)</label>
                <input type="text" name="display_name" placeholder="مثال: امیررضا" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px 12px; color: #ffffff; outline: none; transition: border-color 0.2s;" onfocus="this.style.borderColor='#0072FF'" onblur="this.style.borderColor='#2D3139'">
            </div>
            <div style="display: flex; flex-direction: column; gap: 4px;">
                <label style="color: #BDC1C6; font-size: 12px; font-weight: bold;">رمز عبور حساب</label>
                <input type="password" name="password" required placeholder="••••••••" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px 12px; color: #ffffff; direction: ltr; outline: none; transition: border-color 0.2s;" onfocus="this.style.borderColor='#0072FF'" onblur="this.style.borderColor='#2D3139'">
            </div>
            <button type="submit" style="background: linear-gradient(135deg, #ffd700, #ffa500); color: #101216; border: none; padding: 12px; border-radius: 10px; font-weight: bold; font-size: 14px; margin-top: 10px; cursor: pointer; transition: all 0.25s;"><i class="fa-solid fa-user-plus" style="margin-left: 6px;"></i>ثبت‌نام و دریافت هدیه خوش‌آمدگویی</button>
            <p style="text-align: center; color: #9AA0A6; font-size: 11px; margin-top: 6px;">قبلاً ثبت‌نام کرده‌اید؟ <a href="#" onclick="document.getElementById('mangata-register-modal').style.display='none'; document.getElementById('mangata-login-modal').style.display='flex';" style="color: #00C6FF; font-weight: bold;">ورود به حساب</a></p>
        </form>
    </div>
</div>
