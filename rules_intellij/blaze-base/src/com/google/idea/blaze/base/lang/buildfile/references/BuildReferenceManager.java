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
package com.google.idea.blaze.base.lang.buildfile.references;

import com.google.common.collect.Lists;
import com.google.idea.blaze.base.io.FileAttributeProvider;
import com.google.idea.blaze.base.lang.buildfile.completion.BuildLookupElement;
import com.google.idea.blaze.base.lang.buildfile.psi.BuildFile;
import com.google.idea.blaze.base.lang.buildfile.psi.FuncallExpression;
import com.google.idea.blaze.base.model.primitives.Label;
import com.google.idea.blaze.base.model.primitives.RuleName;
import com.google.idea.blaze.base.model.primitives.WorkspacePath;
import com.google.idea.blaze.base.model.primitives.WorkspaceRoot;
import com.google.idea.blaze.base.sync.workspace.WorkspacePathResolver;
import com.google.idea.blaze.base.sync.workspace.WorkspacePathResolverProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.ex.temp.TempFileSystem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.util.PathUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

/**
 * Handles reference caching and resolving labels to PSI elements.
 */
public class BuildReferenceManager {

  public static BuildReferenceManager getInstance(Project project) {
    return ServiceManager.getService(project, BuildReferenceManager.class);
  }

  private final Project project;

  public BuildReferenceManager(Project project) {
    this.project = project;
  }

  /**
   * Finds the PSI element associated with the given label.
   */
  @Nullable
  public PsiElement resolveLabel(Label label) {
    return resolveLabel(label.blazePackage(), label.ruleName(), false);
  }

  /**
   * Finds the PSI element associated with the given label.
   */
  @Nullable
  public PsiElement resolveLabel(WorkspacePath packagePath, RuleName ruleName, boolean excludeRules) {
    File packageDir = resolvePackage(packagePath);
    if (packageDir == null) {
      return null;
    }

    if (!excludeRules) {
      FuncallExpression target = findRule(packageDir, ruleName);
      if (target != null) {
        return target;
      }
    }

    // try a direct file reference (e.g. ":a.java")
    File fullFile = new File(packageDir, ruleName.toString());
    if (FileAttributeProvider.getInstance().exists(fullFile)) {
      return resolveFile(fullFile);
    }

    return null;
  }

  private FuncallExpression findRule(File packageDir, RuleName ruleName) {
    BuildFile psiFile = findBuildFile(packageDir);
    return psiFile != null ? psiFile.findRule(ruleName.toString()) : null;
  }

  @Nullable
  public PsiFileSystemItem resolveFile(File file) {
    VirtualFile vf = getFileSystem().findFileByPath(file.getPath());
    if (vf == null) {
      return null;
    }
    PsiManager manager = PsiManager.getInstance(project);
    return vf.isDirectory() ? manager.findDirectory(vf) : manager.findFile(vf);
  }

  @Nullable
  public File resolvePackage(@Nullable WorkspacePath packagePath) {
    return resolveWorkspaceRelativePath(packagePath != null ? packagePath.relativePath() : null);
  }

  @Nullable
  private File resolveWorkspaceRelativePath(@Nullable String relativePath) {
    WorkspacePathResolver pathResolver = getWorkspacePathResolver();
    if (pathResolver == null || relativePath == null) {
      return null;
    }
    return pathResolver.resolveToFile(relativePath);
  }

  @Nullable
  private WorkspacePathResolver getWorkspacePathResolver() {
    return WorkspacePathResolverProvider.getInstance(project).getPathResolver();
  }

