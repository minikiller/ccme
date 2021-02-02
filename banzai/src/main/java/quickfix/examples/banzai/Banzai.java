/*******************************************************************************
 * Copyright (c) quickfixengine.org  All rights reserved.
 *
 * This file is part of the QuickFIX FIX Engine
 *
 * This file may be distributed under the terms of the quickfixengine.org
 * license as defined by quickfixengine.org and appearing in the file
 * LICENSE included in the packaging of this file.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING
 * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.quickfixengine.org/LICENSE for licensing information.
 *
 * Contact ask@quickfixengine.org if any conditions of this licensing
 * are not clear to you.
 ******************************************************************************/

package quickfix.examples.banzai;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.quickfixj.jmx.JmxExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.*;
import quickfix.examples.banzai.ui.BanzaiFrame;

/**
 * Entry point for the Banzai application.
 */
public class Banzai {
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);

    private static final Logger log = LoggerFactory.getLogger(Banzai.class);
    private static Banzai banzai;
    private boolean initiatorStarted = false;
    private Initiator initiator = null;
    private Initiator md_initiator=null;
    private JFrame frame = null;

    public Banzai(String[] args) throws Exception {
        InputStream inputStream = null;

        inputStream = Banzai.class.getResourceAsStream("banzai.cfg");

        SessionSettings settings = new SessionSettings(inputStream);
        inputStream.close();

        boolean logHeartbeats = Boolean.valueOf(System.getProperty("logHeartbeats", "true"));

        OrderTableModel orderTableModel = new OrderTableModel();
        ExecutionTableModel executionTableModel = new ExecutionTableModel();
        BanzaiApplication application = new BanzaiApplication(orderTableModel, executionTableModel);
        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new ScreenLogFactory(true, true, true, logHeartbeats);
        MessageFactory messageFactory = new DefaultMessageFactory();

        initiator = new SocketInitiator(application, messageStoreFactory, settings, logFactory,
                messageFactory);
        boolean runMdClient = false;

        // 根据参数判断是否启动 market client
        if (args.length == 1) {
            String value = args[0];
            if (value.equals("run_banzai_client"))
                runMdClient = true;
        }

        //启动连接大盘的client
        MarketClientApplication marketClientApplication = new MarketClientApplication();
        if (runMdClient) {
            md_initiator = createMarketClientApp(marketClientApplication);
        }
        if(md_initiator!=null)
            md_initiator.start();

        JmxExporter exporter = new JmxExporter();
        exporter.register(initiator);

        frame = new BanzaiFrame(orderTableModel, executionTableModel, application,marketClientApplication);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static Initiator createMarketClientApp(MarketClientApplication application) {
        InputStream inputStream = null;

        inputStream = Banzai.class.getResourceAsStream("marketclient.cfg");

        SessionSettings settings = null;
        try {
            settings = new SessionSettings(inputStream);
            inputStream.close();
        } catch (ConfigError configError) {
            configError.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean logHeartbeats = Boolean.valueOf(System.getProperty("logHeartbeats", "true"));

        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new ScreenLogFactory(true, true, true, logHeartbeats);
        MessageFactory messageFactory = new DefaultMessageFactory();

        try {
            Initiator initiator = new SocketInitiator(application, messageStoreFactory, settings, logFactory,
                    messageFactory);
//            logon();
            initiator.start();
            return initiator;
        } catch (ConfigError configError) {
            configError.printStackTrace();
        }
        return null;
    }

    public synchronized void logon() {
        if (!initiatorStarted) {
            try {
                initiator.start();
                initiatorStarted = true;
            } catch (Exception e) {
                log.error("Logon failed", e);
            }
        } else {
            for (SessionID sessionId : initiator.getSessions()) {
                Session.lookupSession(sessionId).logon();
            }
        }
    }

    public void logout() {
        for (SessionID sessionId : initiator.getSessions()) {
            Session.lookupSession(sessionId).logout("user requested");
        }
    }

    public void stop() {
        shutdownLatch.countDown();
    }

    public JFrame getFrame() {
        return frame;
    }

    public static Banzai get() {
        return banzai;
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
        banzai = new Banzai(args);
        if (!System.getProperties().containsKey("openfix")) {
            banzai.logon();
        }
        shutdownLatch.await();
    }

}
