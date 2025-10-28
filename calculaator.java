import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

/**
 * Calculator.java
 * Simple but realistic desktop calculator using Java Swing.
 * Supports: +, -, *, /, =, %, sqrt, decimal, clear, backspace, +/- and simple memory (M+, M-, MR, MC).
 *
 * To compile:
 *   javac Calculator.java
 * To run:
 *   java Calculator
 */
public class Calculator extends JFrame implements ActionListener {
    private final JTextField display;
    private double currentValue = 0.0;      // value stored before pressing operator
    private String currentOperator = "";    // "+", "-", "*", "/"
    private boolean startNewNumber = true;  // whether next digit starts a new number
    private double memoryValue = 0.0;       // memory register
    private final DecimalFormat df = new DecimalFormat("0.##########"); // avoid long exponent notation

    public Calculator() {
        super("Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(360, 520);
        setResizable(false);
        setLocationRelativeTo(null); // center window

        // Top panel for display
        display = new JTextField("0");
        display.setFont(new Font("SansSerif", Font.PLAIN, 28));
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setEditable(false);
        display.setBackground(Color.WHITE);
        display.setPreferredSize(new Dimension(340, 70));

        // Buttons layout
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 4, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] buttons = {
            "MC", "MR", "M+", "M-",
            "←", "C", "√", "/",
            "7", "8", "9", "*",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "±", "0", ".", "="
        };

        for (String text : buttons) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("SansSerif", Font.BOLD, 18));
            btn.addActionListener(this);
            panel.add(btn);
        }

        // Put display at top and buttons below
        getContentPane().setLayout(new BorderLayout(6, 6));
        getContentPane().add(display, BorderLayout.NORTH);
        getContentPane().add(panel, BorderLayout.CENTER);

        // Key bindings to support keyboard input
        setupKeyBindings();

        setVisible(true);
    }

    private void setupKeyBindings() {
        // Map key presses to button actions by binding keys to the root pane's input map
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();

        String keys = "0123456789.+-*/=%\n\b"; // note: \n for Enter, \b for Backspace
        for (char k : keys.toCharArray()) {
            String keyStr = String.valueOf(k);
            im.put(KeyStroke.getKeyStroke(k), "key-" + keyStr);
            am.put("key-" + keyStr, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleKey(keyStr);
                }
            });
        }
        // Enter
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "key-=");
        am.put("key-=", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { handleOperator("="); }
        });
        // Backspace
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "key-back");
        am.put("key-back", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { backspace(); }
        });
    }

    private void handleKey(String k) {
        switch (k) {
            case "+":
            case "-":
            case "*":
            case "/":
                handleOperator(k);
                break;
            case ".":
                appendDecimal();
                break;
            case "\n":
                computeResult();
                break;
            case "\b":
                backspace();
                break;
            default:
                if (k.matches("\\d")) appendDigit(k);
                break;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        if (cmd.matches("\\d")) {
            appendDigit(cmd);
        } else if (cmd.equals(".")) {
            appendDecimal();
        } else if (cmd.equals("C")) {
            clearAll();
        } else if (cmd.equals("←")) {
            backspace();
        } else if (cmd.equals("±")) {
            toggleSign();
        } else if (cmd.equals("√")) {
            sqrtOp();
        } else if (cmd.equals("M+")) {
            memoryAdd();
        } else if (cmd.equals("M-")) {
            memorySubtract();
        } else if (cmd.equals("MR")) {
            memoryRecall();
        } else if (cmd.equals("MC")) {
            memoryClear();
        } else if (cmd.equals("=")) {
            computeResult();
        } else if (cmd.equals("%")) {
            percentOp();
        } else if ("+-*/".contains(cmd)) {
            handleOperator(cmd);
        }
    }

    // Append a digit (0-9)
    private void appendDigit(String d) {
        if (startNewNumber) {
            display.setText(d);
            startNewNumber = false;
        } else {
            if (display.getText().equals("0")) display.setText(d);
            else display.setText(display.getText() + d);
        }
    }

    // Append decimal point
    private void appendDecimal() {
        if (startNewNumber) {
            display.setText("0.");
            startNewNumber = false;
            return;
        }
        if (!display.getText().contains(".")) {
            display.setText(display.getText() + ".");
        }
    }

    // Clear everything
    private void clearAll() {
        display.setText("0");
        currentValue = 0.0;
        currentOperator = "";
        startNewNumber = true;
    }

    private void backspace() {
        if (startNewNumber) return;
        String s = display.getText();
        if (s.length() <= 1) {
            display.setText("0");
            startNewNumber = true;
        } else {
            display.setText(s.substring(0, s.length() - 1));
        }
    }

    private void toggleSign() {
        try {
            double v = Double.parseDouble(display.getText());
            v = -v;
            display.setText(df.format(v));
        } catch (NumberFormatException ex) {
            display.setText("0");
        }
    }

    private void sqrtOp() {
        try {
            double v = Double.parseDouble(display.getText());
            if (v < 0) {
                display.setText("Error");
                startNewNumber = true;
                return;
            }
            display.setText(df.format(Math.sqrt(v)));
            startNewNumber = true;
        } catch (NumberFormatException ex) {
            display.setText("0");
        }
    }

    private void percentOp() {
        try {
            double v = Double.parseDouble(display.getText());
            display.setText(df.format(v / 100.0));
            startNewNumber = true;
        } catch (NumberFormatException ex) {
            display.setText("0");
        }
    }

    // Memory operations
    private void memoryAdd() {
        try {
            memoryValue += Double.parseDouble(display.getText());
        } catch (NumberFormatException ignored) {}
        startNewNumber = true;
    }

    private void memorySubtract() {
        try {
            memoryValue -= Double.parseDouble(display.getText());
        } catch (NumberFormatException ignored) {}
        startNewNumber = true;
    }

    private void memoryRecall() {
        display.setText(df.format(memoryValue));
        startNewNumber = true;
    }

    private void memoryClear() {
        memoryValue = 0.0;
    }

    // Handle operator button (+, -, *, /)
    private void handleOperator(String op) {
        try {
            double displayed = Double.parseDouble(display.getText());
            if (!currentOperator.isEmpty()) {
                // If there was a previous operator, compute it first
                currentValue = compute(currentValue, displayed, currentOperator);
                display.setText(df.format(currentValue));
            } else {
                currentValue = displayed;
            }
        } catch (NumberFormatException ignored) {}

        currentOperator = op;
        startNewNumber = true;
    }

    // Compute when = pressed
    private void computeResult() {
        if (currentOperator.isEmpty()) return;
        try {
            double displayed = Double.parseDouble(display.getText());
            double res = compute(currentValue, displayed, currentOperator);
            display.setText(df.format(res));
            currentValue = res;
            currentOperator = "";
            startNewNumber = true;
        } catch (NumberFormatException ex) {
            display.setText("Error");
            startNewNumber = true;
        }
    }

    // Perform arithmetic with basic checks
    private double compute(double a, double b, String op) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/":
                if (b == 0) {
                    JOptionPane.showMessageDialog(this, "Cannot divide by zero", "Error", JOptionPane.WARNING_MESSAGE);
                    return a; // return previous value unchanged
                }
                return a / b;
            default: return b;
        }
    }

    public static void main(String[] args) {
        // Set a simple native-like look if available
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        SwingUtilities.invokeLater(Calculator::new);
    }
}
