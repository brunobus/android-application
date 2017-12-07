package hr.bpervan.novaeva.customviews

import android.content.Context
import android.content.Intent
import android.view.MotionEvent
import android.widget.TextView
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import kotlin.properties.Delegates

/**
 * Created by vpriscan on 30.10.17..
 */
abstract class EvaBaseCustomView(context: Context) : TextView(context) {

    val hasNewStuff: Boolean by Delegates.observable(true) { prop, oldValue, newValue ->
        if (newValue) {
            setBackgroundResource(R.drawable.button_aktualno_news)
        } else {
            setBackgroundResource(R.drawable.button_aktualno)
        }
    }

    init {
        setOnClickListener {
            context.startActivity(intentToStartActivity())
        }
        setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val text = textFeedback()
                NovaEvaApp.bus.frontTextChange.onNext(text)
            }
            false
        }
    }

    abstract fun intentToStartActivity(): Intent
    abstract fun textFeedback(): String
    abstract fun backgroundResource(hasNewStuff: Boolean): Int
}