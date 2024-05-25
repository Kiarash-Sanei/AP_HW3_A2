import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TCPClient {
    private Socket socket;
    private DataInputStream receiveBuffer;
    private DataOutputStream sendBuffer;

    public boolean establishConnection(String address, int port) {
        try {
            socket = new Socket(address, port);
            receiveBuffer = new DataInputStream(socket.getInputStream());
            sendBuffer = new DataOutputStream(socket.getOutputStream());
            return true;
        } catch (Exception e) {
            System.out.println(Message.UNABLE_TO_CONNECT.getMessage());
            return false;
        }
    }

    public boolean sendMessage(String message) {
        try {
            sendBuffer.writeUTF(message);
            return true;
        } catch (IOException e) {
            System.out.println(Message.UNABLE_TO_SEND.getMessage());
            return false;
        }
    }

    public String receiveMessage() {
        try {
            return receiveBuffer.readUTF();
        } catch (IOException e) {
            System.out.println(Message.UNABLE_TO_RECEIVE.getMessage());
            return null;
        }
    }

    public boolean endConnection() {
        if (socket == null)
            return true;
        try {
            socket.close();
            receiveBuffer.close();
            sendBuffer.close();
            return true;
        } catch (IOException e) {
            System.out.println(Message.UNABLE_TO_DISCONNECT.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        TCPClient client = new TCPClient();
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        while (!Command.EXIT.matcher(line).matches()) {
            if (!client.establishConnection("127.0.0.1", 9999))
                break;
            if (!client.sendMessage(line))
                break;
            System.out.println(client.receiveMessage());
//            if (!client.endConnection())
//                break;
            System.out.println("done");
            line = scanner.nextLine();
        }
        System.out.println(Message.END_OF_CONNECTION.getMessage());
        scanner.close();
    }
}
