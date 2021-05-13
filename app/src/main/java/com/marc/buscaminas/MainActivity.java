package com.marc.buscaminas;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Intent toAyuda, toConfiguration;
    private Switch switchMusic;
    public final static int CLOSE_ALL = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnAyuda = (Button) findViewById(R.id.buttonAyuda);
        Button btnEmpezar = (Button) findViewById(R.id.buttonIniciar);
        Button btnSalir = (Button) findViewById(R.id.buttonsalir);
        switchMusic = (Switch) findViewById(R.id.switchMusic);

        switchMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });
        btnAyuda.setOnClickListener(this);
        btnEmpezar.setOnClickListener(this);
        btnSalir.setOnClickListener(this);

        toAyuda = new Intent(this,AyudaActivity.class);
        toConfiguration = new Intent(this,Configuration.class);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.buttonAyuda:
                startActivity(toAyuda);
                finish();
                break;
            case R.id.buttonIniciar:
                if(switchMusic.isChecked())
                    toConfiguration.putExtra("Music","ON");
                else
                    toConfiguration.putExtra("Music","OFF");
                toConfiguration.addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(toConfiguration);
                finish();
                break;
            case R.id.buttonsalir:
                exitAppCLICK(v);
                break;
        }
    }

    public void exitAppCLICK (View view) {
        finishAffinity();
        System.exit(0);
    }
}