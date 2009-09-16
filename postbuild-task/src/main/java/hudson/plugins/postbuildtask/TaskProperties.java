package hudson.plugins.postbuildtask;

import java.util.Collection;

import hudson.util.Iterators;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * A task properties.
 * 
 * @author Shinod Mohandas
 */
public final class TaskProperties {
	/**
	 * The text string which shoud be searched in the build log.
	 */
	public LogProperties[] logTexts;
	/**
	 * Shell script to be executed.
	 */
	public String script;

	@DataBoundConstructor
	public TaskProperties(String script) {
		this.script = script;
	}

	public void setLogTexts(LogProperties[] logTexts) {
		this.logTexts = logTexts;
	}

	public LogProperties[] getLogProperties() {
		return logTexts;
	}

	public String getScript() {
		return script;
	}

	/*
	 * public TaskProperties(LogProperties[] logTexts, String script) {
	 * this.logTexts = logTexts; this.script = script; }
	 */

	/*
	 * public TaskProperties(Collection<LogProperties> logTexts,String script) {
	 * this((LogProperties[])logTexts.toArray(new
	 * LogProperties[logTexts.size()]),script); }
	 */

	// TODO
	/*
	 * public String getLogText() { return null; }
	 */

}
