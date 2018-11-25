package com.mitraillet.sym_labo2_data;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.RealResponseBody;
import okio.Okio;

public  class compress extends AppCompatActivity {

    private final int PHONE_NUMBER_SIZE = 10;
    private final String URL_SERVER_XML = "http://sym.iict.ch/rest/xml";
    private final String URL_SERVER_JSON = "http://sym.iict.ch/rest/json";
    private static final int COMMAND_DISPLAY_SERVER_RESPONSE = 1;
    private static final String KEY_SERVER_RESPONSE_OBJECT = "KEY_SERVER_RESPONSE_OBJECT";
    private static final String TAG_OK_HTTP_ACTIVITY = "OK_HTTP_ACTIVITY";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType XML = MediaType.parse("application/xml; charset=utf-8");

    private Spinner choice;
    private Spinner genderChoice;
    private EditText firstName;
    private EditText lastName;
    private EditText phoneNumber;
    private TextView response;
    private Button sendButton;
    private Handler displayRespTextHandler;

    private OkHttpClient okHttpClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datasender);

        choice = findViewById(R.id.formatSpinner);
        genderChoice = findViewById(R.id.genderSpinner);
        firstName = findViewById(R.id.firstName);
        lastName  = findViewById(R.id.lastName);
        phoneNumber = findViewById(R.id.phoneNumber);
        sendButton  = findViewById(R.id.sendButton);
        response = findViewById(R.id.response);

        displayRespTextHandler = new Handler()
        {
            // When this handler receive message from child thread.
            @Override
            public void handleMessage(Message msg) {

                // Check what this message want to do.
                if(msg.what == COMMAND_DISPLAY_SERVER_RESPONSE)
                {
                    // Get server response text.
                    Bundle bundle = msg.getData();
                    String respText = bundle.getString(KEY_SERVER_RESPONSE_OBJECT);

                    // Display server response text in text view.
                    response.setText(respText);
                }
            }
        };
        okHttpClient = new OkHttpClient();
        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String url;
                String data;
                MediaType type;
                // Make sure every field is complete, otherwise we complete the lastname field
                if (firstName == null && lastName == null ) {
                    firstName.setText("");
                    lastName.setText("Unnamed");
                }

                if (!isValidNumber(phoneNumber.getText().toString())) {
                    phoneNumber.setText("+0000000000");
                }

                // Prepare the Data form to send to the server. Up to you to choose the format
                try
                {
                    String fNameString = firstName.getText().toString();
                    String lNameString = lastName.getText().toString();
                    String phoneString = phoneNumber.getText().toString();
                    String gender      = genderChoice.getSelectedItem().toString();

                    if (choice.getSelectedItem().toString().equals("XML")) {
                        // Set format
                        data = setXML(fNameString, lNameString, gender, phoneString);
                        url = URL_SERVER_XML;
                        type = XML;

                    } else {
                        data = setJSON(fNameString, lNameString, gender, phoneString);
                        url = URL_SERVER_JSON;
                        type = JSON;
                    }
                    // Create okhttp3.Call object with post http request method.
                    Call call = createHttpPostMethodCall(url, data, type);

                    // Execute the request and get the response asynchronously.
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            sendChildThreadMessageToMainThread("Asynchronous http post request failed.");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if(response.isSuccessful())
                            {
                                String respData;
                                String contentEncoding = response.headers().get("Content-Encoding");
                                // Parse and get server response text data.
                                if (contentEncoding != null && contentEncoding.equals("Deflate"))
                                {
                                    InflaterInputStream responseBody = new InflaterInputStream(response.body().byteStream());
                                    respData = responseBody.toString();
                                }
                                else {
                                    respData = parseResponseText(response);
                                }

                                // Notify activity main thread to update UI display text with Handler.
                                sendChildThreadMessageToMainThread(respData);
                            }
                            else {
                                // error case
                                switch (response.code()) {
                                    case 404:
                                        sendChildThreadMessageToMainThread("not found");
                                        break;
                                    case 500:
                                        sendChildThreadMessageToMainThread("server problem");
                                        break;
                                    default:
                                        sendChildThreadMessageToMainThread("unknown error");
                                        break;
                                }
                            }
                        }
                    });
                }catch(Exception ex)
                {
                    Log.e(TAG_OK_HTTP_ACTIVITY, ex.getMessage(), ex);
                    sendChildThreadMessageToMainThread(ex.getMessage());
                }
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    /* Create OkHttp3 Call object use post method with url. */
    private Call createHttpPostMethodCall(String url, String param, MediaType type)
    {
        try {
        // Encode a String into bytes
        byte[] data = compressDeflate(param.getBytes("UTF-8"));

        RequestBody body = RequestBody.create(type,  data);

            // Create a http request object.
        Request.Builder builder = new Request.Builder();
        builder = builder.addHeader("X-Network", "CSD");
        /* L'entête ne fonctionne pas/n'est pas reconnue car sans le serveur reçoit un JSON invalid
        mais avec rien du tout ou alors la compression n'est pas la bonne */
        //builder = builder.addHeader("X-Content-Encoding", "deflate");
        builder = builder.url(url);
        builder = builder.post(body);
        Request request = builder.build();

        // Create a new Call object with post method.
        return okHttpClient.newCall(request);

        }
        catch (java.io.UnsupportedEncodingException ex) {
        // handle
            ex.printStackTrace();
    } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static byte[] compressDeflate(byte[] data) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(1000);
            DeflaterOutputStream compresser = new DeflaterOutputStream(bout);
            compresser.write(data, 0, data.length);
            compresser.finish();
            compresser.flush();
            return bout.toByteArray();
        } catch (IOException ex) {
            AssertionError ae = new AssertionError("IOException while writing to ByteArrayOutputStream!");
            ae.initCause(ex);
            throw ae;
        }
    }

    /* Parse response code, message, headers and body string from server response object. */
    private String parseResponseText(Response response)
    {
        // Get body text.
        String respBody = "";
        try {
            respBody = response.body().string();
        }catch(IOException ex)
        {
            Log.e(TAG_OK_HTTP_ACTIVITY, ex.getMessage(), ex);
        }

        return respBody;
    }

    // Send message from child thread to activity main thread.
    // Because can not modify UI controls in child thread directly.
    private void sendChildThreadMessageToMainThread(String respData)
    {
        // Create a Message object.
        Message message = new Message();

        // Set message type.
        message.what = COMMAND_DISPLAY_SERVER_RESPONSE;

        // Set server response text data.
        Bundle bundle = new Bundle();
        bundle.putString(KEY_SERVER_RESPONSE_OBJECT, respData);
        message.setData(bundle);

        // Send message to activity Handler.
        displayRespTextHandler.sendMessage(message);
    }

    private boolean isValidNumber(String phone) {
        // Or find a super extra regex to match phone number validation
        return phone.length() >= PHONE_NUMBER_SIZE && !phone.matches("A-Za-z");
    }

    private String setJSON(String fName, String lName, String gender,String num) {

        JSONObject contact = new JSONObject();

        try {

            contact.put("First Name", fName);
            contact.put("Last Name", lName);
            contact.put("Gender", gender);
            contact.put("Number", num);

            return contact.toString(3);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // reaching this point shouldn't arrive
        return null;
    }

    private String setXML(String firstName, String lastName, String gender, String number) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        Document doc;
        DOMSource source;
        String xmlString = "";

        try {

            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // Create a document to easily add xml nodes.
            doc = docBuilder.newDocument();
            source = new DOMSource(doc);

            // Setting properties
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.VERSION,"1.0");
            transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"http://sym.iict.ch/directory.dtd");

            // Create Elements
            Element directory = doc.createElement("directory");

            Element person = doc.createElement("person");
            Element fName  = doc.createElement("firstname");
            Element lName  = doc.createElement("name");
            Element mName  = doc.createElement("middlename");
            Element nGender = doc.createElement("gender");
            Element phone  = doc.createElement("phone");

            fName.setTextContent(firstName);
            lName.setTextContent(lastName);
            nGender.setTextContent(gender);
            phone.setTextContent(number);
            phone.setAttribute("type","mobile");

            // Transform it to XML
            doc.appendChild(directory);

            directory.appendChild(person);
            person.appendChild(lName);
            person.appendChild(fName);
            person.appendChild(mName);
            person.appendChild(nGender);
            person.appendChild(phone);

            // transform document to String
            StringWriter writer = new StringWriter();

            transformer.transform(source, new StreamResult(writer));
            xmlString = writer.getBuffer().toString();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return xmlString;
    }
}
