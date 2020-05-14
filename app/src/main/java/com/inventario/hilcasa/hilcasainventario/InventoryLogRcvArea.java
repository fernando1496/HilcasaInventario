package com.inventario.hilcasa.hilcasainventario;

/**
 * Created by fernando maldonado on 5/12/2017.
 */

public class InventoryLogRcvArea {

    private String sContainer, sDate, sArea, sStateForm,  sContainerDesc;

    public InventoryLogRcvArea() {}

    public InventoryLogRcvArea(String sContainer, String sDate, String sArea, String sStateForm, String sContainerDesc) {
        this.sContainer = sContainer;
        this.sDate = sDate;
        this.sArea = sArea;
        this.sStateForm = sStateForm;
        this.sContainerDesc = sContainerDesc;
    }

    public String getsContainer() {
        return sContainer;
    }

    public void setsContainer(String sContainer) {
        this.sContainer = sContainer;
    }

    public String getsDate() {
        return sDate;
    }

    public void setsDate(String sDate) {
        this.sDate = sDate;
    }

    public String getsArea() {
        return sArea;
    }

    public void setsArea(String sArea) {
        this.sArea = sArea;
    }

    public String getsStateForm() {
        return sStateForm;
    }

    public void setsStateForm(String sStateForm) {
        this.sStateForm = sStateForm;
    }

    public String getsContainerDesc() {
        return sContainerDesc;
    }

    public void setsContainerDesc(String sContainerDesc) {
        this.sContainerDesc = sContainerDesc;
    }
}
