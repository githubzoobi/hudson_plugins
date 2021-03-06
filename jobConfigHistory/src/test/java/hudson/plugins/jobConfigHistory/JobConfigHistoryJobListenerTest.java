/**
 * Copyright 2010 Mirko Friedenhagen
 */

package hudson.plugins.jobConfigHistory;

import hudson.model.FreeStyleProject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jvnet.hudson.test.HudsonTestCase;
import org.xml.sax.SAXException;

/**
 * @author mirko
 *
 */
public class JobConfigHistoryJobListenerTest extends HudsonTestCase {

    private File jobsDir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        jobsDir = new File(hudson.root, "jobs");
    }

    public void testCreation() throws IOException, SAXException {
        createFreeStyleProject("newjob");
        final List<File> historyFiles = Arrays.asList(new File(jobsDir, "newjob/config-history").listFiles());
        assertTrue("Expected " + historyFiles.toString() + " to have at least one entry", historyFiles.size()>=1);
    }

    public void testRename() throws IOException, SAXException, InterruptedException {
        final FreeStyleProject project = createFreeStyleProject("newjob");
        // Sleep two seconds to make sure we have at least two history entries.
        Thread.sleep(TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS));
        project.renameTo("renamedjob");
        final File[] historyFiles = new File(jobsDir, "newjob/config-history").listFiles();
        assertNull("Got history files for old job", historyFiles);
        final List<File> historyFilesNew = Arrays.asList(new File(jobsDir, "renamedjob/config-history").listFiles());
        assertTrue("Expected " + historyFilesNew.toString() + " to have at least two entries", historyFilesNew.size()>=1);
    }

    public void testNonAbstractProjects() {
        final JobConfigHistoryJobListener listener = new JobConfigHistoryJobListener();
        listener.onCreated(null);
        listener.onRenamed(null, "oldName", "newName");
    }
}
