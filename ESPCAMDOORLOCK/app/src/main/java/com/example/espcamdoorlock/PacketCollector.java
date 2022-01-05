package com.example.espcamdoorlock;

import java.nio.*;
import java.util.*;
public class PacketCollector
{
    public char symb='F';
    boolean frag;

    ByteBuffer bb;
    int frag_id;

    int seq;
    int rseq;
    int total_frag_len;
    int current_frag_len;

    int stat_frag[];
    int frag_size;


    byte data[];
    boolean ready;

    public PacketCollector()
    {
        bb=null;
        frag_id=-1;

        seq=-1;
        total_frag_len=-1;
        current_frag_len=-1;

        stat_frag=null;
        frag_size=-1;

        data=null;
        ready=false;
    }

    public boolean decodePacket(byte a[])
    {
        int tfrag_id;
        try
        {
            if(a[1]=='F')
            {
                frag_id=byteConversion(a[2],a[3],a[4],a[5]);
                seq=unsignedByteConversion(a[6]);
                total_frag_len=byteConversion(a[7],a[8]);
                current_frag_len=byteConversion(a[9],a[10]);
                calculateFragSize();
                bb.position(stat_frag[seq]);
                bb.put(Arrays.copyOfRange(a,11,a.length));
                stat_frag[seq]=-1;
                rseq=0;
            }
            else if(a[1]=='L')
            {
                tfrag_id=byteConversion(a[2],a[3],a[4],a[5]);
                if(tfrag_id==frag_id)
                {
                    seq=unsignedByteConversion(a[6]);
                    current_frag_len=byteConversion(a[7],a[8]);
                    bb.position(stat_frag[seq]+rseq);
                    bb.put(Arrays.copyOfRange(a,9,a.length));
                    stat_frag[seq]=-1;
                    for(int x : stat_frag)
                    {
                        if(x!=-1)
                        {
                            return false;
                        }
                    }
                    data=bb.array();
                    ready=true;
                    return true;
                }
            }
            else if(a[1]=='M')
            {
                tfrag_id=byteConversion(a[2],a[3],a[4],a[5]);
                if(tfrag_id==frag_id)
                {
                    seq=unsignedByteConversion(a[6]);
                    current_frag_len=byteConversion(a[7],a[8]);
                    bb.position(stat_frag[seq]+rseq);
                    bb.put(Arrays.copyOfRange(a,9,a.length));
                    stat_frag[seq]=-1;
                    updateRelativeSequence(seq);
                }
            }
            else if(a[1]=='N')
            {
                current_frag_len=byteConversion(a[2],a[3]);
                data=Arrays.copyOfRange(a,4,a.length);
                if(current_frag_len==data.length)
                {
                    ready=true;
                    return true;
                }
            }
        }
        catch(Exception e)
        {
        }
        return false;
    }
    public boolean decodePacket(ByteBuffer a)
    {
        char packet_type;
        int packet_id;
        int tfrag_id;
        byte extracted_data[];
        try
        {
            a.rewind();
            packet_id=a.get();
            packet_type=(char)(a.get());
            if(packet_type=='F')
            {
                frag_id=a.getInt();
                seq=unsignedByteConversion(a.get());
                total_frag_len=unsignedByteConversion(a.get(),a.get());
                current_frag_len=unsignedByteConversion(a.get(),a.get());
                calculateFragSize();
                bb.position(stat_frag[seq]);
                extracted_data = new byte[current_frag_len];
                a.get(extracted_data);
                bb.put(extracted_data);
                stat_frag[seq]=-1;
                rseq=0;
            }
            else if(packet_type=='L')
            {
                tfrag_id=a.getInt();
                if(tfrag_id==frag_id)
                {
                    seq=unsignedByteConversion(a.get());
                    current_frag_len=unsignedByteConversion(a.get(),a.get());
                    bb.position(stat_frag[seq]+rseq);
                    extracted_data = new byte[current_frag_len];
                    a.get(extracted_data);
                    bb.put(extracted_data);
                    stat_frag[seq]=-1;
                    for(int x : stat_frag)
                    {
                        if(x!=-1)
                        {
                            return false;
                        }
                    }
                    data=bb.array();
                    ready=true;
                    return true;
                }
            }
            else if(packet_type=='M')
            {
                tfrag_id=a.getInt();
                if(tfrag_id==frag_id)
                {
                    seq=unsignedByteConversion(a.get());
                    current_frag_len=unsignedByteConversion(a.get(),a.get());
                    bb.position(stat_frag[seq]+rseq);
                    extracted_data = new byte[current_frag_len];
                    a.get(extracted_data);
                    bb.put(extracted_data);
                    stat_frag[seq]=-1;
                    updateRelativeSequence(seq);
                }
            }
            else if(packet_type=='N')
            {
                current_frag_len=unsignedByteConversion(a.get(),a.get());
                data=new byte[current_frag_len];
                a.get(data);
                if(current_frag_len==data.length)
                {
                    ready=true;
                    return true;
                }
            }
        }
        catch(Exception e)
        {
        }
        return false;
    }
    public byte[] getData()
    {
        return data;
    }
    private void calculateFragSize()
    {
        int i;
        frag_size=(total_frag_len/current_frag_len)+1;
        stat_frag=new int[frag_size];
        for(i=0;i<frag_size;i++)
        {
            stat_frag[i]=i*current_frag_len;
        }
        bb= ByteBuffer.allocate(total_frag_len);
    }
    private void updateRelativeSequence(int a)
    {
        if(a==0xFF)
        {
            rseq+=256;
        }
    }
    private int byteConversion(byte a,byte b)
    {
        ByteBuffer conv = ByteBuffer.allocate(4);
        conv.put((byte)0).put((byte)0).put(a).put(b);
        conv.rewind();
        return conv.getInt();
    }
    private int byteConversion(byte a,byte b,byte c,byte d)
    {
        ByteBuffer conv = ByteBuffer.allocate(4);
        conv.put(a).put(b).put(c).put(d);
        conv.rewind();
        return conv.getInt();
    }
    private int unsignedByteConversion(byte a)
    {
        ByteBuffer conv = ByteBuffer.allocate(4);
        conv.put((byte)0).put((byte)0).put((byte)0).put(a);
        conv.rewind();
        return conv.getInt();
    }
    private int unsignedByteConversion(byte a,byte b)
    {
        ByteBuffer conv = ByteBuffer.allocate(4);
        conv.put((byte)0).put((byte)0).put(a).put(b);
        conv.rewind();
        return conv.getInt();
    }
}

