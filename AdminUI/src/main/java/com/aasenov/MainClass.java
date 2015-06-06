package com.aasenov;

import java.awt.EventQueue;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.aasenov.adminui.AdminFrame;
import com.aasenov.helper.PathHelper;

/**
 * Entry point of this project.
 */
public class MainClass {

    /**
     * Property to use in log4j.xml to place log file next to executable jar.
     */
    private static final String JAR_FOLDER_PROPERTY = "jarFolder";

    /**
     * Property to set to disable log4j auto config in order to set proper log file location.
     */
    private static final String LOG4J_DEFAULT_INIT_PROPERTY = "log4j.defaultInitOverride";

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        // set environment variable for file location
        System.setProperty(JAR_FOLDER_PROPERTY, PathHelper.getJarContainingFolder());
        System.setProperty(LOG4J_DEFAULT_INIT_PROPERTY, "false");

        // DOMConfigurator is used to configure logger from xml configuration file
        DOMConfigurator.configure(PathHelper.getJarContainingFolder() + "log4j.xml");

        // init logger after log4j configuring
        final Logger sLog = Logger.getLogger(MainClass.class);

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    AdminFrame frame = new AdminFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    sLog.error(e.getMessage(), e);
                }
            }
        });
    }

}
