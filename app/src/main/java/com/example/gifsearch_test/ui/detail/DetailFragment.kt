package com.example.gifsearch_test.ui.detail

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.gifsearch_test.R

class DetailFragment : Fragment(R.layout.fragment_detail) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = arguments?.getString("gifUrl").orEmpty()
        val title = arguments?.getString("gifTitle").orEmpty()
        view.findViewById<TextView>(R.id.titleView).text = title
        Glide.with(view)
            .asGif()
            .load(url)
            .into(view.findViewById<ImageView>(R.id.detailImage))
    }
}