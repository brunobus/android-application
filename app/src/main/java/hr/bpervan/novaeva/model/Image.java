package hr.bpervan.novaeva.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Branimir on 29.4.2017..
 */

public class Image {
    @SerializedName("640")
    public String size640;
    @SerializedName("720")
    public String size720;
    public int date;
    public String original;

    @Override
    public String toString() {
        return "Image{" +
                "size640='" + size640 + '\'' +
                ", size720='" + size720 + '\'' +
                ", date=" + date +
                ", original='" + original + '\'' +
                '}';
    }
}
