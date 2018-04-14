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
    public List<VideoData> requestedVideoDatas;
    List<Payload> outgoingPayloadReferences;
    MainBGService ref;

    public FileTransferTask(MainBGService ref, List<VideoData> requestedVideoDatas, List<Payload> outgoingPayloadReferences) {
        this.requestedVideoDatas = requestedVideoDatas;
        this.outgoingPayloadReferences = outgoingPayloadReferences;
        this.ref = ref;
    }

    @Override
    public void run() {
        for (int i = 0; i < outgoingPayloadReferences.size(); i++) {
            Payload filePayload = outgoingPayloadReferences.get(i);
            ref.outgoingPayloads.add(Long.valueOf(filePayload.getId()));
            VideoData vd = requestedVideoDatas.get(i);
            if (vd != null) {
                String videoDataJSON = vd.toString();
                videoDataJSON = MessageScheme.createStringType(MessageScheme.MessageType.JSON, videoDataJSON);
                ref.mConnectionClient.sendPayload(ref.connectedEndpoint, Payload.fromBytes(videoDataJSON.getBytes(UTF_8)));
                ref.outgoingTransfersMetadata.put(Long.valueOf(filePayload.getId()), vd);
            }
            ref.mConnectionClient.sendPayload(ref.connectedEndpoint, outgoingPayloadReferences.get(i));
        }
    }
}
