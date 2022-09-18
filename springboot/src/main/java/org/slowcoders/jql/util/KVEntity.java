package org.slowcoders.jql.util;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.LinkedHashMap;

// swagger schema 설정
@Schema(implementation = Object.class)
public class KVEntity extends LinkedHashMap<String, Object> {

    public static KVEntity of(String key, Object value) {
        KVEntity entity = new KVEntity();
        entity.put(key, value);
        return entity;
    }

}
