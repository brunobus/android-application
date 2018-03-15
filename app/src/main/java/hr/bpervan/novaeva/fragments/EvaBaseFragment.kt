package hr.bpervan.novaeva.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.ImageLoader
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

    protected val prefs: SharedPreferences by lazy {
        context!!.getSharedPreferences("hr.bpervan.novaeva", Context.MODE_PRIVATE)
    }

    protected val imageLoader: ImageLoader by lazy {
        ImageLoader.getInstance()
    }

    interface EvaFragmentFactory<out T : Fragment, in K> {
        val tag: String
            get() = javaClass.canonicalName

        fun newInstance(initializer: K): T
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
}