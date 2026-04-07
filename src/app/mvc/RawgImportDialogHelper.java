package app.mvc;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides themed dialogs that support importing game information from the RAWG API.
 */
public class RawgImportDialogHelper {
    public static final int MANUAL_ENTRY = 0;
    public static final int IMPORT_FROM_RAWG = 1;
    public static final int CANCEL = 2;
    public static final int MAX_SIMILAR_CHOICES = 10;

    /**
     * Represents a RAWG search result that can be shown to the user and optionally selected for import.
     */
    public static class SearchCandidate {
        private final String rawgId;
        private final String title;
        private final String releaseYear;
        private final String platforms;
        private final boolean selectable;

        public SearchCandidate(String rawgId, String title, String releaseYear, String platforms) {
            this(rawgId, title, releaseYear, platforms, true);
        }

        private SearchCandidate(String rawgId, String title, String releaseYear, String platforms, boolean selectable) {
            this.rawgId = rawgId == null ? "" : rawgId.trim();
            this.title = title == null ? "" : title.trim();
            this.releaseYear = releaseYear == null ? "" : releaseYear.trim();
            this.platforms = platforms == null ? "" : platforms.trim();
            this.selectable = selectable;
        }

        /**
         * Creates a non-selectable placeholder search candidate for an empty result slot.
         *
         * @param index the index involved in the operation
         *
         * @return the resulting value
         */
        public static SearchCandidate placeholder(int index) {
            return new SearchCandidate("", "No additional result", "", "Slot " + index, false);
        }

        /**
         * Returns the rawg id.
         *
         * @return the resulting string value
         */
        public String getRawgId() {
            return rawgId;
        }

        /**
         * Returns the title.
         *
         * @return the resulting string value
         */
        public String getTitle() {
            return title;
        }

        /**
         * Returns the release year.
         *
         * @return the resulting string value
         */
        public String getReleaseYear() {
            return releaseYear;
        }

        /**
         * Returns the platforms.
         *
         * @return the resulting string value
         */
        public String getPlatforms() {
            return platforms;
        }

        /**
         * Returns whether selectable.
         *
         * @return {@code true} when the requested condition is met; otherwise {@code false}
         */
        public boolean isSelectable() {
            return selectable && !rawgId.isBlank();
        }

        /**
         * Performs the to string operation.
         *
         * @return the resulting string value
         */
        @Override
        public String toString() {
            if (!isSelectable()) {
                return title;
            }

            StringBuilder label = new StringBuilder(title.isBlank() ? "Untitled RAWG Entry" : title);
            if (!releaseYear.isBlank()) {
                label.append(" (").append(releaseYear).append(")");
            }
            if (!platforms.isBlank()) {
                label.append(" - ").append(platforms.replace("|", ", ").replaceAll("\\s+,", ",").trim());
            }
            return label.toString();
        }
    }

    /**
     * Prompts an administrator to choose between manual entry and RAWG import.
     *
     * @param parent the parent component used for ownership and theme lookup
     *
     * @return the resulting numeric value
     */
    public static int showAdminAddChoice(Component parent) {
        String[] options = {"Manual Entry", "Import From RAWG", "Cancel"};
        int choice = AppDialogThemeHelper.showOptionDialog(
                parent,
                "Add Entry",
                "How would you like to add this game?",
                options,
                0);

        if (choice == JOptionPane.CLOSED_OPTION) {
            return CANCEL;
        }
        return choice;
    }

