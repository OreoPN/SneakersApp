package com.example.sneakersapp

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel สำหรับจัดการระบบยืนยันตัวตน (Authentication)
 * รองรับการสมัครสมาชิก เข้าสู่ระบบด้วยอีเมล และ Google
 */
class AuthViewModel : ViewModel() {
    // อ้างอิงถึง Firebase Auth
    private val auth: FirebaseAuth = Firebase.auth

    /**
     * ตรวจสอบว่าผู้ใช้ล็อกอินอยู่หรือไม่
     */
    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    /**
     * ข้อมูลผู้ใช้ปัจจุบันจาก Firebase
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // เก็บสถานะปัจจุบันของการทำงาน (Idle, Loading, Success, Error)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * คลาสสำหรับนิยามสถานะต่างๆ ของระบบ Auth
     */
    sealed class AuthState {
        object Idle : AuthState() // สถานะปกติ/ว่าง
        object Loading : AuthState() // กำลังทำงาน
        object Success : AuthState() // ทำงานสำเร็จ
        object ResetPasswordSent : AuthState() // ส่งอีเมลรีเซ็ตรหัสผ่านสำเร็จ
        data class Error(val message: String) : AuthState() // เกิดข้อผิดพลาด
    }

    /**
     * ลงทะเบียนผู้ใช้ใหม่ด้วยอีเมลและรหัสผ่าน
     * @param email อีเมลที่ใช้สมัคร
     * @param password รหัสผ่าน
     */
    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: FirebaseAuthException) {
                // กรองข้อผิดพลาดจาก Firebase และแสดงเป็นภาษาอังกฤษ
                val message = when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE", "account-exists-with-different-credential" -> "This email is already in use."
                    "ERROR_WEAK_PASSWORD", "weak-password" -> "Password is too weak. (Minimum 6 characters)"
                    "ERROR_INVALID_EMAIL", "invalid-email" -> "The email address is badly formatted."
                    else -> e.localizedMessage ?: "Registration failed. Please try again."
                }
                _authState.value = AuthState.Error(message)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    /**
     * ส่งลิงก์รีเซ็ตรหัสผ่านไปยังอีเมลที่ระบุ
     * @param email อีเมลที่ต้องการรีเซ็ตรหัสผ่าน
     */
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.ResetPasswordSent
            } catch (e: FirebaseAuthException) {
                val message = when (e.errorCode) {
                    "ERROR_USER_NOT_FOUND", "user-not-found" -> "No account found with this email."
                    "ERROR_INVALID_EMAIL", "invalid-email"  -> "Invalid email format."
                    else -> "Failed to send reset email. Please try again."
                }
                _authState.value = AuthState.Error(message)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An error occurred")
            }
        }
    }

    /**
     * เข้าสู่ระบบด้วยอีเมลและรหัสผ่าน
     * @param email อีเมล
     * @param password รหัสผ่าน
     */
    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: FirebaseAuthException) {
                val message = when (e.errorCode) {
                    "ERROR_INVALID_EMAIL", "invalid-email" -> "Invalid email format."
                    "ERROR_WRONG_PASSWORD", "wrong-password" -> "Incorrect password."
                    "ERROR_USER_NOT_FOUND", "user-not-found" -> "No account found with this email."
                    "ERROR_USER_DISABLED", "user-disabled" -> "This account has been disabled."
                    "ERROR_TOO_MANY_REQUESTS" -> "Too many failed attempts. Please try again later."
                    else -> e.localizedMessage ?: "Login failed. Please check your credentials."
                }
                _authState.value = AuthState.Error(message)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    /**
     * เข้าสู่ระบบด้วย Google Account ผ่าน Credential Manager
     * @param context บริบทของแอป
     */
    fun loginWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credentialManager = CredentialManager.create(context)

                val signInWithGoogleOption = GetSignInWithGoogleOption
                    .Builder("250559904923-6j5n41809hosn1vbahprjnq1eneunoel.apps.googleusercontent.com")
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(
                        googleIdTokenCredential.idToken, null
                    )
                    auth.signInWithCredential(firebaseCredential).await()
                    _authState.value = AuthState.Success
                }

            } catch (e: GetCredentialCancellationException) {
                // กรณีผู้ใช้กดยกเลิกเอง แสดงข้อความเป็นภาษาอังกฤษ
                _authState.value = AuthState.Error("Sign-in was cancelled. Please try again if you want to log in.")
            } catch (e: GetCredentialException) {
                _authState.value = AuthState.Error("Google Login is currently unavailable.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Something went wrong with Google Login.")
            }
        }
    }

    /**
     * ออกจากระบบ (Logout)
     */
    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    /**
     * รีเซ็ตสถานะกลับเป็นสถานะปกติ (Idle)
     */
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
