package com.inventario.hilcasa.hilcasainventario;

/**
 * Created by fernando maldonado on 5/12/2017.
 */

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class InventoryGroupAdapter extends RecyclerView.Adapter<InventoryGroupAdapter.MyViewHolder> {

    private List<InventoryGroup> LogList;
    ConfigData cfData = new ConfigData();
    QueryDB dbQrs;
    Cursor cCursor;
    Context ctx;
    PublicInterface piInterface;
    String sEmployeeId = "";
    int iCurrentPosition;
    String sScannerId = "";


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtComponentId;
        public TextView txtQuantity;
        public TextView txtComponentDesc;
        public ImageButton btnDelete;


        public MyViewHolder(final View view) {
            super(view);
            txtComponentId = (TextView) view.findViewById(R.id.txtComponentId);
            txtQuantity = (TextView) view.findViewById(R.id.txtQuantity);
            txtComponentDesc = (TextView)view.findViewById(R.id.txtComponentDesc);
            btnDelete = (ImageButton)view.findViewById(R.id.btnDelete);
            ctx =  btnDelete.getContext();

            sScannerId = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
            dbQrs = new QueryDB(ctx);
            dbQrs.open();

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isConnectedToNetwork()) {
                        cCursor = dbQrs.GetEmployeeInfo();
                        if (cCursor.moveToFirst()) {
                            sEmployeeId = cCursor.getString(0);
                        }
                        InventoryGroup Log = LogList.get(getAdapterPosition());
                        iCurrentPosition = getAdapterPosition();
                        new DeleteContainer().execute();
                    }
                }
            });

        }
    }

    public InventoryGroupAdapter(List<InventoryGroup> LogList, PublicInterface piInterace) {
        this.LogList = LogList;
        this.piInterface = piInterace;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.containerrowgroup, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder,final int position) {

        //final Context ctx =  holder.btnDelete.getContext();

        InventoryGroup Log = LogList.get(position);
        holder.txtComponentId.setText(Log.getsBarcode());
        holder.txtQuantity.setText(Log.getsQuantity());
        holder.txtComponentDesc.setText(Log.getsComponentDesc());
    }

    @Override
    public int getItemCount() {
        return LogList.size();
    }

    public void RemoveAt(int position) {
        LogList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, LogList.size());
    }


    private class DeleteContainer extends AsyncTask<Void, Void, DeleteContainer> {
        String sError = "";
        @Override
        protected DeleteContainer doInBackground(Void... params) {
            try {
                InventoryGroup Log = LogList.get(iCurrentPosition);
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlDeleteTempGroupedContainers(Log.getsBarcode(), sEmployeeId, sScannerId), GetDataWS.class);

                if(GetData.getId().equals("1")){}else{
                    sError = "-400";
                }
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
                sError = "-400";
            }
            return null;
        }

        @Override
        protected void onPostExecute(DeleteContainer DeleteContainer) {
            if(sError.equals("-400")){
                Toast.makeText(ctx, "Ocurrio un error al elinminar el dato.", Toast.LENGTH_SHORT).show();
            }else{
                RemoveAt(iCurrentPosition);
                sEmployeeId = "";
                piInterface.Reload();
            }
        }
    }

    /*public boolean isConnectedToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager) ctx
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
        WifiManager wifiManager = (WifiManager)  ctx.getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
        Boolean bState = false;

        if(level >= 2){
            ConnectivityManager connectivity = (ConnectivityManager)ctx
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
}