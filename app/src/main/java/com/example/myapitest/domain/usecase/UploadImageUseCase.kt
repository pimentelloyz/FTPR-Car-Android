package com.example.myapitest.domain.usecase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UploadImageUseCase {

    private val storage = FirebaseStorage.getInstance()

    suspend operator fun invoke(uri: Uri): String {
        val ref = storage.reference.child("cars/${UUID.randomUUID()}.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}
