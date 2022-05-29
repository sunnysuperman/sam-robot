package com.sunnysuperman.samrobot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sunnysuperman.commons.bean.Bean;
import com.sunnysuperman.commons.util.FileUtil;
import com.sunnysuperman.commons.util.FormatUtil;
import com.sunnysuperman.commons.util.JSONUtil;
import com.sunnysuperman.commons.util.StringUtil;
import com.sunnysuperman.http.client.HttpClient;
import com.sunnysuperman.http.client.HttpTextResult;
import com.sunnysuperman.samrobot.log.Logger;
import com.sunnysuperman.samrobot.model.CapacityResult;
import com.sunnysuperman.samrobot.model.CreateOrderResult;
import com.sunnysuperman.samrobot.model.DeliveryAddress;
import com.sunnysuperman.samrobot.model.GetCouponListResult;
import com.sunnysuperman.samrobot.model.Store;
import com.sunnysuperman.samrobot.model.StoreWrap;

public class Api {
    private static final String DEVICE_ID = StringUtil.randomString("0123456789abcdef", 36);
    private static final String UID = "2" + StringUtil.randomString("0123456789", 8);

    private HttpClient httpClient;
    private String authToken;
    private Logger logger;

    public Api(HttpClient httpClient, String authToken, Logger logger) {
        super();
        this.httpClient = httpClient;
        this.authToken = authToken;
        this.logger = logger;
    }

