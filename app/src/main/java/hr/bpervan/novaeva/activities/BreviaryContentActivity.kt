package hr.bpervan.novaeva.activities

import android.os.Bundle
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import hr.bpervan.novaeva.fragments.BreviaryContentFragment
import hr.bpervan.novaeva.main.R

class BreviaryContentActivity : EvaBaseActivity() {

    private var breviaryId: Int = -1

    private lateinit var mGaTracker: Tracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_breviary_details)

        val inState = savedInstanceState ?: intent.extras
        breviaryId = inState.getInt(BREVIARY_ID_KEY, 4)

        if (supportFragmentManager.findFragmentByTag(Companion.TAG_RETAINED_BREVIARY_FRAGMENT) == null) {
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.eva_fragment_frame, BreviaryContentFragment.newInstance(breviaryId), Companion.TAG_RETAINED_BREVIARY_FRAGMENT)
                    .commit()
        }


//        mGaTracker = (application as NovaEvaApp).getTracker(NovaEvaApp.TrackerName.APP_TRACKER)
//        mGaTracker.send(
//                HitBuilders.EventBuilder()
//                        .setCategory("Brevijar")
//                        .setAction("OtvorenaMolitva")
//                        .setLabel(breviaryId.toString())
//                        .build()
//        )

        //mGaTracker.sendEvent("Brevijar", "OtvorenaMolitva", breviaryId, null);
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(BREVIARY_ID_KEY, breviaryId)
        super.onSaveInstanceState(outState)
    }

    public override fun onStart() {
        super.onStart()
        GoogleAnalytics.getInstance(this).reportActivityStart(this)
    }

    public override fun onStop() {
        super.onStop()
        GoogleAnalytics.getInstance(this).reportActivityStop(this)
    }

    companion object {
        val BREVIARY_ID_KEY = "breviaryId"

        private val TAG_RETAINED_BREVIARY_FRAGMENT = "RetainedBreviaryContentFragment"
    }
}