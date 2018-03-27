package in.swifiic.shmbridge;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by abhishek on 22/3/18.
 */

public class DownloadAsyncTask extends AsyncTask<String, Integer, Integer> {

    private static final String TAG = "DownloadTsk";
    private static final String SEPARATOR = ",";
    ProgressDialog pDialog;
    AppCompatActivity act;

    DownloadAsyncTask(AppCompatActivity baseAct){
        this.act = baseAct;
    }

    /**
     * Before starting background thread
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("Starting download");

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

            String downloadUrl = f_urlIn[0] + "/GetFile.php";

            Log.d(TAG, "Downloading from:" + downloadUrl);
            URL url = new URL(downloadUrl);

            FileModule fileMod = new FileModule(this.act);

            String fileList = fileMod.getFileList();

            HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setReadTimeout(10000);
            httpConn.setConnectTimeout(10000);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("FilesList", fileList);
            String query = builder.build().getEncodedQuery();

            OutputStream os = httpConn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            httpConn.connect();
            // getting file length
            int lenghtOfFile = httpConn.getContentLength();
            String nameOfFile = null;
            String disposition = httpConn.getHeaderField("Content-Disposition");
            if(null != disposition){
                int nameOffset = disposition.indexOf("FileName=");
                String filename= disposition.substring(nameOffset+9);
                nameOfFile = filename.replaceAll("\"", "");
            }
            if(null == nameOfFile){
                nameOfFile = "download.bin";
                String[] keyList = (String[]) httpConn.getHeaderFields().keySet().toArray();
                StringBuilder csvBuilder = new StringBuilder();
                for(String city : keyList){
                    csvBuilder.append(city);
                    csvBuilder.append(SEPARATOR);
                }
                String keySet = csvBuilder.toString();

                Log.e(TAG, "missing FileName - fields are:" + keySet);
            }

            // 128 KB buffered input stream to read
            InputStream input = new BufferedInputStream(url.openStream(), 128 * 1024);

            // Output stream to write file - buffered to reduce overhead on flash
            OutputStream outStr = new FileOutputStream(folderName + "/" + nameOfFile);
            BufferedOutputStream output = new BufferedOutputStream(outStr, 128 * 1024);

            byte data[] = new byte[1024];

            while ((count = input.read(data)) != -1) {
                output.write(data, offset, count);
                offset += count;
                publishProgress(offset);

            }
            output.close();
            input.close();

            // now get the copy count to create json - for now only the copy count
            String countUrl = f_urlIn[0] + "/GetCopyCount.php?FileName=" + nameOfFile;

            Log.d(TAG, "Getting count from:" + countUrl);
            url = new URL(countUrl);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setReadTimeout(10000);
            httpConn.setConnectTimeout(10000);


//            builder = new Uri.Builder()
//                    .appendQueryParameter("FileName", nameOfFile);
//            query = builder.build().getEncodedQuery();
//
//            os = httpConn.getOutputStream();
//            writer = new BufferedWriter(
//                    new OutputStreamWriter(os, "UTF-8"));
//            writer.write(query);
//            writer.flush();
//            writer.close();
//            os.close();

            httpConn.connect();
            int copyCount =0;
            int httpStatus = httpConn.getResponseCode();
            if(200 == httpStatus || 201 == httpStatus){
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                br.close();
                String strJSon = sb.toString();
                Gson gson = new Gson();
                CopyCount res = gson.fromJson(strJSon, CopyCount.class);
                copyCount = Integer.parseInt(res.getCopycount());
            }

            // getting file length

            Log.d(TAG, "CopyCount for filename:" + nameOfFile + " is " + copyCount);

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