    private Map<String, Object> getHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("auth-token", authToken);
        headers.put("app-version", "5.0.45.1");
        headers.put("device-id", DEVICE_ID);
        headers.put("device-type", "ios");
        headers.put("apptype", "ios");
        headers.put("device-name", "iPhone10,3");
        headers.put("device-os-version", "15.3.1");
        return headers;
    }

    private Map<String, Object> doPost(String url, Map<String, Object> request) throws Exception {
        HttpTextResult response = httpClient.postJSON(url, JSONUtil.toJSONString(request), getHeaders());
        if (logger != null) {
            logger.log("请求接口返回 " + url + "\n" + response.getBody());
        }
        Map<String, Object> result = JSONUtil.parseJSONObject(response.getBody());
        return result;
    }

    private Map<?, ?> post(String url, Map<String, Object> request) throws Exception {
        Map<String, Object> result = doPost(url, request);
        if (!FormatUtil.parseBoolean(result.get("success"))) {
            return null;
        }
        Map<?, ?> data = (Map<?, ?>) result.get("data");
        return data;
    }

    private <T> T post(String url, Map<String, Object> request, Class<T> clazz) throws Exception {
        Map<?, ?> data = post(url, request);
        if (data == null) {
            return null;
        }
        return Bean.fromMap(data, clazz.newInstance());
    }

    // 加载收货地址
    public DeliveryAddress getDeliveryAddressDetail() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("uid", UID);
        return post("https://api-sams.walmartmobile.cn/api/v1/sams/trade/cart/getDeliveryAddressDetail", request,
                DeliveryAddress.class);
    }

    // 获取门店列表
    public StoreWrap getRecommendStoreListByLocation(String longitude, String latitude) throws Exception {
        double longitudeAsDouble = new BigDecimal(longitude).setScale(6, RoundingMode.HALF_UP).doubleValue();
        double latitudeAsDouble = new BigDecimal(latitude).setScale(6, RoundingMode.HALF_UP).doubleValue();
        Map<String, Object> request = new HashMap<>();
        request.put("longitude", longitudeAsDouble);
        request.put("latitude", latitudeAsDouble);
        return post("https://api-sams.walmartmobile.cn/api/v1/sams/merchant/storeApi/getRecommendStoreListByLocation",
                request, StoreWrap.class);
    }

    // 获取支付方式ID
    public String getPaymentMethodId(String storeId) throws Exception {
        String template = FileUtil.read(Api.class.getResourceAsStream("/sam/get-payments.json"));
        Map<String, Object> request = JSONUtil.parseJSONObject(template);
        request.put("storeId", storeId);
        Map<?, ?> data = post("https://api-sams.walmartmobile.cn/api/v1/sams/configuration/paymentStore4C/querylist",
                request);
        if (data == null) {
            return null;
        }
        List<?> paymentStoreInfoList = (List<?>) data.get("paymentStoreInfoList");
        for (Object paymentStoreInfo : paymentStoreInfoList) {
            Map<?, ?> paymentStoreInfoAsMap = (Map<?, ?>) paymentStoreInfo;
            if (paymentStoreInfoAsMap.get("channel").toString().equals("alipay")) {
                return paymentStoreInfoAsMap.get("subSaasId").toString();
            }
        }
        return null;
    }

    // TODO 购物车结构有点复杂，为了简单起见，这里只返回JSONObject，后续再考虑优化
    // 获取购物车
    public Map<?, ?> getCart(DeliveryAddress sAddress, List<Store> storeList, int deliveryType) throws Exception {
        String template = FileUtil.read(Api.class.getResourceAsStream("/sam/query-cart.json"));
        Map<String, Object> request = JSONUtil.parseJSONObject(template);
        // 指定配送类型
        if (deliveryType > 0) {
            request.put("deliveryType", String.valueOf(deliveryType));
        }
        request.put("uid", UID);
        request.put("homePagelongitude", sAddress.getLongitude());
        request.put("homePagelatitude", sAddress.getLatitude());
        List<Map<String, Object>> convertStoreList = new ArrayList<>();
        for (Store store : storeList) {
            Map<String, Object> convertStore = new HashMap<>();
            convertStore.put("storeType", store.getStoreType());
            convertStore.put("storeId", store.getStoreId());
            convertStore.put("areaBlockId", store.getStoreAreaBlockVerifyData().getAreaBlockId());
            convertStore.put("storeDeliveryTemplateId",
                    store.getStoreRecmdDeliveryTemplateData().getStoreDeliveryTemplateId());
            convertStore.put("deliveryModeId", store.getStoreDeliveryModeVerifyData().getDeliveryModeId());
            convertStoreList.add(convertStore);
        }
        request.put("storeList", convertStoreList);
        return post("https://api-sams.walmartmobile.cn/api/v1/sams/trade/cart/getUserCart", request);
    }

    // 获取优惠券列表
    public GetCouponListResult getCouponList() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("status", "1");
        request.put("uid", UID);
        request.put("pageSize", 20);
        request.put("pageNum", 1);
        return post("https://api-sams.walmartmobile.cn/api/v1/sams/coupon/coupon/query", request,
                GetCouponListResult.class);
    }

    // 获取运力
    public CapacityResult getCapacityData(String storeDeliveryTemplateId, List<String> perDateList) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("storeDeliveryTemplateId", storeDeliveryTemplateId);
        request.put("perDateList", perDateList);
        Map<?, ?> data = post("https://api-sams.walmartmobile.cn/api/v1/sams/delivery/portal/getCapacityData", request);
        if (data == null) {
            return null;
        }
        List<?> capcityResponseList = (List<?>) data.get("capcityResponseList");
        CapacityResult result = new CapacityResult();
        for (Object item : capcityResponseList) {
            Map<?, ?> capcityResponse = (Map<?, ?>) item;
            if (FormatUtil.parseBoolean(capcityResponse.get("dateISFull"))) {
                continue;
            }
            List<?> list = (List<?>) capcityResponse.get("list");
            if (list == null) {
                continue;
            }
            for (Object timeItem : list) {
                Map<?, ?> timeItemMap = (Map<?, ?>) timeItem;
                if (FormatUtil.parseBoolean(timeItemMap.get("timeISFull"))) {
                    continue;
                }
                result.setTime(new String[] { timeItemMap.get("startRealTime").toString(),
                        timeItemMap.get("endRealTime").toString() });
                return result;
            }
        }
        return result;
    }

    public CreateOrderResult createOrder(Map<String, Object> request) throws Exception {
        request.put("uid", UID);
        Map<String, Object> result = doPost("https://api-sams.walmartmobile.cn/api/v1/sams/trade/settlement/commitPay",
                request);
        if (FormatUtil.parseBoolean(result.get("success"))) {
            return CreateOrderResult.OK;
        }
        if ("NOT_DELIVERY_CAPACITY_ERROR".equals(FormatUtil.parseString(result.get("code")))
                || "NO_MATCH_DELIVERY_MODE".equals(FormatUtil.parseString(result.get("code")))) {
            return CreateOrderResult.ERROR_NOCAPACITY;
        }
        return CreateOrderResult.ERROR_OTHERS;
    }
}
