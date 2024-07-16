package spendreport_detailed;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * A keyed process function that analyses DetailedTransaction events and emits DetailedAlerts for suspected fraudulent transactions.
 */
public class DetailedFraudDetector extends KeyedProcessFunction<Long, DetailedTransaction, DetailedAlert> {

    private static final long serialVersionUID = 1L;

    private static final double SMALL_AMOUNT = 1.00;
    private static final double LARGE_AMOUNT = 500.00;
    private static final long ONE_MINUTE = 60 * 1000;

    private transient ValueState<Boolean> flagState;
    private transient ValueState<Long> timerState;
    private transient ValueState<String> lastZipCodeState;

    /**
     * Initializing the state descriptors, which stores states required for Flink application.
     * @param parameters
     */
    @Override
    public void open(Configuration parameters) {
        //Descriptor for flagState, indicating if the last transaction was small.
        ValueStateDescriptor<Boolean> flagDescriptor = new ValueStateDescriptor<>(
                "flag",
                BasicTypeInfo.BOOLEAN_TYPE_INFO
        );
        flagState = getRuntimeContext().getState(flagDescriptor);

        // Descriptor for timerState, used for setting timers to detect fraud within a time window.
        ValueStateDescriptor<Long> timerDescriptor = new ValueStateDescriptor<>(
                "timer-state",
                BasicTypeInfo.LONG_TYPE_INFO
        );
        timerState = getRuntimeContext().getState(timerDescriptor);

        //Initialize zipCodeState to store the zip code of the last transaction
        ValueStateDescriptor<String> zipCodeDescriptor = new ValueStateDescriptor<>(
                "zip-code",
                BasicTypeInfo.STRING_TYPE_INFO
        );
        lastZipCodeState = getRuntimeContext().getState(zipCodeDescriptor);
    }

    /** Process each transaction with the business logic to check if it is a fraudulent transaction */
    @Override
    public void processElement(
            DetailedTransaction transaction,
            Context context,
            Collector<DetailedAlert> collector) throws Exception {

        // Get the current state for the last transaction
        Boolean lastTransactionWasSmall = flagState.value();
        String lastZipCode = lastZipCodeState.value();

        // checking if last transaction was small and not null and also checking if the transaction was done from the
        // same zip code, also checking if the current transaction is greater than the Large amount
        if (lastTransactionWasSmall != null && lastZipCode != null && lastZipCode.equals(transaction.getZipCode())) {
            if (transaction.getAmount() > LARGE_AMOUNT) {
                //Output an alert downstream
                DetailedAlert alert = new DetailedAlert();
                alert.setId(transaction.getAccountId());
                alert.setAccountId(transaction.getAccountId());
                alert.setAmount(transaction.getAmount());
                alert.setTimestamp(transaction.getTimestamp());
                alert.setZipCode(transaction.getZipCode());

                collector.collect(alert);
            }
            //clean up the context
            cleanUp(context);
        }
        //checking if the current transaction is of a small amount, setting the flag true and setting the timer
        else if (transaction.getAmount() < SMALL_AMOUNT) {
            flagState.update(true);
            lastZipCodeState.update(transaction.getZipCode());

            long timer = context.timerService().currentProcessingTime() + ONE_MINUTE;
            context.timerService().registerProcessingTimeTimer(timer);
            timerState.update(timer);
        }
    }

    /**
     * // Called when the timer set in processElement expires after 1 min, indicating no large transaction followed a small one within the window.
     * @param timestamp
     * @param ctx
     * @param out
     */
    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<DetailedAlert> out) {
        // remove flag after 1 minute
        flagState.clear();
        timerState.clear();
        lastZipCodeState.clear();
    }

    /**
     * Helper method to cleanup the state variable and the cancel the timer
     * @param ctx
     * @throws Exception
     */
    private void cleanUp(Context ctx) throws Exception {
        // delete timer
        Long timer = timerState.value();
        ctx.timerService().deleteProcessingTimeTimer(timer);

        //clear up all the states
        flagState.clear();
        timerState.clear();
        lastZipCodeState.clear();
    }
}

