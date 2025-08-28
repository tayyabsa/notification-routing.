package com.multibank.notification_routing.config;

import com.multibank.notification_routing.service.channel.NotificationChannel;
import com.multibank.notification_routing.service.enums.Channel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "routing")
public class RoutingProperties {
    private List<Rule> rules = new ArrayList<>();

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public static class Rule {
        private String name;
        private When when;
        private Then then;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public When getWhen() {
            return when;
        }

        public void setWhen(When when) {
            this.when = when;
        }

        public Then getThen() {
            return then;
        }

        public void setThen(Then then) {
            this.then = then;
        }
    }
 public static class When {
        private String eventType;
        private String priority; // HIGH/MEDIUM/LOW

     public String getEventType() {
         return eventType;
     }

     public void setEventType(String eventType) {
         this.eventType = eventType;
     }

     public String getPriority() {
         return priority;
     }

     public void setPriority(String priority) {
         this.priority = priority;
     }
 }
 public static class Then {
        private List<Channel> channels;

     public List<Channel> getChannels() {
         return channels;
     }

     public void setChannels(List<Channel> channels) {
         this.channels = channels;
     }

    }
}