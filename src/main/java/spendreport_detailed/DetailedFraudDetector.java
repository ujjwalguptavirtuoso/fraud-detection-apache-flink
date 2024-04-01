package spendreport_detailed;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.sql.Types;

public class DetailedFraudDetector extends KeyedProcessFunction<Long, DetailedTransaction, DetailedAlert> {

    private static final long serialVersionUID = 1L;

    private static final double SMALL_AMOUNT = 1.00;
    private static final double LARGE_AMOUNT = 500.00;
    private static final long ONE_MINUTE = 60 * 1000;

    private transient ValueState<Boolean> flagState;
    private transient ValueState<Long> timerState;
    private transient ValueState<String> lastZipCodeState;

    @Override
    public void open(Configuration parameters) {
        ValueStateDescriptor<Boolean> flagDescriptor = new ValueStateDescriptor<>(
                "flag",
                BasicTypeInfo.BOOLEAN_TYPE_INFO
        );
        flagState = getRuntimeContext().getState(flagDescriptor);

        ValueStateDescriptor<Long> timerDescriptor = new ValueStateDescriptor<>(
                "timer-state",
                BasicTypeInfo.LONG_TYPE_INFO
        );
        timerState = getRuntimeContext().getState(timerDescriptor);

        ValueStateDescriptor<String> zipCodeDescriptor = new ValueStateDescriptor<>(
                "zip-code",
                BasicTypeInfo.STRING_TYPE_INFO
        );
        lastZipCodeState = getRuntimeContext().getState(zipCodeDescriptor);
    }

    @Override
    public void processElement(
            DetailedTransaction transaction,
            Context context,
            Collector<DetailedAlert> collector) throws Exception {

        Boolean lastTransactionWasSmall = flagState.value();
        String lastZipCode = lastZipCodeState.value();

        if (lastTransactionWasSmall != null && lastZipCode != null && lastZipCode.equals(transaction.getZipCode())) {
            if (transaction.getAmount() > LARGE_AMOUNT) {
                DetailedAlert alert = new DetailedAlert();
                alert.setId(transaction.getAccountId());
                alert.setAccountId(transaction.getAccountId());
                alert.setAmount(transaction.getAmount());
                alert.setTimestamp(transaction.getTimestamp());
                alert.setZipCode(transaction.getZipCode());

                collector.collect(alert);
            }

            cleanUp(context);
        } else if (transaction.getAmount() < SMALL_AMOUNT) {
            flagState.update(true);
            lastZipCodeState.update(transaction.getZipCode());

            long timer = context.timerService().currentProcessingTime() + ONE_MINUTE;
            context.timerService().registerProcessingTimeTimer(timer);
            timerState.update(timer);
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<DetailedAlert> out) {
        flagState.clear();
        timerState.clear();
        lastZipCodeState.clear();
    }

    private void cleanUp(Context ctx) throws Exception {
        Long timer = timerState.value();
        ctx.timerService().deleteProcessingTimeTimer(timer);

        flagState.clear();
        timerState.clear();
        lastZipCodeState.clear();
    }
}

