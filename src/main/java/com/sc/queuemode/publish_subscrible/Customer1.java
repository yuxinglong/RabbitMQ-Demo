package com.sc.queuemode.publish_subscrible;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.sc.queuemode.connection.ConnectionUtil;

public class Customer1 {
	
	//队列1
	private final static String QUEUE_NAME = "publishSubscrible_queue";
	private final static String EXCHANGE_NAME = "publishSubscrible_exchange";

	public static void main(String[] args) throws IOException, TimeoutException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {
		Connection connection = ConnectionUtil.getConnection();
		Channel channel = connection.createChannel();
		//声明交换机
		channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
		//声明队列
		channel.queueDeclare(QUEUE_NAME, true, false, false, null);
		//绑定队列到交换机
		channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
		
		//同一时刻服务器只发送1条消息给消费者（能者多劳，消费消息快的，会消费更多的消息）
		//保证在接收端一个消息没有处理完时不会接收另一个消息，即消费者端发送了ack后才会接收下一个消息。
		//在这种情况下生产者端会尝试把消息发送给下一个空闲的消费者。
		channel.basicQos(1);
		
		//申明消费者
		Consumer consumer1 = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println("发布订阅 消费者1 消费消息："+message);
				//手动返回结果
				channel.basicAck(envelope.getDeliveryTag(), false);
			}
		};
		channel.basicConsume(QUEUE_NAME, false, consumer1);
	}
}
