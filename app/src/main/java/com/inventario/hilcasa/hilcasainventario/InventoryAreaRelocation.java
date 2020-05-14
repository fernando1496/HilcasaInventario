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
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.util.Log;
import android.util.TypedValue;
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
import android.widget.RelativeLayout;
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


public class InventoryAreaRelocation extends Fragment {

    static final String LOG_TAG = "InventoryInAreaTabs";
    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    QueryDB dbQrs;
    Cursor cCursor;
    String sUser = "";
    private ProgressDialog pgProgressDialog;
    ConfigData cfData = new ConfigData();
    Spinner spArea;
    String sProductStateFrom = "";
    String sAreaId = "";
    ImageButton btnSearch;
    String sQuery = "";
    String sBinId = "";
    String sCheckPoint = "";
    String sScannerId = "";
    String sContainerNumber = "";
    String sContainerId = "";
    EditText txtCode;
    TextView lblBin;
    Button lblScanned;
    TextView lblQuantity;

    MediaPlayer mpPlayer;
    int iRawOk;
    int iRawError;
    private List<InventoryLogRcvArea> LogList = new ArrayList<>();
    private List<InventoryLogRcvArea> LogListHist = new ArrayList<>();
    private RecyclerView rvLog, rvLogHist;
    private InventoryLogRcvAreaAdapter mAdapter, mAdapetrHist;
    FloatingActionButton fabClearLog, fabCalendar;

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
        return inflater.inflate(R.layout.inventoryrelocationfragment, container, false);
    }

    // BEGIN_INCLUDE (fragment_onviewcreated)
    /**
     * This is called after the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished.
     * Here we can pick out the {@link View}s we need to configure from the content view.
     *
     * We set the {@link ViewPager}'s adapter to be an instance of {@link SamplePagerAdapter}. The
     * {@link SlidingTabLayout} is then given the {@link ViewPager} so that it can populate itself.
     *
     * @param view View created in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
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
    // END_INCLUDE (fragment_onviewcreated)

    /**
     * The {@link PagerAdapter} used to display pages in this sample.
     * The individual pages are simple and just display two lines of text. The important section of
     * this class is the {@link #getPageTitle(int)} method which controls what is displayed in the
     * {@link SlidingTabLayout}.
     */
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
                sTabName = "Re-ubicación";
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

            iRawOk =  getResources().getIdentifier(cfData.sRawOk, "raw", getActivity().getPackageName());
            iRawError =  getResources().getIdentifier(cfData.sRawError, "raw", getActivity().getPackageName());

            cCursor = dbQrs.GetEmployeeInfo();
            if(cCursor.moveToFirst()){
                sUser = cCursor.getString(0);
            }
            if(position == 0){
                view = getActivity().getLayoutInflater().inflate(R.layout.frmtabinventoryrelocation,
                        container, false);

                sScannerId = Settings.Secure.getString(getActivity().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

                spArea = (Spinner)view.findViewById(R.id.spArea);
                btnSearch = (ImageButton)view.findViewById(R.id.btnSearch);
                txtCode = (EditText) view.findViewById(R.id.txtCode);
                lblBin = (TextView)view.findViewById(R.id.lblBin);
                rvLog = (RecyclerView) view.findViewById(R.id.rvLog);
                fabClearLog = (FloatingActionButton)view.findViewById(R.id.fabClearLog);
                lblScanned = (Button)view.findViewById(R.id.lblScanned);
                txtCode.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

                //ABRE LA CONEXION A LA BD PARA PODER REALIZAR LAS CONSULTAS
                dbQrs.open();
                //OBTIENE EL USUARIO LOGEADO
                cCursor = dbQrs.GetEmployeeInfo();
                if(cCursor.moveToFirst()){
                    sUser = cCursor.getString(0);
                }

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
                        sProductStateFrom = crlCur.getString(crlCur.getColumnIndex("productstatefrom"));
                        sAreaId = crlCur.getString(crlCur.getColumnIndex("_id"));

                        sCheckPoint = cfData.sInventoryRelocationCheckpointId;

                        /*Cursor cTempCursor = dbQrs.GetBinByAreaAndAisle(sAreaId, cfData.sAisleRcv);
                        if(cTempCursor.moveToFirst()){
                            sBinId = cTempCursor.getString(0);
                            lblBin.setText(cTempCursor.getString(1));
                            //Toast.makeText(getApplicationContext(), cTempCursor.getString(0), Toast.LENGTH_SHORT).show();
                        }else{
                            sBinId = "";
                            lblBin.setText("-");
                        }*/
                        ShowLog(sAreaId);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                fabClearLog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dbQrs.UpdateLogMovimiento();
                        ShowLog(sAreaId);
                    }
                });


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
                            dbQrs.InsertLastLog(txtCode.getText().toString(),cfData.sIdentifierRelocation,sFormattedDate);
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
                            if (isConnectedToNetwork()){
                                if(sQuery.length()>1){
                                    pgProgressDialog = new ProgressDialog(getActivity());
                                    pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    pgProgressDialog.setMessage("Enviando...");
                                    pgProgressDialog.setIndeterminate(true);
                                    pgProgressDialog.setCancelable(false);
                                    pgProgressDialog.show();
                                new SetData().execute();
                                }
                            }
                        }
                    }
                });
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
                ShowLog(sAreaId);
            }else{
                 view = getActivity().getLayoutInflater().inflate(R.layout.frmtabinventoryrelocationlog,
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
    public boolean isConnectedToNetwork() {
        WifiManager wifiManager = (WifiManager)  getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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
        ConnectivityManager connectivity = (ConnectivityManager)  getActivity().getApplicationContext()
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

    private class SetData extends AsyncTask<Void, Void, SetData> {
        String sError = "";
        @Override
        protected SetData doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(cfData.iTimeOut);
                //((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(cfData.iTimeOut);
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlWebServiceSetDataUpdateContainer+sQuery, GetDataWS.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
                //INSERTA A OFFLINE CUANDO HAY UN ERROR DE CUALQUIER TIPO
                dbQrs.EndTransaction();
                dbQrs.InsertLogOffline(sQuery);
                sError = "-400";
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(SetData SetData) {
            if (pgProgressDialog != null) {
                pgProgressDialog.dismiss();
            }
                sQuery = "";
                sContainerNumber = "";
                if(sError.equals("-400")){
                    Toast.makeText(getActivity().getApplicationContext(), "Ocurrió un error al enviar los datos, se almacenara localmente.", Toast.LENGTH_SHORT).show();
                }else{
                    dbQrs.CommitTransaction();
                    Toast.makeText(getActivity().getApplicationContext(), "Registro enviado con éxito.", Toast.LENGTH_SHORT).show();
                }
                mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawOk);
                mpPlayer.start();
                ShowLogHist();
                ShowLog(sAreaId);
            //}
        }
    }


    private class SetDataModal extends AsyncTask<Void, Void, SetDataModal> {
        String sError = "";
        @Override
        protected SetDataModal doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(cfData.iTimeOut);
                //((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(cfData.iTimeOut);
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlWebServiceSetDataUpdateContainer+sQuery, GetDataWS.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
                //INSERTA A OFFLINE CUANDO HAY UN ERROR DE CUALQUIER TIPO
                dbQrs.InsertLogOffline(sQuery);
                sError = "-400";
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(SetDataModal SetDataModal) {
            if (pgProgressDialog != null) {
                pgProgressDialog.dismiss();
            }
            sQuery = "";
            sContainerNumber = "";
            alertDialog.dismiss();
            if(sError.equals("-400")){
                Toast.makeText(getActivity().getApplicationContext(), "Ocurrió un error al enviar los datos, se almacenara localmente.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity().getApplicationContext(), "Registro enviado con éxito.", Toast.LENGTH_SHORT).show();
            }
            //}
        }
    }
    public void ShowLog(String sAreaId){
        LogList.clear();
        int iScanned = 0;
        mAdapter = new InventoryLogRcvAreaAdapter(LogList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rvLog.setLayoutManager(mLayoutManager);
        rvLog.setItemAnimator(new DefaultItemAnimator());
        rvLog.setAdapter(mAdapter);
        cCursor = dbQrs.GetLogMovimiento(cfData.sCheckpointGroupRelocation, sAreaId);
        if(cCursor.moveToFirst()){
            InventoryLogRcvArea ilLog;
            do{
                ilLog = new InventoryLogRcvArea(cCursor.getString(0), cCursor.getString(1), cCursor.getString(2), cCursor.getString(3), cCursor.getString(4));
                LogList.add(ilLog);
                mAdapter.notifyDataSetChanged();
                iScanned = iScanned + 1;
            }while (cCursor.moveToNext());
        }
        lblScanned.setText(String.valueOf(iScanned));
    }

    public void ShowLogHist(){
        LogListHist.clear();
        mAdapetrHist = new InventoryLogRcvAreaAdapter(LogListHist);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rvLogHist.setLayoutManager(mLayoutManager);
        rvLogHist.setItemAnimator(new DefaultItemAnimator());
        rvLogHist.setAdapter(mAdapetrHist);
        Cursor cCursor = dbQrs.GetLogMovimientoHist(cfData.sCheckpointGroupRelocation);
        int iC= 0;
        if(cCursor.moveToFirst()){
            InventoryLogRcvArea ilLog;
            do{
                iC = iC + 1;
                ilLog = new InventoryLogRcvArea(cCursor.getString(0), cCursor.getString(1), cCursor.getString(2), cCursor.getString(3), cCursor.getString(4));
                LogListHist.add(ilLog);
                mAdapetrHist.notifyDataSetChanged();
            }while (cCursor.moveToNext());
        }
        lblQuantity.setText(String.valueOf(iC));
    }

    public void SearchCode(){
        sQuery = "";
        String sCode = txtCode.getText().toString().trim();
        txtCode.setText("");
        if (sCode.length() >= 1) {
             //if(sCode.length() == 13 || sCode.length() == 16){
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
                    /*}}else{
                    Toast.makeText(getActivity().getApplicationContext(),"Ingrese un código valido.", Toast.LENGTH_SHORT).show();
                    //txtCode.setText("");
                    mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
                    mpPlayer.start();*/
                }
            }else {
                 if (sBinId.length() > 0){
                 if (BinTaken()){
                     cCursor = dbQrs.GetContainerByAreaAndBinByArea(sCode, sAreaId);
                 if (cCursor.moveToFirst()) {
                     sContainerId = cCursor.getString(0);
                     String sContainerWeight = cCursor.getString(1);
                     sContainerNumber = cCursor.getString(2);
                     Calendar cCalendar = Calendar.getInstance();
                     SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
                     SimpleDateFormat sdfFormatSQL = new SimpleDateFormat("yyyyMMdd hh:mm:ss a", Locale.US);
                     String sFormattedDate = sdfFormat.format(cCalendar.getTime());
                     String sFormattedDateSQL = sdfFormatSQL.format(cCalendar.getTime());
                     sQuery = sCheckPoint + (char) 166 + sFormattedDateSQL + (char) 166 + sContainerNumber + (char) 166 + sAreaId + (char) 166 + sBinId + (char) 166 + sScannerId + (char) 166 + sContainerWeight + (char) 166 + sUser + (char) 166 + "CUERP" + (char) 167;
                     String sStatusInv = cCursor.getString(2);
                     if (isConnectedToNetwork()) {
                         pgProgressDialog = new ProgressDialog(getActivity());
                         pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                         pgProgressDialog.setMessage("Enviando...");
                         pgProgressDialog.setIndeterminate(true);
                         pgProgressDialog.setCancelable(false);
                         pgProgressDialog.show();
                         dbQrs.BeginTransaction();
                         dbQrs.InsertMovimientoLog(sContainerNumber, sFormattedDate, cfData.sCheckpointGroupRelocation, sProductStateFrom, sAreaId, sBinId, "1","Nuevo registro");
                         dbQrs.UpdateContainersSpecific(sContainerNumber, sFormattedDate, sBinId, sAreaId);

                         new SetData().execute();
                     } else {
                         dbQrs.InsertMovimientoLog(sContainerNumber, sFormattedDate, cfData.sCheckpointGroupRelocation, sProductStateFrom, sAreaId, sBinId, "1", "Nuevo registro");
                         dbQrs.InsertLogOffline(sQuery);
                         dbQrs.UpdateContainersSpecific(sContainerNumber, sFormattedDate, sBinId, sAreaId);
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
                     Toast.makeText(getActivity().getApplicationContext(), "No existe un item con este código.", Toast.LENGTH_SHORT).show();

                     mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawError);
                     mpPlayer.start();
                 }
             }else{
                     Toast.makeText(getActivity().getApplicationContext(), "Ubicación ocupada", Toast.LENGTH_SHORT).show();
                     mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
                     mpPlayer.start();
                     SendToRcv();
                 }
             }else{
                Toast.makeText(getActivity().getApplicationContext(), "Ingrese una ubicación primero.", Toast.LENGTH_SHORT).show();
                mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
                mpPlayer.start();
            }
        }
        } else {
           /* Toast.makeText(getActivity().getApplicationContext(), "Ingrese un código valido.", Toast.LENGTH_SHORT).show();
            mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
            mpPlayer.start();*/
        }
    }

    public void SearchCode(String sContainerNumberCode){

        //sQuery = "";
        String sCode = sContainerNumberCode;
        txtCode.setText("");
        if (sCode.length() >= 1) {
           /* if(sCode.length() == 13 || sCode.length() == 16){*/
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
                   /* }}else{
                    Toast.makeText(getActivity().getApplicationContext(),"Ingrese un código valido.", Toast.LENGTH_SHORT).show();
                    //txtCode.setText("");
                    mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
                    mpPlayer.start();*/
                }
            }else{
                if (sBinId.length() > 0){
                if(BinTaken()){
                    cCursor = dbQrs.GetContainerByAreaAndBinByArea(sCode, sAreaId);
                    if (cCursor.moveToFirst()) {
                        sContainerId = cCursor.getString(0);
                        String sContainerWeight = cCursor.getString(1);
                        sContainerNumber = cCursor.getString(2);
                        Calendar cCalendar = Calendar.getInstance();
                        SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
                        SimpleDateFormat sdfFormatSQL = new SimpleDateFormat("yyyyMMdd hh:mm:ss a", Locale.US);
                        String sFormattedDate = sdfFormat.format(cCalendar.getTime());
                        String sFormattedDateSQL = sdfFormatSQL.format(cCalendar.getTime());
                        sQuery = sQuery + sCheckPoint + (char) 166 + sFormattedDateSQL + (char) 166 + sContainerNumber + (char) 166 + sAreaId + (char) 166 + sBinId + (char) 166 + sScannerId + (char) 166 + sContainerWeight + (char) 166 + sUser + (char) 166 + "CUERP" + (char) 167;
                        if (isConnectedToNetwork()) {
                            pgProgressDialog = new ProgressDialog(getActivity());
                            pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            pgProgressDialog.setMessage("Enviando...");
                            pgProgressDialog.setIndeterminate(true);
                            pgProgressDialog.setCancelable(false);
                            pgProgressDialog.show();
                            dbQrs.InsertMovimientoLog(sContainerNumber, sFormattedDate, cfData.sCheckpointGroupRelocation, sProductStateFrom, sAreaId, sBinId, "1", "Nuevo registro");
                            dbQrs.UpdateContainersSpecific(sContainerNumber, sFormattedDate, sBinId, sAreaId);
                            // new SetData().execute();
                        } else {
                            /*if (pgProgressDialog != null) {
                                pgProgressDialog.dismiss();*/
                            dbQrs.InsertMovimientoLog(sContainerNumber, sFormattedDate, cfData.sCheckpointGroupRelocation, sProductStateFrom, sAreaId, sBinId, "1", "Nuevo registro");
                            dbQrs.InsertLogOffline(sQuery);
                            dbQrs.UpdateContainersSpecific(sContainerNumber, sFormattedDate, sBinId, sAreaId);
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
                        Toast.makeText(getActivity().getApplicationContext(), "No existe un item con este código.", Toast.LENGTH_SHORT).show();

                        mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawError);
                        mpPlayer.start();
                    }
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "Ubicación ocupada", Toast.LENGTH_SHORT).show();
                    mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
                    mpPlayer.start();
                    SendToRcv();
                }
            }else{
                Toast.makeText(getActivity().getApplicationContext(), "Ingrese una ubicación primero.", Toast.LENGTH_SHORT).show();
                mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
                mpPlayer.start();
            }
        }
        } else {
            /*Toast.makeText(getActivity().getApplicationContext(), "Ingrese un codigo valido.", Toast.LENGTH_SHORT).show();
            mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
            mpPlayer.start();*/
        }

    }

    public Boolean BinTaken(){
        int iPackQuantity = 0;
        int iContainerInBin = 0;
        cCursor = dbQrs.GetBinQuantity(sBinId);
        if(cCursor.moveToFirst()){
            iPackQuantity = cCursor.getInt(0);
        }

        cCursor = dbQrs.GetContainerInBin(sBinId);
        if(cCursor.moveToFirst()){
            do{
                iContainerInBin = iContainerInBin + 1;
            }while(cCursor.moveToNext());
        }

        if(iContainerInBin<iPackQuantity){
            return true;
        }else{
            return false;
        }
    }
    private void SendToRcv() {
        try {
                LayoutInflater liLayoutInflater = LayoutInflater.from(getActivity());
                View vPromptsView = liLayoutInflater.inflate(R.layout.modalremovecontainer, null);
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setView(vPromptsView);

                final ImageButton btnClose =  (ImageButton) vPromptsView.findViewById(R.id.btnClose);
                final LinearLayout llModalContainer = (LinearLayout) vPromptsView.findViewById(R.id.llModalContainer);
                ShowContainerInBin(llModalContainer);
                alertDialogBuilder.setCancelable(false);
                alertDialog = alertDialogBuilder.create();

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
    private void ShowContainerInBin(LinearLayout llModalContainer) {
        LinearLayout.LayoutParams txtParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        txtParams.setMargins(1, 0, 1, 1);

        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llParams.setMargins(1, 1, 1, 1);

        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        cCursor = dbQrs.GetContainerByBin(sBinId);
        if(cCursor.moveToFirst()){
            do{
                final String sContainerNumber = cCursor.getString(2);
                final String sContainerWeight = cCursor.getString(1);
                LinearLayout llNumber = new LinearLayout(getContext());
                llNumber.setLayoutParams(llParams);
                llNumber.setOrientation(LinearLayout.HORIZONTAL);
                llModalContainer.addView(llNumber);

                TextView txtContainerNumberS = new TextView(getContext());
                txtContainerNumberS.setLayoutParams(txtParams);
                txtContainerNumberS.setText("Código: ");
                llNumber.addView(txtContainerNumberS);
                txtContainerNumberS.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        getResources().getDimension(R.dimen.text_size_modal_description ));

                TextView txtContainerNumber = new TextView(getContext());
                txtContainerNumber.setLayoutParams(txtParams);
                txtContainerNumber.setTextColor(Color.parseColor("#000000"));
                txtContainerNumber.setText(sContainerNumber);
                llNumber.addView(txtContainerNumber);
                txtContainerNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        getResources().getDimension(R.dimen.text_size_modal_description ));

                LinearLayout llDesc = new LinearLayout(getContext());
                llDesc.setLayoutParams(llParams);
                llDesc.setOrientation(LinearLayout.HORIZONTAL);
                llModalContainer.addView(llDesc);

                TextView txtContainerDescS = new TextView(getContext());
                txtContainerDescS.setLayoutParams(txtParams);
                txtContainerDescS.setText("Descripción: ");
                llDesc.addView(txtContainerDescS);
                txtContainerDescS.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        getResources().getDimension(R.dimen.text_size_modal_description ));

                TextView txtContainerDesc = new TextView(getContext());
                txtContainerDesc.setLayoutParams(txtParams);
                txtContainerDesc.setTextColor(Color.parseColor("#000000"));
                txtContainerDesc.setText(cCursor.getString(3));
                llDesc.addView(txtContainerDesc);
                txtContainerDesc.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        getResources().getDimension(R.dimen.text_size_modal_description ));

                LinearLayout llWeight = new LinearLayout(getContext());
                llWeight.setLayoutParams(llParams);
                llWeight.setOrientation(LinearLayout.HORIZONTAL);
                llModalContainer.addView(llWeight);

                TextView txtContainerWeightS = new TextView(getContext());
                txtContainerWeightS.setLayoutParams(txtParams);
                txtContainerWeightS.setText("Peso: ");
                llWeight.addView(txtContainerWeightS);
                txtContainerWeightS.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        getResources().getDimension(R.dimen.text_size_modal_description ));

                TextView txtContainerWeight = new TextView(getContext());
                txtContainerWeight.setLayoutParams(txtParams);
                txtContainerWeight.setTextColor(Color.parseColor("#000000"));
                txtContainerWeight.setText(sContainerWeight);
                llWeight.addView(txtContainerWeight);
                txtContainerWeight.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        getResources().getDimension(R.dimen.text_size_modal_description ));

                RelativeLayout rlButton = new RelativeLayout(getContext());
                rlButton.setLayoutParams(rlParams);
                llModalContainer.addView(rlButton);

                Button ibButton = new Button(getContext());
                ibButton.setLayoutParams(rlParams);
                ibButton.setText("Mover");
                ibButton.setTextColor(Color.parseColor("#FFFFFF"));
                //ibButton.setImageResource(R.drawable.ic_move);
                ibButton.setBackground(getResources().getDrawable(R.drawable.ripple_effect_yellow));
                rlButton.addView(ibButton);

                ibButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar cCalendar = Calendar.getInstance();
                        SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
                        SimpleDateFormat sdfFormatSQL = new SimpleDateFormat("yyyyMMdd hh:mm:ss a", Locale.US);
                        String sFormattedDate = sdfFormat.format(cCalendar.getTime());
                        String sFormattedDateSQL = sdfFormatSQL.format(cCalendar.getTime());
                        String sBinId = "";


                        cCursor = dbQrs.GetBinByAreaAndAisle(sAreaId, cfData.sAisleRcv);
                        if(cCursor.moveToFirst()){
                            sBinId = cCursor.getString(0);
                        }
                        sQuery = sCheckPoint + (char) 166 + sFormattedDateSQL + (char) 166 + sContainerNumber + (char) 166 + sAreaId + (char) 166 + sBinId + (char) 166 + sScannerId + (char) 166 + sContainerWeight + (char) 166 + sUser + (char) 166 + "CUERP" + (char) 167;
                        if (isConnectedToNetwork()) {
                            pgProgressDialog = new ProgressDialog(getActivity());
                            pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            pgProgressDialog.setMessage("Enviando...");
                            pgProgressDialog.setIndeterminate(true);
                            pgProgressDialog.setCancelable(false);
                            pgProgressDialog.show();
                            dbQrs.InsertMovimientoLog(sContainerNumber, sFormattedDate, cfData.sCheckpointGroupRelocation, sProductStateFrom, sAreaId, sBinId, "3", "Nuevo registro");
                            dbQrs.UpdateContainersSpecific(sContainerNumber, sFormattedDate, sBinId, sAreaId);
                            new SetDataModal().execute();

                        }else{
                            dbQrs.InsertMovimientoLog(sContainerNumber, sFormattedDate, cfData.sCheckpointGroupRelocation, sProductStateFrom, sAreaId, sBinId, "3", "Nuevo registro");
                            dbQrs.InsertLogOffline(sQuery);
                            dbQrs.UpdateContainersSpecific(sContainerNumber, sFormattedDate, sBinId, sAreaId);
                            sQuery = "";
                            alertDialog.dismiss();
                            Toast.makeText(getActivity().getApplicationContext(), "Datos guardados localmente, enviarlos cuando se reestablezca la conexión.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }while(cCursor.moveToNext());
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
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlSetEmployeeIdUserDate(sUser, sSelectedDate + " " + sSelectedTime), GetDataWS.class);
                if (GetData.getId().equals("1")){
                    GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetLog(sUser), GetDataWS.class);
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
                    GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetLogArea + sUser, GetDataWS.class);
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
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlGetCurrentDate(sUser), GetDataWS.class);

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
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlDeleteEmployeeIdUserDate(sUser), GetDataWS.class);

                if (GetData.getId().equals("1")){
                    GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetLog(sUser) , GetDataWS.class);
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
                    GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetLogArea + sUser, GetDataWS.class);
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
