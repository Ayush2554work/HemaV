package com.meditech.hemav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.meditech.hemav.navigation.HemaVNavGraph
import com.meditech.hemav.ui.theme.HemaVTheme

import androidx.compose.runtime.*
import androidx.compose.foundation.isSystemInDarkTheme
import com.meditech.hemav.ui.theme.LocalThemeToggle
import com.meditech.hemav.ui.theme.LocalIsDark

import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    var paymentCallback: ((Boolean, String?) -> Unit)? = null

    override fun onPaymentSuccess(razorpayPaymentID: String?, paymentData: PaymentData?) {
        paymentCallback?.invoke(true, razorpayPaymentID)
        paymentCallback = null
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        paymentCallback?.invoke(false, response)
        paymentCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDark by remember { mutableStateOf(true) }

            CompositionLocalProvider(
                LocalThemeToggle provides { isDark = !isDark },
                LocalIsDark provides isDark
            ) {
                HemaVTheme(darkTheme = isDark) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        HemaVNavGraph()
                    }
                }
            }
        }
    }
}
