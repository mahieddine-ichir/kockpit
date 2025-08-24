package com.example.insightsstreamingappapi;

import java.util.function.Consumer;

public interface InsightsConsumer extends Consumer<InsightDocument>{
    void start();
    void stop();

    void onError(Throwable throwable);
}
