package spendreport_detailed;

import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Flink sink function for logging detailed alert information for fraudulent transactions.
 */
@PublicEvolving
public class DetailedAlertSink implements SinkFunction<DetailedAlert> {
    private static final long serialVersionUID = 1L;

    // Logger for this class. It uses LoggerFactory to create a logger instance based on the DetailedAlertSink class.
    private static final Logger LOG = LoggerFactory.getLogger(DetailedAlertSink.class);

    public DetailedAlertSink() {
    }

    /**
     * This method is called by Flink for each DetailedAlert object
     * sent to DetailedAlertSink. It logs the alert information at the INFO log level.
     * @param value
     * @param context
     */
    public void invoke(DetailedAlert value, SinkFunction.Context context) {
        LOG.info(value.toString());
    }
}
