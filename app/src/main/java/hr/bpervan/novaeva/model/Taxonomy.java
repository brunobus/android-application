package hr.bpervan.novaeva.model;

import java.util.List;

/**
 * Created by Branimir on 28.4.2017..
 */

public class Taxonomy {

    public int APIstatus;
    public int rezultat;
    public List<Subcat> subcat;
    public Image image;
    public int paket;
    public int jos;
    public List<TaxonomyArticleElement> vijesti;

    public class Subcat {
        public int cid;
        public String name;

        @Override
        public String toString() {
            return "Subcat{" +
                    "cid=" + cid +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Taxonomy{" +
                "APIstatus=" + APIstatus +
                ", rezultat=" + rezultat +
                ", subcat=" + subcat +
                ", image=" + image +
                ", paket=" + paket +
                ", jos=" + jos +
                ", vijesti=" + vijesti +
                '}';
    }
}
