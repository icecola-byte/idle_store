package com.lh.idlestore.distributed.id.generator.biz.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "leaf")
public class LeafProperties {

    private String name = "idle-store";
    private Segment segment = new Segment();
    private Jdbc jdbc = new Jdbc();
    private Snowflake snowflake = new Snowflake();

    @Data
    public static class Segment {
        private boolean enable = true;
    }

    @Data
    public static class Jdbc {
        private String url;
        private String username;
        private String password;
    }

    @Data
    public static class Snowflake {
        private boolean enable = true;
        private Zookeeper zk = new Zookeeper();
        private int port;
    }

    @Data
    public static class Zookeeper {
        private String address;
    }
}
