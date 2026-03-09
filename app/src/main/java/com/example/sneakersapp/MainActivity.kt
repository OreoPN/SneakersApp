package com.example.sneakersapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sneakersapp.ui.theme.SneakersAppTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * MainActivity เป็น Activity หลักของแอปพลิเคชันที่ทำหน้าที่เป็นจุดเริ่มต้น
 * และจัดการระบบ Navigation ทั้งหมดในระดับแอป
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // เปิดใช้งานโหมดแสดงผลเต็มหน้าจอ (Edge-to-Edge)
        enableEdgeToEdge()
        setContent {
            // เรียกใช้งานธีมที่กำหนดเองสำหรับ SneakersApp
            SneakersAppTheme {
                // NavController สำหรับจัดการการเปลี่ยนหน้าภายใน NavHost
                val navController = rememberNavController()
                // เรียกใช้ ViewModel หลักสำหรับจัดการข้อมูลสินค้าและแอป
                val productViewModel: ProductViewModel = viewModel()
                // เรียกใช้ ViewModel สำหรับจัดการระบบ Authentication
                val authVM: AuthViewModel = viewModel()
                
                // ตรวจสอบสถานะการล็อกอินปัจจุบันเพื่อกำหนดหน้าเริ่มต้น
                val startDest = if (Firebase.auth.currentUser != null) "main" else "login"
                
                // ดึงข้อมูลผู้ใช้จาก Firestore ทันทีหากมีการล็อกอินค้างไว้
                LaunchedEffect(Unit) {
                    if (Firebase.auth.currentUser != null) {
                        productViewModel.updateUserInfo()
                    }
                }

                // ส่วนจัดการเส้นทาง (Routes) ทั้งหมดภายในแอป
                NavHost(navController = navController, startDestination = startDest) {
                    // เส้นทางหน้า Login
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = { 
                                productViewModel.updateUserInfo()
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToRegister = { 
                                authVM.resetState() // ล้างสถานะ Error ก่อนไปหน้าสมัครสมาชิก
                                navController.navigate("register") 
                            },
                            authVM = authVM
                        )
                    }
                    // เส้นทางหน้า Register
                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = { 
                                authVM.resetState() // ล้างสถานะก่อนกลับไปหน้า Login
                                navController.popBackStack() 
                            },
                            onNavigateBack = { 
                                authVM.resetState() // ล้างสถานะเมื่อกดย้อนกลับ
                                navController.popBackStack() 
                            },
                            authVM = authVM
                        )
                    }
                    // เส้นทางหน้าหลัก (HomeScreen, Favorite, Profile)
                    composable("main") {
                        MainScreen(
                            productViewModel = productViewModel,
                            onNavigateToDetail = { product ->
                                navController.navigate("detail/${product.productid}")
                            },
                            onNavigateToCart = { navController.navigate("cart") },
                            onLogout = {
                                productViewModel.clearData()
                                authVM.logout() // ออกจากระบบ Firebase
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        )
                    }
                    // เส้นทางหน้าแสดงรายละเอียดสินค้า
                    composable(
                        "detail/{productId}",
                        arguments = listOf(navArgument("productId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val productId = backStackEntry.arguments?.getInt("productId")
                        val product = productViewModel.productList.find { it.productid == productId }
                        product?.let {
                            ProductDetailScreen(
                                product = it,
                                onBack = { navController.popBackStack() },
                                productViewModel = productViewModel
                            )
                        }
                    }
                    // เส้นทางหน้าแก้ไขสินค้าในตะกร้า
                    composable(
                        "edit_cart/{productId}/{index}",
                        arguments = listOf(
                            navArgument("productId") { type = NavType.IntType },
                            navArgument("index") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val productId = backStackEntry.arguments?.getInt("productId")
                        val index = backStackEntry.arguments?.getInt("index") ?: -1
                        val product = productViewModel.productList.find { it.productid == productId }
                        val cartItem = if (index != -1 && index < productViewModel.cartList.size) 
                                        productViewModel.cartList[index] else null
                        
                        if (product != null && cartItem != null) {
                            ProductDetailScreen(
                                product = product,
                                cartItemToEdit = cartItem,
                                onBack = { navController.popBackStack() },
                                productViewModel = productViewModel
                            )
                        }
                    }
                    // เส้นทางหน้าตะกร้าสินค้า
                    composable("cart") {
                        CartScreen(
                            onBack = { navController.popBackStack() },
                            onNavigateToEdit = { product, index ->
                                navController.navigate("edit_cart/${product.productid}/$index")
                            },
                            productViewModel = productViewModel
                        )
                    }
                }
            }
        }
    }
}
