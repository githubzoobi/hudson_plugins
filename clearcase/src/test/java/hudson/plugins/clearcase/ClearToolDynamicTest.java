package hudson.plugins.clearcase;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItemInArray;
import hudson.FilePath;
import hudson.util.VariableResolver;

import java.io.InputStream;
import java.io.OutputStream;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClearToolDynamicTest extends AbstractWorkspaceTest {

	private Mockery context;

	private ClearTool clearToolExec;
	private ClearToolLauncher launcher;

	private VariableResolver resolver;

	@Before
	public void setUp() throws Exception {
		createWorkspace();
		context = new Mockery();

		launcher = context.mock(ClearToolLauncher.class);
		resolver = context.mock(VariableResolver.class);
		clearToolExec = new ClearToolDynamic(resolver, launcher, "/cc/drives");
	}

	@After
	public void tearDown() throws Exception {
		deleteWorkspace();
	}

	@Test
	public void testSetcs() throws Exception {
		context.checking(new Expectations() {
			{
				one(launcher).getWorkspace();
				will(returnValue(workspace));
				one(launcher).run(
						with(allOf(hasItemInArray("setcs"),
								hasItemInArray("-tag"),
								hasItemInArray("viewName"))),
						with(aNull(InputStream.class)),
						with(aNull(OutputStream.class)),
						with(aNull(FilePath.class)));
				will(returnValue(Boolean.TRUE));
			}
		});

		clearToolExec.setcs("viewName", "configspec");
		context.assertIsSatisfied();
	}

	@Test
	public void testStartview() throws Exception {
		context.checking(new Expectations() {
			{
				one(launcher).run(
						with(allOf(hasItemInArray("startview"),
								hasItemInArray("viewName"))),
						with(aNull(InputStream.class)),
						with(aNull(OutputStream.class)),
						with(aNull(FilePath.class)));
			}
		});

		clearToolExec.startView("viewName");
		context.assertIsSatisfied();
	}
}
