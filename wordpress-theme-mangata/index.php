<?php get_header(); ?>

<?php if ( is_user_logged_in() ) : 
    // Fetch real mangas from Database (WP_Query)
    $args = array(
        'post_type'      => 'manga',
        'posts_per_page' => -1,
        'post_status'    => 'publish'
    );
    $query = new WP_Query($args);
    $mangas = [];
    if ($query->have_posts()) {
        while ($query->have_posts()) {
            $query->the_post();
            $id = get_the_ID();
            $title_fa = get_the_title();
            $title_en = get_post_meta($id, '_manga_title_en', true) ?: 'Manga';
            $type = get_post_meta($id, '_manga_type', true) ?: 'مانهوا';
            $rating = get_post_meta($id, '_manga_rating', true) ?: '4.9';
            $status = get_post_meta($id, '_manga_status', true) ?: 'در حال انتشار';
            $chapters_count = get_post_meta($id, '_manga_chapters_count', true) ?: '150';
            $is_premium = get_post_meta($id, '_manga_is_premium', true);
            $description = get_post_meta($id, '_manga_description', true) ?: get_the_excerpt() ?: 'خلاصه داستان برای این اثر ثبت نشده است.';
            
            $cover_url = get_the_post_thumbnail_url($id, 'medium');
            if (empty($cover_url)) {
                $cover_url = get_post_meta($id, '_manga_cover_url', true);
            }
            if (empty($cover_url)) {
                $cover_url = 'https://picsum.photos/id/1025/400/600';
            }

            $banner_url = get_post_meta($id, '_manga_banner_url', true);
            if (empty($banner_url)) {
                $banner_url = 'https://picsum.photos/id/1025/1200/600';
            }

            $mangas[] = [
                'id' => $id,
                'title_fa' => $title_fa,
                'title_en' => $title_en,
                'type' => $type,
                'rating' => $rating,
                'status' => $status,
                'chapters_count' => $chapters_count,
                'is_premium' => $is_premium,
                'cover_url' => $cover_url,
                'banner_url' => $banner_url,
                'description' => $description
            ];
        }
        wp_reset_postdata();
    }
