package hr.bpervan.novaeva.utilities;

import hr.bpervan.novaeva.main.R;

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

public class VijestAdapter extends BaseAdapter implements OnClickListener {
	
    private Context context;
    private List<ListElement> listVijest;
    private TextView tvDatum, tvNaslov, tvUvod, tvGodinaSatMinuta;
    private TextView tvMapaNatpis, tvUvodNatpis;
    
    private ImageView imgHasLink, imgHasTxt, imgHasAudio;
    private Calendar cal;
    
    private Typeface openSansBold, openSansItalic, openSansLight, openSansRegular;
    
    private ResourceHandler resourceHandler;
    
    int duljina = 0;
    
    /**
     * 
     * @param context Application context
     * @param listVijest List of Vijest objects
     * @param customFontNaslov Custom font used for field 'Naslov'
     * @param customFontUvod Custom font used for field 'Uvod'
     * @param customFontGodina Custom font used for field 'Godina'
     * @param customFontDatum Custom font used for field 'Datum'
     * @param customFontSatMinuta Custom font used for field 'Vrijeme'
     */
    public VijestAdapter(Context context, List<ListElement> listVijest, 
    		Typeface openSansBold, Typeface openSansItalic, Typeface openSansLight, Typeface openSansRegular,
    		int kategorija) {
        this.context = context;
        this.listVijest = listVijest;
        this.cal = Calendar.getInstance();
        
        this.openSansBold = openSansBold;
        this.openSansItalic = openSansItalic;
        this.openSansLight = openSansLight;
        this.openSansRegular = openSansRegular;
        
        this.resourceHandler = new ResourceHandler(kategorija);
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
        	convertView.setBackgroundResource(resourceHandler.getResourceId(ListTypes.PODKATEGORIJA,
        			context.getResources().getConfiguration().orientation));
        }
        else{
        	convertView = inflater.inflate(R.layout.vijest_row, null);
        	convertView.setBackgroundResource(resourceHandler.getResourceId(ListTypes.VIJEST,
        			context.getResources().getConfiguration().orientation));
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
        	if(openSansBold != null){
        		tvNaslov.setTypeface(openSansBold);
        		tvMapaNatpis.setTypeface(openSansBold);
        	}
        	tvNaslov.setText(entry.getNaslov());   	
        } else {
            if(openSansBold != null){
            	tvNaslov.setTypeface(openSansBold);
            	tvUvodNatpis.setTypeface(openSansBold);//null pointer? xD
            	
            }      	
            if(openSansItalic != null){
            	tvUvod.setTypeface(openSansItalic);
            }      	
            if(openSansLight != null){
            	tvDatum.setTypeface(openSansLight);
            }     	
            if(openSansRegular != null){
            	tvGodinaSatMinuta.setTypeface(openSansRegular);
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
