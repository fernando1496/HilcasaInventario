package com.inventario.hilcasa.hilcasainventario;

/**
 * Created by fernando maldonado on 5/12/2017.
 */

public class InventoryLogSndArea {

    private String sContainer, sDate, sArea, sStateForm, sCheckpointId, sEmployeeId, sContainerDesc, sCheckPointGroup;

    public InventoryLogSndArea() {}

    public InventoryLogSndArea(String sContainer, String sDate, String sArea, String sStateForm, String sCheckpointId, String sEmployeeId, String sContainerDesc, String sCheckPointGroup) {
        this.sContainer = sContainer;
        this.sDate = sDate;
        this.sArea = sArea;
        this.sStateForm = sStateForm;
        this. sCheckpointId = sCheckpointId;
        this.sEmployeeId = sEmployeeId;
        this.sContainerDesc = sContainerDesc;
        this.sCheckPointGroup = sCheckPointGroup;
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

    public String getsCheckpointId() {
        return sCheckpointId;
    }

    public void setsCheckpointId(String sCheckpointId) {
        this.sCheckpointId = sCheckpointId;
    }

    public String getsEmployeeId() {
        return sEmployeeId;
    }

    public void setsEmployeeId(String sEmployeeId) {
        this.sEmployeeId = sEmployeeId;
    }

    public String getsContainerDesc() {
        return sContainerDesc;
    }

    public void setsContainerDesc(String sContainerDesc) {
        this.sContainerDesc = sContainerDesc;
    }

    public String getsCheckPointGroup() {
        return sCheckPointGroup;
    }

    public void setsCheckPointGroup(String sCheckPointGroup) {
        this.sCheckPointGroup = sCheckPointGroup;
    }
}
