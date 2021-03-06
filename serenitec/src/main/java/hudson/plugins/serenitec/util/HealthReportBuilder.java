/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.4 $
 * @since $Date: 2008/07/23 12:05:04 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec.util;

import hudson.Util;
import hudson.model.HealthReport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jvnet.localizer.Localizable;

/**
 * Creates a health report for integer values based on healthy and unhealthy thresholds.
 * 
 * @see HealthReport
 * @author Ulli Hafner
 */
public class HealthReportBuilder implements Serializable
{

    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5191317904662711835L;
    /** Report health as 100% when the number of warnings is less than this value. */
    private int healthy;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private int unHealthy;
    /** Determines whether to use the provided healthy thresholds. */
    private boolean isHealthEnabled;
    /** Name of the report. */
    private String reportName;
    /** Name of a item. */
    private String itemName;
    /** Determines whether to use the provided unstable threshold. */
    private boolean isThresholdEnabled;
    /** Bug threshold to be reached if a build should be considered as unstable. */
    private int severityMax;
    /** Message to be shown for a single item count. */
    private final String reportSingleCount;
    /** Message to be shown for a multiple item count. */
    private final String reportMultipleCount;

    /**
     * Creates a new dummy instance of <code>HealthReportBuilder</code>.
     */
    public HealthReportBuilder()
    {

        this(false, 0, false, 0, 0, "1 item", "%d items");
    }

    /**
     * Creates a new instance of <code>HealthReportBuilder</code>.
     * 
     * @param isFailureThresholdEnabled
     *            determines whether to use the provided unstable threshold
     * @param severityMax
     *            if this severityMax is exceeded the build will be considered as unstable.
     * @param isHealthyReportEnabled
     *            determines whether to use the provided healthy thresholds.
     * @param healthy
     *            report health as 100% when the number of warnings is less than this value
     * @param unHealthy
     *            report health as 0% when the number of warnings is greater than this value
     * @param reportSingleCount
     *            message to be shown if there is exactly one item found
     * @param reportMultipleCount
     *            message to be shown if there are zero or more than one items found
     */
    public HealthReportBuilder(final boolean isFailureThresholdEnabled, final int severityMax, final boolean isHealthyReportEnabled,
            final int healthy, final int unHealthy, final String reportSingleCount, final String reportMultipleCount)
    {

        this.reportSingleCount = reportSingleCount;
        this.reportMultipleCount = reportMultipleCount;
        this.severityMax = severityMax;
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        isThresholdEnabled = isFailureThresholdEnabled;
        isHealthEnabled = isHealthyReportEnabled;
    }

    /**
     * Computes the healthiness of a build based on the specified counter. Reports a health of 100% when the specified counter is less than
     * {@link #healthy}. Reports a health of 0% when the specified counter is greater than {@link #unHealthy}.
     * 
     * @param counter
     *            the number of items in a build
     * @return the healthiness of a build
     */
    public HealthReport computeHealth(final int counter)
    {

        System.out.println("Execution de computeHealth");
        if (isHealthEnabled)
        {
            System.out.println("ifHealthEnabled == true");
            System.out.println("Healthy =" + healthy);
            System.out.println("unHealthy =" + unHealthy);
            System.out.println("counter =" + counter);
            int percentage;
            if (counter < healthy)
            {
                percentage = 100;
            }
            else if (counter > unHealthy)
            {
                percentage = 0;
            }
            else
            {
                percentage = 100 - (counter - healthy) * 100 / (unHealthy - healthy);
            }
            Localizable description;
            if (isLocalizedRelease())
            {
                if (counter == 1)
                {
                    description = Messages._HealthReportBuilder_HealthString(
                            reportSingleCount);
                }
                else
                {
                    description = Messages._HealthReportBuilder_HealthString(
                            String.format(reportMultipleCount, counter));
                }
            }
            else
            {
                description = Messages._HealthReportBuilder_HealthDescription(
                        reportName, counter + " " + itemName);
            }
            return new HealthReport(percentage, description);
        }
        return null;
    }

    /**
     * Returns the healthy.
     * 
     * @return the healthy
     */
    public final int getHealthy()
    {

        return healthy;
    }

    /**
     * Returns the itemName.
     * 
     * @return the itemName
     */
    public final String getItemName()
    {

        return itemName;
    }

    /**
     * Returns the reportName.
     * 
     * @return the reportName
     */
    public final String getReportName()
    {

        return reportName;
    }

    /**
     * Returns the threshold.
     * 
     * @return the threshold
     */
    public int getThreshold()
    {

        return severityMax;
    }

    /**
     * Returns the unHealthy.
     * 
     * @return the unHealthy
     */
    public final int getUnHealthy()
    {

        return unHealthy;
    }

