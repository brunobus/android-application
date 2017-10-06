package hr.bpervan.novaeva.model;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Branimir on 28.4.2017..
 */

public class Article {
    public int APIstatus;
    public int rezultat;
    public List<Attachment> prilozi;
    public Image image;
    public String tekst;
    public int nid;
    public int cid;
    public String naslov;
    public String youtube;
    public String audio;
    public String time;

    public boolean hasImage(){
        return (this.image != null) && (this.image.size640 != null);
    }


    public class Attachment {
        public String naziv;
        public String url;

        @Override
        public String toString() {
            return "Attachment{" +
                    "naziv='" + naziv + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Article{" +
                "APIstatus=" + APIstatus +
                ", rezultat=" + rezultat +
                ", prilozi=" + prilozi +
                ", image=" + image +
                ", tekst='" + tekst.substring(0, 50) + '\'' +
                ", nid=" + nid +
                ", cid=" + cid +
                ", naslov='" + naslov + '\'' +
                ", youtube='" + youtube + '\'' +
                ", audio='" + audio + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
