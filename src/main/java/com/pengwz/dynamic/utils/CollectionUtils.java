package com.pengwz.dynamic.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

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

//    public static <T, C extends Collection<T>> void addElement(Collection<T> coll, T element) {
//        addElement(coll, element, ArrayList::new);
//    }
//
//    public static <T, C extends Collection<T>> void addElement(Collection<T> coll, T element, Supplier<C> mapFactory) {
//        if (coll == null) {
//            coll = mapFactory.get();
//        }
//        coll.add(element);
//    }
}
