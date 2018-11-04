package com.mitraillet.sym_labo2_data;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class XMLDataSend extends AppCompatActivity  {

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {

        super.onCreate(savedInstanceState, persistentState);

        // Get every listeners from the layout

        setContentView(R.layout.xmldatasender);

        EditText firstName = findViewById(R.id.firstName);
        EditText lastName  = findViewById(R.id.lastName);
        EditText phoneNumber = findViewById(R.id.phoneNumber);
        Button   sendButton  = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Make sure every field is complete

                // Prepare the XML form to send to the server

                // Send it !
            }
        });
    }

    @Override
    public void onResume() {

        super.onResume();

        // data serialization and data sending

    }

    private void setXMLForm(){

        // Get data from the fields

        // Transform it to XML
    }





}
