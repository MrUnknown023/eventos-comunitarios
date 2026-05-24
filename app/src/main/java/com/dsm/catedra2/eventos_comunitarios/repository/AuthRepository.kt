package com.dsm.catedra2.eventos_comunitarios.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    // Retornan un Pair con el ID del usuario y su Rol
    suspend fun login(email: String, pass: String): Result<Pair<String, String>>
    suspend fun register(email: String, pass: String, role: String): Result<Pair<String, String>>
    suspend fun loginWithGoogle(idToken: String): Result<Pair<String, String>>
    suspend fun getUserRole(userId: String): String
    fun getCurrentUserId(): String?
    fun logout()
}

class FirebaseAuthRepository : AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance() // Instancia de Firestore

    override suspend fun login(email: String, pass: String): Result<Pair<String, String>> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: throw Exception("Usuario no encontrado")
            val role = getUserRole(uid) // Buscamos su rol en Firestore
            Result.success(Pair(uid, role))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, pass: String, role: String): Result<Pair<String, String>> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: throw Exception("Error al crear usuario")

            // Guardamos el perfil en la colección "users" de Firestore
            val userData = hashMapOf("email" to email, "role" to role)
            db.collection("users").document(uid).set(userData).await()

            Result.success(Pair(uid, role))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<Pair<String, String>> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val uid = result.user?.uid ?: throw Exception("Error de Google")

            // Si el usuario de Google entra por primera vez, le asignamos rol "user" por defecto
            var role = "user"
            try {
                role = getUserRole(uid)
            } catch (e: Exception) {
                val userData = hashMapOf("email" to (result.user?.email ?: ""), "role" to role)
                db.collection("users").document(uid).set(userData).await()
            }
            Result.success(Pair(uid, role))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserRole(userId: String): String {
        val doc = db.collection("users").document(userId).get().await()
        return doc.getString("role") ?: "user"
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid
    override fun logout() = auth.signOut()
}