package appProtocol;

import java.io.Serializable;

public class Request implements Serializable {
    private int requestTyper;
    private String registerName;
    private int UDPPort;
    private String chatRegisterName;

    public Request(int requestTyper,String registerName){
        this.requestTyper=requestTyper;
        this.registerName=registerName;
    }
    public Request(int requestTyper,String registerName, int UDPPort){
        this(requestTyper,registerName);
        this.UDPPort=UDPPort;
    }
    public Request(int requestTyper,String registerName, String chatRegisterName){
        this(requestTyper,registerName);
        this.chatRegisterName=chatRegisterName;
    }

    public int getRequestTyper() {
        return requestTyper;
    }

    public String getRegisterName() {
        return registerName;
    }

    public int getUDPPort() {
        return UDPPort;
    }

    public String getChatRegisterName() {
        return chatRegisterName;
    }
}
