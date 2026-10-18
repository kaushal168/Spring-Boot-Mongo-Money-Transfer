import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;

public class MQDrain {
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
        
        // Uncomment and populate if your Dev queue requires authentication
        // MQEnvironment.userID = "your_username";
        // MQEnvironment.password = "your_password";

        try {
            System.out.println("Connecting to Queue Manager: " + qmgrName);
            MQQueueManager qmgr = new MQQueueManager(qmgrName);

            // Open the queue for input (consumption) and fail if queue manager is stopping
            int openOptions = MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_FAIL_IF_QUIESCING;
            System.out.println("Opening queue: " + queueName + "...\n");
            MQQueue queue = qmgr.accessQueue(queueName, openOptions);

            // Set options to get messages without waiting (NO_WAIT)
            MQGetMessageOptions gmo = new MQGetMessageOptions();
            gmo.options = MQConstants.MQGMO_NO_WAIT | MQConstants.MQGMO_FAIL_IF_QUIESCING;

            int count = 0;
            boolean keepRunning = true;

            // Loop and consume until empty
            while (keepRunning) {
                try {
                    MQMessage message = new MQMessage();
                    queue.get(message, gmo);
                    count++;
                } catch (MQException e) {
                    // Reason code 2033 (MQRC_NO_MSG_AVAILABLE) means the queue is empty
                    if (e.reasonCode == MQConstants.MQRC_NO_MSG_AVAILABLE) {
                        System.out.println("Queue is now completely empty.");
                        System.out.println("Total messages consumed: " + count);
                        keepRunning = false;
                    } else {
                        System.err.println("Encountered an MQException: CC=" + e.completionCode + " RC=" + e.reasonCode);
                        keepRunning = false;
                    }
                }
            }

            // Clean up
            queue.close();
            qmgr.disconnect();
            
        } catch (MQException e) {
            System.err.println("Failed to connect or access queue: CC=" + e.completionCode + " RC=" + e.reasonCode);
            // If you get RC=2035 (MQRC_NOT_AUTHORIZED), check your userID/password properties
            // If you get RC=2538 (MQRC_HOST_NOT_AVAILABLE), check your VPN/network connectivity
        }
    }
}