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
package com.google.idea.blaze.base.plugin;

import com.google.idea.blaze.base.lang.buildfile.language.BuildFileLanguage;
import com.google.idea.blaze.base.lang.buildfile.language.BuildFileTypeFactory;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.components.ApplicationComponent;

/**
 * Runs on startup.
 */
public class BlazeSpecificInitializer extends ApplicationComponent.Adapter {

  @Override
  public void initComponent() {
    hideMakeActions();
    initializeBuildFileSupportStatus();
  }

  private static void initializeBuildFileSupportStatus() {
    BuildFileTypeFactory.updateBuildFileLanguageEnabled(BuildFileLanguage.buildFileSupportEnabled());
  }

  // The original actions will be visible only on plain IDEA projects.
  private static void hideMakeActions() {
    // 'Build' > 'Make Project' action
    BlazeActionRemover.hideAction("CompileDirty");

    // 'Build' > 'Make Modules' action
    BlazeActionRemover.hideAction(IdeActions.ACTION_MAKE_MODULE);

    // 'Build' > 'Rebuild' action
    BlazeActionRemover.hideAction(IdeActions.ACTION_COMPILE_PROJECT);

    // 'Build' > 'Compile Modules' action
    BlazeActionRemover.hideAction(IdeActions.ACTION_COMPILE);
  }

}
