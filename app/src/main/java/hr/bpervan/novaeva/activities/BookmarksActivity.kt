package hr.bpervan.novaeva.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.EditText
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.RxEventBus
import hr.bpervan.novaeva.adapters.EvaRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContentMetadata
import hr.bpervan.novaeva.storage.EvaContentDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_bookmarks.*
import kotlinx.android.synthetic.main.eva_recycler_view.view.*
import java.util.*
import java.util.concurrent.TimeUnit

class BookmarksActivity : EvaBaseActivity(), View.OnClickListener {

    private val lifecycleBoundDisposables = CompositeDisposable()

    private lateinit var realm: Realm
    private lateinit var adapter: EvaRecyclerAdapter
    private var bookmarksList: MutableList<EvaContentMetadata> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as NovaEvaApp).defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Zabiljeske")
                        .setAction("OtvoreneZabiljeske")
                        .setLabel("")
                        .setValue(0L)
                        .build())
        //mGaTracker.sendEvent("Zabiljeske", "OtvoreneZabiljeske", "", null);
        //initUI();
        //reloadBookmarksFromDb();

        realm = Realm.getInstance(RealmConfigProvider.evaDBConfig)
        adapter = EvaRecyclerAdapter(bookmarksList)

        initUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    private fun initUI() {
        setContentView(R.layout.activity_bookmarks)

        this.title = "Bookmarks"

        btnSearch.setOnClickListener(this)

        evaRecyclerView.evaRecyclerView.adapter = adapter
        evaRecyclerView.evaRecyclerView.layoutManager = LinearLayoutManager(evaRecyclerView.context)
        evaRecyclerView.evaRecyclerView.itemAnimator = DefaultItemAnimator()
    }

    private fun reloadBookmarksFromDb() {
        bookmarksList.clear()
        EvaContentDbAdapter.loadManyEvaContentMetadata(realm, { it.bookmark }, {
            bookmarksList.add(it)
        }) { adapter.notifyDataSetChanged() }
    }

    private fun showEmptyListInfo() {
        val emptyInfo = AlertDialog.Builder(this)
        emptyInfo.setTitle("Zabilješke")
        emptyInfo.setMessage("Trenutno nemate zabilješki")

        emptyInfo.setPositiveButton("U redu") { _, _ -> this.onBackPressed() }

        emptyInfo.show()
    }

    override fun onResume() {
        super.onResume()
        bookmarksList.clear()
        reloadBookmarksFromDb()

        lifecycleBoundDisposables.add(RxEventBus.contentOpenRequest
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { contentInfo ->
                    val i = Intent(this, EvaContentActivity::class.java)
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

    override fun onClick(v: View) {
        val vId = v.id
        when (vId) {
            R.id.btnSearch -> showSearchPopup()
//            R.id.btnHome -> NovaEvaApp.goHome(this)
//            R.id.btnBack -> onBackPressed()
        }
    }

    private fun showSearchPopup() {
        val searchBuilder = AlertDialog.Builder(this)
        searchBuilder.setTitle("Pretraga")
        val et = EditText(this)
        searchBuilder.setView(et)
        searchBuilder.setPositiveButton("Pretrazi") { _, _ ->
            val search = et.text.toString()
            NovaEvaApp.goSearch(search, this)
        }
        searchBuilder.setNegativeButton("Odustani") { dialog, whichButton -> }
        searchBuilder.show()
    }
}
