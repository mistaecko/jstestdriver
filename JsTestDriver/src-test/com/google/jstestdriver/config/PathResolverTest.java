/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.FlagsImpl;
import com.google.jstestdriver.PathResolver;
import com.google.jstestdriver.Plugin;
import com.google.jstestdriver.hooks.FileParsePostProcessor;
import com.google.jstestdriver.util.DisplayPathSanitizer;

public class PathResolverTest extends TestCase {

  File tmpDir;

  @Override
  protected void setUp() throws Exception {
    tmpDir = File.createTempFile("test", "JsTestDriver", new File(System
      .getProperty("java.io.tmpdir")));
    tmpDir.delete();
    tmpDir.mkdir();
    tmpDir.deleteOnExit();
  }

  private File createTmpSubDir(String dirName) {
    File codeDir = new File(tmpDir, dirName);
    codeDir.mkdir();
    codeDir.deleteOnExit();
    return codeDir;
  }

  private File createTmpFile(File codeDir, String fileName) throws IOException {
    File code = new File(codeDir, fileName);
    code.createNewFile();
    code.deleteOnExit();
    return code;
  }

  private FlagsImpl createFlags() {
    FlagsImpl flags = new FlagsImpl();
    flags.setPort(8080);
    return flags;
  }

  public void testParseConfigFileAndHaveListOfFiles() throws Exception {
    File codeDir = createTmpSubDir("code");
    File testDir = createTmpSubDir("test");
    createTmpFile(codeDir, "code.js");
    createTmpFile(codeDir, "code2.js");
    createTmpFile(testDir, "test.js");
    createTmpFile(testDir, "test2.js");
    createTmpFile(testDir, "test3.js");

    String configFile =
        "load:\n" +
        " - code/*.js\n" +
        " - test/*.js\n" +
        "exclude:\n" +
        " - code/code2.js\n" +
        " - test/test2.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config =
        parser.parse(new InputStreamReader(bais), null).resolvePaths(
            new PathResolver(tmpDir, Collections.<FileParsePostProcessor>emptySet(),
                new DisplayPathSanitizer(tmpDir)), createFlags());

    Set<FileInfo> files = config.getFilesList();
    List<FileInfo> listFiles = new ArrayList<FileInfo>(files);

    assertEquals(3, files.size());
    assertTrue(listFiles.get(0).getFilePath().replace(File.separatorChar, '/').endsWith("code/code.js"));
    assertTrue(listFiles.get(1).getFilePath().replace(File.separatorChar, '/').endsWith("test/test.js"));
    assertTrue(listFiles.get(2).getFilePath().replace(File.separatorChar, '/').endsWith("test/test3.js"));
  }
  public void testParseConfigFileAndHaveListOfFilesRelative() throws Exception {
    File codeDir = createTmpSubDir("code");
    File testDir = createTmpSubDir("test");
    createTmpFile(codeDir, "code.js");
    createTmpFile(codeDir, "code2.js");
    createTmpFile(testDir, "test.js");
    createTmpFile(testDir, "test2.js");
    createTmpFile(testDir, "test3.js");

    String configFile =
      "load:\n" +
      " - '*.js'\n" +
      " - '../test/*.js'\n" +
      "exclude:\n" +
      " - code2.js\n" +
      " - '../test/test2.js'\n";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();
    
    Configuration config =
      parser.parse(new InputStreamReader(bais), null).resolvePaths(
          new PathResolver(codeDir, Collections.<FileParsePostProcessor>emptySet(),
              new DisplayPathSanitizer(codeDir)), createFlags());
    
    Set<FileInfo> files = config.getFilesList();
    List<FileInfo> listFiles = new ArrayList<FileInfo>(files);
    
    assertEquals(3, files.size());
    assertFalse(listFiles.get(0).getDisplayPath().contains(".."));
    assertFalse(listFiles.get(1).getDisplayPath().contains(".."));
    assertFalse(listFiles.get(2).getDisplayPath().contains(".."));
    assertTrue(listFiles.get(0).getFilePath().replace(File.separatorChar, '/').endsWith("code/code.js"));
    assertTrue(listFiles.get(1).getFilePath().replace(File.separatorChar, '/').endsWith("test/test.js"));
    assertTrue(listFiles.get(2).getFilePath().replace(File.separatorChar, '/').endsWith("test/test3.js"));
  }

