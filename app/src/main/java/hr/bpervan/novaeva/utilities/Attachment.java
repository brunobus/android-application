package hr.bpervan.novaeva.utilities;

public class Attachment{
	
	private String naziv;
	private String url;
	
	public Attachment(String naziv, String url){
		this.naziv = naziv;
		this.url = url;
	}
	
	public void setNaziv(String naziv){this.naziv = naziv;}
	public void setUrl(String url){this.url = url;}
	public String getNaziv(){return this.naziv;}
	public String getUrl(){return this.url;}
}