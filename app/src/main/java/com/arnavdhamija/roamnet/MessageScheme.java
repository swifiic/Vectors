package com.arnavdhamija.roamnet;

public class MessageScheme {

    public static String parsePayloadString(String originalMsg) {
        return originalMsg.substring(4);
    }

    public static enum MessageType {
        WELCOME, JSON, FILENAME, EXTRA, FILELIST, REQUESTFILES, ERROR, DESTINATIONACK, GOODBYE;
    }

    public static String createStringType(MessageType type, String msg) {
        if (type == MessageType.WELCOME) {
            return "WLCM" + msg;
        } else if (type == MessageType.JSON) {
            return "JSON" + msg;
        } else if (type == MessageType.FILENAME) {
            return "FLNM" + msg;
        } else if (type == MessageType.EXTRA) {
            return "EXTR" + msg;
        } else if (type == MessageType.FILELIST) {
            return "FLST" + msg;
        } else if (type == MessageType.REQUESTFILES) {
            return "REQE" + msg;
        } else if (type == MessageType.DESTINATIONACK) {
            return "DACK" + msg;
        } else if (type == MessageType.GOODBYE) {
            return "GBYE";
        } else {
            return null;
        }
    }

    public static MessageType getMessageType(String originalMsg) {
        String msgHeader = originalMsg.substring(0, 4);
//        customLogger("Header " + msgHeader);
        if (msgHeader.compareTo("WLCM") == 0) {
            return MessageType.WELCOME;
        } else if (msgHeader.compareTo("JSON") == 0) {
            return MessageType.JSON;
        } else if (msgHeader.compareTo("FLNM") == 0) {
            return MessageType.FILENAME;
        } else if (msgHeader.compareTo("EXTR") == 0) {
            return MessageType.EXTRA;
        } else if (msgHeader.compareTo("FLST") == 0) {
            return MessageType.FILELIST;
        } else if (msgHeader.compareTo("REQE") == 0) {
            return MessageType.REQUESTFILES;
        } else if (msgHeader.compareTo("DACK") == 0) {
            return MessageType.DESTINATIONACK;
        } else if (msgHeader.compareTo("GBYE") == 0) {
            return MessageType.GOODBYE;
        } else {
            return MessageType.ERROR;
        }
    }
}
