# SneakersApp

แอปขายรองเท้าสนีกเกอร์บน Android เขียนด้วย **Kotlin + Jetpack Compose** ใช้ **Firebase** (Authentication + Cloud Firestore)
สมัครสมาชิก/เข้าสู่ระบบ เลือกดูสินค้าตามแบรนด์ เก็บรายการโปรด ใส่ตะกร้า เลือกขนาด และสั่งซื้อได้ครบขั้นตอน

An Android sneaker-shopping app built with Kotlin and Jetpack Compose, backed by Firebase Authentication and Cloud Firestore.
Browse by brand, favorite items, pick a size, manage a cart and place an order.

> งานกลุ่มในรายวิชา *Mobile Application Design and Development* (เพื่อการศึกษา) — ไฟล์ตั้งค่า Firebase (`google-services.json`) และ OAuth Client ID ใน repo นี้เป็นค่าสมมติ
> ต้องใส่ค่าของโปรเจกต์ Firebase ของคุณเองก่อนรัน (ดูหัวข้อ *ตั้งค่า Firebase*)

▶️ [ดูวิดีโอเดโม / Watch the demo](https://drive.google.com/file/d/1TBd9jAlkPKn1NZMz7QRl36k9J9YE71n3/view?usp=sharing)

## ความสามารถ / Features

| ด้าน | รายละเอียด |
|---|---|
| บัญชีผู้ใช้ | สมัครสมาชิกและเข้าสู่ระบบด้วยอีเมล/รหัสผ่าน, เข้าสู่ระบบด้วย Google (Credential Manager), ลืมรหัสผ่านผ่านอีเมลรีเซ็ต, ออกจากระบบ |
| สินค้า | แสดงรายการจาก Firestore กรองตามแบรนด์ (All / Nike / Adidas / New Balance / Puma) หน้ารายละเอียดสินค้า เลือกไซซ์ 38–43 และจำนวน |
| รายการโปรด | กดหัวใจเพิ่ม/ลบ ปัดเพื่อลบ เก็บแยกต่อผู้ใช้ใน Firestore |
| ตะกร้า | ปรับจำนวน เปลี่ยนไซซ์ ปัดเพื่อลบ สรุปราคา (ค่าสินค้า + ค่าจัดส่ง) |
| สั่งซื้อ | เลือกการจัดส่ง (Normal 2–4 วัน 25 บาท / Fast 1–2 วัน 55 บาท) และวิธีชำระเงิน (Credit Card / PayPal / Cash on Delivery) บันทึกออเดอร์ลง Firestore แล้วล้างตะกร้า |
| การแจ้งเตือน | หลังสั่งซื้อจะมีแจ้งเตือนสถานะ "Package Shipped" พร้อมรายละเอียดออเดอร์ |
| โปรไฟล์ | ดูอีเมล แก้ไขเบอร์โทรและที่อยู่จัดส่ง (ใช้ตอนสั่งซื้อ) |

## เทคโนโลยี / Tech stack

- Kotlin 2.0, Jetpack Compose (Material 3), Navigation Compose, ViewModel
- Firebase: Authentication, Cloud Firestore, Analytics (Firebase BoM 33.7)
- AndroidX Credentials + Google ID (Sign in with Google)
- Coil (โหลดรูปสินค้าจาก URL), Kotlin Coroutines
- Android Gradle Plugin 8.13, `minSdk` 24, `targetSdk` / `compileSdk` 36

## โครงสร้าง / Structure

```
app/src/main/java/com/example/sneakersapp/
  MainActivity.kt          NavHost: login → register → main → product detail → cart
  MainScreen.kt            Bottom navigation: Home · Favorite · Profile
  AuthViewModel.kt         สมัคร / ล็อกอิน (อีเมล, Google) / รีเซ็ตรหัสผ่าน / ออกจากระบบ
  ProductViewModel.kt      สินค้า รายการโปรด ตะกร้า ออเดอร์ โปรไฟล์ (Firestore)
  LoginScreen.kt  RegisterScreen.kt
  ProductListScreen.kt     หน้าแรก + กรองแบรนด์ + แจ้งเตือน
  ProductDetailScreen.kt   รายละเอียด เลือกไซซ์/จำนวน
  FavoriteScreen.kt  CartScreen.kt  ProfileScreen.kt
  Product.kt  Order.kt  User.kt     Data classes
  ui/theme/                ธีม Material 3
```

### โครงสร้างข้อมูลใน Firestore

```
products/{productid}                สินค้า (productid, name, imageUrl, price, brand, description)
users/{uid}                         โปรไฟล์ (name, email, address, phone)
users/{uid}/favorites/{productid}   รายการโปรด
users/{uid}/cart/{productid_size}   ตะกร้า (สินค้า + ไซซ์ + จำนวน)
orders/{autoId}                     ออเดอร์ (userId, items, total, shipping, payment, status, timestamp)
```

## ตั้งค่า Firebase / Firebase setup

1. สร้างโปรเจกต์ใน [Firebase Console](https://console.firebase.google.com/) และเพิ่มแอป Android ด้วย package `com.example.sneakersapp`
   พร้อมใส่ **SHA-1** ของเครื่องที่ใช้ดีบัก (`./gradlew signingReport`)
2. เปิด **Authentication** → Sign-in method: **Email/Password** และ **Google**
3. สร้างฐานข้อมูล **Cloud Firestore** และตั้ง Security Rules ให้ผู้ใช้อ่าน/เขียนได้เฉพาะข้อมูลของตัวเอง (`users/{uid}/…`) และอ่านได้อย่างเดียวที่ `products`
4. ดาวน์โหลด `google-services.json` ของคุณ มาวางแทนที่ `app/google-services.json` (ไฟล์ใน repo เป็นค่าสมมติ)
5. ใส่ **Web client ID** ของโปรเจกต์คุณ (Firebase Console → Authentication → Google → Web SDK configuration) ที่ `AuthViewModel.kt` ใน `GetSignInWithGoogleOption.Builder("...")`
6. เติมสินค้าตัวอย่าง: ใน `ProductViewModel` มีฟังก์ชัน `addSampleProducts()` (สินค้า Puma / Nike / Adidas / New Balance) ที่เขียนลง `products` — เรียกใช้ครั้งเดียวเพื่อเริ่มต้นข้อมูล หรือเพิ่มเอกสารใน Firestore เอง

> ⚠️ อย่า commit `google-services.json` ตัวจริงและคีย์ใดๆ ขึ้น repo สาธารณะ

## วิธีรัน / Getting started

1. ติดตั้ง [Android Studio](https://developer.android.com/studio) (เวอร์ชันล่าสุด) และ JDK 11 ขึ้นไป
2. เปิดโฟลเดอร์นี้ใน Android Studio แล้วรอ Gradle sync
3. ตั้งค่า Firebase ตามหัวข้อด้านบน
4. เลือกอีมูเลเตอร์หรือเครื่องจริง (Android 7.0 / API 24 ขึ้นไป — Google Sign-In ต้องใช้อุปกรณ์ที่มี Google Play services) แล้วกด **Run**

## ผู้จัดทำ / Project members

| ชื่อ |
|---|
| นายเงินเพชร เดชมา |
| นายเตชิต เสนาราช |
| นายธีรภัทร วันขวัญ |
| นายภานุพงษ์ ศรีสุยงค์ |

**รายวิชา / Course:** Mobile Application Design and Development

## License

Created for educational purposes.
