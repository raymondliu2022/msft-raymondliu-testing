package com.example.test4

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.util.Consumer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.window.DisplayFeature
import androidx.window.FoldingFeature
import androidx.window.WindowLayoutInfo
import androidx.window.WindowManager
import java.util.concurrent.Executor

class BookActivity : AppCompatActivity(), ViewTreeObserver.OnGlobalLayoutListener {
    private lateinit var book: Book

    private lateinit var viewPagerView: ViewPager2
    private lateinit var windowManager: WindowManager
    private var pagePagerCallback = PagePagerCallback()
    private val handler = Handler(Looper.getMainLooper())
    private val mainThreadExecutor = Executor { r: Runnable -> handler.post(r) }
    private val layoutStateContainer = LayoutStateContainer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        renderLoading()

        windowManager = WindowManager(this)

        book = Book(intent.getStringExtra("BOOK_FILEPATH")!!, assets, this)
        book.currentChapter = 2
    }

    override fun onStart() {
        super.onStart()
        windowManager.registerLayoutChangeCallback(mainThreadExecutor, layoutStateContainer)
    }

    override fun onStop() {
        super.onStop()
        windowManager.unregisterLayoutChangeCallback(layoutStateContainer)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.d("RL2022", "onConfigurationChanged")
        super.onConfigurationChanged(newConfig)

        renderLoading()
    }

    private fun renderLoading() {
        Log.d("RL2022", "renderLoading")
        setContentView(R.layout.activity_book)
        findViewById<View>(R.id.loading_text_view).viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        Log.d("RL2022", "onGlobalLayout")
        findViewById<View>(R.id.loading_text_view).viewTreeObserver.removeOnGlobalLayoutListener(this)

        val loadingTextView = findViewById<View>(R.id.loading_text_view)
        book.paragraphWidth = if (layoutStateContainer.layoutMode != LayoutMode.SPLIT_HORIZONTAL) loadingTextView.width else (((loadingTextView.width - layoutStateContainer.dividerWidth) / 2) - resources.getDimension(R.dimen.page_margins).toInt())
        book.screenHeight = if (layoutStateContainer.layoutMode != LayoutMode.SPLIT_VERTICAL) loadingTextView.height else (((loadingTextView.height - layoutStateContainer.dividerWidth) / 2) - resources.getDimension(R.dimen.page_margins).toInt())
        book.buildPages()

        renderBook()
    }

    private fun renderBook() {
        Log.d("RL2022", "renderBook")
        val viewPagerStub = findViewById<ViewStub>(R.id.view_pager_stub)
        if (viewPagerStub != null) {
            viewPagerView = viewPagerStub.inflate() as ViewPager2
        } else {
            viewPagerView = findViewById(R.id.view_pager_view)
            viewPagerView.unregisterOnPageChangeCallback(pagePagerCallback)
        }

        viewPagerView.adapter = PagePagerAdapter(book, layoutStateContainer)
        val position = if (layoutStateContainer.layoutMode == LayoutMode.NORMAL) book.currentPage + 1 else (book.currentPage/2) + 1
        viewPagerView.setCurrentItem(position, false)
        viewPagerView.registerOnPageChangeCallback(pagePagerCallback)
    }

    private inner class PagePagerCallback() : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            Log.d("RL2022", "onPageSelected $position")
            when (position) {
                0 -> {
                    if (book.currentChapter > 0) {
                        book.currentChapter = book.currentChapter - 1
                        book.buildPages()
                        book.currentPage = book.pageViews.size - 1
                        handler.postDelayed({
                            renderBook()
                        }, 500)
                    }
                }
                viewPagerView.adapter!!.itemCount - 1 -> {
                    if (book.currentChapter < book.chapterStarts.size - 1) {
                        book.currentChapter = book.currentChapter + 1
                        book.buildPages()
                        book.currentPage = 0
                        handler.postDelayed({
                            renderBook()
                        }, 500)
                    }
                }
                else -> {
                    book.currentPage = if (layoutStateContainer.layoutMode == LayoutMode.NORMAL) {
                        position - 1
                    } else {
                        (2 * (position - 1))
                    }
                }
            }
        }
    }

    private inner class PagePagerAdapter(inBook: Book, inLayoutStateContainer: LayoutStateContainer) : RecyclerView.Adapter<PageViewHolder>() {
        private val book = inBook
        private val layoutStateContainer = inLayoutStateContainer

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
            return when(layoutStateContainer.layoutMode) {
                LayoutMode.NORMAL -> {
                    PageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.page_layout, parent, false))
                }
                LayoutMode.SPLIT_HORIZONTAL -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.split_horizontal_page_layout, parent, false)
                    view.findViewById<View>(R.id.page_divider).layoutParams = LinearLayout.LayoutParams(layoutStateContainer.dividerWidth,LinearLayout.LayoutParams.WRAP_CONTENT)
                    SplitPageViewHolder(view)
                }
                LayoutMode.SPLIT_VERTICAL -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.split_vertical_page_layout, parent, false)
                    view.findViewById<View>(R.id.page_divider).layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,layoutStateContainer.dividerWidth)
                    SplitPageViewHolder(view)
                }
            }
        }

        override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
            if (layoutStateContainer.layoutMode == LayoutMode.SPLIT_HORIZONTAL) {
                (holder as SplitPageViewHolder).caption(book.paragraphStrings[0])
                (holder as SplitPageViewHolder).caption2(book.paragraphStrings[0])
            }
            else {
                holder.caption(book.paragraphStrings[0])
            }

            if (layoutStateContainer.layoutMode == LayoutMode.NORMAL) {
                var viewList = ArrayList<TextView>()
                if (position == 0) {
                    viewList.add(book.underflowTextView)
                }
                else if (position == itemCount - 1){
                    viewList.add(book.overflowTextView)
                } else {
                    viewList = book.getPageView(position - 1)
                }
                holder.repopulate(viewList)
            }
            else {
                var viewList = ArrayList<TextView>()
                var viewList2 = ArrayList<TextView>()
                if (position == 0) {
                    viewList.add(book.underflowTextView)
                    viewList2.add(book.underflowTextView2)
                }
                else if (position == itemCount - 1){
                    viewList.add(book.overflowTextView)
                    viewList2.add(book.overflowTextView2)

                } else {
                    viewList = book.getPageView(2 * (position - 1))
                    viewList2 = book.getPageView((2 * (position - 1)) + 1)
                }
                (holder as SplitPageViewHolder).repopulate(viewList)
                (holder as SplitPageViewHolder).repopulate2(viewList2)
            }
        }

        override fun getItemCount(): Int {
            return if (layoutStateContainer.layoutMode == LayoutMode.NORMAL) {
                book.pageViews.size + 2
            } else {
                (book.pageViews.size / 2) + (book.pageViews.size % 2) + 2
            }

        }
    }

    private open inner class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val linearLayout = view.findViewById<LinearLayout>(R.id.linear_layout)
        private val captionView = view.findViewById<TextView>(R.id.caption_view)

        fun repopulate(paragraphViews: ArrayList<TextView>) {
            linearLayout.removeAllViews()
            for (paragraphView in paragraphViews) {
                linearLayout.addView(paragraphView)
            }
        }

        fun caption(chapter: String) {
            captionView.text = chapter
        }
    }

    private inner class SplitPageViewHolder(view: View) : PageViewHolder(view) {
        private val linearLayout2 = view.findViewById<LinearLayout>(R.id.linear_layout2)
        private val captionView2 = view.findViewById<TextView>(R.id.caption_view2)

        fun repopulate2(paragraphViews: ArrayList<TextView>) {
            linearLayout2.removeAllViews()
            for (paragraphView in paragraphViews) {
                linearLayout2.addView(paragraphView)
            }
        }

        fun caption2(chapter: String) {
            captionView2.text = chapter
        }
    }

    enum class LayoutMode {
        NORMAL, SPLIT_VERTICAL, SPLIT_HORIZONTAL
    }

    inner class LayoutStateContainer : Consumer<WindowLayoutInfo> {
        var layoutMode = LayoutMode.NORMAL
        var dividerWidth = 0

        override fun accept(newLayoutInfo: WindowLayoutInfo) {
            Log.d("RL2022", "newLayoutInfo")
            layoutMode = LayoutMode.NORMAL
            for (displayFeature : DisplayFeature in newLayoutInfo.displayFeatures) {
                if (displayFeature is FoldingFeature) {
                    if (displayFeature.orientation == FoldingFeature.ORIENTATION_HORIZONTAL) {
                        layoutMode = LayoutMode.SPLIT_VERTICAL
                        dividerWidth = displayFeature.bounds.height()
                    } else {
                        layoutMode = LayoutMode.SPLIT_HORIZONTAL
                        dividerWidth = displayFeature.bounds.width()
                    }
                }
            }
            renderLoading()
        }
    }

}