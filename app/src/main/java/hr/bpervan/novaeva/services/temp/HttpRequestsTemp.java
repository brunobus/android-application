package hr.bpervan.novaeva.services.temp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import hr.bpervan.novaeva.activities.ListaVijestiActivity;
import hr.bpervan.novaeva.utilities.ListElement;
import hr.bpervan.novaeva.utilities.ListTypes;

/**
 * Created by vpriscan on 07.10.17..
 */

@Deprecated
//TODO 1: MOVE OTHER HTTP STUFF FROM ACTIVITIES TO HERE
//TODO 2: USE RETROFIT2 INSTEAD OF EVERYTHING HERE
public class HttpRequestsTemp {
    public static @NonNull String getBreviar(String breviarCategory) throws Exception {
        String URL = "http://novaeva.com/json?api=2&brev=";

        InputStream is = null;
        JSONObject jObj = null;
        String json = null;

        String tempHTML;

        try{
            URL = URL + breviarCategory;
            HttpParams httpParams = new BasicHttpParams();
            httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpClient httpClient = new DefaultHttpClient(httpParams);

            HttpGet httpGet = new HttpGet(URL);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntitiy = httpResponse.getEntity();
            is = httpEntitiy.getContent();
        }catch(IOException e){
            Log.e("ioexception", e.getMessage(), e);
            throw e;
        }

        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null){
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        }catch(Exception e){
            Log.e("GreskaNaMedjuspremniku","Greska sa konvertiranjem " + e.toString());
        }

        try{
            jObj = new JSONObject(json);
            tempHTML = jObj.getString("tekst");
        }catch(JSONException e){
            tempHTML = "Greška u parsiranju JSONa";
            Log.e("JSON Parser","Greska u parsiranju JSONa " + e.toString());
        }
        return tempHTML;
    }

    public static class TempMultiresponseWrapper{
        public final String responseContent;
        public final boolean hasMore;

        public TempMultiresponseWrapper(String responseContent, boolean hasMore) {
            this.responseContent = responseContent;
            this.hasMore = hasMore;
        }
    }

    public static @NonNull String getMenuElements(String categoryId, @Nullable String date) throws Exception{
        InputStream is = null;
        JSONObject jObj = null;
        String json = null;

        String URL="http://novaeva.com/json?api=2&items=20&filter=1&cid=";

        if(date == null){
            URL = URL + categoryId;
        } else{
            URL = URL + categoryId + "&date=" + date;
        }
        try{
            HttpParams httpParams = new BasicHttpParams();
            httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpPost httpPost = new HttpPost(URL);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntitiy = httpResponse.getEntity();
            is = httpEntitiy.getContent();
            Log.d("NetworkDebug", "Promet gotov");
        }
        catch(Exception e){
            Log.e("erroor", e.getMessage(), e);
            throw e;
        }

        try{
            Log.d("NetworkDebug", "Počinje parsiranje");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null){
                sb.append(line + "\n");
            }
            Log.d("NetworkDebug", "Parsiranje gotovo");
            is.close();
            json = sb.toString();
        } catch (IOException e) {
            Log.e("ioerroor", e.getMessage(), e);
            throw e;
        }

        return json;
//        listaVijesti.addAll(parseCidList(jObj));
//        return null;
    }
}