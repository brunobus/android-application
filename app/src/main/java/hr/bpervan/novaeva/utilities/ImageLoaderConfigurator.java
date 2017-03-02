package hr.bpervan.novaeva.utilities;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class ImageLoaderConfigurator {

	private File cacheDir;
	private ImageLoaderConfiguration config;
	
	private Context context;
	
	public ImageLoaderConfigurator(Context context){
		this.context = context;	
	}
	
	public void doInit(){
		cacheDir = StorageUtils.getCacheDirectory(context);
		
		config = new ImageLoaderConfiguration.Builder(context)
		.diskCacheExtraOptions(480, 800, null)
		.denyCacheImageMultipleSizesInMemory()
		.memoryCache(new LruMemoryCache(2 * 1024 * 1024))
		.diskCache(new UnlimitedDiscCache(cacheDir)) // default
		.diskCacheFileCount(100)
		.diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
		.imageDownloader(new BaseImageDownloader(context)) // default
		.imageDecoder(new BaseImageDecoder(true)) // default
		.defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
		.writeDebugLogs()
		.build();
		
		ImageLoader.getInstance().init(config);
	}
	
	public DisplayImageOptions doConfig(boolean cacheOnDisk){
		DisplayImageOptions options = new DisplayImageOptions.Builder()
	        .resetViewBeforeLoading(false)  // default
	        .cacheInMemory(true) // default
	        .cacheOnDisk(cacheOnDisk) // default
	        .considerExifParams(false) // default
	        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
	        .bitmapConfig(Bitmap.Config.ARGB_8888) // default
	        .build();
		
		return options;
	}
}
