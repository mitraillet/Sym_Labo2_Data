package com.mitraillet.sym_labo2_data;

/*
 * Author : Steve Henriquet
 */
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class async extends AppCompatActivity {

    private static final String TAG_OK_HTTP_ACTIVITY = "OK_HTTP_ACTIVITY";

    private Button asyncButton;
    // Field containing request to send.
    private TextView resqTextView;
    // Display server response text.
    private TextView respTextView;

    private OkHttpClient okHttpClient;

    // Process child thread sent command to show server response text in activity main thread.
    private Handler displayRespTextHandler;

    private static final int COMMAND_DISPLAY_SERVER_RESPONSE = 1;

    private static final String KEY_SERVER_RESPONSE_OBJECT = "KEY_SERVER_RESPONSE_OBJECT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.async);

        setTitle("Async text post");

        // Init okhttp3 example controls.
        resqTextView = findViewById(R.id.fieldRequest);
        asyncButton = findViewById(R.id.sendAsync);
        respTextView = findViewById(R.id.fieldResponse);

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
                    respTextView.setText(respText);
                }
            }
        };
        okHttpClient = new OkHttpClient();

        // Click this button to send post data to url asynchronously.
        asyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = ("http://sym.iict.ch/rest/txt");
                try
                {
                    // Create okhttp3.Call object with post http request method.
                    Call call = createHttpPostMethodCall(url, resqTextView.getText().toString());

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
                                // Parse and get server response text data.
                                String respData = parseResponseText(response);

                                // Notify activity main thread to update UI display text with Handler.
                                sendChildThreadMessageToMainThread(respData);
                            }else {
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

    /* Create OkHttp3 Call object use post method with url. */
    private Call createHttpPostMethodCall(String url, String param)
    {
        // Create okhttp3 form body builder.
        RequestBody reqBody = RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), param);

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
}