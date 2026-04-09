package app.mvc;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Centralizes the visual styling and dialog-building behavior used by the helper
 * windows in the desktop application.
 * <p>
 * This class exposes theme lookups, component stylers, and reusable dialog
 * builders so helper dialogs can stay visually consistent with the main data
 * view in both light mode and dark mode.
 */
public class AppDialogThemeHelper {
    /**
     * Immutable container for all colors used by the helper window theme system.
     * <p>
     * Each instance represents one complete palette for either light mode or
     * dark mode so styling methods can read a single source of truth.
     */
    public static final class Theme {
        public final boolean dark;
        public final Color windowBg;
        public final Color panelBg;
        public final Color cardBg;
        public final Color border;
        public final Color softBorder;
        public final Color buttonBg;
        public final Color buttonHover;
        public final Color buttonPressed;
        public final Color primaryBg;
        public final Color primaryHover;
        public final Color primaryPressed;
        public final Color text;
        public final Color mutedText;
        public final Color accent;
        public final Color scrollTrack;
        public final Color scrollThumb;
        public final Color selection;
        public final Color headerBg;
        public final Color headerText;
        public final Color dividerBar;
        public final Color warningText;
        public final Color errorText;
        public final Color disabledText;
        public final Color lattice;

        private Theme(boolean dark,
                      Color windowBg,
                      Color panelBg,
                      Color cardBg,
                      Color border,
                      Color softBorder,
                      Color buttonBg,
                      Color buttonHover,
                      Color buttonPressed,
                      Color primaryBg,
                      Color primaryHover,
                      Color primaryPressed,
                      Color text,
                      Color mutedText,
                      Color accent,
                      Color scrollTrack,
                      Color scrollThumb,
                      Color selection,
                      Color headerBg,
                      Color headerText,
                      Color dividerBar,
                      Color warningText,
                      Color errorText,
                      Color disabledText,
                      Color lattice) {
            this.dark = dark;
            this.windowBg = windowBg;
            this.panelBg = panelBg;
            this.cardBg = cardBg;
            this.border = border;
            this.softBorder = softBorder;
            this.buttonBg = buttonBg;
            this.buttonHover = buttonHover;
            this.buttonPressed = buttonPressed;
            this.primaryBg = primaryBg;
            this.primaryHover = primaryHover;
            this.primaryPressed = primaryPressed;
            this.text = text;
            this.mutedText = mutedText;
            this.accent = accent;
            this.scrollTrack = scrollTrack;
            this.scrollThumb = scrollThumb;
            this.selection = selection;
            this.headerBg = headerBg;
            this.headerText = headerText;
            this.dividerBar = dividerBar;
            this.warningText = warningText;
            this.errorText = errorText;
            this.disabledText = disabledText;
            this.lattice = lattice;
        }
    }

    private static final Theme LIGHT_THEME = new Theme(
            false,
            new Color(245, 245, 245),
            new Color(240, 240, 240),
            new Color(238, 238, 238, 235),
            new Color(188, 188, 188),
            new Color(205, 205, 205),
            new Color(235, 235, 235),
            new Color(220, 226, 234),
            new Color(205, 212, 222),
            new Color(255, 170, 60),
            new Color(255, 183, 90),
            new Color(232, 150, 45),
            new Color(30, 30, 30),
            new Color(80, 80, 80),
            new Color(255, 140, 0),
            new Color(230, 230, 230),
            new Color(130, 130, 130),
            new Color(170, 170, 170, 205),
            new Color(214, 214, 214),
            new Color(255, 140, 0),
            new Color(118, 118, 118),
            new Color(140, 110, 30),
            new Color(150, 50, 50),
            new Color(120, 120, 120),
            new Color(255, 170, 0, 34)
    );

    private static final Theme DARK_THEME = new Theme(
            true,
            new Color(28, 28, 28),
            new Color(55, 55, 55),
            new Color(48, 48, 48, 238),
            new Color(90, 90, 90),
            new Color(72, 72, 72),
            new Color(55, 55, 55),
            new Color(74, 74, 74),
            new Color(92, 92, 92),
            new Color(255, 140, 0),
            new Color(255, 163, 48),
            new Color(214, 117, 0),
            new Color(235, 235, 235),
            new Color(190, 190, 190),
            new Color(255, 140, 0),
            new Color(38, 38, 38),
            new Color(180, 180, 180),
            new Color(132, 132, 132, 205),
            new Color(60, 60, 60),
            new Color(255, 140, 0),
            new Color(168, 168, 168),
            new Color(230, 200, 120),
            new Color(255, 150, 150),
            new Color(140, 140, 140),
            new Color(255, 140, 0, 28)
    );

