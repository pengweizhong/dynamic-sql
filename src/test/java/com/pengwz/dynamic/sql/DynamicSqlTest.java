package com.pengwz.dynamic.sql;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.net.SocketTimeoutException;
import java.sql.SQLOutput;
import java.util.*;

public class DynamicSqlTest {

    @Test
    public void insertFundCategoryEntity(){

        List<FundTacticsOutSelectFundDTO> selectFundList = new ArrayList<>();
        FundTacticsOutSelectFundDTO dto = new FundTacticsOutSelectFundDTO();
        dto.setFundId("12345");
        dto.setReason("                 ads            sds          ");
        selectFundList.add(dto);
        selectFundList.forEach(selectFund -> {
            if (!StringUtils.isBlank(selectFund.getReason())) {
                selectFund.setReason(selectFund.getReason().trim());
            }
        });
        System.out.println(selectFundList);
    }

    static class FundTacticsOutSelectFundDTO {
        private String fundId;
        private String reason;

        public String getFundId() {
            return fundId;
        }

        public void setFundId(String fundId) {
            this.fundId = fundId;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }

        @Override
        public String toString() {
            return "FundTacticsOutSelectFundDTO{" +
                    "fundId='" + fundId + '\'' +
                    ", reason='" + reason + '\'' +
                    '}';
        }
    }

    @Test
    public void test2(){
        Map<Integer,String> map = new HashMap<>();
        map.put(123,"124");
        map.put(124,"1121");
        map.put(1224,"1121");
        List<Integer> list1 = new ArrayList<>();
        list1.add(123);
        list1.add(124);
        Set<Integer> integers = map.keySet();
        integers.removeAll(list1);
        System.out.println(integers);
    }

}































