package com.inventario.hilcasa.hilcasainventario;

/**
 * Created by fernando maldonado on 5/12/2017.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class InventoryLogRcvAreaAdapter extends RecyclerView.Adapter<InventoryLogRcvAreaAdapter.MyViewHolder> {

    private List<InventoryLogRcvArea> LogList;
    ConfigData cfData = new ConfigData();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView lblDesc, lblDate, lblArea;
        public ImageView ivItem;

        public MyViewHolder(View view) {
            super(view);
            lblDesc = (TextView) view.findViewById(R.id.lblDesc);
            lblDate = (TextView) view.findViewById(R.id.lblDate);
            ivItem = (ImageView) view.findViewById(R.id.ivItem);
            lblArea = (TextView) view.findViewById(R.id.lblArea);
        }
    }


    public InventoryLogRcvAreaAdapter(List<InventoryLogRcvArea> LogList) {
        this.LogList = LogList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inventoryarearow, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        InventoryLogRcvArea Log = LogList.get(position);
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
}