package p2pChatEnd;

import appProtocol.Request;
import appProtocol.Response;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class Exit extends JPanel implements ActionListener {
    private CommWithServer commWithServer;
    private Request request;
    private Response response;
    private ObjectOutputStream pipedOut;
    private ObjectInputStream pipedIn;
    private static String registerName;
    private P2PChatEnd P2PChatEnd;
    private static ChatWindow chatWindow;
    public Exit(CommWithServer commWithServer,P2PChatEnd P2PChatEnd){
        this.commWithServer=commWithServer;
        this.P2PChatEnd=P2PChatEnd;
        JLabel hint=new JLabel("确认要退出信息服务器吗？");
        JButton quit=new JButton("退出");
        quit.addActionListener(this);
        setLayout(new BorderLayout());
        add(hint,BorderLayout.CENTER);
        add(quit,BorderLayout.SOUTH);
    }
    public static void setRegisterName(String name){
        registerName=name;
    }
    public static void setChatWindow(ChatWindow cw){
        chatWindow=cw;
    }
    public void actionPerformed(ActionEvent e) {
        if(registerName!=null&&commWithServer.isAlive()){
            try {
                PipedInputStream pipedI=new PipedInputStream();
                PipedOutputStream pipedO=new PipedOutputStream(pipedI);
                pipedOut=new ObjectOutputStream(pipedO);
                pipedIn=new ObjectInputStream(pipedI);
                request=new Request(4, registerName);
                commWithServer.setRequest(request);
                commWithServer.setPipedOut(pipedOut);
                commWithServer.notifyCommWithServer();
                response=(Response)pipedIn.readObject();
            }catch (Exception ex){
                JOptionPane.showMessageDialog(this, "与服务器通信出错","警告",JOptionPane.WARNING_MESSAGE);

            }
            String message=response.getMessage()+"单击|确定|退出";
            JOptionPane.showMessageDialog(null, message,"信息提示",JOptionPane.PLAIN_MESSAGE);
            commWithServer.keepCommunicating=false;
            commWithServer.interrupt();
            commWithServer.close();
        }
        commWithServer=null;
        if(chatWindow==null||!chatWindow.isVisible())
            System.exit(0);
        else
            P2PChatEnd.setVisible(false);
    }
}
