package hudson.plugins.warnings.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests the class {@link AntJavacParser}.
 */
public class AntJavacParserTest extends ParserTester {
    /** Error message. */
    private static final String WRONG_NUMBER_OF_WARNINGS_DETECTED = "Wrong number of warnings detected.";

    /**
     * Parses a file with two deprecation warnings.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseDeprecation() throws IOException {
        Collection<FileAnnotation> warnings = new AntJavacParser().parse(openFile());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        FileAnnotation annotation = iterator.next();
        checkWarning(annotation,
                28,
                "begrussen() in ths.types.IGruss has been deprecated",
                "C:/Users/tiliven/.hudson/jobs/Hello THS Trunk - compile/workspace/HelloTHSTest/src/ths/Hallo.java",
                AntJavacParser.WARNING_TYPE, "Deprecation", Priority.NORMAL);
    }

    /**
     * Parses a warning log with 2 ANT warnings.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.hudson-ci.org/browse/HUDSON-2133">Issue 2133</a>
     */
    @Test
    public void issue2133() throws IOException {
        Collection<FileAnnotation> warnings = new AntJavacParser().parse(openFile("issue2133.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 2, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                86,
                "non-varargs call of varargs method with inexact argument type for last parameter;",
                "/home/hudson/hudson/data/jobs/Mockito/workspace/trunk/test/org/mockitousage/misuse/DescriptiveMessagesOnMisuseTest.java",
                AntJavacParser.WARNING_TYPE, "", Priority.NORMAL);
        checkWarning(iterator.next(),
                51,
                "<T>stubVoid(T) in org.mockito.Mockito has been deprecated",
                "/home/hudson/hudson/data/jobs/Mockito/workspace/trunk/test/org/mockitousage/stubbing/StubbingWithThrowablesTest.java",
                AntJavacParser.WARNING_TYPE, RegexpParser.DEPRECATION, Priority.NORMAL);
    }

    /**
     * Parses a warning log with 1 warnings that has no associated file.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.hudson-ci.org/browse/HUDSON-4098">Issue 4098</a>
     */
    @Test
    public void issue4098() throws IOException {
        Collection<FileAnnotation> warnings = new AntJavacParser().parse(openFile("issue4098.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                0,
                "bad path element \"C:\\...\\.hudson\\jobs\\...\\log4j.jar\": no such file or directory",
                "C:/.../.hudson/jobs/.../log4j.jar",
                AntJavacParser.WARNING_TYPE, "Path", Priority.NORMAL);
    }

    /**
     * Parses a warning log with 20 ANT warnings. 2 of them are duplicate, all are of priority Normal.
     *
     * @throws IOException
     *      if the file could not be read
     * @see <a href="http://issues.hudson-ci.org/browse/HUDSON-2316">Issue 2316</a>
     */
    @Test
    public void issue2316() throws IOException {
        Collection<FileAnnotation> warnings = new AntJavacParser().parse(openFile("issue2316.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 20, warnings.size());

        ParserResult result = new ParserResult();
        result.addAnnotations(warnings);
        Assert.assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 18, result.getNumberOfAnnotations());
        Assert.assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 0, result.getNumberOfAnnotations(Priority.HIGH));
        Assert.assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 18, result.getNumberOfAnnotations(Priority.NORMAL));
        Assert.assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 0, result.getNumberOfAnnotations(Priority.LOW));
    }

    /**
     * Parses a warning log with 3 ANT warnings. They all use different tasks.
     *
     * @throws IOException
     *      if the file could not be read
     */
    @Test
    public void parseDifferentTaskNames() throws IOException {
        Collection<FileAnnotation> warnings = new AntJavacParser().parse(openFile("taskname.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 3, warnings.size());
    }

    /**
     * Verifies that arrays in deprecated methods are correctly handled.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void parseArrayInDeprecatedMethod() throws IOException {
        Collection<FileAnnotation> warnings = new AntJavacParser().parse(openFile("issue5868.txt"));

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, 1, warnings.size());

        Iterator<FileAnnotation> iterator = warnings.iterator();
        checkWarning(iterator.next(),
                225,
                "loadAvailable(java.lang.String,int,int,java.lang.String[]) in my.OtherClass has been deprecated",
                "D:/path/to/my/Class.java",
                AntJavacParser.WARNING_TYPE, "Deprecation", Priority.NORMAL);
    }

    /** {@inheritDoc} */
    @Override
    protected String getWarningsFile() {
        return "ant-javac.txt";
    }
}

