package hr.bpervan.novaeva.adapters;

import hr.bpervan.novaeva.NovaEvaApp;
import hr.bpervan.novaeva.main.R;
import hr.bpervan.novaeva.utilities.ListElement;
import hr.bpervan.novaeva.utilities.ListTypes;
import hr.bpervan.novaeva.utilities.ResourceHandler;

import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

@Deprecated
public class VijestAdapter extends BaseAdapter implements OnClickListener {
	
    private Context context;
    private List<ListElement> listVijest;
    private TextView tvDatum, tvNaslov, tvUvod, tvGodinaSatMinuta;
    private TextView tvMapaNatpis, tvUvodNatpis;
    
    private ImageView imgHasLink, imgHasTxt, imgHasAudio;
    private Calendar cal;

    private int kategorija;
    
    int duljina = 0;
    
    /**
     * 
     * @param context Application context
     * @param listVijest List of Vijest objects
     */
    public VijestAdapter(Context context, List<ListElement> listVijest, int kategorija) {
        this.context = context;
        this.listVijest = listVijest;
        this.cal = Calendar.getInstance();
        this.kategorija = kategorija;
    }
/*
    public VijestAdapter(Context context, List<ListElement> listVijest) {
        this.context = context;
        this.listVijest = listVijest;
        this.cal = Calendar.getInstance();
    }*/

    public int getCount() {
        return listVijest.size();
    }

    public Object getItem(int position) {
        return listVijest.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

	public View getView(int position, View convertView, ViewGroup viewGroup) {
        ListElement entry = listVijest.get(position);

    	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(entry.getListType() == ListTypes.PODKATEGORIJA){
        	convertView = inflater.inflate(R.layout.folder_row, null);
        	convertView.setBackgroundResource(ResourceHandler.INSTANCE.getDirectoryListItemResourceId(kategorija));
        }
        else{
        	convertView = inflater.inflate(R.layout.vijest_row, null);
        	convertView.setBackgroundResource(ResourceHandler.INSTANCE.getContentListItemResourceId(kategorija));
        }

        //TODO : ovo je trošenje resursa, zašto bi ja radio sva polja ako imam mapu i treba mi samo tvNaslov? :)
        tvNaslov = (TextView) convertView.findViewById(R.id.tvNaslov);
        tvUvod = (TextView) convertView.findViewById(R.id.tvUvod);
        tvDatum = (TextView) convertView.findViewById(R.id.tvDatum);
        tvGodinaSatMinuta = (TextView) convertView.findViewById(R.id.tvGodinaSatMinuta);
        
        imgHasLink = (ImageView) convertView.findViewById(R.id.imgViewLink);
        imgHasTxt = (ImageView) convertView.findViewById(R.id.imgViewTxt);
        imgHasAudio = (ImageView) convertView.findViewById(R.id.imgViewMp3);
        
        tvMapaNatpis = (TextView) convertView.findViewById(R.id.tvMapaNatpis);
        tvUvodNatpis = (TextView) convertView.findViewById(R.id.tvUvodNatpis);
        
        if(entry.getListType() == ListTypes.PODKATEGORIJA){
        	if(NovaEvaApp.Companion.getOpenSansBold() != null){
        		tvNaslov.setTypeface(NovaEvaApp.Companion.getOpenSansBold());
        		tvMapaNatpis.setTypeface(NovaEvaApp.Companion.getOpenSansBold());
        	}
        	tvNaslov.setText(entry.getNaslov());   	
        } else {
            if(NovaEvaApp.Companion.getOpenSansBold() != null){
            	tvNaslov.setTypeface(NovaEvaApp.Companion.getOpenSansBold());
            	tvUvodNatpis.setTypeface(NovaEvaApp.Companion.getOpenSansBold());//null pointer? xD
            	
            }      	
            if(NovaEvaApp.Companion.getOpenSansItalic() != null){
            	tvUvod.setTypeface(NovaEvaApp.Companion.getOpenSansItalic());
            }      	
            if(NovaEvaApp.Companion.getOpenSansLight() != null){
            	tvDatum.setTypeface(NovaEvaApp.Companion.getOpenSansLight());
            }     	
            if(NovaEvaApp.Companion.getOpenSansRegular() != null){
            	tvGodinaSatMinuta.setTypeface(NovaEvaApp.Companion.getOpenSansRegular());
            }
            
        	tvNaslov.setText(entry.getNaslov());
            cal.setTimeInMillis(1000 * Long.parseLong(entry.getUnixDatum()));
            
            String sat, minuta, godina, dan, mjesec;
            
            godina = String.valueOf(cal.get(Calendar.YEAR));
            //sat = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
            
            if(cal.get(Calendar.HOUR_OF_DAY) < 10){
            	sat = "0" + String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
            }else {
            	sat = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
            }
            
            if(cal.get(Calendar.MINUTE) < 10){
            	minuta = "0" + String.valueOf(cal.get(Calendar.MINUTE));
            }else {
            	minuta = String.valueOf(cal.get(Calendar.MINUTE));
            }
            
            //dan = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
            
            if(cal.get(Calendar.DAY_OF_MONTH) < 9){
            	dan = "0" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH) + 1);
            }else {
            	dan = String.valueOf(cal.get(Calendar.DAY_OF_MONTH) + 1);
            }
            
            if(cal.get(Calendar.MONTH) < 9){
            	mjesec = "0" + String.valueOf(cal.get(Calendar.MONTH) + 1);
            }else {
            	mjesec = String.valueOf(cal.get(Calendar.MONTH) + 1);
            }
            /** Pitanje je kako će se u APIu ovo mapirati youtube - video itd. */
            if(entry.hasVideo()){
            	imgHasLink.setVisibility(View.VISIBLE);
            }
            if(entry.hasMusic()){
            	imgHasAudio.setVisibility(View.VISIBLE);
            }
            if(entry.hasDocuments()){
            	imgHasTxt.setVisibility(View.VISIBLE);
            }
            
            tvGodinaSatMinuta.setText(godina + ", " + sat + ":" + minuta);
            tvDatum.setText(dan + "." + mjesec + ".");
            tvUvod.setText(entry.getUvod());
        } 
        return convertView;
    }

	//TODO: Prebaciti ovdje hendlanje klika?
    @Override
    public void onClick(View view) { }
}
