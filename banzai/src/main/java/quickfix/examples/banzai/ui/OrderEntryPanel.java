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

package quickfix.examples.banzai.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.examples.banzai.*;
import quickfix.field.*;
import quickfix.fix44.MarketDataRequest;
import quickfix.fix44.SecurityDefinitionRequest;

@SuppressWarnings("unchecked")
public class OrderEntryPanel extends JPanel implements Observer {
    private boolean symbolEntered = false;
    private boolean quantityEntered = true;
    private boolean limitEntered = false;
    private boolean stopEntered = false;
    private boolean sessionEntered = false;

    private final JTextField symbolTextField = new JTextField();
    private final IntegerNumberTextField quantityTextField = new IntegerNumberTextField();

    public void setSymbolTestFieldValue(String value) {
        symbolTextField.setText(value);
        symbolEntered = true;
        activateSubmit();

    }

    private final JComboBox sideComboBox = new JComboBox(OrderSide.toArray());
    private final JComboBox typeComboBox = new JComboBox(OrderType.toArray());
    private final JComboBox tifComboBox = new JComboBox(OrderTIF.toArray());

    private final JTextField limitPriceTextField = new JTextField();
    //    private final DoubleNumberTextField limitPriceTextField = new DoubleNumberTextField();
    private final DoubleNumberTextField stopPriceTextField = new DoubleNumberTextField();

    private final JComboBox sessionComboBox = new JComboBox();

    private final JLabel limitPriceLabel = new JLabel("Price");
    private final JLabel stopPriceLabel = new JLabel("Stop");

    private final JLabel messageLabel = new JLabel(" ");
    private final JButton submitButton = new JButton("Submit");
    private final JButton subscribeButton = new JButton("Subscribe");
    private final JButton clearButton = new JButton("clear");
    private final JButton sendButton = new JButton("send");

    private OrderTableModel orderTableModel = null;
    private transient BanzaiApplication application = null;
    private transient MarketClientApplication marketClientApplication = null;

    private final GridBagConstraints constraints = new GridBagConstraints();


    public OrderEntryPanel(final OrderTableModel orderTableModel,
                           final BanzaiApplication application, MarketClientApplication marketClientApplication) {
        setName("OrderEntryPanel");
        this.orderTableModel = orderTableModel;
        this.application = application;
        this.marketClientApplication = marketClientApplication;
        if (this.marketClientApplication != null)
            marketClientApplication.setOrderTableModel(orderTableModel);

        application.addLogonObserver(this);

        SubmitActivator activator = new SubmitActivator();
        symbolTextField.addKeyListener(activator);
        quantityTextField.addKeyListener(activator);
        limitPriceTextField.addKeyListener(activator);
        stopPriceTextField.addKeyListener(activator);
        sessionComboBox.addItemListener(activator);

        setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        setLayout(new GridBagLayout());
        createComponents();
    }

    public void setSymbolTextField(String value) {
        symbolTextField.setText(value);
    }

    public void addActionListener(ActionListener listener) {
        submitButton.addActionListener(listener);
        subscribeButton.addActionListener(listener);
        clearButton.addActionListener(listener);
        sendButton.addActionListener(listener);

    }

    public void setMessage(String message) {
        messageLabel.setText(message);
        if (message == null || message.equals(""))
            messageLabel.setText(" ");
    }

    public void clearMessage() {
        setMessage(null);
    }

