import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;

public class TCPServer {
    private static ServerSocket serverSocket;
    public static final ArrayList<Product> inventory = new ArrayList<>();
    public static final ArrayList<Customer> customers = new ArrayList<>();

    public TCPServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println(Message.UNABLE_TO_OPEN_SERVER.getMessage());
        }
    }

    public static void main(String[] args) {
        inventory.add(new Product("shoe1", 1, 5));
        inventory.add(new Product("shoe2", 2, 5));
        inventory.add(new Product("shoe3", 3, 5));
        new TCPServer(9999);
        try {
            Listener listener = new Listener(serverSocket);
            listener.start();
            listener.join();
        } catch (Exception e) {
            System.out.println(Message.UNABLE_TO_LISTEN.getMessage());
        }
    }

}

