package com.sunnysuperman.samrobot;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import com.sunnysuperman.commons.bean.Bean;
import com.sunnysuperman.commons.util.FileUtil;
import com.sunnysuperman.commons.util.FormatUtil;
import com.sunnysuperman.commons.util.JSONUtil;
import com.sunnysuperman.commons.util.StringUtil;
import com.sunnysuperman.http.client.HttpClient;
import com.sunnysuperman.samrobot.MailHelper.SMTPInfo;
import com.sunnysuperman.samrobot.log.Logger;
import com.sunnysuperman.samrobot.model.CapacityResult;
import com.sunnysuperman.samrobot.model.CreateOrderResult;
import com.sunnysuperman.samrobot.model.DeliveryAddress;
import com.sunnysuperman.samrobot.model.Store;
import com.sunnysuperman.samrobot.model.StoreWrap;

import okhttp3.ConnectionPool;

public class Robot {
    private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("GMT+08:00");

    private static Config sConfig;
    private static Api sApi;
    private static File sLogFile;
    private static List<String> sDateList;
    private static DeliveryAddress sAddress;
    private static List<Store> sStoreList;
    private static String[] sDeliveryTime;
    private static String sPaymentMethodId;
    private static SMTPInfo sSmtpInfo;

    public static void main(String[] args) throws Exception {
        // 1.初始化
        init(args);

        // 2.获取收货地址
        sAddress = ensureDeliveryAddressDetail();

        Map<?, ?> cart = null;
        while (true) {
            // 3.获取购物车(保证有满足购买条件的商品)
            if (cart == null || sConfig.isReloadCart()) {
                cart = ensureCart();
            }
            try {
                // 4.下单
                if (createOrder(cart)) {
                    log("抢成功了!!!!!!");
                    if (sSmtpInfo != null) {
                        MailService.sendSuccessMail(sSmtpInfo, sConfig.getEmail());
                    }
                    break;
                }
            } catch (Exception ex) {
                logError(ex);
            }
        }
    }

    private static void init(String[] args) throws Exception {
        // 1.初始化配置
        Field[] fields = Config.class.getDeclaredFields();
        final CommandLine cmd;
        {
            Options options = new Options();
            for (Field field : fields) {
                options.addOption(field.getName(), field.getName(), true, field.getName());
            }
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);
        }
        Map<String, Object> argsAsMap = new HashMap<>();
        for (Field field : fields) {
            String value = cmd.getOptionValue(field.getName());
            if (StringUtil.isEmpty(value)) {
                continue;
            }
            argsAsMap.put(field.getName(), value);
        }
        sConfig = Bean.fromMap(argsAsMap, new Config());
        log("==============环境配置==============");
        log(JSONUtil.toJSONString(sConfig));

        // 2.初始化日志
        if (StringUtil.isNotEmpty(sConfig.getLogPath())
                && (sConfig.getLogType() == LogType.FILE || sConfig.getLogType() == LogType.ALL)) {
            sLogFile = new File(sConfig.getLogPath());
            FileUtil.ensureFile(sLogFile);
            if (sLogFile.exists()) {
                log("初始化文件日志成功");
            } else {
                log("初始化文件日志失败");
                sLogFile = null;
            }
        }

        // 3.初始化http客户端
        HttpClient httpClient = new HttpClient(new ConnectionPool(30, 30, TimeUnit.MILLISECONDS));
        httpClient.setConnectTimeout(sConfig.getConnectTimeout());
        httpClient.setReadTimeout(sConfig.getReadTimeout());
        sApi = new Api(httpClient, sConfig.getAuthToken(), sConfig.isLogApi() ? new Logger() {

            @Override
            public void log(String msg) {
                Robot.log(msg, true);
            }

            @Override
            public void logError(Throwable t) {
                Robot.logError(t);
            }

        } : null);

