package org.d3if3157.assessment3.ui.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.d3if3157.assessment3.model.Galeri
import org.d3if3157.assessment3.network.ApiStatus
import org.d3if3157.assessment3.network.GaleriApi
import java.io.ByteArrayOutputStream

class MainViewModel : ViewModel() {
    var data = mutableStateOf(emptyList<Galeri>())
        private set
    var status = MutableStateFlow(ApiStatus.LOADING)
        private  set
    var errorMessage = mutableStateOf<String?>(null)
        private set

    fun retrieveData(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            status.value = ApiStatus.LOADING
            try {
                data.value = GaleriApi.service.getHewan(userId)
                Log.d("MainViewModel", "Berhasil")
                status.value = ApiStatus.SUCCESS
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure Retrieve Data: ${e.message}")
                status.value = ApiStatus.FAILED
            }
        }
    }
    fun postGaleri(id: String, namaLengkap: String, deskripsi: String, imageUrl: String, mine: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val galeri = Galeri(id, namaLengkap, deskripsi, imageUrl, mine)
                GaleriApi.service.postGaleri(galeri)
                retrieveData(mine)
                Log.d("MainViewModel", "Berhasil")

            } catch (e: Exception) {
                Log.e("MainViewModel", "Error posting galeri", e)
                errorMessage.value = "Error: ${e.message}"
            }

        }
    }
    suspend fun deleteImage(userId: String, id: String) {
        try {
            val result = GaleriApi.service.deleteGaleri(id)
                Log.d("MainViewModel", "Berhasil menghapus gambar")
                retrieveData(userId)
        } catch (e: Exception) {
            Log.d("MainViewModel", "Gagal Hapus gambar: ${e.message}")
        }
    }
    private fun Bitmap.toMultipartBody(): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody(
            "image/jpg".toMediaTypeOrNull(), 0, byteArray.size
        )
        return MultipartBody.Part.createFormData(
            "image", "image.jpg", requestBody
        )
    }
    fun clearMessage() { errorMessage.value = null}

}

