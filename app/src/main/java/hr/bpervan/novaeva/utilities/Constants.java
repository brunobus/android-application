package hr.bpervan.novaeva.utilities;

import java.util.ArrayList;
import java.util.List;

public class Constants {
	
	public static final int CAT_DUHOVNOST = 354;
	public static final int CAT_AKTUALNO = 9;
	public static final int CAT_IZREKE = 1;
	public static final int CAT_MULTIMEDIJA = 10;
	public static final int CAT_EVANDJELJE = 4;
	public static final int CAT_PROPOVJEDI = 7;
	public static final int CAT_POZIV = 8;
	public static final int CAT_ODGOVORI = 11;
	public static final int CAT_PJESMARICA = 355;
	
	//Deprecated
	public static final int CAT_PAPA = 2; 
	public static final int CAT_SM = 18;
	
	public static final String CAT_DUHOVNOST_NAZIV = "Duhovnost";
	public static final String CAT_AKTUALNO_NAZIV = "Aktualno";
	public static final String CAT_IZREKE_NAZIV = "Izreke";
	public static final String CAT_MULTIMEDIJA_NAZIV = "Multimedija";
	public static final String CAT_EVANDJELJE_NAZIV = "Evanđelje";
	public static final String CAT_PROPOVJEDI_NAZIV = "Propovijedi";
	public static final String CAT_POZIV_NAZIV = "Poziv";
	public static final String CAT_ODGOVORI_NAZIV = "Odgovori";
	public static final String CAT_PJESMARICA_NAZIV = "Pjesmarica";
	
	public static final String CAT_DUHOVNOST_NAZIV_PORT = "Duhovnost";
	public static final String CAT_AKTUALNO_NAZIV_PORT = "Aktualno";
	public static final String CAT_IZREKE_NAZIV_PORT = "Izreke";
	public static final String CAT_MULTIMEDIJA_NAZIV_PORT = "Multimedija";
	public static final String CAT_EVANDJELJE_NAZIV_PORT = "Evanđelje";
	public static final String CAT_PROPOVJEDI_NAZIV_PORT = "Propovijedi";
	public static final String CAT_POZIV_NAZIV_PORT = "Poziv";
	public static final String CAT_ODGOVORI_NAZIV_PORT = "Odgovori";
	public static final String CAT_PJESMARICA_NAZIV_PORT = "Pjesmarica";
	
	public static final String CAT_MOLITVENIK_NAZIV_PORT = "Molitvenik";
	public static final String CAT_BREVIJAR_NAZIV_PORT = "Brevijar";
	public static final String CAT_INFO_NAZIV_PORT = "Informacije";
	public static final String CAT_BOOKMARKS_NAZIV_PORT = "Zabilješke";
	
	public static final String CAT_DUHOVNOST_NAZIV_LAND = "D\nu\nh\no\nv\nn\no\ns\nt";
	public static final String CAT_AKTUALNO_NAZIV_LAND = "A\nk\nt\nu\na\nl\nn\no";
	public static final String CAT_IZREKE_NAZIV_LAND = "I\nz\nr\ne\nk\ne";
	public static final String CAT_MULTIMEDIJA_NAZIV_LAND = "M\nu\nl\nt\ni\nm\ne\nd\ni\nj\na";
	public static final String CAT_EVANDJELJE_NAZIV_LAND = "E\nv\na\nn\nđ\ne\nlj\n";
	public static final String CAT_PROPOVJEDI_NAZIV_LAND = "P\nr\no\np\no\nv\ni\nj\ne\nd\ni";
	public static final String CAT_POZIV_NAZIV_LAND = "P\no\nz\ni\nv";
	public static final String CAT_ODGOVORI_NAZIV_LAND = "O\nd\ng\no\nv\no\nr\ni";
	public static final String CAT_PJESMARICA_NAZIV_LAND = "P\nj\ne\ns\nm\na\nr\ni\nc\na";
	
