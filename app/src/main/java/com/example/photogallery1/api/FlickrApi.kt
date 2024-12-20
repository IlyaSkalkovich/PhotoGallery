package com.example.photogallery1.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface FlickrApi {
    @GET
    fun fetchUrlBytes(@Url url: String): Call<ResponseBody>
    
    @GET("services/rest?method=flickr.interestingness.getList")
    suspend fun fetchInterestingPhotos(
        @Query("page") whichPage: Int,
        @Query("per_page") photosPerPage: Int,
    ): Response<PhotoResponse>

    @GET("services/rest?method=flickr.photos.search")
    suspend fun searchPhotos(
        @Query("text") query: String,
        @Query("page") whichPage: Int,
        @Query("per_page") photosPerPage: Int,): Response<PhotoResponse>
}