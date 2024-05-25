import java.net.ServerSocket;
import java.net.Socket;

public class Listener extends Thread {
    private final ServerSocket serverSocket;

    public Listener(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandler.start();
                clientHandler.join();
            } catch (Exception e) {
                System.out.println(Message.UNABLE_TO_HANDLE.getMessage());
            }
        }
    }
}