	public static final String CAT_MOLITVENIK_NAZIV_LAND = "M\no\nl\ni\nt\nv\ne\nn\ni\nk";
	public static final String CAT_BREVIJAR_NAZIV_LAND = "B\nr\ne\nv\ni\nj\na\nr";
	public static final String CAT_INFO_NAZIV_LAND = "I\nn\nf\no\nr\nm\na\nc\ni\nj\ne";
	public static final String CAT_BOOKMARKS_NAZIV_LAND = "Z\na\nb\ni\nlj\ne\nđ\nk\ne";
	
	public static final String APP_NAME = "Nova Eva";
	public static final String APP_NAME_LAND = "N\no\nv\na\n \nE\nv\na";

	public static final String searchURL = "http://novaeva.com/json?api=2&search=";
	public static final String alertURL = "http://novaeva.com/json?api=2&alert=1";
	
	public static String getCatNameById(long id){
		if(id == CAT_AKTUALNO)
			return CAT_AKTUALNO_NAZIV;
		else if(id == CAT_EVANDJELJE)
			return CAT_EVANDJELJE_NAZIV;
		else if(id == CAT_ODGOVORI)
			return CAT_ODGOVORI_NAZIV;
		else if(id == CAT_POZIV)
			return CAT_POZIV_NAZIV;
		else if(id == CAT_PROPOVJEDI)
			return CAT_PROPOVJEDI_NAZIV;
		else if(id == CAT_DUHOVNOST)
			return CAT_DUHOVNOST_NAZIV;
		else if(id == CAT_PJESMARICA)
			return CAT_PJESMARICA_NAZIV;
		else if(id == CAT_MULTIMEDIJA)
			return CAT_MULTIMEDIJA_NAZIV;
		else if(id == CAT_IZREKE)
			return CAT_IZREKE_NAZIV;
		else if(id == CAT_DUHOVNOST)
			return CAT_DUHOVNOST_NAZIV;
		else if(id == CAT_PJESMARICA)
			return CAT_PJESMARICA_NAZIV;
		else
			return "";
	}
	public static String getCatNameById(String id){
		return getCatNameById(Integer.parseInt(id));
	}
	
	public static List<Integer> getIntCategoryList(boolean includeEvandjelje){
		List<Integer> listaKategorija = new ArrayList<Integer>();
		
		listaKategorija.add(Constants.CAT_AKTUALNO);
        if(includeEvandjelje){
            listaKategorija.add(Constants.CAT_EVANDJELJE);
        }

		listaKategorija.add(Constants.CAT_ODGOVORI);
		listaKategorija.add(Constants.CAT_POZIV);
		listaKategorija.add(Constants.CAT_PROPOVJEDI);
		listaKategorija.add(Constants.CAT_DUHOVNOST);
		listaKategorija.add(Constants.CAT_MULTIMEDIJA);
		listaKategorija.add(Constants.CAT_PJESMARICA);
		listaKategorija.add(Constants.CAT_IZREKE);

		return listaKategorija;
	}
	
	public static List<String> getCategoryList(){
		List<String> listaKategorija = new ArrayList<String>();
		
		listaKategorija.add(String.valueOf(Constants.CAT_AKTUALNO));
		listaKategorija.add(String.valueOf(Constants.CAT_EVANDJELJE));
		listaKategorija.add(String.valueOf(Constants.CAT_ODGOVORI));
		listaKategorija.add(String.valueOf(Constants.CAT_POZIV));
		listaKategorija.add(String.valueOf(Constants.CAT_PROPOVJEDI));
		listaKategorija.add(String.valueOf(Constants.CAT_DUHOVNOST));
		listaKategorija.add(String.valueOf(Constants.CAT_MULTIMEDIJA));
		listaKategorija.add(String.valueOf(Constants.CAT_PJESMARICA));
		listaKategorija.add(String.valueOf(Constants.CAT_IZREKE));

		return listaKategorija;
	}
}
