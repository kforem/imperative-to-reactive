package org.hazelcast.cache;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.connectionfactory.init.CompositeDatabasePopulator;
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer;
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import reactor.blockhound.BlockHound;

@SpringBootApplication
@EnableR2dbcRepositories
public class ImperativeToReactiveApplication {

    @Bean
    public ConnectionFactoryInitializer initialize(ConnectionFactory factory) {
        var initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(factory);
        var populator = new CompositeDatabasePopulator();
        populator.addPopulators(
                new ResourceDatabasePopulator(new ClassPathResource("/schema.sql")),
                new ResourceDatabasePopulator(new ClassPathResource("/data.sql"))
        );
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    @Bean
    public CachingService service(IMap<Long, Person> cache, PersonRepository repository) {
        return new CachingService(cache, repository);
    }

    @Bean
    public IMap<Long, Person> cache(HazelcastInstance instance) {
        return instance.getMap("persons");
    }

    public static void main(String[] args) {
        BlockHound.install();
        SpringApplication.run(ImperativeToReactiveApplication.class, args);
    }
}