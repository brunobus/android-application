package hr.bpervan.novaeva.fonts

import android.content.Context
import android.graphics.Typeface

class FontFactory(ctx: Context) {

    init {
        if (context == null) {
            context = ctx
        }
    }

    companion object {

        private var openSansBold: Typeface? = null
        private var openSansItalic: Typeface? = null
        private var openSansRegular: Typeface? = null
        private var openSansLight: Typeface? = null

        private var context: Context? = null

        fun getFontInstance(customFont: CustomFont): Typeface? {
            if (customFont == CustomFont.BOLD) {
                if (openSansBold == null) {
                    openSansBold = Typeface.createFromAsset(context!!.assets, "opensans-bold.ttf")
                }
                return openSansBold
            } else if (customFont == CustomFont.ITALIC) {
                if (openSansItalic == null) {
                    openSansItalic = Typeface.createFromAsset(context!!.assets, "opensans-italic.ttf")
                }
                return openSansItalic
            } else if (customFont == CustomFont.REGULAR) {
                if (openSansRegular == null) {
                    openSansRegular = Typeface.createFromAsset(context!!.assets, "opensans-regular.ttf")
                }
                return openSansRegular
            } else if (customFont == CustomFont.LIGHT) {
                if (openSansLight == null) {
                    openSansLight = Typeface.createFromAsset(context!!.assets, "opensans-light.ttf")
                }
                return openSansLight
            }
            return null
        }
    }
}
