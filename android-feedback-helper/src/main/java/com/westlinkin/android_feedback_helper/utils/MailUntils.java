/*
 * Copyright 2015 Wesley Lin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.westlinkin.android_feedback_helper.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.westlinkin.android_feedback_helper.Configuration;
import com.westlinkin.android_feedback_helper.module.Data;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.IllegalFormatException;

/**
 * Created by Wesley Lin on 3/23/15.
 */
public class MailUntils {

    // mail subject, need app_name and case number to format
    private static final String MAIL_SUBJECT = "Feedback about %s #%s";

    public static String getMailSubject(String appName) {
        try {
            return String.format(MAIL_SUBJECT, appName, getFeedbackNumber());
        } catch (IllegalFormatException e) {
            return String.format(MAIL_SUBJECT, "Unknown app", "0000");
        }
    }

    private static String getFeedbackNumber() {
        Calendar calendar = Calendar.getInstance();
        return "" + calendar.get(Calendar.YEAR)
                + Utils.formatCalendarIntValue((calendar.get(Calendar.MONTH) + 1), 2)
                + Utils.formatCalendarIntValue(calendar.get(Calendar.DAY_OF_MONTH), 2)
                + "-"
                + Utils.formatCalendarIntValue(calendar.get(Calendar.HOUR_OF_DAY), 2)
                + Utils.formatCalendarIntValue(calendar.get(Calendar.MINUTE), 2)
                + Utils.formatCalendarIntValue(calendar.get(Calendar.SECOND), 2)
                + "-"
                + Utils.formatCalendarIntValue(calendar.get(Calendar.MILLISECOND), 3);
    }

    public static String emailPwdProcessor(String pwdInValue) {
        return Utils.getHashMD5(pwdInValue);
    }

    public static String emailNameProcessor(String nameInValue) {
        return nameInValue + Configuration.DOMAIN_GMAIL;
    }

    public static String getMailBody(Context context, String msg, String userEmail) {
        String html = "";
        try {
            html = IOUtils.toString(context.getAssets().open("android_feedback_helper.html"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (html.isEmpty())
            return null;

        // version name and code
        PackageManager packageManager = context.getPackageManager();
        String versionName = "Unknown";
        int versionCode = -1;
        if (packageManager != null) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
                versionName = packageInfo.versionName;
                versionCode = packageInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss_zzz");
        String time = sdf.format(now);

        return html.replaceAll(Data.APP_PACKAGE, context.getPackageName())
                .replaceAll(Data.VERSION_NAME, versionName)
                .replaceAll(Data.VERSION_CODE, String.valueOf(versionCode))
                .replaceAll(Data.FEEDBACK_TIME, time)
                .replaceAll(Data.FEEDBACK_MESSAGE, msg)
                .replaceAll(Data.USER_EMAIL, userEmail);
    }
}
