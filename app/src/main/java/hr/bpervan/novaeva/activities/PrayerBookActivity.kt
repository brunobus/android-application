package hr.bpervan.novaeva.activities

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.fragments.PrayerContentFragment
import hr.bpervan.novaeva.fragments.PrayerRecyclerFragment
import hr.bpervan.novaeva.main.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_list_eva_content.*
import java.util.concurrent.TimeUnit


class PrayerBookActivity : EvaBaseActivity() {
    /*private Tracker mGaTracker;
	private GoogleAnalytics mGaInstance;*/

    private var themeId = -1

    private val lifecycleBoundDisposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inState: Bundle = savedInstanceState ?: intent.extras
        themeId = inState.getInt("themeId", -1)

        if (themeId != -1) {
            setTheme(themeId)
        }

        setContentView(R.layout.activity_list_eva_content)

        if (supportFragmentManager.findFragmentByTag(TAG_RETAINED_PRAYERBOOK_FRAGMENT) == null) {
            supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.move_right_in, R.anim.move_left_out, R.anim.move_left_in, R.anim.move_right_out)
                    .add(R.id.evaDirectoryFragmentFrame, PrayerRecyclerFragment.newInstance(), TAG_RETAINED_PRAYERBOOK_FRAGMENT)
                    .commit()
        }

        btnSearch.setOnClickListener { showSearchPopup() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("themeId", themeId)

        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()

        lifecycleBoundDisposables.add(NovaEvaApp.bus.prayerOpenRequest
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { prayer ->
                    supportFragmentManager
                            .beginTransaction()
                            .setCustomAnimations(R.anim.move_right_in, R.anim.move_left_out, R.anim.move_left_in, R.anim.move_right_out)
                            .replace(R.id.evaDirectoryFragmentFrame, PrayerContentFragment.newInstance(prayer))
                            .addToBackStack(null)
                            .commit()
                })
    }

    override fun onPause() {
        super.onPause()

        lifecycleBoundDisposables.clear() //clears and disposes
    }

    private fun showSearchPopup() {
        val searchBuilder = AlertDialog.Builder(this)
        searchBuilder.setTitle("Pretraga")

        val et = EditText(this)
        searchBuilder.setView(et)

        searchBuilder.setPositiveButton("Pretrazi") { _, _ ->
            val search = et.text.toString()
            //todo
        }
        searchBuilder.setNegativeButton("Odustani") { _, _ -> }
        searchBuilder.show()
    }

    companion object {
        val TAG_RETAINED_PRAYERBOOK_FRAGMENT = "RetainedPrayerbookFragment"
    }
}
