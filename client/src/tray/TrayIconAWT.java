/*
 * Based on code from Johannes Buchner who retrieved the code from swinghelper's domain
 * (https://swinghelper.dev.java.net/) which is no longer accessible.
 * https://github.com/JohannesBuchner/Jake/blob/master/gui/src/main/java/com/jakeapp/gui/swing/controls/JXTrayIcon.java
 */

package tray;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * The tray icon that allows for the application and its controls to be displayed in the system tray.
 */
public class TrayIconAWT extends TrayIcon {
    private JDialog dialog;
    private JPopupMenu menu;

    /**
     * Sets image of tray icon and prepares listeners upon mouse event.
     *
     * @param image The image of the tray icon itself.
     * @param menu  The popup menu that will be displayed upon right-clicking the icon.
     */
    public TrayIconAWT(Image image, JPopupMenu menu) {
        super(image);

        this.menu = menu;

        dialog = new JDialog((Frame) null);
        dialog.setUndecorated(true);
        dialog.setAlwaysOnTop(true);

        menu.addPopupMenuListener(new TrayIconPopupListener());
        addMouseListener(new TrayIconMouseListener());
    }

    /**
     * Upon mouse pressed and mouse released, show the tray menu.
     */
    private class TrayIconMouseListener extends MouseAdapter {
        public void mousePressed(MouseEvent mouseEvent) {
            showMenu(mouseEvent);
        }

        public void mouseReleased(MouseEvent mouseEvent) {
            showMenu(mouseEvent);
        }

        /**
         * Sets location of dialog to top-right of mouse location and then enables visibility.
         *
         * @param mouseEvent Data about the mouse event. Only the x and y co-ordinates will be used to calculate
         *                   location of the menu.
         */
        private void showMenu(MouseEvent mouseEvent) {
            dialog.setLocation(mouseEvent.getX(), mouseEvent.getY() - menu.getPreferredSize().height);
            dialog.setVisible(true);

            menu.show(dialog.getContentPane(), 0, 0);

            dialog.toFront();
        }
    }

    /**
     * If the popup menu is canceled by clicking away or if a menu item is clicked, the dialog box is to become hidden.
     */
    private class TrayIconPopupListener implements PopupMenuListener {
        public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
            dialog.setVisible(false);
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
            dialog.setVisible(false);
        }

        public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
        }
    }
}
