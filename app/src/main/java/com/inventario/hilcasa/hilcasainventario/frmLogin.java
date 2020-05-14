package com.inventario.hilcasa.hilcasainventario;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
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
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class frmLogin extends AppCompatActivity {

    ImageButton btnLogin, btnClear;
    ConfigData cfData = new ConfigData();
    EditText txtLoginCode;
    String sUser = "";
    String AppName = "";
    String sScannerId = "";
    String sVersion = "";
    Cursor cCursor;
    QueryDB dbQrs = new QueryDB(this);
    TextView txtAppVersion;
    private ProgressDialog pgProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frmlogin);
        AppName = getResources().getString(R.string.app_name);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        try {
            sVersion = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        btnLogin = (ImageButton) findViewById(R.id.btnLogin);
        btnClear = (ImageButton) findViewById(R.id.btnClear);
        txtLoginCode = (EditText) findViewById(R.id.txtLoginCode);
        txtAppVersion = (TextView) findViewById(R.id.txtAppVersion);
        sScannerId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        txtAppVersion.setText("Version "+sVersion);
        dbQrs.open();
        if (isConnectedToNetwork()) {
            pgProgressDialog = new ProgressDialog(frmLogin.this);
            pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pgProgressDialog.setMessage("Sincronizando...");
            pgProgressDialog.setIndeterminate(true);
            pgProgressDialog.setCancelable(false);
            pgProgressDialog.show();
            new SetData().execute();

        } else {
            Toast.makeText(getApplicationContext(), "No tiene conexión a una red.", Toast.LENGTH_SHORT).show();
        }
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtLoginCode.setText("");
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sUser = txtLoginCode.getText().toString();
                if (sUser.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Ingrese su código de empleado", Toast.LENGTH_SHORT).show();
                } else {
                    if (isConnectedToNetwork()) {
                        pgProgressDialog = new ProgressDialog(frmLogin.this);
                        pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pgProgressDialog.setMessage("Sincronizando...");
                        pgProgressDialog.setIndeterminate(true);
                        pgProgressDialog.setCancelable(false);
                        pgProgressDialog.show();
                        new GetUser().execute();
                    } else {
                        cCursor = dbQrs.GetEmployeeInfo();
                        if (cCursor.moveToFirst()) {
                            if (sUser.equals(cCursor.getString(0))) {
                                dbQrs.DeleteLastLog();
                                txtLoginCode.setText("");
                                final SharedPreferences spPref = getSharedPreferences("UserData", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = spPref.edit();
                                editor.putString("sAreaId","");
                                editor.putString("sBinId","");
                                editor.commit();
                                Intent intent = new Intent(getApplicationContext(), frmMenu.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), "No tiene acceso a la red.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        });
        txtLoginCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if (txtLoginCode.getText().toString().startsWith("0")){
                        sUser = txtLoginCode.getText().toString().substring(1);
                    if (sUser.length() == 0) {
                        Toast.makeText(getApplicationContext(), "Ingrese su código de empleado", Toast.LENGTH_SHORT).show();
                    } else {
                        if (isConnectedToNetwork()) {
                            pgProgressDialog = new ProgressDialog(frmLogin.this);
                            pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            pgProgressDialog.setMessage("Sincronizando...");
                            pgProgressDialog.setIndeterminate(true);
                            pgProgressDialog.setCancelable(false);
                            pgProgressDialog.show();
                            new GetUser().execute();
                        } else {
                            cCursor = dbQrs.GetEmployeeInfo();
                            if (cCursor.moveToFirst()) {
                                if (sUser.equals(cCursor.getString(0))) {
                                    dbQrs.DeleteLastLog();
                                    Intent intent = new Intent(getApplicationContext(), frmMenu.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(getApplicationContext(), "No tiene acceso a la red.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
                }
                return false;
            }
        });
    }

    private class GetUser extends AsyncTask<Void, Void, GetUser> {
        String sError = "";
        @Override
        protected GetUser doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(cfData.iTimeOut);
                ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(cfData.iTimeOut);
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlWebServiceGetUserValidation(sUser) , GetDataWS.class);
                if(GetData.getId().equals("1")){
                    dbQrs.DeleteUser();
                    if (GetData.getContent().contains(String.valueOf((char) 164))) {
                        String[] sContainer = GetData.getContent().split(String.valueOf((char) 164));
                        for (int n = 0; n < sContainer.length; n++) {
                            String strContainer = sContainer[n];
                            String sData[] = strContainer.split(String.valueOf((char) 165));
                            String sEmployeeId = sData[0];
                            String sFullName = sData[1];
                            sUser = sEmployeeId;
                            dbQrs.InsertCurrentUser(sEmployeeId, sFullName);
                        }
                    }else{
                        sUser = "";
                    }}else{
                    sError =  sError + "-400" ;
                }
            } catch (Exception e) {
                    if (pgProgressDialog != null) {
                        pgProgressDialog.dismiss();
                        Log.e("MainActivity", e.getMessage(), e);
                        sError = sError + "-400";
                    }
            }
            return null;
        }

        @Override
        protected void onPostExecute(GetUser GetEmployee) {

            if (sError.contains("-400")) {
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Problemas de conexión.", Toast.LENGTH_SHORT).show();
                    cCursor = dbQrs.GetEmployeeInfo();
                    if(cCursor.moveToFirst()){
                        if(sUser.equals(cCursor.getString(0))){
                            dbQrs.DeleteLastLog();
                            txtLoginCode.setText("");
                            Intent intent = new Intent(getApplicationContext(), frmMenu.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(getApplicationContext(), "No tiene acceso a la red.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } else {
                    if (sUser.length() == 0) {
                        if (pgProgressDialog != null) {
                            pgProgressDialog.dismiss();
                        }
                        Toast.makeText(getApplicationContext(), "Usuario no valido.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (pgProgressDialog != null) {
                            pgProgressDialog.dismiss();
                        }
                        final SharedPreferences spPref = getSharedPreferences("UserData", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = spPref.edit();
                        editor.putString("sAreaId","");
                        editor.putString("sBinId","");
                        editor.commit();
                        Intent intent = new Intent(getApplicationContext(), frmMenu.class);
                        startActivity(intent);
                    }
                }
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
    /*public boolean isConnectedToInternet() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);

        if(level >= 2){
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
        }else{
            return false;
        }
        return false;
    }*/

    private class SetData extends AsyncTask<Void, Void, SetData> {
        String sQuery = "";
        GetDataWS GetData;
        String sAppUpdated = "";
        Boolean bFullLog = false;
        @Override
        protected SetData doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(cfData.iTimeOut);
                ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(cfData.iTimeOut);
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                /*
                +
                cCursor = dbQrs.GetOfflineLogContainer();
                if(cCursor.moveToFirst()) {
                    do{
                        String sId = cCursor.getString(0);
                        sQuery = sQuery + cCursor.getString(1);
                        GetData = restTemplate.getForObject(cfData.sUrlWebServiceInsertNewContainer + sQuery, GetDataWS.class);
                    }while(cCursor.moveToNext());
                    if (GetData.getId().equals("1")) {
                        dbQrs.DeleteLogOfflineContainers();
                    }
                }

                cCursor = dbQrs.GetOfflineLog();
                if(cCursor.moveToFirst()) {
                    do{
                        String sId = cCursor.getString(0);
                        String sQuery = cCursor.getString(1);
                        GetData = restTemplate.getForObject(cfData.sUrlWebServiceSetData + sQuery, GetDataWS.class);
                        if (GetData.getId().equals("1")) {
                        dbQrs.DeleteLogOffline(sId);
                        }
                }while(cCursor.moveToNext());
                }*/

                 GetData = restTemplate.getForObject(cfData.sUrlWebServiceReload + sScannerId, GetDataWS.class);
                if(GetData.getId().equals("1")){
                    if(GetData.getContent().equals("True")){
                        final SharedPreferences spPref = getSharedPreferences("UserData", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = spPref.edit();
                        editor.putString("updated", "false");
                        editor.commit();
                    }
                }

                cCursor = dbQrs.GetOfflineLogContainer();
                if(cCursor.moveToFirst()) {
                    do{
                        bFullLog = true;
                    }while(cCursor.moveToNext());
                }

                cCursor = dbQrs.GetOfflineLog();
                if(cCursor.moveToFirst()) {
                    do{
                        bFullLog = true;
                    }while(cCursor.moveToNext());
                }

                if(bFullLog == false) {
                    GetData = restTemplate.getForObject(cfData.sUrlWebServiceUpdateAppVersion(sScannerId, AppName, String.valueOf(BuildConfig.VERSION_CODE)), GetDataWS.class);
                    GetData = restTemplate.getForObject(cfData.sUrlWebServiceCheckCurrentAppVersion(sScannerId, AppName), GetDataWS.class);
                    if (GetData.getId().equals("1")) {
                        sAppUpdated = GetData.getContent();
                    }
                }

            } catch (Exception e) {
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                }
                Log.e("MainActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(SetData SetData) {
            if (pgProgressDialog != null) {
                pgProgressDialog.dismiss();
            }

            if(sAppUpdated.equals("True")){

            }else if(sAppUpdated.equals("False")){
                pgProgressDialog = new ProgressDialog(frmLogin.this);
                pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pgProgressDialog.setMessage("Actualizando app...");
                pgProgressDialog.setIndeterminate(true);
                pgProgressDialog.setCancelable(false);
                pgProgressDialog.show();
                new GetApp().execute();
            }else{

            }
        }
    }

    private class GetApp extends AsyncTask<Void, Void, GetApp> {
        String sError = "";
        @Override
        protected GetApp doInBackground(Void... params) {



            FTPClient ftpClient = new FTPClient();
            FileOutputStream FileOut;
            File TargetPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "Apk");
            File targetFile = new File(TargetPath, AppName + ".apk");

            if (!TargetPath.exists()) {
                TargetPath.mkdirs();
            }

            try {
                ftpClient.connect("192.168.167.65", 21);
                ftpClient.enterLocalPassiveMode();
                boolean b = ftpClient.login("anonymous", "");

                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                targetFile.createNewFile();
                FileOut = new FileOutputStream(targetFile);
                boolean result = ftpClient.retrieveFile("/" + AppName + ".apk", FileOut);
                ftpClient.disconnect();
                FileOut.close();

            } catch (IOException e) {
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                }
                e.printStackTrace();
            } catch (Exception e){
                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();
                }
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(GetApp GetApp) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "Apk" + "/" + AppName + ".apk")), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (pgProgressDialog != null) {
                pgProgressDialog.dismiss();
            }
            startActivity(intent);
        }
    }


   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menuprueba, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case R.id.btnContar:
                int ic= 0;
               cCursor = dbQrs.GetOfflineLog();
                if (cCursor.moveToFirst()){
                do{
                    ic = ic +1;
                }
                while(cCursor.moveToNext());
            }

                cCursor = dbQrs.GetOfflineLogContainer();
                if(cCursor.moveToFirst()) {
                    do{
                        ic = ic +1;
                    }while(cCursor.moveToNext());
                }

                Toast.makeText(getApplicationContext(), String.valueOf(ic), Toast.LENGTH_LONG).show();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

}
