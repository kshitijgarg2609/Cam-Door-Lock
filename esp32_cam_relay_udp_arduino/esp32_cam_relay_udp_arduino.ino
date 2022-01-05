#include "camera_frame_data.h"
#include "WiFiModuleEEPROM.h"
#include "PacketFragmentModule.h"

boolean relay_flg=false;
int relay=14;
int relay_led=15;

boolean flash_flg=false;
int flash=4;

WiFiUDP udp;

void setup()
{
  Serial.begin(115200);
  pinMode(relay,OUTPUT);
  pinMode(relay_led,OUTPUT);
  pinMode(flash,OUTPUT);
  triggerRelay(false);
  triggerFlash(false);
  initPacketDividerModule('F',20480,1280);
  initProcess(true);
  udp.begin(4210);
  initCam(true);
}

void loop()
{
  int i=udp.parsePacket();
  if(i>0)
  {
    uint8_t rdata[i];
    udp.read(rdata,i);
    cmd(byteArrayToString(rdata,i));
  }
  confProcess();
}

void triggerRelay(boolean a)
{
  relay_flg=a;
  if(relay_flg)
  {
    digitalWrite(relay,HIGH);
    digitalWrite(relay_led,HIGH);
  }
  else
  {
    digitalWrite(relay,LOW);
    digitalWrite(relay_led,LOW);
  }
}

String relayState()
{
  return (relay_flg)?"R:1":"R:0";
}

void triggerFlash(boolean a)
{
  flash_flg=a;
  if(flash_flg)
  {
    digitalWrite(flash,HIGH);
  }
  else
  {
    digitalWrite(flash,LOW);
  }
}

String flashState()
{
  return (flash_flg)?"L:1":"L:0";
}

void cmd(String a)
{
  if(a.equals("D:ECHO"))
  {
    sendStringToUdp("D:ECHO");
  }
  else if(a.equals("F:FRAME"))
  {
    sendFrameToUdp();
  }
  else if(a.equals("R:0"))
  {
    triggerRelay(false);
    sendStringToUdp("R:0");
  }
  else if(a.equals("R:1"))
  {
    triggerRelay(true);
    sendStringToUdp("R:1");
  }
  else if(a.equals("R:STATE"))
  {
    sendStringToUdp(relayState());
  }
  else if(a.equals("L:0"))
  {
    triggerFlash(false);
    sendStringToUdp("L:0");
  }
  else if(a.equals("L:1"))
  {
    triggerFlash(true);
    sendStringToUdp("L:1");
  }
  else if(a.equals("L:STATE"))
  {
    sendStringToUdp(flashState());
  }
  else
  {
    sendStringToUdp("D:INAVLID COMMAND");
  }
}

String byteArrayToString(uint8_t *arr,int len)
{
  String a="";
  for(int i=0;i<len;i++)
  {
    a+=(char)(arr[i]);
  }
  return a;
}

void sendStringToUdp(String a)
{
  int flen=a.length();
  initPacketDivider(flen);
  for(int i=0;i<flen;i++)
  {
    if(writePacket((uint8_t)(a.charAt(i))))
    {
      udp.beginPacket(udp.remoteIP(),udp.remotePort());
      udp.write(pkt,pkt_len);
      udp.endPacket();
    }
  }
}

void sendFrameToUdp()
{
  if(getFrame()==1)
  {
    unsigned int flen=framecam->len;
    initPacketDivider(flen+3);
    writePacket((uint8_t)('F'));
    writePacket((uint8_t)(':'));
    writePacket((uint8_t)('1'));
    for(int i=0;i<flen;i++)
    {
      if(writePacket(framecam->buf[i])==1)
      {
        udp.beginPacket(udp.remoteIP(),udp.remotePort());
        udp.write(pkt,pkt_len);
        udp.endPacket();
      }
    }
  }
  else
  {
    sendStringToUdp("F:0");
  }
}
