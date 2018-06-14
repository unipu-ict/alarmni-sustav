package com.unipu.pir_test;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {

    private Gpio mPir;
    private static final String PirPin = "BCM4";
    private static final String TAG = "MainActivity";
    private SensorCallBack callBack;
    private com.ford.openxc.webcam.WebcamPreview webcamPreview;
    private ImageView imageView;
    private Bitmap bitmap;

    StorageReference firebaseStorage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webcamPreview = (com.ford.openxc.webcam.WebcamPreview) findViewById(R.id.cp);
        imageView = (ImageView) findViewById(R.id.imageView3);

        PeripheralManager service = PeripheralManager.getInstance();

        try {
            mPir = service.openGpio(PirPin);
            mPir.setDirection(Gpio.DIRECTION_IN);
            mPir.setActiveType(Gpio.ACTIVE_HIGH);

            callBack = new SensorCallBack();

            mPir.setEdgeTriggerType(Gpio.EDGE_RISING);
            mPir.registerGpioCallback(callBack);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendImage(Bitmap bitmap){

            firebaseStorage = FirebaseStorage.getInstance().getReference();
            storageReference = firebaseStorage.child("images/photo");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = storageReference.putBytes(data);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.e("MainAcitivity", "Image successfully sent to Firebase");
                }
            });
    }


/*        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        boolean status = mPir.getValue();
                        //Log.e(TAG, "State [" + status + "]");
                        if (status) {
                            Log.e(TAG, "Detektiran pokret!");
                        }
                        Thread.sleep(5000);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
*/

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        if (mPir != null){
            mPir.unregisterGpioCallback(callBack);
            try {
                mPir.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public class SensorCallBack implements GpioCallback {
        @Override
        public boolean onGpioEdge(Gpio gpio) {

            bitmap = webcamPreview.uslikaj();
            imageView.setImageBitmap(bitmap);
            sendImage(bitmap);
            try {
                boolean callBackState = mPir.getValue();
                Log.e(TAG, "Callback state [" + callBackState + "]");
                NotificationManager.getInstance().sendNotificaton("Alarm!", "server key");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    }
}
