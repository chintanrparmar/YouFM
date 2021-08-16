package app.chintan.youfm.home

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.chintan.youfm.data.State
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val storageRef = Firebase.storage.reference

    private val _downloadURL = MutableLiveData<State<String>>()
    val downloadURL: LiveData<State<String>> get() = _downloadURL

    private val _uploadFileLD = MutableLiveData<State<String>>()
    val uploadFileLD: LiveData<State<String>> get() = _uploadFileLD

    private val _audioList = MutableLiveData<State<List<StorageReference>>>()
    val audioList: LiveData<State<List<StorageReference>>> get() = _audioList

    fun getDownloadURL(path: String) {
        _downloadURL.postValue(State.loading())
        viewModelScope.launch(Dispatchers.IO) {
            storageRef.child(path).downloadUrl.addOnSuccessListener {
                _downloadURL.postValue(State.success(it.toString()))
            }.addOnFailureListener {
                _downloadURL.postValue(State.failure(it))
            }
        }
    }

    fun getAudioList() {
        _audioList.postValue(State.loading())
        val listRef = storageRef.child("audio")
        viewModelScope.launch(Dispatchers.IO) {
            listRef.listAll()
                .addOnSuccessListener { (items, _) ->
                    _audioList.postValue(State.success(items))
                }
                .addOnFailureListener {
                    _audioList.postValue(State.failure(it))
                }
        }

    }

    fun uploadAudio(uri: Uri?, fileName: String) {
        _uploadFileLD.postValue(State.loading())
        viewModelScope.launch(Dispatchers.IO) {
            try {
                uri?.let {
                    storageRef.child("audio/$fileName").putFile(uri).addOnProgressListener {
                    }.addOnSuccessListener {
                        _uploadFileLD.postValue(State.success("File Uploaded"))
                    }
                }
            } catch (e: Exception) {
                _uploadFileLD.postValue(State.failure(e))
            }
        }
    }

}
