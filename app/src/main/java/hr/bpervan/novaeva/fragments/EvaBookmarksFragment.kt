package hr.bpervan.novaeva.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.RxEventBus
import hr.bpervan.novaeva.adapters.EvaRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContentMetadata
import hr.bpervan.novaeva.storage.EvaContentDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import io.reactivex.disposables.Disposable
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_bookmarks.*
import java.util.*

/**
 *
 */
class EvaBookmarksFragment : EvaBaseFragment() {

    companion object : EvaFragmentFactory<EvaBookmarksFragment, Unit> {

        override fun newInstance(initializer: Unit): EvaBookmarksFragment {
            return EvaBookmarksFragment()
        }
    }

    private var loadBookmarksFromDbDisposable: Disposable? = null
        set(value) {
            field = safeReplaceDisposable(field, value)
        }

    private lateinit var adapter: EvaRecyclerAdapter
    private var bookmarksList: MutableList<EvaContentMetadata> = ArrayList()

    private val realm: Realm by lazy {
        Realm.getInstance(RealmConfigProvider.evaDBConfig)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = EvaRecyclerAdapter(bookmarksList)

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Zabiljeske")
                        .setAction("OtvoreneZabiljeske")
                        .build())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_bookmarks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        activity?.title = "Bookmarks"
    }

    override fun onResume() {
        super.onResume()

        bookmarksList.clear()
        reloadBookmarksFromDb()
    }

    private fun initUI() {

        btnSearch.setOnClickListener {
            showSearchPopup()
        }

        val recyclerView = evaRecyclerView as RecyclerView

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.itemAnimator = DefaultItemAnimator()
    }

    private fun reloadBookmarksFromDb() {
        bookmarksList.clear()
        loadBookmarksFromDbDisposable =
                EvaContentDbAdapter.loadManyEvaContentMetadata(realm, { it.bookmark }) {
                    bookmarksList.add(it)
                    adapter.notifyDataSetChanged()
                }
    }


    private fun showSearchPopup() {
        activity?.let { activity ->
            val searchBuilder = AlertDialog.Builder(activity)
            searchBuilder.setTitle("Pretraga")
            val et = EditText(activity)
            searchBuilder.setView(et)
            searchBuilder.setPositiveButton("Pretrazi") { _, _ ->
                val searchString = et.text.toString()
                RxEventBus.search.onNext(searchString)
            }
            searchBuilder.setNegativeButton("Odustani") { _, _ -> }

            searchBuilder.show()
        }
    }
}