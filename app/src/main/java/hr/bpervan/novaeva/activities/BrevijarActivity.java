package hr.bpervan.novaeva.activities;

import hr.bpervan.novaeva.NovaEvaApp;
import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.utilities.ImageLoaderConfigurator;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class BrevijarActivity extends Activity implements OnClickListener{
	
	private static final String BREV_CAT = "BREV_CAT";
	private Button btnJucerJutarnja,btnJucerVecernja,btnJucerPovecerje,btnDanasJutarnja,btnDanasVecernja,btnDanasPovecerje,
					btnSutraJutarnja,btnSutraVecernja,btnSutraPovecerje;
	
	private ImageView headerImageBrevijar;
	private TextView txtKs, txtLaudato;
	
	private TextView imgDanas;
	
	private SharedPreferences prefs;
	
	//private DisplayImageOptions options;
	private ImageLoader imageLoader;
	private ImageLoaderConfigurator imageLoaderConfigurator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("hr.bpervan.novaeva", MODE_PRIVATE);

        imageLoaderConfigurator = new ImageLoaderConfigurator(this);
        imageLoader = ImageLoader.getInstance();
        if(!imageLoader.isInited()){
            imageLoaderConfigurator.doInit();
        }

        initUI();
	}

    private void initUI(){
        setContentView(R.layout.activity_brevijar);

        txtKs = (TextView) findViewById(R.id.txtKs);
        txtLaudato = (TextView) findViewById(R.id.txtLaudato);

        Typeface openSansRegular = NovaEvaApp.Companion.getOpenSansRegular();
        if (openSansRegular != null) {
            txtKs.setTypeface(openSansRegular);
            txtLaudato.setTypeface(openSansRegular);
        }
        imgDanas = (TextView) findViewById(R.id.imgDanas);
        SimpleDateFormat datum = new SimpleDateFormat("dd.MM.yyyy");
        imgDanas.setText(datum.format(new Date()));

        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            headerImageBrevijar = (ImageView) findViewById(R.id.headerImageBrevijar);
        }

        String headerUrl = prefs.getString("hr.bpervan.novaeva.brevijarheaderimage", null);
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            if(headerUrl != null){
                if(imageLoader.isInited()){
                    imageLoader.displayImage(headerUrl, headerImageBrevijar, imageLoaderConfigurator.doConfig(true));
                }
            }
        }

        btnJucerJutarnja = (Button) findViewById(R.id.btnJucerJutarnja);
        btnJucerVecernja = (Button) findViewById(R.id.btnJucerVecernja);
        btnJucerPovecerje = (Button) findViewById(R.id.btnJucerPovecerje);
        btnDanasJutarnja = (Button) findViewById(R.id.btnDanasJutarnja);
        btnDanasVecernja = (Button) findViewById(R.id.btnDanasVecernja);
        btnDanasPovecerje = (Button) findViewById(R.id.btnDanasPovecerje);
        btnSutraJutarnja = (Button) findViewById(R.id.btnSutraJutarnja);
        btnSutraVecernja = (Button) findViewById(R.id.btnSutraVecernja);
        btnSutraPovecerje = (Button) findViewById(R.id.btnSutraPovecerje);

        btnJucerJutarnja.setOnClickListener(this);
        btnJucerVecernja.setOnClickListener(this);
        btnJucerPovecerje.setOnClickListener(this);
        btnDanasJutarnja.setOnClickListener(this);
        btnDanasVecernja.setOnClickListener(this);
        btnDanasPovecerje.setOnClickListener(this);
        btnSutraJutarnja.setOnClickListener(this);
        btnSutraVecernja.setOnClickListener(this);
        btnSutraPovecerje.setOnClickListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);

        initUI();
    }

	@Override
	public void onClick(View arg0) {
		Intent intent = new Intent(BrevijarActivity.this,BrevijarDetaljiActivity.class);
		switch(arg0.getId()){
		case R.id.btnJucerJutarnja:
			intent.putExtra(BREV_CAT, 1);
			break;
		case R.id.btnJucerVecernja:
			intent.putExtra(BREV_CAT, 2);
			break;
		case R.id.btnJucerPovecerje:
			intent.putExtra(BREV_CAT, 3);
			break;
		case R.id.btnDanasJutarnja:
			intent.putExtra(BREV_CAT, 4);
			break;
		case R.id.btnDanasVecernja:
			intent.putExtra(BREV_CAT, 5);
			break;
		case R.id.btnDanasPovecerje:
			intent.putExtra(BREV_CAT, 6);
			break;
		case R.id.btnSutraJutarnja:
			intent.putExtra(BREV_CAT, 7);
			break;
		case R.id.btnSutraVecernja:
			intent.putExtra(BREV_CAT, 8);
			break;
		case R.id.btnSutraPovecerje:
			intent.putExtra(BREV_CAT, 9);
			break;
		}
		this.startActivity(intent);
	}
}
