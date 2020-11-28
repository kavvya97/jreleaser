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
package org.kordamp.jreleaser.util;

import kr.motd.maven.os.Detector;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class OsUtils {
    private static final OsDetector OS_DETECTOR = new OsDetector();

    private OsUtils() {
        //noop
    }

    public static boolean isWindows() {
        return "windows".equals(OS_DETECTOR.get("os.detected.name"));
    }

    public static boolean isMac() {
        return "osx".equals(OS_DETECTOR.get("os.detected.name"));
    }

    public static String getValue(String key) {
        return OS_DETECTOR.get(key);
    }

    public static Set<String> keySet() {
        return OS_DETECTOR.getProperties().keySet();
    }

    public static Collection<String> values() {
        return OS_DETECTOR.getProperties().values();
    }

    public static Set<Map.Entry<String, String>> entrySet() {
        return OS_DETECTOR.getProperties().entrySet();
    }

    private static final class OsDetector extends Detector {
        private final Map<String, String> props = new LinkedHashMap<>();

        private OsDetector() {
            Properties p = new Properties();
            p.put("failOnUnknownOS", "false");
            detect(p, Collections.emptyList());
            p.stringPropertyNames().forEach(k -> props.put(k, p.getProperty(k)));
        }

        private Map<String, String> getProperties() {
            return Collections.unmodifiableMap(props);
        }

        private String get(String key) {
            return props.get(key);
        }

        @Override
        protected void log(String message) {
            // quiet
        }

        @Override
        protected void logProperty(String name, String value) {
            // quiet
        }
    }
}
