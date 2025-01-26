package com.example.photogallery1

import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "PhotoGalleryFragment"
private const val DEFAULT_RECYCLER_VIEW_COLUMN_WIDTH = 300

class PhotoGalleryFragment : Fragment(), MenuProvider {
    private lateinit var photoRecyclerView: RecyclerView
    private val photoGalleryViewModel: PhotoGalleryViewModel by viewModels()
    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        StrictMode.enableDefaults()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)
        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        progressBar = view.findViewById(R.id.progressBar)

        photoRecyclerView.doOnPreDraw {
            val recyclerViewWidth = photoRecyclerView.width
            val numberOfColumns = recyclerViewWidth / DEFAULT_RECYCLER_VIEW_COLUMN_WIDTH

            photoRecyclerView.layoutManager = GridLayoutManager(context, numberOfColumns)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoAdapter = PhotoAdapter()
        photoRecyclerView.adapter = photoAdapter

        photoAdapter.addLoadStateListener { state ->
            progressBar.isVisible = state.refresh is LoadState.Loading
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(lifecycle.currentState) {
                photoGalleryViewModel.galleryItems.collectLatest { galleryItems ->
                    photoAdapter.submitData(galleryItems)
                }
            }
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(
            this,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_photo_gallery, menu)

        val menuItem = menu.findItem(R.id.menu_item_search)
        val searchView = menuItem.actionView as SearchView

        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(queryText: String): Boolean {
                    Log.d(TAG, "QueryTextSubmit: $queryText")

                    photoGalleryViewModel.setSearchTerm(queryText)
                    photoAdapter.refresh()

                    return true
                }

                override fun onQueryTextChange(queryText: String): Boolean {
                    //Log.d(TAG, "QueryTextChange: $queryText")
                    return false
                }
            })
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        photoGalleryViewModel.setSearchTerm("")
        photoAdapter.refresh()

        return true
    }

    private object ArticleComparator : DiffUtil.ItemCallback<GalleryItem>(){
        override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
            return oldItem == newItem
        }
    }

    private inner class PhotoAdapter :
        PagingDataAdapter<GalleryItem, PhotoHolder>(ArticleComparator) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val view = layoutInflater.inflate(
                R.layout.list_item_gallery,
                parent,
                false
            ) as ImageView

            return PhotoHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            getItem(position)?.let { holder.bind(it.url) }
        }
    }

    private inner class PhotoHolder(private val itemImageView: ImageView) : ViewHolder(itemImageView) {
        fun bind(url: String) {
            Glide.with(this@PhotoGalleryFragment)
                .load(url)
                .placeholder(R.drawable.placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(itemImageView)
        }
    }

    companion object {
        fun newInstance() = PhotoGalleryFragment()
    }
}