package com.framework;

public class NotificationData {

    private String provider;
    private String startDate;
    private String endDate;
    private String remark;

    public NotificationData() {
    }

    public NotificationData(String provider,
                            String startDate,
                            String endDate,
                            String remark) {

        this.provider = provider;
        this.startDate = startDate;
        this.endDate = endDate;
        this.remark = remark;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "NotificationData{" +
                "provider='" + provider + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}