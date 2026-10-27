
public class TestBankApi {
    public static void main(String[] args) {
        try {
            String result = BankApi.saveTransaction(
                    1,
                    "Java API Test",
                    "Deposit",
                    200.00,
                    "Completed",
                    ""
            );

            System.out.println("API Response:");
            System.out.println(result);

        } catch (Exception e) {
            System.out.println("API test failed:");
            e.printStackTrace();
        }
    }
}
