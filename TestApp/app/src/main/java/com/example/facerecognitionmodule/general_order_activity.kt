package com.example.facerecognitionmodule

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import com.example.facerecognitionmodule.databinding.ActivityGeneralOrderBinding

class general_order_activity : AppCompatActivity() {
    lateinit var binding: ActivityGeneralOrderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityGeneralOrderBinding.inflate(layoutInflater)


        setContentView(binding.root)

    }
}