package hr.bpervan.novaeva.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.nostra13.universalimageloader.core.ImageLoader
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.util.minusAssign
import hr.bpervan.novaeva.util.plusAssign
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
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

        baseDisposables += EventPipelines.changeFragmentBackgroundResource
                .distinctUntilChanged()
                .subscribe {
                    view.setBackgroundResource(it)
                }

        optionsBtn?.setOnClickListener {
            EventPipelines.openOptionsDrawer.onNext(Unit)
        }

        radioBtn?.setOnClickListener {
            EventPipelines.openRadio.onNext(Unit)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
}
