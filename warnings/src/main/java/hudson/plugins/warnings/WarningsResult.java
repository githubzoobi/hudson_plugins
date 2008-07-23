package hudson.plugins.warnings; // NOPMD

import hudson.model.AbstractBuild;
import hudson.plugins.warnings.parser.Warning;
import hudson.plugins.warnings.util.AnnotationsBuildResult;
import hudson.plugins.warnings.util.model.JavaProject;

/**
 * Represents the results of the warning analysis. One instance of this class is persisted for
 * each build via an XML file.
 *
 * @author Ulli Hafner
 */
public class WarningsResult extends AnnotationsBuildResult {
    /** Unique identifier of this class. */
    static {
        XSTREAM.alias("warning", Warning.class);
    }

    /**
     * Creates a new instance of <code>WarningsResult</code>.
     *
     * @param build
     *            the current build as owner of this action
     * @param project
     *            the parsed warnings result
     */
    public WarningsResult(final AbstractBuild<?, ?> build, final JavaProject project) {
        super(build, project);
    }

    /**
     * Creates a new instance of <code>WarningsResult</code>.
     *
     * @param build
     *            the current build as owner of this action
     * @param project
     *            the parsed warnings result
     * @param previous
     *            the result of the previous build
     */
    public WarningsResult(final AbstractBuild<?, ?> build, final JavaProject project, final WarningsResult previous) {
        super(build, project, previous);
    }

    /**
     * Returns a summary message for the summary.jelly file.
     *
     * @return the summary message
     */
    public String getSummary() {
        return ResultSummary.createSummary(this);
    }

    /**
     * Returns the detail messages for the summary.jelly file.
     *
     * @return the summary message
     */
    public String getDetails() {
        String message = ResultSummary.createDeltaMessage(this);
        if (getNumberOfAnnotations() == 0 && getDelta() == 0) {
            return message + "<li>" + Messages.Warnings_ResultAction_NoWarningsSince(getZeroWarningsSinceBuild()) + "</li>";
        }
        return message;
    }

    /** {@inheritDoc} */
    @Override
    protected String getSerializationFileName() {
        return "compiler-warnings.xml";
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.Warnings_ProjectAction_Name();
    }

    /**
     * Returns the results of the previous build.
     *
     * @return the result of the previous build, or <code>null</code> if no
     *         such build exists
     */
    @Override
    public JavaProject getPreviousResult() {
        WarningsResultAction action = getOwner().getAction(WarningsResultAction.class);
        if (action.hasPreviousResultAction()) {
            return action.getPreviousResultAction().getResult().getProject();
        }
        else {
            return null;
        }
    }

    /**
     * Returns whether a previous build result exists.
     *
     * @return <code>true</code> if a previous build result exists.
     */
    @Override
    public boolean hasPreviousResult() {
        return getOwner().getAction(WarningsResultAction.class).hasPreviousResultAction();
    }
}