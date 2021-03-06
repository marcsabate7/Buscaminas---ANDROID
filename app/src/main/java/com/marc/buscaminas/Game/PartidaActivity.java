package com.marc.buscaminas.Game;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import com.marc.buscaminas.AuxiliarStructures.Datalog;
import com.marc.buscaminas.AuxiliarStructures.Matriu;
import com.marc.buscaminas.Fragments.GridFrag;
import com.marc.buscaminas.Fragments.LogFrag;
import com.marc.buscaminas.Music.SoundTrackService;
import com.marc.buscaminas.PopUpDialog.PopUpBomb;
import com.marc.buscaminas.PopUpDialog.PopUpTimeOut;
import com.marc.buscaminas.PopUpDialog.PopUpWin;
import com.marc.buscaminas.R;
import com.marc.buscaminas.AuxiliarStructures.DadesDePartida;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class PartidaActivity extends AppCompatActivity implements GridFrag.CellListener {

    /**
     * De més, s'ha implementat l'opció que l'usuari col·loqui banderes on cregui que hi ha una bomba. També s'ha configurat
     * la partida de manera que quan s'acabi el joc, es mostrin les posicions de les bombes perquè l'usuari no es quedi amb l'intriga.
     * Un cop acabada la partida, es deshabiliten els botons de la graella per evitar que es repeteixin accions que només s'han de dur
     * a terme un cop i així evitar actuacions d'error de l'aplicació. Com a transicions s'han incorporat uns Dialogs per donar feedback
     * a l'usuari i millorar la seva experiència.
     */

    public static String DADES, CASILLAS_POR_DESCUBRIR, GAMEOVER, MUSIC, ON, TIEMPO_RESTANTE, CASILLAS_RESTANTES,
            ARRAY_ORIENTATION, LIST_OF_BOMBS, FLAGS_POSADES, IS_FINISHED, PARTIDA_STATUS, RECEIVED_MUSIC, USER_NAME, TIEMPO_TOTAL,
            CASILLAS_TOTALES, PORCENTAGE_MINAS_ELEGIDO, TOTAL_MINAS, START;

    private Intent receivedIntent, toActivityFinal;
    private int[][] matrix;
    private int[] drawableOfNumbers, list_orientation, array_caught, list_of_flags, flags_caught;
    private DadesDePartida receivedData;
    private ArrayList<Integer> listOfBombsIndexes;
    private HashMap<Integer, ImageButton> copyofviews = new HashMap<>();
    private int numberOfcolumns, num_cells;
    private Intent toStopService;
    private GridView graella;
    private CustomAdapter gridAdapter;
    private long tiempo_restante;
    private float percentage_bombs;
    private String user_name, timeString;
    private CountDownTimer time;
    private boolean game_finished = false, is_change_orientation, havetimer, have_music;
    private TextView num_casillas, timer, titol_partida;
    private Datalog datalog;
    private Matriu matriu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.partida);


        DADES = getResources().getString(R.string.DadesDePartida);
        CASILLAS_POR_DESCUBRIR = getResources().getString(R.string.CasillasPorDescubrir);
        GAMEOVER = getResources().getString(R.string.GAMEOVER);
        MUSIC = getResources().getString(R.string.Music);
        ON = getResources().getString(R.string.On);
        TIEMPO_RESTANTE = getResources().getString(R.string.TiempoRestante);
        CASILLAS_RESTANTES = getResources().getString(R.string.CasillasRestantes);
        ARRAY_ORIENTATION = getResources().getString(R.string.ArrayOrientation);
        LIST_OF_BOMBS = getResources().getString(R.string.ListBombs);
        FLAGS_POSADES = getResources().getString(R.string.FlagsPosades);
        IS_FINISHED = getResources().getString(R.string.IsFinished);
        PARTIDA_STATUS = getResources().getString(R.string.PartidaStatus);
        RECEIVED_MUSIC = getResources().getString(R.string.ReceivedMusic);
        USER_NAME = getResources().getString(R.string.UserNameKEY);
        TIEMPO_TOTAL = getResources().getString(R.string.TiempoTotal);
        CASILLAS_TOTALES = getResources().getString(R.string.CasillasTotales);
        PORCENTAGE_MINAS_ELEGIDO = getResources().getString(R.string.PercentatgeEscollitMines);
        TOTAL_MINAS = getResources().getString(R.string.TotalMinas);
        START = getResources().getString(R.string.start);

        num_casillas = (TextView) findViewById(R.id.casillasid);
        timer = (TextView) findViewById(R.id.timer);
        titol_partida = (TextView) findViewById(R.id.title_partidaenmarxa);

        toStopService = new Intent(this, SoundTrackService.class);
        receivedIntent = getIntent();

        // Intent que passarem cap activity final
        toActivityFinal = new Intent(this, FinalActivity.class);
        if (receivedIntent.getStringExtra(MUSIC) != null && receivedIntent.getStringExtra(MUSIC).equals(ON)) {
            have_music = true;
            toActivityFinal.putExtra(RECEIVED_MUSIC, ON);
        }

        receivedData = receivedIntent.getExtras().getParcelable(DADES);
        user_name = receivedData.getUserName();
        numberOfcolumns = receivedData.getNumero_graella();
        percentage_bombs = receivedData.getPercentatge();
        havetimer = receivedData.isHave_timer();
        if ((havetimer = receivedData.isHave_timer()))
            timeString = receivedData.getTime();

        // Partida text en negreta
        SpannableString mitextoU = new SpannableString("PARTIDA EN MARXA, " + user_name.toUpperCase() + "!!");
        mitextoU.setSpan(new UnderlineSpan(), 0, mitextoU.length(), 0);
        titol_partida.setText(mitextoU);

        // Tractament de les flags
        list_orientation = new int[numberOfcolumns * numberOfcolumns];
        list_of_flags = new int[numberOfcolumns * numberOfcolumns];
        for (int i = 0; i < list_orientation.length; i++) {
            list_orientation[i] = -1;
            list_of_flags[i] = -1;
        }
        // OnSavedInstanceState per si el usuari gira la pantalla recuperarem les dades de la partida pertinents
        if (savedInstanceState != null) {
            num_cells = savedInstanceState.getInt(CASILLAS_RESTANTES);
            tiempo_restante = savedInstanceState.getLong(TIEMPO_RESTANTE);
            array_caught = savedInstanceState.getIntArray(ARRAY_ORIENTATION);
            matriu = savedInstanceState.getParcelable("matriu");
            listOfBombsIndexes = (ArrayList<Integer>) matriu.getBombs_index_list();
            num_casillas.setText(CASILLAS_POR_DESCUBRIR + num_cells);
            flags_caught = savedInstanceState.getIntArray(FLAGS_POSADES);
            game_finished = savedInstanceState.getBoolean(IS_FINISHED);
            is_change_orientation = true;
        }else{
            matriu = new Matriu(numberOfcolumns, percentage_bombs);
            listOfBombsIndexes = (ArrayList<Integer>) matriu.getBombs_index_list();
            num_cells = (numberOfcolumns * numberOfcolumns) - listOfBombsIndexes.size();
            num_casillas.setText(CASILLAS_POR_DESCUBRIR + num_cells);
        }
        drawableOfNumbers = matriu.initialize_drawableOfNumbers();

        // Inicialització del grid view
        graella = (GridView) findViewById(R.id.gridview);
        gridAdapter = new CustomAdapter(this, numberOfcolumns * numberOfcolumns);
        graella.setAdapter(gridAdapter);
        graella.setNumColumns(numberOfcolumns);


        // Controlem l'orientació de la pantalla ja que ens es util en una funció implementada al final del codi
        if (is_change_orientation == false && havetimer)
            tiempo_restante = timechoice(timeString);


        if (havetimer) {
            num_casillas.setText(CASILLAS_POR_DESCUBRIR + num_cells);
            num_casillas.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
        } else {
            num_casillas.setText(CASILLAS_POR_DESCUBRIR + num_cells);
            num_casillas.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
        }

    }

    // Adapter del gridView
    private class CustomAdapter extends BaseAdapter {
        private Context context;
        private ImageButton cell;
        private int numberOfCells;
        private LayoutInflater inflter;
        private Drawable defaultbackgrond;

        public CustomAdapter(Context c, int numberOfCells) {
            this.context = c;
            inflter = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.numberOfCells = numberOfCells;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                LogFrag logFrag = (LogFrag) getSupportFragmentManager().findFragmentById(R.id.fraglog);
                if (logFrag != null && logFrag.isInLayout())
                    view = inflter.inflate(R.layout.row_datalarge, null);
                else
                    view = inflter.inflate(R.layout.row_data, null);
            }

            cell = (ImageButton) view.findViewById(R.id.buttoninGrid);
            defaultbackgrond = cell.getBackground();

            if (is_change_orientation) {
                if (array_caught[position] != -1) {
                    cell.setBackgroundResource(drawableOfNumbers[array_caught[position]]);
                    list_orientation[position] = array_caught[position];
                    cell.setEnabled(false);
                    cell.setClickable(false);
                }
                if (flags_caught[position] == 0) {
                    list_of_flags[position] = 0;
                    cell.setBackgroundResource(R.drawable.blueflag);
                }
            }

            cell.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if (view.getBackground().getConstantState().equals(getDrawable(R.drawable.blueflag).getConstantState())) {
                        if (list_orientation[position] != -1) {
                            view.setBackgroundResource(drawableOfNumbers[list_orientation[position]]);
                        } else
                            view.setBackground(defaultbackgrond);
                        list_of_flags[position] = -1;
                    } else {
                        view.setBackgroundResource(R.drawable.blueflag);
                        list_of_flags[position] = 0;
                    }

                    if (havetimer) {
                        num_casillas.setText(CASILLAS_POR_DESCUBRIR + num_cells);
                        num_casillas.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                    } else {
                        num_casillas.setText(CASILLAS_POR_DESCUBRIR + num_cells);
                        num_casillas.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
                    }
                    return true;
                }
            });

            cell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position_x = position / numberOfcolumns;
                    int position_y = position % numberOfcolumns;
                    datalog = new Datalog(receivedData, position_x, position_y, tiempo_restante);
                    onCasillaSeleccionada(datalog);

                    System.out.println("\n" + position + "\n");
                    if (listOfBombsIndexes.contains(position)) {
                        view.setBackgroundResource(R.drawable.ic_bomb2);
                        timer.setText(GAMEOVER);
                        view.setEnabled(false);
                        view.setClickable(false);
                        changeActivityToFinal(2, position);

                    } else {

                        if (list_orientation[position] == -1) {
                            num_cells--;
                        }

                        if (havetimer) {

                            num_casillas.setText(CASILLAS_POR_DESCUBRIR + num_cells);
                            num_casillas.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                        } else {
                            num_casillas.setText(CASILLAS_POR_DESCUBRIR + num_cells);
                            num_casillas.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
                        }
                        if (num_cells == 0) {
                            // PARTIDA GUANYADA
                            changeActivityToFinal(3, 0);
                        }
                        int counter = matriu.numberSurroundingBombs(matriu.getMatrix(), position);
                        //int counter = numberSurroundingBombs(matrix, position);
                        list_orientation[position] = counter;
                        view.setBackgroundResource(drawableOfNumbers[counter]);
                    }
                    view.setClickable(false);
                    view.setEnabled(false);
                }
            });

            copyofviews.put(position, cell);
            return view;
        }

        @Override
        public int getCount() {
            return this.numberOfCells;
        }

        @Override
        public Object getItem(int i) {
            return copyofviews.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }
    }


    @Override
    public void onCasillaSeleccionada(Datalog datalog) {
        LogFrag logFrag = (LogFrag) getSupportFragmentManager().findFragmentById(R.id.fraglog);
        boolean hayLog = (logFrag != null && logFrag.isInLayout());
        if (hayLog) {
            logFrag.mostrarDetalle(datalog);
        }
    }

    // Aquesta funció s'utilitza per pasar les dades de la partida al intent i aquest cap al activity final amb un status que controlara si la aprtida s'ha guanyat / per
    // Falta bloquejar el grid view quan s'ensenyen les bombes per a que el usuari no pugui clicar a cap item de la graella
    @SuppressLint("WrongConstant")
    public void changeActivityToFinal(int status_partida, int position) {
        stopService(toStopService);
        if (time != null) {
            time.cancel();
        }

        toActivityFinal.putExtra(USER_NAME, user_name);
        toActivityFinal.putExtra(CASILLAS_TOTALES, numberOfcolumns * numberOfcolumns);
        toActivityFinal.putExtra(PORCENTAGE_MINAS_ELEGIDO, percentage_bombs);
        int num_minas = (int) ((numberOfcolumns * numberOfcolumns) * percentage_bombs);
        toActivityFinal.putExtra(TOTAL_MINAS, num_minas);
        toActivityFinal.putExtra(TIEMPO_TOTAL, (tiempo_restante) / 1000);
        toActivityFinal.putExtra(CASILLAS_RESTANTES, num_cells);

        final Handler handler2 = new Handler();
        Timer tt = new Timer();
        tt.schedule(new TimerTask() {
            public void run() {
                handler2.post(new Runnable() {
                    public void run() {
                        for (int i = 0; i < copyofviews.size(); i++) {
                            copyofviews.get(0).setClickable(false);
                            copyofviews.get(0).setEnabled(false);
                            copyofviews.get(i).setClickable(false);
                            copyofviews.get(i).setEnabled(false);
                        }
                        for (int i = 0; i < listOfBombsIndexes.size(); i++) {
                            copyofviews.get(listOfBombsIndexes.get(i)).setBackgroundResource(R.drawable.ic_bomb2);
                        }
                    }
                });
            }
        }, 250);

        // Estatus == 1 per a partides on s'acabe el temps
        if (status_partida == 1) {
            timer.setText(GAMEOVER);
            MediaPlayer game_over_sound = MediaPlayer.create(this, R.raw.gameover);
            game_over_sound.start();
            delayPopups(5000, status_partida);
            toActivityFinal.putExtra(PARTIDA_STATUS, "Ha perdido la partida porque se ha agotado el tiempo...!!, Te han quedado " + num_cells + CASILLAS_POR_DESCUBRIR);
        }
        // Estatus == 2 per a partides on s'ha clicat a una bomba
        if (status_partida == 2) {
            MediaPlayer boom = MediaPlayer.create(this, R.raw.boomsound);
            boom.start();
            delayPopups(5000, status_partida);
            game_finished = true;
            int position_x = position / numberOfcolumns;
            int position_y = position % numberOfcolumns;
            toActivityFinal.putExtra(PARTIDA_STATUS, "Has perdido!! Bomba en casilla " + position_x + ", " + position_y + ".\n" + "Te han quedado " + num_cells + " " + CASILLAS_POR_DESCUBRIR);
        }
        // Estatus == 3 per a partides guanyades
        if (status_partida == 3) {

            MediaPlayer victory = MediaPlayer.create(this, R.raw.victory);
            victory.start();
            delayPopups(5000, status_partida);

            toActivityFinal.putExtra(CASILLAS_RESTANTES, num_cells);
            if (receivedData.isHave_timer()) {
                toActivityFinal.putExtra(PARTIDA_STATUS, "Has ganado!! Y te han sobrado " + (tiempo_restante) / 1000 + " segundos!");
            } else {
                toActivityFinal.putExtra(PARTIDA_STATUS, "Has ganado!! Sin control de tiempo!!");
            }
        }

        toActivityFinal.putExtra(DADES, receivedData);
        final Handler handler = new Handler();
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        // Afegim flags al intent per controlar les activitats obertes
                        toActivityFinal.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(toActivityFinal);
                        finish();
                    }
                });
            }
        }, 6000);
    }

    // Funció que mostra un dialog si volem tirar cap a radere
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.quitMessageConfirmation)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).create().show();
    }

    // Temps elegit pel usuari
    public long timechoice(String received) {
        long result = 0;
        switch (received) {
            case "Fácil - 150s":
                result = 150000;
                break;
            case "Medio - 80s":
                result = 80000;
                break;
            case "Duro - 60s":
                result = 60000;
                break;
            case "Leyenda - 40s":
                result = 40000;
                break;
        }
        return result;
    }

    // PopUp's que es mostren quan una partida s'acabe per X motius, mirar noms de les funcions per saber a quin pertany
    public void showpopupTimeLoss() {
        PopUpTimeOut popUpTimeOut = new PopUpTimeOut(this, this.getLayoutInflater());
        popUpTimeOut.configurePopUp();
        popUpTimeOut.show();
    }

    public void showpopupWin() {
        PopUpWin popUpWin = new PopUpWin(this, this.getLayoutInflater());
        popUpWin.configurePopUp();
        popUpWin.show();
    }

    public void showpopupBomb() {
        PopUpBomb popUpBomb = new PopUpBomb(this, this.getLayoutInflater());
        popUpBomb.configurePopUp();
        popUpBomb.show();
    }

    // Funcio que realitza una espera abans de mostrar els Popup's i que despres fa la crida
    // Opció 1 per Timeloss, opció 2 per bomba clicada, opció 3 per victory
    public void delayPopups(int milliseconds, int option) {
        final Handler handler = new Handler();
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        switch (option) {
                            case 1:
                                showpopupTimeLoss();
                                break;
                            case 2:
                                showpopupBomb();
                                break;
                            case 3:
                                showpopupWin();
                                break;
                        }
                    }
                });
            }
        }, milliseconds);

    }

    // Guardem les dades a recuperar al OnCreate
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (havetimer) {
            time.cancel();
        }
        outState.putLong(TIEMPO_RESTANTE, tiempo_restante);
        outState.putInt(CASILLAS_RESTANTES, num_cells);
        outState.putIntArray(ARRAY_ORIENTATION, list_orientation);
        outState.putParcelable("matriu",matriu);
        //outState.putIntegerArrayList(LIST_OF_BOMBS, listOfBombsIndexes);
        outState.putIntArray(FLAGS_POSADES, list_of_flags);
        outState.putBoolean(IS_FINISHED, game_finished);
    }

    /* Metodes per parar musica i per continuar el timer, falte fixejar el tema de la musica ja que torna a comensar quan pausem i cridem al onRestart(),
    hem pensat utilitzar un broadcast Receiver que ens permetra pausar i reanudar falte implementar per la seguent entrega */

    @Override
    protected void onPause() {
        super.onPause();
        if (have_music) {
            stopService(toStopService);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Comprovar si sonido haurie de estar activat si ho esta engeggarlo
        if (have_music) {
            toStopService.putExtra(START, START);
            startService(toStopService);
        }
        if (havetimer && game_finished == false) {
            time = new CountDownTimer(tiempo_restante, 1000) {
                @Override
                public void onTick(long l) {
                    tiempo_restante = l;
                    if (havetimer) {
                        timer.setText("Segundos restantes: " + l / 1000);
                        timer.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                    } else {
                        timer.setText("Segundos restantes: " + l / 1000);
                        timer.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
                    }
                }

                @Override
                public void onFinish() {
                    // PARTIDA PERDUDA PER TEMPS
                    int status_partida = 1;
                    changeActivityToFinal(status_partida, 0);
                }
            }.start();
        } else {
            timer.setText("No hay tiempo!");
            timer.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
        }
    }
}
