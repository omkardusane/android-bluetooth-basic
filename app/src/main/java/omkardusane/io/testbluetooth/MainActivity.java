package omkardusane.io.testbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Set;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    TextView myLabel;
    EditText myTextbox;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    boolean stopWorker;


    TextView text_view1;
    TextView text_view2;
    TextView text_view3;

    public void log(Object o ){
        Log.d("__om om__ : ",o.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text_view1 = (TextView) findViewById(R.id.id1);
        text_view2 = (TextView) findViewById(R.id.id2);
        text_view3 = (TextView) findViewById(R.id.id3);

        String MarathiFont = "Shiv01.ttf";

        Typeface font1 = Typeface.createFromAsset(getAssets(), MarathiFont);

        String hindi2 = "\u0909 \u0908";
        //  String hu = "\u00A0 \u00A9 \u00Ad ";
        String hu = "\u28a0 \u28a9 \u28ad ";

        text_view1.setText(Html.fromHtml(hu));
        text_view2.setText("");
        text_view3.setText("");

        EditText editText = (EditText) findViewById(R.id.id5);
        editText.setTypeface(font1);
        editText.setVisibility(View.GONE);

        log("OMkar test "+hu);

        Button openButton = (Button) findViewById(R.id.open);
        Button sendButton = (Button) findViewById(R.id.send);
        Button closeButton = (Button) findViewById(R.id.close);
        myLabel = (TextView) findViewById(R.id.label);
        myTextbox = (EditText) findViewById(R.id.entry);

        //Open Button
        //findBT();

        try {
            log("try2");
            //try2();
        } catch (Exception e) {
            e.printStackTrace();
        }

        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    log("Opening");
                    log("try2");
                    try2();

/*                    if(mmDevice !=null)
                    openBT();*/
                } catch (Exception ex) {
                }
            }
        });

        //Send Button
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    try2Send();
                    //sendData();
                } catch (Exception ex) {
                }
            }
        });

        //Close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    closeBT();
                } catch (IOException ex) {
                }
            }
        });
    }
/*         String ChineseFont = "DFXSM1B.TTF";
        String JapanFont = "KADEN___.TTF";

*/


    void findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            myLabel.setText("No bluetooth adapter available");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {

            for (BluetoothDevice device : pairedDevices) {

                Log.d("__om: Device found one", device.toString());
                Log.d("__om: Device name ", device.getName());
                Log.d("__om: Device name ", device.getAddress());
                for (ParcelUuid parcelUuid : device.getUuids()) {
                    Log.d("__om:             :  ", parcelUuid.getUuid().toString());
                }

                if (device.getName().contains("Smart")) {
                    log("using "+device.getName());
                    mmDevice = device;
                    break;
                }
            }
        }
        myLabel.setText("Bluetooth Device Found : " + mmDevice.getName());

    }

    void openBT() throws IOException {
      //  UUID uuid = UUID.fromString("0000110a-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID desktop
       // UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID smartbeetle
        UUID uuid = UUID.fromString  ("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID smartbeetl-8
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);



        // mmDevice.createInsecureRfcommSocketToServiceRecord()
        log("created mSocket : already connected: "+mmSocket.isConnected());
        mmSocket.connect();
        log("connected to msocket");
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();
        log("have streams");

        beginListenForData();

        myLabel.setText(" "+mmDevice.getName().toString()+": Bluetooth Opened");
    }

    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        log("inputstream available ");
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            myLabel.setText(data);
                                            text_view3.setText(data);
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    void sendData() throws IOException {
        String msg = myTextbox.getText().toString();
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        myLabel.setText("Data Sent");
    }

    void closeBT() throws IOException {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        myLabel.setText("Bluetooth Closed");
    }

    BluetoothSocket bluetoothSocket = null;

    public boolean try2() throws IOException
    {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        BluetoothDevice device = getPrinterByName();

        if (bluetoothSocket != null)
        {
            bluetoothSocket.close();
        }


        try {

            Method m=device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            log("creatring socket");
            bluetoothSocket=    (BluetoothSocket) m.invoke(device, 1);
            log("created socket");
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (bluetoothSocket == null)
            return false;

        log("attempting to connect.. ");
        bluetoothSocket.connect();
        log("connected");
        mmOutputStream = bluetoothSocket.getOutputStream();
        log("have OutputStream");
        mmInputStream = bluetoothSocket.getInputStream();
        log("have InputStream");

        return true;
    }

    public void try2Send() throws IOException {

        mmOutputStream.write("Yo".getBytes());
        log("Data Sent");

    }

    private BluetoothDevice getPrinterByName()
    {
        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        for (BluetoothDevice device : pairedDevices)
        {

            log("device name: "+device.getName());

            if (device.getName() == null)
                continue;
           // if (device.getName().contains("Smart"))
            if (device.getAddress().equals("AC:C1:EE:61:98:CE"))
            {
                log("found "+device.getName());
                return device;
                //              pairPrinter(printerName);
                //return remoteDevice;
            }
        }
        return null;
    }

}





