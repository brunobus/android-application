package hr.bpervan.novaeva.utilities;

import android.text.Html;

public class Vijest {
	
	private int nid, kategorija;
	
	private String datum, naslov, uvod, tekst;
	
	private String link, audio;
	private Attachment[] attachments;
	private Image image;
	
	private ListTypes vrstaPodatka;
	private BookmarkTypes vrstaZaBookmark;
	
	public Vijest(){}
	
	/** 
	 * @param nid - Unique 'vijest' identifier
	 * @param kategorija - Cid
	 * @param datum - Unix timestamp
	 * @param naslov - Naslov
	 * @param uvod - Pocetak teksta
	 * @param tekst - Cijela vijest
	 * @param attach - 
	 * */
	public Vijest(int nid, int kategorija, 
			String datum, String naslov, String uvod, String tekst, 
			Attachment[] attach, String link, String audio, Image image){
		this.setNid(nid);
		this.setKategorija(kategorija);
		this.setDatum(datum);
		this.setNaslov(naslov);
		this.setTekst(tekst);
		this.setAttach(attach);
		this.setLink(link);
		this.setAudio(audio);
		this.setImage(image);
		
		
		/** OVO */
		if(uvod != null){
			this.setUvod(uvod);
		} else {
			this.setUvod(stripHtml(tekst));
		}
		
	}
	
	private String stripHtml(String html) {
	    return Html.fromHtml(html).toString();
	}

	public int getNid() {return nid;}
	public void setNid(int nid) {this.nid = nid;}
	public int getKategorija() {return kategorija;}
	public void setKategorija(int kategorija) {this.kategorija = kategorija;}
	public String getNaslov() {return naslov;}
	public void setNaslov(String naslov) {this.naslov = naslov;}
	public String getTekst() {return tekst;}
	public void setTekst(String tekst) {this.tekst = tekst;}
	public String getDatum() {return datum;}
	public void setDatum(String datum) {this.datum = datum;}
	public String getUvod() {return uvod;}
	public void setUvod(String uvod) {this.uvod = uvod;}
	public String getLink() {return link;}
	public void setLink(String link) {this.link = link;}
	public String getAudio() {return audio;}
	public void setAudio(String audio) {this.audio = audio;}
	public Image getImage() {return image;}
	public void setImage(Image image) {this.image = image;}
	public Attachment[] getAttach() {return attachments;}
	public void setAttach(Attachment[] attachments) {this.attachments = attachments;}
	public ListTypes getVrstaPodatka() {return vrstaPodatka;}
	public void setVrstaPodatka(ListTypes vrstaPodatka) {this.vrstaPodatka = vrstaPodatka;}
	public BookmarkTypes getVrstaZaBookmark() {return vrstaZaBookmark;}
	public void setVrstaZaBookmark(BookmarkTypes vrstaZaBookmark) {this.vrstaZaBookmark = vrstaZaBookmark;}
	public boolean hasImage(){
		if(this.image != null){
			return this.image.getUrl640() != null;
		}
		return false;
	}
	public boolean hasLink(){return this.link != null;}
	public boolean hasAudio(){return this.audio != null;}
	public boolean hasAttach(){
		if(this.attachments == null)
			return false;
		if(this.attachments.length == 0)
			return false;
		return true;
	}
	
	public String getFirstAttachmentNaslov(){
		if(hasAttach()){
			return this.attachments[0].getNaziv();
		}
		return null;
	}
	
	public String getFirstAttachmentUrl(){
		if(hasAttach()){
			return this.attachments[0].getUrl();
		}
		return null;
	}
}
