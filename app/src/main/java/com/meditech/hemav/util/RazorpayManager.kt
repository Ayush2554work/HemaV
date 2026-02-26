package com.meditech.hemav.util

import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.meditech.hemav.MainActivity
import com.razorpay.Checkout
import org.json.JSONObject

object RazorpayManager {

    /**
     * Initializes Razorpay module and opens the Checkout UI.
     * @param activity The Activity context required by Razorpay.
     * @param amountInInr The amount to be charged, in INR.
     * @param onResult Callback when payment succeeds or fails.
     */
    fun startPayment(activity: ComponentActivity, amountInInr: Int, onResult: (Boolean, String?) -> Unit) {
        val checkout = Checkout()
        // Test Key for Razorpay development
        checkout.setKeyID("rzp_test_1DP5mmOlF5G5ag")

        // Cast to MainActivity to set the callback so it can notify Compose
        if (activity is MainActivity) {
            activity.paymentCallback = { success, data ->
                onResult(success, data)
            }
        }

        try {
            val options = JSONObject()
            options.put("name", "HemaV Diagnostics")
            options.put("description", "HemaV Pro Upgrade")
            options.put("theme.color", "#E2CA76") // AccentGold
            options.put("currency", "INR")
            options.put("amount", amountInInr * 100) // Amount must be in paise
            
            val user = FirebaseAuth.getInstance().currentUser
            val prefill = JSONObject()
            prefill.put("email", user?.email ?: "")
            options.put("prefill", prefill)

            checkout.open(activity, options)
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(activity, "Error initializing payment: ${e.message}", Toast.LENGTH_SHORT).show()
            onResult(false, e.message)
        }
    }
}
