package com.example.espcamdoorlock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;

public class MainActivity extends AppCompatActivity
{
    private DeviceScanner ds;
    private List<String> device_list = new LinkedList<>();
    private ArrayAdapter<String> aa;
    private Button scan,connect,disconnect,relay,flash;
    private Spinner ips;
    private ImageView live_screen;
    public static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=getApplicationContext();
        grantPersmission();
        ds = new DeviceScanner();
        esp_cmd_process.start();
        scan=(Button)findViewById(R.id.scan);
        connect=(Button)findViewById(R.id.connect);
        disconnect=(Button)findViewById(R.id.disconnect);
        relay=(Button)findViewById(R.id.relay);
        flash=(Button)findViewById(R.id.flash);
        ips=(Spinner)findViewById(R.id.ips);
        live_screen=(ImageView)findViewById(R.id.video_stream);
        aa=new ArrayAdapter<>
                (context,android.R.layout.simple_spinner_item,device_list);
        aa.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        ips.setAdapter(aa);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                device_list.clear();
                aa.notifyDataSetChanged();
                Set<String> scanned_dev = ds.scanDevice("D:ECHO",4210,3000,7);
                if(scanned_dev.size()>0) {
                    device_list.addAll(scanned_dev);
                    aa.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Device Scanned !!!",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Device Not Found !!!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ucd.sendCommand("start:"+(String)ips.getSelectedItem());
                    cmd_control.put("C:start");
                }
                catch(Exception e)
                {

                }

            }
        });
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ucd.sendCommand("stop");
                    cmd_control.put("C:stop");
                }
                catch(Exception e)
                {

                }
            }
        });
        relay.setOnClickListener(new View.OnClickListener()
        {
            boolean flg=false;
            @Override
            public void onClick(View v) {
                flg=!flg;
                try {
                    cmd_control.put("D:R:"+((flg)?"1":"0"));
                }
                catch(Exception e)
                {

                }
            }
        });
        flash.setOnClickListener(new View.OnClickListener()
        {
            boolean flg=false;
            @Override
            public void onClick(View v) {
                flg=!flg;
                try {
                    cmd_control.put("D:L:"+((flg)?"1":"0"));
                }
                catch(Exception e)
                {

                }
            }
        });
    }
    private static final String[] REQUIRED_PERMISSIONS = new String[]
            { Manifest.permission.INTERNET};
    private static final int REQUEST_CODE_PERMISSIONS = 100;
    void grantPersmission()
    {
        for(String permission: REQUIRED_PERMISSIONS)
        {
            if(ContextCompat.checkSelfPermission(
                getBaseContext(), permission) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,
                        REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            }
        }
    }
    private UdpCmdDecoder ucd = new UdpCmdDecoder(new DecodedDataListener() {
        @Override
        public void onConnect(boolean flg) {

        }

        @Override
        public void frameData(Bitmap frame, boolean flg) {
            //Log.v("TAG","check frame : "+flg);
            if(flg)
            {
                try {
                    //live_screen.setImageBitmap(Bitmap.createBitmap(frame,0,0,live_screen.getWidth(),live_screen.getHeight()));
                    live_screen.setImageBitmap(frame);
                }
                catch(Exception e){

                }
            }
        }

        @Override
        public void relayData(boolean flg) {

        }

        @Override
        public void flashData(boolean flg) {

        }
    },4210);

    private SynchronousQueue<String> cmd_control = new SynchronousQueue<>(true);
    private Thread esp_cmd_process = new Thread()
    {
        boolean connect_flg=false;

        long frame_request=50;
        long frame_snp=0;

        long relay_state_timeout=2000;
        long relay_snp=0;

        long flash_state_timeout=2000;
        long flash_snp=0;

        public void run()
        {
            while(true)
            {
                try {
                    String cmd=cmd_control.poll();
                    if(cmd!=null)
                    {
                        if(cmd.charAt(0)=='C') {
                            if (cmd.equals("C:start")) {
                                connect_flg = true;
                            } else if (cmd.equals("C:stop")) {
                                connect_flg = false;
                            }
                        }
                        else if(cmd.charAt(0)=='D')
                        {
                            ucd.sendData(cmd.substring(2));
                        }
                    }
                    if(connect_flg)
                    {
                        if((System.currentTimeMillis()-frame_snp)>=frame_request)
                        {
                            ucd.sendData("F:FRAME");
                            frame_snp=System.currentTimeMillis();
                        }
                        if((System.currentTimeMillis()-relay_snp)>=relay_state_timeout)
                        {
                            ucd.sendData("R:STATE");
                            relay_snp=System.currentTimeMillis();
                        }
                        if((System.currentTimeMillis()-flash_snp)>=flash_state_timeout)
                        {
                            ucd.sendData("L:STATE");
                            flash_snp=System.currentTimeMillis();
                        }
                    }
                }
                catch(Exception e)
                {

                }
            }
        }
    }
    ;
}