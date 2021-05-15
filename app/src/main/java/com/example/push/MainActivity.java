package com.example.push;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.SharedPreferencesCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fStore = FirebaseFirestore.getInstance();
    }


    @Override
    protected void onStart() {
        super.onStart();

        DocumentReference documentReference = fStore.collection("users").document("userId"); //저장 위치 수정 필요
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists()){
                    //notification manager 생성!
                    NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

                    String id = "message_push"; // 채널의 id 지정!
                    CharSequence name = getString(R.string.channel_name); //사용자가 볼 수 있는 채널 이름!
                    String description = getString(R.string.channel_description); // 채널에 대한 설명!

                    int importance = NotificationManager.IMPORTANCE_DEFAULT; // 중요도를 default로 설정!

                    // 채널을 생성해줍니다!
                    NotificationChannel push_channel = new NotificationChannel(id, name, importance);

                    push_channel.setDescription(description);

                    notificationManager.createNotificationChannel(push_channel); //채널을 등록해줍니다!

                    int notifyID = 1; //알림의 ID
                    String CHANNEL_ID = "message_push";

                    //알림 채널에 push라는 알림을 만들어 연결합니다!
                    Notification push = new Notification.Builder(MainActivity.this, CHANNEL_ID)
                            .setContentTitle("New Message")
                            .setContentText("장지민님으로부터 사진 도착!!") //데이터베이스에 저장된 사용자의 이름과 카테고리로 수정 필요
                            .setSmallIcon(R.drawable.bell)
                            .setChannelId(CHANNEL_ID)
                            .build();

                    notificationManager.notify(notifyID, push);
                } else {
                    Log.d("tag", "onEvent: Document do not exists");
                }
            }
        });

        getToken();
    }

    public void getToken(){ //디바이스 토큰 값을 받아오는 함수입니다!
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>(){

                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()){
                            Log.w("tag", "failed", task.getException());
                            return;
                        }
                        //디바이스 토큰을 받아옵니다!
                        String token = task.getResult();

                        //생성된 디바이스 토큰 값을 데이터베이스에 저장합니다!
                        Map<String, String> PushToken = new HashMap<>();
                        PushToken.put("Token",token);
                        fStore.collection("users").document("userId").set(PushToken); //저장 위치 수정 필요
                    }
                });
    }
}