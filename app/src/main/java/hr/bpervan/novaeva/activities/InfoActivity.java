package hr.bpervan.novaeva.activities;

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		
		initUI();
	}
	
	private void initUI(){
		webView = (WebView) findViewById(R.id.webViewInfo);
		
		webView.loadUrl("file:///android_asset/info.htm");

		View fakeActionBar = findViewById(R.id.fakeActionBar);
		fakeActionBar.findViewById(R.id.btnBack).setOnClickListener(this);
		fakeActionBar.findViewById(R.id.btnSearch).setOnClickListener(this);
		fakeActionBar.findViewById(R.id.btnHome).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnSearch:
			showSearchPopup();
			break;
		case R.id.btnHome:
			startActivity(new Intent(InfoActivity.this,DashboardActivity.class));
			break;
		case R.id.btnBack:
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
