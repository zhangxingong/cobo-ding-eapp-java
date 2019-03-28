package com.util;

/**
 * Created by xgzhang on 2018/4/16.
 */

import java.util.Collection;
import java.util.Map;

public class CollectionUtils {
    private CollectionUtils() {
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

}
