package com.mitraillet.sym_labo2_data;

/*
 * Author : Steve Henriquet
 */
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class differee extends AppCompatActivity {

    private final String URL_SERVER = "http://sym.iict.ch/rest/json";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG_OK_HTTP_ACTIVITY = "OK_HTTP_ACTIVITY";
    private static final int COMMAND_DISPLAY_SERVER_RESPONSE = 1;
    private static final String KEY_SERVER_RESPONSE_OBJECT = "KEY_SERVER_RESPONSE_OBJECT";

    //Field on the view
    private Button viewButton;
    private TextView email;
    private TextView password;
    private TextView retourServeur;

    private Runnable _run;
    private OkHttpClient okHttpClient;
    private LinkedList<Tuple> data;

    private Handler displayRespTextHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.differee);

        viewButton = findViewById(R.id.button);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        retourServeur = findViewById(R.id.retourServeur);

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
                    retourServeur.setText(respText);
                }
            }
        };


        data = new LinkedList<Tuple>();
        final Handler handler = new Handler();
        // Create a runnable to wait the server connection
        _run = new Runnable() {
            @Override
            public void run() {
                //Each 30 seconds check if the server si connected

                if(data.size() > 0){

                    if(isNetworkAvailable()){
                        okHttpClient = new OkHttpClient();
                        try
                        {
                            Tuple donnee = data.get(0);
                            data.remove(0);
                            String _data = setJSON(donnee.x.toString(), donnee.y.toString());

                            // Create okhttp3.Call object with post http request method.
                            Call call = createHttpPostMethodCall(URL_SERVER, _data, JSON);

                            // Execute the request and get the response asynchronously.
                            call.enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    sendChildThreadMessageToMainThread("Server request sending failed.");
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    if(response.isSuccessful())
                                    {
                                        // Parse and get server response text data.
                                        String respData = parseResponseText(response);

                                        // Notify activity main thread to update UI display text with Handler.
                                        sendChildThreadMessageToMainThread(respData);
                                    }
                                }
                            });
                        }catch(Exception ex)
                        {
                            Log.e(TAG_OK_HTTP_ACTIVITY, ex.getMessage(), ex);
                            sendChildThreadMessageToMainThread(ex.getMessage());
                        }
                    }else{
                        handler.postDelayed(_run, 30000);
                    }
                }
            }
        };


        handler.postDelayed(_run, 30000);

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Tuple _data = new Tuple(email.getText(), password.getText());
                data.add(_data);

                handler.postDelayed(_run, 30000);
            }
        });
    }

    //Internal class to get the data
    private class Tuple<X, Y> {
        public final X x;
        public final Y y;
        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }

    private String setJSON(String Email, String Password) {

        JSONObject contact = new JSONObject();

        try {
            contact.put("Email", Email);
            contact.put("Password", Password);

            return contact.toString(3);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // reaching this point shouldn't arrive
        return null;
    }
    /* Create OkHttp3 Call object use post method with url. */
    private Call createHttpPostMethodCall(String url, String param, MediaType type)
    {
        // Create okhttp3 form body builder.
        RequestBody reqBody = RequestBody.create(type, param);

        // Create a http request object.
        Request.Builder builder = new Request.Builder();
        builder = builder.url(url);
        builder = builder.post(reqBody);
        Request request = builder.build();

        // Create a new Call object with post method.
        Call call = okHttpClient.newCall(request);

        return call;
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
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

// test for connection
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }
}