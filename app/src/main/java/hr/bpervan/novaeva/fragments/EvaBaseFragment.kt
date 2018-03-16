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
import hr.bpervan.novaeva.*
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContextType
import hr.bpervan.novaeva.model.EvaContextType.*
import hr.bpervan.novaeva.model.EvaTheme
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

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

    abstract val evaContextType: EvaContextType

    interface EvaFragmentFactory<out T : Fragment, in K> {
        val tag: String
            get() = javaClass.canonicalName

        fun newInstance(initializer: K): T
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseDisposables.add(RxEventBus.changeEvaTheme
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::applyEvaTheme))
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
            baseDisposables.remove(oldDisposable)
            oldDisposable.dispose() //do not rely on baseDisposables.remove()
        }
        if (newDisposable != null && !newDisposable.isDisposed) {
            baseDisposables.add(newDisposable)
        }
        return newDisposable
    }

    //todo
    private fun applyEvaTheme(evaTheme: EvaTheme) {
        val activity = activity ?: return

        val backgroundDrawable: Drawable?
        val statusBarColorId: Int
        val navBarColorId: Int

        when (evaContextType) {
            CONTENT -> {
                when (evaTheme) {
                    EvaTheme.DEFAULT -> {
                        backgroundDrawable = ColorDrawable(ContextCompat.getColor(activity, R.color.White))
                        statusBarColorId = R.color.VeryDarkGray
                        navBarColorId = R.color.Black
                    }
                    EvaTheme.NIGHT -> {
                        backgroundDrawable = ColorDrawable(ContextCompat.getColor(activity, R.color.VeryDarkGray))
                        statusBarColorId = R.color.VeryDarkGray
                        navBarColorId = R.color.Black
                    }
                }
            }
            BREVIARY -> {
                backgroundDrawable = ContextCompat.getDrawable(activity, R.drawable.brevijar_backbrevijar)!!
                statusBarColorId = R.color.Transparent
                navBarColorId = R.color.Transparent
            }
            DASHBOARD -> {
                backgroundDrawable = null
                statusBarColorId = R.color.Transparent
                navBarColorId = R.color.Transparent
            }
        }

        if (backgroundDrawable != null) {
            activity.window?.setBackgroundDrawable(backgroundDrawable)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val statusBarColor = ContextCompat.getColor(activity, statusBarColorId)
            activity.window?.navigationBarColor = statusBarColor

            val navBarColor = ContextCompat.getColor(activity, navBarColorId)
            activity.window?.navigationBarColor = navBarColor
        }
    }
}