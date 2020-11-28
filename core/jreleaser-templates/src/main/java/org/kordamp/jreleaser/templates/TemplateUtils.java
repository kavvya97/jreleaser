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
package org.kordamp.jreleaser.templates;

import org.kordamp.jreleaser.model.Distribution;
import org.kordamp.jreleaser.util.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class TemplateUtils {
    private TemplateUtils() {
        // noop
    }

    public static String trimTplExtension(String str) {
        if (str.endsWith(".tpl")) {
            return str.substring(0, str.length() - 4);
        }
        return str;
    }

    public static Map<String, Reader> resolveAndMergeTemplates(Logger logger, Distribution.DistributionType distributionType, String toolName, Path templateDirectory) {
        Map<String, Reader> templates = resolveTemplates(logger, distributionType, toolName);
        if (null != templateDirectory && templateDirectory.toFile().exists()) {
            templates.putAll(resolveTemplates(logger, distributionType, toolName, templateDirectory));
        }
        return templates;
    }

    public static Map<String, Reader> resolveTemplates(Logger logger, Distribution.DistributionType distributionType, String toolName, Path templateDirectory) {
        Map<String, Reader> templates = new LinkedHashMap<>();

        try {
            Files.walkFileTree(templateDirectory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    templates.put(templateDirectory.relativize(file).toString(),
                        Files.newBufferedReader(file));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            String distributionTypeName = distributionType.name().toLowerCase().replace('_', '-');
            throw new IllegalStateException("Unexpected error reading templates for distribution " +
                distributionTypeName + "/" + toolName + " from " + templateDirectory.toAbsolutePath());
        }

        return templates;
    }

    public static Map<String, Reader> resolveTemplates(Logger logger, Distribution.DistributionType distributionType, String toolName) {
        String distributionTypeName = distributionType.name().toLowerCase().replace('_', '-');
        String templatePrefix = "META-INF/jreleaser/templates/" +
            distributionTypeName + "/" + toolName.toLowerCase();

        Map<String, Reader> templates = new LinkedHashMap<>();

        logger.debug("Resolving templates from classpath");
        URL location = resolveLocation(TemplateUtils.class);
        if (null == location) {
            throw new IllegalStateException("Could not find location of classpath templates");
        }

        try {
            if ("file".equals(location.getProtocol())) {
                boolean templateFound = false;
                JarFile jarFile = new JarFile(new File(location.toURI()));
                logger.debug("Searching for templates matching {}/*", templatePrefix);
                for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); ) {
                    JarEntry entry = e.nextElement();
                    if (entry.isDirectory() || !entry.getName().startsWith(templatePrefix)) {
                        continue;
                    }

                    String templateName = entry.getName().substring(templatePrefix.length() + 1);
                    templates.put(templateName, new InputStreamReader(jarFile.getInputStream(entry)));
                    logger.debug("Found template {}", templateName);
                    templateFound = true;
                }

                if (!templateFound) {
                    logger.error("Templates for {}/{} were not found", distributionTypeName, toolName);
                }
            } else {
                throw new IllegalStateException("Could not find location of classpath templates");
            }
        } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException("Unexpected error reading templates for distribution " +
                distributionTypeName + "/" + toolName + " from classpath.");
        }

        return templates;
    }

    public static Reader resolveTemplate(Logger logger, Class<?> anchor, String templateKey) {
        logger.debug("Resolving template from classpath for {}@{}", anchor.getName(), templateKey);
        URL location = resolveLocation(anchor);
        if (null == location) {
            throw new IllegalStateException("Could not find location of classpath templates");
        }

        try {
            if ("file".equals(location.getProtocol())) {
                JarFile jarFile = new JarFile(new File(location.toURI()));
                logger.debug("Searching for template matching {}", templateKey);
                for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); ) {
                    JarEntry entry = e.nextElement();
                    if (entry.isDirectory() || !entry.getName().equals(templateKey)) {
                        continue;
                    }

                    logger.debug("Found template {}", templateKey);
                    return new InputStreamReader(jarFile.getInputStream(entry));
                }
                throw new IllegalStateException("Template for " +
                    anchor.getName() + "@" + templateKey + " were not found");
            } else {
                throw new IllegalStateException("Could not find location of classpath templates");
            }
        } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException("Unexpected error reading template for " +
                anchor.getName() + "@" + templateKey + " from classpath.");
        }
    }

    private static URL resolveLocation(Class<?> klass) {
        if (klass == null) return null;

        try {
            URL codeSourceLocation = klass.getProtectionDomain()
                .getCodeSource()
                .getLocation();
            if (codeSourceLocation != null) return codeSourceLocation;
        } catch (SecurityException | NullPointerException ignored) {
            // noop
        }

        URL classResource = klass.getResource(klass.getSimpleName() + ".class");
        if (classResource == null) return null;

        String url = classResource.toString();
        String suffix = klass.getCanonicalName().replace('.', '/') + ".class";
        if (!url.endsWith(suffix)) return null;
        String path = url.substring(0, url.length() - suffix.length());

        if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);

        try {
            return new URL(path);
        } catch (MalformedURLException ignored) {
            return null;
        }
    }
}
