package com.example.facerecognitionmodule

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import com.example.facerecognitionmodule.databinding.ActivityGeneralOrderBinding
import java.text.NumberFormat
import java.util.*

class general_order_activity : AppCompatActivity() {
    lateinit var binding: ActivityGeneralOrderBinding
    val price = 3000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityGeneralOrderBinding.inflate(layoutInflater)

        /* 장바구니 리스트 */
        val menuBasketList = listOf(
            binding.layoutBasket1,
            binding.layoutBasket2,
            binding.layoutBasket3,
            binding.layoutBasket4,
            binding.layoutBasket5
        )
        menuBasketList.forEachIndexed{index,layout->
            layout.visibility= View.GONE
        }

        val menuCountArray = IntArray(5) // menu 5개 카운트
        val menuCountBinding = listOf( // 장바구니 메뉴 count
            binding.textMenu1Cnt,binding.textMenu2Cnt,
            binding.textMenu3Cnt,binding.textMenu4Cnt,
            binding.textMenu5Cnt)
        val menuPriceBinding = listOf( // 장바구니 메뉴 count
            binding.textMenu1Price,binding.textMenu2Price,
            binding.textMenu3Price,binding.textMenu4Price,
            binding.textMenu5Price)
        val menuList = listOf( // 메뉴선택 레이아웃
            binding.layoutMenu1, binding.layoutMenu2,
            binding.layoutMenu3, binding.layoutMenu4,
            binding.layoutMenu5
        )

//      각 메뉴 클릭 리스너
        menuList.forEachIndexed { index, menu ->
            menu.setOnClickListener {
                Log.d("orderTest", "menu${index + 1}_click")
                menuCountArray[index]++
                menuBasketList[index].visibility=View.VISIBLE
                menuCountBinding[index].text=menuCountArray[index].toString()+"개"
                val formattedAmount = NumberFormat.getNumberInstance(Locale.getDefault()).format(price*menuCountArray[index])
                menuPriceBinding[index].text=formattedAmount+"원"

            }
        }
        binding.btnPay.setOnClickListener{
            Log.d("orderTest", "${menuCountArray[0]}, ${menuCountArray[1]}, " +
                    "${menuCountArray[2]}, ${menuCountArray[3]}, ${menuCountArray[4]}")
        }


        setContentView(binding.root)

    }
}