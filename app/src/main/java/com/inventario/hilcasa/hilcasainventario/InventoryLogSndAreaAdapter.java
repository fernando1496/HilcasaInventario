package com.inventario.hilcasa.hilcasainventario;

/**
 * Created by fernando maldonado on 5/12/2017.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class InventoryLogSndAreaAdapter extends RecyclerView.Adapter<InventoryLogSndAreaAdapter.MyViewHolder> {

    private List<InventoryLogSndArea> LogList;
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
        public TextView lblDesc, lblDate, lblArea;
        public ImageView ivItem;
        public LinearLayoutCompat llContainer;

        public MyViewHolder(View view) {
            super(view);
            lblDesc = (TextView) view.findViewById(R.id.lblDesc);
            lblDate = (TextView) view.findViewById(R.id.lblDate);
            ivItem = (ImageView) view.findViewById(R.id.ivItem);
            lblArea = (TextView) view.findViewById(R.id.lblArea);
            llContainer= (LinearLayoutCompat) view.findViewById(R.id.llContainer);
            ctx =  llContainer.getContext();
            sScannerId = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
            dbQrs = new QueryDB(ctx);
            dbQrs.open();

            llContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    InventoryLogSndArea Log = LogList.get(getAdapterPosition());
                    //if(Log.getsCheckPointId().equals(cfData.sInventoryAddCheckpointId)) {
                        sCheckpointId = Log.getsCheckpointId();
                        sCheckpointGroup = Log.getsCheckPointGroup();
                        ShowReturnModal(Log.getsContainer().toString(), Log.getsArea().toString(), Log.getsDate().toString() ,getAdapterPosition(), Log.getsContainerDesc());

                        //Toast.makeText(ctx, Log.getsContainer().toString(), Toast.LENGTH_SHORT).show();
                   // }
                    return false;
                }
            });

        }
    }


    public InventoryLogSndAreaAdapter(List<InventoryLogSndArea> LogList, PublicInterface piInterace ) {
        this.LogList = LogList;
        this.piInterface = piInterace;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inventoryarearow, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        InventoryLogSndArea Log = LogList.get(position);
        holder.lblDesc.setText(Log.getsContainer() + " " + Log.getsContainerDesc());
        holder.lblDate.setText(Log.getsDate());
        holder.lblArea.setText(Log.getsArea());

        if(Log.getsStateForm().equals(cfData.sChemicals)){
            holder.ivItem.setImageResource(R.drawable.ic_chem);
        }else if(Log.getsStateForm().equals(cfData.sFabricsT)){
            holder.ivItem.setImageResource(R.drawable.ic_fabric);
        }else if(Log.getsStateForm().equals(cfData.sRawMaterial)){
            holder.ivItem.setImageResource(R.drawable.ic_hay);
        }else if(Log.getsStateForm().equals(cfData.sThread)){
            holder.ivItem.setImageResource(R.drawable.ic_thread);
        }else if(Log.getsStateForm().equals(cfData.sFuel)){
            holder.ivItem.setImageResource(R.drawable.ic_fuel);
        }else if(Log.getsStateForm().equals(cfData.sOfficeSupplies)){
            holder.ivItem.setImageResource(R.drawable.ic_office);
        }else if(Log.getsStateForm().equals(cfData.sGroceries)){
            holder.ivItem.setImageResource(R.drawable.ic_groceries);
        }else if(Log.getsStateForm().equals(cfData.sSupplies)){
            holder.ivItem.setImageResource(R.drawable.ic_supplies);
        }else if(Log.getsStateForm().equals(cfData.sFabricsC)){
            holder.ivItem.setImageResource(R.drawable.ic_fabric);
        }else{
            holder.ivItem.setImageResource(R.drawable.ic_supplies);}


    }

    @Override
    public int getItemCount() {
        return LogList.size();
    }

    private void ShowReturnModal(final String sContainerId,final String sArea,final String sBin, final int iPosition, String sContainerDesc) {
        try {
            LayoutInflater liLayoutInflater = LayoutInflater.from(ctx);
            View vPromptsView = liLayoutInflater.inflate(R.layout.modalreturncontainer, null);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
            alertDialogBuilder.setView(vPromptsView);

            final ImageButton btnClose =  (ImageButton) vPromptsView.findViewById(R.id.btnClose);
            final ImageButton btnSearch = (ImageButton) vPromptsView.findViewById(R.id.btnSearch);
            final TextView lblArea = (TextView) vPromptsView.findViewById(R.id.lblArea);
            final TextView lblBin = (TextView) vPromptsView.findViewById(R.id.lblBin);
            final TextView lblCode = (TextView) vPromptsView.findViewById(R.id.lblCode);

            lblArea.setText(sArea);
            lblBin.setText(sBin);
            lblCode.setText(sContainerId + " " + sContainerDesc);

            alertDialogBuilder.setCancelable(false);
            final AlertDialog alertDialog = alertDialogBuilder.create();

            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isConnectedToNetwork()) {
                        pgProgressDialog = new ProgressDialog(ctx);
                        pgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pgProgressDialog.setMessage("Enviando...");
                        pgProgressDialog.setIndeterminate(true);
                        pgProgressDialog.setCancelable(false);
                        pgProgressDialog.show();
                        cCursor = dbQrs.GetEmployeeInfo();
                        if(cCursor.moveToFirst()){
                            sEmployeeId = cCursor.getString(0);
                        }
                        iCurrentPosition = iPosition;
                        sContainerNumber = sContainerId;
                        new ReturnContainer().execute();
                        alertDialog.cancel();
                    }else{
                    }
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

    /*public boolean isConnectedToInternet() {
        WifiManager wifiManager = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);

        if(level >= 2){
            ConnectivityManager connectivity = (ConnectivityManager)ctx
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