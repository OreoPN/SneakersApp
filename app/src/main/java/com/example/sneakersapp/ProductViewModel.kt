package com.example.sneakersapp

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * ข้อมูลไอเทมในตะกร้าสินค้า
 */
data class CartItem(
    val product: Product = Product(),
    var quantity: Int = 1,
    var size: String = "40"
)

/**
 * ข้อมูลการแจ้งเตือนและรายละเอียดออเดอร์
 */
data class Notification(
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val orderId: String = "",
    val status: String = "Shipping", // สถานะเริ่มต้น: กำลังจัดส่ง
    val items: List<Map<String, Any>> = emptyList(),
    val total: Double = 0.0
)

/**
 * ViewModel สำหรับจัดการข้อมูลทั้งหมด
 */
class ProductViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    
    val productList = mutableStateListOf<Product>()
    val cartList = mutableStateListOf<CartItem>()
    val favoriteList = mutableStateListOf<Product>()
    val notifications = mutableStateListOf<Notification>()
    
    val userPhone = mutableStateOf("")
    val userAddress = mutableStateOf("")
    val userEmail = mutableStateOf("")

    init {
        fetchProducts()
    }

    /**
     * ดึงข้อมูลผู้ใช้และอัปเดตรายการต่างๆ
     */
    fun updateUserInfo() {
        val currentUser = auth.currentUser
        clearData()
        if (currentUser == null) return
        userEmail.value = currentUser.email ?: ""
        val uid = currentUser.uid
        
        db.collection("users").document(uid).get().addOnSuccessListener { document ->
            if (document.exists()) {
                userPhone.value = document.getString("phone") ?: ""
                userAddress.value = document.getString("address") ?: ""
            }
        }
        
        db.collection("users").document(uid).collection("favorites").get().addOnSuccessListener { result ->
            favoriteList.clear()
            for (doc in result) favoriteList.add(doc.toObject(Product::class.java))
        }

        db.collection("users").document(uid).collection("cart").get().addOnSuccessListener { result ->
            cartList.clear()
            for (doc in result) cartList.add(doc.toObject(CartItem::class.java))
        }
    }

    fun saveUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        val userData = hashMapOf("email" to userEmail.value, "phone" to userPhone.value, "address" to userAddress.value)
        db.collection("users").document(uid).set(userData)
    }

    fun clearData() {
        cartList.clear()
        favoriteList.clear()
        notifications.clear()
        userPhone.value = ""
        userAddress.value = ""
        userEmail.value = ""
    }

    fun fetchProducts() {
        db.collection("products").get().addOnSuccessListener { result ->
            productList.clear()
            for (document in result) productList.add(document.toObject(Product::class.java))
        }
    }

    fun addToCart(product: Product, quantity: Int, size: String) {
        val uid = auth.currentUser?.uid ?: return
        val existingItem = cartList.find { it.product.productid == product.productid && it.size == size }
        if (existingItem != null) {
            existingItem.quantity += quantity
            val index = cartList.indexOf(existingItem)
            cartList[index] = existingItem.copy()
            db.collection("users").document(uid).collection("cart").document("${product.productid}_$size").set(cartList[index])
        } else {
            val newItem = CartItem(product, quantity, size)
            cartList.add(newItem)
            db.collection("users").document(uid).collection("cart").document("${product.productid}_$size").set(newItem)
        }
    }

    fun updateCartItem(oldItem: CartItem, newSize: String, newQuantity: Int) {
        val uid = auth.currentUser?.uid ?: return
        val index = cartList.indexOf(oldItem)
        if (index != -1) {
            db.collection("users").document(uid).collection("cart").document("${oldItem.product.productid}_${oldItem.size}").delete()
            val updatedItem = oldItem.copy(size = newSize, quantity = newQuantity)
            cartList[index] = updatedItem
            db.collection("users").document(uid).collection("cart").document("${updatedItem.product.productid}_$newSize").set(updatedItem)
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        val uid = auth.currentUser?.uid ?: return
        cartList.remove(cartItem)
        db.collection("users").document(uid).collection("cart").document("${cartItem.product.productid}_${cartItem.size}").delete()
    }

    fun updateCartQuantity(cartItem: CartItem, delta: Int) {
        val uid = auth.currentUser?.uid ?: return
        val index = cartList.indexOf(cartItem)
        if (index != -1) {
            val newQty = cartItem.quantity + delta
            if (newQty > 0) {
                val updatedItem = cartItem.copy(quantity = newQty)
                cartList[index] = updatedItem
                db.collection("users").document(uid).collection("cart").document("${updatedItem.product.productid}_${updatedItem.size}").set(updatedItem)
            } else {
                removeFromCart(cartItem)
            }
        }
    }

    fun toggleFavorite(product: Product) {
        val uid = auth.currentUser?.uid ?: return
        if (favoriteList.any { it.productid == product.productid }) {
            favoriteList.removeAll { it.productid == product.productid }
            db.collection("users").document(uid).collection("favorites").document(product.productid.toString()).delete()
        } else {
            favoriteList.add(product)
            db.collection("users").document(uid).collection("favorites").document(product.productid.toString()).set(product)
        }
    }

    /**
     * สั่งซื้อสินค้า และบันทึกสถานะการจัดส่ง พร้อมข้อมูลติดต่อผู้ใช้
     */
    fun placeOrder(shippingMethod: String, paymentMethod: String, total: Double) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: return
        val orderItems = cartList.map { mapOf("name" to it.product.name, "qty" to it.quantity, "size" to it.size) }
        
        // ข้อมูลออเดอร์ที่จะบันทึกลง Firestore
        val orderData = hashMapOf(
            "userId" to userId,
            "userEmail" to (currentUser.email ?: ""),
            "userPhone" to userPhone.value, // เพิ่มเบอร์โทรศัพท์
            "userAddress" to userAddress.value, // เพิ่มที่อยู่จัดส่ง
            "items" to orderItems,
            "total" to total,
            "shipping" to shippingMethod,
            "payment" to paymentMethod,
            "status" to "Shipping",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("orders").add(orderData).addOnSuccessListener { docRef ->
            // เพิ่มการแจ้งเตือนที่สวยงามและดูรายละเอียดได้
            notifications.add(0, Notification(
                title = "Package Shipped",
                message = "Your order #${docRef.id.takeLast(6).uppercase()} is on the way!",
                orderId = docRef.id,
                status = "Shipping",
                items = orderItems,
                total = total
            ))
            
            // ล้างตะกร้าสินค้า
            db.collection("users").document(userId).collection("cart").get().addOnSuccessListener { result ->
                for (doc in result) doc.reference.delete()
            }
            cartList.clear()
        }
    }

    fun addSampleProducts() {
        val samples = listOf(
            Product(1, "Puma Suede Classic XXI", "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa/global/374915/01/sv01/fnd/THA/fmt/png", 3200.0, "Puma", "Street icon."),
            Product(9, "Nike Air Force 1", "https://static.nike.com/a/images/t_PDP_1280_v1/f_auto,q_auto:eco/air-force-1-07-shoes-WrLlWX.png", 4200.0, "Nike", "Basketball icon."),
            Product(17, "Adidas Superstar", "https://assets.adidas.com/images/h_840,f_auto,q_auto,fl_lossy/superstar-shoes.png", 3200.0, "Adidas", "Shell-toe classic."),
            Product(25, "New Balance 530", "https://nb.scene7.com/is/image/NB/mr530sg_nb_02_i", 3900.0, "New Balance", "Y2K style.")
        )
        for (product in samples) db.collection("products").document(product.productid.toString()).set(product)
        fetchProducts()
    }
}
