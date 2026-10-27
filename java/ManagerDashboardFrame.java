import javax.swing.table.TableRowSorter;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManagerDashboardFrame extends JFrame {

    private static final String BASE_URL =
            "http://localhost/smartbank-api/";

    private final String token;
    private final HttpClient client = HttpClient.newHttpClient();

    private final Color navy = new Color(10, 24, 48);
    private final Color cyan = new Color(60, 220, 245);

    private final JLabel customersValue = new JLabel("—");
    private final JLabel accountsValue = new JLabel("—");
    private final JLabel balanceValue = new JLabel("—");
    private final JLabel depositsValue = new JLabel("—");
    private final JLabel withdrawalsValue = new JLabel("—");
    private final JLabel flaggedValue = new JLabel("—");

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{
                "Transaction ID", 
                "Customer", 
                "Account",
                "Type",
                "Amount (₹)", 
                "Status",
                "Fraud Reason", 
                "Date"
            }, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable transactionTable = new JTable(model);
    
    private final JTextField searchField = new JTextField(20);
    
    private final JComboBox<String> typeFilter =
        new JComboBox<>(new String[]{
            "All Types",
            "Deposit (Money In)",
            "Withdrawal (Money Out)",
            "Transfer (Money Sent)"
        });
    
    private final JComboBox<String> statusFilter =
        new JComboBox<>(new String[]{
            "All Statuses",
            "Completed (Successful)",
            "Flagged for Review",
            "Pending",
            "Failed",
            "Rejected"
        });
    
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    private final JLabel statusLabel = new JLabel("Ready");
    private final JLabel resultCount = new JLabel("0 records");

    public ManagerDashboardFrame(String token) {
        this.token = token;

        setTitle("SmartBank | Manager Dashboard");
        setSize(1200, 750);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(242, 246, 252));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(navy);
        header.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel heading = new JLabel("SMARTBANK  |  MANAGER CENTER");
        heading.setFont(new Font("SansSerif", Font.BOLD, 21));
        heading.setForeground(Color.WHITE);

        JButton refreshButton = new JButton("Refresh");
        JButton backButton = new JButton("← Back to Welcome");
        JButton logoutButton = new JButton("Logout");
        styleHeaderButton(refreshButton, true);
        styleHeaderButton(backButton, false);
        styleHeaderButton(logoutButton, false);

        refreshButton.addActionListener(e -> loadData());

        backButton.addActionListener(e -> returnToWelcome());
        logoutButton.addActionListener(e -> logout());

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        headerActions.setOpaque(false);
        headerActions.add(refreshButton);
        headerActions.add(backButton);
        headerActions.add(logoutButton);

        header.add(heading, BorderLayout.WEST);
        header.add(headerActions, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setBackground(new Color(242, 246, 252));
        body.setBorder(new EmptyBorder(22, 25, 22, 25));

        JLabel overviewHeading = new JLabel("Bank Overview");
        overviewHeading.setFont(new Font("SansSerif", Font.BOLD, 22));
        overviewHeading.setForeground(navy);

        JPanel cards = new JPanel(new GridLayout(2, 3, 14, 14));
        cards.setOpaque(false);

        cards.add(makeCard("Total Customers", customersValue));
        cards.add(makeCard("Total Accounts", accountsValue));
        cards.add(makeCard("Total Balance", balanceValue));
        cards.add(makeCard("Completed Deposits", depositsValue));
        cards.add(makeCard("Completed Withdrawals", withdrawalsValue));
        cards.add(makeCard("Flagged Transactions", flaggedValue));

        JPanel top = new JPanel(new BorderLayout(0, 15));
        top.setOpaque(false);
        top.add(overviewHeading, BorderLayout.NORTH);
        top.add(cards, BorderLayout.CENTER);

        transactionTable.setRowHeight(28);
        transactionTable.setAutoCreateRowSorter(true);
        transactionTable.setRowSorter(sorter);
        transactionTable.setFillsViewportHeight(true);

        JPanel transactionsPanel = new JPanel(new BorderLayout(0, 10));
        transactionsPanel.setOpaque(false);

        JLabel transactionsHeading = new JLabel("Recent Transactions");
        transactionsHeading.setFont(new Font("SansSerif", Font.BOLD, 19));
        transactionsHeading.setForeground(navy);

        JPanel transactionTop = new JPanel(new BorderLayout(10, 10));
        transactionTop.setOpaque(false);
        
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.setOpaque(false);
        
        searchField.setToolTipText("Search customer, account, amount, status or fraud reason");
        filters.add(new JLabel("Search:"));
        filters.add(searchField);
        filters.add(typeFilter);
        filters.add(statusFilter);
        
        transactionTop.add(transactionsHeading, BorderLayout.NORTH);
        transactionTop.add(filters, BorderLayout.CENTER);
        
        transactionsPanel.add(transactionTop, BorderLayout.NORTH);
        transactionsPanel.add(new JScrollPane(transactionTable),BorderLayout.CENTER);
        
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyFilters();
            }
            
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyFilters();
            }
            
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyFilters();
            }
        }
    );
    
    typeFilter.addActionListener(e -> applyFilters());
    statusFilter.addActionListener(e -> applyFilters());
    
    body.add(top, BorderLayout.NORTH);
    body.add(transactionsPanel, BorderLayout.CENTER);

    statusLabel.setBorder(new EmptyBorder(8, 15, 8, 15));

    root.add(header, BorderLayout.NORTH);
    root.add(body, BorderLayout.CENTER);
    root.add(statusLabel, BorderLayout.SOUTH);

    setContentPane(root);

    loadData();
}

    private JPanel makeCard(String title, JLabel value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 239)),
                new EmptyBorder(15, 16, 15, 16)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(90, 105, 125));
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        value.setForeground(navy);
        value.setFont(new Font("SansSerif", Font.BOLD, 23));

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(value);

        return card;
    }

    private String request(String endpoint) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                req,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException(
                    "HTTP " + response.statusCode() + ": " + response.body()
            );
        }

        return response.body();
    }

    private void loadData() {
        statusLabel.setText("Loading bank data...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String overviewJson = request("manager_overview.php");
                String transactionsJson = request("manager_transactions.php");

                if (!"true".equals(jsonValue(overviewJson, "success"))) {
                    throw new RuntimeException(overviewJson);
                }

                if (!"true".equals(jsonValue(transactionsJson, "success"))) {
                    throw new RuntimeException(transactionsJson);
                }

                SwingUtilities.invokeLater(() -> {
                    customersValue.setText(jsonValue(overviewJson, "total_customers"));
                    accountsValue.setText(jsonValue(overviewJson, "total_accounts"));
                    balanceValue.setText("₹" + jsonValue(overviewJson, "total_balance"));
                    depositsValue.setText("₹" + jsonValue(overviewJson, "total_deposits"));
                    withdrawalsValue.setText("₹" + jsonValue(overviewJson, "total_withdrawals"));
                    flaggedValue.setText(jsonValue(overviewJson, "flagged_transactions"));

                    model.setRowCount(0);

                    Matcher matcher = Pattern.compile("\\{([^{}]*)\\}")
                            .matcher(transactionsJson);

                    while (matcher.find()) {
                        String obj = matcher.group(1);

                        if (!obj.contains("\"transaction_id\"")) {
                            continue;
                        }

                        model.addRow(new Object[]{
                            jsonValue(obj, "transaction_id"),
                            jsonValue(obj, "full_name"),
                            jsonValue(obj, "account_number"),
                            jsonValue(obj, "transaction_type"),
                            jsonValue(obj, "amount"),
                            jsonValue(obj, "status"),
                            jsonValue(obj, "fraud_reason"),
                            jsonValue(obj, "transaction_date")
                        });
                    }

                    applyFilters();
                    statusLabel.setText(
                            "Data refreshed successfully. "
                            + model.getRowCount() + " transactions loaded."
                    );
                });

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    statusLabel.setText("Could not load data.");
                    JOptionPane.showMessageDialog(
                            ManagerDashboardFrame.this,
                            "Unable to load manager data.\n"
                                    + ex.getMessage(),
                            "Dashboard Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private String jsonValue(String json, String key) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key)
                        + "\"\\s*:\\s*(null|true|false|\"(?:\\\\.|[^\"\\\\])*\"|[^,}\\s]+)"
        );

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            return "";
        }

        String value = matcher.group(1).trim();

        if ("null".equals(value)) {
            return "";
        }

        if (value.length() >= 2
                && value.startsWith("\"")
                && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
            value = value.replace("\\\"", "\"")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t")
                    .replace("\\\\", "\\");
        }

        return value;
    }

    private void styleHeaderButton(JButton button, boolean primary) {
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBackground(primary ? new Color(11, 92, 171) : Color.WHITE);
        button.setForeground(primary ? Color.WHITE : navy);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? new Color(11, 92, 171) : new Color(220, 229, 238), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
    }

    private void returnToWelcome() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Return to the SmartBank welcome page?",
                "Back to Welcome",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new WelcomeFrame().setVisible(true);
        }
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "manager_logout.php"))
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            client.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ignored) {
        }

        dispose();
        new WelcomeFrame().setVisible(true);
    }

    
