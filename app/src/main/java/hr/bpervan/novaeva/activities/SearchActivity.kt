package hr.bpervan.novaeva.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText

import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.HitBuilders

import java.util.ArrayList
import java.util.concurrent.TimeUnit

import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.MenuElementAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.ContentInfo
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.utilities.ConnectionChecker
import hr.bpervan.novaeva.utilities.EvaCategory
import hr.bpervan.novaeva.utilities.LoadableFromBundle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.eva_recycler_view.view.*
import kotlinx.android.synthetic.main.simple_fake_action_bar.view.*

class SearchActivity : EvaBaseActivity(), OnClickListener, LoadableFromBundle {

    private val lifecycleBoundDisposables = CompositeDisposable()
    private var searchForContentDisposable: Disposable? = null

    private val searchResultList = ArrayList<ContentInfo>()

    private lateinit var adapter: MenuElementAdapter
    private lateinit var searchString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            loadStateFromBundle(savedInstanceState)
        } else {
            loadStateFromBundle(intent.extras)
        }

        this.title = "Pretraga: " + searchString

        /*mGaTracker = mGaInstance.getTracker("UA-40344870-1");

		mGaTracker.sendEvent("Pretraga", "KljucneRijeci", searchString, null);*/

        val mGaTracker = (application as NovaEvaApp).getTracker(NovaEvaApp.TrackerName.APP_TRACKER)
        mGaTracker.send(
                HitBuilders.EventBuilder()
                        .setCategory("Pretraga")
                        .setAction("KljucneRijeci")
                        .setLabel(searchString)
                        .build()
        )

        val configData = MenuElementAdapter.ConfigData(EvaCategory.PROPOVIJEDI.id, { false })

        adapter = MenuElementAdapter(searchResultList, configData, null)

        initUI()

        if (ConnectionChecker.hasConnection(this)) {
            searchForContent(searchString)
        }
    }

    override fun loadStateFromBundle(bundle: Bundle) {
        searchString = bundle.getString("searchString")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("searchString", searchString)

        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()

        lifecycleBoundDisposables.add(NovaEvaApp.bus.contentOpenRequest
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { contentInfo ->
                    val i = Intent(this, VijestActivity::class.java)
                    i.putExtra("contentId", contentInfo.contentId)
                    startActivity(i)
                })
    }

    override fun onPause() {
        super.onPause()

        lifecycleBoundDisposables.clear()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        this?.clearFindViewByIdCache()
        initUI()
    }

    public override fun onStart() {
        super.onStart()
        GoogleAnalytics.getInstance(this).reportActivityStart(this)
    }

    public override fun onStop() {
        super.onStop()
        GoogleAnalytics.getInstance(this).reportActivityStop(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        searchForContentDisposable?.dispose()
    }

    private fun initUI() {
        setContentView(R.layout.activity_search)

        fakeActionBar.btnHome.setOnClickListener(this)
        fakeActionBar.btnSearch.setOnClickListener(this)
        fakeActionBar.btnBack.setOnClickListener(this)

        //todo make this nicer
        evaRecyclerView.evaRecyclerView.adapter = adapter
        evaRecyclerView.evaRecyclerView.layoutManager = LinearLayoutManager(evaRecyclerView.evaRecyclerView.context)
        evaRecyclerView.evaRecyclerView.itemAnimator = DefaultItemAnimator()
    }

    private fun showSearchPopup() {
        val searchBuilder = AlertDialog.Builder(this)
        searchBuilder.setTitle("Pretraga")

        val et = EditText(this)
        searchBuilder.setView(et)

        searchBuilder.setPositiveButton("Pretrazi") { _, _ ->
            val search = et.text.toString()
            searchForContent(search)
        }
        searchBuilder.setNegativeButton("Odustani") { _, _ -> /**/ }
        searchBuilder.show()
    }

    private fun searchForContent(searchString: String) {
        this.searchString = searchString

        searchResultList.clear()
        adapter.notifyDataSetChanged()

        searchForContentDisposable?.dispose()

        searchForContentDisposable = NovaEvaService.instance
                .searchForContent(searchString)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ searchResult ->
                    if (searchResult.searchResultContentInfoList != null && !searchResult.searchResultContentInfoList.isEmpty()) {
                        searchResultList.addAll(searchResult.searchResultContentInfoList)
                        adapter.notifyDataSetChanged()
                    } else {
                        showEmptyListInfo()
                    }
                }) { t ->
                    showErrorPopup(t) {
                        searchForContent(searchString)
                    }
                }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnSearch -> showSearchPopup()
            R.id.btnHome -> NovaEvaApp.goHome(this)
            R.id.btnBack -> onBackPressed()
        }
    }

    private fun showEmptyListInfo() {
        val emptyInfo = AlertDialog.Builder(this)
        emptyInfo.setTitle("Pretraga")
        emptyInfo.setMessage("Pretraga nije vratila rezultate")

        emptyInfo.setPositiveButton("U redu") { dialog, which -> this@SearchActivity.onBackPressed() }

        emptyInfo.show()
    }
}
