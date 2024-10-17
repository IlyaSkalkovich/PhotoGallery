package com.example.photogallery1.api

import com.example.photogallery1.GalleryItem
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class PhotoDeserializer : JsonDeserializer<PhotoResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PhotoResponse {
        val jsonObj = json.asJsonObject
        val jsonPhotos = jsonObj.get("photos").asJsonObject
        val jsonPhotoArray = jsonPhotos.get("photo").asJsonArray

        val galleryItems = mutableListOf<GalleryItem>()

        for (i in 0 until jsonPhotoArray.size()) {
            val jsonGalleryItem = jsonPhotoArray.get(i).asJsonObject

            val galleryItem = GalleryItem(
                jsonGalleryItem.get("title").asString,
                jsonGalleryItem.get("id").asString,
                jsonGalleryItem.get("url_s").asString
            )

            galleryItems.add(galleryItem)
        }

        return PhotoResponse().apply { this.galleryItems = galleryItems }
    }
}
