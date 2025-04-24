package com.example.circleareaclient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class CommunicateActivity extends AppCompatActivity {

    private Socket socket = null;
    private PrintWriter out = null;
    private Scanner in = null;
    private TextView tv;

    // In onCreate, connect to the server, and then wait for the
    // user to input the radius and press the button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_communicate);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        int port;
        String hostname;

        // Get the hostname from the intent

        Intent intent = getIntent();
        hostname = intent.getStringExtra(MainActivity.HOST_NAME);

        // Get the port from the intent.  Default port is 4000

        port = intent.getIntExtra(MainActivity.PORT, 4000);

        // get a handle on the textview for displaying the area

        tv = (TextView) findViewById(R.id.text_answer);

        // Try to open the connection to the server

        try
        {
            socket = new Socket(hostname, port);

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new Scanner(new InputStreamReader(socket.getInputStream()));

            tv.setText("Connected to server.");
        }
        catch(IOException e)  // socket problems
        {
            tv.setText("Problem: " + e.toString());
            socket = null;
        }

    } // end onCreate

    // send the radius to the server, and receive the result (area of the
    // circle) in return

    //////////////////////////////////////////////////////////////////////
    //
    //  Preet Patel
    //  4/12/25
    //  Project #2
    //
    //////////////////////////////////////////////////////////////////////

    public void sendParagraphRequest(View view) {
        EditText opInput = findViewById(R.id.edit_operation);
        EditText fileInput = findViewById(R.id.edit_filename);
        String operation = opInput.getText().toString().trim();
        String filename = fileInput.getText().toString().trim();

        if (socket == null) {
            tv.setText("not connected");
            return;
        }

        if (operation.isEmpty() || filename.isEmpty()) {
            tv.setText("please enter both operation and filename");
            return;
        }

        List<String> par = new LinkedList<>();

        try {
            InputStream is = getAssets().open(filename);
            Scanner fileScanner = new Scanner(new InputStreamReader(is));
            while (fileScanner.hasNextLine()) {
                par.add(fileScanner.nextLine());
            }

            out.println(operation);

            out.println(par.size());

            for (int i=0; i<par.size(); i++) {
                out.println(par.get(i));
            }

            int numResponseLines = Integer.parseInt(in.nextLine());
            StringBuilder response = new StringBuilder();
            for (int i = 0; i < numResponseLines; i++) {
                response.append(in.nextLine()).append("\n");
            }

            tv.setText(response.toString().trim());

            out.close();
            in.close();
            socket.close();
            socket = null;

        } catch (IOException e) {
            tv.setText("error: " + e.toString());
        }
    }
} // end CommunicateActivity
