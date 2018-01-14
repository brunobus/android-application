package hr.bpervan.novaeva.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.actions.sendEmailIntent
import hr.bpervan.novaeva.fragments.EvaRecyclerFragment
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaCategory
import hr.bpervan.novaeva.storage.RealmConfigProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_list_eva_content.*
import java.util.concurrent.TimeUnit

class ListEvaContentActivity : EvaBaseActivity(), OnClickListener {

    private var categoryId = -1
    private lateinit var categoryName: String

    private var themeId = -1

    private val lifecycleBoundDisposables = CompositeDisposable()

    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inState: Bundle = savedInstanceState ?: intent.extras
        categoryName = inState.getString("categoryName", "")
        categoryId = inState.getInt("categoryId", 11)
        themeId = inState.getInt("themeId", -1)

        if (themeId != -1) {
            setTheme(themeId)
        }

        setContentView(R.layout.activity_list_eva_content)

        //mGaTracker = mGaInstance.getTracker("UA-40344870-1");
        val mGaTracker = (application as NovaEvaApp).getTracker(NovaEvaApp.TrackerName.APP_TRACKER)
        mGaTracker.send(
                HitBuilders.EventBuilder()
                        .setCategory("Kategorije")
                        .setAction("OtvorenaKategorija")
                        .setLabel(categoryName)
                        .build()
        )

        //mGaTracker.sendEvent("Kategorije", "OtvorenaKategorija", Constants.getCatNameById(kategorija), null);
        killRedDot(categoryId)

        realm = Realm.getInstance(RealmConfigProvider.evaDBConfig)

        initUI()

        if (savedInstanceState == null) {
            showFragmentForDirectory(categoryId.toLong(), categoryName.toUpperCase(), false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putString("categoryName", categoryName)
        outState.putInt("categoryId", categoryId)
        outState.putInt("themeId", themeId)

        super.onSaveInstanceState(outState)
    }

    private fun initUI() {

        if (categoryId == EvaCategory.ODGOVORI.id) {
            btnImamPitanjeListaVijesti.setOnClickListener(this)
            btnImamPitanjeListaVijesti.visibility = View.VISIBLE
        }

        btnSearch.visibility = View.VISIBLE

        btnSearch.setOnClickListener { showSearchPopup() }
    }

    private fun showFragmentForDirectory(dirId: Long, dirName: String, isSubDir: Boolean) {

        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.move_right_in, R.anim.move_left_out, R.anim.move_left_in, R.anim.move_right_out)
                .replace(R.id.evaDirectoryFragmentFrame, EvaRecyclerFragment.newInstance(dirId, dirName, isSubDir, themeId))
                .addToBackStack(null)
                .commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        this?.clearFindViewByIdCache()
        setContentView(R.layout.activity_list_eva_content)
        initUI()
    }

    override fun onClick(v: View) {
        val vId = v.id

        when (vId) {
            R.id.btnSearch -> showSearchPopup()
            R.id.btnImamPitanjeListaVijesti -> {
                val text = "Hvaljen Isus i Marija, javljam Vam se jer imam pitanje."
                sendEmailIntent(this, "Nova Eva pitanje", text, arrayOf("odgovori.novaeva@gmail.com"))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    public override fun onResume() {
        super.onResume()

        lifecycleBoundDisposables.add(NovaEvaApp.bus.directoryOpenRequest
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { evaDirectoryMetadata ->
                    showFragmentForDirectory(evaDirectoryMetadata.directoryId, evaDirectoryMetadata.title, true)
                })

        lifecycleBoundDisposables.add(NovaEvaApp.bus.contentOpenRequest
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { evaContentMetadata ->
                    val i = Intent(this@ListEvaContentActivity, EvaContentActivity::class.java)
                    i.putExtra("contentId", evaContentMetadata.contentId)
                    i.putExtra("themeId", themeId)
                    i.putExtra("categoryId", categoryId)
                    startActivity(i)
                    overridePendingTransition(R.anim.move_right_in, R.anim.move_left_out)
                })
    }

    override fun onPause() {
        super.onPause()

        lifecycleBoundDisposables.clear() //clears and disposes
    }

    // Napraviti Builder za search i errorokvir
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

    private fun killRedDot(category: Int) {
        prefs.edit().putInt("vidjenoKategorija" + category, 1).apply()
    }
}