package com.accor.wcp.sample.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
enum Topics {

    WCPSAMPLES_TOPIC_XML("wcpsamples_topic_xml"),
    WCPSAMPLES_TOPIC_JSON("wcpsamples_topic_json"),
    WCPSAMPLES_TOPIC_AVRO("wcpsamples_topic_avro");

    @Getter
    private final String name;
}
