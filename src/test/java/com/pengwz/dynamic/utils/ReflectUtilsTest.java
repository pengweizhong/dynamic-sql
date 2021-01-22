package com.pengwz.dynamic.utils;

import com.pengwz.dynamic.entity.invest_adviser.FundPoolFullConsignmentEntity;
import com.pengwz.dynamic.entity.t1.invest_adviser.T1FundPoolFullConsignmentEntity;
import com.pengwz.dynamic.entity.t2.FundCore;
import com.pengwz.dynamic.entity.t2.FundTradeSupportEntity;
import com.pengwz.dynamic.entity.t2.SupportPersonalTypeEnum;
import com.pengwz.dynamic.entity.t5.product.FundManagerEntity;
import com.pengwz.dynamic.sql.BraveSql;
import com.pengwz.dynamic.sql.DynamicSql;
import com.pengwz.dynamic.sql.OrderByMode;
import com.pengwz.dynamic.sql.PageInfo;
import org.junit.Test;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.RoundingMode.HALF_UP;

public class ReflectUtilsTest {
    @Test
    public void test1() {
        List<FundPoolFullConsignmentEntity> selectT5 = BraveSql.build(FundPoolFullConsignmentEntity.class).select();
        System.out.println("invest-adviserT5数量：" + selectT5.size());
        List<T1FundPoolFullConsignmentEntity> selectT1 = BraveSql.build(T1FundPoolFullConsignmentEntity.class).select();
        System.out.println("invest-adviserT1数量：" + selectT1.size());
        System.out.println(selectT5.size() - selectT1.size());

        Map<String, FundPoolFullConsignmentEntity> t5Map = selectT5.stream().collect(Collectors.toMap(FundPoolFullConsignmentEntity::getFundId, v -> v));
        Map<String, T1FundPoolFullConsignmentEntity> t1Map = selectT1.stream().collect(Collectors.toMap(T1FundPoolFullConsignmentEntity::getFundId, v -> v));
        List<String> tempCode = new ArrayList<>();
        t5Map.forEach((k, v) -> {
            if (t1Map.get(k) == null) {
                tempCode.add(k);
            }
        });
        String join = String.join(",", tempCode);
        System.out.println(join);
        ////////////////////////////////////////////
        List<String> tempCode2 = new ArrayList<>();
        t1Map.forEach((k, v) -> {
            if (t5Map.get(k) == null) {
                tempCode2.add(k);
            }
        });
        String join2 = String.join(",", tempCode2);
        System.out.println(join2);

    }

    @Test
    public void test2() {
        DynamicSql<T1FundPoolFullConsignmentEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.orIsNull(T1FundPoolFullConsignmentEntity::getDeleted);
        dynamicSql.orEqualTo(T1FundPoolFullConsignmentEntity::getDeleted, 1);
        List<T1FundPoolFullConsignmentEntity> select = BraveSql.build(dynamicSql, T1FundPoolFullConsignmentEntity.class).select();
        System.out.println(select.size());
        System.out.println(select);

    }


    @Test
    public void test3() {
        //调仓卖出总值
        BigDecimal totalPortfolioTransfer = BigDecimal.ZERO;
        //市值
        BigDecimal marketValue = BigDecimal.valueOf(101717.87);
        BigDecimal var1 = marketValue.multiply(BigDecimal.valueOf(0.20000000 - 0.19670000));
        BigDecimal var2 = marketValue.multiply(BigDecimal.valueOf(0.47000000 - 0.39460000));
        BigDecimal var3 = marketValue.multiply(BigDecimal.valueOf(0.4 - 0.39460000));
        BigDecimal var4 = marketValue.multiply(BigDecimal.valueOf(0.1 - 0.96));
        BigDecimal var5 = marketValue.multiply(BigDecimal.valueOf(0.2 - 0.1967));
        BigDecimal historyVar = var3.add(var4).add(var5);
//        BigDecimal historyVar = var1.add(var2).add(var3).add(var4).add(var5);
        System.out.println(historyVar);
        //总资产4534954.58（已加入本次）
        //总资产平均值 200152.968444
        BigDecimal divide = historyVar.divide(BigDecimal.valueOf(200152.968444), 8, RoundingMode.HALF_UP);
        System.out.println(divide);
        BigDecimal divide2 = divide.divide(BigDecimal.valueOf(365), 8, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(65));
        System.out.println(divide2);
    }

    @Test
    public void test4() {
        BigDecimal tempRatio = BigDecimal.ZERO.subtract(BigDecimal.valueOf(-1));
        System.out.println(tempRatio);
        boolean ff = BigDecimal.valueOf(11).compareTo(BigDecimal.ZERO) > 0;
        System.out.println(ff);
    }

