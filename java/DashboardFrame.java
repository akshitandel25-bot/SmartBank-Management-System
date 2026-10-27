
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public class DashboardFrame extends JFrame {

    private final Color navy = new Color(39, 39, 55);
    private final Color blue = new Color(119, 93, 215);
    private final Color background = new Color(244, 242, 251);
    private final Color muted = new Color(111, 112, 133);
    private final Color glassBorder = new Color(255, 255, 255, 190);
    private final Color ink = new Color(39, 39, 55);

    private double balance = 0.00;
    private double income = 0.00;
    private double expenses = 0.00;
    private int suspiciousCount = 0;
    private final Map<String, Integer> recipientTransferCounts = new HashMap<>();

    private JLabel balanceValue;
    private JLabel incomeValue;
    private JLabel expensesValue;
    private JLabel securityValue;
    private JLabel statusMessage;

    private DefaultTableModel tableModel;
    private DefaultTableModel securityTableModel;
    private JTable securityTable;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private final Map<String, JButton> navButtons = new HashMap<>();
    private String activePage = "Overview";
    private String lastShownAlertSignature = "";
    private JLabel insightsPatternCount;
    private JPanel recurringPaymentsPanel;
    private JLabel insightsExpensesValue;
    private JLabel insightsIncomeValue;

    public DashboardFrame() {
        setTitle("SmartBank | Banking Dashboard");
        setSize(1320, 820);
        setMinimumSize(new Dimension(1020, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new GradientPanel();
        contentPanel.setLayout(cardLayout);
        contentPanel.setOpaque(false);

        contentPanel.add(createOverview(), "Overview");
        contentPanel.add(createTransactionsPage(), "Transactions");
        contentPanel.add(createSecurityPage(), "Security");
        contentPanel.add(createInsightsPage(), "Insights");
        contentPanel.add(new MonthlyExpensesPanel(1), "MonthlyExpenses");

        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "Overview");
        loadDatabaseBalance();
        loadDatabaseStats();
        loadDatabaseTransactions();
        loadDatabaseSecurityEvents();
    }

    private void loadDatabaseBalance() {
        try {
            balance = BankApi.getBalance(1);
            refreshOverview();
        } catch (Exception ex) {
            showMessage("Could not load the database balance.\n"
                    + "Check that XAMPP Apache and MySQL are running.\n\n"
                    + ex.getMessage());
        }
    }

private void loadDatabaseStats() {
    try {
        double[] stats = BankApi.getStats(1);

        income = stats[0];
        expenses = stats[1];

        refreshOverview();
    } catch (Exception ex) {
        showMessage(
                "Could not load dashboard statistics.\n"
                + "Check that XAMPP Apache and MySQL are running.\n\n"
                + ex.getMessage());
    }
}

    private boolean saveTransactionAndRefreshBalance(
            String recipient, String type, double amount,
            String status, String fraudReason) {
        try {
            String response = BankApi.saveTransaction(
                    1, recipient, type, amount, status, fraudReason);

            if (response == null || !response.contains("\"success\":true")) {
                showMessage("The database did not confirm the transaction:\n"
                        + response);
                return false;
            }

            balance = BankApi.getBalance(1);
            loadDatabaseStats();
            loadDatabaseTransactions();
            return true;
        } catch (Exception ex) {
            showMessage("Could not save the transaction.\n"
                    + "Check that XAMPP Apache and MySQL are running.\n\n"
                    + ex.getMessage());
            return false;
        }
    }

    private JPanel createSidebar() {
        JPanel side = new GradientPanel(new Color(7, 15, 29), new Color(11, 25, 44));
        side.setPreferredSize(new Dimension(242, 0));
        side.setBorder(new EmptyBorder(30, 18, 22, 18));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));

        JPanel brand = new JPanel(new BorderLayout(10, 0));
        brand.setOpaque(false);
        JLabel mark = new JLabel("S");
        mark.setHorizontalAlignment(SwingConstants.CENTER);
        mark.setForeground(Color.WHITE);
        mark.setFont(new Font("SansSerif", Font.BOLD, 24));
        mark.setOpaque(true);
        mark.setBackground(new Color(45, 91, 238));
        mark.setPreferredSize(new Dimension(46, 46));
        mark.setBorder(new EmptyBorder(4, 4, 4, 4));
        JLabel logo = new JLabel("SMARTBANK");
        logo.setForeground(new Color(244, 248, 255));
        logo.setFont(new Font("SansSerif", Font.BOLD, 19));
        JLabel subtitle = new JLabel("DIGITAL BANKING");
        subtitle.setForeground(new Color(126, 157, 190));
        subtitle.setFont(new Font("SansSerif", Font.BOLD, 9));
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.add(logo); brandText.add(Box.createVerticalStrut(4)); brandText.add(subtitle);
        brand.add(mark, BorderLayout.WEST); brand.add(brandText, BorderLayout.CENTER);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(brand);
        side.add(Box.createVerticalStrut(42));

        JLabel navLabel = new JLabel("WORKSPACE");
        navLabel.setForeground(new Color(100, 132, 164));
        navLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        navLabel.setBorder(new EmptyBorder(0, 12, 12, 0));
        navLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(navLabel);
        addNavButton(side, "◈   Overview", "Overview");
        addNavButton(side, "▤   Transactions", "Transactions");
        addNavButton(side, "⬡   Security Center", "Security");
        addNavButton(side, "◷   Smart Insights", "Insights");
        addNavButton(side, "◉   Monthly Expenses", "MonthlyExpenses");

        side.add(Box.createVerticalGlue());
        GlassPanel demo = new GlassPanel(new Color(18, 36, 59, 235), new Color(62, 104, 145, 180), 18);
        demo.setLayout(new BorderLayout(0, 8));
        demo.setBorder(new EmptyBorder(15, 14, 15, 14));
        JLabel demoTitle = new JLabel("DEMO ENVIRONMENT");
        demoTitle.setForeground(new Color(101, 222, 242));
        demoTitle.setFont(new Font("SansSerif", Font.BOLD, 10));
        JLabel demoText = new JLabel("<html><div style='width:155px'>Sample accounts only.<br>No real money is moved.</div></html>");
        demoText.setForeground(new Color(190, 208, 229));
        demoText.setFont(new Font("SansSerif", Font.PLAIN, 11));
        demo.add(demoTitle, BorderLayout.NORTH); demo.add(demoText, BorderLayout.CENTER);
        demo.setAlignmentX(Component.LEFT_ALIGNMENT);
        demo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        side.add(demo); side.add(Box.createVerticalStrut(15));

        JButton logout = new JButton("←   Back to Welcome");
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        logout.setFont(new Font("SansSerif", Font.BOLD, 12));
        logout.setForeground(new Color(213, 229, 246));
        logout.setContentAreaFilled(false);
        logout.setFocusPainted(false);
        logout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(56, 83, 117)),
                new EmptyBorder(10, 12, 10, 12)));
        logout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Return to the SmartBank welcome page?",
                    "Back to Welcome",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                new WelcomeFrame().setVisible(true);
            }
        });
        side.add(logout);
        return side;
    }

    private void addNavButton(JPanel side, String text, String page) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = page.equals(activePage);
                boolean hover = getModel().isRollover();
                if (active || hover) {
                    Color top = active ? new Color(22, 63, 91, 235) : new Color(255, 255, 255, 0);
                    Color bottom = active ? new Color(19, 47, 74, 235) : new Color(42, 78, 108, 100);
                    g2.setPaint(new GradientPaint(0, 0, top, getWidth(), getHeight(), bottom));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    if (active) {
                        g2.setColor(new Color(67, 224, 247));
                        g2.fillRoundRect(0, 10, 4, getHeight() - 20, 4, 4);
                    }
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(new Color(205, 221, 240));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(14, 14, 14, 10));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        button.setPreferredSize(new Dimension(220, 48));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        navButtons.put(page, button);
        button.addActionListener(e -> {
            activePage = page;
            for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
                entry.getValue().repaint();
            }
            cardLayout.show(contentPanel, page);
            if (page.equals("Overview") || page.equals("Security")) refreshOverview();
            if (page.equals("Transactions")) loadDatabaseTransactions();
            if (page.equals("Security")) { loadDatabaseSecurityEvents(); showSecurityAlertPopupIfNeeded(); }
            if (page.equals("Insights")) loadInsights();
            if (page.equals("MonthlyExpenses")) { /* panel refreshes when opened */ }
        });
        side.add(button);
        side.add(Box.createVerticalStrut(8));
    }

    private JPanel createOverview() {
        JPanel page = pagePanel();

        JPanel welcome = new JPanel(new BorderLayout());
        welcome.setOpaque(false);
        JPanel welcomeText = new JPanel();
        welcomeText.setOpaque(false);
        welcomeText.setLayout(new BoxLayout(welcomeText, BoxLayout.Y_AXIS));
        JLabel greeting = heading("Good day, Admin  ✦");
        welcomeText.add(greeting);
        welcomeText.add(Box.createVerticalStrut(5));
        welcomeText.add(label("Here's your financial snapshot for today.", muted, 14));
        welcome.add(welcomeText, BorderLayout.WEST);

        JLabel liveBadge = new JLabel("●  DEMO ACCOUNT");
        liveBadge.setFont(new Font("SansSerif", Font.BOLD, 10));
        liveBadge.setForeground(new Color(25, 139, 113));
        liveBadge.setOpaque(true);
        liveBadge.setBackground(new Color(225, 249, 241));
        liveBadge.setBorder(new EmptyBorder(9, 12, 9, 12));
        welcome.add(liveBadge, BorderLayout.EAST);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(welcome);
        page.add(Box.createVerticalStrut(22));

        JPanel stats = new JPanel(new GridLayout(1, 3, 16, 0));
        stats.setOpaque(false);
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));

        balanceValue = new JLabel();
        incomeValue = new JLabel();
        expensesValue = new JLabel();

        stats.add(statCard("◉  TOTAL BALANCE", balanceValue, "Available funds", new Color(132, 105, 220)));
        stats.add(statCard("↗  TOTAL INCOME", incomeValue, "Demo income", new Color(47, 169, 143)));
        stats.add(statCard("↘  TOTAL EXPENSES", expensesValue, "Demo spending", new Color(230, 133, 119)));
        page.add(stats);
        page.add(Box.createVerticalStrut(20));

        // Two-column dashboard fills the previously empty left side.
        JPanel lower = new JPanel(new GridLayout(1, 2, 18, 0));
        lower.setOpaque(false);
        lower.setAlignmentX(Component.LEFT_ALIGNMENT);
        lower.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel activity = whitePanel();
        activity.setLayout(new BorderLayout(10, 12));
        JPanel activityHeading = new JPanel(new BorderLayout());
        activityHeading.setOpaque(false);
        activityHeading.add(heading("Recent transactions"), BorderLayout.WEST);
        JButton viewAll = new JButton("View all  →");
        viewAll.setFont(new Font("SansSerif", Font.BOLD, 11));
        viewAll.setForeground(blue);
        viewAll.setContentAreaFilled(false);
        viewAll.setFocusPainted(false);
        viewAll.setBorder(new EmptyBorder(5, 8, 5, 4));
        viewAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAll.addActionListener(e -> {
            activePage = "Transactions";
            for (Map.Entry<String, JButton> entry : navButtons.entrySet()) entry.getValue().repaint();
            cardLayout.show(contentPanel, "Transactions");
            loadDatabaseTransactions();
        });
        activityHeading.add(viewAll, BorderLayout.EAST);
        activity.add(activityHeading, BorderLayout.NORTH);

        JTable table = createTransactionTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        TableRowSorter<DefaultTableModel> recentSorter = new TableRowSorter<>(tableModel);
        recentSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                return entry.getIdentifier() < 4;
            }
        });
        table.setRowSorter(recentSorter);
        JScrollPane transactionScroll = styleScrollPane(new JScrollPane(table), false);
        transactionScroll.setBorder(BorderFactory.createEmptyBorder());
        transactionScroll.getViewport().setBackground(new Color(255, 255, 255, 235));
        transactionScroll.setPreferredSize(new Dimension(500, 320));
        activity.add(transactionScroll, BorderLayout.CENTER);
        lower.add(activity);

        JPanel rightColumn = new JPanel();
        rightColumn.setOpaque(false);
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.setAlignmentY(Component.TOP_ALIGNMENT);

        JPanel actions = whitePanel();
        actions.setLayout(new BorderLayout(10, 14));
        JPanel actionHeading = new JPanel(new BorderLayout());
        actionHeading.setOpaque(false);
        actionHeading.add(heading("Quick actions"), BorderLayout.WEST);
        JLabel actionIcon = new JLabel("✦");
        actionIcon.setFont(new Font("SansSerif", Font.BOLD, 20));
        actionIcon.setForeground(blue);
        actionHeading.add(actionIcon, BorderLayout.EAST);
        actions.add(actionHeading, BorderLayout.NORTH);

        JPanel actionButtons = new JPanel(new GridLayout(2, 2, 10, 9));
        actionButtons.setOpaque(false);
        JButton transfer = actionButton("↗  Transfer money");
        JButton deposit = actionButton("＋  Deposit");
        JButton withdraw = actionButton("−  Withdraw");
        transfer.addActionListener(e -> transferMoney());
        deposit.addActionListener(e -> depositMoney());
        withdraw.addActionListener(e -> withdrawMoney());
        actionButtons.add(transfer);
        actionButtons.add(transfer == null ? new JLabel() : new JLabel(""));
        actionButtons.add(deposit);
        actionButtons.add(withdraw);
        // Transfer spans the full first row; deposit and withdrawal share the second row.
        actionButtons.setLayout(new GridBagLayout());
        actionButtons.removeAll();
        GridBagConstraints actionGbc = new GridBagConstraints();
        actionGbc.fill = GridBagConstraints.BOTH;
        actionGbc.insets = new Insets(0, 0, 9, 0);
        actionGbc.gridx = 0; actionGbc.gridy = 0; actionGbc.gridwidth = 2;
        actionGbc.weightx = 1; actionGbc.weighty = 1;
        actionButtons.add(transfer, actionGbc);
        actionGbc.gridy = 1; actionGbc.gridwidth = 1; actionGbc.insets = new Insets(0, 0, 0, 5);
        actionButtons.add(deposit, actionGbc);
        actionGbc.gridx = 1; actionGbc.insets = new Insets(0, 5, 0, 0);
        actionButtons.add(withdraw, actionGbc);
        actions.add(actionButtons, BorderLayout.CENTER);
        actions.setPreferredSize(new Dimension(380, 220));
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        rightColumn.add(actions);
        rightColumn.add(Box.createVerticalStrut(16));

        GlassPanel insight = new GlassPanel(new Color(31, 49, 97, 238), new Color(255,255,255,70), 22);
        insight.setLayout(new BorderLayout(8, 10));
        insight.setBorder(new EmptyBorder(20, 20, 20, 20));
        insight.setPreferredSize(new Dimension(380, 245));
        JLabel insightTitle = new JLabel("✧  SMART INSIGHTS");
        insightTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        insightTitle.setForeground(new Color(191, 207, 255));
        JLabel insightMain = new JLabel("<html><div style='width:280px'>Your banking activity,<br>made easier to understand.</div></html>");
        insightMain.setFont(new Font("SansSerif", Font.BOLD, 19));
        insightMain.setForeground(Color.WHITE);
        JLabel insightSub = new JLabel("<html><div style='width:280px'>Review spending patterns and TrustGuard alerts from one place.</div></html>");
        insightSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        insightSub.setForeground(new Color(211, 221, 249));
        JButton insightButton = new JButton("Explore insights  →");
        insightButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        insightButton.setForeground(Color.WHITE);
        insightButton.setContentAreaFilled(false);
        insightButton.setFocusPainted(false);
        insightButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255,255,255,100)),
                new EmptyBorder(9, 12, 9, 12)));
        insightButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        insightButton.setHorizontalAlignment(SwingConstants.LEFT);
        insightButton.addActionListener(e -> {
            activePage = "Insights";
            for (Map.Entry<String, JButton> entry : navButtons.entrySet()) entry.getValue().repaint();
            cardLayout.show(contentPanel, "Insights");
            loadInsights();
        });
        JPanel insightText = new JPanel();
        insightText.setOpaque(false);
        insightText.setLayout(new BoxLayout(insightText, BoxLayout.Y_AXIS));
        insightTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        insightMain.setAlignmentX(Component.CENTER_ALIGNMENT);
        insightSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        insightButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        insightMain.setHorizontalAlignment(SwingConstants.CENTER);
        insightSub.setHorizontalAlignment(SwingConstants.CENTER);
        insightText.add(insightTitle);
        insightText.add(Box.createVerticalStrut(14));
        insightText.add(insightMain);
        insightText.add(Box.createVerticalStrut(9));
        insightText.add(insightSub);
        insightText.add(Box.createVerticalStrut(18));
        insightText.add(insightButton);
        insight.add(insightText, BorderLayout.CENTER);
        insight.setAlignmentX(Component.LEFT_ALIGNMENT);
        insight.setMaximumSize(new Dimension(Integer.MAX_VALUE, 245));
        rightColumn.add(insight);
        lower.add(rightColumn);

        page.add(lower);
        refreshOverview();
        return page;
    }

    private JPanel createTransactionsPage() {
        JPanel page = pagePanel();
        page.add(heading("Transactions"));
        page.add(label("Search all saved transactions from MySQL.",
                muted, 14));
        page.add(Box.createVerticalStrut(20));

        JPanel card = whitePanel();
        card.setLayout(new BorderLayout(10, 12));

        // Search bar with an icon and helpful placeholder text.
        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setBackground(new Color(250, 251, 255));
        searchBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 205, 250), 1),
                new EmptyBorder(0, 14, 0, 14)));
        searchBar.setPreferredSize(new Dimension(100, 54));
        searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        JLabel searchIcon = new JLabel("⌕");
        searchIcon.setFont(new Font("SansSerif", Font.PLAIN, 29));
        searchIcon.setForeground(new Color(88, 111, 210));
        searchIcon.setBorder(new EmptyBorder(0, 0, 3, 0));
        searchBar.add(searchIcon, BorderLayout.WEST);

        JTextField search = new PlaceholderTextField(
                "Search transactions by recipient, type, status or ID...");
        search.setFont(new Font("SansSerif", Font.PLAIN, 14));
        search.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        search.setBackground(new Color(250, 251, 255));
        search.setForeground(ink);
        search.setToolTipText("Search by recipient, type, status or transaction ID");
        searchBar.add(search, BorderLayout.CENTER);
        card.add(searchBar, BorderLayout.NORTH);

        JTable table = createTransactionTable();
        TableRowSorter<DefaultTableModel> sorter =
                new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        JScrollPane tableScroll = styleScrollPane(new JScrollPane(table), false);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(new Color(255,255,255,220));
        card.add(tableScroll, BorderLayout.CENTER);

        search.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            private void filter() {
                String query = search.getText().trim();
                if (query.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(
                            RowFilter.regexFilter(
                                    "(?i)" + java.util.regex.Pattern.quote(query)));
                }
            }

            public void insertUpdate(
                    javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(
                    javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(
                    javax.swing.event.DocumentEvent e) { filter(); }
        });

        page.add(card);
        return page;
    }

    private JPanel createSecurityPage() {
        JPanel page = new CyberPage();
        page.setBorder(new EmptyBorder(26, 30, 26, 30));
        page.setLayout(new BorderLayout(0, 18));

        JPanel hero = new JPanel(new BorderLayout(18, 8));
        hero.setOpaque(false);
        hero.setBorder(new EmptyBorder(8, 8, 8, 8));
        JPanel heroText = new JPanel();
        heroText.setOpaque(false);
        heroText.setLayout(new BoxLayout(heroText, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("●  LIVE THREAT MONITORING");
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 11));
        eyebrow.setForeground(new Color(67, 226, 247));
        JLabel title = new JLabel("Security Center");
        title.setFont(new Font("SansSerif", Font.BOLD, 29));
        title.setForeground(new Color(241, 248, 255));
        JLabel subtitle = new JLabel("TrustGuard events, risk signals and recommended actions.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(165, 190, 216));
        heroText.add(eyebrow);
        heroText.add(Box.createVerticalStrut(8));
        heroText.add(title);
        heroText.add(Box.createVerticalStrut(5));
        heroText.add(subtitle);
        hero.add(heroText, BorderLayout.CENTER);

        JButton refresh = new JButton("↻  Refresh alerts");
        refresh.setFont(new Font("SansSerif", Font.BOLD, 12));
        refresh.setForeground(new Color(92, 231, 248));
        refresh.setBackground(new Color(16, 52, 76));
        refresh.setOpaque(true);
        refresh.setFocusPainted(false);
        refresh.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 126, 155), 1),
                new EmptyBorder(12, 16, 12, 16)));
        refresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refresh.addActionListener(e -> { loadDatabaseSecurityEvents(); showSecurityAlertPopupIfNeeded(); });
        JPanel refreshWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        refreshWrap.setOpaque(false);
        refreshWrap.add(refresh);
        hero.add(refreshWrap, BorderLayout.EAST);
        page.add(hero, BorderLayout.NORTH);

        securityTableModel = new DefaultTableModel(
                new String[]{"Event ID", "Date", "Event", "Risk", "Description", "Recommended action", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        securityTable = new JTable(securityTableModel);
        securityTable.setRowHeight(42);
        securityTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        securityTable.setForeground(new Color(218, 234, 249));
        securityTable.setBackground(new Color(12, 29, 50));
        securityTable.setGridColor(new Color(35, 68, 96));
        securityTable.setSelectionBackground(new Color(24, 69, 94));
        securityTable.setSelectionForeground(new Color(235, 248, 255));
        securityTable.setFillsViewportHeight(true);
        securityTable.setShowVerticalLines(false);
        securityTable.setIntercellSpacing(new Dimension(0, 1));
        securityTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean selected, boolean focus, int row, int column) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
                cell.setBorder(new EmptyBorder(0, 10, 0, 10));
                cell.setFont(new Font("SansSerif", Font.PLAIN, 12));
                if (selected) {
                    cell.setBackground(new Color(24, 69, 94));
                    cell.setForeground(new Color(235, 248, 255));
                } else {
                    cell.setBackground(row % 2 == 0 ? new Color(13, 32, 54) : new Color(16, 40, 66));
                    String text = value == null ? "" : value.toString().toLowerCase(Locale.ROOT);
                    if (column == 3 && (text.contains("high") || text.contains("critical"))) {
                        cell.setForeground(new Color(255, 111, 128));
                        cell.setFont(new Font("SansSerif", Font.BOLD, 12));
                    } else if (column == 3 && text.contains("medium")) {
                        cell.setForeground(new Color(255, 196, 87));
                        cell.setFont(new Font("SansSerif", Font.BOLD, 12));
                    } else if (column == 6 && (text.contains("review") || text.contains("pending"))) {
                        cell.setForeground(new Color(255, 196, 87));
                        cell.setFont(new Font("SansSerif", Font.BOLD, 12));
                    } else {
                        cell.setForeground(column == 0 ? new Color(86, 225, 247) : new Color(209, 228, 245));
                    }
                }
                return cell;
            }
        });
        JTableHeader header = securityTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 11));
        header.setForeground(new Color(105, 224, 245));
        header.setBackground(new Color(20, 48, 75));
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(39, 78, 108)));

        JScrollPane scroll = styleScrollPane(new JScrollPane(securityTable), false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(12, 29, 50));
        scroll.getVerticalScrollBar().setBackground(new Color(10, 28, 48));
        GlassPanel tableCard = new GlassPanel(new Color(11, 29, 49, 238), new Color(36, 91, 119), 20);
        tableCard.setLayout(new BorderLayout(0, 12));
        tableCard.setBorder(new EmptyBorder(18, 18, 18, 18));
        JPanel tableHeading = new JPanel(new BorderLayout());
        tableHeading.setOpaque(false);
        JLabel records = new JLabel("◈  SECURITY EVENT LOG");
        records.setFont(new Font("SansSerif", Font.BOLD, 11));
        records.setForeground(new Color(80, 227, 247));
        JLabel note = new JLabel("Saved events • MySQL");
        note.setFont(new Font("SansSerif", Font.PLAIN, 11));
        note.setForeground(new Color(143, 174, 203));
        tableHeading.add(records, BorderLayout.WEST);
        tableHeading.add(note, BorderLayout.EAST);
        tableCard.add(tableHeading, BorderLayout.NORTH);
        tableCard.add(scroll, BorderLayout.CENTER);

        JPanel reviewBar = new JPanel(new BorderLayout(12, 0));
        reviewBar.setOpaque(false);
        JLabel reviewHint = new JLabel("Select an alert, then tell us whether you recognize it.");
        reviewHint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        reviewHint.setForeground(new Color(161, 188, 215));
        reviewBar.add(reviewHint, BorderLayout.CENTER);
        JPanel reviewButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        reviewButtons.setOpaque(false);
        JButton confirmActivity = new JButton("✓  This was me");
        styleDialogButton(confirmActivity, new Color(24, 139, 101), Color.WHITE);
        JButton reportActivity = new JButton("No, report it");
        styleDialogButton(reportActivity, new Color(194, 58, 72), Color.WHITE);
        confirmActivity.addActionListener(e -> reviewSelectedSecurityEvent(true));
        reportActivity.addActionListener(e -> reviewSelectedSecurityEvent(false));
        reviewButtons.add(confirmActivity);
        reviewButtons.add(reportActivity);
        reviewBar.add(reviewButtons, BorderLayout.EAST);
        tableCard.add(reviewBar, BorderLayout.SOUTH);
        page.add(tableCard, BorderLayout.CENTER);
        loadDatabaseSecurityEvents();
        return page;
    }

    private JPanel createInsightsPage() {
        JPanel page = pagePanel();

        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setOpaque(false);
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(heading("Smart Insights"));
        titleBlock.add(Box.createVerticalStrut(5));
        titleBlock.add(label("A clearer view of your income, spending and activity patterns.", muted, 14));
        header.add(titleBlock, BorderLayout.WEST);

        JButton refresh = actionButton("↻  Refresh insights");
        Dimension insightButtonSize = new Dimension(175, 44);
        refresh.setPreferredSize(insightButtonSize);
        refresh.setMinimumSize(insightButtonSize);
        refresh.setMaximumSize(insightButtonSize);
        refresh.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel refreshWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        refreshWrap.setOpaque(false);
        refreshWrap.add(refresh);
        refreshWrap.setPreferredSize(new Dimension(190, 48));
        refreshWrap.setMinimumSize(new Dimension(190, 48));
        refreshWrap.setMaximumSize(new Dimension(190, 48));
        refresh.addActionListener(e -> loadInsights());
        header.add(refreshWrap, BorderLayout.EAST);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(header);
        page.add(Box.createVerticalStrut(22));

        JPanel summary = new JPanel(new GridLayout(1, 3, 16, 0));
        summary.setOpaque(false);
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);
        summary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));

        insightsIncomeValue = new JLabel("₹0.00");
        insightsExpensesValue = new JLabel("₹0.00");
        insightsPatternCount = new JLabel("0 patterns");
        summary.add(insightMetricCard("↗  TOTAL INCOME", insightsIncomeValue,
                "Money recorded as deposits", new Color(31, 166, 120), new Color(231, 250, 242)));
        summary.add(insightMetricCard("↘  TOTAL EXPENSES", insightsExpensesValue,
                "Withdrawals and transfers", new Color(219, 112, 83), new Color(255, 241, 235)));
        summary.add(insightMetricCard("✧  REPEATED PATTERNS", insightsPatternCount,
                "Candidates to review", new Color(91, 105, 235), new Color(238, 240, 255)));
        page.add(summary);
        page.add(Box.createVerticalStrut(20));

        JPanel lower = new JPanel(new GridLayout(1, 2, 18, 0));
        lower.setOpaque(false);
        lower.setAlignmentX(Component.LEFT_ALIGNMENT);
        lower.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel recurringCard = whitePanel();
        recurringCard.setLayout(new BorderLayout(8, 14));
        JPanel recurringHeader = new JPanel(new BorderLayout());
        recurringHeader.setOpaque(false);
        recurringHeader.add(heading("Possible recurring payments"), BorderLayout.WEST);
        JLabel recurringIcon = new JLabel("◷");
        recurringIcon.setFont(new Font("SansSerif", Font.BOLD, 22));
        recurringIcon.setForeground(blue);
        recurringHeader.add(recurringIcon, BorderLayout.EAST);
        recurringCard.add(recurringHeader, BorderLayout.NORTH);

        recurringPaymentsPanel = new JPanel();
        recurringPaymentsPanel.setOpaque(false);
        recurringPaymentsPanel.setLayout(new BoxLayout(recurringPaymentsPanel, BoxLayout.Y_AXIS));
        JLabel placeholder = label("Refresh insights to analyze saved transactions.", muted, 13);
        placeholder.setBorder(new EmptyBorder(8, 2, 8, 2));
        recurringPaymentsPanel.add(placeholder);
        recurringCard.add(recurringPaymentsPanel, BorderLayout.CENTER);
        lower.add(recurringCard);

        JPanel guidance = new GlassPanel(new Color(34, 51, 101, 245), new Color(255,255,255,90), 22);
        guidance.setLayout(new BorderLayout(8, 12));
        guidance.setBorder(new EmptyBorder(22, 22, 22, 22));
        JLabel guidanceKicker = new JLabel("✦  INSIGHT NOTE");
        guidanceKicker.setFont(new Font("SansSerif", Font.BOLD, 11));
        guidanceKicker.setForeground(new Color(172, 194, 255));
        JLabel guidanceTitle = new JLabel("Patterns are signals, not conclusions.");
        guidanceTitle.setFont(new Font("SansSerif", Font.BOLD, 19));
        guidanceTitle.setForeground(Color.WHITE);
        JLabel guidanceBody = new JLabel("<html><div style='width:260px;color:#D8E2FF'>Repeated recipients or withdrawals can be useful to review, but they do not prove a subscription or fraud.</div></html>");
        guidanceBody.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JPanel guidanceText = new JPanel();
        guidanceText.setOpaque(false);
        guidanceText.setLayout(new BoxLayout(guidanceText, BoxLayout.Y_AXIS));
        guidanceText.add(guidanceKicker);
        guidanceText.add(Box.createVerticalStrut(18));
        guidanceText.add(guidanceTitle);
        guidanceText.add(Box.createVerticalStrut(10));
        guidanceText.add(guidanceBody);
        guidance.add(guidanceText, BorderLayout.NORTH);

        JPanel tip = new JPanel(new BorderLayout(10, 0));
        tip.setOpaque(false);
        JLabel tipIcon = new JLabel("✓");
        tipIcon.setForeground(new Color(94, 232, 209));
        tipIcon.setFont(new Font("SansSerif", Font.BOLD, 20));
        tip.add(tipIcon, BorderLayout.WEST);
        JLabel tipText = new JLabel("<html><div style='width:240px;color:#D8E2FF'>Use Refresh insights to load the latest records from your database.</div></html>");
        tipText.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tip.add(tipText, BorderLayout.CENTER);
        guidance.add(tip, BorderLayout.CENTER);
        lower.add(guidance);
        page.add(lower);
        return page;
    }

    private JPanel insightMetricCard(String title, JLabel value, String note, Color accent, Color tint) {
        GlassPanel card = new GlassPanel(new Color(255,255,255,225), new Color(255,255,255,245), 20);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 17, 20));
        card.setMinimumSize(new Dimension(175, 120));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        titleLabel.setForeground(muted);
        value.setFont(new Font("SansSerif", Font.BOLD, 25));
        value.setForeground(accent);
        JLabel noteLabel = label(note, muted, 11);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(value);
        card.add(Box.createVerticalStrut(7));
        card.add(noteLabel);
        return card;
    }

