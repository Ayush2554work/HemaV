package com.meditech.hemav.feature.patient.store

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.meditech.hemav.data.repository.StoreProduct

class CartViewModel : ViewModel() {
    private val _cartItems = mutableStateMapOf<StoreProduct, Int>()
    val cartItems: Map<StoreProduct, Int> get() = _cartItems

    fun addToCart(product: StoreProduct) {
        val currentCount = _cartItems[product] ?: 0
        _cartItems[product] = currentCount + 1
    }

    fun removeFromCart(product: StoreProduct) {
        val currentCount = _cartItems[product] ?: 0
        if (currentCount > 1) {
            _cartItems[product] = currentCount - 1
        } else {
            _cartItems.remove(product)
        }
    }

    fun clearCart() {
        _cartItems.clear()
    }

    fun getTotalPrice(): Double {
        return _cartItems.entries.sumOf { (product, quantity) -> product.price * quantity }
    }

    fun getItemCount(): Int {
        return _cartItems.values.sum()
    }
}
