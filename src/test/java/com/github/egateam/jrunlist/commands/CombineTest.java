/**
 * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED
 * REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
 */

package com.github.egateam.jrunlist.commands;

import com.github.egateam.commons.Utils;
import com.github.egateam.jrunlist.Cli;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CombineTest {
    // Store the original standard out before changing it.
    private final PrintStream originalStdout = System.out;
    private final PrintStream originalStderr = System.err;
    private ByteArrayOutputStream stdoutContent = new ByteArrayOutputStream();
    private ByteArrayOutputStream stderrContent = new ByteArrayOutputStream();

    @BeforeMethod
    public void beforeTest() {
        // Redirect all System.out to stdoutContent.
        System.setOut(new PrintStream(this.stdoutContent));
        System.setErr(new PrintStream(this.stderrContent));
    }

    @Test
    public void testCombineFailed() throws Exception {
        String[] args = {"combine"};
        Cli.main(args);

        Assert.assertTrue(this.stderrContent.toString().contains("Main parameters are required"),
            "Except parameters");
    }

    @Test(description = "Test command with Atha.yml")
    public void testExecute1() throws Exception {
        String fileName = Utils.expendResource("Atha.yml");
        String[] args = {"combine", fileName, "--outfile", "stdout"};
        Cli.main(args);

        Assert.assertEquals(this.stdoutContent.toString().split("\r\n|\r|\n").length, 3, "line count");
        Assert.assertFalse(this.stdoutContent.toString().contains("7232,7384"), "combined");
    }

    @Test(description = "Test command with brca2.yml")
    public void testExecute2() throws Exception {
        String fileName = Utils.expendResource("brca2.yml");
        String[] args = {"combine", fileName, "--outfile", "stdout"};
        Cli.main(args);

        Assert.assertEquals(this.stdoutContent.toString().split("\r\n|\r|\n").length, 2, "line count");
        Assert.assertTrue(this.stdoutContent.toString().contains("32316461-32316527"), "no changes");
    }

    @AfterMethod
    public void afterTest() {
        // Put back the standard out.
        System.setOut(this.originalStdout);
        System.setErr(this.originalStderr);

        // Clear the stdoutContent.
        this.stdoutContent = new ByteArrayOutputStream();
        this.stderrContent = new ByteArrayOutputStream();
    }
}
