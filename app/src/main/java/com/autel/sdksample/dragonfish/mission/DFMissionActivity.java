package com.autel.sdksample.dragonfish.mission;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.autel.sdksample.R;


public class DFMissionActivity extends AppCompatActivity {
    final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Evo2 Mission");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_mission_evo2);
        findViewById(R.id.WayPoint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DFMissionActivity.this, DFWayPointActivity.class));
            }
        });
    }

}
