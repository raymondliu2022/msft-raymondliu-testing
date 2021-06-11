package com.example.test4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setMargins

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val book = Book("books/test_book.txt", assets)
        book.indexChapters()
        book.loadChapterAtLine(2000)

        val linearLayout = findViewById<LinearLayout>(R.id.temp_linear_layout)
        for (paragraphString in book.getParagraphStrings()) {
            val textView = TextView(this)
            textView.setText(paragraphString)
            textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            textView.setPadding(10,10,10, 10)
            linearLayout.addView(textView)
        }

    }
}