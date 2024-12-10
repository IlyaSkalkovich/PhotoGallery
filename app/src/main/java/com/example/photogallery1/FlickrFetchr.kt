package com.example.photogallery1

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.decodeStream
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.photogallery1.api.FlickrApi
import com.example.photogallery1.api.PhotoDeserializer
import com.example.photogallery1.api.PhotoResponse
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import kotlin.math.max

private const val TAG = "FlickrFetchr"
private const val STARTING_KEY = 1
private const val LAST_KEY = 5

class FlickrFetchr: PagingSource<Int, GalleryItem>() {
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

    private suspend fun fetchPhotos(whichPage: Int = 1, photosPerPage: Int = 100): List<GalleryItem> {
        val flickrResponse: Response<PhotoResponse> = flickrApi.fetchPhotos(whichPage, photosPerPage)
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
        Log.i(TAG, "Decoded bitmap=$bitmap from Response=$response")

        return bitmap
    }

    override fun getRefreshKey(state: PagingState<Int, GalleryItem>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null

        return anchorPage.prevKey?.plus(1) ?: anchorPage.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GalleryItem> {
        val startKey = params.key ?: STARTING_KEY
        val data: List<GalleryItem> = fetchPhotos(whichPage = startKey)

        return LoadResult.Page(
            data = data,

            prevKey = if (startKey == STARTING_KEY) null
            else startKey - 1,

            nextKey = if (startKey == LAST_KEY) null
            else startKey + 1
        )
    }
}