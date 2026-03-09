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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * หน้าจอโปรไฟล์ผู้ใช้ (Profile Screen)
 * @param onLogout ฟังก์ชันสำหรับนำทางกลับหน้า Login เมื่อออกจากระบบ
 * @param productViewModel ViewModel สำหรับจัดการข้อมูลโปรไฟล์และล้างข้อมูล
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    productViewModel: ProductViewModel = viewModel()
) {
    // สถานะสำหรับการแสดง Bottom Sheet ยืนยันการออกจากระบบ
    var showLogoutSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    // สถานะเปิด/ปิดโหมดการแก้ไขข้อมูล
    var isEditing by remember { mutableStateOf(false) }

    // หน้าต่างยืนยันการออกจากระบบ แบบ Bottom Sheet (เหมือน Google Sign-in)
    if (showLogoutSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLogoutSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ไอคอน Logout
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Sign Out",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Are you sure you want to sign out? You will need to login again to access your account.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // ปุ่มยืนยันออกจากระบบ
                Button(
                    onClick = {
                        showLogoutSheet = false
                        productViewModel.clearData()
                        Firebase.auth.signOut()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Sign Out", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // ปุ่มยกเลิก
                TextButton(
                    onClick = { showLogoutSheet = false },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Cancel", fontSize = 16.sp, color = Color.Gray)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    // ปุ่ม Edit/Check สำหรับสลับโหมดแก้ไขและบันทึก
                    IconButton(onClick = { 
                        if (isEditing) {
                            productViewModel.saveUserProfile() 
                        }
                        isEditing = !isEditing 
                    }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = null
                        )
                    }
                    // ปุ่มเปิด Bottom Sheet เพื่อ Logout
                    IconButton(onClick = { showLogoutSheet = true }) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ส่วนหัว (Header Section)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            // ส่วนข้อมูลโปรไฟล์ (Info Section)
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-30).dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        ProfileInfoItem(
                            label = "Email Address",
                            value = productViewModel.userEmail.value,
                            onValueChange = {},
                            icon = Icons.Default.Email,
                            enabled = false
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            thickness = 1.dp,
                            color = Color.LightGray
                        )

                        ProfileInfoItem(
                            label = "Phone Number",
                            value = productViewModel.userPhone.value,
                            onValueChange = { productViewModel.userPhone.value = it },
                            icon = Icons.Default.Phone,
                            enabled = isEditing
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            thickness = 1.dp,
                            color = Color.LightGray
                        )
                        
                        ProfileInfoItem(
                            label = "Delivery Address",
                            value = productViewModel.userAddress.value,
                            onValueChange = { productViewModel.userAddress.value = it },
                            icon = Icons.Default.LocationOn,
                            isMultiline = true,
                            enabled = isEditing
                        )
                    }
                }

                if (isEditing) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            productViewModel.saveUserProfile()
                            isEditing = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Save Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * คอมโพเนนต์แสดงข้อมูลแต่ละแถวในหน้าโปรไฟล์
 */
@Composable
fun ProfileInfoItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    isMultiline: Boolean = false,
    enabled: Boolean = false
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                disabledTextColor = Color.Black
            ),
            singleLine = !isMultiline,
            minLines = if (isMultiline) 3 else 1
        )
    }
}
