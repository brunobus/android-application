package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.util.TransitionAnimation
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sequenceOf(btnInfo, btnHelp, btnTextSize, btnChurch,
                btnTheme, btnShare, btnBookmark, btnHome).forEach { textView ->

        }

//        val lastFragment = activity?.supportFragmentManager?.fragments?.last()
//        if (lastFragment is EvaContentFragment){
//
//        }

        btnInfo.setOnClickListener {
            EventPipelines.openInfo.onNext(TransitionAnimation.LEFTWARDS)
        }
        btnHelp.setOnClickListener {

        }
        btnTextSize.setOnClickListener {

        }
        btnChurch.setOnClickListener {

        }
        btnTheme.setOnClickListener {

        }
        btnShare.setOnClickListener {

        }
        btnBookmark.setOnClickListener {

        }
        btnHome.setOnClickListener {
            EventPipelines.goHome.onNext(TransitionAnimation.RIGHTWARDS)
        }
    }
}