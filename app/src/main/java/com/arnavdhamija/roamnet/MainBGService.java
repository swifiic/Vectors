package com.arnavdhamija.roamnet;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.arnavdhamija.common.AckItem;
import com.arnavdhamija.common.Acknowledgement;
import com.arnavdhamija.common.Constants;
import com.arnavdhamija.common.FileModule;
import com.arnavdhamija.common.VideoData;
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

import static com.arnavdhamija.roamnet.MessageScheme.getMessageType;
import static java.nio.charset.StandardCharsets.UTF_8;

/***
 * Abhishek Thakur : coding the background service with Binder
 * Ref: https://developer.android.com/guide/components/bound-services.html
 */
public class MainBGService extends IntentService {

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private boolean nearbyEnabled = false;

    private ConnectionsClient mConnectionClient;
    private NotificationManager mNotificationManager;
    private String connectedEndpoint;
    private String startTime;
    private boolean extraChecks = false;
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor; // TODO - may not need edit

    static MainBGService ourRef = null;

    private FileModule mFileModule;


    final String TAG = "RoamnetSvc";


    String deviceId = "NOT-INITIALIZED";

    private final SimpleArrayMap<Long, NotificationCompat.Builder> incomingPayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, NotificationCompat.Builder> outgoingPayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, Payload> incomingPayloadReferences = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, String> filePayloadFilenames = new SimpleArrayMap<>();
    private List<VideoData> incomingTransfersMetadata = new ArrayList<>();
    private SimpleArrayMap<Long, VideoData> outgoingTransfersMetadata = new SimpleArrayMap<>();
    private List<Pair<String, Long>> recentlyVisitedNodes = new ArrayList<>();


    public class LocalBinder extends Binder {
        MainBGService getService() {
            // Return this instance of LocalService so clients can call public methods
            ourRef = MainBGService.this;
            return MainBGService.this;
        }
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean enableBackgroundService() {
        return mSharedPreferences.getBoolean(Constants.STATUS_ENABLE_BG_SERVICE, false);
    }