    /**
     * Prompts the user for the RAWG game title to search for.
     *
     * @param parent the parent component used for ownership and theme lookup
     *
     * @return the resulting string value
     */
    public static String promptForGameName(Component parent) {
        String gameName = AppDialogThemeHelper.showInputDialog(
                parent,
                "Import Game From RAWG",
                "Enter the game name to search on RAWG:",
                "");

        if (gameName == null) {
            return null;
        }

        gameName = gameName.trim();
        if (gameName.isEmpty()) {
            AppDialogThemeHelper.showMessageDialog(
                    parent,
                    "RAWG Import",
                    "Please enter a game name.",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }

        return gameName;
    }

    /**
     * Shows a selection dialog for similar RAWG results and returns the chosen candidate.
     *
     * @param parent the parent component used for ownership and theme lookup
     * @param gameName the game name to search for
     * @param candidates the candidate results to display
     *
     * @return the resulting value
     */
    public static SearchCandidate promptToChooseSimilarMatch(Component parent, String gameName,
                                                             List<SearchCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            showNoMatch(parent, gameName);
            return null;
        }

        boolean dark = AppDialogThemeHelper.isDark(parent);
        AppDialogThemeHelper.Theme theme = AppDialogThemeHelper.getTheme(parent);

        List<SearchCandidate> displayCandidates = new ArrayList<>(candidates);
        while (displayCandidates.size() < MAX_SIMILAR_CHOICES) {
            displayCandidates.add(SearchCandidate.placeholder(displayCandidates.size() + 1));
        }
        if (displayCandidates.size() > MAX_SIMILAR_CHOICES) {
            displayCandidates = new ArrayList<>(displayCandidates.subList(0, MAX_SIMILAR_CHOICES));
        }

        DefaultListModel<SearchCandidate> listModel = new DefaultListModel<>();
        for (SearchCandidate candidate : displayCandidates) {
            listModel.addElement(candidate);
        }

        JList<SearchCandidate> resultList = new JList<>(listModel);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setVisibleRowCount(MAX_SIMILAR_CHOICES);
        resultList.setFont(new Font("Inter", Font.PLAIN, 13));
        resultList.setFixedCellHeight(28);
        resultList.setBackground(theme.panelBg);
        resultList.setForeground(theme.text);
        resultList.setSelectionBackground(theme.selection);
        resultList.setSelectionForeground(theme.text);
        resultList.setSelectedIndex(0);
        resultList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SearchCandidate candidate) {
                    label.setText((index + 1) + ". " + candidate);
                    label.setForeground(candidate.isSelectable() ? theme.text : theme.disabledText);
                }
                label.setBackground(isSelected ? theme.selection : theme.panelBg);
                label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(resultList);
        scrollPane.setPreferredSize(new Dimension(700, 300));
        AppDialogThemeHelper.styleScrollPane(scrollPane, dark);

        JPanel panel = AppDialogThemeHelper.createSurfacePanel(new BorderLayout(0, 8), dark);
        JLabel label = new JLabel("Select one of the 10 RAWG results for \"" + gameName + "\":");
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        AppDialogThemeHelper.styleLabel(label, false, dark);
        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        int result = AppDialogThemeHelper.showConfirmDialog(parent, "RAWG Import", panel, "Use Selected Game", "Cancel");
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        SearchCandidate selected = resultList.getSelectedValue();
        if (selected == null || !selected.isSelectable()) {
            AppDialogThemeHelper.showMessageDialog(
                    parent,
                    "RAWG Import",
                    "Please select one of the real RAWG results.",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }

        return selected;
    }

    /**
     * Shows a message explaining that a RAWG API key is required before import can continue.
     *
     * @param parent the parent component used for ownership and theme lookup
     */
    public static void showMissingApiKey(Component parent) {
        AppDialogThemeHelper.showMessageDialog(
                parent,
                "RAWG Import",
                "RAWG API key not found. Add your key first, or use Manual Entry.",
                JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Shows a message indicating that no suitable RAWG match was found.
     *
     * @param parent the parent component used for ownership and theme lookup
     * @param gameName the game name to search for
     */
    public static void showNoMatch(Component parent, String gameName) {
        AppDialogThemeHelper.showMessageDialog(
                parent,
                "RAWG Import",
                "No RAWG match was found for \"" + gameName + "\".",
                JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Shows a message describing a RAWG import failure.
     *
     * @param parent the parent component used for ownership and theme lookup
     * @param message the message text to display
     */
    public static void showImportFailed(Component parent, String message) {
        AppDialogThemeHelper.showMessageDialog(
                parent,
                "RAWG Import",
                "RAWG import failed: " + message,
                JOptionPane.ERROR_MESSAGE);
    }
}
