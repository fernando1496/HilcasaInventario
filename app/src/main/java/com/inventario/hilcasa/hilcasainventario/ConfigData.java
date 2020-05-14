package com.inventario.hilcasa.hilcasainventario;

/**
 * Created by fernando maldonado on 3/3/2017.
 */

public class ConfigData {
    //URL A WS

    String sIp = "192.168.167.51:98";//REAL

    //String sUrlWebSerivceGetContainer = "http://192.168.167.51:98/index/GetInventoryContainerResult?sAreaId=";

    public String sUrlWebServiceGetUserValidation(String sEmployeeId){
        String sURL = String.format("http://192.168.167.51:98/index/GetUserValidation?sEmployeeId=%1$s", sEmployeeId);
        return sURL;
    }
    //String sUrlWebServiceGetUserValidation = "http://192.168.167.51:98/index/GetUserValidation?sEmployeeId=";

    public String sUrlWebServiceGetAreasByUser(String sEmployeeId){
        String sURL = String.format("http://192.168.167.51:98/index/GetAreasByUser?sEmployeeId=%1$s", sEmployeeId);
        return sURL;
    }
   // String sUrlWebServiceGetAreasByUser = "http://192.168.167.51:98/index/GetAreasByUser?sEmployeeId=";

    public String sUrlWebServiceGetBinByArea(){
        String sURL = String.format("http://192.168.167.51:98/index/GetBinByArea");
        return sURL;
    }
    //String sUrlWebServiceGetBinByArea = "http://192.168.167.51:98/index/GetBinByArea?sAreaId=";

    //String sUrlWebServiceGetAllContainers = "http://192.168.167.51:98/index/GetInventoryAllContainerResult";
   // String sUrlWebServiceSetData = "http://192.168.167.51:98/index/SetDataInventory?sQuery=";
    String sUrlWebServiceGetContainerHist = "http://192.168.167.51:98/index/GetContainerHist?sContainerNumber=";
   // String sUrlWebServiceGetLog = "http://192.168.167.51:98/index/GetLog?sScannerId=";
    //String sUrlWebServiceGetCheckPointUsers = "http://192.168.167.51:98/index/GetCheckPointUsers?sEmployeeId=";
    String sUrlWebServiceGetCheckpoints = "http://192.168.167.51:98/index/GetCheckpoints";
    String sUrlWebServiceSetDataUpdateContainer = "http://192.168.167.51:98/index/SetDataInventoryContainer?sQuery=";
    String sUrlWebServiceGetContainerByUpdate = "http://192.168.167.51:98/index/GetInventoryContainerByUpdated?sScannerId=";
    String sUrlWebServiceSetUpdateScannerSync = "http://192.168.167.51:98/index/SetLastSync?sScannerNumber=";
    String sUrlWebServiceGetLogArea = "http://192.168.167.51:98/index/GetLogArea?sScannerId=";
    String sUrlWebServiceInsertNewContainer = "http://192.168.167.51:98/index/SetDataNewContainer?sQuery=";
    String sUrlWebServiceReload = "http://192.168.167.51:98/index/GetScannerReload?sScannerNumber=";
    String sUrlWebServiceGetSync = "http://192.168.167.51:98/index/GetScannerGetSync?sScannerNumber=";
    String sUrlWebServiceGetBinByUpdated = "http://192.168.167.51:98/index/GetBinByAreaUpdated?sScannerNumber=";
    String sUrlWebServiceGetContainerDeleted = "http://192.168.167.51:98/index/GetContainerResultDeletedAll?sScannerNumber=";
    String sUrlWebServiceGetBinDeleted = "http://192.168.167.51:98/index/GetBinByAreaDeleted?sScannerNumber=";
    String sUrlWebServiceGetComponentsByUpdated = "http://192.168.167.51:98/index/GetComponentByUpdatedResult?sScannerNumber=";
    String sUrlWebServiceGetComponentsByDeleted = "http://192.168.167.51:98/index/GetComponentByDeletedResult?sScannerNumber=";
    String sUrlWebServiceGetComponents = "http://192.168.167.51:98/index/GetComponentResult?sScannerNumber=";
    String sUrlWebServiceDeleteContainer = "http://192.168.167.51:98/index/DeleteContainer?sContainerNumber=";
    //String sUrlWebServiceCreateNewContainer = "http://192.168.167.51:98/index/InformixTest?sQueryMaster=";//&sQueryDetail=

