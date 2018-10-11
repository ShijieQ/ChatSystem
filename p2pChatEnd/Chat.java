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

public class Chat extends JPanel implements ActionListener,Runnable {
    private JButton popButton;
    private static String registerName;
    private static DatagramSocket socket;
    private static Vector<InetSocketAddress> chatP2PEndAddress;
    P2PChatEnd p2pChatEnd;
    private ChatWindow chatWindow;
    public Chat(P2PChatEnd p2pChatEnd){
        this.p2pChatEnd=p2pChatEnd;
        setLayout(new BorderLayout());
        popButton=new JButton("弹出聊天窗口");
        popButton.addActionListener(this);
        chatP2PEndAddress=new Vector<InetSocketAddress>();
    }
    public static void setRegisterName(String name){
        registerName=name;
    }
    public static void  setSocket(DatagramSocket datagramsocket){
        socket=datagramsocket;
    }
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
    public void run() {
        byte[] buffer=new byte[256];
        DatagramPacket packet=null;
        try{
            while (true){
                for(int i=0;i<buffer.length;i++)
                    buffer[i]=(byte)0;
                packet=new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                InetAddress ip=packet.getAddress();
                int port=packet.getPort();
                InetSocketAddress socketAddress=new InetSocketAddress(ip, port);
                String received=new String(packet.getData()).trim();
                int index=received.indexOf('>');
                boolean receiveGoodbye=received.indexOf("再见", index+1)==index+1;
                boolean contain=chatP2PEndAddress.contains(socketAddress);
                if(!contain||chatWindow==null||!chatWindow.isVisible()){
                    if(receiveGoodbye)
                        continue;
                    String chatP2PEnd=received.substring(0, index);
                    int option=JOptionPane.showConfirmDialog(this, "收到|"+chatP2PEnd+"|聊天请求，是否接受？");
                    if(option==0){
                        chatP2PEndAddress.add(socketAddress);
                        if(chatWindow==null){
                            chatWindow=new ChatWindow(registerName, socket, p2pChatEnd);
                            chatWindow.validate();
                        }
                        chatWindow.setChatP2PEndAddress(chatP2PEndAddress);
                        if(!chatWindow.isVisible()){
                            chatWindow.beginMonitor(true);
                            chatWindow.setVisible(true);
                        }
                        chatWindow.setReceived(received);
                    }
                    continue;
                }
                chatWindow.setReceived(received);
                if(receiveGoodbye){
                    chatWindow.endChat(socketAddress);
                    chatP2PEndAddress.remove(socketAddress);
                }
            }
        }catch (IOException e){
            JOptionPane.showMessageDialog(this, "接受信息时，网络连接出现了问题！");
        }
    }
}
