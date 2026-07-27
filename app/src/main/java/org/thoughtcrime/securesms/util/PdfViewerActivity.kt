package org.thoughtcrime.securesms.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class PdfViewerActivity : AppCompatActivity() {
    private var renderer: PdfRenderer? = null
    private lateinit var pageView: ImageView
    private lateinit var toolbar: Toolbar
    private lateinit var scrollView: ScrollView
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uriString = intent.getStringExtra(EXTRA_URI) ?: run { finish(); return }
        val uri = Uri.parse(uriString)

        val pfd = try {
            contentResolver.openFileDescriptor(uri, "r") ?: run { finish(); return }
        } catch (e: Exception) {
            finish()
            return
        }

        val r = try {
            PdfRenderer(pfd)
        } catch (e: Exception) {
            finish()
            return
        }
        renderer = r

        currentPage = savedInstanceState?.getInt(KEY_PAGE, 0) ?: 0

        toolbar = Toolbar(this).apply {
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            setNavigationOnClickListener { finish() }
            setBackgroundColor(Color.BLACK)
            setTitleTextColor(Color.WHITE)
        }

        pageView = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.BLACK)
        }

        scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.BLACK)
            addView(pageView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            addView(toolbar, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            addView(scrollView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        }

        if (r.pageCount > 1) {
            val prevButton = TextView(this).apply {
                text = "← Prev"
                setTextColor(Color.LTGRAY)
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(0, 32, 0, 32)
                setOnClickListener { showPage(currentPage - 1) }
            }
            val nextButton = TextView(this).apply {
                text = "Next →"
                setTextColor(Color.LTGRAY)
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(0, 32, 0, 32)
                setOnClickListener { showPage(currentPage + 1) }
            }
            val navRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(Color.BLACK)
                addView(prevButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                addView(nextButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            }
            layout.addView(navRow, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        setContentView(layout)
        showPage(currentPage)
    }

    private fun showPage(index: Int) {
        val r = renderer ?: return
        if (index < 0 || index >= r.pageCount) return
        currentPage = index

        val page = r.openPage(index)
        val width = resources.displayMetrics.widthPixels
        val height = (page.height.toFloat() / page.width * width).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()

        pageView.setImageBitmap(bitmap)
        scrollView.scrollTo(0, 0)

        toolbar.title = if (r.pageCount > 1) {
            "Page ${index + 1} of ${r.pageCount}"
        } else {
            intent.getStringExtra(EXTRA_NAME) ?: "Document"
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_PAGE, currentPage)
    }

    override fun onDestroy() {
        super.onDestroy()
        renderer?.close()
    }

    companion object {
        private const val EXTRA_URI = "uri"
        private const val EXTRA_NAME = "name"
        private const val KEY_PAGE = "page"

        @JvmStatic
        fun getIntent(context: Context, uri: Uri, name: String?): Intent =
            Intent(context, PdfViewerActivity::class.java)
                .putExtra(EXTRA_URI, uri.toString())
                .putExtra(EXTRA_NAME, name)
    }
}
