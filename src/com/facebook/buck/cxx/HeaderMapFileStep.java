/*
 * Copyright 2015-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.cxx;

import com.facebook.buck.apple.clang.HeaderMap;
import com.facebook.buck.io.ProjectFilesystem;
import com.facebook.buck.step.ExecutionContext;
import com.facebook.buck.step.Step;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.nio.file.Path;

public class HeaderMapFileStep implements Step {

  private final Path output;
  private final ImmutableMap<Path, Path> entries;

  public HeaderMapFileStep(Path output, ImmutableMap<Path, Path> entries) {
    this.output = output;
    this.entries = entries;
  }

  @Override
  public String getDescription(ExecutionContext context) {
    return "header map @ " + output.toString();
  }

  @Override
  public String getShortName() {
    return "header_map";
  }

  @Override
  public int execute(ExecutionContext context) {
    if (entries.isEmpty()) {
      return 0;
    }

    HeaderMap.Builder builder = HeaderMap.builder();
    ProjectFilesystem projectFilesystem = context.getProjectFilesystem();

    for (ImmutableMap.Entry<Path, Path> ent : entries.entrySet()) {
      String key = ent.getKey().toString();
      Path value = projectFilesystem.resolve(ent.getValue());
      builder.add(key, value);
    }

    HeaderMap headerMap = builder.build();

    try {
      projectFilesystem.writeBytesToPath(
          headerMap.getBytes(),
          output);
    } catch (IOException e) {
      context.logError(e, "Failed to write header map %s.", output);
      e.printStackTrace(context.getStdErr());
      return 1;
    }
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof HeaderMapFileStep)) {
      return false;
    }
    HeaderMapFileStep that = (HeaderMapFileStep) obj;
    return Objects.equal(this.output, that.output) && Objects.equal(this.entries, that.entries);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(output, entries);
  }

}
