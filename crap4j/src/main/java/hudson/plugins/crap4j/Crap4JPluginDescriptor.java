package hudson.plugins.crap4j;

import hudson.Extension;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.plugins.crap4j.calculation.HealthBuilder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

@Extension
public class Crap4JPluginDescriptor extends BuildStepDescriptor<Publisher> {
	
	public static final String ACTION_ICON_PATH = "/plugin/crap4j/icons/crap-32x32.png";
	
	private HealthBuilder healthBuilder;
	
	public Crap4JPluginDescriptor() {
		super(Crap4JPublisher.class);
		this.healthBuilder = new HealthBuilder();
	}
	
	public HealthBuilder getHealthBuilder() {
		return this.healthBuilder;
	}
	
	@Override
	public String getDisplayName() {
		return "Report Crap";
	}
	
	@Override
	public Crap4JPublisher newInstance(StaplerRequest req, JSONObject object) throws FormException {
        return req.bindParameters(Crap4JPublisher.class, "crap_");
	}
	
	@Override
	public boolean configure(StaplerRequest req, JSONObject object) throws FormException {
        try {
        	double healthThreshold = Double.parseDouble(req.getParameter("crap_publisher.health.threshold"));
        	this.healthBuilder = new HealthBuilder(healthThreshold);
        } catch (NumberFormatException e) {
            throw new FormException("health threshold field must be a positive Double",
                    "crap_publisher.health.threshold");
        } catch (IllegalArgumentException e) {
            throw new FormException("health threshold field must be a positive Double",
                    "crap_publisher.health.threshold");
        }
		return super.configure(req, object);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return (FreeStyleProject.class.isAssignableFrom(jobType) || MatrixProject.class.isAssignableFrom(jobType));
	}
}
