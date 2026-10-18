import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;

public class MQDepthCheck {
    public static void main(String[] args) {
        // --- Configuration ---
        String host = "YOUR_HOST_IP";
        int port = 1414;
        String channel = "YOUR.CHANNEL.NAME";
        String qmgrName = "YOUR_QMGR_NAME";
        String queueName = "YOUR.QUEUE.NAME";
        // ---------------------

        // Set connection properties
        MQEnvironment.hostname = host;
        MQEnvironment.port = port;
        MQEnvironment.channel = channel;

        try {
            System.out.println("Connecting to Queue Manager: " + qmgrName);
            MQQueueManager qmgr = new MQQueueManager(qmgrName);

            // MQOO_INQUIRE is specifically required to read queue properties like depth
            int openOptions = MQConstants.MQOO_INQUIRE | MQConstants.MQOO_FAIL_IF_QUIESCING;
            MQQueue queue = qmgr.accessQueue(queueName, openOptions);

            // Fetch and print the exact number of messages currently on the queue
            int depth = queue.getCurrentDepth();
            System.out.println("Current Queue Depth for " + queueName + " : " + depth);

            // Clean up
            queue.close();
            qmgr.disconnect();

        } catch (MQException e) {
            System.err.println("Failed to inquire queue depth: CC=" + e.completionCode + " RC=" + e.reasonCode);
        }
    }
}