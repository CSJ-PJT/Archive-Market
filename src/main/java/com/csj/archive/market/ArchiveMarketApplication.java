package com.csj.archive.market;

import com.csj.archive.market.integration.archiveos.ArchiveOsProperties;
import com.csj.archive.market.integration.ledger.LedgerPublishProperties;
import com.csj.archive.market.integration.nexus.NexusPublishProperties;
import com.csj.archive.market.profitability.ProfitabilityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        NexusPublishProperties.class,
        LedgerPublishProperties.class,
        ArchiveOsProperties.class,
        ProfitabilityProperties.class
})
public class ArchiveMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArchiveMarketApplication.class, args);
    }
}
