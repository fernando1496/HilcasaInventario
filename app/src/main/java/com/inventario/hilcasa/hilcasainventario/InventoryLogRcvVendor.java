package com.inventario.hilcasa.hilcasainventario;

/**
 * Created by fernando maldonado on 5/12/2017.
 */

public class InventoryLogRcvVendor {

    private String sContainerNumber, sDesc, sIdentifier, sAreaId, sBinId;

    public InventoryLogRcvVendor() {}

    public InventoryLogRcvVendor(String sContainerNumber, String sDesc, String sIdentifier, String sAreaId, String sBinId) {
        this.sContainerNumber = sContainerNumber;
        this.sDesc = sDesc;
        this.sIdentifier = sIdentifier;
        this.sAreaId = sAreaId;
        this.sBinId = sBinId;
    }

    public String getsContainerNumber() {
        return sContainerNumber;
    }

    public void setsContainerNumber(String sContainerNumber) { this.sContainerNumber = sContainerNumber; }

    public String getsDesc() {
        return sDesc;
    }

    public void setsDesc(String sDesc) {
        this.sDesc = sDesc;
    }

    public String getsIdentifier() {
        return sIdentifier;
    }

    public void setsIdentifier(String sIdentifier) {
        this.sIdentifier = sIdentifier;
    }

    public String getsAreaId() {
        return sAreaId;
    }

    public void setsAreaId(String sAreaId) {
        this.sAreaId = sAreaId;
    }

    public String getsBinId() {
        return sBinId;
    }

    public void setsBinId(String sBinId) {
        this.sBinId = sBinId;
    }

}
