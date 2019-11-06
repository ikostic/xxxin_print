
package com.auth_xxxxin.cardview;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import android.app.Activity;

import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.os.Handler;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.content.Intent;
import android.graphics.BitmapFactory;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.Promise;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.Callback;

import honeywell.connection.ConnectionBase;
import honeywell.connection.Connection_Bluetooth;
import honeywell.connection.Connection_TCP;
import honeywell.printer.DocumentDPL;
import honeywell.printer.DocumentDPL.ImageType;
import honeywell.printer.DocumentEZ;
import honeywell.printer.DocumentExPCL_LP;
import honeywell.printer.DocumentExPCL_PP;
import honeywell.printer.DocumentExPCL_PP.PaperWidth;
import honeywell.printer.DocumentLP;
import honeywell.printer.ParametersDPL;
import honeywell.printer.ParametersDPL.DoubleByteSymbolSet;
import honeywell.printer.ParametersEZ;
import honeywell.printer.ParametersExPCL_LP;
import honeywell.printer.ParametersExPCL_LP.BarcodeExPCL_LP;
import honeywell.printer.ParametersExPCL_LP.GS1DataBar;
import honeywell.printer.ParametersExPCL_PP;
import honeywell.printer.ParametersExPCL_PP.BarcodeExPCL_PP;
import honeywell.printer.ParametersExPCL_PP.RotationAngle;
import honeywell.printer.UPSMessage;
import honeywell.printer.configuration.dpl.AutoUpdate_DPL;
import honeywell.printer.configuration.dpl.AvalancheEnabler_DPL;
import honeywell.printer.configuration.dpl.BluetoothConfiguration_DPL;
import honeywell.printer.configuration.dpl.Fonts_DPL;
import honeywell.printer.configuration.dpl.MediaLabel_DPL;
import honeywell.printer.configuration.dpl.MemoryModules_DPL.FileInformation;
import honeywell.printer.configuration.dpl.Miscellaneous_DPL;
import honeywell.printer.configuration.dpl.NetworkGeneralSettings_DPL;
import honeywell.printer.configuration.dpl.NetworkWirelessSettings_DPL;
import honeywell.printer.configuration.dpl.PrintSettings_DPL;
import honeywell.printer.configuration.dpl.PrinterInformation_DPL;
import honeywell.printer.configuration.dpl.PrinterStatus_DPL;
import honeywell.printer.configuration.dpl.PrinterStatus_DPL.PrinterStatus;
import honeywell.printer.configuration.dpl.SensorCalibration_DPL;
import honeywell.printer.configuration.dpl.SerialPortConfiguration_DPL;
import honeywell.printer.configuration.dpl.SystemSettings_DPL;
import honeywell.printer.configuration.expcl.BatteryCondition_ExPCL;
import honeywell.printer.configuration.expcl.BluetoothConfiguration_ExPCL;
import honeywell.printer.configuration.expcl.GeneralStatus_ExPCL;
import honeywell.printer.configuration.expcl.MagneticCardData_ExPCL;
import honeywell.printer.configuration.expcl.MemoryStatus_ExPCL;
import honeywell.printer.configuration.expcl.PrinterOptions_ExPCL;
import honeywell.printer.configuration.expcl.PrintheadStatus_ExPCL;
import honeywell.printer.configuration.expcl.VersionInformation_ExPCL;
import honeywell.printer.configuration.ez.AvalancheSettings;
import honeywell.printer.configuration.ez.BatteryCondition;
import honeywell.printer.configuration.ez.BluetoothConfiguration;
import honeywell.printer.configuration.ez.FontData;
import honeywell.printer.configuration.ez.FontList;
import honeywell.printer.configuration.ez.FormatData;
import honeywell.printer.configuration.ez.FormatList;
import honeywell.printer.configuration.ez.GeneralConfiguration;
import honeywell.printer.configuration.ez.GeneralStatus;
import honeywell.printer.configuration.ez.GraphicData;
import honeywell.printer.configuration.ez.GraphicList;
import honeywell.printer.configuration.ez.IrDAConfiguration;
import honeywell.printer.configuration.ez.LabelConfiguration;
import honeywell.printer.configuration.ez.MagneticCardConfiguration;
import honeywell.printer.configuration.ez.MagneticCardData;
import honeywell.printer.configuration.ez.ManufacturingDate;
import honeywell.printer.configuration.ez.MemoryStatus;
import honeywell.printer.configuration.ez.PrinterOptions;
import honeywell.printer.configuration.ez.PrintheadStatus;
import honeywell.printer.configuration.ez.SerialNumber;
import honeywell.printer.configuration.ez.SmartCardConfiguration;
import honeywell.printer.configuration.ez.TCPIPStatus;
import honeywell.printer.configuration.ez.UpgradeData;
import honeywell.printer.configuration.ez.VersionInformation;