    @Test
    public void test5() {
        String formatTime = "2020-12-112";
        boolean matches = formatTime.matches("^\\d{4}(\\-)\\d{1,2}\\-\\d{1,2}$");
        System.out.println(matches);
        List<String> dateList = new ArrayList<>();
        dateList.add("555");
        dateList.add("444");
        dateList.add("333");
        System.out.println(dateList.subList(0, 2));
        for (int i = 0; i < dateList.size(); i++) {
            //如果相同，就返回下一个日期
            if ("333".equals(dateList.get(i)) && i < dateList.size() - 1) {
                System.out.println(dateList.get(i + 1));
            }
        }
        System.out.println("--------------");
        Map<String, String> map = new HashMap<>();
        map.put(null, null);
        map.put("1212", "1212");
        map.put(null, null);
//        map.remove(null);
        System.out.println(map);
        System.out.println("map.get(null) = " + map.get(null));
        System.out.println("--------------");
        BigDecimal divide = BigDecimal.valueOf(61110).divide(new BigDecimal(Math.pow(10, 4)));
        BigDecimal divide2 = BigDecimal.valueOf(6111000).divide(new BigDecimal(Math.pow(10, 6)));
        System.out.println(divide + " = " + divide2);
    }

    @Test
    public void test6() {
        String growthRateStr = String.valueOf(BigDecimal.valueOf(0).divide(BigDecimal.valueOf(Math.pow(10, 6)), 2, HALF_UP));
        System.out.println("growthRateStr = " + growthRateStr);
        String growthRateStr2 = String.valueOf(BigDecimal.valueOf(-1050000).divide(BigDecimal.valueOf(Math.pow(10, 4)), 2, HALF_UP));
        System.out.println("growthRateStr2 = " + growthRateStr2);
        LocalDate fundsArriveDate = LocalDate.of(2021, 1, 7);
        System.out.println(fundsArriveDate.getDayOfWeek());
        System.out.println("--------------");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd mm:HH");
        LocalDate maxTradeDate = LocalDate.of(2021, 12, 31);
        System.out.println(fundsArriveDate.isBefore(maxTradeDate));
        System.out.println("------------------------------------------");
        //只要股票 债券 基金
        Map<Integer, BigDecimal> maxRatioAssetCodeMap = new HashMap<>();
        maxRatioAssetCodeMap.put(1, BigDecimal.valueOf(-12));
        maxRatioAssetCodeMap.put(2, BigDecimal.valueOf(10.45));
        maxRatioAssetCodeMap.put(3, BigDecimal.valueOf(11));
        maxRatioAssetCodeMap.put(4, BigDecimal.valueOf(12));
        maxRatioAssetCodeMap.put(5, BigDecimal.valueOf(15));
        Map<Integer, BigDecimal> filterTypeCodeMap = maxRatioAssetCodeMap.entrySet().stream().filter(entry -> Arrays.asList(1, 2, 3).contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (!filterTypeCodeMap.isEmpty()) {
            Optional<Map.Entry<Integer, BigDecimal>> integerBigDecimalEntry = filterTypeCodeMap.entrySet().stream().max(Map.Entry.comparingByValue());
            integerBigDecimalEntry.ifPresent(bigDecimalEntry -> System.out.println(bigDecimalEntry.getKey()));
        }
//        System.out.println(integerBigDecimalEntry.getKey() + " -- " + integerBigDecimalEntry.getValue());
    }

    @Test
    public void test7() {
        DynamicSql<FundManagerEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andIsNotNull(FundManagerEntity::getPersonalData);
        dynamicSql.groupBy("personalcode");
        Integer integer = BraveSql.build(dynamicSql, FundManagerEntity.class).selectCount();
        int name = 0;
        int page = 0;
        for (int j = 0; j < integer; j++) {
            PageInfo<FundManagerEntity> pageInfo = BraveSql.build(dynamicSql, FundManagerEntity.class).selectPageInfo(page, 1000);
            page++;
            List<FundManagerEntity> select = pageInfo.getResultList();
            try {
                for (int k = 0; k < select.size(); k++) {
                    byte[] personalData = select.get(k).getPersonalData();
                    if (personalData != null && personalData.length > 0) {
                        FileOutputStream fos = new FileOutputStream("C:\\Users\\pengw\\Desktop\\touxiang2\\" + (name++) + ".png", true);
                        fos.write(personalData);
                        fos.flush(); //强制刷新输出流
                        fos.close(); //强制关闭输出流
                        BigDecimal bigDecimal = BigDecimal.valueOf(name).divide(BigDecimal.valueOf(select.size()), 3, HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(3, HALF_UP);
                        System.out.println("进度：" + bigDecimal.doubleValue() + "%");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private String getSupportPersonalType() {
        Set<String> codes = SupportPersonalTypeEnum.toMap().keySet();
        codes.remove(String.valueOf(new Random().nextInt(10)));
        codes.remove(String.valueOf(new Random().nextInt(10)));
        codes.remove(String.valueOf(new Random().nextInt(10)));
        codes.remove(String.valueOf(new Random().nextInt(10)));
        codes.remove(String.valueOf(new Random().nextInt(10)));
        return String.join(",", codes);
    }

    @Test
    public void test8() {
        DynamicSql<FundCore> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andIsNotNull(FundCore::getTaCode);
        dynamicSql.groupBy(FundCore::getTaCode);
        List<FundCore> fundCores = BraveSql.build(dynamicSql, FundCore.class).select();
        System.out.println(fundCores.size());
        ///////////////
        fundCores = fundCores.subList(0, 2);
        List<FundTradeSupportEntity> lllllllll = new ArrayList<>();
        for (FundCore fundCore : fundCores) {
            String taCode = fundCore.getTaCode();
            FundTradeSupportEntity supportEntity = new FundTradeSupportEntity();
            supportEntity.setTaCode(taCode);
            supportEntity.setCreateTime(new Date());
            supportEntity.setLastModTime(new Date());
            supportEntity.setSupportPersonalType(getSupportPersonalType());
            supportEntity.setDeleted(0);
            lllllllll.add(supportEntity);
        }
        Integer integer = BraveSql.build(FundTradeSupportEntity.class).insertMany(lllllllll);
        System.out.println(integer);

    }

    @Test
    public void test9() {
        List<FundTradeSupportEntity> fundTradeSupportEntities = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (i == 3) {
                FundTradeSupportEntity entity = new FundTradeSupportEntity();
                entity.setTaCode((i - 1) + "");
                entity.setId((long) (i - 1));
                fundTradeSupportEntities.add(entity);
            }
            FundTradeSupportEntity entity = new FundTradeSupportEntity();
            entity.setTaCode(i + "");
            entity.setId((long) i);
            fundTradeSupportEntities.add(entity);
        }
        Map<String, FundTradeSupportEntity> collect = fundTradeSupportEntities.stream().collect(Collectors.toMap(FundTradeSupportEntity::getTaCode, v -> v, (v1, v2) -> v2));
        System.out.println(collect);
    }


    @Test
    public void test10() {
        String fundIds = "000009, 000106, 000003, 000004, 000005, 000007, 000008, 002621, 000032, 000111, 000112, 000113, 000199, 000936, 000056, 000216, 000103, 000218, 001475, 008052, 003095, 008381, 020011, 020090, 050003, 100061, 160922, 161005, 161028, 161603, 161604, 161810, 162712, 163415, 164906, 340001, 020007, 000701, 000362, 000526, 000373, 000376, 000452, 000878, 000879, 000960, 000974, 001180, 001230, 001550, 001551, 001984, 000023, 000390, 000411, 001277, 000022, 000024, 000104, 000105, 000221, 000241, 000251, 000265, 000178, 000179, 000181, 000182, 000149, 000150, 000151, 000152, 000121, 000122, 000123, 000124, 000107, 000014, 000125, 000126, 000051, 000128, 000165, 000166, 000167, 000169, 000120, 020029, 167601, 003207, 000252, 000355, 002929, 690012, 750002, 686868, 590009, 160618, 161820, 163907, 020009, 000082, 000061, 240022, 121005, 040035, 217022, 110037, 110038, 000205, 000206, 110003, 000217, 100066, 001666, 001410, 000877, 000967, 161226, 000914, 002168, 000595, 001717, 000754, 002528, 000419, 163411, 003017, 003474, 004417, 000707, 003327, 003672, 005275, 006837, 007130, 100026, 161605, 166005, 000028, 000062, 000072, 161123, 000277, 000722, 000046, 000084, 000142, 000141, 000147, 000255, 100007, 041003, 100016, 100018, 000130, 000131, 001113, 161116, 000455, 001236, 000220, 000015, 000016, 000025, 000037, 000033, 000038, 000045, 000047";
        fundIds = fundIds.replace(" ", "");
        String[] split = fundIds.split(",");
        String start = "\"";
        String end = "\"";
        String join = String.join("\",\"", split);
        System.out.println(start + join + end);
    }


    @Test
    public void test11() throws Exception {
        Map<String, Date> tableNameAndReportDateMap = new HashMap<>();
        tableNameAndReportDateMap.put("456",new SimpleDateFormat("yyyy-MM-dd").parse("2010-12-12"));
        tableNameAndReportDateMap.put("789",new SimpleDateFormat("yyyy-MM-dd").parse("2020-11-11"));
        tableNameAndReportDateMap.put("123",new Date());
        tableNameAndReportDateMap.put("666",null);
        tableNameAndReportDateMap.put(null,null);
        Optional<Map.Entry<String, Date>> max = tableNameAndReportDateMap.entrySet().stream().filter(entry -> Objects.nonNull(entry.getValue())).min(Map.Entry.comparingByValue());
        if (!max.isPresent()) {
            System.out.println("kong de");
        } else{
            System.out.println(max.get().getKey());
            System.out.println(max.get().getValue());
        }
    }


}














