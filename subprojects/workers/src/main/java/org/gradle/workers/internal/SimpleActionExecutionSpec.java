/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.workers.internal;

import org.gradle.workers.WorkerExecution;
import org.gradle.workers.WorkerParameters;

public class SimpleActionExecutionSpec<T extends WorkerParameters> implements ActionExecutionSpec<T> {
    private final Class<? extends WorkerExecution<T>> implementationClass;
    private final String displayName;
    private final T params;
    private final ClassLoaderStructure classLoaderStructure;

    public SimpleActionExecutionSpec(Class<? extends WorkerExecution<T>> implementationClass, String displayName, T params, ClassLoaderStructure classLoaderStructure) {
        this.implementationClass = implementationClass;
        this.displayName = displayName;
        this.params = params;
        this.classLoaderStructure = classLoaderStructure;
    }

    @Override
    public Class<? extends WorkerExecution<T>> getImplementationClass() {
        return implementationClass;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public T getParameters() {
        return params;
    }

    @Override
    public ClassLoaderStructure getClassLoaderStructure() {
        return classLoaderStructure;
    }
}
