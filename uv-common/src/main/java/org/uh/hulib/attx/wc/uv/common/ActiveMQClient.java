/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.common;

import java.util.Random;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;


/**
 *
 * @author jkesanie
 */
public class ActiveMQClient implements MessagingClient {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String PROV_QUEUE = "provenance.inbox";
    
    private static final int ACK_MODE = Session.AUTO_ACKNOWLEDGE;
    private static final boolean TRANSACTED = false;
    
    
    private Connection connection;
    private Session session;
    
    private MessageProducer producer;
    private MessageConsumer tempQueueConsumer;
    
    private Destination tempQueue;    
    private Destination provQueue;
    
    public ActiveMQClient() throws Exception {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);        
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(TRANSACTED, ACK_MODE);       

        producer = session.createProducer(null);
        tempQueue = session.createTemporaryQueue();
        provQueue = session.createQueue(PROV_QUEUE);        
        tempQueueConsumer = session.createConsumer(tempQueue);        
    }
    
    public Message createMessage(String content, boolean useRequestResponse) throws Exception {
        TextMessage msg = session.createTextMessage();  
        msg.setText(content);
        if(useRequestResponse) {
            msg.setStringProperty("correlation-id", createRandomString());
            msg.setStringProperty("reply-to", tempQueue.toString());
        }
        return msg;
    }
    
    @Override
    public void sendProvMessage(String content) throws Exception {        
        producer.send(provQueue, createMessage(content, false));
    }
    
    @Override
    public void sendSyncServiceMessage(String content, String targetQueue, int timeout) throws Exception {
        Message request = createMessage(content, true);
        this.producer.send(session.createQueue(targetQueue), request);
        Message response = tempQueueConsumer.receive(timeout);
        if(response == null) {
            throw new Exception("No response received within timeout!");
        }
    }
    
    private String createRandomString() {
        Random random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return "cor_" + Math.abs(randomLong);
    }  
        

    
}
