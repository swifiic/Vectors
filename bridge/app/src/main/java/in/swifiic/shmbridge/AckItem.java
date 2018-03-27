package in.swifiic.shmbridge;

/**
 * Created by abhishek on 27/3/18.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AckItem {

    @SerializedName("filename")
    @Expose
    private String filename;
    @SerializedName("time")
    @Expose
    private Long time;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

}
