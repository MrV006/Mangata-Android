<?php get_header(); ?>

<!-- 🚀 Featured Header Hero Banner (Solo Leveling / Mangata recommendation) -->
<section class="featured-hero-banner" style="background-image: url('https://picsum.photos/id/1025/1200/600');">
    <div class="hero-content">
        <span class="type-tag">مانهوا داغ هفته</span>
        <span class="rating-tag"><i class="fa-solid fa-star" style="margin-left: 4px; color:#ffd700;"></i>۴.۹</span>
        <h1 class="hero-title">سولو لولینگ (تک‌رو) • Solo Leveling</h1>
        <p class="hero-meta">اثری بی‌بدیل از Chugong • ژانر: اکشن، ماجراجویی، سیستم فانتزی • هم‌اکنون در مانگاتا</p>
        <p style="max-width: 650px; color: #BDC1C6; font-size: 13px; margin-bottom: 20px;">
            در دنیایی دلهره‌آور که سیاهچاله‌های مخوف بشریت را تهدید می‌کنند، ضعیف‌ترین شکارچی جهان با دریافت آپگرید فوق سکرت سیستم بازی‌ها شروع به فتح قله‌های بی پایان قدرت فیزیکی می‌کند...
        </p>
        <a href="#mangacatalog" class="reader-btn" style="background: linear-gradient(135deg, #00C6FF, #0072FF); padding: 10px 24px; border-radius: 12px; font-weight: bold;"><i class="fa-solid fa-play" style="margin-left:8px;"></i>شروع به خواندن آخرین فصل</a>
    </div>
</section>

