import java.net.*;
class TesterCam
{
public static void main(String args[])
{
DataHandle dh;
try
{
if(args.length==0)
{
dh=new DataHandle();
}
else if(args.length==1)
{
InetAddress inet = InetAddress.getByName(args[0]);
dh=new DataHandle(args[0]);
}
else
{
System.out.println("EXCEPTION !!!");
}
}
catch(Exception e)
{
System.out.println("EXCEPTION !!!");
}
}
}