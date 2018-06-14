package com.unipu.pir_test;

import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NotificationManager {
    private static NotificationManager me = null;

    private NotificationManager() {}

    public static NotificationManager getInstance() {
        if (me == null)
            me = new NotificationManager();

        return me;
    }

    public void sendNotificaton(String message, String key) {

        (new FirebaseNotificationTask()).execute(new String[]{message, key});
    }

    private class FirebaseNotificationTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String msg = strings[0];
            String key = strings[1];
            Log.d("Alm", "Send data");
            try {
                HttpURLConnection con = (HttpURLConnection) (new URL("http://fcm.googleapis.com/fcm/send")).openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Authorization", "key=" + key);
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoInput(true);
                con.setDoOutput(true);
                con.connect();

                String body = "{\n" +
                        "  \"to\": \"/topics/alarm\",\n" +
                        "  \"data\": {\n" +
                        "  \"message\": \"" + msg + "\"" +
                        "  }\n" +
                        "}";
                Log.d("Alm", "Body ["+body+"]");
                con.getOutputStream().write(body.getBytes());
                InputStream is = con.getInputStream();
                byte[] buffer = new byte[1024];
                while ( is.read(buffer) != -1)
                    Log.d("Alm", new String(buffer));
                con.disconnect();
            }

            catch(Throwable t) {
                t.printStackTrace();
            }

            return null;
        }
    }
}
