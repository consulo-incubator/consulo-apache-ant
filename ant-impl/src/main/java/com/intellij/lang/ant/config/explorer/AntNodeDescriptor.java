/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.lang.ant.config.explorer;

import javax.annotation.Nonnull;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.CellAppearanceEx;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import consulo.awt.TargetAWT;

public abstract class AntNodeDescriptor extends NodeDescriptor implements CellAppearanceEx {
  public AntNodeDescriptor(Project project, NodeDescriptor parentDescriptor) {
    super(project, parentDescriptor);
  }

  public abstract boolean isAutoExpand();

  public void customize(@Nonnull SimpleColoredComponent component) {
    component.setIcon(getIcon());
    component.append(toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
  }

  @Nonnull
  public String getText() {
    return toString();
  }
}
