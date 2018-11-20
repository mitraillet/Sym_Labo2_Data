package com.mitraillet.sym_labo2_data;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public abstract class DataSender extends AppCompatActivity {

    private final int PHONE_NUMBER_SIZE = 10;
    private final String URL_SERVER_XML = "http://sym.iict.ch/rest/xml";
    private final String URL_SERVER_JSON = "http://sym.iict.ch/rest/json";

    private Spinner choice = findViewById(R.id.spinner);
    private EditText firstName = findViewById(R.id.firstName);
    private EditText lastName  = findViewById(R.id.lastName);
    private EditText phoneNumber = findViewById(R.id.phoneNumber);
    private Button sendButton  = findViewById(R.id.sendButton);

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {

        super.onCreate(savedInstanceState, persistentState);

        // Get every listeners from the layout
        setContentView(R.layout.datasender);

        choice = findViewById(R.id.spinner);

        List<String> list = new ArrayList<String>();
        list.add("Json");
        list.add("XML");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        choice.setAdapter(dataAdapter);

        firstName = findViewById(R.id.firstName);
        lastName  = findViewById(R.id.lastName);
        phoneNumber = findViewById(R.id.phoneNumber);
        sendButton  = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Make sure every field is complete, otherwise we complete the lastname field
                if (firstName == null && lastName == null ) {
                    firstName.setText("");
                    lastName.setText("Unnamed");
                }

                if (!isValidNumber(phoneNumber.getText().toString())) {
                    phoneNumber.setText("+0000000000");
                }

                // Prepare the Data form to send to the server. Up to you to choose the format
                if (choice.getSelectedItem().toString().equals("XML")) {

                    // Set format
                    String data = setXML(firstName.toString(), lastName.toString(), phoneNumber.getText().toString());
                } else {

                    String data = setJSON(firstName.toString(), lastName.toString(), phoneNumber.getText().toString());
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private boolean isValidNumber(String phone) {
        // Or find a super extra regex to match phone number validation
        return phone.length() >= PHONE_NUMBER_SIZE && !phone.matches("A-Za-z");
    }

    private String setJSON(String fName, String lName, String num) {

        JSONObject contact = new JSONObject();

        try {
            contact.put("First Name", fName);
            contact.put("Last Name", lName);
            contact.put("Number", num);

            return contact.toString(3);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // reaching this point shouldn't arrive
        return null;
    }

    private String setXML(String firstName, String lastName, String number) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;

        try {
            docBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        Document doc = docBuilder.newDocument();

        Element directory = doc.createElement("Directory");
        Element contact = doc.createElement("Contact");
        Element fName  = doc.createElement("FirstName");
        Element lName  = doc.createElement("LastName");
        Element num  = doc.createElement("Number");

        fName.setTextContent(firstName);
        lName.setTextContent(lastName);
        num.setTextContent(number);

        // Transform it to XML

        doc.setXmlVersion("1.0");
        doc.appendChild(directory);
        directory.appendChild(contact);
        contact.appendChild(fName);
        contact.appendChild(lName);
        contact.appendChild(num);

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult();

        try {

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.VERSION,"1.0");
            transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"http://sym.iict.ch/directory.dtd");
            transformer.transform(source,result);

        } catch (TransformerException e) {
            e.printStackTrace();
            return "";
        }

        // And transform it to a String
        return result.toString();
    }
}