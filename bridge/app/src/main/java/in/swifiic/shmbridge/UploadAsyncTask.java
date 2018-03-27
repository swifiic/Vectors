package in.swifiic.shmbridge;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.arnavdhamija.common.FileModule;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


/**
 * Created by abhishek on 22/3/18.
 */

public class UploadAsyncTask extends AsyncTask<String, Integer, Integer> {

    String TAG = "UploadTsk";
    ProgressDialog pDialog;
    AppCompatActivity act;

    UploadAsyncTask(AppCompatActivity baseAct){
        this.act = baseAct;
    }

    /**
     * Before starting background thread
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("Starting upload query");

        pDialog = new ProgressDialog(this.act);
        pDialog.setMessage("Loading... Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    /**
     * Downloading file in background thread
     */
    @Override
    protected Integer doInBackground(String... f_urlIn) {
        int count;
        int offset = 0;
        try {
            String folderName = Environment.getExternalStorageDirectory().toString() + "/RoamnetData";
            FileModule fileMod = new FileModule(this.act);

            String fileList = fileMod.getFileList();

            String checkUrl = f_urlIn[0] + "/FilterUploadList.php?FilesList="+fileList;
            Log.d(TAG, "Filter Upload checking:" + checkUrl);
            URL url = new URL(checkUrl);


            String uploadListCsv = "";

            HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setReadTimeout(10000);
            httpConn.setConnectTimeout(10000);
            httpConn.connect();
            int copyCount =0;
            int httpStatus = httpConn.getResponseCode();
            if(200 == httpStatus || 201 == httpStatus){
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);//sb.append(line+"\n");
                }
                br.close();
                uploadListCsv = sb.toString();
                // Gson gson = new Gson();
                // CopyCount res = gson.fromJson(strJSon, CopyCount.class);
                // copyCount = Integer.parseInt(res.getCopycount());
            }

            // getting file length

            Log.d(TAG, "Filenames to upload:" + uploadListCsv);
            ArrayList<String> uploadFileList = new ArrayList<String>();
            String[] listSent = fileList.split(",");
            for(int i=0; i < listSent.length; i++){
                if(listSent[i].isEmpty()) continue;
                if(uploadListCsv.contains(listSent[i])) {
                    Log.i(TAG, "To try to deliver:" + listSent[i]);
                    uploadFileList.add(listSent[i]);
                } else {
                    // TODO actually create the Ack
                    Log.e(TAG, "To mark as delivered:" + listSent[i]);
                }
            }

            for(int i =0; i < uploadFileList.size(); i++) {
                String uploadUrl = f_urlIn[0] + "/PostFiles.php";
                url = new URL(uploadUrl);
                String nameOfFile = uploadFileList.get(i);
                Log.d(TAG, "Uploading:" + nameOfFile + " at url " + checkUrl);


                int serverResponseCode = 0;

                DataOutputStream dataOutputStream;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";


                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                File selectedFile = new File(folderName + "/" + nameOfFile);

                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("FileName", nameOfFile);
                connection.connect();

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"upload\"; filename=\""
                        + nameOfFile + "\"" + lineEnd);
                dataOutputStream.writeBytes("Content-Type: " + URLConnection.guessContentTypeFromName(nameOfFile) + lineEnd);
                dataOutputStream.writeBytes("Content-Transfer-Encoding: binary"+ lineEnd);



                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0) {
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);


                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();
            }

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return offset;
    }

    protected void onProgressUpdate(Integer... progress) {
        pDialog.setMessage("Progress at:" + progress[0]);
    }

    /**
     * After completing background task
     **/
    @Override
    protected void onPostExecute(Integer size) {
        Log.d(TAG,"Downloaded:" + size);

        pDialog.dismiss();
    }

}
