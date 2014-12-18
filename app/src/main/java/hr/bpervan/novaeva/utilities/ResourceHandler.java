package hr.bpervan.novaeva.utilities;

import hr.bpervan.novaeva.main.R;
import android.content.res.Configuration;

/** 
 * Class which handles requiremens for various colour types based on a different categories
 * for ListaVijestiActivity / VijestAdapter or VijestActivity
 * @author Branimir
 * */
public class ResourceHandler {
	
	private int kategorija;
	
	/** 
	 * @author Branimir
	 * @params Orientation -> ORIENTATION_LANDSCAPE or ORIENTATION_PORTRAIT or Throws InvalidParamException
	 * 		   Kategorija -> Constants.KATEGORIJA or Throws InvalidParamException
	 * 
	 * @throws InvalidParamException
	 * */
	public ResourceHandler(int kategorija){
		this.kategorija = kategorija;
		
		/** OVO JE WORKAROUND I NEMRE OVAK
		 * 	Treba pri samom činu bookmarkiranja utrpat colourset a ne kategoriju
		 * */
		if(!Constants.getIntCategoryList(true).contains(kategorija)){
			this.kategorija = Constants.CAT_PROPOVJEDI;
		}
	}
	
	public int getKategorija(){
		return this.kategorija;
	}
	
	
	/** 
	 * Used by VijestActivity object for displaying title bar in appropriate colour. To avoid multiple
	 * switch - case blocks, this method may have same code as other methods in this class
	 * @author Branimir
	 * @return Array of 2 integers [0] -> FakeActionBar, [1] -> TitleBar
	 * */
	public int[] getVijestResource(int orientation){
		int resources[] = new int[2];
		if(orientation == Configuration.ORIENTATION_LANDSCAPE){
			switch(kategorija){
			case Constants.CAT_AKTUALNO:
				resources[0] = R.drawable.vijest_navbgaktualno_land;
				resources[1] = R.drawable.vijest_naslovnaaktualno_land;
				break;
			case Constants.CAT_DUHOVNOST:
			case Constants.CAT_SM:
				resources[0] = R.drawable.vijest_navbgduhovnost_land;
				resources[1] = R.drawable.vijest_naslovnaduhovnost_land;
				break;
			case Constants.CAT_EVANDJELJE:
				resources[0] = R.drawable.vijest_navbgevandjelje_land;
				resources[1] = R.drawable.vijest_naslovnaevandjelje_land;
				break;
			case Constants.CAT_IZREKE:
				resources[0] = R.drawable.vijest_navbgizreke_land;
				resources[1] = R.drawable.vijest_naslovnaizreke_land;
				break;
			case Constants.CAT_MULTIMEDIJA:
				resources[0] = R.drawable.vijest_navbgmultimedija_land;
				resources[1] = R.drawable.vijest_naslovnamultimedija_land;
				break;
			case Constants.CAT_ODGOVORI:
				resources[0] = R.drawable.vijest_navbgodgovori_land;
				resources[1] = R.drawable.vijest_naslovnaodgovori_land;
				break;
			case Constants.CAT_PJESMARICA:
			case Constants.CAT_PAPA:
				resources[0] = R.drawable.vijest_navbgmp3_land;
				resources[1] = R.drawable.vijest_naslovnamp3_land;
				break;
			case Constants.CAT_POZIV:
				resources[0] = R.drawable.vijest_navbgpoziv_land;
				resources[1] = R.drawable.vijest_naslovnapoziv_land;
				break;
			case Constants.CAT_PROPOVJEDI:
				resources[0] = R.drawable.vijest_navbgpropovjedi_land;
				resources[1] = R.drawable.vijest_naslovnapropovjedi_land;
				break;
			}
		} else {
			switch(kategorija){
			case Constants.CAT_AKTUALNO:
				resources[0] = R.drawable.izbornik_navbgaktualno;
				resources[1] = R.drawable.vijest_naslovnaaktualno;
				break;
			case Constants.CAT_DUHOVNOST:
			case Constants.CAT_SM:
				resources[0] = R.drawable.izbornik_navbgduhovnost;
				resources[1] = R.drawable.vijest_naslovnaduhovnost;
				break;
			case Constants.CAT_EVANDJELJE:
				resources[0] = R.drawable.izbornik_navbgevandjelje;
				resources[1] = R.drawable.vijest_naslovnaevandjelje;
				break;
			case Constants.CAT_IZREKE:
				resources[0] = R.drawable.izbornik_navbgizreke;
				resources[1] = R.drawable.vijest_naslovnaizreke;
				break;
			case Constants.CAT_MULTIMEDIJA:
				resources[0] = R.drawable.izbornik_navbgmultimedija;
				resources[1] = R.drawable.vijest_naslovnamultimedija;
				break;
			case Constants.CAT_ODGOVORI:
				resources[0] = R.drawable.izbornik_navbgodgovori;
				resources[1] = R.drawable.vijest_naslovnaodgovori;
				break;
			case Constants.CAT_PJESMARICA:
			case Constants.CAT_PAPA:
				resources[0] = R.drawable.izbornik_navbgmp3;
				resources[1] = R.drawable.vijest_naslovnamp3;
				break;
			case Constants.CAT_POZIV:
				resources[0] = R.drawable.izbornik_navbgpoziv;
				resources[1] = R.drawable.vijest_naslovnapoziv;
				break;
			case Constants.CAT_PROPOVJEDI:
				resources[0] = R.drawable.izbornik_navbgpropovjedi;
				resources[1] = R.drawable.vijest_naslovnapropovjedi;
				break;
			}
		}
		
		
		return resources;
	}

