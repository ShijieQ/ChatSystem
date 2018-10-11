package appProtocol;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Vector;

public class Response implements Serializable {
    private int responseType;
    private String message;
    private Vector<String> allNameOfRegister;
    private InetSocketAddress chatP2PEndAddress;
    public  Response(int responseType){
        this.responseType=responseType;
    }
    public Response(int responseType, String message) {
        this.responseType = responseType;
        this.message = message;
    }

    public Response(int responseType, Vector<String> allNameOfRegister) {
        this(responseType);
        this.allNameOfRegister = allNameOfRegister;
    }

    public Response(int responseType, InetSocketAddress chatP2PEndAddress) {
        this(responseType);
        this.chatP2PEndAddress = chatP2PEndAddress;
    }

    public int getResponseType() {
        return responseType;
    }

    public String getMessage() {
        return message;
    }

    public Vector<String> getAllNameOfRegister() {
        return allNameOfRegister;
    }

    public InetSocketAddress getChatP2PEndAddress() {
        return chatP2PEndAddress;
    }
}
