package com.inventario.hilcasa.hilcasainventario;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class frmOfflineM extends AppCompatActivity {

    Button btnOfflineSend, btnOfflineDelete;
    QueryDB dbQrs = new QueryDB(this);
    Cursor cCursor;
    ConfigData cfData = new ConfigData();
    private ProgressDialog pgProgressDialog;
    String sScannerId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frmoffline);
        dbQrs.open();
        btnOfflineSend = (Button)findViewById(R.id.btnSend);
        btnOfflineDelete = (Button)findViewById(R.id.btnDeleteOffline);
        sScannerId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        btnOfflineSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnectedToNetwork()){
                    pgProgressDialog = new ProgressDialog(frmOfflineM.this);
                    pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pgProgressDialog.setMessage("Enviando...");
                    pgProgressDialog.setIndeterminate(true);
                    pgProgressDialog.setCancelable(false);
                    pgProgressDialog.show();
                    new SendOffline().execute();
                }
            }
        });

        btnOfflineDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbQrs.DeleteLogOfflineContainers();
                dbQrs.DeleteLogOfflineComplete();
            }
        });
    }

    public boolean isConnectedToNetwork() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
        Boolean bState = false;

        if(level >= 2){
            ConnectivityManager connectivity = (ConnectivityManager) getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null)
                {
                    if(info.isConnected()) {
                        String state = info.getState().toString();
                        if (state.equals("CONNECTED")) {
                            bState = true;
                        }
                    }else {
                        bState = false;
                    }
                }
            }
        }else{
            bState = false;
        }
        return bState;
    }

   /* public boolean isConnectedToInternet() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
        if(level >= 2){
            ConnectivityManager connectivity = (ConnectivityManager) getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null)
                    for (int i = 0; i < info.length; i++)
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
            }
        }else{
            return false;
        }
        return false;
    }*/

    private class SendOffline extends AsyncTask<Void, Void, SendOffline> {
        String sQuery = "";
        GetDataWS GetData;

        @Override
        protected SendOffline doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(cfData.iTimeOut);
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                cCursor = dbQrs.GetOfflineLogContainer();
                if(cCursor.moveToFirst()) {
                    do{
                        String sId = cCursor.getString(0);
                        sQuery = sQuery + cCursor.getString(1);
                    }while(cCursor.moveToNext());
                    GetData = restTemplate.getForObject(cfData.sUrlSendErrors (sScannerId, sQuery), GetDataWS.class);
                    if (GetData.getId().equals("1")) {
                        Toast.makeText(getApplicationContext(),"Enviados", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(),"Error", Toast.LENGTH_SHORT).show();
                    }
                }

                cCursor = dbQrs.GetOfflineLog();
                if(cCursor.moveToFirst()) {
                    do{
                        String sId = cCursor.getString(0);
                        String sQuery = cCursor.getString(1);
                        GetData = restTemplate.getForObject(cfData.sUrlSendErrors (sScannerId, sQuery), GetDataWS.class);
                        if (GetData.getId().equals("1")) {
                            //Toast.makeText(getApplicationContext(),"Enviados", Toast.LENGTH_SHORT).show();
                        }else{
                            //Toast.makeText(getApplicationContext(),"Error", Toast.LENGTH_SHORT).show();
                        }
                    }while(cCursor.moveToNext());
                }
            } catch (Exception e) {
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                }
                Log.e("MainActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(SendOffline SendOffline) {
            if (pgProgressDialog != null) {
                pgProgressDialog.dismiss();
            }
        }
    }
}

