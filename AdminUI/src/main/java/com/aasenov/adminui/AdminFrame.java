package com.aasenov.adminui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.util.AbstractMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

import com.aasenov.helper.ConfigHelper;
import com.aasenov.helper.ConfigProperty;
import com.aasenov.restapi.UltimateSpeakerComponent;

public class AdminFrame extends JFrame {

    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(AdminFrame.class);

    private static final int MAX_THREADS_ALLOWED = 1;
    private static final int MAX_QUEUE_SIZE = MAX_THREADS_ALLOWED * 3;
    private static final int KEEP_ALIVE_TIME = 7200 * 1000;// 2 hours

    private CustomThreadPool mExecutionPool;
    private final ReentrantLock mPoolQueueLock;
    private final Condition mPoolQueueFullCond;

    private AtomicBoolean mStarted = new AtomicBoolean(false);
    private AtomicBoolean mClosing = new AtomicBoolean(false);
    private AtomicBoolean mEditingState = new AtomicBoolean(false);

    private JPanel mContentPane;

    private JMenuItem mButtonStart;
    private JMenuItem mButtonStop;
    private JMenuItem mButtonSettings;
    private JMenuItem mButtonExit;
    private JMenuItem mButtonAbout;
    private JButton mButtonSaveEditSettings;
    private JButton mButtonCancelEditingSettings;
    private JButton mButtonChooseStorageDir;
    private JButton mButtonAbutOK;

    private JLabel mStatusLabel;
    private JTextPane mLoggingPane;

    private UltimateSpeakerComponent mUltimateSpeakerComponent;
    private JTextField mTxtListenPort;
    private JTextField mTxtStorageDir;
    private JMenuItem mButtonClean;

    /**
     * Create the frame.
     */
    public AdminFrame() {
        setTitle("Ultimate Speaker");
        // init pool
        mPoolQueueLock = new ReentrantLock();
        mPoolQueueFullCond = mPoolQueueLock.newCondition();
        mExecutionPool = new CustomThreadPool(MAX_THREADS_ALLOWED, MAX_THREADS_ALLOWED, KEEP_ALIVE_TIME,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), mPoolQueueLock, mPoolQueueFullCond);

        // do not auto exit on close. Windows listener will take care of this.
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setBounds(100, 100, 600, 400);

        createComponents();
        defineButtonActions();

        mUltimateSpeakerComponent = new UltimateSpeakerComponent();

