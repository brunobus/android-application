package hr.bpervan.novaeva.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.firebase.analytics.FirebaseAnalytics
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.adapters.EvaRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.storage.EvaContentDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import io.reactivex.disposables.Disposable
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_bookmarks.*
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
    private var bookmarksList: MutableList<EvaContent> = ArrayList()

    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        realm = Realm.getInstance(RealmConfigProvider.evaDBConfig)

        adapter = EvaRecyclerAdapter(bookmarksList)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bookmarks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeNavbarColor.onNext(R.color.Black)
        EventPipelines.changeStatusbarColor.onNext(R.color.VeryDarkGray)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.White)

        val recyclerView = evaRecyclerView as androidx.recyclerview.widget.RecyclerView

        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(recyclerView.context)
        recyclerView.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        activity?.title = "Bookmarks"
    }

    override fun onResume() {
        super.onResume()

        bookmarksList.clear()
        reloadBookmarksFromDb()

        FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), "Zabilješke", "Bookmarks")
    }

    private fun reloadBookmarksFromDb() {
        bookmarksList.clear()
        loadBookmarksFromDbDisposable =
                EvaContentDbAdapter.loadManyEvaContents(realm, predicate = { it.bookmarked }) {
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
                EventPipelines.search.onNext(searchString)
            }
            searchBuilder.setNegativeButton("Odustani") { _, _ -> }

            searchBuilder.show()
        }
    }
}