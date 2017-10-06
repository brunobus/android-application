package hr.bpervan.novaeva.model;


/**
 * Created by Branimir on 29.4.2017..
 */

public class TaxonomyArticleElement {

    public Attachments attach;
    public int nid;
    public String datum;
    public String naslov;
    public String text;

    public class Attachments {
        public boolean video;
        public boolean documents;
        public boolean music;
        public boolean images;
        public boolean text;

        @Override
        public String toString() {
            return "Attachments{" +
                    "video=" + video +
                    ", documents=" + documents +
                    ", music=" + music +
                    ", images=" + images +
                    ", text=" + text +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "TaxonomyArticleElement{" +
                "attach=" + attach +
                ", nid=" + nid +
                ", datum='" + datum + '\'' +
                ", naslov='" + naslov + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
