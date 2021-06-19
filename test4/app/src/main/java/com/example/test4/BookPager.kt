package com.example.test4

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView



class BookPagerAdapter(inBook: Book, inLayoutStateContainer: BookActivity.LayoutStateContainer) : RecyclerView.Adapter<PageViewHolder>() {
    private val book = inBook
    private val layoutStateContainer = inLayoutStateContainer

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        return when(layoutStateContainer.layoutMode) {
            BookActivity.LayoutMode.NORMAL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.book_page_layout_normal, parent, false)
                view.findViewById<TextView>(R.id.caption_view).text = book.chapterTitle
                PageViewHolder(view)
            }
            BookActivity.LayoutMode.SPLIT_HORIZONTAL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.book_page_layout_split_horizontal, parent, false)
                view.findViewWithTag<View>("page_container_0").layoutParams = LinearLayout.LayoutParams(book.pageRects[0].width(),
                    LinearLayout.LayoutParams.MATCH_PARENT)
                view.findViewWithTag<View>("page_container_1").layoutParams = LinearLayout.LayoutParams(book.pageRects[1].width(),
                    LinearLayout.LayoutParams.MATCH_PARENT)

                view.findViewById<TextView>(R.id.caption_view).text = book.chapterTitle
                view.findViewById<TextView>(R.id.caption_view2).text = book.chapterTitle
                SplitPageViewHolder(view)
            }
            BookActivity.LayoutMode.SPLIT_VERTICAL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.book_page_layout_split_vertical, parent, false)
                view.findViewWithTag<View>("page_container_0").layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, book.pageRects[0].height())
                view.findViewWithTag<View>("page_container_1").layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, book.pageRects[1].height())

                view.findViewById<TextView>(R.id.caption_view).text = book.chapterTitle
                SplitPageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        if (layoutStateContainer.layoutMode == BookActivity.LayoutMode.NORMAL) {
            var viewList = ArrayList<TextView>()
            when (position) {
                0 -> {
                    viewList.add(LayoutInflater.from(holder.rootView.context).inflate(R.layout.book_page_text_underflow, null) as TextView)
                }
                itemCount - 1 -> {
                    viewList.add(LayoutInflater.from(holder.rootView.context).inflate(R.layout.book_page_text_overflow, null) as TextView)
                }
                else -> {
                    viewList = book.getPageView(position - 1)
                }
            }
            holder.repopulate(viewList)
        }
        else {
            var viewList = ArrayList<TextView>()
            var viewList2 = ArrayList<TextView>()
            when (position) {
                0 -> {
                    viewList.add(LayoutInflater.from(holder.rootView.context).inflate(R.layout.book_page_text_underflow, null) as TextView)
                    viewList2.add(LayoutInflater.from(holder.rootView.context).inflate(R.layout.book_page_text_underflow, null) as TextView)
                }
                itemCount - 1 -> {
                    viewList.add(LayoutInflater.from(holder.rootView.context).inflate(R.layout.book_page_text_overflow, null) as TextView)
                    viewList2.add(LayoutInflater.from(holder.rootView.context).inflate(R.layout.book_page_text_overflow, null) as TextView)
                }
                else -> {
                    viewList = book.getPageView(2 * (position - 1))
                    viewList2 = book.getPageView((2 * (position - 1)) + 1)
                }
            }
            (holder as SplitPageViewHolder).repopulate(viewList)
            (holder as SplitPageViewHolder).repopulate2(viewList2)
        }
    }

    override fun getItemCount(): Int {
        return if (layoutStateContainer.layoutMode == BookActivity.LayoutMode.NORMAL) {
            book.numPages + 2
        } else {
            (book.numPages / 2) + (book.numPages % 2) + 2
        }

    }
}

open class PageViewHolder(inView: View) : RecyclerView.ViewHolder(inView) {
    val rootView = inView
    private val linearLayout = inView.findViewById<LinearLayout>(R.id.linear_layout)

    fun repopulate(paragraphViews: ArrayList<TextView>) {
        linearLayout.removeAllViews()
        for (paragraphView in paragraphViews) {
            linearLayout.addView(paragraphView)
        }
    }
}

class SplitPageViewHolder(view: View) : PageViewHolder(view) {
    private val linearLayout2 = view.findViewById<LinearLayout>(R.id.linear_layout2)

    fun repopulate2(paragraphViews: ArrayList<TextView>) {
        linearLayout2.removeAllViews()
        for (paragraphView in paragraphViews) {
            linearLayout2.addView(paragraphView)
        }
    }
}