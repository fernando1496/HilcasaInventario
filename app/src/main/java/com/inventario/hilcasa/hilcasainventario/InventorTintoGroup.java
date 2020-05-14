package com.inventario.hilcasa.hilcasainventario;

/**
 * Created by fernando maldonado on 5/12/2017.
 */

public class InventorTintoGroup {

    private String sBarcode, sComponentDesc, sQuantity, sComponentId, sEmployeeId;

    public InventorTintoGroup() {}

    public InventorTintoGroup(String sBarcode, String sComponentDesc, String sQuantity, String sComponentId, String sEmployeeId) {
        this.sBarcode = sBarcode;
        this.sComponentDesc = sComponentDesc;
        this.sQuantity = sQuantity;
        this.sComponentId = sComponentId;
        this.sEmployeeId = sEmployeeId;
    }

    public String getsBarcode() {
        return sBarcode;
    }

    public void setsBarcode(String sBarcode) {
        this.sBarcode = sBarcode;
    }

    public String getsComponentDesc() {
        return sComponentDesc;
    }

    public void setsComponentDesc(String sComponentDesc) {
        this.sComponentDesc = sComponentDesc;
    }

    public String getsQuantity() {
        return sQuantity;
    }

    public void setsQuantity(String sQuantity) {
        this.sQuantity = sQuantity;
    }

    public String getsComponentId() {
        return sComponentId;
    }

    public void setsComponentId(String sComponentId) {
        this.sComponentId = sComponentId;
    }

    public String getsEmployeeId() {
        return sEmployeeId;
    }

    public void setsEmployeeId(String sEmployeeId) {
        this.sEmployeeId = sEmployeeId;
    }
}
