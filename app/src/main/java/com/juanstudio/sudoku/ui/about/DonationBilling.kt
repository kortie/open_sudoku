package com.juanstudio.sudoku.ui.about

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

/**
 * Google Play Billing wrapper for "buy the developer a coffee" donations.
 *
 * Donations are consumable one-time products: each purchase is consumed right away
 * so the same product can be bought again. Nothing is unlocked, so purchases are
 * not verified on a server.
 *
 * The product IDs in [PRODUCT_IDS] must exist (and be active) in Play Console for
 * this package, otherwise [Listener.onProductsLoaded] receives an empty list.
 */
class DonationBilling(context: Context, private val listener: Listener) : PurchasesUpdatedListener {

    interface Listener {
        /** Products available for donation, in [PRODUCT_IDS] order; empty when billing is unavailable. */
        fun onProductsLoaded(products: List<ProductDetails>)
        fun onDonationCompleted()
        fun onDonationPending()
        fun onDonationFailed()
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    private val client = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .enableAutoServiceReconnection()
        .build()

    fun start() {
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingResponseCode.OK) {
                    queryProducts()
                    consumeLeftoverPurchases()
                } else {
                    Log.w(TAG, "Billing setup failed: ${result.responseCode} ${result.debugMessage}")
                    onMain { listener.onProductsLoaded(emptyList()) }
                }
            }

            // enableAutoServiceReconnection() re-establishes the connection on the next request.
            override fun onBillingServiceDisconnected() = Unit
        })
    }

    fun end() {
        mainHandler.removeCallbacksAndMessages(null)
        client.endConnection()
    }

    fun launchDonation(activity: Activity, product: ProductDetails) {
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(product)
                        .build()
                )
            )
            .build()
        val result = client.launchBillingFlow(activity, params)
        if (result.responseCode != BillingResponseCode.OK) {
            Log.w(TAG, "launchBillingFlow failed: ${result.responseCode} ${result.debugMessage}")
            listener.onDonationFailed()
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingResponseCode.OK -> purchases?.forEach { handlePurchase(it, notify = true) }
            BillingResponseCode.USER_CANCELED -> Unit
            else -> {
                Log.w(TAG, "Purchase failed: ${result.responseCode} ${result.debugMessage}")
                onMain { listener.onDonationFailed() }
            }
        }
    }

    private fun queryProducts() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                PRODUCT_IDS.map {
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(it)
                        .setProductType(ProductType.INAPP)
                        .build()
                }
            )
            .build()
        client.queryProductDetailsAsync(params) { result, queryResult ->
            val products = if (result.responseCode == BillingResponseCode.OK) {
                // Products missing or inactive in Play Console come back here with a status code.
                queryResult.unfetchedProductList.forEach {
                    Log.w(TAG, "Product not available: ${it.productId} (status ${it.statusCode})")
                }
                queryResult.productDetailsList.sortedBy { PRODUCT_IDS.indexOf(it.productId) }
            } else {
                Log.w(TAG, "Product query failed: ${result.responseCode} ${result.debugMessage}")
                emptyList()
            }
            onMain { listener.onProductsLoaded(products) }
        }
    }

    /** Consumes donations that completed while the app wasn't listening (e.g. killed mid-purchase). */
    private fun consumeLeftoverPurchases() {
        val params = QueryPurchasesParams.newBuilder().setProductType(ProductType.INAPP).build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingResponseCode.OK) {
                purchases.forEach { handlePurchase(it, notify = false) }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase, notify: Boolean) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                val params = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                client.consumeAsync(params) { result, _ ->
                    if (result.responseCode == BillingResponseCode.OK) {
                        if (notify) onMain { listener.onDonationCompleted() }
                    } else {
                        Log.w(TAG, "Consume failed: ${result.responseCode} ${result.debugMessage}")
                    }
                }
            }
            Purchase.PurchaseState.PENDING -> if (notify) onMain { listener.onDonationPending() }
            else -> Unit
        }
    }

    private fun onMain(block: () -> Unit) {
        mainHandler.post(block)
    }

    companion object {
        private const val TAG = "DonationBilling"

        // One-time (consumable) product IDs to create in Play Console.
        const val PRODUCT_COFFEE_SMALL = "coffee_small"
        const val PRODUCT_COFFEE_MEDIUM = "coffee_medium"
        const val PRODUCT_COFFEE_LARGE = "coffee_large"
        val PRODUCT_IDS = listOf(PRODUCT_COFFEE_SMALL, PRODUCT_COFFEE_MEDIUM, PRODUCT_COFFEE_LARGE)
    }
}