private void showSecurityAlertPopupIfNeeded() {
        if (securityTableModel == null || !isShowing()) return;
        java.util.List<String> unresolved = new java.util.ArrayList<>();
        for (int row = 0; row < securityTableModel.getRowCount(); row++) {
            String status = String.valueOf(securityTableModel.getValueAt(row, 6));
            if (status.toLowerCase(Locale.ROOT).contains("review")
                    || status.toLowerCase(Locale.ROOT).contains("pending")) {
                String id = String.valueOf(securityTableModel.getValueAt(row, 0));
                String event = String.valueOf(securityTableModel.getValueAt(row, 2));
                String risk = String.valueOf(securityTableModel.getValueAt(row, 3));
                unresolved.add(id + ":" + event + ":" + risk);
            }
        }
        if (unresolved.isEmpty()) {
            lastShownAlertSignature = "";
            return;
        }
        String signature = String.join("|", unresolved);
        if (signature.equals(lastShownAlertSignature)) return;
        lastShownAlertSignature = signature;

        StringBuilder details = new StringBuilder();
        int shown = 0;
        for (int row = 0; row < securityTableModel.getRowCount() && shown < 3; row++) {
            String status = String.valueOf(securityTableModel.getValueAt(row, 6));
            if (status.toLowerCase(Locale.ROOT).contains("review")
                    || status.toLowerCase(Locale.ROOT).contains("pending")) {
                details.append("• ")
                       .append(securityTableModel.getValueAt(row, 2))
                       .append(" — ")
                       .append(securityTableModel.getValueAt(row, 3))
                       .append(" risk\n");
                shown++;
            }
        }
        if (unresolved.size() > shown) details.append("• And ").append(unresolved.size() - shown).append(" more alert(s)\n");
        showTrustGuardAlertDialog(unresolved.size(), details.toString());
    }

    private void showTrustGuardAlertDialog(int count, String details) {
        JDialog dialog = new JDialog(this, "SmartBank · Security review", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        Color red = new Color(202, 57, 70);
        Color pale = new Color(255, 246, 247);
        Color ink = new Color(35, 48, 76);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(new Color(238, 205, 210)));

        JPanel body = new JPanel();
        body.setBackground(pale);
        body.setBorder(new EmptyBorder(24, 32, 22, 32));
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel symbol = new JLabel("!", SwingConstants.CENTER);
        symbol.setOpaque(true);
        symbol.setBackground(new Color(255, 225, 228));
        symbol.setForeground(red);
        symbol.setFont(new Font("SansSerif", Font.BOLD, 27));
        symbol.setBorder(BorderFactory.createLineBorder(new Color(248, 205, 210)));
        symbol.setPreferredSize(new Dimension(54, 54));
        symbol.setMaximumSize(new Dimension(54, 54));
        symbol.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(symbol);
        body.add(Box.createVerticalStrut(13));

        JLabel title = new JLabel("Security review recommended", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 19));
        title.setForeground(new Color(157, 38, 51));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(title);
        body.add(Box.createVerticalStrut(6));
        JLabel subtitle = new JLabel("TrustGuard found activity that may need your attention", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(111, 76, 83));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(subtitle);
        body.add(Box.createVerticalStrut(20));

        JLabel countLabel = new JLabel(count + " alert(s) awaiting review", SwingConstants.CENTER);
        countLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        countLabel.setForeground(ink);
        countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(countLabel);
        body.add(Box.createVerticalStrut(10));
        JTextArea detailArea = new JTextArea(details.trim());
        detailArea.setEditable(false);
        detailArea.setFocusable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        detailArea.setForeground(new Color(92, 67, 74));
        detailArea.setBackground(pale);
        detailArea.setBorder(null);
        detailArea.setRows(Math.min(4, Math.max(2, count)));
        detailArea.setColumns(38);
        body.add(detailArea);
        body.add(Box.createVerticalStrut(12));
        JLabel note = new JLabel("If an activity was not intended, report it in Security Center.", SwingConstants.CENTER);
        note.setFont(new Font("SansSerif", Font.PLAIN, 11));
        note.setForeground(new Color(111, 76, 83));
        note.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(note);
        root.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 14));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(244, 220, 223)));
        JButton dismiss = new JButton("Not now");
        styleDialogButton(dismiss, new Color(247, 249, 253), new Color(63, 75, 101));
        JButton review = new JButton("Review alerts");
        styleDialogButton(review, red, Color.WHITE);
        dismiss.addActionListener(e -> dialog.dispose());
        review.addActionListener(e -> {
            dialog.dispose();
            activePage = "Security";
            for (Map.Entry<String, JButton> entry : navButtons.entrySet()) entry.getValue().repaint();
            cardLayout.show(contentPanel, "Security");
            loadDatabaseSecurityEvents();
        });
        footer.add(dismiss);
        footer.add(review);
        root.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.getRootPane().setDefaultButton(review);
        dialog.pack();
        dialog.setSize(510, Math.max(350, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    private void loadDatabaseSecurityEvents() {
        if (securityTableModel == null) return;
        try {
            java.util.List<String[]> events = BankApi.getSecurityEvents(1);
            Map<String, String> reviewStates = new HashMap<>();
            for (String[] event : events) {
                if (event.length < 7) continue;
                String type = event[2] == null ? "" : event[2];
                String description = event[4] == null ? "" : event[4];
                if (type.equalsIgnoreCase("Customer review") || type.equalsIgnoreCase("Customer report")) {
                    java.util.regex.Matcher match = java.util.regex.Pattern
                            .compile("Alert #([^|]+)\\|").matcher(description);
                    String decisionStatus = type.equalsIgnoreCase("Customer review")
                            ? "Confirmed by customer" : "Reported by customer";
                    if (match.find()) reviewStates.put(match.group(1).trim(), decisionStatus);
                }
            }
            securityTableModel.setRowCount(0);
            for (String[] event : events) {
                if (event.length < 7) continue;
                String[] display = event.clone();
                String type = display[2] == null ? "" : display[2];
                if (type.equalsIgnoreCase("Customer review")) display[6] = "Confirmed by customer";
                else if (type.equalsIgnoreCase("Customer report")) display[6] = "Reported by customer";
                else {
                    String reviewedStatus = reviewStates.get(display[0]);
                    if (reviewedStatus != null) display[6] = reviewedStatus;
                }
                securityTableModel.addRow(display);
            }
        } catch (Exception ex) {
            showMessage("Could not load saved security alerts. Check get_security_events.php.\n\n" + ex.getMessage());
        }
    }

    private void reviewSelectedSecurityEvent(boolean recognized) {
        int selected = securityTable.getSelectedRow();
        if (selected < 0) {
            showMessage("Select a security alert first, then choose an action.");
            return;
        }
        int row = securityTable.convertRowIndexToModel(selected);
        String eventId = String.valueOf(securityTableModel.getValueAt(row, 0));
        String eventType = String.valueOf(securityTableModel.getValueAt(row, 2));
        String description = String.valueOf(securityTableModel.getValueAt(row, 4));
        String status = String.valueOf(securityTableModel.getValueAt(row, 6));
        if (!(status.toLowerCase(Locale.ROOT).contains("review")
                || status.toLowerCase(Locale.ROOT).contains("pending"))) {
            showMessage("This alert has already been reviewed.");
            return;
        }
        if (eventType.equalsIgnoreCase("Customer review") || eventType.equalsIgnoreCase("Customer report")) {
            showMessage("Choose an unreviewed security alert.");
            return;
        }

        String reviewType = recognized ? "Customer review" : "Customer report";
        String reviewStatus = recognized ? "Confirmed by customer" : "Reported by customer";
        String risk = recognized ? "Low" : "High";
        String action = recognized ? "Customer confirmed this activity; no further action requested."
                : "Customer does not recognize this activity; contact the account holder and review the account.";
        String note = "Alert #" + eventId + " | " + (recognized ? "Customer confirmed activity" : "Customer reported activity")
                + " | " + eventType + " | " + description;
        try {
            String response = BankApi.saveSecurityEvent(1, null, reviewType, risk, note, action, reviewStatus);
            if (response == null || !response.replace(" ", "").contains("\"success\":true")) {
                showMessage("Could not save your alert review.\n" + response);
                return;
            }
            loadDatabaseSecurityEvents();
            showMessage(recognized
                    ? "Thanks. This alert is marked as activity you recognize."
                    : "Your report has been recorded. Please contact your bank if this was not you.");
        } catch (Exception ex) {
            showMessage("Could not save your alert review. Check save_security_event.php.\n\n" + ex.getMessage());
        }
    }

    private void loadInsights() {
        try {
            double[] stats = BankApi.getStats(1);
            java.util.List<String[]> rows = BankApi.getTransactions(1);
            if (insightsIncomeValue != null) insightsIncomeValue.setText(money(stats[0]));
            if (insightsExpensesValue != null) insightsExpensesValue.setText(money(stats[1]));

            Map<String, Integer> counts = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (String[] row : rows) {
                String type = row.length > 4 && row[4] != null ? row[4] : "";
                String recipient = row.length > 3 && row[3] != null ? row[3].trim() : "";
                if (!recipient.isBlank() && (type.equalsIgnoreCase("Transfer") || type.equalsIgnoreCase("Withdrawal"))) {
                    counts.put(recipient, counts.getOrDefault(recipient, 0) + 1);
                }
            }
            java.util.List<Map.Entry<String, Integer>> matches = new java.util.ArrayList<>();
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                if (entry.getValue() >= 2) matches.add(entry);
            }
            matches.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
            if (insightsPatternCount != null) insightsPatternCount.setText(matches.size() + (matches.size() == 1 ? " pattern" : " patterns"));
            if (recurringPaymentsPanel != null) {
                recurringPaymentsPanel.removeAll();
                if (matches.isEmpty()) {
                    JPanel empty = new JPanel();
                    empty.setOpaque(false);
                    empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
                    JLabel emptyTitle = new JLabel("No repeated patterns yet");
                    emptyTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
                    emptyTitle.setForeground(ink);
                    empty.add(emptyTitle);
                    empty.add(Box.createVerticalStrut(6));
                    empty.add(label("More transaction history can help surface patterns.", muted, 12));
                    recurringPaymentsPanel.add(empty);
                } else {
                    int limit = Math.min(6, matches.size());
                    for (int i = 0; i < limit; i++) {
                        Map.Entry<String, Integer> entry = matches.get(i);
                        JPanel rowPanel = new JPanel(new BorderLayout(10, 8));
                        rowPanel.setOpaque(false);
                        rowPanel.setBorder(new EmptyBorder(10, 4, 10, 4));
                        JLabel marker = new JLabel("●");
                        marker.setForeground(i % 2 == 0 ? new Color(91, 111, 245) : new Color(31, 166, 120));
                        marker.setFont(new Font("SansSerif", Font.BOLD, 13));
                        rowPanel.add(marker, BorderLayout.WEST);
                        JLabel recipient = new JLabel("<html><b>" + escapeHtml(entry.getKey()) + "</b><br><span style='color:#7581A0'>Repeated activity detected</span></html>");
                        recipient.setFont(new Font("SansSerif", Font.PLAIN, 13));
                        recipient.setForeground(ink);
                        rowPanel.add(recipient, BorderLayout.CENTER);
                        JLabel count = new JLabel(entry.getValue() + " transactions");
                        count.setFont(new Font("SansSerif", Font.BOLD, 11));
                        count.setForeground(new Color(75, 91, 171));
                        count.setOpaque(true);
                        count.setBackground(new Color(237, 240, 255));
                        count.setBorder(new EmptyBorder(7, 9, 7, 9));
                        rowPanel.add(count, BorderLayout.EAST);
                        recurringPaymentsPanel.add(rowPanel);
                        if (i < limit - 1) recurringPaymentsPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
                    }
                }
                recurringPaymentsPanel.revalidate();
                recurringPaymentsPanel.repaint();
            }
        } catch (Exception ex) {
            showMessage("Could not calculate insights.\nCheck that XAMPP Apache and MySQL are running.\n\n" + ex.getMessage());
        }
    }
    private void findAndSetInsights(Component c,String text) { if(c instanceof JTextArea && "insightsOutput".equals(c.getName())) ((JTextArea)c).setText(text); if(c instanceof Container) for(Component child:((Container)c).getComponents()) findAndSetInsights(child,text); }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private JPanel pagePanel() {
        GradientPanel page = new GradientPanel(new Color(249, 248, 252), new Color(243, 241, 248));
        page.setBorder(new EmptyBorder(30, 32, 28, 32));
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setOpaque(false);
        return page;
    }

    private JPanel whitePanel() {
        GlassPanel panel = new GlassPanel(new Color(255, 255, 255, 245), new Color(231, 228, 239), 22);
        panel.setLayout(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return panel;
    }

    private JPanel statCard(String title, JLabel value, String note, Color accent) {
        GlassPanel card = new GlassPanel(new Color(255,255,255,245), new Color(231,228,239), 22);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(19, 20, 19, 20));
        JPanel titleRow = new JPanel(new BorderLayout()); titleRow.setOpaque(false);
        JLabel dot = new JLabel("●"); dot.setForeground(accent); dot.setFont(new Font("SansSerif", Font.BOLD, 13));
        JLabel titleLabel = label(title, muted, 10); titleLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        titleRow.add(titleLabel, BorderLayout.WEST); titleRow.add(dot, BorderLayout.EAST);
        card.setMinimumSize(new Dimension(180, 120));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleRow); card.add(Box.createVerticalStrut(17));
        value.setFont(new Font("SansSerif", Font.BOLD, 27)); value.setForeground(ink); card.add(value);
        card.add(Box.createVerticalStrut(8)); card.add(label(note, muted, 11));
        return card;
    }

    private JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 25));
        label.setForeground(ink);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel label(String text, Color color, int size) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, size));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JButton actionButton(String text) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color top = getModel().isPressed() ? new Color(105, 82, 190) : new Color(130, 105, 218);
                Color bottom = getModel().isRollover() ? new Color(157, 136, 229) : new Color(112, 86, 198);
                g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(15, 12, 15, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

private static class CyberPage extends JPanel {
        private double pulse = 0;
        private final javax.swing.Timer animation;
        CyberPage() {
            setOpaque(false);
            animation = new javax.swing.Timer(70, e -> {
                pulse += 0.035;
                if (pulse > Math.PI * 2) pulse = 0;
                repaint();
            });
            animation.start();
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, new Color(7, 17, 33), getWidth(), getHeight(), new Color(10, 31, 54)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(new Color(52, 157, 197, 28));
            for (int x = 0; x < getWidth(); x += 42) g2.drawLine(x, 0, x, getHeight());
            for (int y = 0; y < getHeight(); y += 42) g2.drawLine(0, y, getWidth(), y);
            int[][] points = {{getWidth()-190, 54}, {getWidth()-110, 115}, {getWidth()-250, 180}, {getWidth()-70, 270}, {getWidth()-300, 310}, {80, getHeight()-90}};
            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(new Color(45, 190, 220, 75));
            for (int i = 0; i < points.length - 1; i++) g2.drawLine(points[i][0], points[i][1], points[i+1][0], points[i+1][1]);
            for (int i = 0; i < points.length; i++) {
                int glow = 5 + (int)(3 * (1 + Math.sin(pulse + i)));
                g2.setColor(new Color(52, 212, 241, 35));
                g2.fillOval(points[i][0] - glow, points[i][1] - glow, glow * 2, glow * 2);
                g2.setColor(new Color(65, 226, 247, 210));
                g2.fillOval(points[i][0] - 2, points[i][1] - 2, 5, 5);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class GradientPanel extends JPanel {
        private final Color first;
        private final Color second;
        GradientPanel() { this(new Color(250,249,253), new Color(241,239,248)); }
        GradientPanel(Color first, Color second) { this.first=first; this.second=second; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setPaint(new GradientPaint(0,0,first,getWidth(),getHeight(),second));
            g2.fillRect(0,0,getWidth(),getHeight());
            // Soft blurred-looking accent orbs create a glassmorphism backdrop.
            g2.setColor(new Color(145, 114, 220, 18)); g2.fillOval(getWidth()-300, 25, 240, 240);
            g2.setColor(new Color(234, 156, 177, 18)); g2.fillOval(getWidth()-210, getHeight()-250, 230, 230);
            g2.dispose(); super.paintComponent(g);
        }
    }

    private static class PlaceholderTextField extends JTextField {
        private final String placeholder;
        PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(true);
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(132, 143, 174));
                g2.setFont(getFont());
                Insets insets = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(placeholder, insets.left + 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        }
    }

    private static class GlassPanel extends JPanel {
        private final Color fill;
        private final Color borderColor;
        private final int radius;
        GlassPanel(Color fill, Color borderColor, int radius) {
            this.fill=fill; this.borderColor=borderColor; this.radius=radius; setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(45, 63, 125, 12));
            g2.fillRoundRect(2, 4, getWidth()-4, getHeight()-5, radius, radius);
            g2.setColor(fill); g2.fillRoundRect(0, 0, getWidth()-2, getHeight()-4, radius, radius);
            g2.setColor(borderColor); g2.setStroke(new BasicStroke(1.1f));
            g2.drawRoundRect(0, 0, getWidth()-2, getHeight()-4, radius, radius);
            g2.dispose(); super.paintComponent(g);
        }
    }

    

private JScrollPane styleScrollPane(JScrollPane pane, boolean darkTheme) {
        Color track = darkTheme ? new Color(12, 25, 48) : new Color(239, 243, 255);
        Color thumb = darkTheme ? new Color(49, 196, 219) : new Color(133, 151, 224);
        Color hover = darkTheme ? new Color(91, 226, 241) : new Color(91, 111, 226);
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.getViewport().setOpaque(true);
        pane.getVerticalScrollBar().setUI(new RoundedScrollBarUI(track, thumb, hover));
        pane.getHorizontalScrollBar().setUI(new RoundedScrollBarUI(track, thumb, hover));
        pane.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        pane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        pane.getVerticalScrollBar().setUnitIncrement(16);
        pane.getHorizontalScrollBar().setUnitIncrement(16);
        pane.setWheelScrollingEnabled(true);
        return pane;
    }

private static class RoundedScrollBarUI extends BasicScrollBarUI {
        private final Color trackColor;
        private final Color thumbColor;
        private final Color hoverColor;

        RoundedScrollBarUI(Color trackColor, Color thumbColor, Color hoverColor) {
            this.trackColor = trackColor;
            this.thumbColor = thumbColor;
            this.hoverColor = hoverColor;
        }

        @Override protected void configureScrollBarColors() {
            trackColor.getRGB();
            thumbColor.getRGB();
        }

        @Override protected JButton createDecreaseButton(int orientation) {
            return zeroButton();
        }

        @Override protected JButton createIncreaseButton(int orientation) {
            return zeroButton();
        }

        private JButton zeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            button.setVisible(false);
            return button;
        }

        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle bounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(trackColor);
            g2.fillRoundRect(bounds.x + 2, bounds.y, Math.max(1, bounds.width - 4), bounds.height, 10, 10);
            g2.dispose();
        }

        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle bounds) {
            if (bounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean isHover = isDragging || isThumbRollover();
            g2.setColor(isHover ? hoverColor : thumbColor);
            if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
                g2.fillRoundRect(bounds.x + 2, bounds.y + 2, Math.max(4, bounds.width - 4), Math.max(8, bounds.height - 4), 9, 9);
            } else {
                g2.fillRoundRect(bounds.x + 2, bounds.y + 2, Math.max(8, bounds.width - 4), Math.max(4, bounds.height - 4), 9, 9);
            }
            g2.dispose();
        }
    }

    private void loadDatabaseTransactions() {
        try {
            java.util.List<String[]> transactions = BankApi.getTransactions(1);
            if (tableModel == null) {
                return;
            }

            tableModel.setRowCount(0);
            for (String[] transaction : transactions) {
                // BankApi row: [transaction ID, account ID, date, recipient, type, amount, status, reason]
                String transactionId = transaction[0];
                String accountId = transaction[1];
                String date = transaction[2];
                String recipient = transaction[3];
                String type = transaction[4];
                String amountText = transaction[5];
                String status = transaction[6];
                String reason = transaction[7];

                double amount = 0.0;
                try {
                    amount = Double.parseDouble(amountText);
                } catch (NumberFormatException ignored) {
                    // Keep the raw value if the API returns an unexpected amount.
                }

                String sign = type.equalsIgnoreCase("Deposit") ? "+ " : "- ";
                String displayAmount = sign + money(Math.abs(amount));
                String description = recipient == null || recipient.isBlank()
                        ? type : recipient;

                tableModel.addRow(new Object[]{
                    transactionId, accountId, date, description, type,
                    displayAmount, status, reason == null ? "" : reason
                });
            }
        } catch (Exception ex) {
            showMessage("Could not load transaction history from MySQL.\n"
                    + "Check XAMPP and get_transactions.php.\n\n"
                    + ex.getMessage());
        }
    }

    private JTable createTransactionTable() {
        if (tableModel == null) {
            tableModel = new DefaultTableModel(
                    new String[]{"Transaction ID", "Account ID", "Date", "Description",
                        "Type", "Amount", "Status", "Fraud Reason"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

        }

        JTable table = new JTable(tableModel);
        table.setRowHeight(36);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setForeground(ink);
        table.setBackground(new Color(255,255,255,230));
        table.setGridColor(new Color(226,231,246));
        table.setSelectionBackground(new Color(224,231,255));
        table.setSelectionForeground(navy);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(231, 237, 255));
        table.getTableHeader().setForeground(new Color(55, 72, 125));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setForeground(new Color(76, 88, 124));
        table.getTableHeader().setBackground(new Color(238,242,255));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(220,226,244)));

        // Subtle alternating rows make the transaction history easier to scan.
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int column) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(
                        t, value, selected, focus, row, column);
                cell.setBorder(new EmptyBorder(0, 8, 0, 8));
                if (selected) {
                    cell.setBackground(new Color(224, 232, 255));
                    cell.setForeground(navy);
                } else {
                    cell.setBackground(row % 2 == 0
                            ? new Color(255, 255, 255) : new Color(247, 249, 255));
                    cell.setForeground(ink);
                }
                return cell;
            }
        });

        // Amounts: green for incoming money, warm red for outgoing money.
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int column) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(
                        t, value, selected, focus, row, column);
                cell.setHorizontalAlignment(SwingConstants.RIGHT);
                cell.setBorder(new EmptyBorder(0, 5, 0, 12));
                String amount = value == null ? "" : value.toString().trim();
                if (selected) {
                    cell.setBackground(new Color(224, 232, 255));
                    cell.setForeground(navy);
                } else if (amount.startsWith("+")) {
                    cell.setBackground(new Color(233, 250, 242));
                    cell.setForeground(new Color(16, 126, 83));
                    cell.setFont(cell.getFont().deriveFont(Font.BOLD));
                } else if (amount.startsWith("-")) {
                    cell.setBackground(new Color(255, 241, 240));
                    cell.setForeground(new Color(190, 58, 65));
                    cell.setFont(cell.getFont().deriveFont(Font.BOLD));
                }
                return cell;
            }
        });

        // Statuses: green for completed, amber/red for flagged transactions.
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int column) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(
                        t, value, selected, focus, row, column);
                String status = value == null ? "" : value.toString().toLowerCase(Locale.ROOT);
                cell.setBorder(new EmptyBorder(0, 8, 0, 8));
                cell.setFont(cell.getFont().deriveFont(Font.BOLD, 11f));
                if (selected) {
                    cell.setBackground(new Color(224, 232, 255));
                    cell.setForeground(navy);
                } else if (status.contains("completed")) {
                    cell.setBackground(new Color(226, 248, 237));
                    cell.setForeground(new Color(18, 128, 79));
                } else if (status.contains("flag") || status.contains("review") || status.contains("failed")) {
                    cell.setBackground(new Color(255, 239, 220));
                    cell.setForeground(new Color(166, 83, 14));
                } else {
                    cell.setBackground(row % 2 == 0 ? Color.WHITE : new Color(247, 249, 255));
                    cell.setForeground(ink);
                }
                return cell;
            }
        });

        // Type labels get a quiet category tint.
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int column) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(
                        t, value, selected, focus, row, column);
                String type = value == null ? "" : value.toString().toLowerCase(Locale.ROOT);
                cell.setBorder(new EmptyBorder(0, 8, 0, 8));
                cell.setFont(cell.getFont().deriveFont(Font.BOLD, 11f));
                if (selected) {
                    cell.setBackground(new Color(224, 232, 255)); cell.setForeground(navy);
                } else if (type.contains("deposit")) {
                    cell.setBackground(new Color(233, 247, 255)); cell.setForeground(new Color(36, 103, 174));
                } else if (type.contains("withdraw")) {
                    cell.setBackground(new Color(255, 246, 228)); cell.setForeground(new Color(161, 103, 22));
                } else if (type.contains("transfer")) {
                    cell.setBackground(new Color(239, 236, 255)); cell.setForeground(new Color(91, 70, 180));
                } else {
                    cell.setBackground(row % 2 == 0 ? Color.WHITE : new Color(247, 249, 255)); cell.setForeground(ink);
                }
                return cell;
            }
        });
        return table;
    }

    private void addTransaction(
            String description, String type, double amount, String status) {
        String date = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));

        tableModel.insertRow(0, new Object[]{
            date, description, type,
            (amount < 0 ? "- " : "+ ") + money(Math.abs(amount)),
            status
        });
    }

    private String money(double amount) {
        return String.format(Locale.US, "₹%,.2f", amount);
    }

    private void refreshOverview() {
        if (balanceValue != null) {
            balanceValue.setText(money(balance));
        }
        
        if (incomeValue != null) {
            incomeValue.setText(money(income));
        }
        
        if (expensesValue != null) {
            expensesValue.setText(money(expenses));
        }
        if (securityValue != null) {
            securityValue.setText(suspiciousCount == 0? "Monitoring active": suspiciousCount + " flagged transaction(s)");
        }
    }

    private boolean validAmount(String text) {
        try {
            double amount = Double.parseDouble(text.trim());
            return Double.isFinite(amount) && amount > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private void transferMoney() {
        // The same field accepts either a demo recipient name or a real
        // account number already present in the SmartBank demo database.
        JTextField accountNumber = styledInput("e.g. Sheetal or SB1002");
        JTextField amountField = styledInput("0.00");
        JLabel inlineHint = new JLabel("Demo transfer • no real money is moved");
        inlineHint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        inlineHint.setForeground(new Color(105, 117, 148));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(4, 4, 4, 4));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 0, 5, 0);
        c.gridy = 0;
        JLabel title = new JLabel("Send money");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(ink);
        form.add(title, c);
        c.gridy++;
        JLabel subtitle = new JLabel("Transfer funds to a name or another SmartBank demo account.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(muted);
        form.add(subtitle, c);
        c.gridy++; c.insets = new Insets(16, 0, 5, 0);
        form.add(fieldLabel("RECIPIENT NAME OR ACCOUNT NUMBER"), c);
        c.gridy++; c.insets = new Insets(0, 0, 8, 0);
        form.add(accountNumber, c);
        c.gridy++; c.insets = new Insets(8, 0, 5, 0);
        form.add(fieldLabel("AMOUNT (₹)"), c);
        c.gridy++; c.insets = new Insets(0, 0, 8, 0);
        form.add(amountField, c);
        c.gridy++; c.insets = new Insets(8, 0, 4, 0);
        form.add(inlineHint, c);

        JDialog dialog = new JDialog(this, "SmartBank • Transfer", true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setContentPane(form);
        dialog.getRootPane().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 224, 244), 1),
                new EmptyBorder(20, 22, 18, 22)));
        dialog.getRootPane().setBackground(Color.WHITE);

        JButton cancel = dialogButton("Cancel", new Color(240, 244, 252), ink);
        JButton continueButton = dialogButton("Continue  →", blue, Color.WHITE);
        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setBackground(Color.WHITE);
        buttons.setBorder(new EmptyBorder(16, 0, 0, 0));
        buttons.add(cancel); buttons.add(continueButton);
        c.gridy++; c.insets = new Insets(0, 0, 0, 0);
        form.add(buttons, c);

        final boolean[] confirmed = {false};
        Runnable submit = () -> { confirmed[0] = true; dialog.dispose(); };
        continueButton.addActionListener(e -> submit.run());
        cancel.addActionListener(e -> dialog.dispose());
        amountField.addActionListener(e -> submit.run());
        accountNumber.addActionListener(e -> amountField.requestFocusInWindow());
        dialog.getRootPane().setDefaultButton(continueButton);
        dialog.pack();
        dialog.setSize(new Dimension(440, dialog.getPreferredSize().height));
        dialog.setLocationRelativeTo(this);
        SwingUtilities.invokeLater(() -> accountNumber.requestFocusInWindow());
        dialog.setVisible(true);
        if (!confirmed[0]) return;

        String destination = accountNumber.getText().trim();
        if (destination.isEmpty() || !validAmount(amountField.getText())) {
            showMessage("Enter a recipient name or account number and a valid positive amount.");
            return;
        }

        double amount = Double.parseDouble(amountField.getText().trim());
        if (amount > balance) {
            showMessage("Insufficient account balance.");
            return;
        }

        // Keep the existing self-transfer guard for the sender account.
        if (destination.equalsIgnoreCase("SB100001")) {
            showMessage("Choose a different destination account.");
            return;
        }

        String reason = "";
        if (amount >= 10000) reason = "Large transfer (₹10,000 or more)";
        String key = destination.toLowerCase(Locale.ROOT);
        int prior = countPreviousTransfersTo(destination);
        if (prior > 0) {
            reason = reason.isEmpty()
                    ? "Repeated transfer to same recipient"
                    : reason + "; repeated destination";
        }

        // SB followed by digits is treated as an account number. It must exist
        // in the database; the PHP account-transfer endpoint credits that account.
        // Any other text is a name-based demo transfer. It debits the sender and
        // records the recipient name in transaction history without pretending
        // that an unregistered account received a real credit.
        boolean accountNumberTransfer = destination.matches("(?i)^SB[0-9]+$");

        try {
            String response;
            if (accountNumberTransfer) {
                response = BankApi.transferBetweenAccounts(1, destination, amount, reason);
            } else {
                response = BankApi.saveTransaction(
                        1,
                        destination,
                        "Transfer",
                        amount,
                        reason.isBlank() ? "Completed" : "Flagged for review",
                        reason);
            }

            if (response == null || !response.contains("\"success\":true")) {
                showMessage("Transfer failed: " + response);
                return;
            }

            recipientTransferCounts.put(key, prior + 1);
            balance = BankApi.getBalance(1);
            loadDatabaseStats();
            loadDatabaseTransactions();
            loadDatabaseSecurityEvents();
            refreshOverview();

            String transferType = accountNumberTransfer
                    ? "Account transfer"
                    : "Name-based demo transfer";
            showPaymentSuccess("Transfer successful", amount,
                    accountNumberTransfer
                            ? "Your demo transfer has been recorded."
                            : "Your demo transfer to " + destination + " has been recorded.",
                    destination);

            String receipt = "SMARTBANK DEMO TRANSFER RECEIPT\nDate: " + LocalDateTime.now()
                    + "\nFrom account: SB100001\nTo: " + destination
                    + "\nTransfer type: " + transferType
                    + "\nAmount: " + money(amount)
                    + "\nStatus: " + (reason.isBlank() ? "Completed" : "Flagged for review")
                    + "\nRisk review: " + (reason.isBlank() ? "No rule triggered" : reason)
                    + (accountNumberTransfer
                        ? "\nDestination account balance updated in the demo database."
                        : "\nName-based demo only; no destination bank account was credited.")
                    + "\n\nDemo only — no real money moved.";
            previewReceipt(receipt, "SmartBank Transfer Receipt");

            if (!reason.isBlank()) {
                suspiciousCount++;
                if (statusMessage != null) statusMessage.setText("TrustGuard: " + reason);
                try {
                    BankApi.saveSecurityEvent(1, null, "Transfer risk check", "Medium",
                            reason + "; destination " + destination + ", amount " + money(amount),
                            "Verify recipient and transfer amount");
                } catch (Exception ex) {
                    showMessage("Transfer completed, but alert saving failed: " + ex.getMessage());
                }
                loadDatabaseSecurityEvents();
                if (prior > 0 && reason.toLowerCase(Locale.ROOT).contains("repeated")) {
                    showRepeatedTransferReviewPrompt(destination, amount);
                }
            }
        } catch (Exception ex) {
            showMessage("Transfer failed. For an SB account number, confirm that the destination account exists. "
                    + "For a recipient name, confirm that XAMPP Apache and MySQL are running.\n\n"
                    + ex.getMessage());
        }
    }
