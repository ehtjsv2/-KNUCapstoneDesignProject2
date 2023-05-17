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
        val menuCountArray = IntArray(5) // menu 5개 카운트

        val menuList = listOf(
            binding.layoutMenu1,
            binding.layoutMenu2,
            binding.layoutMenu3,
            binding.layoutMenu4,
            binding.layoutMenu5
        )

//      각 메뉴 클릭 리스너
        menuList.forEachIndexed { index, menu ->
            menu.setOnClickListener {
                Log.d("orderTest", "menu${index + 1}_click")
                menuCountArray[index]++
            }
        }
        binding.btnPay.setOnClickListener{
            Log.d("orderTest", "${menuCountArray[0]}, ${menuCountArray[1]}, ${menuCountArray[2]}, ${menuCountArray[3]}, ${menuCountArray[4]}")
        }


        setContentView(binding.root)

    }
}