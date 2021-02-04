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
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.fix44.*;


public class Application extends MessageCracker implements quickfix.Application {
    private OrderMatcher orderMatcher = null;
    private final IdGenerator generator = new IdGenerator();
    private MarketClientApplication app = null; //用于和大盘数据交换的变量

    public Application(MarketClientApplication app) {
        this.app = app;
        this.orderMatcher = new OrderMatcher(app);
    }

    public void fromAdmin(Message message, SessionID sessionId) {
        MatchUtil.printMsg(message);
    }

    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectTagValue, UnsupportedMessageType {
        MatchUtil.printMsg(message);
        crack(message, sessionId);
    }

    public void onMessage(NewOrderSingle message, SessionID sessionID) throws FieldNotFound, IncorrectTagValue, UnsupportedMessageType {
        orderMatcher.processNewOrderSingle(message, sessionID);
    }

    public void onMessage(OrderCancelRequest message, SessionID sessionID) throws FieldNotFound {
        orderMatcher.processOrderCancelRequest(message, sessionID);
    }

    public void onMessage(OrderCancelReplaceRequest message, SessionID sessionID) throws FieldNotFound {
        orderMatcher.processOrderCancelReplaceRequest(message, sessionID);
    }


    public void onCreate(SessionID sessionId) {
        System.out.println("Create - " + sessionId);
    }

    public void onLogon(SessionID sessionId) {
        System.out.println("Logon - " + sessionId);
    }

    public void onLogout(SessionID sessionId) {
        System.out.println("Logout - " + sessionId);
    }

    public void toAdmin(Message message, SessionID sessionId) {
        // empty
    }

    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        // empty
    }

    public OrderMatcher getOrderMatcher() {
        return this.orderMatcher;
    }
}
