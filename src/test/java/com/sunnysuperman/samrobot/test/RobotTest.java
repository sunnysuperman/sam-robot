package com.sunnysuperman.samrobot.test;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.sunnysuperman.samrobot.Robot;

import junit.framework.TestCase;

public class RobotTest extends TestCase {
    private String homePath = System.getProperty("user.home") + "/sam-robot";
    private String logPath = homePath + "/log-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    private String emailConfigPath = homePath + "/email";
    private String email = "xx@xx";
    private String authToken = "xx";

    // 最简单
    public void testSimplest() throws Exception {
        Robot.main(new String[] { "--authToken", authToken });
    }

    // 设最小金额
    public void testMinAmount() throws Exception {
        Robot.main(new String[] { "--authToken", authToken, "--minAmount", "29900" });
    }

    // 仅全城送
    public void testDeliveryType() throws Exception {
        Robot.main(new String[] { "--authToken", authToken, "--deliveryType", "2" });
    }

    // 有通知
    public void testNotify() throws Exception {
        Robot.main(new String[] { "--authToken", authToken, "--emailConfigPath", emailConfigPath, "--email", email });
    }

    // 带日志
    public void testLog() throws Exception {
        Robot.main(new String[] { "--authToken", authToken, "--logPath", logPath, "--logApi", "true" });
    }
}
