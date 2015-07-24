/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package obtainSrcData;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import global.GlobalConfig;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author root
 */
public class Consumer {
    private static DefaultMQPushConsumer consumer;
    private static Logger logger;
    private LinkedBlockingQueue<byte[]> srcQueue;
    private GlobalConfig gConfig;
    public Consumer(GlobalConfig gc) {
        logger = LogManager.getLogger(Consumer.class);
        this.gConfig = gc;
        try {
            this.consumer = new DefaultMQPushConsumer("wbyq_consumer");
            this.srcQueue = new LinkedBlockingQueue<>(gConfig.maxBlockingQueueCapacity);
            
            consumer.setNamesrvAddr(gConfig.consumerNameSrv);
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
            consumer.setInstanceName(gConfig.consumerInstanceName);
            consumer.subscribe(gConfig.MQTopicName, "*");
            
            consumer.setConsumeThreadMin(1);
            consumer.setConsumeThreadMax(1);
            consumer.registerMessageListener(new MessageListenerConcurrently() {
            //int count = 0;

                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                        ConsumeConcurrentlyContext context) {
                    
                    MessageExt msg = msgs.get(0);                    
                    try {
                       // System.out.println("consumer bq size " + srcQueue.size());
                        srcQueue.put(msg.getBody());
                        //System.out.println("consumer blocking queue remaining capacity " + srcQueue.remainingCapacity());
                    } catch (InterruptedException ex) {
                       logger.info(ex);
                    }

                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            
        } catch (MQClientException ex) {
            logger.info(ex);
        }
        logger.debug("construct Consumer");
    }

    public LinkedBlockingQueue<byte[]> getSrcQueue() {
        return srcQueue;
    }
    
    public void start() {
        try {
            consumer.start();
        } catch (MQClientException ex) {
            logger.info(ex);
        }
        logger.debug("Consumer Started.");
    }
    

}
