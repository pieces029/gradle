/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.workers;

import org.gradle.api.Incubating;

import javax.inject.Inject;

/**
 * Represents the implementation of a unit of work to be used when submitting work to the
 * {@link WorkerExecutor}.
 *
 * <p>
 *     A worker execution implementation is an abstract class implementing the {@link #execute()} method.
 *     A minimal implementation may look like this:
 * </p>
 *
 * <pre class='autoTested'>
 * import org.gradle.workers.WorkerParameters;
 *
 * public abstract class MyExecution implements WorkerExecution&lt;WorkerParameters.None&gt; {
 *     private final String greeting;
 *
 *     {@literal @}Inject
 *     public MyExecution() {
 *         this.greeting = "hello";
 *     }
 *
 *     {@literal @}Override
 *     public void execute() {
 *         System.out.println(greeting);
 *     }
 * }
 * </pre>
 *
 * Implementations of WorkerExecution are subject to the following constraints:
 * <ul>
 *     <li>Do not implement {@link #getParameters()} in your class, the method will be implemented by Gradle.</li>
 *     <li>Constructors must be annotated with {@link Inject}.</li>
 * </ul>
 *
 * @param <T> Parameter type for the worker execution. Should be {@link WorkerParameters.None} if the execution does not have parameters.
 * @since 5.6
 **/
@Incubating
public interface WorkerExecution<T extends WorkerParameters> {
    /**
     * The parameters associated with a concrete work item.
     */
    @Inject
    T getParameters();

    /**
     * The work to perform when this work item executes.
     */
    void execute();
}
