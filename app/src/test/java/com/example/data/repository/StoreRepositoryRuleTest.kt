package com.example.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Product
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createStoreAndAddProduct_authenticatedOwner_succeedsAndEmits(): Unit = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StoreRepository(db = firestore, appContext = context)

        val storeId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createStore("Dokan Test", "01711111111", "Dhaka")
        }
        assertTrue(storeId.isNotBlank())

        val outcome = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addProduct(
                storeId = storeId,
                product = Product(
                    storeId = storeId,
                    name = "Test Sugar",
                    buyPrice = 100.0,
                    sellPrice = 120.0,
                    stock = 10L
                )
            )
        }
        assertTrue(outcome.productId.isNotBlank())

        val products = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeProducts(storeId).first { list -> list.any { it.id == outcome.productId } }
        }
        assertTrue(products.any { it.name == "Test Sugar" })
    }

    @Test
    fun observeProducts_crossUserAccess_failsWithPermissionDenied(): Unit = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val aliceRepo = StoreRepository(db = firestore, appContext = context)
        val storeId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createStore("Alice Private Shop", "01711111111", "Dhaka")
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = StoreRepository(db = firestore, appContext = context)
        val exception = runCatching {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeProducts(storeId).first()
            }
        }.exceptionOrNull()

        val firestoreEx = checkNotNull(exception?.unwrapFirestoreException()) {
            "Expected FirebaseFirestoreException for unauthorized user, got $exception"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
    }

    @Test
    fun observeProducts_unauthenticatedUser_failsWithPermissionDenied(): Unit = runBlocking {
        auth.signOut()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StoreRepository(db = firestore, appContext = context)

        val exception = runCatching {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeProducts("some_store_id").first()
            }
        }.exceptionOrNull()

        val firestoreEx = checkNotNull(exception?.unwrapFirestoreException()) {
            "Expected FirebaseFirestoreException for unauthenticated user, got $exception"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_storeos@test.com"
        const val BOB_EMAIL = "bob_storeos@test.com"
        const val DEFAULT_TIMEOUT_MS = 8000L
        const val FLOW_TIMEOUT_MS = 5000L
    }
}
