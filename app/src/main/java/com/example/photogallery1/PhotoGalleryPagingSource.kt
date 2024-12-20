package com.example.photogallery1

import android.widget.Gallery
import androidx.paging.PagingSource
import androidx.paging.PagingState

class PhotoGalleryPagingSource(
    private val fetchGalleryItems: suspend (Int, Int, String) -> List<GalleryItem>,
    private val whichPage: Int,
    private val photosPerPage: Int,
    private val query: String,
): PagingSource<Int, GalleryItem>() {
    override fun getRefreshKey(state: PagingState<Int, GalleryItem>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null

        return anchorPage.prevKey?.plus(1) ?: anchorPage.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GalleryItem> {
        val data: List<GalleryItem> = fetchGalleryItems(whichPage, photosPerPage, query)

        return LoadResult.Page(
            data = data,

            prevKey = null,

            nextKey = null
        )
    }
}