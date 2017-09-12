package tray;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Constantly captures the latest active window and saves the title of that window it to {@link #ACTIVE_WINDOW}.
 *
 * @since 1.0
 */
public class WindowInformation {
    public static String ACTIVE_WINDOW = "";

    /**
     * Max length of window title.
     */
    private static final int MAX_TITLE_LENGTH = 1024;

    /**
     * Time in milliseconds to wait until updating the latest {@link #ACTIVE_WINDOW}.
     */
    private static final int TIME_TO_WAIT = 100;

    /**
     * Creates and schedules a timer to continuously check the active window title and update {@link #ACTIVE_WINDOW} if
     * the new window has changed.
     * <p>
     * which repeats every {@link #TIME_TO_WAIT} milliseconds.
     */
    public static void beginObservingWindows() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                String newWindow = getActiveWindowTitle();
                if (!newWindow.equalsIgnoreCase(ACTIVE_WINDOW) && !newWindow.equals("")) {
                    ACTIVE_WINDOW = newWindow.replaceAll("\\P{Print}", "");
                }
            }
        }, 1, TIME_TO_WAIT);
    }

    /**
     * Retrieves the title of the active window.
     *
     * @return The title of the window.
     */
    private static String getActiveWindowTitle() {
        char[] buffer = new char[MAX_TITLE_LENGTH * 2];
        HWND foregroundWindow = User32.GetForegroundWindow();
        User32.GetWindowTextW(foregroundWindow, buffer, MAX_TITLE_LENGTH);

        return Native.toString(buffer);
    }

    /**
     * Uses JNA to get the active window title using User32.dll
     */
    private static class User32 {
        static {
            Native.register("user32");
        }

        public static native HWND GetForegroundWindow();

        public static native int GetWindowTextW(HWND hWnd, char[] lpString, int nMaxCount);
    }
}
