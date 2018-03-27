package in.swifiic.shmbridge;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CopyCount {

    @SerializedName("filename")
    @Expose
    private String filename;
    @SerializedName("copycount")
    @Expose
    private String copycount;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getCopycount() {
        return copycount;
    }

    public void setCopycount(String copycount) {
        this.copycount = copycount;
    }

}