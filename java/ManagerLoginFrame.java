
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManagerLoginFrame extends JFrame {

    private final Color navy = new Color(10, 24, 48);
    private final Color cyan = new Color(60, 220, 245);

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton loginButton = new JButton("Secure Manager Login");

    public ManagerLoginFrame() {
        setTitle("SmartBank | Manager Login");
        setSize(520, 470);
        setMinimumSize(new Dimension(450, 420));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(navy);
        background.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(19, 39, 65));
        panel.setBorder(new EmptyBorder(28, 30, 28, 30));

        JLabel title = new JLabel("MANAGER ACCESS");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("SMARTBANK SECURITY CENTER");
        subtitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        subtitle.setForeground(cyan);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel usernameLabel = makeLabel("Username");
        JLabel passwordLabel = makeLabel("Password");

        styleField(usernameField);
        styleField(passwordField);

        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setBackground(new Color(35, 105, 220));
        loginButton.setForeground(Color.WHITE);
        loginButton.setOpaque(true);
        loginButton.setFocusPainted(false);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginButton.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR));

        JButton backButton = new JButton("Back to Welcome");
        backButton.setForeground(cyan);
        backButton.setBackground(new Color(19, 39, 65));
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginButton.addActionListener(e -> login());
        passwordField.addActionListener(e -> login());

        backButton.addActionListener(e -> {
            dispose();
            new WelcomeFrame().setVisible(true);
        });

        panel.add(title);
        panel.add(Box.createVerticalStrut(8));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(28));
        panel.add(usernameLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(usernameField);
        panel.add(Box.createVerticalStrut(16));
        panel.add(passwordLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(24));
        panel.add(loginButton);
        panel.add(Box.createVerticalStrut(12));
        panel.add(backButton);

        background.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        background.add(panel, gbc);
        setContentPane(background);
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 15));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setPreferredSize(new Dimension(300, 38));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        usernameField.setPreferredSize(new Dimension(285, 42));
        passwordField.setPreferredSize(new Dimension(285, 42));

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter both username and password.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Verifying...");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return ManagerApi.login(username, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                loginButton.setText("Secure Manager Login");

                try {
                    String response = get();

                    if (!response.contains("\"success\":true")
                            && !response.contains("\"success\": true")) {
                        JOptionPane.showMessageDialog(
                                ManagerLoginFrame.this,
                                "Manager login failed. Check your credentials.\n"
                                        + response,
                                "Access Denied",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    String token = readJson(response, "token");

                    if (token.isBlank()) {
                        JOptionPane.showMessageDialog(
                                ManagerLoginFrame.this,
                                "The API did not return a session token.\n"
                                        + "Check manager_login.php.",
                                "Login Response Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    dispose();
                    new ManagerDashboardFrame(token).setVisible(true);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ManagerLoginFrame.this,
                            "Could not connect to the manager API.\n"
                                    + ex.getMessage(),
                            "Connection Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private String readJson(String json, String key) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key)
                        + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
        );

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            return "";
        }

        return matcher.group(1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
