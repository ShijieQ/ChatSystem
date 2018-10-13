package p2pChatEnd;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Vector;
/*
    Chat类是"从其他客户端接收聊天信息"子线程的线程体类
 */
public class Chat extends JPanel implements ActionListener,Runnable {
    private JButton popButton;
    private static String registerName;
    private static DatagramSocket socket;
    private static Vector<InetSocketAddress> chatP2PEndAddress;//存放各个聊天对象的地址
    P2PChatEnd p2pChatEnd;
    private ChatWindow chatWindow;//弹出的聊天窗口界面
    public Chat(P2PChatEnd p2pChatEnd){
        this.p2pChatEnd=p2pChatEnd;
        setLayout(new BorderLayout());
        popButton=new JButton("弹出聊天窗口");
        popButton.addActionListener(this);
        add(popButton,BorderLayout.CENTER);
        chatP2PEndAddress= new Vector<>();
    }
    public static void setRegisterName(String name){
        registerName=name;
    }
    public static void  setSocket(DatagramSocket datagramsocket){
        socket=datagramsocket;
    }
    //获得从GetOnlineP2PEnds类中传递过来的聊天对象地址
    public static void setChatP2PEndAddress(Vector<InetSocketAddress>address){
        chatP2PEndAddress.addAll(address);
    }
    public void actionPerformed(ActionEvent e) {
        if(registerName==null){
            JOptionPane.showMessageDialog(null, "你还没有注册！","信息提示",JOptionPane.PLAIN_MESSAGE);
            return;
        }
        if(chatP2PEndAddress.isEmpty()){
            JOptionPane.showMessageDialog(null, "你还没有获取到聊天的P2P端！","信息提示",JOptionPane.PLAIN_MESSAGE);
            return;
        }
        if(chatWindow==null)
            chatWindow=new ChatWindow(registerName, socket, p2pChatEnd);
        chatWindow.setChatP2PEndAddress(chatP2PEndAddress);
        if(!chatWindow.isVisible()){
            chatWindow.beginMonitor(true);
            chatWindow.setVisible(true);
        }
    }
    //"从其他客户端接收聊天信息"子线程的线程体
    public void run() {
        byte[] buffer=new byte[256];
        DatagramPacket packet=null;
        try{
            while (true){
                for(int i=0;i<buffer.length;i++)
                    buffer[i]=(byte)0;
                packet=new DatagramPacket(buffer, buffer.length);//构件数据报
                socket.receive(packet);//从此套接字接收数据包，接收从其他客户端发送的聊天信息
                //获得发送信息的IP地址和端口号
                InetAddress ip=packet.getAddress();
                int port=packet.getPort();
                //构件发送方的套接字地址
                InetSocketAddress socketAddress=new InetSocketAddress(ip, port);
                String received=new String(packet.getData()).trim();
                //返回指定字符第一次出现的字符串内的索引
                int index=received.indexOf('>');
                //返回指定子串的第一次出现的字符串中的索引，从指定的索引开始
                boolean receiveGoodbye=received.indexOf("再见", index+1)==index+1;
                //判断聊天对象地址列表是否包含发送方的地址
                boolean contain=chatP2PEndAddress.contains(socketAddress);
                if(!contain||chatWindow==null||!chatWindow.isVisible()){
                    if(receiveGoodbye)
                        continue;
                    //获得发送方的名字
                    String chatP2PEnd=received.substring(0, index);
                    int option=JOptionPane.showConfirmDialog(this, "收到*"+chatP2PEnd+"*聊天请求，是否接受？");
                    if(option==0){
                        //接受聊天请求，将发送方的地址加入到聊天对象地址列表
                        chatP2PEndAddress.add(socketAddress);
                        if(chatWindow==null){//创建聊天窗口界面
                            chatWindow=new ChatWindow(registerName, socket, p2pChatEnd);
                            chatWindow.validate();
                        }
                        //将聊天对象地址列表传递给聊天界面窗口
                        chatWindow.setChatP2PEndAddress(chatP2PEndAddress);
                        if(!chatWindow.isVisible()){//建立并启动子线程，用于在聊天过程中检测其他客户端是否都已经退出
                            chatWindow.beginMonitor(true);
                            chatWindow.setVisible(true);
                        }
                        //将接收的聊天信息显示与聊天窗口界面
                        chatWindow.setReceived(received);
                    }
                    continue;
                }
                chatWindow.setReceived(received);
                if(receiveGoodbye){
                    chatWindow.endChat(socketAddress);
                    //将发送方从聊天地址列表中移除
                    chatP2PEndAddress.remove(socketAddress);
                }
            }
        }catch (IOException e){
            JOptionPane.showMessageDialog(this, "接受信息时，网络连接出现了问题！");
        }
    }
}
