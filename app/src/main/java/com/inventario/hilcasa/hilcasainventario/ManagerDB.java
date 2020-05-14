package com.inventario.hilcasa.hilcasainventario;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ManagerDB extends SQLiteOpenHelper {
	private static final String TAG = "Inventario";

	//TABLA CONTAINERS
	private static final String sTest = "drop table if exists Test;";
	private static final String sTestC = "create table if not exists Test(test text);";

	//TABLA CONTAINERS
	private static final String sDropTableContainers = "drop table if exists Containers;";
	private static final String sCreateTableContainers = "create table if not exists Containers(containerid text , containerNumber text, CurrentProcessStatus text, CurrentProcess text, dateprinted text, ContainerDescription text, ContainerWeight text, ProcessDuration text, previousdateprinted text, StartWeight text, DateTime text, StopReasonId text, Part text, binid text, areaid text, vendornumber text, containertype text, componentid text, parentcontainernumber text, statusinv text, PRIMARY KEY (containerNumber, Part));";

	//TABLA AREA
	private static final String sDropTableArea = "drop table if exists Area;";
	private static final String sCreateTableArea = "create table if not exists Area(areaid text, name text, isproductionpoint text, ispackingpoint, issubcontractor text, productstatefrom text);";

	//TABLA CHECKPOINTS
	private static final String sDropTableCheckPoints = "drop table if exists Checkpoints;";
	private static final String sCreateTableCheckPoints = "create table if not exists Checkpoints(CheckPointId text, Description text, productstateform text, CheckPointGroup text);";

	//TABLA USER
	private static final String sDropTableUser = "drop table if exists User;";
	private static final String sCreateTableUser = "create table if not exists User(EmployeeId text, name text);";

	//TABLA AREAUSER
	private static final String sDropTableAreaUser = "drop table if exists AreaUser;";
	private static final String sCreateTableAreaUser = "create table if not exists AreaUser(EmployeeId text, AreaId text, CheckPointId text, ProductState text);";

	//TABLA BINS
	private static final String sDropTableBin = "drop table if exists Bin;";
	private static final String sCreateTableBin = "create table if not exists Bin(BinId Text, AreaId Text, Description text, Aisle text, PackQuantity text);";

	//TABLA LOG
	private static final String sDropTableLog = "drop table if exists LogInventory;";
	private static final String sCreateTableLog = "create table if not exists LogInventory(LogId integer primary key autoincrement, AreaId text, BinId text, ContainerId text, dateprinted datetime, ContainerNumber text, ContainerWeight text, ContainerDescription text, ShowLog Text, CheckPointId text);";

	//TABLA LOGOFFLINE
	private static final String sDropTableLogOffline = "drop table if exists LogOffline;";
	private static final String sCreateTableLogOffline = "create table if not exists LogOffline(Id integer primary key autoincrement, Query text);";

	//TABLA CHECKPOINTUSERS
	private static final String sDropTableCheckPointUsers = "drop table if exists CheckpointUsers;";
	private static final String sCreateTableCheckPointUsers = "create table if not exists CheckpointUsers(CheckpointId text, EmployeeId text);";

	//TABLA LOGMOVIMIENTOAREA
	private static final String sDropTableLogMovimiento = "drop table if exists LogMovimiento;";
	private static final String sCreateTableLogMovimiento = "create table if not exists LogMovimiento(ContainerNumber text, FechaMovimiento text, CheckpointGroup text, StateForm text, AreaId text, BinId text, ShowLog Text, Description text);";

	//TABLA LOGOFFLINE
	private static final String sDropTableLogInicialOffline = "drop table if exists LogInicialOffline;";
	private static final String sCreateTableLogInicialOffline = "create table if not exists LogInicialOffline(Id integer primary key autoincrement, Query text);";

	//TABLA LASTLOG
	private static final String sDropTableLastLog = "drop table if exists LastLog;";
	private static final String sCreateTableLastLog = "create table if not exists LastLog(InputText text, Identifier Text, Date text);";

	//TABLA COMPONENTS
	private static final String sDropTableComponents = "drop table if exists Components;";
	private static final String sCreateTableComponents = "create table if not exists Components(ComponentId text, ProductStateFrom Text, Description text, Barcode text, ReasonBarcode text, VendorReference text, PurchaseUom text, QuantityMin text, QuantityMax text);";

	//TABLA TEMPCONTAINER
	private static final String sDropTableTempContainer = "drop table if exists TempContainer;";
	private static final String sCreateTableTempContainer = "create table if not exists TempContainer(ContainerNumber Text, ComponentId Text, ComponentDesc Text, ComponentQuantity Text, Flag text, TempAreaId text, EmployeeId text, Hist text);";

	//TABLA TEMPLIST
	private static final String sDropTableTempList = "drop table if exists TempList;";
	private static final String sCreateTableTempList = "create table if not exists TempList(TempListComponentId text, TempListDescription Text, TempQuantity, EmployeeId text, Barcode text, TempId integer primary key autoincrement);";

	//TABLA LASTLOG
	private static final String sDropTableRoutes = "drop table if exists Routes;";
	private static final String sCreateTableRoutes = "create table if not exists Routes(routeid text, description Text, originareaid text, destinationareaid text, packinglist text);";

	//TABLA PACKINGLISTINFO
	private static final String sDropTablePackingListInfo = "drop table if exists PackingListInfo;";
	private static final String sCreateTablePackingListInfo = "create table if not exists PackingListInfo(PackingListId text, RouteDescription text, CheckPointDescription text, ContainerNumber text, IsChecked text, Quantity text, Compcode text);";

	//TABLA TEMPLISTSALES
	private static final String sDropTableTempListSales = "drop table if exists TempListSales;";
	private static final String sCreateTableTempListSales = "create table if not exists TempListSales(TempListComponentId text, TempListDescription Text, TempQuantity, EmployeeId text, Barcode text, TempId integer primary key autoincrement);";

	private static final String sDropTableMarketInventory = "drop table if exists MarketInventory;";
	private static final String sCreateTableMarketInventory= "create table if not exists MarketInventory(ComponentId text, CompCode Text,Description Text, Quantity Text, Barcode text, MarketInventoryId integer primary key autoincrement);";

	// NOMBRE Y VERSION DE LA BASE DE DATOS A CREAR
	private static final String databaseName = "HilcasaInventario";
	// VERSION DE BASE DE DATOS PARA VERSION DE CODIGO 23
	private static final int databaseVersion = 15;

	public ManagerDB(Context CtxContext) {
		super(CtxContext, databaseName, null, databaseVersion);
	}

	@Override
	public void onCreate(SQLiteDatabase dbDataBase) {
		try {
			dbDataBase.execSQL(sTestC);
			//CREACION DE TABLA CONTAINERS
			dbDataBase.execSQL(sCreateTableContainers);
			//CREACION DE TABLA AREA
			dbDataBase.execSQL(sCreateTableArea);
			//CREACION DE TABLA CHECKPOINTS
			dbDataBase.execSQL(sCreateTableCheckPoints);
			//CREACION DE TABLA USER
			dbDataBase.execSQL(sCreateTableUser);
			//CREACION DE TABLA AREAUSER
			dbDataBase.execSQL(sCreateTableAreaUser);
			//CREACION DE TABLA BIN
			dbDataBase.execSQL(sCreateTableBin);
			//CREACION DE TABLA LOG
			dbDataBase.execSQL(sCreateTableLog);
			//CREACION DE TABLA LOGOFFLINE
			dbDataBase.execSQL(sCreateTableLogOffline);
			//CREACION DE TABLA CHECKPOINTUSERS
			dbDataBase.execSQL(sCreateTableCheckPointUsers);
			//CREACION DE TABLA LOGMOVIMIENTO
			dbDataBase.execSQL(sCreateTableLogMovimiento);
			//CREACION DE TABLA LOGINICIALOFFLINE
			dbDataBase.execSQL(sCreateTableLogInicialOffline);
			//CREACION DE TABLA LASTLOG
			dbDataBase.execSQL(sCreateTableLastLog);
			//CREACION DE TABLA COMPONENTS
			dbDataBase.execSQL(sCreateTableComponents);
			//CREACION DE TABLA TEMPCONTAINER
			dbDataBase.execSQL(sCreateTableTempContainer);
			//CREACION DE TABLA TEMPLIST
			dbDataBase.execSQL(sCreateTableTempList);
			//CREACION DE TABLA ROUTES
			dbDataBase.execSQL(sCreateTableRoutes);
			//CREACION DE TABLA PACKINGLISTINFO
			dbDataBase.execSQL(sCreateTablePackingListInfo);
			//CREACION DE TABLA TEMPLISTSALES
			dbDataBase.execSQL(sCreateTableTempListSales);
			//CREACION DE TABLA MAKERINVENTORY
			dbDataBase.execSQL(sCreateTableMarketInventory);

		} catch (Exception e) {
			Log.i(TAG, "Error al abrir o crear la base de datos" + e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase dbDataBase, int iPreviousVersion,
			int iNewVersion) {
		if (iNewVersion > iPreviousVersion) {
			try {
				dbDataBase.execSQL(sTest);
				//ELIMINACION DE TABLA CONTAINERS
				dbDataBase.execSQL(sDropTableContainers);
				//ELIMINACION DE TABLA AREA
				dbDataBase.execSQL(sDropTableArea);
				//ELIMINACION DE TABLA CHECKPOINTS
				dbDataBase.execSQL(sDropTableCheckPoints);
				//ELIMINACION DE TABLA USER
				dbDataBase.execSQL(sDropTableUser);
				//ELIMINACION DE TABLA AREAUSER
				dbDataBase.execSQL(sDropTableAreaUser);
				//ELIMINACION DE TABLA BIN
				dbDataBase.execSQL(sDropTableBin);
				//ELIMINACION DE TABLA LOG
				dbDataBase.execSQL(sDropTableLog);
				//ELIMINACION DE TABLA LOGOFFLINE
				dbDataBase.execSQL(sDropTableLogOffline);
				//ELIMINACION DE TABLA CHECKPOINTUSERS
				dbDataBase.execSQL(sDropTableCheckPointUsers);
				//ELIMINACION DE TABLA LOGMOVIMIENTO
				dbDataBase.execSQL(sDropTableLogMovimiento);
				//ELIMINACION DE TABLA LOGINICIALOFFLINE
				dbDataBase.execSQL(sDropTableLogInicialOffline);
				//ELIMINACION DE TABLA LASTLOG
				dbDataBase.execSQL(sDropTableLastLog);
				//ELIMINACION DE TABLA COMPONENTS
				dbDataBase.execSQL(sDropTableComponents);
				//ELIMINACION DE TABLA TEMPCONTAINERS
				dbDataBase.execSQL(sDropTableTempContainer);
				//ELIMINACION DE TABLA TEMPLIST
				dbDataBase.execSQL(sDropTableTempList);
				//ELIMINACION DE TABLA ROUTES
				dbDataBase.execSQL(sDropTableRoutes);
				//ELIMINACION DE TABLA PACKINGLISTINFO
				dbDataBase.execSQL(sDropTablePackingListInfo);
				//ELIMINACION DE TABLA TEMPLISTSALES
				dbDataBase.execSQL(sDropTableTempListSales);
				//ELIMINACION DE TABLA MAKERINVENTORY
				dbDataBase.execSQL(sDropTableMarketInventory);

				onCreate(dbDataBase);

			} catch (Exception e) {
				Log.i(TAG, "Error al abrir o crear la base de datos" + e);
			}

		}
	}

}
