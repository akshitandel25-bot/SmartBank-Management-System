
public class TestBalance {
    public static void main(String[] args) {
        try {
            double balance = BankApi.getBalance(1);
            System.out.println("Database balance: ₹" + balance);
        } catch (Exception e) {
            System.out.println("Could not load balance.");
            e.printStackTrace();
        }
    }
}
