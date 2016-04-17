package hr.bpervan.novaeva.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import hr.bpervan.novaeva.NovaEvaApp;
import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.utilities.Attachment;
import hr.bpervan.novaeva.utilities.BookmarkTypes;
import hr.bpervan.novaeva.utilities.BookmarksDBHandlerV2;
import hr.bpervan.novaeva.utilities.ConnectionChecker;
import hr.bpervan.novaeva.utilities.Constants;
import hr.bpervan.novaeva.utilities.Image;
import hr.bpervan.novaeva.utilities.ImageLoaderConfigurator;
import hr.bpervan.novaeva.utilities.ListTypes;
import hr.bpervan.novaeva.utilities.ResourceHandler;
import hr.bpervan.novaeva.utilities.Vijest;

public class VijestActivity extends Activity implements 
		View.OnClickListener, 
		MediaPlayer.OnCompletionListener, 
		SeekBar.OnSeekBarChangeListener {
	
	private static final String TAG = "MediaPlayerDebug";
	
	/** Main display engine */
	private WebView vijestWebView;
	
	/** Used for displaying Title and category name */
	private TextView tvNaslov;
	
	private ImageView btnHome, btnFace, btnMail, btnBookmark, btnSearch, btnTextPlus, btnBack;
		
	private ImageView imgMp3, imgLink, imgText;
	private ImageView headerImage;
	private ImageView imgNaslovnaTraka;
	private RelativeLayout fakeActionBar;
	//private LockableScrollView scrollView1;
    private ScrollView scrollView1;

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
	
	/** Google analytics API*/
	private Tracker mGaTracker;

	/** Database handler for bookmarks, resource handler for category colours*/
	private BookmarksDBHandlerV2 dbHandler;
	private ResourceHandler resourceHandler;
	
	/** Custom font resources*/
	private static Typeface openSansBold, openSansLight, openSansRegular;
	
	/** Image loader*/
	private ImageLoader imageLoader;
	private ImageLoaderConfigurator imageLoaderConfigurator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbHandler = new BookmarksDBHandlerV2(this);
		nid = getIntent().getIntExtra("nid",0);
		
		prefs = getSharedPreferences("hr.bpervan.novaeva", MODE_PRIVATE);
		openSansBold = Typeface.createFromAsset(getAssets(), "opensans-bold.ttf");
		//openSansLight = Typeface.createFromAsset(getAssets(), "opensans-light.ttf");
		openSansRegular = Typeface.createFromAsset(getAssets(), "opensans-regular.ttf");

		/*mGaTracker = mGaInstance.getTracker("UA-40344870-1");
		mGaTracker.sendEvent("Vijesti", "OtvorenaVijest", nid + "", null);*/

        mGaTracker = ((NovaEvaApp) getApplication()).getTracker(NovaEvaApp.TrackerName.APP_TRACKER);
        mGaTracker.send(
                new HitBuilders.EventBuilder()
                        .setCategory("Vijesti")
                        .setAction("OtvorenaVijest")
                        .setLabel(nid + "")
                        .build()
        );

		resourceHandler = new ResourceHandler(this.getIntent().getIntExtra("kategorija", Constants.CAT_PROPOVJEDI));
        
		imageLoaderConfigurator = new ImageLoaderConfigurator(this);
		imageLoader = ImageLoader.getInstance();
		if(!imageLoader.isInited()){
			new ImageLoaderConfigurator(this).doInit();
		}
		initUI();
		
		if(!ConnectionChecker.hasConnection(this)){
			Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(VijestActivity.this,DashboardActivity.class));
		} else {
			new AsyncHttpPostTask(this).execute(getIntent().getIntExtra("nid",0) + "");
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		initUI();

		/** Test and set various attachments*/
		setAttachments();

        if(ovaVijest != null){
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
            vijestWebView.loadDataWithBaseURL(null, ovaVijest.getTekst(), "text/html", "utf-8", null);
        }
    }
	
	/**
	 * Initializes User interface, makes connections and references to
	 * widgets on User interface, sets category specified colour
	 */
	private void initUI(){
        setContentView(R.layout.activity_vijest);

        /** Basic data */
		tvNaslov = (TextView) findViewById(R.id.tvNaslov);
		if(openSansBold != null)
			tvNaslov.setTypeface(openSansBold);

		/** Lets try displaying news using WebView */
		vijestWebView = (WebView) findViewById(R.id.vijestWebView);
        vijestWebView.reload();
        if(ovaVijest != null){
            vijestWebView.loadDataWithBaseURL(null, ovaVijest.getTekst(), "text/html", "UTF-8", null);
        }

        vijestWebView.setFocusable(false);
        vijestWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.reload();
                view.loadDataWithBaseURL(null, ovaVijest.getTekst(), "text/html", "utf-8", null);
                return false;
            }
        });

		/** LayoutAlgorithm.SingleNešto is Deprecated */
        //vijestWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
		vijestWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);

        /* Prevent horizontal and vertical scrolling in webview */
        /*vijestWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });*/

        /* Prevent C/P */
        vijestWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        vijestWebView.setLongClickable(false);

		/** Duhovni poziv */
		btnPoziv = (ImageView) findViewById(R.id.btnPoziv);
		
		/** Is this 'Duhovni poziv' or 'Odgovori' category? */
		if(getIntent().getIntExtra("kategorija", Constants.CAT_PROPOVJEDI) == Constants.CAT_POZIV){
			setQuestionButton();
		}
		
		/** Controls for integrated simple media player */
		btnPlay = (ImageView) findViewById(R.id.btnPlay);
		btnPause = (ImageView) findViewById(R.id.btnPause);
		seekArc = (SeekBar) findViewById(R.id.seekArc);	
		seekArc.setOnSeekBarChangeListener(this);
		
		scrollView1 = (ScrollView) findViewById(R.id.scrollView1);
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
		
		/** Set category name and set text size*/
		vijestWebView.getSettings().setDefaultFontSize(prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14));

		this.setCategoryTypeColour();
		if(mPlayer != null){
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
		}
		
		if(dbHandler.nidExists(nid))
			btnBookmark.setImageResource(R.drawable.vijest_button_bookmarked);
		if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			headerImage = (ImageView) findViewById(R.id.headerImage);
		}
		getWindow().getDecorView().setBackgroundColor(this.getResources().getColor(android.R.color.background_light));
    }
	
	@Override
	protected void onPause(){
		super.onPause();
		if(mPlayer != null){
			if(mPlayer.isPlaying()){
				mPlayer.stop();
			}
			mPlayer.release();
			seekArc.removeCallbacks(onEverySecond);
		}
		mPlayer = null;
	}


    public void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    public void onStop(){
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
	
	@Override
	protected void onResume(){
		super.onResume();
        Log.d(TAG, "On resume, mplayer je " + ((mPlayer == null) ? "null" : "nijeNULL"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	private void setCategoryTypeColour(){
		int[] resources = {R.drawable.izbornik_navbgodgovori, R.drawable.vijest_naslovnaodgovori};
		switch(this.getResources().getConfiguration().orientation){
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
		switch(v.getId()){
		case R.id.btnPoziv:
			String text;
			String[] mail = new String[1];
			Intent i;
			switch (getIntent().getIntExtra("kategorija", 11)){
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
			if(!mPlayer.isPlaying()){	
				Log.d(TAG, "btnPlay");
				mPlayer.start();
				seekArc.postDelayed(onEverySecond, 1000);
				btnPlay.setVisibility(View.INVISIBLE);
				btnPause.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.btnPause:
            //TODO: mPlayer == Null pointer?
			if(mPlayer.isPlaying()){
				Log.d(TAG, "btnPlay");
				mPlayer.pause();
				seekArc.removeCallbacks(onEverySecond);
				btnPause.setVisibility(View.INVISIBLE);
				btnPlay.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.btnHome:
			startActivity(new Intent(this, DashboardActivity.class));
			break;
		case R.id.btnSearch:
			if(ConnectionChecker.hasConnection(VijestActivity.this))
				showSearchPopup();
			break;
		case R.id.btnBookmark:
			if(dbHandler.nidExists(nid)){
				dbHandler.deleteVijest(nid);
				btnBookmark.setImageResource(R.drawable.vijest_button_bookmark);
			} else {
				dbHandler.insertVijest(ovaVijest);
				btnBookmark.setImageResource(R.drawable.vijest_button_bookmarked);
			}
			break;
		case R.id.btnFace:
			CharSequence temp = "http://novaeva.com/node/" + getIntent().getIntExtra("nid", 1025);		
			Intent faceIntent = new Intent(Intent.ACTION_SEND);
			faceIntent.setType("text/plain");
			faceIntent.putExtra(Intent.EXTRA_TEXT,temp);
			startActivity(Intent.createChooser(faceIntent, "Facebook"));
			break;
		case R.id.btnMail:
			String temp2 = "http://novaeva.com/node/" + getIntent().getIntExtra("nid", 1025);
			Intent mailIntent = new Intent(Intent.ACTION_SEND);
			mailIntent.setType("message/rfc822");
			mailIntent.putExtra(Intent.EXTRA_SUBJECT, ovaVijest.getNaslov());
			mailIntent.putExtra(Intent.EXTRA_TEXT, temp2);
			startActivity(Intent.createChooser(mailIntent, "Odaberite aplikaciju"));
			break;
		case R.id.btnTextPlus:
			int mCurrentSize = prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14);
			mCurrentSize += 2;
			if(mCurrentSize >= 28){
				mCurrentSize = 12;
			}
				
			prefs.edit().putInt("hr.bpervan.novaeva.velicinateksta", mCurrentSize).commit();
			vijestWebView.getSettings().setDefaultFontSize(mCurrentSize);
			break;
		case R.id.btnBack:
			VijestActivity.this.onBackPressed();
			break;
		case R.id.imgLink:
			if(ovaVijest.hasLink()){
				startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(ovaVijest.getLink())));
			}
			break;
		case R.id.imgText:
			if(ovaVijest.hasAttach()){
				startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, Uri.parse(ovaVijest.getFirstAttachmentUrl())), 
						"Otvaranje dokumenta " +  ovaVijest.getFirstAttachmentNaslov()));
			}
			break;
		}
	}
	
	private void showSearchPopup(){
		AlertDialog.Builder search = new AlertDialog.Builder(this);
		search.setTitle("Pretraga");
		final EditText et = new EditText(this);
		search.setView(et);	
		search.setPositiveButton("Pretraži", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String search=et.getText().toString();
				Intent i=new Intent(VijestActivity.this,SearchActivity.class);	
				i.putExtra("searchString", search);
				startActivity(i);
			}
		});
		search.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {}});
		search.show();
	}
	
	private void setQuestionButton(){
		btnPoziv.setVisibility(View.VISIBLE);
		btnPoziv.setOnClickListener(VijestActivity.this);
	}

	private void setAttachments(){
        if(ovaVijest != null){
            if(ovaVijest.hasAudio()){
                //imgMp3.setVisibility(View.VISIBLE);
				imgMp3.setImageResource(R.drawable.vijest_ind_mp3_active);
            }
            if(ovaVijest.hasLink()){
                //imgLink.setVisibility(View.VISIBLE);
				imgLink.setImageResource(R.drawable.vijest_ind_www_active);
                imgLink.setOnClickListener(this);
            }
            if(ovaVijest.hasAttach()){
                //imgText.setVisibility(View.VISIBLE);
				imgText.setImageResource(R.drawable.vijest_ind_txt_active);
                imgText.setOnClickListener(this);
            }
        }
	}
	
	@Override
	public void onProgressChanged(SeekBar seekArc, int progress,
			boolean fromUser) {
		if(mPlayer != null && fromUser){
			mPlayer.seekTo(progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekArc) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekArc) {}


	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.d(TAG, "onCompletion");
		btnPlay.setVisibility(View.VISIBLE);
		btnPause.setVisibility(View.INVISIBLE);
		
		seekArc.setProgress(0);
		tvElapsed.setText("00:00");
		mPlayer.seekTo(0);
	}

	private Runnable onEverySecond = new Runnable(){
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
	};

	private void showErrorPopup(){
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
				new AsyncHttpPostTask(VijestActivity.this).execute(ovaVijest.getNid() + "");
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
	
	private class AsyncHttpPostTask extends AsyncTask<String, Void, Void>{
		private String URL = null;
		private ProgressDialog pDialog;
		
		private Context context;
		
		private InputStream is = null;
		private JSONObject jObj = null;
		private String json = null;
		private JSONArray attachArray = null;
		private Attachment[] prilozi = null;
		private Image image = null;

		public AsyncHttpPostTask(Context context){
			this.URL = "http://novaeva.com/json?api=2&nid=";
			
			this.context = context;
			
			this.pDialog = new ProgressDialog(context);
	        this.pDialog.setMessage("Učitavam...");
	        this.pDialog.setIndeterminate(true);
	        this.pDialog.setCancelable(false);
            this.pDialog.setCanceledOnTouchOutside(false);
	        this.pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Odustani", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					
				}
			});
	        
			pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					AsyncHttpPostTask.this.cancel(true);
				}
			});
		}

        @Override
        protected void onCancelled(){
            VijestActivity.this.onBackPressed();
        }
		
		@Override
		protected void onPreExecute(){
            super.onPreExecute();
            pDialog.show();
        }

		@Override
		protected Void doInBackground(String... params) {
			this.URL = URL + params[0];
			try{
                HttpParams httpParams = new BasicHttpParams();
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
				HttpClient httpClient = new DefaultHttpClient(httpParams);

				HttpPost httpPost = new HttpPost(this.URL);

				HttpResponse httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntitiy = httpResponse.getEntity();
				is = httpEntitiy.getContent();
			}catch(IOException e){
				//TODO: tu bi moglo bit svašta xD
				VijestActivity.this.runOnUiThread( new Runnable() {
					public void run(){
						showErrorPopup();
					}
				});
				this.cancel(true);
			}
			
			try{
				BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while((line = reader.readLine()) != null){
					sb.append(line + "\n");
				}
				is.close();
				json = sb.toString();
			}catch(Exception e){
				VijestActivity.this.runOnUiThread( new Runnable() {
					public void run(){
						showErrorPopup();
					}
				});
				this.cancel(true);
			}
			try{
				/*tekst = "<!DOCTYPE html>";
				tekst += "<head>";
				tekst += "<style type=\"text/css\"> " + "@font-face {font-family: \"OpenSansRegular\"; src: url('opensans-regular.ttf');}" + 
						"body {font-family: 'OpenSansRegular', Verdana, sans-serif;} " +
						"</style>";
				tekst += "</head>";
				tekst += "<body>";*/
				
				jObj = new JSONObject(json);
				if(!jObj.getString("prilozi").equals("null")){
					attachArray = jObj.getJSONArray("prilozi");
					prilozi = new Attachment[attachArray.length()];
					Log.d("attachArray.length()",attachArray.length()+"");
					for(int i = 0; i < attachArray.length(); ++i){
						prilozi[i] = new Attachment(attachArray.getJSONObject(i).getString("naziv"), attachArray.getJSONObject(i).getString("url"));
					}
				}

				/** Samo za sad će biti da image ide direkno, inače ispitivati screen size itd*/
				ovaVijest = new Vijest(jObj.getInt("nid"), Integer.parseInt(jObj.getString("cid")), 
						jObj.getString("time"), jObj.getString("naslov"), null, jObj.getString("tekst"), 
						prilozi, null, null, null);


				ovaVijest.setVrstaZaBookmark(BookmarkTypes.VIJEST);
				ovaVijest.setVrstaPodatka(ListTypes.VIJEST);
				if(!jObj.getString("youtube").equals("null")){
					ovaVijest.setLink(jObj.getString("youtube"));
				}
				if(!jObj.getString("audio").equals("null")){
					ovaVijest.setAudio(jObj.getString("audio"));
				}
				if(!jObj.getString("image").equals("null")){
					JSONObject imageField = jObj.getJSONArray("image").getJSONObject(0);
					image = new Image(imageField.getString("640"), imageField.getString("720"), imageField.getString("date"), imageField.getString("original"));
					ovaVijest.setImage(image);
				}
				//tekst += "</body></html>";

                if(ovaVijest.hasAudio()){
                    VijestActivity.this.isMediaPlayerLoading = true;
                    mPlayer = new MediaPlayer();
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.setDataSource(ovaVijest.getAudio());
                    mPlayer.prepare();

                    mPlayer.setOnCompletionListener(VijestActivity.this);
                }
			}catch(Exception e){
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void param){
            super.onPostExecute(param);
			if(ovaVijest.hasAudio()){
				//imgMp3.setVisibility(View.VISIBLE);
				imgMp3.setImageResource(R.drawable.vijest_ind_mp3_active);
                //TODO: TEST integrity 15.12.2014
                Log.d(TAG, "onPrepared()");
                mp3Player.setVisibility(View.VISIBLE);
                btnPlay.setOnClickListener(VijestActivity.this);
                btnPause.setOnClickListener(VijestActivity.this);

                int seconds = (int) (mPlayer.getDuration() / 1000) % 60 ;
                int minutes = (int) ((mPlayer.getDuration() / (1000*60)) % 60);

                tvDuration.setText(String.format("%02d:%02d", minutes, seconds));
                tvElapsed.setText("00:00");

                seekArc.setMax(mPlayer.getDuration());
                VijestActivity.this.isMediaPlayerLoading = false;
			}
			if(ovaVijest.hasAttach()){
				//imgText.setVisibility(View.VISIBLE);
				imgText.setImageResource(R.drawable.vijest_ind_txt_active);
				imgText.setOnClickListener(VijestActivity.this);
			}
			if(ovaVijest.hasLink()){
				//imgLink.setVisibility(View.VISIBLE);
				imgLink.setImageResource(R.drawable.vijest_ind_www_active);
				imgLink.setOnClickListener(VijestActivity.this);
			}

			if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
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
				setQuestionButton();
			}

			tvNaslov.setText(ovaVijest.getNaslov());
            vijestWebView.reload();
            vijestWebView.loadDataWithBaseURL(null, ovaVijest.getTekst(), "text/html", "UTF-8", null);
            pDialog.dismiss();
		}
	}
}