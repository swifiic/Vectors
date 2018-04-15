package in.swifiic.vectors;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.SimpleArrayMap;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import in.swifiic.common.AckItem;
import in.swifiic.common.Acknowledgement;
import in.swifiic.common.ConnectionLog;
import in.swifiic.common.Constants;
import in.swifiic.common.FileModule;
import in.swifiic.common.VideoData;

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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.jaredrummler.android.device.DeviceName;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

import static in.swifiic.vectors.MessageScheme.getMessageType;
import static java.nio.charset.StandardCharsets.UTF_8;

/***
 * Abhishek Thakur : coding the background service with Binder
 * Ref: https://developer.android.com/guide/components/bound-services.html
 */
public class MainBGService extends IntentService {

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private boolean nearbyEnabled = false;

    public ConnectionsClient mConnectionClient;
    public String connectedEndpoint;
    private String startTime;
    private boolean extraChecks = true;
    private boolean goodbyeSent = false;
    private FileModule mFileModule;
    private ConnectionLog mConnectionLog;
    private StringBuilder logBuffer = new StringBuilder();
    private int bufferLines = 0;
    private boolean enableNotifications = false;
    private String endpointName;
    private boolean goodbyeReceived = false;
    private long lastNodeContactTime = 0;
    private Timer timer;
    private TimerTask timerTask;

    private final ArrayList<Long> incomingPayloads = new ArrayList<>();
    public final ArrayList<Long> outgoingPayloads = new ArrayList<>();

    private final SimpleArrayMap<Long, Payload> incomingPayloadReferences = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, String> filePayloadFilenames = new SimpleArrayMap<>();
    public SimpleArrayMap<Long, VideoData> outgoingTransfersMetadata = new SimpleArrayMap<>();
    private List<Pair<String, Long>> recentlyVisitedNodes = new ArrayList<>();

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor; // TODO - may not need edit

    static MainBGService ourRef = null;

    final String TAG = "VectorsSvc";

    String deviceId = "NOT-INITIALIZED";

    public class LocalBinder extends Binder {
        MainBGService getService() {
            // Return this instance of LocalService so clients can call public methods
            if (null == ourRef) {
                ourRef = MainBGService.this;
                customLogger("Setting reference from BGService");
            }
            return MainBGService.this;
        }
    }

