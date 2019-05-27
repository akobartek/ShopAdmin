package com.barteksokolowski.shopadmin.ui.activities

import android.app.AlertDialog
import android.app.LoaderManager
import android.content.ContentValues
import android.content.CursorLoader
import android.content.DialogInterface
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast

import com.barteksokolowski.shopadmin.R
import com.barteksokolowski.shopadmin.model.Book
import kotlinx.android.synthetic.main.activity_edit_book.*

class EditBookActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    private var mCurrentBookUri: Uri? = null

    private var mPersonHasChanged = false

    private val mTouchListener = View.OnTouchListener { _, _ ->
        mPersonHasChanged = true
        false
    }

    private val EDITOR_LOADER_ID = 2138

    private lateinit var spinnerAdapter: ArrayAdapter<CharSequence>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_book)

        spinnerAdapter = ArrayAdapter.createFromResource(
                this@EditBookActivity, R.array.categories, R.layout.support_simple_spinner_dropdown_item
        )
        categorySpinner.adapter = spinnerAdapter

        mCurrentBookUri = intent.data
        if (mCurrentBookUri == null) {
            title = getString(R.string.add_book)
            invalidateOptionsMenu()
        } else {
            title = getString(R.string.edit_book)
            loaderManager.initLoader(EDITOR_LOADER_ID, null, this@EditBookActivity)
        }

        titleET.setOnTouchListener(mTouchListener)
        authorsET.setOnTouchListener(mTouchListener)
        priceET.setOnTouchListener(mTouchListener)
        categorySpinner.setOnTouchListener(mTouchListener)
        imageUrlET.setOnTouchListener(mTouchListener)
        noteET.setOnTouchListener(mTouchListener)

        saveBookBtn.setOnClickListener {
            if (saveProduct())
                finish()
        }
    }

    override fun onBackPressed() {
        if (!mPersonHasChanged) {
            super.onBackPressed()
            return
        }
        showUnsavedChangesDialog(DialogInterface.OnClickListener { _, _ -> finish() })
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        if (mCurrentBookUri == null) {
            menu.findItem(R.id.action_delete).isVisible = false
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.editor, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                showDeleteConfirmationDialog()
                true
            }
            android.R.id.home -> {
                if (!mPersonHasChanged) {
                    NavUtils.navigateUpFromSameTask(this@EditBookActivity)
                } else {
                    showUnsavedChangesDialog(DialogInterface.OnClickListener { _, _ ->
                        NavUtils.navigateUpFromSameTask(this@EditBookActivity)
                    })
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showUnsavedChangesDialog(listener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
                .setMessage(R.string.unsaved_changes_dialog_msg)
                .setCancelable(false)
                .setPositiveButton(R.string.discard, listener)
                .setNegativeButton(R.string.keep_editing, { dialog, _ -> dialog?.dismiss() })
                .create()
                .show()
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
                .setMessage(R.string.delete_dialog_msg)
                .setPositiveButton(R.string.delete, { _, _ -> deleteBook() })
                .setNegativeButton(R.string.cancel, { dialog, _ -> dialog?.dismiss() })
                .create()
                .show()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor>? {
        if (mCurrentBookUri == null) {
            return null
        }
        return CursorLoader(this@EditBookActivity, mCurrentBookUri, null, null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {
        if (data == null || data.count < 1) {
            return
        }

        if (data.moveToFirst()) {
            val book = Book.fromCursor(data)
            val emptyPhotoURL = "http://dobreksiazki.pl/Photos/nophoto.jpg"

            titleET.setText(book.title)
            authorsET.setText(book.authors)
            priceET.setText(book.price.toString())
            categorySpinner.setSelection(book.category)
            if (book.photoURL == emptyPhotoURL)
                imageUrlET.setText("")
            else
                imageUrlET.setText(book.photoURL)
            noteET.setText(book.note)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {
        titleET.setText("")
        authorsET.setText("")
        priceET.setText("")
        categorySpinner.setSelection(0)
        imageUrlET.setText("")
        noteET.setText("")
    }

    private fun saveProduct(): Boolean {
        if (!mPersonHasChanged) {
            return true
        }

        val title = titleET.text.toString().trim()
        val authors = authorsET.text.toString().trim()
        val category = categorySpinner.selectedItemPosition
        var imageURL = imageUrlET.text.toString().trim()
        val note = noteET.text.toString().trim()
        val price = priceET.text.toString().trim()

        if (mCurrentBookUri == null && TextUtils.isEmpty(title) && TextUtils.isEmpty(authors)) {
            return true
        }
        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(authors)) {
            titleET.error = getString(R.string.error_empty_edittext)
            authorsET.error = getString(R.string.error_empty_edittext)
        }
        if (TextUtils.isEmpty(title)) {
            titleET.error = getString(R.string.error_empty_edittext)
            return false
        }
        if (TextUtils.isEmpty(authors)) {
            authorsET.error = getString(R.string.error_empty_edittext)
            return false
        }
        if (TextUtils.isEmpty(price)) {
            priceET.error = getString(R.string.error_empty_edittext)
            return false
        }

        val values = ContentValues()
        values.put("title", title)
        values.put("author", authors)
        values.put("category", category)
        values.put("note", note)
        values.put("price", price)

        if (TextUtils.isEmpty(imageURL))
            imageURL = "http://dobreksiazki.pl/Photos/nophoto.jpg"
        values.put("photoURL", imageURL)


        if (mCurrentBookUri == null) {
            val CONTENT_URI = Uri.parse("content://com.barteksokolowski.shop/books")
            val newUri = contentResolver.insert(CONTENT_URI, values)
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.save_error), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show()
            }
            return true
        } else {
            Log.d("EDITORTAG", mCurrentBookUri.toString())
            Log.d("EDITORTAG", values.toString())
            val rowsAffected = contentResolver.update(mCurrentBookUri, values, null, null)

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.update_error), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.updated), Toast.LENGTH_SHORT).show()
            }
            return true
        }
    }

    private fun deleteBook() {
        if (mCurrentBookUri != null) {
            val rowsDeleted = contentResolver.delete(mCurrentBookUri, null, null)

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.delete_failed), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.delete_successful), Toast.LENGTH_SHORT).show()
            }

            val id = mCurrentBookUri!!.lastPathSegment.toInt()
            val CART_CONTENT_URI = Uri.parse("content://com.barteksokolowski.shop/cart")
            val selection = "bookID = $id"
            contentResolver.delete(CART_CONTENT_URI, selection, null)
        }
        finish()
    }
}
