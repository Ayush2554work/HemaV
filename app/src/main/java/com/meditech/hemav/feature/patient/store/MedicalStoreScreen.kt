package com.meditech.hemav.feature.patient.store

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meditech.hemav.data.repository.MedicineRepository
import com.meditech.hemav.data.repository.StoreProduct
import com.meditech.hemav.ui.components.*
import com.meditech.hemav.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalStoreScreen(
    onBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    cartViewModel: CartViewModel
) {
    val repository = remember { MedicineRepository() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    
    val categories = listOf("All", "Blood Health", "Immunity", "Digestion", "Women's Health", "Mental Wellness", "Skin & Hair", "Pain Relief")
    
    val filteredProducts = remember(searchQuery, selectedCategory) {
        val base = if (selectedCategory == "All") repository.getProducts() else repository.getProductsByCategory(selectedCategory)
        if (searchQuery.isBlank()) base else base.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HemaV Ayurvedic Store") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (cartViewModel.getItemCount() > 0) {
                                Badge(containerColor = NeonRed) {
                                    Text(cartViewModel.getItemCount().toString())
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = onNavigateToCart) {
                            Icon(Icons.Default.ShoppingCart, "Cart")
                        }
                    }
                }
            )
        }
    ) { padding ->
        val isDark = LocalIsDark.current
        val liquidColors = if (isDark) {
            listOf(Color(0xFF003300), Color(0xFF0D2A0D), Color.Black)
        } else {
            listOf(AyurvedicGreen, NeonGreen, Color(0xFFF1F8E9))
        }

        AnimatedLiquidBackground(
            modifier = Modifier.padding(padding),
            colors = liquidColors
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar
                Box(modifier = Modifier.padding(16.dp)) {
                    GlassCard(shape = RoundedCornerShape(20.dp), backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search medicines...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = AyurvedicGreen) },
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                    }
                }

                // Categories Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        CategoryChip(
                            label = category,
                            isSelected = isSelected,
                            onClick = { selectedCategory = category }
                        )
                    }
                }

                // Products Grid
                if (filteredProducts.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No products found for this search.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 130.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredProducts) { product ->
                            StoreProductCard(product, onAdd = { cartViewModel.addToCart(product) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) AyurvedicGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.animateContentSize()
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

@Composable
fun StoreProductCard(product: StoreProduct, onAdd: () -> Unit) {
    val isDark = LocalIsDark.current
    
    GlassCard(
        modifier = Modifier.fillMaxWidth().height(260.dp),
        shape = RoundedCornerShape(24.dp),
        backgroundColor = if(isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.7f)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // "Image" Placeholder with Brand Icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(AyurvedicGreen.copy(alpha = 0.2f), AyurvedicGreen.copy(alpha = 0.05f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Medication, 
                    null, 
                    tint = AyurvedicGreen.copy(alpha = 0.6f),
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                product.category,
                style = MaterialTheme.typography.labelSmall,
                color = AyurvedicGreen,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "₹${product.price.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                IconButton(
                    onClick = onAdd,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AyurvedicGreen)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
