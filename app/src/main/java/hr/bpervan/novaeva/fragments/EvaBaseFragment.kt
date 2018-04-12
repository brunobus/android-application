package hr.bpervan.novaeva.fragments

import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.View
import com.nostra13.universalimageloader.core.ImageLoader
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaTheme
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_prayers.*

/**
 * Created by vpriscan on 26.11.17..
 */
abstract class EvaBaseFragment : Fragment() {

    /**
     * Fragment's lifecycle bound disposables
     */
    protected val baseDisposables = CompositeDisposable()

    protected val prefs: SharedPreferences
        get() = NovaEvaApp.prefs

    protected val imageLoader: ImageLoader
        get() = NovaEvaApp.imageLoader

    interface EvaFragmentFactory<out T : Fragment, in K> {
        val tag: String
            get() = javaClass.canonicalName

        fun newInstance(initializer: K): T
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        optionsBtn?.setOnClickListener {
            EventPipelines.openOptionsDrawer.onNext(Unit)
        }

        radioBtn?.setOnClickListener {
            EventPipelines.openRadio.onNext(Unit)
        }

        baseDisposables += EventPipelines.changeEvaTheme
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::applyEvaTheme)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        baseDisposables.clear()
    }

    override fun onStop() {
        super.onStop()
        baseDisposables.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        baseDisposables.dispose()
    }

    protected fun safeReplaceDisposable(oldDisposable: Disposable?, newDisposable: Disposable?): Disposable? {
        if (oldDisposable != null) {
            baseDisposables -= oldDisposable
            oldDisposable.dispose() //do not rely on baseDisposables.remove()
        }
        if (newDisposable != null && !newDisposable.isDisposed) {
            baseDisposables += newDisposable
        }
        return newDisposable
    }

    protected open fun provideNavBarColorId(evaTheme: EvaTheme): Int {
        return R.color.Black
    }

    protected open fun provideStatusBarColorId(evaTheme: EvaTheme): Int {
        return R.color.VeryDarkGray
    }

    protected open fun provideFragmentBackgroundDrawable(evaTheme: EvaTheme): Drawable? {
        val activity = activity ?: return null
        return when (evaTheme) {
            EvaTheme.DEFAULT -> ColorDrawable(ContextCompat.getColor(activity, R.color.White))
            EvaTheme.NIGHT -> ColorDrawable(ContextCompat.getColor(activity, R.color.VeryDarkGray))
        }
    }

    protected open fun provideWindowBackgroundDrawable(evaTheme: EvaTheme): Drawable? {
        return null
    }

    private fun applyEvaTheme(evaTheme: EvaTheme) {
        val activity = activity ?: return

        val windowBackground: Drawable? = provideWindowBackgroundDrawable(evaTheme)
        val fragmentBackground: Drawable? = provideFragmentBackgroundDrawable(evaTheme)
        val statusBarColorId: Int = provideStatusBarColorId(evaTheme)
        val navBarColorId: Int = provideNavBarColorId(evaTheme)

        if (windowBackground != null) {
            activity.window?.setBackgroundDrawable(windowBackground)
        }

        if (fragmentBackground != null) {
            view?.background = fragmentBackground
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val statusBarColor = ContextCompat.getColor(activity, statusBarColorId)
            activity.window?.statusBarColor = statusBarColor

            val navBarColor = ContextCompat.getColor(activity, navBarColorId)
            activity.window?.navigationBarColor = navBarColor
        }
    }

    private operator fun CompositeDisposable.minusAssign(oldDisposable: Disposable) {
        remove(oldDisposable)
    }
}
