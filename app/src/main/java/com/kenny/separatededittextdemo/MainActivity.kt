package com.kenny.separatededittextdemo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.kenny.separatededittext.SeparatedEditText
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var showContent = false
    var showCursor = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun handleContent(v: View?) {
        edit_solid.setPassword(!showContent)
        edit_hollow.setPassword(!showContent)
        edit_underline.setPassword(!showContent)
        showContent = !showContent
    }

    fun handleCursor(v: View?) {
        edit_solid.setShowCursor(!showCursor)
        edit_hollow.setShowCursor(!showCursor)
        edit_underline.setShowCursor(!showCursor)
        showCursor = !showCursor
    }
}