package hr.bpervan.novaeva.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.EditText
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.EvaRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContentMetadata
import hr.bpervan.novaeva.model.EvaCategory
import hr.bpervan.novaeva.storage.RealmConfigProvider
import hr.bpervan.novaeva.storage.EvaBookmarkDbAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_bookmarks.*
import kotlinx.android.synthetic.main.eva_recycler_view.view.*
import kotlinx.android.synthetic.main.simple_fake_action_bar.view.*
import java.util.*
import java.util.concurrent.TimeUnit

class BookmarksActivity : EvaBaseActivity(), View.OnClickListener {

    private val lifecycleBoundDisposables = CompositeDisposable()

    private lateinit var bookmarksRealm: Realm
    private lateinit var adapter: EvaRecyclerAdapter
    private var bookmarksList: MutableList<EvaContentMetadata> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mGaTracker = (application as NovaEvaApp).getTracker(NovaEvaApp.TrackerName.APP_TRACKER)
        mGaTracker.send(
                HitBuilders.EventBuilder()
                        .setCategory("Zabiljeske")
                        .setAction("OtvoreneZabiljeske")
                        .setLabel("")
                        .setValue(0L)
                        .build()
        )
        //mGaTracker.sendEvent("Zabiljeske", "OtvoreneZabiljeske", "", null);
        //initUI();
        //reloadBookmarksFromDb();

        val configData = EvaRecyclerAdapter.ConfigData(EvaCategory.PROPOVIJEDI.id)

        bookmarksRealm = Realm.getInstance(RealmConfigProvider.bookmarksConfig)
        adapter = EvaRecyclerAdapter(bookmarksList, configData, null)

        initUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        bookmarksRealm.close()
    }

    private fun initUI() {
        setContentView(R.layout.activity_bookmarks)

        this.title = "Bookmarks"

        fakeActionBar.btnHome.setOnClickListener(this)
        fakeActionBar.btnSearch.setOnClickListener(this)
        fakeActionBar.btnBack.setOnClickListener(this)

        /*
		for(ListElement l : listaBookmarksa){
			l.setUvod(makeUvod())
		}*/

        //todo make this nicer
        evaRecyclerView.evaRecyclerView.adapter = adapter
        evaRecyclerView.evaRecyclerView.layoutManager = LinearLayoutManager(evaRecyclerView.context)
        evaRecyclerView.evaRecyclerView.itemAnimator = DefaultItemAnimator()
    }

    private fun reloadBookmarksFromDb() {
        bookmarksList.clear()
        EvaBookmarkDbAdapter.loadEvaBookmarksAsync(bookmarksRealm) {
            bookmarksList.clear()
            bookmarksList.addAll(it)
            adapter.notifyDataSetChanged()
        }
    }

    private fun showEmptyListInfo() {
        val emptyInfo = AlertDialog.Builder(this)
        emptyInfo.setTitle("Zabilješke")
        emptyInfo.setMessage("Trenutno nemate zabilješki")

        emptyInfo.setPositiveButton("U redu") { dialog, which -> this@BookmarksActivity.onBackPressed() }

        emptyInfo.show()
    }

    override fun onResume() {
        super.onResume()
        bookmarksList.clear()
        reloadBookmarksFromDb()

        lifecycleBoundDisposables.add(NovaEvaApp.bus.contentOpenRequest
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { contentInfo ->
                    val i = Intent(this@BookmarksActivity, VijestActivity::class.java)
                    i.putExtra("contentId", contentInfo.contentId)
                    startActivity(i)
                })
    }

    override fun onPause() {
        super.onPause()

        lifecycleBoundDisposables.clear()
    }

    public override fun onStart() {
        super.onStart()
        GoogleAnalytics.getInstance(this).reportActivityStart(this)
    }

    public override fun onStop() {
        super.onStop()
        GoogleAnalytics.getInstance(this).reportActivityStop(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        this?.clearFindViewByIdCache()
        initUI()
    }

    override fun onClick(v: View) {
        val vId = v.id
        if (vId == R.id.btnSearch) {
            showSearchPopup()

        } else if (vId == R.id.btnHome) {
            NovaEvaApp.goHome(this)

        } else if (vId == R.id.btnBack) {
            onBackPressed()

        }
    }

    private fun showSearchPopup() {
        val search = AlertDialog.Builder(this)
        search.setTitle("Pretraga")
        val et = EditText(this)
        search.setView(et)
        search.setPositiveButton("Pretrazi") { dialog, which ->
            val search = et.text.toString()
            NovaEvaApp.goSearch(search, this@BookmarksActivity)
        }
        search.setNegativeButton("Odustani") { dialog, whichButton -> }
        search.show()
    }
}
