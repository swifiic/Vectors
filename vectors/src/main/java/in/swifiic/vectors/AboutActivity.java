/***************************************************************************
 *   Copyright (C) 2018 by The SWiFiIC Project <apps4rural@gmail.com>      *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA            *
 ***************************************************************************/

/***************************************************************************
 *   Code for Campus Experiments: April 2018                               *
 *   Authors: Abhishek Thakur, Arnav Dhamija, Tejashwar Reddy G            *
 ***************************************************************************/

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

        TextView privacyPolicyText = findViewById(R.id.privacyPolicyLink);
        TextView swifiicLinkText = findViewById(R.id.swifiicLink);
        TextView fiveHunderedYearsText = findViewById(R.id.fiveHundredLink);
        TextView googleFormText = findViewById(R.id.googleFormLink);
        TextView buildVersionText = findViewById(R.id.buildVersion);

        // Make the URLs clickable
        privacyPolicyText.setMovementMethod(LinkMovementMethod.getInstance());
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
    }
}