    public int getListViewHeader(int orientation){
        int resourceId = R.drawable.izbornik_top_odgovori;
        if(orientation == Configuration.ORIENTATION_LANDSCAPE){
            switch(kategorija){
                case Constants.CAT_AKTUALNO:
                    resourceId = R.drawable.izbornik_top_aktualno_land;
                    break;
                case Constants.CAT_DUHOVNOST:
                    resourceId = R.drawable.izbornik_top_duhovnost_land;
                    break;
                case Constants.CAT_EVANDJELJE:
                    resourceId = R.drawable.izbornik_top_evandjelje_land;
                    break;
                case Constants.CAT_MULTIMEDIJA:
                    resourceId = R.drawable.izbornik_top_multimedija_land;
                    break;
                case Constants.CAT_ODGOVORI:
                    resourceId = R.drawable.izbornik_top_odgovori_land;
                    break;
                case Constants.CAT_PJESMARICA:
                    resourceId = R.drawable.izbornik_top_mp3_land;
                    break;
                case Constants.CAT_POZIV:
                    resourceId = R.drawable.izbornik_top_poziv_land;
                    break;
                case Constants.CAT_PROPOVJEDI:
                    resourceId = R.drawable.izbornik_top_propovijedi_land;
                    break;
            }
        } else {
            switch(kategorija){
                case Constants.CAT_AKTUALNO:
                    resourceId = R.drawable.izbornik_top_aktualno;
                    break;
                case Constants.CAT_DUHOVNOST:
                    resourceId = R.drawable.izbornik_top_duhovnost;
                    break;
                case Constants.CAT_EVANDJELJE:
                    resourceId = R.drawable.izbornik_top_evandjelje;
                    break;
                case Constants.CAT_MULTIMEDIJA:
                    resourceId = R.drawable.izbornik_top_multimedija;
                    break;
                case Constants.CAT_ODGOVORI:
                    resourceId = R.drawable.izbornik_top_odgovori;
                    break;
                case Constants.CAT_PJESMARICA:
                    resourceId = R.drawable.izbornik_top_mp3;
                    break;
                case Constants.CAT_POZIV:
                    resourceId = R.drawable.izbornik_top_poziv;
                    break;
                case Constants.CAT_PROPOVJEDI:
                    resourceId = R.drawable.izbornik_top_propovjedi;
                    break;
            }
        }
        return resourceId;
    }
	
