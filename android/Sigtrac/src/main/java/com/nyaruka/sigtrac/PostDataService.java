package com.nyaruka.sigtrac;


import android.app.IntentService;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PostDataService extends IntentService {



    public PostDataService() {
        super("PostDataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        for (String filename: fileList()) {
            postData(filename);
        }

    }

    public String computeHash(String secret, String input) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec((secret).getBytes(), "ASCII"));
        mac.update(input.getBytes("UTF-8"));
        byte[] byteData = mac.doFinal();
        return Base64.encodeToString(byteData, Base64.URL_SAFE).trim();
    }

    public void postData(String filename) {

        HttpClient client = new DefaultHttpClient();

        try {

            FileInputStream fileInputStream = openFileInput(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
            int i;
            char[] buffer = new char[fileInputStream.available()];
            StringBuffer output = new StringBuffer();
            while ((i = reader.read(buffer)) > 0) {
                output.append(buffer, 0, i);
            }
            reader.close();

            String entityString =  output.toString();

            // String url = "http://192.168.0.108:8000/submit";
            String url = "http://bitranks.com/submit";

            if (BuildConfig.SECRET != null) {
                Sigtrac.log("Signing with " + BuildConfig.SECRET);
                String signature = computeHash(BuildConfig.SECRET+filename, entityString);
                url += "?" + entityString + "&s=" + URLEncoder.encode(signature);
            }
            Sigtrac.log("URL: " + url);

            HttpPost post = new HttpPost(url);
            Sigtrac.log(entityString);

            // Execute HTTP Post Request
            HttpResponse response = client.execute(post);
            String responseContent = EntityUtils.toString(response.getEntity());
            Sigtrac.log("Response: " + responseContent);

            if (responseContent.equals("New Report Created")) {
                deleteFile(filename);
            }

        } catch (Throwable t) {
            Log.e(Sigtrac.TAG, "Failed to post file", t);
        }
    }

}
