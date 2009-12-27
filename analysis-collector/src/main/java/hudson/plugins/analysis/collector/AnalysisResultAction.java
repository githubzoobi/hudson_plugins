package hudson.plugins.analysis.collector;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.AbstractResultAction;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.PluginDescriptor;
import hudson.plugins.warnings.Messages;
import hudson.plugins.warnings.WarningsHealthDescriptor;

import java.util.NoSuchElementException;

/**
 * Controls the live cycle of the analysis results. This action persists the
 * results of the warnings analysis of a build and displays the results on the
 * build page. The actual visualization of the results is defined in the
 * matching <code>summary.jelly</code> file.
 * <p>
 * Moreover, this class renders the warnings result trend.
 * </p>
 *
 * @author Ulli Hafner
 */
public class AnalysisResultAction extends AbstractResultAction<AnalysisResult> {
    /** Unique identifier of this class. */

    /**
     * Creates a new instance of <code>WarningsResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor to use
     * @param result
     *            the result of this build
     */
    public AnalysisResultAction(final AbstractBuild<?, ?> owner, final HealthDescriptor healthDescriptor, final AnalysisResult result) {
        super(owner, new AnalysisHealthDescriptor(healthDescriptor), result);
    }

    /**
     * Creates a new instance of <code>WarningsResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor to use
     */
    public AnalysisResultAction(final AbstractBuild<?, ?> owner, final HealthDescriptor healthDescriptor) {
        super(owner, new WarningsHealthDescriptor(healthDescriptor));
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.Warnings_ProjectAction_Name();
    }

    /** {@inheritDoc} */
    @Override
    protected PluginDescriptor getDescriptor() {
        return AnalysisPublisher.DESCRIPTOR;
    }

    /**
     * Gets the warnings result of the previous build.
     *
     * @return the warnings result of the previous build.
     * @throws NoSuchElementException
     *             if there is no previous build for this action
     */
    public AnalysisResultAction getPreviousResultAction() {
        AbstractResultAction<AnalysisResult> previousBuild = getPreviousBuild();
        if (previousBuild instanceof AnalysisResultAction) {
            return (AnalysisResultAction)previousBuild;
        }
        throw new NoSuchElementException("There is no previous build for action " + this);
    }

    /** {@inheritDoc} */
    @Override
    public String getMultipleItemsTooltip(final int numberOfItems) {
        return Messages.Warnings_ResultAction_MultipleWarnings(numberOfItems);
    }

    /** {@inheritDoc} */
    @Override
    public String getSingleItemTooltip() {
        return Messages.Warnings_ResultAction_OneWarning();
    }
}