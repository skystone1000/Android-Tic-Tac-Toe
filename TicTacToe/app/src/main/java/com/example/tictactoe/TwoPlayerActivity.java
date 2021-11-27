package com.example.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class TwoPlayerActivity extends AppCompatActivity {

    boolean gameActive = true;

    // Player representation ==>  0 - X , 1 - O
    int activePlayer = 0;

    int[] gameState = {2, 2, 2, 2, 2, 2, 2, 2, 2};
    // State representation ==>  0 - X , 1 - O , 2 - Empty

    int[][] winPositions = {{0,1,2}, {3,4,5}, {6,7,8},
                            {0,3,6}, {1,4,7}, {2,5,8},
                            {0,4,8}, {2,4,6}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_player);

    }

    public void playerTap(View view){
        ImageView img = (ImageView) view;
        int tappedImage = Integer.parseInt(img.getTag().toString());
        if(!gameActive){
            gameReset(view);
        }

        if(gameState[tappedImage] == 2){
            gameState[tappedImage] = activePlayer;
            img.setTranslationY(-1000f);
            if(activePlayer == 0){
                img.setImageResource(R.drawable.x);
                activePlayer = 1;
                TextView status = findViewById(R.id.status);
                status.setText("O's Turn");
            }else{
                img.setImageResource(R.drawable.o);
                activePlayer = 0;
                TextView status = findViewById(R.id.status);
                status.setText("X's Turn");
            }
            img.animate().translationYBy(1000f).setDuration(300);
        }

        // Check if Player has won
        for(int[] winPosition:winPositions){
            if(gameState[winPosition[0]] == gameState[winPosition[1]]
                    && gameState[winPosition[1]] == gameState[winPosition[2]]
                    && gameState[winPosition[0]] != 2){

                // Someone has won
                String winnerStr;
                if(gameState[winPosition[0]] == 0){
                    winnerStr = "Result: X has WON !!";
                } else {
                    winnerStr = "Result: O has WON !!";
                }
                // Update the status bar for winner announcement
                TextView result = findViewById(R.id.result);
                TextView status = findViewById(R.id.status);
                status.setText("Game has Ended");
                result.setText(winnerStr);
                gameActive = false;

            }
        }
    }

    public void gameReset(View view){
        gameActive = true;
        activePlayer = 0;
        TextView result = findViewById(R.id.result);
        TextView status = findViewById(R.id.status);
        status.setText(R.string.startStatus);
        result.setText("");
        for(int i=0;i<gameState.length;i++){
            gameState[i] = 2;
        }
        ((ImageView)findViewById(R.id.imageView1)).setImageResource(R.drawable.empty);
        ((ImageView)findViewById(R.id.imageView2)).setImageResource(R.drawable.empty);
        ((ImageView)findViewById(R.id.imageView3)).setImageResource(R.drawable.empty);
        ((ImageView)findViewById(R.id.imageView4)).setImageResource(R.drawable.empty);
        ((ImageView)findViewById(R.id.imageView5)).setImageResource(R.drawable.empty);
        ((ImageView)findViewById(R.id.imageView6)).setImageResource(R.drawable.empty);
        ((ImageView)findViewById(R.id.imageView7)).setImageResource(R.drawable.empty);
        ((ImageView)findViewById(R.id.imageView8)).setImageResource(R.drawable.empty);
        ((ImageView)findViewById(R.id.imageView9)).setImageResource(R.drawable.empty);
    }
}