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
 * Provides shared theming, layout, and dialog-building utilities for the styled helper windows used by the application.
 */
public class AppDialogThemeHelper {
    /**
     * Stores the color, typography, and surface values that define a single helper-window theme.
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

    /**
     * Performs the theme operation.
     *
     * @param false the false value
     * @param Color(245 the color(245 value
     * @param 245 the 245 value
     * @param 245) the 245) value
     * @param Color(240 the color(240 value
     * @param 240 the 240 value
     * @param 240) the 240) value
     * @param Color(238 the color(238 value
     * @param 238 the 238 value
     * @param 238 the 238 value
     * @param 235) the 235) value
     * @param Color(188 the color(188 value
     * @param 188 the 188 value
     * @param 188) the 188) value
     * @param Color(205 the color(205 value
     * @param 205 the 205 value
     * @param 205) the 205) value
     * @param Color(235 the color(235 value
     * @param 235 the 235 value
     * @param 235) the 235) value
     * @param Color(220 the color(220 value
     * @param 226 the 226 value
     * @param 234) the 234) value
     * @param Color(205 the color(205 value
     * @param 212 the 212 value
     * @param 222) the 222) value
     * @param Color(255 the color(255 value
     * @param 170 the 170 value
     * @param 60) the 60) value
     * @param Color(255 the color(255 value
     * @param 183 the 183 value
     * @param 90) the 90) value
     * @param Color(232 the color(232 value
     * @param 150 the 150 value
     * @param 45) the 45) value
     * @param Color(30 the color(30 value
     * @param 30 the 30 value
     * @param 30) the 30) value
     * @param Color(80 the color(80 value
     * @param 80 the 80 value
     * @param 80) the 80) value
     * @param Color(255 the color(255 value
     * @param 140 the 140 value
     * @param 0) the 0) value
     * @param Color(230 the color(230 value
     * @param 230 the 230 value
     * @param 230) the 230) value
     * @param Color(130 the color(130 value
     * @param 130 the 130 value
     * @param 130) the 130) value
     * @param Color(170 the color(170 value
     * @param 170 the 170 value
     * @param 170 the 170 value
     * @param 205) the 205) value
     * @param Color(214 the color(214 value
     * @param 214 the 214 value
     * @param 214) the 214) value
     * @param Color(255 the color(255 value
     * @param 140 the 140 value
     * @param 0) the 0) value
     * @param Color(118 the color(118 value
     * @param 118 the 118 value
     * @param 118) the 118) value
     * @param Color(140 the color(140 value
     * @param 110 the 110 value
     * @param 30) the 30) value
     * @param Color(150 the color(150 value
     * @param 50 the 50 value
     * @param 50) the 50) value
     * @param Color(120 the color(120 value
     * @param 120 the 120 value
     * @param 120) the 120) value
     * @param Color(255 the color(255 value
     * @param 170 the 170 value
     * @param 0 the 0 value
     * @param true the true value
     * @param Color(28 the color(28 value
     * @param 28 the 28 value
     * @param 28) the 28) value
     * @param Color(55 the color(55 value
     * @param 55 the 55 value
     * @param 55) the 55) value
     * @param Color(48 the color(48 value
     * @param 48 the 48 value
     * @param 48 the 48 value
     * @param 238) the 238) value
     * @param Color(90 the color(90 value
     * @param 90 the 90 value
     * @param 90) the 90) value
     * @param Color(72 the color(72 value
     * @param 72 the 72 value
     * @param 72) the 72) value
     * @param Color(55 the color(55 value
     * @param 55 the 55 value
     * @param 55) the 55) value
     * @param Color(74 the color(74 value
     * @param 74 the 74 value
     * @param 74) the 74) value
     * @param Color(92 the color(92 value
     * @param 92 the 92 value
     * @param 92) the 92) value
     * @param Color(255 the color(255 value
     * @param 140 the 140 value
     * @param 0) the 0) value
     * @param Color(255 the color(255 value
     * @param 163 the 163 value
     * @param 48) the 48) value
     * @param Color(214 the color(214 value
     * @param 117 the 117 value
     * @param 0) the 0) value
     * @param Color(235 the color(235 value
     * @param 235 the 235 value
     * @param 235) the 235) value
     * @param Color(190 the color(190 value
     * @param 190 the 190 value
     * @param 190) the 190) value
     * @param Color(255 the color(255 value
     * @param 140 the 140 value
     * @param 0) the 0) value
     * @param Color(38 the color(38 value
     * @param 38 the 38 value
     * @param 38) the 38) value
     * @param Color(180 the color(180 value
     * @param 180 the 180 value
     * @param 180) the 180) value
     * @param Color(132 the color(132 value
     * @param 132 the 132 value
     * @param 132 the 132 value
     * @param 205) the 205) value
     * @param Color(60 the color(60 value
     * @param 60 the 60 value
     * @param 60) the 60) value
     * @param Color(255 the color(255 value
     * @param 140 the 140 value
     * @param 0) the 0) value
     * @param Color(168 the color(168 value
     * @param 168 the 168 value
     * @param 168) the 168) value
     * @param Color(230 the color(230 value
     * @param 200 the 200 value
     * @param 120) the 120) value
     * @param Color(255 the color(255 value
     * @param 150 the 150 value
     * @param 150) the 150) value
     * @param Color(140 the color(140 value
     * @param 140 the 140 value
     * @param 140) the 140) value
     * @param Color(255 the color(255 value
     * @param 140 the 140 value
     * @param 0 the 0 value
     * @param Font("Inter" the font("inter" value
     * @param Font.BOLD the font.bold value
     * @param Font("Inter" the font("inter" value
     * @param Font.PLAIN the font.plain value
     * @param Font("Inter" the font("inter" value
     * @param Font.BOLD the font.bold value
     * @param Font("Inter" the font("inter" value
     * @param Font.PLAIN the font.plain value
     * @param Font("Inter" the font("inter" value
     * @param Font.BOLD the font.bold value
     * @param AppDialogThemeHelper( the app dialog theme helper( value
     *
     * @return the resulting value
     */
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
     * Returns the dialog theme that should be used for the supplied component hierarchy.
     *
     * @param parent the parent component used for ownership and theme lookup
     *
     * @return the resulting value
     */
    public static Theme getTheme(Component parent) {
        return isDark(parent) ? DARK_THEME : LIGHT_THEME;
    }