    public String sUrlWebServiceCreateNewContainer(String sMasterQuery, String sDetailQuery){
        String sURL = String.format("http://192.168.167.51:98/index/CreateNewContainerPantr?sQueryMaster=%1$s&sQueryDetail=%2$s", sMasterQuery, sDetailQuery);
        return sURL;
    }

    String sUrlWebServiceDeleteComponentContainer = "http://192.168.167.51:98/index/DeleteComponentContainer?sContainerNumber=";//&sScannerNumber= &sEmployeeId= &sAreaId= ";

    public String sUrlWebServiceGetCheckPointUsers(String sEmployeeId){
        String sURL = String.format("http://192.168.167.51:98/index/GetCheckPointUsers?sEmployeeId=%1$s", sEmployeeId);
        return sURL;
    }

    public String sUrlAddGroupedTintoContainers(String sContainerNumber, String sParentContainerNumber, String sEmployeeId, String sScannerNumber){
        String sURL = String.format("http://192.168.167.51:98/index/InsertNewGroupedTintoContainer?sContainerNumber=%1$s&sParentContainerNumber=%2$s&sEmployeeId=%3$s&sScannerNumber=%4$s", sContainerNumber, sParentContainerNumber, sEmployeeId, sScannerNumber);
        return sURL;
    }

    public String sUrlWebServiceGetLog(String sScannerId){
        String sURL = String.format("http://192.168.167.51:98/index/GetLog?sScannerId=%1$s", sScannerId);
        return sURL;
    }

    public String sUrlWebServiceSetData(String sQuery){
        String sURL = String.format("http://192.168.167.51:98/index/SetDataInventory?sQuery=%1$s", sQuery);
        return sURL;
    }

    public String sUrlWebServiceGetAllContainers(){
        String sURL = String.format("http://192.168.167.51:98/index/GetInventoryAllContainerResult");
        return sURL;
    }

    public String sUrlWebServiceUpdateAppVersion(String sScannerNumber, String  sAppName, String sScannerVersion){
        String sURL = String.format("http://192.168.167.51:98/index/UpdateVersionApp?sScannerNumber=%1$s&sAppName=%2$s&sScannerVersion=%3$s", sScannerNumber, sAppName, sScannerVersion);
        return sURL;
    }

    public String sUrlWebServiceCheckCurrentAppVersion(String sScannerNumber, String sAppName){
        String sURL = String.format("http://192.168.167.51:98/index/VersionIsUpdated2?sScannerNumber=%1$s&sAppName=%2$s", sScannerNumber, sAppName);
        return sURL;
    }

    public String sUrlTempGroupedContainers(String sUserId){
        String sURL = String.format("http://192.168.167.51:98/index/GetTempGroupedContainersByUser?sUserId=%1$s", sUserId);
        return sURL;
    }

    public String sUrlTempGroupedContainersByParent(String sParentNumber){
        String sURL = String.format("http://192.168.167.51:98/index/GetTempGroupedContainersByParent?sParentNumber=%1$s", sParentNumber);
        return sURL;
    }

    public String sUrlTempAddGroupedContainers(String sContainerNumber, String sEmployee, String sScannerId){
        String sURL = String.format("http://192.168.167.51:98/index/InsertNewTempGroupedContainer?sContainerNumber=%1$s&sEmployeeId=%2$s&sScannerNumber=%3$s", sContainerNumber, sEmployee, sScannerId);
        return sURL;
    }

