package com.example.gifsearch_test.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.gifsearch_test.R
import com.example.gifsearch_test.data.model.GifData

class GifAdapter(
    private val onClick: (GifData) -> Unit
) : RecyclerView.Adapter<GifAdapter.GifViewHolder>() {

    private val gifList = mutableListOf<GifData>()

    class GifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gifImageView: ImageView = itemView.findViewById(R.id.gifImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GifViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gif, parent, false)
        return GifViewHolder(view)
    }

    override fun onBindViewHolder(holder: GifViewHolder, position: Int) {
        val gif = gifList[position]

        val url = gif.images.fixedWidthSmall.url
            .ifEmpty { gif.images.fixedWidth.url }

        Glide.with(holder.itemView)
            .asGif()
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(android.R.color.darker_gray)
            .into(holder.gifImageView)

        holder.itemView.setOnClickListener { onClick(gif) }
    }

    override fun onViewRecycled(holder: GifViewHolder) {
        Glide.with(holder.itemView).clear(holder.gifImageView)
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int = gifList.size

    fun submitList(newList: List<GifData>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = gifList.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(o: Int, n: Int) = gifList[o].id == newList[n].id
            override fun areContentsTheSame(o: Int, n: Int) = gifList[o] == newList[n]
        })
        gifList.clear()
        gifList.addAll(newList)
        diff.dispatchUpdatesTo(this)
    }
}