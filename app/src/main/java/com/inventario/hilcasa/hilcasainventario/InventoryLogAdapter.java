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
        import android.support.v7.widget.LinearLayoutCompat;
        import android.support.v7.widget.RecyclerView;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageButton;
        import android.widget.TextView;
        import android.widget.Toast;
        import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
        import org.springframework.web.client.RestTemplate;
        import java.util.List;

public class InventoryLogAdapter extends RecyclerView.Adapter<InventoryLogAdapter.MyViewHolder> {

    private List<InventoryLog> LogList;
    PublicInterface piInterface;
    QueryDB dbQrs;
    Cursor cCursor;
    Context ctx;
    String sContainerNumber = "";
    String sEmployeeId = "";
    ConfigData cfData = new ConfigData();
    int iCurrentPosition;
    String sScannerId= "";

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtContainerId, txtArea, txtBin, txtWeight;
        public LinearLayoutCompat llContainer;

        public MyViewHolder(View view) {
            super(view);
            txtContainerId = (TextView) view.findViewById(R.id.txtContainerId);
            txtBin = (TextView) view.findViewById(R.id.txtBin);
            txtWeight = (TextView) view.findViewById(R.id.txtWeight);
            txtArea = (TextView) view.findViewById(R.id.txtArea);
            llContainer= (LinearLayoutCompat) view.findViewById(R.id.llContainer);
            ctx =  llContainer.getContext();
            sScannerId = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
            dbQrs = new QueryDB(ctx);
            dbQrs.open();

            llContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    InventoryLog Log = LogList.get(getAdapterPosition());
                    if(Log.getsCheckPointId().equals(cfData.sInventoryAddCheckpointId)) {
                        ShowDeleteModal(Log.getsContainer().toString(), Log.getsArea().toString(), Log.getsBin().toString() ,getAdapterPosition());
                        //Toast.makeText(ctx, Log.getsContainer().toString(), Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });
        }
    }

    public InventoryLogAdapter(List<InventoryLog> LogList, PublicInterface piInterace) {
        this.LogList = LogList;
        this.piInterface = piInterace;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inventoryrvrow, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        InventoryLog Log = LogList.get(position);
        holder.txtContainerId.setText(Log.getsContainer());
        holder.txtBin.setText(Log.getsBin());
        holder.txtWeight.setText(Log.getsWeight());
        holder.txtArea.setText(Log.getsArea());
    }

    @Override
    public int getItemCount() {
        return LogList.size();
    }


    private void ShowDeleteModal(final String sContainerId,final String sArea,final String sBin, final int iPosition) {
        try {
            LayoutInflater liLayoutInflater = LayoutInflater.from(ctx);
            View vPromptsView = liLayoutInflater.inflate(R.layout.modaldeletecontainer, null);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
            alertDialogBuilder.setView(vPromptsView);

            final ImageButton btnClose =  (ImageButton) vPromptsView.findViewById(R.id.btnClose);
            final ImageButton btnSearch = (ImageButton) vPromptsView.findViewById(R.id.btnSearch);
            final TextView lblArea = (TextView) vPromptsView.findViewById(R.id.lblArea);
            final TextView lblBin = (TextView) vPromptsView.findViewById(R.id.lblBin);
            final TextView lblCode = (TextView) vPromptsView.findViewById(R.id.lblCode);

            lblArea.setText(sArea);
            lblBin.setText(sBin);
            lblCode.setText(sContainerId);

            alertDialogBuilder.setCancelable(false);
            final AlertDialog alertDialog = alertDialogBuilder.create();

            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isConnectedToNetwork()) {

                       cCursor = dbQrs.GetEmployeeInfo();
                        if(cCursor.moveToFirst()){
                            sEmployeeId = cCursor.getString(0);
                        }
                        iCurrentPosition = iPosition;
                        sContainerNumber = sContainerId;
                        new DeleteContainer().execute();
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

    private class DeleteContainer extends AsyncTask<Void, Void, DeleteContainer> {
        String sError = "";
        @Override
        protected DeleteContainer doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlWebServiceDeleteContainer + sContainerNumber + "&sScannerNumber=" + sScannerId + "&sEmployeeId=" + sEmployeeId, GetDataWS.class);

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
                dbQrs.DeleteContainersDeleted(sContainerNumber, "CUERP");
                dbQrs.DeleteContainersLog(sContainerNumber, cfData.sInventoryAddCheckpointId);
                LogList.remove(iCurrentPosition);
                notifyItemRemoved(iCurrentPosition);
                notifyItemRangeChanged(iCurrentPosition, LogList.size());
                sEmployeeId = "";
                piInterface.Reload();
            }
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



}