package com.example.testapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.Transport;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText messageText;
    private Button sendBtn;
    private Socket mSocket;
    private String userName;
    private ListView messageList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageText = findViewById(R.id.msgText);
        sendBtn = findViewById(R.id.sendButton);
        messageList = findViewById(R.id.messageList);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        userName = getIntent().getExtras().getString("userName");
        messageList.setAdapter(adapter);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageText.getText().toString().trim();
                //Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                messageText.setText("");
                mSocket.emit("new message",message);
                adapter.add(userName+": "+ message);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        try{
            mSocket = IO.socket("http://192.168.0.182:3000");
            mSocket.connect();
            mSocket.on("new message",onMessage);
            mSocket.on(Socket.EVENT_CONNECT,onConnect);
            mSocket.on(Socket.EVENT_CONNECT_ERROR,onConnectError);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT,onConnectTimeout);
         //   mSocket.emit("new Message")
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject)args[0];
                    try {
                        Log.i("newMessage","Got some message");
                        adapter.add(userName+": "+ data.getString("message"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    };
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("ConnectionInfo: ", "Connected");
                    Toast.makeText(getApplicationContext(),"Connected",Toast.LENGTH_SHORT).show();
                    adapter.add(userName + " has Joined");
                }
            });
        }
    };
    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("ConnectionInfo: ", "Connection Error");
                }
            });
        }
    };
    private Emitter.Listener onConnectTimeout = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("ConnectionInfo: ", "Connection TimedOut");
                }
            });
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off("new message",onMessage);
        mSocket.off(Socket.EVENT_CONNECT,onConnect);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT,onConnectTimeout);
        mSocket.off(Socket.EVENT_CONNECT_ERROR,onConnectError);
    }
}
