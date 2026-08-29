package com.mygeqo.companion;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.tcp.TcpConnection;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "GeqoPrinter")
public class GeqoPrinterPlugin extends Plugin {
    private static final String TAG = "GeqoPrinter";
    private static final String PREFS_NAME = "GeqoPrinterPrefs";
    private static final String KEY_TYPE = "printer_type";
    private static final String KEY_ADDRESS = "printer_address";
    private static final String KEY_PORT = "printer_port";

    // Phase 1 defaults for a standard 80mm thermal printer, Font A.
    // Untested against real hardware yet — may need tuning.
    private static final int PRINTER_DPI = 203;
    private static final float PRINTER_WIDTH_MM = 80f;
    private static final int PRINTER_CHARS_PER_LINE = 48;

    @PluginMethod
    public void pair(PluginCall call) {
        String type = call.getString("type");
        if (type == null || !type.equals("tcp")) {
            call.reject("Phase 1 only supports type: 'tcp'. Bluetooth is Phase 2, not implemented.");
            return;
        }
        String address = call.getString("address");
        Integer port = call.getInt("port", 9100);
        if (address == null || address.isEmpty()) {
            call.reject("address is required for tcp pairing");
            return;
        }

        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putString(KEY_TYPE, "tcp")
            .putString(KEY_ADDRESS, address)
            .putInt(KEY_PORT, port)
            .apply();

        JSObject ret = new JSObject();
        ret.put("paired", true);
        ret.put("type", "tcp");
        ret.put("address", address);
        ret.put("port", port);
        call.resolve(ret);
    }

    @PluginMethod
    public void getStatus(PluginCall call) {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String type = prefs.getString(KEY_TYPE, null);
        String address = prefs.getString(KEY_ADDRESS, null);
        int port = prefs.getInt(KEY_PORT, 9100);

        JSObject ret = new JSObject();
        ret.put("paired", type != null && address != null);
        if (type != null) ret.put("type", type);
        if (address != null) ret.put("address", address);
        ret.put("port", port);
        call.resolve(ret);
    }

    @PluginMethod
    public void printTicket(PluginCall call) {
        String formattedText = call.getString("formattedText");
        if (formattedText == null || formattedText.isEmpty()) {
            call.reject("formattedText is required");
            return;
        }

        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String type = prefs.getString(KEY_TYPE, null);
        String address = prefs.getString(KEY_ADDRESS, null);
        int port = prefs.getInt(KEY_PORT, 9100);

        if (type == null || address == null) {
            call.reject("No printer paired. Call pair() first.");
            return;
        }
        if (!type.equals("tcp")) {
            call.reject("Only tcp printers are supported in Phase 1.");
            return;
        }

        try {
            TcpConnection connection = new TcpConnection(address, port);
            EscPosPrinter printer = new EscPosPrinter(connection, PRINTER_DPI, PRINTER_WIDTH_MM, PRINTER_CHARS_PER_LINE);
            printer.printFormattedTextAndCut(formattedText);

            JSObject ret = new JSObject();
            ret.put("printed", true);
            call.resolve(ret);
        } catch (EscPosConnectionException | EscPosParserException | EscPosEncodingException | EscPosBarcodeException e) {
            Log.e(TAG, "Print failed", e);
            call.reject("Print failed: " + e.getMessage(), e);
        }
    }
}
