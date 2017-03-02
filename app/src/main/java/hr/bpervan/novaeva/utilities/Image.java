package hr.bpervan.novaeva.utilities;

public class Image {
	
	private String url640;
	private String url720;
	private String date;
	private String urlOriginal;
	
	public Image(String url640, String url720, String date, String urlOriginal){
		this.setUrl640(url640);
		this.setUrl720(url720);
		this.setDate(date);
		this.setUrlOriginal(urlOriginal);
	}

	public String getUrl640() {return url640;}
	public void setUrl640(String url640) {this.url640 = url640;}

	public String getUrl720() {return url720;}
	public void setUrl720(String url720) {this.url720 = url720;}

	public String getDate() {return date;}
	public void setDate(String date) {this.date = date;}

	public String getUrlOriginal() {return urlOriginal;}
	public void setUrlOriginal(String urlOriginal) {this.urlOriginal = urlOriginal;}

}
