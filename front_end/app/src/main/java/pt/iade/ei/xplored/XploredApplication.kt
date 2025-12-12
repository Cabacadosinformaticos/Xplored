package pt.iade.ei.xplored

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.OkHttpClient

class XploredApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        // FIX: Initialize this immediately before doing anything else
        instance = this
    }

    // App wide Coil loader. Wikimedia Commons (upload.wikimedia.org) rejects
    // requests without a descriptive User-Agent, so send one on every image call.
    override fun newImageLoader(): ImageLoader {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .build()
                chain.proceed(request)
            }
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
    }

    companion object {
        private const val USER_AGENT = "Xplored/1.0 (Android; IADE school project)"

        lateinit var instance: XploredApplication
            private set

        // Helper to prevent crashes if accessed too early
        fun getContext(): Context {
            return instance.applicationContext
        }
    }
}
