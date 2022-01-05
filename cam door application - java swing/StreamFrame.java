import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
class StreamFrame
{
Dimension dim;
static int w,h;
int width=800,height=600;
JFrame jf;

JLabel main_screen;
JButton relay_button;
JButton flash_button;
BufferedImage icon;
StreamFrame()
{

dim=(Toolkit.getDefaultToolkit()).getScreenSize();
w=(int)(dim.getWidth());
h=(int)(dim.getHeight());

jf=new JFrame("CAM DOOR LOCK");
jf.setBounds(((w-width)/2),((h-height)/2),width,height);
jf.setResizable(false);
jf.getContentPane().setLayout(null);
addComponents();
jf.setDefaultCloseOperation(jf.EXIT_ON_CLOSE);
jf.setVisible(true);
jf.repaint();
jf.revalidate();
}
void addComponents()
{
main_screen = new JLabel();
main_screen.setBounds(40, 80, 710, 445);
main_screen.setOpaque(true);
main_screen.setBackground(Color.cyan);
jf.getContentPane().add(main_screen);
relay_button = new JButton("Toggle Relay");
relay_button.setBounds(88, 48, 148, 23);
jf.getContentPane().add(relay_button);
flash_button = new JButton("Toggle Flash");
flash_button.setBounds(274, 48, 148, 23);
jf.getContentPane().add(flash_button);
}
BufferedImage scaledImage(Image a,int w,int h)
{
BufferedImage b;
Graphics2D g2;
b=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
g2=b.createGraphics();
g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
g2.drawImage(a,0,0,w,h,null);
g2.dispose();
return b;
}
void updateFrame(BufferedImage icon)
{
icon=scaledImage(icon,main_screen.getWidth(),main_screen.getHeight());
main_screen.setIcon(new ImageIcon(icon));
}
void updateFrame(byte a[])
{
try
{
updateFrame(ImageIO.read(new ByteArrayInputStream(a)));
}
catch(Exception e)
{
}
}
}