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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class InventoryOutClientTabs extends Fragment implements PublicInterface{

    static final String LOG_TAG = "InventoryOutAreaTabs";
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
    String sDestinationAreaId = "";
    ImageButton btnSearch;
   // TextView lblBin;
    String sQuery = "";
    String sBinId = "";
    String sCheckPoint = "";
    String sScannerId = "";
    String sContainerNumber = "";
    String sContainerId = "";
    EditText txtCode;
    MediaPlayer mpPlayer;
    TextView lblQuantity;
    FloatingActionButton fabClearLog, fabCalendar;
    int iRawOk;
    int iRawError;
    private List<InventoryLogSndArea> LogList = new ArrayList<>();
    private List<InventoryLogSndArea> LogListHist = new ArrayList<>();
    private RecyclerView rvLog, rvLogHist;
    private InventoryLogSndAreaAdapter mAdapter, mAdapetrHist;

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
        return inflater.inflate(R.layout.inventoryoutareafragment, container, false);
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
                sTabName = "Despacho de item";
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
            iRawOk =  getResources().getIdentifier(cfData.sRawOk, "raw", getActivity().getPackageName());
            iRawError =  getResources().getIdentifier(cfData.sRawError, "raw", getActivity().getPackageName());
            dbQrs = new QueryDB(getActivity().getApplicationContext());
            dbQrs.open();
            final SharedPreferences spPref = getActivity().getSharedPreferences("UserData", Activity.MODE_PRIVATE);
            sBinId = new String(spPref.getString("sBinId", sBinId)).toString();


            cCursor = dbQrs.GetEmployeeInfo();
            if(cCursor.moveToFirst()){
                sUser = cCursor.getString(0);
            }
            if(position == 0){
                view = getActivity().getLayoutInflater().inflate(R.layout.frmtabinventoryoutclient,
                        container, false);

                sScannerId = Settings.Secure.getString(getActivity().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                spArea = (Spinner)view.findViewById(R.id.spArea);
                fabClearLog = (FloatingActionButton)view.findViewById(R.id.fabClearLog);
                btnSearch = (ImageButton)view.findViewById(R.id.btnSearch);
                txtCode = (EditText) view.findViewById(R.id.txtCode);
                //lblBin = (TextView)view.findViewById(R.id.lblBin);
                rvLog = (RecyclerView) view.findViewById(R.id.rvLog);
                txtCode.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

                //ABRE LA CONEXION A LA BD PARA PODER REALIZAR LAS CONSULTAS
                dbQrs.open();
                //OBTIENE EL USUARIO LOGEADO
                cCursor = dbQrs.GetEmployeeInfo();
                if(cCursor.moveToFirst()){
                    sUser = cCursor.getString(0);
                }

                cCursor = dbQrs.GetAreaByUser();
                if(cCursor.moveToFirst()){
                    sAreaId = cCursor.getString(0);
                }

                cCursor = dbQrs.GetBinByAreaOrder(cfData.sClientAreaId, sBinId);
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
                        sBinId = crlCur.getString(0);
                        sDestinationAreaId = cfData.sClientAreaId;

                        final SharedPreferences spTemp = getActivity().getSharedPreferences("UserData", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = spTemp.edit();
                        editor.putString("sBinId",sBinId);
                        editor.commit();


                        if(sAreaId.equals("10003")||sAreaId.equals("10004")||sAreaId.equals("10005")||sAreaId.equals("18001")){
                            sProductStateFrom = "CHMIS";
                        }else if(sAreaId.equals("10001")||sAreaId.equals("21002")||sAreaId.equals("24002")||sAreaId.equals("25002")||sAreaId.equals("26002")||sAreaId.equals("47003")){
                            sProductStateFrom = "FABC";
                        }else if(sAreaId.equals("10002")||sAreaId.equals("10007")||sAreaId.equals("10008")||sAreaId.equals("10009")||sAreaId.equals("10015")||sAreaId.equals("21003")||sAreaId.equals("24003")){
                            sProductStateFrom = "FABT";
                        }else if(sAreaId.equals("12003")||sAreaId.equals("41001")||sAreaId.equals("43000")||sAreaId.equals("44000")||sAreaId.equals("44001")){
                            sProductStateFrom = "MP";
                        }else if(sAreaId.equals("46004")||sAreaId.equals("46005")){
                            sProductStateFrom = "OFFIC";
                        }else if(sAreaId.equals("46001")){
                            sProductStateFrom = "SUPPL";
                        }else if(sAreaId.equals("12001")||sAreaId.equals("12002")||sAreaId.equals("21001")||sAreaId.equals("24001")||sAreaId.equals("25001")||sAreaId.equals("26001")||sAreaId.equals("40001")||sAreaId.equals("42001")||sAreaId.equals("43002")||sAreaId.equals("44006")||sAreaId.equals("44007")||sAreaId.equals("45006")||sAreaId.equals("47001")||sAreaId.equals("47004")){
                            sProductStateFrom = "THRP";
                        }

                        Cursor cTempCursor = dbQrs.GetCheckpointByProduct(sProductStateFrom, cfData.sCheckPointGroupCostumerSend);
                        if(cTempCursor.moveToFirst()){
                            sCheckPoint = cTempCursor.getString(0);
                        }

                       /* cTempCursor = dbQrs.GetBinByAreaAndAisle(sDestinationAreaId, cfData.sAisleRcv);
                        if(cTempCursor.moveToFirst()){
                            sBinId = cTempCursor.getString(0);
                            lblBin.setText(cTempCursor.getString(1));
                        }*/
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
                            dbQrs.InsertLastLog(txtCode.getText().toString(),cfData.sIdentifierCustomerOut,sFormattedDate);
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
                 view = getActivity().getLayoutInflater().inflate(R.layout.frmtabinventoryoutarealog,
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

    /*public boolean isConnectedToInternet() {
        WifiManager wifiManager = (WifiManager)getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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

    private class
    SetData extends AsyncTask<Void, Void, SetData> {
        String sError = "";
        @Override
        protected SetData doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlWebServiceSetDataUpdateContainer+sQuery, GetDataWS.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
                //INSERTA A OFFLINE CUANDO HAY UN ERROR DE CUALQUIER TIPO
                dbQrs.InsertLogOffline(sQuery);
                dbQrs.EndTransaction();
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
                    dbQrs.EndTransaction();
                    Toast.makeText(getActivity().getApplicationContext(), "Ocurrió un error al enviar los datos, se almacenara localmente.", Toast.LENGTH_SHORT).show();
                }else{
                    dbQrs.CommitTransaction();
                    Toast.makeText(getActivity().getApplicationContext(), "Registro enviado con éxito.", Toast.LENGTH_SHORT).show();
                }
                ShowLogHist();
                ShowLog(sAreaId);
                //txtCode.setText("");
                mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawOk);
                mpPlayer.start();

        }
    }

    public void ShowLog(String sAreaId){
        LogList.clear();
        mAdapter = new InventoryLogSndAreaAdapter(LogList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rvLog.setLayoutManager(mLayoutManager);
        rvLog.setItemAnimator(new DefaultItemAnimator());
        rvLog.setAdapter(mAdapter);
        cCursor = dbQrs.GetLogMovimiento(cfData.sCheckPointGroupCostumerSend,sAreaId);
        if(cCursor.moveToFirst()){
            InventoryLogSndArea ilLog;
            do{
                ilLog = new InventoryLogSndArea(cCursor.getString(0), cCursor.getString(1), cCursor.getString(5), cCursor.getString(3), sCheckPoint, sUser, cCursor.getString(4),  cfData.sCheckPointGroupCostumerSend);
                LogList.add(ilLog);
                mAdapter.notifyDataSetChanged();
            }while (cCursor.moveToNext());
        }
    }

    public void ShowLogHist(){
        int iC= 0;
        LogListHist.clear();
        mAdapetrHist = new InventoryLogSndAreaAdapter(LogListHist, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rvLogHist.setLayoutManager(mLayoutManager);
        rvLogHist.setItemAnimator(new DefaultItemAnimator());
        rvLogHist.setAdapter(mAdapetrHist);
        Cursor cCursor = dbQrs.GetLogMovimientoHist(cfData.sCheckPointGroupCostumerSend);
        if(cCursor.moveToFirst()){
            InventoryLogSndArea ilLog;
            do{
                iC = iC + 1;
                ilLog = new InventoryLogSndArea(cCursor.getString(0), cCursor.getString(1), cCursor.getString(5), cCursor.getString(3), sCheckPoint, sUser, cCursor.getString(4), cfData.sCheckPointGroupCostumerSend);
                LogListHist.add(ilLog);
                mAdapetrHist.notifyDataSetChanged();
            }while (cCursor.moveToNext());
        }
        lblQuantity.setText(String.valueOf(iC));
    }

    public void SearchCode(){
        sQuery = "";
        String sCode = txtCode.getText().toString();
        txtCode.setText("");

        if (sCode.length() >= 1) {

           // if(sCode.length() == 13 || sCode.length() == 16){
                //PARA BIN
                if (sCode.toString().matches(cfData.regexpBinFormat1)||sCode.matches(cfData.regexpBinFormat2)){
                    cCursor = dbQrs.GetBin(sDestinationAreaId, sCode);
                    if (cCursor.moveToFirst()) {
                        sBinId = cCursor.getString(0);
                        //lblBin.setText(cCursor.getString(1));
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
            cCursor = dbQrs.GetContainerByAreaAndBin(sCode);
            if (cCursor.moveToFirst()) {
                sContainerId = cCursor.getString(0);
                String sContainerWeight = cCursor.getString(1);
                sContainerNumber = cCursor.getString(2);
                Calendar cCalendar = Calendar.getInstance();
                SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
                SimpleDateFormat sdfFormatSQL = new SimpleDateFormat("yyyyMMdd hh:mm:ss a", Locale.US);
                String sFormattedDate = sdfFormat.format(cCalendar.getTime());
                String sFormattedDateSQL = sdfFormatSQL.format(cCalendar.getTime());
                sQuery = sCheckPoint + (char) 166 + sFormattedDateSQL + (char) 166 + sContainerNumber + (char) 166 + sDestinationAreaId + (char) 166 + sBinId + (char) 166 + sScannerId + (char) 166 + sContainerWeight + (char) 166 + sUser + (char) 166 + "CUERP" + (char) 167;


                if (isConnectedToNetwork()){
                    pgProgressDialog = new ProgressDialog(getActivity());
                    pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pgProgressDialog.setMessage("Enviando...");
                    pgProgressDialog.setIndeterminate(true);
                    pgProgressDialog.setCancelable(false);
                    pgProgressDialog.show();
                    dbQrs.BeginTransaction();
                    dbQrs.InsertMovimientoLog(sContainerNumber,sFormattedDate,cfData.sCheckPointGroupCostumerSend,sProductStateFrom,sAreaId,sBinId,"1", "Nuevo registro");
                    dbQrs.UpdateContainersSpecificSnd(sContainerNumber,sFormattedDate,sBinId,sAreaId);
                    //txtCode.setText("");
                    new SetData().execute();
                }else{
                        dbQrs.InsertMovimientoLog(sContainerNumber,sFormattedDate,cfData.sCheckPointGroupCostumerSend,sProductStateFrom,sAreaId,sBinId,"1", "Nuevo registro");
                        dbQrs.InsertLogOffline(sQuery);
                        dbQrs.UpdateContainersSpecificSnd(sContainerNumber,sFormattedDate,sBinId,sAreaId);
                        sQuery = "";
                        //txtCode.setText("");
                        sContainerNumber = "";
                        ShowLog(sAreaId);
                        ShowLogHist();
                        mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawOk);
                        mpPlayer.start();
                        Toast.makeText(getActivity().getApplicationContext(),"Datos guardados localmente, enviarlos cuando se reestablezca la conexión.",Toast.LENGTH_SHORT).show();

                }
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "No existe un item con este código.", Toast.LENGTH_SHORT).show();
                mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
                mpPlayer.start();
               // txtCode.setText("");
            }
        }

        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Ingrese un código valido.", Toast.LENGTH_SHORT).show();
            mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
            mpPlayer.start();
           // txtCode.setText("");
        }
    }

    public void SearchCode(String sContainerNumberCode){
        String sCode = sContainerNumberCode;
        txtCode.setText("");
        if (sCode.length() >= 1) {
            if(sCode.length() == 13 || sCode.length() == 16){
                //PARA BIN
                if (sCode.toString().matches(cfData.regexpBinFormat1)||sCode.matches(cfData.regexpBinFormat2)){
                    cCursor = dbQrs.GetBin(sAreaId, sCode);
                    if (cCursor.moveToFirst()) {
                        sBinId = cCursor.getString(0);
                       // lblBin.setText(cCursor.getString(1));
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
            }else{
            cCursor = dbQrs.GetContainerByAreaAndBin(sCode);
            if (cCursor.moveToFirst()) {
                sContainerId = cCursor.getString(0);
                String sContainerWeight = cCursor.getString(1);
                sContainerNumber = cCursor.getString(2);
                Calendar cCalendar = Calendar.getInstance();
                SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
                SimpleDateFormat sdfFormatSQL = new SimpleDateFormat("yyyyMMdd hh:mm:ss a", Locale.US);
                String sFormattedDate = sdfFormat.format(cCalendar.getTime());
                String sFormattedDateSQL = sdfFormatSQL.format(cCalendar.getTime());
                sQuery = sQuery + sCheckPoint + (char) 166 + sFormattedDateSQL + (char) 166 + sContainerNumber + (char) 166 + sDestinationAreaId + (char) 166 + sBinId + (char) 166 + sScannerId + (char) 166 + sContainerWeight + (char) 166 + sUser + (char) 166 + "CUERP" + (char) 167;

                if (isConnectedToNetwork()) {

                    dbQrs.InsertMovimientoLog(sContainerNumber, sFormattedDate, cfData.sCheckPointGroupCostumerSend, sProductStateFrom, sAreaId, sBinId, "1", "Nuevo registro");
                    dbQrs.UpdateContainersSpecificSnd(sContainerNumber, sFormattedDate, sBinId, sAreaId);

                    //new SetData().execute();
                } else {

                    dbQrs.InsertMovimientoLog(sContainerNumber, sFormattedDate, cfData.sCheckPointGroupCostumerSend, sProductStateFrom, sAreaId, sBinId, "1", "Nuevo registro");
                    dbQrs.InsertLogOffline(sQuery);
                    dbQrs.UpdateContainersSpecificSnd(sContainerNumber, sFormattedDate, sBinId, sAreaId);
                    sQuery = "";
                    sContainerNumber = "";
                    ShowLog(sAreaId);
                    ShowLogHist();
                    mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawOk);
                    mpPlayer.start();
                    Toast.makeText(getActivity().getApplicationContext(), "Datos guardados localmente, enviarlos cuando se reestablezca la conexión.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "No existe un item con este código.", Toast.LENGTH_SHORT).show();
                mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(), iRawError);
                mpPlayer.start();
            }
        }
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Ingrese un código valido.", Toast.LENGTH_SHORT).show();
            mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
            mpPlayer.start();
        }
    }
    public void Reload()
    {
        ShowLog(sAreaId);
        ShowLogHist();
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
                                dbQrs.InsertMovimientoLog(sContainerNumber,sDate,sCheckPointGroup,sProductStateFrom,sArea,sBinId,"2", sContainerDesc);
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
