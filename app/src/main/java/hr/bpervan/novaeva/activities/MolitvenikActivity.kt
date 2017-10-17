package hr.bpervan.novaeva.activities

import android.app.AlertDialog
import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemClickListener
import android.widget.EditText
import android.widget.ListView
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.MolitvenikAdapter
import hr.bpervan.novaeva.main.R
import kotlinx.android.synthetic.main.activity_molitvenik.*
import kotlinx.android.synthetic.main.simple_fake_action_bar.view.*

/*import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;*/

class MolitvenikActivity : ListActivity(), OnClickListener {
    /*private Tracker mGaTracker;
	private GoogleAnalytics mGaInstance;*/

    private lateinit var mainListView: ListView
    private lateinit var mainContentList: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_molitvenik)

        /*mGaInstance = GoogleAnalytics.getInstance(this);
		mGaTracker = mGaInstance.getTracker("UA-40344870-1");*/

        initUI()

        mainContentList = listOf(
                "Često tražene molitve",
                "0. Uvod",
                "1. Obrasci vjere",
                "2. Osnovne molitve",
                "3. Svagdanje jutarnje molitve",
                "4. Svagdanje večernje molitve",
                "5. Prigodne molitve",
                "6. Molitve mladih",
                "7. Molitve u kušnji i napasti",
                "8. Molitve za obitelj i roditelje",
                "9. Molitve za bolesne i umiruće",
                "10. Molitve po posebnim nakanama",
                "11. Molitve svetih i velikih ljudi",
                "12. Kratke molitve i zazivi",
                "13. Molitve Duhu Svetome",
                "14. Euharistijska pobožnost",
                "15. Pomirenje",
                "16. Pobožnost križnog puta",
                "17. Deventica i krunica Božanskom milosrđu",
                "18. Molitve Blaženoj Djevici Mariji",
                "19. Salezijanske molitve",
                "20. Molitve mladih",
                "21. Molitve svetima",
                "22. Lectio Divina",
                "23. Moliti igrajući pred Gospodinom")


        mainListView.adapter = MolitvenikAdapter(this, mainContentList)

        mainListView.onItemClickListener = OnItemClickListener { arg0, arg1, position, id ->
            //mGaTracker.sendEvent("Molitvenik", "OtvorenaMolitva", String.valueOf(position), null);
            val intent = Intent(this@MolitvenikActivity, MolitvenikDetaljiActivity::class.java)
            intent.putExtra("id", position.toString())
            intent.putExtra("naslov", mainContentList[position])
            startActivity(intent)
        }
    }

    private fun initUI() {

        this.title = "Molitvenik"

        mainListView = listView
        mainListView.isClickable = true

        fakeActionBar.btnBack.setOnClickListener(this)
        fakeActionBar.btnSearch.setOnClickListener(this)
        fakeActionBar.btnHome.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnSearch -> showSearchPopup()
            R.id.btnHome -> NovaEvaApp.goHome(this)
            R.id.btnBack -> onBackPressed()
        }
    }

    private fun showSearchPopup() {
        val searchBuilder = AlertDialog.Builder(this)
        searchBuilder.setTitle("Pretraga")

        val et = EditText(this)
        searchBuilder.setView(et)

        searchBuilder.setPositiveButton("Pretraži") { _, _ ->
            val search = et.text.toString()
            NovaEvaApp.goSearch(search, this@MolitvenikActivity)
        }
        searchBuilder.setNegativeButton("Odustani") { _, _ -> }
        searchBuilder.show()
    }

}