    public String sUrlAddGroupedContainers(String sContainerNumber, String sParentContainerNumber, String sEmployeeId, String sScannerNumber){
        String sURL = String.format("http://192.168.167.51:98/index/InsertNewGroupedContainer?sContainerNumber=%1$s&sParentContainerNumber=%2$s&sEmployeeId=%3$s&sScannerNumber=%4$s", sContainerNumber, sParentContainerNumber, sEmployeeId, sScannerNumber);
        return sURL;
    }

    public String sUrlDeleteTempGroupedContainers(String sContainerNumber, String sEmpployeeId, String sScannerId){
        String sURL = String.format("http://192.168.167.51:98/index/DeleteTempGroupedContainer?sContainerNumber=%1$s&sEmployeeId=%2$s&sScannerNumber=%3$s", sContainerNumber, sEmpployeeId, sScannerId);
        return sURL;
    }

    public String sUrlGenerateParentContainer(String sEmployeeId, String sScannerNumber){
        String sURL = String.format("http://192.168.167.51:98/index/GenerateParentContainer?sEmployeeId=%1$s&sScannerNumber=%2$s",  sEmployeeId, sScannerNumber);
        return sURL;
    }

    public String sUrlGetRoutes(String sEmployeeId){
        String sURL = String.format("http://192.168.167.51:98/index/GetInventoryRoutesByArea?sEmployee=%1$s", sEmployeeId);
        return sURL;
    }

    public String sUrlSendErrors(String sScannerNumber, String sQuery){
        String sURL = String.format("http://192.168.167.51:98/index/SetErrorLog?sScannerNumber=%1$s&sQuery=%2$s", sScannerNumber, sQuery);
        return sURL;
    }

    public String sUrlReturnContainer(String sContainerNumber, String sScannerNumber, String sEmployeeId, String sCheckPointId){
        String sURL = String.format("http://192.168.167.51:98/index/ReturnContainer?sContainerNumber=%1$s&sScannerNumber=%2$s&sEmployeeId=%3$s&sCheckpointId=%4$s", sContainerNumber, sScannerNumber, sEmployeeId, sCheckPointId);
        return sURL;
    }

    public String sUrlGetTintoParentContainer( String sContainerNumber, String sPart){
        String sURL = String.format("http://192.168.167.51:98/index/GetTintoParentContainer?sContainerNumber=%1$s&sPart=%2$s", sContainerNumber, sPart);
        return sURL;
    }

