package com.inventario.hilcasa.hilcasainventario;

/**
 * Created by fernando maldonado on 5/12/2017.
 */

public class InventoryLog {

    private String sContainer, sBin, sArea, sCheckPointId, sWeight;

    public InventoryLog() {}

    public InventoryLog(String sContainer, String sBin, String sArea, String sCheckPointId, String sWeight) {
        this.sContainer = sContainer;
        this.sBin = sBin;
        this.sArea = sArea;
        this.sCheckPointId = sCheckPointId;
        this.sWeight = sWeight;
    }

    public String getsContainer() {
        return sContainer;
    }

    public void setsContainer(String sContainer) {
        this.sContainer = sContainer;
    }

    public String getsBin() {
        return sBin;
    }

    public void setsBin(String sBin) {
        this.sBin = sBin;
    }

    public String getsArea() {
        return sArea;
    }

    public void setsArea(String sArea) {
        this.sArea = sArea;
    }

    public String getsCheckPointId() {
        return sCheckPointId;
    }

    public void setsCheckPointId(String sCheckPointId) {
        this.sCheckPointId = sCheckPointId;
    }

    public String getsWeight() {
        return sWeight;
    }

    public void setsWeight(String sWeight) {
        this.sWeight = sWeight;
    }
}
