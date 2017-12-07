package hr.bpervan.novaeva.activities

import android.os.Bundle
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.fragments.VijestFragment
import hr.bpervan.novaeva.main.R

class VijestActivity : EvaBaseActivity() {

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

        setContentView(R.layout.eva_fragment_frame_layout)

//        startService(Intent(this, BackgroundPlayerService::class.java)) //todo

        (application as NovaEvaApp).getTracker(NovaEvaApp.TrackerName.APP_TRACKER).send(
                HitBuilders.EventBuilder()
                        .setCategory("Vijesti")
                        .setAction("OtvorenaVijest")
                        .setLabel(contentId.toString() + "")
                        .build()
        )

        if (supportFragmentManager.findFragmentByTag(TAG_RETAINED_VIJEST_FRAGMENT) == null) {
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.eva_fragment_frame, VijestFragment.newInstance(contentId, categoryId.toLong()), TAG_RETAINED_VIJEST_FRAGMENT)
                    .commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(CONTENT_ID_KEY, contentId)
        outState.putInt(THEME_ID_KEY, themeId)
        outState.putInt(CATEGORY_ID_KEY, themeId)

        super.onSaveInstanceState(outState)
    }

    /*if(mPlayer != null){
        if(mPlayer.isPlaying()){
            mPlayer.stop();
        }
        mPlayer.release();
        seekArc.removeCallbacks(onEverySecond);
    }
    mPlayer = null;*/
//
//    override fun onClick(v: View) {
//        when (v.id) {
//            R.id.btnPoziv -> {
//                when (intent.getIntExtra("kategorija", 11)) {
//                    EvaCategory.POZIV.id -> {
//                        val text = "Hvaljen Isus i Marija, javljam Vam se jer razmišljam o duhovnom pozivu."
//                        sendEmailIntent(this, "Duhovni poziv", text, arrayOf("duhovnipoziv@gmail.com"))
//                    }
//                }
//            }
//            R.id.btnPlay -> {
//                Log.d(TAG, "btnPlay")
//                if (BackgroundPlayerService.isRunning) {
//                    val messageIntent = Intent(this@VijestActivity, BackgroundPlayerService::class.java)
//                    messageIntent.putExtra(BackgroundPlayerService.KEY_DIRECTIVE, BackgroundPlayerService.DIRECTIVE_SET_SOURCE_PLAY)
//                    messageIntent.putExtra(BackgroundPlayerService.KEY_PATH, evaContent!!.audioURL)
//                    messageIntent.putExtra(BackgroundPlayerService.KEY_TITLE, evaContent!!.contentMetadata!!.title)
//                    startService(messageIntent)
//                    btnPlay.visibility = View.INVISIBLE
//                    btnPause.visibility = View.VISIBLE
//                    btnPause.isEnabled = false
//                    Log.d(TAG, "Sent MSG_SET_SOURCE_AND_PLAY")
//                }
//            }
//            R.id.btnPause -> {
//                Log.d(TAG, "btnPlay")
//                val pauseIntent = Intent(this@VijestActivity, BackgroundPlayerService::class.java)
//                pauseIntent.putExtra(BackgroundPlayerService.KEY_DIRECTIVE, BackgroundPlayerService.DIRECTIVE_PAUSE)
//                startService(pauseIntent)
//                /*if(mPlayer.isPlaying()){
//				Log.d(TAG, "btnPlay");
//				mPlayer.pause();
//				seekArc.removeCallbacks(onEverySecond);
//				btnPause.setVisibility(View.INVISIBLE);
//				btnPlay.setVisibility(View.VISIBLE);
//			}*/
//                btnPause.visibility = View.INVISIBLE
//                btnPlay.visibility = View.VISIBLE
//                btnPlay.isEnabled = false
//            }
////            R.id.btnTextPlus -> {
////                var mCurrentSize = prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14)
////                mCurrentSize += 2
////                if (mCurrentSize >= 28) {
////                    mCurrentSize = 12
////                }
////
////                prefs.edit().putInt("hr.bpervan.novaeva.velicinateksta", mCurrentSize).apply()
////                vijestWebView.settings.defaultFontSize = mCurrentSize
////            }
////            R.id.btnBack -> this@VijestActivity.onBackPressed()
//            R.id.imgLink ->
//                evaContent!!.videoURL?.let { videoUrl ->
//                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)))
//                }
//            R.id.imgText ->
//                if (evaContent!!.attachments.isNotEmpty()) {
//                    startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse(evaContent!!.attachments[0]!!.url)),
//                            "Otvaranje dokumenta " + evaContent!!.attachments[0]!!.name))
//                }
//        }
//    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.move_left_in, R.anim.move_right_out)
    }

    companion object {
        val CONTENT_ID_KEY = "contentId"
        val CATEGORY_ID_KEY = "categoryId"
        val THEME_ID_KEY = "themeId"

        private val TAG_RETAINED_VIJEST_FRAGMENT = "RetainedVijestFragment"
    }
}