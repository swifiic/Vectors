package in.swifiic.vectors;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView swifiicLinkText = findViewById(R.id.swifiicLink);
        TextView fiveHunderedYearsText = findViewById(R.id.fiveHundredLink);
        TextView googleFormText = findViewById(R.id.googleFormLink);
        swifiicLinkText.setMovementMethod(LinkMovementMethod.getInstance());
        fiveHunderedYearsText.setMovementMethod(LinkMovementMethod.getInstance());
        googleFormText.setMovementMethod(LinkMovementMethod.getInstance());
        //        Linkify.addLinks(swifiicLinkText, Linkify.ALL);
//        Linkify.addLinks(fiveHunderedYearsText, Linkify.ALL);
    }
}
