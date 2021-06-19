package com.example.test4

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Rect
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
    private val activityContext: Context = inActivityContext
    private var chapterStarts: ArrayList<Int> = ArrayList()
    private var chapterLengths: ArrayList<Int> = ArrayList()
    var numChapters = 0
        get() = chapterStarts.size

    init {
        val bufferedReader = BufferedReader(InputStreamReader(assetManager.open(filePath)))

        var line = 0
        var lineCount = 0
        var lineString: String? = ""
        var emptyLineCount = 2

        do {
            if (lineString!!.isEmpty()) {
                emptyLineCount += 1
            } else {
                if (emptyLineCount >= 3) {
                    chapterStarts.add(line)
                    chapterLengths.add(lineCount)
                    lineCount = 0
                }
                emptyLineCount = 0
            }
            line += 1
            lineCount += 1
            lineString = bufferedReader.readLine()
        } while (lineString != null)
        bufferedReader.close()

        chapterLengths.add(lineCount)
        chapterLengths.removeAt(0)
    }

    private lateinit var paragraphStrings: ArrayList<String>
    var chapterTitle = ""
        get() = paragraphStrings[0]

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
                for (line in 0 until chapterLengths[field]) {
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

            buildPages()
        }


    private var currentParagraph = 0

    private lateinit var pageViews: ArrayList<ArrayList<TextView>>
    fun getPageView(index: Int): ArrayList<TextView> {
        return if (index < pageViews.size && index >= 0) {
            pageViews[index]
        }
        else {
            ArrayList()
        }
    }
    var numPages = 0
        get() = pageViews.size

    var currentPage: Int
        get(){
            var page = 0
            var remainingParagraphs = currentParagraph
            while (page < pageViews.size && pageViews[page].size <= remainingParagraphs ) {
                remainingParagraphs -= pageViews[page].size
                page += 1
            }
            return page
        }
        set(inPage) {
            currentParagraph = 0
            for (page in 0 until min(inPage, pageViews.size)) {
                currentParagraph += pageViews[page].size
            }
        }

    var pageRects = ArrayList<Rect>()
        set(input) {
            field = input
            buildPages()
        }

    var fontSize = 12
        set(input) {
            field = input
            buildPages()
        }

    fun buildPages() {
        if (pageRects.isEmpty()) { return }

        val pagePadding = activityContext.resources.getDimension(R.dimen.page_padding).toInt()

        pageViews = ArrayList()
        var textViews = ArrayList<TextView>()

        var pageRectIndex = 0
        var availableHeight = pageRects[pageRectIndex].height() - (2 * pagePadding)
        for (paragraphString in paragraphStrings) {
            val textView =
                View.inflate(activityContext, R.layout.book_page_text_paragraph, null) as TextView
            textView.text = Html.fromHtml(paragraphString)
            textView.textSize = fontSize.toFloat()
            textView.measure(
                View.MeasureSpec.makeMeasureSpec(
                    pageRects[pageRectIndex].width() - (2 * pagePadding),
                    View.MeasureSpec.AT_MOST
                ), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            if (textView.measuredHeight > availableHeight) {
                pageViews.add(textViews)
                textViews = ArrayList()
                pageRectIndex = (pageRectIndex + 1) % pageRects.size
                availableHeight = pageRects[pageRectIndex].height() - (2 * pagePadding)
            }
            availableHeight -= textView.measuredHeight
            textViews.add(textView)
        }

        pageViews.add(textViews)
    }

    private fun textStringHelper(inString: String): String {
        var step1 = inString.replace("\\s+".toRegex(), " ")
        var step2 = step1.replace("_(?<italics>((?!_).)+)_".toRegex(),"<i>\${italics}</i>")

        return "\t$step2"
    }
}
