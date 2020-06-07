/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/flyme/NewFlyme/Code/MEIZU_Apps_Lib_Publish_Artifactory_2944/MzAnalyticSdk/sdk/src/main/aidl/com/meizu/statsrpk/IRpkStatsInterface.aidl
 */
package com.meizu.statsrpk;
// Declare any non-default types here with import statements

public interface IRpkStatsInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements IRpkStatsInterface
{
private static final String DESCRIPTOR = "com.meizu.statsrpk.IRpkStatsInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.meizu.statsrpk.IRpkStatsInterface interface,
 * generating a proxy if needed.
 */
public static IRpkStatsInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof IRpkStatsInterface))) {
return ((IRpkStatsInterface)iin);
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
case TRANSACTION_track:
{
data.enforceInterface(DESCRIPTOR);
RpkEvent _arg0;
if ((0!=data.readInt())) {
_arg0 = RpkEvent.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
RpkInfo _arg1;
if ((0!=data.readInt())) {
_arg1 = RpkInfo.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.track(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements IRpkStatsInterface
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
@Override public void track(RpkEvent rpkEvent, RpkInfo rpkInfo) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((rpkEvent!=null)) {
_data.writeInt(1);
rpkEvent.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
if ((rpkInfo!=null)) {
_data.writeInt(1);
rpkInfo.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_track, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_track = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void track(RpkEvent rpkEvent, RpkInfo rpkInfo) throws android.os.RemoteException;
}
