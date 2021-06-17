package com.example.test4

import android.content.Context
import android.content.res.AssetManager
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.TextView
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.min

class Book(inFilePath: String, inAssetManager: AssetManager, inActivityContext: Context) {
    private val filePath: String = inFilePath
    private val assetManager: AssetManager = inAssetManager
    var chapterStarts: ArrayList<Int> = ArrayList()
    private val activityContext: Context = inActivityContext

    init {
        val bufferedReader = BufferedReader(InputStreamReader(assetManager.open(filePath)))

        var line = 0
        var lineString: String? = ""
        var emptyLineCount = 2

        do {
            if (lineString!!.isEmpty()) {
                emptyLineCount += 1
            } else {
                if (emptyLineCount >= 3) {
                    chapterStarts.add(line)
                }
                emptyLineCount = 0
            }
            line += 1
            lineString = bufferedReader.readLine()
        } while (lineString != null)
        bufferedReader.close()
    }

    lateinit var paragraphStrings: ArrayList<String>
    var currentChapter = 0
        set(inChapter) {
            field = inChapter

            paragraphStrings = ArrayList()

            if (field >= 0 && field < chapterStarts.size) {
                val bufferedReader = BufferedReader(InputStreamReader(assetManager.open(filePath)))

                for (line in 0 until chapterStarts[field] - 1) {
                    bufferedReader.readLine()
                }

                var paragraphBuffer = StringBuffer()
                var lineBuffer = ""
                for (line in chapterStarts[field] until chapterStarts[field + 1]) {
                    lineBuffer = bufferedReader.readLine()
                    if (lineBuffer.isEmpty()) {
                        if (!paragraphBuffer.isEmpty()) {
                            paragraphStrings.add(textStringHelper(paragraphBuffer.toString()))
                            paragraphBuffer = StringBuffer()
                        }
                    } else {
                        paragraphBuffer.append("$lineBuffer ")
                    }
                }
                bufferedReader.close()
            }
        }

    var paragraphWidth = 0
    var screenHeight = 0
    var currentParagraph = 0

    lateinit var underflowTextView: TextView
    lateinit var underflowTextView2: TextView
    lateinit var overflowTextView: TextView
    lateinit var overflowTextView2: TextView

    lateinit var pageViews: ArrayList<ArrayList<TextView>>
    fun getPageView(index: Int): ArrayList<TextView> {
        return if (index < pageViews.size && index >= 0) {
            pageViews[index]
        }
        else {
            ArrayList()
        }
    }

    var currentPage: Int
        get(){
            var page = 0
            var remainingParagraphs = currentParagraph
            while (page < pageViews.size && pageViews[page].size <= remainingParagraphs ) {
                remainingParagraphs -= pageViews[page].size
                page += 1
            }
            Log.d("RL2022","get page $page paragraph $currentParagraph")
            return page
        }
        set(inPage) {
            currentParagraph = 0
            for (page in 0 until min(inPage, pageViews.size)) {
                currentParagraph += pageViews[page].size
            }
            Log.d("RL2022","set page $inPage paragraph $currentParagraph")
        }

    fun buildPages() {
        pageViews = ArrayList()
        pageViews.add(ArrayList())

        var availableHeight = screenHeight
        for (paragraphString in paragraphStrings) {
            val textView =
                View.inflate(activityContext, R.layout.paragraph_text_layout, null) as TextView
            textView.text = Html.fromHtml(paragraphString)
            textView.measure(
                View.MeasureSpec.makeMeasureSpec(
                    paragraphWidth,
                    View.MeasureSpec.AT_MOST
                ), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            if (textView.measuredHeight > availableHeight) {
                pageViews.add(ArrayList())
                availableHeight = screenHeight
            }
            availableHeight -= textView.measuredHeight
            pageViews[pageViews.size - 1].add(textView)
        }

        underflowTextView = View.inflate(activityContext, R.layout.underflow_text_layout, null) as TextView
        underflowTextView2 = View.inflate(activityContext, R.layout.underflow_text_layout, null) as TextView
        overflowTextView = View.inflate(activityContext, R.layout.overflow_text_layout, null) as TextView
        overflowTextView2 = View.inflate(activityContext, R.layout.overflow_text_layout, null) as TextView
    }

    private fun textStringHelper(inString: String): String {
        var step1 = inString.replace("\\s+".toRegex(), " ")
        var step2 = step1.replace("_(?<italics>((?!_).)+)_".toRegex(),"<i>\${italics}</i>")

        return "\t$step2"
    }
}