        // stop component on exit.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
                terminate();
            }
        });
    }

    /**
     * Create components that will be displayed to user.
     */
    private void createComponents() {
        // initialize menu bar
        JMenuBar menuBar = new JMenuBar();

        JMenu menuMain = new JMenu("Menu");
        menuMain.setHorizontalAlignment(SwingConstants.CENTER);
        mButtonStart = new JMenuItem("Start");
        menuMain.add(mButtonStart);
        mButtonStop = new JMenuItem("Stop");
        mButtonStop.setEnabled(false);
        menuMain.add(mButtonStop);
        mButtonClean = new JMenuItem("Clean");
        menuMain.add(mButtonClean);
        JSeparator separator_1 = new JSeparator();
        menuMain.add(separator_1);
        mButtonSettings = new JMenuItem("Settings");
        menuMain.add(mButtonSettings);
        JSeparator separator = new JSeparator();
        menuMain.add(separator);
        mButtonExit = new JMenuItem("Exit");
        menuMain.add(mButtonExit);

        JMenu menuHelp = new JMenu("Help");
        menuHelp.setHorizontalAlignment(SwingConstants.RIGHT);
        mButtonAbout = new JMenuItem("About");
        menuHelp.add(mButtonAbout);

        menuBar.add(menuMain);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(menuHelp);
        setJMenuBar(menuBar);

        // init content pane
        mContentPane = new JPanel();
        mContentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        mContentPane.setLayout(new CardLayout());
        setContentPane(mContentPane);

        // ######## Main window ##############
        JPanel mainPanel = new JPanel();
        mContentPane.add(mainPanel, "mainPanel");
        mainPanel.setLayout(new BorderLayout(0, 0));
        // init status bar
        JToolBar toolBar = new JToolBar();
        toolBar.setEnabled(false);
        mStatusLabel = new JLabel(" ");
        toolBar.add(mStatusLabel);
        mainPanel.add(toolBar, BorderLayout.SOUTH);
        // init logging pane
        JScrollPane scrollPane = new JScrollPane();
        mLoggingPane = new JTextPane();
        mLoggingPane.setEditable(false);
        scrollPane.setViewportView(mLoggingPane);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // redirect console to write to the text area.
        TextPaneOutputStream out = new TextPaneOutputStream(mLoggingPane);
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(out));
        WriterAppender appender = new WriterAppender(new PatternLayout("%-6p | %d{YYYY/MM/dd HH:mm:ss} | %c | %m%n"),
                out);
        appender.setThreshold(Level.INFO); // log only info+ messages in text area.
        Logger.getRootLogger().addAppender(appender);

        // ######## Settings window ##############
        JPanel settingsPanel = new JPanel();
        mContentPane.add(settingsPanel, "settingsPanel");
        GridBagLayout gbl_settingsPanel = new GridBagLayout();
        gbl_settingsPanel.columnWidths = new int[] { 0, 0, 0, 0, 0 };
        gbl_settingsPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        gbl_settingsPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
        gbl_settingsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
        settingsPanel.setLayout(gbl_settingsPanel);

        JLabel lblListenPort = new JLabel("Listen port:");
        GridBagConstraints gbc_lblListenPort = new GridBagConstraints();
        gbc_lblListenPort.anchor = GridBagConstraints.EAST;
        gbc_lblListenPort.insets = new Insets(0, 0, 5, 5);
        gbc_lblListenPort.gridx = 1;
        gbc_lblListenPort.gridy = 1;
        settingsPanel.add(lblListenPort, gbc_lblListenPort);

        mTxtListenPort = new JTextField(ConfigHelper.getInstance().getConfigPropertyValue(ConfigProperty.RestAPIPort));
        mTxtListenPort.setEditable(false);
        GridBagConstraints gbc_mTxtListenPort = new GridBagConstraints();
        gbc_mTxtListenPort.insets = new Insets(0, 0, 5, 5);
        gbc_mTxtListenPort.anchor = GridBagConstraints.WEST;
        gbc_mTxtListenPort.gridx = 3;
        gbc_mTxtListenPort.gridy = 1;
        settingsPanel.add(mTxtListenPort, gbc_mTxtListenPort);
        mTxtListenPort.setColumns(10);

        JLabel lblStorageDirectory = new JLabel("Storage Directory:");
        GridBagConstraints gbc_lblStorageDirectory = new GridBagConstraints();
        gbc_lblStorageDirectory.insets = new Insets(0, 0, 5, 5);
        gbc_lblStorageDirectory.gridx = 1;
        gbc_lblStorageDirectory.gridy = 2;
        settingsPanel.add(lblStorageDirectory, gbc_lblStorageDirectory);

        mTxtStorageDir = new JTextField(ConfigHelper.getInstance().getConfigPropertyValue(ConfigProperty.StorageDir));
        mTxtStorageDir.setEditable(false);
        mTxtStorageDir.setColumns(10);
        GridBagConstraints gbc_mTxtStorageDir = new GridBagConstraints();
        gbc_mTxtStorageDir.fill = GridBagConstraints.BOTH;
        gbc_mTxtStorageDir.anchor = GridBagConstraints.WEST;
        gbc_mTxtStorageDir.insets = new Insets(0, 0, 5, 5);
        gbc_mTxtStorageDir.gridx = 3;
        gbc_mTxtStorageDir.gridy = 2;
        settingsPanel.add(mTxtStorageDir, gbc_mTxtStorageDir);

        mButtonChooseStorageDir = new JButton("Browse");
        GridBagConstraints gbc_mButtonChooseStorageDir = new GridBagConstraints();
        gbc_mButtonChooseStorageDir.insets = new Insets(0, 0, 5, 0);
        // gbc_mButtonChooseStorageDir.anchor = GridBagConstraints.SOUTH;
        gbc_mButtonChooseStorageDir.gridx = 4;
        gbc_mButtonChooseStorageDir.gridy = 2;
        settingsPanel.add(mButtonChooseStorageDir, gbc_mButtonChooseStorageDir);

        JLabel newLineLbl = new JLabel(" ");
        GridBagConstraints gbc_newLineLbl = new GridBagConstraints();
        gbc_newLineLbl.insets = new Insets(0, 0, 5, 5);
        gbc_newLineLbl.gridx = 1;
        gbc_newLineLbl.gridy = 6;
        settingsPanel.add(newLineLbl, gbc_newLineLbl);

        mButtonCancelEditingSettings = new JButton("Cancel");
        GridBagConstraints gbc_mButtonCancelEditingSettings = new GridBagConstraints();
        gbc_mButtonCancelEditingSettings.insets = new Insets(0, 0, 5, 5);
        gbc_mButtonCancelEditingSettings.gridwidth = 3;
        gbc_mButtonCancelEditingSettings.gridheight = 0;
        gbc_mButtonCancelEditingSettings.anchor = GridBagConstraints.SOUTHEAST;
        gbc_mButtonCancelEditingSettings.gridx = 0;
        gbc_mButtonCancelEditingSettings.gridy = 7;
        settingsPanel.add(mButtonCancelEditingSettings, gbc_mButtonCancelEditingSettings);

        mButtonSaveEditSettings = new JButton("Edit");
        GridBagConstraints gbc_btnSaveSettings = new GridBagConstraints();
        gbc_btnSaveSettings.insets = new Insets(0, 0, 5, 5);
        gbc_btnSaveSettings.anchor = GridBagConstraints.SOUTHWEST;
        gbc_btnSaveSettings.gridheight = 0;
        gbc_btnSaveSettings.gridwidth = 2;
        gbc_btnSaveSettings.gridx = 3;
        gbc_btnSaveSettings.gridy = 7;
        settingsPanel.add(mButtonSaveEditSettings, gbc_btnSaveSettings);

        // ######## About Panel window ##############
        JPanel aboutPanel = new JPanel();
        mContentPane.add(aboutPanel, "aboutPanel");

        JTextPane txtpnUltimateSpeakerApplication = new JTextPane();
        txtpnUltimateSpeakerApplication.setContentType("text/html"); // format html page
        StringBuilder aboutText = new StringBuilder();
        aboutText.append("<html>");
        aboutText.append("<body style='text-align:center;'> ");
        aboutText.append("<h1>Ultimate Speaker</h1>");
        aboutText
                .append("<h3>Platform independent and free system, that transform various types of text files into speech</h3>");
        aboutText.append("<br/><br/>");
        aboutText.append("<h3>Created by: <b>Asen Asenov</b> (https://github.com/aasenov/ultimate-speaker)</h3>");
        aboutText.append("<br/>");
        aboutText.append("<h4>Copyrigh &copy; 2015 FMI <br/> Sofia University \"St. Kliment Ohridski\", Bulgaria</h4>");
        aboutText.append("</body>");
        aboutText.append("</html>");
        txtpnUltimateSpeakerApplication.setText(aboutText.toString());
        txtpnUltimateSpeakerApplication.setEditable(false);
        aboutPanel.setLayout(new BorderLayout(0, 0));
        aboutPanel.add(txtpnUltimateSpeakerApplication);

        mButtonAbutOK = new JButton("OK");
        aboutPanel.add(mButtonAbutOK, BorderLayout.SOUTH);

    }

    /**
     * Define actions that will be performed when button is pressed.
     */
    private void defineButtonActions() {
        // settings
        mButtonSettings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        ((CardLayout) mContentPane.getLayout()).show(mContentPane, "settingsPanel");
                    }
                });
            }
        });

        // cancel edit
        mButtonCancelEditingSettings.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                // restore
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        mTxtListenPort.setEditable(false);
                        mEditingState.set(false);
                        mButtonSaveEditSettings.setText("Edit");
                        mTxtListenPort.setText(ConfigHelper.getInstance().getConfigPropertyValue(
                                ConfigProperty.RestAPIPort));
                        mTxtStorageDir.setText(ConfigHelper.getInstance().getConfigPropertyValue(
                                ConfigProperty.StorageDir));
                        ((CardLayout) mContentPane.getLayout()).show(mContentPane, "mainPanel");
                    }
                });
            }
        });

        // save settings
        mButtonSaveEditSettings.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                if (mStarted.get()) {
                    // editing only if application is stopped
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            JOptionPane.showMessageDialog(((JButton) e.getSource()).getTopLevelAncestor(),
                                    "Please stop the application before changing settings.", "Info",
                                    JOptionPane.PLAIN_MESSAGE);
                            ((CardLayout) mContentPane.getLayout()).show(mContentPane, "mainPanel");
                        }
                    });
                } else {
                    if (mEditingState.get()) {
                        // validate
                        AbstractMap.SimpleEntry<Boolean, String> validationResult = ConfigHelper.getInstance()
                                .validateSettings(mTxtListenPort.getText(), mTxtStorageDir.getText());
                        if (!validationResult.getKey()) {
                            // unable to validate
                            JOptionPane.showMessageDialog(((JButton) e.getSource()).getTopLevelAncestor(),
                                    validationResult.getValue(), "Problem", JOptionPane.PLAIN_MESSAGE);
                        } else {

                            // saving
                            ConfigHelper.getInstance().setConfigPropertyValue(ConfigProperty.RestAPIPort,
                                    mTxtListenPort.getText());
                            ConfigHelper.getInstance().setConfigPropertyValue(ConfigProperty.StorageDir,
                                    mTxtStorageDir.getText());
                            ConfigHelper.getInstance().storeConfiguration();

                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    mTxtListenPort.setEditable(false);
                                    mEditingState.set(false);
                                    mButtonSaveEditSettings.setText("Edit");
                                    ((CardLayout) mContentPane.getLayout()).show(mContentPane, "mainPanel");
                                }
                            });
                        }
                    } else {
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                mTxtListenPort.setEditable(true);
                                mButtonSaveEditSettings.setText("Save");
                                mEditingState.set(true);
                            }
                        });
                    }
                }
            }
        });

        // file chooser
        mButtonChooseStorageDir.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                if (mEditingState.get()) {
                    // Create a file chooser
                    JFileChooser fc = new JFileChooser();
                    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    fc.setAcceptAllFileFilterUsed(false);
                    if (fc.showOpenDialog((JButton) e.getSource()) == JFileChooser.APPROVE_OPTION) {
                        mTxtStorageDir.setText(fc.getSelectedFile().getAbsolutePath());
                    }
                }
            }
        });

        // about settings
        mButtonAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        ((CardLayout) mContentPane.getLayout()).show(mContentPane, "aboutPanel");
                    }
                });
            }
        });

        // about OK
        mButtonAbutOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        ((CardLayout) mContentPane.getLayout()).show(mContentPane, "mainPanel");
                    }
                });
            }
        });

        // exit
        mButtonExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
                terminate();
            }
        });

        // start
        mButtonStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (mStarted.get()) {
                    sLog.info("Already started - nothing to do.");
                } else {
                    // enable/disable button
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                mButtonStart.setEnabled(false);
                                mButtonClean.setEnabled(false);
                                mButtonStop.setEnabled(true);
                            } catch (Exception e) {
                                sLog.error(e.getMessage(), e);
                            }
                        }
                    });

                    // start component in separate thread to release
                    processInPool(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mUltimateSpeakerComponent.start();
                                mStarted.set(true);
                            } catch (Exception e) {
                                sLog.error(e.getMessage(), e);
                            }
                        }
                    });

                }
            }
        });

        // stop
        mButtonStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (mStarted.get()) {
                    // enable/disable button
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                mButtonStart.setEnabled(true);
                                mButtonClean.setEnabled(true);
                                mButtonStop.setEnabled(false);
                            } catch (Exception e) {
                                sLog.error(e.getMessage(), e);
                            }
                        }
                    });

                    // start component in separate thread to release
                    processInPool(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mUltimateSpeakerComponent.stop();
                                mStarted.set(false);
                            } catch (Exception e) {
                                sLog.error(e.getMessage(), e);
                            }
                        }
                    });
                } else {
                    sLog.info("Already stopped - nothing to do.");
                }
            }
        });

        // clean
        mButtonClean.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (mStarted.get()) {
                    JOptionPane.showMessageDialog(((JButton) e.getSource()).getTopLevelAncestor(),
                            "Please stop the application before cleaning.", "Info", JOptionPane.PLAIN_MESSAGE);
                } else {
                    // cleanup component in separate thread to release
                    processInPool(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mUltimateSpeakerComponent.cleanup();
                            } catch (Exception e) {
                                sLog.error(e.getMessage(), e);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Process given task in thread pool.
     * 
     * @param task - task to execute.
     */
    private void processInPool(Runnable task) {
        mPoolQueueLock.lock();
        try {
            while (mExecutionPool.getQueue().size() >= MAX_QUEUE_SIZE) {
                mPoolQueueFullCond.await();
            }
        } catch (InterruptedException e) {
            sLog.error("ThreadPool is interupted, returning ....", e);
            return;
        } finally {
            mPoolQueueLock.unlock();
        }

        mExecutionPool.submit(task);
    }

    /**
     * Cleanup allocated resources.
     */
    private void close() {
        if (mClosing.getAndSet(true)) {
            return;// allow only one close
        }

        try {
            // start component in separate thread to release
            processInPool(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mStarted.get()) {
                            mUltimateSpeakerComponent.stop();
                            mStarted.set(false);
                        }
                    } catch (Exception ex) {
                        sLog.error(ex.getMessage(), ex);
                    }
                }
            });

            mExecutionPool.shutdown();
            if (mExecutionPool.getActiveCount() > 0) {
                try {
                    mExecutionPool.awaitTermination(5000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    sLog.error(e.getMessage(), e);
                }
            }
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
        }
    }

    /**
     * Terminate this frame.
     */
    private void terminate() {
        // close JVM in thread in order to show closing logs in text area.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (Exception ex) {
                    sLog.error(ex.getMessage(), ex);
                }
                System.exit(0);
            }
        }).start();
    }
}
