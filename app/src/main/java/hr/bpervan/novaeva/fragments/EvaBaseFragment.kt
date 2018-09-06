package hr.bpervan.novaeva.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.google.android.exoplayer2.Player
import com.nostra13.universalimageloader.core.ImageLoader
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.util.minusAssign
import hr.bpervan.novaeva.util.plusAssign
import hr.bpervan.novaeva.views.EvaRadioBtn
import io.reactivex.android.schedulers.AndroidSchedulers
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
    protected val disposables = CompositeDisposable()

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

        val evaRadioBtn = (radioBtn as? EvaRadioBtn)

        disposables += EventPipelines.changeFragmentBackgroundResource
                .distinctUntilChanged()
                .subscribe {
                    view.setBackgroundResource(it)
                }

        if (evaRadioBtn != null) {
            disposables += EventPipelines.playbackStartStopPause
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        evaRadioBtn.expanded = it.playbackInfo?.isRadio == true
                                && it.player.playWhenReady
                                && it.player.playbackState == Player.STATE_READY
                    }
        }

        optionsBtn?.setOnClickListener {
            EventPipelines.toggleOptionsDrawer.onNext(Unit)
        }

        evaRadioBtn?.initialize()
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }

    protected fun safeReplaceDisposable(oldDisposable: Disposable?, newDisposable: Disposable?): Disposable? {
        if (oldDisposable != null) {
            disposables -= oldDisposable
            oldDisposable.dispose() //do not rely on disposables.remove(oldDisposable)
        }
        if (newDisposable != null && !newDisposable.isDisposed) {
            disposables += newDisposable
        }
        return newDisposable
    }
}