?>

    <?php if ( !empty($mangas) ) : 
        $featured = $mangas[0]; // Set first manga as featured banner dynamically
    ?>
    <!-- 🚀 Featured Header Hero Banner Dynamically Loaded from Central DB -->
    <section class="featured-hero-banner" style="background-image: url('<?php echo esc_url($featured['banner_url']); ?>');">
        <div class="hero-content">
            <span class="type-tag"><?php echo esc_html($featured['type']); ?> داغ هفته</span>
            <span class="rating-tag"><i class="fa-solid fa-star" style="margin-left: 4px; color:#ffd700;"></i><?php echo esc_html($featured['rating']); ?></span>
            <h1 class="hero-title"><?php echo esc_html($featured['title_fa']); ?> • <?php echo esc_html($featured['title_en']); ?></h1>
            <p class="hero-meta">وضعیت: <?php echo esc_html($featured['status']); ?> • کل فصول: <?php echo esc_html($featured['chapters_count']); ?></p>
            <p style="max-width: 650px; color: #BDC1C6; font-size: 13px; margin-bottom: 20px;">
                <?php echo esc_html($featured['description']); ?>
            </p>
            <a href="#mangacatalog" class="reader-btn" style="background: linear-gradient(135deg, #00C6FF, #0072FF); padding: 10px 24px; border-radius: 12px; font-weight: bold;"><i class="fa-solid fa-play" style="margin-left:8px;"></i>شروع به خواندن اثر</a>
        </div>
    </section>
    <?php endif; ?>

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

        <?php 
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
                            <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">خلاصه داستان مانهوا (توضیحات)</label>
                            <textarea name="description" placeholder="داستان جذاب این مانهوا..." style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #ffffff; height: 80px;"></textarea>
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

                <!-- 📦 Batch Uploading System (پنل بارگذاری مدیریت دسته‌ای تصاویر مانهوا) -->
                <div id="batch-uploader-console" style="background-color: #16191E; border: 1.5px solid #2D3139; border-radius: 16px; padding: 25px; margin-bottom: 40px; box-shadow: 0 4px 15px rgba(0,0,0,0.2);">
                    <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 10px; margin-bottom: 15px;">
                        <h3 style="color: #ffd700; font-size: 15px; font-weight: bold; margin: 0;"><i class="fa-solid fa-file-zipper" style="margin-left:8px; color: #ffd700;"></i>پنل بارگذاری مدیریت دسته‌ای صفحات مانهوا (Batch ZIP Extract & Map)</h3>
                        <span style="background-color: #ffd700; color: #101216; font-size: 10px; padding: 3px 8px; border-radius: 6px; font-weight: bold;">پشتیبانی از فرمت زیپ</span>
                    </div>
                    <p style="color: #9AA0A6; font-size: 12px; line-height: 1.6; margin-bottom: 20px;">
                        فایل آرشیو زیپ چپتر (شامل فایل‌های تصویری با پسوندهای JPG, PNG, WEBP) خود را بارگذاری کنید. هسته اتوماتیک مانگاتا صفحات را زیپ‌گشایی کرده، ترتیب طبیعی تصاویر را مرتب کرده و آدرس‌های هاست واقعیشان را به صورت کدهای یکپارچه JSON وب‌تون‌خوان برای شما آماده می‌کند تا در آثار جدید پیست کنید!
                    </p>

                    <div style="display: flex; flex-wrap: wrap; gap: 20px;">
                        <!-- Left Panel: Upload Form -->
                        <div style="flex: 1; min-width: 280px; background-color: #1E2229; border: 1px solid #2D3139; border-radius: 12px; padding: 20px;">
                            <form action="<?php echo esc_url( home_url('/') ); ?>" method="POST" enctype="multipart/form-data" style="display: flex; flex-direction: column; gap: 15px;">
                                <input type="hidden" name="mangata_web_action" value="batch_upload_zip">
                                
                                <div style="display: flex; flex-direction: column; gap: 6px;">
                                    <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">انتخاب فایل فشرده فصول (Manga Chap ZIP File)</label>
                                    <input type="file" name="manga_zip" required accept=".zip" style="background-color: #16191E; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #ffffff; width: 100%;">
                                    <span style="color: #5F6368; font-size: 10px;">سعی کنید نام فایل‌های درون زیپ به ترتیب طبیعی باشند (مثال: 01.jpg, 02.jpg و ...).</span>
                                </div>

                                <button type="submit" style="background: linear-gradient(135deg, #00C6FF, #0072FF); color: #ffffff; border: none; padding: 12px; border-radius: 8px; font-weight: bold; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px; transition: all 0.2s;"><i class="fa-solid fa-file-zipper"></i> شروع فرآیند پردازش، زیپ‌گشایی و مپینگ صفحات</button>
                            </form>
                        </div>

                        <!-- Right Panel: Show Generated JSON Result if exits -->
                        <div style="flex: 1; min-width: 280px; background-color: #1E2229; border: 1px solid #2D3139; border-radius: 12px; padding: 20px; display: flex; flex-direction: column; gap: 10px;">
                            <?php 
                            $last_json = get_option('mangata_last_uploaded_batch_json');
                            $last_name = get_option('mangata_last_uploaded_batch_name') ?: 'تاریخچه نامعلوم';
                            if ( !empty($last_json) ) : 
                                $decoded = json_decode($last_json);
                                $count = is_array($decoded) ? count($decoded) : 0;
                            ?>
                                <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #2D3139; padding-bottom: 8px; margin-bottom: 10px;">
                                    <span style="color: #69F0AE; font-size: 11px; font-weight: bold;"><i class="fa-solid fa-circle-check"></i> آخرین مپ انجام شده (<?php echo $count; ?> تصویر)</span>
                                    <span style="color: #9AA0A6; font-size: 11px;"><?php echo esc_html($last_name); ?></span>
                                </div>
                                <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">کد آرایه خروجی وب‌تون‌خوان (آماده درج در فیلد صفحات مانهوا):</label>
                                <textarea readonly onclick="this.select(); document.execCommand('copy'); alert('کد JSON با موفقیت در کلیپ‌برد کپی شد!');" style="background-color: #16191E; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #00ff66; direction: ltr; font-family: monospace; height: 110px; font-size: 11px; cursor: pointer;" title="برای کپی کردن روی متن کلیک کنید"><?php echo esc_textarea($last_json); ?></textarea>
                                <span style="color: #888888; font-size: 10px; text-align: left; display: block;"><i class="fa-solid fa-info-circle"></i> جهت کپی، بر روی کادر بالا به سادگی کلیک نمایید.</span>
                            <?php else : ?>
                                <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; min-height: 150px; text-align: center; color: #5F6368;">
                                    <i class="fa-solid fa-hourglass-empty" style="font-size: 32px; margin-bottom: 10px;"></i>
                                    <span style="font-size: 12px; font-weight: bold;">در انتظار آپلود اولین فایل زیپ...</span>
                                    <span style="font-size: 10px; margin-top: 5px;">خروجی آدرس‌های هاست نهایی تصاویر شما بلافاصله در این بخش ظاهر خواهد شد.</span>
                                </div>
                            <?php endif; ?>
                        </div>
                    </div>
                </div>
            <?php endif; ?>

        <!-- 💳 Web Wallet Refill Sandbox Console (Allows charging for VIP reads) -->
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

        <h2 class="section-head" id="catalog-header">کاتالوگ آثار بروز شده مانگاتا (دیتابیس متمرکز)</h2>
        <div class="manga-grid">
            <?php if ( !empty($mangas) ) : ?>
                <?php foreach ( $mangas as $manga ) : ?>
                    <!-- Dynamic Manga Card -->
                    <article class="manga-card" onclick="alert('عنوان: <?php echo esc_attr($manga['title_fa']); ?>\nآی‌دی اثر: <?php echo $manga['id']; ?>\n\nاین اثر با موفقیت بر روی دیتابیس وب‌سایت ثبت شده و بلافاصله در اپلیکیشن اندروید شما قابل مطالعه آنلاین و دانلود آفلاین است!')">
                        <div class="manga-cover-wrapper">
                            <img src="<?php echo esc_url($manga['cover_url']); ?>" alt="<?php echo esc_attr($manga['title_fa']); ?>">
                            <span class="status-badge" style="<?php echo ($manga['status'] === 'پایان یافته') ? 'background-color: rgba(60, 10, 10, 0.85); color: #ff5252; border-color: #cc0033;' : ''; ?>"><?php echo esc_html($manga['status']); ?></span>
                        </div>
                        <div class="manga-details">
                            <div class="manga-sub">
                                <span><?php echo esc_html($manga['type']); ?></span>
                                <span><i class="fa-solid fa-star" style="color: #FFD700;"></i> <?php echo esc_html($manga['rating']); ?></span>
                            </div>
                            <h3 class="manga-title"><?php echo esc_html($manga['title_fa']); ?></h3>
                            <div style="display: flex; justify-content: space-between; align-items: center;">
                                <span class="chapters-badge"><?php echo esc_html($manga['chapters_count']); ?> فصل</span>
                                <?php if ($manga['is_premium'] === '1' || $manga['is_premium'] === 'true') : ?>
                                    <span style="background-color: #3b111a; border: 1px solid #cc0033; color: #ff8093; font-size: 9px; padding: 2px 6px; border-radius: 4px; font-weight: bold;"><i class="fa-solid fa-crown" style="font-size: 8px;"></i> ویژه / VIP</span>
                                <?php else : ?>
                                    <span style="background-color: #0d3c26; border: 1px solid #196f43; color: #3cd070; font-size: 9px; padding: 2px 6px; border-radius: 4px; font-weight: bold;">رایگان</span>
                                <?php endif; ?>
                            </div>
                        </div>
                    </article>
                <?php endforeach; ?>
            <?php else : ?>
                <!-- Clean Empty State - ZERO Simulator/Fake items shown -->
                <div style="grid-column: 1 / -1; background-color: #16191E; border: 1.5px dashed #2D3139; color: #BDC1C6; padding: 40px; border-radius: 16px; text-align: center; font-weight: bold; font-size: 15px; width: 100%;">
                    <i class="fa-solid fa-folder-open" style="font-size: 38px; color: #5F6368; display: block; margin-bottom: 15px;"></i>
                    <span>هیچ اثری در پایگاه داده پیدا نشد. لطفاً از طریق داشبورد مدیریت فوق، مانهوای جدید ثبت کنید تا منتشر شود.</span>
                </div>
            <?php endif; ?>
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

