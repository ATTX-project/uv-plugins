/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.common;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author jkesanie
 */
public class RabbitMQClient implements MessagingClient {

    private Connection connection = null;
    private Channel channel = null;
    private String replyQueueName;
    
    public RabbitMQClient(String brokerURL, String username, String password, String provQueueName) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(brokerURL);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connection = connectionFactory.newConnection();
        
        channel = connection.createChannel();
        replyQueueName = channel.queueDeclare().getQueue();
    }    
    
    @Override
    public void sendProvMessage(String content) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String sendSyncServiceMessage(String content, String targetQueue, int timeout) throws Exception {
        String corrId = UUID.randomUUID().toString();
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();
        channel.basicPublish("", targetQueue, props, content.getBytes("UTF-8"));

        final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);

        channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                if (properties.getCorrelationId().equals(corrId)) {
                    response.offer(new String(body, "UTF-8"));
                }
            }
        });

        return response.take();        
    }
    
    public void close() throws IOException {
        connection.close();
    }    
}
