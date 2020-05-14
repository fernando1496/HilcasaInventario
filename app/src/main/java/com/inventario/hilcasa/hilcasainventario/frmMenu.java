package com.inventario.hilcasa.hilcasainventario;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.StringTokenizer;

public class frmMenu extends AppCompatActivity {

    LinearLayout llInventoryCheck, llInventoryAdd, llInventoryIn, llInventoryOut, llInventoryInArea, llInventoryOutArea, llInventoryValidation, llRelocation, llTintoCreate, llPackingListCheck, llPanttrInv, llMarketDispatch;
    TextView lblUserName;
    QueryDB dbQrs = new QueryDB(this);
    Cursor cCursor;
    String sUser = "";
    String sScannerId = "";
    private ProgressDialog pgProgressDialog;
    ConfigData cfData = new ConfigData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frmmenu);
        llInventoryCheck = (LinearLayout)findViewById(R.id.llInventoryCheck);
        lblUserName = (TextView)findViewById(R.id.lblUserName);
        llInventoryAdd = (LinearLayout) findViewById(R.id.llInventoryAdd);
        llInventoryIn = (LinearLayout)findViewById(R.id.llInventoryIn);
        llInventoryOut = (LinearLayout)findViewById(R.id.llInventoryOut);
        llInventoryInArea = (LinearLayout)findViewById(R.id.llInventoryInArea);
        llInventoryOutArea = (LinearLayout)findViewById(R.id.llInventoryOutArea);
        llInventoryValidation = (LinearLayout)findViewById(R.id.llInventoryValidation);
        llRelocation = (LinearLayout)findViewById(R.id.llRelocation);
        llTintoCreate = (LinearLayout) findViewById(R.id.llTintoCreate);
        llPackingListCheck = (LinearLayout) findViewById(R.id.llPlCheckList);
        llPanttrInv = (LinearLayout) findViewById(R.id.llPanttrInv);
        llMarketDispatch = (LinearLayout) findViewById(R.id.llMarketDispatch);


        sScannerId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        //ABRE LA CONEXION A LA BD PARA PODER REALIZAR LAS CONSULTAS
        dbQrs.open();
        //OBTIENE EL USUARIO LOGEADO
        cCursor = dbQrs.GetEmployeeInfo();
        if(cCursor.moveToFirst()){
            sUser = cCursor.getString(0);
            lblUserName.setText(cCursor.getString(1));
        }

        if (isConnectedToNetwork()) {
            pgProgressDialog = new ProgressDialog(frmMenu.this);
            pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pgProgressDialog.setMessage("Sincronizando...");
            pgProgressDialog.setIndeterminate(true);
            pgProgressDialog.setCancelable(false);
            pgProgressDialog.show();

            new GetData().execute();
        }else{
            ShowCheckPointMenu();
            dbQrs.DeleteLastLog();
            Toast.makeText(getApplicationContext(),"offline", Toast.LENGTH_SHORT).show();
        }

        llInventoryCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), frmInventoryCheck.class);
                startActivity(intent);
            }
        });
        llInventoryAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), frmInventoryAdd.class);
                startActivity(intent);
            }
        });
        llInventoryInArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), frmInventoryInAreaContainer.class);
                startActivity(intent);
            }
        });
        llInventoryOutArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), frmInventoryOutAreaContainer.class);
                startActivity(intent);
            }
        });
        llInventoryIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), frmInventoryInVendorContainer.class);
                startActivity(intent);
            }
        });
        llRelocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), frmInventoryRelocationContainer.class);
                startActivity(intent);
            }
        });
        llInventoryOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), frmInventoryOutClientContainer.class);
                startActivity(intent);
            }
        });
        llTintoCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), frmTintoGroup.class);
                startActivity(intent);
            }
        });
        llPackingListCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), frmPlCheckListContainer.class);
                startActivity(intent);
            }
        });
        llPanttrInv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), frmPantrInventoryContainer.class);
                startActivity(intent);
            }
        });
        llMarketDispatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), frmMarketDispatchContainer.class);
                startActivity(intent);
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

    private class GetData extends AsyncTask<Void, Void, GetData> {
        String sError="";
        Calendar cCalendarS = Calendar.getInstance();
        SimpleDateFormat sdfFormatSQLS = new SimpleDateFormat("yyyyMMdd hh:mm:ss a", Locale.US);
        String sFormattedDateSQLSyncStart = sdfFormatSQLS.format(cCalendarS.getTime());
        @Override
        protected GetData doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(cfData.iTimeOut);
                //((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(cfData.iTimeOut);
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetAreasByUser(sUser), GetDataWS.class);

                if (GetData.getId().equals("1")){
                    dbQrs.DeleteArea();
                    if (GetData.getContent().contains(String.valueOf((char) 164))) {
                        String[] sContainer = GetData.getContent().split(String.valueOf((char) 164));
                        for (int n = 0; n < sContainer.length; n++) {
                            String strContainer = sContainer[n];
                            String sDataRow[] = strContainer.split(String.valueOf((char) 165));
                            String sAreaId = sDataRow[0];
                            String sName = sDataRow[1];
                            String sProductStateFrom = sDataRow[2];
                            dbQrs.InsertAreaByUser(sAreaId, sName, sProductStateFrom);
                        }
                    }
            }else{
                    sError = sError + "-400";
                }
                        String sUpdated = "";
                        final SharedPreferences spPref = getSharedPreferences("UserData", Activity.MODE_PRIVATE);
                        sUpdated = new String(spPref.getString("updated", sUpdated)).toString();

                        if (sUpdated.equals("true")) {
                                    GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetContainerByUpdate+sScannerId, GetDataWS.class);
                                    if(GetData.getId().equals("1")){
                                        dbQrs.BeginTransaction();
                                    if (GetData.getContent().contains(String.valueOf((char) 164))) {
                                        String[] sContainer = GetData.getContent().split(String.valueOf((char) 164));
                                        for (int n = 0; n < sContainer.length; n++) {
                                            String strContainer = sContainer[n];
                                            String sDataRow[] = strContainer.split(String.valueOf((char) 165));
                                            String sContainerId = sDataRow[0];
                                            String sContainerNumber = sDataRow[1];
                                            String CurrentProcess = sDataRow[2];
                                            String sCurrentProcessStatus = sDataRow[3];
                                            String sDatePrinted = sDataRow[4];
                                            String sContainerDescription = sDataRow[5];
                                            String sContainerWeight = sDataRow[6];
                                            String sStartWeight = sDataRow[7];
                                            String sPreviousDatePrinted = sDataRow[8];
                                            String sCurrentStopReason = sDataRow[9];
                                            String sContainerPart = sDataRow[10];
                                            String sBindId = sDataRow[11];
                                            String sAreaId = sDataRow[12];
                                            String sContainerType = sDataRow[13];
                                            String sComponentId = sDataRow[14];
                                            String sParentContainerNumber = sDataRow[15];
                                            String sStatusInv = sDataRow[16];
                                            Boolean bFlag = dbQrs.UpdateContainers(sContainerId, sCurrentProcessStatus, CurrentProcess, sContainerNumber, sDatePrinted, sContainerDescription, sContainerWeight, sPreviousDatePrinted, sStartWeight, sCurrentStopReason, sContainerPart, sBindId, sAreaId, sContainerType, sComponentId, sParentContainerNumber, sStatusInv);
                                            if(bFlag == false){
                                                dbQrs.InsertContainers(sContainerId, sCurrentProcessStatus, CurrentProcess, sContainerNumber, sDatePrinted, sContainerDescription, sContainerWeight, sPreviousDatePrinted, sStartWeight, sCurrentStopReason, sContainerPart, sBindId, sAreaId,  sContainerType, sComponentId, sParentContainerNumber, sStatusInv);
                                            }
                                        }
                                    }
                                        dbQrs.CommitTransaction();
                                    }else{
                                        sError = sError+"-400";
                                    }

                                    GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetContainerDeleted+sScannerId, GetDataWS.class);
                                    if (GetData.getContent().contains(String.valueOf((char) 164))) {
                                        String[] sContainer = GetData.getContent().split(String.valueOf((char) 164));
                                        for (int n = 0; n < sContainer.length; n++) {
                                            String strContainer = sContainer[n];
                                            String sData[] = strContainer.split(String.valueOf((char) 165));
                                            String sContainerNumber = sData[0];
                                            String sContainerPart = sData[1];
                                            dbQrs.DeleteContainersDeleted(sContainerNumber, sContainerPart);
                                        }
                                    }

                                        GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetBinByUpdated+sScannerId, GetDataWS.class);
                                    if (GetData.getId().equals("1")) {
                                        if (GetData.getContent().contains(String.valueOf((char) 164))) {
                                            String[] sData = GetData.getContent().split(String.valueOf((char) 164));
                                            for (int n = 0; n < sData.length; n++) {
                                                String strData = sData[n];
                                                String sDataRow[] = strData.split(String.valueOf((char) 165));
                                                String sBinId = sDataRow[0];
                                                String sBinArea = sDataRow[1];
                                                String sDesc = sDataRow[2];
                                                String sAisle = sDataRow[3];
                                                String sPackQuantity = sDataRow[4];
                                                Boolean bFlag = dbQrs.UpdateBin(sBinId, sBinArea, sDesc, sAisle, sPackQuantity);
                                                if(bFlag == false){
                                                    dbQrs.InsertBin(sBinId, sBinArea, sDesc, sAisle,sPackQuantity);
                                                }
                                            }
                                        }
                                    } else {
                                        sError = sError + "-400";
                                    }

                                    GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetBinDeleted+sScannerId, GetDataWS.class);
                                    if (GetData.getContent().contains(String.valueOf((char) 164))) {
                                        String[] sBin = GetData.getContent().split(String.valueOf((char) 164));
                                        for (int n = 0; n < sBin.length; n++) {
                                            String strBin = sBin[n];
                                            String sData[] = strBin.split(String.valueOf((char) 165));
                                            String sBinId = sData[0];
                                            dbQrs.DeleteBinDeleted(sBinId);
                                        }
                                    }

                                    GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetComponentsByUpdated+sScannerId, GetDataWS.class);
                                    if (GetData.getId().equals("1")){
                                        if (GetData.getContent().contains(String.valueOf((char) 164))) {
                                            String[] sContainer = GetData.getContent().split(String.valueOf((char) 164));
                                            for (int n = 0; n < sContainer.length; n++) {
                                                String strContainer = sContainer[n];
                                                String sDataRow[] = strContainer.split(String.valueOf((char) 165));
                                                String sComponentId = sDataRow[0];
                                                String sProductStateForm = sDataRow[1];
                                                String sDescription = sDataRow[2];
                                                String sBarcode = sDataRow[3];
                                                String sReasonBarcode = sDataRow[4];
                                                String sVendorReference = sDataRow[5];
                                                String sPurchaseUom = sDataRow[6];
                                                String sQuantityMin = sDataRow[7];
                                                String sQuantityMax = sDataRow[8];
                                                dbQrs.InsertComponents(sComponentId,sProductStateForm, sDescription, sBarcode, sReasonBarcode, sVendorReference, sPurchaseUom, sQuantityMin, sQuantityMax);
                                            }
                                        }
                                    }else {
                                        sError = sError + "-400";
                                    }

                                    GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetComponentsByDeleted+sScannerId, GetDataWS.class);
                                    if (GetData.getContent().contains(String.valueOf((char) 164))) {
                                        String[] sComponent = GetData.getContent().split(String.valueOf((char) 164));
                                        for (int n = 0; n < sComponent.length; n++) {
                                            String strComponent = sComponent[n];
                                            String sData[] = strComponent.split(String.valueOf((char) 165));
                                            String sComponentId = sData[0];
                                            dbQrs.DeleteComponentById(sComponentId);
                                        }
                                    }
                        } else {
                            GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetBinByArea(), GetDataWS.class);
                            if (GetData.getId().equals("1")) {

                                dbQrs.BeginTransaction();
                                if (GetData.getContent().contains(String.valueOf((char) 164))) {
                                    dbQrs.DeleteBin();
                                    String[] sData = GetData.getContent().split(String.valueOf((char) 164));

                                    for (int n = 0; n < sData.length; n++) {
                                        String strData = sData[n];
                                        String sDataRow[] = strData.split(String.valueOf((char) 165));
                                        String sBinId = sDataRow[0];
                                        String sBinArea = sDataRow[1];
                                        String sDesc = sDataRow[2];
                                        String sAisle = sDataRow[3];
                                        String sPackQuantity = sDataRow[4];
                                        dbQrs.InsertBin(sBinId, sBinArea, sDesc, sAisle, sPackQuantity);
                                    }
                                }
                              dbQrs.CommitTransaction();
                            } else {
                                sError = sError + "-400";
                            }


                            GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetAllContainers(), GetDataWS.class);
                            if (GetData.getId().equals("1")){
                                dbQrs.BeginTransaction();
                                if (GetData.getContent().contains(String.valueOf((char) 164))) {
                                    dbQrs.DeleteContainer();
                                    String[] sContainer = GetData.getContent().split(String.valueOf((char) 164));
                                    for (int n = 0; n < sContainer.length; n++) {
                                        String strContainer = sContainer[n];
                                        String sDataRow[] = strContainer.split(String.valueOf((char) 165));
                                        String sContainerId = sDataRow[0];
                                        String sContainerNumber = sDataRow[1];
                                        String CurrentProcess = sDataRow[2];
                                        String sCurrentProcessStatus = sDataRow[3];
                                        String sDatePrinted = sDataRow[4];
                                        String sContainerDescription = sDataRow[5];
                                        String sContainerWeight = sDataRow[6];
                                        String sStartWeight = sDataRow[7];
                                        String sPreviousDatePrinted = sDataRow[8];
                                        String sCurrentStopReason = sDataRow[9];
                                        String sContainerPart = sDataRow[10];
                                        String sBindId = sDataRow[11];
                                        String sAreaId = sDataRow[12];
                                        String sContainerType = sDataRow[13];
                                        String sComponentId = sDataRow[14];
                                        String sParentContainerNumber = sDataRow[15];
                                        String sStatusInv = sDataRow[16];
                                        dbQrs.InsertContainers(sContainerId, sCurrentProcessStatus, CurrentProcess, sContainerNumber, sDatePrinted, sContainerDescription, sContainerWeight, sPreviousDatePrinted, sStartWeight, sCurrentStopReason, sContainerPart, sBindId, sAreaId, sContainerType, sComponentId, sParentContainerNumber, sStatusInv);
                                    }
                                }
                                dbQrs.CommitTransaction();
                            }else {
                                sError = sError + "-400";
                        }
                            GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetComponents+sScannerId, GetDataWS.class);
                            if (GetData.getId().equals("1")){
                                if (GetData.getContent().contains(String.valueOf((char) 164))) {
                                    dbQrs.DeleteComponents();
                                    String[] sContainer = GetData.getContent().split(String.valueOf((char) 164));
                                    for (int n = 0; n < sContainer.length; n++) {
                                        String strContainer = sContainer[n];
                                        String sDataRow[] = strContainer.split(String.valueOf((char) 165));
                                        String sComponentId = sDataRow[0];
                                        String sProductStateForm = sDataRow[1];
                                        String sDescription = sDataRow[2];
                                        String sBarcode = sDataRow[3];
                                        String sReasonBarcode = sDataRow[4];
                                        String sVendorReference = sDataRow[5];
                                        String sPurchaseUom = sDataRow[6];
                                        String sQuantityMin = sDataRow[7];
                                        String sQuantityMax = sDataRow[8];
                                        dbQrs.InsertComponents(sComponentId,sProductStateForm, sDescription, sBarcode, sReasonBarcode, sVendorReference, sPurchaseUom, sQuantityMin, sQuantityMax);
                                    }
                                }
                            }else {
                                sError = sError + "-400";
                            }
                    }
                GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetLog(sUser)  , GetDataWS.class);
                if (GetData.getId().equals("1")){
                    dbQrs.DeleteLog();
                    if (GetData.getContent().contains(String.valueOf((char) 164))) {
                        dbQrs.BeginTransaction();
                        String[] sData = GetData.getContent().split(String.valueOf((char) 164));
                        for (int n = 0; n < sData.length; n++) {
                            String strData = sData[n];
                            String sDataRow[] = strData.split(String.valueOf((char) 165));
                            String sArea = sDataRow[0];
                            String sBinId = sDataRow[1];
                            String sContainerId = sDataRow[2];
                            String sDate = sDataRow[3];
                            String sContainerNumber = sDataRow[4];
                            String sContainerWeight = sDataRow[5];
                            String sContainerDesc = sDataRow[6];
                            String sCheckPoint = sDataRow[7];
                            dbQrs.InsertLog(sArea, sBinId, sContainerId, sDate, sContainerNumber, sContainerWeight, sContainerDesc, "2", sCheckPoint);
                        }
                        dbQrs.CommitTransaction();
                    }
                }else{
                    sError = sError+"-400";
                }

                GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetLogArea + sUser, GetDataWS.class);
                if (GetData.getId().equals("1")){
                    dbQrs.DeleteLogMovimiento();

                    if (GetData.getContent().contains(String.valueOf((char) 164))) {
                        dbQrs.BeginTransaction();
                        String[] sData = GetData.getContent().split(String.valueOf((char) 164));
                        for (int n = 0; n < sData.length; n++) {
                            String strData = sData[n];
                            String sDataRow[] = strData.split(String.valueOf((char) 165));
                            String sArea = sDataRow[0];
                            String sBinId = sDataRow[1];
                            String sContainerId = sDataRow[2];
                            String sDate = sDataRow[3];
                            String sContainerNumber = sDataRow[4];
                            String sContainerWeight = sDataRow[5];
                            String sContainerDesc = sDataRow[6];
                            String sCheckPointGroup = sDataRow[7];
                            String sProductStateFrom = "";
                            cCursor = dbQrs.GetProductStateForm(sArea);
                            if(cCursor.moveToFirst()){
                                sProductStateFrom = cCursor.getString(0);
                            }
                            dbQrs.InsertMovimientoLog(sContainerNumber,sDate,sCheckPointGroup,sProductStateFrom,sArea,sBinId,"2", sContainerDesc);
                        }
                        dbQrs.CommitTransaction();
                    }
                }else{
                    sError = sError+"-400";
                }

                GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetCheckPointUsers(sUser), GetDataWS.class);
                if (GetData.getId().equals("1")){
                    dbQrs.DeleteCheckpointUsers();
                    if (GetData.getContent().contains(String.valueOf((char) 164))) {
                        String[] sData = GetData.getContent().split(String.valueOf((char) 164));
                        for (int n = 0; n < sData.length; n++) {
                            String strData = sData[n];
                            String sDataRow[] = strData.split(String.valueOf((char) 165));
                            String sCheckPointId = sDataRow[0];
                            String sEmployeId = sDataRow[1];
                            dbQrs.InsertCheckPointUsers(sCheckPointId, sEmployeId);
                        }
                    }
                }else{
                    sError = sError+"-400";
                }
                GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetCheckpoints, GetDataWS.class);
                if (GetData.getId().equals("1")){
                    dbQrs.DeleteCheckpoint();
                    if (GetData.getContent().contains(String.valueOf((char) 164))) {
                        String[] sData = GetData.getContent().split(String.valueOf((char) 164));
                        for (int n = 0; n < sData.length; n++) {
                            String strData = sData[n];
                            String sDataRow[] = strData.split(String.valueOf((char) 165));
                            String sCheckPointId = sDataRow[0];
                            String sDescription = sDataRow[1];
                            String sProductStateFrom = sDataRow[2];
                            String sCheckPointGroup = sDataRow[3];
                            dbQrs.InsertCheckpoints(sCheckPointId, sDescription, sProductStateFrom, sCheckPointGroup);
                        }
                    }
                }else{
                    sError = sError+"-400";
                }

                GetData = restTemplate.getForObject(cfData.sUrlGetRoutes(sUser), GetDataWS.class);
                if (GetData.getId().equals("1")){
                    dbQrs.DeleteRoutes();
                    if (GetData.getContent().contains(String.valueOf((char) 164))) {
                        String[] sData = GetData.getContent().split(String.valueOf((char) 164));
                        for (int n = 0; n < sData.length; n++) {
                            String strData = sData[n];
                            String sDataRow[] = strData.split(String.valueOf((char) 165));
                            String sRouteId = sDataRow[0];
                            String sDescription = sDataRow[1];
                            String sOriginAreaId = sDataRow[2];
                            String sDestinationAreaId = sDataRow[3];
                            String sPackingList = sDataRow[4];
                            dbQrs.InsertRoutes(sRouteId, sDescription, sOriginAreaId, sDestinationAreaId, sPackingList);
                        }
                    }
                }else{
                    sError = sError+"-400";
                }
                if (sError.contains("-400")){

                }else {
                    if(sUser.equals("3001150")){

                    }else {
                        dbQrs.DeleteAllTempContainer();
                    }
                    GetData = restTemplate.getForObject(cfData.sUrlWebServiceSetUpdateScannerSync+sScannerId+"&sLastSyncDate="+sFormattedDateSQLSyncStart, GetDataWS.class);
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
                    ShowCheckPointMenu();
                }
            }else{
                    if (pgProgressDialog != null) {
                        pgProgressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Datos sincronizados con exitó.", Toast.LENGTH_SHORT).show();
                        sError = "";
                        final SharedPreferences spPref = getSharedPreferences("UserData", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = spPref.edit();
                        editor.putString("updated", "true");
                        editor.commit();
                        ShowCheckPointMenu();
                    }
            }
        }
    }
    @Override
    public void onConfigurationChanged(final Configuration newConfig)
    {
        // Ignore orientation change to keep activity from restarting
        super.onConfigurationChanged(newConfig);
    }
    public  void  ShowCheckPointMenu(){
        llInventoryCheck.setVisibility(View.GONE);
        llInventoryOut.setVisibility(View.GONE);
        llInventoryOutArea.setVisibility(View.GONE);
        llInventoryIn.setVisibility(View.GONE);
        llInventoryInArea.setVisibility(View.GONE);
        llRelocation.setVisibility(View.GONE);
        llInventoryValidation.setVisibility(View.GONE);
        llInventoryAdd.setVisibility(View.GONE);
        llTintoCreate.setVisibility(View.GONE);
        llPanttrInv.setVisibility(View.GONE);
        llMarketDispatch.setVisibility(View.GONE);

        cCursor = dbQrs.GetCheckpointUsers();
        if(cCursor.moveToFirst()){
            do{
                String sCheckpointGroup = "";
                Cursor cCursor2 = dbQrs.GetCheckpointGroup(cCursor.getString(0));
                if(cCursor2.moveToFirst()) {
                    sCheckpointGroup = cCursor2.getString(0);
                }

                String sCheckpointId = cCursor.getString(0);
                if (sCheckpointId.equals("10")) {
                    llInventoryCheck.setVisibility(View.VISIBLE);
                }

                if (sCheckpointId.equals("11")) {
                    llInventoryAdd.setVisibility(View.VISIBLE);
                }

                if(sCheckpointId.equals("15")){
                    llInventoryOut.setVisibility(View.VISIBLE);
                }

                if(sCheckpointId.equals("16") || sCheckpointGroup.equals("ASND")){
                    llInventoryOutArea.setVisibility(View.VISIBLE);
                }

                if(sCheckpointId.equals("205")){
                    llInventoryIn.setVisibility(View.VISIBLE);
                    if(sUser.equals("3001150")) {
                        llPanttrInv.setVisibility(View.VISIBLE);
                        llMarketDispatch.setVisibility(View.VISIBLE);
                    }
                }

                if(sCheckpointId.equals("21") || sCheckpointGroup.equals("ARCV")){
                    llInventoryInArea.setVisibility(View.VISIBLE);
                }

                if(sCheckpointId.equals("25")){
                    llRelocation.setVisibility(View.VISIBLE);
                }

                if(sCheckpointId.equals("30")){
                    llInventoryValidation.setVisibility(View.VISIBLE);
                }
                if(sCheckpointId.equals("38")){
                    llTintoCreate.setVisibility(View.VISIBLE);
                }
            }while (cCursor.moveToNext());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case R.id.btnSendOfffline:

                if(isConnectedToNetwork()) {
                    pgProgressDialog = new ProgressDialog(frmMenu.this);
                    pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pgProgressDialog.setMessage("Enviando Offline...");
                    pgProgressDialog.setIndeterminate(true);
                    pgProgressDialog.setCancelable(false);
                    pgProgressDialog.show();
                    new SendOffline().execute();
                }else{
                    Toast.makeText(getApplicationContext(),"Necesita estar conectado para realizar esta acción", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.btnSincC:

                if (isConnectedToNetwork()) {
                    ShowModalSinc();
                }else{
                    Toast.makeText(getApplicationContext(),"Necesita estar conectado para realizar esta acción", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.btnSincP:
                if (isConnectedToNetwork()) {
                    pgProgressDialog = new ProgressDialog(frmMenu.this);
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
                ShowModalExit();
                return true;

            case R.id.btnOfflineM:
                Intent intent = new Intent(getApplicationContext(), frmOfflineM.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class SendOffline extends AsyncTask<Void, Void, SendOffline> {
        String sError = "";
        GetDataWS GetData;

        @Override
        protected SendOffline doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
               ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(cfData.iTimeOut);
                ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(cfData.iTimeOut);
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                cCursor = dbQrs.GetOfflineLogContainer();
                if(cCursor.moveToFirst()) {
                    do{
                        String sId = cCursor.getString(0);
                        String sQuery =  cCursor.getString(1);
                        GetData = restTemplate.getForObject(cfData.sUrlWebServiceInsertNewContainer + sQuery, GetDataWS.class);
                        if (GetData.getId().equals("1")) {
                            dbQrs.DeleteLogOfflineContainersById(sId);
                        }
                    }while(cCursor.moveToNext());

                }

                cCursor = dbQrs.GetOfflineLog();
                if(cCursor.moveToFirst()) {
                    do{
                        String sId = cCursor.getString(0);
                        String sQuery = cCursor.getString(1);
                        GetData = restTemplate.getForObject(cfData.sUrlWebServiceSetData(sQuery) , GetDataWS.class);
                        if (GetData.getId().equals("1")) {
                            dbQrs.DeleteLogOffline(sId);
                        }
                    }while(cCursor.moveToNext());
                }
            } catch (Exception e) {
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    sError = "404";
                }
                Log.e("MainActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(SendOffline SendOffline) {
            if (pgProgressDialog != null) {
                pgProgressDialog.dismiss();
                if (sError.equals("404")){
                    Toast.makeText(getApplicationContext(),"Error", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Enviado", Toast.LENGTH_SHORT).show();
                }
            }
            }
        }


    private void ShowModalExit() {
        try {
            LayoutInflater liLayoutInflater = LayoutInflater.from(frmMenu.this);
            View vPromptsView = liLayoutInflater.inflate(R.layout.modalexit, null);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(frmMenu.this);
            alertDialogBuilder.setView(vPromptsView);


            final ImageButton btnClose =  (ImageButton) vPromptsView.findViewById(R.id.btnClose);
            final ImageButton btnSearch = (ImageButton) vPromptsView.findViewById(R.id.btnSearch);


            alertDialogBuilder.setCancelable(false);
            final AlertDialog alertDialog = alertDialogBuilder.create();


            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    finish();
                    alertDialog.cancel();
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

    private void ShowModalSinc() {
        try {
            LayoutInflater liLayoutInflater = LayoutInflater.from(frmMenu.this);
            View vPromptsView = liLayoutInflater.inflate(R.layout.modalsinc, null);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(frmMenu.this);
            alertDialogBuilder.setView(vPromptsView);


            final ImageButton btnClose =  (ImageButton) vPromptsView.findViewById(R.id.btnClose);
            final ImageButton btnSearch = (ImageButton) vPromptsView.findViewById(R.id.btnSearch);


            alertDialogBuilder.setCancelable(false);
            final AlertDialog alertDialog = alertDialogBuilder.create();


            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    alertDialog.cancel();
                    final SharedPreferences spPref = getSharedPreferences("UserData", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = spPref.edit();
                    editor.putString("updated", "false");
                    editor.commit();
                    pgProgressDialog = new ProgressDialog(frmMenu.this);
                    pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                    pgProgressDialog.setMessage("Sincronizando Completo...");
                    pgProgressDialog.setIndeterminate(true);
                    pgProgressDialog.setCancelable(false);
                    pgProgressDialog.show();
                    new GetData().execute();
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
    @Override
    public void onBackPressed() {
        ShowModalExit();
    }



    }