<?php else : ?>
    <!-- 🔒 Web Access Gate (Required Registration/Login inside the Home Gateway) -->
    <div class="web-gate-container" style="display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: calc(100vh - 80px); padding: 40px; background: radial-gradient(circle at center, #11141A 0%, #06080C 100%);">
        <div style="text-align: center; margin-bottom: 30px; animation: fadeIn 0.8s ease;">
            <div class="brand-logo-m" style="margin: 0 auto 15px auto; width: 60px; height: 60px; font-size: 32px; line-height: 60px; text-align: center;">M</div>
            <h2 style="color: #ffffff; font-size: 26px; font-weight: 900; letter-spacing: -0.5px;">دروازه ورود و عضویت باشگاه مانگاتا</h2>
            <p style="color: #9AA0A6; font-size: 14px; max-width: 450px; margin: 10px auto 0 auto; line-height: 1.6;">جهت استفاده از کتابخانه آثار، پنل کلاینت و تماشای آنلاین مانهواها و مانگاها، باید ابتدا وارد حساب خود شوید یا ثبت‌نام کنید.</p>
        </div>

        <!-- Error/Success Notices for Login/Register inside Gate -->
        <?php if ( isset($_GET['mangata_success']) ) : ?>
            <div style="background-color: #0d3c26; border: 1.5px solid #196f43; color: #3cd070; padding: 12px 20px; border-radius: 12px; font-weight: bold; font-size: 13px; margin-bottom: 20px; width: 100%; max-width: 420px; text-align: center;">
                <i class="fa-solid fa-circle-check" style="margin-left: 6px;"></i><?php echo esc_html(urldecode($_GET['mangata_success'])); ?>
            </div>
        <?php endif; ?>

        <?php if ( isset($_GET['mangata_error']) ) : ?>
            <div style="background-color: #3b1313; border: 1.5px solid #7c2222; color: #ff5252; padding: 12px 20px; border-radius: 12px; font-weight: bold; font-size: 13px; margin-bottom: 20px; width: 100%; max-width: 420px; text-align: center;">
                <i class="fa-solid fa-circle-exclamation" style="margin-left: 6px;"></i><?php echo esc_html(urldecode($_GET['mangata_error'])); ?>
            </div>
        <?php endif; ?>

        <div style="display: flex; flex-wrap: wrap; justify-content: center; gap: 30px; width: 100%; max-width: 860px; direction: rtl;">
            <!-- Card 1: Login Form -->
            <div style="background-color: #16191E; border: 1.5px solid #2D3139; border-radius: 20px; padding: 30px; flex: 1; min-width: 300px; box-shadow: 0 10px 25px rgba(0,0,0,0.3); text-align: right;">
                <div style="margin-bottom: 20px;">
                    <h3 style="color: #ffffff; font-size: 18px; font-weight: bold;"><i class="fa-solid fa-right-to-bracket" style="color: #00C6FF; margin-left: 8px;"></i>ورود به حساب کاربری</h3>
                    <p style="color: #9AA0A6; font-size: 11px; margin-top: 5px;">اگر قبلاً ثبت‌نام کرده‌اید، اطلاعات خود را وارد کنید.</p>
                </div>
                <form action="<?php echo esc_url( home_url('/') ); ?>" method="POST" style="display: flex; flex-direction: column; gap: 15px;">
                    <input type="hidden" name="mangata_web_action" value="login">
                    <div style="display: flex; flex-direction: column; gap: 6px;">
                        <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">نام کاربری</label>
                        <input type="text" name="username" required placeholder="مثال: amirreza" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #ffffff; direction: ltr;">
                    </div>
                    <div style="display: flex; flex-direction: column; gap: 6px;">
                        <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">رمز عبور</label>
                        <input type="password" name="password" required placeholder="••••••••" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #ffffff; direction: ltr;">
                    </div>
                    <button type="submit" style="background: linear-gradient(135deg, #00C6FF, #0072FF); color: #ffffff; border: none; padding: 12px; border-radius: 8px; font-weight: bold; cursor: pointer; margin-top: 10px;"><i class="fa-solid fa-right-to-bracket"></i> ورود ایمن به سایت</button>
                </form>
            </div>

            <!-- Card 2: Register Form -->
            <div style="background-color: #16191E; border: 1.5px solid #2D3139; border-radius: 20px; padding: 30px; flex: 1; min-width: 300px; box-shadow: 0 10px 25px rgba(0,0,0,0.3); text-align: right;">
                <div style="margin-bottom: 20px;">
                    <h3 style="color: #ffffff; font-size: 18px; font-weight: bold;"><i class="fa-solid fa-user-plus" style="color: #ffd700; margin-left: 8px;"></i>ایجاد حساب کاربری جدید</h3>
                    <p style="color: #9AA0A6; font-size: 11px; margin-top: 5px;">هم‌اکنون ثبت‌نام کنید و پکیج هدیه شارژ کیف‌پول را دریافت نمایید.</p>
                </div>
                <form action="<?php echo esc_url( home_url('/') ); ?>" method="POST" style="display: flex; flex-direction: column; gap: 15px;">
                    <input type="hidden" name="mangata_web_action" value="register">
                    <div style="display: flex; flex-direction: column; gap: 4px;">
                        <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">نام کاربری (حروف انگلیسی)</label>
                        <input type="text" name="username" required placeholder="مثال: amirreza45" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #ffffff; direction: ltr;">
                    </div>
                    <div style="display: flex; flex-direction: column; gap: 4px;">
                        <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">نام نمایشی (فارسی/انگلیسی)</label>
                        <input type="text" name="display_name" placeholder="مثال: امیررضا" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #ffffff;">
                    </div>
                    <div style="display: flex; flex-direction: column; gap: 4px;">
                        <label style="color: #BDC1C6; font-size: 11px; font-weight: bold;">رمز عبور حساب</label>
                        <input type="password" name="password" required placeholder="••••••••" style="background-color: #1E2229; border: 1px solid #2D3139; border-radius: 8px; padding: 10px; color: #ffffff; direction: ltr;">
                    </div>
                    <button type="submit" style="background: linear-gradient(135deg, #ffd700, #ffa500); color: #101216; border: none; padding: 12px; border-radius: 8px; font-weight: bold; cursor: pointer; margin-top: 10px;"><i class="fa-solid fa-user-plus"></i> ثبت‌نام و دریافت هدیه خوش‌آمدگویی</button>
                </form>
            </div>
        </div>
    </div>
<?php endif; ?>

<?php get_footer(); ?>
