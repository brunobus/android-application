package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.toast
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.storage.EvaContentDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import hr.bpervan.novaeva.util.TransitionAnimation
import hr.bpervan.novaeva.util.shareIntent
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_options.*

/**
 *
 */
class OptionsFragment : Fragment() {
    companion object : EvaBaseFragment.EvaFragmentFactory<OptionsFragment, Unit> {

        override fun newInstance(initializer: Unit): OptionsFragment {
            return OptionsFragment()
        }
    }

    private lateinit var realm: Realm
    var contentId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        realm = Realm.getInstance(RealmConfigProvider.evaDBConfig)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        topFragmentChecker.invoke()
        activity?.supportFragmentManager?.addOnBackStackChangedListener(topFragmentChecker)

        btnInfo.setOnClickListener {
            EventPipelines.openInfo.onNext(TransitionAnimation.LEFTWARDS)
        }
//        btnHelp.setOnClickListener {
//            context?.toast("Not yet supported")
//        }
        btnTextSize.setOnClickListener {
            context?.toast("Not yet supported")
        }
        btnChurch.setOnClickListener {
            context?.toast("Not yet supported")
        }
        btnTheme.setOnClickListener {
            context?.toast("Not yet supported")
        }
        btnShare.setOnClickListener {
            shareIntent(context, "http://novaeva.com/node/$contentId")
        }
        btnBookmark.setOnClickListener {
            EvaContentDbAdapter.updateEvaContentMetadataAsync(realm, contentId,
                    updateFunction = {
                        it.bookmark = true
                    },
                    onSuccess = {
                        context?.toast(R.string.bookmarked)
                    })
        }
        btnHome.setOnClickListener {
            EventPipelines.goHome.onNext(TransitionAnimation.RIGHTWARDS)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        activity?.supportFragmentManager?.removeOnBackStackChangedListener(topFragmentChecker)
    }

    override fun onDestroy() {
        super.onDestroy()

        realm.close()
    }

    private val topFragmentChecker: () -> Unit = {
        val fm = activity?.supportFragmentManager
        if (fm != null && fm.backStackEntryCount > 0) {
            val backStackEntryAt: FragmentManager.BackStackEntry = fm
                    .getBackStackEntryAt(fm.backStackEntryCount - 1)

            val fragment = fm.findFragmentByTag(backStackEntryAt.name)

            when (fragment) {
                is EvaContentFragment -> {
                    enableOptions(btnInfo, btnHelp, btnTextSize, btnChurch,
                            btnTheme, btnHome, btnShare, btnBookmark)

                    contentId = fragment.contentId
                }
                is EvaDashboardFragment -> {
                    enableOptions(btnInfo, btnHelp, btnChurch, btnTheme)
                    disableOptions(btnHome, btnTextSize, btnShare, btnBookmark)
                }
                is EvaInfoFragment -> {
                    enableOptions(btnHelp, btnChurch, btnTheme, btnHome)
                    disableOptions(btnInfo, btnTextSize, btnShare, btnBookmark)
                }
                else -> {
                    enableOptions(btnInfo, btnHelp, btnChurch, btnTheme, btnHome)
                    disableOptions(btnTextSize, btnShare, btnBookmark)
                }
            }
        }
    }

    private fun disableOptions(vararg views: View) = views.forEach { it.isEnabled = false }

    private fun enableOptions(vararg views: View) = views.forEach { it.isEnabled = true }
}