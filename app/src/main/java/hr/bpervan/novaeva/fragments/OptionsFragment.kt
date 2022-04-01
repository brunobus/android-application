package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.view.isVisible
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.main.databinding.FragmentOptionsBinding
import hr.bpervan.novaeva.storage.EvaContentDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import hr.bpervan.novaeva.util.TEXT_SIZE_KEY
import hr.bpervan.novaeva.util.TransitionAnimation
import hr.bpervan.novaeva.util.defaultTextSize
import hr.bpervan.novaeva.util.maxTextSize
import hr.bpervan.novaeva.util.minTextSize
import hr.bpervan.novaeva.util.shareIntent
import hr.bpervan.novaeva.util.toast
import io.realm.Realm

/**
 *
 */
class OptionsFragment : androidx.fragment.app.Fragment() {
    companion object : EvaBaseFragment.EvaFragmentFactory<OptionsFragment, Unit> {

        override fun newInstance(initializer: Unit): OptionsFragment {
            return OptionsFragment()
        }
    }

    private lateinit var realm: Realm

    private var _viewBinding: FragmentOptionsBinding? = null
    private val viewBinding get() = _viewBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        realm = Realm.getInstance(RealmConfigProvider.evaDBConfig)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _viewBinding = FragmentOptionsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        topFragmentChecker.invoke()
        activity?.supportFragmentManager?.addOnBackStackChangedListener(topFragmentChecker)

        viewBinding.btnInfo.setOnClickListener {
            EventPipelines.openInfo.onNext(TransitionAnimation.LEFTWARDS)
        }
        viewBinding.btnTextSize.setOnClickListener {
            val currentTextSize = NovaEvaApp.prefs.getInt(TEXT_SIZE_KEY, defaultTextSize)
            val newShift = currentTextSize + 2 - minTextSize
            val width = maxTextSize - minTextSize
            val newTextSize = newShift % width + minTextSize

            NovaEvaApp.prefs.edit {
                putInt(TEXT_SIZE_KEY, newTextSize)
            }
            EventPipelines.resizeText.onNext(Unit)
        }
        viewBinding.btnChurch.setOnClickListener {
            context?.toast(getString(R.string.not_supported))
        }
        viewBinding.btnTheme.setOnClickListener {
            context?.toast(getString(R.string.not_supported))
        }
        viewBinding.btnShare.setOnClickListener {

            val topFragment = getTopFragment()

            val shareString = when (topFragment) {
                is EvaContentFragment -> "http://novaeva.com/node/${topFragment.contentId}"
                is EvaQuotesFragment -> {
                    @Suppress("DEPRECATION")
                    val quoteText = Html.fromHtml(topFragment.quoteData).toString().trim()
                    "$quoteText\n\nhttp://novaeva.com/node/${topFragment.quoteId}#quote"
                }
                else -> getString(R.string.recommendation) + getString(R.string.app_links)

            }
            shareIntent(context, shareString)
        }

        viewBinding.btnBookmark.setOnClickListener { _ ->

            val topFragment = getTopFragment()
            if (topFragment is EvaContentFragment) {
                val contentId = topFragment.contentId
                val wasBookmarked = isContentBookmarked(contentId)
                EvaContentDbAdapter.updateEvaContentIfExistsAsync(realm, contentId,
                        updateFunc = { it.bookmarked = !wasBookmarked },
                        onSuccess = {
                            context?.toast(if (!wasBookmarked) R.string.bookmarked else R.string.unbookmarked)
                            viewBinding.btnBookmark.bookmarked = !wasBookmarked
                        })
            }
        }
        viewBinding.btnHome.setOnClickListener {
            EventPipelines.goHome.onNext(TransitionAnimation.NONE)
        }
    }

    private fun isContentBookmarked(contentId: Long): Boolean {
        return EvaContentDbAdapter.loadEvaContent(realm, contentId)?.bookmarked ?: false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
        activity?.supportFragmentManager?.removeOnBackStackChangedListener(topFragmentChecker)
    }

    override fun onDestroy() {
        super.onDestroy()

        realm.close()
    }

    private val topFragmentChecker: () -> Unit = {

        val fragment = getTopFragment()

        if (fragment != null) {

            with(viewBinding) {
                when (fragment) {
                    is EvaContentFragment -> {
                        showOptions(
                            btnInfo, /*btnHelp, */btnTextSize, /*btnChurch,
                            btnTheme, */btnHome, btnShare, btnBookmark
                        )

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
    }

    private fun getTopFragment(): androidx.fragment.app.Fragment? {
        val fm = activity?.supportFragmentManager
        return if (fm != null && fm.backStackEntryCount > 0) {
            val backStackEntryAt: androidx.fragment.app.FragmentManager.BackStackEntry = fm
                    .getBackStackEntryAt(fm.backStackEntryCount - 1)

            fm.findFragmentByTag(backStackEntryAt.name)
        } else null
    }

    private fun hideOptions(vararg views: View) = views.forEach { it.isVisible = false }

    private fun showOptions(vararg views: View) = views.forEach { it.isVisible = true }
}