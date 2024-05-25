public class Customer {
    private final String name;
    private final String id;
    private int money;

    public Customer(String name, String id, int money) {
        this.name = name;
        this.id = id;
        this.money = money;
    }

    public int getMoney() {
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