    public void setBackgroundService() {
        if (enableBackgroundService()) {
            if (!nearbyEnabled) {
                startAdvertising();
                startDiscovery();
                nearbyEnabled = true;
            }
        } else {
            stopAllEndpoints();
            stopDiscovery();
            stopAdvertising();
            nearbyEnabled = false;
        }
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
    public int onStartCommand(Intent startIntent, int flags, int startId) {
//        super.onStartCommand(startIntent, flags, startId);
//        initConnectionAndNotif();
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        initConnectionAndNotif();
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();

        // Do work here, Nothing for now

    }

    void initBGService() {
        mFileModule = new FileModule(this);
        deviceId = "Roamnet_" + DeviceName.getDeviceName();
        customLogger("From bgservice" + deviceId);
        initConnectionAndNotif();
        startTime = new SimpleDateFormat("HH.mm.ss").format(new Date());
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(RoamNetApp.getContext());
        mEditor = mSharedPreferences.edit();
        mEditor.putString(Constants.DEVICE_ID, deviceId);
        mEditor.apply();
        if (enableBackgroundService()) {
            customLogger( "BgserviceEnable");
//            startAdvertising();
//            startDiscovery();
        } else {
            customLogger( "Bgservicedisable");
        }
    }

    public MainBGService() {
    // default Constructor
        super("DemoWorkerName");
        initBGService();
    }

    public MainBGService(String workerName) {
        super(workerName);
        initBGService();
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
        incomingPayloads.clear();
        outgoingPayloads.clear();
        incomingPayloadReferences.clear();
        filePayloadFilenames.clear();
        incomingTransfersMetadata.clear();
        outgoingTransfersMetadata.clear();
        mConnectionClient.stopAdvertising();
        mConnectionClient.stopDiscovery();
        goodbyeReceived = false;
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
    private void startAdvertising() {
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

    private void startDiscovery() {
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

    private void stopDiscovery() {
        mConnectionClient.stopDiscovery();
    }

    private void stopAdvertising() {
        mConnectionClient.stopAdvertising();
    }

    private void stopAllEndpoints() {
        mConnectionClient.stopAllEndpoints();
    }


    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(
                        String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
                    customLogger("FOUND ENDPOINT: " + endpointId + "Info " + discoveredEndpointInfo.getEndpointName() + " id " + discoveredEndpointInfo.getServiceId());
                    if (discoveredEndpointInfo.getEndpointName().startsWith("Roamnet")) {
                        stopAdvertising();
                        stopDiscovery();
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

    String endpointName;

    private boolean recentlyVisited(String endpointName) {
        for (int i = 0; i < recentlyVisitedNodes.size(); i++) {
            if (recentlyVisitedNodes.get(i).first.compareTo(endpointName)==0) {
                if ((recentlyVisitedNodes.get(i).second + Constants.MIN_CONNECTION_GAP_TIME) < System.currentTimeMillis()/1000) {
                    return true;
                } else {
                    recentlyVisitedNodes.remove(i);
                    return false;
                }
            }
        }
        return false;
    }

    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(
                        String endpointId, ConnectionInfo connectionInfo) {
                    // Automatically accept the connection on both sides.
                    endpointName = connectionInfo.getEndpointName();
                    if (endpointName.startsWith("Roamnet") && !recentlyVisited(endpointName)) {
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
                    customLogger("connection terminated, find a way to autostart, clearing arrays");
                    incomingPayloads.clear();
                    outgoingPayloads.clear();
                    incomingPayloadReferences.clear();
                    filePayloadFilenames.clear();
                    incomingTransfersMetadata.clear();
                    outgoingTransfersMetadata.clear();

                    sendConnectionStatus("Disconnected");
                    restartNearby();
                }
            };

    private void addPayloadFilename(String payloadFilenameMessage) {
        customLogger("adding2fnamerefs" + payloadFilenameMessage);
        int colonIndex = payloadFilenameMessage.indexOf(':');
        String payloadId = payloadFilenameMessage.substring(0, colonIndex);
        String filename = payloadFilenameMessage.substring(colonIndex + 1);
        filePayloadFilenames.put(Long.valueOf(payloadId), filename);
    }

    private void sendWelcomeMessage() {
        String welcome = "welcome2beconnected from  to " + connectedEndpoint;
        welcome = MessageScheme.createStringType(MessageScheme.MessageType.WELCOME, welcome);
        mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(welcome.getBytes(UTF_8)));
    }

    private void sendFile(String filename) {
        ParcelFileDescriptor pfd = mFileModule.getPfd(filename);
        if (pfd == null) {
            customLogger("File not found - lite");
            return;
        }
        Payload filePayload = Payload.fromFile(pfd);
        NotificationCompat.Builder notification = buildNotification(filePayload, false);

        mNotificationManager.notify((int)filePayload.getId(), notification.build());
        outgoingPayloads.put(Long.valueOf(filePayload.getId()), notification);
        try {
            String payloadFilenameMessage = filePayload.getId() + ":" + filename;
            customLogger("using method2 to send" + payloadFilenameMessage);
            payloadFilenameMessage = MessageScheme.createStringType(MessageScheme.MessageType.FILENAME, payloadFilenameMessage);
            mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(payloadFilenameMessage.getBytes("UTF-8")));
            mConnectionClient.sendPayload(connectedEndpoint, filePayload);
        } catch (UnsupportedEncodingException e) {
            customLogger("encode fail");
        }
    }

    private void sendFile(VideoData data) {
        ParcelFileDescriptor pfd = mFileModule.getPfd(data.getFileName());
        if (pfd == null) {
            customLogger("File not found - lite");
            return;
        }
        Payload filePayload = Payload.fromFile(pfd);

        String payloadFilenameMessage = filePayload.getId() + ":" + data.getFileName();
        sendFile(filePayload, payloadFilenameMessage, data);
    }

    // remove endpoint id from here?!
    private void sendFile(Payload payload, String payloadFilenameMsg, VideoData data) {
        NotificationCompat.Builder notification = buildNotification(payload, false);
        mNotificationManager.notify((int)payload.getId(), notification.build());
        outgoingPayloads.put(Long.valueOf(payload.getId()), notification);

        try {
            payloadFilenameMsg = MessageScheme.createStringType(MessageScheme.MessageType.FILENAME, payloadFilenameMsg);
            customLogger("Sending a file - filename is : " + payloadFilenameMsg);
            mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(payloadFilenameMsg.getBytes("UTF-8")));
            mConnectionClient.sendPayload(connectedEndpoint, payload);

            // now we send the JSON metadata mapped by the payload ID
            String videoDataJSON = data.toString();
            videoDataJSON = MessageScheme.createStringType(MessageScheme.MessageType.JSON, videoDataJSON);
            mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(videoDataJSON.getBytes(UTF_8)));
            outgoingTransfersMetadata.put(Long.valueOf(payload.getId()), data);

        } catch (UnsupportedEncodingException e) {
            customLogger("encode fail");
        }
    }

    private void sendFileList() {
        String fileList = mFileModule.getFileList();
        fileList = MessageScheme.createStringType(MessageScheme.MessageType.FILELIST, fileList);
        mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(fileList.getBytes(UTF_8)));
    }

    private void sendGoodbye() {
        String goodbye = MessageScheme.createStringType(MessageScheme.MessageType.GOODBYE, null);
        mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(goodbye.getBytes(UTF_8)));
    }