    private void createComponents() {
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;

        int x = 0;
        int y = 0;

        add(new JLabel("Symbol"), x, y);
        add(new JLabel("Quantity"), ++x, y);
        add(new JLabel("Side"), ++x, y);
        add(new JLabel("Type"), ++x, y);
        constraints.ipadx = 30;
        add(limitPriceLabel, ++x, y);
        add(stopPriceLabel, ++x, y);
        constraints.ipadx = 0;
        add(new JLabel("TIF"), ++x, y);
        constraints.ipadx = 30;

        symbolTextField.setName("SymbolTextField");
        add(symbolTextField, x = 0, ++y);
        constraints.ipadx = 0;
        quantityTextField.setName("QuantityTextField");
        quantityTextField.setText("1");
        add(quantityTextField, ++x, y);
        sideComboBox.setName("SideComboBox");
        add(sideComboBox, ++x, y);
        typeComboBox.setName("TypeComboBox");
        limitEntered = true;
        add(typeComboBox, ++x, y);
        limitPriceTextField.setName("LimitPriceTextField");
        limitPriceTextField.setText("111.25");
        add(limitPriceTextField, ++x, y);
        stopPriceTextField.setName("StopPriceTextField");
        add(stopPriceTextField, ++x, y);
        tifComboBox.setName("TifComboBox");
        add(tifComboBox, ++x, y);

        constraints.insets = new Insets(3, 0, 0, 0);
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        sessionComboBox.setName("SessionComboBox");
        add(sessionComboBox, 0, ++y);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        submitButton.setName("SubmitButton");
        add(submitButton, x, y);
        subscribeButton.setName("SubscribeButton");
        add(subscribeButton, x, ++y);
        clearButton.setName("clearButton");
        add(clearButton, x, ++y);
        sendButton.setName("sendButton");
        add(sendButton, x, ++y);
        constraints.gridwidth = 0;
        add(messageLabel, 0, ++y);

        typeComboBox.addItemListener(new PriceListener());
        typeComboBox.setSelectedItem(OrderType.STOP);
        typeComboBox.setSelectedItem(OrderType.LIMIT);

        Font font = new Font(messageLabel.getFont().getFontName(), Font.BOLD, 12);
        messageLabel.setFont(font);
        messageLabel.setForeground(Color.red);
        messageLabel.setHorizontalAlignment(JLabel.CENTER);
//        submitButton.setEnabled(false);
        submitButton.addActionListener(new SubmitListener());
        subscribeButton.addActionListener(new SubscribeListener());
        clearButton.addActionListener(new ClearListener());
        sendButton.addActionListener(new SendListener());
        symbolTextField.setEnabled(false);
        activateSubmit();
    }

    private JComponent add(JComponent component, int x, int y) {
        constraints.gridx = x;
        constraints.gridy = y;
        add(component, constraints);
        return component;
    }

    private void activateSubmit() {
        OrderType type = (OrderType) typeComboBox.getSelectedItem();
        boolean activate = symbolEntered && quantityEntered && sessionEntered;

        if (type == OrderType.MARKET)
            submitButton.setEnabled(activate);
        else if (type == OrderType.LIMIT)
            submitButton.setEnabled(activate && limitEntered);
        else if (type == OrderType.STOP)
            submitButton.setEnabled(activate && stopEntered);
        else if (type == OrderType.STOP_LIMIT)
            submitButton.setEnabled(activate && limitEntered && stopEntered);
    }

