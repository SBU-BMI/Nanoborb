/*
 * Software by Erich Bremer
 * ALL RIGHTS RESERVED
 */

package edu.stonybrook.bmi.nanoborb;

import com.ebremer.imagebox.ImageBoxServer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefAppHandlerAdapter;
import org.slf4j.LoggerFactory;

public class MainFrame extends JFrame {
    private static final long serialVersionUID = -5570653778104813836L;
    private String address_;
    private final CefApp cefApp_;
    private final CefClient client_;
    private final CefBrowser browser_;
    private final Component browerUI_;
    private static final ImageBoxServer w = new ImageBoxServer();

    private MainFrame(String startURL, boolean useOSR, boolean isTransparent) {
        CefApp.addAppHandler(new CefAppHandlerAdapter(null) {
            @Override
            public void stateHasChanged(org.cef.CefApp.CefAppState state) {
                if (state == CefAppState.TERMINATED) System.exit(0);
            }
        });
        setTitle("Nanoborb");
        CefSettings settings = new CefSettings();
        settings.windowless_rendering_enabled = useOSR;
        cefApp_ = CefApp.getInstance(settings);
        client_ = cefApp_.createClient();
        browser_ = client_.createBrowser(startURL, useOSR, isTransparent);
        browerUI_ = browser_.getUIComponent();
        address_ = startURL;
        getContentPane().add(browerUI_, BorderLayout.CENTER);
        pack();
        setSize(1720, 840);
        createMenuBar();
        setVisible(true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                CefApp.getInstance().dispose();
                dispose();
            }
        });
    }
    
    private void createMenuBar() {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        JMenuBar menubar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem openMenuItem = new JMenuItem(new AbstractAction("Open") {
            @Override
            public void actionPerformed(ActionEvent ae) {
		JFileChooser jfc = new JFileChooser(new File(System.getProperty("user.dir")));
		jfc.setDialogTitle("Select an image");
		jfc.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("SVS and TIF images", "svs", "tif", "tiff");
		jfc.addChoosableFileFilter(filter);
		int returnValue = jfc.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
                    System.out.println("You chose wisely!");
                    File f = jfc.getSelectedFile();
                    address_ = "http://localhost:8888/files/camic.html?id=http://localhost:8888/bog/"+f.toURI().toString();
                    browser_.loadURL(address_);
		}
            }
        });
        fileMenu.add(openMenuItem);
        JMenuItem eMenuItem = new JMenuItem("Exit");
        eMenuItem.setMnemonic(KeyEvent.VK_E);
        eMenuItem.setToolTipText("Exit application");
        eMenuItem.addActionListener((event) -> System.exit(0));
        fileMenu.add(eMenuItem);
        
        JMenu viewMenu = new JMenu("View");
        JMenuItem externalMenuItem = new JMenuItem(new AbstractAction("External Browser") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                SendURLtoExternal();
            }
        });
        viewMenu.add(externalMenuItem);
        JMenu aboutMenu = new JMenu("About");
        JMenuItem quipMenuItem = new JMenuItem(new AbstractAction("QuIP") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                browser_.loadURL("http://localhost:8888/files/about.html");
            }
        });
        aboutMenu.add(quipMenuItem);
        
        menubar.add(fileMenu);
        menubar.add(viewMenu);
        menubar.add(aboutMenu);
        
        setJMenuBar(menubar);
    }
    
    private void SendURLtoExternal() {
        try {
            Desktop.getDesktop().browse(new URI(address_));
        } catch (URISyntaxException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)(org.slf4j.Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(ch.qos.logback.classic.Level.OFF);
        w.start();
        System.out.println("Library path : "+System.getProperty("java.library.path"));
        if (OS.isWindows()) {
            System.out.println("Windows OS Detected...");
            System.setProperty("java.library.path", "lib/win64" );
        } else if (OS.isMacintosh()) {
            System.out.println("Mac OS Detected...");
        } else if (OS.isLinux()) {
            System.out.println("Linux OS Detected...");
        } else {
            System.out.println("Unknown OS Detected...");
        }
        Field fieldSysPath = null;
        try {
            fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible( true );
            fieldSysPath.set( null, null );
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!CefApp.startup()) {
            System.out.println("Startup initialization failed!");
            return;
        }
        boolean useOsr = false;
        MainFrame mf = new MainFrame("http://localhost:8888/files/splash.html", useOsr, false);
    }
}
