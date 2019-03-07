/*
 * Software by Erich Bremer
 * ALL RIGHTS RESERVED
 */

package edu.stonybrook.bmi.nanoborb;

import com.ebremer.imagebox.ImageBoxServer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
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
    private static ImageBoxServer w;
    private String currentdirectory;
    private String target;

    private MainFrame(String currentdirectory, String webfiles, String startURL, boolean useOSR, boolean isTransparent) {
        this.currentdirectory = currentdirectory;
        createMenuBar();
        ImageIcon img = new ImageIcon(getClass().getResource("/webfiles/Nanoborb-256x256.png"));
        setIconImage(img.getImage());
        w = new ImageBoxServer(webfiles);
        w.start();
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
        downloadDialog downloadDialog = new downloadDialog(this);
        client_.addDownloadHandler(downloadDialog);
        browser_ = client_.createBrowser(startURL, useOSR, isTransparent);
        browerUI_ = browser_.getUIComponent();
        address_ = startURL;
        getContentPane().add(browerUI_, BorderLayout.CENTER);
        pack();
        setSize(1720, 840);
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
                File cd = new File(currentdirectory);
                System.out.println("CURRENT DIRECTORY "+currentdirectory);
		JFileChooser jfc = new JFileChooser(cd);
		jfc.setDialogTitle("Select an image");
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("TIFF Images", "tif", "tiff"));
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("SVS Images", "svs"));
                jfc.addChoosableFileFilter(new FileNameExtensionFilter("VSI Images", "vsi"));
                jfc.setAcceptAllFileFilterUsed(true);
		int returnValue = jfc.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File f = jfc.getSelectedFile();
                    if (f.exists()) {
                        System.out.println("You chose wisely! "+f.getPath());
                        try {
                            target = URLEncoder.encode(f.toURI().toString(),"UTF-8");
                        } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        address_ = "http://localhost:8888/files/camic.html?id=http://localhost:8888/bog/"+target;
                        browser_.loadURL(address_);
                        currentdirectory = f.getParent();
                        System.out.println("New CURRENT DIRECTORY "+currentdirectory);
                    } else {
                        System.out.println("File cannot be found.  Please check for weird and offending characters.");
                    }
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
                try {
                    Desktop.getDesktop().browse(new URI(address_));
                } catch (URISyntaxException | IOException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        viewMenu.add(externalMenuItem);
        
        JMenuItem OSDMenuItem = new JMenuItem(new AbstractAction("OpenSeaDragon") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                address_ = "http://localhost:8888/files/debug/osd.html?id=http://localhost:8888/bog/"+target;
                browser_.loadURL(address_);
            }
        });
        viewMenu.add(OSDMenuItem);
        
        JMenu aboutMenu = new JMenu("About");
        JMenuItem quipMenuItem = new JMenuItem(new AbstractAction("QuIP") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                browser_.loadURL("https://sbu-bmi.github.io/quip_distro/");
            }
        });
        aboutMenu.add(quipMenuItem);
        JMenuItem aboutMenuItem = new JMenuItem(new AbstractAction("Main Page") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                browser_.loadURL("http://localhost:8888/files/splash.html");
            }
        });
        aboutMenu.add(aboutMenuItem);
        final JMenuItem showDevTools = new JMenuItem("Developer Tools");
        showDevTools.addActionListener((ActionEvent e) -> {
            Frame owner_ = this;
            DevToolsDialog devToolsDlg = new DevToolsDialog(owner_, "Developer Tools", browser_);
            devToolsDlg.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentHidden(ComponentEvent e) {
                    showDevTools.setEnabled(true);
                }
            });
            devToolsDlg.setVisible(true);
            showDevTools.setEnabled(false);
        });
        viewMenu.add(showDevTools);
        menubar.add(fileMenu);
        menubar.add(viewMenu);
        menubar.add(aboutMenu);
        setJMenuBar(menubar);
    }
    
    public static void main(String[] args) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)(org.slf4j.Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(ch.qos.logback.classic.Level.OFF);
        System.out.println("Java Version : "+System.getProperty("java.version"));
        String webfiles = null;
        String currentdirectory = System.getProperty("user.dir");
        if (OS.isWindows()) {
            System.out.println("Windows OS Detected...");
            System.setProperty("java.library.path", "lib/win64" );
            webfiles = "files/webfiles";
        } else if (OS.isMacintosh()) {
            System.out.println("Mac OS Detected...");
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            Path jarfilepath = Paths.get(MainFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            Path webpath = jarfilepath.getParent();
            webfiles = webpath.toString()+"/files/webfiles";
            try {
                webfiles = URLDecoder.decode(webfiles, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            currentdirectory = jarfilepath.getParent().getParent().getParent().getParent().toString();
            System.out.println("Nanoborb location : "+currentdirectory);
            System.out.println("Web files location : "+webfiles);
        } else if (OS.isLinux()) {
            System.out.println("Linux OS Detected...");
            webfiles = "files/webfiles";
        } else {
            System.out.println("Unknown OS Detected...");
        }
        Field fieldSysPath;
        try {
            fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible( true );
            fieldSysPath.set( null, null );
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!CefApp.startup()) {
            System.out.println("Startup initialization failed!");
            return;
        }
        if (webfiles != null) {
            MainFrame mf = new MainFrame(currentdirectory, webfiles,"http://localhost:8888/files/splash.html", false, false);
        } else {
            System.out.println("Unsupported OS...");
        }
    }
}
