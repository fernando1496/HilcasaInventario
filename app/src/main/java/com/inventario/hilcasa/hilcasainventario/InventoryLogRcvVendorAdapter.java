package com.inventario.hilcasa.hilcasainventario;

/**
 * Created by fernando maldonado on 5/12/2017.
 */

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class InventoryLogRcvVendorAdapter extends RecyclerView.Adapter<InventoryLogRcvVendorAdapter.MyViewHolder> {

    private List<InventoryLogRcvVendor> LogList;
    QueryDB dbQrs;
    Cursor cCursor;
    Context ctx;
    PublicInterface piInterface;
    String sContainerNumber = "";
    String sEmployeeId = "";
    ConfigData cfData = new ConfigData();
    int iCurrentPosition;
    String sScannerId= "";
    String sAreaId= "";
    LinearLayout.LayoutParams txtParamsCode, txtParamsDesc, txtParamsQuantity, llParameters;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtContainerId;
        public TextView txtQuantity;
        public TextView txtArea;
        public CardView cvContainer;
        public LinearLayout llComponentContainer;

        public MyViewHolder(View view) {
            super(view);
            txtContainerId = (TextView) view.findViewById(R.id.txtContainerId);
            txtQuantity = (TextView) view.findViewById(R.id.txtQuantity);
            txtArea = (TextView)view.findViewById(R.id.txtArea);
            cvContainer = (CardView)view.findViewById(R.id.card_view);
            llComponentContainer = (LinearLayout)view.findViewById(R.id.llComponentContainer);
            ctx =  view.getContext();

            llParameters = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            llParameters.setMargins(1, 3, 1, 3);

            txtParamsCode = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1F);
            txtParamsCode.setMargins(0, 0, 0, 0);

            txtParamsDesc = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1F);
            txtParamsDesc.setMargins(0, 0, 0, 0);

            txtParamsQuantity = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 10F);
            txtParamsQuantity.setMargins(0, 0, 0, 0);

            dbQrs = new QueryDB(ctx);
            dbQrs.open();
            view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {

                            if(llComponentContainer.getVisibility() == View.GONE){

                                expand(llComponentContainer);
                            }else{

                                collapse(llComponentContainer);
                            }
                        }
                    });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                   /* InventoryLogRcvVendor Log = LogList.get(getAdapterPosition());
                    cCursor = dbQrs.GetAreaEsp(Log.getsAreaId());
                    String sArea = Log.getsAreaId();
                    if(cCursor.moveToFirst()){
                        sArea = cCursor.getString(1);
                    }
                    if(Log.getsBinId().equals("0")){}else {
                        ShowDeleteModal(Log.getsContainerNumber(), sArea, Log.getsBinId(), getAdapterPosition());
                    }*/
                    return false;
                }
            });
        }
    }

    public InventoryLogRcvVendorAdapter(List<InventoryLogRcvVendor> LogList, PublicInterface piInterace) {
        this.LogList = LogList;
        this.piInterface = piInterace;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inventoryvendorrow, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        InventoryLogRcvVendor Log = LogList.get(position);
        holder.txtContainerId.setText(Log.getsContainerNumber());

        int iQuantity = 0;
        String sDescC = "";
        cCursor =  dbQrs.GetTempContainerByComponentId(Log.getsContainerNumber(), Log.getsIdentifier());
        if(cCursor.moveToFirst()){
            do{
                LinearLayout llRow = new LinearLayout(ctx);
                llRow.setLayoutParams(llParameters);
                llRow.setOrientation(LinearLayout.HORIZONTAL);
                llRow.setBackgroundColor(Color.parseColor("#33d0d0d0"));
                holder.llComponentContainer.addView(llRow);

                TextView txtDesc = new TextView(ctx);
                txtDesc.setLayoutParams(txtParamsDesc);
                txtDesc.setTextColor(Color.parseColor("#000000"));
                txtDesc.setText(cCursor.getString(0));
                txtDesc.setTextSize(20);
                llRow.addView(txtDesc);

                TextView txtQuantity = new TextView(ctx);
                txtQuantity.setLayoutParams(txtParamsQuantity);
                txtQuantity.setTextColor(Color.parseColor("#000000"));
                txtQuantity.setText(cCursor.getString(1));
                txtQuantity.setTextSize(20);
                llRow.addView(txtQuantity);

                iQuantity = iQuantity + cCursor.getInt(1);
                sDescC = sDescC + cCursor.getString(0) + ", ";
                holder.txtArea.setText(cCursor.getString(3));
            }while(cCursor.moveToNext());
        }

        holder.txtQuantity.setText(String.valueOf(iQuantity));
        sDescC = sDescC.substring(0, sDescC.length() - 2);
        String s;
        if(sDescC.length() > 16) {
             s = sDescC.substring(0, 17);
             s = s + "...";
        }else{
             s = sDescC;
        }
        //holder.txtArea.setText(s);
    }

    @Override
    public int getItemCount() {
        return LogList.size();
    }

    public static void expand(final View v) {
        v.measure(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? RecyclerView.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density*4));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density)*4);
        v.startAnimation(a);}

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
                    sAreaId = sArea;
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
                        Toast.makeText(ctx, "Necesita estar conectado a la red.", Toast.LENGTH_LONG).show();
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
        }
    }

    private class DeleteContainer extends AsyncTask<Void, Void, DeleteContainer> {
        String sError = "";
        @Override
        protected DeleteContainer doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                GetDataWS GetData = restTemplate.getForObject(cfData.sUrlWebServiceDeleteComponentContainer + sContainerNumber + "&sScannerNumber=" + sScannerId + "&sEmployeeId=" + sEmployeeId + "&sAreaId=", GetDataWS.class);

                if(GetData.getId().equals("1")){}else{
                    sError = "-400";
                }
            } catch (Exception e) {

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
                dbQrs.RemoveTempContainer(sContainerNumber);
                LogList.remove(iCurrentPosition);
                notifyItemRemoved(iCurrentPosition);
                notifyItemRangeChanged(iCurrentPosition, LogList.size());
                sEmployeeId = "";
                sAreaId = "";
                piInterface.Reload();
            }
        }
    }

   /* public boolean isConnectedToInternet() {
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
        WifiManager wifiManager = (WifiManager)  ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
        Boolean bState = false;

        if(level >= 2){
            ConnectivityManager connectivity = (ConnectivityManager)ctx.getApplicationContext()
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