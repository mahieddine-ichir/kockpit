package com.accor.wcp.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Setter
@Mojo(name = "verify", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class CodeGenMojo extends AbstractMojo {

    @Parameter(name = "api-url", property = "featureflipping.api.url")
    protected String apiUrl;

    @Parameter(name = "fail-on-expired", property = "featureflipping.fail-on-expired", defaultValue = "false")
    protected boolean failOnExpired;

    @Parameter(readonly = true, required = true, defaultValue = "${project}")
    private MavenProject project;

    @SneakyThrows
    @Override
    public void execute() {
        if (isBlank(apiUrl)) {
            throw new MojoExecutionException("apiUrl must be specified");
        }

        getLog().info("Verifying FeatureFlipping keys against "+apiUrl);
        URL apiUrlObj = new URL(apiUrl);
        URLConnection urlConnection = apiUrlObj.openConnection();
        if (urlConnection instanceof HttpURLConnection httpURLConnection) {
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setConnectTimeout(1000); // fixme set through config
            httpURLConnection.setReadTimeout(1000); // fixme set through config
            try {
                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    List<FeatureFlippingDto> featureFlippingDtos = readResponse(httpURLConnection);
                    List<FeatureFlippingDto> expired = featureFlippingDtos.stream()
                            .filter(featureFlippingDto -> Objects.nonNull(featureFlippingDto.getExpiration()))
                            .filter(featureFlippingDto -> LocalDate.now().isAfter(featureFlippingDto.getExpiration()))
                            .toList();

                    expired.forEach(featureFlippingDto ->
                            getLog().warn("Expired key: %s, expired at %s".formatted(featureFlippingDto.getKey(), featureFlippingDto.getExpiration())));

                    if (!expired.isEmpty() && failOnExpired) {
                        throw new MojoExecutionException("You have expired keys! (see logs for keys list or set failedOnExpired to false)");
                    }
                } else {
                    getLog().error("call against apiUrl %s returned non 200 Code %d".formatted(apiUrl, httpURLConnection.getResponseCode()));
                    throw new MojoExecutionException("apiUrl returned non 200 Code " + httpURLConnection.getResponseCode());
                }
            } catch (Exception e) {
                getLog().error("error initiating call to %s".formatted(apiUrl), e);
                throw new RuntimeException(e);
            } finally {
                httpURLConnection.disconnect();
            }

        } else {
            throw new MojoExecutionException("Internal error initiating API connection to " + apiUrl);
        }
    }

    @SneakyThrows
    private List<FeatureFlippingDto> readResponse(HttpURLConnection httpURLConnection) {
        BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        FeatureFlippingDto[] data = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .readValue(response.toString(), FeatureFlippingDto[].class);
        return Arrays.asList(data);
    }

    private boolean isBlank(String inputSpec) {
        return inputSpec == null || inputSpec.trim().isEmpty();
    }
}