        // 4.初始化其他
        {
            Date now = new Date();
            final int days = 7;
            final long millsADay = 24L * 60 * 60 * 1000;
            sDateList = new ArrayList<>(days);
            for (int i = 0; i < days; i++) {
                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(now.getTime() + millsADay * i));
                sDateList.add(date);
            }
            log("日期列表: " + StringUtil.join(sDateList, ","));
        }
        // 初始化通知
        if (sConfig.getEmailConfigPath() != null && sConfig.getEmail() != null) {
            String emailConfig = FileUtil.read(sConfig.getEmailConfigPath());
            sSmtpInfo = Bean.fromJson(emailConfig, new SMTPInfo());
            // 发送开始通知(主要是为了测试邮件能发送成功)
            // 新开一个线程，不影响主线进度
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        MailService.sendStartMail(sSmtpInfo, sConfig.getEmail());
                    } catch (Exception ex) {
                        logError(ex);
                    }
                }

            }).start();
        }
    }

    private static void log(String msg, boolean detail) {
        int logType = sConfig.getLogType();
        if (!detail && (logType == LogType.STD || logType == LogType.ALL)) {
            System.out.println(msg);
        }
        if (sLogFile != null) {
            try {
                StringBuilder buf = new StringBuilder(msg.length() + 20);
                buf.append("[").append(System.currentTimeMillis()).append("]").append(": ").append(msg).append("\n");
                FileUtil.append(sLogFile, buf.toString());
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private static void log(String msg) {
        log(msg, false);
    }

    public static void logError(Throwable t) {
        log("发生错误: " + t.getMessage());
        if (sLogFile != null) {
            try {
                log(t.getStackTrace().toString(), true);
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    private static void sleep(long ms) {
        if (ms <= 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static String formatTime(String s) {
        return FormatUtil.formatISO8601Date(new Date(Long.parseLong(s)), DEFAULT_TIMEZONE);
    }

    private static DeliveryAddress ensureDeliveryAddressDetail() throws Exception {
        DeliveryAddress result = null;
        while (true) {
            try {
                result = sApi.getDeliveryAddressDetail();
                if (result != null) {
                    log("获取地址成功: " + result.getDetailAddress());
                    return result;
                }
            } catch (Exception ex) {
                logError(ex);
            }
            sleep(sConfig.getDelay());
        }
    }

    private static StoreWrap ensureRecommendStoreListByLocation(String longitude, String latitude) throws Exception {
        while (true) {
            try {
                return sApi.getRecommendStoreListByLocation(longitude, latitude);
            } catch (Exception ex) {
                logError(ex);
            }
            sleep(sConfig.getDelay());
        }
    }

    private static String ensurePaymentMethodId(String storeId) throws Exception {
        while (true) {
            try {
                String result = sApi.getPaymentMethodId(storeId);
                if (result != null) {
                    log("获取支付方式成功:" + result);
                    return result;
                }
            } catch (Exception ex) {
                logError(ex);
            }
            sleep(sConfig.getDelay());
        }
    }

    private static Map<?, ?> ensureCart() throws Exception {
        while (true) {
            try {
                // 先获取门店(因为门店信息经常发生变化)【部分抢购场景不需要更换门店】
                if (sStoreList == null || sConfig.isReloadCart()) {
                    sStoreList = ensureRecommendStoreListByLocation(sAddress.getLongitude(), sAddress.getLatitude())
                            .getStoreList();
                    log("获取门店成功: " + sStoreList.size() + "个");
                }
                // 再加载购物车
                Map<?, ?> result = sApi.getCart(sAddress, sStoreList, sConfig.getDeliveryType());
                if (result != null) {
                    List<?> floorInfoList = (List<?>) result.get("floorInfoList");
                    if (floorInfoList == null || floorInfoList.isEmpty()) {
                        log("门店信息为空");
                    } else {
                        Map<?, ?> floorInfo = (Map<?, ?>) floorInfoList.get(0);
                        int amount = FormatUtil.parseIntValue(floorInfo.get("amount"), 0);
                        log("配送类型: " + floorInfo.get("deliveryType"));
                        int minAmount = sConfig.getMinAmount();
                        if (amount <= 0) {
                            log("购物车金额为0" + "<" + (minAmount / 100d));
                        } else if (amount < minAmount) {
                            log("购物车低于最小金额: " + (amount / 100d) + "<" + (minAmount / 100d));
                        } else {
                            log("获取购物车成功, 金额:" + amount / 100d);
                            return result;
                        }
                    }
                }
            } catch (Exception ex) {
                logError(ex);
            }
            sleep(sConfig.getCartDelay());
        }
    }

    private static CapacityResult ensureCapacityData(String storeDeliveryTemplateId, boolean allowEmpty) {
        while (true) {
            try {
                CapacityResult result = sApi.getCapacityData(storeDeliveryTemplateId, sDateList);
                if (result == null) {
                    log("获取运力失败");
                } else if (result.getTime() == null) {
                    log("没有运力");
                    if (allowEmpty) {
                        return result;
                    }
                } else {
                    log("获取运力成功: " + formatTime(result.getTime()[0]) + "~" + formatTime(result.getTime()[1]));
                    return result;
                }
            } catch (Exception ex) {
                log("获取运力失败");
                logError(ex);
            }
            sleep(sConfig.getCapacityDelay());
        }
    }

    private static boolean createOrder(Map<?, ?> cart) throws Exception {
        String addressId = FormatUtil.parseString(((Map<?, ?>) cart.get("deliveryAddress")).get("addressId"));
        List<?> floorInfoList = (List<?>) cart.get("floorInfoList");
        Map<?, ?> floorInfo = (Map<?, ?>) floorInfoList.get(0);
        List<?> goodsList = (List<?>) floorInfo.get("normalGoodsList");
        Map<?, ?> storeInfo = (Map<?, ?>) floorInfo.get("storeInfo");
        String storeDeliveryTemplateId = storeInfo.get("storeDeliveryTemplateId").toString();
        Integer amount = FormatUtil.parseInteger(floorInfo.get("amount"));
        Integer deliveryType = FormatUtil.parseInteger(floorInfo.get("deliveryType"));

        List<Map<String, Object>> goodsList2 = new ArrayList<>();
        for (Object item : goodsList) {
            Map<?, ?> itemMap = (Map<?, ?>) item;
            Map<String, Object> goods = new HashMap<>();
            if (!FormatUtil.parseBoolean(itemMap.get("isSelected"))) {
                continue;
            }
            int quantity = FormatUtil.parseInteger(itemMap.get("quantity"));

            goods.put("isSelected", true);
            goods.put("quantity", quantity);
            goods.put("spuId", itemMap.get("spuId"));
            goods.put("storeId", itemMap.get("storeId"));
            goodsList2.add(goods);
        }
        if (goodsList2.isEmpty()) {
            log("购物车选中商品为空，不应该发生");
            sleep(sConfig.getCartDelay());
            return false;
        }

        if (sPaymentMethodId == null || sConfig.isReloadCart()) {
            sPaymentMethodId = ensurePaymentMethodId(storeInfo.get("storeId").toString());
        }

        if (sDeliveryTime == null) {
            sDeliveryTime = ensureCapacityData(storeDeliveryTemplateId, sConfig.isReloadCart()).getTime();
            if (sDeliveryTime == null) {
                sleep(sConfig.isReloadCart() ? sConfig.getCartDelay() : sConfig.getCapacityDelay());
                return false;
            }
        }

        Map<String, Object> deliveryInfoVO = new HashMap<>();
        deliveryInfoVO.put("deliveryModeId", storeInfo.get("deliveryModeId").toString());
        deliveryInfoVO.put("storeDeliveryTemplateId", storeDeliveryTemplateId);
        deliveryInfoVO.put("storeType", storeInfo.get("storeType").toString());

        Map<String, Object> storeInfo2 = new HashMap<>();
        storeInfo2.put("storeId", storeInfo.get("storeId").toString());
        storeInfo2.put("storeType", storeInfo.get("storeType").toString());
        storeInfo2.put("areaBlockId", storeInfo.get("areaBlockId").toString());

        Map<String, Object> settleDeliveryInfo = new HashMap<>();
        settleDeliveryInfo.put("expectArrivalTime", sDeliveryTime[0]);
        settleDeliveryInfo.put("expectArrivalEndTime", sDeliveryTime[1]);
        settleDeliveryInfo.put("deliveryType", deliveryType);

        String body = FileUtil.read(Robot.class.getResourceAsStream("/sam/pay.json"));

        Map<String, Object> req = JSONUtil.parseJSONObject(body);
        req.put("addressId", addressId);
        req.put("goodsList", goodsList2);
        req.put("amount", amount);
        req.put("storeInfo", storeInfo2);
        req.put("deliveryInfoVO", deliveryInfoVO);
        req.put("settleDeliveryInfo", settleDeliveryInfo);
        req.put("payMethodId", sPaymentMethodId);
        req.put("cartDeliveryType", deliveryType);
        CreateOrderResult result = sApi.createOrder(req);
        if (result.isOk()) {
            return true;
        }
        // 没有运力需要重新加载运力时间
        if (result.isNoCapacity()) {
            sDeliveryTime = null;
        }
        return false;
    }

}
