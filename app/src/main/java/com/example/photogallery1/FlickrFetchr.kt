package com.example.photogallery1

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import com.example.photogallery1.api.FlickrApi
import com.example.photogallery1.api.PhotoDeserializer
import com.example.photogallery1.api.PhotoResponse
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "FlickrFetchr"

class FlickrFetchr {
    private val flickrApi: FlickrApi

    init {
        val client = OkHttpClient.Builder()
            .addInterceptor(PhotoInterceptor())
            .build()

        val gsonBuilder = GsonBuilder().registerTypeAdapter(
            PhotoResponse::class.javaObjectType,
            PhotoDeserializer()
        ).create()

        val retrofit: Retrofit = Retrofit
            .Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create(gsonBuilder))
            .client(client)
            .build()

        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    suspend fun fetchGalleryItems(
        whichPage: Int = 1,
        photosPerPage: Int = 100,
        query: String = ""
    ): List<GalleryItem> {
        return if (query.isBlank())
            fetchPhotoMetadata(flickrApi.fetchInterestingPhotos(whichPage, photosPerPage))

        else fetchPhotoMetadata(flickrApi.searchPhotos(query, whichPage, photosPerPage))
    }

    private fun fetchPhotoMetadata(flickrResponse: Response<PhotoResponse>): List<GalleryItem> {
        var galleryItems = emptyList<GalleryItem>()

        if (flickrResponse.isSuccessful) {
            Log.d(TAG, "Response received")

            val photoResponse: PhotoResponse? = flickrResponse.body()
            galleryItems = photoResponse?.galleryItems ?: mutableListOf()

            galleryItems = galleryItems.filterNot { it.url.isBlank() }
            Log.d(TAG, "Items: $galleryItems")

        } else Log.e(TAG, "Failed to fetch photos")

        return galleryItems
    }

    @WorkerThread
    fun fetchPhoto(url: String): Bitmap? {
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
        //Log.i(TAG, "Decoded bitmap=$bitmap from Response=$response")

        return bitmap
    }
}