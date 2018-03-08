package com.flipkart.vbroker.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flipkart.vbroker.utils.JsonUtils;
import com.google.common.collect.Sets;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Slf4j
public class CallbackConfig {
    //set of ranges of codes on which callback should be applied for the queue
    private final Set<CodeRange> codeRanges = Sets.newHashSet();

    public CallbackConfig(final Set<CodeRange> codeRanges) {
        this.codeRanges.addAll(codeRanges);
    }

    public static CallbackConfig getCallbackConfig(com.flipkart.vbroker.proto.CallbackConfig callbackConfig) {
        Set<CodeRange> newCodeRanges = new HashSet<>();
        for (int i = 0; i < callbackConfig.getCodeRangesCount(); i++) {
            com.flipkart.vbroker.proto.CodeRange range = callbackConfig.getCodeRanges(i);
            newCodeRanges.add(new CodeRange(range.getFrom(), range.getTo()));
        }
        return new CallbackConfig(newCodeRanges);
    }

    /**
     * Assuming request json will be list of code ranges, sent in http message as a header
     * <p>
     * Looks like:
     * [{"from": 200, "to": 299}, {"from": 400, "to": 404}, {"from": 423, "to": 423}]
     *
     * @return the callback configuration converted to CallbackConfig
     */
    public static Optional<CallbackConfig> fromJson(String requestJson1) {
        Optional<String> requestJson = Optional.ofNullable(requestJson1);
        if (requestJson.isPresent()) {
            try {
                final Set<CodeRange> codeRanges = JsonUtils.getObjectMapper().readValue(requestJson.get(),
                    new TypeReference<Set<CodeRange>>() {
                    });
                return Optional.of(new CallbackConfig(codeRanges));
            } catch (IOException | RuntimeException ex) {
                log.error("Unable to deserialize to json the request callback codes " + requestJson + " " + ex.getMessage());
            }
        }

        return Optional.empty();
    }

    public void addRange(final CodeRange range) {
        codeRanges.add(range);
    }

    public boolean shouldCallback(final int responseCode) {
        for (CodeRange range : codeRanges) {
            if (range.inRange(responseCode)) {
                return true;
            }
        }
        return false;
    }

    //Closed range. Not using Guava's Range as serialization and deserialization with Jackson's json can't be done
    @NoArgsConstructor
    @AllArgsConstructor
    @Setter
    @Getter
    @EqualsAndHashCode
    public static final class CodeRange {
        private int from;
        private int to;

        public boolean inRange(final int code) {
            return code >= from && code <= to;
        }
    }
}
