package in.swifiic.vectors;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView swifiicLinkText = findViewById(R.id.swifiicLink);
        TextView fiveHunderedYearsText = findViewById(R.id.fiveHundredLink);
        TextView googleFormText = findViewById(R.id.googleFormLink);
        TextView buildVersionText = findViewById(R.id.buildVersion);
        swifiicLinkText.setMovementMethod(LinkMovementMethod.getInstance());
        fiveHunderedYearsText.setMovementMethod(LinkMovementMethod.getInstance());
        googleFormText.setMovementMethod(LinkMovementMethod.getInstance());

        Date buildDate = new Date(BuildConfig.TIMESTAMP);
        String timeStamp = new SimpleDateFormat("yyyyMMdd").format(buildDate);
        String hostname = BuildConfig.BUILD_HOST;
        String buildType = "_Release";
        if (BuildConfig.DEBUG) {
            buildType = "_Debug";
        }
        String versionName = BuildConfig.VERSION_NAME;
        buildVersionText.setText("App Version: v" + versionName + "\nBuild Version: " + timeStamp + buildType + " on " + hostname);
        //        Linkify.addLinks(swifiicLinkText, Linkify.ALL);
//        Linkify.addLinks(fiveHunderedYearsText, Linkify.ALL);
    }
}
