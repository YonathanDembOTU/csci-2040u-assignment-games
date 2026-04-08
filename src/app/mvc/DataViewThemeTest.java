package app.mvc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataViewThemeTest {

    private DataView view;

    @BeforeEach
    void setUp() throws Exception {
        SwingUtilities.invokeAndWait(() -> view = new DataView());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (view != null) {
            SwingUtilities.invokeAndWait(() -> view.dispose());
        }
    }

    @Test
    void ut02_toggleTheme_flipsDarkModeState() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertFalse(view.isDarkMode());

            view.toggleTheme();
            assertTrue(view.isDarkMode());

            view.toggleTheme();
            assertFalse(view.isDarkMode());
        });
    }

    @Test
    void ut03_applyTheme_appliesLightAndDarkColours() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // initial constructor theme = light
            view.applyTheme();
            assertEquals(new Color(245, 245, 245), view.getContentPane().getBackground());
            assertEquals(Color.BLACK, view.table.getForeground());
            assertEquals(Color.BLACK, view.table.getTableHeader().getForeground());

            // switch to dark
            view.toggleTheme();
            assertEquals(new Color(28, 28, 28), view.getContentPane().getBackground());
            assertEquals(new Color(235, 235, 235), view.table.getForeground());
            assertEquals(Color.WHITE, view.table.getTableHeader().getForeground());

            // switch back to light
            view.toggleTheme();
            assertEquals(new Color(245, 245, 245), view.getContentPane().getBackground());
            assertEquals(Color.BLACK, view.table.getForeground());
            assertEquals(Color.BLACK, view.table.getTableHeader().getForeground());
        });
    }
}