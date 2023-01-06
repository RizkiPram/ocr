package com.example.ocr.viewmodel

import androidx.lifecycle.ViewModel
import java.io.File

class MainViewModel: ViewModel() {
    var getFile: File? = null

    fun getFileResult(getFiles: File?){
        getFile = getFiles
    }
}