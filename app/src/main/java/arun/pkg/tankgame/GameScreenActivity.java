package arun.pkg.tankgame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import arun.pkg.simpletankgame.R;
import arun.pkg.tankgame.constants.Constants;

public class GameScreenActivity extends Activity {

    private static final String TAG = GameScreenActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_screen);
    }

    private void pauseGame() {
        //// write all positions and values to database
    }

    public void incrementPoints(int byValue) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView score = (TextView) findViewById(R.id.kill_count);
                String count = score.getText().toString();
                if (null != count && 0 < count.length()) {
                    int scr = Integer.parseInt(count);
                    score.setText("" + (scr + 1));
                    saveScoreIfHighest();
                }
            }
        });
    }

    public void decreaseHealth(final int percent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.health_bar);
                if (0 < progressBar.getProgress()) {
                    TextView healthTextView = (TextView) findViewById(R.id.health_percent);
                    String currentHealth = healthTextView.getText().toString();
                    if (null != currentHealth && 0 < currentHealth.length()) {
                        String[] per = currentHealth.split("%");
                        int health = Integer.parseInt(per[0]);
                        int newHealth = health - percent;
                        if (0 < newHealth) {
                            healthTextView.setText("" + newHealth + "%");
                            progressBar.setProgress(newHealth);
                        } else {
                            healthTextView.setText("" + 0 + "%");
                            progressBar.setProgress(0);
                            GameScreenView gameScreen = (GameScreenView) findViewById(R.id.gameScreen);
                            gameScreen.setGameOver();
                            showGameOverDialogWithScore();
                        }
                    }
                }
            }
        });
    }

    public void showGameOverDialogWithScore() {
        saveScoreIfHighest();

        TextView score = (TextView) findViewById(R.id.kill_count);
        String count = score.getText().toString();
        new AlertDialog.Builder(this).setTitle(getString(R.string.gameover)).setMessage(getString(R.string.youscore) + count)
                .setNeutralButton(getString(R.string.ok), null).create().show();
    }

    private void saveScoreIfHighest() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES_FILE_NAME_SCORES, MODE_PRIVATE);
        int oldHighScore = prefs.getInt(Constants.PREFERENCES_KEY_HIGH_SCORE, 0);

        TextView scoreText = (TextView) findViewById(R.id.kill_count);

        String count = scoreText.getText().toString();
        if (null != count && 0 < count.length()) {
            int scr = Integer.parseInt(count);
            if (scr > oldHighScore) {
                Editor edit = prefs.edit();
                edit.putInt(Constants.PREFERENCES_KEY_HIGH_SCORE, scr);
                edit.commit();
            }
        }
    }
}
