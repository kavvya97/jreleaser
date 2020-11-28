/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.kordamp.jreleaser.gradle.plugin.dsl.Plug

import javax.inject.Inject

import static org.kordamp.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class PlugImpl implements Plug {
    String name
    final MapProperty<String, String> attributes

    @Inject
    PlugImpl(ObjectFactory objects) {
        attributes = objects.mapProperty(String, String).convention([:])
    }

    void setName(String name) {
        this.name = name
    }

    @Override
    void addAttribute(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            attributes.put(key.trim(), value.trim())
        }
    }

    org.kordamp.jreleaser.model.Plug toModel() {
        org.kordamp.jreleaser.model.Plug plug = new org.kordamp.jreleaser.model.Plug()
        plug.name = name
        plug.attributes.putAll(attributes.get())
        plug
    }
}
