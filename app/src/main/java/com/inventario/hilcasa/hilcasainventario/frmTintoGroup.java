package com.inventario.hilcasa.hilcasainventario;

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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class frmTintoGroup extends AppCompatActivity implements PublicInterface{

    ConfigData cfData = new ConfigData();
    String sUser = "";
    String sUserId = "";
    TextView txtUser;
    String sScannerId = "";
    AlertDialog alertDialog , alertDialog2;
    ImageButton btnSearch;
    EditText txtCode;
    Boolean bIsEmpty = true;
    Cursor cCursor;
    String sChildContainerNumber = "";
    EditText txtBarcodeAddModal;
    Button lblScanned;
    FloatingActionButton fabPartial;
    FloatingActionButton fabAdd;
    QueryDB dbQrs = new QueryDB(this);
    private ProgressDialog pgProgressDialog;
    private List<InventorTintoGroup> TempList = new ArrayList<>();
    private InventoryTintoGroupAdapter mAdapterTempList;
    private RecyclerView rvTempList;
    int iRawOk;
    int iRawError;
    MediaPlayer mpPlayer;
    Spinner spPart;
    String[] sParts = {"CUERP", "RIBS", "JERSE"};
    String sSelectedPart = "";
    String sContainerNumber = "";
    ImageButton btnContainer;
    TextView lblContainerNumberPart;
    TextView lblContainerInfo;
    TextView lblWeight, lblCurrentWeight;
    LinearLayout llInfoContainer;
    Double dCurrentWeight = 0.0;
    TextView lblPartialContainerWeight;
    LinearLayout llPartialWeight;
    String sPartialContainerNumber = "";
    ImageButton btnSearchPartial;
    TextView txtPartialWeight;
    ImageButton btnGetWeight;
    EditText txtContainerNumber;
    Double dPartialWeight = 0.0;
    Double dParentContainerPartialWeight = 0.0;
    TextView lblContainerQuantity;
    Double dAvailableWeight = 0.0;
    int iQuantity = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frmtintogroupcontainer
        );

        sScannerId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        txtUser = (TextView)findViewById(R.id.lblUser);
        rvTempList = (RecyclerView) findViewById(R.id.rvTempList);
        fabPartial = (FloatingActionButton) findViewById(R.id.fabPartial);
        fabAdd = (FloatingActionButton)findViewById(R.id.fabAdd);
        txtCode = (EditText) findViewById(R.id.txtCode) ;
        btnSearch = (ImageButton) findViewById(R.id.btnSearch);
        lblScanned = (Button)findViewById(R.id.lblScanned);
        spPart = (Spinner)findViewById(R.id.spPart);
        btnContainer = (ImageButton) findViewById(R.id.btnContainer);
        lblContainerNumberPart = (TextView)findViewById(R.id.lblContainerNumberPart);
        lblContainerInfo = (TextView)findViewById(R.id.lblContainerInfo);
        lblWeight = (TextView)findViewById(R.id.lblWeight);
        llInfoContainer = (LinearLayout)findViewById(R.id.llInfoContainer);
        lblCurrentWeight = (TextView)findViewById(R.id.lblCurrentWeight);

        txtCode.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.customspinnerlayout, android.R.id.text1, sParts);
        spPart.setAdapter(adapter);

        spPart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                sSelectedPart = spPart.getSelectedItem().toString();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        dbQrs.open();
        getSupportActionBar().setTitle("Lotes");
        iRawOk =  getResources().getIdentifier(cfData.sRawOk, "raw", getApplicationContext().getPackageName());
        iRawError =  getResources().getIdentifier(cfData.sRawError, "raw", getApplicationContext().getPackageName());

        getCurrentUser();

        if(isConnectedToNetwork()) {

        }else{
            Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este módulo.", Toast.LENGTH_SHORT).show();
        }

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnectedToNetwork()) {
                    ShowModalConfirmation();
                }else{
                    Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este módulo.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        fabPartial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowModalPartialWeight();
            }
        });

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnectedToNetwork()) {
                    ShowModalAddContainer();
                }
            }
        });

        btnContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sContainerNumber = txtCode.getText().toString();

                    if(isConnectedToNetwork()) {
                        pgProgressDialog = new ProgressDialog(frmTintoGroup.this);
                        pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pgProgressDialog.setMessage("Obteniendo datos...");
                        pgProgressDialog.setIndeterminate(true);
                        pgProgressDialog.setCancelable(false);
                        pgProgressDialog.show();
                        new GetData().execute();
                    }else{
                        Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este módulo.", Toast.LENGTH_SHORT).show();
                    }
            }
        });

        txtCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    sContainerNumber = txtCode.getText().toString();

                    if(isConnectedToNetwork()) {
                        pgProgressDialog = new ProgressDialog(frmTintoGroup.this);
                        pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pgProgressDialog.setMessage("Obteniendo datos...");
                        pgProgressDialog.setIndeterminate(true);
                        pgProgressDialog.setCancelable(false);
                        pgProgressDialog.show();
                        new GetData().execute();
                    }else{
                        Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este módulo.", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
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
       // txtCode.setText("Nuevo");
    }



    private class GetData extends AsyncTask<Void, Void, GetData> {
        String sError="";
        String sQuery = "";

        @Override
        protected GetData doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlGetTintoParentContainer( sContainerNumber, sSelectedPart), GetDataWS.class);

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
            dAvailableWeight = 0.0;
            lblCurrentWeight.setText(String.valueOf(" - "));
            if (sError.contains("-400")){
                if (pgProgressDialog != null) {

                    lblContainerNumberPart.setText(" - ");
                    lblContainerInfo.setText(" - ");
                    lblWeight.setText(" - ");
                    pgProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Ha ocurrido un problema.", Toast.LENGTH_SHORT).show();
                    sError = "";
                }
            }else {
                if (sQuery.contains(String.valueOf((char) 164))) {
                    String[] sContainer = sQuery.split(String.valueOf((char) 164));
                    for (int n = 0; n < sContainer.length; n++) {
                        String strContainer = sContainer[n];
                        String sDataRow[] = strContainer.split(String.valueOf((char) 165));
                        String sContainerNumber = sDataRow[0];
                        String sContainerDesc = sDataRow[1];
                        String sContainerWeight = sDataRow[2];
                        String sContainerPart = sDataRow[3];

                        llInfoContainer.setVisibility(View.VISIBLE);
                        fabAdd.setVisibility(View.VISIBLE);
                        fabPartial.setVisibility(View.VISIBLE);
                        lblContainerNumberPart.setText(sContainerNumber + " - " + sContainerPart);
                        lblContainerInfo.setText(sContainerDesc);
                        lblWeight.setText(sContainerWeight);
                        dAvailableWeight = Double.valueOf(sContainerWeight);
                        iCont = iCont + 1;
                        Toast.makeText(getApplicationContext(), "Datos obtenidos con exitó.", Toast.LENGTH_SHORT).show();
                        txtCode.setText("");
                    }
                    new GetChildContainers().execute();
                }else{
                    llInfoContainer.setVisibility(View.GONE);
                    fabAdd.setVisibility(View.GONE);
                    fabPartial.setVisibility(View.GONE);
                    lblContainerNumberPart.setText(" - ");
                    lblContainerInfo.setText(" - ");
                    lblWeight.setText(" - ");
                    TempList.clear();
                    Toast.makeText(getApplicationContext(), "Este lote no existe o no puede ser obtenido.", Toast.LENGTH_SHORT).show();
                    if (pgProgressDialog != null) {
                        pgProgressDialog.dismiss();
                        sError = "";
                    }
                }
            }

        }
    }

    private class GetChildContainers extends AsyncTask<Void, Void, GetChildContainers> {
        String sError="";
        String sQuery = "";
        @Override
        protected GetChildContainers doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlGetTintoChildContainer( sContainerNumber, sSelectedPart), GetDataWS.class);

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
                    sError = "";
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(GetChildContainers GetChildContainers) {
            int iCont = 0;
            iQuantity = 0;
            dCurrentWeight = 0.0;
            if (sError.contains("-400")){
                if (pgProgressDialog != null) {

                    pgProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Ha ocurrido un problema.", Toast.LENGTH_SHORT).show();
                    sError = "";
                }
            }else
            {
                InventorTintoGroup ilLog;
                TempList.clear();
                mAdapterTempList = new InventoryTintoGroupAdapter(TempList, frmTintoGroup.this);
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

                            ilLog = new InventorTintoGroup(sContainerNumber, sContainerDesc,  sContainerWeight + " Lbs  -  " + sContainerPart, sContainerPart, sUser);
                            TempList.add(ilLog);
                            mAdapterTempList.notifyDataSetChanged();
                            bIsEmpty = false;
                            iCont = iCont + 1;
                            dCurrentWeight = dCurrentWeight + Double.valueOf(sContainerWeight);
                        }
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(btnContainer.getWindowToken(), 0);
                        iQuantity = iCont;
                    }
                    lblCurrentWeight.setText(String.valueOf(dCurrentWeight));
            }else{
                    bIsEmpty = true;
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



    private class GetPartialContainer extends AsyncTask<Void, Void, GetPartialContainer> {
        String sError="";
        String sQuery = "";
        @Override
        protected GetPartialContainer doInBackground(Void... params) {
            try {


                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlGetPartialWeight(sPartialContainerNumber), GetDataWS.class);

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
        protected void onPostExecute(GetPartialContainer GetPartialContainer) {
            int iCont = 0;
            if (sError.contains("-400")){
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Ha ocurrido un problema.", Toast.LENGTH_SHORT).show();
                    sError = "";
                }
            }else {
                if (sQuery.length() > 0){
                    if (sQuery.contains(String.valueOf((char) 164))) {
                        String[] sContainer = sQuery.split(String.valueOf((char) 164));
                        for (int n = 0; n < sContainer.length; n++) {
                            String strContainer = sContainer[n];
                            String sDataRow[] = strContainer.split(String.valueOf((char) 165));
                            //String sContainerNumber = sDataRow[0];
                            String sContainerWeight = sDataRow[1];
                            //String sContainerDesc = sDataRow[2];
                            lblPartialContainerWeight.setText("/" + String.valueOf(sContainerWeight + " Lbs"));
                            txtContainerNumber.setEnabled(false);
                            btnGetWeight.setEnabled(false);
                            llPartialWeight.setVisibility(View.VISIBLE);
                            dParentContainerPartialWeight = Double.valueOf(sContainerWeight);
                            }
                        btnSearchPartial.setEnabled(true);
                        }

                    if(alertDialog != null) {
                        alertDialog.cancel();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Número de contenedor no existe.", Toast.LENGTH_SHORT).show();
                }
                lblScanned.setText(String.valueOf(iCont));
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    sError = "";
                }

            }
            txtContainerNumber.requestFocus();
        }
    }


    private void ShowModalAddContainer() {
        try {
            LayoutInflater liLayoutInflater = LayoutInflater.from(frmTintoGroup.this);
            View vPromptsView = liLayoutInflater.inflate(R.layout.modaltintogroupcontainer, null);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(frmTintoGroup.this);
            alertDialogBuilder.setView(vPromptsView);


            txtBarcodeAddModal = (EditText)vPromptsView.findViewById(R.id.txtBarcode);
            final ImageButton btnClose =  (ImageButton) vPromptsView.findViewById(R.id.btnClose);
            final ImageButton btnSearch = (ImageButton) vPromptsView.findViewById(R.id.btnSearch);
            lblContainerQuantity = (TextView) vPromptsView.findViewById(R.id.lblContainerQuantity);
            txtBarcodeAddModal.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
            txtBarcodeAddModal.setMaxLines(1);
            lblContainerQuantity.setText("Cantidad actual: " + String.valueOf(iQuantity));

            alertDialogBuilder.setCancelable(false);
            alertDialog2 = alertDialogBuilder.create();

                //PARA AGREGAR CONTENEDORES A UN PADRE QUE YA EXISTE
            txtBarcodeAddModal.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                            if (txtBarcodeAddModal.getText().toString().length() > 0) {
                            if(isConnectedToNetwork()) {
                                sChildContainerNumber = txtBarcodeAddModal.getText().toString();
                                pgProgressDialog = new ProgressDialog(frmTintoGroup.this);
                                pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                pgProgressDialog.setMessage("Obteniendo datos...");
                                pgProgressDialog.setIndeterminate(true);
                                pgProgressDialog.setCancelable(false);
                                pgProgressDialog.show();
                                new AddGroupedContainer().execute();
                            }else{
                                Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este módulo.", Toast.LENGTH_SHORT).show();
                                txtBarcodeAddModal.requestFocus();
                            }
                        }else{
                            Toast.makeText(getApplicationContext(),"Ingrese un número valido.", Toast.LENGTH_SHORT).show();
                            txtBarcodeAddModal.requestFocus();
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
                                pgProgressDialog = new ProgressDialog(frmTintoGroup.this);
                                pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                pgProgressDialog.setMessage("Obteniendo datos...");
                                pgProgressDialog.setIndeterminate(true);
                                pgProgressDialog.setCancelable(false);
                                pgProgressDialog.show();
                                new AddGroupedContainer().execute();
                            }else{
                                Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este módulo.", Toast.LENGTH_SHORT).show();
                                txtBarcodeAddModal.requestFocus();
                            }
                        }else{
                            Toast.makeText(getApplicationContext(),"Ingrese un número valido.", Toast.LENGTH_SHORT).show();
                            txtBarcodeAddModal.requestFocus();
                        }
                    }
                });

                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(btnClose.getWindowToken(), 0);
                        alertDialog2.cancel();

                    }
                });


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
                     GetData = restTemplate.getForObject(cfData.sUrlAddGroupedTintoContainers(sChildContainerNumber, sContainerNumber, sUserId, sScannerId), GetDataWS.class);
                    if (GetData.getId().equals("1")){
                        sMsj = GetData.getContent();
                        dbQrs.UpdateStatusContainer(sChildContainerNumber, "CUERP");
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
                lblContainerQuantity.setText("Cantidad actual: " + String.valueOf(iQuantity));
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
                   new GetData().execute();

            }
            txtBarcodeAddModal.requestFocus();
        }
    }

    public void Reload(){
            if(isConnectedToNetwork()) {
                sChildContainerNumber = txtCode.getText().toString();
                pgProgressDialog = new ProgressDialog(frmTintoGroup.this);
                pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pgProgressDialog.setMessage("Obteniendo datos...");
                pgProgressDialog.setIndeterminate(true);
                pgProgressDialog.setCancelable(false);
                pgProgressDialog.show();
                new GetData().execute();
            }else{
                Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este modulo.", Toast.LENGTH_SHORT).show();
            }

    }



    private void ShowModalPartialWeight() {
        try {
            LayoutInflater liLayoutInflater = LayoutInflater.from(frmTintoGroup.this);
            View vPromptsView = liLayoutInflater.inflate(R.layout.modalpartialweight, null);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(frmTintoGroup.this);
            alertDialogBuilder.setView(vPromptsView);


            txtContainerNumber = (EditText)vPromptsView.findViewById(R.id.txtContainerNumber);
            final ImageButton btnClose =  (ImageButton) vPromptsView.findViewById(R.id.btnClose);
            btnSearchPartial = (ImageButton) vPromptsView.findViewById(R.id.btnSearchPartial);
            txtPartialWeight = (EditText)vPromptsView.findViewById(R.id.txtPartialWeight);
            lblPartialContainerWeight = (TextView)vPromptsView.findViewById(R.id.lblPartialContainerWeight);
            llPartialWeight = (LinearLayout)vPromptsView.findViewById(R.id.llPartialWeight);
            btnGetWeight = (ImageButton)vPromptsView.findViewById(R.id.btnGetWeight);
            txtContainerNumber.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
            txtContainerNumber.setMaxLines(1);
            //btnSearch.setEnabled(false);
            btnSearchPartial.setEnabled(false);
            dPartialWeight = 0.0;
            dParentContainerPartialWeight = 0.0;
            //txtPartialWeight.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL );
            txtPartialWeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

            final Double dWeight = dAvailableWeight - dCurrentWeight;



            alertDialogBuilder.setCancelable(false);
            alertDialog2 = alertDialogBuilder.create();

            //PARA AGREGAR CONTENEDORES A UN PADRE QUE YA EXISTE
            txtContainerNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                        sPartialContainerNumber = String.valueOf(txtContainerNumber.getText());
                        if(isConnectedToNetwork()) {
                                pgProgressDialog = new ProgressDialog(frmTintoGroup.this);
                                pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                pgProgressDialog.setMessage("Obteniendo datos...");
                                pgProgressDialog.setIndeterminate(true);
                                pgProgressDialog.setCancelable(false);
                                pgProgressDialog.show();
                                new GetPartialContainer().execute();
                        }else{
                            Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este módulo.", Toast.LENGTH_SHORT).show();
                            txtContainerNumber.requestFocus();
                        }
                    }return false;
                }
            });
            btnGetWeight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sPartialContainerNumber = String.valueOf(txtContainerNumber.getText());
                    if (sPartialContainerNumber.length() > 0) {
                        if(isConnectedToNetwork()) {
                            pgProgressDialog = new ProgressDialog(frmTintoGroup.this);
                            pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            pgProgressDialog.setMessage("Obteniendo datos...");
                            pgProgressDialog.setIndeterminate(true);
                            pgProgressDialog.setCancelable(false);
                            pgProgressDialog.show();
                            new GetPartialContainer().execute();
                        }else{
                            Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este módulo.", Toast.LENGTH_SHORT).show();
                            txtContainerNumber.requestFocus();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"Ingrese un número valido.", Toast.LENGTH_SHORT).show();
                        txtContainerNumber.requestFocus();
                    }
                }
            });

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(btnClose.getWindowToken(), 0);
                    alertDialog2.cancel();

                }
            });
            btnSearchPartial.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(txtPartialWeight.getText().toString().length()> 0){
                        dPartialWeight = Double.valueOf(txtPartialWeight.getText().toString());
                    //if (dParentContainerPartialWeight > dPartialWeight) {
                        if(isConnectedToNetwork()) {
                            //if(dWeight >= 0.0 && dPartialWeight < dWeight) {
                                pgProgressDialog = new ProgressDialog(frmTintoGroup.this);
                                pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                pgProgressDialog.setMessage("Enviando datos...");
                                pgProgressDialog.setIndeterminate(true);
                                pgProgressDialog.setCancelable(false);
                                pgProgressDialog.show();
                                new SendPartialWeight().execute();
                            /*}else{
                                Toast.makeText(getApplicationContext(), "El peso sobrepasa la capacidad del lote.", Toast.LENGTH_SHORT).show();
                            }*/
                        }else{
                            Toast.makeText(getApplicationContext(), "Necesita estar conectado a la red para utilizar este módulo.", Toast.LENGTH_SHORT).show();
                        }
                    /*}else{
                        Toast.makeText(getApplicationContext(),"Ingrese una cantidad valida, que no sobrepase el peso total del rollo.", Toast.LENGTH_SHORT).show();
                    }*/
                }else{
                    Toast.makeText(getApplicationContext(),"Ingrese una cantidad valida.", Toast.LENGTH_SHORT).show();
                }
                }
            });

            alertDialog2.show();

        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
    }

    private class SendPartialWeight extends AsyncTask<Void, Void, SendPartialWeight> {
        String sError="";
        String sMsj = "";
        @Override
        protected SendPartialWeight doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                GetDataWS GetData;
                //String sContainerNumber, String sParentContainerNumber, String sPartialWeight, String sScannerNumber, String sEmployeeId
                GetData = restTemplate.getForObject(cfData.sUrlSetPartialWeightContainer(sPartialContainerNumber,sContainerNumber, String.valueOf(dPartialWeight), sScannerId, sUserId), GetDataWS.class);
                if (GetData.getId().equals("1")){
                    sMsj = GetData.getContent();
                    //dbQrs.UpdateStatusContainer(sChildContainerNumber, "CUERP");
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
        protected void onPostExecute(SendPartialWeight SendPartialWeight) {
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
                new GetData().execute();

            }
            alertDialog2.cancel();
        }
    }

    private class SendToWip extends AsyncTask<Void, Void, SendToWip> {
        String sError="";
        String sMsj = "";
        @Override
        protected SendToWip doInBackground(Void... params) {
            try {


                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                GetDataWS GetData;
                GetData = restTemplate.getForObject(cfData.sUrlSendContainerToWip(sContainerNumber, sSelectedPart, sScannerId, sUserId), GetDataWS.class);
                if (GetData.getId().equals("1")){
                    sMsj = GetData.getContent();
                    //dbQrs.UpdateStatusContainer(sChildContainerNumber, "CUERP");
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
        protected void onPostExecute(SendToWip SendToWip) {
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
                if(sMsj.contains("Éxito")){
                    mpPlayer = MediaPlayer.create(getApplicationContext(),iRawOk);
                    mpPlayer.start();
                }else{
                    mpPlayer = MediaPlayer.create(getApplicationContext(),iRawError);
                    mpPlayer.start();
                }
                new GetData().execute();

            }

        }
    }

    private void ShowModalConfirmation() {
        try {
            LayoutInflater liLayoutInflater = LayoutInflater.from(frmTintoGroup.this);
            View vPromptsView = liLayoutInflater.inflate(R.layout.modalconfirmation , null);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(frmTintoGroup.this);
            alertDialogBuilder.setView(vPromptsView);


            final ImageButton btnClose =  (ImageButton) vPromptsView.findViewById(R.id.btnClose);
            final ImageButton btnSearch = (ImageButton) vPromptsView.findViewById(R.id.btnSearch);


            alertDialogBuilder.setCancelable(false);
            final AlertDialog alertDialog = alertDialogBuilder.create();


            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pgProgressDialog = new ProgressDialog(frmTintoGroup.this);
                    pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pgProgressDialog.setMessage("Enviando datos...");
                    pgProgressDialog.setIndeterminate(true);
                    pgProgressDialog.setCancelable(false);
                    pgProgressDialog.show();
                    alertDialog.cancel();
                    new SendToWip().execute();



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
}
