package com.supplier.test;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

/**
 * 单元测试基类（纯 Mockito，不启动 Spring 容器）
 * <p>
 * 用于 Service/Util 层的快速单元测试。
 */
public abstract class BaseUnitTest {

    @BeforeEach
    void initMocks() {
        MockitoAnnotations.openMocks(this);
    }
}