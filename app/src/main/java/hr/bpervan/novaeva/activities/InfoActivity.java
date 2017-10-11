package hr.bpervan.novaeva.activities;

import hr.bpervan.novaeva.NovaEvaApp;
import hr.bpervan.novaeva.main.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.EditText;

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
        int id = v.getId();
        if (id == R.id.btnSearch) {
            showSearchPopup();

        } else if (id == R.id.btnHome) {
			NovaEvaApp.Companion.goHome(this);

        } else if (id == R.id.btnBack) {
            onBackPressed();

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
				NovaEvaApp.Companion.goSearch(search, InfoActivity.this);
			}
		});
		search.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {}
			});
		search.show();
	}
}
