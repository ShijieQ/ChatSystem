package p2pChatEnd;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Vector;

public class ChatWindow extends JFrame implements ActionListener,Runnable {
    private  JTextArea messageArea,inputArea;
    private JButton sendButton,quitButton,clearButton;
    private JLabel hintMessage1,hintMessage2,statusBar;
    private String registerName;
    private DatagramSocket socket;
    private Vector<InetSocketAddress> chatP2PEndAddress;
    private P2PChatEnd p2pChatEnd;
    private Thread chatP2PEndMonitor;//聊天过程中，检测其他P2P端是否都已经退出子线程
    private boolean monitoring;
    private String newLine;
    public ChatWindow(String registerName,DatagramSocket socket,P2PChatEnd p2pChatEnd){
        super("聊天窗口");
        this.registerName=registerName;
        this.socket=socket;
        this.p2pChatEnd=p2pChatEnd;
        monitoring=true;
        newLine= System.getProperty("line.separator");//换行符，作用与'\n'相同，这样可以保证在其他操作系统也能过执行
        hintMessage1=new JLabel("显示聊天记录");
        hintMessage2=new JLabel("编辑信息");
        messageArea=new JTextArea(4, 20);
        messageArea.setEditable(false);
        messageArea.setWrapStyleWord(true);
        messageArea.setLineWrap(true);
        inputArea=new JTextArea(4, 20);
        inputArea.setWrapStyleWord(true);
        messageArea.setLineWrap(true);
         sendButton=new JButton("发送");
         sendButton.addActionListener(this);
         quitButton=new JButton("退出");
         quitButton.addActionListener(this);
         clearButton=new JButton("清空");//清空当前的聊天记录
         clearButton.addActionListener(this);
         statusBar=new JLabel("在线： "+registerName);
         statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
         JPanel messagePanel=new JPanel();
         messagePanel.setLayout(new BorderLayout());
         messagePanel.add(hintMessage1,BorderLayout.NORTH);
         messagePanel.add(new JScrollPane(messageArea),BorderLayout.CENTER);
         JPanel buttonPanel=new JPanel();
         buttonPanel.setLayout(new GridLayout(3,1));
         buttonPanel.add(sendButton);
         buttonPanel.add(quitButton);
         buttonPanel.add(clearButton);
         Box box1=new Box(BoxLayout.X_AXIS);
         box1.add(new JScrollPane(inputArea));
         box1.add(buttonPanel);
         Box box=new Box(BoxLayout.Y_AXIS);
         box.add(hintMessage2);
         box.add(box1);
         messagePanel.add(box,BorderLayout.SOUTH);
         Container contenPane=getContentPane();
         contenPane.add(messagePanel,BorderLayout.CENTER);
         contenPane.add(statusBar,BorderLayout.SOUTH);
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 close();
             }
         });
         setSize(300,400 );
         Exit.setChatWindow(this);
    }
    public void setChatP2PEndAddress(Vector<InetSocketAddress> chatP2PEndAddress){//获得聊天对象地址列表
        this.chatP2PEndAddress=chatP2PEndAddress;
    }
    public void beginMonitor(boolean monitoring){
        this.monitoring=monitoring;
        chatP2PEndMonitor=new Thread(this);
        chatP2PEndMonitor.start();
    }
    public void setReceived(String received){
        messageArea.append(received+newLine);
    }
    public void endChat(InetSocketAddress isa){//向聊天对象也发送一个再见
        String message=registerName+">再见！";
        messageArea.append(message+newLine);
        byte[] buf=message.getBytes();
        try{
            DatagramPacket packet=null;
            packet=new DatagramPacket(buf,buf.length ,isa.getAddress() ,isa.getPort() );
            socket.send(packet);
        }catch (IOException ee){
            JOptionPane.showMessageDialog(this, "发送|再见|时，网络连接错误!");
        }
        chatP2PEndAddress.remove(isa);
    }
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==sendButton){
            String message=registerName+">"+inputArea.getText().trim();
            sendMessage(message);
            inputArea.setText("");
        }
        if(e.getSource()==quitButton){
            close();
        }
        if(e.getSource()==clearButton){
            messageArea.setText("");
        }
    }
    public void sendMessage(String message){
        messageArea.append(message+newLine);
        byte[] buf=message.getBytes();
        DatagramPacket packet=null;
        try {
            for(InetSocketAddress isa:chatP2PEndAddress){//将信息发送给聊天对象地址列表中每一个
                packet=new DatagramPacket(buf,buf.length ,isa.getAddress(),isa.getPort());
                //UDP是无连接通信，获得IP地址和端口号即可通信
                socket.send(packet);
            }
        }catch (IOException ee){
            JOptionPane.showMessageDialog(this, "发送信息时，网络连接错误！");
        }
    }
    public void close(){
        if(!chatP2PEndAddress.isEmpty()){
            int option=JOptionPane.showConfirmDialog(this, "正在聊天，确认要退出窗口吗？");
            if(option!=0)
                return;
            String message=registerName+">再见";
            sendMessage(message);
            monitoring=false;
            int i=0;
            do{

            }while (!chatP2PEndAddress.isEmpty()&&++i<=30);
        }
        monitoring=false;
        chatP2PEndMonitor=null;
        //如果主界面不存在或者主界面不可见就退出程序
        if(p2pChatEnd==null||!p2pChatEnd.isVisible())
            System.exit(0);
        else{
            setVisible(false);
            messageArea.setText("");
        }

    }
    public void run() {
        while(monitoring){
            if(!isVisible()||!chatP2PEndAddress.isEmpty())
                continue;
            int option=JOptionPane.showConfirmDialog(this, "对方都已经退出，是否关闭本窗口？");
            if(option!=0){
                try {
                    chatP2PEndMonitor.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                continue;
            }
            close();
        }
    }
}
