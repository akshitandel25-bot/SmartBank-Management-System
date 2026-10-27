
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class WelcomeFrame extends JFrame {

    private final Color navy = new Color(10, 24, 48);
    private final Color cyan = new Color(60, 220, 245);
    private final Color blue = new Color(45, 95, 230);

    public WelcomeFrame() {
        setTitle("SmartBank | Welcome");
        setSize(650, 500);
        setMinimumSize(new Dimension(550, 450));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(navy);
        background.setBorder(new EmptyBorder(35, 45, 35, 45));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("◈  SMARTBANK");
        logo.setFont(new Font("SansSerif", Font.BOLD, 30));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("DIGITAL BANKING • CYBERSHIELD");
        subtitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        subtitle.setForeground(cyan);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcome = new JLabel("Welcome to SmartBank");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 23));
        welcome.setForeground(Color.WHITE);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel description = new JLabel("Choose how you want to continue");
        description.setFont(new Font("SansSerif", Font.PLAIN, 14));
        description.setForeground(new Color(190, 205, 225));
        description.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton customerButton = makeButton(
                "Customer Login", blue, Color.WHITE);

        JButton managerButton = makeButton(
                "Manager Login", new Color(18, 48, 70), cyan);

        customerButton.addActionListener(e -> {
            dispose();
            new Main().setVisible(true);
        });

        managerButton.addActionListener(e -> {
            dispose();
            new ManagerLoginFrame().setVisible(true);
        });
        
        content.add(logo);
        content.add(Box.createVerticalStrut(8));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(45));
        content.add(welcome);
        content.add(Box.createVerticalStrut(10));
        content.add(description);
        content.add(Box.createVerticalStrut(32));
        content.add(customerButton);
        content.add(Box.createVerticalStrut(14));
        content.add(managerButton);

        background.add(content);
        setContentPane(background);
    }

    private JButton makeButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 15));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(70, 125, 165)),
                new EmptyBorder(13, 20, 13, 20)
        ));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(320, 50));
        button.setPreferredSize(new Dimension(320, 50));
        button.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR));
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new WelcomeFrame().setVisible(true));
    }
}