private void applyFilters() {
    final String search = searchField.getText().trim().toLowerCase(java.util.Locale.ROOT);
    final String selectedType = (String) typeFilter.getSelectedItem();
    final String selectedStatus = (String) statusFilter.getSelectedItem();

    sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
        @Override
        public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
            boolean searchMatches = search.isEmpty();
            if (!searchMatches) {
                for (int column = 0; column < entry.getValueCount(); column++) {
                    String value = String.valueOf(entry.getStringValue(column))
                            .toLowerCase(java.util.Locale.ROOT);
                    if (value.contains(search)) {
                        searchMatches = true;
                        break;
                    }
                }
            }
            if (!searchMatches) return false;

            String rawType = entry.getStringValue(3).trim().toLowerCase(java.util.Locale.ROOT);
            if (selectedType != null && !"All Types".equals(selectedType)) {
                String wantedType = selectedType.startsWith("Deposit") ? "deposit"
                        : selectedType.startsWith("Withdrawal") ? "withdrawal"
                        : selectedType.startsWith("Transfer") ? "transfer" : "";
                if (!rawType.contains(wantedType) || wantedType.isEmpty()) return false;
            }

            String rawStatus = entry.getStringValue(5).trim().toLowerCase(java.util.Locale.ROOT);
            if (selectedStatus != null && !"All Statuses".equals(selectedStatus)) {
                boolean statusMatches;
                switch (selectedStatus) {
                    case "Completed (Successful)":
                        statusMatches = rawStatus.equals("completed")
                                || rawStatus.equals("success")
                                || rawStatus.equals("successful")
                                || rawStatus.equals("completed (successful)");
                        break;
                    case "Flagged for Review":
                        statusMatches = rawStatus.contains("flagged")
                                || rawStatus.contains("review");
                        break;
                    case "Pending":
                        statusMatches = rawStatus.contains("pending");
                        break;
                    case "Failed":
                        statusMatches = rawStatus.contains("failed")
                                || rawStatus.contains("failure");
                        break;
                    case "Rejected":
                        statusMatches = rawStatus.contains("rejected")
                                || rawStatus.contains("blocked");
                        break;
                    default:
                        statusMatches = rawStatus.equalsIgnoreCase(selectedStatus);
                }
                if (!statusMatches) return false;
            }
            return true;
        }
    });

    int visible = transactionTable.getRowCount();
    resultCount.setText(visible + (visible == 1 ? " record" : " records"));
    if (visible == 0 && model.getRowCount() > 0) {
        statusLabel.setText("No transactions match these filters. Try clearing Search or selecting All Types / All Statuses.");
    } else {
        statusLabel.setText(visible + " of " + model.getRowCount() + " transactions shown.");
    }
}

}
