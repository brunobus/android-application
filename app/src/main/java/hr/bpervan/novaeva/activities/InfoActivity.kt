package hr.bpervan.novaeva.activities

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import kotlinx.android.synthetic.main.eva_simple_content.*
import kotlinx.android.synthetic.main.eva_collapsing_bar.view.*

class InfoActivity : EvaBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.eva_simple_content)

        initUI()
    }

    private fun initUI() {

        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.loadUrl("file:///android_asset/info.htm")

        evaCollapsingBar.collapsingToolbar.title = "Nova Eva info"
    }

    private fun showSearchPopup() {
        val searchBuilder = AlertDialog.Builder(this)
        searchBuilder.setTitle("Pretraga")

        val et = EditText(this)
        searchBuilder.setView(et)

        searchBuilder.setPositiveButton("Pretrazi") { _, _ ->
            val search = et.text.toString()
            NovaEvaApp.goSearch(search, this)
        }
        searchBuilder.setNegativeButton("Odustani") { _, _ -> }
        searchBuilder.show()
    }
}
