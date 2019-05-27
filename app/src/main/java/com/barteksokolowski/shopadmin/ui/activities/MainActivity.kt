package com.barteksokolowski.shopadmin.ui.activities

import android.app.LoaderManager
import android.content.CursorLoader
import android.content.Intent
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.Toast

import com.barteksokolowski.shopadmin.R
import com.barteksokolowski.shopadmin.ui.adapters.BookRecyclerAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    private lateinit var mAdapter: BookRecyclerAdapter
    private val LOG_TAG = MainActivity::class.java.simpleName

    private var BOOK_LOADER_ID: Int = 2137

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerview.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
        recyclerview.setHasFixedSize(true)

        mAdapter = BookRecyclerAdapter(context = this@MainActivity)
        recyclerview.adapter = mAdapter

        loaderManager.initLoader(BOOK_LOADER_ID, null, this@MainActivity)

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val id = viewHolder.itemView.tag as Int

                val stringId = Integer.toString(id)
                var uri = Uri.parse("content://com.barteksokolowski.shop/books")
                uri = uri.buildUpon().appendPath(stringId).build()

                contentResolver.delete(uri, null, null)

                Toast.makeText(this@MainActivity, getString(R.string.delete_successful), Toast.LENGTH_SHORT).show()

                val CART_CONTENT_URI = Uri.parse("content://com.barteksokolowski.shop/cart")
                val selection = "bookID = $id"
                contentResolver.delete(CART_CONTENT_URI, selection, null)

                loaderManager.restartLoader(BOOK_LOADER_ID, null, this@MainActivity)
            }
        }).attachToRecyclerView(recyclerview)

        addBookBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, EditBookActivity::class.java))
        }

        showLoading()
    }

    private fun showLoading() {
        loadingIndicator.visibility = View.VISIBLE
        recyclerview.visibility = View.INVISIBLE
        noBooksLayout.visibility = View.INVISIBLE
    }

    private fun showDataView() {
        loadingIndicator.visibility = View.INVISIBLE
        recyclerview.visibility = View.VISIBLE
        noBooksLayout.visibility = View.INVISIBLE
    }

    private fun showEmptyLayout() {
        loadingIndicator.visibility = View.INVISIBLE
        recyclerview.visibility = View.INVISIBLE
        noBooksLayout.visibility = View.VISIBLE
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val CONTENT_URI = Uri.parse("content://com.barteksokolowski.shop/books")
        return CursorLoader(this@MainActivity, CONTENT_URI, null, null, null, null)
    }

    override fun onLoadFinished(loader: android.content.Loader<Cursor>?, data: Cursor?) {
        mAdapter.swapCursor(data)
        if (data!!.count != 0)
            showDataView()
        else
            showEmptyLayout()
    }

    override fun onLoaderReset(loader: android.content.Loader<Cursor>?) {
        mAdapter.swapCursor(null)
    }
}
