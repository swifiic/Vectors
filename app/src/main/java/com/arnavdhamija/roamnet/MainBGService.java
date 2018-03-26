package com.arnavdhamija.roamnet;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.jaredrummler.android.device.DeviceName;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/***
 * Abhishek Thakur : coding the background service with Binder
 * Ref: https://developer.android.com/guide/components/bound-services.html
 */
public class MainBGService extends IntentService {

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    private ConnectionsClient mConnectionClient;
    private NotificationManager mNotificationManager;
    private String connectedEndpoint;
    private String startTime;
    private boolean extraChecks = false;
    SharedPreferences mSharedPreferences;
    private FileModule mFileModule;

    enum MessageType {
        WELCOME, JSON, FILENAME, EXTRA, FILELIST, REQUESTFILES, ERROR, DESTINATIONACK;
    }

    final String TAG = "RoamnetSvc";


    String deviceId = "NOT-INITIALIZED";

    private final SimpleArrayMap<Long, NotificationCompat.Builder> incomingPayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, NotificationCompat.Builder> outgoingPayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, Payload> incomingPayloadReferences = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, String> filePayloadFilenames = new SimpleArrayMap<>();
    private List<VideoData> incomingTransfersMetadata = new ArrayList<>();
    private SimpleArrayMap<Long, VideoData> outgoingTransfersMetadata = new SimpleArrayMap<>();


    public class LocalBinder extends Binder {
        MainBGService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MainBGService.this;
        }
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean enableBackgroundService() {
        return mSharedPreferences.getBoolean(Constants.STATUS_ENABLE_BG_SERVICE, false);
    }

    public String getStartTime() {
        return startTime;
    }

