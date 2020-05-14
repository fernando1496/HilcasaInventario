/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.inventario.hilcasa.hilcasainventario.common.view.SlidingTabLayout;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class InventoryCheckTabs extends Fragment {

    static final String LOG_TAG = "InventoryCheckTabs";
    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    String sAreaId = "", sBinId = "", sContainerId = "", sContainerNumber = "";
    Cursor cCursor;
    private List<InventoryLog> LogList = new ArrayList<>();
    private List<InventoryLog> LogListHist = new ArrayList<>();
    private RecyclerView rvLog, rvLogHist;
    private InventoryLogAdapter mAdapter, mAdapetrHist;
    QueryDB dbQrs;
    ProgressDialog pgProgressDialog;
    ConfigData cfData = new ConfigData();
    String sQuery = "";
    String sScannerId = "";
    String sEmployeeId = "";
    Button lblScanned;
    EditText txtCode;
    TextView lblBin;
    TextView lblQuantity;
    FloatingActionButton fabClearLog, fabCalendar;
    MediaPlayer mpPlayer;
    int iRawOk;
    int iRawError;
    int iCont = 0;

    //PARA VENTANAS DE SETEO DE FECHA.
    AlertDialog alertDialog, alertDialogDate, alertDialogTime;
    String sSelectedDate = "";
    String sSelectedShowDate = "";
    String sSelectedTime = "";
    int iHour = 9999;
    int iMin = 9999;
    int iDay = 0;
    int iMonth = 0;
    int iYear = 0;
    String sOldDate = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.inventorycheckfragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // BEGIN_INCLUDE (setup_viewpager)
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new SamplePagerAdapter());
        // END_INCLUDE (setup_viewpager)

        // BEGIN_INCLUDE (setup_slidingtablayout)
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        // END_INCLUDE (setup_slidingtablayout)
    }

    class SamplePagerAdapter extends PagerAdapter {

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 2;
        }

        /**
         * @return true if the value returned from {@link #instantiateItem(ViewGroup, int)} is the
         * same object as the {@link View} added to the {@link ViewPager}.
         */
        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        // BEGIN_INCLUDE (pageradapter_getpagetitle)
        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {@link SlidingTabLayout}.
         * <p>
         * Here we construct one using the position value, but for real application the title should
         * refer to the item's contents.
         */
        @Override
        public CharSequence getPageTitle(int iTabName) {
            String sTabName = "";
            if(iTabName == 0){
                sTabName = "Toma de datos para inventario";
            }else if(iTabName == 1){
                sTabName = "Historial";
            }
            return (sTabName);
        }

        // END_INCLUDE (pageradapter_getpagetitle)

        /**
         * Instantiate the {@link View} which should be displayed at {@code position}. Here we
         * inflate a layout from the apps resources and then change the text view to signify the position.
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // Inflate a new layout from our resources
            View view;
            sScannerId = Settings.Secure.getString(getActivity().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            dbQrs = new QueryDB(getActivity().getApplicationContext());
            dbQrs.open();
            iCont = 0;
            iRawOk =  getResources().getIdentifier(cfData.sRawOk, "raw", getActivity().getPackageName());
            iRawError =  getResources().getIdentifier(cfData.sRawError, "raw", getActivity().getPackageName());


            cCursor = dbQrs.GetEmployeeInfo();
            if(cCursor.moveToFirst()){
                sEmployeeId = cCursor.getString(0);
            }
            if(position == 0){
                 view = getActivity().getLayoutInflater().inflate(R.layout.frmtabinventorycheck,
                        container, false);
                final ImageButton btnSearch = (ImageButton)view.findViewById(R.id.btnSearch);
                txtCode = (EditText)view.findViewById(R.id.txtCode);
                lblBin = (TextView)view.findViewById(R.id.lblBin);
                final Spinner spArea = (Spinner)view.findViewById(R.id.spArea);
                rvLog = (RecyclerView) view.findViewById(R.id.rvLog);
                lblScanned = (Button)view.findViewById(R.id.lblScanned);
                fabClearLog = (FloatingActionButton)view.findViewById(R.id.fabClearLog);
                txtCode.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

                cCursor = dbQrs.GetAreaByUser();
                getActivity().startManagingCursor(cCursor);
                SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.customspinnerlayout, cCursor,
                        new String[]{"description"}, new int[]{android.R.id.text1},1);
                mAdapter.setDropDownViewResource(R.layout.customspinnerlayout);
                spArea.setAdapter(mAdapter);
                spArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        Cursor crlCur = (Cursor) spArea
                                .getSelectedItem();

                        if(sAreaId.equals(crlCur.getColumnIndex("_id"))){

                        }else{
                            sAreaId = crlCur.getString(crlCur.getColumnIndex("_id"));
                            sBinId = "";
                            lblBin.setText("-");
                            ShowLog(sAreaId);
                            txtCode.setText("");
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                ShowLog(sAreaId);
                btnSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String sFlag = "";
                        final SharedPreferences spPref = getActivity().getSharedPreferences("UserData", Activity.MODE_PRIVATE);
                        sFlag = new String(spPref.getString("Lote", sFlag)).toString();

                        if(txtCode.getText().toString().length()>1){
                            Calendar cCalendar = Calendar.getInstance();
                            SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
                            String sFormattedDate = sdfFormat.format(cCalendar.getTime());
                            dbQrs.InsertLastLog(txtCode.getText().toString(),cfData.sIdentifierCheck,sFormattedDate);
                        }

                        if(sFlag.equals("0")){
                            SearchCode();
                        }else{
                            String sCodeMultilines = txtCode.getText().toString();
                            String[] sCodesArray;
                            String sDelimiter = "\n";
                            sCodesArray = sCodeMultilines.split(sDelimiter);
                            //SearchCode();
                            for (int i = 0; i < sCodesArray.length; i++) {
                                SearchCode(sCodesArray[i]);
                            }
                            iCont = 0;
                            lblScanned.setText(String.valueOf(iCont));
                            if (isConnectedToNetwork()){
                                if(sQuery.length()>1){
                                    new SetData().execute();
                                }
                            }
                        }
                    }
                });

                fabClearLog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dbQrs.UpdateLog();
                        ShowLog(sAreaId);
                    }
                });

                txtCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {



                            txtCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                                        String sFlag = "";
                                        final SharedPreferences spPref = getActivity().getSharedPreferences("UserData", Activity.MODE_PRIVATE);
                                        sFlag = new String(spPref.getString("Lote", sFlag)).toString();

                                        if(sFlag.equals("0")){
                                            txtCode.setMaxLines(1);
                                            SearchCode();
                                        }else{
                                            txtCode.setMaxLines(Integer.MAX_VALUE);
                                            txtCode.setText(txtCode.getText() + "\n");
                                            txtCode.setSelection(txtCode.getText().length());
                                            // SearchCode();

                                        }
                                    }
                                    return false;
                                }
                            });
                            String sFlag = "";
                            final SharedPreferences spPref = getActivity().getSharedPreferences("UserData", Activity.MODE_PRIVATE);
                            sFlag = new String(spPref.getString("Lote", sFlag)).toString();

                            if(sFlag.equals("0")){
                                if(txtCode.getText().toString().length()>1){
                                    Calendar cCalendar = Calendar.getInstance();
                                    SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
                                    String sFormattedDate = sdfFormat.format(cCalendar.getTime());
                                    dbQrs.InsertLastLog(txtCode.getText().toString(),cfData.sIdentifierCheck,sFormattedDate);
                                }
                                txtCode.setMaxLines(1);
                                SearchCode();

                            }else{
                                iCont = iCont + 1;
                                txtCode.setMaxLines(Integer.MAX_VALUE);
                                txtCode.setText(txtCode.getText() + "\n");
                                txtCode.setSelection(txtCode.getText().length());
                                lblScanned.setText(String.valueOf(iCont));
                               // SearchCode();

                            }
                        }
                        return false;
                    }
                });
            }else{
                 view = getActivity().getLayoutInflater().inflate(R.layout.frmtabinventorychecklog,
                        container, false);
                rvLogHist = (RecyclerView) view.findViewById(R.id.rvLogHist);
                lblQuantity = (TextView) view.findViewById(R.id.lblQuantity);
                fabCalendar = (FloatingActionButton)  view.findViewById(R.id.fabCalendar);
                ShowLogHist();
                HideKeyboard(view);

                fabCalendar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isConnectedToNetwork()) {
                            pgProgressDialog = new ProgressDialog(getActivity());
                            pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            pgProgressDialog.setMessage("Obteniendo...");
                            pgProgressDialog.setIndeterminate(true);
                            pgProgressDialog.setCancelable(false);
                            pgProgressDialog.show();
                            new GetUserDate().execute();
                        }else{
                            Toast.makeText(getActivity().getApplicationContext(), "Necesita estar conectado a la red para realizar esta accion.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            // Add the newly created View to the ViewPager
            container.addView(view);
            // Return the View
            return view;
        }
    }

    public void ShowLog(String sAreaId){
        LogList.clear();
        mAdapter = new InventoryLogAdapter(LogList, null);
        int iScanned = 0;
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rvLog.setLayoutManager(mLayoutManager);
        rvLog.setItemAnimator(new DefaultItemAnimator());
        rvLog.setAdapter(mAdapter);
        cCursor = dbQrs.GetLogList(sAreaId, cfData.sInventoryCheckCheckpointId );
        if(cCursor.moveToFirst()){
            InventoryLog ilLog;
            do{
                ilLog = new InventoryLog(cCursor.getString(0), cCursor.getString(2), cCursor.getString(1), cfData.sInventoryCheckCheckpointId, cCursor.getString(3));
                LogList.add(ilLog);
                mAdapter.notifyDataSetChanged();
                iScanned = iScanned + 1;
            }while (cCursor.moveToNext());
        }
        lblScanned.setText(String.valueOf(iScanned));
    }

    public void ShowLogHist(){
        LogListHist.clear();
        mAdapetrHist = new InventoryLogAdapter(LogListHist, null);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rvLogHist.setLayoutManager(mLayoutManager);
        rvLogHist.setItemAnimator(new DefaultItemAnimator());
        rvLogHist.setAdapter(mAdapetrHist);
        Cursor cCursor = dbQrs.GetLogListHist(cfData.sInventoryCheckCheckpointId );
        int iC= 0;
        if(cCursor.moveToFirst()){
            InventoryLog ilLog;
            do{
                iC = iC + 1;
                ilLog = new InventoryLog(cCursor.getString(0), cCursor.getString(2), cCursor.getString(1), "", cCursor.getString(4) + "Lb");
                LogListHist.add(ilLog);
                mAdapetrHist.notifyDataSetChanged();
            }while (cCursor.moveToNext());
        }
        lblQuantity.setText(String.valueOf(iC));
    }

    private class SetData extends AsyncTask<Void, Void, InventoryCheckTabs.SetData> {
        String sError = "";
        @Override
        protected InventoryCheckTabs.SetData doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(cfData.iTimeOut);
                //((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(cfData.iTimeOut);
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlWebServiceSetData(sQuery), GetDataWS.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
                //INSERTA A OFFLINE CUANDO HAY UN ERROR DE CUALQUIER TIPO
                dbQrs.InsertLogOffline(sQuery);
                sError = "-400";
            }
            return null;
        }

        @Override
        protected void onPostExecute(InventoryCheckTabs.SetData SetData) {
               // if (pgProgressDialog != null) {
                    //pgProgressDialog.dismiss();
                    ShowLog(sAreaId);
                    ShowLogHist();
                    sQuery = "";
                    sContainerNumber = "";
                    if(sError.equals("-400")){
                        Toast.makeText(getActivity().getApplicationContext(), "Ocurrio un error al enviar los datos, se almacenara localmente.", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getActivity().getApplicationContext(), "Registro enviado con éxito.", Toast.LENGTH_SHORT).show();
                    }
                    mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawOk);
                    mpPlayer.start();
                    //txtCode.setText("");
                //}
        }
    }

    public boolean isConnectedToNetwork() {
        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
        Boolean bState = false;

        if(level >= 2){
            ConnectivityManager connectivity = (ConnectivityManager) getActivity().getApplicationContext()
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
        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);

        if(level >= 2){
            ConnectivityManager connectivity = (ConnectivityManager)getActivity().getApplicationContext()
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

    private class GetContainerHist extends AsyncTask<Void, Void, InventoryCheckTabs.GetContainerHist> {
        GetDataWS GetData;
        String sError = "";
        @Override
        protected InventoryCheckTabs.GetContainerHist doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(cfData.iTimeOut);
                //((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(cfData.iTimeOut);
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetContainerHist+sContainerNumber, GetDataWS.class);
            } catch (Exception e) {
                sError = "-400";
                Log.e("MainActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(InventoryCheckTabs.GetContainerHist GetContainerHist) {
            if (GetData.getContent().contains(String.valueOf((char) 164))) {
                String[] sContainer = GetData.getContent().split(String.valueOf((char) 164));
                for (int n = 0; n < sContainer.length; n++) {
                    String strContainer = sContainer[n];
                    String sDatos[] = strContainer.split(String.valueOf((char) 165));
                    String sContainerId = sDatos[0];
                    String sContainerNumber = sDatos[1];
                    String sContainerDescription = sDatos[2];
                    String sContainerWeight = sDatos[3];
                    //String sContainerPart = sDatos[10];
                    //String sAreaId = sDatos[12];

                    Calendar cCalendar = Calendar.getInstance();
                    SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
                    SimpleDateFormat sdfFormatSQL = new SimpleDateFormat("yyyyMMdd hh:mm:ss a", Locale.US);
                    String sFormattedDate = sdfFormat.format(cCalendar.getTime());
                    String sFormattedDateSQL = sdfFormatSQL.format(cCalendar.getTime());
                    dbQrs.InsertLog(sAreaId, sBinId, sContainerId, sFormattedDate, sContainerNumber, sContainerWeight, sContainerDescription,"1",cfData.sInventoryCheckCheckpointId );
                    sQuery = cfData.sInventoryCheckCheckpointId + (char) 166 + sFormattedDateSQL + (char) 166 + sContainerNumber + (char) 166 + sAreaId + (char) 166 + sBinId + (char) 166 + sScannerId + (char)166 + sContainerWeight  + (char)166 + sEmployeeId + (char)166 + "CUERP" + (char)167;
                    new SetData().execute();
                }
            }else{
                //if (pgProgressDialog != null) {
                    //pgProgressDialog.dismiss();
                    if(sError.equals("-400")){
                        Toast.makeText(getActivity().getApplicationContext(), "Se ecnontro un problema al consultar los datos.", Toast.LENGTH_SHORT).show();
                        mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawError);
                        mpPlayer.start();
                    }else {
                        Toast.makeText(getActivity().getApplicationContext(), "No existe este registro", Toast.LENGTH_SHORT).show();
                        mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawError);
                        mpPlayer.start();
                    }
                    //txtCode.setText("");
                //}
            }
            }
        }

    public void SearchCode() {
        String sCode;
        sCode = txtCode.getText().toString().trim();
        txtCode.setText("");
        if (sCode.length() < 1) {//CUANDO ESTA VACIO
            /*Toast.makeText(getActivity().getApplicationContext(),"Ingrese un código valido.", Toast.LENGTH_SHORT).show();
            mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
            mpPlayer.start();*/
            //txtCode.setText("");
        } else if (sCode.length() == 12) {
            //CUANDO ES CONTAINER DE CONTAINER. EJ: TARA, TARIMA, LOTE
            if (sBinId.length() > 0) {
                cCursor = dbQrs.GetContainerByAreaAndBin(sCode);
                if (cCursor.moveToFirst()) {
                    sContainerId = cCursor.getString(0);
                    String sContainerWeight = cCursor.getString(1);
                    sContainerNumber = cCursor.getString(2);
                    String sContainerDesc = cCursor.getString(3);
                    Calendar cCalendar = Calendar.getInstance();
                    SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
                    SimpleDateFormat sdfFormatSQL = new SimpleDateFormat("yyyyMMdd hh:mm:ss a", Locale.US);
                    String sFormattedDate = sdfFormat.format(cCalendar.getTime());
                    String sFormattedDateSQL = sdfFormatSQL.format(cCalendar.getTime());

                    sQuery = cfData.sInventoryCheckCheckpointId + (char) 166 + sFormattedDateSQL + (char) 166 + sContainerNumber + (char) 166 + sAreaId + (char) 166 + sBinId + (char) 166 + sScannerId + (char) 166 + sContainerWeight + (char) 166 + sEmployeeId + (char) 166 + "CUERP" + (char) 167;

                    pgProgressDialog = new ProgressDialog(getActivity());
                    pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pgProgressDialog.setMessage("Enviando...");
                    pgProgressDialog.setIndeterminate(true);
                    pgProgressDialog.setCancelable(false);
                    //pgProgressDialog.show();

                    if (isConnectedToNetwork()) {
                        dbQrs.InsertLog(sAreaId, sBinId, sContainerId, sFormattedDate, sContainerNumber, sContainerWeight, sContainerDesc, "1", cfData.sInventoryCheckCheckpointId);
                        //txtCode.setText("");
                        new SetData().execute();
                    } else {
                        //MODO OFFLINE
                        //if (pgProgressDialog != null) {
                        // pgProgressDialog.dismiss();
                        dbQrs.InsertLog(sAreaId, sBinId, sContainerId, sFormattedDate, sContainerNumber, sContainerWeight, sContainerDesc, "1", cfData.sInventoryCheckCheckpointId);
                        dbQrs.InsertLogOffline(sQuery);
                        sQuery = "";
                        sContainerNumber = "";
                        ShowLog(sAreaId);
                        ShowLogHist();
                        mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawOk);
                        mpPlayer.start();
                        Toast.makeText(getActivity().getApplicationContext(), "Datos guardados localmente, enviarlos cuando se reestablezca la conexión.", Toast.LENGTH_SHORT).show();
                        //}
                    }
                } else {
                    //BUSCAR CONTAINER EN LA TABLA DE HISTORIAL
                    sContainerNumber = sCode;

                        if (isConnectedToNetwork()) {
                            pgProgressDialog = new ProgressDialog(getActivity());
                            pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            pgProgressDialog.setMessage("Enviando...");
                            pgProgressDialog.setIndeterminate(true);
                            pgProgressDialog.setCancelable(false);
                            //pgProgressDialog.show();
                            //txtCode.setText("");
                            new GetContainerHist().execute();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), "No tiene conexión a internet.", Toast.LENGTH_SHORT).show();
                            mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawError);
                            mpPlayer.start();
                            // txtCode.setText("");
                        }

                }
            } else {
                //EL BIN ESTA VACIO
                mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawError);
                mpPlayer.start();
                Toast.makeText(getActivity().getApplicationContext(), "Ingrese una ubicación primero.", Toast.LENGTH_SHORT).show();
                //txtCode.setText("");
            }
        } else if (sCode.length() == 14 || sCode.length() == 9) {
            //PARA CONTAINERS
            if (sBinId.length() > 0) {
                cCursor = dbQrs.GetContainerByAreaAndBin(sCode);
                if (cCursor.moveToFirst()) {
                sContainerId = cCursor.getString(0);
                String sContainerWeight = cCursor.getString(1);
                sContainerNumber = cCursor.getString(2);
                String sContainerDesc = cCursor.getString(3);
                Calendar cCalendar = Calendar.getInstance();
                SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
                SimpleDateFormat sdfFormatSQL = new SimpleDateFormat("yyyyMMdd hh:mm:ss a", Locale.US);
                String sFormattedDate = sdfFormat.format(cCalendar.getTime());
                String sFormattedDateSQL = sdfFormatSQL.format(cCalendar.getTime());
                dbQrs.InsertLog(sAreaId, sBinId, sContainerId, sFormattedDate, sContainerNumber, sContainerWeight, sContainerDesc, "1", cfData.sInventoryCheckCheckpointId);
                sQuery = cfData.sInventoryCheckCheckpointId + (char) 166 + sFormattedDateSQL + (char) 166 + sContainerNumber + (char) 166 + sAreaId + (char) 166 + sBinId + (char) 166 + sScannerId + (char) 166 + sContainerWeight + (char) 166 + sEmployeeId + (char) 166 + "CUERP" + (char) 167;

                if (isConnectedToNetwork()) {
                    pgProgressDialog = new ProgressDialog(getActivity());
                    pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pgProgressDialog.setMessage("Enviando...");
                    pgProgressDialog.setIndeterminate(true);
                    pgProgressDialog.setCancelable(false);
                    //pgProgressDialog.show();
                    //txtCode.setText("");
                    new SetData().execute();
                } else {
                    //MODO OFFLINE
                    // if (pgProgressDialog != null) {
                    //pgProgressDialog.dismiss();
                    dbQrs.InsertLog(sAreaId, sBinId, sContainerId, sFormattedDate, sContainerNumber, sContainerWeight, sContainerDesc, "1", cfData.sInventoryCheckCheckpointId);
                    dbQrs.InsertLogOffline(sQuery);
                    sQuery = "";
                    sContainerNumber = "";
                    ShowLog(sAreaId);
                    ShowLogHist();
                    mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawOk);
                    mpPlayer.start();
                    Toast.makeText(getActivity().getApplicationContext(), "Datos guardados localmente, enviarlos cuando se reestablezca la conexión.", Toast.LENGTH_SHORT).show();
                    // txtCode.setText("");
                    //}
                }
                /*} else {
                    //BUSCAR EL CONTAINER EN EL HISTORIAL
                    sContainerNumber = sCode;
                    if (isConnectedToInternet()) {
                        pgProgressDialog = new ProgressDialog(getActivity());
                        pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pgProgressDialog.setMessage("Enviando...");
                        pgProgressDialog.setIndeterminate(true);
                        pgProgressDialog.setCancelable(false);
                        //pgProgressDialog.show();
                        //txtCode.setText("");
                        new GetContainerHist().execute();
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "No tiene conexión a internet.", Toast.LENGTH_SHORT).show();
                        mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawError);
                        mpPlayer.start();
                        //txtCode.setText("");
                    }*/
                }else{
                    //NO EXISTE
                        sContainerId = "NA";
                        String sContainerWeight = "0";
                        sContainerNumber =sCode;
                        String sContainerDesc = "NO";
                        Calendar cCalendar = Calendar.getInstance();
                        SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
                        SimpleDateFormat sdfFormatSQL = new SimpleDateFormat("yyyyMMdd hh:mm:ss a", Locale.US);
                        String sFormattedDate = sdfFormat.format(cCalendar.getTime());
                        String sFormattedDateSQL = sdfFormatSQL.format(cCalendar.getTime());
                        dbQrs.InsertLog(sAreaId, sBinId, sContainerId, sFormattedDate, sContainerNumber, sContainerWeight, sContainerDesc, "1", cfData.sInventoryCheckCheckpointId);
                        sQuery = cfData.sInventoryCheckCheckpointId + (char) 166 + sFormattedDateSQL + (char) 166 + sContainerNumber + (char) 166 + sAreaId + (char) 166 + sBinId + (char) 166 + sScannerId + (char) 166 + sContainerWeight + (char) 166 + sEmployeeId + (char) 166 + "CUERP" + (char) 167;

                        if (isConnectedToNetwork()) {
                            pgProgressDialog = new ProgressDialog(getActivity());
                            pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            pgProgressDialog.setMessage("Enviando...");
                            pgProgressDialog.setIndeterminate(true);
                            pgProgressDialog.setCancelable(false);
                            //pgProgressDialog.show();
                            //txtCode.setText("");
                            new SetData().execute();
                        } else {
                            //MODO OFFLINE
                            // if (pgProgressDialog != null) {
                            //pgProgressDialog.dismiss();
                            dbQrs.InsertLog(sAreaId, sBinId, sContainerId, sFormattedDate, sContainerNumber, sContainerWeight, sContainerDesc, "1", cfData.sInventoryCheckCheckpointId);
                            dbQrs.InsertLogOffline(sQuery);
                            sQuery = "";
                            sContainerNumber = "";
                            ShowLog(sAreaId);
                            ShowLogHist();
                            mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawOk);
                            mpPlayer.start();
                            Toast.makeText(getActivity().getApplicationContext(), "Datos guardados localmente, enviarlos cuando se reestablezca la conexión.", Toast.LENGTH_SHORT).show();
                            // txtCode.setText("");
                            //}
                        }

                }
            } else {
                //EL BIN ESTA VACIO
                mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawError);
                mpPlayer.start();
                Toast.makeText(getActivity().getApplicationContext(), "Ingrese una ubicación primero.", Toast.LENGTH_SHORT).show();
                //txtCode.setText("");
            }
        } else if (sCode.length() == 13 || sCode.length() == 16) {
            //PARA BIN
            if (sCode.toString().matches(cfData.regexpBinFormat1) || sCode.matches(cfData.regexpBinFormat2)) {
                cCursor = dbQrs.GetBin(sAreaId, sCode);
                if (cCursor.moveToFirst()) {
                    sBinId = cCursor.getString(0);
                    lblBin.setText(cCursor.getString(1));
                    //txtCode.setText("");
                    mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawOk);
                    mpPlayer.start();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "No se encontro esta ubicación.", Toast.LENGTH_SHORT).show();
                    //txtCode.setText("");
                    mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawError);
                    mpPlayer.start();
                }/*}else{
                Toast.makeText(getActivity().getApplicationContext(),"Ingrese un código valido.", Toast.LENGTH_SHORT).show();
                //txtCode.setText("");
                mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
                mpPlayer.start();
            }*/
            } else if (sCode.equals("1")) {
                if (sBinId.length() > 0) {

                    sContainerId = "1";
                    String sContainerWeight = "0";
                    sContainerNumber = "1";
                    String sContainerDesc = "VACIO";
                    Calendar cCalendar = Calendar.getInstance();
                    SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
                    SimpleDateFormat sdfFormatSQL = new SimpleDateFormat("yyyyMMdd hh:mm:ss a", Locale.US);
                    String sFormattedDate = sdfFormat.format(cCalendar.getTime());
                    String sFormattedDateSQL = sdfFormatSQL.format(cCalendar.getTime());
                    dbQrs.InsertLog(sAreaId, sBinId, sContainerId, sFormattedDate, sContainerNumber, sContainerWeight, sContainerDesc, "1", cfData.sInventoryCheckCheckpointId);
                    sQuery = cfData.sInventoryCheckCheckpointId + (char) 166 + sFormattedDateSQL + (char) 166 + sContainerNumber + (char) 166 + sAreaId + (char) 166 + sBinId + (char) 166 + sScannerId + (char) 166 + sContainerWeight + (char) 166 + sEmployeeId + (char) 166 + "CUERP" + (char) 167;

                    if (isConnectedToNetwork()) {
                        pgProgressDialog = new ProgressDialog(getActivity());
                        pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pgProgressDialog.setMessage("Enviando...");
                        pgProgressDialog.setIndeterminate(true);
                        pgProgressDialog.setCancelable(false);
                        new SetData().execute();
                    } else {
                        //MODO OFFLINE
                        dbQrs.InsertLog(sAreaId, sBinId, sContainerId, sFormattedDate, sContainerNumber, sContainerWeight, sContainerDesc, "1", cfData.sInventoryCheckCheckpointId);
                        dbQrs.InsertLogOffline(sQuery);
                        sQuery = "";
                        sContainerNumber = "";
                        ShowLog(sAreaId);
                        ShowLogHist();
                        mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawOk);
                        mpPlayer.start();
                        Toast.makeText(getActivity().getApplicationContext(), "Datos guardados localmente, enviarlos cuando se reestablezca la conexión.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    //EL BIN ESTA VACIO
                    mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawError);
                    mpPlayer.start();
                    Toast.makeText(getActivity().getApplicationContext(), "Ingrese una ubicación primero.", Toast.LENGTH_SHORT).show();
                    //txtCode.setText("");
                }
            } else {
                //FORMATO NO EXISTE
                //.
                // Toast.makeText(getActivity().getApplicationContext(),"Ingrese un código valido.", Toast.LENGTH_SHORT).show();
            /*mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
            mpPlayer.start();*/
                //txtCode.setText("");
            }
        }
    }

    public void SearchCode(String sContainerNumberCode){
        String sCode;
        sCode = sContainerNumberCode;
        txtCode.setText("");
        if (sCode.length() < 1) {//CUANDO ESTA VACIO
           /* Toast.makeText(getActivity().getApplicationContext(),"Ingrese un código valido.", Toast.LENGTH_SHORT).show();
            mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
            mpPlayer.start();*/
            //txtCode.setText("");
        }else if(sCode.length() == 12) {
            //CUANDO ES CONTAINER DE CONTAINER. EJ: TARA, TARIMA, LOTE
            if (sBinId.length() > 0){
                cCursor = dbQrs.GetContainerByAreaAndBin(sCode);
                if (cCursor.moveToFirst()) {
                    sContainerId = cCursor.getString(0);
                    String sContainerWeight = cCursor.getString(1);
                    sContainerNumber = cCursor.getString(2);
                    String sContainerDesc = cCursor.getString(3);
                    Calendar cCalendar = Calendar.getInstance();
                    SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
                    SimpleDateFormat sdfFormatSQL = new SimpleDateFormat("yyyyMMdd hh:mm:ss a", Locale.US);
                    String sFormattedDate = sdfFormat.format(cCalendar.getTime());
                    String sFormattedDateSQL = sdfFormatSQL.format(cCalendar.getTime());

                    sQuery = sQuery + cfData.sInventoryCheckCheckpointId + (char) 166 + sFormattedDateSQL + (char) 166 + sContainerNumber + (char) 166 + sAreaId + (char) 166 + sBinId + (char) 166 + sScannerId + (char) 166 + sContainerWeight + (char) 166 + sEmployeeId + (char) 166 + "CUERP" + (char) 167;

                    pgProgressDialog = new ProgressDialog(getActivity());
                    pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pgProgressDialog.setMessage("Enviando...");
                    pgProgressDialog.setIndeterminate(true);
                    pgProgressDialog.setCancelable(false);
                    //pgProgressDialog.show();

                    if (isConnectedToNetwork()) {
                        dbQrs.InsertLog(sAreaId, sBinId, sContainerId, sFormattedDate, sContainerNumber, sContainerWeight, sContainerDesc, "1", cfData.sInventoryCheckCheckpointId);
                        //txtCode.setText("");

                    } else {
                        //MODO OFFLINE
                        //if (pgProgressDialog != null) {
                        // pgProgressDialog.dismiss();
                        dbQrs.InsertLog(sAreaId, sBinId, sContainerId, sFormattedDate, sContainerNumber, sContainerWeight, sContainerDesc, "1", cfData.sInventoryCheckCheckpointId);
                        dbQrs.InsertLogOffline(sQuery);
                        sQuery = "";
                        sContainerNumber = "";
                        ShowLog(sAreaId);
                        ShowLogHist();
                        mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawOk);
                        mpPlayer.start();
                        Toast.makeText(getActivity().getApplicationContext(), "Datos guardados localmente, enviarlos cuando se reestablezca la conexión.", Toast.LENGTH_SHORT).show();
                        //}
                    }
                } else {
                    //BUSCAR CONTAINER EN LA TABLA DE HISTORIAL
                    sContainerNumber = sCode;

                        if (isConnectedToNetwork()) {
                            pgProgressDialog = new ProgressDialog(getActivity());
                            pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            pgProgressDialog.setMessage("Enviando...");
                            pgProgressDialog.setIndeterminate(true);
                            pgProgressDialog.setCancelable(false);
                            //pgProgressDialog.show();
                            //txtCode.setText("");
                            new GetContainerHist().execute();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), "No tiene conexión a internet.", Toast.LENGTH_SHORT).show();
                            mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawError);
                            mpPlayer.start();
                            // txtCode.setText("");
                        }

                }
            }else{
                //EL BIN ESTA VACIO
                mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
                mpPlayer.start();
                Toast.makeText(getActivity().getApplicationContext(), "Ingrese una ubicación primero.", Toast.LENGTH_SHORT).show();
                //txtCode.setText("");
            }
        }else if(sCode.length() == 14) {
            //PARA CONTAINERS
            if (sBinId.length() > 0){
                cCursor = dbQrs.GetContainerByAreaAndBin(sCode);
                if (cCursor.moveToFirst()) {
                    sContainerId = cCursor.getString(0);
                    String sContainerWeight = cCursor.getString(1);
                    sContainerNumber = cCursor.getString(2);
                    String sContainerDesc = cCursor.getString(3);
                    Calendar cCalendar = Calendar.getInstance();
                    SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
                    SimpleDateFormat sdfFormatSQL = new SimpleDateFormat("yyyyMMdd hh:mm:ss a", Locale.US);
                    String sFormattedDate = sdfFormat.format(cCalendar.getTime());
                    String sFormattedDateSQL = sdfFormatSQL.format(cCalendar.getTime());
                    dbQrs.InsertLog(sAreaId, sBinId, sContainerId, sFormattedDate, sContainerNumber, sContainerWeight, sContainerDesc, "1", cfData.sInventoryCheckCheckpointId);
                    sQuery = sQuery + cfData.sInventoryCheckCheckpointId + (char) 166 + sFormattedDateSQL + (char) 166 + sContainerNumber + (char) 166 + sAreaId + (char) 166 + sBinId + (char) 166 + sScannerId + (char) 166 + sContainerWeight + (char) 166 + sEmployeeId + (char) 166 + "CUERP" + (char) 167;

                    if (isConnectedToNetwork()) {
                        pgProgressDialog = new ProgressDialog(getActivity());
                        pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pgProgressDialog.setMessage("Enviando...");
                        pgProgressDialog.setIndeterminate(true);
                        pgProgressDialog.setCancelable(false);
                        //pgProgressDialog.show();
                        //txtCode.setText("");
                        //new SetData().execute();
                    } else {
                        //MODO OFFLINE
                        // if (pgProgressDialog != null) {
                        //pgProgressDialog.dismiss();
                        dbQrs.InsertLog(sAreaId, sBinId, sContainerId, sFormattedDate, sContainerNumber, sContainerWeight, sContainerDesc, "1", cfData.sInventoryCheckCheckpointId);
                        dbQrs.InsertLogOffline(sQuery);
                        sQuery = "";
                        sContainerNumber = "";
                        ShowLog(sAreaId);
                        ShowLogHist();
                        mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawOk);
                        mpPlayer.start();
                        Toast.makeText(getActivity().getApplicationContext(), "Datos guardados localmente, enviarlos cuando se reestablezca la conexión.", Toast.LENGTH_SHORT).show();
                        // txtCode.setText("");
                        //}
                    }
                } else {
                    //BUSCAR EL CONTAINER EN EL HISTORIAL
                    sContainerNumber = sCode;
                    if (isConnectedToNetwork()) {
                        pgProgressDialog = new ProgressDialog(getActivity());
                        pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pgProgressDialog.setMessage("Enviando...");
                        pgProgressDialog.setIndeterminate(true);
                        pgProgressDialog.setCancelable(false);
                        //pgProgressDialog.show();
                        //txtCode.setText("");
                        new GetContainerHist().execute();
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "No tiene conexión a internet.", Toast.LENGTH_SHORT).show();
                        mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawError);
                        mpPlayer.start();
                        //txtCode.setText("");
                    }
                }
            }else{
                //EL BIN ESTA VACIO
                mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
                mpPlayer.start();
                Toast.makeText(getActivity().getApplicationContext(), "Ingrese una ubicación primero.", Toast.LENGTH_SHORT).show();
                //txtCode.setText("");
            }
        }else if(sCode.length() == 13 || sCode.length() == 16){
            //PARA BIN
            if (sCode.toString().matches(cfData.regexpBinFormat1)||sCode.matches(cfData.regexpBinFormat2)){
                cCursor = dbQrs.GetBin(sAreaId, sCode);
                if (cCursor.moveToFirst()) {
                    sBinId = cCursor.getString(0);
                    lblBin.setText(cCursor.getString(1));
                    //txtCode.setText("");
                    mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawOk);
                    mpPlayer.start();
                }else{
                    Toast.makeText(getActivity().getApplicationContext(),"No se encontro esta ubicación.", Toast.LENGTH_SHORT).show();
                    //txtCode.setText("");
                    mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
                    mpPlayer.start();
                }}else{
                Toast.makeText(getActivity().getApplicationContext(),"Ingrese un código valido.", Toast.LENGTH_SHORT).show();
                //txtCode.setText("");
                mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
                mpPlayer.start();
            }
        } else if(sCode.equals("1")) {
            if (sBinId.length() > 0){

                sContainerId = "1";
                String sContainerWeight = "0";
                sContainerNumber = "1";
                String sContainerDesc = "VACIO";
                Calendar cCalendar = Calendar.getInstance();
                SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
                SimpleDateFormat sdfFormatSQL = new SimpleDateFormat("yyyyMMdd hh:mm:ss a", Locale.US);
                String sFormattedDate = sdfFormat.format(cCalendar.getTime());
                String sFormattedDateSQL = sdfFormatSQL.format(cCalendar.getTime());
                dbQrs.InsertLog(sAreaId, sBinId, sContainerId, sFormattedDate, sContainerNumber, sContainerWeight, sContainerDesc, "1", cfData.sInventoryCheckCheckpointId);
                sQuery = cfData.sInventoryCheckCheckpointId + (char) 166 + sFormattedDateSQL + (char) 166 + sContainerNumber + (char) 166 + sAreaId + (char) 166 + sBinId + (char) 166 + sScannerId + (char) 166 + sContainerWeight + (char) 166 + sEmployeeId + (char) 166 + "CUERP" + (char) 167;

                if (isConnectedToNetwork()) {
                    pgProgressDialog = new ProgressDialog(getActivity());
                    pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pgProgressDialog.setMessage("Enviando...");
                    pgProgressDialog.setIndeterminate(true);
                    pgProgressDialog.setCancelable(false);
                    new SetData().execute();
                } else {
                    //MODO OFFLINE
                    dbQrs.InsertLog(sAreaId, sBinId, sContainerId, sFormattedDate, sContainerNumber, sContainerWeight, sContainerDesc, "1", cfData.sInventoryCheckCheckpointId);
                    dbQrs.InsertLogOffline(sQuery);
                    sQuery = "";
                    sContainerNumber = "";
                    ShowLog(sAreaId);
                    ShowLogHist();
                    mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawOk);
                    mpPlayer.start();
                    Toast.makeText(getActivity().getApplicationContext(), "Datos guardados localmente, enviarlos cuando se reestablezca la conexión.", Toast.LENGTH_SHORT).show();
                }

            }else{
                //EL BIN ESTA VACIO
                mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
                mpPlayer.start();
                Toast.makeText(getActivity().getApplicationContext(), "Ingrese una ubicación primero.", Toast.LENGTH_SHORT).show();
                //txtCode.setText("");
            }
        }else {
            //FORMATO NO EXISTE
            Toast.makeText(getActivity().getApplicationContext(),"Ingrese un código valido.", Toast.LENGTH_SHORT).show();
            mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
            mpPlayer.start();
            //txtCode.setText("");
        }
    }

    public void HideKeyboard(final View v){
        mViewPager.addOnPageChangeListener (new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                final InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                        INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }

            @Override
            public void onPageScrolled(int position, float offset, int offsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void ShowModalCalendar() {
        try {
            LayoutInflater liLayoutInflater = LayoutInflater.from(getActivity());
            View vPromptsView = liLayoutInflater.inflate(R.layout.modalcalendar, null);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setView(vPromptsView);

            final ImageButton btnClose =  (ImageButton) vPromptsView.findViewById(R.id.btnClose);
            final LinearLayout llModalContainer = (LinearLayout) vPromptsView.findViewById(R.id.llModalContainer);
            final ImageButton btnDate = (ImageButton)vPromptsView.findViewById(R.id.btnDate);
            final ImageButton btnTime = (ImageButton)vPromptsView.findViewById(R.id.btnTime);
            final ImageButton btnSearch = (ImageButton)vPromptsView.findViewById(R.id.btnSearch);
            final EditText txtDate = (EditText)vPromptsView.findViewById(R.id.txtDate);
            final EditText txtTime = (EditText)vPromptsView.findViewById(R.id.txtTime);
            final TextView lblOldDate = (TextView)vPromptsView.findViewById(R.id.lblOldDate);
            final ImageButton btnClearDate = (ImageButton) vPromptsView.findViewById(R.id.btnClearDate);


            if(sOldDate.equals("") || sOldDate.equals("Null")){
                lblOldDate.setText("Ultima: -");
            }else{
                SimpleDateFormat sFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a");
                Date sNewDate = sFormat.parse(sOldDate);

                sFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
                String sOldDate2 = sFormat.format(sNewDate);
                lblOldDate.setText("Ultima: " +sOldDate2);
            }
            txtDate.setText(sSelectedShowDate);
            txtTime.setText(sSelectedTime);



            alertDialogBuilder.setCancelable(false);
            alertDialog = alertDialogBuilder.create();

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.cancel();
                }
            });

            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(sSelectedTime.equals("") || sSelectedDate.equals("")){
                        Toast.makeText(getActivity().getApplicationContext(),"Complete los datos requeridos.", Toast.LENGTH_LONG).show();
                    }else{
                        if(isConnectedToNetwork()){
                            pgProgressDialog = new ProgressDialog(getActivity());
                            pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            pgProgressDialog.setMessage("Sincronizando...");
                            pgProgressDialog.setIndeterminate(true);
                            pgProgressDialog.setCancelable(false);
                            pgProgressDialog.show();
                            new SetUserDate().execute();

                        }else{
                            Toast.makeText(getActivity().getApplicationContext(), "Es necesario estar conectado a una red para realizar esta accion.",Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

            btnClearDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isConnectedToNetwork()){
                        pgProgressDialog = new ProgressDialog(getActivity());
                        pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pgProgressDialog.setMessage("Sincronizando...");
                        pgProgressDialog.setIndeterminate(true);
                        pgProgressDialog.setCancelable(false);
                        pgProgressDialog.show();
                        new DeleteUserDate().execute();

                    }else{
                        Toast.makeText(getActivity().getApplicationContext(), "Es necesario estar conectado a una red para realizar esta accion.",Toast.LENGTH_LONG).show();
                    }
                }
            });

            btnDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowModalSetDate();
                    alertDialog.cancel();
                }
            });

            btnTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowModalSetTime();
                    alertDialog.cancel();
                }
            });


            alertDialog.show();

        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
    }
    private void ShowModalSetDate() {
        try {
            LayoutInflater liLayoutInflater = LayoutInflater.from(getActivity());
            View vPromptsView = liLayoutInflater.inflate(R.layout.modalsetdate, null);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setView(vPromptsView);

            final ImageButton btnSearch =  (ImageButton) vPromptsView.findViewById(R.id.btnSearch);
            final ImageButton btnClose =  (ImageButton) vPromptsView.findViewById(R.id.btnClose);
            final LinearLayout llModalContainer = (LinearLayout) vPromptsView.findViewById(R.id.llModalContainer);
            final DatePicker dpDate = (DatePicker) vPromptsView.findViewById(R.id.dpDate);


            final int iHours = 24*3600000;
            final Long dMinDate = (System.currentTimeMillis() - iHours);
            final Long dMaxDate = (System.currentTimeMillis());

            dpDate.setMinDate(dMinDate);
            dpDate.setMaxDate(dMaxDate);

            alertDialogBuilder.setCancelable(false);
            alertDialogDate = alertDialogBuilder.create();

            if(iYear != 0 && iDay != 0){
                dpDate.updateDate(iYear,iMonth,iDay);
            }

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowModalCalendar();
                    alertDialogDate.cancel();
                }
            });

            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    iYear = dpDate.getYear();
                    iMonth = dpDate.getMonth();
                    iDay = dpDate.getDayOfMonth();
                    Calendar cCalendar = Calendar.getInstance();
                    cCalendar.set(dpDate.getYear(), dpDate.getMonth(), dpDate.getDayOfMonth());
                    Date dtDate = cCalendar.getTime();
                    SimpleDateFormat sdfDateShow = new SimpleDateFormat("dd-MMM-yyyy");
                    String sShowDate = sdfDateShow.format(dtDate);
                    sSelectedShowDate = sShowDate;
                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd");
                    String sDate = sdfDate.format(dtDate);
                    sSelectedDate = sDate;
                    ShowModalCalendar();
                    alertDialogDate.cancel();

                }
            });
            alertDialogDate.show();

        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
    }
    private void ShowModalSetTime() {
        try {
            LayoutInflater liLayoutInflater = LayoutInflater.from(getActivity());
            View vPromptsView = liLayoutInflater.inflate(R.layout.modalsettime, null);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setView(vPromptsView);

            final ImageButton btnSearch =  (ImageButton) vPromptsView.findViewById(R.id.btnSearch);
            final ImageButton btnClose =  (ImageButton) vPromptsView.findViewById(R.id.btnClose);
            final LinearLayout llModalContainer = (LinearLayout) vPromptsView.findViewById(R.id.llModalContainer);
            final TimePicker tpTime = (TimePicker)vPromptsView.findViewById(R.id.tpTime);

            tpTime.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
            tpTime.setIs24HourView(true);
            alertDialogBuilder.setCancelable(false);
            alertDialogTime = alertDialogBuilder.create();

            if(iHour != 9999 && iMin != 9999){
                tpTime.setCurrentHour(iHour);
                tpTime.setCurrentMinute(iMin);
            }


            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Calendar cCalendar = Calendar.getInstance();
                    cCalendar.set(Calendar.HOUR_OF_DAY,tpTime.getCurrentHour());
                    cCalendar.set(Calendar.MINUTE,tpTime.getCurrentMinute());
                    cCalendar.clear(Calendar.SECOND);
                    Date dtDate = cCalendar.getTime();

                    iHour = tpTime.getCurrentHour();
                    iMin = tpTime.getCurrentMinute();

                    SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss");
                    String sTime = sdfDate.format(dtDate);


                    sSelectedTime = sTime;
                    ShowModalCalendar();
                    alertDialogTime.cancel();
                }
            });

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowModalCalendar();
                    alertDialogTime.cancel();

                }
            });
            alertDialogTime.show();

        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
    }
    private class SetUserDate extends AsyncTask<Void, Void, SetUserDate> {
        String sError="";
        String sQuery = "";
        @Override
        protected SetUserDate doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlSetEmployeeIdUserDate(sEmployeeId, sSelectedDate + " " + sSelectedTime), GetDataWS.class);

                if (GetData.getId().equals("1")){
                    GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetLog(sEmployeeId), GetDataWS.class);
                    if (GetData.getId().equals("1")){
                        dbQrs.DeleteLog();
                        if (GetData.getContent().contains(String.valueOf((char) 164))) {
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
                        }
                    }else{
                        sError = sError+"-400";
                    }
                    GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetLogArea + sEmployeeId, GetDataWS.class);
                    if (GetData.getId().equals("1")){
                        dbQrs.DeleteLogMovimiento();
                        if (GetData.getContent().contains(String.valueOf((char) 164))) {
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
                                dbQrs.InsertMovimientoLog(sContainerNumber,sDate,sCheckPointGroup,sProductStateFrom,sArea,sBinId,"2", "Nuevo registro");
                            }
                        }
                    }else{
                        sError = sError+"-400";
                    }
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
        protected void onPostExecute(SetUserDate GetData) {
            int iCont = 0;
            if (sError.contains("-400")){
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    alertDialog.cancel();
                    Toast.makeText(getActivity().getApplicationContext(), "Ha ocurrido un problema.", Toast.LENGTH_SHORT).show();
                    sError = "";
                }
            }else {
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    alertDialog.cancel();
                    ShowLogHist();
                    Toast.makeText(getActivity().getApplicationContext(), "Datos obtenidos con exitó.", Toast.LENGTH_SHORT).show();
                    sError = "";
                }
            }
        }
    }
    private class GetUserDate extends AsyncTask<Void, Void, SetUserDate> {
        String sError="";
        String sQuery = "";
        @Override
        protected SetUserDate doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlGetCurrentDate(sEmployeeId), GetDataWS.class);

                if (GetData.getId().equals("1")){

                    if (GetData.getContent().contains(String.valueOf((char) 164))) {
                        String[] sData = GetData.getContent().split(String.valueOf((char) 164));
                        for (int n = 0; n < sData.length; n++) {
                            String strData = sData[n];
                            String sDataRow[] = strData.split(String.valueOf((char) 165));
                            sOldDate  = sDataRow[1];

                        }
                    }
                }else{
                    sError = sError+"-400";
                }
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
                sError = sError + "-400";
            }
            return null;
        }

        @Override
        protected void onPostExecute(SetUserDate GetData) {
            int iCont = 0;
            if (sError.contains("-400")){
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    alertDialog.cancel();
                    Toast.makeText(getActivity().getApplicationContext(), "Ha ocurrido un problema.", Toast.LENGTH_SHORT).show();
                    sError = "";

                }
            }else {
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    ShowModalCalendar();
                    sError = "";
                }
            }
        }
    }
    private class DeleteUserDate extends AsyncTask<Void, Void, SetUserDate> {
        String sError="";
        String sQuery = "";
        @Override
        protected SetUserDate doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlDeleteEmployeeIdUserDate(sEmployeeId), GetDataWS.class);

                if (GetData.getId().equals("1")){
                    GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetLog(sEmployeeId), GetDataWS.class);
                    if (GetData.getId().equals("1")){
                        dbQrs.DeleteLog();
                        if (GetData.getContent().contains(String.valueOf((char) 164))) {
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
                        }
                    }else{
                        sError = sError+"-400";
                    }
                    GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetLogArea + sEmployeeId, GetDataWS.class);
                    if (GetData.getId().equals("1")){
                        dbQrs.DeleteLogMovimiento();
                        if (GetData.getContent().contains(String.valueOf((char) 164))) {
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
                                dbQrs.InsertMovimientoLog(sContainerNumber,sDate,sCheckPointGroup,sProductStateFrom,sArea,sBinId,"2", "Nuevo registro");
                            }
                        }
                    }else{
                        sError = sError+"-400";
                    }
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
        protected void onPostExecute(SetUserDate GetData) {
            int iCont = 0;
            if (sError.contains("-400")){
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    alertDialog.cancel();
                    Toast.makeText(getActivity().getApplicationContext(), "Ha ocurrido un problema.", Toast.LENGTH_SHORT).show();
                    sError = "";
                }
            }else {
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    alertDialog.cancel();
                    ShowLogHist();
                    Toast.makeText(getActivity().getApplicationContext(), "Datos obtenidos con exitó.", Toast.LENGTH_SHORT).show();
                    sError = "";
                }
            }
        }
    }

}
