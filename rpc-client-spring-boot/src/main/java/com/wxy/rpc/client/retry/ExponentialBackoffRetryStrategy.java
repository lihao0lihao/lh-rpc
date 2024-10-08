package com.wxy.rpc.client.retry;

import com.google.common.base.Stopwatch;
import com.wxy.rpc.core.protocol.RpcMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Slf4j
@Component
public class ExponentialBackoffRetryStrategy implements RetryStrategy {
    private static final int MAX_RETRY_TIMES = 5;
    private static final long INITIAL_BACKOFF_INTERVAL = 100; // 初始退避时间100毫秒
    private static final ExecutorService es = Executors.newFixedThreadPool(1);

    @Override
    public RpcMessage doRetry(Callable<RpcMessage> callable) throws Exception {
        int retryTimes = 0;
        long backoffInterval = INITIAL_BACKOFF_INTERVAL;
        Stopwatch stopwatch = Stopwatch.createUnstarted();

        while (retryTimes < MAX_RETRY_TIMES) {
            try {
                stopwatch.start();
                Future<RpcMessage> future = es.submit(callable);
                RpcMessage result = future.get();
                return result;
                //return callable.call();
            } catch (Exception e) {
                retryTimes++;
                log.warn("RPC call failed, retrying... Current retry times: {}", retryTimes, e);

                stopwatch.stop();
                long elapsedTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                long sleepTime = Math.min(backoffInterval, Math.max(0, backoffInterval - elapsedTime));
                stopwatch.reset();

                if (sleepTime > 0) {
                    log.info("Backing off for {} ms before next retry.", sleepTime);
                    Thread.sleep(sleepTime);
                }

                backoffInterval *= 2; // 指数退避
            }
        }

        throw new Exception("Maximum retry times exceeded, giving up.");
    }
}
