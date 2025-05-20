package documentManagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync // Enable Spring's asynchronous method execution capability
public class AsyncConfig {

    @Bean(name = "taskExecutor") // Define a custom executor bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Number of threads to keep in the pool, even if they are idle
        executor.setMaxPoolSize(10); // Maximum number of threads that can be created
        executor.setQueueCapacity(25); // Capacity of the queue for tasks awaiting execution
        executor.setThreadNamePrefix("DocumentIngest-"); // Prefix for the names of the threads
        executor.initialize(); // Initialize the executor
        return executor;
    }
}
