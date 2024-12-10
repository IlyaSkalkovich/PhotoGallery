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
    @GET("services/rest?method=flickr.interestingness.getList")
    suspend fun fetchPhotos(
        @Query("page") whichPage: Int,
        @Query("per_page") photosPerPage: Int,
    ): Response<PhotoResponse>

    @GET
    fun fetchUrlBytes(@Url url: String): Call<ResponseBody>
}