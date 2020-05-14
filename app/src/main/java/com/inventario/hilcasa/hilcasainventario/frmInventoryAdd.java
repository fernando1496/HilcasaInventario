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
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
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
public class frmInventoryAdd extends AppCompatActivity {

    Cursor cCursor;
    QueryDB dbQrs = new QueryDB(this);
    String sUser;
    TextView txtUser;
    ConfigData cfData = new ConfigData();
    private ProgressDialog pgProgressDialog;
    SwitchCompat scUseLot;

    public static final String TAG = "frmInventoryAdd";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frminventoryadd);
        txtUser = (TextView)findViewById(R.id.lblUser);
        scUseLot = (SwitchCompat) findViewById(R.id.scUseLot);
        dbQrs.open();

        getCurrentUser();


        SharedPreferences spPref = getSharedPreferences("UserData", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = spPref.edit();
        editor.putString("Lote", "0");
        editor.commit();

        scUseLot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if(scUseLot.isChecked()){
                    SharedPreferences spPref = getSharedPreferences("UserData", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = spPref.edit();
                    editor.putString("Lote", "1");
                    editor.commit();
                }else{
                    SharedPreferences spPref = getSharedPreferences("UserData", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = spPref.edit();
                    editor.putString("Lote", "0");
                    editor.commit();
                }
            }
        });

        getSupportActionBar().setTitle(R.string.sTomaInvInicial);
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            InvetoryAddTabs fragment = new InvetoryAddTabs();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mbtnSinc = menu.findItem(R.id.btnSinc);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.btnSinc:
                if (isConnectedToNetwork()) {
                    pgProgressDialog = new ProgressDialog(frmInventoryAdd.this);
                    pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pgProgressDialog.setMessage("Sincronizando...");
                    pgProgressDialog.setIndeterminate(true);
                    pgProgressDialog.setCancelable(false);
                    pgProgressDialog.show();
                    new GetData().execute();
                }
                return true;
            case R.id.btnLastLog:
                Intent IttIntent = new Intent(getApplicationContext(), frmLastLog.class);
                Bundle bdlExtras = new Bundle();
                bdlExtras.putString("sIdentifier",cfData.sIdentifierNew);
                IttIntent.putExtras(bdlExtras);
                startActivity(IttIntent);
                return true;
            case android.R.id.home:
                finish();
                return true;
            case R.id.btnGroup:
                Intent Intent = new Intent(getApplicationContext(), frmGroup.class);
                startActivity(Intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void EditWeight(final String sContainerNumber,final String sContainerWeight) {
        try {
            //CAMBIAR ICONOS Y TEXTO
            LayoutInflater liLayoutInflater = LayoutInflater.from(frmInventoryAdd.this);
            View vPromptsView = liLayoutInflater.inflate(R.layout.editweightmodal, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(frmInventoryAdd.this);
            alertDialogBuilder.setView(vPromptsView);
            final TextView lblContainer = (TextView) vPromptsView.findViewById(R.id.lblContainer);
            final EditText txtWeight = (EditText) vPromptsView.findViewById(R.id.txtWeight);

            lblContainer.setText(sContainerNumber);
            txtWeight.setText(sContainerWeight);

            alertDialogBuilder.setCancelable(false).setTitle(R.string.sCambiarPeso).setIcon(R.drawable.ic_weight)
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    {
                        if(txtWeight.getText().length()<0){
                            Toast.makeText(getApplicationContext(),"Ingresar un peso valido.",Toast.LENGTH_LONG).show();
                        }else{
                            alertDialog.cancel();
                        }
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    private class GetData extends AsyncTask<Void, Void, frmInventoryAdd.GetData> {
        String sError = "";
        @Override
        protected frmInventoryAdd.GetData doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(cfData.iTimeOut);
                //((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(cfData.iTimeOut);

                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetAllContainers(), GetDataWS.class);
                if (GetData.getContent().contains(String.valueOf((char) 164))) {
                    dbQrs.DeleteContainer();
                    String[] sContainer = GetData.getContent().split(String.valueOf((char) 164));
                    for (int n = 0; n < sContainer.length; n++) {
                        String strContainer = sContainer[n];
                        String sDatos[] = strContainer.split(String.valueOf((char) 165));
                        String sContainerId = sDatos[0];
                        String sContainerNumber = sDatos[1];
                        String CurrentProcess = sDatos[2];
                        String sCurrentProcessStatus = sDatos[3];
                        String sDatePrinted = sDatos[4];
                        String sContainerDescription = sDatos[5];
                        String sContainerWeight = sDatos[6];
                        String sStartWeight = sDatos[7];
                        String sPreviousDatePrinted = sDatos[8];
                        String sCurrentStopReason = sDatos[9];
                        String sContainerPart = sDatos[10];
                        String sBindId = sDatos[11];
                        String sAreaId = sDatos[12];
                        String sContainerType = sDatos[13];
                        String sComponentId = sDatos[14];
                        String sParentContainerNumber = sDatos[15];
                        String sStatusInv = sDatos[16];
                        dbQrs.InsertContainers(sContainerId, sCurrentProcessStatus, CurrentProcess, sContainerNumber, sDatePrinted, sContainerDescription, sContainerWeight, sPreviousDatePrinted, sStartWeight, sCurrentStopReason, sContainerPart, sBindId, sAreaId, "", "", "", sStatusInv);
                    }
                }else{
                    sError = "-400";
                }


            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);

            }
            return null;
        }

        @Override
        protected void onPostExecute(frmInventoryAdd.GetData GetData) {
            if(sError.contains("-400")){
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "No se han ingresado los datos actualizados.", Toast.LENGTH_SHORT).show();
                }
            }else {
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Datos sincronizados con exitó.", Toast.LENGTH_SHORT).show();
                }
            }
        }
}

    public boolean isConnectedToNetwork() {
        WifiManager wifiManager = (WifiManager)  getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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

    /*public boolean isConnectedToInternet() {
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
        return false;
    }*/

    public void getCurrentUser(){
        cCursor = dbQrs.GetEmployeeInfo();
        if(cCursor.moveToFirst()){
            sUser = cCursor.getString(1);
            txtUser.setText(sUser);
        }
    }
}