import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;

public class ClientHandler extends Thread {
    private static DataInputStream receiveBuffer;
    private static DataOutputStream sendBuffer;
    private static Customer currentCustomer;
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            receiveBuffer = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            sendBuffer = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            while (true) {
                try {
                    String line = receiveBuffer.readUTF();
                    Instruction instruction = commandFinder(line);
                    if (instruction == null ||
                            instruction.command() == Command.EXIT) {
                        break;
                    }
                    instructionHandler(instruction);
                } catch (IOException e) {
                    System.out.println(Message.UNABLE_TO_RECEIVE);
                }
            }
        } catch (Exception e) {
            System.out.println(Message.UNABLE_TO_CONNECT.getMessage());
        }
    }

    private static void instructionHandler(Instruction instruction) throws IOException {
        ArrayList<String> arguments = instruction.arguments();
        switch (instruction.command()) {
            case REGISTER:
                register(arguments.get(0), arguments.get(1), arguments.get(2));
                return;
            case LOGIN:
                login(arguments.get(0));
                return;
            case LOGOUT:
                logout();
                return;
            case GET_PRICE:
                givePrice(arguments.get(0));
                return;
            case GET_QUANTITY:
                giveQuantity(arguments.get(0));
                return;
            case GET_MONEY:
                giveMoney();
                return;
            case CHARGE:
                charge(arguments.get(0));
                return;
            case PURCHASE:
                buy(arguments.get(0), arguments.get(1));
                return;
            case EXIT:
                return;
            default:
                sendBuffer.writeUTF(Message.INVALID_COMMAND.getMessage());
        }
    }

    private static Instruction commandFinder(String input) {
        for (Command command : Command.values()) {
            Matcher matcher = command.matcher(input);
            if (matcher.matches()) {
                return switch (command) {
                    case EXIT, LOGOUT, GET_MONEY -> new Instruction(command, null);
                    case REGISTER -> new Instruction(command,
                            new ArrayList<>(Arrays.asList(
                                    matcher.group("id"),
                                    matcher.group("name"),
                                    matcher.group("money"))));
                    case LOGIN -> new Instruction(command,
                            new ArrayList<>(Collections.singletonList(
                                    matcher.group("id"))));
                    case GET_PRICE, GET_QUANTITY -> new Instruction(command,
                            new ArrayList<>(Collections.singletonList(
                                    matcher.group("name"))));
                    case CHARGE -> new Instruction(command,
                            new ArrayList<>(Collections.singletonList(
                                    matcher.group("money"))));
                    case PURCHASE -> new Instruction(command,
                            new ArrayList<>(Arrays.asList(
                                    matcher.group("quantity"),
                                    matcher.group("name"))));
                };
            }
        }
        return null;
    }

    private static void register(String id, String name, String moneyString) throws IOException {
        if (!isValidId(id))
            sendBuffer.writeUTF(Message.INVALID_ID.getMessage());
        else if (!isValidName(name))
            sendBuffer.writeUTF(Message.INVALID_NAME.getMessage());
        else if (!isValidMoney(moneyString))
            sendBuffer.writeUTF(Message.INVALID_MONEY.getMessage());
        else
            TCPServer.customers.add(new Customer(name, id, Integer.parseInt(moneyString)));
    }

    private static void login(String id) throws IOException {
        for (Customer customer : TCPServer.customers)
            if (customer.getId().equals(id)) {
                currentCustomer = customer;
                return;
            }
        sendBuffer.writeUTF(Message.INVALID_ID.getMessage());
    }

    private static void logout() throws IOException {
        if (currentCustomer == null)
            sendBuffer.writeUTF(Message.NO_LOGIN.getMessage());
        else
            currentCustomer = null;
    }

    private static void givePrice(String productName) throws IOException {
        int price = getPrice(productName);
        if (price != -1)
            sendBuffer.writeInt(price);
    }

    private static void giveQuantity(String productName) throws IOException {
        int quantity = getQuantity(productName);
        if (quantity != -1)
            sendBuffer.writeInt(quantity);
    }

    private static void giveMoney() throws IOException {
        if (currentCustomer == null)
            sendBuffer.writeUTF(Message.NO_LOGIN.getMessage());
        else
            sendBuffer.writeInt(currentCustomer.getMoney());
    }

    private static void charge(String moneyString) throws IOException {
        if (!isValidMoney(moneyString))
            sendBuffer.writeUTF(Message.INVALID_MONEY.getMessage());
        else if (currentCustomer == null)
            sendBuffer.writeUTF(Message.NO_LOGIN.getMessage());
        else
            currentCustomer.addMoney(Integer.parseInt(moneyString));
    }

    private static void buy(String quantityString, String productName) throws IOException {
        if (!isValidQuantity(quantityString))
            sendBuffer.writeUTF(Message.INVALID_QUANTITY.getMessage());
        else if (!isValidProductName(productName))
            sendBuffer.writeUTF(Message.INVALID_PRODUCT.getMessage());
        else if (Integer.parseInt(quantityString) > getQuantity(productName))
            sendBuffer.writeUTF(Message.OUT_OF_STOCK.getMessage());
        else if (currentCustomer == null)
            sendBuffer.writeUTF(Message.NO_LOGIN.getMessage());
        else if (currentCustomer.getMoney() < getPrice(productName) * Integer.parseInt(quantityString))
            sendBuffer.writeUTF(Message.INSUFFICIENT_MONEY.getMessage());
        else {
            currentCustomer.addMoney(-getPrice(productName) * Integer.parseInt(quantityString));
            Objects.requireNonNull(getProduct(productName)).sell();
        }
    }

    private static boolean isValidId(String id) {
        for (Customer customer : TCPServer.customers)
            if (customer.getId().equals(id))
                return false;
        return true;
    }

    private static boolean isValidName(String name) {
        for (Customer customer : TCPServer.customers)
            if (customer.getName().equals(name))
                return false;
        return true;
    }

    private static boolean isValidMoney(String moneyString) {
        try {
            int money = Integer.parseInt(moneyString);
            return money >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isValidProductName(String productName) {
        for (Product product : TCPServer.inventory)
            if (productName.equals(product.getName()))
                return true;
        return false;
    }

    private static boolean isValidQuantity(String quantityString) {
        try {
            int quantity = Integer.parseInt(quantityString);
            return quantity >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static Product getProduct(String productName) {
        for (Product product : TCPServer.inventory)
            if (productName.equals(product.getName()))
                return product;
        return null;
    }

    private static int getPrice(String productName) throws IOException {
        if (!isValidProductName(productName))
            sendBuffer.writeUTF(Message.INVALID_PRODUCT.getMessage());
        else
            return Objects.requireNonNull(getProduct(productName)).getPrice();
        return -1;
    }

    private static int getQuantity(String productName) throws IOException {
        if (!isValidProductName(productName))
            sendBuffer.writeUTF(Message.INVALID_PRODUCT.getMessage());
        else
            return Objects.requireNonNull(getProduct(productName)).getQuantity();
        return -1;
    }
}
