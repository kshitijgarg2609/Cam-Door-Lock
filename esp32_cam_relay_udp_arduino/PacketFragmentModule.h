void initPacketDividerModule(char,int,int);
void initPacketDivider(unsigned int);
void readyPacket();
int writePacket(uint8_t);

typedef struct packetinfo
{
  unsigned int p_cnt;
  unsigned int plen;
  unsigned int tlen;
  uint8_t seq;
  unsigned char pflg;
}
packetinfo;

uint8_t indc;
unsigned int pdiv;
packetinfo *pktinfo;
uint8_t *pkt;
unsigned int pkt_len=0;

unsigned int pfrag_n;
unsigned int pfrag_c;
unsigned int total_plen;
uint8_t pkt_id[4];
unsigned int p_indx;
boolean rdy;

void initPacketDividerModule(char symb,int tl,int fl)
{
  indc=(uint8_t)(symb);
  pdiv=fl;
  pktinfo=(packetinfo*)malloc(((tl/fl)+1)*sizeof(packetinfo));
  pkt=(uint8_t*)malloc((fl+11)*sizeof(uint8_t));
}

void initPacketDivider(unsigned int a)
{
  rdy=false;
  total_plen=a;
  pfrag_c=-1;
  unsigned long tstamp=millis();
  pkt_id[0]=(uint8_t)(tstamp>>24);
  pkt_id[1]=(uint8_t)(tstamp>>16);
  pkt_id[2]=(uint8_t)(tstamp>>8);
  pkt_id[3]=(uint8_t)(tstamp);
  if(a>=pdiv)
  {
    float temp_len=a;
    pfrag_n=ceil(temp_len/pdiv);
    for(unsigned int i=0;i<pfrag_n;i++)
    {
      pktinfo[i].seq=(uint8_t)(i);
      pktinfo[i].p_cnt=0;
      if(i==0)
      {
        pktinfo[i].plen=pdiv;
        pktinfo[i].tlen=pktinfo[i].plen+7+4;
        pktinfo[i].pflg='F';
      }
      else if(i==pfrag_n-1)
      {
        pktinfo[i].plen=(a%pdiv);
        pktinfo[i].tlen=pktinfo[i].plen+5+4;
        pktinfo[i].pflg='L';
      }
      else
      {
        pktinfo[i].plen=pdiv;
        pktinfo[i].tlen=pktinfo[i].plen+5+4;
        pktinfo[i].pflg='M';
      }
    }
  }
  else
  {
    //Serial.println("test");
    pfrag_n=1;
    pktinfo[0].p_cnt=0;
    pktinfo[0].seq=(uint8_t)(0);
    pktinfo[0].plen=a;
    pktinfo[0].tlen=pktinfo[0].plen+4;
    pktinfo[0].pflg='N';
  }
  readyPacket();
}

void readyPacket()
{
   pfrag_c++;
   p_indx=0;
   pkt[0]=indc;
   if(pktinfo[pfrag_c].pflg=='F')
   {
      pkt[1]=(uint8_t)('F');
      pkt[2]=pkt_id[0];
      pkt[3]=pkt_id[1];
      pkt[4]=pkt_id[2];
      pkt[5]=pkt_id[3];
      pkt[6]=pktinfo[pfrag_c].seq;
      pkt[7]=(uint8_t)(total_plen/256);
      pkt[8]=(uint8_t)(total_plen%256);
      pkt[9]=(uint8_t)(pktinfo[pfrag_c].plen/256);
      pkt[10]=(uint8_t)(pktinfo[pfrag_c].plen%256);
      p_indx=11;
   }
   else if(pktinfo[pfrag_c].pflg=='L')
   {
      pkt[1]=(uint8_t)('L');
      pkt[2]=pkt_id[0];
      pkt[3]=pkt_id[1];
      pkt[4]=pkt_id[2];
      pkt[5]=pkt_id[3];
      pkt[6]=pktinfo[pfrag_c].seq;
      pkt[7]=(uint8_t)(pktinfo[pfrag_c].plen/256);
      pkt[8]=(uint8_t)(pktinfo[pfrag_c].plen%256);
      p_indx=9;
   }
   else if(pktinfo[pfrag_c].pflg=='M')
   {
      pkt[1]=(uint8_t)('M');
      pkt[2]=pkt_id[0];
      pkt[3]=pkt_id[1];
      pkt[4]=pkt_id[2];
      pkt[5]=pkt_id[3];
      pkt[6]=pktinfo[pfrag_c].seq;
      pkt[7]=(uint8_t)(pktinfo[pfrag_c].plen/256);
      pkt[8]=(uint8_t)(pktinfo[pfrag_c].plen%256);
      p_indx=9;
   }
   else if(pktinfo[pfrag_c].pflg=='N')
   {
      pkt[1]=(uint8_t)('N');
      pkt[2]=(uint8_t)(pktinfo[pfrag_c].plen/256);
      pkt[3]=(uint8_t)(pktinfo[pfrag_c].plen%256);
      p_indx=4;
   }
}

int writePacket(uint8_t a)
{
  if(pfrag_c>=pfrag_n)
  {
    return -1;
  }
  if(rdy)
  {
    readyPacket();
    rdy=false;
  }
  pkt[p_indx++]=a;
  pktinfo[pfrag_c].p_cnt++;
  if(pktinfo[pfrag_c].p_cnt==pktinfo[pfrag_c].plen)
  {
    pkt_len=pktinfo[pfrag_c].tlen;
    if(pfrag_c!=pfrag_n-1)
    {
      rdy=true;
    }
    return 1;
  }
  return 0;
}
