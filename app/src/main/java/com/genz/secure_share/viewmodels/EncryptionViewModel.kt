package com.genz.secure_share.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genz.secure_share.repostory.FileRepository
import com.genz.secure_share.utils.Resource
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File

class EncryptionViewModel(private val fileRepository: FileRepository) : ViewModel() {
    private val fileMutable = MutableLiveData<File>()
    val file: LiveData<File>
        get() = fileMutable


    private val mainEventMutable = MutableLiveData<MainEvent>()

    private val storeEventMutable = MutableLiveData<MainEvent>()
    val storeEvent : LiveData<MainEvent>
        get() = storeEventMutable

    val allFiles : LiveData<List<com.genz.secure_share.room.File>> = fileRepository.allFiles

    var algo = "AES"
    var pass = ""
    var fileName = ""

    sealed class MainEvent {
        class Success(val jsonObject: Any) : MainEvent()
        class Failure(val m: String) : MainEvent()
        object Loading : MainEvent()
    }

    // upload image function
    fun uploadImage(file: File, expiry : String, iv : String) {
        mainEventMutable.value = MainEvent.Loading
        storeEventMutable.value = MainEvent.Loading

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
        val reqBody2 = getMultiPartFormRequestBody(iv)

        viewModelScope.launch {

            when (val response = fileRepository.uploadFile(profileImageBody, reqBody,
                reqBody2)) {
                is Resource.Success -> {
                    mainEventMutable.value = MainEvent.Success(response.data!!)
                    saveInfo()
                }

                is Resource.Error -> {
                    mainEventMutable.value = MainEvent.Failure(response.message!!)
                    storeEventMutable.value = MainEvent.Failure(response.message)
                    Log.e("Error : ", response.message)
                }
            }
        }
    }

    private fun saveInfo() {
        val data = ((mainEventMutable.value as MainEvent.Success).jsonObject).toString()
        val json = JSONObject(data)

        val date = json.getString("expiry").split("-")
        Log.e("Date : ", json.getString("expiry"))
        val expiry = date[2] + "-" + date[1] + "-" + date[0]
        Log.e("Expiry : ", expiry)


        val encryptedFile = com.genz.secure_share.room.File(
            name = fileName,
            algo = algo,
            searchKey = json.getString("searchKey"),
            expiry = expiry,
            pass = pass
        )

        viewModelScope.launch {
            when (val response = fileRepository.insert(encryptedFile)) {
                is Resource.Success -> {
                    storeEventMutable.value = MainEvent.Success("Success")
                }

                is Resource.Error -> {
                    storeEventMutable.value = MainEvent.Failure(response.message!!)
                    Log.e("Error : ", response.message)
                }
            }

        }
    }

    private fun getMultiPartFormRequestBody(tag: String?): RequestBody {
        return RequestBody.create(MultipartBody.FORM, tag!!)
    }

    fun deleteFile(searchKey : String, file : com.genz.secure_share.room.File) {
        mainEventMutable.value = MainEvent.Loading
        storeEventMutable.value = MainEvent.Loading

        viewModelScope.launch {
            when (val result= fileRepository.deleteFile(searchKey)) {
                is Resource.Success -> {
                    mainEventMutable.value = MainEvent.Success(result.data!!)
                    deleteInfo(file)
                }


                is Resource.Error -> {
                    mainEventMutable.value = MainEvent.Failure(result.message!!)
                    storeEventMutable.value = MainEvent.Failure(result.message)
                    Log.e("Error : ", result.message)
                }
            }
        }
    }

    private fun deleteInfo(file : com.genz.secure_share.room.File) {
        viewModelScope.launch {
            when (val response = fileRepository.delete(file)) {
                is Resource.Success -> {
                    storeEventMutable.value = MainEvent.Success("Success")
                }

                is Resource.Error -> {
                    storeEventMutable.value = MainEvent.Failure(response.message!!)
                    Log.e("Error : ", response.message)
                }
            }

        }
    }
}