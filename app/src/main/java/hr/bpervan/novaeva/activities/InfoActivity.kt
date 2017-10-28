package hr.bpervan.novaeva.activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import kotlinx.android.synthetic.main.activity_info.*
import kotlinx.android.synthetic.main.simple_fake_action_bar.view.*

class InfoActivity : EvaBaseActivity(), OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        initUI()
    }

    private fun initUI() {

        webViewInfo.loadUrl("file:///android_asset/info.htm")

//        fakeActionBar.btnBack.setOnClickListener(this)
        fakeActionBar.btnSearch.setOnClickListener(this)
//        fakeActionBar.btnHome.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val id = v.id
        when (id) {
            R.id.btnSearch -> showSearchPopup()
//            R.id.btnHome -> NovaEvaApp.goHome(this)
//            R.id.btnBack -> onBackPressed()
        }
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
