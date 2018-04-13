package in.swifiic.vectors;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.util.SimpleArrayMap;
import android.util.Pair;

import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import in.swifiic.common.Constants;
import in.swifiic.common.VideoData;

import static java.nio.charset.StandardCharsets.UTF_8;


public class FileTransferTask extends Thread {
    public ArrayList<Long> outgoingPayloads;
    public List<VideoData> requestedVideoDatas;
    public SimpleArrayMap<Long, VideoData> outgoingTransfersMetadata;
    public List<Payload> outgoingPayloadReferences;
    public ConnectionsClient mConnectionClient;
    String connectedEndpoint;
    MainBGService ref;

    public FileTransferTask(ArrayList<Long> outgoingPayloads, List<VideoData> requestedVideoDatas, SimpleArrayMap<Long, VideoData> outgoingTransfersMetadata, List<Payload> outgoingPayloadReferences, ConnectionsClient connectionsClient, String connectedEndpoint, MainBGService ref) {
        this.outgoingPayloads = outgoingPayloads;
        this.requestedVideoDatas = requestedVideoDatas;
        this.outgoingTransfersMetadata = outgoingTransfersMetadata;
        this.outgoingPayloadReferences = outgoingPayloadReferences;
        this.mConnectionClient = connectionsClient;
        this.connectedEndpoint = connectedEndpoint;
        this.ref = ref;
    }

    @Override
    public void run() {
        for (int i = 0; i < outgoingPayloadReferences.size(); i++) {
            Payload filePayload = outgoingPayloadReferences.get(i);
            outgoingPayloads.add(Long.valueOf(filePayload.getId()));
            VideoData vd = requestedVideoDatas.get(i);
            if (vd != null) {
                String videoDataJSON = vd.toString();
                videoDataJSON = MessageScheme.createStringType(MessageScheme.MessageType.JSON, videoDataJSON);
                mConnectionClient.sendPayload(connectedEndpoint, Payload.fromBytes(videoDataJSON.getBytes(UTF_8)));
                outgoingTransfersMetadata.put(Long.valueOf(filePayload.getId()), vd);
            }
            mConnectionClient.sendPayload(connectedEndpoint, outgoingPayloadReferences.get(i));
        }
    }
}
