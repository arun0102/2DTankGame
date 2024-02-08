package arun.pkg.tankgame;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


import arun.pkg.tankgame.constants.Constants;

public class HomePageActivity extends Activity implements OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.home_screen);

        Button newGamebtn = (Button) findViewById(R.id.new_game_btn);
        newGamebtn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES_FILE_NAME_SCORES, MODE_PRIVATE);
        int highScore = prefs.getInt(Constants.PREFERENCES_KEY_HIGH_SCORE, 0);
        TextView highScoresTxt = (TextView) findViewById(R.id.high_scores_btn);
        highScoresTxt.setText("Highest Score : " + highScore);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.new_game_btn:
                startActivity(new Intent(HomePageActivity.this, GameScreenActivity.class));
                break;

            default:
                break;
        }
    }

}
