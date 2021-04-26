/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.testfixtures


import org.gradle.api.internal.tasks.testing.worker.TestWorker
import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.executer.GradleContextualExecuter
import org.gradle.util.internal.TextUtil
import spock.lang.IgnoreIf

@IgnoreIf({ GradleContextualExecuter.isEmbedded()}) // this requires a full distribution
class ProjectBuilderEndUserIntegrationTest extends AbstractIntegrationSpec {

    def setup() {
        buildFile << """
        apply plugin: 'groovy'

        dependencies {
            implementation localGroovy()
            implementation gradleApi()
            testImplementation(platform("org.spockframework:spock-bom:2.0-M5-groovy-3.0"))
            testImplementation("org.spockframework:spock-core")
            testImplementation("org.spockframework:spock-junit4")
            testImplementation("junit:junit:4.13.1")
        }

        ${mavenCentralRepository()}

        test {
            useJUnitPlatform()
            testLogging.exceptionFormat = 'full'
        }

        """
    }

    def "project builder has correctly set working directory"() {
        when:
        def workerTmpDir = file("build/tmp/test/work")
        groovyTestSourceFile """
        import org.gradle.testfixtures.ProjectBuilder
        import spock.lang.Specification

        class Test extends Specification {

            def "system property is set"() {
                expect:
                System.getProperty("${TestWorker.WORKER_TMPDIR_SYS_PROPERTY}") != null
            }

            def "project builder has expected user home"() {
                when:
                def gradleUserHome = ProjectBuilder.builder().build().gradle.gradleUserHomeDir
                then:
                gradleUserHome.toPath().startsWith("${TextUtil.normaliseFileSeparators(workerTmpDir.absolutePath)}")
                gradleUserHome.absolutePath.endsWith("userHome")
            }
        }
        """
        then:
        succeeds('test')
    }
}
