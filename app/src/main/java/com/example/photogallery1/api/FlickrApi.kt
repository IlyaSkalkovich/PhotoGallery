package com.example.photogallery1.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface FlickrApi {
    @GET("services/rest/?method=flickr.interestingness.getList" +
            "&api_key=601437fa10cca35e1feacf5295e5b8b9" +
            "&format=json" +
            "&nojsoncallback=1" +
            "&extras=url_s"
    )
    suspend fun fetchPhotos(
        @Query("page") whichPage: Int,
        @Query("per_page") photosPerPage: Int,
    ): Response<PhotoResponse>
}
