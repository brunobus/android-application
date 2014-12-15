package hr.bpervan.novaeva.utilities;

import hr.bpervan.novaeva.main.R;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MolitvenikAdapter extends BaseAdapter implements OnClickListener {
	
    private Context context;
    private List<String> listVijest;
    private Typeface openSansBold;
    
    public MolitvenikAdapter(Context context, List<String> listVijest, Typeface openSansBold){
    	this.context = context;
    	this.listVijest = listVijest;
    	this.openSansBold = openSansBold;
    }

    public MolitvenikAdapter(Context context, List<String> listVijest) {
        this.context = context;
        this.listVijest = listVijest;
    }

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
        String entry = listVijest.get(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.molitvenik_row, null);
        }
        TextView tvContact = (TextView) convertView.findViewById(R.id.tvMolitva);
        if(this.openSansBold != null){
        	tvContact.setTypeface(openSansBold);
        }
        tvContact.setText(entry);
        
        return convertView;
    }

    @Override
    public void onClick(View view) {
        //Vijest entry = (Vijest) view.getTag();
        //listVijest.remove(entry);
       // notifyDataSetChanged();
    }

}
