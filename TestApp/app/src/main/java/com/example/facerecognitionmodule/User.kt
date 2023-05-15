package com.example.facerecognitionmodule

import java.util.Vector

data class User(val ID:String,val vector: DoubleArray, val americano_cnt:Int, val caffelatte_cnt:Int,
                    val cappuccino_cnt:Int, val coldbrew_cnt:Int, val caffemocah_cnt:Int)
