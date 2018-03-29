package in.swifiic.shmbridge;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.arnavdhamija.common.AckItem;
import com.arnavdhamija.common.Acknowledgement;
import com.arnavdhamija.common.Constants;
import com.arnavdhamija.common.FileModule;
import com.arnavdhamija.common.VideoData;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;



public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private ProgressDialog pDialog;

    private String TAG = "SHM_BRDG";


    void getPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
//                    0);
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                    0);
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    0);
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            String[] requiredPermissionsArray = listPermissionsNeeded.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, requiredPermissionsArray, 0);
        }
    }

    boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case 0:
                if (grantResults.length > 0) {
                    int j=0;
                    for (int i : grantResults) {
                        if (i == PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "Perm granted for " + permissions[j]);
                        } else {
                            Log.d(TAG, "We didn't get perms :( for " + permissions[j]);
                        }
                        j++;
                    }
                }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /* get SharedPreference and upload the server addresses */
        SharedPreferences sp = getSharedPreferences("config", 0);
        String srcValue = sp.getString("srcUrl","http://172.16.2.194/php_server");
        String destValue = sp.getString("destUrl","Dest");

        EditText srcId = findViewById(R.id.idSrcVal);
        EditText destId = findViewById(R.id.idDestVal);
        srcId.setText(srcValue);
        destId.setText(destValue);

        getPermissions();



        final Button checkButton = findViewById(R.id.CheckNow);
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText srcId = findViewById(R.id.idSrcVal);
                EditText destId = findViewById(R.id.idDestVal);
                Editable valueSrc = srcId.getText();
                Editable valueDest = destId.getText();
                String srcUrl = valueSrc.toString();
                String destUrl = valueDest.toString();

                if(srcUrl.contains("http")) {
                    DownloadAsyncTask downloadBg = new DownloadAsyncTask(MainActivity.this);
                    downloadBg.execute(srcUrl);
                }
                if(destUrl.contains("http")) {
                    UploadAsyncTask uploadBg = new UploadAsyncTask(MainActivity.this);
                    uploadBg.execute(destUrl);

                }
                SharedPreferences sp = getSharedPreferences("config", 0);
                SharedPreferences.Editor sedt = sp.edit();
                sedt.putString("srcUrl", srcUrl);
                sedt.putString("destUrl", destUrl);
                sedt.commit();

            }
        });

        final Button injectButton = findViewById(R.id.InjectNow); // SoakNow
        injectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                injectBurst();
            }
        });

        final Button soakButton = findViewById(R.id.SoakNow); //
        soakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soakDataAndCreateAck();
            }
        });

    }



    /*** Burst injection logic ***/
    private void injectBurst(){
        SharedPreferences mSharedPreferences;
        SharedPreferences.Editor mEditor;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(BridgeApp.getContext());
        mEditor = mSharedPreferences.edit();
        int burstCount =mSharedPreferences.getInt(Constants.BURST_COUNT, 0) + 1;
        mEditor.putInt(Constants.BURST_COUNT, burstCount);
        mEditor.commit();

        Log.d(TAG,"Sending burst:" + burstCount);

        for(int i=0; i < 5; i++){
            int fileSize = Constants.fileSizeArrayL0[i] * ThreadLocalRandom.current().nextInt(800, 1200);

            createVideoAndJSON(burstCount, i+1, fileSize);
        }
        String dummyMD = "This is MD5 file for burst " + burstCount + "\n";
        int mdSize = ThreadLocalRandom.current().nextInt(800, 1200);
        createFile(Constants.BASE_NAME + burstCount + ".md", mdSize, dummyMD);
    }

    private void createVideoAndJSON(int burstNum, int temp, int sizeOfFile){
        String fileBase= Constants.BASE_NAME + burstNum + "_L0T" + temp;
        String dataStr = "Created file with name " + fileBase + ".out\n";

        createFile(fileBase + ".out", sizeOfFile, dataStr);

        VideoData vd = new VideoData();
        vd.setCreationTime(System.currentTimeMillis()/1000);
        vd.setFileName(fileBase + ".out");
        vd.setMaxSvcLayer(1);
        vd.setMaxTemporalLayer(5);
        vd.setSequenceNumber(burstNum);
        vd.setTickets(Constants.CopyCountL0[temp]);
        vd.setTtl(24 * 3600);
        vd.addTraversedNode("injector");
        String jsonStr = vd.toString();
        byte[] data = jsonStr.getBytes();
        createFile(fileBase + ".out.json", data.length, jsonStr);
    }

    private void createFile(String name, int size, String dataStr){
        String folderName = Environment.getExternalStorageDirectory().toString() + Constants.FLDR;
        OutputStream outStr = null;
        try {
            outStr = new FileOutputStream(folderName + "/" + name);
            BufferedOutputStream output = new BufferedOutputStream(outStr, 128 * 1024);

            byte[] data = dataStr.getBytes();



            for(int i = 0; i < size; ) {
                int len = ((size -i) > data.length)? data.length: size -i;
                output.write(data, 0, len);
                i = i+len;
            }
            output.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    /*** End Burst Injection Logic ***/

    /*** START SOAK ***/
    void soakDataAndCreateAck(){
        FileModule fM = new FileModule(BridgeApp.getContext());
        String[] fileNames = fM.getFileList().split(",");
        List <String> ackedFiles = new ArrayList<String>();
        String folderBase =  Environment.getExternalStorageDirectory().toString() + Constants.FLDR;
        String folderOther = Environment.getExternalStorageDirectory().toString() + Constants.FLDR + "_Other";

        File destDirectory = new File(folderOther);
        if (!destDirectory.exists()) {
            if (destDirectory.mkdir());
        }

        long currTime = System.currentTimeMillis();
        List<AckItem> ackItemList = new ArrayList<AckItem>();

        for(int i =0; i < fileNames.length; i++){
            if(fileNames[i].length() > Constants.BASE_NAME.length() && fileNames[i].startsWith(Constants.ACK_PREFIX) == false){
                ackedFiles.add(fileNames[i]);
                File toMove = new File(folderBase + "/" + fileNames[i]);
                File dest = new File (folderOther + "/" + fileNames[i]);
                if(dest.exists()) dest.delete();
                toMove.renameTo(dest);
                AckItem ackItem = new AckItem();
                ackItem.setFilename(fileNames[i]);
                ackItem.setTime(currTime);
                ackItemList.add(ackItem);
            }
        }
        if(!ackItemList.isEmpty()) {
            Acknowledgement ack = new Acknowledgement();
            ack.setAckTime(currTime);
            ack.setItems(ackItemList);

            String ackStr = ack.toString();
            byte[] data = ackStr.getBytes();

            createFile(Constants.ACK_FILENAME + ".json", data.length, ackStr);
        }


    }


    /*** END SOAK ***/

}
