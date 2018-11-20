package com.mitraillet.sym_labo2_data;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mClickButton1 = findViewById(R.id.async);
        Button mClickButton2 = findViewById(R.id.diff);
        Button mClickButton3 = findViewById(R.id.object);
        Button mClickButton4 = findViewById(R.id.JSONXML);

        mClickButton1.setOnClickListener(onClickListener);
        mClickButton2.setOnClickListener(onClickListener);
        mClickButton3.setOnClickListener(onClickListener);
        mClickButton4.setOnClickListener(onClickListener);
    }

    // somewhere else in your code
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.async: {
                    // do something for button 1 click
                    Intent intent = new Intent(MainActivity.this, com.mitraillet.sym_labo2_data.async.class);
                    startActivity(intent);
                    break;
                }

                case R.id.diff: {
                    // do something for button 2 click
                    break;
                }

                case R.id.object: {
                    // do something for button 1 click
                    break;
                }

                case R.id.JSONXML: {
                    // do something for button 2 click
                    Intent intent = new Intent(MainActivity.this, com.mitraillet.sym_labo2_data.DataSender.class);
                    startActivity(intent);
                    break;
                }
            }
        }
    };
}
