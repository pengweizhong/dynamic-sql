package com.pengwz.dynamic.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class CollectionUtils {

    public static boolean isEmpty(Collection collection) {
        if (Objects.isNull(collection) || collection.isEmpty()) {
            return true;
        }
        return false;
    }

    public static boolean isNotEmpty(Collection collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Map map) {
        if (Objects.isNull(map) || map.isEmpty()) {
            return true;
        }
        return false;
    }


    public static boolean isNotEmpty(Map map) {
        return !isEmpty(map);
    }
}
