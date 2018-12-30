package hr.bpervan.novaeva.util

import android.content.Context
import android.graphics.Bitmap
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder
import com.nostra13.universalimageloader.core.download.BaseImageDownloader
import com.nostra13.universalimageloader.utils.StorageUtils
import hr.bpervan.novaeva.rest.SERVER_V3
import java.net.HttpURLConnection

object ImageLoaderConfigurator {

    fun createConfiguration(context: Context): ImageLoaderConfiguration {
        val cacheDir = StorageUtils.getCacheDirectory(context)

        return ImageLoaderConfiguration.Builder(context)
                .diskCacheExtraOptions(1080, 1080, null)
                .diskCache(UnlimitedDiskCache(cacheDir))
                .diskCacheFileCount(20)
                .diskCacheFileNameGenerator(HashCodeFileNameGenerator())
                .memoryCache(LruMemoryCache(4 * 1024 * 1024))
                .denyCacheImageMultipleSizesInMemory()
                .imageDecoder(BaseImageDecoder(true))
                .imageDownloader(AuthImageDownloader(context))
                .defaultDisplayImageOptions(DisplayImageOptions.Builder()
                        .resetViewBeforeLoading(false)
                        .cacheInMemory(true)
                        .cacheOnDisk(false)
                        .considerExifParams(false)
                        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                        .bitmapConfig(Bitmap.Config.ARGB_8888)
                        .extraForDownloader(SERVER_V3.auth)
                        .build())
                .writeDebugLogs()
                .build()
    }

    private class AuthImageDownloader(context: Context) : BaseImageDownloader(context) {

        override fun createConnection(url: String, extra: Any?): HttpURLConnection {
            return super.createConnection(url, extra).apply {
                if (extra is String) {
                    setRequestProperty("Authorization", extra)
                }
            }
        }
    }
}
