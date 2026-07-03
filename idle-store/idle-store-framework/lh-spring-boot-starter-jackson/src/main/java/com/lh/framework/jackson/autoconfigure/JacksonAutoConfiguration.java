package com.lh.framework.jackson.autoconfigure;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.YearMonthDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.YearMonthSerializer;
import com.lh.framework.common.constant.DateConstants;
import com.lh.framework.common.util.JsonUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;


@AutoConfiguration
@ConditionalOnClass(ObjectMapper.class)
public class JacksonAutoConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        DateTimeFormatter dateTimeFormatter =
                DateTimeFormatter.ofPattern(DateConstants.Y_M_D_H_M_S_FORMAT);
        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern(DateConstants.Y_M_D_FORMAT);
        DateTimeFormatter timeFormatter =
                DateTimeFormatter.ofPattern(DateConstants.H_M_S_FORMAT);

        return builder -> {
            // 时区统一成东八区
            builder.timeZone(TimeZone.getTimeZone("Asia/Shanghai"));

            // JSON 中有 Java 对象不存在的字段时，不要直接报错
            builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            // 遇到空 Bean 时，不要直接报错
            builder.featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

            // LocalDateTime：yyyy-MM-dd HH:mm:ss
            builder.serializerByType(LocalDateTime.class,
                    new LocalDateTimeSerializer(dateTimeFormatter));
            builder.deserializerByType(LocalDateTime.class,
                    new LocalDateTimeDeserializer(dateTimeFormatter));

            // LocalDate：yyyy-MM-dd
            builder.serializerByType(LocalDate.class,
                    new LocalDateSerializer(dateFormatter));
            builder.deserializerByType(LocalDate.class,
                    new LocalDateDeserializer(dateFormatter));

            // LocalTime：HH:mm:ss
            builder.serializerByType(LocalTime.class,
                    new LocalTimeSerializer(timeFormatter));
            builder.deserializerByType(LocalTime.class,
                    new LocalTimeDeserializer(timeFormatter));
        };
    }

    @Bean
    public SmartInitializingSingleton jsonUtilsInitializer(ObjectMapper objectMapper) {
        return () -> JsonUtils.init(objectMapper);
    }
}