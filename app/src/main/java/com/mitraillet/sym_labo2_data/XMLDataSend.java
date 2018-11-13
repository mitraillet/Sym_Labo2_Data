package com.mitraillet.sym_labo2_data;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.xmlpull.v1.XmlSerializer;

import javax.xml.parsers.DocumentBuilder;

public class XMLDataSend extends AppCompatActivity  {

    private final int PHONE_NUMBER_SIZE = 10;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {

        super.onCreate(savedInstanceState, persistentState);

        // Get every listeners from the layout

        setContentView(R.layout.xmldatasender);

        EditText firstName = findViewById(R.id.firstName);
        EditText lastName  = findViewById(R.id.lastName);
        final EditText phoneNumber = findViewById(R.id.phoneNumber);
        Button   sendButton  = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Make sure every field is complete, otherwise we complete the lastname field
                if (firstName == null && lastName == null ) {
                    firstName.setText("");
                    lastName.setText("Unnamed");
                }

                if (isValidNumber(phoneNumber.getText().toString())) {
                    // Throw an error or write something in red...
                }
                // Prepare the XML form to send to the server

                // Send it !
            }
        });
    }

    public boolean isValidNumber(String phone) {
        // Or find a super extra regex to match phone number validation
        return phone.length() >= PHONE_NUMBER_SIZE && !phone.matches("A-Za-z");
    }

    @Override
    public void onResume() {

        super.onResume();

        // data serialization and data sending

    }

    private void setXMLForm() {

        // Get data from the fields
        DocumentBuilder builder;
        // Transform it to XML
    }





}