    /**
     * Returns whether this health report build is enabled, i.e. at least one of the health or failed thresholds are provided.
     * 
     * @return <code>true</code> if health or failed thresholds are provided
     */
    public boolean isEnabled()
    {

        return isHealthEnabled || isThresholdEnabled;
    }

    /**
     * Returns the isThresholdEnabled.
     * 
     * @return the isThresholdEnabled
     */
    public boolean isFailureThresholdEnabled()
    {

        return isThresholdEnabled;
    }

    /**
     * Returns the isHealthyReportEnabled.
     * 
     * @return the isHealthyReportEnabled
     */
    public final boolean isHealthyReportEnabled()
    {

        return isHealthEnabled;
    }

    List<Integer> createSeries(int numberOfEntry)
    {
        List<Integer> series = new ArrayList<Integer>(3);
        int remainder = numberOfEntry;

        if (isHealthEnabled) {
            series.add(Math.min(remainder, healthy));

            int range = unHealthy - healthy;
            remainder -= healthy;
            if (remainder > 0) {
                series.add(Math.min(remainder, range));
            }
            else {
                series.add(0);
            }

            remainder -= range;
            if (remainder > 0) {
                series.add(remainder);
            }
            else {
                series.add(0);
            }
        }
        else if (isThresholdEnabled) {
            series.add(Math.min(remainder, severityMax));

            remainder -= severityMax;
            if (remainder > 0) {
                series.add(remainder);
            }
            else {
                series.add(0);
            }
        }

        return series;
    }

    /**
     * Returns whether the result is recorded in a localized release (i.e., release 2.2 and newer).
     * 
     * @return <code>true</code> if the result is recorded in a localized release (i.e., release 2.2 and newer)
     */
    private boolean isLocalizedRelease()
    {

        return itemName == null;
    }

    /**
     * Sets the isThresholdEnabled to the specified value.
     * 
     * @param isFailureThresholdEnabled
     *            the value to set
     */
    public void setFailureThresholdEnabled(final boolean isFailureThresholdEnabled)
    {

        isThresholdEnabled = isFailureThresholdEnabled;
    }

    /**
     * Sets the healthy to the specified value.
     * 
     * @param healthy
     *            the value to set
     */
    public final void setHealthy(final int healthy)
    {

        this.healthy = healthy;
    }

    /**
     * Sets the isHealthyReportEnabled to the specified value.
     * 
     * @param isHealthyReportEnabled
     *            the value to set
     */
    public final void setHealthyReportEnabled(final boolean isHealthyReportEnabled)
    {

        isHealthEnabled = isHealthyReportEnabled;
    }

    /**
     * Sets the itemName to the specified value.
     * 
     * @param itemName
     *            the value to set
     */
    public final void setItemName(final String itemName)
    {

        this.itemName = itemName;
    }

    /**
     * Sets the reportName to the specified value.
     * 
     * @param reportName
     *            the value to set
     */
    public final void setReportName(final String reportName)
    {

        this.reportName = reportName;
    }

    /**
     * Sets the threshold to the specified value.
     * 
     * @param threshold
     *            the value to set
     */
    public void setThreshold(final int threshold)
    {

        this.severityMax = threshold;
    }

    /**
     * Sets the unHealthy to the specified value.
     * 
     * @param unHealthy
     *            the value to set
     */
    public final void setUnHealthy(final int unHealthy)
    {

        this.unHealthy = unHealthy;
    }

    /**
     * Creates a trend graph for the corresponding action using the thresholds of this health builder.
     * 
     * @param useHealthBuilder
     *            if the health thresholds should be used at all
     * @param url
     *            the URL shown in the tool tips
     * @param dataset
     *            the data set of the values to render
     * @param toolTipProvider
     *            tooltip provider for the clickable map
     * @return the created graph
     */
    public JFreeChart createGraph(final boolean useHealthBuilder, final String url, final CategoryDataset dataset,
            final ToolTipProvider toolTipProvider)
    {
        StackedAreaRenderer renderer;
        if (useHealthBuilder && isEnabled())
        {
            renderer = new ResultAreaRenderer(url, toolTipProvider);
        }
        else
        {
            renderer = new PrioritiesAreaRenderer(url, toolTipProvider);
        }

        return ChartBuilder.createChart(dataset, renderer, getThreshold(),
                isHealthyReportEnabled() || !isFailureThresholdEnabled() || !useHealthBuilder);
    }

    public JFreeChart createGraphLine(final boolean useHealthBuilder, final String url, final CategoryDataset dataset,
            final ToolTipProvider toolTipProvider)
    {

        return ChartBuilder.createChart(dataset);
    }
}
