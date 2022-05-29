package com.sunnysuperman.samrobot.model;

import java.util.List;

public class GetCouponListResult {
    public static class Coupon {
        private String name;
        private String remark;
        private String ruleId;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getRuleId() {
            return ruleId;
        }

        public void setRuleId(String ruleId) {
            this.ruleId = ruleId;
        }
    }

    private List<Coupon> couponInfoList;

    public List<Coupon> getCouponInfoList() {
        return couponInfoList;
    }

    public void setCouponInfoList(List<Coupon> couponInfoList) {
        this.couponInfoList = couponInfoList;
    }

}
