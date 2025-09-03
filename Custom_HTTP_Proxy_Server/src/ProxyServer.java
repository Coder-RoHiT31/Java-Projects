import java.io.*;
import java.net.*;
public class ProxyServer {
    public static void main(String[] args) throws Exception{
        ServerSocket serversocket = new ServerSocket(8888);
        System.out.println("Proxy server running on port 8888...");
        while(true){
            Socket clientSocket = serversocket.accept();
            new Thread(new ProxyHandler(clientSocket)).start();
        }
    }
}
class ProxyHandler implements Runnable{
    private Socket clientSocket;
    public ProxyHandler (Socket clientSocket){
        this.clientSocket = clientSocket;
    }
    public void run(){
        try{
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream clientOut = clientSocket.getOutputStream();
            String requestLine = clientReader.readLine();
            if(requestLine==null || !requestLine.startsWith("Get")){
                clientSocket.close();
                return;
            }
            String[] parts = requestLine.split(" ");
            String url = parts[1];
            URL targetUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)targetUrl.openConnection();
            connection.setRequestMethod("Get");
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String responseLine;
            while((responseLine=serverReader.readLine())!=null){
                clientOut.write((responseLine + "\n").getBytes());
            }
            clientOut.flush();
            clientSocket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
