package com.example.espcamdoorlock;

import android.graphics.BitmapFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.concurrent.SynchronousQueue;

public class UdpCmdDecoder
{
    private DatagramSocket ds;
    private int dest_port=4210;
    private DecodedDataListener ddl;
    private SynchronousQueue<String> thread_control = new SynchronousQueue<>(true);
    private SynchronousQueue<String> tx_data = new SynchronousQueue<>(true);
    private Thread loop_tx = new Thread()
    {
        SocketAddress sa;
        boolean connect_flg=false;
        public void run()
        {
            try
            {
                while(true)
                {
                    try
                    {
                        String cmd = thread_control.poll();
                        if(cmd!=null)
                        {
                            if(cmd.indexOf("start:")==0)
                            {
                                sa=new InetSocketAddress(cmd.substring(6),dest_port);
                                connect_flg=true;
                            }
                            else if(cmd.equals("stop"))
                            {
                                connect_flg=false;
                            }
                        }
                    }
                    catch(Exception ee)
                    {

                    }

                    try
                    {
                        String txd = tx_data.poll();
                        if(txd!=null && connect_flg)
                        {
                            DatagramPacket dp = new DatagramPacket(txd.getBytes(),0,txd.length());
                            dp.setSocketAddress(sa);
                            ds.send(dp);
                        }
                    }
                    catch(Exception ee)
                    {

                    }
                }
            }
            catch(Exception e)
            {

            }
        }
    };
    private Thread loop_rx = new Thread()
    {
        PacketCollector pc = new PacketCollector();
        public void run()
        {
            while(true)
            {
                try
                {
                    byte arr[] = new byte[1300];
                    DatagramPacket dp = new DatagramPacket(arr,arr.length);
                    ds.receive(dp);
                    if(pc.decodePacket(Arrays.copyOf(dp.getData(),dp.getLength())))
                    {
                        byte dec_data[] = pc.getData();
                        if(dec_data[0]==(byte)('F'))
                        {
                            if(dec_data[2]==(byte)('1'))
                            {
                                try {
                                    byte raw_frame[] =
                                            Arrays.copyOfRange(dec_data, 3, dec_data.length);
                                    ddl.frameData(
                                            BitmapFactory.decodeByteArray
                                                    (raw_frame, 0, raw_frame.length), true);
                                }
                                catch(Exception ee1)
                                {
                                    ddl.frameData(null,false);
                                }

                            }
                            else
                            {
                                ddl.frameData(null,false);
                            }
                        }
                        else if(dec_data[0]==(byte)('R'))
                        {
                            ddl.relayData((dec_data[2])=='1');
                        }
                        else if(dec_data[0]==(byte)('L'))
                        {
                            ddl.flashData((dec_data[2])=='1');
                        }
                    }
                }
                catch(Exception ee)
                {

                }
            }
        }
    }
    ;
    public UdpCmdDecoder(DecodedDataListener d,int p)
    {
        ddl=d;
        dest_port=p;
        try
        {
            ds=new DatagramSocket();
            loop_rx.start();
            loop_tx.start();
        }
        catch(Exception e)
        {

        }
    }
    public void sendCommand(String a)
    {
        try
        {
            thread_control.put(a);
        }
        catch(Exception e)
        {

        }
    }
    public void sendData(String a)
    {
        try
        {
            tx_data.put(a);
        }
        catch(Exception e)
        {

        }
    }
}
