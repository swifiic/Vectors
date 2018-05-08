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

public final class Constants {
    public static final String BROADCAST_ACTION = "swifiic.vectors.android.BROADCAST";
    public static final String LOG_STATUS = "swifiic.vectors.android.LOG_STATUS";
    public static final String CONNECTION_STATUS = "swifiic.vectors.android.CONNECTION_STATUS";
    public static final String ANDROID_BOOT_COMPLETION = "android.intent.action.BOOT_COMPLETED";
    public static final String USER_EMAIL_ID = "swifiic.vectors.android.USER_EMAIL_ID";

    public static final String STATUS_ENABLE_BG_SERVICE = "connectionStatus";
    public static final String DEVICE_ID = "DEVICE_ID";
    public static final String PAYLOAD_PREFIX = "video";
    public static final String CONNECTION_LOG_FILENAME = "ConnectionLog";
    public static final String LOGGER_FILENAME = "LogFile";
    public static final String FLDR =  "/VectorsData";
    public static final String FOLDER_LOG = "/VectorsLogs";
    public static final String ACK_PREFIX = "ack_";
    public static final String BASE_NAME =  "video_00";
    public static final String ENDPOINT_PREFIX = "Vectors_";
    public static final String ACK_FILENAME = ACK_PREFIX + BASE_NAME;
    
    public static final int MIN_CONNECTION_GAP_TIME = 60;
    public static final int LOG_BUFFER_SIZE = 200;
    public static final int LOG_TEXT_VIEW_LINES = 1000;
    public static final int DELAY_TIME_MS = 10;
    public static final int RESTART_NEARBY_SECS = 300;
    public static final int FILE_BUFFER_SIZE = 1024*16;
    public static final int ACK_BUFFER_SIZE = 1024*128;
}
