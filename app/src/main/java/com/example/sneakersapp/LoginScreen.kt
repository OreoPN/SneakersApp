package com.example.sneakersapp

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * หน้าจอเข้าสู่ระบบ (Login Screen)
 * ปรับปรุงใหม่ให้โชว์ชื่อแอป "SNEAKERS HUB" เพื่อความสวยงามและพรีเมียม
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    authVM: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showForgotDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    val authState by authVM.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Success) {
            onLoginSuccess()
            authVM.resetState()
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotDialog = false
                resetEmail = ""
            },
            title = { Text("Forgot Password") },
            text = {
                Column {
                    Text("Enter your email to receive a password reset link.")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        authVM.resetPassword(resetEmail)
                        showForgotDialog = false
                        resetEmail = ""
                    },
                    enabled = resetEmail.isNotBlank(),
                ) { Text("Send Email", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showForgotDialog = false
                    resetEmail = ""
                }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- ส่วนแสดงชื่อแอปแบบพรีเมียม ---
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Black)) {
                    append("SNEAKERS")
                }
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(" SHOP")
                }
            },
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp
        )
        Text(
            text = "Step into style", 
            color = Color.Gray,
            fontSize = 16.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Light
        )

        Spacer(modifier = Modifier.height(48.dp))

        // ช่องกรอก Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ช่องกรอก Password
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = {
                resetEmail = email
                showForgotDialog = true
            }) {
                Text("Forgot password?", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (authState is AuthViewModel.AuthState.Loading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            Button(
                onClick = { authVM.loginWithEmail(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = email.isNotBlank() && password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = buildAnnotatedString {
                        append("Don't have an account? ")
                        withStyle(style = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold)) {
                            append("Register here")
                        }
                    },
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        if (authState is AuthViewModel.AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (authState as AuthViewModel.AuthState.Error).message,
                color = Color.Red,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        if (authState is AuthViewModel.AuthState.ResetPasswordSent) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Password reset email sent!", 
                color = Color(0xFF4CAF50),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "OR",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Gray,
                fontSize = 14.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { authVM.loginWithGoogle(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.google_color),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified
                )
                Spacer(Modifier.width(12.dp))
                Text("Sign in with Google", color = Color.Black)
            }
        }
    }
}
