package hr.bpervan.novaeva.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.BaseAdapter
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import kotlinx.android.synthetic.main.molitvenik_row.view.*

class MolitvenikAdapter(private val context: Context, private val listVijest: List<String>) : BaseAdapter(), OnClickListener {

    override fun getCount(): Int = listVijest.size

    override fun getItem(position: Int): Any = listVijest[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, parentConvertView: View?, parentViewGroup: ViewGroup): View {

        val entry = listVijest[position]

        val convertView: View = parentConvertView ?:
                LayoutInflater.from(context).inflate(R.layout.molitvenik_row, parentViewGroup, false)

        val tvContact = convertView.tvMolitva
        val openSansBold = NovaEvaApp.openSansBold
        if (openSansBold != null) {
            tvContact.typeface = openSansBold
        }
        tvContact.text = entry

        return convertView
    }

    override fun onClick(view: View) {
        //Vijest entry = (Vijest) view.getTag();
        //listVijest.remove(entry);
        // notifyDataSetChanged();
    }

}
