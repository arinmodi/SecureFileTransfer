package com.example.video_share_app.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.video_share_app.repostory.FileRepository
import com.example.video_share_app.room.Database
import com.example.video_share_app.utils.Resource
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File

class EncryptionViewModel(application: Application) : ViewModel() {
    private var fileRepository: FileRepository? = null

    private val fileMutable = MutableLiveData<File>()
    val file: LiveData<File>
        get() = fileMutable


    private val mainEventMutable = MutableLiveData<MainEvent>()
    val mainEvent: LiveData<MainEvent>
        get() = mainEventMutable

    private val storeEventMutable = MutableLiveData<MainEvent>()
    val storeEvent : LiveData<MainEvent>
        get() = storeEventMutable

    val allNotes : LiveData<List<com.example.video_share_app.room.File>>

    var algo = "AES"
    var pass = ""

    sealed class MainEvent {
        class Success(val jsonObject: Any) : MainEvent()
        class Failure(val m: String) : MainEvent()
        object Empty : MainEvent()
        object Loading : MainEvent()
    }

    init {
        val dao = Database.getDatabase(application).getFileDao()
        fileRepository = FileRepository(dao)
        allNotes = fileRepository!!.allFiles
    }

    // upload image function
    fun uploadImage(file: File, expiry : String) {
        mainEventMutable.value = MainEvent.Loading

        val profileImage: RequestBody = RequestBody.create(
            MediaType.parse("application/octet-stream"),
            file
        )

        val profileImageBody: MultipartBody.Part =
            MultipartBody.Part.createFormData(
                "file",
                file.name, profileImage
            )

        val reqBody = getMultiPartFormRequestBody(expiry)


        viewModelScope.launch {

            when (val response = fileRepository?.uploadFile(profileImageBody, reqBody)) {

                is Resource.Success -> {
                    mainEventMutable.value = MainEvent.Success(response.data!!)
                    saveInfo()
                }


                is Resource.Error -> {
                    mainEventMutable.value = MainEvent.Failure(response.message!!)
                    storeEventMutable.value = MainEvent.Failure(response.message)
                    Log.e("Error : ", response.message)
                }

                // this will never happen
                else -> {}
            }
        }
    }

    private fun saveInfo() {
        val data = ((mainEventMutable.value as MainEvent.Success).jsonObject).toString()
        val json = JSONObject(data)


        val encryptedFile = com.example.video_share_app.room.File(
            algo = algo,
            searchKey = json.getString("searchKey"),
            expiry = json.getString("expiry"),
            pass = pass
        )

        viewModelScope.launch {
            when (val response = fileRepository?.insert(encryptedFile)) {
                is Resource.Success -> {
                    storeEventMutable.value = MainEvent.Success("Success")
                }

                is Resource.Error -> {
                    storeEventMutable.value = MainEvent.Failure(response.message!!)
                    Log.e("Error : ", response.message)
                }

                // this will never happen
                else -> {}
            }

        }
    }

    private fun getMultiPartFormRequestBody(tag: String?): RequestBody {
        return RequestBody.create(MultipartBody.FORM, tag!!)
    }
}