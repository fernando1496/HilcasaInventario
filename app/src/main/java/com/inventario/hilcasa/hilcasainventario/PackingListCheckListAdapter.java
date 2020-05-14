package com.inventario.hilcasa.hilcasainventario;

/**
 * Created by fernando maldonado on 5/12/2017.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class PackingListCheckListAdapter extends RecyclerView.Adapter<PackingListCheckListAdapter.MyViewHolder> {

    private List<PackingListLog> LogList;
    ConfigData cfData = new ConfigData();
    Context ctx;
    QueryDB dbQrs;
    Cursor cCursor;
    int iCurrentPosition;
    String sContainerNumber = "";
    String sCheckpointId = "";
    String sCheckpointGroup = "";
    String sEmployeeId = "";
    String sScannerId= "";
    PublicInterface piInterface;
    private ProgressDialog pgProgressDialog;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView lblDesc;
        public LinearLayoutCompat llContainer;
        public CardView cvCardView;

        public MyViewHolder(View view) {
            super(view);
            lblDesc = (TextView) view.findViewById(R.id.lblDesc);

            llContainer= (LinearLayoutCompat) view.findViewById(R.id.llContainer);
            cvCardView = (CardView)view.findViewById(R.id.card_view);
            ctx =  llContainer.getContext();
            sScannerId = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
            dbQrs = new QueryDB(ctx);
            dbQrs.open();



            llContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                   // }
                    return false;
                }
            });

        }
    }


    public PackingListCheckListAdapter(List<PackingListLog> LogList, PublicInterface piInterace ) {
        this.LogList = LogList;
        this.piInterface = piInterace;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.packinglistcheckrow, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        PackingListLog Log = LogList.get(position);
        holder.lblDesc.setText(Log.getsContainerNumber());
        if(Log.getsIsChecked().equals("t")){
            holder.cvCardView.setCardBackgroundColor(Color.parseColor("#c8e6c9"));
        }else{
            holder.cvCardView.setCardBackgroundColor(Color.parseColor("#ffcdd2"));
        }

    }

    @Override
    public int getItemCount() {
        return LogList.size();
    }


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



    private class ReturnContainer extends AsyncTask<Void, Void, ReturnContainer> {
        String sError = "";
        @Override
        protected ReturnContainer doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlReturnContainer(sContainerNumber , sScannerId, sEmployeeId, sCheckpointId), GetDataWS.class);

                if(GetData.getId().equals("1")){}else{
                    sError = "-400";
                }
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
                sError = "-400";

                if (pgProgressDialog != null) {
                    pgProgressDialog.dismiss();}

            }
            return null;
        }

        @Override
        protected void onPostExecute(ReturnContainer ReturnContainer) {
            if (pgProgressDialog != null) {
                pgProgressDialog.dismiss();}
            if(sError.equals("-400")){
                Toast.makeText(ctx, "Ocurrio un error al regresar el dato.", Toast.LENGTH_SHORT).show();
            }else{
                dbQrs.UpdateContainersSpecificRcvReturn(sContainerNumber);
                dbQrs.DeleteMovimientoLog(sContainerNumber, sCheckpointGroup);
                LogList.remove(iCurrentPosition);
                notifyItemRemoved(iCurrentPosition);
                Toast.makeText(ctx, "Realizado exitosamente.", Toast.LENGTH_SHORT).show();
                notifyItemRangeChanged(iCurrentPosition, LogList.size());
                sEmployeeId = "";
                piInterface.Reload();
            }
        }
    }

}