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

import android.app.ProgressDialog;
import android.content.Context;
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
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.inventario.hilcasa.hilcasainventario.common.view.SlidingTabLayout;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class InventoryMarketDispatchTabs extends Fragment implements PublicInterface{

    static final String LOG_TAG = "InventoryInVendorTabs";
    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    QueryDB dbQrs;
    Cursor cCursor;
    String sUser = "";
    //private ProgressDialog pgProgressDialog;
    ConfigData cfData = new ConfigData();
    Spinner spArea;
    String sProductStateFrom = "";
    String sAreaId = "";
    ImageButton btnSearch;
    String sComponentId ="";
    String sComponentQuantity = "";
    String sContainerQuantity = "";
    String sMasterQuery = "";
    String sDetailQuery = "";
    String sBinId = "";
    String sCheckPoint = "";
    String sScannerId = "";
    String sContainerNumber = "";
    String sContainerId = "";
    EditText txtCode;
    TextView lblBin,lblContainerQty;
    private ProgressDialog pgProgressDialog;
    List<String> lList = new ArrayList<String>();
    List<String> lListId = new ArrayList<String>();
    HashMap<String, String> hmList = new HashMap();
    MediaPlayer mpPlayer;
    int iRawOk;
    int iRawError;
    private List<InventoryLogRcvVendor> LogList = new ArrayList<>();
    private List<InventoryLogRcvVendor> LogListHist = new ArrayList<>();
    private List<InventoryTempListRcvVendor> TempList = new ArrayList<>();
    private RecyclerView rvLog, rvLogHist, rvTempList;
    private InventoryLogRcvVendorAdapter mAdapter, mAdapetrHist;
    private InventoryTempListRcvVendorAdapter mAdapterTempList;
    FloatingActionButton fabClearLog, fabAdd, fabCreateContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        dbQrs = new QueryDB(getActivity().getApplicationContext());
        dbQrs.open();
        cCursor = dbQrs.GetAreaByUser();
        if(cCursor.moveToFirst()){
            sProductStateFrom = cCursor.getString(cCursor.getColumnIndex("productstatefrom"));
        }


        cCursor = dbQrs.GetAllComponents(sProductStateFrom);
        if(cCursor.moveToFirst()){
            do {
                hmList.put(cCursor.getString(2), cCursor.getString(3));
                lList.add(cCursor.getString(2));
                lListId.add(cCursor.getString(3));
            }while (cCursor.moveToNext());
        }
        return inflater.inflate(R.layout.inventoryinareafragment, container, false);
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
            switch (iTabName){
                case 0:
                    sTabName = "Creación de lista";
                    break;
                case 1:
                    sTabName = "Historial";
                    break;
                /*case 2:
                    sTabName = "Detalle";
                    break;*/
                default:
                    break;
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
            /*dbQrs = new QueryDB(getActivity().getApplicationContext());
            dbQrs.open();*/

            iRawOk =  getResources().getIdentifier(cfData.sRawOk, "raw", getActivity().getPackageName());
            iRawError =  getResources().getIdentifier(cfData.sRawError, "raw", getActivity().getPackageName());


            cCursor = dbQrs.GetEmployeeInfo();
            if(cCursor.moveToFirst()){
                sUser = cCursor.getString(0);
            }
            if(position == 1){
                view = getActivity().getLayoutInflater().inflate(R.layout.frmtabvendordetail,
                        container, false);

                sScannerId = Settings.Secure.getString(getActivity().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

                spArea = (Spinner)view.findViewById(R.id.spArea);
                btnSearch = (ImageButton)view.findViewById(R.id.btnSearch);
                txtCode = (EditText) view.findViewById(R.id.txtCode);
                lblBin = (TextView)view.findViewById(R.id.lblBin);
                lblContainerQty = (TextView)view.findViewById(R.id.lblContainerQty);
                rvLog = (RecyclerView) view.findViewById(R.id.rvLog);
                fabClearLog = (FloatingActionButton)view.findViewById(R.id.fabClearLog);
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

                        Cursor cTempCursor = dbQrs.GetCheckpointByProduct(sProductStateFrom, cfData.sCheckpointGroupPRecive);
                        if(cTempCursor.moveToFirst()){
                            sCheckPoint = cTempCursor.getString(0);
                        }

                         cTempCursor = dbQrs.GetBinByAreaAndAisle(sAreaId, cfData.sAisleRcv);
                        if(cTempCursor.moveToFirst()){
                            sBinId = cTempCursor.getString(0);
                            lblBin.setText(cTempCursor.getString(1));
                        }else{
                            sBinId = "";
                            lblBin.setText("-");
                        }
                        ShowLog(sAreaId);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                fabClearLog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dbQrs.DeleteTempContainer(sUser);
                        ShowLog(sAreaId);
                    }
                });

                btnSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(txtCode.getText().toString().length()>0){
                            SearchCode();
                            ShowLog(sAreaId);
                            txtCode.setText("");
                        }
                    }
                });

                txtCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                                SearchCode();
                        }
                        return false;
                    }
                });
                ShowLog(sAreaId);
            }else if(position == 0){
                view = getActivity().getLayoutInflater().inflate(R.layout.frmtabmarketdispatchlist,
                        container, false);
                HideKeyboard(view);
                rvTempList = (RecyclerView) view.findViewById(R.id.rvTempList);
                fabAdd = (FloatingActionButton)view.findViewById(R.id.fabAdd);
                fabClearLog = (FloatingActionButton)view.findViewById(R.id.fabClearLog);
                fabCreateContainer = (FloatingActionButton)view.findViewById(R.id.fabCreateContainer);

                ShowTempList();

                fabAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShowModalAddContainer();
                    }
                });

                fabClearLog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dbQrs.DeleteAllComponentListItem();
                        ShowTempList();
                    }
                });

                fabCreateContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SearchCode();
                        //ShowLog(sAreaId);
                       // txtCode.setText("")
                    }
                });
            }else{
                 view = getActivity().getLayoutInflater().inflate(R.layout.frmtabmakertdispatchlog,
                        container, false);
                HideKeyboard(view);
                rvLogHist = (RecyclerView) view.findViewById(R.id.rvLogHist);
                //ShowLogHist();
            }
            // Add the newly created View to the ViewPager
            container.addView(view);
            // Return the View
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((ViewGroup) object);
        }

    }

    public boolean isConnectedToNetwork() {
        WifiManager wifiManager = (WifiManager)  getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
        Boolean bState = false;

        if(level >= 2){
            ConnectivityManager connectivity = (ConnectivityManager)getActivity().getApplicationContext()
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
        ConnectivityManager connectivity = (ConnectivityManager) getActivity().getApplicationContext()
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

    public void ShowLog(String sAreaId){
        LogList.clear();
        mAdapter = new InventoryLogRcvVendorAdapter(LogList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rvLog.setLayoutManager(mLayoutManager);
        rvLog.setItemAnimator(new DefaultItemAnimator());
        rvLog.setAdapter(mAdapter);
        cCursor = dbQrs.GetTempContainerByComponent(sAreaId, sUser);
        if(cCursor.moveToFirst()){
            InventoryLogRcvVendor ilLog;
            do{
                ilLog = new InventoryLogRcvVendor(cCursor.getString(0) ,sAreaId, cfData.sIdentifierVendorIn, sAreaId, lblBin.getText().toString());
                LogList.add(ilLog);
                mAdapter.notifyDataSetChanged();
            }while (cCursor.moveToNext());
        }

        cCursor = dbQrs.GetTempContainerByArea(sAreaId, sUser);
        if(cCursor.moveToFirst()){
            int iCount = 0;
            do{
               iCount = iCount + 1;
            }while (cCursor.moveToNext());
            lblContainerQty.setText(String.valueOf(iCount));
        }
    }

    public void ShowTempList(){
        TempList.clear();
        mAdapterTempList = new InventoryTempListRcvVendorAdapter(TempList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rvTempList.setLayoutManager(mLayoutManager);
        rvTempList.setItemAnimator(new DefaultItemAnimator());
        rvTempList.setAdapter(mAdapterTempList);
        cCursor = dbQrs.GetListByUser(sUser);
        if(cCursor.moveToFirst()){
            InventoryTempListRcvVendor ilLog;
            do{
                ilLog = new InventoryTempListRcvVendor(cCursor.getString(0) ,cCursor.getString(1) , cCursor.getString(2),cCursor.getString(0), sUser);
                TempList.add(ilLog);
                mAdapterTempList.notifyDataSetChanged();
            }while (cCursor.moveToNext());
        }
    }

    public void ShowLogHist(){
        LogListHist.clear();
        mAdapetrHist = new InventoryLogRcvVendorAdapter(LogListHist, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rvLogHist.setLayoutManager(mLayoutManager);
        rvLogHist.setItemAnimator(new DefaultItemAnimator());
        rvLogHist.setAdapter(mAdapetrHist);
        Cursor cCursor = dbQrs.GetAllTempContainerByArea();
        if(cCursor.moveToFirst()){
            InventoryLogRcvVendor ilLog;
            do{
                ilLog = new InventoryLogRcvVendor(cCursor.getString(0) ,sAreaId, cfData.sIdentifierVendorIn, sAreaId, "0");
                LogListHist.add(ilLog);
                mAdapetrHist.notifyDataSetChanged();
            }while (cCursor.moveToNext());
        }
    }

    private void ShowModalAddContainer() {
        try {
            hmList = new HashMap<>();
            lList.clear();
            lListId.clear();

            LayoutInflater liLayoutInflater = LayoutInflater.from(this.getActivity());
            View vPromptsView = liLayoutInflater.inflate(R.layout.modaladdcontainer, null);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getActivity());
            alertDialogBuilder.setView(vPromptsView);
            String sComponentId = "";

            final EditText txtBarcode = (EditText)vPromptsView.findViewById(R.id.txtBarcode);
            final EditText txtQuantity = (EditText)vPromptsView.findViewById(R.id.txtQuantity);
            final AutoCompleteTextView lblComponent = (AutoCompleteTextView)vPromptsView.findViewById(R.id.lblComponent);
            final ImageButton btnClose =  (ImageButton) vPromptsView.findViewById(R.id.btnClose);
            final ImageButton btnSearch = (ImageButton) vPromptsView.findViewById(R.id.btnSearch);
            final CheckBox cbDesc = (CheckBox)vPromptsView.findViewById(R.id.cbDesc);
            final TextView lblMaxQuantity = (TextView)vPromptsView.findViewById(R.id.lblMaxQuantity);

            lblComponent.setEnabled(false);
            txtBarcode.setMaxLines(1);
            alertDialogBuilder.setCancelable(false);
            final AlertDialog alertDialog = alertDialogBuilder.create();


            cbDesc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(cbDesc.isChecked()){
                        lblComponent.setEnabled(true);
                        txtBarcode.setEnabled(false);
                        lblComponent.requestFocus();
                        lblComponent.setFocusable(true);
                        lblComponent.setText("");
                        txtBarcode.setText("");
                        lblMaxQuantity.setText("");
                        InputMethodManager imm = (InputMethodManager)getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }else{
                        lblComponent.setEnabled(false);
                        txtBarcode.setEnabled(true);
                        txtBarcode.requestFocus();
                        lblComponent.setText("---");
                        txtBarcode.setText("");
                        lblMaxQuantity.setText("");
                        InputMethodManager imm = (InputMethodManager)getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
                }
            });

            txtBarcode.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(cbDesc.isChecked()){

                    }else{cCursor = dbQrs.GetComponentsByMarketInv(txtBarcode.getText().toString(),sProductStateFrom);
                        if(cCursor.moveToFirst()){
                            lblComponent.setText(cCursor.getString(2));
                            lblMaxQuantity.setText(cCursor.getString(4));
                        }else{
                            lblComponent.setText("---");
                            lblMaxQuantity.setText("");
                        }}


                }
            });

            lblComponent.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(s.length() < 3){
                        txtBarcode.setText("");
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            cCursor = dbQrs.GetAllComponentsByMarketInv("PANTR");
            if(cCursor.moveToFirst()){
                do {
                    hmList.put(cCursor.getString(2), cCursor.getString(3));
                    lList.add(cCursor.getString(2));
                    lListId.add(cCursor.getString(3));
                }while (cCursor.moveToNext());
            }

            ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(getContext(), R.layout.customdropdown, lList);
            lblComponent.setAdapter(aAdapter);
            lblComponent.setThreshold(3);

            lblComponent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                    String textValue = p.getAdapter().getItem(pos).toString();
                    txtBarcode.setText(hmList.get(textValue));

                    lblComponent.setSelection(0,lblComponent.getText().toString().length());

                    cCursor = dbQrs.GetComponentsByMarketInv(hmList.get(textValue),"PANTR");
                    if(cCursor.moveToFirst()){
                        do {
                            lblMaxQuantity.setText(cCursor.getString(4));
                        }while (cCursor.moveToNext());
                    }
                }
            });

            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (/*txtBarcode.getText().toString().length() < 1 ||*/ txtQuantity.getText().toString().length() < 1) {
                        Toast.makeText(getActivity().getApplicationContext(), "Complete la información.", Toast.LENGTH_SHORT).show();
                    }else{
                        if(Double.valueOf(txtQuantity.getText().toString()) <= Double.valueOf(lblMaxQuantity.getText().toString())){

                        String sTempQuantity = txtQuantity.getText().toString();
                        cCursor = dbQrs.GetComponentsByBarcode(txtBarcode.getText().toString(), sProductStateFrom);
                        if (cCursor.moveToFirst()) {
                            String sComponentId = cCursor.getString(0);
                            String sComponentDesc = cCursor.getString(2);
                            cCursor = dbQrs.GetListByUserAndComponentId(sUser, sComponentId);
                            if (cCursor.moveToFirst()) {
                                Toast.makeText(getActivity().getApplicationContext(), "Ya se encuentra en la lista.", Toast.LENGTH_SHORT).show();
                            } else {
                                dbQrs.InsertTempListSales(sComponentId,  sComponentDesc, sTempQuantity, sUser, txtBarcode.getText().toString() );
                                ShowTempList();
                                InputMethodManager imm = (InputMethodManager)getActivity().getApplicationContext().getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                                lblComponent.setEnabled(false);
                                txtBarcode.setEnabled(true);
                                txtBarcode.requestFocus();
                                lblComponent.setText("");
                                txtBarcode.setText("");
                                txtQuantity.setText("");
                                cbDesc.setChecked(false);
                                //alertDialog.cancel();
                            }
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), "El producto no existe.", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                            Toast.makeText(getActivity().getApplicationContext(), "No hay suficientes existencias para realizar este despacho.", Toast.LENGTH_SHORT).show();
                        }
                }
                }
            });

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager imm = (InputMethodManager)getActivity().getApplicationContext().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    alertDialog.cancel();



                }
            });
           /* txtBarcode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                        txtQuantity.findFocus();
                        txtQuantity.requestFocus();


                    }
                    return true;
                }
            });*/
            alertDialog.show();

        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
    }

    public void SearchCode(){
        try{
        sMasterQuery = "";
        sDetailQuery = "";
        String sCode = "1";//txtCode.getText().toString();
        txtCode.setText("");




                //PARA CREAR CONTAINER
                cCursor = dbQrs.GetListByUser(sUser);
                if(cCursor.moveToFirst()) {

                    if (isConnectedToNetwork()){

                        pgProgressDialog = new ProgressDialog(getActivity());
                        pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pgProgressDialog.setMessage("Enviando...");
                        pgProgressDialog.setIndeterminate(true);
                        pgProgressDialog.setCancelable(false);
                        pgProgressDialog.show();

                        sMasterQuery = "";
                        String cc = sCode;
                        int iQ = 0;
                        String sD = "";
                        String sC = "";
                        do {
                            iQ = iQ + cCursor.getInt(2);
                            sD = sD + cCursor.getString(1) + ", ";
                            sDetailQuery = sDetailQuery + cCursor.getString(0) + (char) 166 + cCursor.getString(2) + (char) 166 + cCursor.getString(1) + (char) 167;

                        } while (cCursor.moveToNext());
                        sD = sD.substring(0, sD.length() - 2);
                        if (sD.length() > 19) {
                            sC = sD.substring(0, 20);
                            sC = sC + "...";
                        } else {
                            sC = sD;
                    }
                        sMasterQuery =sMasterQuery + cc + (char) 166 + String.valueOf(iQ) + (char) 166 + sD + (char) 166 + sScannerId + (char) 166 + sUser + (char) 166 + sAreaId + (char) 166 + sBinId + (char) 167;
                        new SetData().execute();
                }else{
                        Toast.makeText(getActivity().getApplicationContext(), "Necesita conexión para poder realizar este proceso.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "Ingrese componentes a la lista.", Toast.LENGTH_SHORT).show();
                }




    } catch (Exception e) {
        if (pgProgressDialog != null) {
            pgProgressDialog.dismiss();
            }
            Log.e("MainActivity", e.getMessage(), e);
    }
    }

    private class SetData extends AsyncTask<Void, Void, SetData> {
        String sError = "";
        @Override
        protected InventoryMarketDispatchTabs.SetData doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlWebServiceCreateNewContainer(sMasterQuery,sDetailQuery), GetDataWS.class);

                if(GetData.getId().equals("1")){
                    String sNewContainerId = "";
                    if (GetData.getContent().contains(String.valueOf((char) 164))) {
                        String[] sData = GetData.getContent().split(String.valueOf((char) 164));
                        for (int n = 0; n < sData.length; n++) {
                            String strData = sData[n];
                            String sDataRow[] = strData.split(String.valueOf((char) 165));
                            sNewContainerId = sDataRow[0];
                        }
                    }

                    if(sNewContainerId.length()>0){
                        Calendar cCalendar = Calendar.getInstance();
                        SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
                        String sFormattedDate = sdfFormat.format(cCalendar.getTime());

                        if (sMasterQuery.contains(String.valueOf((char) 167))) {
                            String[] sDataM = sMasterQuery.split(String.valueOf((char) 167));
                            for (int n1 = 0; n1 < sDataM.length; n1++) {
                                String strData = sDataM[n1];
                                String sDataRow[] = strData.split(String.valueOf((char) 166));
                                String sOldContainerNumber = sDataRow[0];
                                String sTotalQuantity = sDataRow[1];
                                String sContainerDesc = sDataRow[2];
                                String sScannerNumber = sDataRow[3];
                                String sEmployee = sDataRow[4];
                                String sAreaId= "47019";
                                String sBinId = "15283";
                                dbQrs.InsertContainers("", "I", "1",sNewContainerId, sFormattedDate, sContainerDesc, sTotalQuantity, "", sTotalQuantity, "1", "CUERP", sBinId, sAreaId, "", "","", "I");
                            }
                        }

                        if (sDetailQuery.contains(String.valueOf((char) 167))) {
                            String[] sData = sDetailQuery.split(String.valueOf((char) 167));
                            for (int n = 0; n < sData.length; n++) {
                                String strData = sData[n];
                                String sDataRow[] = strData.split(String.valueOf((char) 166));
                                String sComponentId = sDataRow[0];
                                String sComponentQuantity = sDataRow[1];
                                String sDesc = sDataRow[2];
                                dbQrs.InsertTempContainer(sNewContainerId, sComponentId, sDesc,sComponentQuantity, sUser, sAreaId, cfData.sIdentifierVendorIn, "1");
                            }


                        }

                    }
                }else{
                    sError = "-400";
                }

            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                }
                Log.e("MainActivity", e.getMessage(), e);
                sError = "-400";
            }
            return null;
        }

        @Override
        protected void onPostExecute(InventoryMarketDispatchTabs.SetData SetData) {
            if (pgProgressDialog != null) {
                pgProgressDialog.dismiss();
            }
            ShowLog(sAreaId);
            //ShowLogHist();
            sMasterQuery = "";
            sDetailQuery = "";
            sContainerNumber = "";
            if(sError.equals("-400")){
                Toast.makeText(getActivity().getApplicationContext(), "Ocurrio un error al enviar los datos.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity().getApplicationContext(), "Registro enviado con éxito.", Toast.LENGTH_SHORT).show();
            }
            mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawOk);
            mpPlayer.start();

        }
    }

    public void Reload()
    {
        ShowLog(sAreaId);
        //ShowLogHist();
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
    }
