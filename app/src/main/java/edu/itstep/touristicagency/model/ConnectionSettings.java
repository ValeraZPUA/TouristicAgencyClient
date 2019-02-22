package edu.itstep.touristicagency.model;

public class ConnectionSettings
{
    private String ip;
    private int port;

    public ConnectionSettings()
    {
        ip = "192.168.1.103";
        port = 5296;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }


    @Override
    public String toString() {
        return "ConnectionSettings{" +
                "ip='" + ip + '\'' +
                ", port=" + port + '}';
    }
}
