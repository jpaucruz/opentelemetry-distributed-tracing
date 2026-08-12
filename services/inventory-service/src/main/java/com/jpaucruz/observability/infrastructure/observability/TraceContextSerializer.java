package com.jpaucruz.observability.infrastructure.observability;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.Properties;

@Component
public class TraceContextSerializer {

    public String serializeCurrentContext() {
        Properties properties = new Properties();
        GlobalOpenTelemetry
            .getPropagators()
            .getTextMapPropagator()
            .inject(Context.current(), properties, (carrier, key, value) ->  carrier.setProperty(key, value));
        if (properties.isEmpty()) {
            return null;
        }
        StringWriter writer = new StringWriter();
        try {
            properties.store(writer, null);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize OpenTelemetry trace context", exception);
        }
        return writer.toString();
    }

}
