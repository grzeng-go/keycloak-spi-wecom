package com.grzeng.api.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;

/**
 * Gson工具类，提供JSON序列化和反序列化功能
 *
 * @author grzeng
 * @version 1.0
 */
public class GsonUtil {

    private static final Gson GSON = new Gson();
    private static final Gson GSON_WITH_EXPOSE = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    private static final Gson GSON_WITH_DATE_ADAPTER = new GsonBuilder()
            .registerTypeAdapter(Date.class, new DateSerializer())
            .registerTypeAdapter(Date.class, new DateDeserializer())
            .setDateFormat(DateFormat.LONG, DateFormat.LONG)
            .create();

    /**
     * 将对象转换为JSON字符串
     *
     * @param o 待转换的对象
     * @return JSON字符串
     */
    public static String toJson(Object o) {
        return GSON.toJson(o);
    }

    /**
     * 将对象转换为JSON字符串（支持@Expose注解过滤）
     *
     * @param o 待转换的对象
     * @param limit 如果为true，则只序列化包含@Expose注解的字段
     * @return JSON字符串
     */
    public static String toJson(Object o, boolean limit) {
        return limit ? GSON_WITH_EXPOSE.toJson(o) : toJson(o);
    }

    /**
     * 将JSON字符串转换为对象（使用TypeToken）
     *
     * @param json JSON字符串
     * @param typeToken 类型令牌
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, TypeToken<T> typeToken) {
        return GSON.fromJson(json, typeToken.getType());
    }

    /**
     * 将JSON字符串转换为指定类型的对象
     *
     * @param json JSON字符串
     * @param clazz 目标类型Class
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    /**
     * 将JSON字符串转换为对象（带日期适配器）
     *
     * @param json JSON字符串
     * @param clazz 目标类型Class
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T fromJsonWithDate(String json, Class<T> clazz) {
        return GSON_WITH_DATE_ADAPTER.fromJson(json, clazz);
    }

    static class DateDeserializer implements JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return new Date(json.getAsJsonPrimitive().getAsLong());
        }
    }

    static class DateSerializer implements JsonSerializer<Date> {
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getTime());
        }
    }
}

