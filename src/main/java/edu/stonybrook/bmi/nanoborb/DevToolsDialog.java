package edu.stonybrook.bmi.nanoborb;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JDialog;

import org.cef.browser.CefBrowser;

@SuppressWarnings("serial")
public class DevToolsDialog extends JDialog {
    private final CefBrowser devTools_;
    public DevToolsDialog(Frame owner, String title, CefBrowser browser) {
        this(owner, title, browser, null);
    }

    public DevToolsDialog(Frame owner, String title, CefBrowser browser, Point inspectAt) {
        super(owner, title, false);
        setLayout(new BorderLayout());
        setSize(800, 600);
        setLocation(owner.getLocation().x + 20, owner.getLocation().y + 20);
        devTools_ = browser.getDevTools(inspectAt);
        add(devTools_.getUIComponent());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                dispose();
            }
        });
    }

    @Override
    public void dispose() {
        devTools_.close(true);
        super.dispose();
    }
}