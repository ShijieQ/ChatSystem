package messageServer;

import appProtocol.Request;
import appProtocol.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * MessageHandler是子线程的线程体类，定义了子线程所需完成的各种方法
 * 与客户端TCP连接的线程完成接收请求，解析请求，发送的响应的操作
 */
public class MessageHandler implements  Runnable {
    private Socket socket;
    //与聊天端的通信时的输入，输出端
    private ObjectInputStream datainput;
    private ObjectOutputStream dataoutput;
    private Thread listener;//子线程对象
    private  static Hashtable<String,InetSocketAddress>clientMessage= new Hashtable<>();//保存p2p注册名和地址
    private Request request;//请求变量
    private Response response;//响应变量
    private boolean keepListening=true;
    //创建客户端子线程的线程体
    public MessageHandler(Socket socket){
        this.socket=socket;
    }
    public synchronized  void start(){
        if(listener==null){
            try{
                //初始化通信与子线程，建立起与commWithServer通信
                datainput=new ObjectInputStream(socket.getInputStream());
                dataoutput=new ObjectOutputStream(socket.getOutputStream());
                listener=new Thread(this);
                listener.start();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    public synchronized  void stop(){
        if(listener!=null){
            try{
                listener.interrupt();;
                listener=null;
                datainput.close();
                dataoutput.close();
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    public void run() {
        try {
            while(keepListening){
                receiveRequest();//接收请求
                parseRequest();//解析请求
                sendResponse();//发送响应
                request=null;
            }
            stop();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            stop();
            System.err.println("与客户端通信出现错误...");
        }
    }
    private void receiveRequest()throws IOException,ClassNotFoundException{
        request=(Request)datainput.readObject();//从commWithServer发送的请求
    }
    private void parseRequest(){
        if(request==null)
            return;
        response=null;
        int requestType=request.getRequestTyper();
        String registerName=request.getRegisterName();
        if(requestType!=1&&!registerNameHasBeenUsed(registerName)){
            response=new Response(1,registerName+"你还未注册！" );
            return;
        }
        switch (requestType){//测试请求类型
            case 1:
                if(registerNameHasBeenUsed(registerName)){
                    response=new Response(1,"|"+registerName+"|"+"已被其他人使用，请使用其他名字注册" );
                    break;
                }
                clientMessage.put(registerName, new InetSocketAddress(socket.getInetAddress(), request.getUDPPort()));
                response=new Response(1,registerName+",你已经注册成功！" );
                System.out.println("|"+registerName+"| 注册成功...");
                break;
            case 2:
                Vector<String> allNameOfRegister= new Vector<>();
                for(Enumeration<String>e=clientMessage.keys();e.hasMoreElements(); ){
                    //生成已注册的P2P端注册名列表
                    allNameOfRegister.addElement(e.nextElement());
                }
                response=new Response(2,allNameOfRegister );
                break;
            case 3:
                String chatRegisterName=request.getChatRegisterName();
                InetSocketAddress chatP2PEndAddress=clientMessage.get(chatRegisterName);
                response=new Response(3, chatP2PEndAddress);
                break;
            case 4:
                clientMessage.remove(registerName);
                response=new Response(1,registerName+",你已经从服务器退出！" );
                keepListening=false;
                System.out.println("|"+registerName+"| 从服务器退出...");
        }
    }
    private boolean registerNameHasBeenUsed(String registerName){
        if(registerName!=null&&clientMessage.get(registerName)!=null)
            return true;
        return false;
    }
    private void sendResponse()throws IOException{
        if(response!=null){
            dataoutput.writeObject(response);//将响应写回到commWithServer
        }
    }
}