    public String sUrlGetTintoChildContainer(String sContainerNumber, String sPart){
        String sURL = String.format("http://192.168.167.51:98/index/GetTintoChildContainer?sContainerNumber=%1$s&sPart=%2$s", sContainerNumber, sPart);
        return sURL;
    }
    public String sUrlSetEmployeeIdUserDate(String sEmployeeId, String sUserDate){
        String sURL = String.format("http://192.168.167.51:98/index/SetEmployeeIdUserDate?sEmployeeId=%1$s&sUserDate=%2$s", sEmployeeId, sUserDate);
        return sURL;
    }
    public String sUrlGetCurrentDate(String sEmployeeId){
            String sURL = String.format("http://192.168.167.51:98/index/GetCurrentDate?sEmployeeId=%1$s", sEmployeeId);
        return sURL;
    }
    public String sUrlDeleteEmployeeIdUserDate(String sEmployeeId){
        String sURL = String.format("http://192.168.167.51:98/index/DeleteEmployeeIdUserDate?sEmployeeId=%1$s", sEmployeeId);
        return sURL;
    }
    public String sUrlGetPartialWeight(String sContainerNumber){
        String sURL = String.format("http://192.168.167.51:98/index/GetPartialWeight?sContainerNumber=%1$s", sContainerNumber);
        return sURL;
    }
    public String sUrlSetPartialWeightContainer(String sContainerNumber, String sParentContainerNumber, String sPartialWeight, String sScannerNumber, String sEmployeeId){
        String sURL = String.format("http://192.168.167.51:98/index/SetPartialWeightContainer?sContainerNumber=%1$s&sParentContainerNumber=%2$s&sPartialWeight=%3$s&sScannerNumber=%4$s&sEmployeeId=%5$s", sContainerNumber, sParentContainerNumber, sPartialWeight, sScannerNumber, sEmployeeId);
        return sURL;
    }
    public String sUrlDeleteTempGroupedContainersTinto(String sContainerNumber, String sEmpployeeId, String sScannerId){
        String sURL = String.format("http://192.168.167.51:98/index/DeleteTempGroupedContainerTinto?sContainerNumber=%1$s&sEmployeeId=%2$s&sScannerNumber=%3$s", sContainerNumber, sEmpployeeId, sScannerId);
        return sURL;
    }
    public String sUrlSendContainerToWip(String sContainerNumber, String sPart, String sScannerNumber, String sEmployeeId){
        String sURL = String.format("http://192.168.167.51:98/index/SendContainerToWip?sContainerNumber=%1$s&sPart=%2$s&sScannerNumber=%3$s&sEmployeeId=%4$s", sContainerNumber, sPart, sScannerNumber, sEmployeeId);
        return sURL;
    }
    public String sUrlGetPackingListDetails(String sPackingListId){
        String sURL = String.format("http://192.168.167.51:98/index/GetPlChecklist?sPackinglistId=%1$s", sPackingListId);
        return sURL;
    }
    public String sUrlGetMakerInventory(){
        String sURL = String.format("http://192.168.167.51:98/index/GetMaketInv",null);
        return sURL;
    }

    //CHECKPOINTSID
    String sInventoryCheckCheckpointId = "10";
    String sInventoryAddCheckpointId = "11";
    String sInventoryRelocationCheckpointId = "25";

    //CHECKPOINGROUPS
    String sCheckPointGroupAreaRecive = "ARCV";
    String sCheckPointGroupAreaSend = "ASND";
    String sCheckPointGroupCostumerSend = "CSND";
    String sCheckpointGroupPRecive = "PRCV";
    String sCheckpointGroupRelocation = "LOCAT";

    //AISLES
    String sAisleRcv = "RECIBO";
    String sAisleSnd = "ENVIO";

    //REGULAR EXPRESIONS PARA FORMATOS ESPECIFICOS
    String regexpBinFormat1 = "[A-Z0-9]+[-]+[A-Z0-9]+[A-Z0-9]+[-]+[A-Z0-9]+[A-Z0-9]+[-]+[A-Z0-9]+[A-Z0-9]";
    String regexpBinFormat2 = "[A-Z0-9]+[-]+[A-Z0-9]+[A-Z0-9]+[-]+[A-Z0-9]+[A-Z0-9]+[-]+[A-Z0-9]+[A-Z0-9]+[-]+[A-Z0-9]+[A-Z0-9]";

    //PRODUCTSTATES
    String sChemicals = "CHMIS";
    String sFabricsT = "FABT";
    String sRawMaterial = "MP";
    String sThread = "THR";
    String sFuel = "FUEL";
    String sOfficeSupplies = "OFFIC";
    String sGroceries = "PANTR";
    String sSupplies = "SUPPL";
    String sFabricsC = "FABC";

    //RAWIDS
    String sRawOk = "ok";
    String sRawError = "error";

    //IDENTIFIERS
    String sIdentifierCheck = "IC";
    String sIdentifierNew = "IN";
    String sIdentifierAreaIn = "AI";
    String sIdentifierAreaOut = "AO";
    String sIdentifierVendorIn = "VI";
    String sIdentifierCustomerOut = "CO";
    String sIdentifierRelocation = "RL";

    //AREAS
    String sClientAreaId = "4";

    int iTimeOut = 20000;
}
