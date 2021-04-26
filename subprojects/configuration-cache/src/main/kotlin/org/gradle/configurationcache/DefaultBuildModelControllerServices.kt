/*
 * Copyright 2021 the original author or authors.
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

package org.gradle.configurationcache

import org.gradle.api.internal.GradleInternal
import org.gradle.configuration.ProjectsPreparer
import org.gradle.configurationcache.extensions.get
import org.gradle.configurationcache.initialization.ConfigurationCacheStartParameter
import org.gradle.initialization.ConfigurationCache
import org.gradle.initialization.ConfigurationCacheAwareBuildModelController
import org.gradle.initialization.SettingsPreparer
import org.gradle.initialization.TaskExecutionPreparer
import org.gradle.initialization.VintageBuildModelController
import org.gradle.internal.build.BuildModelController
import org.gradle.internal.build.BuildModelControllerServices
import org.gradle.internal.service.scopes.BuildScopeServices


class DefaultBuildModelControllerServices : BuildModelControllerServices {
    override fun supplyBuildScopeServices(services: BuildScopeServices) {
        services.register {
            addProvider(BuildScopeServicesProvider())
        }
    }

    class BuildScopeServicesProvider {
        fun createBuildModelController(gradle: GradleInternal, startParameter: ConfigurationCacheStartParameter): BuildModelController {
            val projectsPreparer: ProjectsPreparer = gradle.services.get()
            val settingsPreparer: SettingsPreparer = gradle.services.get()
            val taskExecutionPreparer: TaskExecutionPreparer = gradle.services.get()
            val configurationCache: ConfigurationCache = gradle.services.get()
            val vintageController = VintageBuildModelController(gradle, projectsPreparer, settingsPreparer, taskExecutionPreparer)
            return if (startParameter.isEnabled) {
                ConfigurationCacheAwareBuildModelController(gradle, vintageController, configurationCache)
            } else {
                vintageController
            }
        }
    }
}
