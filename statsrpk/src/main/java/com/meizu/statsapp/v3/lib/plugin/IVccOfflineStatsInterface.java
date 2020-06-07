/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/flyme/NewFlyme/Code/MEIZU_Apps_Lib_Publish_Artifactory_2944/MzAnalyticSdk/sdk/src/main/aidl/com/meizu/statsapp/v3/lib/plugin/IVccOfflineStatsInterface.aidl
 */
package com.meizu.statsapp.v3.lib.plugin;
// Declare any non-default types here with import statements

public interface IVccOfflineStatsInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements IVccOfflineStatsInterface
{
private static final String DESCRIPTOR = "com.meizu.statsapp.v3.lib.plugin.IVccOfflineStatsInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.meizu.statsapp.v3.lib.plugin.IVccOfflineStatsInterface interface,
 * generating a proxy if needed.
 */
public static IVccOfflineStatsInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof IVccOfflineStatsInterface))) {
return ((IVccOfflineStatsInterface)iin);
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
case TRANSACTION_emitterInit:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
com.meizu.statsapp.v3.lib.plugin.emitter.EmitterConfig _arg1;
if ((0!=data.readInt())) {
_arg1 = com.meizu.statsapp.v3.lib.plugin.emitter.EmitterConfig.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.emitterInit(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_emitterAddEvent:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
long _arg1;
_arg1 = data.readLong();
com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload _arg2;
if ((0!=data.readInt())) {
_arg2 = com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload.CREATOR.createFromParcel(data);
}
else {
_arg2 = null;
}
this.emitterAddEvent(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_emitterAddEventRealtime:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
long _arg1;
_arg1 = data.readLong();
com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload _arg2;
if ((0!=data.readInt())) {
_arg2 = com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload.CREATOR.createFromParcel(data);
}
else {
_arg2 = null;
}
this.emitterAddEventRealtime(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_emitterBulkAddEvents:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
java.util.List _arg1;
ClassLoader cl = (ClassLoader)this.getClass().getClassLoader();
_arg1 = data.readArrayList(cl);
java.util.List _arg2;
_arg2 = data.readArrayList(cl);
this.emitterBulkAddEvents(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_emitterFlush:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
this.emitterFlush(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_emitterUpdateConfig:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
com.meizu.statsapp.v3.lib.plugin.emitter.EmitterConfig _arg1;
if ((0!=data.readInt())) {
_arg1 = com.meizu.statsapp.v3.lib.plugin.emitter.EmitterConfig.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.emitterUpdateConfig(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_emitterGetUmid:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
String _result = this.emitterGetUmid(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_emitterUpdateEventSource:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
String _arg1;
_arg1 = data.readString();
this.emitterUpdateEventSource(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_netRequest:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
java.util.Map _arg1;
ClassLoader cl = (ClassLoader)this.getClass().getClassLoader();
_arg1 = data.readHashMap(cl);
String _arg2;
_arg2 = data.readString();
com.meizu.statsapp.v3.lib.plugin.net.NetResponse _result = this.netRequest(_arg0, _arg1, _arg2);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_setCallback:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
IVccOfflineStatsCallback _arg1;
_arg1 = IVccOfflineStatsCallback.Stub.asInterface(data.readStrongBinder());
this.setCallback(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements IVccOfflineStatsInterface
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
/////////////////emitter
//已废弃

@Override public void emitterInit(String packageName, com.meizu.statsapp.v3.lib.plugin.emitter.EmitterConfig config) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
if ((config!=null)) {
_data.writeInt(1);
config.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_emitterInit, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * @param payload the payload to be added to
     *                the EventStore
     */
@Override public void emitterAddEvent(String packageName, long eventId, com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload payload) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeLong(eventId);
if ((payload!=null)) {
_data.writeInt(1);
payload.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_emitterAddEvent, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * @param payload the payload to be added to
     *                the EventStore realtime
     */
@Override public void emitterAddEventRealtime(String packageName, long eventId, com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload payload) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeLong(eventId);
if ((payload!=null)) {
_data.writeInt(1);
payload.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_emitterAddEventRealtime, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * bulk add events
     */
@Override public void emitterBulkAddEvents(String packageName, java.util.List eventIds, java.util.List payloads) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeList(eventIds);
_data.writeList(payloads);
mRemote.transact(Stub.TRANSACTION_emitterBulkAddEvents, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * Sends everything in the database to the endpoint.
     */
@Override public void emitterFlush(String packageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
mRemote.transact(Stub.TRANSACTION_emitterFlush, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void emitterUpdateConfig(String packageName, com.meizu.statsapp.v3.lib.plugin.emitter.EmitterConfig config) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
if ((config!=null)) {
_data.writeInt(1);
config.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_emitterUpdateConfig, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public String emitterGetUmid(String packageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
mRemote.transact(Stub.TRANSACTION_emitterGetUmid, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void emitterUpdateEventSource(String sessionId, String eventSource) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(sessionId);
_data.writeString(eventSource);
mRemote.transact(Stub.TRANSACTION_emitterUpdateEventSource, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
//已废弃

@Override public com.meizu.statsapp.v3.lib.plugin.net.NetResponse netRequest(String originalUrl, java.util.Map headers, String content) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
com.meizu.statsapp.v3.lib.plugin.net.NetResponse _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(originalUrl);
_data.writeMap(headers);
_data.writeString(content);
mRemote.transact(Stub.TRANSACTION_netRequest, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = com.meizu.statsapp.v3.lib.plugin.net.NetResponse.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setCallback(String packageName, IVccOfflineStatsCallback callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
_data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_setCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_emitterInit = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_emitterAddEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_emitterAddEventRealtime = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_emitterBulkAddEvents = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_emitterFlush = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_emitterUpdateConfig = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_emitterGetUmid = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_emitterUpdateEventSource = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_netRequest = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_setCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
}
/////////////////emitter
//已废弃

public void emitterInit(String packageName, com.meizu.statsapp.v3.lib.plugin.emitter.EmitterConfig config) throws android.os.RemoteException;
/**
     * @param payload the payload to be added to
     *                the EventStore
     */
public void emitterAddEvent(String packageName, long eventId, com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload payload) throws android.os.RemoteException;
/**
     * @param payload the payload to be added to
     *                the EventStore realtime
     */
public void emitterAddEventRealtime(String packageName, long eventId, com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload payload) throws android.os.RemoteException;
/**
     * bulk add events
     */
public void emitterBulkAddEvents(String packageName, java.util.List eventIds, java.util.List payloads) throws android.os.RemoteException;
/**
     * Sends everything in the database to the endpoint.
     */
public void emitterFlush(String packageName) throws android.os.RemoteException;
public void emitterUpdateConfig(String packageName, com.meizu.statsapp.v3.lib.plugin.emitter.EmitterConfig config) throws android.os.RemoteException;
public String emitterGetUmid(String packageName) throws android.os.RemoteException;
public void emitterUpdateEventSource(String sessionId, String eventSource) throws android.os.RemoteException;
//已废弃

public com.meizu.statsapp.v3.lib.plugin.net.NetResponse netRequest(String originalUrl, java.util.Map headers, String content) throws android.os.RemoteException;
public void setCallback(String packageName, IVccOfflineStatsCallback callback) throws android.os.RemoteException;
}
