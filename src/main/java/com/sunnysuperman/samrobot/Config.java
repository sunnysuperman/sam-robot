package com.sunnysuperman.samrobot;

public class Config {
    private String authToken;
    private int deliveryType;
    private int connectTimeout = 3000;
    private int readTimeout = 3000;
    private int delay = 300;
    private int cartDelay = 1500;
    private int capacityDelay = 300;
    private boolean reloadCart = true;
    private int minAmount;
    private int logType = LogType.ALL;
    private String logPath;
    private boolean logApi;
    private String emailConfigPath;
    private String email;

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public int getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(int deliveryType) {
        this.deliveryType = deliveryType;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getCartDelay() {
        return cartDelay;
    }

    public void setCartDelay(int cartDelay) {
        this.cartDelay = cartDelay;
    }

    public int getCapacityDelay() {
        return capacityDelay;
    }

    public void setCapacityDelay(int capacityDelay) {
        this.capacityDelay = capacityDelay;
    }

    public boolean isReloadCart() {
        return reloadCart;
    }

    public void setReloadCart(boolean reloadCart) {
        this.reloadCart = reloadCart;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(int minAmount) {
        this.minAmount = minAmount;
    }

    public int getLogType() {
        return logType;
    }

    public void setLogType(int logType) {
        this.logType = logType;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public boolean isLogApi() {
        return logApi;
    }

    public void setLogApi(boolean logApi) {
        this.logApi = logApi;
    }

    public String getEmailConfigPath() {
        return emailConfigPath;
    }

    public void setEmailConfigPath(String emailConfigPath) {
        this.emailConfigPath = emailConfigPath;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