    private void sendDestinationAck() {
        Acknowledgement ack = mFileModule.getAckFromFile();
        if (ack != null) {
            String dackMsg = ack.toString();
            dackMsg = MessageScheme.createStringType(MessageScheme.MessageType.DESTINATIONACK, dackMsg);
            mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(dackMsg.getBytes(UTF_8)));
        }
    }

    private void processJSONMsg(String parseMsg) {
        VideoData vd = VideoData.fromString(parseMsg);
        // this list has to be managed, when we start getting files of course
        incomingTransfersMetadata.add(vd);
    }

    private void processDackJSON(String parseMsg) {
        Acknowledgement incomingAck = Acknowledgement.fromString(parseMsg);
        Acknowledgement currentAck = mFileModule.getAckFromFile();
        if (currentAck == null) {
            mFileModule.writeToJSONFile(incomingAck);
        } else {
            if (incomingAck.getAckTime() > currentAck.getAckTime()) {
                customLogger("Newer ack received, writing back to file");
                mFileModule.writeToJSONFile(incomingAck);
            }
        }

        for (AckItem item : mFileModule.getAckFromFile().getItems()) {
            if (mFileModule.getFileList().compareTo(item.getFilename())==0) {
                deleteFile(item.getFilename());
            }
        }
    }


    boolean readyToTerminate = false;
    private void initiateConnectionTermination() {
        readyToTerminate = true;
    }

