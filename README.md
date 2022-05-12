# sam-robot

山姆抢菜插件傻瓜JAVA版本

# 编译

```
需要安装JDK1.8版本以上
mvn clean package
在target目录下生成
sam-robot-jar-with-dependencies.jar
```

# 傻瓜式使用

```
1.获取客户端token
关于token怎么拿，不用问了，我看github其他项目说明都很清楚了，这里不再说明了

2.提前把商品加到购物车里，记得一定要选中(重要说明)

3.启动程序

极简模式(只要token就可以了，有极速达抢极速达，没有抢全城配, 把xx换成token即可)
java -jar './sam-robot-jar-with-dependencies.jar' --authToken xx

全城配模式抢单(如全城配需要299免运费)
java -jar './sam-robot-jar-with-dependencies.jar' --authToken xx --deliveryType 2 --minAmount 29900

极速达模式抢单
java -jar './sam-robot-jar-with-dependencies.jar' --authToken xx --minAmount 9900

4.抢成功后付款
抢到后，控制台输出"抢成功了"，程序自动停止，如果需要帮别人抢单，请多开本程序(建议一个网络只有一个在跑的程序，否则有被防火墙屏蔽的风险)
抢成功后请及时付款
```

# 重要说明

```
本项目为 GPL3.0 协议，请所有进行二次开发的开发者遵守 GPL3.0协议，并且不得将代码用于商用。
本项目仅供学习交流，严禁用作商业行为，特别禁止黄牛加价代抢等！
因违法违规等不当使用导致的后果与本人无关，如有任何问题可联系本人删除此项目！
```

# 其他说明

```
1.暂时不支持保供套餐的抢购
2.本程序支持更多自定义参数(如邮件通知等)，建议有Java基础的同学们阅读一下源码(开发时间有限，程序不完善，请见谅)
```
