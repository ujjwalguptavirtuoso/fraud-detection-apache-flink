package spendreport_detailed;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * A job to detect fraudulent transactions from a stream of detailed transactions using Flink
 */
public class DetailedFraudDetectionJob {
    public static void main(String[] args) throws Exception {
        // Setting up the environment for Flink to run
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //Create the data stream from the custom data source (i.e. DetailedTransactionSource)
        DataStream<DetailedTransaction> transactions = env
                .addSource(new DetailedTransactionSource())
                .name("detailed-transactions");

        // Key the stream by account ID and process it using the DetailedFraudDetector
        // to ensure transactions from same account are processed in the same partition
        DataStream<DetailedAlert> alerts = transactions
                .keyBy(DetailedTransaction::getAccountId)
                .process(new DetailedFraudDetector())
                .name("detailed-fraud-detector");

        // Add a sink to the stream for the generated alerts of fraudulent transactions.
        alerts
                .addSink(new DetailedAlertSink())
                .name("send-detailed-alerts");

        // Execute the Flink job with the defined environment and data stream
        env.execute("Detailed Fraud Detection");
    }
}
