/*
 * Copyright 2016 The Bazel Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.idea.blaze.base.projectview.section;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import org.jetbrains.jps.model.fileTypes.FileNameMatcherFactory;

import java.io.Serializable;
import java.util.Collection;

/**
 * Glob matcher.
 */
public class Glob implements Serializable {
  private static final long serialVersionUID = 1L;

  private String pattern;
  transient private FileNameMatcher matcher;

  public Glob(String pattern) {
    this.pattern = pattern;
  }

  public static class GlobSet implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Collection<Glob> globs = Lists.newArrayList();

    public GlobSet(Collection<Glob> globs) {
      this.globs.addAll(globs);
    }

    public boolean isEmpty() {
      return globs.isEmpty();
    }

    public void add(Glob glob) {
      globs.add(glob);
    }

    public boolean matches(String string) {
      for (Glob glob : globs) {
        if (glob.matches(string)) {
          return true;
        }
      }
      return false;
    }
  }

  public boolean matches(String string) {
    if (matcher == null) {
      matcher = FileNameMatcherFactory.getInstance().createMatcher(pattern);
    }
    return matcher.accept(string);
  }

  @Override
  public String toString() {
    return pattern;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Glob glob = (Glob)o;
    return Objects.equal(pattern, glob.pattern);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(pattern);
  }
}
