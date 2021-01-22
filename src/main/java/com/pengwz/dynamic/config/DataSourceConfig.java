package com.pengwz.dynamic.config;

import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.utils.StringUtils;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public interface DataSourceConfig /*extends CommonDataSource*/ {

    Properties getProperties();

    class Properties {
        private EnumMap<DBConfigEnum, String> configEnumMap = new EnumMap<>(DBConfigEnum.class);
        private Map<String, String> otherConfigMap;

        public EnumMap<DBConfigEnum, String> getConfigEnumMap() {
            return configEnumMap;
        }

        public void setConfig(DBConfigEnum configEnum, String value) {
            if (StringUtils.isEmpty(value)) {
                throw new BraveException("value不可为空");
            }
            configEnumMap.put(configEnum, value);
        }

        public Map<String, String> getOtherConfigMap() {
            return otherConfigMap;
        }

        public void setOtherConfigMap(Map<String, String> otherConfigMap) {
            this.otherConfigMap = otherConfigMap;
        }

    }

}
