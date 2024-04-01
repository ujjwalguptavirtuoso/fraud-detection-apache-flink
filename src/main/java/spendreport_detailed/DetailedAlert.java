package spendreport_detailed;

import java.util.Objects;

public final class DetailedAlert {

    private long id;

    private String zipCode;

    private long accountId;
    private long timestamp;
    private double amount;

    public DetailedAlert(){

    }

    public DetailedAlert(long id, String zipCode, long accountId, long timestamp, double amount) {
        this.id = id;
        this.zipCode = zipCode;
        this.accountId = accountId;
        this.timestamp = timestamp;
        this.amount = amount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetailedAlert that = (DetailedAlert) o;
        return id == that.id && accountId == that.accountId && timestamp == that.timestamp && Double.compare(amount, that.amount) == 0 && Objects.equals(zipCode, that.zipCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, zipCode, accountId, timestamp, amount);
    }

    @Override
    public String toString() {
        return "DetailedAlert{" +
                "id=" + id +
                ", zipCode='" + zipCode + '\'' +
                ", accountId=" + accountId +
                ", timestamp=" + timestamp +
                ", amount=" + amount +
                '}';
    }
}
