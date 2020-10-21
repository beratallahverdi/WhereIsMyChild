package beratdamla.wimchild;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoDatabase;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    GoogleSignInAccount account;
    final StitchAppClient client = Stitch.initializeDefaultAppClient("wimchild-hvezp");
    RemoteMongoClient mongoClient;
    RemoteMongoDatabase wimchildb;
    RemoteMongoCollection<Document> users;
    RemoteMongoCollection<Document> locations;
    String email_login;
    String femail_login;
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPreferences sharedPref = getSharedPreferences(HomepageActivity.PREFS,Context.MODE_PRIVATE);
        email_login = sharedPref.getString(HomepageActivity.EMAIL,"NONE");
        femail_login = sharedPref.getString(HomepageActivity.FEMAIL,"NONE");

        if(!email_login.equals("NONE")&& !femail_login.equals("NONE")){
            onNormalLoggedIn();
        }
        else{
            SplashTimer();
        }
    }
    private void SplashTimer(){
        new CountDownTimer(3000, 1000) {
            @Override

            public void onTick(long millisUntilFinished) { }
            @Override
            public void onFinish() {
                toLogin();
            }
        }.start();
    }
    private void toLogin(){
        startActivity(new Intent(this, LoginActivity.class));
        this.finish();
    }
    @Override
    protected void onStart(){
        super.onStart();

        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            //Toast.makeText(this,account.getDisplayName()+" "+account.getEmail(),Toast.LENGTH_SHORT).show();
            //toLogin();
            toLogin();
        }
    }
    private void onNormalLoggedIn(){

        client.getAuth().loginWithCredential(new AnonymousCredential()).continueWithTask(new Continuation<StitchUser, Task<List<Document>>>() {
            @Override
            public Task<List<Document>> then(@NonNull Task<StitchUser> task) throws Exception {
                if(!task.isSuccessful()){
                    Log.e("STITCH","Login Failed");
                    throw task.getException();
                }
                else{
                    mongoClient = client.getServiceClient(RemoteMongoClient.factory, "wimchildstitch");
                    users = mongoClient.getDatabase("wimchildb").getCollection("users");
                    List<Document> result = new ArrayList<>();
                    final Document doc = new Document();
                    doc.append("email", email_login);
                    doc.append("femail", femail_login);
                    return users.find(doc).into(result);
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<List<Document>>() {
            @Override
            public void onComplete(@NonNull Task<List<Document>> task) {
                if(task.isSuccessful() && task.getResult().size()>0){
                    String name = task.getResult().get(0).getString("name");
                    String surname = task.getResult().get(0).getString("surname");
                    String email = task.getResult().get(0).getString("email");
                    String cellphone = task.getResult().get(0).getString("cellphone");
                    String femail = task.getResult().get(0).getString("femail");
                    boolean is_member = task.getResult().get(0).getBoolean("is_member");
                    setAccount(new User(name,surname,email,femail,cellphone,is_member,null));
                }
            }
        });
    }
    private void onLoggedIn() {
        client.getAuth().loginWithCredential(new AnonymousCredential()).continueWithTask(new Continuation<StitchUser, Task<List<Document>>>() {
            @Override
            public Task<List<Document>> then(@NonNull Task<StitchUser> task) throws Exception {
                mongoClient = client.getServiceClient(RemoteMongoClient.factory, "wimchildstitch");
                wimchildb = mongoClient.getDatabase("wimchildb");
                users = wimchildb.getCollection("users");
                if(!task.isSuccessful()){
                    Log.e("STITCH","Login Failed");
                    throw task.getException();
                }
                else{
                    List<Document> result = new ArrayList<>();
                    return users.find(new Document("email",account.getEmail())).into(result);
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<List<Document>>() {
            @Override
            public void onComplete(@NonNull Task<List<Document>> task) {
                if(task.isSuccessful()){
                    String name = task.getResult().get(0).getString("name");
                    String surname = task.getResult().get(0).getString("surname");
                    String email = task.getResult().get(0).getString("email");
                    String cellphone = task.getResult().get(0).getString("cellphone");
                    String femail = task.getResult().get(0).getString("femail");
                    boolean is_member = task.getResult().get(0).getBoolean("is_member");
                    setAccount(new User(name,surname,email,femail,cellphone,is_member,account.getPhotoUrl()));
                }
            }
        });
    }
    public void setAccount(User a){
        Intent intent = new Intent(this, HomepageActivity.class);
        Bundle bundle = new Bundle();
        intent.putExtra(HomepageActivity.USER_ACCOUNT,a);
        startActivity(intent);
        finish();
    }
}
