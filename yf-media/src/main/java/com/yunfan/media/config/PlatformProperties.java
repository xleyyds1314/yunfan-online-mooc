package com.yunfan.media.config;

import com.yunfan.media.enums.Platform;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "yf.platform")
public class PlatformProperties {
    private Platform file;
    private Platform media;
}
