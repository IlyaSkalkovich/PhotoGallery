package com.example.photogallery1

class GalleryItemsRepository {
    fun provideFlickrFetchr() = FlickrFetchr()

    fun providePagingSource(
        fetchGalleryItems: suspend (Int, Int, String) -> List<GalleryItem>,
        whichPage: Int = 1,
        photosPerPage: Int = 100,
        query: String = ""
    ) = PhotoGalleryPagingSource(fetchGalleryItems, whichPage, photosPerPage, query)
}