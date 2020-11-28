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
package org.kordamp.jreleaser.maven.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.kordamp.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Jreleaser extends AbstractDomain {
    private final Project project = new Project();
    private final Release release = new Release();
    private final Packagers packagers = new Packagers();
    private final List<Distribution> distributions = new ArrayList<>();

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project.setAll(project);
    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release.setAll(release);
    }

    public Packagers getPackagers() {
        return packagers;
    }

    public void setPackagers(Packagers packagers) {
        this.packagers.setAll(packagers);
    }

    public List<Distribution> getDistributions() {
        return distributions;
    }

    public void setDistributions(Collection<Distribution> distributions) {
        this.distributions.clear();
        this.distributions.addAll(distributions);
    }

    public void addDistributions(Collection<Distribution> distributions) {
        this.distributions.addAll(distributions);
    }

    public Distribution findDistribution(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("Distribution name must not be blank");
        }

        if (distributions.isEmpty()) {
            throw new IllegalArgumentException("No distributions have been configured");
        }

        return distributions.stream()
            .filter(d -> name.equals(d.getName()))
            .findFirst()
            .orElseThrow((Supplier<IllegalArgumentException>) () -> {
                throw new IllegalArgumentException("Distribution '" + name + "' not found");
            });
    }

    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("project", project.asMap());
        map.put("release", release.asMap());
        map.put("packagers", packagers.asMap());
        map.put("distributions", distributions.stream()
            .map(Distribution::asMap)
            .collect(Collectors.toList()));
        return map;
    }
}
