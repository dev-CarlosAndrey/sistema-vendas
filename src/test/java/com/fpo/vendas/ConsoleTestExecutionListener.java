package com.fpo.vendas;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

public class ConsoleTestExecutionListener implements TestExecutionListener {

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (!testIdentifier.isTest()) return;

        String status = switch (testExecutionResult.getStatus()) {
            case SUCCESSFUL -> "PASSOU";
            case FAILED -> "FALHOU";
            case ABORTED -> "ABORTADO";
        };

        String className = testIdentifier.getSource()
                .filter(source -> source instanceof MethodSource)
                .map(source -> ((MethodSource) source).getClassName())
                .orElse("");

        System.out.printf("[%s] %s :: %s%n", status, className, testIdentifier.getDisplayName());
    }
}
