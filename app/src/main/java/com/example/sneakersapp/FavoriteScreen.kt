package com.example.sneakersapp

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
 * หน้าจอแสดงรายการสินค้าที่ผู้ใช้กดถูกใจ (Favorites)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    productViewModel: ProductViewModel = viewModel(),
    onNavigateToDetail: (Product) -> Unit = {}
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("My Favorites", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        if (productViewModel.favoriteList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No favorite items yet", fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(productViewModel.favoriteList) { product ->
                    SwipeableFavoriteItem(
                        product = product,
                        onDelete = { productViewModel.toggleFavorite(product) },
                        onClick = { onNavigateToDetail(product) }
                    )
                }
            }
        }
    }
}

/**
 * รายการสินค้าในหน้า Favorite ที่รองรับการสไลด์เพื่อลบ
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableFavoriteItem(
    product: Product,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var showDeleteSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val density = LocalDensity.current
    val actionWidth = with(density) { 80.dp.toPx() }

    // รีเซ็ตตำแหน่งการปัดทุกครั้งที่คอมโพเนนต์นี้ถูกสร้างใหม่หรือกลับมาแสดงผล
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
                Text(text = "Remove Favorite", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Are you sure you want to remove ${product.name} from your favorites?",
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
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1F1F1))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(80.dp)
                .align(Alignment.CenterEnd)
                .background(Color.Red)
                .clickable { showDeleteSheet = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
        }

        val animatedOffset by animateFloatAsState(targetValue = offsetX, label = "offset")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
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
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            onClick = onClick
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = product.name, fontWeight = FontWeight.Bold)
                    Text(text = "${product.price} THB", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