private int countPreviousTransfersTo(String destination) {
        int count = 0;
        try {
            for (String[] transaction : BankApi.getTransactions(1)) {
                if (transaction == null || transaction.length < 5) continue;
                String type = transaction[4] == null ? "" : transaction[4].trim();
                String description = transaction[3] == null ? "" : transaction[3].trim();
                if (type.equalsIgnoreCase("Transfer")
                        && description.toLowerCase(Locale.ROOT).contains(destination.toLowerCase(Locale.ROOT))) {
                    count++;
                }
            }
        } catch (Exception ignored) {
            // Fall back to the current-session count if the transaction list is temporarily unavailable.
            count = recipientTransferCounts.getOrDefault(destination.toLowerCase(Locale.ROOT), 0);
        }
        return count;
    }

    private void showRepeatedTransferReviewPrompt(String destination, double amount) {
        JDialog dialog = new JDialog(this, "SmartBank · Payment review", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        Color red = new Color(202, 57, 70);
        Color pale = new Color(255, 246, 247);
        Color ink = new Color(35, 48, 76);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(new Color(238, 205, 210)));
        JPanel body = new JPanel();
        body.setBackground(pale);
        body.setBorder(new EmptyBorder(24, 32, 22, 32));
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel symbol = new JLabel("!", SwingConstants.CENTER);
        symbol.setOpaque(true);
        symbol.setBackground(new Color(255, 225, 228));
        symbol.setForeground(red);
        symbol.setFont(new Font("SansSerif", Font.BOLD, 27));
        symbol.setBorder(BorderFactory.createLineBorder(new Color(248, 205, 210)));
        symbol.setPreferredSize(new Dimension(54, 54));
        symbol.setMaximumSize(new Dimension(54, 54));
        symbol.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(symbol);
        body.add(Box.createVerticalStrut(13));

        JLabel title = new JLabel("Repeated transfer detected", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 19));
        title.setForeground(new Color(157, 38, 51));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(title);
        body.add(Box.createVerticalStrut(6));
        JLabel subtitle = new JLabel("Please review this payment activity", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(111, 76, 83));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(subtitle);
        body.add(Box.createVerticalStrut(18));

        JPanel detailCard = new JPanel();
        detailCard.setBackground(Color.WHITE);
        detailCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(244, 220, 223)),
                new EmptyBorder(13, 16, 13, 16)));
        detailCard.setLayout(new BoxLayout(detailCard, BoxLayout.Y_AXIS));
        JLabel recipient = new JLabel("Recipient account");
        recipient.setFont(new Font("SansSerif", Font.PLAIN, 11));
        recipient.setForeground(new Color(112, 125, 148));
        JLabel recipientValue = new JLabel(destination);
        recipientValue.setFont(new Font("SansSerif", Font.BOLD, 14));
        recipientValue.setForeground(ink);
        JLabel amountCaption = new JLabel("Latest transfer");
        amountCaption.setFont(new Font("SansSerif", Font.PLAIN, 11));
        amountCaption.setForeground(new Color(112, 125, 148));
        JLabel amountValue = new JLabel(money(amount));
        amountValue.setFont(new Font("SansSerif", Font.BOLD, 22));
        amountValue.setForeground(red);
        detailCard.add(recipient);
        detailCard.add(Box.createVerticalStrut(3));
        detailCard.add(recipientValue);
        detailCard.add(Box.createVerticalStrut(12));
        detailCard.add(amountCaption);
        detailCard.add(Box.createVerticalStrut(3));
        detailCard.add(amountValue);
        detailCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(detailCard);
        body.add(Box.createVerticalStrut(14));
        JLabel explanation = new JLabel("The transfer was recorded and flagged for review.", SwingConstants.CENTER);
        explanation.setFont(new Font("SansSerif", Font.PLAIN, 12));
        explanation.setForeground(ink);
        explanation.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(explanation);
        body.add(Box.createVerticalStrut(5));
        JLabel note = new JLabel("If this activity was not intended, report it in Security Center.", SwingConstants.CENTER);
        note.setFont(new Font("SansSerif", Font.PLAIN, 11));
        note.setForeground(new Color(111, 76, 83));
        note.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(note);
        body.add(Box.createVerticalStrut(5));
        JLabel disclaimer = new JLabel("A review reminder, not a confirmed fraud finding.", SwingConstants.CENTER);
        disclaimer.setFont(new Font("SansSerif", Font.ITALIC, 10));
        disclaimer.setForeground(new Color(130, 106, 111));
        disclaimer.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(disclaimer);
        root.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 14));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(244, 220, 223)));
        JButton dismiss = new JButton("Dismiss");
        styleDialogButton(dismiss, new Color(247, 249, 253), new Color(63, 75, 101));
        JButton review = new JButton("Review Security Center");
        styleDialogButton(review, red, Color.WHITE);
        dismiss.addActionListener(e -> dialog.dispose());
        review.addActionListener(e -> {
            dialog.dispose();
            activePage = "Security";
            for (Map.Entry<String, JButton> entry : navButtons.entrySet()) entry.getValue().repaint();
            cardLayout.show(contentPanel, "Security");
            loadDatabaseSecurityEvents();
        });
        footer.add(dismiss);
        footer.add(review);
        root.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.getRootPane().setDefaultButton(review);
        dialog.pack();
        dialog.setSize(500, Math.max(420, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(new Color(76, 91, 128));
        return label;
    }

private JTextField styledInput(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(ink);
        field.setCaretColor(blue);
        field.setBackground(new Color(249, 251, 255));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(199, 211, 239), 1),
                new EmptyBorder(10, 12, 10, 12)));
        field.setPreferredSize(new Dimension(350, 42));
        field.setToolTipText(placeholder);
        return field;
    }

