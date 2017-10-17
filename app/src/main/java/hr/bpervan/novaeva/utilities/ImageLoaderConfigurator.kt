package hr.bpervan.novaeva.utilities

import android.content.Context
import android.graphics.Bitmap

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder
import com.nostra13.universalimageloader.core.download.BaseImageDownloader
import com.nostra13.universalimageloader.utils.StorageUtils

object ImageLoaderConfigurator {

    fun doInit(context: Context) {
        val cacheDir = StorageUtils.getCacheDirectory(context)

        val config = ImageLoaderConfiguration.Builder(context)
                .diskCacheExtraOptions(480, 800, null)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(LruMemoryCache(2 * 1024 * 1024))
                .diskCache(UnlimitedDiskCache(cacheDir)) // default
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(HashCodeFileNameGenerator()) // default
                .imageDownloader(BaseImageDownloader(context)) // default
                .imageDecoder(BaseImageDecoder(true)) // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
                .writeDebugLogs()
                .build()

        ImageLoader.getInstance().init(config)
    }

    fun createDefaultDisplayImageOptions(cacheOnDisk: Boolean): DisplayImageOptions {

        return DisplayImageOptions.Builder()
                .resetViewBeforeLoading(false)  // default
                .cacheInMemory(true) // default
                .cacheOnDisk(cacheOnDisk) // default
                .considerExifParams(false) // default
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                .bitmapConfig(Bitmap.Config.ARGB_8888) // default
                .build()
    }
}
