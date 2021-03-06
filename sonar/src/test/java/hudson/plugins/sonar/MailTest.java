package hudson.plugins.sonar;

import hudson.maven.MavenModuleSet;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.Mailer;
import org.jvnet.hudson.test.Bug;
import org.jvnet.mock_javamail.Mailbox;

/**
 * SONARPLUGINS-286:
 * If your build is successful and the post-build fails,
 * then the job will show as fail but you will not get an email.
 * <p>
 * TODO check that internal build was succesfull
 * </p>
 *
 * @author Evgeny Mandrikov
 */
@Bug(286)
public class MailTest extends SonarTestCase {
  private Mailbox inbox;
  private Mailer mailer;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    configureDefaultMaven();
    configureDefaultSonar();
    // Configure Mailer and Mailbox
    Mailer.descriptor().setAdminAddress("admin@example.org");
    String recipient = "me@example.org";
    inbox = Mailbox.get(recipient);
    mailer = new Mailer();
    mailer.recipients = "me@example.org";
  }

  public void testMavenProject() throws Exception {
    MavenModuleSet project = setupMavenProject();
    project.getPublishersList().add(mailer);
    inbox.clear();
    AbstractBuild build = build(project, Result.FAILURE);

    assertSonarExecution(build, "-f pom.xml");
    assertEquals(1, inbox.size());
  }

  public void testFreeStyleProject() throws Exception {
    FreeStyleProject project = setupFreeStyleProject();
    project.getPublishersList().add(mailer);
    inbox.clear();
    AbstractBuild build = build(project, Result.FAILURE);

    assertSonarExecution(build, "-f sonar-pom.xml");
    assertEquals(1, inbox.size());
  }
}