<!-- 📚 Main Catalog Library -->
<div class="main-container" id="mangacatalog">

    <?php if ( isset($_GET['mangata_success']) ) : ?>
        <div style="background-color: #0d3c26; border: 1.5px solid #196f43; color: #3cd070; padding: 15px; border-radius: 12px; font-weight: bold; font-size: 14px; margin-bottom: 25px; display: flex; align-items: center; gap: 10px;">
            <i class="fa-solid fa-circle-check"></i>
            <span><?php echo esc_html(urldecode($_GET['mangata_success'])); ?></span>
        </div>
    <?php endif; ?>

    <?php if ( isset($_GET['mangata_error']) ) : ?>
        <div style="background-color: #3b1313; border: 1.5px solid #7c2222; color: #ff5252; padding: 15px; border-radius: 12px; font-weight: bold; font-size: 14px; margin-bottom: 25px; display: flex; align-items: center; gap: 10px;">
            <i class="fa-solid fa-circle-exclamation"></i>
            <span><?php echo esc_html(urldecode($_GET['mangata_error'])); ?></span>
        </div>
    <?php endif; ?>

    <?php if ( is_user_logged_in() ) : 
        $curr_user = wp_get_current_user();
        $u_role = get_user_meta($curr_user->ID, 'mangata_role', true) ?: 'NORMAL_USER';
        if ( $u_role === 'SUPER_ADMIN' || current_user_can('manage_options') || strtolower($curr_user->user_login) === 'mr.v' ) : ?>
            <h2 class="section-head" id="mangata-web-admin" style="border-right-color: #ffd700;"><i class="fa-solid fa-gauge-high" style="color: #ffd700; margin-left:8px;"></i>داشبورد مدیریت جامع وب‌سایت (SUPER_ADMIN)</h2>
            <div style="background-color: #16191E; border: 1.5px solid #2D3139; border-radius: 16px; padding: 25px; margin-bottom: 40px; box-shadow: 0 4px 15px rgba(0,0,0,0.2);">
                <p style="color: #ffd700; font-size: 13px; font-weight: bold; margin-bottom: 15px;"><i class="fa-solid fa-circle-info" style="margin-left:8px;"></i>ابزار درج و بارگذاری مانهوا / مانگای جدید به صورت مستقیم در سنترال دیتابیس (mr-v.ir)</p>
                <form action="<?php echo esc_url( home_url('/') ); ?>" method="POST" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px;">
                    <input type="hidden" name="mangata_web_action" value="add_manga">
                    
                    <div style="display: flex; flex-direction: column; gap: 6px;">
                        <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">عنوان فارسی مانهوا</label>
                        <input type="text" name="title_fa" required placeholder="مثال: بازگشت قهرمان" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #ffffff;">
                    </div>
                    
                    <div style="display: flex; flex-direction: column; gap: 6px;">
                        <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">عنوان انگلیسی مانهوا</label>
                        <input type="text" name="title_en" placeholder="مثال: The Return of Hero" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #ffffff; direction: ltr;">
                    </div>
                    
                    <div style="display: flex; flex-direction: column; gap: 6px;">
                        <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">ژانرها (جدا شده با کاما)</label>
                        <input type="text" name="genres" placeholder="مثال: اکشن، فانتزی، حماسی" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #ffffff;">
                    </div>

                    <div style="display: flex; flex-direction: column; gap: 6px;">
                        <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">نویسنده / طراح</label>
                        <input type="text" name="author" placeholder="مثال: Chugong" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #ffffff;">
                    </div>

                    <div style="display: flex; flex-direction: column; gap: 6px;">
                        <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">تیم ترجمه</label>
                        <input type="text" name="translator" placeholder="مثال: کادر اختصاصی مانگاتا" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #ffffff;">
                    </div>

                    <div style="display: flex; flex-direction: column; gap: 6px;">
                        <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">نوع اثر</label>
                        <select name="manga_type" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #ffffff;">
                            <option value="مانهوا">مانهوا (کره‌ای)</option>
                            <option value="مانگا">مانگا (ژاپنی)</option>
                            <option value="مانها">مانها (چینی)</option>
                        </select>
                    </div>

                    <div style="display: flex; flex-direction: column; gap: 6px;">
                        <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">وضعیت انتشار</label>
                        <select name="manga_status" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #ffffff;">
                            <option value="در حال انتشار">در حال انتشار</option>
                            <option value="پایان یافته">پایان یافته</option>
                        </select>
                    </div>

                    <div style="display: flex; flex-direction: column; gap: 6px;">
                        <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">تعداد کل فصول</label>
                        <input type="number" name="chapters_count" value="150" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #ffffff;">
                    </div>

                    <div style="display: flex; flex-direction: column; gap: 6px;">
                        <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">امتیاز اثر (از ۱۰)</label>
                        <input type="number" step="0.1" name="rating" value="4.8" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #ffffff;">
                    </div>

                    <div style="display: flex; flex-direction: column; gap: 6px;">
                        <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">آدرس تصویر کاور (Poster Image URL)</label>
                        <input type="text" name="cover_url" placeholder="https://picsum.photos/id/1025/400/600" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #ffffff; direction: ltr;">
                    </div>

                    <div style="display: flex; flex-direction: column; gap: 6px;">
                        <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">آدرس تصویر بنر هدر (Banner Image URL)</label>
                        <input type="text" name="banner_url" placeholder="https://picsum.photos/id/1025/1200/600" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #ffffff; direction: ltr;">
                    </div>

                    <div style="display: flex; flex-direction: column; gap: 6px;">
                        <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">فصل پولی (Premium/VIP)</label>
                        <label style="display: flex; align-items: center; gap: 8px; margin-top: 10px; color: #BDC1C6; font-size: 13px; cursor: pointer;">
                            <input type="checkbox" name="is_premium" value="1" checked style="width: 16px; height: 16px;"> بله، مانهوا شامل فصول ویژه باشد
                        </label>
                    </div>

                    <div style="display: flex; flex-direction: column; gap: 6px; grid-column: span 1; grid-column-end: -1;">
                        <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">صفحات مانهوا به صورت JSON Array (جهت وب‌تون‌خوان)</label>
                        <textarea name="pages_json" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #00ff66; direction: ltr; font-family: monospace; height: 80px;">["https://picsum.photos/id/1015/800/1200", "https://picsum.photos/id/1016/800/1200", "https://picsum.photos/id/1018/800/1200"]</textarea>
                    </div>

                    <div style="display: flex; align-items: flex-end; grid-column: span 1;">
                        <button type="submit" style="background: linear-gradient(135deg, #ffd700, #ffa500); color: #101216; border: none; width: 100%; padding: 12px; border-radius: 8px; font-weight: bold; cursor: pointer; transition: all 0.2s;"><i class="fa-solid fa-cloud-arrow-up"></i> تایید و انتشار مانهوا در سنترال دیتابیس</button>
                    </div>
                </form>
            </div>
        <?php endif; ?>
    <?php endif; ?>

    <!-- 💳 Web Wallet Refill Sandbox Console (Allows charging for VIP reads) -->
    <?php if ( is_user_logged_in() ) : ?>
        <h2 class="section-head" id="wallet-charging-hub"><i class="fa-solid fa-wallet" style="margin-left: 8px;"></i>کیف‌پول تستی کاربران متصل به سایت</h2>
        <div style="background-color: #16191E; border: 1.5px solid #2D3139; border-radius: 16px; padding: 25px; margin-bottom: 40px; display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 20px;">
            <div>
                <h3 style="color:#ffffff; font-size: 16px; font-weight: bold;">تست شارژ کیف‌پول واقعی وب‌سایت (به دیتابیس متمرکز)</h3>
                <p style="color: #9AA0A6; font-size: 11px; margin-top: 5px;">مبلغ مورد نظر خود را وارد کنید تا اعتبار ریالی بلافاصله به اطلاعات کاربری شما در دیتابیس mr-v.ir واریز و در اپلیکیشن اندروید نیز سینک شود.</p>
            </div>
            <form action="<?php echo esc_url( home_url('/') ); ?>" method="POST" style="display: flex; align-items: center; gap: 10px;">
                <input type="hidden" name="mangata_web_action" value="charge_wallet">
                <select name="amount" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px 15px; color: #ffffff; font-weight: bold;">
                    <option value="50000">۵۰,۰۰۰ ریال شارژ تستی</option>
                    <option value="100000" selected>۱۰۰,۰۰۰ ریال شارژ تستی</option>
                    <option value="250000">۲۵۰,۰۰۰ ریال شارژ طلایی تستی</option>
                    <option value="500000">۵۰۰,۰۰۰ ریال شارژ فوق‌العاده</option>
                </select>
                <button type="submit" style="background: linear-gradient(135deg, #00C6FF, #0072FF); color: #ffffff; border: none; padding: 10px 20px; border-radius: 8px; font-weight: bold; cursor: pointer; transition: all 0.2s;"><i class="fa-solid fa-coins"></i> افزایش موجودی زنده</button>
            </form>
        </div>
    <?php endif; ?>

    <h2 class="section-head" id="catalog-header">کاتالوگ آثار بروز شده مانگاتا (دیتابیس متمرکز)</h2>
    <div class="manga-grid">
        <?php
        $args = array(
            'post_type'      => 'manga',
            'posts_per_page' => -1,
            'post_status'    => 'publish'
        );

        $query = new WP_Query($args);
        $manga_count = 0;

        if ($query->have_posts()) :
            while ($query->have_posts()) : $query->the_post();
                $id = get_the_ID();
                $manga_count++;
                
                $title_en = get_post_meta($id, '_manga_title_en', true) ?: 'Solo Leveling';
                $type = get_post_meta($id, '_manga_type', true) ?: 'مانهوا';
                $rating = get_post_meta($id, '_manga_rating', true) ?: '4.9';
                $status = get_post_meta($id, '_manga_status', true) ?: 'در حال انتشار';
                $chapters_count = get_post_meta($id, '_manga_chapters_count', true) ?: '150';
                $is_premium = get_post_meta($id, '_manga_is_premium', true);
                
                $cover_url = get_the_post_thumbnail_url($id, 'medium');
                if (empty($cover_url)) {
                    $cover_url = get_post_meta($id, '_manga_cover_url', true);
                }
                if (empty($cover_url)) {
                    $cover_url = 'https://picsum.photos/id/1025/400/600';
                }
        ?>
                <!-- Dynamic Manga Card -->
                <article class="manga-card" onclick="alert('عنوان: <?php the_title(); ?>\nآی‌دی اثر: <?php echo $id; ?>\n\nاین اثر با موفقیت بر روی دیتابیس وب‌سایت ثبت شده و بلافاصله در اپلیکیشن اندروید شما قابل مطالعه آنلاین و دانلود آفلاین است!')">
                    <div class="manga-cover-wrapper">
                        <img src="<?php echo esc_url($cover_url); ?>" alt="<?php the_title_attribute(); ?>">
                        <span class="status-badge" style="<?php echo ($status === 'پایان یافته') ? 'background-color: rgba(60, 10, 10, 0.85); color: #ff5252; border-color: #cc0033;' : ''; ?>"><?php echo esc_html($status); ?></span>
                    </div>
                    <div class="manga-details">
                        <div class="manga-sub">
                            <span><?php echo esc_html($type); ?></span>
                            <span><i class="fa-solid fa-star" style="color: #FFD700;"></i> <?php echo esc_html($rating); ?></span>
                        </div>
                        <h3 class="manga-title"><?php the_title(); ?></h3>
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <span class="chapters-badge"><?php echo esc_html($chapters_count); ?> فصل</span>
                            <?php if ($is_premium === '1' || $is_premium === 'true') : ?>
                                <span style="background-color: #3b111a; border: 1px solid #cc0033; color: #ff8093; font-size: 9px; padding: 2px 6px; border-radius: 4px; font-weight: bold;"><i class="fa-solid fa-crown" style="font-size: 8px;"></i> ویژه / VIP</span>
                            <?php else : ?>
                                <span style="background-color: #0d3c26; border: 1px solid #196f43; color: #3cd070; font-size: 9px; padding: 2px 6px; border-radius: 4px; font-weight: bold;">رایگان</span>
                            <?php endif; ?>
                        </div>
                    </div>
                </article>
        <?php
            endwhile;
            wp_reset_postdata();
        endif;

        // Fallback seed inside server UI if database has 0 postings
        if ($manga_count == 0) :
        ?>
            <!-- Fallback Manga Card 1 -->
            <article class="manga-card" onclick="alert('سولو لولینگ (تک‌رو) • این اثر پیش‌فرض است.')">
                <div class="manga-cover-wrapper">
                    <img src="https://picsum.photos/id/1025/400/600" alt="سولو لولینگ">
                    <span class="status-badge">پایان یافته</span>
                </div>
                <div class="manga-details">
                    <div class="manga-sub">
                        <span>مانهوا کره</span>
                        <span><i class="fa-solid fa-star" style="color: #FFD700;"></i> ۴.۹</span>
                    </div>
                    <h3 class="manga-title">سولو لولینگ (تک‌رو)</h3>
                    <span class="chapters-badge">۱۷۹ فصل ترجمه شده</span>
                </div>
            </article>

            <!-- Fallback Manga Card 2 -->
            <article class="manga-card" onclick="alert('برج خدا • این اثر پیش‌فرض است.')">
                <div class="manga-cover-wrapper">
                    <img src="https://picsum.photos/id/1027/400/600" alt="برج خدا">
                    <span class="status-badge">در حال انتشار</span>
                </div>
                <div class="manga-details">
                    <div class="manga-sub">
                        <span>مانهواکاپ</span>
                        <span><i class="fa-solid fa-star" style="color: #FFD700;"></i> ۴.۸</span>
                    </div>
                    <h3 class="manga-title">برج خدا (Tower Of God)</h3>
                    <span class="chapters-badge">۵۹۰ فصل • VIP</span>
                </div>
            </article>
        <?php
        endif;
        ?>
    </div> <!-- close manga-grid -->

    <!-- 👑 Myket & Host Web VIP Store Pricing Plan Section -->
    <h2 class="section-head" id="vippricing">طرح‌های ارتقای کاربری VIP مانگاتا</h2>
    <div class="store-wrapper">
        <div class="plan-card">
            <div class="plan-title">اشتراک ویژه ۱ ماهه برنزی</div>
            <p style="color: #9AA0A6; font-size: 11px;">تک کاربره، جهت تسریع گام‌های ترجمه</p>
            <div class="plan-price">۲۹,۰۰۰ تومان</div>
            <hr style="border-color: #2D3139; margin-bottom: 20px;">
            <p style="font-size: 13px; color: #BDC1C6; margin-bottom: 20px;"><i class="fa-solid fa-check" style="color:#59B259; margin-left:8px;"></i>دسترسی به ۳۰ فصل پیش‌نویس</p>
            <p style="font-size: 13px; color: #BDC1C6; margin-bottom: 20px;"><i class="fa-solid fa-check" style="color:#59B259; margin-left:8px;"></i>دانلود لوکال در اندروید</p>
            <button class="plan-btn">خرید با کلیه کارت‌های شتاب</button>
        </div>

        <div class="plan-card" style="border-color: #ffd700; background-color: #1a1506;">
            <div class="plan-title" style="color: #ffd700;"><i class="fa-solid fa-crown"></i> اشتراک ویژه ۳ ماهه طلایی</div>
            <p style="color: #bfa100; font-size: 11px;">پیشنهاد ویژه تیم ترجمه و کلینرها</p>
            <div class="plan-price" style="color: #ffd700;">۶۹,۰۰۰ تومان</div>
            <hr style="border-color: #4a3b0a; margin-bottom: 20px;">
            <p style="font-size: 13px; color: #ffd700; margin-bottom: 20px;"><i class="fa-solid fa-check" style="margin-left:8px;"></i>دسترسی نامحدود به کلیه آثار</p>
            <p style="font-size: 13px; color: #ffd700; margin-bottom: 20px;"><i class="fa-solid fa-check" style="margin-left:8px;"></i>ترجمه بدون سانسور حباب‌ها</p>
            <button class="plan-btn gold-plan-btn">خرید با درگاه مایکت / شتاب</button>
        </div>

        <div class="plan-card">
            <div class="plan-title">اشتراک طلایی مادام‌العمر (Lifetime)</div>
            <p style="color: #9AA0A6; font-size: 11px;">خرید برای همیشه بدون شارژ مجدد</p>
            <div class="plan-price">۱۸۹,۰۰۰ تومان</div>
            <hr style="border-color: #2D3139; margin-bottom: 20px;">
            <p style="font-size: 13px; color: #BDC1C6; margin-bottom: 20px;"><i class="fa-solid fa-check" style="color:#59B259; margin-left:8px;"></i>دسترسی همیشگی به تمام کارها</p>
            <p style="font-size: 13px; color: #BDC1C6; margin-bottom: 20px;"><i class="fa-solid fa-check" style="color:#59B259; margin-left:8px;"></i>بدون منقضی شدن لایسنس</p>
            <button class="plan-btn">خرید و لایسنس ابدی</button>
        </div>
    </div>

    <!-- 👥 Team Hub / Workspace Sync Section -->
    <h2 class="section-head" id="collaborators">پنل هماهنگی کادر ترجمه مانگاتا</h2>
    <div class="team-dashboard">
        <p style="color: #9AA0A6; font-size: 12px; margin-bottom: 20px;">
            اعضای تیم ترجمه و مدیران ما به طور شبانه‌روزی زحمت پاک‌سازی (Cleaning)، ریدرافت حباب‌ها (Redraw) و جاسازی متون مانهوا را متحمل می‌شوند.
        </p>
        <div class="team-grid">
            <div class="team-member-card">
                <div class="member-avatar">الف</div>
                <div>
                    <h3 style="font-size: 15px; color:#ffffff;">امیررضا</h3>
                    <span class="member-role" style="background-color: #ffd700; color:#121419;">مدیر کل و موسس</span>
                    <p style="font-size: 10px; color:#C3C7CF; margin-top:5px;">پروژه: مدیریت زیرساخت mr-v.ir</p>
                </div>
            </div>

            <div class="team-member-card">
                <div class="member-avatar">م</div>
                <div>
                    <h3 style="font-size: 15px; color:#ffffff;">مهدی خسروی</h3>
                    <span class="member-role">سرپرست مترجمان</span>
                    <p style="font-size: 10px; color:#C3C7CF; margin-top:5px;">پروژه: سولو لولینگ، برج خدا</p>
                </div>
            </div>

            <div class="team-member-card">
                <div class="member-avatar">ت</div>
                <div>
                    <h3 style="font-size: 15px; color:#ffffff;">تینا مهدوی</h3>
                    <span class="member-role" style="background-color: #E21B5A;">تایپیست و کلینر</span>
                    <p style="font-size: 10px; color:#C3C7CF; margin-top:5px;">پروژه: خانه شیرین، برج خدا</p>
                </div>
            </div>
        </div>
    </div>
</div>

<?php get_footer(); ?>
