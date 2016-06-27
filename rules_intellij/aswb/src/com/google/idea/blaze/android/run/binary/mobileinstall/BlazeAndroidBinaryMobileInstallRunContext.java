/*
 * Copyright 2016 The Bazel Authors. All rights reserved.
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
package com.google.idea.blaze.android.run.binary.mobileinstall;

import com.android.ddmlib.IDevice;
import com.android.tools.idea.run.ApplicationIdProvider;
import com.android.tools.idea.run.ConsoleProvider;
import com.android.tools.idea.run.LaunchOptions;
import com.android.tools.idea.run.activity.DefaultStartActivityFlagsProvider;
import com.android.tools.idea.run.activity.StartActivityFlagsProvider;
import com.android.tools.idea.run.editor.AndroidDebugger;
import com.android.tools.idea.run.editor.AndroidDebuggerState;
import com.android.tools.idea.run.tasks.DebugConnectorTask;
import com.android.tools.idea.run.tasks.LaunchTask;
import com.android.tools.idea.run.tasks.LaunchTasksProvider;
import com.android.tools.idea.run.util.ProcessHandlerLaunchStatus;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.idea.blaze.android.run.BlazeAndroidRunConfigurationCommonState;
import com.google.idea.blaze.android.run.binary.BlazeAndroidBinaryApplicationIdProvider;
import com.google.idea.blaze.android.run.binary.BlazeAndroidBinaryApplicationLaunchTaskProvider;
import com.google.idea.blaze.android.run.binary.BlazeAndroidBinaryConsoleProvider;
import com.google.idea.blaze.android.run.binary.BlazeAndroidBinaryRunConfigurationState;
import com.google.idea.blaze.android.run.deployinfo.BlazeAndroidDeployInfo;
import com.google.idea.blaze.android.run.runner.*;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Run context for android_binary.
 */
public class BlazeAndroidBinaryMobileInstallRunContext implements BlazeAndroidRunContext {

  private final Project project;
  private final AndroidFacet facet;
  private final RunConfiguration runConfiguration;
  private final ExecutionEnvironment env;
  private final BlazeAndroidBinaryRunConfigurationState configState;
  private final ConsoleProvider consoleProvider;
  private final ApplicationIdProvider applicationIdProvider;
  private final BlazeApkBuildStepMobileInstall buildStep;

  public BlazeAndroidBinaryMobileInstallRunContext(Project project,
                                                   AndroidFacet facet,
                                                   RunConfiguration runConfiguration,
                                                   ExecutionEnvironment env,
                                                   BlazeAndroidRunConfigurationCommonState commonState,
                                                   BlazeAndroidBinaryRunConfigurationState configState,
                                                   ImmutableList<String> buildFlags) {
    this.project = project;
    this.facet = facet;
    this.runConfiguration = runConfiguration;
    this.env = env;
    this.configState = configState;
    this.consoleProvider = new BlazeAndroidBinaryConsoleProvider(project);
    this.buildStep = new BlazeApkBuildStepMobileInstall(project, env, commonState, buildFlags, configState.isUseSplitApksIfPossible());
    this.applicationIdProvider = new BlazeAndroidBinaryApplicationIdProvider(project, buildStep.getDeployInfo());
  }

  @Override
  public BlazeAndroidDeviceSelector getDeviceSelector() {
    return new BlazeAndroidDeviceSelector.NormalDeviceSelector();
  }

  @Override
  public void augmentEnvironment(ExecutionEnvironment env) {
  }

  @Override
  public void augmentLaunchOptions(@NotNull LaunchOptions.Builder options) {
    options
      .setDeploy(false)
      .setPmInstallOptions(configState.ACTIVITY_EXTRA_FLAGS)
      .setOpenLogcatAutomatically(true);
  }

  @NotNull
  @Override
  public ConsoleProvider getConsoleProvider() {
    return consoleProvider;
  }

  @Override
  public ApplicationIdProvider getApplicationIdProvider() throws ExecutionException {
    return applicationIdProvider;
  }

  @Override
  public BlazeApkBuildStep getBuildStep() {
    return buildStep;
  }

  @Override
  public LaunchTasksProvider getLaunchTasksProvider(
    LaunchOptions launchOptions,
    BlazeAndroidRunConfigurationDebuggerManager debuggerManager) throws ExecutionException {
    return new BlazeAndroidLaunchTasksProvider(project, this, applicationIdProvider, launchOptions, debuggerManager);
  }

  @Override
  public ImmutableList<LaunchTask> getDeployTasks(IDevice device, LaunchOptions launchOptions) throws ExecutionException {
    return ImmutableList.of();
  }

  @Override
  public LaunchTask getApplicationLaunchTask(LaunchOptions launchOptions,
                                             AndroidDebugger androidDebugger,
                                             AndroidDebuggerState androidDebuggerState,
                                             ProcessHandlerLaunchStatus processHandlerLaunchStatus) throws ExecutionException {
    final StartActivityFlagsProvider startActivityFlagsProvider = new DefaultStartActivityFlagsProvider(
      androidDebugger,
      androidDebuggerState,
      project,
      launchOptions.isDebug(),
      configState.ACTIVITY_EXTRA_FLAGS
    );

    BlazeAndroidDeployInfo deployInfo = Futures.get(buildStep.getDeployInfo(), ExecutionException.class);

    return BlazeAndroidBinaryApplicationLaunchTaskProvider.getApplicationLaunchTask(
      project,
      applicationIdProvider,
      deployInfo.getMergedManifestFile(),
      configState,
      startActivityFlagsProvider,
      processHandlerLaunchStatus
    );
  }

  @Nullable
  @Override
  public DebugConnectorTask getDebuggerTask(LaunchOptions launchOptions,
                                            AndroidDebugger androidDebugger,
                                            AndroidDebuggerState androidDebuggerState,
                                            Set<String> packageIds) throws ExecutionException {
    //noinspection unchecked
    return androidDebugger.getConnectDebuggerTask(env, null, packageIds, facet, androidDebuggerState, runConfiguration.getType().getId());
  }
}
