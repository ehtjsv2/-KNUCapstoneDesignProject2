package com.example.facerecognitionmodule

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout

class general_order_activity : AppCompatActivity() {
    private lateinit var selectedDrinks: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general_order)

        selectedDrinks = mutableListOf()

        val drinkListLayout: LinearLayout = findViewById(R.id.drinkListLayout)
        val completeButton: Button = findViewById(R.id.completeButton)
        val backButton: Button = findViewById(R.id.backButton)

        val buttons: List<Button> = listOf(
            findViewById(R.id.americanoButton),
            findViewById(R.id.cafeLatteButton),
            findViewById(R.id.cappuccinoButton),
            findViewById(R.id.coldBrewButton),
            findViewById(R.id.caffeMochaButton)
        )


        // 초기 상태에서 버튼의 배경색을 변경
        for (button in buttons) {
            button.setBackgroundColor(Color.GRAY)
        }

        for (button in buttons) {
            button.setOnClickListener {
                val drinkName = button.text.toString()

                if (button.isSelected) {
                    selectedDrinks.add(drinkName)
                    button.isSelected = false
                    button.setBackgroundColor(Color.GRAY)
                } else {
                    selectedDrinks.remove(drinkName)
                    button.isSelected = true
                    button.setBackgroundColor(resources.getColor(R.color.purple_500))
                }
            }
        }

        completeButton.setOnClickListener {
            completeSelection()
        }

        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun completeSelection() {
        val intent = Intent()
        intent.putStringArrayListExtra("selectedDrinks", ArrayList(selectedDrinks))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}