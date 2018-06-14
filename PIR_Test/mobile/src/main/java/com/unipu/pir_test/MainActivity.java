package com.unipu.pir_test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity  {

    private Switch aSwitch;
    private ScrollView scrollView;
    private ImageView imageView;
    private FirebaseAuth mAuth;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            skiniSliku();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter("download"));

        mAuth = FirebaseAuth.getInstance();

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.photo);

        scrollView = (ScrollView) findViewById(R.id.scrollView);

        aSwitch = (Switch) findViewById(R.id.btnSwitch);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b == true){
                    FirebaseMessaging.getInstance().subscribeToTopic("alarm");
                    String tkn = FirebaseInstanceId.getInstance().getToken();
                    Toast.makeText(MainActivity.this, "Connected to Firebase!", Toast.LENGTH_SHORT).show();
                    Log.e("App", "Token [" + tkn + "]");
                } else {
                    Toast.makeText(MainActivity.this, "Cisconnected from Firebase!", Toast.LENGTH_SHORT).show();
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("alarm");
                }
            }
        });
    }

    public void skiniSliku() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReferenceFromUrl("storage location");
            storageReference.child("images/photo").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imageView.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("MainActivity", "Can't load image");
                }
            });
        } else {
            signInAnonymously();
            Log.e("MainActivity", "There's no user");
        }
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                skiniSliku();
            }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("MainActivity", "Error signInAnonymously", exception);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseMessaging.getInstance().unsubscribeFromTopic("alarm");
    }
}
