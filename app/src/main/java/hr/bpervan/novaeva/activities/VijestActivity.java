package hr.bpervan.novaeva.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.ImageLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import hr.bpervan.novaeva.NovaEvaApp;
import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.model.Article;
import hr.bpervan.novaeva.services.BackgroundPlayerService;
import hr.bpervan.novaeva.services.NovaEvaService;
import hr.bpervan.novaeva.utilities.BookmarksDBHandlerV2;
import hr.bpervan.novaeva.utilities.ConnectionChecker;
import hr.bpervan.novaeva.utilities.Constants;
import hr.bpervan.novaeva.utilities.ImageLoaderConfigurator;
import hr.bpervan.novaeva.utilities.ResourceHandler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class VijestActivity extends Activity implements
        View.OnClickListener,
        MediaPlayer.OnCompletionListener,
        SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "VijestActivity";
    /** ------------------------------------------FIELDS------------------------------------------*/
    /**
     * Main display engine
     */
    @BindView(R.id.vijestWebView)
    WebView vijestWebView;

    /**
     * Used for displaying Title and category name
     */
    @BindView(R.id.tvNaslov)
    TextView tvNaslov;

    @BindView(R.id.btnHome)
    ImageView btnHome;
    @BindView(R.id.btnFace)
    ImageView btnFace;
    @BindView(R.id.btnMail)
    ImageView btnMail;
    @BindView(R.id.btnBookmark)
    ImageView btnBookmark;
    @BindView(R.id.btnSearch)
    ImageView btnSearch;
    @BindView(R.id.btnTextPlus)
    ImageView btnTextPlus;
    @BindView(R.id.btnBack)
    ImageView btnBack;

    @BindView(R.id.imgMp3)
    ImageView imgMp3;
    @BindView(R.id.imgLink)
    ImageView imgLink;
    @BindView(R.id.imgText)
    ImageView imgText;

    ImageView headerImage;

    @BindView(R.id.imgNaslovnaTraka)
    ImageView imgNaslovnaTraka;
    @BindView(R.id.fakeActionBar)
    RelativeLayout fakeActionBar;
    @BindView(R.id.scrollView1)
    ScrollView scrollView1;

    /**
     * Used if category == 8, displays 'Poziv' button
     */
    @BindView(R.id.btnPoziv)
    ImageView btnPoziv;

    /**
     * Simple integrated audio streaming player
     */
    @BindView(R.id.mp3Player)
    RelativeLayout mp3Player;
    @BindView(R.id.tvElapsed)
    TextView tvElapsed;
    @BindView(R.id.tvDuration)
    TextView tvDuration;
    @BindView(R.id.seekArc)
    SeekBar seekBar;
    @BindView(R.id.btnPlay)
    ImageView btnPlay;
    @BindView(R.id.btnPause)
    ImageView btnPause;

    /**
     * Used to store Font size
     */
    private SharedPreferences prefs;

    /**
     * Pure 'Vijest' data
     */
    private int nid;
    //private Vijest ovaVijest;

    /**
     * Google analytics API
     */
    private Tracker mGaTracker;

    /**
     * Database handler for bookmarks, resource handler for category colours
     */
    private BookmarksDBHandlerV2 dbHandler;
    private ResourceHandler resourceHandler;

    /**
     * Custom font resources
     */
    private static Typeface openSansBold, openSansRegular;

    /**
     * Image loader
     */
    private ImageLoader imageLoader;
    private ImageLoaderConfigurator imageLoaderConfigurator;

    /**
     * Retrofit
     */
    private Article thisArticle;
    private ProgressDialog progressDialog;

    /**
     * Exoplayer
     * */
    private SimpleExoPlayer exoPlayer;
    @BindView(R.id.player_view)
    SimpleExoPlayerView exoPlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHandler = new BookmarksDBHandlerV2(this);
        nid = getIntent().getIntExtra("nid", 0);

        startService(new Intent(this, BackgroundPlayerService.class));

        prefs = getSharedPreferences("hr.bpervan.novaeva", MODE_PRIVATE);
        openSansBold = Typeface.createFromAsset(getAssets(), "opensans-bold.ttf");
        openSansRegular = Typeface.createFromAsset(getAssets(), "opensans-regular.ttf");

        mGaTracker = ((NovaEvaApp) getApplication()).getTracker(NovaEvaApp.TrackerName.APP_TRACKER);
        mGaTracker.send(
                new HitBuilders.EventBuilder()
                        .setCategory("Vijesti")
                        .setAction("OtvorenaVijest")
                        .setLabel(nid + "")
                        .build()
        );

        resourceHandler = new ResourceHandler(this.getIntent().getIntExtra("kategorija", Constants.CAT_PROPOVJEDI));

        this.progressDialog = new ProgressDialog(this);
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
            }
        });
        progressDialog.show();

        imageLoaderConfigurator = new ImageLoaderConfigurator(this);
        imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()) {
            new ImageLoaderConfigurator(this).doInit();
        }
        initUI();

		/*if(!ConnectionChecker.hasConnection(this)){
            Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(VijestActivity.this,DashboardActivity.class));
		} else {
			new AsyncHttpPostTask(this).execute(getIntent().getIntExtra("nid",0) + "");
		}*/
    }

    private void streamAudio(String audioUri){
        int RENDER_COUNT = 1;
        int minBufferMs = 1000;
        int minRebufferMs = 5000;

        Uri streamingUri = Uri.parse(audioUri);

        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                        this,
                        Util.getUserAgent(this, getResources().getString(R.string.app_name)),
                        bandwidthMeter);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource mediaSource = new ExtractorMediaSource(streamingUri, dataSourceFactory, extractorsFactory, 1, new Handler(), null, audioUri);

        exoPlayer.prepare(mediaSource);
    }

    private void createPlayer(){
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory factory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(factory);

        this.exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        exoPlayerView.requestFocus();
        exoPlayerView.setPlayer(exoPlayer);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initUI();

        /** Test and set various attachments*/
        setAttachments();

        if (thisArticle != null) {
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (thisArticle.hasImage()) {
                    if (imageLoader.isInited()) {
                        imageLoader.displayImage(thisArticle.image.size640, headerImage, imageLoaderConfigurator.doConfig(false));
                    }
                } else {
                    String url = prefs.getString("hr.bpervan.novaeva.categoryheader." + thisArticle.cid, null);
                    if (url != null) {
                        if (imageLoader.isInited()) {
                            imageLoader.displayImage(url, headerImage, imageLoaderConfigurator.doConfig(true));
                        }
                    }
                }
            }
            vijestWebView.reload();
            vijestWebView.loadDataWithBaseURL(null, thisArticle.tekst, "text/html", "utf-8", null);
        }
    }

    /**
     * Initializes User interface, makes connections and references to
     * widgets on User interface, sets category specified colour
     */
    private void initUI() {
        setContentView(R.layout.activity_vijest);
        ButterKnife.bind(this);

        /** Basic data */
        if (openSansBold != null)
            tvNaslov.setTypeface(openSansBold);

        /** Lets try displaying news using WebView */
        vijestWebView.reload();
        if (thisArticle != null) {
            vijestWebView.loadDataWithBaseURL(null, thisArticle.tekst, "text/html", "UTF-8", null);
        }

        vijestWebView.setFocusable(false);
        vijestWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.reload();
                view.loadDataWithBaseURL(null, thisArticle.tekst, "text/html", "utf-8", null);
                return false;
            }
        });

        vijestWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);

        /* Prevent C/P */
        vijestWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        vijestWebView.setLongClickable(false);

        /** Is this 'Duhovni poziv' or 'Odgovori' category? */
        if (getIntent().getIntExtra("kategorija", Constants.CAT_PROPOVJEDI) == Constants.CAT_POZIV) {
            setQuestionButton();
        }

        seekBar.setOnSeekBarChangeListener(this);
        tvDuration.setTypeface(openSansRegular);
        tvElapsed.setTypeface(openSansRegular);

        btnHome.setOnClickListener(this);
        btnFace.setOnClickListener(this);
        btnMail.setOnClickListener(this);
        btnBookmark.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        btnTextPlus.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        /** Set category name and set text size*/
        vijestWebView.getSettings().setDefaultFontSize(prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14));

        this.setCategoryTypeColour();
        /*if(mPlayer != null){
			mp3Player.setVisibility(View.VISIBLE);
			btnPlay.setOnClickListener(VijestActivity.this);
			btnPause.setOnClickListener(VijestActivity.this);
			
			seekArc.setMax(mPlayer.getDuration());
			
            int seconds = (int) (mPlayer.getDuration() / 1000) % 60 ;
            int minutes = (int) ((mPlayer.getDuration() / (1000 * 60)) % 60);
            
            if(mPlayer.isPlaying()){
            	seekArc.setProgress(mPlayer.getCurrentPosition());
				btnPlay.setVisibility(View.INVISIBLE);
				btnPause.setVisibility(View.VISIBLE);
            }
            
            tvDuration.setText(String.format("%02d:%02d", minutes, seconds));
            
            tvElapsed.setText(String.format("%02d:%02d", 
            		(int) (mPlayer.getCurrentPosition() / 1000) % 60, 
            		(int) ((mPlayer.getCurrentPosition() / (1000 * 60)) % 60)));
		}*/

        if (dbHandler.nidExists(nid))
            btnBookmark.setImageResource(R.drawable.vijest_button_bookmarked);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            headerImage = (ImageView) findViewById(R.id.headerImage);
        }
        getWindow().getDecorView().setBackgroundColor(this.getResources().getColor(android.R.color.background_light));
    }

    Disposable disposable;

    @Override
    protected void onResume() {
        //LocalBroadcastManager.getInstance(this).registerReceiver(serviceMessageReceiver, new IntentFilter(BackgroundPlayerService.INTENT_CLASS));
        super.onResume();

        disposable = NovaEvaService.Companion.getInstance()
                .getArticle(getIntent().getIntExtra("nid", 0))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Article>() {
                    @Override
                    public void accept(Article article) throws Exception {
                        VijestActivity.this.thisArticle = article;
                        if (VijestActivity.this.thisArticle.audio != null) {
                            imgMp3.setImageResource(R.drawable.vijest_ind_mp3_active);

                            VijestActivity.this.createPlayer();
                            VijestActivity.this.streamAudio(VijestActivity.this.thisArticle.audio);
                        }
                        if (VijestActivity.this.thisArticle.prilozi != null) {
                            imgText.setImageResource(R.drawable.vijest_ind_txt_active);
                            imgText.setOnClickListener(VijestActivity.this);
                        }
                        if (VijestActivity.this.thisArticle.youtube != null) {
                            imgLink.setImageResource(R.drawable.vijest_ind_www_active);
                            imgLink.setOnClickListener(VijestActivity.this);
                        }

                        if (VijestActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                            if (VijestActivity.this.thisArticle.image != null) {
                                if (imageLoader.isInited()) {
                                    imageLoader.displayImage(VijestActivity.this.thisArticle.image.size640, headerImage, imageLoaderConfigurator.doConfig(false));
                                }
                            } else {
                                String url = prefs.getString("hr.bpervan.novaeva.categoryheader." + VijestActivity.this.thisArticle.cid, null);
                                if (url != null) {
                                    if (imageLoader.isInited()) {
                                        imageLoader.displayImage(url, headerImage, imageLoaderConfigurator.doConfig(true));
                                    }
                                }
                            }
                        }
                        if (VijestActivity.this.thisArticle.cid == Constants.CAT_POZIV) {
                            setQuestionButton();
                        }

                        tvNaslov.setText(VijestActivity.this.thisArticle.naslov);
                        vijestWebView.reload();
                        vijestWebView.loadDataWithBaseURL(null, VijestActivity.this.thisArticle.tekst, "text/html", "UTF-8", null);
                        progressDialog.dismiss();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable t) throws Exception {
                        Log.e("vijestiError", t.getMessage(), t);
                    }
                });
    }

    @Override
    protected void onPause() {
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceMessageReceiver);
        super.onPause();
        disposable.dispose(); //// TODO: 07.10.17.


		/*if(mPlayer != null){
			if(mPlayer.isPlaying()){
				mPlayer.stop();
			}
			mPlayer.release();
			seekArc.removeCallbacks(onEverySecond);
		}
		mPlayer = null;*/
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    private void setCategoryTypeColour() {
        int[] resources = {R.drawable.izbornik_navbgodgovori, R.drawable.vijest_naslovnaodgovori};
        switch (this.getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                resources = resourceHandler.getVijestResource(Configuration.ORIENTATION_LANDSCAPE);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                resources = resourceHandler.getVijestResource(Configuration.ORIENTATION_PORTRAIT);
                break;
        }
        fakeActionBar.setBackgroundResource(resources[0]);
        imgNaslovnaTraka.setImageResource(resources[1]);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPoziv:
                String text;
                String[] mail = new String[1];
                Intent i;
                switch (getIntent().getIntExtra("kategorija", 11)) {
                    case Constants.CAT_POZIV:
                        text = "Hvaljen Isus i Marija, javljam Vam se jer razmišljam o duhovnom pozivu.";
                        mail[0] = "duhovnipoziv@gmail.com";
                        i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_SUBJECT, "Duhovni poziv");
                        i.putExtra(Intent.EXTRA_TEXT, text);
                        i.putExtra(Intent.EXTRA_EMAIL, mail);
                        startActivity(Intent.createChooser(i, "Odaberite aplikaciju"));
                        break;
                }
                break;
            case R.id.btnPlay:
                Log.d(TAG, "btnPlay");
                if (BackgroundPlayerService.isRunning) {
                    Intent messageIntent = new Intent(VijestActivity.this, BackgroundPlayerService.class);
                    messageIntent.putExtra(BackgroundPlayerService.KEY_DIRECTIVE, BackgroundPlayerService.DIRECTIVE_SET_SOURCE_PLAY);
                    messageIntent.putExtra(BackgroundPlayerService.KEY_PATH, thisArticle.audio);
                    messageIntent.putExtra(BackgroundPlayerService.KEY_TITLE, thisArticle.naslov);
                    startService(messageIntent);
                    btnPlay.setVisibility(View.INVISIBLE);
                    btnPause.setVisibility(View.VISIBLE);
                    btnPause.setEnabled(false);
                    Log.d(TAG, "Sent MSG_SET_SOURCE_AND_PLAY");
                }
                break;
            case R.id.btnPause:
                Log.d(TAG, "btnPlay");
                Intent pauseIntent = new Intent(VijestActivity.this, BackgroundPlayerService.class);
                pauseIntent.putExtra(BackgroundPlayerService.KEY_DIRECTIVE, BackgroundPlayerService.DIRECTIVE_PAUSE);
                startService(pauseIntent);
			/*if(mPlayer.isPlaying()){
				Log.d(TAG, "btnPlay");
				mPlayer.pause();
				seekArc.removeCallbacks(onEverySecond);
				btnPause.setVisibility(View.INVISIBLE);
				btnPlay.setVisibility(View.VISIBLE);
			}*/
                btnPause.setVisibility(View.INVISIBLE);
                btnPlay.setVisibility(View.VISIBLE);
                btnPlay.setEnabled(false);
                break;
            case R.id.btnHome:
                startActivity(new Intent(this, DashboardActivity.class));
                break;
            case R.id.btnSearch:
                if (ConnectionChecker.hasConnection(VijestActivity.this))
                    showSearchPopup();
                break;
            case R.id.btnBookmark:
                if (dbHandler.nidExists(nid)) {
                    dbHandler.deleteVijest(nid);
                    btnBookmark.setImageResource(R.drawable.vijest_button_bookmark);
                } else {
                    dbHandler.insertArticle(thisArticle);
                    btnBookmark.setImageResource(R.drawable.vijest_button_bookmarked);
                }
                break;
            case R.id.btnFace:
                CharSequence temp = "http://novaeva.com/node/" + getIntent().getIntExtra("nid", 1025);
                Intent faceIntent = new Intent(Intent.ACTION_SEND);
                faceIntent.setType("text/plain");
                faceIntent.putExtra(Intent.EXTRA_TEXT, temp);
                startActivity(Intent.createChooser(faceIntent, "Facebook"));
                break;
            case R.id.btnMail:
                String temp2 = "http://novaeva.com/node/" + getIntent().getIntExtra("nid", 1025);
                Intent mailIntent = new Intent(Intent.ACTION_SEND);
                mailIntent.setType("message/rfc822");
                mailIntent.putExtra(Intent.EXTRA_SUBJECT, thisArticle.naslov);
                mailIntent.putExtra(Intent.EXTRA_TEXT, temp2);
                startActivity(Intent.createChooser(mailIntent, "Odaberite aplikaciju"));
                break;
            case R.id.btnTextPlus:
                int mCurrentSize = prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14);
                mCurrentSize += 2;
                if (mCurrentSize >= 28) {
                    mCurrentSize = 12;
                }

                prefs.edit().putInt("hr.bpervan.novaeva.velicinateksta", mCurrentSize).commit();
                vijestWebView.getSettings().setDefaultFontSize(mCurrentSize);
                break;
            case R.id.btnBack:
                VijestActivity.this.onBackPressed();
                break;
            case R.id.imgLink:
                if (thisArticle.youtube != null) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(thisArticle.youtube)));
                }
                break;
            case R.id.imgText:
                if (thisArticle.prilozi != null) {
                    startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, Uri.parse(thisArticle.prilozi.get(0).url)),
                            "Otvaranje dokumenta " + thisArticle.prilozi.get(0).naziv));
                }
                break;
        }
    }

    private void showSearchPopup() {
        AlertDialog.Builder search = new AlertDialog.Builder(this);
        search.setTitle("Pretraga");
        final EditText et = new EditText(this);
        search.setView(et);
        search.setPositiveButton("Pretraži", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String search = et.getText().toString();
                Intent i = new Intent(VijestActivity.this, SearchActivity.class);
                i.putExtra("searchString", search);
                startActivity(i);
            }
        });
        search.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        search.show();
    }

    private void setQuestionButton() {
        btnPoziv.setVisibility(View.VISIBLE);
        btnPoziv.setOnClickListener(VijestActivity.this);
    }

    private void setAttachments() {
        if (thisArticle != null) {
            if (thisArticle.audio != null) {
                imgMp3.setImageResource(R.drawable.vijest_ind_mp3_active);
            }
            if (thisArticle.youtube != null) {
                imgLink.setImageResource(R.drawable.vijest_ind_www_active);
                imgLink.setOnClickListener(this);
            }
            if (thisArticle.prilozi != null) {
                imgText.setImageResource(R.drawable.vijest_ind_txt_active);
                imgText.setOnClickListener(this);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekArc, int progress,
                                  boolean fromUser) {
		/*if(mPlayer != null && fromUser){
			mPlayer.seekTo(progress);
		}*/
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekArc) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekArc) {
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion");
        btnPlay.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.INVISIBLE);

        seekBar.setProgress(0);
        tvElapsed.setText("00:00");
        //mPlayer.seekTo(0);
    }

	/*private Runnable onEverySecond = new Runnable(){
		@Override
		public void run() {
			if(mPlayer != null){
	            int seconds = (int) (mPlayer.getCurrentPosition() / 1000) % 60 ;
	            int minutes = (int) ((mPlayer.getCurrentPosition() / (1000*60)) % 60);

	            tvElapsed.setText(String.format("%02d:%02d", minutes, seconds));
				seekArc.setProgress(mPlayer.getCurrentPosition());
	        }
	        seekArc.postDelayed(this, 1000);
		}
	};*/

    private void showErrorPopup() {
        AlertDialog.Builder error = new AlertDialog.Builder(this);
        error.setTitle("Greška");

        final TextView tv = new TextView(this);
        tv.setText("Greška pri dohvaćanju podataka sa poslužitelja");
        tv.setTypeface(openSansRegular);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        error.setView(tv);

        error.setPositiveButton("Pokušaj ponovno", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //new AsyncHttpPostTask(VijestActivity.this).execute(ovaVijest.getNid() + "");
            }
        });
        error.setNegativeButton("Povratak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                startActivity(new Intent(VijestActivity.this, DashboardActivity.class));
                VijestActivity.this.onBackPressed();
            }
        });
        error.show();
    }
}