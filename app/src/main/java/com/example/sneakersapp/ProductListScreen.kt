package com.example.sneakersapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

/**
 * หน้าจอหลัก (Home Screen) แสดงรายการยี่ห้อและสินค้า
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    productViewModel: ProductViewModel = viewModel(),
    onNavigateToDetail: (Product) -> Unit = {},
    onNavigateToCart: () -> Unit = {}
) {
    var selectedBrand by remember { mutableStateOf("All") }
    val brands = listOf("All", "Nike", "Adidas", "New Balance", "Puma")
    var showNotiSheet by remember { mutableStateOf(false) }
    // สำหรับแสดงรายละเอียดออเดอร์เมื่อกดแจ้งเตือน
    var selectedOrder by remember { mutableStateOf<Notification?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            Surface(
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(text = "Explore Our", fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        Text(text = "Brands", fontWeight = FontWeight.Black, fontSize = 42.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = (-1.5).sp)
                    }
                    IconButton(
                        onClick = { showNotiSheet = true },
                        modifier = Modifier.padding(top = 12.dp, end = 8.dp)
                    ) {
                        BadgedBox(badge = {
                            if (productViewModel.notifications.isNotEmpty()) {
                                Badge(containerColor = Color.Red, modifier = Modifier.offset(x = (-4).dp, y = 4.dp)) { 
                                    Text(productViewModel.notifications.size.toString(), color = Color.White) 
                                }
                            }
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(32.dp), tint = BlackText)
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCart, containerColor = MaterialTheme.colorScheme.primary, shape = CircleShape, elevation = FloatingActionButtonDefaults.elevation(8.dp)) {
                BadgedBox(badge = {
                    if (productViewModel.cartList.isNotEmpty()) {
                        Badge(containerColor = Color.Red) { Text(productViewModel.cartList.size.toString(), color = Color.White) }
                    }
                }) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(top = padding.calculateTopPadding() + 10.dp).fillMaxSize()) {
            LazyRow(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(brands) { brand ->
                    val isSelected = selectedBrand == brand
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedBrand = brand },
                        label = { Text(brand, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = Color.White),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSelected, borderColor = Color.LightGray, selectedBorderColor = Color.Transparent)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            val filteredProducts = if (selectedBrand == "All") productViewModel.productList else productViewModel.productList.filter { it.brand == selectedBrand }
            if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No shoes found for $selectedBrand", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp), verticalArrangement = Arrangement.spacedBy(20.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    items(filteredProducts) { product ->
                        ProductGridItem(product, onClick = { onNavigateToDetail(product) })
                    }
                }
            }
        }
    }

    // แผ่นแจ้งเตือน และ รายละเอียดออเดอร์
    if (showNotiSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showNotiSheet = false 
                selectedOrder = null 
            },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                if (selectedOrder == null) {
                    // --- หน้าแสดงรายการแจ้งเตือนทั้งหมด ---
                    Column {
                        Text(text = "Notifications", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        if (productViewModel.notifications.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Text("No notifications yet", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(productViewModel.notifications) { noti ->
                                    NotificationItem(noti, onClick = { selectedOrder = noti })
                                }
                            }
                        }
                    }
                } else {
                    // --- หน้าแสดงรายละเอียดออเดอร์ (เลื่อนได้) ---
                    OrderDetailsContent(order = selectedOrder!!, onBack = { selectedOrder = null })
                }
            }
        }
    }
}

/**
 * คอมโพเนนต์แสดงเนื้อหารายละเอียดออเดอร์ (ปรับปรุงให้พอดีและเลื่อนได้)
 */
@Composable
fun OrderDetailsContent(order: Notification, onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState), // ทำให้เลื่อนดูได้ถ้าข้อมูลยาวเกินหน้าจอ
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { 
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black) 
            }
            Text(text = "Order Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Status: ${order.status}", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = "Order ID: #${order.orderId.takeLast(8).uppercase()}", fontSize = 14.sp, color = Color.Gray)
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))
        
        // รายการสินค้าในออเดอร์
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Items Ordered", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = item["name"].toString(), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Text(text = "Size: ${item["size"]}", fontSize = 13.sp, color = Color.Gray)
                    }
                    Text(text = "x${item["qty"]}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Total Amount", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "${order.total} THB", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Close Details", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NotificationItem(noti: Notification, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = noti.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = noti.message, fontSize = 14.sp, color = Color.Gray)
                Text(text = sdf.format(Date(noti.timestamp)), fontSize = 12.sp, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun ProductGridItem(product: Product, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))) {
        Column(modifier = Modifier.padding(8.dp)) {
            AsyncImage(model = product.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(20.dp)), contentScale = ContentScale.Fit)
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                Text(text = product.brand, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "${product.price} THB", color = MaterialTheme.colorScheme.primary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

val BlackText = Color(0xFF212121)