    private static final Font TITLE_FONT = new Font("Inter", Font.BOLD, 22);
    private static final Font SUBTITLE_FONT = new Font("Inter", Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font("Inter", Font.BOLD, 13);
    private static final Font BODY_FONT = new Font("Inter", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Inter", Font.BOLD, 13);

    private AppDialogThemeHelper() {
    }

    /**
     * Resolves the active helper dialog theme from the closest owning view.
     *
     * @param parent component whose parent chain is inspected for theme context
     * @return the dark or light helper theme that matches the owning window
     */
    public static Theme getTheme(Component parent) {
        return isDark(parent) ? DARK_THEME : LIGHT_THEME;
    }

    /**
     * Determines whether the helper window should render in dark mode.
     *
     * @param parent component whose ancestors are inspected
     * @return {@code true} when the owning data view is currently in dark mode;
     *         otherwise {@code false}
     */
    public static boolean isDark(Component parent) {
        Component current = parent;
        while (current != null) {
            if (current instanceof DataView dataView) {
                return dataView.isDarkMode();
            }
            current = current.getParent();
        }

        Window owner = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        while (owner != null) {
            if (owner instanceof DataView dataView) {
                return dataView.isDarkMode();
            }
            owner = owner.getOwner();
        }
        return false;
    }

    /**
     * Creates a surface panel using the default light-mode assumption.
     *
     * @param layout layout manager assigned to the created panel
     * @return a themed surface panel
     */
    public static JPanel createSurfacePanel(LayoutManager layout) {
        return createSurfacePanel(layout, false);
    }

    /**
     * Creates a basic themed surface panel.
     *
     * @param layout layout manager assigned to the created panel
     * @param dark whether the dark palette should be used
     * @return a panel styled as a dialog surface
     */
    public static JPanel createSurfacePanel(LayoutManager layout, boolean dark) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        JPanel panel = new JPanel(layout);
        panel.setBackground(theme.windowBg);
        return panel;
    }

    /**
     * Creates a card-style panel using the default light-mode assumption.
     *
     * @param layout layout manager assigned to the created panel
     * @return a themed card panel
     */
    public static JPanel createCardPanel(LayoutManager layout) {
        return createCardPanel(layout, false);
    }

    /**
     * Creates a card-style panel used to visually group dialog content.
     *
     * @param layout layout manager assigned to the created panel
     * @param dark whether the dark palette should be used
     * @return a themed card panel
     */
    public static JPanel createCardPanel(LayoutManager layout, boolean dark) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        JPanel panel = new JPanel(layout);
        panel.setOpaque(true);
        panel.setBackground(theme.cardBg);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, theme.softBorder),
                new EmptyBorder(12, 12, 12, 12)));
        return panel;
    }

    /**
     * Styles a label with the default non-accent light-mode appearance.
     *
     * @param label label to style
     */
    public static void styleLabel(JLabel label) {
        styleLabel(label, false, false);
    }

    /**
     * Styles a label while optionally using the accent color.
     *
     * @param label label to style
     * @param accent whether the accent color should be applied
     */
    public static void styleLabel(JLabel label, boolean accent) {
        styleLabel(label, accent, false);
    }

    /**
     * Styles a label for the requested theme variant.
     *
     * @param label label to style
     * @param accent whether the accent color should be applied
     * @param dark whether the dark palette should be used
     */
    public static void styleLabel(JLabel label, boolean accent, boolean dark) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        label.setFont(LABEL_FONT);
        label.setForeground(accent ? theme.accent : theme.text);
        label.setOpaque(false);
    }

    /**
     * Creates a large section title label using the default light-mode theme.
     *
     * @param text title text to display
     * @return a styled section title label
     */
    public static JLabel createSectionTitle(String text) {
        return createSectionTitle(text, false);
    }

    /**
     * Creates a large section title label for the requested theme.
     *
     * @param text title text to display
     * @param dark whether the dark palette should be used
     * @return a styled section title label
     */
    public static JLabel createSectionTitle(String text, boolean dark) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        JLabel label = new JLabel(text == null ? "" : text.toUpperCase(Locale.ROOT));
        label.setFont(TITLE_FONT);
        label.setForeground(theme.accent);
        return label;
    }

    /**
     * Creates a subtitle label using the default light-mode theme.
     *
     * @param text subtitle text to display
     * @return a styled subtitle label
     */
    public static JLabel createSubtitle(String text) {
        return createSubtitle(text, false);
    }

    /**
     * Creates a subtitle label for the requested theme.
     *
     * @param text subtitle text to display
     * @param dark whether the dark palette should be used
     * @return a styled subtitle label
     */
    public static JLabel createSubtitle(String text, boolean dark) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        JLabel label = new JLabel(text == null ? "" : text.toUpperCase(Locale.ROOT));
        label.setFont(SUBTITLE_FONT);
        label.setForeground(theme.mutedText);
        return label;
    }

    /**
     * Styles a text field using the default light-mode theme.
     *
     * @param field text field to style
     */
    public static void styleTextField(JTextField field) {
        styleTextField(field, false);
    }

    /**
     * Styles a text field for the requested theme.
     *
     * @param field text field to style
     * @param dark whether the dark palette should be used
     */
    public static void styleTextField(JTextField field, boolean dark) {
        styleTextComponent(field, dark);
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        field.setCaretColor(theme.text);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 34));
    }

    /**
     * Styles a password field using the default light-mode theme.
     *
     * @param field password field to style
     */
    public static void stylePasswordField(JPasswordField field) {
        stylePasswordField(field, false);
    }

    /**
     * Styles a password field for the requested theme.
     *
     * @param field password field to style
     * @param dark whether the dark palette should be used
     */
    public static void stylePasswordField(JPasswordField field, boolean dark) {
        styleTextComponent(field, dark);
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        field.setCaretColor(theme.text);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 34));
    }

    /**
     * Styles a text area using the default light-mode theme.
     *
     * @param area text area to style
     */
    public static void styleTextArea(JTextArea area) {
        styleTextArea(area, false);
    }

    /**
     * Styles a text area for the requested theme.
     *
     * @param area text area to style
     * @param dark whether the dark palette should be used
     */
    public static void styleTextArea(JTextArea area, boolean dark) {
        styleTextComponent(area, dark);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setMargin(new Insets(8, 8, 8, 8));
    }

    private static void styleTextComponent(JTextComponent component, boolean dark) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        component.setFont(BODY_FONT);
        component.setForeground(theme.text);
        component.setBackground(theme.panelBg);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, theme.softBorder),
                new EmptyBorder(6, 8, 6, 8)));
    }

    /**
     * Styles a check box using the default light-mode theme.
     *
     * @param box check box to style
     */
    public static void styleCheckBox(JCheckBox box) {
        styleCheckBox(box, false);
    }

    /**
     * Styles a check box for the requested theme.
     *
     * @param box check box to style
     * @param dark whether the dark palette should be used
     */
    public static void styleCheckBox(JCheckBox box, boolean dark) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        box.setOpaque(false);
        box.setForeground(theme.text);
        box.setFont(BODY_FONT);
        box.setFocusPainted(false);
    }

    /**
     * Styles a combo box using the default light-mode theme.
     *
     * @param combo combo box to style
     */
    public static void styleComboBox(JComboBox<?> combo) {
        styleComboBox(combo, false);
    }

    /**
     * Styles a combo box for the requested theme.
     *
     * @param combo combo box to style
     * @param dark whether the dark palette should be used
     */
    public static void styleComboBox(JComboBox<?> combo, boolean dark) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        combo.setFont(BODY_FONT);
        combo.setBackground(theme.panelBg);
        combo.setForeground(theme.text);
        combo.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, theme.softBorder));
        combo.setFocusable(false);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                component.setBackground(isSelected ? theme.selection : theme.panelBg);
                component.setForeground(theme.text);
                if (component instanceof JComponent jc) {
                    jc.setBorder(new EmptyBorder(4, 8, 4, 8));
                    jc.setOpaque(true);
                }
                return component;
            }
        });
    }

    /**
     * Styles a button using the default light-mode theme.
     *
     * @param button button to style
     * @param primary whether the primary action palette should be used
     */
    public static void styleButton(AbstractButton button, boolean primary) {
        styleButton(button, primary, false);
    }

    /**
     * Styles a button for the requested theme and priority.
     *
     * @param button button to style
     * @param primary whether the primary action palette should be used
     * @param dark whether the dark palette should be used
     */
    public static void styleButton(AbstractButton button, boolean primary, boolean dark) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        Color base = primary ? theme.primaryBg : theme.buttonBg;
        Color hover = primary ? theme.primaryHover : theme.buttonHover;
        Color pressed = primary ? theme.primaryPressed : theme.buttonPressed;

        button.setFocusPainted(false);
        button.setFont(BUTTON_FONT);
        button.setForeground(theme.text);
        button.setBackground(base);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.border),
                new EmptyBorder(8, 16, 8, 16)));

        for (var listener : button.getMouseListeners()) {
            if (listener.getClass() == DialogButtonHoverListener.class) {
                button.removeMouseListener(listener);
            }
        }
        button.addMouseListener(new DialogButtonHoverListener(button, base, hover, pressed));
    }

    /**
     * Styles a table using the default light-mode theme.
     *
     * @param table table to style
     */
    public static void styleTable(JTable table) {
        styleTable(table, false);
    }

    /**
     * Styles a table for the requested theme.
     *
     * @param table table to style
     * @param dark whether the dark palette should be used
     */
    public static void styleTable(JTable table, boolean dark) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        table.setFont(BODY_FONT);
        table.setRowHeight(30);
        table.setBackground(theme.panelBg);
        table.setForeground(theme.text);
        table.setGridColor(theme.softBorder);
        table.setShowGrid(true);
        table.setSelectionBackground(theme.selection);
        table.setSelectionForeground(theme.text);
        JTableHeader header = table.getTableHeader();
        header.setFont(LABEL_FONT);
        header.setBackground(theme.headerBg);
        header.setForeground(theme.accent);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, theme.dividerBar));
        header.setReorderingAllowed(false);
    }

    /**
     * Styles a scroll pane using the default light-mode theme.
     *
     * @param scrollPane scroll pane to style
     */
    public static void styleScrollPane(JScrollPane scrollPane) {
        styleScrollPane(scrollPane, false);
    }

    /**
     * Styles a scroll pane for the requested theme.
     *
     * @param scrollPane scroll pane to style
     * @param dark whether the dark palette should be used
     */
    public static void styleScrollPane(JScrollPane scrollPane, boolean dark) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(theme.panelBg);
        scrollPane.getVerticalScrollBar().setUI(new RoundedScrollBarUI(theme));
        scrollPane.getHorizontalScrollBar().setUI(new RoundedScrollBarUI(theme));
        scrollPane.getVerticalScrollBar().setBackground(theme.scrollTrack);
        scrollPane.getHorizontalScrollBar().setBackground(theme.scrollTrack);
    }

    /**
     * Shows a themed confirmation dialog with default button labels.
     *
     * @param parent parent component used for ownership and theme resolution
     * @param title dialog title
     * @param content content component shown inside the dialog
     * @return a {@link JOptionPane} option result
     */
    public static int showConfirmDialog(Component parent, String title, JComponent content) {
        return showConfirmDialog(parent, title, content, "OK", "Cancel", null);
    }

    /**
     * Shows a themed confirmation dialog with custom button labels.
     *
     * @param parent parent component used for ownership and theme resolution
     * @param title dialog title
     * @param content content component shown inside the dialog
     * @param okText text used for the confirm button
     * @param cancelText text used for the cancel button
     * @return a {@link JOptionPane} option result
     */
    public static int showConfirmDialog(Component parent, String title, JComponent content, String okText, String cancelText) {
        return showConfirmDialog(parent, title, content, okText, cancelText, null);
    }

    /**
     * Shows a fully customized themed confirmation dialog.
     *
     * @param parent parent component used for ownership and theme resolution
     * @param title dialog title
     * @param content content component shown inside the dialog
     * @param okText text used for the confirm button
     * @param cancelText text used for the cancel button
     * @param messageType semantic message type used to derive subtitle text
     * @return a {@link JOptionPane} option result
     */
    public static int showConfirmDialog(Component parent, String title, JComponent content, String okText, String cancelText,
                                        Dimension minimumSize) {
        final int[] result = {JOptionPane.CANCEL_OPTION};
        boolean dark = isDark(parent);
        JDialog dialog = createDialog(parent, title, dark);
        JButton okButton = createDialogButton(okText, true, dark);
        JButton cancelButton = createDialogButton(cancelText, false, dark);

        okButton.addActionListener(e -> {
            result[0] = JOptionPane.OK_OPTION;
            dialog.dispose();
        });
        cancelButton.addActionListener(e -> dialog.dispose());

        installEscapeToClose(dialog);
        dialog.getRootPane().setDefaultButton(okButton);
        dialog.setContentPane(buildDialogShell(title, null, content, dark, okButton, cancelButton));
        dialog.pack();
        if (minimumSize != null) {
            dialog.setMinimumSize(minimumSize);
            int width = Math.max(dialog.getWidth(), minimumSize.width);
            int height = Math.max(dialog.getHeight(), minimumSize.height);
            dialog.setSize(width, height);
        }
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    /**
     * Shows a themed option dialog for a plain text message.
     *
     * @param parent parent component used for ownership and theme resolution
     * @param title dialog title
     * @param message dialog body text
     * @param options button labels shown to the user
     * @param defaultIndex index of the default option
     * @return the selected option index or {@link JOptionPane#CLOSED_OPTION}
     */
    public static int showOptionDialog(Component parent, String title, String message, String[] options, int defaultIndex) {
        JComponent content = buildMessageContent(message, isDark(parent));
        return showOptionDialog(parent, title, content, options, defaultIndex);
    }

    /**
     * Shows a themed option dialog for custom component content.
     *
     * @param parent parent component used for ownership and theme resolution
     * @param title dialog title
     * @param content custom content component
     * @param options button labels shown to the user
     * @param defaultIndex index of the default option
     * @return the selected option index or {@link JOptionPane#CLOSED_OPTION}
     */
    public static int showOptionDialog(Component parent, String title, JComponent content, String[] options, int defaultIndex) {
        final int[] result = {JOptionPane.CLOSED_OPTION};
        boolean dark = isDark(parent);
        JDialog dialog = createDialog(parent, title, dark);
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonBar.setOpaque(false);

        for (int i = 0; i < options.length; i++) {
            JButton button = createDialogButton(options[i], i == defaultIndex, dark);
            final int idx = i;
            button.addActionListener(e -> {
                result[0] = idx;
                dialog.dispose();
            });
            buttonBar.add(button);
            if (i == defaultIndex) {
                dialog.getRootPane().setDefaultButton(button);
            }
        }

        installEscapeToClose(dialog);
        dialog.setContentPane(buildDialogShell(title, null, content, dark, buttonBar));
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    /**
     * Shows a themed text-input dialog and returns the entered value.
     *
     * @param parent parent component used for ownership and theme resolution
     * @param title dialog title
     * @param prompt prompt text displayed above the input field
     * @param initialValue initial text placed into the field
     * @return the entered string, or {@code null} when cancelled
     */
    public static String showInputDialog(Component parent, String title, String prompt, String initialValue) {
        boolean dark = isDark(parent);
        JTextField field = new JTextField(initialValue == null ? "" : initialValue, 24);
        styleTextField(field, dark);
        JLabel label = new JLabel(prompt);
        styleLabel(label, false, dark);

        JPanel panel = createSurfacePanel(new BorderLayout(0, 10), dark);
        panel.setOpaque(false);
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        int result = showConfirmDialog(parent, title, panel, "Search", "Cancel");
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }
        return field.getText();
    }

    /**
     * Shows a themed message dialog.
     *
     * @param parent parent component used for ownership and theme resolution
     * @param title dialog title
     * @param message message body text
     * @param messageType semantic message type from {@link JOptionPane}
     */
    public static void showMessageDialog(Component parent, String title, String message, int messageType) {
        boolean dark = isDark(parent);
        Theme theme = getTheme(parent);

        JLabel tone = new JLabel(messageTypeToText(messageType));
        tone.setFont(SUBTITLE_FONT);
        tone.setForeground(messageType == JOptionPane.ERROR_MESSAGE
                ? theme.errorText
                : (messageType == JOptionPane.WARNING_MESSAGE ? theme.warningText : theme.mutedText));

        JComponent content = buildMessageContent(message, dark);
        JDialog dialog = createDialog(parent, title, dark);
        JButton okButton = createDialogButton("OK", true, dark);
        okButton.addActionListener(e -> dialog.dispose());
        dialog.getRootPane().setDefaultButton(okButton);
        installEscapeToClose(dialog);
        dialog.setContentPane(buildDialogShell(title, tone, content, dark, okButton));
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /**
     * Shows a themed informational dialog for arbitrary component content.
     *
     * @param parent parent component used for ownership and theme resolution
     * @param title dialog title
     * @param content content component shown inside the dialog
     */
    public static void showContentDialog(Component parent, String title, JComponent content) {
        boolean dark = isDark(parent);
        JDialog dialog = createDialog(parent, title, dark);
        JButton okButton = createDialogButton("Close", true, dark);
        okButton.addActionListener(e -> dialog.dispose());
        dialog.getRootPane().setDefaultButton(okButton);
        installEscapeToClose(dialog);
        dialog.setContentPane(buildDialogShell(title, null, content, dark, okButton));
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static JPanel buildMessageContent(String message, boolean dark) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        JTextArea area = new JTextArea(message == null ? "" : message);
        area.setEditable(false);
        area.setOpaque(false);
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setFont(BODY_FONT);
        area.setForeground(theme.text);
        area.setBorder(null);
        area.setRows(Math.max(2, Math.min(6, (message == null ? 1 : message.length() / 42) + 1)));

        JPanel wrapper = createSurfacePanel(new BorderLayout(), dark);
        wrapper.setOpaque(false);
        wrapper.add(area, BorderLayout.CENTER);
        return wrapper;
    }

    private static JDialog createDialog(Component parent, String title, boolean dark) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        Window owner = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog;
        if (owner instanceof Frame frame) {
            dialog = new JDialog(frame, title, true);
        } else if (owner instanceof Dialog ownerDialog) {
            dialog = new JDialog(ownerDialog, title, true);
        } else {
            dialog = new JDialog((Frame) null, title, true);
        }

        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.getContentPane().setBackground(theme.windowBg);
        return dialog;
    }

    private static JPanel buildDialogShell(String title, JComponent subtitle, JComponent content, boolean dark, JComponent... buttons) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        JPanel root = new HexPatternPanel(theme, new BorderLayout(0, 10));
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel headerTextPanel = createSurfacePanel(new BorderLayout(0, 3), dark);
        headerTextPanel.setOpaque(false);
        headerTextPanel.add(createSectionTitle(title, dark), BorderLayout.NORTH);
        if (subtitle != null) {
            headerTextPanel.add(subtitle, BorderLayout.CENTER);
        }

        JComponent dividerBar = new RoundedDividerBar(theme);
        dividerBar.setPreferredSize(new Dimension(10, 6));

        JPanel header = createSurfacePanel(new BorderLayout(0, 4), dark);
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 0, 0));
        header.add(headerTextPanel, BorderLayout.NORTH);
        header.add(dividerBar, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(true);
        center.setBackground(theme.cardBg);
        center.setBorder(new EmptyBorder(10, 10, 10, 10));
        center.add(content, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        JPanel footer = createSurfacePanel(new FlowLayout(FlowLayout.RIGHT, 10, 0), dark);
        footer.setOpaque(false);
        for (JComponent button : buttons) {
            footer.add(button);
        }
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private static JButton createDialogButton(String text, boolean primary, boolean dark) {
        JButton button = new JButton(text);
        styleButton(button, primary, dark);
        return button;
    }

    private static void installEscapeToClose(JDialog dialog) {
        JRootPane rootPane = dialog.getRootPane();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-dialog");
        rootPane.getActionMap().put("close-dialog", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dialog.dispose();
            }
        });
    }

    private static String messageTypeToText(int messageType) {
        return switch (messageType) {
            case JOptionPane.ERROR_MESSAGE -> "Something needs attention";
            case JOptionPane.WARNING_MESSAGE -> "Check this before continuing";
            default -> "";
        };
    }


    /**
     * Small custom component that paints the rounded contrast bar displayed
     * beneath helper dialog headers.
     */
    private static class RoundedDividerBar extends JComponent {
        private final Theme theme;

        private RoundedDividerBar(Theme theme) {
            this.theme = theme;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = Math.max(0, getWidth() - 2);
            int height = Math.max(0, getHeight() - 1);
            int arc = Math.max(height, 6);

            g2.setColor(theme.dividerBar);
            g2.fillRoundRect(1, 0, width, height, arc, arc);
            g2.dispose();
        }
    }

    /**
     * Mouse listener that updates helper dialog buttons to match hover and
     * pressed states from the active theme.
     */
    private static class DialogButtonHoverListener extends MouseAdapter {
        private final AbstractButton button;
        private final Color base;
        private final Color hover;
        private final Color pressed;

        private DialogButtonHoverListener(AbstractButton button, Color base, Color hover, Color pressed) {
            this.button = button;
            this.base = base;
            this.hover = hover;
            this.pressed = pressed;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (button.isEnabled()) {
                button.setBackground(hover);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            button.setBackground(base);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (button.isEnabled()) {
                button.setBackground(pressed);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (button.isEnabled()) {
                button.setBackground(button.contains(e.getPoint()) ? hover : base);
            }
        }
    }

    /**
     * Surface panel that paints the faint hexagon lattice used behind helper
     * dialog content.
     */
    private static class HexPatternPanel extends JPanel {
        private final Theme theme;

        private HexPatternPanel(Theme theme, LayoutManager layout) {
            super(layout);
            this.theme = theme;
            setOpaque(true);
            setBackground(theme.windowBg);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(theme.windowBg);
            g2.fillRect(0, 0, getWidth(), getHeight());

            int radius = 16;
            double hexHeight = Math.sqrt(3) * radius;
            double xStep = 1.5 * radius;
            double yStep = hexHeight;

            g2.setColor(theme.lattice);
            g2.setStroke(new BasicStroke(theme.dark ? 1.2f : 1.4f));

            for (int col = -2; col < (int) (getWidth() / xStep) + 3; col++) {
                double x = col * xStep;
                double yOffset = (col % 2 == 0) ? 0 : hexHeight / 2.0;
                for (int row = -2; row < (int) (getHeight() / yStep) + 3; row++) {
                    double y = row * yStep + yOffset;
                    Polygon hex = createHexagon((int) Math.round(x), (int) Math.round(y), radius);
                    g2.drawPolygon(hex);
                }
            }
            g2.dispose();
        }

        private Polygon createHexagon(int x, int y, int size) {
            int[] xs = {
                    x + size / 2,
                    x + (3 * size) / 2,
                    x + 2 * size,
                    x + (3 * size) / 2,
                    x + size / 2,
                    x
            };
            int h = (int) (Math.sqrt(3) * size / 2);
            int[] ys = {
                    y,
                    y,
                    y + h,
                    y + 2 * h,
                    y + 2 * h,
                    y + h
            };
            return new Polygon(xs, ys, 6);
        }
    }

    /**
     * Scroll bar UI delegate that draws slimmer rounded helper dialog scroll
     * thumbs and tracks.
     */
    private static class RoundedScrollBarUI extends BasicScrollBarUI {
        private final Theme theme;

        private RoundedScrollBarUI(Theme theme) {
            this.theme = theme;
        }

        @Override
        protected void configureScrollBarColors() {
            trackColor = theme.scrollTrack;
            thumbColor = theme.scrollThumb;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(theme.scrollTrack);
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds == null || thumbBounds.width <= 0 || thumbBounds.height <= 0) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(theme.scrollThumb);
            g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 1,
                    Math.max(thumbBounds.width - 2, 10),
                    Math.max(thumbBounds.height - 2, 10), 10, 10);
            g2.dispose();
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return zeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return zeroButton();
        }

        private JButton zeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
    }
}
