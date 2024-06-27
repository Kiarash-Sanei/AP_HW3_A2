import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;

public class StoreServer extends Thread {
    private static ArrayList<Product> inventory = new ArrayList<>();
    private static ArrayList<Customer> customers = new ArrayList<>();
    private Socket socket;
    private ServerSocket serverSocket;
    private Customer currentCustomer;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public StoreServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try {
            socket = serverSocket.accept();
            dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            while (true) {
                try {
                    String line = dataInputStream.readUTF();
                    System.out.println(line);
                    Instruction instruction = commandFinder(line);
                    if (instruction == null)
                        continue;
                    if (instruction.command() == Command.EXIT)
                        break;
                    System.out.println(instruction);
                    instructionHandler(instruction);
                } catch (IOException e) {
                    System.out.println(Message.UNABLE_TO_RECEIVE);
                }
            }
        } catch (Exception e) {
            System.out.println(Message.UNABLE_TO_CONNECT.getMessage());
        }
    }

    private void instructionHandler(Instruction instruction) throws IOException {
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
                dataOutputStream.writeUTF(Message.INVALID_COMMAND.getMessage());
        }
    }

    private Instruction commandFinder(String input) {
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

    private void register(String id, String name, String moneyString) throws IOException {
        if (!isValidId(id))
            dataOutputStream.writeUTF(Message.INVALID_ID.getMessage());
        else if (!isValidName(name))
            dataOutputStream.writeUTF(Message.INVALID_NAME.getMessage());
        else if (!isValidMoney(moneyString))
            dataOutputStream.writeUTF(Message.INVALID_MONEY.getMessage());
        else {
            customers.add(new Customer(name, id, Integer.parseInt(moneyString)));
            dataOutputStream.writeUTF(Message.REGISTER.getMessage());
        }
    }

    private void login(String id) throws IOException {
        for (Customer customer : customers)
            if (customer.getId().equals(id)) {
                currentCustomer = customer;
                dataOutputStream.writeUTF(Message.LOGIN.getMessage());
                return;
            }
        dataOutputStream.writeUTF(Message.INVALID_ID.getMessage());
    }

    private void logout() throws IOException {
        if (currentCustomer == null)
            dataOutputStream.writeUTF(Message.NO_LOGIN.getMessage());
        else {
            currentCustomer = null;
            dataOutputStream.writeUTF(Message.LOGOUT.getMessage());
        }
    }

    private void givePrice(String productName) throws IOException {
        int price = getPrice(productName);
        if (price != -1)
            dataOutputStream.writeInt(price);
    }

    private void giveQuantity(String productName) throws IOException {
        int quantity = getQuantity(productName);
        if (quantity != -1)
            dataOutputStream.writeInt(quantity);
    }

    private synchronized void giveMoney() throws IOException {
        if (currentCustomer == null)
            dataOutputStream.writeUTF(Message.NO_LOGIN.getMessage());
        else
            dataOutputStream.writeUTF(((Integer) currentCustomer.getMoney()).toString());
    }

    private void charge(String moneyString) throws IOException {
        if (!isValidMoney(moneyString))
            dataOutputStream.writeUTF(Message.INVALID_MONEY.getMessage());
        else if (currentCustomer == null)
            dataOutputStream.writeUTF(Message.NO_LOGIN.getMessage());
        else {
            currentCustomer.addMoney(Integer.parseInt(moneyString));
            dataOutputStream.writeUTF(Message.CHARGED.getMessage());
        }
    }

    private synchronized void buy(String quantityString, String productName) throws IOException {
        if (!isValidQuantity(quantityString))
            dataOutputStream.writeUTF(Message.INVALID_QUANTITY.getMessage());
        else if (!isValidProductName(productName))
            dataOutputStream.writeUTF(Message.INVALID_PRODUCT.getMessage());
        else if (Integer.parseInt(quantityString) > getQuantity(productName))
            dataOutputStream.writeUTF(Message.OUT_OF_STOCK.getMessage());
        else if (currentCustomer == null)
            dataOutputStream.writeUTF(Message.NO_LOGIN.getMessage());
        else if (currentCustomer.getMoney() < getPrice(productName) * Integer.parseInt(quantityString))
            dataOutputStream.writeUTF(Message.INSUFFICIENT_MONEY.getMessage());
        else {
            currentCustomer.addMoney(-getPrice(productName) * Integer.parseInt(quantityString));
            Objects.requireNonNull(getProduct(productName)).sell();
            dataOutputStream.writeUTF(Message.BOUGHT.getMessage());
        }
    }

    private boolean isValidId(String id) {
        for (Customer customer : customers)
            if (customer.getId().equals(id))
                return false;
        return true;
    }

    private boolean isValidName(String name) {
        for (Customer customer : customers)
            if (customer.getName().equals(name))
                return false;
        return true;
    }

    private boolean isValidMoney(String moneyString) {
        try {
            int money = Integer.parseInt(moneyString);
            return money >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidProductName(String productName) {
        for (Product product : inventory)
            if (productName.equals(product.getName()))
                return true;
        return false;
    }

    private boolean isValidQuantity(String quantityString) {
        try {
            int quantity = Integer.parseInt(quantityString);
            return quantity >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Product getProduct(String productName) {
        for (Product product : inventory)
            if (productName.equals(product.getName()))
                return product;
        return null;
    }

    private int getPrice(String productName) throws IOException {
        if (!isValidProductName(productName))
            dataOutputStream.writeUTF(Message.INVALID_PRODUCT.getMessage());
        else
            return Objects.requireNonNull(getProduct(productName)).getPrice();
        return -1;
    }

    private int getQuantity(String productName) throws IOException {
        if (!isValidProductName(productName))
            dataOutputStream.writeUTF(Message.INVALID_PRODUCT.getMessage());
        else
            return Objects.requireNonNull(getProduct(productName)).getQuantity();
        return -1;
    }

    public static void main(String[] args) {
        inventory.add(new Product("shoe1", 1, 5));
        inventory.add(new Product("shoe2", 2, 5));
        inventory.add(new Product("shoe3", 3, 5));
        try {
            StoreServer storeServer = new StoreServer(new ServerSocket(12345));
            storeServer.start();
            storeServer.join();
        } catch (Exception e) {
            System.out.println(Message.UNABLE_TO_CONNECT.getMessage());
        }
    }
}

class Customer {
    private final String name;
    private final String id;
    private int money;

    public Customer(String name, String id, int money) {
        this.name = name;
        this.id = id;
        this.money = money;
    }

    public synchronized int getMoney() {
        return money;
    }

    public void addMoney(int money) {
        this.money += money;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
