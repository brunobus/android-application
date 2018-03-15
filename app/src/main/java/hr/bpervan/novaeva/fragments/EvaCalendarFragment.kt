package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hr.bpervan.novaeva.RxEventBus
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.BackgroundReplaceEvent
import hr.bpervan.novaeva.model.BackgroundType

/**
 *
 */
class EvaCalendarFragment : EvaBaseFragment() {

    companion object : EvaFragmentFactory<EvaCalendarFragment, Unit> {
        override fun newInstance(initializer: Unit): EvaCalendarFragment {
            return EvaCalendarFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        RxEventBus.appBackground.onNext(BackgroundReplaceEvent(R.color.WhiteSmoke, BackgroundType.COLOR))
        RxEventBus.navigationAndStatusBarColor.onNext(R.color.Black)
    }
}