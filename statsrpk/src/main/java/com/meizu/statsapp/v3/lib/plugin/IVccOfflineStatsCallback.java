/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/flyme/NewFlyme/Code/MEIZU_Apps_Lib_Publish_Artifactory_2944/MzAnalyticSdk/sdk/src/main/aidl/com/meizu/statsapp/v3/lib/plugin/IVccOfflineStatsCallback.aidl
 */
package com.meizu.statsapp.v3.lib.plugin;
// Declare any non-default types here with import statements

public interface IVccOfflineStatsCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements IVccOfflineStatsCallback
{
private static final String DESCRIPTOR = "com.meizu.statsapp.v3.lib.plugin.IVccOfflineStatsCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.meizu.statsapp.v3.lib.plugin.IVccOfflineStatsCallback interface,
 * generating a proxy if needed.
 */
public static IVccOfflineStatsCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof IVccOfflineStatsCallback))) {
return ((IVccOfflineStatsCallback)iin);
}
return new Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_onRealInsertEvent:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
long _arg1;
_arg1 = data.readLong();
this.onRealInsertEvent(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_onRealBulkInsertEvents:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
java.util.List _arg1;
ClassLoader cl = (ClassLoader)this.getClass().getClassLoader();
_arg1 = data.readArrayList(cl);
this.onRealBulkInsertEvents(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_onRealInsertH5Event:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
long _arg1;
_arg1 = data.readLong();
this.onRealInsertH5Event(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements IVccOfflineStatsCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
    * 远端真正把事件插入了数据库
    */
@Override public void onRealInsertEvent(String packageName, long eventId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeLong(eventId);
mRemote.transact(Stub.TRANSACTION_onRealInsertEvent, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
    * 远端真正把事件批量插入了数据库
    */
@Override public void onRealBulkInsertEvents(String packageName, java.util.List eventIds) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeList(eventIds);
mRemote.transact(Stub.TRANSACTION_onRealBulkInsertEvents, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onRealInsertH5Event(String packageName, long id) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeLong(id);
mRemote.transact(Stub.TRANSACTION_onRealInsertH5Event, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onRealInsertEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onRealBulkInsertEvents = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_onRealInsertH5Event = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
/**
    * 远端真正把事件插入了数据库
    */
public void onRealInsertEvent(String packageName, long eventId) throws android.os.RemoteException;
/**
    * 远端真正把事件批量插入了数据库
    */
public void onRealBulkInsertEvents(String packageName, java.util.List eventIds) throws android.os.RemoteException;
public void onRealInsertH5Event(String packageName, long id) throws android.os.RemoteException;
}
