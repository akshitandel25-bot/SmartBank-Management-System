import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.*;
import java.util.regex.*;

/** Standalone Monthly Expenses page embedded in DashboardFrame CardLayout. */
public class MonthlyExpensesPanel extends JPanel {
    private static final String API = "http://localhost/smartbank-api/";
    private static final String[] CATEGORIES = {"Groceries", "Shopping", "Food & dining", "Transport", "Bills", "Other"};
    private final int accountId;
    private final Color ink = new Color(39,39,55), muted = new Color(111,112,133), purple = new Color(119,93,215);
    private final JTextField budgetField = new JTextField("1000", 9);
    private final JTextField amountField = new JTextField(9);
    private final JTextField descriptionField = new JTextField(14);
    private final JComboBox<String> budgetCategory = new JComboBox<>(CATEGORIES);
    private final JComboBox<String> expenseCategory = new JComboBox<>(CATEGORIES);
    private final JLabel monthLabel = new JLabel();
    private final JLabel totalSpent = new JLabel("₹0.00"), totalBudget = new JLabel("₹0.00"), remaining = new JLabel("₹0.00"), status = new JLabel("Strict Mode is enabled for categories with a saved cap.");
    private final BudgetChart chart = new BudgetChart();
    private final DefaultTableModel tableModel = new DefaultTableModel(new String[]{"Category","Spent","Budget","Remaining","Status"},0) { public boolean isCellEditable(int r,int c){return false;} };
    private YearMonth month = YearMonth.now();
    private final Map<String,Double> spentMap = new LinkedHashMap<>(), budgetMap = new LinkedHashMap<>();
    private final Set<String> configured = new HashSet<>();

