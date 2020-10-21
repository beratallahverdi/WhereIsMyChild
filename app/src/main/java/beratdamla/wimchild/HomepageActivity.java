package beratdamla.wimchild;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteAggregateIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoDatabase;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class HomepageActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String GOOGLE_ACCOUNT = "google_account";
    public static final String PREFS = "PREFS";
    public static final String EMAIL = "EMAIL";
    public static final String FEMAIL = "FEMAIL";
    public static final String USER_ACCOUNT = "user_account";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomAppBar bottomAppBar;
    private FloatingActionButton fab;
    public Intent service;

    private GoogleSignInAccount account;
    private User user;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private ArrayList<User> family;
    private ArrayList<UserLocation> familyLocation;

    StitchAppClient client;
    RemoteMongoClient mongoClient;
    RemoteMongoDatabase wimchildb;
    RemoteMongoCollection<Document> users;
    RemoteMongoCollection<Document> locations;

    SharedPreferences sharedpreferences;

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "HomepageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        user = intent.getParcelableExtra(USER_ACCOUNT);

        sharedpreferences = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.putString(EMAIL,user.getEmail());
        editor.putString(FEMAIL,user.getFamilyEmail());
        editor.commit();

        service = new Intent(this, WimChildService.class);
        client = Stitch.getDefaultAppClient();
        startService(service);

        family = new ArrayList<>();
        familyLocation = new ArrayList<>();
        bottomAppBar = findViewById(R.id.bar);
        fab = findViewById(R.id.fab);
        swipeRefreshLayout = findViewById(R.id.family_swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMyFamily();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        getMyFamily();
        setUpBottomAppBar();
        setUpMaps();
    }
    public User getUser(){
        return user;
    }
    private void getMyFamily() {
        family.clear();
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
                    if(user.getMember()){
                        if(!(user.getEmail().equals(user.getFamilyEmail()))){
                            doc.append("femail", user.getFamilyEmail());
                            doc.append("is_member",true);
                        }
                        else{
                            doc.append("femail", user.getFamilyEmail());
                        }
                    }
                    else{
                        if(!(user.getEmail().equals(user.getFamilyEmail()))){
                            doc.append("email", user.getEmail());
                        }
                        else{
                            doc.append("femail", user.getFamilyEmail());
                        }
                    }
                    return users.find(doc).into(result);
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<List<Document>>() {
            @Override
            public void onComplete(@NonNull Task<List<Document>> task) {
                if(task.isSuccessful() && task.getResult().size()>0){
                    String name;
                    String surname;
                    String email;
                    String cellphone;
                    String femail;
                    boolean is_member;
                    for(int i = 0; i<task.getResult().size();i++){
                        Document a = task.getResult().get(i);
                        name = a.getString("name");
                        surname = a.getString("surname");
                        email = a.getString("email");
                        cellphone = a.getString("cellphone");
                        femail = a.getString("femail");
                        is_member = a.getBoolean("is_member");
                        family.add(i, new User(name,surname,email,femail,cellphone,is_member,null));
                    }
                    setUpFamily();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Eposta veya Sifre Hatali",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void setUpFamily(){
        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new PersonsViewAdapter(family,user);
        mRecyclerView.setAdapter(mAdapter);
        ((PersonsViewAdapter) mAdapter).setOnItemClickListener(new PersonsViewAdapter
                .MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                final int tik = position;
                if(user.getEmail().equals(user.getFamilyEmail())){
                    client.getAuth().loginWithCredential(new AnonymousCredential()).continueWithTask(new Continuation<StitchUser, Task<RemoteUpdateResult>>() {
                        @Override
                        public Task<RemoteUpdateResult> then(@NonNull Task<StitchUser> task) throws Exception {
                            if (!task.isSuccessful()) {
                                Log.e("STITCH", "Login failed!");
                                throw task.getException();
                            }
                            else{
                                mongoClient = client.getServiceClient(RemoteMongoClient.factory, "wimchildstitch");
                                users = mongoClient.getDatabase("wimchildb").getCollection("users");
                                Bson filter = new Document("email", family.get(tik).getEmail());
                                Bson newValue = new Document("is_member", !family.get(tik).getMember());
                                Bson updateOperationDocument = new Document("$set", newValue);
                                return users.updateOne(filter, updateOperationDocument);
                            }
                        }
                    }).addOnCompleteListener(new OnCompleteListener<RemoteUpdateResult>() {
                        @Override
                        public void onComplete(@NonNull Task<RemoteUpdateResult> task) {
                            Toast.makeText(getApplicationContext(),"Aileniz bu üyeyi göremeyecektir.",Toast.LENGTH_SHORT).show();
                            getMyFamily();
                        }
                    });
                }
            }
        });
    }
    private void setUpMaps(){
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MarkerOptions options = new MarkerOptions();
        ArrayList<LatLng> latlngs = new ArrayList<>();
        for(int i=0;i<familyLocation.size();i++){
            options.position(new LatLng(familyLocation.get(i).getLatitude(), familyLocation.get(i).getLongitude()));
            options.title(familyLocation.get(i).getEmail());
            mMap.addMarker(options);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(familyLocation.get(0).getLatitude(),familyLocation.get(0).getLongitude()),5));

        }

    }
    /**
     * set up Bottom Bar
     */
    private void setUpBottomAppBar() {
        //find id

        //set bottom bar to Action bar as it is similar like Toolbar
        setSupportActionBar(bottomAppBar);

        //click event over Bottom bar menu item
        bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                   /*
                   case R.id.action_notification:
                        Toast.makeText(HomepageActivity.this, "Notification clicked.", Toast.LENGTH_SHORT).show();
                        break;
                   */
                }
                return false;
            }
        });

        //click event over navigation menu like back arrow or hamburger icon
        bottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open bottom sheet
                BottomSheetDialogFragment bottomSheetDialogFragment = BottomSheetNavigationFragment.newInstance();
                bottomSheetDialogFragment.show(getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");
            }
        });
    }

    //Inflate menu to bottom bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*
            case R.id.action_notification:
                break;
            */
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * method to toggle fab mode
     *
     * @param view
     */
    public void toggleFabMode(View view) {
        View layout;
        if (bottomAppBar.getFabAlignmentMode() == BottomAppBar.FAB_ALIGNMENT_MODE_END) {
            layout = findViewById(R.id.map_homepage);
            layout.setVisibility(View.GONE);
            layout = findViewById(R.id.family_homepage);
            layout.setVisibility(View.VISIBLE);
            bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
            fab.setImageResource(R.drawable.ic_explore);
        } else {
            updateMap();
            layout = findViewById(R.id.family_homepage);
            layout.setVisibility(View.GONE);
            layout = findViewById(R.id.map_homepage);
            layout.setVisibility(View.VISIBLE);
            bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
            fab.setImageResource(R.drawable.ic_people);
        }
    }

    private void updateMap() {
        familyLocation.clear();
        mMap.clear();
        client.getAuth().loginWithCredential(new AnonymousCredential()).continueWithTask(new Continuation<StitchUser,Task<List<Document>>>() {
            @Override
            public Task<List<Document>> then(@NonNull Task<StitchUser> task) throws Exception {
                if (!task.isSuccessful()) {
                    Log.e("STITCH", "Login failed!");
                    throw task.getException();
                }
                else{
                    mongoClient = client.getServiceClient(RemoteMongoClient.factory, "wimchildstitch");
                    locations = mongoClient.getDatabase("wimchildb").getCollection("locations");
                    List<Document> result = new ArrayList<>();
                    RemoteAggregateIterable<Document> output = locations.aggregate(Arrays.asList(
                            new Document("$match", new Document("femail", user.getFamilyEmail())),
                            new Document("$sort", new Document("datetime", -1)),
                            new Document("$group", new Document("_id",new Document("email","$email"))
                                    .append("date",new Document("$first","$datetime"))
                                    .append("longitude",new Document("$first","$longitude"))
                                    .append("latitude",new Document("$first","$latitude"))
                                    .append("femail",new Document("$first","$femail"))
                                    .append("email",new Document("$first","$email"))
                            )
                    ));
                    return output.into(result);
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<List<Document>>() {
            @Override
            public void onComplete(@NonNull Task<List<Document>> task) {
                if(task.getResult().size()>0){
                    Log.i("TAG",task.getResult().toString());
                    for(int i = 0; i<task.getResult().size();i++){
                        familyLocation.add(new UserLocation(task.getResult().get(i).getString("email"),task.getResult().get(i).getString("femail"),(Double)task.getResult().get(i).get("longitude"), (Double)task.getResult().get(i).get("latitude")));
                    }
                }
                onMapReady(mMap);
            }
        });
    }

    private ArrayList<User> getDataSet() {
        ArrayList<User> results = new ArrayList<>();
        User obj;
        for (int index = 0; index < family.size(); index++) {
            obj = family.get(index);
            results.add(index, obj);
        }
        Toast.makeText(this,String.valueOf(results.size()),Toast.LENGTH_SHORT).show();
        return results;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart(){
        super.onStart();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopService(service);
    }
}