  public void testParseConfigFileAndHaveListOfFilesWithTests() throws Exception {
    File codeDir = createTmpSubDir("code");
    File testDir = createTmpSubDir("test");
    createTmpFile(codeDir, "code.js");
    createTmpFile(codeDir, "code2.js");
    createTmpFile(testDir, "test.js");
    createTmpFile(testDir, "test2.js");
    createTmpFile(testDir, "test3.js");
    
    String configFile =
      "load:\n" +
      " - code/*.js\n" +
      "test:\n" +
      " - test/*.js\n" +
      "exclude:\n" +
      " - code/code2.js\n" +
      " - test/test2.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();
    
    Configuration config = parser.parse(new InputStreamReader(bais), null).resolvePaths(
        new PathResolver(tmpDir, Collections.<FileParsePostProcessor> emptySet(), new DisplayPathSanitizer(tmpDir)), createFlags());
    
    Set<FileInfo> files = config.getFilesList();
    List<FileInfo> listFiles = Lists.newArrayList(files);
    
    assertEquals(1, files.size());
    assertTrue(listFiles.get(0).getFilePath().replace(File.separatorChar, '/').endsWith("code/code.js"));
    
    List<FileInfo> tests = config.getTests();
    assertEquals(new File(tmpDir, "test/test.js").getAbsolutePath(), tests.get(0).getFilePath());
    assertEquals("test/test.js", tests.get(0).getDisplayPath());
    assertEquals(new File(tmpDir, "test/test3.js").getAbsolutePath(), tests.get(1).getFilePath());
    assertEquals("test/test3.js", tests.get(1).getDisplayPath());
  }

  public void testParseConfigFileAndProcessAListOfFiles() throws Exception {
    File codeDir = createTmpSubDir("code");
    final String fileName = "code.js";
    final File code = createTmpFile(codeDir, fileName);

    String configFile = "load:\n - code/*.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bais), null).resolvePaths(
        new PathResolver(tmpDir,
            Sets.<FileParsePostProcessor>newHashSet(new FileParsePostProcessor(){
              public Set<FileInfo> process(Set<FileInfo> files) {
                Set<FileInfo> processed = Sets.newHashSet();
                for (FileInfo fileInfo : files) {
                  processed.add(new FileInfo(fileInfo.getFilePath(),
                      code.lastModified(), -1, false, true, null, fileInfo.getDisplayPath()));
                }
                return processed;
              }
            }), new DisplayPathSanitizer(tmpDir)), createFlags());
    Set<FileInfo> files = config.getFilesList();
    List<FileInfo> listFiles = new ArrayList<FileInfo>(files);

