package com.racing.newracingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int RC_LOCATION_REQUEST = 1234;
    private FloatingActionButton initRace_fab, joinRace_fab;
    private AlertDialog alertDialog;
    private List<String> raceIdList;
    private DatabaseReference dbRefUser;
    private RecyclerView race_rv;
    private String userId;
    private List<RaceModel> useRaceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userId = Settings.System.getString(MainActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);

        initRace_fab = findViewById(R.id.initiateRace_fab);
        joinRace_fab = findViewById(R.id.joinRace_fab);
        race_rv = findViewById(R.id.race_rv);

        race_rv.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        race_rv.setHasFixedSize(true);
        dbRefUser = FirebaseDatabase.getInstance().getReference().child("myRacingApp").child("Users");
        populateRace_rv();
        buttonClickFunctions();


    }

    private void populateRace_rv() {
        dbRefUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                useRaceList = new ArrayList<>();
                for(DataSnapshot ds:snapshot.child(userId).getChildren())
                {
                    useRaceList.add(new RaceModel(ds.getValue(String.class),ds.getKey()));
                }
                RaceAdapter raceAdapter = new RaceAdapter(useRaceList,MainActivity.this);
                race_rv.setAdapter(raceAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void buttonClickFunctions() {
        initRace_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION }, RC_LOCATION_REQUEST );
                }
                else {

                    LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                    View alertView = layoutInflater.inflate(R.layout.alert_init_race, null);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.
                            this);
                    Button ok_bt = (Button) alertView.findViewById(R.id.alert_startRace_bt);
                    EditText raceId_et = alertView.findViewById(R.id.raceId_et);
                    EditText raceTitle_et = alertView.findViewById(R.id.raceTitle_et);
                    Button cancel_bt = (Button) alertView.findViewById(R.id.cancel_bt);

                    ok_bt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String raceTitle = raceTitle_et.getText().toString();

                            if (!raceTitle.equals("")){
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("myRacingApp");
                                final String raceId = ref.push().getKey();
                                Intent i = new Intent(MainActivity.this, LocationTrackerService.class);
                                i.putExtra("raceId",raceId);
                                dbRefUser.child(userId).child(raceId).setValue(raceTitle);
                                startService(i);
                                alertDialog.dismiss();
                                finish();
                                startActivity(getIntent());
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Please Enter Title", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    cancel_bt.setOnClickListener(view -> alertDialog.dismiss());
                    alertDialogBuilder.setView(alertView);
                    alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                }
            }
        });
        joinRace_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                View alertView = layoutInflater.inflate(R.layout.alert_custom, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.
                        this);
                final EditText raceId_et = (EditText) alertView.findViewById(R.id.raceId_et);
                Button okButton = (Button) alertView.findViewById(R.id.btnJoin);
                Button cancelButton = (Button) alertView.findViewById(R.id.btncancel);

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION }, RC_LOCATION_REQUEST );
                        }
                        else {

                        String raceIdd = raceId_et.getText().toString();
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("myRacingApp");
                        dbRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                raceIdList = new ArrayList<>();

                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    raceIdList.add(ds.getKey());
                                }

                               if (raceIdList.contains(raceIdd)){
                                   Intent i = new Intent(MainActivity.this, LocationTrackerService.class);
                                   i.putExtra("raceId",raceIdd);
                                   dbRefUser.child(userId).child(raceIdd).setValue(raceIdd);
                                   startService(i);
                                   alertDialog.dismiss();
                                }
                                else {
                                    Toast.makeText(MainActivity.this, "No such race Id existed", Toast.LENGTH_SHORT).show();
                                }
                                alertDialog.dismiss();
                                finish();
                                startActivity(getIntent());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        }



                    }
                });
                cancelButton.setOnClickListener(view -> alertDialog.dismiss());
                alertDialogBuilder.setView(alertView);
                alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        switch (requestCode) {
//            case 1:
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //permission with request code 1 granted
//                    Toast.makeText(this, "Permission Granted" , Toast.LENGTH_LONG).show();
//                    Intent i = new Intent(this, LocationTrackerService.class);
//                    startService(i);
//                } else {
//                    //permission with request code 1 was not granted
//                    Toast.makeText(this, "Permission was not Granted" , Toast.LENGTH_LONG).show();
//                }
//                break;
//            default:
//                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//    }

}