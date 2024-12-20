package com.example.photogallery1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class PhotoGalleryViewModel : ViewModel() {
    private val galleryItemsRepository = GalleryItemsRepository()
    private val flickrFetchr = galleryItemsRepository.provideFlickrFetchr()

    val galleryItems: Flow<PagingData<GalleryItem>> = Pager(
        config = PagingConfig(pageSize = 100, enablePlaceholders = false),
        pagingSourceFactory = { galleryItemsRepository.providePagingSource(
            flickrFetchr::fetchGalleryItems,
            query = mutableSearchTerm.value,
            whichPage = 5
        )}
    ).flow.cachedIn(viewModelScope)

    private val mutableSearchTerm = MutableStateFlow("")

    fun setSearchTerm(query: String) {
        mutableSearchTerm.value = query
    }
}