    assertEquals(1, files.size());
    assertTrue(listFiles.get(0).getFilePath().replace(File.separatorChar, '/').endsWith("code/code.js"));
    assertTrue(listFiles.get(0).isServeOnly());
  }

  public void testGlobIsExpanded() throws Exception {
    File codeDir = createTmpSubDir("code");
    createTmpFile(codeDir, "code.js");
    createTmpFile(codeDir, "code2.js");

    String configFile = "load:\n - code/*.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bais), null).resolvePaths(
        new PathResolver(tmpDir, Collections.<FileParsePostProcessor> emptySet(), new DisplayPathSanitizer(tmpDir)), createFlags());
    Set<FileInfo> files = config.getFilesList();
    List<FileInfo> listFiles = new ArrayList<FileInfo>(files);

    assertEquals(2, files.size());
    assertTrue(listFiles.get(0).getFilePath().replace(File.separatorChar, '/').endsWith("code/code.js"));
    assertTrue(listFiles.get(1).getFilePath().replace(File.separatorChar, '/').endsWith("code/code2.js"));
  }

  public void testParseConfigFileAndHaveListOfFilesWithPatches()
      throws Exception {
    File codeDir = createTmpSubDir("code");
    File testDir = createTmpSubDir("test");
    createTmpFile(codeDir, "code.js");
    createTmpFile(codeDir, "code2.js");
    createTmpFile(codeDir, "patch.js");
    createTmpFile(testDir, "test.js");
    createTmpFile(testDir, "test2.js");
    createTmpFile(testDir, "test3.js");

    String configFile = "load:\n" + "- code/code.js\n"
      + "- patch code/patch.js\n" + "- code/code2.js\n" + "- test/*.js\n"
      + "exclude:\n" + "- code/code2.js\n" + "- test/test2.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bais), null)
        .resolvePaths(new PathResolver(tmpDir, Collections.<FileParsePostProcessor> emptySet(), new DisplayPathSanitizer(tmpDir)), createFlags());
    Set<FileInfo> files = config.getFilesList();
    List<FileInfo> listFiles = new ArrayList<FileInfo>(files);

    assertEquals(3, files.size());
    assertTrue(listFiles.get(0).getFilePath().replace(File.separatorChar, '/').endsWith("code/code.js"));
    assertTrue(listFiles.get(1).getFilePath().replace(File.separatorChar, '/').endsWith("test/test.js"));
    assertTrue(listFiles.get(2).getFilePath().replace(File.separatorChar, '/').endsWith("test/test3.js"));
    assertTrue(listFiles.get(0).getPatches().get(0).getFilePath().replace(File.separatorChar, '/').endsWith(
      "code/patch.js"));
  }

  public void testParseConfigFileAndHaveListOfFilesWithUnassociatedPatch()
      throws Exception {
    File codeDir = createTmpSubDir("code");
    File testDir = createTmpSubDir("test");
    createTmpFile(codeDir, "code.js");
    createTmpFile(codeDir, "code2.js");
    createTmpFile(codeDir, "patch.js");
    createTmpFile(testDir, "test.js");
    createTmpFile(testDir, "test2.js");
    createTmpFile(testDir, "test3.js");

    String configFile = "load:\n" + "- patch code/patch.js\n"
      + "- code/code.js\n" + "- code/code2.js\n" + "- test/*.js\n"
      + "exclude:\n" + "- code/code2.js\n" + "- test/test2.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();
    try {
      parser.parse(new InputStreamReader(bais), null).resolvePaths(
          new PathResolver(tmpDir, Collections.<FileParsePostProcessor> emptySet(), new DisplayPathSanitizer(tmpDir)), createFlags());
      fail("should have thrown an exception due to patching a non-existant file");
    } catch (IllegalStateException e) {
      // pass
    }
  }

  public void testParsePlugin() throws IOException {
    String jarPath = "pathto.jar";
    File jar = createTmpFile(this.tmpDir, jarPath);
    Plugin expected = new Plugin("test", jar.getAbsolutePath(), "com.test.PluginModule",
        Lists.<String>newArrayList());
    String configFile =
        String.format("plugin:\n  - name: %s\n    jar: \"%s\"\n    module: \"%s\"\n",
            "test",
            jarPath,
            "com.test.PluginModule");
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config =
        parser.parse(new InputStreamReader(bais), null)
            .resolvePaths(new PathResolver(tmpDir, Collections.<FileParsePostProcessor>emptySet(), new DisplayPathSanitizer(tmpDir)),
                createFlags());
    List<Plugin> plugins = config.getPlugins();
    assertEquals(expected, plugins.get(0));
  }

  public void testParsePlugins() throws IOException {
    String jarPath = "pathto.jar";
    String jarPath2 = "pathto.jar2";
    File jar = createTmpFile(this.tmpDir, jarPath);
    File jar2 = createTmpFile(this.tmpDir, jarPath2);
    List<Plugin> expected = new LinkedList<Plugin>(Arrays.asList(
      new Plugin("test", jar.getAbsolutePath(), "com.test.PluginModule",
        Lists.<String> newArrayList()),
      new Plugin("test2", jar2.getAbsolutePath(), "com.test.PluginModule2",
        Lists.<String> newArrayList("hello", "world", "some/file.js"))));
    String configFile = String.format("plugin:\n" + "  - name: test\n"
      + "    jar: \"%s\"\n" + "    module: \"com.test.PluginModule\"\n"
      + "  - name: test2\n" + "    jar: \"%s\"\n"
      + "    module: \"com.test.PluginModule2\"\n"
      + "    args: hello, world, some/file.js\n", jarPath, jarPath2);
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bais), null)
        .resolvePaths(new PathResolver(tmpDir, Collections.<FileParsePostProcessor> emptySet(), new DisplayPathSanitizer(tmpDir)), createFlags());
    List<Plugin> plugins = config.getPlugins();

    assertEquals(2, plugins.size());
    assertEquals(expected, plugins);
    assertEquals(0, plugins.get(0).getArgs().size());
    assertEquals(3, plugins.get(1).getArgs().size());
  }

  public void testParsePluginArgs() throws Exception {
    String jarPath = "pathtojar";
    String jarPath2 = "pathtojar2";
    createTmpFile(this.tmpDir, jarPath);
    createTmpFile(this.tmpDir, jarPath2);
    String configFile = "plugin:\n" + "  - name: test\n"
      + "    jar: \"pathtojar\"\n" + "    module: \"com.test.PluginModule\"\n"
      + "    args: hello, mooh, some/file.js, another/file.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bais), null).resolvePaths(
        new PathResolver(tmpDir, Collections.<FileParsePostProcessor> emptySet(), new DisplayPathSanitizer(tmpDir)), createFlags());
    List<Plugin> plugins = config.getPlugins();
    Plugin plugin = plugins.get(0);
    List<String> args = plugin.getArgs();

    assertEquals(4, args.size());
    assertEquals("hello", args.get(0));
    assertEquals("mooh", args.get(1));
    assertEquals("some/file.js", args.get(2));
    assertEquals("another/file.js", args.get(3));
  }

  public void testParsePluginNoArgs() throws Exception {
    String jarPath = "pathtojar";
    String jarPath2 = "pathtojar2";
    createTmpFile(this.tmpDir, jarPath);
    createTmpFile(this.tmpDir, jarPath2);
    String configFile = "plugin:\n" + "  - name: test\n"
      + "    jar: \"pathtojar\"\n" + "    module: \"com.test.PluginModule\"\n";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bais), null).resolvePaths(
        new PathResolver(tmpDir, Collections.<FileParsePostProcessor> emptySet(), new DisplayPathSanitizer(tmpDir)), createFlags());
    List<Plugin> plugins = config.getPlugins();
    Plugin plugin = plugins.get(0);
    List<String> args = plugin.getArgs();

    assertEquals(0, args.size());
  }

  public void testServeFile() throws Exception {
    File codeDir = createTmpSubDir("code");
    File testDir = createTmpSubDir("test");
    File serveDir = createTmpSubDir("serve");
    createTmpFile(codeDir, "code.js");
    createTmpFile(codeDir, "code2.js");
    createTmpFile(testDir, "test.js");
    createTmpFile(testDir, "test2.js");
    createTmpFile(testDir, "test3.js");
    createTmpFile(serveDir, "serve1.js");

    String configFile = "load:\n" + " - code/*.js\n" + " - test/*.js\n"
      + "serve:\n" + " - serve/serve1.js\n" + "exclude:\n"
      + " - code/code2.js\n" + " - test/test2.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bais), null).resolvePaths(
        new PathResolver(tmpDir, Collections.<FileParsePostProcessor> emptySet(), new DisplayPathSanitizer(tmpDir)), createFlags());
    Set<FileInfo> serveFilesSet = config.getFilesList();
    List<FileInfo> serveFiles = new ArrayList<FileInfo>(serveFilesSet);

    assertEquals(4, serveFilesSet.size());
    assertTrue(serveFiles.get(0).getFilePath().replace(File.separatorChar, '/').endsWith("code/code.js"));
    assertTrue(serveFiles.get(1).getFilePath().replace(File.separatorChar, '/').endsWith("test/test.js"));
    assertTrue(serveFiles.get(2).getFilePath().replace(File.separatorChar, '/').endsWith("test/test3.js"));
    assertTrue(serveFiles.get(3).getFilePath().replace(File.separatorChar, '/').endsWith("serve/serve1.js"));
    assertTrue(serveFiles.get(3).isServeOnly());
  }

  public void testCheckValidTimeStamp() throws Exception {
    File codeDir = createTmpSubDir("code");
    File testDir = createTmpSubDir("test");
    createTmpFile(codeDir, "code.js");
    createTmpFile(codeDir, "code2.js");
    createTmpFile(testDir, "test.js");
    createTmpFile(testDir, "test2.js");
    createTmpFile(testDir, "test3.js");

    String configFile = "load:\n - code/*.js\n - test/*.js\nexclude:\n"
      + " - code/code2.js\n - test/test2.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bais), null).resolvePaths(
        new PathResolver(tmpDir, Collections.<FileParsePostProcessor> emptySet(), new DisplayPathSanitizer(tmpDir)), createFlags());
    Set<FileInfo> files = config.getFilesList();
    List<FileInfo> listFiles = new ArrayList<FileInfo>(files);

    assertEquals(3, files.size());
    assertTrue(listFiles.get(0).getTimestamp() > 0);
    assertTrue(listFiles.get(1).getTimestamp() > 0);
    assertTrue(listFiles.get(2).getTimestamp() > 0);
  }

  public void testExceptionIsThrownIfFileNotFound() throws Exception {
    File codeDir = createTmpSubDir("code");
    createTmpFile(codeDir, "code.js");

    String configFile = "load:\n - invalid-dir/code.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    try {
      parser.parse(new InputStreamReader(bais), null)
          .resolvePaths(new PathResolver(tmpDir, Collections.<FileParsePostProcessor> emptySet(), new DisplayPathSanitizer(tmpDir)), createFlags());
      fail("Exception not caught");
    } catch (IllegalArgumentException e) {
    }
  }

  public void testResolveFullQualifiedPath() throws Exception {
    File baseDir = createTmpSubDir("base");
    // test dir and file reside outside the base directory
    File absoluteDir = createTmpSubDir("absolute");
    File absoluteFile = new File(absoluteDir, "file.js");

    PathResolver pathResolver = new PathResolver(baseDir,
        Collections.<FileParsePostProcessor>emptySet(),
        new DisplayPathSanitizer(baseDir));

    File result1 = pathResolver.resolvePath(absoluteFile.getAbsolutePath());
    assertEquals(absoluteFile, result1);
  }

  public void testResolveFullQualifiedPathWithParentRef() throws Exception {
    File baseDir = createTmpSubDir("base");
    File dir = createTmpSubDir("absolute");
    File subDir = new File(dir, "sub");
    subDir.mkdir();
    subDir.deleteOnExit();
    
    File otherDir = createTmpSubDir("other");

    PathResolver pathResolver = new PathResolver(baseDir,
        Collections.<FileParsePostProcessor>emptySet(),
        new DisplayPathSanitizer(baseDir));

    {
      File file = new File(dir, "../file.js");
      File result = pathResolver.resolvePath(file.getAbsolutePath());
      assertEquals(new File(tmpDir, "file.js"), result);
    }
    {
      File file = new File(subDir, "../../other/file.js");
      File result = pathResolver.resolvePath(file.getAbsolutePath());
      assertEquals(new File(otherDir, "file.js"), result);
    }

  }

  public void testResolvePathFragement() {
    File baseDir = createTmpSubDir("base");

    PathResolver pathResolver = new PathResolver(baseDir,
        Collections.<FileParsePostProcessor>emptySet(),
        new DisplayPathSanitizer(baseDir));

    File result1 = pathResolver.resolvePath("file.js");
    assertEquals(new File(baseDir, "file.js").getAbsolutePath(), result1.getAbsolutePath());
  }

  public void testResolvePathFragementWithParentRef() {
    File baseDir = tmpDir;
    File dir = createTmpSubDir("dir");

    PathResolver pathResolver = new PathResolver(baseDir,
        Collections.<FileParsePostProcessor>emptySet(),
        new DisplayPathSanitizer(baseDir));

    File result = pathResolver.resolvePath("other/nowhere/../../dir/file.js");
    assertEquals(new File(dir, "file.js"), result);
  }
}
