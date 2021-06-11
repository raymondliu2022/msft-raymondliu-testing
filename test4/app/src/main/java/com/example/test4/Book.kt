package com.example.test4

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.system.measureTimeMillis

class Book(inFilePath: String, inAssetManager: AssetManager) {
    private val filePath: String = inFilePath
    private val assetManager: AssetManager = inAssetManager
    lateinit var chapterStarts: ArrayList<Int>
    private lateinit var paragraphStrings: ArrayList<String>
    private var currentChapter = -1

    fun indexChapters() {
        try {
            val bufferedReader = BufferedReader(InputStreamReader(assetManager.open(filePath)))

            chapterStarts = ArrayList()
            var line = 0
            var lineString: String? = ""
            var emptyLineCount = 2

            val executionTime = measureTimeMillis {
                do {
                    if (lineString!!.isEmpty()) {
                        emptyLineCount += 1
                    } else {
                        emptyLineCount = 0
                    }
                    if (emptyLineCount == 3) {
                        chapterStarts.add(line)
                    }
                    line += 1
                    lineString = bufferedReader.readLine()
                } while (lineString != null)
            }

            Log.d("RL2022", "indexChapters() - ${chapterStarts.size} chapters found - ${line - 1} lines read - $executionTime milliseconds")
        }
        catch (e: IOException) {
            Log.d("RL2022", "indexChapters() - IOException!")
        }
    }

    fun loadChapterAtLine(line: Int) {
        if (chapterStarts == null) {
            Log.d("RL2022", "loadChapterAtLine( line $line ) - chapters haven't been indexed yet!")
            return
        }

        var chapter = 0
        for (chapterStart in chapterStarts) {
            if (line < chapterStart) {break;}
            chapter += 1
        }
        chapter -= 1

        if (chapter >= chapterStarts.size) {
            Log.d("RL2022", "loadChapterAtLine( line $line ) - lineIndex too large!")
            return
        }

        if (chapter == currentChapter) {
            Log.d("RL2022", "loadChapterAtLine( line $line ) - same chapter")
            return
        }

        currentChapter = chapter
        val chapterStart = chapterStarts[chapter]
        val chapterEnd = chapterStarts[chapter + 1]

        try {
            paragraphStrings = ArrayList()

            val bufferedReader = BufferedReader(InputStreamReader(assetManager.open(filePath)))

            for (line in 0..chapterStart) {
                bufferedReader.readLine()
            }

            val executionTime = measureTimeMillis {
                var paragraphBuffer = StringBuffer()
                var lineBuffer = ""
                for (line in chapterStart..chapterEnd) {
                    lineBuffer = bufferedReader.readLine()
                    if (lineBuffer.isEmpty()) {
                        paragraphStrings.add(paragraphBufferToString(paragraphBuffer))
                        paragraphBuffer = StringBuffer()
                    } else {
                        paragraphBuffer.append(lineBuffer)
                    }
                }
                //paragraphStrings.add(paragraphBufferToString(paragraphBuffer))
            }

            Log.d("RL2022", "loadChapterAtLine( line $line ) - ${paragraphStrings.size} paragraphs found - chapter $chapter - read lines $chapterStart to $chapterEnd - $executionTime milliseconds")
        }
        catch (e: IOException) {
            Log.d("RL2022", "loadChapterAtLine( line $line ) - IOException!")
        }
    }

    fun paragraphBufferToString(inBuffer: StringBuffer): String {
        return "\t${inBuffer.toString().replace("\n","").replace("\t","")}" //Fix!
    }

    fun getParagraphStrings(): ArrayList<String> {
        return paragraphStrings
    }
}