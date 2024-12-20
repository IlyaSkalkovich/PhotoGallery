package com.example.photogallery1

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.TrafficStats
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0

class ThumbnailDownloader<in T>(
    private val responseHandler: Handler,
    private val onThumbnailDownloaded: (T, Bitmap) -> Unit
) : HandlerThread(TAG), DefaultLifecycleObserver {
    private var hasQuit = false
    private val requestMap = ConcurrentHashMap<T, String>()
    private val galleryItemsRepository = GalleryItemsRepository()
    private val flickrFetchr = galleryItemsRepository.provideFlickrFetchr()
    private lateinit var requestHandler: Handler

    val fragmentLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)

            TrafficStats.setThreadStatsTag(Thread.currentThread().id.toInt())

            Log.i(TAG, "Starting background thread")
            start()
            looper
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)

            Log.i(TAG, "Destroying background thread")
            quit()
        }
    }

    val viewLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)

            Log.i(TAG, "Clearing all requests from queue")
            requestHandler.removeMessages(MESSAGE_DOWNLOAD)
            requestMap.clear()
        }
    }

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        requestHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val target = msg.obj as T
                    //Log.i(TAG, "Got a request for URL: ${requestMap[target]}")
                    handleRequest(target)
                }
            }
        }
    }

    private fun handleRequest(target: T) {
        val url = requestMap[target] ?: return
        val bitmap = flickrFetchr.fetchPhoto(url) ?: return

        responseHandler.post(Runnable {
            if (requestMap[target] != url || hasQuit)
                return@Runnable

            requestMap.remove(target)
            onThumbnailDownloaded(target, bitmap)
        })
    }

    fun queueThumbnail(target: T, url: String) {
        //Log.i(TAG, "Got a URL: $url")

        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
    }
}