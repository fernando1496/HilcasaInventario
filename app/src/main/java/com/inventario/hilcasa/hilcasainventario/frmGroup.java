package com.inventario.hilcasa.hilcasainventario;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class frmGroup extends AppCompatActivity implements PublicInterface{

    ConfigData cfData = new ConfigData();
    String sUser = "";
    String sUserId = "";
    TextView txtUser;
    String sScannerId = "";
    AlertDialog alertDialog , alertDialog2;
    ImageButton btnSearch;
    TextView txtCode;
    Boolean bIsEmpty = true;
    Cursor cCursor;
    String sParentContainerNumber = "";
    String sChildContainerNumber = "";
    EditText txtBarcodeAddModal;
    Button lblScanned;
    int iFlag = 0;
    FloatingActionButton fabSearch;
    FloatingActionButton fabAdd;
    QueryDB dbQrs = new QueryDB(this);
    private ProgressDialog pgProgressDialog;
    private List<InventoryGroup> TempList = new ArrayList<>();
    private InventoryGroupAdapter mAdapterTempList;
    private RecyclerView rvTempList;
    int iRawOk;
    int iRawError;
    MediaPlayer mpPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frmgroupcontainer
        );

        sScannerId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        txtUser = (TextView)findViewById(R.id.lblUser);
        rvTempList = (RecyclerView) findViewById(R.id.rvTempList);
        fabSearch = (FloatingActionButton) findViewById(R.id.fabSearch);
        fabAdd = (FloatingActionButton)findViewById(R.id.fabAdd);
        txtCode = (TextView)findViewById(R.id.txtCode) ;
        btnSearch = (ImageButton) findViewById(R.id.btnSearch);
        lblScanned = (Button)findViewById(R.id.lblScanned);

        dbQrs.open();
        getSupportActionBar().setTitle("Contenedores padres");
        iRawOk =  getResources().getIdentifier(cfData.sRawOk, "raw", getApplicationContext().getPackageName());
        iRawError =  getResources().getIdentifier(cfData.sRawError, "raw", getApplicationContext().getPackageName());

        getCurrentUser();

        if(isConnectedToNetwork()) {
            pgProgressDialog = new ProgressDialog(frmGroup.this);
            pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pgProgressDialog.setMessage("Obteniendo datos...");
            pgProgressDialog.setIndeterminate(true);
            pgProgressDialog.setCancelable(false);
            pgProgressDialog.show();
            ShowTempList();
        }else{
            Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este módulo.", Toast.LENGTH_SHORT).show();
        }

        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowModalSearchContainer();
            }
        });

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowModalAddContainer();
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(iFlag == 0){
                    //nuevo
                    if(isConnectedToNetwork()) {
                        pgProgressDialog = new ProgressDialog(frmGroup.this);
                        pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pgProgressDialog.setMessage("Obteniendo datos...");
                        pgProgressDialog.setIndeterminate(true);
                        pgProgressDialog.setCancelable(false);
                        pgProgressDialog.show();
                        new GenerateParent().execute();
                    }else{
                        Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este módulo.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    //contenedor padre ya existe
                    if(isConnectedToNetwork()) {
                        pgProgressDialog = new ProgressDialog(frmGroup.this);
                        pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pgProgressDialog.setMessage("Obteniendo datos...");
                        pgProgressDialog.setIndeterminate(true);
                        pgProgressDialog.setCancelable(false);
                        pgProgressDialog.show();
                        ShowTempList();
                    }else{
                        Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este módulo.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

   /* public boolean isConnectedToInternet() {
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
            sUserId = cCursor.getString(0);
            sUser = cCursor.getString(1);
            txtUser.setText(sUser);
        }
    }

    public void ShowTempList(){

        new GetData().execute();
        txtCode.setText("Nuevo");
    }



    private class GetData extends AsyncTask<Void, Void, GetData> {
        String sError="";
        String sQuery = "";
        @Override
        protected GetData doInBackground(Void... params) {
            try {


                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlTempGroupedContainers(sUserId), GetDataWS.class);

                if (GetData.getId().equals("1")){
                    sQuery = GetData.getContent();
                }else{
                    sError = sError + "-400";
                }

            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
                sError = sError + "-400";
            }
            return null;
        }

        @Override
        protected void onPostExecute(GetData GetData) {
            int iCont = 0;
            if (sError.contains("-400")){
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Ha ocurrido un problema.", Toast.LENGTH_SHORT).show();
                    sError = "";
                }
            }else {
                InventoryGroup ilLog;
                TempList.clear();
                mAdapterTempList = new InventoryGroupAdapter(TempList, frmGroup.this);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                rvTempList.setLayoutManager(mLayoutManager);
                rvTempList.setItemAnimator(new DefaultItemAnimator());
                rvTempList.setAdapter(mAdapterTempList);
                if (sQuery.length() > 0){
                    if (sQuery.contains(String.valueOf((char) 164))) {
                        String[] sContainer = sQuery.split(String.valueOf((char) 164));
                        for (int n = 0; n < sContainer.length; n++) {
                            String strContainer = sContainer[n];
                            String sDataRow[] = strContainer.split(String.valueOf((char) 165));
                            String sContainerNumber = sDataRow[0];
                            String sContainerDesc = sDataRow[1];
                            String sContainerWeight = sDataRow[2];
                            String sContainerPart = sDataRow[3];

                            ilLog = new InventoryGroup(sContainerNumber, sContainerDesc,  sContainerWeight + " Lbs          " + sContainerPart, sContainerPart, sUser);
                            TempList.add(ilLog);
                            mAdapterTempList.notifyDataSetChanged();
                            bIsEmpty = false;
                            iFlag = 0;
                            EnableButton();
                            txtCode.setText("Nuevo");
                            iCont = iCont + 1;
                        }
                    }
            }else{
                    iFlag = 0;
                    bIsEmpty = true;
                    EnableButton();
                }
                lblScanned.setText(String.valueOf(iCont));
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Datos obtenidos con exitó.", Toast.LENGTH_SHORT).show();
                    sError = "";
                }
            }
        }
    }

    private void ShowModalSearchContainer() {
        try {
            LayoutInflater liLayoutInflater = LayoutInflater.from(frmGroup.this);
            View vPromptsView = liLayoutInflater.inflate(R.layout.modalsearchgroupedcontainer, null);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(frmGroup.this);
            alertDialogBuilder.setView(vPromptsView);


            final EditText txtBarcode = (EditText)vPromptsView.findViewById(R.id.txtBarcode);
            final ImageButton btnClose =  (ImageButton) vPromptsView.findViewById(R.id.btnClose);
            final ImageButton btnSearch = (ImageButton) vPromptsView.findViewById(R.id.btnSearch);
            txtBarcode.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
            txtBarcode.setMaxLines(1);

            alertDialogBuilder.setCancelable(false);
            alertDialog = alertDialogBuilder.create();

            txtBarcode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                        if (txtBarcode.getText().toString().length() > 0) {

                            sParentContainerNumber = txtBarcode.getText().toString();

                            if(isConnectedToNetwork()) {
                                pgProgressDialog = new ProgressDialog(frmGroup.this);
                                pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                pgProgressDialog.setMessage("Obteniendo datos...");
                                pgProgressDialog.setIndeterminate(true);
                                pgProgressDialog.setCancelable(false);
                                pgProgressDialog.show();
                                new GetParentContainer().execute();
                            }else{
                                Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este modulo.", Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            Toast.makeText(getApplicationContext(),"Ingrese un número valido.", Toast.LENGTH_SHORT).show();
                        }
                    }return false;
            }
        });



            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (txtBarcode.getText().toString().length() > 0) {

                        sParentContainerNumber = txtBarcode.getText().toString();

                        if(isConnectedToNetwork()) {
                            pgProgressDialog = new ProgressDialog(frmGroup.this);
                            pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            pgProgressDialog.setMessage("Obteniendo datos...");
                            pgProgressDialog.setIndeterminate(true);
                            pgProgressDialog.setCancelable(false);
                            pgProgressDialog.show();
                            new GetParentContainer().execute();
                        }else{
                            Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este modulo.", Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        Toast.makeText(getApplicationContext(),"Ingrese un número valido.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    alertDialog.cancel();

                }
            });
            alertDialog.show();

        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
    }

    private class GetParentContainer extends AsyncTask<Void, Void, GetParentContainer> {
        String sError="";
        String sQuery = "";
        @Override
        protected GetParentContainer doInBackground(Void... params) {
            try {


                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlTempGroupedContainersByParent(sParentContainerNumber), GetDataWS.class);

                if (GetData.getId().equals("1")){
                    sQuery = GetData.getContent();
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
        protected void onPostExecute(GetParentContainer GetParentContainer) {
            int iCont = 0;
            if (sError.contains("-400")){
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Ha ocurrido un problema.", Toast.LENGTH_SHORT).show();
                    sError = "";
                }
            }else {
                if (sQuery.length() > 0){
                    InventoryGroup ilLog;
                    TempList.clear();
                    mAdapterTempList = new InventoryGroupAdapter(TempList, frmGroup.this);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    rvTempList.setLayoutManager(mLayoutManager);
                    rvTempList.setItemAnimator(new DefaultItemAnimator());
                    rvTempList.setAdapter(mAdapterTempList);
                    if (sQuery.contains(String.valueOf((char) 164))) {
                        String[] sContainer = sQuery.split(String.valueOf((char) 164));
                        for (int n = 0; n < sContainer.length; n++) {
                            String strContainer = sContainer[n];
                            String sDataRow[] = strContainer.split(String.valueOf((char) 165));
                            String sContainerNumber = sDataRow[0];
                            String sContainerDesc = sDataRow[1];
                            String sContainerWeight = sDataRow[2];
                            String sContainerPart = sDataRow[3];

                            ilLog = new InventoryGroup(sContainerNumber, sContainerDesc, sContainerWeight + " Lbs.          " + sContainerPart, sContainerPart, sUser);
                            TempList.add(ilLog);
                            mAdapterTempList.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), "Datos obtenidos con exitó.", Toast.LENGTH_SHORT).show();
                            if(alertDialog != null) {
                                alertDialog.cancel();
                            }
                            iCont = iCont +1;
                        }
                        txtCode.setText(sParentContainerNumber);
                        iFlag = 1;
                       /* mpPlayer = MediaPlayer.create(getApplicationContext(),iRawOk);
                        mpPlayer.start();*/
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Número de contenedor no existe.", Toast.LENGTH_SHORT).show();
                    iFlag = 0;
                   /* mpPlayer = MediaPlayer.create(getApplicationContext(),iRawError);
                    mpPlayer.start();*/
                }
                lblScanned.setText(String.valueOf(iCont));
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    sError = "";
                }
                EnableButton();
            }
        }
    }

    public void EnableButton() {
        if (iFlag == 0){
            btnSearch.setImageResource(R.drawable.ic_send);
            if (bIsEmpty) {
                btnSearch.setEnabled(false);
            } else {
                btnSearch.setEnabled(true);
            }
        }else if(iFlag == 1){
            btnSearch.setEnabled(true);
            btnSearch.setImageResource(R.drawable.ic_noteadd_small);
        }
    }

    private void ShowModalAddContainer() {
        try {
            LayoutInflater liLayoutInflater = LayoutInflater.from(frmGroup.this);
            View vPromptsView = liLayoutInflater.inflate(R.layout.modaladdgroupedcontainer, null);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(frmGroup.this);
            alertDialogBuilder.setView(vPromptsView);


            txtBarcodeAddModal = (EditText)vPromptsView.findViewById(R.id.txtBarcode);
            final ImageButton btnClose =  (ImageButton) vPromptsView.findViewById(R.id.btnClose);
            final ImageButton btnSearch = (ImageButton) vPromptsView.findViewById(R.id.btnSearch);
            txtBarcodeAddModal.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
            txtBarcodeAddModal.setMaxLines(1);

            alertDialogBuilder.setCancelable(false);
            alertDialog2 = alertDialogBuilder.create();

            if(iFlag==0){
                //PARA AGREGAR UN CONTAINER A UN PADRE AUN NO CREADO
                txtBarcodeAddModal.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                            if(isConnectedToNetwork()) {
                                sChildContainerNumber = txtBarcodeAddModal.getText().toString();
                                pgProgressDialog = new ProgressDialog(frmGroup.this);
                                pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                pgProgressDialog.setMessage("Obteniendo datos...");
                                pgProgressDialog.setIndeterminate(true);
                                pgProgressDialog.setCancelable(false);
                                pgProgressDialog.show();
                                new AddGroupedContainer().execute();
                            }else{
                                Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este módulo.", Toast.LENGTH_SHORT).show();
                            }
                        }return false;
                    }
                });
                btnSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (txtBarcodeAddModal.getText().toString().length() > 0) {
                            if(isConnectedToNetwork()) {
                                sChildContainerNumber = txtBarcodeAddModal.getText().toString();
                                pgProgressDialog = new ProgressDialog(frmGroup.this);
                                pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                pgProgressDialog.setMessage("Obteniendo datos...");
                                pgProgressDialog.setIndeterminate(true);
                                pgProgressDialog.setCancelable(false);
                                pgProgressDialog.show();
                                new AddGroupedContainer().execute();
                            }else{
                                Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este módulo.", Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            Toast.makeText(getApplicationContext(),"Ingrese un número valido.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        alertDialog2.cancel();

                    }
                });
            } else if(iFlag==1){
                //PARA AGREGAR CONTENEDORES A UN PADRE QUE YA EXISTE
                txtBarcodeAddModal.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                            if(isConnectedToNetwork()) {
                                sChildContainerNumber = txtBarcodeAddModal.getText().toString();
                                pgProgressDialog = new ProgressDialog(frmGroup.this);
                                pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                pgProgressDialog.setMessage("Obteniendo datos...");
                                pgProgressDialog.setIndeterminate(true);
                                pgProgressDialog.setCancelable(false);
                                pgProgressDialog.show();
                                sParentContainerNumber = txtCode.getText().toString();
                                new AddGroupedContainer().execute();
                            }else{
                                Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este módulo.", Toast.LENGTH_SHORT).show();
                            }
                        }return false;
                    }
                });
                btnSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (txtBarcodeAddModal.getText().toString().length() > 0) {
                            if(isConnectedToNetwork()) {
                                sChildContainerNumber = txtBarcodeAddModal.getText().toString();
                                pgProgressDialog = new ProgressDialog(frmGroup.this);
                                pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                pgProgressDialog.setMessage("Obteniendo datos...");
                                pgProgressDialog.setIndeterminate(true);
                                pgProgressDialog.setCancelable(false);
                                pgProgressDialog.show();
                                new AddGroupedContainer().execute();
                            }else{
                                Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este módulo.", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(getApplicationContext(),"Ingrese un número valido.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        alertDialog2.cancel();

                    }
                });
            }

            alertDialog2.show();

        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
    }

    private class AddGroupedContainer extends AsyncTask<Void, Void, AddGroupedContainer> {
        String sError="";
        String sMsj = "";
        @Override
        protected AddGroupedContainer doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                GetDataWS GetData;
                if(iFlag == 0) {
                     GetData = restTemplate.getForObject(cfData.sUrlTempAddGroupedContainers(sChildContainerNumber, sUserId, sScannerId), GetDataWS.class);
                    if (GetData.getId().equals("1")){
                        sMsj = GetData.getContent();
                        dbQrs.UpdateStatusContainer(sChildContainerNumber, "CUERP");
                    }else{
                        sError = sError + "-400";
                    }
                }else if(iFlag == 1){
                     GetData = restTemplate.getForObject(cfData.sUrlAddGroupedContainers(sChildContainerNumber, sParentContainerNumber, sUserId, sScannerId), GetDataWS.class);
                    if (GetData.getId().equals("1")){
                        sMsj = GetData.getContent();
                        dbQrs.UpdateStatusContainer(sChildContainerNumber, "CUERP");
                    }else{
                        sError = sError + "-400";
                    }

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
        protected void onPostExecute(AddGroupedContainer AddGroupedContainer) {
            if (sError.contains("-400")){
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Ha ocurrido un problema.", Toast.LENGTH_SHORT).show();
                    sError = "";
                    mpPlayer = MediaPlayer.create(getApplicationContext(),iRawError);
                    mpPlayer.start();
                }
            }else{
               // alertDialog.dismiss();
                Toast.makeText(getApplicationContext(), sMsj, Toast.LENGTH_SHORT).show();
                if(sMsj.contains("éxito")){
                    mpPlayer = MediaPlayer.create(getApplicationContext(),iRawOk);
                    mpPlayer.start();
                }else{
                    mpPlayer = MediaPlayer.create(getApplicationContext(),iRawError);
                    mpPlayer.start();
                }
                txtBarcodeAddModal.setText("");
               if(iFlag == 0) {
                   new GetData().execute();
               }else if(iFlag == 1){

                   new GetParentContainer().execute();
               }
            }
        }
    }

    public void Reload(){
        if(iFlag == 0){
            if(isConnectedToNetwork()) {
                sChildContainerNumber = txtCode.getText().toString();
                pgProgressDialog = new ProgressDialog(frmGroup.this);
                pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pgProgressDialog.setMessage("Obteniendo datos...");
                pgProgressDialog.setIndeterminate(true);
                pgProgressDialog.setCancelable(false);
                pgProgressDialog.show();
                new GetData().execute();
            }else{
                Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este modulo.", Toast.LENGTH_SHORT).show();
            }
        }else if(iFlag == 1){
            if(isConnectedToNetwork()) {
                sParentContainerNumber = txtCode.getText().toString();
                pgProgressDialog = new ProgressDialog(frmGroup.this);
                pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pgProgressDialog.setMessage("Obteniendo datos...");
                pgProgressDialog.setIndeterminate(true);
                pgProgressDialog.setCancelable(false);
                pgProgressDialog.show();
                new GetParentContainer().execute();
            }else{
                Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este modulo.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class GenerateParent extends AsyncTask<Void, Void, GenerateParent> {
        String sError="";
        String sQuery = "";
        @Override
        protected GenerateParent doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlGenerateParentContainer(sUserId, sScannerId), GetDataWS.class);

                if (GetData.getId().equals("1")){
                    sParentContainerNumber = GetData.getContent();
                }else{
                    sError = sError + "-400";
                }

            } catch (Exception e) {
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                }
                Log.e("MainActivity", e.getMessage(), e);
                sError = sError + "-400";
            }
            return null;
        }

        @Override
        protected void onPostExecute(GenerateParent GenerateParent) {
            if (sError.contains("-400")){
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Ha ocurrido un problema.", Toast.LENGTH_SHORT).show();
                    sError = "";
                }
            }else {
                new GetParentContainer().execute();
            }
        }
    }
}
