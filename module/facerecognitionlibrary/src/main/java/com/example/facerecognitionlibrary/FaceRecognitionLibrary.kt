package com.example.facerecognitionlibrary

import android.content.Context
import android.widget.Toast

class FaceRecognitionLibrary {
    companion object {
        fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}