	/** 
	 * Used by ListaVijestiActivity object. 
	 * Returns Id from R class defining background colour for FakeActionBar
	 * @author Branimir
	 * @return Resource id from R.java directly usable in View.setBackgroundDrawable
	 * */
	public int getResourceId(int orientation){
		int resourceId = R.drawable.izbornik_navbgodgovori;
		if(orientation == Configuration.ORIENTATION_LANDSCAPE){
			switch(kategorija){
			case Constants.CAT_AKTUALNO:
				resourceId = R.drawable.vijest_navbgaktualno_land;
				break;
			case Constants.CAT_DUHOVNOST:
			case Constants.CAT_SM:
				resourceId = R.drawable.vijest_navbgduhovnost_land;
				break;
			case Constants.CAT_EVANDJELJE:
				resourceId = R.drawable.vijest_navbgevandjelje_land;
				break;
			case Constants.CAT_IZREKE:
				resourceId = R.drawable.vijest_navbgizreke_land;
				break;
			case Constants.CAT_MULTIMEDIJA:
				resourceId = R.drawable.vijest_navbgmultimedija_land;
				break;
			case Constants.CAT_ODGOVORI:
				resourceId = R.drawable.vijest_navbgodgovori_land;
				break;
			case Constants.CAT_PJESMARICA:
			case Constants.CAT_PAPA:
				resourceId = R.drawable.vijest_navbgmp3_land;
				break;
			case Constants.CAT_POZIV:
				resourceId = R.drawable.vijest_navbgpoziv_land;
				break;
			case Constants.CAT_PROPOVJEDI:
				resourceId = R.drawable.vijest_navbgpropovjedi_land;
				break;
			}
		} else {
			switch(kategorija){
			case Constants.CAT_AKTUALNO:
				resourceId = R.drawable.izbornik_navbgaktualno;
				break;
			case Constants.CAT_DUHOVNOST:
			case Constants.CAT_SM:
				resourceId = R.drawable.izbornik_navbgduhovnost;
				break;
			case Constants.CAT_EVANDJELJE:
				resourceId = R.drawable.izbornik_navbgevandjelje;
				break;
			case Constants.CAT_IZREKE:
				resourceId = R.drawable.izbornik_navbgizreke;
				break;
			case Constants.CAT_MULTIMEDIJA:
				resourceId = R.drawable.izbornik_navbgmultimedija;
				break;
			case Constants.CAT_ODGOVORI:
				resourceId = R.drawable.izbornik_navbgodgovori;
				break;
			case Constants.CAT_PJESMARICA:
			case Constants.CAT_PAPA:
				resourceId = R.drawable.izbornik_navbgmp3;
				break;
			case Constants.CAT_POZIV:
				resourceId = R.drawable.izbornik_navbgpoziv;
				break;
			case Constants.CAT_PROPOVJEDI:
				resourceId = R.drawable.izbornik_navbgpropovjedi;
				break;
			}
		}
		return resourceId;
	}
	
