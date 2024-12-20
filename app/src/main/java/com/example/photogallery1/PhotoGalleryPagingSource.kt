package com.example.photogallery1

import androidx.paging.PagingSource
import androidx.paging.PagingState

private const val STARTING_KEY = 1
private const val LAST_KEY = 5

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
        val startKey = params.key ?: STARTING_KEY

        val data: List<GalleryItem> = fetchGalleryItems(whichPage, photosPerPage, query)

        return LoadResult.Page(
            data = data,

            prevKey = if (startKey == STARTING_KEY) null
            else startKey - 1,

            nextKey = if (startKey == LAST_KEY) null
            else startKey + 1
        )
    }
}