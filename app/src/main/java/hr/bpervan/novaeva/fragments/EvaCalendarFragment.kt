package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

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
    }
}