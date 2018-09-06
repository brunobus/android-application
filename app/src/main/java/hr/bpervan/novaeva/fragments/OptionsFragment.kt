package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.core.widget.toast
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.storage.EvaContentDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import hr.bpervan.novaeva.util.*
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
        btnTextSize.setOnClickListener {
            val currentTextSize = NovaEvaApp.prefs.getInt(TEXT_SIZE_KEY, defaultTextSize)
            val newShift = currentTextSize + 2 - minTextSize
            val width = maxTextSize - minTextSize
            val newTextSize = newShift % width + minTextSize

            NovaEvaApp.prefs.edit {
                putInt(TEXT_SIZE_KEY, newTextSize)
            }
            EventPipelines.resizeText.onNext(Unit)
        }
        btnChurch.setOnClickListener {
            context?.toast(getString(R.string.not_supported))
        }
        btnTheme.setOnClickListener {
            context?.toast(getString(R.string.not_supported))
        }
        btnShare.setOnClickListener {

            val topFragment = getTopFragment()

            val shareString = when (topFragment) {
                is EvaContentFragment -> "http://novaeva.com/node/${topFragment.contentId}"
                is EvaQuotesFragment -> {
                    @Suppress("DEPRECATION")
                    val quoteText = Html.fromHtml(topFragment.quoteData).toString().trim()
                    "$quoteText\n\nhttp://novaeva.com/node/${topFragment.quoteId}#quote"
                }
                else -> getString(R.string.recommendation) +
                        "\n\nhttps://play.google.com/store/apps/details?id=hr.bpervan.novaeva.main" +
                        "\nhttps://itunes.apple.com/us/app/nova-eva/id599928807"

            }
            shareIntent(context, shareString)
        }

        btnBookmark.setOnClickListener { _ ->

            val topFragment = getTopFragment()
            if (topFragment is EvaContentFragment) {
                val contentId = topFragment.contentId
                val wasBookmarked = isContentBookmarked(contentId)
                EvaContentDbAdapter.updateEvaContentMetadataAsync(realm, contentId,
                        updateFunction = {
                            it.bookmark = !wasBookmarked
                        },
                        onSuccess = {
                            context?.toast(if (!wasBookmarked) R.string.bookmarked else R.string.unbookmarked)
                            btnBookmark.bookmarked = !wasBookmarked
                        })
            }
        }
        btnHome.setOnClickListener {
            EventPipelines.goHome.onNext(TransitionAnimation.FADE)
        }
    }

    private fun isContentBookmarked(contentId: Long): Boolean {
        val evaContentMetadata = EvaContentDbAdapter.loadEvaContentMetadata(realm, contentId)
        return evaContentMetadata?.bookmark ?: false
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

        val fragment = getTopFragment()

        if (fragment != null) {

            when (fragment) {
                is EvaContentFragment -> {
                    showOptions(btnInfo, /*btnHelp, */btnTextSize, /*btnChurch,
                            btnTheme, */btnHome, btnShare, btnBookmark)

                    btnBookmark.bookmarked = isContentBookmarked(fragment.contentId)
                }
                is EvaQuotesFragment -> {
                    showOptions(btnInfo, /*btnHelp, */btnTextSize, /*btnChurch, btnTheme, */btnHome, btnShare)
                    hideOptions(btnBookmark)
                }
                is BreviaryContentFragment -> {
                    showOptions(btnInfo, /*btnHelp, */btnTextSize, /*btnChurch, btnTheme, */btnHome)
                    hideOptions(btnShare, btnBookmark)
                }
                is EvaInfoFragment -> {
                    showOptions(/*btnHelp, btnChurch, btnTheme, */btnHome, btnTextSize)
                    hideOptions(btnInfo, btnShare, btnBookmark)
                }
                is EvaDashboardFragment -> {
                    showOptions(btnInfo, /*btnHelp, btnChurch, btnTheme, */btnShare)
                    hideOptions(btnHome, btnTextSize, btnBookmark)
                }
                else -> {
                    showOptions(btnInfo, /*btnHelp, btnChurch, btnTheme, */btnHome)
                    hideOptions(btnTextSize, btnShare, btnBookmark)
                }
            }
        }
    }

    private fun getTopFragment(): Fragment? {
        val fm = activity?.supportFragmentManager
        return if (fm != null && fm.backStackEntryCount > 0) {
            val backStackEntryAt: FragmentManager.BackStackEntry = fm
                    .getBackStackEntryAt(fm.backStackEntryCount - 1)

            fm.findFragmentByTag(backStackEntryAt.name)
        } else null
    }

    private fun hideOptions(vararg views: View) = views.forEach { it.isVisible = false }

    private fun showOptions(vararg views: View) = views.forEach { it.isVisible = true }
}