public class RNCardviewModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private ConnectionBase conn;
    DocumentDPL docDPL = new DocumentDPL();
    int bytesWritten = 0;
    int bytesToWrite = 1024;
    ParametersDPL paramDPL= new ParametersDPL();
    byte[] printData = {0};

    public RNCardviewModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @ReactMethod
    public void connectBlue(String uuid) {
        try {
            conn = Connection_Bluetooth.createClient(uuid);
            if(!conn.getIsOpen()){
                conn.open();
                PrintSettings_DPL printSettings = new PrintSettings_DPL(conn);
                MediaLabel_DPL mediaLabel = new MediaLabel_DPL(conn);
                mediaLabel.setLabelWidth(412);
                mediaLabel.setContinuousLabelLength(600);
                mediaLabel.setMaxLabelLength(1000);
                mediaLabel.setPaperEmptyDistance(25);
                SystemSettings_DPL sysSettings = new SystemSettings_DPL(conn);
                sysSettings.setLabelRotation(SystemSettings_DPL.LabelRotationValue.Rotate_180);
                //sysSettings.update(1000);
                //sysSettings.queryPrinter(1000);
                Activity currentActivity = getCurrentActivity();
                //Toast.makeText(currentActivity, "Connected printer " + printSettings.getColumnOffset(), Toast.LENGTH_LONG).show();
                //Toast.makeText(currentActivity, "Connected printer " + sysSettings.getLabelRotation().name(), Toast.LENGTH_LONG).show();
                Toast.makeText(currentActivity, "Connected printer", Toast.LENGTH_LONG).show();
            }
        } catch(Exception e){
            // System.out.println("Wrong!");//
            Activity currentActivity = getCurrentActivity();
            Toast.makeText(currentActivity, "Printer connection error " + e, Toast.LENGTH_LONG).show();
        }
    }

    @ReactMethod
    public void printText(String text, int fontID, int row, int col, int fontHeight, int fontWidth) {
        try {
            try {
                docDPL.setEnableAdvanceFormatAttribute(true);
                paramDPL.setIsUnicode(true);
                paramDPL.setDBSymbolSet(DoubleByteSymbolSet.Unicode);

                paramDPL.setFontHeight(fontHeight);
                paramDPL.setFontWidth(fontWidth);

                paramDPL.setIsBold(false);
                paramDPL.setIsItalic(false);
                paramDPL.setIsUnderline(false);

                //paramDPL.setIsMirrored(true);

                //paramDPL.setRotate(ParametersDPL.Rotation.Rotate_180);

                //docDPL.writeTextScalable(text, fontID, row, col, paramDPL);
                //docDPL.writeText(text, row, col, fontID, paramDPL);
                docDPL.writeTextInternalBitmapped(text, fontID, row, col, paramDPL);
                printData = docDPL.getDocumentData();
            } catch (Exception e) {
                Activity currentActivity = getCurrentActivity();
                Toast.makeText(currentActivity, "Printer printLine error " + e, Toast.LENGTH_LONG).show();
            } 
        } catch (Exception e) {
            Activity currentActivity = getCurrentActivity();
            Toast.makeText(currentActivity, "Printer printLine error " + e, Toast.LENGTH_LONG).show();
        }
    }

    @ReactMethod
    public void qrCode(String text, String fontID, int row, int col) {
        try{
            try{
                paramDPL.setIsUnicode(false);
                paramDPL.setWideBarWidth(4);
                paramDPL.setNarrowBarWidth(4);
                paramDPL.setSymbolHeight(0);
                //AutoFormatting
                docDPL.writeBarCodeQRCode(text, true, 0, "", "", "", "", row, col, paramDPL);
                // docDPL.writeTextInternalBitmapped("QR Barcode w/ Auto Formatting", 1, 1030, 0);
                printData = docDPL.getDocumentData();
            } catch (Exception e) {
                Activity currentActivity = getCurrentActivity();
                Toast.makeText(currentActivity, "Printer qrCode error "+e, Toast.LENGTH_LONG).show();
            } 
        } catch (Exception e) {
            Activity currentActivity = getCurrentActivity();
            Toast.makeText(currentActivity, "Printer qrCode error "+e, Toast.LENGTH_LONG).show();
        }
    }

    @ReactMethod
    public void barCode(String barcodeID, String text, int row, int col) {
        try{
            try{
                paramDPL.setIsUnicode(false);
                paramDPL.setWideBarWidth(3);
                paramDPL.setNarrowBarWidth(1);
                paramDPL.setSymbolHeight(8);
                //AutoFormatting
                docDPL.writeBarCode(barcodeID, text, row, col);
                // docDPL.writeTextInternalBitmapped("QR Barcode w/ Auto Formatting", 1, 1030, 0);
                printData = docDPL.getDocumentData();
            } catch (Exception e) {
                Activity currentActivity = getCurrentActivity();
                Toast.makeText(currentActivity, "Printer qrCode error "+e, Toast.LENGTH_LONG).show();
            } 
        } catch (Exception e) {
            Activity currentActivity = getCurrentActivity();
            Toast.makeText(currentActivity, "Printer qrCode error "+e, Toast.LENGTH_LONG).show();
        }
    }

    @ReactMethod
    public void printLine(int row, int col, int height, int width) {
        try {
            try {
                //writeLine
                docDPL.writeLine(row, col, height, width);
                printData = docDPL.getDocumentData();
            } catch (Exception e) {
                Activity currentActivity = getCurrentActivity();
                Toast.makeText(currentActivity, "Printer printLine error " + e, Toast.LENGTH_LONG).show();
            } 
        } catch (Exception e) {
            Activity currentActivity = getCurrentActivity();
            Toast.makeText(currentActivity, "Printer printLine error " + e, Toast.LENGTH_LONG).show();
        }
    }

    @ReactMethod
    public void print() {
        try {
            if(conn.getIsOpen()) {
                int bytesWritten = 0;
                int bytesToWrite = 1024;
                int totalBytes = printData.length;
                int remainingBytes = totalBytes;
                while (bytesWritten < totalBytes) {
                    if (remainingBytes < bytesToWrite)
                        bytesToWrite = remainingBytes;

                    //Send data, 1024 bytes at a time until all data sent
                    conn.write(printData, bytesWritten, bytesToWrite);
                    bytesWritten += bytesToWrite;
                    remainingBytes = remainingBytes - bytesToWrite;
                    Thread.sleep(100);
                }
            }
        } catch (Exception e) {
            Activity currentActivity = getCurrentActivity();
            Toast.makeText(currentActivity, "Printer print error " + e, Toast.LENGTH_LONG).show();
        }
    }

    @ReactMethod
    public void clearDPL(){
        docDPL.clear();
    }

    @ReactMethod
    public void closeBlue(String uuid) {
        try {
            if(conn.getIsOpen()){
                conn.close();
                Activity currentActivity = getCurrentActivity();
                Toast.makeText(currentActivity, "Disconnected printer", Toast.LENGTH_LONG).show();
            }
        } catch(Exception e){
            Activity currentActivity = getCurrentActivity();
            Toast.makeText(currentActivity, "Disconnected printer error " + e, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public String getName() {
        return "RNCardview";
    }
}
