package hr.bpervan.novaeva.activities

import android.os.Bundle
import hr.bpervan.novaeva.fragments.IzrekeFragment
import hr.bpervan.novaeva.main.R

//import com.google.analytics.tracking.android.EasyTracker;

class IzrekeActivity : EvaBaseActivity() {

    private var themeId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs.edit().putInt("vidjenoKategorija1", 1).apply()

        val inState = savedInstanceState ?: intent.extras
        themeId = inState.getInt("themeId", -1)

        if (themeId != -1) {
            setTheme(themeId)
        }

        setContentView(R.layout.eva_fragment_frame_layout)

        if (supportFragmentManager.findFragmentByTag(TAG_RETAINED_IZREKE_FRAGMENT) == null) {
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.eva_fragment_frame, IzrekeFragment.newInstance(), TAG_RETAINED_IZREKE_FRAGMENT)
                    .commit()

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("themeId", themeId)
        super.onSaveInstanceState(outState)
    }

//    override fun onClick(v: View) {
//        val vId = v.id
//        when (vId) {
//            R.id.btnTextPlus -> {//showTextSizePopup();
//                var mCurrentSize = prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14)
//                mCurrentSize += 2
//                if (mCurrentSize >= 28) {
//                    mCurrentSize = 12
//                }
//
//                prefs.edit().putInt("hr.bpervan.novaeva.velicinateksta", mCurrentSize).apply()
//                webText.settings.defaultFontSize = mCurrentSize
//
//            }
//        }
//    }


    companion object {
        private val TAG_RETAINED_IZREKE_FRAGMENT = "RetainedIzrekeFragment"
    }
}
