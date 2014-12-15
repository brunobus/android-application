package hr.bpervan.novaeva.v2;

import hr.bpervan.novaeva.DashboardActivity;
import hr.bpervan.novaeva.SearchActivity;
import hr.bpervan.novaeva.main.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;

public class InfoActivity extends Activity implements OnClickListener{
	
	private WebView webView;
	
	private ImageView btnBack, btnHome, btnSearch;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		
		initUI();
	}
	
	private void initUI(){
		webView = (WebView) findViewById(R.id.webViewInfo);
		
		webView.loadUrl("file:///android_asset/info.htm");
		
		btnBack = (ImageView) findViewById(R.id.btnBackListaVijesti);
		btnSearch = (ImageView) findViewById(R.id.btnSearchListaVijesti);
		btnHome = (ImageView) findViewById(R.id.btnHomeListaVijesti);
		
		btnBack.setOnClickListener(this);
		btnSearch.setOnClickListener(this);
		btnHome.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnSearchListaVijesti:
			showSearchPopup();
			break;
		case R.id.btnHomeListaVijesti:
			startActivity(new Intent(InfoActivity.this,DashboardActivity.class));
			break;
		case R.id.btnBackListaVijesti:
			InfoActivity.this.onBackPressed();
			break;
		}
	}
	
	private void showSearchPopup(){
		AlertDialog.Builder search = new AlertDialog.Builder(this);
		search.setTitle("Pretraga");
		
		final EditText et = new EditText(this);
		search.setView(et);
				
		search.setPositiveButton("Pretrazi", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String search = et.getText().toString();
				Intent i = new Intent(InfoActivity.this,SearchActivity.class);	
				i.putExtra("searchString", search);
				startActivity(i);
			}
		});
		search.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {}
			});
		search.show();
	}
}
