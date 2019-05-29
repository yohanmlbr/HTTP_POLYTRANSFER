import java.io.IOException;
import java.net.ServerSocket;

public class Server {

    private ServerSocket ss;
    private static String folder="D:\\Desktop\\Web\\";

    public Server(int port){
        try {
            ss=new ServerSocket(port);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void start(){
        try {
            while(true) {
                System.out.println("waiting for clients...");
                Communication c=new Communication(ss.accept());
                //wait for opensucces
                new Thread(c).start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    public static String getFolder() {
        return folder;
    }

    public static void main(String args[]){
        Server s=new Server(8080);
        s.start();
    }
}
