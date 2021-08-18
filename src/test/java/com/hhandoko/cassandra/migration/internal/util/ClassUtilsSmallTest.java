/**
 * File     : ClassUtilsSmallTest.java
 * License  :
 *   Original   - Copyright (c) 2010 - 2016 Boxfuse GmbH
 *   Derivative - Copyright (c) 2016 - 2018 cassandra-migration Contributors
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.hhandoko.cassandra.migration.internal.util;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.hhandoko.cassandra.migration.internal.util.scanner.Resource;
import com.hhandoko.cassandra.migration.internal.util.scanner.classpath.ClassPathResource;
import com.hhandoko.cassandra.migration.internal.util.scanner.classpath.ClassPathScanner;

import static org.junit.Assert.*;

/**
 * Test for ClassUtils.
 */
public class ClassUtilsSmallTest {
    /**
     * The old classloader, to be restored after a test completes.
     */
    private static ClassLoader oldClassLoader;

    @BeforeClass
    public static void setUp() throws IOException {
        oldClassLoader = getClassLoader();
        String jar = new ClassPathResource("no-directory-entries.jar", getClassLoader()).getLocationOnDisk();
        assertTrue(new File(jar).isFile());
        ClassUtils.addJarOrDirectoryToClasspath(jar);
    }

    private static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    @AfterClass
    public static void tearDown() {
        Thread.currentThread().setContextClassLoader(oldClassLoader);
    }

    @Ignore
    @Test
    public void isPresent() {
        assertTrue(ClassUtils.isPresent("com.hhandoko.cassandra.migration.CassandraMigration", Thread.currentThread().getContextClassLoader()));
    }

    @Ignore
    @Test
    public void isPresentNot() {
        assertFalse(ClassUtils.isPresent("com.example.FakeClass", Thread.currentThread().getContextClassLoader()));
    }

    /**
     * Tests dynamically adding a directory to the classpath.
     */
    @Ignore
    @Test
    public void addDirectoryToClasspath() throws Exception {
        assertFalse(new ClassPathResource("pkg/runtime.conf", getClassLoader()).exists());

        String folder = new ClassPathResource("dynamic", getClassLoader()).getLocationOnDisk();
        ClassUtils.addJarOrDirectoryToClasspath(folder);

        assertTrue(new ClassPathResource("pkg/runtime.conf", getClassLoader()).exists());

        Resource[] resources = new ClassPathScanner(getClassLoader()).scanForResources(new Location("classpath:pkg"), "run", ".conf");
        assertEquals("pkg/runtime.conf", resources[0].getLocation());
    }

    /**
     * Tests dynamically adding a directory to the default package of classpath.
     */
    @Ignore
    @Test
    public void addDirectoryToClasspathDefaultPackage() throws Exception {
        assertFalse(new ClassPathResource("runtime.conf", getClassLoader()).exists());

        String folder = new ClassPathResource("dynamic/pkg2", getClassLoader()).getLocationOnDisk();
        ClassUtils.addJarOrDirectoryToClasspath(folder);

        assertTrue(new ClassPathResource("funtime.properties", getClassLoader()).exists());

        Resource[] resources = new ClassPathScanner(getClassLoader()).scanForResources(new Location("classpath:"), "fun", ".properties");
        assertEquals("funtime.properties", resources[1].getLocation());
    }
}
