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
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Vector;

public class GetOnlineP2PEnds extends JPanel implements ActionListener {
    private JButton getOnlineP2PEnds,submit;
    private JList list;
    private CommWithServer commWithServer;
    private Request request;
    private Response response;
    private ObjectOutputStream pipedOut;
    private ObjectInputStream pipedIn;
    private static String registerName;
    private int clickNum=0;
    public GetOnlineP2PEnds(CommWithServer commWithServer){
        this.commWithServer=commWithServer;
        setLayout(new BorderLayout());
        getOnlineP2PEnds=new JButton("获取在线P2P端");
        getOnlineP2PEnds.setBackground(Color.green);
        submit=new JButton("提 交");
        submit.setBackground(Color.green);
        getOnlineP2PEnds.addActionListener(this);
        submit.addActionListener(this);
        list=new JList();
        list.setFont(new Font("楷体", Font.BOLD, 15));
        JScrollPane scroll=new JScrollPane();
        scroll.getViewport().setView(list);
        Box box=Box.createHorizontalBox();
        box.add(new JLabel("单击 '获取' ：",JLabel.CENTER));
        box.add(getOnlineP2PEnds);
        JPanel panelR=new JPanel(new BorderLayout());
        panelR.setBackground(new Color(201,210,110 ));
        panelR.add(submit,BorderLayout.SOUTH);
        JPanel panel=new JPanel(new BorderLayout());
        panel.setBackground(new Color(210,210,110 ));
        panel.add(box,BorderLayout.NORTH);
        panel.add(new JLabel("选择聊天P2P端："),BorderLayout.WEST);
        panel.add(scroll,BorderLayout.CENTER);
        panel.add(panelR,BorderLayout.EAST);
        add(panel,BorderLayout.CENTER);
        submit.setEnabled(false);
        validate();
    }
    public static void setRegisterName(String name){
        registerName=name;
    }
    public void actionPerformed(ActionEvent e) {
        if(registerName==null||commWithServer==null||!commWithServer.isAlive()){
            JOptionPane.showMessageDialog(null, "你还没有注册！","信息提示",JOptionPane.PLAIN_MESSAGE);
            return;
        }
        try{
            if(e.getSource()==getOnlineP2PEnds){
                clickNum++;
                if(clickNum==1){
                    PipedInputStream pipedI=new PipedInputStream();
                    PipedOutputStream pipedO=new PipedOutputStream(pipedI);
                    pipedOut=new ObjectOutputStream(pipedO);
                    pipedIn=new ObjectInputStream(pipedI);
                }
                //因为该commWithServer与Register类的commWithServer是同一个，在Register类里commWithServer
                //已经启动了，所以这里不需要再次connect
                request=new Request(2, registerName);
                commWithServer.setRequest(request);
                commWithServer.setPipedOut(pipedOut);
                commWithServer.notifyCommWithServer();
                response=(Response)pipedIn.readObject();
                //以下一行代码从响应中得到在线的P2P端注册名列表
                Vector<String> onLineP2PEnds=response.getAllNameOfRegister();
                list.setListData(onLineP2PEnds);//尝试将null值传递给此方法会导致未定义的行为，并且最有可能发生异常
                submit.setEnabled(true);
            }
            if(e.getSource()==submit){
                List<Object> list2=list.getSelectedValuesList();
                int len=list2.size();
                if(len==0){
                    JOptionPane.showMessageDialog(this, "你还未选择聊天P2P端！","信息提示",JOptionPane.PLAIN_MESSAGE);
                    return;
                }
                String register[]=new String[list2.size()];
                for(int i=0;i<list2.size();i++)
                    register[i]=(String)list2.get(i);
                Vector<InetSocketAddress> P2PEndAddress=new Vector<InetSocketAddress>();
                int chatP2PEnds=0;
                for(int i=0;i<len;i++){
                    if(register[i].equals(registerName))//如果聊天对象名与当前相同，则跳过
                        continue;
                    request=new Request(3, registerName, register[i]);
                    commWithServer.setRequest(request);
                    commWithServer.setPipedOut(pipedOut);
                    commWithServer.notifyCommWithServer();
                    response=(Response)pipedIn.readObject();
                    //将从响应中得到的聊天对象地址加入到列表中
                    P2PEndAddress.add(response.getChatP2PEndAddress());
                    chatP2PEnds++;
                }
                String message=null;
                if(chatP2PEnds==0){
                    message="你只选择了与自己聊天，请重新选择聊天端！";
                }else{
                    Chat.setChatP2PEndAddress(P2PEndAddress);
                    message="已获取到你选择P2P端的地址，请单击左侧的|聊天|按钮";
                }
                JOptionPane.showMessageDialog(this, message,"信息提示",JOptionPane.PLAIN_MESSAGE);
                P2PEndAddress.clear();
                list.setListData(P2PEndAddress);
            }
        }catch (Exception e1){
            JOptionPane.showMessageDialog(this, "与服务器通信出错","警告",JOptionPane.WARNING_MESSAGE);
        }
    }
}
