package edu.stonybrook.bmi.nanoborb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefBeforeDownloadCallback;
import org.cef.callback.CefDownloadItem;
import org.cef.callback.CefDownloadItemCallback;
import org.cef.handler.CefDownloadHandler;

@SuppressWarnings("serial")
public class downloadDialog extends JDialog implements CefDownloadHandler {
    private final Frame owner_;
    private final Map<Integer, DownloadObject> downloadObjects_ = new HashMap<>();
    private final JPanel downloadPanel_ = new JPanel();
    private final downloadDialog dialog_;

    public downloadDialog(Frame owner) {
        super(owner, "Downloads", false);
        setVisible(false);
        setSize(400, 300);
        owner_ = owner;
        dialog_ = this;
        downloadPanel_.setLayout(new BoxLayout(downloadPanel_, BoxLayout.Y_AXIS));
        add(downloadPanel_);
    }

    private class DownloadObject extends JPanel {
        private boolean isHidden_ = true;
        private final int identifier_;
        private JLabel fileName_ = new JLabel();
        private JLabel status_ = new JLabel();
        private JButton dlAbort_ = new JButton();
        private JButton dlRemoveEntry_ = new JButton("Delete");
        private CefDownloadItemCallback callback_;
        private Color bgColor_;

        DownloadObject(CefDownloadItem downloadItem, String suggestedName) {
            super();
            setOpaque(true);
            setLayout(new BorderLayout());
            setMaximumSize(new Dimension(dialog_.getWidth() - 10, 80));
            identifier_ = downloadItem.getId();
            bgColor_ = identifier_ % 2 == 0 ? Color.WHITE : Color.YELLOW;
            setBackground(bgColor_);
            fileName_.setText(suggestedName);
            add(fileName_, BorderLayout.NORTH);
            status_.setAlignmentX(LEFT_ALIGNMENT);
            add(status_, BorderLayout.CENTER);
            JPanel controlPane = new JPanel();
            controlPane.setLayout(new BoxLayout(controlPane, BoxLayout.X_AXIS));
            controlPane.setOpaque(true);
            controlPane.setBackground(bgColor_);
            dlAbort_.setText("Abort");
            dlAbort_.setEnabled(false);
            dlAbort_.addActionListener((ActionEvent e) -> {
                if (callback_ != null) {
                    fileName_.setText("ABORTED - " + fileName_.getText());
                    callback_.cancel();
                }
            });
            controlPane.add(dlAbort_);
            dlRemoveEntry_.setEnabled(false);
            dlRemoveEntry_.addActionListener((ActionEvent e) -> {
                DownloadObject removed = downloadObjects_.remove(identifier_);
                if (removed != null) {
                    downloadPanel_.remove(removed);
                    dialog_.repaint();
                }
            });
            controlPane.add(dlRemoveEntry_);
            add(controlPane, BorderLayout.SOUTH);
            update(downloadItem, null);
        }

        String humanReadableByteCount(long bytes) {
            int unit = 1024;
            if (bytes < unit) return bytes + " B";
            int exp = (int) (Math.log(bytes) / Math.log(unit));
            String pre = "" + ("kMGTPE").charAt(exp - 1);
            return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
        }

        void update(CefDownloadItem downloadItem, CefDownloadItemCallback callback) {
            int percentComplete = downloadItem.getPercentComplete();
            String rcvBytes = humanReadableByteCount(downloadItem.getReceivedBytes());
            String totalBytes = humanReadableByteCount(downloadItem.getTotalBytes());
            String speed = humanReadableByteCount(downloadItem.getCurrentSpeed()) + "it/s";
            if (downloadItem.getReceivedBytes() >= 500000 && isHidden_) {
                dialog_.setVisible(true);
                dialog_.toFront();
                owner_.toBack();
                isHidden_ = false;
            }
            Runtime.getRuntime().runFinalization();
            callback_ = callback;
            status_.setText(rcvBytes + " of " + totalBytes + " - " + percentComplete + "%" + " - " + speed);
            dlAbort_.setEnabled(downloadItem.isInProgress());
            dlRemoveEntry_.setEnabled(!downloadItem.isInProgress() || downloadItem.isCanceled()
                    || downloadItem.isComplete());
            if (!downloadItem.isInProgress() && !downloadItem.isCanceled() && !downloadItem.isComplete()) {
                fileName_.setText("FAILED - " + fileName_.getText());
                callback.cancel();
            }
        }
    }

    @Override
    public void onBeforeDownload(CefBrowser browser, CefDownloadItem downloadItem, String suggestedName, CefBeforeDownloadCallback callback) {
        //System.out.println("onBeforeDownload");
        callback.Continue(suggestedName, true);
        DownloadObject dlObject = new DownloadObject(downloadItem, suggestedName);
        downloadObjects_.put(downloadItem.getId(), dlObject);
        downloadPanel_.add(dlObject);
    }

    @Override
    public void onDownloadUpdated(CefBrowser browser, CefDownloadItem downloadItem, CefDownloadItemCallback callback) {
        //System.out.println("onDownloadUpdated");
        DownloadObject dlObject = downloadObjects_.get(downloadItem.getId());
        if (dlObject == null) {
            return;
        } else {
            //System.out.println("ahead warp factor!");
        }
        dlObject.update(downloadItem, callback);
    }
}