    /**
     * Determines whether the supplied component belongs to a dark-mode view hierarchy.
     *
     * @param parent the parent component used for ownership and theme lookup
     *
     * @return {@code true} when the requested condition is met; otherwise {@code false}
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
     * Creates a surface-styled panel using the supplied layout manager.
     *
     * @param layout the layout manager to install on the panel
     *
     * @return the resulting value
     */
    public static JPanel createSurfacePanel(LayoutManager layout) {
        return createSurfacePanel(layout, false);
    }

    /**
     * Creates a surface-styled panel using the supplied layout manager.
     *
     * @param layout the layout manager to install on the panel
     * @param dark whether dark-mode styling should be applied
     *
     * @return the resulting value
     */
    public static JPanel createSurfacePanel(LayoutManager layout, boolean dark) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        JPanel panel = new JPanel(layout);
        panel.setBackground(theme.windowBg);
        return panel;
    }

    /**
     * Creates a card-styled panel using the supplied layout manager.
     *
     * @param layout the layout manager to install on the panel
     *
     * @return the resulting value
     */
    public static JPanel createCardPanel(LayoutManager layout) {
        return createCardPanel(layout, false);
    }

    /**
     * Creates a card-styled panel using the supplied layout manager.
     *
     * @param layout the layout manager to install on the panel
     * @param dark whether dark-mode styling should be applied
     *
     * @return the resulting value
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
     * Applies the shared helper-dialog label styling to the supplied label.
     *
     * @param label the label to style
     */
    public static void styleLabel(JLabel label) {
        styleLabel(label, false, false);
    }

    /**
     * Applies the shared helper-dialog label styling to the supplied label.
     *
     * @param label the label to style
     * @param accent whether accent styling should be applied
     */
    public static void styleLabel(JLabel label, boolean accent) {
        styleLabel(label, accent, false);
    }

    /**
     * Applies the shared helper-dialog label styling to the supplied label.
     *
     * @param label the label to style
     * @param accent whether accent styling should be applied
     * @param dark whether dark-mode styling should be applied
     */
    public static void styleLabel(JLabel label, boolean accent, boolean dark) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        label.setFont(LABEL_FONT);
        label.setForeground(accent ? theme.accent : theme.text);
        label.setOpaque(false);
    }

    /**
     * Creates a styled section-title label for a helper dialog.
     *
     * @param text the text value
     *
     * @return the resulting value
     */
    public static JLabel createSectionTitle(String text) {
        return createSectionTitle(text, false);
    }

    /**
     * Creates a styled section-title label for a helper dialog.
     *
     * @param text the text value
     * @param dark whether dark-mode styling should be applied
     *
     * @return the resulting value
     */
    public static JLabel createSectionTitle(String text, boolean dark) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        JLabel label = new JLabel(text == null ? "" : text.toUpperCase(Locale.ROOT));
        label.setFont(TITLE_FONT);
        label.setForeground(theme.accent);
        return label;
    }

    /**
     * Creates subtitle.
     *
     * @param text the text value
     *
     * @return the resulting value
     */
    public static JLabel createSubtitle(String text) {
        return createSubtitle(text, false);
    }

    /**
     * Creates subtitle.
     *
     * @param text the text value
     * @param dark whether dark-mode styling should be applied
     *
     * @return the resulting value
     */
    public static JLabel createSubtitle(String text, boolean dark) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        JLabel label = new JLabel(text == null ? "" : text.toUpperCase(Locale.ROOT));
        label.setFont(SUBTITLE_FONT);
        label.setForeground(theme.mutedText);
        return label;
    }

    /**
     * Applies shared styling to the supplied text field.
     *
     * @param field the field value
     */
    public static void styleTextField(JTextField field) {
        styleTextField(field, false);
    }

    /**
     * Applies shared styling to the supplied text field.
     *
     * @param field the field value
     * @param dark whether dark-mode styling should be applied
     */
    public static void styleTextField(JTextField field, boolean dark) {
        styleTextComponent(field, dark);
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        field.setCaretColor(theme.text);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 34));
    }

    /**
     * Applies shared styling to the supplied password field.
     *
     * @param field the field value
     */
    public static void stylePasswordField(JPasswordField field) {
        stylePasswordField(field, false);
    }

    /**
     * Applies shared styling to the supplied password field.
     *
     * @param field the field value
     * @param dark whether dark-mode styling should be applied
     */
    public static void stylePasswordField(JPasswordField field, boolean dark) {
        styleTextComponent(field, dark);
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        field.setCaretColor(theme.text);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 34));
    }

    /**
     * Applies shared styling to the supplied text area.
     *
     * @param area the area value
     */
    public static void styleTextArea(JTextArea area) {
        styleTextArea(area, false);
    }

    /**
     * Applies shared styling to the supplied text area.
     *
     * @param area the area value
     * @param dark whether dark-mode styling should be applied
     */
    public static void styleTextArea(JTextArea area, boolean dark) {
        styleTextComponent(area, dark);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setMargin(new Insets(8, 8, 8, 8));
    }

    /**
     * Performs the style text component operation.
     *
     * @param component the component to configure
     * @param dark whether dark-mode styling should be applied
     */
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
     * Applies shared styling to the supplied check box.
     *
     * @param box the box value
     */
    public static void styleCheckBox(JCheckBox box) {
        styleCheckBox(box, false);
    }

    /**
     * Applies shared styling to the supplied check box.
     *
     * @param box the box value
     * @param dark whether dark-mode styling should be applied
     */
    public static void styleCheckBox(JCheckBox box, boolean dark) {
        Theme theme = dark ? DARK_THEME : LIGHT_THEME;
        box.setOpaque(false);
        box.setForeground(theme.text);
        box.setFont(BODY_FONT);
        box.setFocusPainted(false);
    }

    /**
     * Applies shared styling to the supplied combo box.
     *
     * @param combo the combo box to populate or inspect
     */
    public static void styleComboBox(JComboBox<?> combo) {
        styleComboBox(combo, false);
    }

    /**
     * Applies shared styling to the supplied combo box.
     *
     * @param combo the combo box to populate or inspect
     * @param dark whether dark-mode styling should be applied
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
     * Applies shared styling to the supplied button.
     *
     * @param button the button to style
     * @param primary the primary value
     */
    public static void styleButton(AbstractButton button, boolean primary) {
        styleButton(button, primary, false);
    }

    /**
     * Applies shared styling to the supplied button.
     *
     * @param button the button to style
     * @param primary the primary value
     * @param dark whether dark-mode styling should be applied
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
     * Applies shared styling to the supplied table.
     *
     * @param table the table to style
     */
    public static void styleTable(JTable table) {
        styleTable(table, false);
    }

    /**
     * Applies shared styling to the supplied table.
     *
     * @param table the table to style
     * @param dark whether dark-mode styling should be applied
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
     * Applies shared styling to the supplied scroll pane.
     *
     * @param scrollPane the scroll pane to style
     */
    public static void styleScrollPane(JScrollPane scrollPane) {
        styleScrollPane(scrollPane, false);
    }

    /**
     * Applies shared styling to the supplied scroll pane.
     *
     * @param scrollPane the scroll pane to style
     * @param dark whether dark-mode styling should be applied
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
     * Shows a themed confirmation dialog for the supplied content.
     *
     * @param parent the parent component used for ownership and theme lookup
     * @param title the title text to use
     * @param content the content component to show
     *
     * @return the resulting numeric value
     */
    public static int showConfirmDialog(Component parent, String title, JComponent content) {
        return showConfirmDialog(parent, title, content, "OK", "Cancel", null);
    }

    /**
     * Shows a themed confirmation dialog for the supplied content.
     *
     * @param parent the parent component used for ownership and theme lookup
     * @param title the title text to use
     * @param content the content component to show
     * @param okText the label for the affirmative action button
     * @param cancelText the label for the cancel action button
     *
     * @return the resulting numeric value
     */
    public static int showConfirmDialog(Component parent, String title, JComponent content, String okText, String cancelText) {
        return showConfirmDialog(parent, title, content, okText, cancelText, null);
    }

    /**
     * Shows a themed confirmation dialog for the supplied content.
     *
     * @param parent the parent component used for ownership and theme lookup
     * @param title the title text to use
     * @param content the content component to show
     * @param okText the label for the affirmative action button
     * @param cancelText the label for the cancel action button
     * @param minimumSize the minimum size value
     *
     * @return the resulting numeric value
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
     * Shows a themed option dialog and returns the selected option index.
     *
     * @param parent the parent component used for ownership and theme lookup
     * @param title the title text to use
     * @param message the message text to display
     * @param options the option labels to display
     * @param defaultIndex the default index value
     *
     * @return the resulting numeric value
     */
    public static int showOptionDialog(Component parent, String title, String message, String[] options, int defaultIndex) {
        JComponent content = buildMessageContent(message, isDark(parent));
        return showOptionDialog(parent, title, content, options, defaultIndex);
    }

    /**
     * Shows a themed option dialog and returns the selected option index.
     *
     * @param parent the parent component used for ownership and theme lookup
     * @param title the title text to use
     * @param content the content component to show
     * @param options the option labels to display
     * @param defaultIndex the default index value
     *
     * @return the resulting numeric value
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
     * Shows a themed input dialog and returns the entered text.
     *
     * @param parent the parent component used for ownership and theme lookup
     * @param title the title text to use
     * @param prompt the prompt value
     * @param initialValue the initial text value
     *
     * @return the resulting string value
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
     * Shows a themed message dialog for the supplied message content.
     *
     * @param parent the parent component used for ownership and theme lookup
     * @param title the title text to use
     * @param message the message text to display
     * @param messageType the {@link JOptionPane} message type to use
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
     * Shows a themed dialog that wraps a custom content component.
     *
     * @param parent the parent component used for ownership and theme lookup
     * @param title the title text to use
     * @param content the content component to show
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

    /**
     * Builds message content.
     *
     * @param message the message text to display
     * @param dark whether dark-mode styling should be applied
     *
     * @return the resulting value
     */
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

    /**
     * Creates dialog.
     *
     * @param parent the parent component used for ownership and theme lookup
     * @param title the title text to use
     * @param dark whether dark-mode styling should be applied
     *
     * @return the resulting value
     */
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

    /**
     * Builds the shared helper-dialog shell used to display themed content.
     *
     * @param title the title text to use
     * @param subtitle the subtitle value
     * @param content the content component to show
     * @param dark whether dark-mode styling should be applied
     * @param buttons the buttons value
     *
     * @return the resulting value
     */
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

    /**
     * Creates dialog button.
     *
     * @param text the text value
     * @param primary the primary value
     * @param dark whether dark-mode styling should be applied
     *
     * @return the resulting value
     */
    private static JButton createDialogButton(String text, boolean primary, boolean dark) {
        JButton button = new JButton(text);
        styleButton(button, primary, dark);
        return button;
    }

    /**
     * Performs the install escape to close operation.
     *
     * @param dialog the dialog value
     */
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

    /**
     * Performs the message type to text operation.
     *
     * @param messageType the {@link JOptionPane} message type to use
     *
     * @return the resulting string value
     */
    private static String messageTypeToText(int messageType) {
        return switch (messageType) {
            case JOptionPane.ERROR_MESSAGE -> "Something needs attention";
            case JOptionPane.WARNING_MESSAGE -> "Check this before continuing";
            default -> "";
        };
    }


    /**
     * Provides functionality for rounded divider bar.
     */
    private static class RoundedDividerBar extends JComponent {
        private final Theme theme;

        private RoundedDividerBar(Theme theme) {
            this.theme = theme;
            setOpaque(false);
        }

        /**
         * Performs the paint component operation.
         *
         * @param g the graphics context used for painting
         */
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
     * Provides functionality for dialog button hover listener.
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

        /**
         * Performs the mouse entered operation.
         *
         * @param e the event instance that triggered the callback
         */
        @Override
        public void mouseEntered(MouseEvent e) {
            if (button.isEnabled()) {
                button.setBackground(hover);
            }
        }

        /**
         * Performs the mouse exited operation.
         *
         * @param e the event instance that triggered the callback
         */
        @Override
        public void mouseExited(MouseEvent e) {
            button.setBackground(base);
        }

        /**
         * Performs the mouse pressed operation.
         *
         * @param e the event instance that triggered the callback
         */
        @Override
        public void mousePressed(MouseEvent e) {
            if (button.isEnabled()) {
                button.setBackground(pressed);
            }
        }

        /**
         * Performs the mouse released operation.
         *
         * @param e the event instance that triggered the callback
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            if (button.isEnabled()) {
                button.setBackground(button.contains(e.getPoint()) ? hover : base);
            }
        }
    }

    /**
     * Provides functionality for hex pattern panel.
     */
    private static class HexPatternPanel extends JPanel {
        private final Theme theme;

        private HexPatternPanel(Theme theme, LayoutManager layout) {
            super(layout);
            this.theme = theme;
            setOpaque(true);
            setBackground(theme.windowBg);
        }

        /**
         * Performs the paint component operation.
         *
         * @param g the graphics context used for painting
         */
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

        /**
         * Creates hexagon.
         *
         * @param x the x value
         * @param y the y value
         * @param size the size value
         *
         * @return the resulting value
         */
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
     * Provides functionality for rounded scroll bar ui.
     */
    private static class RoundedScrollBarUI extends BasicScrollBarUI {
        private final Theme theme;

        private RoundedScrollBarUI(Theme theme) {
            this.theme = theme;
        }

        /**
         * Performs the configure scroll bar colors operation.
         */
        @Override
        protected void configureScrollBarColors() {
            trackColor = theme.scrollTrack;
            thumbColor = theme.scrollThumb;
        }

        /**
         * Performs the paint track operation.
         *
         * @param g the graphics context used for painting
         * @param c the c value
         * @param trackBounds the track bounds value
         */
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(theme.scrollTrack);
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }

        /**
         * Performs the paint thumb operation.
         *
         * @param g the graphics context used for painting
         * @param c the c value
         * @param thumbBounds the thumb bounds value
         */
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

        /**
         * Creates decrease button.
         *
         * @param orientation the orientation value
         *
         * @return the resulting value
         */
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return zeroButton();
        }

        /**
         * Creates increase button.
         *
         * @param orientation the orientation value
         *
         * @return the resulting value
         */
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return zeroButton();
        }

        /**
         * Performs the zero button operation.
         *
         * @return the resulting value
         */
        private JButton zeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
    }
}
