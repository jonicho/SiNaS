package de.sinas;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.sinas.net.PROTOCOL;

/**
 * A logger used to log things. Writes logs to System.out and the log file. The
 * log file is named in the following way:<br>
 * SiNaS_log_yyyy-MM-dd_HH-mm-ss-SSS.txt
 *
 */
public class Logger {
    private static File logFile;
    private static boolean isInitialized = false;

    public static void init(boolean isServer) {
        logFile = new File("SiNaS-" + (isServer ? "Server" : "Client") + "_log_"
                + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date()) + ".txt");
        isInitialized = true;
    }

    /**
     * Logs a message without an ip and port.<br>
     * Replaces PROTOCOL:SPLIT with "[SPLIT]" in the message.
     * 
     * @param msg      The message
     * @param incoming Whether the message was an incoming or outgoing one.
     */
    public static void logMessage(String msg, boolean incoming) {
        logMessage(msg, incoming, null, 0);
    }

    /**
     * Logs a message.<br>
     * Replaces PROTOCOL:SPLIT with "[SPLIT]" in the message.<br>
     * When ip is {@code null} ip and port are ignored.
     * 
     * @param msg      The message
     * @param incoming Whether the message was an incoming or outgoing one.
     * @param ip       The client's ip
     * @param port     The client's port
     */
    public static void logMessage(String msg, boolean incoming, String ip, int port) {
        if (!isInitialized) {
            throw new IllegalStateException("Logger is not yet initialized!");
        }
        String str = "";
        if (incoming) {
            str += "Got message";
            if (ip != null) {
                str += " from ";
            }
        } else {
            str += "Sent message";
            if (ip != null) {
                str += " to ";
            }
        }
        if (ip != null) {
            str += ip + ":" + port;
        }
        str += "; ";
        str += msg.replace(PROTOCOL.SPLIT, "[SPLIT]");
        log(str);
    }

    /**
     * Logs a string. Writes a line in System.out and appends a line to the log file
     * with the current time and the string in the following format:<br>
     * [yyyy.MM.dd HH:mm:ss.SSS] string
     * 
     * @param str
     */
    public static void log(String str) {
        if (!isInitialized) {
            throw new IllegalStateException("Logger is not yet initialized!");
        }
        String output = "[" + new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS").format(new Date()) + "] " + str;
        System.out.println(output);
        try {
            FileWriter fw = new FileWriter(logFile, true);
            fw.write(output + "\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}