  /**
   * Finds all child directories. If exactly one is found, continue traversing (and appending to LookupElement string)
   * until there are multiple options.<br>
   * Used for package path completion suggestions.
   */
  public BuildLookupElement[] resolvePackageLookupElements(FileLookupData lookupData) {
    String relativePath = lookupData.filePathFragment;
    File file = resolveWorkspaceRelativePath(relativePath);

    FileAttributeProvider provider = FileAttributeProvider.getInstance();
    String pathFragment = "";
    if (file == null || (!provider.isDirectory(file) && !relativePath.endsWith("/"))) {
      // we might be partway through a file name. Try the parent directory
      relativePath = PathUtil.getParentPath(relativePath);
      file = resolveWorkspaceRelativePath(relativePath);
      pathFragment = StringUtil.trimStart(lookupData.filePathFragment.substring(relativePath.length()), "/");
    }
    if (file == null || !provider.isDirectory(file)) {
      return BuildLookupElement.EMPTY_ARRAY;
    }
    VirtualFile vf = getFileSystem().findFileByPath(file.getPath());
    if (vf == null || !vf.isDirectory()) {
      return BuildLookupElement.EMPTY_ARRAY;
    }
    BuildLookupElement[] uniqueLookup = new BuildLookupElement[1];
    while (true) {
      VirtualFile[] children = vf.getChildren();
      if (children == null || children.length == 0) {
        return uniqueLookup[0] != null ? uniqueLookup : BuildLookupElement.EMPTY_ARRAY;
      }
      List<VirtualFile> validChildren = Lists.newArrayListWithCapacity(children.length);
      for (VirtualFile child : children) {
        if (child.getName().startsWith(pathFragment) && lookupData.acceptFile(child)) {
          validChildren.add(child);
        }
      }
      if (validChildren.isEmpty()) {
        return uniqueLookup[0] != null ? uniqueLookup : BuildLookupElement.EMPTY_ARRAY;
      }
      if (validChildren.size() > 1) {
        return uniqueLookup[0] != null ?
               uniqueLookup : lookupsForFiles(validChildren, lookupData);
      }
      // continue traversing while there's only one option
      uniqueLookup[0] = lookupForFile(validChildren.get(0), lookupData);
      pathFragment = "";
      vf = validChildren.get(0);
    }
  }

  private BuildLookupElement[] lookupsForFiles(List<VirtualFile> files, FileLookupData lookupData) {
    BuildLookupElement[] lookups = new BuildLookupElement[files.size()];
    for (int i = 0; i < files.size(); i++) {
      lookups[i] = lookupForFile(files.get(i), lookupData);
    }
    return lookups;
  }

  private BuildLookupElement lookupForFile(VirtualFile file, FileLookupData lookupData) {
    WorkspacePath workspacePath = getWorkspaceRelativePath(file.getPath());
    return lookupData.lookupElementForFile(project, file, workspacePath);
  }

  @Nullable
  public BuildFile resolveBlazePackage(String workspaceRelativePath) {
    workspaceRelativePath = StringUtil.trimStart(workspaceRelativePath, "//");
    return resolveBlazePackage(WorkspacePath.createIfValid(workspaceRelativePath));
  }

  @Nullable
  public BuildFile resolveBlazePackage(@Nullable WorkspacePath path) {
    return findBuildFile(resolvePackage(path));
  }

  @Nullable
  private BuildFile findBuildFile(@Nullable File packageDirectory) {
    FileAttributeProvider provider = FileAttributeProvider.getInstance();
    if (packageDirectory == null || !provider.isDirectory(packageDirectory)) {
      return null;
    }
    File buildFile = new File(packageDirectory, "BUILD");
    if (!provider.exists(buildFile)) {
      return null;
    }
    VirtualFile vf = getFileSystem().findFileByPath(buildFile.getPath());
    if (vf == null) {
      return null;
    }
    PsiFile psiFile = PsiManager.getInstance(project).findFile(vf);
    return psiFile instanceof BuildFile ? (BuildFile) psiFile : null;
  }

  /**
   * For files references, returns the parent directory.<br>
   * For rule references, return the blaze package directory.
   */
  @Nullable
  public File resolveParentDirectory(@Nullable Label label) {
    return label != null ? resolveParentDirectory(label.blazePackage(), label.ruleName()) : null;
  }

  @Nullable
  private File resolveParentDirectory(WorkspacePath packagePath, RuleName ruleName) {
    File packageFile = resolvePackage(packagePath);
    if (packageFile == null) {
      return null;
    }
    String rulePathParent = PathUtil.getParentPath(ruleName.toString());
    return new File(packageFile, rulePathParent);
  }

  @Nullable
  public WorkspacePath getWorkspaceRelativePath(String absolutePath) {
    WorkspacePathResolver pathResolver = getWorkspacePathResolver();
    WorkspaceRoot workspaceRoot = pathResolver != null ? pathResolver.getWorkspaceRoot() : null;
    return workspaceRoot != null ? getWorkspaceRelativePath(workspaceRoot, absolutePath) : null;
  }

  @Nullable
  static private WorkspacePath getWorkspaceRelativePath(WorkspaceRoot workspaceRoot, String absolutePath) {
    File file = new File(absolutePath);
    if (workspaceRoot.isInWorkspace(file)) {
      return workspaceRoot.workspacePathFor(file);
    }
    return null;
  }

  private static VirtualFileSystem getFileSystem() {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return TempFileSystem.getInstance();
    }
    return LocalFileSystem.getInstance();
  }

}
