package com.example.facerecognitionmodule

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.openOrCreateDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.*

const val menu1:String = "americano_cnt"
const val menu2:String = "caffelatte_cnt"
const val menu3:String = "cappuccino_cnt"
const val menu4:String = "coldbrew_cnt"
const val menu5:String = "caffemocha_cnt"

/** 앱 최초 시작시 createTable()을 해야 테이블 생성됨*/
class DB(context: Context){
    var DBHlper:SQLiteOpenHelper
    var isCraate:Int=0
    init {
        DBHlper=DbHelper(context)
    }
    /** 테이블 생성, 앱 최초 실행이 아닐경우 이 함수를 실행해도 아무 것도 실행 되지않음(isCreate->0을 반환)*/
    fun createTable():Int{

        val w_db = DBHlper.writableDatabase // 쓰기용 으로 db열기
        w_db.close()
        return isCraate
    }

    /** User의 수를 반환합니다*/
    fun sizeOfUser():Int{
        val r_db = DBHlper.readableDatabase

        val countQuery = "SELECT COUNT(*) FROM USER;"
        val cursor = r_db.rawQuery(countQuery, null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        Log.d("dbTest","[sizeOfUser()]  row count = "+count.toString())
        return count
    }

    /**해당 벡터값을 가진 User를 생성, user_ID 반환 */
    fun createID(vector: DoubleArray):String?{

        val w_db=DBHlper.writableDatabase // 쓰기용으로 db 열기

        val vectorString = Arrays.toString(vector) // doubleArray타입을 String으로 변환
        val rowCnt = sizeOfUser()+1 // 현재 테이블의 마지막 ID+1
        val newId="ID_"+rowCnt.toString() // newID 생성

        Log.d("dbTest","[createID()]  ID생성 : id($newId), vector($vectorString)")

        try {
            w_db.execSQL("Insert into USER VALUES(\"$newId\",\"$vectorString\",0,0,0,0,0)")
            w_db.close()
        }catch (e :Exception){
            e.printStackTrace()
            Log.e("dbTest", "[createID()]  Insert Error : ${e.message}")
            w_db.close()
            return null
        }
        return newId
    }


    /** 모든 User를 반환합니다.(MutableList<User>반환)*/
    fun selectAllUser():MutableList<User>{

        val list = mutableListOf<User>()

        val sql = "select * from User"
        val r_db = DBHlper.readableDatabase
        val cursor = r_db.rawQuery(sql,null)
        while (cursor.moveToNext()){
            val id = cursor.getString(0)
            val vector = cursor.getString(1).removeSurrounding("[","]")
                .split(",").map{it.trim().toDouble()}
            val array_vector=vector.toDoubleArray()
            val menu1 = cursor.getInt(2)
            val menu2 = cursor.getInt(3)
            val menu3 = cursor.getInt(4)
            val menu4 = cursor.getInt(5)
            val menu5 = cursor.getInt(6)
            val user = User(id,array_vector,menu1,menu2,menu3,menu4,menu5)
            list.add(user)
        }
        cursor.close()
        r_db.close()
        return list
    }

    /** 특정ID를 가진 유저를 반환 */
    fun selectUser(userId:String):User?{
        val r_db = DBHlper.readableDatabase
        var user:User
        try {
            val query = "select * from User where id=\"$userId\""
            val cursor = r_db.rawQuery(query,null)
            cursor.moveToNext()
            val id = cursor.getString(0)
            val vector = cursor.getString(1).removeSurrounding("[","]")
                .split(",").map{it.trim().toDouble()}
            val array_vector=vector.toDoubleArray()
            val menu_1 = cursor.getInt(2)
            val menu_2 = cursor.getInt(3)
            val menu_3 = cursor.getInt(4)
            val menu_4 = cursor.getInt(5)
            val menu_5 = cursor.getInt(6)
            user = User(id,array_vector,menu_1,menu_2,menu_3,menu_4,menu_5)
        }
        catch (e : Exception){
            Log.e("dbTest","[selectUser()]  select error : No matched ID(${e.message})")
            r_db.close()
            return null
        }

        return user
    }
    /** User의 주문이력(음료_cnt)을 수정합니다 Id와 음료5가지cnt를 매개변수로.*/
    fun updateUser(Id:String,menu_1:Int,menu_2:Int,menu_3:Int,menu_4:Int,menu_5:Int):Int{
        val w_db=DBHlper.writableDatabase
        try{
            val query = "UPDATE USER SET $menu1=?, $menu2=?, $menu3=?, $menu4=?, $menu5=? WHERE id = ?"
            Log.d("dbTest","[updateUser()]  query : $query")

            val statement = w_db.compileStatement(query)
            statement.bindLong(1, menu_1.toLong())
            statement.bindLong(2, menu_2.toLong())
            statement.bindLong(3, menu_3.toLong())
            statement.bindLong(4, menu_4.toLong())
            statement.bindLong(5, menu_5.toLong())
            statement.bindString(6, Id)
            statement.executeUpdateDelete()
            statement.close()
            //UPDATE 테이블명 SET 컬럼명 1 = 값1, 컬럼명2 = 값2, ... WHERE 조건식;
        }catch (e:Exception){
            Log.e("dbTest","[updateUser()]  update error : (${e.message})")
            w_db.close()
            return 0
        }
        w_db.close()
        return 1;
    }

    /** 직접 사용하고 싶은 sql을 수행합니다.*/
    fun customSql(sql:String,mode:Int):Int{
        when(mode){
            0->{
                val r_db = DBHlper.readableDatabase
                r_db.execSQL(sql)
            }
        }
        return 0;
    }


    /** sqlite 수행에 도움을 주는 클래스 */
    inner class DbHelper(context: Context):SQLiteOpenHelper(context,"db",null,1) {
        /* 앱 최초 실행 후 db이용할 때 한번 실행되는 함수, 다시하고싶으면 휴대폰 내 어플데이터삭제->어플삭제 */
        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL("create table USER(" +
                    "id TEXT primary key," +
                    "vector TEXT not null," +
                    "americano_cnt integer not null," +
                    "caffelatte_cnt integer not null," +
                    "cappuccino_cnt integer not null," +
                    "coldbrew_cnt integer not null," +
                    "caffemocha_cnt integer not null" +
                    ")")
            isCraate=1
        }
        /*생성자에 지정한 버전 변경이 있을 때 실행되는 함수 */
        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

        }
    }
}
