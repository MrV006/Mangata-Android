package com.example.myket

import android.app.Activity
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.util.UUID

/**
 * Standard simulated Iab (In-App Billing) Helper for Myket App Store (پرداخت درون برنامه‌ای مایکت).
 * This class mirrors the structure of Myket's standard Billing AIDL Helper, allowing
 * clean separation of concerns, realistic callback parameters, and authentic digital token logging.
 */
class MyketBillingHelper(private val context: Context) {

    private val LOG_TAG = "MyketBillingHelper"
    private var isSetupDone = false
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Simulated SKUs for Myket App Store
    val SKU_VIP_1MONTH = "vip_1month"
    val SKU_VIP_3MONTH = "vip_3month"
    val SKU_VIP_LIFETIME = "vip_lifetime"

    // Sku Details
    data class SkuDetails(
        val sku: String,
        val title: String,
        val priceFa: String,
        val priceToman: Int,
        val description: String
    )

    data class Purchase(
        val orderId: String,
        val packageName: String,
        val sku: String,
        val purchaseTime: Long,
        val purchaseToken: String,
        val developerPayload: String
    )

    data class IabResult(val responseCode: Int, val message: String) {
        val isSuccess: Boolean get() = responseCode == BILLING_RESPONSE_RESULT_OK
    }

    companion object {
        const val BILLING_RESPONSE_RESULT_OK = 0
        const val BILLING_RESPONSE_RESULT_USER_CANCELED = 1
        const val BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3
        const val BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7
        const val BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5
    }

    // Available products in Myket catalog for this app
    val skuDetailsMap = mapOf(
        SKU_VIP_1MONTH to SkuDetails(
            sku = SKU_VIP_1MONTH,
            title = "اشتراک طلایی ۱ ماهه VIP",
            priceFa = "۳۹,۰۰۰ تومان",
            priceToman = 39000,
            description = "دسترسی نامحدود ۳۰ روزه به آرشیو فیلم و سریال‌های ویژه"
        ),
        SKU_VIP_3MONTH to SkuDetails(
            sku = SKU_VIP_3MONTH,
            title = "اشتراک طلایی ۳ ماهه VIP",
            priceFa = "۹۹,۰۰۰ تومان",
            priceToman = 99000,
            description = "دسترسی نامحدود ۹۰ روزه به آرشیو فیلم و سریال‌های ویژه به همراه تخفیف ویژه"
        ),
        SKU_VIP_LIFETIME to SkuDetails(
            sku = SKU_VIP_LIFETIME,
            title = "اشتراک ابدی VIP (مادام‌العمر)",
            priceFa = "۲۹۹,۰۰۰ تومان",
            priceToman = 299000,
            description = "یک بار خرید برای همیشه، تماشای تمام فیلم‌ها بدون نیاز به تمدید"
        )
    )

    /**
     * Set upconnection with Myket Billing Service
     */
    fun startSetup(listener: (IabResult) -> Unit) {
        Log.d(LOG_TAG, "Connecting to Myket Service...")
        scope.launch {
            delay(1200) // Simulating binding service delay
            isSetupDone = true
            Log.d(LOG_TAG, "Myket core service connected successfully.")
            listener(IabResult(BILLING_RESPONSE_RESULT_OK, "با موفقیت به سرویس مایکت متصل شد."))
        }
    }

    /**
     * Query inventory for purchased status of premium items
     */
    fun queryInventory(
        skus: List<String>,
        currentlyOwnedSkus: List<String>,
        listener: (IabResult, List<Purchase>) -> Unit
    ) {
        Log.d(LOG_TAG, "Querying user purchases from Myket servers...")
        scope.launch {
            delay(1000) // Simulating network latency
            if (!isSetupDone) {
                listener(IabResult(BILLING_RESPONSE_RESULT_DEVELOPER_ERROR, "سرویس راه‌اندازی نشده است."), emptyList())
                return@launch
            }

            // Build realistic purchases based on db records passed in
            val purchasesList = currentlyOwnedSkus.map { sku ->
                Purchase(
                    orderId = "mkt-${UUID.randomUUID().toString().substring(0, 8)}",
                    packageName = context.packageName,
                    sku = sku,
                    purchaseTime = System.currentTimeMillis() - 86400000,
                    purchaseToken = "token-${UUID.randomUUID()}",
                    developerPayload = "verified_payload"
                )
            }
            listener(IabResult(BILLING_RESPONSE_RESULT_OK, "سرویس استعلام خرید با موفقیت پاسخ داد."), purchasesList)
        }
    }

    /**
     * Simulate buying flow.
     * In our Jetpack Compose application, when this is triggered, we will toggle the ViewModel status
     * which prompts the UI to display the custom highly-realistic 'Myket Checkout' Dialog.
     * When completed on that Dialog, it fires the purchase callback.
     */
    fun launchPurchaseFlow(
        activity: Activity,
        sku: String,
        developerPayload: String,
        onBought: (IabResult, Purchase?) -> Unit
    ) {
        Log.d(LOG_TAG, "Launching Myket Checkout Flow for SKU: $sku...")
        if (!isSetupDone) {
            onBought(IabResult(BILLING_RESPONSE_RESULT_DEVELOPER_ERROR, "سرویس ست‌آپ نشده است."), null)
            return
        }

        // We will hand over the flow to the UI via the state.
        // This helper will contain the model generators that the ViewModel utilizes.
    }

    /**
     * Generates a purchase record upon successful payment on our custom Myket Sheet.
     */
    fun generateSuccessfulPurchase(sku: String, developerPayload: String): Purchase {
        return Purchase(
            orderId = "mkt-${UUID.randomUUID().toString().substring(0, 12)}",
            packageName = context.packageName,
            sku = sku,
            purchaseTime = System.currentTimeMillis(),
            purchaseToken = "myk-token-${UUID.randomUUID().toString().replace("-", "")}",
            developerPayload = developerPayload
        )
    }
}
