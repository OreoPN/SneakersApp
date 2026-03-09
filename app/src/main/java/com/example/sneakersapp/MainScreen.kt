package com.example.sneakersapp

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * หน้าจอหลักที่รวม Navigation Bar ด้านล่าง (Bottom Bar)
 * ประกอบด้วยหน้า Home, Favorite และ Profile
 */
@Composable
fun MainScreen(
    productViewModel: ProductViewModel,
    onNavigateToDetail: (Product) -> Unit,
    onNavigateToCart: () -> Unit,
    onLogout: () -> Unit
) {
    //NavController สำหรับจัดการการเปลี่ยนหน้าภายในส่วน Main
    val navController = rememberNavController()
    // ติดตามสถานะของ Back Stack ปัจจุบัน
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // รายการเมนูใน Bottom Bar
    val items = listOf("home", "favorite", "profile")
    val icons = listOf(Icons.Default.Home, Icons.Default.Favorite, Icons.Default.Person)
    val labels = listOf("Home", "Favorite", "Profile")

    Scaffold(
        // ส่วนแถบเมนูด้านล่าง
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                items.forEachIndexed { index, route ->
                    // ตรวจสอบว่าเมนูนี้กำลังถูกเลือกอยู่หรือไม่
                    val selected = currentDestination?.hierarchy?.any { it.route == route } == true
                    
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = labels[index]) },
                        label = { Text(labels[index]) },
                        selected = selected,
                        onClick = {
                            navController.navigate(route) {
                                // ป๊อปอัพไปที่หน้าเริ่มต้นเพื่อป้องกันการซ้อนของหน้า
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // ป้องกันการเปิดหน้าเดิมซ้ำๆ
                                launchSingleTop = true
                                // คืนสถานะหน้าเดิมเมื่อกดกลับมา (เช่น ตำแหน่ง Scroll)
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // ส่วนจัดการการเปลี่ยนหน้า (NavHost) ภายใน MainScreen
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            // หน้าแรก: แสดงรายการ Brands และสินค้า
            composable("home") { 
                HomeScreen(
                    productViewModel = productViewModel,
                    onNavigateToDetail = onNavigateToDetail,
                    onNavigateToCart = onNavigateToCart
                ) 
            }
            // หน้ารายการโปรด: แสดงสินค้าที่ผู้ใช้กดหัวใจไว้
            composable("favorite") { 
                FavoriteScreen(
                    productViewModel = productViewModel,
                    onNavigateToDetail = onNavigateToDetail
                ) 
            }
            // หน้าโปรไฟล์: แสดงข้อมูลผู้ใช้และปุ่มออกจากระบบ
            composable("profile") { 
                ProfileScreen(
                    onLogout = onLogout,
                    productViewModel = productViewModel
                ) 
            }
        }
    }
}
