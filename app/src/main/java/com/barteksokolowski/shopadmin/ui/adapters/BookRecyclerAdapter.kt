package com.barteksokolowski.shopadmin.ui.adapters

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.barteksokolowski.shopadmin.ui.activities.EditBookActivity
import com.barteksokolowski.shopadmin.R
import com.barteksokolowski.shopadmin.model.Book
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.book_list_item.view.*

class BookRecyclerAdapter(context: Context?) : RecyclerView.Adapter<BookRecyclerAdapter.BookRecyclerAdapterViewHolder>() {

    private val mContext = context

    private var mCursor: Cursor? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookRecyclerAdapterViewHolder =
            BookRecyclerAdapterViewHolder(LayoutInflater.from(mContext).inflate(R.layout.book_list_item, parent, false))

    override fun getItemCount(): Int {
        if (mCursor == null) return 0
        return mCursor!!.count
    }

    override fun onBindViewHolder(holder: BookRecyclerAdapterViewHolder, position: Int) {
        mCursor!!.moveToPosition(position)
        holder.itemView.tag = mCursor!!.getInt(0)
        holder.bindBook(Book.fromCursor(mCursor!!))
    }

    fun swapCursor(newCursor: Cursor?) {
        mCursor = newCursor
        notifyDataSetChanged()
    }


    inner class BookRecyclerAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @SuppressLint("SetTextI18n")
        fun bindBook(book: Book) {
            Picasso.with(itemView.context)
                    .load(book.photoURL)
                    .into(itemView.itemBookCoverImage)

            itemView.itemBookTitle.text = book.title
            itemView.itemBookAuthors.text = book.authors
            itemView.itemBookPrice.text = "${book.price}PLN"

            itemView.setOnClickListener {
                val CONTENT_URI = Uri.parse("content://com.barteksokolowski.shop/books")

                val intent = Intent(itemView.context, EditBookActivity::class.java)
                intent.data = ContentUris.withAppendedId(CONTENT_URI, book._ID)
                itemView.context.startActivity(intent)
            }
        }
    }
}
