/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal.tasks.compile.daemon;

import com.google.common.collect.Lists;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.compile.BaseForkOptions;
import org.gradle.internal.Cast;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.classloader.ClassLoaderUtils;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.language.base.internal.compile.CompileSpec;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.workers.WorkerExecution;
import org.gradle.workers.WorkerParameters;
import org.gradle.workers.internal.ActionExecutionSpecFactory;
import org.gradle.workers.internal.DaemonForkOptions;
import org.gradle.workers.internal.DefaultWorkResult;
import org.gradle.workers.internal.ProvidesWorkResult;
import org.gradle.workers.internal.Worker;
import org.gradle.workers.internal.WorkerFactory;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Set;

import static org.gradle.process.internal.util.MergeOptionsUtil.mergeHeapSize;
import static org.gradle.process.internal.util.MergeOptionsUtil.normalized;

public abstract class AbstractDaemonCompiler<T extends CompileSpec> implements Compiler<T> {
    private final WorkerFactory workerFactory;
    private final ActionExecutionSpecFactory actionExecutionSpecFactory;

    public AbstractDaemonCompiler(WorkerFactory workerFactory, ActionExecutionSpecFactory actionExecutionSpecFactory) {
        this.workerFactory = workerFactory;
        this.actionExecutionSpecFactory = actionExecutionSpecFactory;
    }

    @Override
    public WorkResult execute(T spec) {
        DaemonForkOptions daemonForkOptions = toDaemonForkOptions(spec);
        Worker worker = workerFactory.getWorker(daemonForkOptions);

        CompilerParameters parameters = getCompilerParameters(spec);
        DefaultWorkResult result = worker.execute(actionExecutionSpecFactory.newIsolatedSpec("compiler daemon", CompilerWorkerExecution.class, parameters, daemonForkOptions.getClassLoaderStructure()));
        if (result.isSuccess()) {
            return result;
        } else {
            throw UncheckedException.throwAsUncheckedException(result.getException());
        }
    }

    protected abstract DaemonForkOptions toDaemonForkOptions(T spec);

    protected abstract CompilerParameters getCompilerParameters(T spec);

    protected BaseForkOptions mergeForkOptions(BaseForkOptions left, BaseForkOptions right) {
        BaseForkOptions merged = new BaseForkOptions();
        merged.setMemoryInitialSize(mergeHeapSize(left.getMemoryInitialSize(), right.getMemoryInitialSize()));
        merged.setMemoryMaximumSize(mergeHeapSize(left.getMemoryMaximumSize(), right.getMemoryMaximumSize()));
        Set<String> mergedJvmArgs = normalized(left.getJvmArgs());
        mergedJvmArgs.addAll(normalized(right.getJvmArgs()));
        merged.setJvmArgs(Lists.newArrayList(mergedJvmArgs));
        return merged;
    }

    public abstract static class CompilerParameters implements WorkerParameters, Serializable {
        private final String compilerClassName;
        private final Object[] compilerInstanceParameters;

        public CompilerParameters(String compilerClassName, Object[] compilerInstanceParameters) {
            this.compilerClassName = compilerClassName;
            this.compilerInstanceParameters = compilerInstanceParameters;
        }

        public String getCompilerClassName() {
            return compilerClassName;
        }

        public Object[] getCompilerInstanceParameters() {
            return compilerInstanceParameters;
        }

        abstract public CompileSpec getCompileSpec();
    }

    public static abstract class CompilerWorkerExecution implements WorkerExecution<CompilerParameters>, ProvidesWorkResult {
        private DefaultWorkResult workResult;
        private final Instantiator instantiator;

        @Inject
        public CompilerWorkerExecution(Instantiator instantiator) {
            this.instantiator = instantiator;
        }

        @Override
        public void execute() {
            Class<? extends Compiler<?>> compilerClass = Cast.uncheckedCast(ClassLoaderUtils.classFromContextLoader(getParameters().getCompilerClassName()));
            Compiler<?> compiler = instantiator.newInstance(compilerClass, getParameters().getCompilerInstanceParameters());
            setWorkResult(compiler.execute(Cast.uncheckedCast(getParameters().getCompileSpec())));
        }

        private void setWorkResult(WorkResult workResult) {
            if (workResult instanceof DefaultWorkResult) {
                this.workResult = (DefaultWorkResult) workResult;
            } else {
                this.workResult = new DefaultWorkResult(workResult.getDidWork(), null);
            }
        }

        @Override
        public DefaultWorkResult getWorkResult() {
            return workResult;
        }
    }
}
