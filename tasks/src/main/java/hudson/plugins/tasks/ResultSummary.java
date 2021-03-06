package hudson.plugins.tasks;


/**
 * Represents the result summary of the Tasks parser. This summary will be
 * shown in the summary.jelly script of the FindBugs result action.
 *
 * @author Ulli Hafner
 */
public final class ResultSummary {
    /**
     * Returns the message to show as the result summary.
     *
     * @param project
     *            the project
     * @return the message
     */
    public static String createSummary(final TasksResult project) {
        StringBuilder summary = new StringBuilder();
        int tasks = project.getNumberOfAnnotations();

        summary.append(Messages.Tasks_ResultAction_Summary());
        summary.append(" ");
        if (tasks > 0) {
            summary.append("<a href=\"tasksResult\">");
        }
        if (tasks == 1) {
            summary.append(Messages.Tasks_ResultAction_OneWarning());
        }
        else {
            summary.append(Messages.Tasks_ResultAction_MultipleWarnings(tasks));
        }
        if (tasks > 0) {
            summary.append("</a>");
        }
        summary.append(" ");
        if (project.getNumberOfFiles() > 1) {
            summary.append(Messages.Tasks_ResultAction_MultipleFiles(project.getNumberOfFiles()));
        }
        else {
            summary.append(Messages.Tasks_ResultAction_OneFile());
        }
        if (project.getDelta() == 0) {
            summary.append(" (\u00B10).");
        }
        else if (project.getDelta() > 0) {
            summary.append(" (+" + project.getDelta() + ").");
        }
        else {
            summary.append(" (" + project.getDelta() + ").");
        }
        return summary.toString();
    }

    /**
     * Instantiates a new result summary.
     */
    private ResultSummary() {
        // prevents instantiation
    }
}

