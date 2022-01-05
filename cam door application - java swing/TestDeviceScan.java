class TestDeviceScan
{
DeviceScanner ds = new DeviceScanner();
void test()
{
for(String str : ds.scanDevice("D:ECHO",4210,3000,8))
{
System.out.println(str);
}
}
void test(int a)throws Exception
{
for(int i=0;i<a;i++)
{
System.out.println("TESTing");
test();
Thread.currentThread().sleep(1500);
}
}
}