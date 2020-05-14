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


public class PackingListCheckListTabs extends Fragment implements PublicInterface{

    static final String LOG_TAG = "PackingListCheckListTabs";
    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    QueryDB dbQrs;
    Cursor cCursor;
    String sUser = "";
    private ProgressDialog pgProgressDialog;
    ConfigData cfData = new ConfigData();

    ImageButton btnSearch, btnLoadPackingList;
    TextView lblBin, lblPackingListId, lblRoute, lblCheckpoint, lblTotalQty, lblQtyScan, lblQtyMiss;
    String sQuery = "";
    String sCheckPoint = "";
    String sScannerId = "";
    String sContainerNumber = "";
    int iCurrentPackingListId = 0;
    String sContainerId = "";
    EditText txtCode;
    MediaPlayer mpPlayer;
    Button lblScanned;
    TextView lblQuantity;
    int iCont = 0;
    int iRawOk;
    int iRawError;
    private List<PackingListLog> LogList = new ArrayList<>();
    private List<PackingListLog> LogListHist = new ArrayList<>();
    private RecyclerView rvLog, rvLogHist;
    private PackingListCheckListAdapter mAdapter, mAdapetrHist;
    String sPackingListId = "";

    //BUSQUEDA DE PL.
    AlertDialog alertDialog;




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
                sTabName = "Listado PL";
            }else if(iTabName == 1){
                sTabName = "Detalle PL";
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
            iCont = 0;


            final SharedPreferences spPref = getActivity().getSharedPreferences("UserData", Activity.MODE_PRIVATE);

            cCursor = dbQrs.GetEmployeeInfo();
            if(cCursor.moveToFirst()){
                sUser = cCursor.getString(0);
            }
            if(position == 0){
                view = getActivity().getLayoutInflater().inflate(R.layout.frmtabchecklist,
                        container, false);

                sScannerId = Settings.Secure.getString(getActivity().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

                btnSearch = (ImageButton)view.findViewById(R.id.btnSearch);
                txtCode = (EditText) view.findViewById(R.id.txtCode);
                lblBin = (TextView)view.findViewById(R.id.lblBin);
                rvLog = (RecyclerView) view.findViewById(R.id.rvLog);
                lblScanned = (Button)view.findViewById(R.id.lblScanned);
                btnLoadPackingList = (ImageButton) view.findViewById(R.id.btnLoadPackingList);
                txtCode.setFilters(new InputFilter[] {new InputFilter.AllCaps()});


                //ABRE LA CONEXION A LA BD PARA PODER REALIZAR LAS CONSULTAS
                dbQrs.open();
                //OBTIENE EL USUARIO LOGEADO
                cCursor = dbQrs.GetEmployeeInfo();
                if(cCursor.moveToFirst()){
                    sUser = cCursor.getString(0);
                }



                btnLoadPackingList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    ShowModalPackingListSearch();
                    }
                });


                btnSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(txtCode.getText().toString().length()>1){
                            SearchCode();
                        }


                    }
                });

                txtCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                                txtCode.setMaxLines(1);
                            if(txtCode.getText().toString().length()>1) {
                                SearchCode();
                            }

                        }
                        return false;
                    }
                });

                ShowList(sPackingListId);
            }else{
                 view = getActivity().getLayoutInflater().inflate(R.layout.frmpackinglistheader,
                        container, false);

                 lblPackingListId = (TextView)view.findViewById(R.id.lblPackingListId);
                 lblRoute = (TextView)view.findViewById(R.id.lblRoute);
                 lblCheckpoint = (TextView)view.findViewById(R.id.lblCheckpoint);
                 lblTotalQty = (TextView)view.findViewById(R.id.lblTotalQty);
                 lblQtyScan = (TextView)view.findViewById(R.id.lblQtyScan);
                 lblQtyMiss = (TextView)view.findViewById(R.id.lblQtyMiss);




                HideKeyboard(view);


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





    public void ShowList(String sPackingListId){
        LogList.clear();
        int iScanned = 0;
        mAdapter = new PackingListCheckListAdapter(LogList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rvLog.setLayoutManager(mLayoutManager);
        rvLog.setItemAnimator(new DefaultItemAnimator());
        rvLog.setAdapter(mAdapter);
        cCursor = dbQrs.GetPackingListInfo(sPackingListId);
        if(cCursor.moveToFirst()){
            PackingListLog ilLog;
            do{
                ilLog = new PackingListLog(cCursor.getString(0) , cCursor.getString(1), cCursor.getString(2), cCursor.getString(3) + " - " + cCursor.getString(5) + " - " + cCursor.getString(6), cCursor.getString(4));
                LogList.add(ilLog);
                mAdapter.notifyDataSetChanged();
                iScanned = iScanned + 1;
            }while (cCursor.moveToNext());
        }
        lblScanned.setText(String.valueOf(iScanned));
    }

    public void ShowDetail(String sPackingListId) {

        cCursor = dbQrs.GetPackingListInfoDetail(sPackingListId);
        if (cCursor.moveToFirst()) {

            do {
                lblPackingListId.setText(cCursor.getString(0));
                lblRoute.setText(cCursor.getString(1));
                lblCheckpoint.setText(cCursor.getString(2));
                lblTotalQty.setText(cCursor.getString(4));

            } while (cCursor.moveToNext());

            double iQtyTotal = 0;
            double iQtyScan = 0;
            double iQtyMiss = 0;
            cCursor = dbQrs.GetPackingListInfoDetailMissing(sPackingListId);
            if (cCursor.moveToFirst()) {
                do {
                    iQtyMiss = Double.parseDouble(cCursor.getString(1));
                } while (cCursor.moveToNext());
            }

            cCursor = dbQrs.GetPackingListInfoDetailScans(sPackingListId);
            if (cCursor.moveToFirst()) {
                do {
                    iQtyScan = Double.parseDouble(cCursor.getString(1));
                } while (cCursor.moveToNext());
            }
            iQtyTotal = iQtyScan + iQtyMiss;

            lblQtyScan.setText(Double.toString(iQtyScan) + "/" + Double.toString(iQtyTotal));
            lblQtyMiss.setText(Double.toString(iQtyMiss) + "/" + Double.toString(iQtyTotal));

        }else{
            lblPackingListId.setText("-");
            lblRoute.setText("-");
            lblCheckpoint.setText("-");
            lblTotalQty.setText("-");
            lblQtyScan.setText("-");
            lblQtyMiss.setText("-");
        }
    }

    public void SearchCode(){
        sQuery = "";
        String sCode = txtCode.getText().toString().trim();
        txtCode.setText("");


        if (sCode.length() >= 1)
        {
            boolean sFound =  dbQrs.UpdatePackingListInfoFlag(sCode,sPackingListId);

            if(sFound){
                mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawOk);
                mpPlayer.start();
            }else{
                mpPlayer = MediaPlayer.create(getActivity().getApplicationContext(),iRawError);
                mpPlayer.start();
            }
            ShowList(sPackingListId);
            ShowDetail(sPackingListId);

        }
    }

    private void ShowModalPackingListSearch() {
        try {
            LayoutInflater liLayoutInflater = LayoutInflater.from(getActivity());
            View vPromptsView = liLayoutInflater.inflate(R.layout.modalpackinglist, null);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setView(vPromptsView);

            final ImageButton btnClose =  (ImageButton) vPromptsView.findViewById(R.id.btnClose);
            final LinearLayout llModalContainer = (LinearLayout) vPromptsView.findViewById(R.id.llModalContainer);
            final EditText txtPackingList = (EditText) vPromptsView.findViewById(R.id.txtBarcode);
            final ImageButton btnSearch = (ImageButton)vPromptsView.findViewById(R.id.btnSearch);

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
                        sPackingListId = txtPackingList.getText().toString();
                        ShowList(sPackingListId);
                        ShowDetail(sPackingListId);
                        alertDialog.cancel();

                    }
            });

            txtPackingList.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                        sPackingListId = txtPackingList.getText().toString();
                        ShowList(sPackingListId);
                        ShowDetail(sPackingListId);
                        alertDialog.cancel();
                    }
                    return false;
                }
            });
            alertDialog.show();
        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
    }

    public void Reload()
    {
        ShowList(sPackingListId);
       // ShowLogHist();
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


                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlGetPackingListDetails(sPackingListId), GetDataWS.class);
                    if (GetData.getId().equals("1")) {
                        dbQrs.BeginTransaction();
                        if (GetData.getContent().contains(String.valueOf((char) 164))) {
                            String[] sContainer = GetData.getContent().split(String.valueOf((char) 164));
                            for (int n = 0; n < sContainer.length; n++) {
                                String strContainer = sContainer[n];
                                String sDataRow[] = strContainer.split(String.valueOf((char) 165));
                                String sPackingListId = sDataRow[0];
                                String sCheckpointDescription = sDataRow[1];
                                String sRouteDescription = sDataRow[2];
                                String sContainerNumber = sDataRow[3];
                                String sQuantity = sDataRow[4];
                                String sCompCode = sDataRow[5];

                                    dbQrs.InsertPackingListInfo(sPackingListId, sCheckpointDescription, sRouteDescription, sContainerNumber, sQuantity, sCompCode);
                            }
                        }
                        dbQrs.CommitTransaction();
                    } else {
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
                    Toast.makeText(getActivity().getApplicationContext(), "Ha ocurrido un problema.", Toast.LENGTH_SHORT).show();
                    sError = "";
                }
            }else{
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    Toast.makeText(getActivity().getApplicationContext(), "Datos sincronizados con exitó.", Toast.LENGTH_SHORT).show();
                    sError = "";

                }
            }
        }
    }
    }


