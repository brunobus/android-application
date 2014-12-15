package hr.bpervan.novaeva.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.utilities.Attachment;
import hr.bpervan.novaeva.utilities.BookmarkTypes;
import hr.bpervan.novaeva.utilities.ConnectionChecker;
import hr.bpervan.novaeva.utilities.Constants;
import hr.bpervan.novaeva.utilities.Image;
import hr.bpervan.novaeva.utilities.ImageLoaderConfigurator;
import hr.bpervan.novaeva.utilities.ListTypes;
import hr.bpervan.novaeva.utilities.ResourceHandler;
import hr.bpervan.novaeva.utilities.Vijest;
import hr.bpervan.novaeva.v2.BookmarksDBHandlerV2;

/**
 * Created by Branimir on 15.12.2014..
 */
public class VijestActivity extends Activity implements
        View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener{
    private static final String TAG = "MediaPlayerDebug";
    /** Main display engine */
    private WebView vijestWebView;

    /** Used for displaying Title and category name */
    private TextView tvNaslov, tvKategorija;

    private ImageView btnHome, btnFace, btnMail, btnBookmark, btnSearch, btnTextPlus, btnBack;

    private ImageView imgMp3, imgLink, imgText;
    private ImageView headerImage;
    private ImageView imgNaslovnaTraka;
    private RelativeLayout fakeActionBar;

    /** Used if category == 8, displays 'Poziv' button */
    private ImageView btnPoziv;

    /** Simple integrated audio streaming player */
    private RelativeLayout mp3Player;
    private TextView tvElapsed, tvDuration;
    private MediaPlayer mPlayer;
    private SeekBar seekArc;
    private ImageView btnPlay, btnPause;
    private volatile boolean isMediaPlayerLoading = false;

    /** Used to store Font size */
    private SharedPreferences prefs;

    /** Pure 'Vijest' data */
    private int nid;
    private Vijest ovaVijest;

    /** Database handler for bookmarks, resource handler for category colours*/
    private BookmarksDBHandlerV2 dbHandler;
    private ResourceHandler resourceHandler;

    /** Custom font resources*/
    private static Typeface openSansBold, openSansLight, openSansRegular;

    /** Image loader*/
    private ImageLoader imageLoader;
    private ImageLoaderConfigurator imageLoaderConfigurator;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        dbHandler = new BookmarksDBHandlerV2(this);
        resourceHandler = new ResourceHandler(this.getIntent().getIntExtra("kategorija", Constants.CAT_PROPOVJEDI));
        prefs = getSharedPreferences("hr.bpervan.novaeva", MODE_PRIVATE);
        nid = getIntent().getIntExtra("nid", 0);

        openSansBold = Typeface.createFromAsset(getAssets(), "opensans-bold.ttf");
        openSansLight = Typeface.createFromAsset(getAssets(), "opensans-light.ttf");
        openSansRegular = Typeface.createFromAsset(getAssets(), "opensans-regular.ttf");

        imageLoaderConfigurator = new ImageLoaderConfigurator(this);
        imageLoader = ImageLoader.getInstance();
        if(!imageLoader.isInited()){
            imageLoaderConfigurator.doInit();
        }

        initUI();

        if(ConnectionChecker.hasConnection(this)){
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(Constants.nidURL + nid, new ResponseHandler(this));
        } else {
            Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show();
            this.onBackPressed();
        }
    }

    private void initUI(){
        setContentView(R.layout.activity_vijest);

        /** Basic data */
        tvNaslov = (TextView) findViewById(R.id.tvNaslov);
        tvKategorija = (TextView) findViewById(R.id.tvKategorija);
        if(openSansBold != null){
            tvNaslov.setTypeface(openSansBold);
        }
        if(openSansLight != null){
            tvKategorija.setTypeface(openSansLight);
        }

        btnPoziv = (ImageView) findViewById(R.id.btnPoziv);
        if(getIntent().getIntExtra("kategorija", Constants.CAT_PROPOVJEDI) == Constants.CAT_POZIV){
            //setQuestionButton();
        }
        /** Lets try displaying news using WebView */
        vijestWebView = (WebView) findViewById(R.id.vijestWebView);
        vijestWebView.setFocusable(false);



        /** Controls for integrated simple media player */
        btnPlay = (ImageView) findViewById(R.id.btnPlay);
        btnPause = (ImageView) findViewById(R.id.btnPause);
        seekArc = (SeekBar) findViewById(R.id.seekArc);
        seekArc.setOnSeekBarChangeListener(this);

        mp3Player = (RelativeLayout) findViewById(R.id.mp3Player);
        tvDuration = (TextView) findViewById(R.id.tvDuration);
        tvElapsed = (TextView) findViewById(R.id.tvElapsed);
        tvDuration.setTypeface(openSansRegular);
        tvElapsed.setTypeface(openSansRegular);

        /** References for audio/textual/generic attachment */
        imgMp3 = (ImageView) findViewById(R.id.imgMp3);
        imgText = (ImageView) findViewById(R.id.imgText);
        imgLink = (ImageView) findViewById(R.id.imgLink);

        /** Controls contained in fakeActionBar with button for text size selection */
        btnHome = (ImageView) findViewById(R.id.btnHome);
        btnFace = (ImageView) findViewById(R.id.btnFace);
        btnMail = (ImageView) findViewById(R.id.btnMail);
        btnBookmark = (ImageView) findViewById(R.id.btnBookmark);
        btnSearch = (ImageView) findViewById(R.id.btnSearch);
        btnTextPlus = (ImageView) findViewById(R.id.btnTextPlus);
        btnBack = (ImageView) findViewById(R.id.btnBack);

        btnHome.setOnClickListener(this);
        btnFace.setOnClickListener(this);
        btnMail.setOnClickListener(this);
        btnBookmark.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        btnTextPlus.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        /** References for fakeActionBar and category - specified coloured bar at the top of the activity*/
        fakeActionBar = (RelativeLayout) findViewById(R.id.fakeActionBar);
        imgNaslovnaTraka = (ImageView) findViewById(R.id.imgNaslovnaTraka);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        initUI();

        //setAttachments();
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            if(ovaVijest.hasImage()){
                if(imageLoader.isInited()){
                    imageLoader.displayImage(ovaVijest.getImage().getUrl640(), headerImage, imageLoaderConfigurator.doConfig(false));
                }
            } else {
                String url = prefs.getString("hr.bpervan.novaeva.categoryheader." + ovaVijest.getKategorija(), null);
                if(url != null){
                    if(imageLoader.isInited()){
                        imageLoader.displayImage(url, headerImage, imageLoaderConfigurator.doConfig(true));
                    }
                }
            }
        }
        vijestWebView.reload();
        vijestWebView.loadDataWithBaseURL(null, ovaVijest.getTekst(), "text/html", "UTF-8", null);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {

    }

    private class ResponseHandler extends JsonHttpResponseHandler{
        private Context context;
        private ProgressDialog progressDialog;
        public ResponseHandler(Context context){
            this.context = context;
            this.progressDialog = new ProgressDialog(context);
            this.progressDialog.setMessage("Učitavam...");
            this.progressDialog.setIndeterminate(true);
            this.progressDialog.setCancelable(false);
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Odustani", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();

                }
            });
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    //AsyncHttpPostTask.this.cancel(true);
                }
            });
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response){
            Attachment[] prilozi = null;
            try{
                if(!response.getString("prilozi").equals("null")){
                    JSONArray attachArray = response.getJSONArray("prilozi");
                    prilozi = new Attachment[attachArray.length()];
                    Log.d("attachArray.length()", attachArray.length() + "");
                    for(int i = 0; i < attachArray.length(); ++i){
                        prilozi[i] = new Attachment(attachArray.getJSONObject(i).getString("naziv"), attachArray.getJSONObject(i).getString("url"));
                    }
                }

                /** Samo za sad će biti da image ide direkno, inače ispitivati screen size itd*/
                ovaVijest = new Vijest(response.getInt("nid"), Integer.parseInt(response.getString("cid")),
                        response.getString("time"), response.getString("naslov"), null, response.getString("tekst"),
                        prilozi, null, null, null);


                ovaVijest.setVrstaZaBookmark(BookmarkTypes.VIJEST);
                ovaVijest.setVrstaPodatka(ListTypes.VIJEST);
                if(!response.getString("youtube").equals("null")){
                    ovaVijest.setLink(response.getString("youtube"));
                }
                if(!response.getString("audio").equals("null")){
                    ovaVijest.setAudio(response.getString("audio"));
                }
                if(!response.getString("image").equals("null")){
                    JSONObject imageField = response.getJSONArray("image").getJSONObject(0);
                    Image image = new Image(imageField.getString("640"), imageField.getString("720"), imageField.getString("date"), imageField.getString("original"));
                    ovaVijest.setImage(image);
                }
                if(ovaVijest.hasAudio() && (mPlayer == null)){
                    mPlayer = MediaPlayer.create(VijestActivity.this, Uri.parse(ovaVijest.getAudio()));
                    mPlayer.setOnPreparedListener(VijestActivity.this);
                    mPlayer.setOnCompletionListener(VijestActivity.this);
                }

            }catch(JSONException e){
            }

            if(ovaVijest.hasAudio()){
                imgMp3.setVisibility(View.VISIBLE);
            }
            if(ovaVijest.hasAttach()){
                imgText.setVisibility(View.VISIBLE);
                imgText.setOnClickListener(VijestActivity.this);
            }
            if(ovaVijest.hasLink()){
                imgLink.setVisibility(View.VISIBLE);
                imgLink.setOnClickListener(VijestActivity.this);
            }

            if(VijestActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                if(ovaVijest.hasImage()){
                    if(imageLoader.isInited()){
                        imageLoader.displayImage(ovaVijest.getImage().getUrl640(), headerImage, imageLoaderConfigurator.doConfig(false));
                    }
                } else {
                    String url = prefs.getString("hr.bpervan.novaeva.categoryheader." + ovaVijest.getKategorija(), null);
                    if(url != null){
                        if(imageLoader.isInited()){
                            imageLoader.displayImage(url, headerImage, imageLoaderConfigurator.doConfig(true));
                        }
                    }
                }
            }
            if(ovaVijest.getKategorija() == Constants.CAT_POZIV){
                //setQuestionButton();
            }

            tvNaslov.setText(ovaVijest.getNaslov());
            vijestWebView.reload();
            vijestWebView.loadDataWithBaseURL(null, ovaVijest.getTekst(), "text/html", "UTF-8", null);

        }

        @Override
        public void onStart(){
            progressDialog.show();
        }

        @Override
        public void onFinish(){
            progressDialog.dismiss();
        }
    }
}
