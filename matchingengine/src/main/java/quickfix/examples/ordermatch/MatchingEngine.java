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

package quickfix.examples.ordermatch;

import quickfix.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 订单匹配引擎
 */
public class MatchingEngine {
    public static void main(String[] args) {
        InputStream inputStream = null;
        boolean runMdClient = true;
        try {
//            if (args.length == 0) {
//                inputStream = OrderMatcher.class.getResourceAsStream("ordermatch.cfg");
//            } else if (args.length == 1) {
//                inputStream = new FileInputStream(args[0]);
//            }
//            if (inputStream == null) {
//                System.out.println("usage: " + OrderMatcher.class.getName() + " [configFile].");
//                return;
//         }
            // 根据参数判断是否启动 market client
//            if (args.length == 1) {
//                String value = args[0];
//                if (value.equals("run_md_client"))
//                    runMdClient = true;
//            }
            Initiator initiator = null; //run market client connect to market data

            inputStream = OrderMatcher.class.getResourceAsStream("ordermatch.cfg");
            SessionSettings settings = new SessionSettings(inputStream);
            MarketClientApplication marketClientApplication = new MarketClientApplication();
            if (runMdClient) {
                initiator = createMarketClientApp(marketClientApplication);
            }
            Application application = new Application(marketClientApplication);

            marketClientApplication.setOrderMatcher(application.getOrderMatcher());
            FileStoreFactory storeFactory = new FileStoreFactory(settings);
//            JdbcStoreFactory storeFactory = new JdbcStoreFactory(settings);

//            MessageStoreFactory storeFactory = MemoryStoreFactory.create(settings);
//            LogFactory logFactory = new ScreenLogFactory(settings);
            LogFactory logFactory = new FileLogFactory(settings);
//            LogFactory logFactory = new JdbcLogFactory(settings);//修改成JDBC

            SocketAcceptor acceptor = new SocketAcceptor(application, storeFactory, settings,
                    logFactory, new DefaultMessageFactory());

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            acceptor.start();
            label:
            while (true) {
                System.out.println("type #quit to quit");
                String value = in.readLine();
                if (value != null) {
                    switch (value) {
                        case "#symbols":
                            application.getOrderMatcher().display();
                            break;
                        case "#quit":
                            break label;
                        default:
                            application.getOrderMatcher().display();
                            break;
                    }
                }
            }
            acceptor.stop();
            initiator.stop();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ex) {
                // ignore on close
            }
        }
    }

    private static Initiator createMarketClientApp(MarketClientApplication application) {
        InputStream inputStream = null;

        inputStream = OrderMatcher.class.getResourceAsStream("marketclient.cfg");

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

//    public static synchronized void logon() {
//       if (!initiatorStarted) {
//            try {
//                initiator.start();
//                initiatorStarted = true;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            for (SessionID sessionId : initiator.getSessions()) {
//                Session.lookupSession(sessionId).logon();
//            }
//        }
//    }

}
