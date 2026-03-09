package com.example.sneakersapp

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlin.math.roundToInt

/**
 * หน้าจอแสดงรายการสินค้าในตะกร้า (Cart Screen)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onNavigateToEdit: (Product, Int) -> Unit,
    productViewModel: ProductViewModel = viewModel()
) {
    var shippingMethod by remember { mutableStateOf("Normal (2-4 days) - 25 THB") }
    var paymentMethod by remember { mutableStateOf("Credit Card") }
    val shippingOptions = listOf("Normal (2-4 days) - 25 THB", "Fast (1-2 days) - 55 THB")
    val paymentOptions = listOf("Credit Card", "PayPal", "Cash on Delivery")

    var shippingExpanded by remember { mutableStateOf(false) }
    var paymentExpanded by remember { mutableStateOf(false) }

    // 1. คำนวณราคาสินค้าทั้งหมดในตะกร้า (Subtotal) เอามาจาก ViewModel ทีละคู่
    val subtotal = productViewModel.cartList.sumOf { it.product.price * it.quantity }
    // 2. กำหนดค่าขนส่งตามวิธีที่เลือก (Shipping Fee)
    val shippingFee = if (shippingMethod.contains("Fast")) 55.0 else 25.0
    // 3. รวมราคาสุทธิ (Total)
    val total = subtotal + shippingFee

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cart", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(productViewModel.cartList) { index, cartItem ->
                    SwipeableCartItem(
                        cartItem = cartItem,
                        onDelete = { productViewModel.removeFromCart(cartItem) },
                        onEdit = { onNavigateToEdit(cartItem.product, index) },
                        viewModel = productViewModel
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Shipping Method", fontWeight = FontWeight.Bold)
                    ExposedDropdownMenuBox(
                        expanded = shippingExpanded,
                        onExpandedChange = { shippingExpanded = !shippingExpanded }
                    ) {
                        OutlinedTextField(
                            value = shippingMethod,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shippingExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = shippingExpanded,
                            onDismissRequest = { shippingExpanded = false }
                        ) {
                            shippingOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        shippingMethod = option
                                        shippingExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Payment Method", fontWeight = FontWeight.Bold)
                    ExposedDropdownMenuBox(
                        expanded = paymentExpanded,
                        onExpandedChange = { paymentExpanded = !paymentExpanded }
                    ) {
                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = paymentExpanded,
                            onDismissRequest = { paymentExpanded = false }
                        ) {
                            paymentOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        paymentMethod = option
                                        paymentExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = 16.sp)
                        Text("$subtotal THB", fontSize = 16.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Shipping Fee", fontSize = 16.sp)
                        Text("$shippingFee THB", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Price", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("$total THB", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { 
                            productViewModel.placeOrder(shippingMethod, paymentMethod, total)
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = productViewModel.cartList.isNotEmpty()
                    ) {
                        Text("Place Order", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

/**
 * รายการสินค้าในตะกร้าที่รองรับการลากเพื่อลบ/แก้ไข
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableCartItem(
    cartItem: CartItem,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    viewModel: ProductViewModel
) {
    var offsetX by remember { mutableStateOf(0f) }
    var showDeleteSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val density = LocalDensity.current
    val actionWidth = with(density) { 120.dp.toPx() }

    // รีเซ็ตตำแหน่งเมื่อกลับมาที่หน้าจอนี้
    LaunchedEffect(Unit) {
        offsetX = 0f
    }

    if (showDeleteSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showDeleteSheet = false
                offsetX = 0f 
            },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Remove Item", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Are you sure you want to remove ${cartItem.product.name} from your cart?",
                    fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        showDeleteSheet = false
                        offsetX = 0f
                        onDelete()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Remove", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = { 
                        showDeleteSheet = false
                        offsetX = 0f 
                    }, 
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Cancel", fontSize = 16.sp, color = Color.Gray)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1F1F1))
    ) {
        Row(
            modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd).padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = { offsetX = 0f; onEdit() },
                modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).size(40.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            IconButton(
                onClick = { showDeleteSheet = true },
                modifier = Modifier.background(Color.Red, CircleShape).size(40.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        val animatedOffset by animateFloatAsState(targetValue = offsetX, label = "offset")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = { offsetX = if (offsetX < -actionWidth / 2) -actionWidth else 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = offsetX + dragAmount
                            offsetX = newOffset.coerceIn(-actionWidth - 20f, 0f)
                        }
                    )
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = cartItem.product.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(cartItem.product.name, fontWeight = FontWeight.Bold)
                    Text("Size: ${cartItem.size}", fontSize = 14.sp, color = Color.Gray)
                    Text("${cartItem.product.price} THB", color = MaterialTheme.colorScheme.primary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                    IconButton(onClick = { viewModel.updateCartQuantity(cartItem, -1) }) { 
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                    Text("${cartItem.quantity}", fontWeight = FontWeight.Bold)
                    IconButton(onClick = { viewModel.updateCartQuantity(cartItem, 1) }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
