import java.net.*;
import java.util.*;
import java.util.concurrent.*;
class DeviceScanner
{
private DatagramSocket ds;
private Set<String> r_ip = new HashSet<String>();
private SynchronousQueue<String> thread_control = new SynchronousQueue<String>(true);
private Thread loop = new Thread()
{
boolean listen=false;
PacketCollector pc;
String dcmd="";
public void run()
{
//System.out.println("THREAD START");
try
{
while(true)
{
//System.out.println("LOOPING start");
try
{
String cmd=thread_control.poll();
if(cmd!=null)
{
if(cmd.equals("LST:0"))
{
dcmd="";
listen=false;
}
else if(cmd.indexOf("LST:1:")==0)
{
pc = new PacketCollector();
dcmd=cmd.substring(6);
listen=true;
}
}
}
catch(Exception ee)
{
}
//System.out.println("second half");
try
{
byte rec[] = new byte[1024];
DatagramPacket dp = new DatagramPacket(rec,rec.length);
ds.receive(dp);
if(listen)
{
if(pc.decodePacket(Arrays.copyOf(dp.getData(),dp.getLength())))
{
byte rdata[]=pc.getData();
String str=new String(rdata,0,rdata.length);
if(str.equals(dcmd))
{
r_ip.add(IPScanner.getIpAsString(dp.getAddress()));
}
}
}
}
catch(Exception ee)
{
}
//System.out.println("LOOPING end");
}
}
catch(Exception e)
{
}
//System.out.println("Thread END");
}
}
;
DeviceScanner()
{
try
{
ds=new DatagramSocket();
ds.setSoTimeout(1);
ds.setTrafficClass(0x04);
loop.start();
}
catch(Exception e)
{
}
}
Set<String> scanDevice(String dcmd,int port,int timeout,int repeat)
{
r_ip.clear();
Set<String> set = new HashSet<String>();
try
{
//System.out.println("SQ CMD 1+");
thread_control.put("LST:1:"+dcmd);
//System.out.println("SQ CMD 1-");
try
{
for(int i=0;i<repeat;i++)
{
for(InetAddress ia : IPScanner.retrieveAllBroadcastIp())
{
DatagramPacket dp = new DatagramPacket(dcmd.getBytes(),0,dcmd.length());
dp.setSocketAddress(new InetSocketAddress(ia,port));
ds.send(dp);
}
}
}
catch(Exception ee)
{
}
Thread.currentThread().sleep(timeout);
//System.out.println("SQ CMD 2+");
thread_control.put("LST:0"+dcmd);
//System.out.println("SQ CMD 2-");
set.addAll(r_ip);
}
catch(Exception e)
{
}
r_ip.clear();
return set;
}
}