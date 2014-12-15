package hr.bpervan.novaeva.fonts;

import android.content.Context;
import android.graphics.Typeface;

public class FontFactory {
	
	private static Typeface openSansBold;
	private static Typeface openSansItalic;
	private static Typeface openSansRegular;
	private static Typeface openSansLight;
	
	private static Context context;
		
	public FontFactory(Context ctx){
		if(context == null){
			context = ctx;
		}
	}

	public static Typeface getFontInstance(CustomFont customFont){		
		if(customFont == CustomFont.BOLD){
			if(openSansBold == null){
				openSansBold = Typeface.createFromAsset(context.getAssets(), "opensans-bold.ttf");
			}
			return openSansBold;
		} else if(customFont == CustomFont.ITALIC){
			if(openSansItalic == null){
				openSansItalic = Typeface.createFromAsset(context.getAssets(), "opensans-italic.ttf");
			}
			return openSansItalic;
		} else if(customFont == CustomFont.REGULAR){
			if(openSansRegular == null){
				openSansRegular = Typeface.createFromAsset(context.getAssets(), "opensans-regular.ttf");
			}
			return openSansRegular;
		} else if(customFont == CustomFont.LIGHT){
			if(openSansLight == null){
				openSansLight = Typeface.createFromAsset(context.getAssets(), "opensans-light.ttf");
			}
			return openSansLight;
		}
		return null;
	}	
}