    private void processRequestFiles(String filelist) {
        Acknowledgement dack = mFileModule.getAckFromFile();
        List<String> requestedFiles = Arrays.asList(filelist.split(","));
        List<VideoData> requestedVideoDatas = new ArrayList<>();
        List<String> otherFileTypes = new ArrayList<>();
        if (requestedFiles.size() > 0) {
            for (int i = 0; i < requestedFiles.size(); i++) {
                if (requestedFiles.get(i).startsWith("video")) {
                    requestedVideoDatas.add(mFileModule.getVideoDataFromFile(requestedFiles.get(i)));
                } else {
                    if (requestedFiles.get(i) != "") {
                        customLogger("OtherFileType! " + requestedFiles.get(i));
                        otherFileTypes.add(requestedFiles.get(i));
                    }
                }
            }

            // sort by tickets and send in that order
            Collections.sort(requestedVideoDatas, new Comparator<VideoData>() {
                @Override
                public int compare(VideoData o1, VideoData o2) {
                    if (o1.getTickets() > o2.getTickets()) { // test if this is descending order
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });

            for (VideoData vd : requestedVideoDatas) {
                if (vd != null) {
                    if (vd.getTickets() > 1) {
                        vd.setTickets(vd.getTickets());// / 2); // SNW strategy allows us to only send half
                        vd.addTraversedNode(deviceId);
                        //send JSON and file
                        boolean sendFile = true;
                        if (extraChecks && (//vd.getCreationTime() + vd.getTtl() < System.currentTimeMillis() / 1000 ||
                                dack.getAckedFilenames().contains(vd.getFileName()))) {
                            customLogger("File has been acked/too old to send" + vd.getFileName());
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
        } else {
            sendGoodbye();
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
        String requestFilesCSV = FileModule.convertListToCSV(requestFilenames);
        customLogger("We want the files of " + requestFilesCSV);
        // we send the files we want to get here
        requestFilesCSV = MessageScheme.createStringType(MessageScheme.MessageType.REQUESTFILES, requestFilesCSV);
        mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(requestFilesCSV.getBytes(UTF_8)));
    }

    private boolean goodbyeReceived = false;

    private void checkConnectionTermination() {
        if (outgoingPayloads.isEmpty() && incomingPayloads.isEmpty() && goodbyeReceived) {
            recentlyVisitedNodes.add(new Pair<>(endpointName, System.currentTimeMillis()/1000));
            restartNearby();
        }
    }

    private final PayloadCallback mPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    if (payload.getType() == Payload.Type.BYTES) {
                        try {
                            String payloadMsg = new String(payload.asBytes(), "UTF-8");
//                            customLogger("Getting a byte pyalod " + payloadMsg);
                            MessageScheme.MessageType type = getMessageType(payloadMsg);
                            String parsedMsg = MessageScheme.parsePayloadString(payloadMsg);
                            if (parsedMsg == null) {
                                customLogger("We got a null string?!?!?!");
                            }
//                            customLogger("MSG TYpeVAL" + type);
                            if (type == MessageScheme.MessageType.WELCOME) {
                                customLogger("Got a welcome MSG! " + parsedMsg);
                            } else if (type == MessageScheme.MessageType.JSON) {
                                processJSONMsg(parsedMsg);
                            } else if (type == MessageScheme.MessageType.FILENAME) {
                                addPayloadFilename(parsedMsg);
                            } else if (type == MessageScheme.MessageType.EXTRA) {
                                customLogger("Got an extra msg!" + parsedMsg);
                            } else if (type == MessageScheme.MessageType.FILELIST) {
                                processFileList(parsedMsg);
                            } else if (type == MessageScheme.MessageType.REQUESTFILES) {
                                processRequestFiles(parsedMsg);
                            } else if (type == MessageScheme.MessageType.DESTINATIONACK) {
                                processDackJSON(parsedMsg);
                            } else if (type == MessageScheme.MessageType.GOODBYE) {
                                goodbyeReceived = true;
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
                        if (incomingPayloads.isEmpty()) {
                            customLogger("Done receiving payloads, can terminate");
                            checkConnectionTermination();
                            // done receiving
                        }
                    } else if (outgoingPayloads.containsKey(payloadId)) {
                        notification = outgoingPayloads.get(payloadId);
                        if (update.getStatus() != PayloadTransferUpdate.Status.IN_PROGRESS) {
                            outgoingPayloads.remove(payloadId);
                            VideoData vd = outgoingTransfersMetadata.remove(payloadId);
                            if (vd != null) {
                                mFileModule.writeToJSONFile(vd); // update JSON file
                                customLogger("Updated the outbound JSON");
                            } else {
                                customLogger("Working with non-vid file, sent"); //very strange stuff
                            }
                        }
                        if (outgoingPayloads.isEmpty()) {
                            customLogger("Done transferring payloads, can terminate");
                            sendGoodbye();
                            checkConnectionTermination();
                            // done sending
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
//                            customLogger("Renaming" + filename);
                            if (payload != null) {
                                File payloadFile = payload.asFile().asJavaFile();
                                if (filename == null) {
                                    customLogger("Strange wrong fname! aborting rename");
                                    payloadFile.delete();
                                } else {
                                    // remove all refs if we get something null
                                    customLogger("Fname " + filename);
                                    payloadFile.renameTo(new File(mFileModule.getDataDirectory(), filename));

                                    // remove it from being tracked by the incomingJSONs and write that to a file
                                    for (int i = 0; i < incomingTransfersMetadata.size(); i++) {
                                        if (filename != null && incomingTransfersMetadata.get(i).getFileName().compareTo(filename) == 0 && incomingTransfersMetadata.get(i) != null) {
                                            mFileModule.writeToJSONFile(incomingTransfersMetadata.get(i));
                                            customLogger("Wrote the incoming JSON of " + filename);
                                            incomingTransfersMetadata.remove(i);
                                            break;
                                        }
                                    }
                                    customLogger("Wrote the data to a file");
                                }
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

//    private enum connectionStates {
//        ENABLED, DISABLED;
//    }
//
//    private connectionStates mCurrentState = connectionStates.DISABLED;
