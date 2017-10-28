package hr.bpervan.novaeva.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.fragments.EvaRecyclerFragment
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaCategory
import hr.bpervan.novaeva.model.EvaDirectory
import hr.bpervan.novaeva.model.EvaDirectoryMetadata
import hr.bpervan.novaeva.storage.EvaDirectoryDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import hr.bpervan.novaeva.utilities.ResourceHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_lista_vijesti.*
import kotlinx.android.synthetic.main.activity_lista_vijesti.view.*
import kotlinx.android.synthetic.main.simple_fake_action_bar.view.*
import java.util.concurrent.TimeUnit

class ListaVijestiActivity : EvaBaseActivity(), OnClickListener {

    private var categoryId = -1
    private lateinit var categoryName: String

    private var colourSet = -1

    private val lifecycleBoundDisposables = CompositeDisposable()

    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_vijesti)

        val inState: Bundle = savedInstanceState ?: intent.extras
        categoryName = inState.getString("categoryName", "")
        categoryId = inState.getInt("categoryId", 11)
        colourSet = inState.getInt("colourSet", categoryId)

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

        realm = Realm.getInstance(RealmConfigProvider.cacheConfig)

        initUI()

        if (savedInstanceState == null) {
            showFragmentForDirectory(categoryId.toLong(), categoryName.toUpperCase(), false)
        } else {
            /*fragment backstack already exists and fragments will be restored*/
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putString("categoryName", categoryName)
        outState.putInt("categoryId", categoryId)
        outState.putInt("colourSet", colourSet)

        super.onSaveInstanceState(outState)
    }

    private fun initUI() {
        eva_directory_fragment_frame.progress_bar.visibility = View.INVISIBLE //todo

//        fakeActionBar.btnHome.setOnClickListener(this)
//        fakeActionBar.btnSearch.setOnClickListener(this)
//        fakeActionBar.btnBack.setOnClickListener(this)

        if (categoryId == EvaCategory.ODGOVORI.id) {
            btnImamPitanjeListaVijesti.setOnClickListener(this)
            btnImamPitanjeListaVijesti.visibility = View.VISIBLE
        }

        floatingSearchButton.setOnClickListener { showSearchPopup() }

        setCategoryTypeColour()
    }

    private fun showFragmentForDirectory(dirId: Long, dirName: String, isSubDir: Boolean) {

        val evaRecyclerFragment = EvaRecyclerFragment()
        val bundle = Bundle()
        bundle.putParcelable("fragmentConfig", EvaRecyclerFragment.FragmentConfig(dirId, dirName, isSubDir, colourSet))
        evaRecyclerFragment.arguments = bundle

        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.move_right_in, R.anim.move_left_out, R.anim.move_left_in, R.anim.move_right_out)
                .replace(R.id.eva_directory_fragment_frame, evaRecyclerFragment)
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
        this?.clearFindViewByIdCache()
        super.onConfigurationChanged(newConfig)
        setContentView(R.layout.activity_lista_vijesti)
        initUI()
    }

    override fun onClick(v: View) {
        val vId = v.id

        when (vId) {
            R.id.btnSearch -> showSearchPopup()
//            R.id.btnHome -> NovaEvaApp.goHome(this)
//            R.id.btnBack -> onBackPressed()
            R.id.btnImamPitanjeListaVijesti -> {
                val text = "Hvaljen Isus i Marija, javljam Vam se jer imam pitanje."
                val mail = arrayOfNulls<String>(1)
                mail[0] = "odgovori.novaeva@gmail.com"
                val i = Intent(Intent.ACTION_SEND)
                i.type = "message/rfc822"
                i.putExtra(Intent.EXTRA_SUBJECT, "Nova Eva pitanje")
                i.putExtra(Intent.EXTRA_TEXT, text)
                i.putExtra(Intent.EXTRA_EMAIL, mail)
                startActivity(Intent.createChooser(i, "Odaberite aplikaciju"))

            }
        }
    }

    private fun setCategoryTypeColour() {
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            fakeActionBar.setBackgroundResource(ResourceHandler.getFakeActionBarResourceId(colourSet))
        } else {
//            fakeActionBar.setBackgroundResource(ResourceHandler.getFakeActionBarResourceId(colourSet))
        }
    }

    public override fun onStart() {
        super.onStart()
        GoogleAnalytics.getInstance(this).reportActivityStart(this)
    }

    public override fun onStop() {
        super.onStop()
        GoogleAnalytics.getInstance(this).reportActivityStop(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    public override fun onResume() {
        super.onResume()

        lifecycleBoundDisposables.add(NovaEvaApp.bus.directoryOpenRequest
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { evaDirectoryMetadata ->
                    showFragmentForDirectory(evaDirectoryMetadata.directoryId, evaDirectoryMetadata.title, true)
                })

        lifecycleBoundDisposables.add(NovaEvaApp.bus.contentOpenRequest
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { evaContentMetadata ->
                    val i = Intent(this@ListaVijestiActivity, VijestActivity::class.java)
                    i.putExtra("contentId", evaContentMetadata.contentId)
                    i.putExtra("colourSet", colourSet)
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