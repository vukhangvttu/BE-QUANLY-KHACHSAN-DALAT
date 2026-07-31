package vttu.khachsanservice.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // ⏱️ Connect Timeout: 10s
                .responseTimeout(Duration.ofSeconds(20)) // ⏱️ Read Timeout: 20s
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(20))     // read timeout
                        .addHandlerLast(new WriteTimeoutHandler(20)));  // write timeout

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter((request, next) -> next.exchange(request)
                        .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                                .filter(throwable -> throwable instanceof WebClientRequestException)
                                .onRetryExhaustedThrow((retrySpec, retrySignal) -> retrySignal.failure())));
    }
}
