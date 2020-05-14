package com.inventario.hilcasa.hilcasainventario;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class QueryDB {
	// DECLARACION DE VARIABLES PARA USO EN SQLITE
	private static final String sTableContainers = "Containers";
	private static final String sTableProcess = "Process";
	private static final String sTableUser = "User";
	private static final String sTableArea = "Area";
	private static final String sTableBin = "Bin";
	private static final String sTableLog = "LogInventory";
	private static final String sTableLogOffline = "LogOffline";
	private static final String sTableCheckPointUsers = "CheckpointUsers";
	private static final String sTableCheckPoints = "Checkpoints";
	private static final String sTableLogMovimiento = "LogMovimiento";
	private static final String sTableLogOfflineContainer = "LogInicialOffline";
	private static final String sTableLastLog = "LastLog";
	private static final String sTableComponents = "Components";
	private static final String sTableTempContainers = "TempContainer";
	private static final String sTableTempList = "TempList";
	private static final String sTest = "Test";
	private static final String sTableRoutes = "Routes";
	private static final String sTablePackingListInfo = "PackingListInfo";
	private static final String sTableTempListSales = "TempListSales";
	private static final String sTableMarketInv = "MarketInventory";

	private Context cContext;
	private SQLiteDatabase dbDatabBase;
	private ManagerDB dbHelper;
	private boolean bStatus = false;

	public QueryDB(Context context) {
		this.cContext = context;
	}
	public QueryDB open() throws SQLException {
		dbHelper = new ManagerDB(cContext);
		dbDatabBase = dbHelper.getWritableDatabase();
		return this;
	}
	public void  BeginTransaction()  {
		dbDatabBase.beginTransaction();
	}
	public void  CommitTransaction()  {
		dbDatabBase.setTransactionSuccessful();
		dbDatabBase.endTransaction();
	}
	public void  EndTransaction()  {

		dbDatabBase.endTransaction();
	}


	public void close() {
		dbHelper.close();
	}

	//CONSULTAS A LA BASE LOCAL

	//INSERTAR USUARIO QUE SE LOGEE
	protected boolean InserTest() {
		ContentValues values = new ContentValues();
		values.put("test", "1");
		return (dbDatabBase.insert(sTest, null, values) > 0);
	}

	//INSERTAR USUARIO QUE SE LOGEE
	protected boolean InsertCurrentUser(String sEmployeeId, String sFullName) {
		ContentValues values = new ContentValues();
		values.put("EmployeeId", sEmployeeId);
		values.put("name", sFullName);
		return (dbDatabBase.insert(sTableUser, null, values) > 0);
	}

	//INSERTAR LAS AREAS ASIGNADAS AL USUARIO
	protected boolean InsertAreaByUser(String sAreaId, String sName, String sProductStateFrom) {
		ContentValues values = new ContentValues();
		values.put("areaid", sAreaId);
		values.put("name", sName);
		values.put("productstatefrom", sProductStateFrom);
		return (dbDatabBase.insert(sTableArea, null, values) > 0);
	}

	//ELIMINA LOS DATOS DE LA TABLA USUARIO
	protected boolean DeleteUser() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableUser, null, null) > 0;
		}
		return (transact);
	}

	//ELIMINA LOS DATOS DE LA TABLA AREA
	protected boolean DeleteArea() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableArea, null, null) > 0;
		}
		return (transact);
	}

	//OBTENER EL USUARIO LOGEADO PARA MOSTRAR LA INFORMACION
	public Cursor GetEmployeeInfo() {
		return dbDatabBase.rawQuery("select EmployeeId, name from User", null);
	}

	//OBTENER LAS AREAS ASIGNADAS AL USUARIO ACTUAL
	public Cursor GetAreaByUser() {
		return dbDatabBase.rawQuery("select areaid as _id, name as description, productstatefrom from Area", null);
	}


	//INSERTAR LOS BIN DE CADA AREA
	protected boolean InsertBin(String sBinId, String sAreaId, String sDesc, String sAisle, String sPackQuantity) {


		String sqlQuery = "insert into " + sTableBin + " (BinId, AreaId, Description, Aisle, PackQuantity) values (?, ?, ?, ?, ?)";
		SQLiteStatement sqlStatement = dbDatabBase.compileStatement(sqlQuery);

		sqlStatement.bindString(1, sBinId);
		sqlStatement.bindString(2, sAreaId);
		sqlStatement.bindString(3, sDesc);
		sqlStatement.bindString(4, sAisle);
		sqlStatement.bindString(5, sPackQuantity);
		Long lInserted = sqlStatement.executeInsert();
		sqlStatement.clearBindings();
		return (lInserted > 0);

		/*ContentValues values = new ContentValues();
		values.put("BinId", sBinId);
		values.put("AreaId", sAreaId);
		values.put("Description", sDesc);
		values.put("Aisle", sAisle);
		values.put("PackQuantity", sPackQuantity);
		return (dbDatabBase.insert(sTableBin, null, values) > 0);*/
	}

	//INSERTAR LOS BIN DE CADA AREA
	protected boolean InsertBulkBin(String sQuery) {
		String sBinId = "";
		String sAreaId = "";
		String sDesc = "";
		String sAisle ="";
		String sPackQuantity = "";
		Long lInserted = Long.valueOf(0);
		if (sQuery.contains(String.valueOf((char) 164))) {
			this.DeleteBin();
			String[] sData = sQuery.split(String.valueOf((char) 164));
			dbDatabBase.beginTransaction();
			for (int n = 0; n < sData.length; n++) {
				String strData = sData[n];
				String sDataRow[] = strData.split(String.valueOf((char) 165));
				 sBinId = sDataRow[0];
				 sAreaId = sDataRow[1];
				 sDesc = sDataRow[2];
				 sAisle = sDataRow[3];
				 sPackQuantity = sDataRow[4];

				String sqlQuery = "insert into " + sTableBin + " (BinId, AreaId, Description, Aisle, PackQuantity) values (?, ?, ?, ?, ?)";
				SQLiteStatement sqlStatement = dbDatabBase.compileStatement(sqlQuery);

				sqlStatement.bindString(1, sBinId);
				sqlStatement.bindString(2, sAreaId);
				sqlStatement.bindString(3, sDesc);
				sqlStatement.bindString(4, sAisle);
				sqlStatement.bindString(5, sPackQuantity);
				lInserted = sqlStatement.executeInsert();
				sqlStatement.clearBindings();
			}
			dbDatabBase.setTransactionSuccessful();
			dbDatabBase.endTransaction();
		}
		return (lInserted > 0);
	}

	//INSERTAR LOS BIN DE CADA AREA
	protected boolean UpdateBin(String sBinId, String sAreaId, String sDesc, String sAisle, String sPackQuantity) {
		ContentValues values = new ContentValues();
		values.put("AreaId", sAreaId);
		values.put("Description", sDesc);
		values.put("Aisle", sAisle);
		values.put("PackQuantity", sPackQuantity);

		return (dbDatabBase.update(sTableBin, values, "BinId='"
				+ sBinId + "'", null) > 0);
	}

	//OBTENER LOS BINES QUE SE ENCUENTRAN EN UN AREA ESPECIFICA
	public Cursor GetBin(String sAreaId, String sBindId) {
		String sParam[] = new String[] { sAreaId, sBindId };
		return dbDatabBase.rawQuery("select BinId, Description from Bin where AreaId = ? and Description = ?", sParam);
	}


	//OBTENER UN AREA ESPECIFICA
	public Cursor GetAreaEsp(String sAreaId) {
		String sParam[] = new String[] { sAreaId };
		return dbDatabBase.rawQuery("select areaid as _id, name as description, productstatefrom from Area order by case when areaid = ? then 1 else 2 end, areaid", sParam);
	}

	//ELIMINA LOS DATOS DE LA TABLA BIN
	protected boolean DeleteBin() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableBin, null, null) > 0;
		}
		return (transact);
	}

	//ELIMINA LOS DATOS DE LA TABLA CONTAINERS
	protected boolean DeleteContainer() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableContainers, null, null) > 0;
		}
		return (transact);
	}

	//INSERTAR CONTAINERS
	protected boolean InsertContainers(String sContainerId, String sCurrentProcessStatus, String sCurrentProcess, String sContainerNumber, String sDatePrinted, String sContainerDescription, String sContainerWeight, String previousdateprinted, String StartWeight, String sStopReason, String sPart, String sBinId, String sAreaId, String sContainerType, String sComponentId, String sParentContainerNumber, String sStatusInv) {
		String sNewSqlDate = "";

		String sqlQuery = "insert into " + sTableContainers + " (containerid, containerNumber, CurrentProcessStatus, dateprinted," +
				" ContainerDescription, ContainerWeight, DateTime, Part, binid, areaid, " +
				"containertype, componentid,  statusinv) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


		SQLiteStatement sqlStatement = dbDatabBase.compileStatement(sqlQuery);
		sqlStatement.bindString(1, sContainerId);
		sqlStatement.bindString(2, sContainerNumber);
		sqlStatement.bindString(3, sCurrentProcessStatus);
		sqlStatement.bindString(4, sDatePrinted);
		sqlStatement.bindString(5, sContainerDescription);
		sqlStatement.bindString(6, sContainerWeight);
		sqlStatement.bindString(7, sNewSqlDate);
		sqlStatement.bindString(8, sPart);
		sqlStatement.bindString(9, sBinId);
		sqlStatement.bindString(10, sAreaId);
		sqlStatement.bindString(11, sContainerType);
		sqlStatement.bindString(12, sComponentId);
		sqlStatement.bindString(13, sStatusInv);
		Long lResult = sqlStatement.executeInsert();
		sqlStatement.clearBindings();



		return (lResult > 0);
	}

	//OBTENER LOS CONTAINER DEL AREA Y BIN ACTUAL
	public Cursor GetContainerByAreaAndBin( String sContainerNumber) {
		String sParam[] = new String[] { sContainerNumber };
		return dbDatabBase.rawQuery("select containerid, ContainerWeight, containerNumber, ContainerDescription, statusinv from Containers where containerNumber = ? and statusinv = 'I'", sParam);
	}

	public Cursor GetContainerByAreaAndBinByArea( String sContainerNumber, String sAreaId) {
		String sParam[] = new String[] { sContainerNumber, sAreaId };
		return dbDatabBase.rawQuery("select containerid, ContainerWeight, containerNumber, ContainerDescription, statusinv from Containers where containerNumber = ? and statusinv = 'I' and areaid = ?", sParam);
	}

	//OBTENER LOS CONTAINER DEL AREA Y BIN ACTUAL
	public Cursor GetContainerArea( String sContainerNumber) {
		String sParam[] = new String[] { sContainerNumber };
		return dbDatabBase.rawQuery("select areaid from Containers where containerNumber = ?", sParam);
	}

	//OBTENER LOS CONTAINER DEL AREA Y BIN ACTUAL
	public Cursor GetContainerByAreaAndBinInventory( String sContainerNumber) {
		String sParam[] = new String[] { sContainerNumber };

		return dbDatabBase.rawQuery("select containerid, ContainerWeight, containerNumber, ContainerDescription, statusinv from Containers where containerNumber = ?", sParam);
	}

	//OBTENER LOS CONTAINER DEL AREA Y BIN ACTUAL
	public Cursor GetContainerByAreaAndBinRecive( String sContainerNumber) {
		String sParam[] = new String[] { sContainerNumber };
		return dbDatabBase.rawQuery("select containerid, ContainerWeight, containerNumber, ContainerDescription from Containers where containerNumber = ? and statusinv = 'T'", sParam);
	}

	//ELIMINA LOS DATOS DE LA TABLA CONTAINERS
	protected boolean DeleteLog() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableLog, null, null) > 0;
		}
		return (transact);
	}

	//INSERTAR LOG
	protected boolean InsertLog(String sAreaId, String sBinId, String sContainerId, String sDatePrinted, String sContainerNumber, String sContainerWeight, String sContainerDesc, String sShowLog, String sCheckPoint) {

		String sqlQuery = "insert into " + sTableLog + " (AreaId, BinId, ContainerId, dateprinted," +
				" ContainerNumber, ContainerWeight, ContainerDescription, ShowLog, CheckPointId) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		SQLiteStatement sqlStatement = dbDatabBase.compileStatement(sqlQuery);
		sqlStatement.bindString(1, sAreaId);
		sqlStatement.bindString(2, sBinId);
		sqlStatement.bindString(3, sContainerId);
		sqlStatement.bindString(4, sDatePrinted);
		sqlStatement.bindString(5, sContainerNumber);
		sqlStatement.bindString(6, sContainerWeight);
		sqlStatement.bindString(7, sContainerDesc);
		sqlStatement.bindString(8, sShowLog);
		sqlStatement.bindString(9, sCheckPoint);

		Long lResult = sqlStatement.executeInsert();
		sqlStatement.clearBindings();

		return (lResult > 0);

		/*ContentValues values = new ContentValues();
		values.put("AreaId", sAreaId);
		values.put("BinId", sBinId);
		values.put("ContainerId", sContainerId);
		values.put("dateprinted", sDatePrinted);
		values.put("ContainerNumber", sContainerNumber);
		values.put("ContainerWeight", sContainerWeight);
		values.put("ContainerDescription", sContainerDesc);
		values.put("ShowLog", sShowLog);
		values.put("CheckPointId", sCheckPoint);
		return (dbDatabBase.insert(sTableLog, null, values) > 0);*/
	}

	//OBTENER LOS REGISTROS HECHOS EN LA SESION
	public Cursor GetLogList(String sAreaId, String sCheckPointId) {
		String sParam[] = new String[] {sAreaId, sCheckPointId};
		return dbDatabBase.rawQuery("select a.ContainerNumber, c.name, d.Description, a.ContainerDescription, a.ContainerWeight from LogInventory as a inner join Area as c on a.AreaId = c.areaid inner join Bin as d on a.BinId = d.BinId where a.Areaid = ? and ShowLog = '1' and CheckPointId = ? order by LogId desc", sParam);
	}

	//OBTENER LOS REGISTROS HECHOS EN LA SESION
	public Cursor GetLogListHist(String sCheckpoint) {
		String sParam[] = new String[] { sCheckpoint};
		return dbDatabBase.rawQuery("select a.ContainerNumber, c.name, d.Description, a.ContainerDescription, a.ContainerWeight from LogInventory as a inner join Area as c on a.AreaId = c.areaid inner join Bin as d on a.BinId = d.BinId where a.CheckPointId = ? order by LogId desc", sParam);
	}

	//LIMPIA LOS REGISTROS QUE SE MUESTRAN EN TOMA DE INVENTARIO
	protected boolean UpdateLog() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			ContentValues values = new ContentValues();
			values.put("ShowLog", "2");
			transact = (dbDatabBase.update(sTableLog, values, "ShowLog='1'", null) > 0);
		}
		return (transact);
	}

	//INSERTAR LOGOFFLINE
	protected boolean InsertLogOffline(String sQuery) {
		ContentValues values = new ContentValues();
		values.put("Query", sQuery);
		return (dbDatabBase.insert(sTableLogOffline, null, values) > 0);
	}

	//ELIMINA EL REGISTRO QUE SE ENVIO
	protected boolean DeleteLogOffline(String sId) {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableLogOffline, "Id = '"+ sId +"'", null) > 0;
		}
		return (transact);
	}

	//OBTIENE LOS REGISTROS QUE SE ENVIARON A LA TABLA DE LOGOFFLINE
	public Cursor GetOfflineLog() {
		return dbDatabBase.rawQuery("select Id, Query from LogOffline", null);
	}

	//INSERTAR LOS CHECKPOINTS ASIGNADOS AL USUARIO ACTUAL
	protected boolean InsertCheckPointUsers(String sCheckPointId, String EmployeeId) {
		ContentValues values = new ContentValues();
		values.put("CheckpointId", sCheckPointId);
		values.put("EmployeeId", EmployeeId);
		return (dbDatabBase.insert(sTableCheckPointUsers, null, values) > 0);
	}

	//OBTIENE LOS CHECKPOINTS ASIGNADOS AL USUARIO
	public Cursor GetCheckpointUsers() {
		return dbDatabBase.rawQuery("select CheckpointId from CheckpointUsers", null);
	}

	//ELIMINA LOS DATOS DE LA TABLA CHECKPOINTUSERS
	protected boolean DeleteCheckpointUsers() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableCheckPointUsers, null, null) > 0;
		}
		return (transact);
	}

	//INSERTAR LOS CHECKPOINTS
	protected boolean InsertCheckpoints(String sCheckPointId, String sDesc, String sProductState, String sCheckpointGroup) {
		ContentValues values = new ContentValues();
		values.put("CheckPointId", sCheckPointId);
		values.put("Description", sDesc);
		values.put("productstateform", sProductState);
		values.put("CheckPointGroup", sCheckpointGroup);
		return (dbDatabBase.insert(sTableCheckPoints, null, values) > 0);
	}

	//ELIMINA LOS DATOS DE LA TABLA CHECKPOINTS
	protected boolean DeleteCheckpoint() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableCheckPoints, null, null) > 0;
		}
		return (transact);
	}

	//OBTEN EL CHECKPOINT A MARCAR
	public Cursor GetCheckpointByProduct(String sProductState,String sCheckPointGroup) {
		String sParam[] = new String[] {sProductState,sCheckPointGroup};
		return dbDatabBase.rawQuery("select CheckPointId from Checkpoints where productstateform = ? and CheckPointGroup = ?", sParam);
	}

	//OBTENER EL BIN DE ENTRADA O SALIDA DE UN AREA
	public Cursor GetBinByAreaAndAisle(String sAreaId, String sAisle) {
		String sParam[] = new String[] { sAreaId, sAisle };
		return dbDatabBase.rawQuery("select BinId, Description from Bin where AreaId = ? and Aisle = ?", sParam);
	}

	//OBTENER EL BIN DE ENTRADA O SALIDA DE UN AREA
	public Cursor GetBinByArea(String sAreaId) {
		String sParam[] = new String[] { sAreaId };
		return dbDatabBase.rawQuery("select BinId as _id, Description as description from Bin where AreaId = ? and description not like '%-%'", sParam);
	}

	//OBTENER EL BIN DE ENTRADA O SALIDA DE UN AREA
	public Cursor GetBinByAreaOrder(String sAreaId, String sOldBinId) {
		String sParam[] = new String[] { sAreaId, sOldBinId };
		return dbDatabBase.rawQuery("select BinId as _id, Description as description from Bin where AreaId = ? and description not like '%-%' order by case when BinId = ? then 1 else 2 end, BinId", sParam);
	}

	//ACTUALIZAR CONTAINERS
	protected boolean UpdateContainers(String sContainerId, String sCurrentProcessStatus, String sCurrentProcess, String sContainerNumber, String sDatePrinted, String sContainerDescription, String sContainerWeight, String previousdateprinted, String StartWeight, String sStopReason, String sPart, String sBinId, String sAreaId, String sContainerType, String sComponentId, String sParentContainerNumber, String sStatusInv) {
		String sNewSqlDate = "";


		String sqlQuery = "update " + sTableContainers + " set containerid = ?, containerNumber = ?, CurrentProcessStatus = ?, dateprinted = ?," +
				" ContainerDescription = ?, ContainerWeight = ?, DateTime = ?, Part = ?, binid = ?, areaid = ?, " +
				"containertype = ?, componentid = ?,  statusinv = ? where containerNumber = ?";

		SQLiteStatement sqlStatement = dbDatabBase.compileStatement(sqlQuery);
		sqlStatement.bindString(1, sContainerId);
		sqlStatement.bindString(2, sContainerNumber);
		sqlStatement.bindString(3, sCurrentProcessStatus);
		sqlStatement.bindString(4, sDatePrinted);
		sqlStatement.bindString(5, sContainerDescription);
		sqlStatement.bindString(6, sContainerWeight);
		sqlStatement.bindString(7, sNewSqlDate);
		sqlStatement.bindString(8, sPart);
		sqlStatement.bindString(9, sBinId);
		sqlStatement.bindString(10, sAreaId);
		sqlStatement.bindString(11, sContainerType);
		sqlStatement.bindString(12, sComponentId);
		sqlStatement.bindString(13, sStatusInv);
		sqlStatement.bindString(14, sContainerNumber);
		int iResult = sqlStatement.executeUpdateDelete();
		sqlStatement.clearBindings();

		return (iResult > 0);
		/*try{

			SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
			Date dDate = sdfFormat.parse(sDatePrinted);
			sdfFormat = new SimpleDateFormat("yyy-MM-dd HH:mm:ss",Locale.US);
			sNewSqlDate = sdfFormat.format(dDate);

		} catch (Exception e) {
			Log.e("MainActivity", e.getMessage(), e);
		}
*/
		/*ContentValues values = new ContentValues();
		values.put("containerNumber", sContainerNumber);
		values.put("CurrentProcessStatus", sCurrentProcessStatus);
		values.put("CurrentProcess", sCurrentProcess);
		values.put("dateprinted", sDatePrinted);
		values.put("ContainerDescription", sContainerDescription);
		values.put("ContainerWeight", sContainerWeight);
		values.put("previousdateprinted", previousdateprinted);
		values.put("StartWeight", StartWeight);
		values.put("DateTime",sNewSqlDate);
		values.put("StopReasonId",sStopReason);
		values.put("ProcessDuration",StartWeight);
		//values.put("Part",sPart);
		values.put("binid",sBindId);
		values.put("areaid",sAreaId);
		values.put("containertype",sContainerType);
		values.put("componentid",sComponentId);
		values.put("statusinv",sStatusInv);
		return (dbDatabBase.update(sTableContainers, values, "containerNumber='"
				+ sContainerNumber + "' and Part = '"+ sPart +"'", null) > 0);*/
	}

	//INSERTAR LOS LOS MOVIMIENTOS QUE SE REALIZAN EN ENVIO Y RECIBO DE CONTAINERS
	protected boolean InsertMovimientoLog(String sContainerNumber, String sFechaMovimiento, String sCheckpointGroup, String sStateForm, String sAreaId, String sBinId, String sShowLog, String sContainerDesc) {
		ContentValues values = new ContentValues();
		values.put("ContainerNumber", sContainerNumber);
		values.put("FechaMovimiento", sFechaMovimiento);
		values.put("CheckpointGroup", sCheckpointGroup);
		values.put("StateForm", sStateForm);
		values.put("AreaId", sAreaId);
		values.put("BinId", sBinId);
		values.put("ShowLog", sShowLog);
		values.put("Description",sContainerDesc);
		return (dbDatabBase.insert(sTableLogMovimiento, null, values) > 0);
		/*String sqlQuery = "insert into " + sTableLogMovimiento + " (ContainerNumber, FechaMovimiento, CheckpointGroup, StateForm," +
				" AreaId, BinId, ShowLog, Description) values (?, ?, ?, ?, ?, ?, ?, ?)";

		SQLiteStatement sqlStatement = dbDatabBase.compileStatement(sqlQuery);
		sqlStatement.bindString(1, sContainerNumber);
		sqlStatement.bindString(2, sFechaMovimiento);
		sqlStatement.bindString(3, sCheckpointGroup);
		sqlStatement.bindString(4, sStateForm);
		sqlStatement.bindString(5, sAreaId);
		sqlStatement.bindString(6, sBinId);
		sqlStatement.bindString(7, sShowLog);
		sqlStatement.bindString(8, sContainerDesc);

		Long lResult = sqlStatement.executeInsert();
		sqlStatement.clearBindings();

		return (lResult > 0);*/
	}

	//OBTENER LOS REGISTROS TEMPORALES DEL MOVIMIENTO
	public Cursor GetLogMovimiento(String sCheckPointGroup, String sAreaId) {
		String sParam[] = new String[] { sCheckPointGroup, sAreaId };
		return dbDatabBase.rawQuery("select a.ContainerNumber, a.FechaMovimiento, b.name, a.StateForm, c.ContainerDescription, d.Description from LogMovimiento as a join Area as b on a.AreaId = b.areaid join Containers as c on a.ContainerNumber = c.ContainerNumber join Bin as d on a.BinId = d.BinId where a.CheckpointGroup = ? and ShowLog = '1' and c.Part = 'CUERP' and a.AreaId = ?" , sParam);
	}

	//OBTENER LOS REGISTROS HISTORICOS DEL MOVIMIENTO
	public Cursor GetLogMovimientoHist(String sCheckPointGroup) {
		String sParam[] = new String[] { sCheckPointGroup };
		return dbDatabBase.rawQuery("select a.ContainerNumber, a.FechaMovimiento, b.name, a.StateForm, c.ContainerDescription, d.Description from LogMovimiento as a join Area as b on a.AreaId = b.areaid join Containers as c on a.ContainerNumber = c.ContainerNumber join Bin as d on a.BinId = d.BinId where a.CheckpointGroup = ? and c.Part = 'CUERP'" , sParam);
	}

	//ELIMINA LOS DATOS DE LOS MOVIMIENTOS ENTRE AREAS REALIZADOS DURANTE LA SESION
	protected boolean DeleteLogMovimiento() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableLogMovimiento, null, null) > 0;
		}
		return (transact);
	}

	//ACTUALIZAR CONTAINERS
	protected boolean UpdateContainersSpecific( String sContainerNumber, String sDatePrinted,  String sBindId, String sAreaId) {
		String sNewSqlDate = "";
		try{

			SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
			Date dDate = sdfFormat.parse(sDatePrinted);
			sdfFormat = new SimpleDateFormat("yyy-MM-dd HH:mm:ss",Locale.US);
			sNewSqlDate = sdfFormat.format(dDate);

		} catch (Exception e) {
			Log.e("MainActivity", e.getMessage(), e);
		}

		ContentValues values = new ContentValues();
		values.put("CurrentProcessStatus", "I");
		values.put("CurrentProcess", "1");
		values.put("dateprinted", sDatePrinted);
		values.put("previousdateprinted", sDatePrinted);
		values.put("StopReasonId","1");
		values.put("binid",sBindId);
		values.put("areaid",sAreaId);
		return (dbDatabBase.update(sTableContainers, values, "containerNumber='"
				+ sContainerNumber + "'", null) > 0);
	}

	//OBTENER PRODUCTSTATEFORM
	public Cursor GetProductStateForm(String sAreaId) {
		String sParam[] = new String[] { sAreaId };
		return dbDatabBase.rawQuery("select productstatefrom from Area where areaid = ?" , sParam);
	}

	//INSERTAR LOGOFFLINE PARA NUEVOS CONTAINERS
	protected boolean InsertLogOfflineContainers(String sQuery) {
		ContentValues values = new ContentValues();
		values.put("Query", sQuery);
		return (dbDatabBase.insert(sTableLogOfflineContainer, null, values) > 0);
	}

	//ELIMINA EL REGISTRO QUE SE ENVIO DE LOGCONTAINERS
	protected boolean DeleteLogOfflineContainers() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableLogOfflineContainer, null, null) > 0;
		}
		return (transact);
	}

	//ELIMINA EL REGISTRO QUE SE ENVIO DE LOGCONTAINERS
	protected boolean DeleteLogOfflineContainersById(String sId) {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableLogOfflineContainer,  "Id = '"+ sId +"'", null) > 0;

		}
		return (transact);
	}

	//ELIMINA EL REGISTRO QUE SE ENVIO DE LOGCONTAINERS
	protected boolean DeleteLogOfflineComplete() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableLogOffline, null, null) > 0;
		}
		return (transact);
	}

	//OBTIENE LOS REGISTROS QUE SE ENVIARON A LA TABLA DE LOGOFFLINECONTAINERS
	public Cursor GetOfflineLogContainer() {
		return dbDatabBase.rawQuery("select Id, Query from LogInicialOffline", null);
	}

	//LIMPIA LOS REGISTROS QUE SE MUESTRAN EN TOMA DE INVENTARIO
	protected boolean UpdateLogMovimiento() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			ContentValues values = new ContentValues();
			values.put("ShowLog", "2");
			transact = (dbDatabBase.update(sTableLogMovimiento, values, "ShowLog = '1'", null) > 0);
		}
		return (transact);
	}

	//ELIMINA EL REGISTRO QUE SE ENVIO DE LOGCONTAINERS
	protected boolean DeleteLastLog() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableLastLog, null, null) > 0;
		}
		return (transact);
	}

	//INSERTAR LOS REGISTROS REALIZADOS EN CADA AREA A LA TABLA LASTLOG
	protected boolean InsertLastLog(String sInputText, String sIdentifier, String sDate) {
		ContentValues values = new ContentValues();
		values.put("InputText", sInputText);
		values.put("Identifier", sIdentifier);
		values.put("Date", sDate);
		return (dbDatabBase.insert(sTableLastLog, null, values) > 0);
	}

	//OBTENER LOS REGISTROS HISTORICOS DEL MOVIMIENTO
	public Cursor GetLastLog(String sIdentifier) {
		String sParam[] = new String[] { sIdentifier };
		return dbDatabBase.rawQuery("select InputText, Date from LastLog where Identifier = ? order by Date desc" , sParam);
	}

	//ELIMINA DATOS DE LA TABLA CONTAINER
	protected boolean DeleteContainersDeleted(String sContainerNumber, String sPart) {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableContainers, "containerNumber = '" + sContainerNumber +"' and Part = '" + sPart + "'", null) > 0;
		}
		return (transact);
	}

	//ELIMINA  DATOS DE LA TABLA BIN
	protected boolean DeleteBinDeleted(String sBinId) {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableBin, "BinId = '"+ sBinId +"'", null) > 0;
		}
		return (transact);
	}

	//INSERTAR LOS COMPONENTES
	protected boolean InsertComponents(String sComponentId, String sProductStateFrom, String sDescription, String sBarcode, String sReasonBarcode, String sVendorReference, String sPurchaseUom, String sQuantityMin, String sQuantityMax) {
		ContentValues values = new ContentValues();
		values.put("ComponentId", sComponentId);
		values.put("ProductStateFrom", sProductStateFrom);
		values.put("Description", sDescription);
		values.put("Barcode", sBarcode);
		values.put("ReasonBarcode", sReasonBarcode);
		values.put("VendorReference", sVendorReference);
		values.put("PurchaseUom", sPurchaseUom);
		values.put("QuantityMin", sQuantityMin);
		values.put("QuantityMax", sQuantityMax);
		return (dbDatabBase.insert(sTableComponents, null, values) > 0);
	}

	//ELIMINA LOS DATOS DE LA TABLA COMPONENTS
	protected boolean DeleteComponents(){
		boolean transact = false;
		if(dbDatabBase != null && dbDatabBase.isOpen()){
			transact = dbDatabBase.delete(sTableComponents, null, null) > 0;
		}
		return (transact);
	}

	//INSERTAR UN CONTAINER A TEMPCONTAINER
	protected boolean InsertTempContainer(String sContainerNumber, String sComponentId, String sComponentDesc, String sComponentQuantity,String sEmployeeId, String sTempAreaId, String sFlag, String sHist) {
		ContentValues values = new ContentValues();
		values.put("ContainerNumber", sContainerNumber);
		values.put("ComponentId", sComponentId);
		values.put("ComponentDesc", sComponentDesc);
		values.put("ComponentQuantity", sComponentQuantity);
		values.put("EmployeeId", sEmployeeId);
		values.put("TempAreaId", sTempAreaId);
		values.put("Flag", sFlag);
		values.put("Hist",sHist);
		return (dbDatabBase.insert(sTableTempContainers, null, values) > 0);
	}

	//OBTENER LOS REGISTROS DE CONTAINERS A CREAR
	public Cursor GetTempContainerByArea(String sAreaId, String sUserId) {
		String sParam[] = new String[] { sAreaId, sUserId };
		return dbDatabBase.rawQuery("select distinct ContainerNumber from TempContainer where TempAreaId = ? and EmployeeId = ? and Hist = '1'" , sParam);//distinct
	}

	//OBTENER LOS REGISTROS DE CONTAINERS A CREAR
	public Cursor GetTempContainerByComponent(String sAreaId, String sUserId) {
		String sParam[] = new String[] { sAreaId, sUserId };
		return dbDatabBase.rawQuery("select ComponentId,ComponentDesc from TempContainer where TempAreaId = ? and EmployeeId = ? and Hist = '1' group by ComponentId, ComponentDesc order by ComponentDesc asc" , sParam);//distinct
	}

	//OBTENER LOS REGISTROS DE CONTAINERS A CREAR
	public Cursor GetTempContainerById(String sContainerNumber, String sFlag) {
		String sParam[] = new String[] { sContainerNumber, sFlag };
		return dbDatabBase.rawQuery("select ComponentDesc, ComponentQuantity, ComponentId from TempContainer where ContainerNumber = ? and Flag = ?" , sParam);//
	}

	//OBTENER LOS REGISTROS DE CONTAINERS A CREAR
	public Cursor GetTempContainerByComponentId(String sComponentId, String sFlag) {
		String sParam[] = new String[] { sComponentId, sFlag };
		return dbDatabBase.rawQuery("select ContainerNumber, ComponentQuantity, ComponentId, ComponentDesc from TempContainer where ComponentId = ? and Flag = ?" , sParam);//
	}

	//ELIMINA  DATOS DE LA TABLA BIN
	protected boolean DeleteComponentById(String sComponentId) {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableComponents, "ComponentId = '"+ sComponentId +"'", null) > 0;
		}
		return (transact);
	}

	//OBTENER TODOS LOS COMPONENTS
	public Cursor GetAllComponents(String sProductState) {
		String sParam[] = new String[] { sProductState };
		return dbDatabBase.rawQuery("select ComponentId, ProductStateFrom, Description ,Barcode, ReasonBarcode, VendorReference, PurchaseUom, QuantityMin, QuantityMax from Components where ProductStateFrom = ?" , sParam);
	}

	//OBTENER TODOS LOS COMPONENTS BY BARCODE
	public Cursor GetComponentsByBarcode(String sBarcode, String sProductState) {
		sProductState = "PANTR";
		String sParam[] = new String[] { sBarcode, sProductState };
		return dbDatabBase.rawQuery("select ComponentId, ProductStateFrom, Description ,Barcode, ReasonBarcode, VendorReference, PurchaseUom, QuantityMin, QuantityMax from Components where Barcode = ? and ProductStateFrom = ?" , sParam);
	}

	//OBTENER TODOS LOS COMPONENTS QUE ESTAN AGREGADOS A LA LISTA
	public Cursor GetListByUser(String sEmployee) {
		String sParam[] = new String[] { sEmployee };
		return dbDatabBase.rawQuery("select TempListComponentId, TempListDescription, TempQuantity ,EmployeeId, Barcode from TempList  where EmployeeId = ? order by TempId DESC" , sParam);
	}

	//INSERTAR UN CONTAINER A TEMPCONTAINER
	protected boolean InsertTempList(String sTempListComponentId, String sTempListDescription, String sTempQuantity, String sEmployeeId, String sBarcode) {
		ContentValues values = new ContentValues();
		values.put("TempListComponentId", sTempListComponentId);
		values.put("TempListDescription", sTempListDescription);
		values.put("TempQuantity", sTempQuantity);
		values.put("EmployeeId", sEmployeeId);
		values.put("Barcode", sBarcode);

		return (dbDatabBase.insert(sTableTempList, null, values) > 0);
	}

	//ELIMINA  DATOS DE LA TABLA COMPONENTLIST
	public boolean DeleteComponentListItem(String sTempListComponentId, String sEmployeeId) {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableTempList, "TempListComponentId = '"+ sTempListComponentId +"' and EmployeeId = '" + sEmployeeId + "'", null) > 0;
		}
		return (transact);
	}

	//ACTUALIZA UN CONTAINER A TEMPCONTAINER
	public boolean UpdateTempList(String sTempListComponentId, String sEmployee, String sTempQuantity) {
		ContentValues values = new ContentValues();
		boolean transact = false;
		values.put("TempQuantity", sTempQuantity);
		transact = (dbDatabBase.update(sTableTempList, values, "TempListComponentId='" + sTempListComponentId + "' and EmployeeId = '" + sEmployee + "'", null) > 0);
		return (transact);
	}

	//OBTENER TODOS LOS COMPONENTS QUE ESTAN AGREGADOS A LA LISTA
	public Cursor GetListByUserAndComponentId(String sEmployee, String sComponentId) {
		String sParam[] = new String[] { sEmployee, sComponentId };
		return dbDatabBase.rawQuery("select TempListComponentId, TempListDescription, TempQuantity ,EmployeeId, Barcode from TempList where EmployeeId = ? and TempListComponentId = ?" , sParam);
	}

	//ELIMINA DATOS DE LA TABLA CONTAINER
	protected boolean DeleteContainersLog(String sContainerNumber, String sChekpointId) {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableLog, "containerNumber = '" + sContainerNumber + "' and CheckPointId = '" + sChekpointId + "'", null) > 0;
		}
		return (transact);
	}

	//ELIMINA LOS REGISTROS DE LA LISTA TEMPORAL
	public boolean DeleteTempContainer( String sEmployee) {
		ContentValues values = new ContentValues();
		boolean transact = false;
		values.put("Hist", "2");
		transact = (dbDatabBase.update(sTableTempContainers, values, "EmployeeId = '" + sEmployee + "'", null) > 0);
		return (transact);
	}

	//ELIMINA  DATOS DE LA TABLA COMPONENTLIST
	public boolean DeleteAllComponentListItem() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableTempList, null, null) > 0;
		}
		return (transact);
	}

	//ELIMINA  DATOS DE LA TABLA TEMPCONTAINERS
	public boolean DeleteAllTempContainer() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableTempContainers, null, null) > 0;
		}
		return (transact);
	}

	//OBTENER LOS TODOS LOS REGISTROS DE RECEPCION DE PROVEEDOR
	public Cursor GetAllTempContainerByArea() {
		return dbDatabBase.rawQuery("select distinct ContainerNumber from TempContainer where Hist = '1'" , null);
	}

	//ELIMINA UN DATO DE LA TABLA TEMP CONTAINER
	protected boolean RemoveTempContainer(String sContainerNumber) {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableTempContainers, "ContainerNumber = '" + sContainerNumber + "'", null) > 0;
		}
		return (transact);
	}

	//OBTIENE LA CAPACIDAD MAXIMA DE CADA UBICACION
	public Cursor GetBinQuantity(String sBinId) {
		String sParam[] = new String[] {sBinId};
		return dbDatabBase.rawQuery("select PackQuantity from Bin where BinId = ?", sParam);
	}

	//OBTIENE LA CANTIDAD DE CONTAINERS EN UNA BIN ESPECIFICA
	public Cursor GetContainerInBin(String sBinId) {
		String sParam[] = new String[] {sBinId};
		return dbDatabBase.rawQuery("select a.containerNumber from Containers as a inner join Bin as b on a.binid = b.BinId where b.binid = ? and statusinv = 'I'", sParam);
	}

	//OBTENER LOS CONTAINER DEL BIN SELECIONADO
	public Cursor GetContainerByBin( String sBinId) {
		String sParam[] = new String[] { sBinId };
		return dbDatabBase.rawQuery("select containerid, ContainerWeight, containerNumber, ContainerDescription from Containers where binid = ? and statusinv = 'I'", sParam);
	}

	//ACTUALIZA UN CONTAINER A TEMPCONTAINER
	public boolean UpdateStatusContainer(String sContainerNumber, String sPart) {
		ContentValues values = new ContentValues();
		boolean transact = false;
		values.put("statusinv", "G");
		transact = (dbDatabBase.update(sTableContainers, values, "containerNumber='" + sContainerNumber + "' and Part = '" + sPart + "'", null) > 0);
		return (transact);
	}

	//ELIMINA LOS DATOS DE LA TABLA DE RUTAS
	protected boolean DeleteRoutes() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableRoutes, null, null) > 0;
		}
		return (transact);
	}

	//INSERTAR DATOS DE LA TABLA DE RUTAS
	protected boolean InsertRoutes(String sRouteId, String sRouteDescription, String sOriginAreaId, String sDestinationAreaId, String sPackingList) {
		ContentValues values = new ContentValues();
		values.put("routeid", sRouteId);
		values.put("description", sRouteDescription);
		values.put("originareaid", sOriginAreaId);
		values.put("destinationareaid", sDestinationAreaId);
		values.put("packinglist", sPackingList);
		return (dbDatabBase.insert(sTableRoutes, null, values) > 0);
	}

	//OBTIENE LOS DESTINOS POSIBLES DE ESA AREA
	public Cursor GetDestinationArea( String sAreaId, String sDestinationAreaId) {
		String sParam[] = new String[] { sAreaId, sDestinationAreaId };
		return dbDatabBase.rawQuery("select a.destinationareaid as _id, a.description as description, b.productstatefrom, a.packinglist from routes as a inner join area as b on a.originareaid = b.areaid where a.originareaid = ? and destinationareaid <> '4' order by case when destinationareaid = ? then 1 else 2 end, destinationareaid", sParam);
	}//select a.destinationareaid as _id, a.description as description, b.productstatefrom from routes as a inner join area as b on a.originareaid = b.areaid where a.originareaid = ?

	//OBTIENE LOS DESTINOS POSIBLES DE ESA AREA
	public Cursor GetDestinationAreaClient( String sAreaId) {
		String sParam[] = new String[] { sAreaId };
		return dbDatabBase.rawQuery("select a.destinationareaid as _id, a.description as description, b.productstatefrom from routes as a inner join area as b on a.originareaid = b.areaid where a.originareaid = ? and destinationareaid = '4'", sParam);
	}

	//OBTIENE LOS DESTINOS POSIBLES DE ESA AREA
	public Cursor GetProductStateByArea(String sAreaId) {
		String sParam[] = new String[] { sAreaId };
		return dbDatabBase.rawQuery("select productstatefrom as productstatefrom from area where areaid = ?", sParam);
	}
	//ACTUALIZAR CONTAINERS
	protected boolean UpdateContainersSpecificSnd( String sContainerNumber, String sDatePrinted,  String sBindId, String sAreaId) {
		String sNewSqlDate = "";
		try{
			SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
			Date dDate = sdfFormat.parse(sDatePrinted);
			sdfFormat = new SimpleDateFormat("yyy-MM-dd HH:mm:ss",Locale.US);
			sNewSqlDate = sdfFormat.format(dDate);

		} catch (Exception e) {
			Log.e("MainActivity", e.getMessage(), e);
		}

		ContentValues values = new ContentValues();
		values.put("CurrentProcessStatus", "I");
		values.put("CurrentProcess", "1");
		values.put("statusinv", "T");
		values.put("dateprinted", sDatePrinted);
		values.put("previousdateprinted", sDatePrinted);
		values.put("StopReasonId","1");
		values.put("binid",sBindId);
		values.put("areaid",sAreaId);
		return (dbDatabBase.update(sTableContainers, values, "containerNumber='"
				+ sContainerNumber + "'", null) > 0);
	}
	protected boolean UpdateContainersSpecificSndI( String sContainerNumber, String sDatePrinted,  String sBindId, String sAreaId) {
		String sNewSqlDate = "";
		try{

			SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
			Date dDate = sdfFormat.parse(sDatePrinted);
			sdfFormat = new SimpleDateFormat("yyy-MM-dd HH:mm:ss",Locale.US);
			sNewSqlDate = sdfFormat.format(dDate);

		} catch (Exception e) {
			Log.e("MainActivity", e.getMessage(), e);
		}

		ContentValues values = new ContentValues();
		values.put("CurrentProcessStatus", "I");
		values.put("CurrentProcess", "1");
		values.put("statusinv", "I");
		values.put("dateprinted", sDatePrinted);
		values.put("previousdateprinted", sDatePrinted);
		values.put("StopReasonId","1");
		values.put("binid",sBindId);
		//values.put("areaid",sAreaId);
		return (dbDatabBase.update(sTableContainers, values, "containerNumber='"
				+ sContainerNumber + "'", null) > 0);
	}

	protected boolean UpdateContainersSpecificRcv( String sContainerNumber, String sDatePrinted,  String sBindId, String sAreaId) {
		String sNewSqlDate = "";
		try{

			SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
			Date dDate = sdfFormat.parse(sDatePrinted);
			sdfFormat = new SimpleDateFormat("yyy-MM-dd HH:mm:ss",Locale.US);
			sNewSqlDate = sdfFormat.format(dDate);

		} catch (Exception e) {
			Log.e("MainActivity", e.getMessage(), e);
		}

		ContentValues values = new ContentValues();
		values.put("CurrentProcessStatus", "I");
		values.put("CurrentProcess", "1");
		values.put("statusinv", "I");
		values.put("dateprinted", sDatePrinted);
		values.put("previousdateprinted", sDatePrinted);
		values.put("StopReasonId","1");
		values.put("binid",sBindId);
		values.put("areaid",sAreaId);
		return (dbDatabBase.update(sTableContainers, values, "containerNumber='"
				+ sContainerNumber + "'", null) > 0);
	}
	protected boolean UpdateContainersSpecificRcvT( String sContainerNumber, String sDatePrinted,  String sBindId, String sAreaId) {
		String sNewSqlDate = "";
		try{

			SimpleDateFormat sdfFormat = new SimpleDateFormat("M/d/yyyy hh:mm:ss a", Locale.US);
			Date dDate = sdfFormat.parse(sDatePrinted);
			sdfFormat = new SimpleDateFormat("yyy-MM-dd HH:mm:ss",Locale.US);
			sNewSqlDate = sdfFormat.format(dDate);

		} catch (Exception e) {
			Log.e("MainActivity", e.getMessage(), e);
		}

		ContentValues values = new ContentValues();
		values.put("CurrentProcessStatus", "I");
		values.put("CurrentProcess", "1");
		values.put("statusinv", "T");
		values.put("dateprinted", sDatePrinted);
		values.put("previousdateprinted", sDatePrinted);
		values.put("StopReasonId","1");
		return (dbDatabBase.update(sTableContainers, values, "containerNumber='"
				+ sContainerNumber + "'", null) > 0);
	}
	//OBTIENE EL CHECKPOINTGROUP UTILIZANDO EN CHECKPOINTID
	public Cursor GetCheckpointGroup(String sCheckpointId) {
		String sParam[] = new String[] {sCheckpointId};
		return dbDatabBase.rawQuery("select CheckPointGroup from Checkpoints where CheckPointId = ?", sParam);
	}

	protected boolean UpdateContainersSpecificRcvReturn( String sContainerNumber) {
		ContentValues values = new ContentValues();
		values.put("CurrentProcessStatus", "I");
		values.put("CurrentProcess", "1");
		values.put("statusinv", "I");
		values.put("StopReasonId","1");

		return (dbDatabBase.update(sTableContainers, values, "containerNumber='"
				+ sContainerNumber + "'", null) > 0);
	}

	//ELIMINA REGISTRO DE LOG
	protected boolean DeleteMovimientoLog(String sContainerNumber, String sCheckpointGroup) {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableLogMovimiento, "containerNumber = '" + sContainerNumber + "' and CheckpointGroup = '" + sCheckpointGroup + "'", null) > 0;
		}
		return (transact);
	}

	//OBTIENE LA RUTA
	public Cursor GetRoute( String sOriginAreaId, String sDestinationAreaId) {
		String sParam[] = new String[] { sOriginAreaId, sDestinationAreaId };
		return dbDatabBase.rawQuery("select routeid, packinglist from routes where originareaid = ? and destinationareaid = ?", sParam);
	}

	//OBTIENE LA INFORMACION DE PACKINGLISTS
	public Cursor GetPackingListInfo( String sPackingListId) {
		String sParam[] = new String[] { sPackingListId };
		return dbDatabBase.rawQuery("select PackingListId, RouteDescription, CheckPointDescription, ContainerNumber, IsChecked, Quantity, Compcode from PackingListInfo where PackingListId in ( " + sPackingListId + " ) order by case when IsChecked = 'f' then 1 else 2 end, IsChecked", null);
	}

	//INSERTAR PACKINGLISTINFO
	protected boolean InsertPackingListInfo(String sPackingListId, String sCheckpointDescription, String sRouteDescription, String sContainerNumber, String sQuantity, String sCompcode) {

		String sqlQuery = "insert into " + sTablePackingListInfo + " (PackingListId, CheckPointDescription, RouteDescription, ContainerNumber, IsChecked, Quantity, Compcode) values (?, ?, ?, ?, ?, ?, ?)";
		SQLiteStatement sqlStatement = dbDatabBase.compileStatement(sqlQuery);
		sqlStatement.bindString(1, sPackingListId.trim());
		sqlStatement.bindString(2, sCheckpointDescription.trim());
		sqlStatement.bindString(3, sRouteDescription.trim());
		sqlStatement.bindString(4, sContainerNumber.trim());
		sqlStatement.bindString(5, "f");
        sqlStatement.bindString(6, sQuantity);
        sqlStatement.bindString(7, sCompcode);

		Long lResult = sqlStatement.executeInsert();
		sqlStatement.clearBindings();

		return (lResult > 0);
	}
	//ELIMINA LOS DATOS DE LA TABLA USUARIO
	protected boolean DeletePackingListInfo() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTablePackingListInfo, null, null) > 0;
		}
		return (transact);
	}

	//ELIMINA LOS DATOS DE LA TABLA USUARIO
	protected boolean DeletePackingListInfoSpecific(String sPackingListId) {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTablePackingListInfo, "PackingListId in (" + sPackingListId + ")", null) > 0;
		}
		return (transact);
	}

	//ACTUALIZA EL ESTADO, SI ENCONTRO O NO ENCONTRO
	protected boolean UpdatePackingListInfoFlag( String sContainerNumber, String sPackingListId) {
		ContentValues values = new ContentValues();
		values.put("IsChecked", "t");

		return (dbDatabBase.update(sTablePackingListInfo, values, "ContainerNumber='"
				+ sContainerNumber + "' and PackingListId in(" + sPackingListId + ")", null) > 0);
	}

    //ACTUALIZA EL ESTADO, PARA TODO EL PL
    protected boolean UpdatePackingListInfoFlagAll() {
        ContentValues values = new ContentValues();
        values.put("IsChecked", "f");

        return (dbDatabBase.update(sTablePackingListInfo, values, null, null) > 0);
    }

	//OBTIENE LA INFORMACION DE PACKINGLISTS
	public Cursor GetPackingListInfoDetail( String sPackingListId) {
		String sParam[] = new String[] { sPackingListId };
		return dbDatabBase.rawQuery("select PackingListId, RouteDescription, CheckPointDescription, Count(ContainerNumber), sum(Quantity) from PackingListInfo where PackingListId in ( " + sPackingListId + " ) group by PackingListId,RouteDescription,CheckPointDescription", null);
	}

	//OBTIENE LA INFORMACION DE PACKINGLISTS
	public Cursor GetPackingListInfoDetailMissing( String sPackingListId) {
		String sParam[] = new String[] { sPackingListId };
		return dbDatabBase.rawQuery("select IsChecked, COUNT(ContainerNumber) from PackingListInfo where PackingListId in ( " + sPackingListId + " ) and IsChecked =  'f' group by IsChecked", null);
	}
	public Cursor GetPackingListInfoDetailScans( String sPackingListId) {
		String sParam[] = new String[] { sPackingListId };
		return dbDatabBase.rawQuery("select IsChecked, COUNT(ContainerNumber) from PackingListInfo where PackingListId in ( " + sPackingListId + " ) and IsChecked =  't' group by IsChecked", null);
	}

	public Cursor GetListSalesByUser(String sEmployee) {
		String sParam[] = new String[] { sEmployee };
		return dbDatabBase.rawQuery("select TempListComponentId, TempListDescription, TempQuantity ,EmployeeId, Barcode from TempListSales  where EmployeeId = ? order by TempId DESC" , sParam);
	}

	public boolean DeleteAllComponentListSalesItem() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableTempListSales, null, null) > 0;
		}
		return (transact);
	}

	//ACTUALIZA UN CONTAINER A TEMPCONTAINER
	public boolean UpdateTempListSales(String sTempListComponentId, String sEmployee, String sTempQuantity) {
		ContentValues values = new ContentValues();
		boolean transact = false;
		values.put("TempQuantity", sTempQuantity);
		transact = (dbDatabBase.update(sTableTempListSales, values, "TempListComponentId='" + sTempListComponentId + "' and EmployeeId = '" + sEmployee + "'", null) > 0);
		return (transact);
	}

	//ELIMINA  DATOS DE LA TABLA COMPONENTLIST
	public boolean DeleteComponentListSalesItem(String sTempListComponentId, String sEmployeeId) {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableTempListSales, "TempListComponentId = '"+ sTempListComponentId +"' and EmployeeId = '" + sEmployeeId + "'", null) > 0;
		}
		return (transact);
	}

	//INSERTAR UN CONTAINER A TEMPCONTAINER
	protected boolean InsertTempListSales(String sTempListComponentId, String sTempListDescription, String sTempQuantity, String sEmployeeId, String sBarcode) {
		ContentValues values = new ContentValues();
		values.put("TempListComponentId", sTempListComponentId);
		values.put("TempListDescription", sTempListDescription);
		values.put("TempQuantity", sTempQuantity);
		values.put("EmployeeId", sEmployeeId);
		values.put("Barcode", sBarcode);

		return (dbDatabBase.insert(sTableTempListSales, null, values) > 0);
	}

	//INSERTAR INVENTARIO DE DESPENSA
	protected boolean InsertMarketInventory(String sComponentId, String sCompCode, String sDescription, String sBarcode, String sQuantity) {

		String sqlQuery = "insert into " + sTableMarketInv + " (ComponentId, CompCode, Description, Quantity," +
				" Barcode) values (?, ?, ?, ?, ?)";

		SQLiteStatement sqlStatement = dbDatabBase.compileStatement(sqlQuery);
		sqlStatement.bindString(1, sComponentId);
		sqlStatement.bindString(2, sCompCode);
		sqlStatement.bindString(3, sDescription);
		sqlStatement.bindString(4, sQuantity);
		sqlStatement.bindString(5, sBarcode);

		Long lResult = sqlStatement.executeInsert();
		sqlStatement.clearBindings();

		return (lResult > 0);
	}

	//ELIMINA  DATOS DE LA TABLA COMPONENTLIST
	public boolean DeleteMarketInventory() {
		boolean transact = false;
		if (dbDatabBase != null && dbDatabBase.isOpen()) {
			transact = dbDatabBase.delete(sTableMarketInv, null, null) > 0;
		}
		return (transact);
	}

	//OBTENER TODOS LOS COMPONENTS BY BARCODE
	public Cursor GetComponentsByMarketInv(String sBarcode, String sProductState) {
		sProductState = "PANTR";
		String sParam[] = new String[] { sBarcode };
		return dbDatabBase.rawQuery("select ComponentId,  CompCode,Description ,Barcode, Quantity from MarketInventory where Barcode = ?" , sParam);
	}

	//OBTENER TODOS LOS COMPONENTS
	public Cursor GetAllComponentsByMarketInv(String sProductState) {
		String sParam[] = new String[] { sProductState };
		return dbDatabBase.rawQuery("select ComponentId ,Quantity,Description, Barcode from MarketInventory" , null);
	}


}
