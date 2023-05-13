package com.example.myapplication

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("dbTest","[MainActivity]  db테스트시작")

        /*DB생성*/
        val db=DB(this)
        val isCreate = db.createTable()
        Log.d("dbTest","[MainActivity]  isCreate = "+isCreate)

        /*테스트*/

        //createID테스트
        val test_id=db.createID(doubleArrayOf(2.0,3.0,1.0))
        Log.d("dbTest","[MainActivity]  test_ID = $test_id")

        //sizeOfUser 테스트
        val size = db.sizeOfUser()
        Log.d("dbTest","[MainActivity]  size = $size")

        //selectAllUser테스트
        val list = db.selectAllUser()
        for(user in list){
            Log.d("dbTest","[MainActivity]  userId = ${user.ID} , vector[0] = ${user.vector[0]}")
        }

        //selectUser 테스트
        val user = db.selectUser("ID_1")
        Log.d("dbTest","[MainActivity]  selectUser : $user")
        val user2 = db.selectUser("ID_13") // error테스트
        Log.d("dbTest","[MainActivity]  selectUser : $user2")

        //updateUser 테스트트

        Log.d("dbTest","[MainActivity]  db테스트종료")
        setContentView(R.layout.activity_main)
    }
}