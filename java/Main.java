import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * SmartBank CyberShield login screen.
 * Standard Java Swing only. Keeps the demo authentication: admin / bank123.
 * Successful login opens the existing DashboardFrame.
 */
public class Main extends JFrame {
    private static final String DEMO_USERNAME = "admin";
    private static final String DEMO_PASSWORD = "bank123";
    private static final Color CYAN = new Color(43, 226, 255);
    private static final Color DEEP = new Color(7, 17, 42);
    private static final Preferences PREFS = Preferences.userNodeForPackage(Main.class);

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox rememberCheck;
    private JButton signInButton;
    private JLabel messageLabel;
    private boolean passwordVisible = false;
    private char passwordEcho;
    private float glow = 0.35f;
    private boolean glowUp = true;
    private Timer animationTimer;
    private Timer fadeTimer;
    private float fade = 0f;
    private boolean loggingIn = false;

    public Main() {
        super("SmartBank | CyberShield Secure Banking");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(900, 590));
        setLocationRelativeTo(null);
        setContentPane(buildUI());
        getRootPane().setDefaultButton(signInButton);
        passwordEcho = passwordField.getEchoChar();
        loadRememberedUsername();
        installAnimations();
        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) { startFadeIn(); }
            @Override public void windowClosed(WindowEvent e) {
                if (animationTimer != null) animationTimer.stop();
                if (fadeTimer != null) fadeTimer.stop();
            }
        });
        SwingUtilities.invokeLater(() -> {
            if (usernameField.getText().trim().isEmpty()) usernameField.requestFocusInWindow();
            else passwordField.requestFocusInWindow();
        });
    }

    private JComponent buildUI() {
        CyberBackground background = new CyberBackground();
        background.setLayout(new GridBagLayout());
        background.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel shell = new RoundedShell(new GridLayout(1, 2, 0, 0));
        shell.setOpaque(false);
        shell.setPreferredSize(new Dimension(1120, 650));
        shell.setMaximumSize(new Dimension(1200, 720));
        shell.setBorder(new EmptyBorder(1, 1, 1, 1));

        // Left-side cybersecurity identity panel.
        JPanel brand = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(239, 244, 255), getWidth(), getHeight(), new Color(218, 232, 255)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                g2.setColor(new Color(72, 105, 255, 22));
                g2.fillOval(-100, getHeight() - 240, 340, 340);
                g2.setColor(new Color(66, 190, 255, 28));
                g2.fillOval(getWidth() - 180, -120, 280, 280);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(new EmptyBorder(30, 30, 28, 26));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        logoRow.setOpaque(false);
        ShieldMark shield = new ShieldMark(52);
        logoRow.add(shield);
        JPanel brandWords = new JPanel();
        brandWords.setOpaque(false);
        brandWords.setLayout(new BoxLayout(brandWords, BoxLayout.Y_AXIS));
        JLabel brandName = new JLabel("SMARTBANK");
        brandName.setFont(new Font("SansSerif", Font.BOLD, 25));
        brandName.setForeground(new Color(28, 42, 88));
        JLabel brandTag = new JLabel("CYBERSHIELD  /  DIGITAL BANKING");
        brandTag.setFont(new Font("SansSerif", Font.BOLD, 9));
        brandTag.setForeground(new Color(54, 111, 205));
        brandWords.add(brandName);
        brandWords.add(Box.createVerticalStrut(4));
        brandWords.add(brandTag);
        logoRow.add(brandWords);
        logoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(logoRow);
        brand.add(Box.createVerticalGlue());

        JLabel eyebrow = new JLabel("YOUR MONEY. YOUR CONTROL.");
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 11));
        eyebrow.setForeground(new Color(47, 117, 225));
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(eyebrow);
        brand.add(Box.createVerticalStrut(18));

        JLabel hero = new JLabel("<html><div style='width:390px'>Security that<br>moves with you.</div></html>");
        hero.setFont(new Font("SansSerif", Font.BOLD, 32));
        hero.setForeground(new Color(31, 45, 91));
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(hero);
        brand.add(Box.createVerticalStrut(17));

        JLabel desc = new JLabel("<html><div style='width:390px; line-height:1.5'>A smarter banking experience with transaction insights,<br>activity monitoring and a security-first mindset.</div></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 14));
        desc.setForeground(new Color(88, 106, 151));
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(desc);
        brand.add(Box.createVerticalStrut(30));

        brand.add(securityFeature("◈", "SMART TRANSACTION MONITORING", "Review activity and spot unusual patterns."));
        brand.add(Box.createVerticalStrut(16));
        brand.add(securityFeature("⌁", "PRIVACY-FIRST EXPERIENCE", "Your demo environment stays clearly labelled."));
        brand.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("●  CYBERSHIELD ACTIVE     •     DEMO ENVIRONMENT");
        footer.setFont(new Font("SansSerif", Font.BOLD, 10));
        footer.setForeground(new Color(48, 117, 208));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(footer);

        // Right-side login panel.
        JPanel loginSide = new JPanel(new GridBagLayout());
        loginSide.setOpaque(false);
        loginSide.setBackground(new Color(255, 255, 255));
        loginSide.setBorder(new EmptyBorder(26, 32, 26, 32));
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setMaximumSize(new Dimension(340, 470));
        form.setPreferredSize(new Dimension(340, 470));

        JLabel welcome = new JLabel("SECURE ACCESS");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 11));
        welcome.setForeground(new Color(49, 119, 226));
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(welcome);
        form.add(Box.createVerticalStrut(10));

        JLabel title = new JLabel("Welcome back");
        title.setFont(new Font("SansSerif", Font.BOLD, 29));
        title.setForeground(new Color(28, 39, 83));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(8));

        JLabel subtitle = new JLabel("Sign in to your SmartBank account");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(103, 119, 157));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(subtitle);
        form.add(Box.createVerticalStrut(24));

        form.add(fieldLabel("USERNAME"));
        form.add(Box.createVerticalStrut(8));
        usernameField = new RoundedTextField();
        styleTextField(usernameField);
        usernameField.setToolTipText("Enter your demo username");
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());
        form.add(usernameField);
        form.add(Box.createVerticalStrut(14));

        form.add(fieldLabel("PASSWORD"));
        form.add(Box.createVerticalStrut(8));
        JPanel passwordRow = new JPanel(new BorderLayout(8, 0));
        passwordRow.setOpaque(false);
        passwordField = new RoundedPasswordField();
        styleTextField(passwordField);
        passwordField.setToolTipText("Enter your password");
        passwordField.addActionListener(e -> performLogin());
        JButton showPassword = new JButton("SHOW");
        showPassword.setFont(new Font("SansSerif", Font.BOLD, 10));
        showPassword.setForeground(new Color(55, 100, 191));
        showPassword.setBackground(new Color(239, 245, 255));
        showPassword.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(190, 207, 238), 16), new EmptyBorder(0, 12, 0, 12)));
        showPassword.setFocusPainted(false);
        showPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        showPassword.addActionListener(e -> {
            passwordVisible = !passwordVisible;
            passwordField.setEchoChar(passwordVisible ? (char) 0 : passwordEcho);
            showPassword.setText(passwordVisible ? "HIDE" : "SHOW");
            passwordField.requestFocusInWindow();
        });
        passwordRow.add(passwordField, BorderLayout.CENTER);
        passwordRow.add(showPassword, BorderLayout.EAST);
        passwordRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        passwordRow.setPreferredSize(new Dimension(390, 48));
        passwordRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(passwordRow);
        form.add(Box.createVerticalStrut(12));

        rememberCheck = new JCheckBox("Remember username on this computer");
        rememberCheck.setOpaque(false);
        rememberCheck.setForeground(new Color(91, 107, 148));
        rememberCheck.setFont(new Font("SansSerif", Font.PLAIN, 11));
        rememberCheck.setFocusPainted(false);
        rememberCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        rememberCheck.setSelected(PREFS.getBoolean("rememberUsername", false));
        form.add(rememberCheck);
        form.add(Box.createVerticalStrut(16));

        signInButton = new GlowButton();
        signInButton.setText("SECURE SIGN IN     →");
        signInButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        signInButton.setForeground(Color.WHITE);
        signInButton.setFocusPainted(false);
        signInButton.setBorder(new EmptyBorder(13, 18, 13, 18));
        signInButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signInButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        signInButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        signInButton.addActionListener(e -> performLogin());
        form.add(signInButton);
        form.add(Box.createVerticalStrut(14));

        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        messageLabel.setForeground(new Color(193, 55, 76));
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(messageLabel);
        form.add(Box.createVerticalGlue());

        JPanel demo = new JPanel(new BorderLayout(10, 0));
        demo.setOpaque(false);
        demo.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(203, 216, 242), 18), new EmptyBorder(12, 13, 12, 13)));
        JLabel demoIcon = new JLabel("ⓘ");
        demoIcon.setFont(new Font("SansSerif", Font.BOLD, 18));
        demoIcon.setForeground(new Color(55, 116, 220));
        demo.add(demoIcon, BorderLayout.WEST);
        JLabel demoText = new JLabel("<html><b style='color:#2c467f'>DEMO ACCESS</b><br><span style='color:#6377a6'>Username: admin &nbsp; / &nbsp; Password: bank123</span></html>");
        demoText.setFont(new Font("SansSerif", Font.PLAIN, 11));
        demo.add(demoText, BorderLayout.CENTER);
        demo.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(demo);

        loginSide.add(form);
        shell.add(brand);
        shell.add(loginSide);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0; gc.weighty = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(18, 18, 18, 18);
        background.add(shell, gc);

        // Keep the rounded login shell at a comfortable medium size when maximized.
        // It grows with smaller/larger windows but is capped so the form never looks stretched.
        background.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int availableWidth = Math.max(820, background.getWidth() - 64);
                int availableHeight = Math.max(500, background.getHeight() - 64);
                int shellWidth = Math.min(1200, availableWidth);
                int shellHeight = Math.min(720, availableHeight);
                shell.setPreferredSize(new Dimension(shellWidth, shellHeight));
                shell.revalidate();
                background.revalidate();
            }
        });
        return background;
    }

    private JPanel securityFeature(String symbol, String heading, String detail) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel icon = new JLabel(symbol, SwingConstants.CENTER);
        icon.setFont(new Font("SansSerif", Font.BOLD, 20));
        icon.setForeground(CYAN);
        icon.setPreferredSize(new Dimension(32, 42));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel h = new JLabel(heading);
        h.setFont(new Font("SansSerif", Font.BOLD, 10));
        h.setForeground(new Color(43, 65, 116));
        JLabel d = new JLabel(detail);
        d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        d.setForeground(new Color(94, 115, 158));
        text.add(h); text.add(Box.createVerticalStrut(5)); text.add(d);
        row.add(icon, BorderLayout.WEST); row.add(text, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        return row;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setForeground(new Color(76, 96, 143));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(new Color(34, 48, 91));
        field.setCaretColor(new Color(65, 104, 235));
        field.setBackground(Color.WHITE);
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(0, 14, 0, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setPreferredSize(new Dimension(300, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void loadRememberedUsername() {
        boolean remember = PREFS.getBoolean("rememberUsername", false);
        rememberCheck.setSelected(remember);
        if (remember) usernameField.setText(PREFS.get("savedUsername", ""));
    }

    private void performLogin() {
        if (loggingIn) return;
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty()) {
            showMessage("Please enter your username.", false);
            usernameField.requestFocusInWindow();
            return;
        }
        if (password.isEmpty()) {
            showMessage("Please enter your password.", false);
            passwordField.requestFocusInWindow();
            return;
        }

        loggingIn = true;
        signInButton.setEnabled(false);
        signInButton.setText("VERIFYING SECURE ACCESS…");
        showMessage("Checking credentials…", true);

        // Short UI feedback delay for the animated button; authentication remains local demo auth.
        Timer verifyTimer = new Timer(450, e -> {
            ((Timer) e.getSource()).stop();
            if (DEMO_USERNAME.equals(username) && DEMO_PASSWORD.equals(password)) {
                if (rememberCheck.isSelected()) {
                    PREFS.putBoolean("rememberUsername", true);
                    PREFS.put("savedUsername", username);
                } else {
                    PREFS.putBoolean("rememberUsername", false);
                    PREFS.remove("savedUsername");
                }
                showMessage("Access approved. Opening your dashboard…", true);
                Timer openTimer = new Timer(350, ev -> {
                    ((Timer) ev.getSource()).stop();
                    dispose();
                    SwingUtilities.invokeLater(() -> {
                        DashboardFrame dashboard = new DashboardFrame();
                        dashboard.setVisible(true);
                    });
                });
                openTimer.setRepeats(false);
                openTimer.start();
            } else {
                showMessage("Sign-in failed. Check your username and password.", false);
                passwordField.setText("");
                passwordField.requestFocusInWindow();
                loggingIn = false;
                signInButton.setEnabled(true);
                signInButton.setText("SECURE SIGN IN     →");
            }
        });
        verifyTimer.setRepeats(false);
        verifyTimer.start();
    }

    private void showMessage(String message, boolean success) {
        messageLabel.setText(message);
        // Keep status text readable against the white login form.
        messageLabel.setForeground(success ? new Color(22, 132, 91) : new Color(190, 55, 73));
    }

    private void installAnimations() {
        animationTimer = new Timer(35, e -> {
            glow += glowUp ? 0.025f : -0.025f;
            if (glow >= 1f) glowUp = false;
            if (glow <= 0.28f) glowUp = true;
            repaint();
        });
        animationTimer.start();
    }

    private void startFadeIn() {
        try { setOpacity(0f); } catch (UnsupportedOperationException | IllegalComponentStateException ignored) { return; }
        fade = 0f;
        fadeTimer = new Timer(25, e -> {
            fade = Math.min(1f, fade + 0.07f);
            try { setOpacity(fade); } catch (Exception ignored) { ((Timer)e.getSource()).stop(); return; }
            if (fade >= 1f) ((Timer)e.getSource()).stop();
        });
        fadeTimer.start();
    }

    /** Rounded light input background with a subtle blue outline. */
    private class RoundedTextField extends JTextField {
        RoundedTextField() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
            g2.setColor(new Color(195, 210, 240));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class RoundedPasswordField extends JPasswordField {
        RoundedPasswordField() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
            g2.setColor(new Color(195, 210, 240));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedLineBorder extends javax.swing.border.AbstractBorder {
        private final Color color; private final int radius;
        RoundedLineBorder(Color color, int radius) { this.color = color; this.radius = radius; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.drawRoundRect(x, y, w-1, h-1, radius, radius); g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(2, 3, 2, 3); }
    }

    private static class RoundedShell extends JPanel {
        RoundedShell(LayoutManager layout) { super(layout); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255,255,255,228));
            g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,28,28);
            g2.setColor(new Color(255,255,255,220));
            g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,28,28);
            g2.dispose(); super.paintComponent(g);
        }
    }

    private class GlowButton extends JButton {
        GlowButton() { setContentAreaFilled(false); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int alpha = getModel().isRollover() ? 85 : (int)(28 + glow * 32);
            g2.setColor(new Color(92, 135, 255, alpha));
            g2.fillRoundRect(-3, -3, getWidth() + 6, getHeight() + 6, 18, 18);
            Color a = getModel().isRollover() ? new Color(119, 151, 255) : new Color(100, 118, 255);
            Color b = getModel().isRollover() ? new Color(183, 133, 255) : new Color(92, 93, 239);
            g2.setPaint(new GradientPaint(0, 0, a, getWidth(), getHeight(), b));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 13, 13);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class ShieldMark extends JComponent {
        private final int side;
        ShieldMark(int side) { this.side = side; setPreferredSize(new Dimension(side, side)); setMaximumSize(new Dimension(side, side)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Path2D shield = new Path2D.Double();
            shield.moveTo(side * .5, side * .06); shield.lineTo(side * .88, side * .20);
            shield.lineTo(side * .83, side * .62); shield.quadTo(side * .72, side * .86, side * .5, side * .96);
            shield.quadTo(side * .28, side * .86, side * .17, side * .62); shield.lineTo(side * .12, side * .20); shield.closePath();
            g2.setPaint(new GradientPaint(0, 0, new Color(66, 240, 255), side, side, new Color(52, 124, 255)));
            g2.fill(shield);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(7, 35, 63));
            g2.drawLine((int)(side*.32), (int)(side*.51), (int)(side*.45), (int)(side*.64));
            g2.drawLine((int)(side*.45), (int)(side*.64), (int)(side*.70), (int)(side*.37));
            g2.dispose();
        }
    }

    private class CyberBackground extends JPanel {
        private final List<Point2D.Float> nodes = new ArrayList<>();
        private float phase = 0f;
        CyberBackground() {
            setOpaque(true);
            for (int i = 0; i < 45; i++) nodes.add(new Point2D.Float((float)Math.random(), (float)Math.random()));
            new Timer(55, e -> { phase += 0.018f; repaint(); }).start();
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, new Color(13, 20, 42), getWidth(), getHeight(), new Color(31, 43, 76)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            for (int i = 0; i < nodes.size(); i++) {
                Point2D.Float p = nodes.get(i);
                float x = p.x * getWidth();
                float y = p.y * getHeight() + (float)Math.sin(phase + i) * 7;
                for (int j = i + 1; j < nodes.size(); j++) {
                    Point2D.Float q = nodes.get(j);
                    float qx = q.x * getWidth(); float qy = q.y * getHeight() + (float)Math.sin(phase + j) * 7;
                    float distance = (float) Point2D.distance(x, y, qx, qy);
                    if (distance < 115) {
                        int alpha = Math.max(0, (int)(36 * (1 - distance / 115)));
                        g2.setColor(new Color(69, 205, 245, alpha));
                        g2.draw(new Line2D.Float(x, y, qx, qy));
                    }
                }
                g2.setColor(new Color(72, 213, 255, (int)(65 + 65 * (0.5 + 0.5 * Math.sin(phase + i)))));
                g2.fill(new Ellipse2D.Float(x - 2, y - 2, 4, 4));
            }
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
            catch (Exception ignored) { }
            new Main().setVisible(true);
        });
    }
}
