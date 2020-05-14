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
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class frmInventoryOutAreaContainer extends AppCompatActivity {

    Cursor cCursor;
    QueryDB dbQrs = new QueryDB(this);
    String sUser;
    TextView txtUser;
    ConfigData cfData = new ConfigData();
    SwitchCompat scUseLot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frminventoryoutareacontainer);
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

        getSupportActionBar().setTitle(R.string.sDespachoArea);
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            InventoryOutAreaTabs fragment = new InventoryOutAreaTabs();
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
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.btnLastLog:
                Intent IttIntent = new Intent(getApplicationContext(), frmLastLog.class);
                Bundle bdlExtras = new Bundle();
                bdlExtras.putString("sIdentifier",cfData.sIdentifierAreaOut);
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
            case R.id.btnPackingList:
                ShowModalPackingList();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getCurrentUser(){
        cCursor = dbQrs.GetEmployeeInfo();
        if(cCursor.moveToFirst()){
            sUser = cCursor.getString(1);
            txtUser.setText(sUser);
        }
    }
    private void ShowModalPackingList() {
        try {
            LayoutInflater liLayoutInflater = LayoutInflater.from(this );
            View vPromptsView = liLayoutInflater.inflate(R.layout.modalpackinglist, null);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setView(vPromptsView);

            final ImageButton btnClose =  (ImageButton) vPromptsView.findViewById(R.id.btnClose);
            final ImageButton btnSearch = (ImageButton) vPromptsView.findViewById(R.id.btnSearch);
            final TextView txtBarcode =(TextView )vPromptsView.findViewById(R.id.txtBarcode);

            txtBarcode.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

            alertDialogBuilder.setCancelable(false);
            final AlertDialog alertDialog = alertDialogBuilder.create();

            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*if(isConnectedToInternet()) {
                        pgProgressDialog = new ProgressDialog(ctx);
                        pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pgProgressDialog.setMessage("Enviando...");
                        pgProgressDialog.setIndeterminate(true);
                        pgProgressDialog.setCancelable(false);
                        pgProgressDialog.show();


                        alertDialog.cancel();
                    }else{
                    }*/
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