package spendreport_detailed;


import org.apache.flink.streaming.api.functions.source.FromIteratorFunction;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Extends the FromIteratorFunction to create a source of DetailedTransaction objects.
  */
public class DetailedTransactionSource extends FromIteratorFunction<DetailedTransaction> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor initializes the DetailedTransactionSource with a RateLimitedIterator.
     * This iterator wraps another iterator to rate limit its consumption.
     */
    public DetailedTransactionSource() {
        super(new DetailedTransactionSource.RateLimitedIterator(DetailedTransactionIterator.unbounded()));
    }

    /**
     * Inner class that implements a rate-limiting for an iterator, to ensure that the iteration has a controlled speed,
     * simulating a real-world environment.
     * @param <T>
     */
    private static class RateLimitedIterator<T> implements Iterator<T>, Serializable {
        private static final long serialVersionUID = 1L;
        private final Iterator<T> inner;

        private RateLimitedIterator(Iterator<T> inner) {
            this.inner = inner;
        }

        public boolean hasNext() {
            return this.inner.hasNext();
        }

        /**
         * Retrieves the next element from the iterator.
         * Introduces a fixed delay (rate limit using sleep) before returning each element to simulate a controlled data flow
         */
        public T next() {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException var2) {
                throw new RuntimeException(var2);
            }

            return this.inner.next();
        }
    }
}