    static public MainBGService getBGServiceRef() {
        return ourRef;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean enableBackgroundService() {
        return mSharedPreferences.getBoolean(Constants.STATUS_ENABLE_BG_SERVICE, true);
    }


    public void setBackgroundService() {
        if (enableBackgroundService()) {
            if (!nearbyEnabled) {
                stopDiscovery();
                stopAdvertising();
                SystemClock.sleep(Constants.DELAY_TIME_MS);
                startAdvertising();
                startDiscovery();
                nearbyEnabled = true;
            }
        } else {
            customLogger("Stopping VectorsBG");
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
            mConnectionClient = Nearby.getConnectionsClient(VectorsApp.getContext());
        }
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        initConnectionAndNotif();
        String dataString = workIntent.getDataString();
    }

    private String createDeviceId() {
        String androidId = Settings.Secure.getString(VectorsApp.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        deviceId = Constants.ENDPOINT_PREFIX + DeviceName.getDeviceName() + "_" + BuildConfig.VERSION_NAME + "_" + androidId.substring(androidId.length() - 4); //get last 4 chars
        return deviceId;
    }

    void initBGService() {
        mFileModule = new FileModule(this);
        deviceId = createDeviceId();
        initConnectionAndNotif();
        startTime = new SimpleDateFormat("HH.mm.ss").format(new Date());
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(VectorsApp.getContext());
        mEditor = mSharedPreferences.edit();
        mEditor.putString(Constants.DEVICE_ID, getDeviceId());
        mEditor.apply();
        if (enableBackgroundService()) {
            customLogger( "BgserviceEnable");
        } else {
            customLogger( "Bgservicedisable");
        }

        Acknowledgement ack = mFileModule.getAckFromFile();
        if (ack != null) {
            // TODO - test this
            byte[] x = Acknowledgement.getCompressedAcknowledgement(ack);
            customLogger("X Str Len " + x.length);
            Acknowledgement newAck = Acknowledgement.getDecompressedAck(x);
        }

        initaliseTimer();
        setBackgroundService();
    }

    private void initaliseTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                softResetNearby();
            }
        };
        timer.schedule(timerTask, 0, Constants.RESTART_NEARBY_SECS * 1000);
    }

    public MainBGService() {
        super("DemoWorkerName");
        initBGService();
    }

    public MainBGService(String workerName) {
        super(workerName);
        initBGService();
    }

    public void customLogger(String msg) {
        Log.d(TAG, msg);
        String logMsg = msg;
        addToLogBuffer(logMsg);

        // Broadcasts the Intent to receivers in this app.
        Intent localIntent =  new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.LOG_STATUS, logMsg);
        LocalBroadcastManager.getInstance(VectorsApp.getContext()).sendBroadcast(localIntent);
    }

    private void addToLogBuffer(String logMsg) {
        if (bufferLines >= Constants.LOG_BUFFER_SIZE) {
            mFileModule.writeLogBuffer(logBuffer);
            logBuffer.setLength(0); // hack to clear the buffer
            bufferLines = 0;
        }
        logBuffer.append(logMsg);
        bufferLines++;
    }

    private void sendConnectionStatus(String msg){

        Intent localIntent =  new Intent(Constants.BROADCAST_ACTION)
                .putExtra(Constants.CONNECTION_STATUS, msg);

        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

    }

    synchronized private void restartNearby() {
        customLogger("RestartingNearby");
        incomingPayloads.clear();
        outgoingPayloads.clear();
        incomingPayloadReferences.clear();
        filePayloadFilenames.clear();
        outgoingTransfersMetadata.clear();
        stopAdvertising();
        stopDiscovery();
//        mConnectionClient.stopAllEndpoints();
        customLogger("StoppedComms");
        if (connectedEndpoint != null) {
            mConnectionClient.disconnectFromEndpoint(connectedEndpoint);
            connectedEndpoint = null;
        }

        if (mConnectionLog != null) {
            mConnectionLog.connectionTerminated();
            mFileModule.writeConnectionLog(mConnectionLog);
        }
        mConnectionLog = null;
        
        goodbyeReceived = false;
        goodbyeSent = false;
        SystemClock.sleep(Constants.DELAY_TIME_MS*5);
        startAdvertising();
        startDiscovery();
        customLogger("RestartedComm");
    }

    private void softResetNearby() { // call this whenever we don't find nodes for some period of time
        long currentTime = System.currentTimeMillis()/1000;
        customLogger("Checking if time to reset nearby");
        if ((currentTime - lastNodeContactTime) > Constants.RESTART_NEARBY_SECS && connectedEndpoint == null && nearbyEnabled) {
            customLogger("Didn't find nodes for a while, force resetting nearby");
            restartNearby();
        } else {
            long timeDiff = currentTime - lastNodeContactTime;
            customLogger("Not reset - last endpoint found at " + timeDiff + " ConnectedEpName " + connectedEndpoint + " Nearby Enabled " + nearbyEnabled);
        }
    }

    private Task<Void> startAdvertising() {
        return mConnectionClient.startAdvertising(
                getDeviceId(),
                VectorsApp.getContext().getPackageName(),
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
                                customLogger("Advert fail" + e.getMessage());
                            }
                        });
    }

    private Task<Void> startDiscovery() {
        return mConnectionClient.startDiscovery(
                VectorsApp.getContext().getPackageName(),
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
                                customLogger("Discovery FAILED! " + e.getMessage());
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

    public void setLastNodeContactTime() {
        lastNodeContactTime = System.currentTimeMillis()/1000;
    }

    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
                    customLogger("FOUND ENDPOINT: " + endpointId + "Info " + discoveredEndpointInfo.getEndpointName() + " id " + discoveredEndpointInfo.getServiceId());
                    setLastNodeContactTime();
                    if (discoveredEndpointInfo.getEndpointName().startsWith(Constants.ENDPOINT_PREFIX) && !recentlyVisited(endpointName) && connectedEndpoint == null) {
                        stopAdvertising();
                        stopDiscovery();
                        customLogger("Stopping before requesting Conn");
                        mConnectionClient.requestConnection(
                                getDeviceId(),
                                endpointId,
                                mConnectionLifecycleCallback).
                                addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            customLogger("requesting conn");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            customLogger("Connection fail " + e.getMessage());
                                            restartNearby();
                                        }
                                    });
                    }
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    customLogger("lost ENDPOINT: " + endpointId);
                    restartNearby();
                }
            };

    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    // Automatically accept the connection on both sides.
                    setLastNodeContactTime();
                    endpointName = connectionInfo.getEndpointName();
                    customLogger("Pending connection From " + endpointName);
                    if (endpointName.startsWith("Vectors") && !recentlyVisited(endpointName)) {
                        customLogger("Connection initated w/ " + endpointName);
                        mConnectionClient.acceptConnection(endpointId, mPayloadCallback);
                    }
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    customLogger("Checking Connection Status " + result.toString());
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            if (BuildConfig.DEBUG) {
                                Toast.makeText(getApplicationContext(), "Connection Established", Toast.LENGTH_LONG).show();
                            }
                            sendConnectionStatus("Connected To: " + endpointName);
                            connectedEndpoint = endpointId;
                            mConnectionLog = new ConnectionLog(deviceId, endpointName);
                            sendDestinationAck();
                            sendFileList();
                            stopAdvertising();
                            stopDiscovery();
                            break;
                        case ConnectionsStatusCodes.STATUS_ENDPOINT_IO_ERROR: //this code is ignored
                            customLogger("endpt error, restart");
                            restartNearby();
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // The connection was rejected by one or both sides.
                            customLogger("Rejection Fail");
                            restartNearby();
                            break;
                        case ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT:
                            customLogger("Other EP might be connected");
                            restartNearby();
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            customLogger("Unknown STATUS_ERROR");
                            // The connection broke before it was able to be accepted.
                            restartNearby();
                            break;
                        default:
                            customLogger("Different error");
                            restartNearby();
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                    sendConnectionStatus("Disconnected");
                    customLogger("Connection terminated, clearing arrays");
                    restartNearby();
                }
            };

    private final PayloadCallback mPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    if (payload.getType() == Payload.Type.BYTES) {
                        String payloadMsg = "";
                        try {//                            customLogger("Getting a byte pyalod " + payloadMsg);
                            payloadMsg = new String(payload.asBytes(), "UTF-8");
                            MessageScheme.MessageType type = getMessageType(payloadMsg);

                            String parsedMsg = MessageScheme.parsePayloadString(payloadMsg);
                            if (parsedMsg == null) {
                                customLogger("Null payload MSG");
                            }
                            handleBytePayload(type, parsedMsg);
                        } catch (Exception e) {
                            customLogger("Byte payload fail " + e.getMessage());
                            if(payloadMsg != null){
                                customLogger("Attempted to decode " + payloadMsg + "#");
                            }
                            e.printStackTrace();
                        }
                    } else if (payload.getType() == Payload.Type.FILE) {
                        customLogger("Getting a file payload " + payload.asFile().getSize());
                        incomingPayloads.add(Long.valueOf(payload.getId()));
                        incomingPayloadReferences.put(payload.getId(), payload);
                    } else {
                        customLogger("Diff type payload");
                    }
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    long payloadId = update.getPayloadId();
                    if (incomingPayloads.contains(payloadId)) {
                        if (update.getStatus() != PayloadTransferUpdate.Status.IN_PROGRESS) {
                            incomingPayloads.remove(payloadId);
                        }
                        if (incomingPayloads.isEmpty()) {
                            checkConnectionTermination();
                            // done receiving
                        }
                    } else if (outgoingPayloads.contains(payloadId)) {
                        if (update.getStatus() != PayloadTransferUpdate.Status.IN_PROGRESS) {
                            outgoingPayloads.remove(payloadId);
                            VideoData vd = outgoingTransfersMetadata.remove(payloadId);
                            if (vd != null) {
                                mFileModule.writeToJSONFile(vd); // update JSON file
//                                customLogger("JSON for " + vd.getFileName() + " curr tickets " + vd.getTickets());
                            } else {
                                customLogger("Working with non-vid file, sent");
                            }
                        }
                        if (outgoingPayloads.isEmpty()) {
//                            customLogger("No more outbound payloads (For now)");
                            checkConnectionTermination();
                            // done sending
                        }
                    }
                    Payload payload = incomingPayloadReferences.get(update.getPayloadId());
                    switch(update.getStatus()) {
                        case PayloadTransferUpdate.Status.IN_PROGRESS:
                            int size = (int)update.getTotalBytes();
                            break;
                        case PayloadTransferUpdate.Status.SUCCESS:
                            String filename = filePayloadFilenames.remove(update.getPayloadId());
                            if (payload != null) {
                                File payloadFile = payload.asFile().asJavaFile();
                                if (filename == null) {
                                    customLogger("Strange wrong fname! aborting rename");
                                    payloadFile.delete();
                                } else {
                                    customLogger("Fname " + filename);
                                    payloadFile.renameTo(new File(mFileModule.getDataDirectory(), filename));
                                    mConnectionLog.addReceivedFile(filename);
                                }
                            }
                            break;
                        case PayloadTransferUpdate.Status.FAILURE:
                            break;
                    }
                }
            };


    private void handleBytePayload(MessageScheme.MessageType type, String parsedMsg) {
        if (type == MessageScheme.MessageType.WELCOME) {
            customLogger("Got a welcome MSG! " + parsedMsg);
        } else if (type == MessageScheme.MessageType.JSON) {
            processJSONMsg(parsedMsg);
        } else if (type == MessageScheme.MessageType.EXTRA) {
            customLogger("Got an extra msg!" + parsedMsg);
        } else if (type == MessageScheme.MessageType.FILELIST) {
            processFileList(parsedMsg);
        } else if (type == MessageScheme.MessageType.FILEMAP) {
            processFileMap(parsedMsg);
        } else if (type == MessageScheme.MessageType.REQUESTFILES) {
            processRequestFiles2(parsedMsg);
        } else if (type == MessageScheme.MessageType.DESTINATIONACK) {
            processDackJSON(parsedMsg);
        } else if (type == MessageScheme.MessageType.GOODBYE) {
            customLogger("Goodbye recv");
            goodbyeReceived = true;
            checkConnectionTermination();
        } else {
            customLogger(" got Diff type " + parsedMsg);
        }
    }

    private void sendFileList() {
        String fileList = mFileModule.getQuickFileList();
        fileList = MessageScheme.createStringType(MessageScheme.MessageType.FILELIST, fileList);
        mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(fileList.getBytes(UTF_8)));
    }

    public void sendGoodbye() {
        String goodbye = MessageScheme.createStringType(MessageScheme.MessageType.GOODBYE, "DUMMYMSG");
        mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(goodbye.getBytes(UTF_8)));
        customLogger("Sent my goodbyes");
        goodbyeSent = true;
        checkConnectionTermination();
    }

    private void sendDestinationAck() {
        Acknowledgement ack = mFileModule.getAckFromFile();
        if (ack != null) {
            ack.addTraversedNode(getDeviceId());
            // TODO - test this
            byte[] compressedAckBytes = Acknowledgement.getCompressedAcknowledgement(ack);
            String compressedBase64 = Base64.encodeToString(compressedAckBytes, Base64.DEFAULT);
            String dackMsg = MessageScheme.createStringType(MessageScheme.MessageType.DESTINATIONACK, compressedBase64);
            customLogger("Sending ack with timestamp as " + ack.getAckTime() + " msg len " + dackMsg.length());

            mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(dackMsg.getBytes(UTF_8)));
        } else  {
            customLogger("Skipping ack as it decodes to null or does not exist.");
        }
    }


    private void sendVideoDataList(List<VideoData> requestedVideoDatas) {
        StringBuilder fileMap = new StringBuilder();
        List<Payload> outgoingPayloadReferences = new ArrayList<>();

        for (VideoData vd : requestedVideoDatas) {
            ParcelFileDescriptor pfd = mFileModule.getPfd(vd.getFileName());
            if (pfd == null) {
                customLogger("File missing " + vd.getFileName());
                continue;
            }
            Payload filePayload = Payload.fromFile(pfd);
            fileMap.append(filePayload.getId() + ":" + vd.getFileName() + ",");
            outgoingPayloadReferences.add(filePayload); // release when done
        }
        customLogger("FileMap" + fileMap.toString());
        // first send filemap
        try {
            String fileMapMsg = MessageScheme.createStringType(MessageScheme.MessageType.FILEMAP, fileMap.toString());
            Task task = mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(fileMapMsg.getBytes(UTF_8)));
            while (!task.isComplete()) {
                SystemClock.sleep(Constants.DELAY_TIME_MS);
            }
        } catch (Exception e) {
            customLogger("FileMap transfer fail" + e.getMessage());
        }
        //put code here?
        for (int i = 0; i < outgoingPayloadReferences.size(); i++) {
            Payload filePayload = outgoingPayloadReferences.get(i);
            outgoingPayloads.add(Long.valueOf(filePayload.getId()));
            VideoData vd = requestedVideoDatas.get(i);
            if (vd != null) {
                String videoDataJSON = vd.toString();
                videoDataJSON = MessageScheme.createStringType(MessageScheme.MessageType.JSON, videoDataJSON);
                Task task = mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(videoDataJSON.getBytes(UTF_8)));
                while(task.isComplete()) {}
                outgoingTransfersMetadata.put(Long.valueOf(filePayload.getId()), vd);
            }
            mConnectionClient.sendPayload(connectedEndpoint, outgoingPayloadReferences.get(i));
        }
    }

    private void processFileList(String filelist) {
        customLogger("Rcvd a filelist of " + filelist);
        List<String> rcvdFilenames = Arrays.asList(filelist.split(","));
        List<String> currFilenames = Arrays.asList(mFileModule.getQuickFileList().split(","));
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


    private void processJSONMsg(String parseMsg) {
        VideoData vd = VideoData.fromString(parseMsg);
        mFileModule.writeToJSONFile(vd);
    }

    private void processDackJSON(String compressedBase64) {
        byte[] data = Base64.decode(compressedBase64, Base64.DEFAULT);
        Acknowledgement incomingAck = Acknowledgement.getDecompressedAck(data);
        customLogger("Received ack with timestamp as " + incomingAck.getAckTime());
        long currentTimeInSec = System.currentTimeMillis() / 1000;
        if(incomingAck.getAckTime() > currentTimeInSec + 3600){
            customLogger("Discarding the ack with possibly skewed clock");
            return;
        }
        Acknowledgement currentAck = mFileModule.getAckFromFile();
        if (currentAck == null) {
            mFileModule.writeAckToJSONFile(incomingAck);
        } else {
            if (incomingAck.getAckTime() > currentAck.getAckTime()) {
                customLogger("Newer ack " + currentAck.getAckTime() + "  received, writing back to file");
                mFileModule.writeAckToJSONFile(incomingAck);
            }
        }

        String localFileList = mFileModule.getQuickFileList();

        List<AckItem> itemsInAck = mFileModule.getAckFromFile().getItems();
        for (AckItem item : itemsInAck) {
            String fileToCheck = item.getFilename();
            if (localFileList.contains(fileToCheck)) {
                mFileModule.deleteFile(fileToCheck);
                customLogger("Deleting on Ack "+ fileToCheck);
            }
        }
    }

    private void processFileMap(String fileMap) {
        if (fileMap.length() > 1) {
            List<String> payloadFilenames = Arrays.asList(fileMap.split(","));
            for (String s : payloadFilenames) {
                addPayloadFilename(s);
            }
        }
    }

    private void processRequestFiles2(String filelist) {
        Acknowledgement dack = mFileModule.getAckFromFile();

        List<String> requestedFiles = Arrays.asList(filelist.split(","));
        List<VideoData> requestedVideoDatas = new ArrayList<>();
        if (filelist.length() > 1) {
            for (int i = 0; i < requestedFiles.size(); i++) {
                if (requestedFiles.get(i).startsWith(Constants.VIDEO_PREFIX)) {
//                    customLogger("Attempting JSON for: " + requestedFiles.get(i));
                    VideoData vd = mFileModule.getVideoDataFromFile(requestedFiles.get(i));
                    if (vd != null) {
                        requestedVideoDatas.add(vd);
                    } else {
                        customLogger("JSON not decodeable / not found for " + requestedFiles.get(i));
                    }
                }
            }

            // sort by tickets and send in that order
            VideoData.sortListCopyCount(requestedVideoDatas);

            for (int i = 0; i < requestedVideoDatas.size(); i++) {
                VideoData vd = requestedVideoDatas.get(i); // TODO - test if this is a shallow copy
                if (vd != null) {
                    if (vd.getTickets() > 1) {
                        vd.setTickets(vd.getTickets() / 2); // SNW strategy allows us to only send half
                        vd.addTraversedNode(getDeviceId());
                        //send JSON and file
                        if (extraChecks && (vd.getCreationTime() + vd.getTtl() < System.currentTimeMillis() / 1000 ||
                                (dack != null && dack.containsFilename(vd.getFileName())))) {
                            customLogger("File has been acked/too old to send - Deleting " + vd.getFileName());
                            mFileModule.deleteFile(vd.getFileName());
                            requestedVideoDatas.remove(i);
                        }
                    }
                }
            }
            sendVideoDataList(requestedVideoDatas);
        }
        sendGoodbye();
    }

    private boolean recentlyVisited(String endpointName) {
        for (int i = 0; i < recentlyVisitedNodes.size(); i++) {
            if (recentlyVisitedNodes.get(i).first.compareTo(endpointName)==0) {
                if ((recentlyVisitedNodes.get(i).second + Constants.MIN_CONNECTION_GAP_TIME) > System.currentTimeMillis()/1000) {
                    customLogger("Seen this guy before! Discarding conn");
                    return true;
                } else {
                    customLogger("Hello old friend" + recentlyVisitedNodes.get(i) + " curr at " + System.currentTimeMillis()/1000);
                    recentlyVisitedNodes.remove(i);
                    return false;
                }
            }
        }
        customLogger("New guy");
        return false;
    }

    private void addPayloadFilename(String payloadFilenameMessage) {
        int colonIndex = payloadFilenameMessage.indexOf(':');
        String payloadId = payloadFilenameMessage.substring(0, colonIndex);
        String filename = payloadFilenameMessage.substring(colonIndex + 1);
        filePayloadFilenames.put(Long.valueOf(payloadId), filename);
    }

    private void checkConnectionTermination() {
        setLastNodeContactTime();
        if (outgoingPayloads.isEmpty() && filePayloadFilenames.isEmpty() && goodbyeSent && goodbyeReceived) {
            customLogger("Time to terminate connection!");
            recentlyVisitedNodes.add(new Pair<>(endpointName, System.currentTimeMillis() / 1000));
            sendConnectionStatus("Disconnect Initiated");
            restartNearby();
        }
    }
}