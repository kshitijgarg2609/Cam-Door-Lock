package com.example.espcamdoorlock;

import java.net.*;
import java.util.*;
class IPScanner
{
    static Set<InetAddress> retrieveAllBroadcastIp()
    {
        Set<InetAddress> broad_cast = new HashSet<>();
        try
        {
            for(NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces()))
            {
                if(ni.isLoopback() || !ni.isUp())
                {
                    continue;
                }
                for(InterfaceAddress ia : ni.getInterfaceAddresses())
                {
                    InetAddress b = ia.getBroadcast();
                    if(b!=null)
                    {
                        broad_cast.add(b);
                    }
                }
            }
        }
        catch(Exception e) {
        }
        return broad_cast;
    }
    static String getIpAsString(InetAddress a)
    {
        byte arr[] = a.getAddress();
        return String.format("%d.%d.%d.%d",new Object[]{
                Byte.toUnsignedInt(arr[0]),
                Byte.toUnsignedInt(arr[1]),
                Byte.toUnsignedInt(arr[2]),
                Byte.toUnsignedInt(arr[3]),
        });
    }
}