	/** 
	 * Used by VijestAdapter object
	 * @author Branimir
	 * @param ListTypes - 'PODKATEGORIJA' or 'VIJEST'
	 * @return Concrete resource id from R.java directly usable in View.setBackgroundDrawable
	 * */
	public int getResourceId(ListTypes listType, int orientation){
		int resourceId = R.drawable.izbornik_btn_normal_odgovori;
		if(listType == ListTypes.PODKATEGORIJA){
			if(orientation == Configuration.ORIENTATION_LANDSCAPE){
				switch(kategorija){
				case Constants.CAT_AKTUALNO:
					resourceId = R.drawable.izbornik_btn_normal_aktualno_folder_land;
					break;
				case Constants.CAT_DUHOVNOST:
				case Constants.CAT_SM:
					resourceId = R.drawable.izbornik_btn_normal_duhovnosti_folder_land;
					break;
				case Constants.CAT_EVANDJELJE:
					resourceId = R.drawable.izbornik_btn_normal_evandjelje_folder_land;
					break;
				case Constants.CAT_MULTIMEDIJA:
					resourceId = R.drawable.izbornik_btn_normal_multimedija_folder_land;
					break;
				case Constants.CAT_ODGOVORI:
					resourceId = R.drawable.izbornik_btn_normal_odgovori_folder_land;
					break;
				case Constants.CAT_PJESMARICA:
				case Constants.CAT_PAPA:
					resourceId = R.drawable.izbornik_btn_normal_mp3_folder_land;
					break;
				case Constants.CAT_POZIV:
					resourceId = R.drawable.izbornik_btn_normal_poziv_folder_land;
					break;
				case Constants.CAT_PROPOVJEDI:
					resourceId = R.drawable.izbornik_btn_normal_propovijedi_folder_land;
					break;
				default:
					resourceId = R.drawable.izbornik_btn_normal_odgovori_folder_land;
					break;
				}
			}else{
				switch(kategorija){
				case Constants.CAT_AKTUALNO:
					resourceId = R.drawable.izbornik_btn_normal_aktualno_folder;
					break;
				case Constants.CAT_DUHOVNOST:
				case Constants.CAT_SM:
					resourceId = R.drawable.izbornik_btn_normal_duhovnosti_folder;
					break;
				case Constants.CAT_EVANDJELJE:
					resourceId = R.drawable.izbornik_btn_normal_evandjelje_folder;
					break;
				case Constants.CAT_MULTIMEDIJA:
					resourceId = R.drawable.izbornik_btn_normal_multimedija_folder;
					break;
				case Constants.CAT_ODGOVORI:
					resourceId = R.drawable.izbornik_btn_normal_odgovori_folder;
					break;
				case Constants.CAT_PJESMARICA:
				case Constants.CAT_PAPA:
					resourceId = R.drawable.izbornik_btn_normal_mp3_folder;
					break;
				case Constants.CAT_POZIV:
					resourceId = R.drawable.izbornik_btn_normal_poziv_folder;
					break;
				case Constants.CAT_PROPOVJEDI:
					resourceId = R.drawable.izbornik_btn_normal_propovijedi_folder;
					break;
				default:
					resourceId = R.drawable.izbornik_btn_normal_odgovori_folder;
					break;
				}
			}
		} else {
			if(orientation == Configuration.ORIENTATION_LANDSCAPE){
				switch(kategorija){
				case Constants.CAT_AKTUALNO:
					resourceId = R.drawable.izbornik_btn_normal_aktualno_land;
					break;
				case Constants.CAT_DUHOVNOST:
				case Constants.CAT_SM:
					resourceId = R.drawable.izbornik_btn_normal_duhovnosti_land;
					break;
				case Constants.CAT_EVANDJELJE:
					resourceId = R.drawable.izbornik_btn_normal_evandjelje_land;
					break;
				case Constants.CAT_MULTIMEDIJA:
					resourceId = R.drawable.izbornik_btn_normal_multimedija_land;
					break;
				case Constants.CAT_ODGOVORI:
					resourceId = R.drawable.izbornik_btn_normal_odgovori_land;
					break;
				case Constants.CAT_PJESMARICA:
				case Constants.CAT_PAPA:
					resourceId = R.drawable.izbornik_btn_normal_mp3_land;
					break;
				case Constants.CAT_POZIV:
					resourceId = R.drawable.izbornik_btn_normal_poziv_land;
					break;
				case Constants.CAT_PROPOVJEDI:
					resourceId = R.drawable.izbornik_btn_normal_propovijedi_land;
					break;
				default:
					resourceId = R.drawable.izbornik_btn_normal_odgovori_land;
					break;
				}
			}else{
				switch(kategorija){
				case Constants.CAT_AKTUALNO:
					resourceId = R.drawable.izbornik_btn_normal_aktualno;
					break;
				case Constants.CAT_DUHOVNOST:
				case Constants.CAT_SM:
					resourceId = R.drawable.izbornik_btn_normal_duhovnosti;
					break;
				case Constants.CAT_EVANDJELJE:
					resourceId = R.drawable.izbornik_btn_normal_evandjelje;
					break;
				case Constants.CAT_MULTIMEDIJA:
					resourceId = R.drawable.izbornik_btn_normal_multimedija;
					break;
				case Constants.CAT_ODGOVORI:
					resourceId = R.drawable.izbornik_btn_normal_odgovori;
					break;
				case Constants.CAT_PJESMARICA:
				case Constants.CAT_PAPA:
					resourceId = R.drawable.izbornik_btn_normal_mp3;
					break;
				case Constants.CAT_POZIV:
					resourceId = R.drawable.izbornik_btn_normal_poziv;
					break;
				case Constants.CAT_PROPOVJEDI:
					resourceId = R.drawable.izbornik_btn_normal_propovijedi;
					break;
				default:
					resourceId = R.drawable.izbornik_btn_normal_odgovori;
					break;
				}
			}
		}
		return resourceId;
	}
}