    public MonthlyExpensesPanel(int accountId) {
        this.accountId = accountId;
        setOpaque(false); setLayout(new BorderLayout(0,18)); setBorder(new EmptyBorder(28,30,28,30));
        add(buildHeader(), BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(0,16)); body.setOpaque(false);
        body.add(buildMetrics(), BorderLayout.NORTH);
        JPanel lower = new JPanel(new GridLayout(1,2,16,0)); lower.setOpaque(false);
        lower.add(buildBudgetCard()); lower.add(buildExpenseCard());
        body.add(lower, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new GridLayout(1,2,16,0)); bottom.setOpaque(false);
        bottom.add(buildTableCard()); bottom.add(buildChartCard());
        JPanel content = new JPanel(new BorderLayout(0,16)); content.setOpaque(false); content.add(body,BorderLayout.NORTH); content.add(bottom,BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
        refreshData();
    }

    private JPanel buildHeader() {
        JPanel p=new JPanel(new BorderLayout(12,8));p.setOpaque(false);
        JPanel left=new JPanel();left.setOpaque(false);left.setLayout(new BoxLayout(left,BoxLayout.Y_AXIS));
        JLabel kicker=label("PERSONAL FINANCE  /  SMARTSPEND",new Color(88,111,210),10,true);
        JLabel title=label("Monthly Expenses",ink,29,true);
        JLabel sub=label("Track categories, set spending limits, and stay within your monthly plan.",muted,13,false);
        left.add(kicker);left.add(Box.createVerticalStrut(7));left.add(title);left.add(Box.createVerticalStrut(5));left.add(sub);p.add(left,BorderLayout.WEST);
        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));right.setOpaque(false);
        JButton prev=button("‹",false), next=button("›",false), refresh=button("↻ Refresh",true);
        monthLabel.setFont(new Font("SansSerif",Font.BOLD,12));monthLabel.setForeground(ink);updateMonthLabel();
        prev.addActionListener(e->{month=month.minusMonths(1);updateMonthLabel();refreshData();});next.addActionListener(e->{month=month.plusMonths(1);updateMonthLabel();refreshData();});refresh.addActionListener(e->refreshData());
        right.add(prev);right.add(monthLabel);right.add(next);right.add(refresh);p.add(right,BorderLayout.EAST);return p;
    }
    private JPanel buildMetrics(){JPanel p=new JPanel(new GridLayout(1,3,14,0));p.setOpaque(false);p.setPreferredSize(new Dimension(100,112));p.add(metric("MONTHLY SPENDING",totalSpent,"Completed expenses",new Color(221,112,89)));p.add(metric("TOTAL BUDGET",totalBudget,"Configured category caps",purple));p.add(metric("BUDGET REMAINING",remaining,"Across configured categories",new Color(32,157,121)));return p;}
    private JPanel metric(String title,JLabel value,String note,Color accent){JPanel c=card();c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS));c.add(label(title,muted,10,true));c.add(Box.createVerticalStrut(12));value.setFont(new Font("SansSerif",Font.BOLD,24));value.setForeground(accent);c.add(value);c.add(Box.createVerticalStrut(6));c.add(label(note,muted,11,false));return c;}
    private JPanel buildBudgetCard(){JPanel p=card();p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.add(label("Set a monthly category budget",ink,16,true));p.add(Box.createVerticalStrut(7));p.add(label("Strict Mode blocks an expense that would exceed a saved cap.",muted,12,false));p.add(Box.createVerticalStrut(16));
        JPanel row=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));row.setOpaque(false);row.add(label("Category",muted,12,true));row.add(budgetCategory);row.add(label("Cap (₹)",muted,12,true));styleField(budgetField);row.add(budgetField);p.add(row);p.add(Box.createVerticalStrut(12));
        JButton save=button("Save budget",true);save.addActionListener(e->saveBudget());JPanel br=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));br.setOpaque(false);br.add(save);p.add(br);p.add(Box.createVerticalGlue());status.setFont(new Font("SansSerif",Font.PLAIN,11));status.setForeground(new Color(31,139,111));status.setAlignmentX(LEFT_ALIGNMENT);p.add(status);return p;}
    private JPanel buildExpenseCard(){JPanel p=card();p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.add(label("Record an expense",ink,16,true));p.add(Box.createVerticalStrut(7));p.add(label("This saves a Withdrawal transaction and updates the demo balance.",muted,12,false));p.add(Box.createVerticalStrut(15));
        JPanel row1=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));row1.setOpaque(false);row1.add(label("Category",muted,12,true));row1.add(expenseCategory);p.add(row1);p.add(Box.createVerticalStrut(10));
        JPanel row2=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));row2.setOpaque(false);row2.add(label("Description",muted,12,true));styleField(descriptionField);row2.add(descriptionField);p.add(row2);p.add(Box.createVerticalStrut(10));
        JPanel row3=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));row3.setOpaque(false);row3.add(label("Amount (₹)",muted,12,true));styleField(amountField);row3.add(amountField);p.add(row3);p.add(Box.createVerticalStrut(12));
        JButton add=button("＋ Add expense",true);add.addActionListener(e->saveExpense());JPanel ar=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));ar.setOpaque(false);ar.add(add);p.add(ar);p.add(Box.createVerticalGlue());return p;}
    private JPanel buildTableCard(){JPanel p=card();p.setLayout(new BorderLayout(0,12));p.add(label("Category breakdown",ink,16,true),BorderLayout.NORTH);JTable t=new JTable(tableModel);t.setRowHeight(36);t.setFont(new Font("SansSerif",Font.PLAIN,12));t.setForeground(ink);t.setGridColor(new Color(235,235,245));t.setFillsViewportHeight(true);t.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,11));t.getTableHeader().setBackground(new Color(246,245,252));t.getTableHeader().setForeground(muted);p.add(new JScrollPane(t),BorderLayout.CENTER);return p;}
    private JPanel buildChartCard(){JPanel p=card();p.setLayout(new BorderLayout(0,12));p.add(label("Spending by category",ink,16,true),BorderLayout.NORTH);p.add(chart,BorderLayout.CENTER);p.add(label("Chart shows recorded spending versus saved budget caps.",muted,11,false),BorderLayout.SOUTH);return p;}
    private JPanel card(){JPanel p=new JPanel();p.setBackground(new Color(255,255,255,238));p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(230,228,242)),new EmptyBorder(18,18,18,18)));p.setOpaque(true);return p;}
    private JLabel label(String text,Color c,int size,boolean bold){JLabel l=new JLabel(text);l.setForeground(c);l.setFont(new Font("SansSerif",bold?Font.BOLD:Font.PLAIN,size));return l;}
    private JButton button(String text,boolean primary){JButton b=new JButton(text);b.setFont(new Font("SansSerif",Font.BOLD,12));b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.setBorder(new EmptyBorder(10,14,10,14));b.setBackground(primary?purple:Color.WHITE);b.setForeground(primary?Color.WHITE:ink);b.setOpaque(true);return b;}
    private void styleField(JTextField f){f.setFont(new Font("SansSerif",Font.PLAIN,12));f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(215,213,232)),new EmptyBorder(8,9,8,9)));}
    private void updateMonthLabel(){monthLabel.setText(month.getMonth().getDisplayName(java.time.format.TextStyle.SHORT,Locale.ENGLISH)+" "+month.getYear());}

    private void refreshData(){status.setText("Loading monthly report…");status.setForeground(muted);new SwingWorker<String,Void>(){protected String doInBackground()throws Exception{return request("GET","get_monthly_expenses.php?account_id="+accountId+"&month="+month.toString(),null);}protected void done(){try{String json=get();if(!json.contains("\"success\":true"))throw new IOException(jsonMessage(json));parseReport(json);status.setText("Strict Mode is enabled for categories with a saved cap.");status.setForeground(new Color(31,139,111));}catch(Exception ex){status.setText("Could not load report: "+ex.getMessage());status.setForeground(new Color(180,60,70));}}}.execute();}
    private void parseReport(String json){spentMap.clear();budgetMap.clear();configured.clear();tableModel.setRowCount(0);Pattern p=Pattern.compile("\\{\\s*\"category\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"spent\"\\s*:\\s*([0-9.]+)\\s*,\\s*\"budget\"\\s*:\\s*([0-9.]+)\\s*,\\s*\"remaining\"\\s*:\\s*([0-9.]+)\\s*,\\s*\"has_budget\"\\s*:\\s*(true|false)");Matcher m=p.matcher(json);double spentTotal=0,budgetTotal=0;
        while(m.find()){String c=m.group(1);double s=Double.parseDouble(m.group(2)),b=Double.parseDouble(m.group(3)),r=Double.parseDouble(m.group(4));boolean has=Boolean.parseBoolean(m.group(5));spentMap.put(c,s);budgetMap.put(c,b);if(has)configured.add(c);spentTotal+=s;if(has)budgetTotal+=b;String st=!has?"No cap":s>b?"Over budget":(b>0&&s/b>=0.8)?"Near limit":"On track";tableModel.addRow(new Object[]{c,money(s),has?money(b):"—",has?money(r):"—",st});}
        totalSpent.setText(money(spentTotal));totalBudget.setText(money(budgetTotal));remaining.setText(money(Math.max(0,budgetTotal-spentTotal)));chart.repaint();}
    private void saveBudget(){String category=(String)budgetCategory.getSelectedItem();double val;try{val=Double.parseDouble(budgetField.getText().trim());if(!Double.isFinite(val)||val<0)throw new NumberFormatException();}catch(Exception e){JOptionPane.showMessageDialog(this,"Enter a valid non-negative budget amount.","Invalid budget",JOptionPane.WARNING_MESSAGE);return;}Map<String,String> data=new LinkedHashMap<>();data.put("account_id",String.valueOf(accountId));data.put("category",category);data.put("month",month.toString());data.put("budget_amount",String.valueOf(val));runPost("save_category_budget.php",data,"Budget saved successfully.");}
    private void saveExpense(){String category=(String)expenseCategory.getSelectedItem();String desc=descriptionField.getText().trim();double amount;try{amount=Double.parseDouble(amountField.getText().trim());if(!Double.isFinite(amount)||amount<=0)throw new NumberFormatException();}catch(Exception e){JOptionPane.showMessageDialog(this,"Enter a valid amount greater than ₹0.","Invalid amount",JOptionPane.WARNING_MESSAGE);return;}if(desc.isEmpty())desc=category;Map<String,String> data=new LinkedHashMap<>();data.put("account_id",String.valueOf(accountId));data.put("category",category);data.put("description",desc);data.put("amount",String.valueOf(amount));runPost("save_monthly_expense.php",data,"Expense saved successfully.");}
    private void runPost(String endpoint,Map<String,String> data,String successText){status.setText("Saving…");new SwingWorker<String,Void>(){protected String doInBackground()throws Exception{return request("POST",endpoint,encode(data));}protected void done(){try{String json=get();if(!json.contains("\"success\":true")){String msg=jsonMessage(json);boolean blocked=json.contains("\"budget_blocked\":true");JOptionPane.showMessageDialog(MonthlyExpensesPanel.this,msg,blocked?"Strict Mode — Expense blocked":"SmartBank",blocked?JOptionPane.WARNING_MESSAGE:JOptionPane.ERROR_MESSAGE);status.setText(blocked?"Expense blocked by Strict Mode.":msg);status.setForeground(new Color(180,60,70));return;}JOptionPane.showMessageDialog(MonthlyExpensesPanel.this,successText,"SmartBank",JOptionPane.INFORMATION_MESSAGE);amountField.setText("");descriptionField.setText("");refreshData();}catch(Exception ex){status.setText("Request failed: "+ex.getMessage());status.setForeground(new Color(180,60,70));JOptionPane.showMessageDialog(MonthlyExpensesPanel.this,"Could not reach the SmartBank API. Check Apache and the smartbank-api folder.\n"+ex.getMessage(),"Connection error",JOptionPane.ERROR_MESSAGE);}}}.execute();}
    private String request(String method,String path,String body)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(API+path).openConnection();c.setRequestMethod(method);c.setConnectTimeout(5000);c.setReadTimeout(8000);if("POST".equals(method)){c.setDoOutput(true);c.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");try(OutputStream os=c.getOutputStream()){os.write((body==null?"":body).getBytes(StandardCharsets.UTF_8));}}InputStream is=(c.getResponseCode()<400)?c.getInputStream():c.getErrorStream();ByteArrayOutputStream out=new ByteArrayOutputStream();is.transferTo(out);c.disconnect();return out.toString(StandardCharsets.UTF_8);}
    private String encode(Map<String,String> map)throws Exception{StringJoiner j=new StringJoiner("&");for(Map.Entry<String,String>e:map.entrySet())j.add(URLEncoder.encode(e.getKey(),"UTF-8")+"="+URLEncoder.encode(e.getValue(),"UTF-8"));return j.toString();}
    private String jsonMessage(String json) {
        Matcher m = Pattern.compile("\\\"message\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"").matcher(json);
        if (!m.find()) {
            return "The API returned an error.";
        }
        return decodeJsonString(m.group(1));
    }

    /** Decode JSON escapes so messages display ₹ rather than literal \\u20b9. */
    private String decodeJsonString(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch != '\\' || i + 1 >= value.length()) {
                result.append(ch);
                continue;
            }
            char escaped = value.charAt(++i);
            switch (escaped) {
                case '"': result.append('"'); break;
                case '\\': result.append('\\'); break;
                case '/': result.append('/'); break;
                case 'b': result.append('\b'); break;
                case 'f': result.append('\f'); break;
                case 'n': result.append('\n'); break;
                case 'r': result.append('\r'); break;
                case 't': result.append('\t'); break;
                case 'u':
                    if (i + 4 < value.length()) {
                        String hex = value.substring(i + 1, i + 5);
                        try {
                            result.append((char) Integer.parseInt(hex, 16));
                            i += 4;
                        } catch (NumberFormatException ex) {
                            result.append("\\u").append(hex);
                            i += 4;
                        }
                    } else {
                        result.append("\\u");
                    }
                    break;
                default: result.append(escaped);
            }
        }
        return result.toString();
    }
    private String money(double d){return String.format(Locale.US,"₹%,.2f",d);}
    private class BudgetChart extends JPanel{BudgetChart(){setOpaque(false);setPreferredSize(new Dimension(350,245));}protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int y=12;int maxW=Math.max(120,getWidth()-150);for(String c:CATEGORIES){double s=spentMap.getOrDefault(c,0.0),b=budgetMap.getOrDefault(c,0.0);g2.setColor(ink);g2.setFont(new Font("SansSerif",Font.PLAIN,11));g2.drawString(c,0,y+10);g2.setColor(new Color(239,237,248));g2.fillRoundRect(112,y,maxW,13,8,8);if(b>0){int bw=(int)(maxW*Math.min(1.0,s/b));g2.setColor(s>b?new Color(221,112,89):purple);g2.fillRoundRect(112,y,bw,13,8,8);}else if(s>0){g2.setColor(new Color(221,112,89));g2.fillRoundRect(112,y,Math.min(maxW,6),13,8,8);}g2.setColor(muted);g2.setFont(new Font("SansSerif",Font.PLAIN,10));String val=money(s);g2.drawString(val,Math.min(116+maxW,getWidth()-70),y+11);y+=31;}g2.dispose();}}
}
