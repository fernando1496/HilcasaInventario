package com.inventario.hilcasa.hilcasainventario;

/**
 * Created by fernando maldonado on 5/12/2017.
 */

import android.animation.ValueAnimator;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class InventoryTempListRcvVendorAdapter extends RecyclerView.Adapter<InventoryTempListRcvVendorAdapter.MyViewHolder> {

    private List<InventoryTempListRcvVendor> LogList;
    ConfigData cfData = new ConfigData();
    QueryDB dbQrs;
    Cursor cCursor;
    Context ctx;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtComponentId;
        public TextView txtQuantity;
        public TextView txtComponentDesc;
        public ImageButton btnDelete;
        public ImageButton btnEdit;


        public MyViewHolder(final View view) {
            super(view);
            txtComponentId = (TextView) view.findViewById(R.id.txtComponentId);
            txtQuantity = (TextView) view.findViewById(R.id.txtQuantity);
            txtComponentDesc = (TextView)view.findViewById(R.id.txtComponentDesc);
            btnDelete = (ImageButton)view.findViewById(R.id.btnDelete);
            btnEdit = (ImageButton)view.findViewById(R.id.btnEdit);
            ctx =  btnDelete.getContext();


            dbQrs = new QueryDB(ctx);
            dbQrs.open();

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InventoryTempListRcvVendor Log = LogList.get(getAdapterPosition());
                    dbQrs.DeleteComponentListItem(Log.getsComponentId(), Log.getsEmployeeId());
                    RemoveAt(getAdapterPosition());
                }
            });

            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    InventoryTempListRcvVendor Log = LogList.get(getAdapterPosition());
                    ShowModalEditContainer(Log.getsComponentId(), Log.getsEmployeeId(), getAdapterPosition());
                }
            });
        }
    }



    public InventoryTempListRcvVendorAdapter(List<InventoryTempListRcvVendor> LogList) {
        this.LogList = LogList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inventoryvendorrowtemplist, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder,final int position) {

        //final Context ctx =  holder.btnDelete.getContext();

        InventoryTempListRcvVendor Log = LogList.get(position);
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

    private void ShowModalEditContainer(final String sComponentId,final String sEmployeeId,final int iPosition) {
        try {
            LayoutInflater liLayoutInflater = LayoutInflater.from(ctx);
            View vPromptsView = liLayoutInflater.inflate(R.layout.modaleditcontainer, null);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
            alertDialogBuilder.setView(vPromptsView);


            final EditText txtBarcode = (EditText)vPromptsView.findViewById(R.id.txtBarcode);
            final EditText txtQuantity = (EditText)vPromptsView.findViewById(R.id.txtQuantity);
            final TextView lblComponent = (TextView)vPromptsView.findViewById(R.id.lblComponent);
            final ImageButton btnClose =  (ImageButton) vPromptsView.findViewById(R.id.btnClose);
            final ImageButton btnSearch = (ImageButton) vPromptsView.findViewById(R.id.btnSearch);


            txtBarcode.setMaxLines(1);

            cCursor = dbQrs.GetListByUserAndComponentId(sEmployeeId, sComponentId);
            if(cCursor.moveToFirst()){
                txtBarcode.setText(cCursor.getString(4));
                lblComponent.setText(cCursor.getString(1));
                txtQuantity.setText(cCursor.getString(2));
                txtBarcode.setEnabled(false);
                txtBarcode.setFocusable(false);
                txtQuantity.setFocusable(true);
            }

            alertDialogBuilder.setCancelable(false);
            final AlertDialog alertDialog = alertDialogBuilder.create();

            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (txtQuantity.getText().toString().length() > 0) {
                        dbQrs.UpdateTempList(sComponentId, sEmployeeId, txtQuantity.getText().toString());
                        InventoryTempListRcvVendor Log = LogList.get(iPosition);
                        Log.setsQuantity(txtQuantity.getText().toString());
                        notifyItemRemoved(iPosition);
                        notifyItemRangeChanged(iPosition, LogList.size());
                        alertDialog.cancel();
                    }else{
                        Toast.makeText(ctx,"Ingrese una cantidad valida.", Toast.LENGTH_SHORT).show();
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


}