package com.example.springboot.integration.tcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.*;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.TcpOutboundGateway;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.time.LocalDateTime;

/**
 * Created by hocklo on 31/08/17.
 */
@SpringBootApplication
@EnableIntegration
@IntegrationComponentScan
public class Main {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
        System.out.println("Microservice with Spring Integration over TCP started very well!");
    }
        private int port = 8000;

        @MessagingGateway(defaultRequestChannel="toTcp")
        public interface Gateway {

            String viaTcp(String in);

        }

        @Bean
        @ServiceActivator(inputChannel="toTcp")
        public MessageHandler tcpOutGate(AbstractClientConnectionFactory connectionFactory) {
            TcpOutboundGateway gate = new TcpOutboundGateway();
            gate.setConnectionFactory(connectionFactory);
            gate.setOutputChannelName("resultToString");
            return gate;
        }

        @Bean
        public TcpInboundGateway tcpInGate(AbstractServerConnectionFactory connectionFactory)  {
            TcpInboundGateway inGate = new TcpInboundGateway();
            inGate.setConnectionFactory(connectionFactory);
            inGate.setRequestChannel(fromTcp());
            inGate.setReplyTimeout(1000);
            return inGate;
        }

        @Bean
        public MessageChannel fromTcp() {
            return new DirectChannel();
        }

        @MessageEndpoint
        public static class Echo {

            ObjectMapper mapper = new ObjectMapper();

            @Transformer(inputChannel="fromTcp", outputChannel="toEcho")
            public String convert(byte[] bytes) {
                return new String(bytes);
            }

            @ServiceActivator(inputChannel="toEcho")
            public String upCase(String in) {
                String output = null;
                try {
                    Request request = mapper.readValue(in, Request.class);
                    output = handleRequest(request);
                    System.out.println(String.format("Date:%s, Input:%s", LocalDateTime.now().toString(), in));
                } catch (Exception e) {
                    output = String.format("FAIL [%s]", e.getMessage());
                } finally {
                    if(output.contains("FAIL")) {
                        return null;
                    } else {
                        System.out.println(String.format("Date:%s, Output:%s", LocalDateTime.now().toString(), output));
                        return output;
                    }
                }

            }

            private String handleRequest(Request request) throws Exception {
                switch (request.getEndpoint()){
                    case "/api/":
                        return mapper.writeValueAsString(new Response(request.getMessage().toUpperCase()));
                    case "/api/hello":
                        return mapper.writeValueAsString(new Response(String.format("Hello, my name is Eduard!")));
                    default:
                        return "Bad request";
                }
            }

            @Transformer(inputChannel="resultToString")
            public String convertResult(byte[] bytes) {
                return new String(bytes);
            }

        }

        @Bean
        public AbstractClientConnectionFactory clientCF() {
            return new TcpNetClientConnectionFactory("localhost", this.port);
        }

        @Bean
        public AbstractServerConnectionFactory serverCF() {
            return new TcpNetServerConnectionFactory(this.port);
        }
}
