package hr.bpervan.novaeva.fragments

/**
 *
 */
class EvaCalendarFragment : EvaBaseFragment() {

    companion object : EvaFragmentFactory<EvaCalendarFragment, Unit> {
        override fun newInstance(initializer: Unit): EvaCalendarFragment {
            return EvaCalendarFragment()
        }
    }
}