    private class PriceListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            OrderType item = (OrderType) typeComboBox.getSelectedItem();
            if (item == OrderType.MARKET) {
                enableLimitPrice(false);
                enableStopPrice(false);
            } else if (item == OrderType.STOP) {
                enableLimitPrice(false);
                enableStopPrice(true);
            } else if (item == OrderType.LIMIT) {
                enableLimitPrice(true);
                enableStopPrice(false);
            } else {
                enableLimitPrice(true);
                enableStopPrice(true);
            }
            activateSubmit();
        }

        private void enableLimitPrice(boolean enabled) {
            Color labelColor = enabled ? Color.black : Color.gray;
            Color bgColor = enabled ? Color.white : Color.gray;
            limitPriceTextField.setEnabled(enabled);
            limitPriceTextField.setBackground(bgColor);
            limitPriceLabel.setForeground(labelColor);
        }

        private void enableStopPrice(boolean enabled) {
            Color labelColor = enabled ? Color.black : Color.gray;
            Color bgColor = enabled ? Color.white : Color.gray;
            stopPriceTextField.setEnabled(enabled);
            stopPriceTextField.setBackground(bgColor);
            stopPriceLabel.setForeground(labelColor);
        }
    }

    public void update(Observable o, Object arg) {
        LogonEvent logonEvent = (LogonEvent) arg;
        if (logonEvent.isLoggedOn())
            sessionComboBox.addItem(logonEvent.getSessionID());
        else
            sessionComboBox.removeItem(logonEvent.getSessionID());
    }

    private class SubscribeListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.out.println("subscribe is running!");
            sendMarketDataRequest();
            subscribeButton.setEnabled(false);
        }
    }

    private class ClearListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.out.println("clear is running!");
            sendCancelDataRequest();
        }
    }

    private class SendListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.out.println("send is running!");
            String str1 = "8=FIX.4.4\u00019=142\u000135=D\u000134=69\u000149=N2N\u000152=20210206-02:12:04.215\u000156=FEME\u000111=1612577524199\u000121=1\u000138=1\u000140=2\u000144=111.25\u000154=1\u000155=FMG3-DEC20\u000159=0\u000160=20210206-02:12:04.212\u000110=073\u0001";
            String str2="8=FIX.4.4\u00019=142\u000135=D\u000134=70\u000149=N2N\u000152=20210206-02:13:21.524\u000156=FEME\u000111=1612577601535\u000121=1\u000138=1\u000140=2\u000144=110.15\u000154=2\u000155=FMG3-MAR21\u000159=0\u000160=20210206-02:13:21.523\u000110=083\u0001";
            String str3="8=FIX.4.4\u00019=145\u000135=D\u000134=71\u000149=N2N\u000152=20210206-02:14:03.572\u000156=FEME\u000111=1612577643586\u000121=1\u000138=1\u000140=2\u000144=1.5\u000154=2\u000155=FMG3-DEC20-MAR21\u000159=0\u000160=20210206-02:14:03.572\u000110=053\u0001";
            String str4="8=FIX.4.4\u00019=142\u000135=D\u000134=72\u000149=N2N\u000152=20210206-02:17:14.602\u000156=FEME\u000111=1612577834617\u000121=1\u000138=1\u000140=2\u000144=109.95\u000154=1\u000155=FMG3-JUN21\u000159=0\u000160=20210206-02:17:14.602\u000110=129\u0001";
            String str5="8=FIX.4.4\u00019=142\u000135=D\u000134=73\u000149=N2N\u000152=20210206-02:18:59.381\u000156=FEME\u000111=1612577939399\u000121=1\u000138=1\u000140=2\u000144=109.35\u000154=2\u000155=FMG3-MAR21\u000159=0\u000160=20210206-02:18:59.381\u000110=153\u0001";
           String str6="8=FIX.4.4\u00019=197\u000135=8\u000134=40020\u000149=FEME\u000152=20210206-12:26:15.406\u000156=N2N\u00016=0\u000111=06a57e598c1643b4a805d5ae85892ba8\u000114=0\u000117=13\u000137=06a57e598c1643b4a805d5ae85892ba8\u000138=1\u000139=0\u000140=2\u000144=110.05\u000154=2\u000155=FMG3-JUN21\u0001150=0\u0001151=1\u000110=200";

            Message msg = null;
            try {
                SessionID sessionID= (SessionID) sessionComboBox.getSelectedItem();
                msg = new Message(str1);
                application.sendMessage(msg,sessionID);
                msg = new Message(str2);
                application.sendMessage(msg,sessionID);

//                msg = new Message(str3);
//                application.sendMessage(msg,sessionID);
//
//                msg = new Message(str4);
//                application.sendMessage(msg,sessionID);
//
//                msg = new Message(str5);
//                application.sendMessage(msg,sessionID);
//
//                msg = new Message(str6);
//                application.sendMessage(msg,sessionID);

            } catch (InvalidMessage invalidMessage)
            {
                invalidMessage.printStackTrace();
            }

        }
    }

    private void sendSecurityDefinitionRequest() {
//        String session = "FIX.4.4:MD_BANZAI_CLIENT->FEMD";
        SessionID sessionId = new SessionID("FIX.4.4", "MD_BANZAI_CLIENT", "FEMD");
        SecurityReqID securityReqID = new SecurityReqID(Util.generateID());
        SecurityRequestType requestType = new SecurityRequestType(SecurityRequestType.REQUEST_LIST_SECURITIES);
        SecurityDefinitionRequest message = new SecurityDefinitionRequest(securityReqID, requestType);
        marketClientApplication.sendSubscribe(message, sessionId);
    }

    private void sendCancelDataRequest() {
//        orderTableModel.deleteData();
        Map<Integer, Order> map = orderTableModel.getCancelData();
        if (map != null) {
            for (Map.Entry<Integer, Order> entry : map.entrySet()) {
                Order order = entry.getValue();
                orderTableModel.removeOneRow(entry.getKey());
                application.cancel(order);
            }
        }

    }

    /**
     * 发送大盘数据请求给MargetData
     */
    private void sendMarketDataRequest() {
        MDReqID reqID = new MDReqID(Util.generateID());
        SubscriptionRequestType requestType = new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_UPDATES);
        MarketDepth marketDepth = new MarketDepth(0);
        MarketDataRequest request = new MarketDataRequest(reqID, requestType, marketDepth);

        MarketDataRequest.NoMDEntryTypes group = new MarketDataRequest.NoMDEntryTypes();
        group.setChar(MDEntryType.FIELD, MDEntryType.BID);
        request.addGroup(group);

        MarketDataRequest.NoRelatedSym noRelatedSym = new MarketDataRequest.NoRelatedSym();
        noRelatedSym.setString(Symbol.FIELD, "All");//设置默认symbols
        request.addGroup(noRelatedSym);

        SessionID sessionId = new SessionID("FIX.4.4", "MD_BANZAI_CLIENT", "FEMD");
        marketClientApplication.sendSubscribe(request, sessionId);
    }

    private class SubmitListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Order order = new Order();
            order.setSide((OrderSide) sideComboBox.getSelectedItem());
            order.setType((OrderType) typeComboBox.getSelectedItem());
            order.setTIF((OrderTIF) tifComboBox.getSelectedItem());

            order.setSymbol(symbolTextField.getText());
            order.setQuantity(Integer.parseInt(quantityTextField.getText()));
            order.setOpen(order.getQuantity());

            OrderType type = order.getType();
            if (type == OrderType.LIMIT || type == OrderType.STOP_LIMIT)
                order.setLimit(limitPriceTextField.getText());
            if (type == OrderType.STOP || type == OrderType.STOP_LIMIT)
                order.setStop(stopPriceTextField.getText());
            order.setSessionID((SessionID) sessionComboBox.getSelectedItem());

            orderTableModel.addOrder(order);
            application.send(order);
        }
    }

    private class SubmitActivator implements KeyListener, ItemListener {
        public void keyReleased(KeyEvent e) {
            Object obj = e.getSource();
            if (obj == symbolTextField) {
                symbolEntered = testField(obj);
            } else if (obj == quantityTextField) {
                quantityEntered = testField(obj);
            } else if (obj == limitPriceTextField) {
                limitEntered = testField(obj);
            } else if (obj == stopPriceTextField) {
                stopEntered = testField(obj);
            }
            activateSubmit();
        }

        public void itemStateChanged(ItemEvent e) {
            sessionEntered = sessionComboBox.getSelectedItem() != null;
            activateSubmit();
        }

        private boolean testField(Object o) {
            String value = ((JTextField) o).getText();
            value = value.trim();
            return value.length() > 0;
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
        }
    }
}
