package com.accor.wcp.sample.dynaconfig;

import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigAttribute;
import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigEnabler;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to the application.
 *
 * <p>Properties are configured in the {@code application.yml} file.
 */
@ConfigurationProperties(prefix = "application")
@Getter
@Setter
@DynaConfigEnabler
public class ApplicationProperties {

  private String id;
  private Client client = new Client();
  private String purchaseTableName;

  @DynaConfigAttribute("application.purchase-ttl-days")
  private int purchaseTtlDays = 30;

  private final ApiDocs apiDocs = new ApiDocs();

  @Getter
  @Setter
  public static class ApiDocs {
    private boolean enabled;
  }

  @Getter
  @Setter
  public static class Client {

    private WcXssInsuranceQuotationConfig wcxssInsuranceQuotation =
        new WcXssInsuranceQuotationConfig();

    private AxaClient axa = new AxaClient();
    private ApimClient apim = new ApimClient();

    @Getter
    @Setter
    public static class AxaClient {

      @DynaConfigAttribute private long timeout = 0;
      @DynaConfigAttribute private String basePath;
      @DynaConfigAttribute private String apikey;
      private Notification notification = new Notification();

      @Getter
      @Setter
      public static class Notification {
        @DynaConfigAttribute private String cancellationQueuePath;
        private String confirmPaymentQueuePath;
      }
    }

    @Getter
    @Setter
    public static class ApimClient {
      private ApimApsClient aps = new ApimApsClient();
      private ApimOrderApi orderApi = new ApimOrderApi();

      @Getter
      @Setter
      public static class ApimApsClient {
        private String apikey;
        @DynaConfigAttribute private long timeout = 0;
        private String basePath;
        private String signPartnerKey;
        private String cancelOrRefundPaymentQueuePath;
        @DynaConfigAttribute private String xCustomHeader;
      }

      @Getter
      @Setter
      public static class ApimOrderApi {
        private String apikey;
        @DynaConfigAttribute private long timeout = 0;
        private String basePath;
        private String cancellationMotives;
        private Retry retry = new Retry();
        private String username;
        private String password;

        @Getter
        @Setter
        public static class Retry {
          private int attempts;
          private int delayMs;
        }

        @Data
        public static class CancellationMotive {
          private String type;
          private Integer category;
          private String cancelCode;
          private String guapol;
        }
      }
    }

    @Getter
    @Setter
    public static class WcXssInsuranceQuotationConfig {
      @DynaConfigAttribute private long timeout = 0;
      private String basePath;
    }
  }
}
