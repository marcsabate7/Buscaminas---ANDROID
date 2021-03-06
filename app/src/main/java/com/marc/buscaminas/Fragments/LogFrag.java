package com.marc.buscaminas.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.marc.buscaminas.AuxiliarStructures.Datalog;
import com.marc.buscaminas.R;

public class LogFrag extends Fragment {
    public static String CASELLES_SELECCIONADES, DATA_LOG;
    private TextView data, caselles;
    private String messageDades, casselles = "";

    public LogFrag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CASELLES_SELECCIONADES = getResources().getString(R.string.CasellesSeleccionades);
        DATA_LOG = getResources().getString(R.string.Data_Log);

        if (savedInstanceState != null) {
            casselles = savedInstanceState.getString(CASELLES_SELECCIONADES);
            messageDades = savedInstanceState.getString(DATA_LOG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.frag_log, container, false);

    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        data = (TextView) getView().findViewById(R.id.dades_log);
        caselles = (TextView) getView().findViewById(R.id.casillaSeleccionada);
        caselles.setText(casselles);
        data.setText(messageDades);
    }

    public void mostrarDetalle(Datalog datalog) {
        messageDades = "ALIAS: " + datalog.getDadesDePartida().getUserName();
        messageDades += " / NUMERO CASILLAS: " + datalog.getDadesDePartida().getNumero_graella();
        messageDades += " / MINAS: " + datalog.getDadesDePartida().getPercentatge() + "%";
        if (datalog.getDadesDePartida().isHave_timer()) {
            messageDades += " / TIEMPO: " + datalog.getDadesDePartida().getTime();
        } else {
            messageDades += " / TIEMPO: No hay tiempo";
        }
        data.setText(messageDades);

        if (datalog.getDadesDePartida().isHave_timer()) {
            casselles = caselles.getText().toString() + "\nCasilla Seleccionada = (" + datalog.getCoordX() + "," + datalog.getCoordY() + ")" + " - Time: " + datalog.getTiempo_restante() / 1000 + "s";
            caselles.setText(casselles);
        } else {
            casselles = caselles.getText().toString() + "\nCasilla Seleccionada = (" + datalog.getCoordX() + "," + datalog.getCoordY() + ")" + " - Time: No hay tiempo";
            caselles.setText(casselles);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CASELLES_SELECCIONADES, caselles.getText().toString());
        outState.putString(DATA_LOG, data.getText().toString());
    }
}