package hr.bpervan.novaeva.utilities;

@Deprecated
public class ListElement {
	
	private String uvod, naslov, datum;
	private int nid;
	private int kategorija;
	private boolean hasVideo, hasDocuments, hasMusic, hasImages, hasText;
	private ListTypes listType;
	
	public ListElement(){}
	
	public ListElement(String uvod, String naslov, String datum,
			int nid, int kategorija, ListTypes listType,
			boolean hasVideo, boolean hasDocuments, boolean hasMusic,
			boolean hasImages, boolean hasText){
		this.uvod = uvod;
		this.naslov = naslov;
		this.datum = datum;
		this.nid = nid;
		this.kategorija = kategorija;
		
		this.hasDocuments = hasDocuments;
		this.hasImages = hasImages;
		this.hasMusic = hasMusic;
		this.hasText = hasText;
		this.hasVideo = hasVideo;
		this.listType = listType;
	}

	public ListTypes getListType(){
		return this.listType;
	}
	
	public void setListType(ListTypes listType){
		this.listType = listType;
	}
	
	public ListElement setNaslov(String naslov){
		this.naslov = naslov;
		return this;
	}
	
	public String getNaslov(){
		return this.naslov;
	}
	
	public ListElement setUvod(String uvod){
		this.uvod = uvod;
		return this;
	}
	
	public String getUvod(){
		return this.uvod;
	}
	
	public ListElement setNid(int nid){
		this.nid = nid;
		return this;
	}
	
	public int getNid(){
		return this.nid;
	}
	
	public String getSatMinuta(){
		return null;
	}
	
	public String getGodina(){
		return null;
	}
	
	public ListElement setUnixDatum(String datum){
		this.datum = datum;
		return this;
	}
	
	public String getUnixDatum(){
		return this.datum;
	}
	
	public boolean hasMusic(){
		return this.hasMusic;
	}
	public boolean hasDocuments(){
		return this.hasDocuments;
	}
	public boolean hasText(){
		return this.hasText;
	}
	public boolean hasVideo(){
		return this.hasVideo;
	}
	public boolean hasImages(){
		return this.hasImages;
	}
	
	public void itHasMusic(){
		this.hasMusic = true;
	}
	public void itHasDocuments(){
		this.hasDocuments = true;
	}
	public void itHasText(){
		this.hasText = true;
	}
	public void itHasVideo(){
		this.hasVideo = true;
	}
	public void itHasImages(){
		this.hasImages = true;
	}

	public int getKategorija() {
		return kategorija;
	}

	public void setKategorija(int kategorija) {
		this.kategorija = kategorija;
	}
	
}
