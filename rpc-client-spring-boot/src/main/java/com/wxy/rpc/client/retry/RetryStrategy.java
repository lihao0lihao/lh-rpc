package com.wxy.rpc.client.retry;

import com.wxy.rpc.client.transport.RpcClient;
import com.wxy.rpc.core.protocol.RpcMessage;

import java.util.concurrent.Callable;

public interface RetryStrategy {
    /**
     * 重试
     * @param callable 重试的方法 代表一个任务
     * @return
     * @throws Exception
     */
    RpcMessage doRetry(Callable<RpcMessage> callable) throws Exception;
}