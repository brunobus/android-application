package hr.bpervan.novaeva.activities

import android.os.Bundle
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.fragments.EvaContentFragment
import hr.bpervan.novaeva.main.R

class EvaContentActivity : EvaBaseActivity() {

    private var contentId: Long = 0
    private var themeId: Int = 0
    private var categoryId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inState = savedInstanceState ?: intent.extras
        contentId = inState.getLong(CONTENT_ID_KEY, -1L)
        themeId = inState.getInt(THEME_ID_KEY, -1)
        categoryId = inState.getInt(CATEGORY_ID_KEY, -1)

        if (themeId != -1) {
            setTheme(themeId)
        }

        if (contentId == -1L) {
            contentId = intent.data?.lastPathSegment?.toLongOrNull() ?: -1L
        }

        if (contentId == -1L) {
            finish()
            return
        }

        setContentView(R.layout.eva_fragment_frame)

        (application as NovaEvaApp).defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Vijesti")
                        .setAction("OtvorenaVijest")
                        .setLabel(contentId.toString())
                        .build())

//        startService(Intent(this, AudioPlayerService::class.java))
//        stopService(Intent(this, AudioPlayerService::class.java))

        supportFragmentManager.findFragmentByTag(TAG_RETAINED_CONTENT_FRAGMENT)
                ?: supportFragmentManager
                        .beginTransaction()
                        .add(R.id.evaFragmentFrame, EvaContentFragment.newInstance(contentId, categoryId.toLong()), TAG_RETAINED_CONTENT_FRAGMENT)
                        .commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(CONTENT_ID_KEY, contentId)
        outState.putInt(THEME_ID_KEY, themeId)
        outState.putInt(CATEGORY_ID_KEY, themeId)

        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.move_left_in, R.anim.move_right_out)
    }

    companion object {
        const val CONTENT_ID_KEY = "contentId"
        const val CATEGORY_ID_KEY = "categoryId"
        const val THEME_ID_KEY = "themeId"

        private const val TAG_RETAINED_CONTENT_FRAGMENT = "RetainedContentFragment"
    }
}