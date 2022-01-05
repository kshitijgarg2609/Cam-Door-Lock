import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
class DataHandle
{
DatagramSocket ds;
SocketAddress dest;
PacketCollector pc = new PacketCollector();
StreamFrame sf = new StreamFrame();
SynchronousQueue<String> sq = new SynchronousQueue<String>(true);
Thread loop = new Thread()
{
public void run()
{
try
{
ds=new DatagramSocket();
tx.start();
send_tx.start();
//System.out.println("INIT");
while(true)
{
try
{
byte buffer[] = new byte[2560];
DatagramPacket dp = new DatagramPacket(buffer,buffer.length);
ds.receive(dp);
//System.out.println("INCOMING PACKET ...");
byte rdata[]=dp.getData();
//System.out.println(Arrays.toString(rdata));
if(pc.decodePacket(Arrays.copyOf(rdata,dp.getLength())))
{
//System.out.println("PACKET DECODED ...");
byte dec_data[] = pc.getData();
if(dec_data[0]==(byte)('F'))
{
if(dec_data[2]==(byte)('1'))
{
sf.updateFrame(Arrays.copyOfRange(dec_data,3,dec_data.length));
}
}
else if(dec_data[0]==(byte)('R'))
{
}
else if(dec_data[0]==(byte)('L'))
{
}
}
}
catch(Exception e)
{
}
}
}
catch(Exception e)
{
}
}
}
;
Thread tx = new Thread()
{
public void run()
{
while(true)
{
try
{
String str=sq.take();
DatagramPacket dp = new DatagramPacket(str.getBytes(),0,str.length());
dp.setSocketAddress(dest);
ds.send(dp);
}
catch(Exception e)
{
}
}
}
}
;
javax.swing.Timer send_tx = new javax.swing.Timer(60,new ActionListener()
{
public void actionPerformed(ActionEvent e)
{
try
{
String req_frame="F:FRAME";
sq.put(req_frame);
}
catch(Exception ee)
{
}
}
}
);
DataHandle(String ip)
{
addFunctionality();
dest=new InetSocketAddress(ip,4210);
loop.start();
}
DataHandle()
{
this("192.168.4.1");
}
void addFunctionality()
{
sf.relay_button.addActionListener(new ActionListener()
{
boolean toggle_flg=false;
public void actionPerformed(ActionEvent e)
{
toggle_flg=!toggle_flg;
try
{
sq.put(toggle_flg?"R:1":"R:0");
}
catch(Exception ee)
{
}
}
}
);
sf.flash_button.addActionListener(new ActionListener()
{
boolean toggle_flg=false;
public void actionPerformed(ActionEvent e)
{
toggle_flg=!toggle_flg;
try
{
sq.put(toggle_flg?"L:1":"L:0");
}
catch(Exception ee)
{
}
}
}
);
}
}