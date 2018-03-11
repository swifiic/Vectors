package com.arnavdhamija.roamnet;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MainActivity extends AppCompatActivity {
    private ConnectionsClient mConnectionClient;
    private NotificationManager mNotificationManager;
    private String connectedEndpoint;
    private boolean connectionActive = false;
    private FileModule mFileModule;

    enum MessageType {
        WELCOME, JSON, FILENAME, EXTRA, FILELIST, ERROR;
    }

    final String TAG = "Roamnet";
    String deviceId;

    private final SimpleArrayMap<Long, NotificationCompat.Builder> incomingPayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, NotificationCompat.Builder> outgoingPayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, Payload> incomingPayloadReferences = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, String> filePayloadFilenames = new SimpleArrayMap<>();

    void getPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    0);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermissions();
        mFileModule = new FileModule(this);
        deviceId = "Roamnet_" + DeviceName.getDeviceName();
        customLogger(deviceId);
        mConnectionClient = Nearby.getConnectionsClient(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        TextView deviceIdView = findViewById(R.id.deviceIdView);
        deviceIdView.setText("Device ID: " + deviceId);

        TextView availableFilesView = findViewById(R.id.availableFilesView);
        availableFilesView.setText("Available Files Count: " + mFileModule.getFilesCount());

        final Button toggleRoamnetButton = findViewById(R.id.toggleRoamnet);
        toggleRoamnetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!connectionActive) {
                    startAdvertising();
                    startDiscovery();
                    toggleRoamnetButton.setText("Stop Roamnet");
                    connectionActive = true;
                } else {
                    mConnectionClient.stopDiscovery();
                    mConnectionClient.stopAdvertising();
                    mConnectionClient.stopAllEndpoints();
                    toggleRoamnetButton.setText("Start Roamnet");
                    connectionActive = false;
                }
            }
        });
        Button logClearButton = findViewById(R.id.logClearButton);
        logClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView logView = findViewById(R.id.logView);
                logView.setText("");
            }
        });
    }

    private void customLogger(String msg) {
        Log.d(TAG, msg);
        TextView logView = findViewById(R.id.logView);
        String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new Date());
        logView.append(timeStamp+' '+msg+"\n");
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

    private void startAdvertising() {
        mConnectionClient.startAdvertising(
                deviceId,
                getPackageName(),
                mConnectionLifecycleCallback,
                new AdvertisingOptions(Strategy.P2P_STAR))
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
                new DiscoveryOptions(Strategy.P2P_STAR))
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
        } else {
            return null;
        }
    }

    MessageType getMessageType(String originalMsg) {
        String msgHeader = originalMsg.substring(0, 4);
        customLogger("HEader " + msgHeader);
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
        } else {
            return MessageType.ERROR;
        }
    }

    String parsePayloadString(String originalMsg) {
        return originalMsg.substring(4);
    }

    private void sendWelcomeMessage() {
        String welcome = "welcome2beconnected from  to " + connectedEndpoint;
        welcome = createStringType(MessageType.WELCOME, welcome);
        mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(welcome.getBytes(UTF_8)));
    }

    private void sendFileList() {
        String fileList = mFileModule.getFileList();
        mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(fileList.getBytes(UTF_8)));
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
                            customLogger("GGWP! :D:D:D");
                            Toast.makeText(getApplicationContext(), "Connection Established", Toast.LENGTH_LONG).show();
                            TextView textView = (TextView) findViewById(R.id.connectionStatusView);
                            textView.setText("Connected To: " + endpointId);
                            connectedEndpoint = endpointId;
                            sendWelcomeMessage();
                            customLogger("stopping AD");
                            mConnectionClient.stopAdvertising();
                            mConnectionClient.stopDiscovery();
                            customLogger("stopped AD!!");
                            // We're connected! Can now start sending and receiving data.
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
                    TextView textView = (TextView) findViewById(R.id.connectionStatusView);
                    textView.setText("Disconnected");
                    restartNearby();
                }
            };

    private void addPayloadFilename(String payloadFilenameMessage) {
        int colonIndex = payloadFilenameMessage.indexOf(':');
        String payloadId = payloadFilenameMessage.substring(0, colonIndex);
        String filename = payloadFilenameMessage.substring(colonIndex + 1);
        filePayloadFilenames.put(Long.valueOf(payloadId), filename);
    }

    private void handleJSONMsg() {

    }

    private void processFileList(String filelist) {
        customLogger("Rcvd a filelist of " + filelist);
        List<String> rcvdFilenames = Arrays.asList(filelist.split(","));
        List<String> currFilenames = Arrays.asList(mFileModule.getFileList());

        rcvdFilenames.removeAll(currFilenames); // gives us the list of files we don't have, but want to get

        String requestedFiles = convertListToCSV(rcvdFilenames);
        customLogger("We want the files of: " + requestedFiles);
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
                    customLogger("getting a payload");
                    if (payload.getType() == Payload.Type.BYTES) {
                        try {
                            String payloadMsg = new String(payload.asBytes(), "UTF-8");
                            customLogger("Getting a byte pyalod " + payloadMsg);
                            MessageType type = getMessageType(payloadMsg);
                            String parsedMsg = parsePayloadString(payloadMsg);

                            customLogger("MSG TYpeVAL" + type);
                            if (type == MessageType.WELCOME) {
                                customLogger("Got a welcome MSG! " + parsedMsg);
                            } else if (type == MessageType.JSON) {
                                handleJSONMsg();
                            } else if (type == MessageType.FILENAME) {
                                //add this to tracking map
                            } else if (type == MessageType.EXTRA) {
                                customLogger("Got an extra msg!" + parsedMsg);
                            } else if (type == MessageType.FILELIST) {
                                processFileList(parsedMsg);
                            } else {
                                customLogger(" got diff type " + parsedMsg);
                            }
                        } catch (Exception e) {
                            customLogger("Byte payload fail");
                        }
                    } else if (payload.getType() == Payload.Type.FILE) {
                        customLogger("Getting a file pyalod " + payload.asFile().getSize());
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
                            // This is the last update, so we no longer need to keep track of this notification.
                            incomingPayloads.remove(payloadId);
                        }
                    } else if (outgoingPayloads.containsKey(payloadId)) {
                        notification = outgoingPayloads.get(payloadId);
                        if (update.getStatus() != PayloadTransferUpdate.Status.IN_PROGRESS) {
                            // This is the last update, so we no longer need to keep track of this notification.
                            outgoingPayloads.remove(payloadId);
                        }
                    }
                    Payload payload = incomingPayloadReferences.get(update.getPayloadId());
                    switch(update.getStatus()) {
                        case PayloadTransferUpdate.Status.IN_PROGRESS:
                            int size = (int)update.getTotalBytes();
//                            customLogger("Bytes transferred " + update.getBytesTransferred());
                            if (size == -1) {
                                // This is a stream payload, so we don't need to update anything at this point.
                                return;
                            }
                            notification.setProgress(size, (int)update.getBytesTransferred(), false /* indeterminate */);
                            break;
                        case PayloadTransferUpdate.Status.SUCCESS:
                            // SUCCESS always means that we transferred 100%.
                            notification
                                    .setProgress(100, 100, false /* indeterminate */)
                                    .setContentText("Transfer complete!");
                            customLogger("Transfer done");
                            String filename = filePayloadFilenames.remove(update.getPayloadId());
                            if (payload != null) {
                                File payloadFile = payload.asFile().asJavaFile();
                                payloadFile.renameTo(new File(payloadFile.getParentFile(), filename));
                                customLogger("found N renamed");
                            } else {
                                customLogger("NUll");
                            }
                            break;
                        case PayloadTransferUpdate.Status.FAILURE:
                            notification
                                    .setProgress(0, 0, false)
                                    .setContentText("Transfer failed");
                            break;
                    }
//                    if (payload != null) {
                    mNotificationManager.notify((int) payloadId, notification.build());
//                    }
                }

            };
}
