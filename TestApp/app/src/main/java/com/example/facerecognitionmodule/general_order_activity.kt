package com.example.facerecognitionmodule

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import com.example.facerecognitionmodule.databinding.ActivityGeneralOrderBinding

class general_order_activity : AppCompatActivity() {
    lateinit var binding: ActivityGeneralOrderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityGeneralOrderBinding.inflate(layoutInflater)
        binding.layoutMenu1.setOnClickListener {
            Log.d("orderTest","menu1_click")
        }
        binding.layoutMenu2.setOnClickListener {
            Log.d("orderTest","menu2_click")
        }
        binding.layoutMenu3.setOnClickListener {
            Log.d("orderTest","menu3_click")
        }
        binding.layoutMenu4.setOnClickListener {
            Log.d("orderTest","menu4_click")
        }
        binding.layoutMenu5.setOnClickListener {
            Log.d("orderTest","menu5_click")
        }

        setContentView(binding.root)

    }
}