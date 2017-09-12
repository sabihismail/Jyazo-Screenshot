package tools;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This class allows for logs to be pushed for any {@link Exception} that are caught.
 *
 * @since 1.0
 */
public class Logging {
    /**
     * Creates a {@link JOptionPane} with the error text on the top and a {@link JTextArea} with the
     * {@link Exception#stackTrace}.
     *
     * @param text The error text to assist in debugging.
     * @param e    The {@link Exception} that may also assist in debugging.
     */
    public static void log(String text, Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exception = sw.toString();

        JLabel label = new JLabel("<html>" + text + "</html>");
        label.setVerticalAlignment(JLabel.CENTER);

        JTextArea textArea = new JTextArea(exception);
        textArea.setEditable(false);
        textArea.setLineWrap(true);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JOptionPane.showMessageDialog(null, panel, "Error!", JOptionPane.ERROR_MESSAGE);
    }
}
