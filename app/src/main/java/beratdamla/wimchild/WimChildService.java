package beratdamla.wimchild;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;

import org.bson.Document;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class WimChildService extends Service{

    StitchAppClient client;
    RemoteMongoClient mongoClient;
    RemoteMongoCollection<Document> locations;

    SharedPreferences sharedpreferences;
    String email_service;
    String femail_service;

    LocationManager lctManager;
    Location location;
    double longitude;
    double latitude;
    SimpleDateFormat sdf;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        client = Stitch.getAppClient(getString(R.string.stitch_id));
        lctManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        sharedpreferences = getSharedPreferences(HomepageActivity.PREFS, Context.MODE_PRIVATE);
        email_service = sharedpreferences.getString(HomepageActivity.EMAIL,"NONE");
        femail_service = sharedpreferences.getString(HomepageActivity.FEMAIL,"NONE");
        Log.d("hesap",email_service);
        mTimer = new Timer();
        mTimer.schedule(timerTask,5000, 5000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private Timer mTimer;

    TimerTask timerTask = new TimerTask() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

            Log.e("Log", "Running");
            location = lctManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(!email_service.equals("NONE")){
                client.getAuth().loginWithCredential(new AnonymousCredential()).continueWithTask(
                        new Continuation<StitchUser, Task<RemoteInsertOneResult>>() {
                            @Override
                            public Task<RemoteInsertOneResult> then(@NonNull Task<StitchUser> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    Log.e("STITCH", "Login failed!");
                                    throw task.getException();
                                }
                                mongoClient = client.getServiceClient(RemoteMongoClient.factory, "wimchildstitch");
                                locations = mongoClient.getDatabase("wimchildb").getCollection("locations");
                                Document newuser = new Document("owner_id", client.getAuth().getUser().getId());
                                newuser.put("email",email_service);
                                newuser.put("femail",femail_service);
                                newuser.put("longitude",location.getLongitude());
                                newuser.put("latitude",location.getLatitude());
                                newuser.put("datetime",sdf.format(new Date()));
                                return locations.insertOne(newuser);
                            }
                        }
                ).addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
                    @Override
                    public void onComplete(@NonNull Task<RemoteInsertOneResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Konum", "Veri Kaydedildi");
                            return;
                        }
                        else{
                            Log.d("Konum", task.getException().getMessage());
                            task.getException().printStackTrace();
                        }
                    }
                });
            }
        }
    };
    @Override
    public void onTaskRemoved (Intent rootIntent){
        super.onTaskRemoved(rootIntent);
    }
    @Override
    public void onDestroy() {
        this.stopSelf();
        try {
            mTimer.cancel();
            timerTask.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent("beratdamla.wimchild.receiver");
        intent.putExtra("yourvalue", "torestore");
        sendBroadcast(intent);
    }
}
