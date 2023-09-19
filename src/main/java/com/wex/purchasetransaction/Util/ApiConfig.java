package com.wex.purchasetransaction.Util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ConfigurationProperties(prefix = "api")
@Configuration("appInfo")
public class ApiConfig {
    private String baseUrl;
    private String endPoint;
    private String fields;
    private String recordDate;
    private String sort;
}