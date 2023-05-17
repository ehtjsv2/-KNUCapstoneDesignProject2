package com.example.facerecognitionmodule

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.facerecognitionmodule.databinding.ActivitySimpleOrderBinding
import kotlin.Pair


class simple_order_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        lateinit var binding: ActivitySimpleOrderBinding
        super.onCreate(savedInstanceState)
        binding = ActivitySimpleOrderBinding.inflate(layoutInflater)

        setContentView(binding.root)


        val db = DB(this)
        val vector = doubleArrayOf(0.1, 0.2, 0.3)
        val userId = db.createID(vector)!!
        println("유저 아이디: $userId")
        val updateResult = db.updateUser(userId, 4, 7, 8, 3, 10)

        userId?.let {
            // User ID 생성 확인
            Log.d("DB", "생성된 유저 ID: $it")

            // 유저 정보 조회
            val user = db.selectUser(userId)
            val menuImageView = binding.menuImageView
            val menuName = binding.menuNameTextView

            user?.let {
                Log.d("DB", "유저: $it")

                val menuCounts = listOf(
                    user.americano_cnt,
                    user.caffelatte_cnt,
                    user.cappuccino_cnt,
                    user.coldbrew_cnt,
                    user.caffemocah_cnt
                )
                val maxCount = menuCounts.maxOrNull() ?: 0 // 가장 많이 선택된 메뉴 수
                val mostOrderedMenuIndex = menuCounts.indexOf(maxCount) // 가장 많이 선택된 메뉴 인덱스

                val mostOrderedMenu = when (mostOrderedMenuIndex) {
                    0 -> "아메리카노" // 1이 가장 많이 선택됨
                    1 -> "카페라떼" // 2가 가장 많이 선택됨
                    2 -> "카푸치노" // 3이 가장 많이 선택됨
                    3 -> "콜드브루" // 4가 가장 많이 선택됨
                    4 -> "카페모카" // 5가 가장 많이 선택됨
                    else -> null
                }

                println("메뉴 명 : $mostOrderedMenu")

                if (mostOrderedMenu != null) {
                    val drawableId = if (mostOrderedMenu == "아메리카노") {
                        R.drawable.americano
                    } else if (mostOrderedMenu == "카페라떼") {
                        R.drawable.caffelatte
                    } else if (mostOrderedMenu == "카푸치노") {
                        R.drawable.cappuccino
                    } else if (mostOrderedMenu == "콜드브루") {
                        R.drawable.coldbrew
                    } else {
                        R.drawable.caffemocha
                    }
                    menuImageView.setImageResource(drawableId)
                    menuName.text = mostOrderedMenu
                }
            }
            binding.grayButton.setOnClickListener {
                finish()
            }
        }
    }
}