private JButton dialogButton(String text, Color fill, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(foreground);
        button.setBackground(fill);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fill.equals(blue) ? blue : new Color(221, 228, 243), 1),
                new EmptyBorder(10, 12, 10, 12)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
    }

    private void previewReceipt(String receipt, String title) {
        // Parse the existing receipt text so the on-screen receipt is structured,
        // while the saved .txt receipt continues to contain the original details.
        Map<String, String> details = new java.util.LinkedHashMap<>();
        String dateText = "";
        for (String line : receipt.split("\\R")) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                String key = line.substring(0, colon).trim();
                String value = line.substring(colon + 1).trim();
                if (key.equalsIgnoreCase("Date")) dateText = value;
                else details.put(key, value);
            }
        }
        String amountText = details.getOrDefault("Amount", "—");
        String statusText = details.getOrDefault("Status", "Completed");
        boolean successful = statusText.equalsIgnoreCase("Completed")
                || statusText.equalsIgnoreCase("Success");

        JDialog dialog = new JDialog(this, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(600, 650);
        dialog.setMinimumSize(new Dimension(560, 600));
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel page = new JPanel(new BorderLayout(0, 0));
        page.setBackground(new Color(244, 247, 253));
        page.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Branded receipt header
        JPanel header = new JPanel(new BorderLayout(14, 8));
        header.setBackground(new Color(242, 238, 250));
        header.setBorder(new EmptyBorder(22, 26, 22, 26));
        JPanel brandBlock = new JPanel();
        brandBlock.setOpaque(false);
        brandBlock.setLayout(new BoxLayout(brandBlock, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("SMARTBANK");
        brand.setFont(new Font("SansSerif", Font.BOLD, 20));
        brand.setForeground(new Color(43, 42, 58));
        JLabel subBrand = new JLabel("DIGITAL BANKING  ·  TRANSACTION RECEIPT");
        subBrand.setFont(new Font("SansSerif", Font.BOLD, 10));
        subBrand.setForeground(new Color(112, 86, 181));
        subBrand.setBorder(new EmptyBorder(5, 0, 0, 0));
        brandBlock.add(brand);
        brandBlock.add(subBrand);
        JLabel badge = new JLabel(successful ? "  ✓  SUCCESS  " : "  •  RECEIPT  ");
        badge.setOpaque(true);
        badge.setBackground(successful ? new Color(27, 164, 111) : new Color(83, 105, 160));
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setBorder(new EmptyBorder(8, 9, 8, 9));
        header.add(brandBlock, BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);
        page.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(22, 26, 18, 26));
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel amountCard = new JPanel();
        amountCard.setBackground(Color.WHITE);
        amountCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 229, 244)),
                new EmptyBorder(18, 20, 18, 20)));
        amountCard.setLayout(new BoxLayout(amountCard, BoxLayout.Y_AXIS));
        JLabel amountCaption = new JLabel("TRANSACTION AMOUNT");
        amountCaption.setFont(new Font("SansSerif", Font.BOLD, 10));
        amountCaption.setForeground(new Color(107, 121, 151));
        amountCaption.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel amountLabel = new JLabel(amountText);
        amountLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        amountLabel.setForeground(successful ? new Color(21, 139, 91) : new Color(39, 53, 88));
        amountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel statusLabel = new JLabel(successful ? "✓  Transaction completed" : statusText);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(successful ? new Color(21, 139, 91) : new Color(181, 111, 31));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        amountCard.add(amountCaption);
        amountCard.add(Box.createVerticalStrut(8));
        amountCard.add(amountLabel);
        amountCard.add(Box.createVerticalStrut(7));
        amountCard.add(statusLabel);
        amountCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(amountCard);
        body.add(Box.createVerticalStrut(18));

        JLabel detailTitle = new JLabel("TRANSACTION DETAILS");
        detailTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        detailTitle.setForeground(new Color(77, 91, 124));
        detailTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(detailTitle);
        body.add(Box.createVerticalStrut(8));

        JPanel detailsCard = new JPanel();
        detailsCard.setBackground(Color.WHITE);
        detailsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 229, 244)),
                new EmptyBorder(4, 16, 4, 16)));
        detailsCard.setLayout(new BoxLayout(detailsCard, BoxLayout.Y_AXIS));
        String[] preferredKeys = {"Date", "From account", "Account", "To account", "Transaction", "Risk review"};
        java.util.Set<String> rendered = new java.util.HashSet<>();
        if (!dateText.isBlank()) {
            addReceiptDetailRow(detailsCard, "Date & time", dateText);
            rendered.add("Date");
        }
        for (String key : preferredKeys) {
            if (key.equals("Date")) continue;
            String value = details.get(key);
            if (value != null && !value.isBlank() && !key.equals("Amount") && !key.equals("Status")) {
                addReceiptDetailRow(detailsCard, key, value);
                rendered.add(key);
            }
        }
        for (Map.Entry<String, String> entry : details.entrySet()) {
            String key = entry.getKey();
            if (!rendered.contains(key) && !key.equalsIgnoreCase("Amount")
                    && !key.equalsIgnoreCase("Status") && !entry.getValue().isBlank()) {
                addReceiptDetailRow(detailsCard, key, entry.getValue());
            }
        }
        detailsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(detailsCard);
        body.add(Box.createVerticalStrut(14));

        JLabel note = new JLabel("ⓘ  Demo transaction only — no real money moved.");
        note.setFont(new Font("SansSerif", Font.PLAIN, 11));
        note.setForeground(new Color(104, 117, 145));
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(note);

        page.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 229, 244)));
        JButton saveButton = new JButton("↓  Save receipt");
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(new Color(79, 105, 239));
        saveButton.setFocusPainted(false);
        saveButton.setBorder(new EmptyBorder(11, 18, 11, 18));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        closeButton.setForeground(new Color(49, 62, 95));
        closeButton.setBackground(new Color(239, 243, 252));
        closeButton.setFocusPainted(false);
        closeButton.setBorder(new EmptyBorder(11, 20, 11, 20));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> { dialog.dispose(); saveReceipt(receipt, title); });
        closeButton.addActionListener(e -> dialog.dispose());
        footer.add(saveButton);
        footer.add(closeButton);
        page.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(page);
        dialog.getRootPane().setDefaultButton(closeButton);
        dialog.setVisible(true);
    }

    private void addReceiptDetailRow(JPanel parent, String labelText, String valueText) {
        JPanel row = new JPanel(new BorderLayout(12, 4));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(10, 0, 10, 0));
        JLabel key = new JLabel(labelText);
        key.setFont(new Font("SansSerif", Font.PLAIN, 12));
        key.setForeground(new Color(105, 117, 145));
        JLabel value = new JLabel(valueText);
        value.setFont(new Font("SansSerif", Font.BOLD, 12));
        value.setForeground(new Color(36, 49, 81));
        value.setHorizontalAlignment(SwingConstants.RIGHT);
        value.setToolTipText(valueText);
        row.add(key, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        parent.add(row);
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(235, 239, 247));
        parent.add(separator);
    }

    private void saveReceipt(String receipt, String title) {
        String lowerTitle = title == null ? "" : title.toLowerCase(Locale.ROOT);
        String fileName = lowerTitle.contains("withdrawal")
                ? "SmartBank_Withdrawal_Receipt.txt"
                : lowerTitle.contains("transfer")
                ? "SmartBank_Transfer_Receipt.txt"
                : lowerTitle.contains("deposit")
                ? "SmartBank_Deposit_Receipt.txt"
                : "SmartBank_Receipt.txt";

        java.io.File documents = new java.io.File(
                System.getProperty("user.home"), "Documents");
        if (!documents.isDirectory()) {
            documents = new java.io.File(System.getProperty("user.home"));
        }

        final java.io.File[] chosenFolder = { documents };
        JDialog dialog = new JDialog(this, "Save transaction receipt", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        Color inkColor = new Color(31, 45, 78);
        Color mutedColor = new Color(104, 119, 148);
        Color pale = new Color(246, 248, 253);
        Color borderColor = new Color(216, 225, 242);
        Color accent = new Color(79, 104, 235);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(borderColor));

        JPanel header = new JPanel(new BorderLayout(12, 4));
        header.setBackground(new Color(242, 238, 250));
        header.setBorder(new EmptyBorder(17, 20, 17, 20));
        JPanel headerText = new JPanel();
        headerText.setOpaque(false);
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("SMARTBANK  /  RECEIPT EXPORT");
        brand.setFont(new Font("SansSerif", Font.BOLD, 11));
        brand.setForeground(new Color(112, 86, 181));
        JLabel heading = new JLabel("Save your transaction receipt");
        heading.setFont(new Font("SansSerif", Font.BOLD, 19));
        heading.setForeground(new Color(43, 42, 58));
        headerText.add(brand);
        headerText.add(Box.createVerticalStrut(5));
        headerText.add(heading);
        header.add(headerText, BorderLayout.CENTER);
        JLabel fileIcon = new JLabel("↓");
        fileIcon.setFont(new Font("SansSerif", Font.BOLD, 28));
        fileIcon.setForeground(new Color(132, 105, 220));
        header.add(fileIcon, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 22, 16, 22));
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel locationLabel = new JLabel("SAVE LOCATION");
        locationLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        locationLabel.setForeground(mutedColor);
        locationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(locationLabel);
        body.add(Box.createVerticalStrut(7));

        JPanel locationRow = new JPanel(new BorderLayout(10, 0));
        locationRow.setBackground(pale);
        locationRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                new EmptyBorder(10, 12, 10, 12)));
        JLabel folderIcon = new JLabel("▣");
        folderIcon.setFont(new Font("SansSerif", Font.BOLD, 17));
        folderIcon.setForeground(accent);
        JLabel locationText = new JLabel(chosenFolder[0].getAbsolutePath());
        locationText.setFont(new Font("SansSerif", Font.PLAIN, 12));
        locationText.setForeground(inkColor);
        locationRow.add(folderIcon, BorderLayout.WEST);
        locationRow.add(locationText, BorderLayout.CENTER);
        JButton browseButton = new JButton("Choose folder");
        styleDialogSecondaryButton(browseButton);
        browseButton.addActionListener(e -> {
            JFileChooser folderChooser = new JFileChooser(chosenFolder[0]);
            folderChooser.setDialogTitle("Choose receipt folder");
            folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            folderChooser.setAcceptAllFileFilterUsed(false);
            if (folderChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                chosenFolder[0] = folderChooser.getSelectedFile();
                locationText.setText(chosenFolder[0].getAbsolutePath());
                locationText.setToolTipText(chosenFolder[0].getAbsolutePath());
            }
        });
        locationRow.add(browseButton, BorderLayout.EAST);
        locationRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(locationRow);
        body.add(Box.createVerticalStrut(18));

        JLabel nameLabel = new JLabel("FILE NAME");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        nameLabel.setForeground(mutedColor);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(nameLabel);
        body.add(Box.createVerticalStrut(7));

        JTextField nameField = new JTextField(fileName);
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        nameField.setForeground(inkColor);
        nameField.setCaretColor(accent);
        nameField.setBackground(Color.WHITE);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                new EmptyBorder(11, 12, 11, 12)));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(nameField);
        body.add(Box.createVerticalStrut(8));

        JLabel hint = new JLabel("Text file  ·  UTF-8 encoding  ·  Your original receipt details are preserved");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setForeground(mutedColor);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(hint);
        root.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(new Color(249, 250, 254));
        footer.setBorder(new EmptyBorder(13, 18, 15, 18));
        JButton cancelButton = new JButton("Cancel");
        styleDialogSecondaryButton(cancelButton);
        JButton saveButton = new JButton("Save receipt");
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(accent);
        saveButton.setOpaque(true);
        saveButton.setFocusPainted(false);
        saveButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent),
                new EmptyBorder(10, 20, 10, 20)));
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                saveButton.setBackground(new Color(61, 85, 215));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                saveButton.setBackground(accent);
            }
        });
        cancelButton.addActionListener(e -> dialog.dispose());
        Runnable saveAction = () -> {
            String enteredName = nameField.getText().trim();
            if (enteredName.isEmpty()) {
                showStyledNotice("File name required", "Enter a name for the receipt file.", false);
                nameField.requestFocusInWindow();
                return;
            }
            if (!enteredName.toLowerCase(Locale.ROOT).endsWith(".txt")) {
                enteredName += ".txt";
            }
            java.io.File file = new java.io.File(chosenFolder[0], enteredName);
            if (file.exists()) {
                int answer = JOptionPane.showConfirmDialog(dialog,
                        "A file with this name already exists. Replace it?",
                        "Confirm replacement", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (answer != JOptionPane.YES_OPTION) return;
            }
            try {
                java.nio.file.Files.writeString(file.toPath(), receipt,
                        java.nio.charset.StandardCharsets.UTF_8);
                dialog.dispose();
                showStyledNotice("Receipt saved successfully",
                        "Your receipt has been saved to:\\n" + file.getAbsolutePath(), true);
            } catch (Exception ex) {
                showStyledNotice("Could not save receipt",
                        "Please choose another folder or file name.\\n" + ex.getMessage(), false);
            }
        };
        saveButton.addActionListener(e -> saveAction.run());
        nameField.addActionListener(e -> saveAction.run());
        footer.add(cancelButton);
        footer.add(saveButton);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.getRootPane().setDefaultButton(saveButton);
        dialog.pack();
        dialog.setSize(Math.max(600, dialog.getWidth()), 355);
        dialog.setLocationRelativeTo(this);
        nameField.selectAll();
        dialog.setVisible(true);
    }

    private void styleDialogSecondaryButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(new Color(43, 59, 96));
        button.setBackground(new Color(241, 245, 253));
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 221, 241)),
                new EmptyBorder(9, 14, 9, 14)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void depositMoney() {
        String input = amountEntryDialog("Add money", "Enter deposit amount (₹)", "Deposit");
        if (input == null) return;
        if (!validAmount(input)) {
            showMessage("Enter a valid positive amount.");
            return;
        }

        double amount = Double.parseDouble(input.trim());
        if (!saveTransactionAndRefreshBalance(
                "Cash deposit", "Deposit", amount, "Completed", "")) {
            return;
        }

        refreshOverview();
        showPaymentSuccess("Deposit successful", amount,
                "Cash deposit added to your demo account.", "SB100001");
        String receipt = "SMARTBANK DEMO DEPOSIT RECEIPT\n"
                + "Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm:ss a")) + "\n"
                + "Account: SB100001\n"
                + "Transaction: Cash deposit\n"
                + "Amount: " + money(amount) + "\n"
                + "Status: Completed\n"
                + "Balance after deposit: " + money(balance) + "\n\n"
                + "Demo only — no real money moved.";
        previewReceipt(receipt, "SmartBank Deposit Receipt");
    }

    private void withdrawMoney() {
        String input = amountEntryDialog("Withdraw money", "Enter withdrawal amount (₹)", "Withdraw");
        if (input == null) return;
        if (!validAmount(input)) {
            showMessage("Enter a valid positive amount.");
            return;
        }

        double amount = Double.parseDouble(input.trim());
        if (amount > balance) {
            showMessage("Insufficient account balance.");
            return;
        }

        if (!saveTransactionAndRefreshBalance(
                "Cash withdrawal", "Withdrawal", amount, "Completed", "")) {
            return;
        }

        refreshOverview();
        showPaymentSuccess("Withdrawal successful", amount,
                "Cash withdrawal completed from your demo account.", "SB100001");
        String receipt = "SMARTBANK DEMO WITHDRAWAL RECEIPT\n"
                + "Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm:ss a")) + "\n"
                + "Account: SB100001\n"
                + "Transaction: Cash withdrawal\n"
                + "Amount: " + money(amount) + "\n"
                + "Status: Completed\n"
                + "Balance after withdrawal: " + money(balance) + "\n\n"
                + "Demo only — no real money moved.";
        previewReceipt(receipt, "SmartBank Withdrawal Receipt");
    }

    private String amountEntryDialog(String title, String prompt, String actionLabel) {
        final String[] result = {null};
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(246, 249, 255));
        root.setBorder(BorderFactory.createLineBorder(new Color(208, 220, 244)));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(new Color(242, 238, 250));
        header.setBorder(new EmptyBorder(17, 20, 17, 20));
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("SMARTBANK  /  DEMO TRANSACTION");
        brand.setForeground(new Color(112, 86, 181));
        brand.setFont(new Font("SansSerif", Font.BOLD, 10));
        JLabel heading = new JLabel(title);
        heading.setForeground(new Color(43, 42, 58));
        heading.setFont(new Font("SansSerif", Font.BOLD, 20));
        heading.setBorder(new EmptyBorder(5, 0, 0, 0));
        brandText.add(brand);
        brandText.add(heading);
        header.add(brandText, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setBackground(new Color(246, 249, 255));
        body.setBorder(new EmptyBorder(20, 24, 18, 24));
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        JLabel promptLabel = new JLabel(prompt);
        promptLabel.setForeground(new Color(43, 57, 91));
        promptLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        promptLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(promptLabel);
        body.add(Box.createVerticalStrut(10));

        JTextField amountField = new JTextField();
        amountField.setFont(new Font("SansSerif", Font.PLAIN, 18));
        amountField.setForeground(new Color(31, 46, 79));
        amountField.setCaretColor(new Color(73, 101, 235));
        amountField.setBackground(Color.WHITE);
        amountField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 207, 242), 1),
                new EmptyBorder(11, 13, 11, 13)));
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountField.setToolTipText("Enter an amount greater than zero");
        body.add(amountField);
        body.add(Box.createVerticalStrut(9));
        JLabel helper = new JLabel("Demo environment · No real money is moved");
        helper.setFont(new Font("SansSerif", Font.PLAIN, 11));
        helper.setForeground(new Color(111, 125, 153));
        helper.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(helper);
        root.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 231, 244)));
        JButton cancel = new JButton("Cancel");
        styleDialogButton(cancel, new Color(237, 242, 252), new Color(52, 67, 101));
        JButton confirm = new JButton(actionLabel);
        styleDialogButton(confirm, new Color(76, 101, 239), Color.WHITE);
        confirm.setPreferredSize(new Dimension(126, 42));
        cancel.setPreferredSize(new Dimension(100, 42));
        confirm.addActionListener(e -> {
            String value = amountField.getText().trim();
            if (!validAmount(value)) {
                JOptionPane.showMessageDialog(dialog,
                        "Enter a valid amount greater than ₹0.", "Check amount",
                        JOptionPane.WARNING_MESSAGE);
                amountField.requestFocusInWindow();
                return;
            }
            result[0] = value;
            dialog.dispose();
        });
        cancel.addActionListener(e -> dialog.dispose());
        amountField.addActionListener(e -> confirm.doClick());
        footer.add(cancel);
        footer.add(confirm);
        root.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.getRootPane().setDefaultButton(confirm);
        dialog.setSize(470, 310);
        dialog.setMinimumSize(new Dimension(450, 300));
        dialog.setLocationRelativeTo(this);
        SwingUtilities.invokeLater(amountField::requestFocusInWindow);
        dialog.setVisible(true);
        return result[0];
    }

    private void styleDialogButton(JButton button, Color fill, Color foreground) {
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(foreground);
        button.setBackground(fill);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fill.equals(new Color(76, 101, 239))
                        ? new Color(76, 101, 239) : new Color(215, 224, 242), 1),
                new EmptyBorder(10, 18, 10, 18)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void showPaymentSuccess(String title, double amount, String message, String account) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(420, 390);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(242, 238, 250));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel brand = new JLabel("SMARTBANK  ·  PAYMENT CONFIRMATION");
        brand.setForeground(new Color(112, 86, 181));
        brand.setFont(new Font("SansSerif", Font.BOLD, 10));
        header.add(brand, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 24, 14, 24));
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        AnimatedTick tick = new AnimatedTick();
        tick.setAlignmentX(Component.CENTER_ALIGNMENT);
        tick.setPreferredSize(new Dimension(92, 92));
        tick.setMaximumSize(new Dimension(92, 92));
        body.add(tick);
        body.add(Box.createVerticalStrut(8));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(new Color(27, 48, 78));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(titleLabel);
        body.add(Box.createVerticalStrut(5));
        JLabel amountLabel = new JLabel(money(amount));
        amountLabel.setFont(new Font("SansSerif", Font.BOLD, 27));
        amountLabel.setForeground(new Color(20, 145, 91));
        amountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(amountLabel);
        body.add(Box.createVerticalStrut(5));
        JLabel messageLabel = new JLabel("<html><div style='text-align:center;width:320px'>" + message + "</div></html>");
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(102, 117, 144));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(messageLabel);
        body.add(Box.createVerticalStrut(13));
        JLabel accountLabel = new JLabel("Account  " + account + "     ·     Demo transaction");
        accountLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        accountLabel.setForeground(new Color(74, 91, 124));
        accountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(accountLabel);
        root.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        footer.setBackground(new Color(247, 249, 253));
        JButton done = new JButton("Done");
        styleDialogButton(done, new Color(76, 101, 239), Color.WHITE);
        done.setPreferredSize(new Dimension(150, 42));
        done.addActionListener(e -> dialog.dispose());
        footer.add(done);
        root.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.getRootPane().setDefaultButton(done);
        javax.swing.Timer timer = new javax.swing.Timer(22, e -> tick.advance());
        timer.start();
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { timer.stop(); }
        });
        dialog.setVisible(true);
    }

    private static class AnimatedTick extends JPanel {
        private float progress = 0f;
        AnimatedTick() { setOpaque(false); }
        void advance() { progress = Math.min(1f, progress + 0.055f); repaint(); }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int d = Math.min(getWidth(), getHeight()) - 12;
            int x = (getWidth() - d) / 2, y = (getHeight() - d) / 2;
            g.setColor(new Color(225, 248, 237));
            g.fillOval(x, y, d, d);
            g.setColor(new Color(24, 166, 105));
            g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawOval(x + 5, y + 5, d - 10, d - 10);
            int[] xs = {x + (int)(d * .27), x + (int)(d * .43), x + (int)(d * .73)};
            int[] ys = {y + (int)(d * .52), y + (int)(d * .67), y + (int)(d * .36)};
            int firstX = xs[0], firstY = ys[0], midX = xs[1], midY = ys[1], lastX = xs[2], lastY = ys[2];
            double firstLength = Math.hypot(midX-firstX, midY-firstY);
            double secondLength = Math.hypot(lastX-midX, lastY-midY);
            double total = firstLength + secondLength;
            double drawn = total * Math.min(1f, progress * 1.35f);
            if (drawn > 0) {
                if (drawn <= firstLength) {
                    double ratio = drawn / firstLength;
                    g.drawLine(firstX, firstY, firstX + (int)((midX-firstX)*ratio), firstY + (int)((midY-firstY)*ratio));
                } else {
                    g.drawLine(firstX, firstY, midX, midY);
                    double ratio = Math.min(1.0, (drawn-firstLength)/secondLength);
                    g.drawLine(midX, midY, midX + (int)((lastX-midX)*ratio), midY + (int)((lastY-midY)*ratio));
                }
            }
            g.dispose();
        }
    }

    private void showStyledNotice(String titleText, String detailText, boolean success) {
        JDialog dialog = new JDialog(this, "SmartBank · Notification", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        Color accent = success ? new Color(29, 145, 100) : new Color(202, 57, 70);
        Color tint = success ? new Color(242, 251, 246) : new Color(255, 246, 247);
        Color line = success ? new Color(204, 235, 219) : new Color(238, 205, 210);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(line));
        JPanel body = new JPanel();
        body.setBackground(tint);
        body.setBorder(new EmptyBorder(26, 34, 24, 34));
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // A simple status medallion, deliberately without a tick graphic.
        JLabel symbol = new JLabel(success ? "●" : "!", SwingConstants.CENTER);
        symbol.setOpaque(true);
        symbol.setBackground(success ? new Color(221, 245, 231) : new Color(255, 225, 228));
        symbol.setForeground(accent);
        symbol.setFont(new Font("SansSerif", Font.BOLD, success ? 25 : 27));
        symbol.setBorder(BorderFactory.createLineBorder(line));
        symbol.setPreferredSize(new Dimension(58, 58));
        symbol.setMaximumSize(new Dimension(58, 58));
        symbol.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(symbol);
        body.add(Box.createVerticalStrut(14));
        JLabel heading = new JLabel(titleText, SwingConstants.CENTER);
        heading.setForeground(success ? new Color(24, 104, 72) : new Color(157, 38, 51));
        heading.setFont(new Font("SansSerif", Font.BOLD, 19));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(heading);
        body.add(Box.createVerticalStrut(8));
        JTextArea detail = new JTextArea(detailText == null ? "" : detailText.replace("\\n", "\n"));
        detail.setEditable(false);
        detail.setFocusable(false);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        detail.setFont(new Font("SansSerif", Font.PLAIN, 12));
        detail.setForeground(new Color(76, 89, 112));
        detail.setBackground(tint);
        detail.setBorder(null);
        detail.setRows(2);
        detail.setColumns(38);
        detail.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(detail);
        root.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 14));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, line));
        JButton close = new JButton(success ? "Done" : "Got it");
        styleDialogButton(close, accent, Color.WHITE);
        close.setPreferredSize(new Dimension(150, 42));
        close.addActionListener(e -> dialog.dispose());
        footer.add(close);
        root.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.getRootPane().setDefaultButton(close);
        dialog.pack();
        dialog.setSize(Math.max(430, Math.min(510, dialog.getWidth())), Math.max(245, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    private void showMessage(String message) {
        String lower = message == null ? "" : message.toLowerCase(Locale.ROOT);
        boolean error = lower.contains("could not") || lower.contains("failed")
                || lower.contains("insufficient") || lower.contains("invalid")
                || lower.contains("did not confirm");
        showStyledNotice(error ? "Please check this action" : "SmartBank notification", message, !error);
    }
}