/*
* 05-09 23:10:59.362 21208-21208/omkardusane.io.testbluetooth D/__om: Device name: DESKTOP-A6TKEJT
05-09 23:10:59.362 21208-21208/omkardusane.io.testbluetooth D/__om: Device name: B4:AE:2B:EB:BA:9F
05-09 23:10:59.364 21208-21208/omkardusane.io.testbluetooth D/__om:             :: 0000110a-0000-1000-8000-00805f9b34fb
05-09 23:10:59.364 21208-21208/omkardusane.io.testbluetooth D/__om:             :: 00001115-0000-1000-8000-00805f9b34fb
05-09 23:10:59.364 21208-21208/omkardusane.io.testbluetooth D/__om:             :: 0000111f-0000-1000-8000-00805f9b34fb


 Device found one: AC:C1:EE:61:98:CE
05-10 14:34:11.405 19458-19458/omkardusane.io.testbluetooth D/__om: Device name: SmartBeetle
05-10 14:34:11.405 19458-19458/omkardusane.io.testbluetooth D/__om: Device name: AC:C1:EE:61:98:CE
05-10 14:34:11.406 19458-19458/omkardusane.io.testbluetooth D/__om:             :: 00001101-0000-1000-8000-00805f9b34fb
05-10 14:34:11.406 19458-19458/omkardusane.io.testbluetooth D/__om:             :: 00001103-0000-1000-8000-00805f9b34fb
05-10 14:34:11.406 19458-19458/omkardusane.io.testbluetooth D/__om:             :: 0000110a-0000-1000-8000-00805f9b34fb
05-10 14:34:11.406 19458-19458/omkardusane.io.testbluetooth D/__om:             :: 00001105-0000-1000-8000-00805f9b34fb
05-10 14:34:11.406 19458-19458/omkardusane.io.testbluetooth D/__om:             :: 00001106-0000-1000-8000-00805f9b34fb
05-10 14:34:11.406 19458-19458/omkardusane.io.testbluetooth D/__om:             :: 00001115-0000-1000-8000-00805f9b34fb
05-10 14:34:11.406 19458-19458/omkardusane.io.testbluetooth D/__om:             :: 00001116-0000-1000-8000-00805f9b34fb
05-10 14:34:11.406 19458-19458/omkardusane.io.testbluetooth D/__om:             :: 0000112d-0000-1000-8000-00805f9b34fb
05-10 14:34:11.406 19458-19458/omkardusane.io.testbluetooth D/__om:             :: 0000110e-0000-1000-8000-00805f9b34fb
05-10 14:34:11.406 19458-19458/omkardusane.io.testbluetooth D/__om:             :: 0000112f-0000-1000-8000-00805f9b34fb
05-10 14:34:11.406 19458-19458/omkardusane.io.testbluetooth D/__om:             :: 00001112-0000-1000-8000-00805f9b34fb
05-10 14:34:11.406 19458-19458/omkardusane.io.testbluetooth D/__om:             :: 0000111f-0000-1000-8000-00805f9b34fb
05-10 14:34:11.406 19458-19458/omkardusane.io.testbluetooth D/__om:             :: 00001132-0000-1000-8000-00805f9b34fb

 */