package hr.bpervan.novaeva.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.analytics.FirebaseAnalytics
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.adapters.EvaRecyclerAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.main.databinding.FragmentBookmarksBinding
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.storage.EvaContentDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import io.reactivex.disposables.Disposable
import io.realm.Realm

/**
 *
 */
class EvaBookmarksFragment : EvaBaseFragment() {

    companion object : EvaFragmentFactory<EvaBookmarksFragment, Unit> {

        override fun newInstance(initializer: Unit): EvaBookmarksFragment {
            return EvaBookmarksFragment()
        }
    }

    private var _viewBinding: FragmentBookmarksBinding? = null
    private val viewBinding get() = _viewBinding!!

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
        _viewBinding = FragmentBookmarksBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeNavbarColor.onNext(R.color.Black)
        EventPipelines.changeStatusbarColor.onNext(R.color.VeryDarkGray)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.White)

        val recyclerView = viewBinding.evaRecyclerView.root

        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(recyclerView.context)
        recyclerView.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
    }

    override fun onAttach(context: Context) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun reloadBookmarksFromDb() {
        bookmarksList.clear()
        loadBookmarksFromDbDisposable =
                EvaContentDbAdapter.loadManyEvaContents(realm, predicate = { it.bookmarked }) {
                    bookmarksList.add(it)
                    adapter.notifyDataSetChanged()
                }
    }
}