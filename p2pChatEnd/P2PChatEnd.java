package p2pChatEnd;

import javax.swing.*;
import java.awt.*;

public class P2PChatEnd extends JFrame {
    private Register register;
    private GetOnlineP2PEnds getOnlineP2PEnds;
    private Chat chat;
    private JLabel label;
    private JTabbedPane tabbedPane;
    private Exit exit;
    private CommWithServer commWithServer;
    public P2PChatEnd(){
        setTitle("P2P聊天端");
        label=new JLabel();
        label.setText("P2P聊天端");
        label.setForeground(Color.blue);
        label.setFont(new Font("隶书", Font.BOLD, 22));
        label.setHorizontalTextPosition(SwingConstants.RIGHT);
        label.setBackground(Color.green);

        commWithServer=new CommWithServer();
        register=new Register(commWithServer);
        getOnlineP2PEnds=new GetOnlineP2PEnds(commWithServer);
        chat=new Chat(this);
        register.setChat(chat);
        exit=new Exit(commWithServer,this);
        tabbedPane=new JTabbedPane(JTabbedPane.LEFT);
        tabbedPane.add("系统封面",label);
        tabbedPane.add("注册信息服务器",register);
        tabbedPane.add("选择聊天对象",getOnlineP2PEnds);
        tabbedPane.add("聊天",chat);
        tabbedPane.add("退出信息服务器",exit);
        add(tabbedPane,BorderLayout.CENTER);
        setBounds(120,60,400,147 );
        setVisible(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    public static void main(String[] args) {
        new P2PChatEnd();
    }
}
