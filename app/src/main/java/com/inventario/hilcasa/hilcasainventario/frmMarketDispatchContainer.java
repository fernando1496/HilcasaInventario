/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package com.inventario.hilcasa.hilcasainventario;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class frmMarketDispatchContainer extends AppCompatActivity {

    Cursor cCursor;
    QueryDB dbQrs = new QueryDB(this);
    String sUser;
    TextView txtUser;
    ConfigData cfData = new ConfigData();
    SwitchCompat scUseLot;
    private ProgressDialog pgProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frmmarketdispatchcontainer);
        txtUser = (TextView)findViewById(R.id.lblUser);

        dbQrs.open();

       getCurrentUser();

        SharedPreferences spPref = getSharedPreferences("UserData", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = spPref.edit();
        editor.putString("Lote", "0");
        editor.commit();



        getSupportActionBar().setTitle("Despacho inventario despensa");
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            InventoryMarketDispatchTabs fragment = new InventoryMarketDispatchTabs();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menumarket, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case R.id.btnSinc:
                if (isConnectedToNetwork()) {
                    pgProgressDialog = new ProgressDialog(frmMarketDispatchContainer.this);
                    pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pgProgressDialog.setMessage("Sincronizando Parcial...");
                    pgProgressDialog.setIndeterminate(true);
                    pgProgressDialog.setCancelable(false);
                    pgProgressDialog.show();
                    new GetData().execute();
                }else{
                    Toast.makeText(getApplicationContext(),"Necesita estar conectado para realizar esta acción", Toast.LENGTH_SHORT).show();
                }
                return true;
            case android.R.id.home:
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void getCurrentUser(){
        cCursor = dbQrs.GetEmployeeInfo();
        if(cCursor.moveToFirst()){
            sUser = cCursor.getString(1);
            txtUser.setText(sUser);
        }
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

    private class GetData extends AsyncTask<Void, Void, GetData> {
        String sError="";


        @Override
        protected GetData doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(cfData.iTimeOut);

                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlGetMakerInventory(), GetDataWS.class);

                if (GetData.getId().equals("1")){
                    dbQrs.DeleteMarketInventory();
                    dbQrs.BeginTransaction();
                    if (GetData.getContent().contains(String.valueOf((char) 164))) {
                        String[] sContainer = GetData.getContent().split(String.valueOf((char) 164));
                        for (int n = 0; n < sContainer.length; n++) {
                            String strContainer = sContainer[n];
                            String sDataRow[] = strContainer.split(String.valueOf((char) 165));
                            String sComponentId = sDataRow[0];
                            String sCompCode = sDataRow[1];
                            String sDescription = sDataRow[2];
                            String sBarcode = sDataRow[3];
                            String sQuantity = sDataRow[4];
                            dbQrs.InsertMarketInventory(sComponentId, sCompCode, sDescription,sBarcode,sQuantity);



                        }
                        dbQrs.CommitTransaction();
                    }

                }else{
                    sError = sError + "-400";
                }


            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
                sError = sError + "-400";
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(GetData GetData) {
            if (sError.contains("-400")){
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Ha ocurrido un problema.", Toast.LENGTH_SHORT).show();
                    sError = "";

                }
            }else{
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Datos sincronizados con exitó.", Toast.LENGTH_SHORT).show();
                    sError = "";
                }
            }
        }
    }
}