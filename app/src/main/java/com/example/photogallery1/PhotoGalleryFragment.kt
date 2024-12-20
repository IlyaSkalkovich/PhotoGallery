package com.example.photogallery1

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.provider.ContactsContract.Contacts.Photo
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuPresenter
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.contains
import androidx.core.view.doOnPreDraw
import androidx.core.view.size
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.internal.ViewUtils.addOnGlobalLayoutListener
import com.google.android.material.internal.ViewUtils.removeOnGlobalLayoutListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "PhotoGalleryFragment"
private const val DEFAULT_RECYCLER_VIEW_COLUMN_WIDTH = 300

class PhotoGalleryFragment : Fragment(), MenuProvider {
    private lateinit var photoRecyclerView: RecyclerView
    private val photoGalleryViewModel: PhotoGalleryViewModel by viewModels()
    private lateinit var photoAdapter: PhotoAdapter

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

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(
            this,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(lifecycle.currentState) {
                photoGalleryViewModel.galleryItems.collectLatest { galleryItems ->
                    photoAdapter.submitData(galleryItems)
                }
            }
        }
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