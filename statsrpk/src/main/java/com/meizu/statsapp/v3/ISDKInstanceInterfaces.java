/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/flyme/NewFlyme/Code/MEIZU_Apps_Lib_Publish_Artifactory_2944/MzAnalyticSdk/sdk/src/main/aidl/com/meizu/statsapp/v3/ISDKInstanceInterfaces.aidl
 */
package com.meizu.statsapp.v3;
// Declare any non-default types here with import statements

public interface ISDKInstanceInterfaces extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements ISDKInstanceInterfaces
{
private static final String DESCRIPTOR = "com.meizu.statsapp.v3.ISDKInstanceInterfaces";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.meizu.statsapp.v3.ISDKInstanceInterfaces interface,
 * generating a proxy if needed.
 */
public static ISDKInstanceInterfaces asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof ISDKInstanceInterfaces))) {
return ((ISDKInstanceInterfaces)iin);
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
case TRANSACTION_onEvent:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
String _arg1;
_arg1 = data.readString();
java.util.Map _arg2;
ClassLoader cl = (ClassLoader)this.getClass().getClassLoader();
_arg2 = data.readHashMap(cl);
this.onEvent(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_onEventRealtime:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
String _arg1;
_arg1 = data.readString();
java.util.Map _arg2;
ClassLoader cl = (ClassLoader)this.getClass().getClassLoader();
_arg2 = data.readHashMap(cl);
this.onEventRealtime(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_onEventNeartime:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
String _arg1;
_arg1 = data.readString();
java.util.Map _arg2;
ClassLoader cl = (ClassLoader)this.getClass().getClassLoader();
_arg2 = data.readHashMap(cl);
this.onEventNeartime(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_onEventLib:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
String _arg1;
_arg1 = data.readString();
java.util.Map _arg2;
ClassLoader cl = (ClassLoader)this.getClass().getClassLoader();
_arg2 = data.readHashMap(cl);
String _arg3;
_arg3 = data.readString();
this.onEventLib(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
return true;
}
case TRANSACTION_onEventRealtimeLib:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
String _arg1;
_arg1 = data.readString();
java.util.Map _arg2;
ClassLoader cl = (ClassLoader)this.getClass().getClassLoader();
_arg2 = data.readHashMap(cl);
String _arg3;
_arg3 = data.readString();
this.onEventRealtimeLib(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
return true;
}
case TRANSACTION_onLog:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
java.util.Map _arg1;
ClassLoader cl = (ClassLoader)this.getClass().getClassLoader();
_arg1 = data.readHashMap(cl);
this.onLog(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_onLogRealtime:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
java.util.Map _arg1;
ClassLoader cl = (ClassLoader)this.getClass().getClassLoader();
_arg1 = data.readHashMap(cl);
this.onLogRealtime(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_onBackgroundUse:
{
data.enforceInterface(DESCRIPTOR);
long _arg0;
_arg0 = data.readLong();
long _arg1;
_arg1 = data.readLong();
long _arg2;
_arg2 = data.readLong();
this.onBackgroundUse(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_onPageStart:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
this.onPageStart(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onPageStop:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
this.onPageStop(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setAttributes:
{
data.enforceInterface(DESCRIPTOR);
java.util.Map _arg0;
ClassLoader cl = (ClassLoader)this.getClass().getClassLoader();
_arg0 = data.readHashMap(cl);
this.setAttributes(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setBulkLimit:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.setBulkLimit(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setSource:
{
data.enforceInterface(DESCRIPTOR);
String _arg0;
_arg0 = data.readString();
this.setSource(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getSource:
{
data.enforceInterface(DESCRIPTOR);
String _result = this.getSource();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_setActive:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.setActive(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setDebug:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.setDebug(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_isActive:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isActive();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isDebug:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isDebug();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getSessionId:
{
data.enforceInterface(DESCRIPTOR);
String _result = this.getSessionId();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getUMID:
{
data.enforceInterface(DESCRIPTOR);
String _result = this.getUMID();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getFlymeUID:
{
data.enforceInterface(DESCRIPTOR);
String _result = this.getFlymeUID();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_onForeground:
{
data.enforceInterface(DESCRIPTOR);
this.onForeground();
reply.writeNoException();
return true;
}
case TRANSACTION_onBackground:
{
data.enforceInterface(DESCRIPTOR);
this.onBackground();
reply.writeNoException();
return true;
}
case TRANSACTION_getSdkVersion:
{
data.enforceInterface(DESCRIPTOR);
String _result = this.getSdkVersion();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_checkPluginUpdate:
{
data.enforceInterface(DESCRIPTOR);
this.checkPluginUpdate();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements ISDKInstanceInterfaces
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
     * 记录一个事件。
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
@Override public void onEvent(String eventName, String pageName, java.util.Map properties) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(eventName);
_data.writeString(pageName);
_data.writeMap(properties);
mRemote.transact(Stub.TRANSACTION_onEvent, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 记录一个事件，并立即上报
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
@Override public void onEventRealtime(String eventName, String pageName, java.util.Map properties) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(eventName);
_data.writeString(pageName);
_data.writeMap(properties);
mRemote.transact(Stub.TRANSACTION_onEventRealtime, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 记录一个事件，缓存几秒后立即上报
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
@Override public void onEventNeartime(String eventName, String pageName, java.util.Map properties) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(eventName);
_data.writeString(pageName);
_data.writeMap(properties);
mRemote.transact(Stub.TRANSACTION_onEventNeartime, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 记录一个引用库事件。
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
@Override public void onEventLib(String eventName, String pageName, java.util.Map properties, String libPackageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(eventName);
_data.writeString(pageName);
_data.writeMap(properties);
_data.writeString(libPackageName);
mRemote.transact(Stub.TRANSACTION_onEventLib, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 记录一个引用库事件，并立即上报
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
@Override public void onEventRealtimeLib(String eventName, String pageName, java.util.Map properties, String libPackageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(eventName);
_data.writeString(pageName);
_data.writeMap(properties);
_data.writeString(libPackageName);
mRemote.transact(Stub.TRANSACTION_onEventRealtimeLib, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 记录日志。
     *
     * @param logName    日志名称
     * @param properties 日志属性
     */
@Override public void onLog(String logName, java.util.Map properties) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(logName);
_data.writeMap(properties);
mRemote.transact(Stub.TRANSACTION_onLog, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 记录日志。
     *
     * @param logName    日志名称
     * @param properties 日志属性
     */
@Override public void onLogRealtime(String logName, java.util.Map properties) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(logName);
_data.writeMap(properties);
mRemote.transact(Stub.TRANSACTION_onLogRealtime, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
    *  记录一个后台使用时长
    *
    */
@Override public void onBackgroundUse(long startTime, long endTime, long duration) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeLong(startTime);
_data.writeLong(endTime);
_data.writeLong(duration);
mRemote.transact(Stub.TRANSACTION_onBackgroundUse, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 跟踪页面启动。
     *
     * @param pageName 页面名称，不能为空
     */
@Override public void onPageStart(String pageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pageName);
mRemote.transact(Stub.TRANSACTION_onPageStart, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * 跟踪页面退出。
     *
     * @param pageName 页面名称，不能为空
     */
@Override public void onPageStop(String pageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pageName);
mRemote.transact(Stub.TRANSACTION_onPageStop, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
    * 增加公共字段event_attrib
    */
@Override public void setAttributes(java.util.Map attributes) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeMap(attributes);
mRemote.transact(Stub.TRANSACTION_setAttributes, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void setBulkLimit(int bulkLimit) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(bulkLimit);
mRemote.transact(Stub.TRANSACTION_setBulkLimit, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void setSource(String source) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(source);
mRemote.transact(Stub.TRANSACTION_setSource, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public String getSource() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSource, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * 设置是否激活，激活状态上传数据。
     *
     * @param active true上传数据，false不上传数据
     */
@Override public void setActive(boolean active) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((active)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setActive, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * set debug mode
     *
     * @param debug is debug mode
     */
@Override public void setDebug(boolean debug) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((debug)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setDebug, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public boolean isActive() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isActive, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isDebug() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isDebug, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * get SessionId
     *
     * @return SessionId
     */
@Override public String getSessionId() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSessionId, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * get UMID
     *
     * @return UMID
     */
@Override public String getUMID() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getUMID, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * get flymeUID
     *
     * @return flymeUID
     */
@Override public String getFlymeUID() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getFlymeUID, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void onForeground() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onForeground, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onBackground() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onBackground, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public String getSdkVersion() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSdkVersion, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void checkPluginUpdate() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_checkPluginUpdate, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onEventRealtime = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_onEventNeartime = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_onEventLib = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_onEventRealtimeLib = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_onLog = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_onLogRealtime = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_onBackgroundUse = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_onPageStart = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_onPageStop = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_setAttributes = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_setBulkLimit = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_setSource = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_getSource = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_setActive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_setDebug = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_isActive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_isDebug = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_getSessionId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_getUMID = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
static final int TRANSACTION_getFlymeUID = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
static final int TRANSACTION_onForeground = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
static final int TRANSACTION_onBackground = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
static final int TRANSACTION_getSdkVersion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
static final int TRANSACTION_checkPluginUpdate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
}
/**
     * 记录一个事件。
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
public void onEvent(String eventName, String pageName, java.util.Map properties) throws android.os.RemoteException;
/**
     * 记录一个事件，并立即上报
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
public void onEventRealtime(String eventName, String pageName, java.util.Map properties) throws android.os.RemoteException;
/**
     * 记录一个事件，缓存几秒后立即上报
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
public void onEventNeartime(String eventName, String pageName, java.util.Map properties) throws android.os.RemoteException;
/**
     * 记录一个引用库事件。
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
public void onEventLib(String eventName, String pageName, java.util.Map properties, String libPackageName) throws android.os.RemoteException;
/**
     * 记录一个引用库事件，并立即上报
     *
     * @param eventName  事件名称
     * @param pageName   事件发生的页面，可以为空
     * @param properties 事件的属性，可以为空
     */
public void onEventRealtimeLib(String eventName, String pageName, java.util.Map properties, String libPackageName) throws android.os.RemoteException;
/**
     * 记录日志。
     *
     * @param logName    日志名称
     * @param properties 日志属性
     */
public void onLog(String logName, java.util.Map properties) throws android.os.RemoteException;
/**
     * 记录日志。
     *
     * @param logName    日志名称
     * @param properties 日志属性
     */
public void onLogRealtime(String logName, java.util.Map properties) throws android.os.RemoteException;
/**
    *  记录一个后台使用时长
    *
    */
public void onBackgroundUse(long startTime, long endTime, long duration) throws android.os.RemoteException;
/**
     * 跟踪页面启动。
     *
     * @param pageName 页面名称，不能为空
     */
public void onPageStart(String pageName) throws android.os.RemoteException;
/**
     * 跟踪页面退出。
     *
     * @param pageName 页面名称，不能为空
     */
public void onPageStop(String pageName) throws android.os.RemoteException;
/**
    * 增加公共字段event_attrib
    */
public void setAttributes(java.util.Map attributes) throws android.os.RemoteException;
public void setBulkLimit(int bulkLimit) throws android.os.RemoteException;
public void setSource(String source) throws android.os.RemoteException;
public String getSource() throws android.os.RemoteException;
/**
     * 设置是否激活，激活状态上传数据。
     *
     * @param active true上传数据，false不上传数据
     */
public void setActive(boolean active) throws android.os.RemoteException;
/**
     * set debug mode
     *
     * @param debug is debug mode
     */
public void setDebug(boolean debug) throws android.os.RemoteException;
public boolean isActive() throws android.os.RemoteException;
public boolean isDebug() throws android.os.RemoteException;
/**
     * get SessionId
     *
     * @return SessionId
     */
public String getSessionId() throws android.os.RemoteException;
/**
     * get UMID
     *
     * @return UMID
     */
public String getUMID() throws android.os.RemoteException;
/**
     * get flymeUID
     *
     * @return flymeUID
     */
public String getFlymeUID() throws android.os.RemoteException;
public void onForeground() throws android.os.RemoteException;
public void onBackground() throws android.os.RemoteException;
public String getSdkVersion() throws android.os.RemoteException;
public void checkPluginUpdate() throws android.os.RemoteException;
}
