package com.itielmaimon.minesweeper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    SharedPreferences preferences;

    int rows;
    int columns;
    int mines;

    boolean isGameStarted = false;

    private Block[][] blocks;

    private int difficultyPref;
    private int themePref;
    private int themeTextColor;

    private int coveredBlock;
    private int openedBlock;
    private int minedBlock;
    private int redMinedBlock;
    private int flaggedBlock;
    private int wrongFlaggedBlock;

    private Boolean isVibrationEnabled;
    private Boolean flagState;

    private Handler mHandler = new Handler();
    int secondsPassed = 0;

    private boolean isGameOver; // check if game is over
    private int minesToFind; // number of mines yet to be discovered
    int openedBlocks = 0;

    private boolean isPlayBoardVisible;

    RelativeLayout mainView;
    Button playButton;
    ImageButton settingsButton;
    ImageButton removeAdsButton;
    TextView appTitle;
    TextView themeTextView;
    TextView difficultyTextView;
    TextView txtMineCount;
    TextView txtTimer;
    RelativeLayout playBoard;
    TableLayout mineField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("night_mode_pref", false))
            setTheme(R.style.AppThemeDark);
        else
            setTheme(R.style.AppTheme);
        String languagePrefKey = getString(R.string.language_pref_key);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = preferences.getString(languagePrefKey, getString(R.string.language_code));
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        setContentView(R.layout.activity_main);

        difficultyPref = preferences.getInt(getString(R.string.difficulty_pref_key), 0);
        themePref = preferences.getInt(getString(R.string.theme_pref_key), 0);
        isVibrationEnabled = preferences.getBoolean(getString(R.string.vibration_pref_key), true);

        mainView = findViewById(R.id.main_view);
        settingsButton = findViewById(R.id.settings_button);
        removeAdsButton = findViewById(R.id.remove_ads_button);
        playButton = findViewById(R.id.play_button);
        appTitle = findViewById(R.id.app_title);
        difficultyTextView = findViewById(R.id.difficulty_textView);
        themeTextView = findViewById(R.id.theme_textView);

        txtMineCount = findViewById(R.id.minesCount);
        txtTimer = findViewById(R.id.timer);
        playBoard = findViewById(R.id.play_board);
        mineField = findViewById(R.id.mineField);

        setGameDifficulty(difficultyPref);
        setGameTheme(themePref);

        findViewById(R.id.play_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainView.setVisibility(View.GONE);
                playBoard.setVisibility(View.VISIBLE);
                isPlayBoardVisible = true;
                startGame();
            }
        });

        findViewById(R.id.flag_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isGameOver) {
                    endExistingGame();
                    startGame();
                }
                else {
                    flagState = !flagState;
                    if (flagState)
                        if (themePref == 2)
                            findViewById(R.id.flag_button).setBackgroundResource(R.drawable.ic_white_flag);
                        else
                            findViewById(R.id.flag_button).setBackgroundResource(R.drawable.ic_flag);
                    else
                        if (themePref == 2)
                            findViewById(R.id.flag_button).setBackgroundResource(R.drawable.ic_white_mine);
                        else
                            findViewById(R.id.flag_button).setBackgroundResource(R.drawable.ic_mine);
                }
            }
        });

        findViewById(R.id.settings_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.difficulty_next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (difficultyPref) {
                    case 0:
                        setGameDifficulty(1);
                        break;
                    case 1:
                        setGameDifficulty(2);
                        break;
                    case 2:
                        setGameDifficulty(0);
                        break;
                }
            }
        });

        findViewById(R.id.difficulty_prev_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (difficultyPref) {
                    case 0:
                        setGameDifficulty(2);
                        break;
                    case 1:
                        setGameDifficulty(0);
                        break;
                    case 2:
                        setGameDifficulty(1);
                        break;
                }
            }
        });

        findViewById(R.id.theme_next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (themePref) {
                    case 0:
                        setGameTheme(1);
                        break;
                    case 1:
                        setGameTheme(2);
                        break;
                    case 2:
                        setGameTheme(0);
                        break;
                }
            }
        });

        findViewById(R.id.theme_prev_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (themePref) {
                    case 0:
                        setGameTheme(2);
                        break;
                    case 1:
                        setGameTheme(0);
                        break;
                    case 2:
                        setGameTheme(1);
                        break;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(isPlayBoardVisible && isGameStarted && !isGameOver) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.end_game_warning))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            playBoard.setVisibility(View.GONE);
                            mainView.setVisibility(View.VISIBLE);
                            isPlayBoardVisible = false;
                            endExistingGame();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .show();
        }
        else if (isPlayBoardVisible) {
            playBoard.setVisibility(View.GONE);
            mainView.setVisibility(View.VISIBLE);
            isPlayBoardVisible = false;
            endExistingGame();
        }
        else
            super.onBackPressed();
    }

    private void setGameDifficulty(int i) {
        switch (i) {
            case 0:
                difficultyTextView.setText(getString(R.string.easy));
                rows = 9;
                columns = 9;
                mines = 10;
                break;
            case 1:
                difficultyTextView.setText(getString(R.string.medium));
                rows = 16;
                columns = 16;
                mines = 40;
                break;
            case 2:
                difficultyTextView.setText(getString(R.string.hard));
                rows = 30;
                columns = 16;
                mines = 99;
                break;
        }
        difficultyPref = i;
        preferences.edit().putInt(getString(R.string.difficulty_pref_key), i).apply();
    }

    private void setGameTheme(int i) {
        switch (i) {
            case 0:
                settingsButton.setImageResource(R.drawable.ic_settings);
                removeAdsButton.setImageResource(R.drawable.ic_remove_ads);
                appTitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/VT323-Regular.ttf"));
                themeTextView.setText(getString(R.string.classic));
                findViewById(R.id.activity_main).setBackgroundColor(ContextCompat.getColor(this, R.color.classicBackgroundColor));
                themeTextColor = ContextCompat.getColor(this, R.color.black);
                coveredBlock = R.drawable.classic_covered;
                openedBlock = R.drawable.classic_opened;
                minedBlock = R.drawable.classic_mine;
                redMinedBlock = R.drawable.classic_redmine;
                flaggedBlock = R.drawable.classic_flag;
                wrongFlaggedBlock = R.drawable.classic_wrong_flag;
                break;
            case 1:
                settingsButton.setImageResource(R.drawable.ic_settings);
                removeAdsButton.setImageResource(R.drawable.ic_remove_ads);
                appTitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/MontserratAlternates-Medium.ttf"));
                themeTextView.setText(getString(R.string.modern));
                findViewById(R.id.activity_main).setBackgroundColor(ContextCompat.getColor(this, R.color.modernBackgroundColor));
                themeTextColor = ContextCompat.getColor(this, R.color.black);
                coveredBlock = R.drawable.modern_covered;
                openedBlock = R.drawable.modern_opened;
                minedBlock = R.drawable.ic_mine;
                redMinedBlock = R.drawable.ic_red_mine;
                flaggedBlock = R.drawable.ic_flag;
                wrongFlaggedBlock = R.drawable.ic_red_flag;
                break;
            case 2:
                settingsButton.setImageResource(R.drawable.ic_settings_spooky);
                removeAdsButton.setImageResource(R.drawable.ic_remove_ads_spooky);
                appTitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/CinzelDecorative-Bold.ttf"));
                themeTextView.setText(getString(R.string.spooky));
                findViewById(R.id.activity_main).setBackgroundColor(ContextCompat.getColor(this, R.color.black));
                themeTextColor = ContextCompat.getColor(this, R.color.white);
                coveredBlock = R.drawable.spooky_covered;
                openedBlock = R.drawable.spooky_opened;
                minedBlock = R.drawable.ic_white_mine;
                redMinedBlock = R.drawable.ic_red_mine;
                flaggedBlock = R.drawable.ic_white_flag;
                wrongFlaggedBlock = R.drawable.ic_red_flag;
                break;
        }
        appTitle.setTextColor(themeTextColor);
        difficultyTextView.setTextColor(themeTextColor);
        themeTextView.setTextColor(themeTextColor);
        txtMineCount.setTextColor(themeTextColor);
        txtTimer.setTextColor(themeTextColor);
        ((TextView) findViewById(R.id.timer_textView)).setTextColor(themeTextColor);
        ((TextView) findViewById(R.id.mines_textView)).setTextColor(themeTextColor);
        themePref = i;
        preferences.edit().putInt(getString(R.string.theme_pref_key), i).apply();
    }

    private void startGame() {
        isGameStarted = false;
        flagState = false;
        secondsPassed = 0;
        minesToFind = mines;
        openedBlocks = 0;
        if (themePref == 2)
            findViewById(R.id.flag_button).setBackgroundResource(R.drawable.ic_white_mine);
        else
            findViewById(R.id.flag_button).setBackgroundResource(R.drawable.ic_mine);
        updateMineCountDisplay();
        blocks = new Block[rows + 2][columns + 2];
        for (int i = 0; i < blocks.length; i++) {
            for (int j = 0; j < blocks[i].length; j++) {
                final int row = i;
                final int column = j;
                blocks[row][column] = new Block(this);
                blocks[row][column].setDefault();
                blocks[row][column].setBackgroundResource(coveredBlock);
                blocks[row][column].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isGameStarted) {
                            startTimer();
                            setMines(row, column);
                            isGameStarted = true;
                        }
                        if (flagState) {
                            if (!blocks[row][column].isFlagged()) {
                                blocks[row][column].setBackgroundResource(flaggedBlock);
                                blocks[row][column].setFlagged(true);
                                minesToFind--;
                                updateMineCountDisplay();
                            } else {
                                blocks[row][column].setBackgroundResource(coveredBlock);
                                blocks[row][column].setFlagged(false);
                                minesToFind++;
                                updateMineCountDisplay();
                            }
                        } else if (!blocks[row][column].isFlagged()) {
                            if (blocks[row][column].isMined())
                                loseGame(row, column);
                            else
                                rippleUncover(row, column);
                            if ((rows * columns) - openedBlocks == mines)
                                winGame();
                        }
                    }
                });
                blocks[row][column].setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (!blocks[row][column].isFlagged()) {
                            blocks[row][column].setBackgroundResource(flaggedBlock);
                            blocks[row][column].setFlagged(true);
                            minesToFind--;
                            updateMineCountDisplay();
                        } else {
                            blocks[row][column].setBackgroundResource(coveredBlock);
                            blocks[row][column].setFlagged(false);
                            minesToFind++;
                            updateMineCountDisplay();
                        }
                        if (isVibrationEnabled) {
                            Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            if (vibrator != null) {
                                vibrator.vibrate(50);
                            }
                        }
                        return true;
                    }
                });
            }
        }
        int screenWidth = MainActivity.this.getResources().getDisplayMetrics().widthPixels - 100;
        int blockDimension = screenWidth / columns;
        for (int i = 1; i < rows + 1; i++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableRow.LayoutParams(blockDimension * columns, blockDimension));
            for (int j = 1; j < columns + 1; j++) {
                blocks[i][j].setLayoutParams(new TableRow.LayoutParams(blockDimension, blockDimension));
                blocks[i][j].setPadding(2, 2, 2, 2);
                tableRow.addView(blocks[i][j]);
            }
            mineField.addView(tableRow);
        }
    }

    private void setMines(int row, int column) {
        Random random = new Random();
        for (int m = 0; m < mines; m++){
            int randRow = random.nextInt(rows) + 1;
            int randColumn = random.nextInt(columns) + 1;
            if ((randRow == row && randColumn == column) || blocks[randRow][randColumn].isMined())
                m--;
            else
                blocks[randRow][randColumn].setMined(true);
        }
        for (int i = 0; i < rows + 2; i++) {
            for (int j = 0; j < columns + 2; j++) {
                int minesAroundCount = 0;
                if (i != 0 && i != rows + 1 && j != 0 && j != columns + 1) {
                    for (int prevRow = -1; prevRow < 2; prevRow++)
                        for (int prevColumn = -1; prevColumn < 2; prevColumn++)
                            if (blocks[i + prevRow][j + prevColumn].isMined())
                                minesAroundCount++;
                    blocks[i][j].setNumberOfMinesAround(minesAroundCount);
                }
                else {
                    blocks[i][j].setNumberOfMinesAround(9);
                    blocks[i][j].setOpened(true);
                    blocks[i][j].setBackgroundResource(openedBlock);
                    blocks[i][j].setEnabled(false);
                    updateNumber(9, i, j);
                }
            }
        }
    }

    private void endExistingGame() {
        stopTimer();
        txtTimer.setText(R.string.zero);
        txtMineCount.setText(R.string.zero);

        // remove all rows from mineField TableLayout
        mineField.removeAllViews();

        // set all variables to support end of game
        isGameStarted = false;
        isGameOver = false;
        flagState = false;
        secondsPassed = 0;
        minesToFind = 0;
    }

    private void updateMineCountDisplay() {
        if (minesToFind < 0)
            txtMineCount.setText(String.valueOf(minesToFind));
        else if (minesToFind < 10)
            txtMineCount.setText(String.format(getString(R.string.mines_count), minesToFind));
        else if (minesToFind < 100)
            txtMineCount.setText(String.format(getString(R.string.larger_mines_count), minesToFind));
        else
            txtMineCount.setText(String.valueOf(minesToFind));
    }

    private void winGame() {
        stopTimer();
        isGameStarted = false;
        isGameOver = true;
        flagState = false;
        minesToFind = 0;

        updateMineCountDisplay();

        for (int i = 0; i < rows + 2; i++) {
            for (int j = 0; j < columns + 2; j++) {
                blocks[i][j].setEnabled(false);
                if (blocks[i][j].isMined())
                    blocks[i][j].setBackgroundResource(flaggedBlock);
            }
        }

        showGameOverDialog(getString(R.string.you_won), getString(R.string.you_won_in) + " " + secondsPassed + " " + getString(R.string.seconds));
        secondsPassed = 0;
    }

    private void loseGame(int row, int column) {
        stopTimer();
        isGameStarted = false;
        isGameOver = true;
        flagState = false;
        minesToFind = 0;

        if (themePref == 2)
            findViewById(R.id.flag_button).setBackgroundResource(R.drawable.ic_white_replay);
        else
            findViewById(R.id.flag_button).setBackgroundResource(R.drawable.ic_replay);

        for (int i = 0; i < rows + 2; i++) {
            for (int j = 0; j < columns + 2; j++) {
                blocks[i][j].setEnabled(false);
                if (blocks[i][j].isMined() && !blocks[i][j].isFlagged())
                    blocks[i][j].setBackgroundResource(minedBlock);
                if (!blocks[i][j].isMined() && blocks[i][j].isFlagged())
                    blocks[i][j].setBackgroundResource(wrongFlaggedBlock);
            }
        }

        blocks[row][column].setBackgroundResource(redMinedBlock);

        if(isVibrationEnabled) {
            Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(100);
            }
        }

        showGameOverDialog(getString(R.string.you_lost), getString(R.string.you_tried_for) + " " + secondsPassed + " " + getString(R.string.seconds));
    }

    private void rippleUncover(int rowClicked, int columnClicked) {
        // don't open opened, flagged, QuestionMarked or mined blocks
        if (blocks[rowClicked][columnClicked].isOpened() || blocks[rowClicked][columnClicked].isMined() || blocks[rowClicked][columnClicked].isFlagged())
            return;

        // open clicked block
        openedBlocks++;
        blocks[rowClicked][columnClicked].setOpened(true);
        blocks[rowClicked][columnClicked].setBackgroundResource(openedBlock);
        blocks[rowClicked][columnClicked].setEnabled(false);
        updateNumber(blocks[rowClicked][columnClicked].getNumberOfMinesAround(), rowClicked, columnClicked);

        // if clicked block have nearby mines then don't open further
        if (blocks[rowClicked][columnClicked].getNumberOfMinesAround() != 0 )
            return;

        // open next 3 rows and 3 columns recursively
        for (int row = -1; row < 2; row++)
            for (int column = -1; column < 2; column++)
                if (!blocks[rowClicked + row][columnClicked + column].isOpened() && rowClicked + row > 0 && columnClicked + column > 0 && rowClicked + row < rows + 1 && columnClicked + column < columns + 1)
                    rippleUncover(rowClicked + row, columnClicked + column);
    }

    private void updateNumber(int number, int row, int column) {
        if (number != 0) {
            switch (number) {
                case 1:
                    blocks[row][column].setTextColor(ContextCompat.getColor(this, R.color.numberColor1));
                    break;
                case 2:
                    blocks[row][column].setTextColor(ContextCompat.getColor(this, R.color.numberColor2));
                    break;
                case 3:
                    blocks[row][column].setTextColor(ContextCompat.getColor(this, R.color.numberColor3));
                    break;
                case 4:
                    blocks[row][column].setTextColor(ContextCompat.getColor(this, R.color.numberColor4));
                    break;
                case 5:
                    blocks[row][column].setTextColor(ContextCompat.getColor(this, R.color.numberColor5));
                    break;
                case 6:
                    blocks[row][column].setTextColor(ContextCompat.getColor(this, R.color.numberColor6));
                    break;
                case 7:
                    blocks[row][column].setTextColor(ContextCompat.getColor(this, R.color.numberColor7));
                    break;
                case 8:
                    blocks[row][column].setTextColor(ContextCompat.getColor(this, R.color.numberColor8));
                    break;
                case 9:
                    blocks[row][column].setTextColor(ContextCompat.getColor(this, R.color.numberColor9));
                    break;
            }
            blocks[row][column].setText(String.valueOf(number));
        }
    }

    public void startTimer() {
        if (secondsPassed == 0) {
            mHandler.removeCallbacks(mUpdateTimeTask);
            mHandler.postDelayed(mUpdateTimeTask, 1000);
        }
    }

    public void stopTimer() {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            secondsPassed++;
            txtTimer.setText(String.valueOf(secondsPassed));
            mHandler.postDelayed(mUpdateTimeTask, 1000);
        }
    };

    private void showGameOverDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(message).show();
    }
}