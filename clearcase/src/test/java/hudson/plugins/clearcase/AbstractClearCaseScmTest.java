/**
 * The MIT License
 *
 * Copyright (c) 2007-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Erik Ramfelt,
 *                          Henrik Lynggaard, Peter Liljenberg, Andrew Bayer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.clearcase;

import hudson.plugins.clearcase.history.HistoryAction;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixBuild;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Build; 
import hudson.model.AbstractBuild; 
import hudson.model.Node; 
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.TaskListener;

import hudson.plugins.clearcase.action.CheckOutAction;
import hudson.plugins.clearcase.action.SaveChangeLogAction;
import hudson.plugins.clearcase.util.EventRecordFilter;
import hudson.scm.ChangeLogParser;
import hudson.scm.SCMDescriptor;
import hudson.util.LogTaskListener;
import hudson.util.VariableResolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AbstractClearCaseScmTest extends AbstractWorkspaceTest {
    private Mockery classContext;
    private Mockery context; 

    private BuildListener taskListener;
    private Launcher launcher;
    private AbstractProject project;
    private Build build;
    private Computer computer;
    private EnvVars envVars;
    
    private EventRecordFilter filter;
    
    private CheckOutAction checkOutAction;
    //    private PollAction pollAction;
    private HistoryAction historyAction;
    
    private String[] branchArray = new String[] {"branch"};
    //    public ChangeLogAction changeLogAction;
    public SaveChangeLogAction saveChangeLogAction;
    private VariableResolver resolver;
    
    @Before
    public void setUp() throws Exception {
        createWorkspace();
        context = new Mockery();
        classContext = new Mockery() {
                {
                    setImposteriser(ClassImposteriser.INSTANCE);
                }
            };
        checkOutAction = context.mock(CheckOutAction.class);
        //        pollAction = context.mock(PollAction.class);
        historyAction = context.mock(HistoryAction.class);
        saveChangeLogAction = context.mock(SaveChangeLogAction.class);
        //        changeLogAction = context.mock(ChangeLogAction.class);
        launcher = classContext.mock(Launcher.class);
        taskListener = context.mock(BuildListener.class);
        project = classContext.mock(AbstractProject.class);
        build = classContext.mock(Build.class);
        computer = classContext.mock(Computer.class);
        envVars = classContext.mock(EnvVars.class);
        Map systemProperties = new HashMap();
        systemProperties.put("user.name", "henrik");
        resolver = context.mock(VariableResolver.class);
    }

    @After
    public void teardown() throws Exception {
        deleteWorkspace();
    }

    @Test
    public void testSupportsPolling() {
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "vob", "");
        assertTrue("The ClearCase SCM supports polling but is reported not to", scm.supportsPolling());
    }

    @Test
    public void assertWorkspaceisRequiredForPolling() {
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "vob", "");
        assertTrue("The ClearCase SCM needs a workspace to poll but is reported no to require one", scm.requiresWorkspaceForPolling());
    }

    @Test
    public void assertFilteringOutDestroySubBranchEventProperty() {
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "vob", "", true);
        assertTrue("The ClearCase SCM is not filtering out destroy sub branch events", scm.isFilteringOutDestroySubBranchEvent());
    }

    @Test
    public void testGetViewName() {
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "vob", "");
        assertEquals("The view name isnt correct", "viewname", scm.getViewName());
    }

    @Test
    public void testGetViewNameNonNull() {
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy(null, "vob", "");
        assertNotNull("The view name can not be null", scm.getViewName());
    }

    @Test
    public void testGetLoadRules() {
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "viewparams", false, false, false, "",
                                                                 false, "", "loadrules", null, false);
        assertEquals("The load rules arent correct", "loadrules", scm.getLoadRules());
    }

    @Test
    public void testGetLoadRulesWithSpaces() {
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "viewparams", false, false, false, "",
                                                                 false, "", "load rules", null, false);
        assertEquals("The load rules arent correct", "load rules", scm.getLoadRules());
    }

    @Test
    public void testGetViewPaths() {
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "viewparams", false, false, false, "",
                                                                 false, "", "loadrules", null, false);
        assertEquals("The view paths aren't correct", "loadrules", scm.getViewPaths()[0]);
    }

    @Test
    public void testGetViewPathsWithSpaces() {
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "viewparams", false, false, false, "",
                                                                 false, "", "test rules", null, false);
        assertEquals("The view paths aren't correct", "test rules", scm.getViewPaths()[0]);
    }

    @Test
    public void testGetViewPathsNoLoad() {
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "viewparams", false, false, false, "",
                                                                 false, "", "load loadrules", null, false);
        assertEquals("The view paths aren't correct", "loadrules", scm.getViewPaths()[0]);
    }

    @Test
    public void testGetViewPathsLeadingSlash() {
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "viewparams", false, false, false, "",
                                                                 false, "", "/loadrules", null, false);
        assertEquals("The view paths aren't correct", "loadrules", scm.getViewPaths()[0]);
    }

    @Test
    public void testGetViewPathsLeadingSlashAndLoad() {
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "viewparams", false, false, false, "",
                                                                 false, "", "load /loadrules", null, false);
        assertEquals("The view paths aren't correct", "loadrules", scm.getViewPaths()[0]);
    }

    @Test
    public void testGetExcludedRegions() {
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "vob", "", false, "excludedone\nexcludedtwo");
        assertArrayEquals("The excluded regions array is incorrect", new String[]{"excludedone", "excludedtwo"}, scm.getExcludedRegionsNormalized());
    }

    @Test
    public void assertViewNameMacrosAreWorking() throws IOException,InterruptedException{
        classContext.checking(new Expectations() {
                {
                    allowing(build).getParent(); will(returnValue(project));
                    allowing(project).getName(); will(returnValue("Hudson"));
                    
                    //              allowing(launcher).getComputer(); will(returnValue(computer));
                    //allowing(computer).currentComputer(); will(returnValue(computer));
                    allowing(computer).getSystemProperties(); will(returnValue(System.getProperties()));
                    allowing(computer).getName(); will(returnValue("test-node"));
                }
            });
        //        String username = (String)Computer.currentComputer().getSystemProperties().get("user.name");
        String username = System.getProperty("user.name");
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("${JOB_NAME}-${USER_NAME}-${NODE_NAME}-view", "vob", "", true);
        assertEquals("The macros were not replaced in the normalized view name", "Hudson-" + username + "-test-node-view", scm.generateNormalizedViewName(build));
        classContext.assertIsSatisfied();
    }

    
    @Test
    public void testViewNameMacrosUsingComputerEnv() throws IOException,InterruptedException{
        classContext.checking(new Expectations() {
                {
                    allowing(build).getParent(); will(returnValue(project));
                    allowing(project).getName(); will(returnValue("Hudson"));
                    allowing(build).getEnvironment(with(any(LogTaskListener.class))); will(returnValue(new EnvVars()));
                    //              allowing(launcher).getComputer(); will(returnValue(computer));
                    //allowing(computer).currentComputer(); will(returnValue(computer));
                    allowing(computer).getSystemProperties(); will(returnValue(System.getProperties()));
                    allowing(computer).getName(); will(returnValue("test-node"));
                    allowing(computer).getEnvironment(); will(returnValue(envVars));
                    allowing(envVars).containsKey("TEST_VARIABLE"); will(returnValue(true));
                    allowing(envVars).get("TEST_VARIABLE"); will(returnValue("result-of-test"));
                }
            });
        //        String username = (String)Computer.currentComputer().getSystemProperties().get("user.name");
        String username = System.getProperty("user.name");
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("${JOB_NAME}-${TEST_VARIABLE}-view", "vob", "", true);
        assertEquals("The macros were not replaced in the normalized view name", "Hudson-result-of-test-view", scm.generateNormalizedViewName(build));
        classContext.assertIsSatisfied();
    }

    @Test
    public void assertNormalizedViewNameDoesNotContainInvalidChars() {
        classContext.checking(new Expectations() {
                {
                    allowing(build).getParent(); will(returnValue(project));
                }
            });        
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("view  with\\-/-:-?-*-|-,", "vob", "", true);
        assertEquals("The invalid view name chars were not removed from the view name", 
                     "view_with_-_-_-_-_-_-,", scm.generateNormalizedViewName(build));
        classContext.assertIsSatisfied();
    }    

    @Test
    public void testGetMkviewOptionalParam() {
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "vob", "extra params");
        assertEquals("The MkviewOptionalParam isnt correct", "extra params", scm.getMkviewOptionalParam());
    }

    @Test
    public void testBuildEnvVars() {
        classContext.checking(new Expectations() {
                {
                    ignoring(build).getParent(); will(returnValue(project));
                }
            });
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "vob", "");
        Map<String, String> env = new HashMap<String, String>();
        env.put("WORKSPACE", "/hudson/jobs/job/workspace");
        scm.generateNormalizedViewName(build);
        scm.buildEnvVars(build, env);
        assertEquals("The env var VIEWNAME wasnt set", "viewname", env.get(AbstractClearCaseScm.CLEARCASE_VIEWNAME_ENVSTR));
        assertEquals("The env var VIEWPATH wasnt set", "/hudson/jobs/job/workspace" + File.separator +"viewname", env.get(AbstractClearCaseScm.CLEARCASE_VIEWPATH_ENVSTR));
    }
    
    @Test
    public void testBuildEnvVarsNoViewName() {
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy(null, "vob", "");
        Map<String, String> env = new HashMap<String, String>();
        env.put("WORKSPACE", "/hudson/jobs/job/workspace");
        scm.buildEnvVars(build, env);
        assertFalse("The env var VIEWNAME was set", env.containsKey(AbstractClearCaseScm.CLEARCASE_VIEWNAME_ENVSTR));
        assertFalse("The env var VIEWPATH was set", env.containsKey(AbstractClearCaseScm.CLEARCASE_VIEWPATH_ENVSTR));
    }

    @Test
    public void testBuildEnvVarsNoWorkspaceVar() {
        classContext.checking(new Expectations() {
                {
                    ignoring(build).getParent(); will(returnValue(project));
                }
            });
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "vob", "");
        Map<String, String> env = new HashMap<String, String>();
        scm.generateNormalizedViewName(build);
        scm.buildEnvVars(build, env);
        assertTrue("The env var VIEWNAME wasnt set", env.containsKey(AbstractClearCaseScm.CLEARCASE_VIEWNAME_ENVSTR));
        assertFalse("The env var VIEWPATH was set", env.containsKey(AbstractClearCaseScm.CLEARCASE_VIEWPATH_ENVSTR));
    }

    @Test
    public void assertBuildEnvVarsUsesNormalizedViewName() {
        classContext.checking(new Expectations() {
                {
                    atLeast(1).of(build).getParent(); will(returnValue(project));
                    one(project).getName(); will(returnValue("CCHudson"));
                    allowing(computer).getName(); will(returnValue("test-node"));
                }
            });
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname-${JOB_NAME}-${NODE_NAME}", "vob", "");
        Map<String, String> env = new HashMap<String, String>();
        env.put("WORKSPACE", "/hudson/jobs/job/workspace");
        scm.generateNormalizedViewName(build);
        scm.buildEnvVars(build, env);
        assertEquals("The env var VIEWNAME wasnt set", "viewname-CCHudson-test-node", env.get(AbstractClearCaseScm.CLEARCASE_VIEWNAME_ENVSTR));
        assertEquals("The env var VIEWPATH wasnt set", "/hudson/jobs/job/workspace" + File.separator +"viewname-CCHudson-test-node", env.get(AbstractClearCaseScm.CLEARCASE_VIEWPATH_ENVSTR));
    }
    
    @Test
    public void testFirstBuild() throws Exception {
        context.checking(new Expectations() {
                {
                    one(checkOutAction).checkout(launcher, workspace, "viewname"); will(returnValue(true));
                }
            });
        classContext.checking(new Expectations() {
                {
                    one(build).getPreviousBuild(); will(returnValue(null));
                    ignoring(build).getParent(); will(returnValue(project));
                }
            });

        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "vob", "");
        File changelogFile = new File(parentFile, "changelog.xml");
        boolean hasChanges = scm.checkout(build, launcher, workspace, taskListener, changelogFile);
        assertTrue("The first time should always return true", hasChanges);

        FilePath changeLogFilePath = new FilePath(changelogFile);
        assertTrue("The change log file is empty", changeLogFilePath.length() > 5);
        context.assertIsSatisfied();
        classContext.assertIsSatisfied();
    }
    
    @Test
    public void assertCheckoutWithChanges() throws Exception {
        workspace.child("viewname").mkdirs();
        final File changelogFile = new File(parentFile, "changelog.xml");
        final File extendedChangelogFile = new File(parentFile, "extended-changelog.xml");
        
        final ArrayList<ClearCaseChangeLogEntry> list = new ArrayList<ClearCaseChangeLogEntry>();
        list.add(new ClearCaseChangeLogEntry(new Date(12), "user", "comment"));
        list.add(new ClearCaseChangeLogEntry(new Date(12), "user", "comment"));

        final Calendar mockedCalendar = Calendar.getInstance();
        mockedCalendar.setTimeInMillis(100000);

        context.checking(new Expectations() {
                {
                    one(checkOutAction).checkout(launcher, workspace, "viewname"); 
                    will(returnValue(true));
                    
                    // normal changelog
                    one(historyAction).getChanges(with(equal(mockedCalendar.getTime())), with(equal("viewname")), with(equal(new String[] {"branch"})), with(equal(new String[]{"vob"})));
                    will(returnValue(list));
                    one(saveChangeLogAction).saveChangeLog(changelogFile, list);
                    
                }
            });
        classContext.checking(new Expectations() {
                {
                    // normal changelog
                    exactly(2).of(build).getPreviousBuild(); will(returnValue(build));
                    one(build).getTimestamp(); will(returnValue(mockedCalendar));
                    ignoring(build).getParent(); will(returnValue(project));
                    
                }
            });

        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "vob", "");
        boolean hasChanges = scm.checkout(build, launcher, workspace, taskListener, changelogFile);
        assertTrue("The first time should always return true", hasChanges);

        context.assertIsSatisfied();
        classContext.assertIsSatisfied();
    }

    @Test
    public void assertCheckoutWithChangesWithBuffer() throws Exception {
        workspace.child("viewname").mkdirs();
        final File changelogFile = new File(parentFile, "changelog.xml");
        final File extendedChangelogFile = new File(parentFile, "extended-changelog.xml");
        
        final ArrayList<ClearCaseChangeLogEntry> list = new ArrayList<ClearCaseChangeLogEntry>();
        list.add(new ClearCaseChangeLogEntry(new Date(12), "user", "comment"));
        list.add(new ClearCaseChangeLogEntry(new Date(12), "user", "comment"));

        final Calendar mockedCalendar = Calendar.getInstance();
        mockedCalendar.setTimeInMillis(1000000);
        final Date bufferedDate = new Date(mockedCalendar.getTimeInMillis() - (1000 * 60 * 5));

        context.checking(new Expectations() {
                {
                    one(checkOutAction).checkout(launcher, workspace, "viewname"); 
                    will(returnValue(true));
                    
                    // normal changelog
                    one(historyAction).getChanges(with(equal(bufferedDate)), with(equal("viewname")), with(equal(new String[] {"branch"})), with(equal(new String[]{"vob"})));
                    will(returnValue(list));
                    one(saveChangeLogAction).saveChangeLog(changelogFile, list);
                    
                }
            });
        classContext.checking(new Expectations() {
                {
                    // normal changelog
                    exactly(2).of(build).getPreviousBuild(); will(returnValue(build));
                    allowing(build).getTimestamp(); will(returnValue(mockedCalendar));
                    ignoring(build).getParent(); will(returnValue(project));
                    
                }
            });

        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "", false, false, false, "",
                                                                 false, "", "vob", "5", false
                                                                 );
        boolean hasChanges = scm.checkout(build, launcher, workspace, taskListener, changelogFile);
        assertTrue("The first time should always return true", hasChanges);

        context.assertIsSatisfied();
        classContext.assertIsSatisfied();
    }

    @Test
    public void assertCheckoutUsesNormalizedViewName() throws Exception {
        workspace.child("viewname-CCHudson").mkdirs();
        final File changelogFile = new File(parentFile, "changelog.xml");

        context.checking(new Expectations() {
                {
                    one(checkOutAction).checkout(launcher, workspace, "viewname-CCHudson-test-node"); 
                    will(returnValue(true));
                    
                    ignoring(historyAction).getChanges(with(any(Date.class)), with(equal("viewname-CCHudson-test-node")),
                                                       with(any(String[].class)), with(any(String[].class)));
                    will(returnValue(new ArrayList<ClearCaseChangeLogEntry>() ));
                    
                }
            });
        classContext.checking(new Expectations() {
                {
                    // normal changelog
                    ignoring(build).getPreviousBuild(); will(returnValue(build));
                    ignoring(build).getTimestamp(); will(returnValue(Calendar.getInstance()));
                    ignoring(build).getParent(); will(returnValue(project));
                    allowing(computer).getName(); will(returnValue("test-node"));
                    ignoring(project).getName(); will(returnValue("CCHudson"));
                    
                }
            });

        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname-${JOB_NAME}-${NODE_NAME}", "vob", "");
        scm.checkout(build, launcher, workspace, taskListener, changelogFile);
        
        context.assertIsSatisfied();
        classContext.assertIsSatisfied();
    }

    @Test
    public void assertCheckoutWithNoChanges() throws Exception {
        workspace.child("viewname").mkdirs();
        final File changelogFile = new File(parentFile, "changelog.xml");
        
        final Calendar mockedCalendar = Calendar.getInstance();
        mockedCalendar.setTimeInMillis(100000);

        context.checking(new Expectations() {
                {   
                    one(checkOutAction).checkout(launcher, workspace, "viewname"); 
                    will(returnValue(true));
                    one(historyAction).getChanges(with(equal(mockedCalendar.getTime())),
                                                  with(equal("viewname")), 
                                                  with(equal(new String[] {"branch"})), 
                                                  with(equal(new String[]{"vob"})));
                    will(returnValue(null));
                }
            });
        classContext.checking(new Expectations() {
                {
                    exactly(2).of(build).getPreviousBuild(); will(returnValue(build));
                    one(build).getTimestamp(); will(returnValue(mockedCalendar));
                    ignoring(build).getParent(); will(returnValue(project));
                }
            });

        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "vob", "");
        boolean hasChanges = scm.checkout(build, launcher, workspace, taskListener, changelogFile);
        assertTrue("The first time should always return true", hasChanges);

        FilePath changeLogFilePath = new FilePath(changelogFile);
        assertTrue("The change log file is empty", changeLogFilePath.length() > 5);
        context.assertIsSatisfied();
        classContext.assertIsSatisfied();
    }

    @Test
    public void assertCheckoutWithMultipleBranches() throws Exception {
        branchArray = new String[]{"branchone", "branchtwo"};
        workspace.child("viewname").mkdirs();

        final Calendar mockedCalendar = Calendar.getInstance();
        mockedCalendar.setTimeInMillis(100000);

        context.checking(new Expectations() {
                {   
                    one(checkOutAction).checkout(launcher, workspace, "viewname"); will(returnValue(true));
                    one(historyAction).getChanges(with(equal(mockedCalendar.getTime())),
                                                  with(equal("viewname")), 
                                                  with(equal(new String[] {"branchone", "branchtwo"})), 
                                                  with(equal(new String[]{"vob"})));
                    will(returnValue(null));
                }
            });
        classContext.checking(new Expectations() {
                {
                    ignoring(build).getPreviousBuild(); will(returnValue(build));
                    ignoring(build).getTimestamp(); will(returnValue(mockedCalendar));
                    ignoring(build).getParent(); will(returnValue(project));
                }
            });

        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "vob", "");
        File changelogFile = new File(parentFile, "changelog.xml");
        boolean hasChanges = scm.checkout(build, launcher, workspace, taskListener, changelogFile);
        assertTrue("The first time should always return true", hasChanges);

        FilePath changeLogFilePath = new FilePath(changelogFile);
        assertTrue("The change log file is empty", changeLogFilePath.length() > 5);
        context.assertIsSatisfied();
        classContext.assertIsSatisfied();
    }

    @Test
    public void testPollChanges() throws Exception {
        final Calendar mockedCalendar = Calendar.getInstance();
        mockedCalendar.setTimeInMillis(400000);

        context.checking(new Expectations() {
                {
                    
                    one(historyAction).hasChanges(with(equal(mockedCalendar.getTime())),
                                                  with(equal("viewname")), 
                                                  with(equal(new String[] {"branch"})), 
                                                  with(equal(new String[]{"vob"}))); 
                    will(returnValue(true));
                }
            });
        classContext.checking(new Expectations() {
                {
                    ignoring(build).getParent(); will(returnValue(project));
                    allowing(build).getTimestamp(); will(returnValue(mockedCalendar));
                    allowing(computer).getName(); will(returnValue("test-node"));
                    one(project).getLastBuild(); will(returnValue(build));
                }
            });
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "vob", "");
        boolean hasChanges = scm.pollChanges(project, launcher, workspace, taskListener);
        assertTrue("The first time should always return true", hasChanges);

        classContext.assertIsSatisfied();
        context.assertIsSatisfied();
    }


    @Test
    public void testPollChangesWithBuffer() throws Exception {
        final Calendar mockedCalendar = Calendar.getInstance();
        mockedCalendar.setTimeInMillis(1000000);
        final Date bufferedDate = new Date(mockedCalendar.getTimeInMillis() - (1000 * 60 * 5));
        context.checking(new Expectations() {
                {
                    
                    one(historyAction).hasChanges(with(equal(bufferedDate)),
                                                  with(equal("viewname")), 
                                                  with(equal(new String[] {"branch"})), 
                                                  with(equal(new String[]{"vob"}))); 
                    will(returnValue(true));
                }
            });
        classContext.checking(new Expectations() {
                {
                    ignoring(build).getParent(); will(returnValue(project));
                    allowing(build).getTimestamp(); will(returnValue(mockedCalendar));
                    one(project).getLastBuild(); will(returnValue(build));
                }
            });
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "", false, false, false, "",
                                                                 false, "", "vob", "5", false
                                                                 );
        boolean hasChanges = scm.pollChanges(project, launcher, workspace, taskListener);
        assertTrue("The first time should always return true", hasChanges);

        classContext.assertIsSatisfied();
        context.assertIsSatisfied();
    }


    @Test
    public void assertPollChangesUsesNormalizedViewName() throws Exception {
        context.checking(new Expectations() {
                {
                    
                    one(historyAction).hasChanges(with(any(Date.class)),
                                                  with(equal("view-CCHudson-test-node")), 
                                                  with(any(String[].class)), 
                                                  with(any(String[].class))); 
                    will(returnValue(true));
                }
            });
        classContext.checking(new Expectations() {
                {
                    ignoring(build).getParent(); will(returnValue(project));
                    ignoring(build).getTimestamp(); will(returnValue(Calendar.getInstance()));
                    ignoring(project).getLastBuild(); will(returnValue(build));
                    one(project).getName(); will(returnValue("CCHudson"));
                    allowing(computer).getName(); will(returnValue("test-node"));
                }
            });
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("view-${JOB_NAME}-${NODE_NAME}", "vob", "");
        scm.pollChanges(project, launcher, workspace, taskListener);

        classContext.assertIsSatisfied();
        context.assertIsSatisfied();
    }

    @Test
    public void testPollChangesFirstTime() throws Exception {
        classContext.checking(new Expectations() {
                {
                    one(project).getLastBuild(); will(returnValue(null));
                }
            });

        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "vob", "");
        boolean hasChanges = scm.pollChanges(project, launcher, workspace, taskListener);
        assertTrue("The first time should always return true", hasChanges);

        classContext.assertIsSatisfied();
        context.assertIsSatisfied();
    }

    @Test
    public void testPollChangesWithNoHistory() throws Exception {
        final Calendar mockedCalendar = Calendar.getInstance();
        mockedCalendar.setTimeInMillis(400000);

        context.checking(new Expectations() {
                {
                    
                    one(historyAction).hasChanges(with(equal(mockedCalendar.getTime())),
                                                  with(equal("viewname")), 
                                                  with(equal(new String[]{"branch"})), 
                                                  with(equal(new String[]{"vob"}))); 
                    will(returnValue(false));
                }
            });
        classContext.checking(new Expectations() {
                {
                    ignoring(build).getParent(); will(returnValue(project));
                    one(build).getTimestamp(); will(returnValue(mockedCalendar));
                    one(project).getLastBuild(); will(returnValue(build));
                }
            });

        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "vob", "");
        boolean hasChanges = scm.pollChanges(project, launcher, workspace, taskListener);
        assertFalse("pollChanges() should return false", hasChanges);

        classContext.assertIsSatisfied();
        context.assertIsSatisfied();
    }

    @Test
    public void testPollChangesWithMultipleBranches() throws Exception {
        branchArray = new String[]{"branchone", "branchtwo"};
        final ArrayList<Object[]> list = new ArrayList<Object[]>();
        list.add(new String[] { "A" });
        final Calendar mockedCalendar = Calendar.getInstance();
        mockedCalendar.setTimeInMillis(400000);

        context.checking(new Expectations() {
                {
                    one(historyAction).hasChanges(with(equal(mockedCalendar.getTime())),
                                                  with(equal("viewname")), 
                                                  with(equal(new String[]{"branchone", "branchtwo"})), 
                                                  with(equal(new String[]{"vob"}))); 
                    will(returnValue(true));
                }
            });
        classContext.checking(new Expectations() {
                {
                    ignoring(build).getParent(); will(returnValue(project));
                    one(build).getTimestamp(); will(returnValue(mockedCalendar));
                    one(project).getLastBuild(); will(returnValue(build));
                }
            });
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "vob", "");
        boolean hasChanges = scm.pollChanges(project, launcher, workspace, taskListener);
        assertTrue("The first time should always return true", hasChanges);

        classContext.assertIsSatisfied();
        context.assertIsSatisfied();
    }

    @Test
    public void testPollChangesMultipleVobPaths() throws Exception {
        final Calendar mockedCalendar = Calendar.getInstance();
        context.checking(new Expectations() {
                {
                    ignoring(build).getParent(); will(returnValue(project));
                    one(historyAction).hasChanges(with(equal(mockedCalendar.getTime())),
                                                  with(equal("viewname")), 
                                                  with(equal(new String[]{"branch"})), 
                                                  with(equal(new String[]{"vob1", "vob2/vob2-1", "vob\\ 3"}))); 
                    will(returnValue(true));
                }
            });
        classContext.checking(new Expectations() {
                {
                    ignoring(build).getParent(); will(returnValue(project));
                    one(build).getTimestamp(); will(returnValue(mockedCalendar));
                    one(project).getLastBuild(); will(returnValue(build));
                }
            });

        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "vob1\nvob2/vob2-1\nvob\\ 3", "");
        scm.pollChanges(project, launcher, workspace, taskListener);

        classContext.assertIsSatisfied();
        context.assertIsSatisfied();
    }

    @Test
    public void testPollChangesNoBranch() throws Exception {
        branchArray = new String[] {""};  
        final Calendar mockedCalendar = Calendar.getInstance();
        context.checking(new Expectations() {
                {
                    one(historyAction).hasChanges(with(equal(mockedCalendar.getTime())),
                                                  with(equal("viewname")), 
                                                  with(equal(new String[]{""})), 
                                                  with(equal(new String[]{""}))); 
                    will(returnValue(false));
                }
            });
        classContext.checking(new Expectations() {
                {
                    ignoring(build).getParent(); will(returnValue(project));
                    one(build).getTimestamp(); will(returnValue(mockedCalendar));
                    one(project).getLastBuild(); will(returnValue(build));
                }
            });

        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "", "");
        scm.pollChanges(project, launcher, workspace, taskListener);

        classContext.assertIsSatisfied();
        context.assertIsSatisfied();
    }

    @Test
    public void testPollChangesWithMatrixProject() throws Exception {
        final Calendar mockedCalendar = Calendar.getInstance();
        mockedCalendar.setTimeInMillis(400000);
        context.checking(new Expectations() {
                {
                    one(historyAction).hasChanges(with(equal(mockedCalendar.getTime())),
                                                  with(equal("viewname")), 
                                                  with(equal(new String[]{"branch"})), 
                                                  with(equal(new String[]{""}))); 
                    will(returnValue(true));
                }
            });
        final MatrixBuild matrixBuild = classContext.mock(MatrixBuild.class);
        classContext.checking(new Expectations() {
                {
                    ignoring(matrixBuild).getParent(); will(returnValue(project));
                    one(project).getLastBuild(); will(returnValue(matrixBuild));
                    one(matrixBuild).getTimestamp(); will(returnValue(mockedCalendar));
                }
            });

        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("viewname", "", "");
        scm.pollChanges(project, launcher, workspace, taskListener);

        classContext.assertIsSatisfied();
        context.assertIsSatisfied();
    }
    
    @Test
    public void assertGetModuleRootReturnsViewFolder() throws Exception {
        // Must initiate a pollChanges() or checkout() to update the normalizedViewName
        createWorkspace();
        context.checking(new Expectations() {
                {
                    ignoring(historyAction).hasChanges(with(any(Date.class)), with(any(String.class)), with(any(String[].class)),with(any(String[].class)));
                    will(returnValue(true));
                }
            });
        classContext.checking(new Expectations() {
                {
                    ignoring(build).getParent(); will(returnValue(project));
                    ignoring(build).getTimestamp(); will(returnValue(Calendar.getInstance()));
                    ignoring(project).getLastBuild(); will(returnValue(build));
                    ignoring(project).getName(); will(returnValue("CCHudson"));
                }
            });
        
        AbstractClearCaseScm scm = new AbstractClearCaseScmDummy("view name", "", "");
        scm.pollChanges(project, launcher, workspace, taskListener);
        FilePath moduleRoot = scm.getModuleRoot(workspace);
        assertEquals("The module root path is incorrect", "view_name", moduleRoot.getName());
        
        FilePath[] moduleRoots = scm.getModuleRoots(workspace);
        assertEquals("The number of module roots are incorrect", 1, moduleRoots.length);
        assertEquals("The module root path is incorrect", "view_name", moduleRoots[0].getName());
    }

    private class AbstractClearCaseScmDummy extends AbstractClearCaseScm {

        public AbstractClearCaseScmDummy(String viewName, String vobPaths, String mkviewOptionalParam,
                                         boolean filterOutDestroySubBranchEvent) {
            this(viewName, mkviewOptionalParam, filterOutDestroySubBranchEvent, false, false, "", false, "",
                 vobPaths, null, false);
        }

        public AbstractClearCaseScmDummy(String viewName, String vobPaths, String mkviewOptionalParam,
                                         boolean filterOutDestroySubBranchEvent, String excludedRegions) {
            this(viewName, mkviewOptionalParam, filterOutDestroySubBranchEvent, false, false, excludedRegions,
                 false, "", vobPaths, null, false);
        }

        public AbstractClearCaseScmDummy(String viewName, String vobPaths, String mkviewOptionalParam) {
            this(viewName, mkviewOptionalParam, false, false, false, "", false, "", vobPaths, null, false);
        }
        
        public AbstractClearCaseScmDummy(String viewName,
                                         String mkviewOptionalParam,
                                         boolean filterOutDestroySubBranchEvent,
                                         boolean useUpdate, 
                                         boolean rmviewonrename,
                                         String excludedRegions,
                                         boolean useDynamicView,
                                         String viewDrive,
                                         String loadRules,
                                         String multiSitePollBuffer,
                                         boolean createDynView) {
            super(viewName, mkviewOptionalParam, filterOutDestroySubBranchEvent, useUpdate, rmviewonrename,
                  excludedRegions, useDynamicView, viewDrive, loadRules, multiSitePollBuffer, createDynView, "", "", createDynView, createDynView);
        }


        @Override
        public Computer getCurrentComputer() {
            return computer;
        }

        @Override
        public Computer getBuildComputer(AbstractBuild<?,?> build) {
            return computer;
        }

        @Override
        public SCMDescriptor<?> getDescriptor() {
            throw new IllegalStateException("GetDescriptor() can not be used in tests");
        }

        @Override
        protected ClearToolLauncher createClearToolLauncher(TaskListener listener, FilePath workspace, Launcher launcher) {
            return null;
        }
        
        //        @Override
        //        protected PollAction createPollAction(VariableResolver resolver, ClearToolLauncher launcher,List<Filter> filters) {
        //            return pollAction;
        //        }

        @Override
        public String[] getBranchNames() {
            return branchArray;
        }

        //        @Override
        //        protected ChangeLogAction createChangeLogAction(ClearToolLauncher launcher, AbstractBuild<?, ?> build,Launcher baseLauncher,List<Filter> filters) {
        //            return changeLogAction;
        //        }
        
        @Override
        protected SaveChangeLogAction createSaveChangeLogAction(ClearToolLauncher launcher) {
            return saveChangeLogAction;
        }


        @Override
        public ChangeLogParser createChangeLogParser() {
            return null;
        }

        @Override
		protected HistoryAction createHistoryAction(
				VariableResolver variableResolver, ClearToolLauncher launcher,
				AbstractBuild build) {
            return historyAction;
        }

		@Override
		protected CheckOutAction createCheckOutAction(
				VariableResolver variableResolver, ClearToolLauncher launcher,
				AbstractBuild build) {
			return checkOutAction;
		}
    }    
}
