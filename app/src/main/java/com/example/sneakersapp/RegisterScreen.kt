package com.example.sneakersapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * หน้าจอสมัครสมาชิก (Register Screen)
 * @param onRegisterSuccess ฟังก์ชันทำงานเมื่อสมัครสมาชิกสำเร็จ (จะพากลับหน้า Login)
 * @param onNavigateBack ฟังก์ชันสำหรับกดย้อนกลับไปหน้าก่อนหน้า
 * @param authVM ViewModel สำหรับจัดการระบบ Authentication
 */
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    authVM: AuthViewModel = viewModel()
) {
    // เก็บค่าข้อมูลที่ผู้ใช้พิมพ์: อีเมล, รหัสผ่าน และ ยืนยันรหัสผ่าน
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // สถานะการเปิด/ปิดตาเพื่อดูรหัสผ่าน ของทั้งสองช่อง
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    // เก็บข้อความข้อผิดพลาดที่ตรวจสอบภายในหน้าจอ (เช่น รหัสไม่ตรงกัน)
    var localError by remember { mutableStateOf<String?>(null) }

    // ดึงสถานะปัจจุบันจาก AuthViewModel
    val authState by authVM.authState.collectAsState()

    // จัดการเหตุการณ์เมื่อสถานะการสมัครสมาชิกสำเร็จ
    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Success) {
            // เมื่อสำเร็จ ให้เรียกฟังก์ชันนำทางกลับไปหน้า Login
            onRegisterSuccess()
            authVM.resetState()
        }
    }

    // ส่วนจัดวาง UI หลักของหน้าสมัครสมาชิก
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // หัวข้อหน้าจอ
        Text(
            text = "Create Account",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = "Fill in the details to register", color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        // ช่องกรอก Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ช่องกรอก Password พร้อมไอคอนเปิด/ปิดตา
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ช่องกรอก Confirm Password พร้อมไอคอนเปิด/ปิดตา
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // แสดงตัวโหลด (Loading) หรือปุ่มสร้างบัญชี
        if (authState is AuthViewModel.AuthState.Loading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            Button(
                onClick = {
                    // ตรวจสอบความถูกต้องเบื้องต้นก่อนส่งข้อมูลไป Firebase
                    if (password != confirmPassword) {
                        localError = "Passwords do not match"
                    } else if (password.length < 6) {
                        localError = "Password must be at least 6 characters"
                    } else {
                        localError = null
                        authVM.register(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
            ) {
                Text("Create Account", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ปุ่มกลับไปหน้า Login
            TextButton(onClick = onNavigateBack) {
                Text(
                    text = buildAnnotatedString {
                        append("Already have an account? ")
                        withStyle(style = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold)) {
                            append("Login")
                        }
                    },
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // แสดงข้อความแจ้งเตือนข้อผิดพลาด
        val displayError = localError ?: if (authState is AuthViewModel.AuthState.Error) (authState as AuthViewModel.AuthState.Error).message else null
        displayError?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, color = Color.Red, fontWeight = FontWeight.Medium)
        }
    }
}
