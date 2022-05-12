package com.sunnysuperman.samrobot.model;

public class Store {
    String storeId;
    String storeName;
    Integer storeType;
    StoreDeliveryTemplate storeRecmdDeliveryTemplateData;
    StoreDeliveryMode storeDeliveryModeVerifyData;
    StoreAreaBlock storeAreaBlockVerifyData;

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public Integer getStoreType() {
        return storeType;
    }

    public void setStoreType(Integer storeType) {
        this.storeType = storeType;
    }

    public StoreDeliveryTemplate getStoreRecmdDeliveryTemplateData() {
        return storeRecmdDeliveryTemplateData;
    }

    public void setStoreRecmdDeliveryTemplateData(StoreDeliveryTemplate storeRecmdDeliveryTemplateData) {
        this.storeRecmdDeliveryTemplateData = storeRecmdDeliveryTemplateData;
    }

    public StoreDeliveryMode getStoreDeliveryModeVerifyData() {
        return storeDeliveryModeVerifyData;
    }

    public void setStoreDeliveryModeVerifyData(StoreDeliveryMode storeDeliveryModeVerifyData) {
        this.storeDeliveryModeVerifyData = storeDeliveryModeVerifyData;
    }

    public StoreAreaBlock getStoreAreaBlockVerifyData() {
        return storeAreaBlockVerifyData;
    }

    public void setStoreAreaBlockVerifyData(StoreAreaBlock storeAreaBlockVerifyData) {
        this.storeAreaBlockVerifyData = storeAreaBlockVerifyData;
    }

}