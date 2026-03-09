package com.example.sneakersapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

/**
 * หน้าจอแสดงรายละเอียดสินค้า (Product Detail Screen)
 * รองรับทั้งการดูรายละเอียดเพื่อเพิ่มลงตะกร้าใหม่ และการแก้ไขสินค้าที่มีอยู่ในตะกร้าแล้ว
 * 
 * @param product ข้อมูลสินค้าที่ต้องการแสดง
 * @param cartItemToEdit ข้อมูลสินค้าในตะกร้าที่ต้องการแก้ไข (ถ้ามี)
 * @param onBack ฟังก์ชันสำหรับกดย้อนกลับ
 * @param productViewModel ViewModel หลักที่ใช้จัดการข้อมูลตะกร้าและรายการโปรด
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    cartItemToEdit: CartItem? = null,
    onBack: () -> Unit,
    productViewModel: ProductViewModel = viewModel()
) {
    // สถานะสำหรับเก็บขนาดรองเท้าที่เลือก (ค่าเริ่มต้นจากสินค้าที่จะแก้ หรือ "40")
    var selectedSize by remember { mutableStateOf(cartItemToEdit?.size ?: "40") }
    // สถานะสำหรับเก็บจำนวนสินค้าที่เลือก
    var quantity by remember { mutableIntStateOf(cartItemToEdit?.quantity ?: 1) }
    // รายการขนาดรองเท้าที่มีให้เลือก
    val sizes = listOf("38", "39", "40", "41", "42", "43")
    // ตรวจสอบว่าสินค้าชิ้นนี้อยู่ในรายการโปรดหรือไม่
    val isFavorite = productViewModel.favoriteList.any { it.productid == product.productid }

    Scaffold(
        topBar = {
            TopAppBar(
                // เปลี่ยนหัวข้อตามโหมด (ดูรายละเอียด หรือ แก้ไข)
                title = { Text(if (cartItemToEdit != null) "Edit Cart Item" else "Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // ทำให้หน้าจอเลื่อนลงได้
        ) {
            // แสดงรูปภาพสินค้าขนาดใหญ่
            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ชื่อสินค้า
                    Text(text = product.name, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    // ปุ่มกดถูกใจ (Favorite)
                    IconButton(onClick = { productViewModel.toggleFavorite(product) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) Color.Red else Color.Gray
                        )
                    }
                }

                // ราคาสินค้า
                Text(text = "${product.price} THB", fontSize = 22.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // รายละเอียดสินค้า
                Text(text = "Description", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = product.description, color = Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))

                // ส่วนเลือกขนาดรองเท้า
                Text(text = "Select Size", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    sizes.forEach { size ->
                        FilterChip(
                            selected = selectedSize == size,
                            onClick = { selectedSize = size },
                            label = { Text(size) },
                            shape = CircleShape
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ส่วนเลือกจำนวนสินค้า (Quantity Selector)
                Text(text = "Quantity", fontWeight = FontWeight.Bold)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // ปุ่มลดจำนวน
                    OutlinedIconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null)
                    }
                    
                    // แสดงตัวเลขจำนวน
                    Text(
                        text = quantity.toString(),
                        modifier = Modifier.padding(horizontal = 20.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // ปุ่มเพิ่มจำนวน
                    OutlinedIconButton(
                        onClick = { quantity++ },
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // ปุ่มยืนยันการทำงาน (Add to Cart หรือ Update Cart)
                Button(
                    onClick = { 
                        if (cartItemToEdit != null) {
                            // กรณีโหมดแก้ไข ให้ทำการอัปเดตข้อมูลเดิม
                            productViewModel.updateCartItem(cartItemToEdit, selectedSize, quantity)
                        } else {
                            // กรณีโหมดปกติ ให้เพิ่มสินค้าใหม่ลงตะกร้า
                            productViewModel.addToCart(product, quantity, selectedSize)
                        }
                        onBack() // กลับไปยังหน้าก่อนหน้า
                    },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (cartItemToEdit != null) "Update Cart" else "Add to Cart")
                }
            }
        }
    }
}
