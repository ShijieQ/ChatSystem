package messageServer;

import java.net.ServerSocket;
import java.net.Socket;

public class MessageServer {
    public static final int PORT=8000;
    public static final int MAX_QUEUE_LENGTH=100;

    public void start(){
        try{
            ServerSocket s=new ServerSocket(PORT, MAX_QUEUE_LENGTH);
            System.out.println("****服务器已经启动...****");
            while(true){
                Socket socket=s.accept();//监听是否有客户端的连接，如果有，返回socket对象，建立起了TCP连接
                System.out.println("已接收到客户来自： "+socket.getInetAddress());
                MessageHandler handler=new MessageHandler(socket);//
                handler.start();//创建MessageHandler类型的子线程，与客户端通信
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        MessageServer ms=new MessageServer();
        ms.start();
    }
}
