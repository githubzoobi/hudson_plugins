/**
 * 
 */
package hudson.plugins.dependencyanalyzer;

import hudson.Launcher;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.dependencyanalyzer.persistence.BuildResultSerializer;
import hudson.plugins.dependencyanalyzer.result.BuildResult;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;

import java.io.IOException;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Vincent Sellier
 * 
 */
public class DependencyAnalyzerPublisher extends Recorder {
	public static final Logger LOGGER = Logger
			.getLogger(DependencyAnalyzerPublisher.class.getName());

	@DataBoundConstructor
	public DependencyAnalyzerPublisher() {
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.STEP;
	}

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {

		Result result = build.getResult();
		if (!(Result.SUCCESS.equals(result) || Result.UNSTABLE.equals(result))) {
			LOGGER.info("Build is not successful, no dependency analysis.");
			return false;
		}

		// Construct the build result
		BuildResult analysis = DependencyAnalyzerResultBuilder
				.buildResult((MavenModuleSetBuild) build);

		// persist this analysis for this build
		BuildResultSerializer.serialize(build.getRootDir(), analysis);

		build.getActions().add(
				new DependencyAnalyzerPublisherAction(build, analysis));

		return true;
	}

}
