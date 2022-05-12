package com.sunnysuperman.samrobot.model;

public class CreateOrderResult {
    public static final CreateOrderResult OK = new CreateOrderResult(true);
    public static final CreateOrderResult ERROR_OTHERS = new CreateOrderResult(false, false);
    public static final CreateOrderResult ERROR_NOCAPACITY = new CreateOrderResult(false, true);

    private boolean ok;
    private boolean noCapacity;

    private CreateOrderResult(boolean ok) {
        super();
        this.ok = ok;
    }

    private CreateOrderResult(boolean ok, boolean noCapacity) {
        super();
        this.ok = ok;
        this.noCapacity = noCapacity;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public boolean isNoCapacity() {
        return noCapacity;
    }

    public void setNoCapacity(boolean noCapacity) {
        this.noCapacity = noCapacity;
    }

}
