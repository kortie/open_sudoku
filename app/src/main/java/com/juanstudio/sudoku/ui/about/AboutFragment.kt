package com.juanstudio.sudoku.ui.about

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.billingclient.api.ProductDetails
import com.juanstudio.sudoku.R
import com.juanstudio.sudoku.databinding.FragmentAboutBinding

/**
 * About screen: app/developer info, Google Play "coffee" donations, and open source license notices.
 */
class AboutFragment : Fragment(), DonationBilling.Listener {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    private var billing: DonationBilling? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        val versionName = context.packageManager
            .getPackageInfo(context.packageName, 0).versionName ?: ""
        binding.textVersion.text = getString(R.string.about_version, versionName)

        binding.textLibraryList.text = OPEN_SOURCE_LIBRARIES.joinToString("\n") { "♡ $it" }

        binding.buttonViewLicense.setOnClickListener {
            showLicenseDialog()
        }

        // Donation buttons stay disabled until Google Play returns the products and prices.
        donationButtons().values.forEach { setButtonEnabled(it, false) }
        billing = DonationBilling(context, this).also { it.start() }
    }

    private fun donationButtons(): Map<String, TextView> = mapOf(
        DonationBilling.PRODUCT_COFFEE_SMALL to binding.buttonCoffeeSmall,
        DonationBilling.PRODUCT_COFFEE_MEDIUM to binding.buttonCoffeeMedium,
        DonationBilling.PRODUCT_COFFEE_LARGE to binding.buttonCoffeeLarge
    )

    private fun setButtonEnabled(button: TextView, enabled: Boolean) {
        button.isEnabled = enabled
        button.alpha = if (enabled) 1f else 0.5f
    }

    override fun onProductsLoaded(products: List<ProductDetails>) {
        val binding = _binding ?: return
        val byId = products.associateBy { it.productId }
        for ((productId, button) in donationButtons()) {
            val product = byId[productId]
            if (product == null) {
                button.visibility = View.GONE
                continue
            }
            val price = product.oneTimePurchaseOfferDetails?.formattedPrice
            if (price != null) {
                button.text = getString(R.string.donate_with_price, button.text, price)
            }
            setButtonEnabled(button, true)
            button.setOnClickListener {
                billing?.launchDonation(requireActivity(), product)
            }
        }
        binding.textDonationStatus.setText(
            if (products.isEmpty()) R.string.donation_unavailable else R.string.donation_note
        )
    }

    override fun onDonationCompleted() {
        if (_binding == null) return
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.donation_thanks_title)
            .setMessage(R.string.donation_thanks_body)
            .setPositiveButton(R.string.close, null)
            .show()
    }

    override fun onDonationPending() {
        if (_binding == null) return
        Toast.makeText(requireContext(), R.string.donation_pending, Toast.LENGTH_LONG).show()
    }

    override fun onDonationFailed() {
        if (_binding == null) return
        Toast.makeText(requireContext(), R.string.donation_failed, Toast.LENGTH_SHORT).show()
    }

    private fun showLicenseDialog() {
        val licenseText = resources.openRawResource(R.raw.apache_license_2_0)
            .bufferedReader().use { it.readText() }
        val padding = (20 * resources.displayMetrics.density).toInt()
        val textView = TextView(requireContext()).apply {
            text = licenseText
            textSize = 11f
            typeface = android.graphics.Typeface.MONOSPACE
            setTextColor(requireContext().getColor(R.color.text_primary))
            setPadding(padding, padding, padding, padding)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Apache License 2.0")
            .setView(ScrollView(requireContext()).apply { addView(textView) })
            .setPositiveButton(R.string.close, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        billing?.end()
        billing = null
        _binding = null
    }

    companion object {
        // Runtime dependencies shipped in the APK (all Apache License 2.0).
        private val OPEN_SOURCE_LIBRARIES = listOf(
            "AndroidX Core, AppCompat, ConstraintLayout — The Android Open Source Project",
            "AndroidX Lifecycle, Navigation — The Android Open Source Project",
            "Material Components for Android — Google",
            "Gson — Google",
            "Android Data Transport — Google",
            "Kotlin Standard Library, kotlinx.coroutines — JetBrains"
        )
    }
}
