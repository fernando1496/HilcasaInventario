package com.inventario.hilcasa.hilcasainventario;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class frmLastLog extends AppCompatActivity {


    TextView lblUser;
    Cursor cCursor;
    QueryDB dbQrs = new QueryDB(this);
    private InventoryLogAdapter mAdapter;
    private List<InventoryLog> LogList = new ArrayList<>();
    private RecyclerView rvLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frmlastlog);
        lblUser = (TextView)findViewById(R.id.lblUser);
        rvLog = (RecyclerView)findViewById(R.id.rvLog);
        dbQrs.open();
        getCurrentUser();

        Bundle b = getIntent().getExtras();
        String  sIdentifier = b.getString("sIdentifier");

        ShowLog(sIdentifier);
    }
    public void getCurrentUser() {
        cCursor = dbQrs.GetEmployeeInfo();
        if (cCursor.moveToFirst()) {
            String sUser = cCursor.getString(1);
            lblUser.setText(sUser);
        }
    }

    public void ShowLog(String sIdentifier){
        LogList.clear();
        mAdapter = new InventoryLogAdapter(LogList, null);
        int iScanned = 0;
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvLog.setLayoutManager(mLayoutManager);
        rvLog.setItemAnimator(new DefaultItemAnimator());
        rvLog.setAdapter(mAdapter);
        cCursor = dbQrs.GetLastLog(sIdentifier);
        if(cCursor.moveToFirst()){
            InventoryLog ilLog;
            do{
                ilLog = new InventoryLog(cCursor.getString(0), "", cCursor.getString(1), "", "");
                LogList.add(ilLog);
                mAdapter.notifyDataSetChanged();
                iScanned = iScanned + 1;
            }while (cCursor.moveToNext());
        }
    }

}

