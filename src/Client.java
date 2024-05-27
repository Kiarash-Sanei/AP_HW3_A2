import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public boolean establishConnection(String address, int port) {
        try {
            socket = new Socket(address, port);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            return true;
        } catch (Exception e) {
            System.out.println(Message.UNABLE_TO_CONNECT.getMessage());
            return false;
        }
    }

    public boolean sendMessage(String message) {
        try {
            dataOutputStream.writeUTF(message);
            return true;
        } catch (IOException e) {
            System.out.println(Message.UNABLE_TO_SEND.getMessage());
            return false;
        }
    }

    public String receiveMessage() {
        try {
            return dataInputStream.readUTF();
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
            dataInputStream.close();
            dataOutputStream.close();
            return true;
        } catch (IOException e) {
            System.out.println(Message.UNABLE_TO_DISCONNECT.getMessage());
            return false;
        }
    }

    public void start() {
        this.establishConnection("localhost", 12345);
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        while (!Command.EXIT.matcher(line).matches()) {
            if (!this.sendMessage(line))
                break;
            System.out.println(this.receiveMessage());
            line = scanner.nextLine();
        }
        this.endConnection();
        System.out.println(Message.END_OF_CONNECTION.getMessage());
        scanner.close();
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}