    public int getFileListSize() {
        return mFileModule.getFilesCount();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    void initConnectionAndNotif(){
        if(null == mConnectionClient) {
            mConnectionClient = Nearby.getConnectionsClient(RoamNetApp.getContext());
        }
        if(null == mNotificationManager) {
            mNotificationManager = (NotificationManager) RoamNetApp.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId){
        initConnectionAndNotif();
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

        initConnectionAndNotif();
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();

        // Do work here, Nothing for now

    }


    public MainBGService() {
    // default Constructor
        super("DemoWorkerName");
        mFileModule = new FileModule(this);
        deviceId = "Roamnet_" + DeviceName.getDeviceName();
        customLogger(deviceId);
        initConnectionAndNotif();
        mSharedPreferences = RoamNetApp.getContext().getSharedPreferences(Constants.APP_KEY, Context.MODE_PRIVATE);
        startTime = new SimpleDateFormat("HH.mm.ss").format(new Date());
//        add check for sharedpref check
        if (enableBackgroundService()) {
            startAdvertising();
            startDiscovery();
        }
    }

    public MainBGService(String workerName) {
        super(workerName);
        mFileModule = new FileModule(this);
        deviceId = "Roamnet_" + DeviceName.getDeviceName();
        customLogger(deviceId);
        initConnectionAndNotif();
        startTime = new SimpleDateFormat("HH.mm.ss").format(new Date());
    }

    private void customLogger(String msg) {
        Log.d(TAG, msg);
        String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new Date());
        String logMsg = timeStamp+' '+msg+"\n";
        Intent localIntent =  new Intent(Constants.BROADCAST_ACTION)
                        .putExtra(Constants.LOG_STATUS, logMsg);

        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(RoamNetApp.getContext()).sendBroadcast(localIntent);
    }

    private void sendConnectionStatus(String msg){

        Intent localIntent =  new Intent(Constants.BROADCAST_ACTION)
                .putExtra(Constants.CONNECTION_STATUS, msg);

        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

    }

    private void restartNearby() {
        customLogger("RestartingNearby");
        mConnectionClient.stopAdvertising();
        mConnectionClient.stopDiscovery();
        startAdvertising();
        startDiscovery();
    }

    private NotificationCompat.Builder buildNotification(Payload payload, boolean isIncoming) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this).setContentTitle(isIncoming ? "Receiving..." : "Sending...").setSmallIcon(R.drawable.common_full_open_on_phone);
        long size = payload.asFile().getSize();
        boolean indeterminate = false;
        if (size == -1) {
            // This is a stream payload, so we don't know the size ahead of time.
            size = 100;
            indeterminate = true;
        }
        notification.setProgress((int)size, 0, indeterminate);
        return notification;
    }


    /******** wrappers for mConnection based on UI trigger *******/
    protected void startAdvertising() {
        mConnectionClient.startAdvertising(
                deviceId,
                getPackageName(),
                mConnectionLifecycleCallback,
                new AdvertisingOptions(Strategy.P2P_CLUSTER))
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                customLogger("Advertising Go!");
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                customLogger("Advert fail");
                                // We were unable to start advertising.
                            }
                        });
    }

    protected void startDiscovery() {
        mConnectionClient.startDiscovery(
                getPackageName(),
                mEndpointDiscoveryCallback,
                new DiscoveryOptions(Strategy.P2P_CLUSTER))
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                customLogger("Discovery go!");
                                // We're discovering!
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // We were unable to start discovering.
                            }
                        });
    }

    protected void stopDiscovery() {
        mConnectionClient.stopDiscovery();
    }

    protected void stopAdvertising() {
        mConnectionClient.stopAdvertising();

    }

    protected void stopAllEndpoints() {
        mConnectionClient.stopAllEndpoints();

    }


    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(
                        String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
                    customLogger("FOUND ENDPOINT: " + endpointId + "Info " + discoveredEndpointInfo.getEndpointName() + " id " + discoveredEndpointInfo.getServiceId());
                    if (discoveredEndpointInfo.getEndpointName().startsWith("Roamnet")) {
                        mConnectionClient.stopAdvertising();
                        mConnectionClient.stopDiscovery();
                        customLogger("Stopping before requesting Conn");
                        mConnectionClient.requestConnection(
                                deviceId,
                                endpointId,
                                mConnectionLifecycleCallback).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                customLogger("requesting conn");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                customLogger("fail conn t_t" + e.getMessage());
                                startAdvertising();
                                startDiscovery();
                            }
                        });
                    }
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    customLogger("lost ENDPOINT: " + endpointId);
                }
            };

    String createStringType(MessageType type, String msg) {
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
        } else {
            return null;
        }
    }

    MessageType getMessageType(String originalMsg) {
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
        } else {
            return MessageType.ERROR;
        }
    }

    String parsePayloadString(String originalMsg) {
        return originalMsg.substring(4);
    }

    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                String endpointName;
                @Override
                public void onConnectionInitiated(
                        String endpointId, ConnectionInfo connectionInfo) {
                    // Automatically accept the connection on both sides.
                    endpointName = connectionInfo.getEndpointName();
                    if (endpointName.startsWith("Roamnet")) {
                        customLogger("Connection initated w/ " + endpointName);
                        mConnectionClient.acceptConnection(endpointId, mPayloadCallback);
                    }
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            Toast.makeText(getApplicationContext(), "Connection Established", Toast.LENGTH_LONG).show();
                            sendConnectionStatus("Connected To: " + endpointId);
                            connectedEndpoint = endpointId;
                            sendFileList();
                            sendDestinationAck();
                            mConnectionClient.stopAdvertising();
                            mConnectionClient.stopDiscovery();
                            break;
                        case ConnectionsStatusCodes.STATUS_ENDPOINT_IO_ERROR: //this code is ignored
                            customLogger("endpt error, restart");
                            restartNearby();
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // The connection was rejected by one or both sides.
                            customLogger("fail D:");
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            customLogger("bigfail");
                            // The connection broke before it was able to be accepted.
                            break;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                    customLogger("connection terminated, find a way to autostart");
                    sendConnectionStatus("Disconnected");
                    restartNearby();
                }
            };

    private void addPayloadFilename(String payloadFilenameMessage) {
        int colonIndex = payloadFilenameMessage.indexOf(':');
        String payloadId = payloadFilenameMessage.substring(0, colonIndex);
        String filename = payloadFilenameMessage.substring(colonIndex + 1);
        filePayloadFilenames.put(Long.valueOf(payloadId), filename);
    }

    private void sendWelcomeMessage() {
        String welcome = "welcome2beconnected from  to " + connectedEndpoint;
        welcome = createStringType(MessageType.WELCOME, welcome);
        mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(welcome.getBytes(UTF_8)));
    }

    private void sendFile(String filename) {
        ParcelFileDescriptor pfd = mFileModule.getPfd(filename);
        if (pfd == null) {
            customLogger("File not found - lite");
            return;
        }
        Payload filePayload = Payload.fromFile(pfd);
        String payloadFilenameMessage = filePayload.getId() + ":" + filename;

        NotificationCompat.Builder notification = buildNotification(filePayload, false);
        mNotificationManager.notify((int)filePayload.getId(), notification.build());
        outgoingPayloads.put(Long.valueOf(filePayload.getId()), notification);
        mConnectionClient.sendPayload(connectedEndpoint, filePayload);

    }

    private void sendFile(VideoData data) {
        ParcelFileDescriptor pfd = mFileModule.getPfd(data.getFileName());
        if (pfd == null) {
            customLogger("File not found - lite");
            return;
        }
        Payload filePayload = Payload.fromFile(pfd);

        String payloadFilenameMessage = filePayload.getId() + ":" + data.getFileName();
        sendFile(connectedEndpoint, filePayload, payloadFilenameMessage, data);
    }
    // remove endpoint id from here?!
    private void sendFile(String endpointId, Payload payload, String payloadFilenameMsg, VideoData data) {
        NotificationCompat.Builder notification = buildNotification(payload, false);
        mNotificationManager.notify((int)payload.getId(), notification.build());
        outgoingPayloads.put(Long.valueOf(payload.getId()), notification);

        // now we send the JSON metadata mapped by the payload ID
        String videoDataJSON = data.toString();
        videoDataJSON = createStringType(MessageType.JSON, videoDataJSON);
        mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(videoDataJSON.getBytes(UTF_8)));
        outgoingTransfersMetadata.put(Long.valueOf(payload.getId()), data);

        try {
            payloadFilenameMsg = createStringType(MessageType.FILENAME, payloadFilenameMsg);
            customLogger("Sending a file - filename is : " + payloadFilenameMsg);
            mConnectionClient.sendPayload(endpointId, Payload.fromBytes(payloadFilenameMsg.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            customLogger("encode fail");
        }
        mConnectionClient.sendPayload(endpointId, payload);
    }

    private void sendFileList() {
        String fileList = mFileModule.getFileList();
        fileList = createStringType(MessageType.FILELIST, fileList);
        mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(fileList.getBytes(UTF_8)));
    }

    private void sendDestinationAck() {
        DestinationAck ack = mFileModule.getAckFromFile();
        if (ack != null) {
            String dackMsg = ack.toString();
            dackMsg = createStringType(MessageType.DESTINATIONACK, dackMsg);
            mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(dackMsg.getBytes(UTF_8)));
        }
    }

    private void processJSONMsg(String parseMsg) {
        VideoData vd = new VideoData();
        vd = VideoData.fromString(parseMsg);
        // this list has to be managed, when we start getting files of course
        incomingTransfersMetadata.add(vd);
    }

    private void processDackJSON(String parseMsg) {
        DestinationAck incomingAck = DestinationAck.fromString(parseMsg);
        DestinationAck currentAck = mFileModule.getAckFromFile();
        if (currentAck == null) {
            mFileModule.writeToJSONFile(incomingAck);
        } else {
            if (incomingAck.getTimestamp() > currentAck.getTimestamp()) {
                customLogger("Newer ack received, writing back to file");
                mFileModule.writeToJSONFile(incomingAck);
            }
        }
    }

    private void processRequestFiles(String filelist) {
        DestinationAck dack = mFileModule.getAckFromFile();
        List<String> requestedFiles = Arrays.asList(filelist.split(","));
        List<VideoData> requestedVideoDatas = new ArrayList<>();
        List<String> otherFileTypes = new ArrayList<>();
        for (int i = 0; i < requestedFiles.size(); i++) {
            if (requestedFiles.get(i).startsWith("video")) {
                requestedVideoDatas.add(mFileModule.getVideoDataFromFile(requestedFiles.get(i)));
            } else {
                otherFileTypes.add(requestedFiles.get(i));
            }
        }

        // sort by tickets and send in that order
        Collections.sort(requestedVideoDatas, new Comparator<VideoData>() {
            @Override
            public int compare(VideoData o1, VideoData o2) {
                if (o1.getTickets() > o2.getTickets()) { // test if this is descending order
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        for (VideoData vd : requestedVideoDatas) {
            if (vd != null) {
                if (vd.getTickets() > 1) {
                    vd.setTickets(vd.getTickets() / 2); // SNW strategy allows us to only send half
                    vd.addTraversedNode(deviceId);
                    //send JSON and file
                    boolean sendFile = true;
                    if (extraChecks && (vd.getCreationTime() + vd.getTtl() < System.currentTimeMillis()/1000
                            || dack.getAckedFiles().contains(vd.getFileName()))) {
                        sendFile = false;
                    }
                    if (sendFile) {
                        sendFile(vd);
                    }
                }
            }
        }

        // for metadata files
        for (String filename : otherFileTypes) {
            sendFile(filename);
        }
    }

    private void processFileList(String filelist) {
        customLogger("Rcvd a filelist of " + filelist);
        List<String> rcvdFilenames = Arrays.asList(filelist.split(","));
        List<String> currFilenames = Arrays.asList(mFileModule.getFileList().split(","));
        List<String> requestFilenames = new ArrayList<>();

        // This code is very bad, but it's the only way not to get a NPE :P
        for (int i = 0; i < rcvdFilenames.size(); i++) {
            boolean includeFile = true;
            for (int j = 0; j < currFilenames.size(); j++) {
                if (rcvdFilenames.get(i).compareTo(currFilenames.get(j)) == 0) {
                    includeFile = false;
                }
            }
            if (includeFile) {
                requestFilenames.add(rcvdFilenames.get(i));
            }
        }
        String requestFilesCSV = convertListToCSV(requestFilenames);
        customLogger("We want the files of " + requestFilesCSV);
        // we send the files we want to get here
        requestFilesCSV = createStringType(MessageType.REQUESTFILES, requestFilesCSV);
        mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(requestFilesCSV.getBytes(UTF_8)));
    }

    private String convertListToCSV(List<String> files) {
        String fileName;
        StringBuilder csvFileList = new StringBuilder();
        for (int i = 0; i < files.size(); i++) {
            fileName = files.get(i);
            if (!fileName.contains(".json")) {
                if (i > 0) {
                    csvFileList.append(",");
                }
                csvFileList.append(fileName);
            }
        }
        return csvFileList.toString();
    }

    private final PayloadCallback mPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    if (payload.getType() == Payload.Type.BYTES) {
                        try {
                            String payloadMsg = new String(payload.asBytes(), "UTF-8");
                            customLogger("Getting a byte pyalod " + payloadMsg);
                            MessageType type = getMessageType(payloadMsg);
                            String parsedMsg = parsePayloadString(payloadMsg);
                            if (parsedMsg == null) {
                                customLogger("We got a null string?!?!?!");
                            }
//                            customLogger("MSG TYpeVAL" + type);
                            if (type == MessageType.WELCOME) {
                                customLogger("Got a welcome MSG! " + parsedMsg);
                            } else if (type == MessageType.JSON) {
                                processJSONMsg(parsedMsg);
                            } else if (type == MessageType.FILENAME) {
                                addPayloadFilename(parsedMsg);
                            } else if (type == MessageType.EXTRA) {
                                customLogger("Got an extra msg!" + parsedMsg);
                            } else if (type == MessageType.FILELIST) {
                                processFileList(parsedMsg);
                            } else if (type == MessageType.REQUESTFILES) {
                                processRequestFiles(parsedMsg);
                            } else if (type == MessageType.DESTINATIONACK) {
                                processDackJSON(parsedMsg);
                            } else {
                                    customLogger(" got diff type " + parsedMsg);
                            }

                        } catch (Exception e) {
                            customLogger("Byte payload fail" + e.getMessage());
                        }
                    } else if (payload.getType() == Payload.Type.FILE) {
                        customLogger("Getting a file payload " + payload.asFile().getSize());
                        NotificationCompat.Builder notification = buildNotification(payload, true /*isIncoming*/);
                        mNotificationManager.notify((int) payload.getId(), notification.build());
                        incomingPayloads.put(Long.valueOf(payload.getId()), notification);
                        incomingPayloadReferences.put(payload.getId(), payload);
                    } else {
                        customLogger("Diff type payload");
                    }
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.drawable.common_full_open_on_phone);
                    long payloadId = update.getPayloadId();
                    if (incomingPayloads.containsKey(payloadId)) {
                        notification = incomingPayloads.get(payloadId);
                        if (update.getStatus() != PayloadTransferUpdate.Status.IN_PROGRESS) {
                            incomingPayloads.remove(payloadId);
                        }
                    } else if (outgoingPayloads.containsKey(payloadId)) {
                        notification = outgoingPayloads.get(payloadId);
                        if (update.getStatus() != PayloadTransferUpdate.Status.IN_PROGRESS) {
                            outgoingPayloads.remove(payloadId);
                            VideoData vd = outgoingTransfersMetadata.remove(payloadId);
                            mFileModule.writeToJSONFile(vd); // update JSON file
                            customLogger("Updated the outbound JSON");
                        }
                    }
                    Payload payload = incomingPayloadReferences.get(update.getPayloadId());
                    switch(update.getStatus()) {
                        case PayloadTransferUpdate.Status.IN_PROGRESS:
                            int size = (int)update.getTotalBytes();
                            notification.setProgress(size, (int)update.getBytesTransferred(), false /* indeterminate */);
                            break;
                        case PayloadTransferUpdate.Status.SUCCESS:
                            notification
                                    .setProgress(100, 100, false /* indeterminate */)
                                    .setContentText("Transfer complete!");
                            String filename = filePayloadFilenames.remove(update.getPayloadId());
                            if (payload != null) {
                                File payloadFile = payload.asFile().asJavaFile();
                                payloadFile.renameTo(new File(mFileModule.getDataDirectory(), filename));

                                // remove it from being tracked by the incomingJSONs and write that to a file
                                for (int i = 0; i < incomingTransfersMetadata.size(); i++) {
                                    if (incomingTransfersMetadata.get(i).getFileName().compareTo(filename) == 0) {
                                        mFileModule.writeToJSONFile(incomingTransfersMetadata.get(i));
                                        customLogger("Wrote the incoming JSON");
                                        incomingTransfersMetadata.remove(i);
                                        break;
                                    }
                                }
                                customLogger("Wrote the data to a file");
                            }
                            break;
                        case PayloadTransferUpdate.Status.FAILURE:
                            notification
                                    .setProgress(0, 0, false)
                                    .setContentText("Transfer failed");
                            break;
                    }
                    mNotificationManager.notify((int) payloadId, notification.build());
                }

            };
}
