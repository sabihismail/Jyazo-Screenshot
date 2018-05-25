package capture;

import javafx.stage.Stage;

import java.awt.*;

/**
 * Base implementation for the function that runs after the mouse is released.
 *
 * @since 1.1
 */
public abstract class Callback {
    public abstract void onRelease(Stage stage, Rectangle selection);
}
