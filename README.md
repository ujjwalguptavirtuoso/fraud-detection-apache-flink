**Important Points**

1. We have added accountId, timestamp and amount fields in the DetailedAlert class to show all the details for a fradulent transaction.
2. We have changed log4j properties to point to DetailedAlertSink instead of AlertSink.
3. Because of step 2, original FraudDetectorJob (not the detailed onem) logs will not print as it used AlertSink class in log4j, which is overriden by DetailedAlertSink.  