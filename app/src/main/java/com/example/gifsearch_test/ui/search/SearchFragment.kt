package com.example.gifsearch_test.ui.search

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.gifsearch_test.R
import com.example.gifsearch_test.data.repository.GifRepository
import com.example.gifsearch_test.ui.adapter.GifAdapter
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: SearchViewModel by viewModels {
        viewModelFactory {
            initializer {
                SearchViewModel(
                    application = requireActivity().application,
                    repository = GifRepository()
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val errorView = view.findViewById<TextView>(R.id.errorView)

        val adapter = GifAdapter { gif ->
            findNavController().navigate(
                R.id.action_search_to_detail,
                bundleOf(
                    "gifUrl" to gif.images.original.url,
                    "gifTitle" to gif.title
                )
            )
        }

        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
            gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = null
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onQueryChanged(newText.orEmpty())
                return true
            }
        })

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val total = layoutManager.itemCount
                val lastPositions = layoutManager.findLastVisibleItemPositions(null)
                val lastVisible = lastPositions.maxOrNull() ?: 0
                if (total > 0 && lastVisible >= total - 5) {
                    viewModel.loadNextPage()
                }
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            progressBar.isVisible = true
                            errorView.isVisible = false
                        }
                        is UiState.Success -> {
                            progressBar.isVisible = false
                            errorView.isVisible = false
                            adapter.submitList(state.gifs)
                        }
                        is UiState.Error -> {
                            progressBar.isVisible = false
                            errorView.isVisible = true
                            errorView.text = state.message
                        }
                    }
                }
            }
        }
    }
}