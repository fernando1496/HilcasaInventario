package com.inventario.hilcasa.hilcasainventario;

/**
 * Created by fernando maldonado on 5/12/2017.
 */

public class PackingListLog {

    private String sPackingListId, sRouteDescription, sCheckpointDescription, sContainerNumber, sIsChecked;

    public PackingListLog() {}

    public PackingListLog(String sPackingListId, String sRouteDescription, String sCheckpointDescription, String sContainerNumber, String sIsChecked) {
        this.sPackingListId = sPackingListId;
        this.sRouteDescription = sRouteDescription;
        this.sCheckpointDescription = sCheckpointDescription;
        this.sContainerNumber = sContainerNumber;
        this.sIsChecked = sIsChecked;

    }

    public String getsPackingListId() {
        return sPackingListId;
    }

    public void setsPackingListId(String sPackingListId) {
        this.sPackingListId = sPackingListId;
    }

    public String getsRouteDescription() {
        return sRouteDescription;
    }

    public void setsRouteDescription(String sRouteDescription) { this.sRouteDescription = sRouteDescription; }

    public String getsCheckpointDescription() {
        return sCheckpointDescription;
    }

    public void setsCheckpointDescription(String sCheckpointDescription) { this.sCheckpointDescription = sCheckpointDescription; }

    public String getsContainerNumber() {
        return sContainerNumber;
    }

    public void setsContainerNumber(String sContainerNumber) { this.sContainerNumber = sContainerNumber; }

    public String getsIsChecked() {
        return sIsChecked;
    }

    public void setsIsChecked(String sIsChecked) {
        this.sIsChecked = sIsChecked;
    }


}
