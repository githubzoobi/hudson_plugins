package com.ikokoon.serenity.hudson;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.LoggingConfigurator;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.serenity.process.Aggregator;
import com.ikokoon.serenity.process.Pruner;
import com.ikokoon.toolkit.Toolkit;

/**
 * This class runs at the end of the build, called by Hudson. The purpose is to copy the database file from the output directory for the reports
 * directory where the build action can present the data to the front end. As well as this the source that was found for the project is copied to the
 * source directory where the front end can access it.
 *
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
@SuppressWarnings("unchecked")
public class SerenityPublisher extends Recorder {

	static {
		LoggingConfigurator.configure();
	}
	protected static Logger logger = Logger.getLogger(SerenityPublisher.class);
	/** The description for Hudson. */
	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	@DataBoundConstructor
	public SerenityPublisher() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener buildListener) throws InterruptedException, IOException {
		logger.debug("perform");

		if (!Result.SUCCESS.equals(build.getResult())) {
			buildListener.getLogger().println("Build was not successful... but will still try to publish the report");
		}

		buildListener.getLogger().println("Publishing Serenity reports...");

		copySourceToBuildDirectory(build, buildListener);

		logger.info("Copy database : " + this);
		IDataBase targetDataBase = copyDataBasesToBuildDirectory(build, buildListener);
		logger.info("Aggregate data : " + this);
		aggregate(build, buildListener, targetDataBase);
		logger.info("Pruning data : " + this);
		prune(build, buildListener, targetDataBase);
		logger.info("Closing database : " + targetDataBase + "," + this);
		targetDataBase.close();

		buildListener.getLogger().println("Accessing Serenity results...");
		ISerenityResult result = new SerenityResult(build);
		SerenityBuildAction buildAction = new SerenityBuildAction(build, result);
		build.getActions().add(buildAction);

		return true;
	}

	private IDataBase copyDataBasesToBuildDirectory(AbstractBuild<?, ?> build, BuildListener buildListener) throws InterruptedException, IOException {
		// Find all the database files
		List<File> sourceDataBaseFiles = new ArrayList<File>();
		Toolkit.findFiles(new File(build.getWorkspace().toURI().getRawPath()), new Toolkit.IFileFilter() {
			public boolean matches(File file) {
				if (file != null && file.getAbsolutePath().contains("serenity.odb")) {
					return true;
				}
				return false;
			}
		}, sourceDataBaseFiles);
		File buildDirectory = build.getRootDir();
		File targetDataBaseFile = new File(buildDirectory, IConstants.DATABASE_FILE_ODB);

		String targetPath = targetDataBaseFile.getAbsolutePath();
		IDataBase odbDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, targetPath, null);
		IDataBase targetDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM + "." + Math.random(),
				odbDataBase);

		AbstractProject<?, ?> abstractProject = build.getProject();
		String projectName = abstractProject.getName();
		logger.info("Target database : " + projectName + ", " + targetPath);

		for (File sourceDataBaseFile : sourceDataBaseFiles) {
			String sourcePath = sourceDataBaseFile.getAbsolutePath();
			buildListener.getLogger().println("Publishing serenity db from... " + sourcePath + ", to... " + targetPath);
			IDataBase sourceDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, sourcePath, null);
			try {
				// Copy the data from the source into the target, then close the source
				buildListener.getLogger().println("Copying database... " + sourcePath);
				DataBaseToolkit.copyDataBase(sourceDataBase, targetDataBase);
				buildListener.getLogger().println("Copied database... " + sourcePath);
			} catch (Exception e) {
				e.printStackTrace(buildListener.fatalError("Unable to copy Serenity database file from : " + sourcePath + ", to : " + targetPath));
				build.setResult(Result.FAILURE);
			} finally {
				sourceDataBase.close();
			}
		}
		return targetDataBase;
	}

	private void aggregate(AbstractBuild<?, ?> build, BuildListener buildListener, IDataBase targetDataBase) {
		buildListener.getLogger().println("Aggregating data... ");
		new Aggregator(null, targetDataBase).execute();
	}

	private void prune(AbstractBuild<?, ?> build, BuildListener buildListener, IDataBase targetDataBase) {
		buildListener.getLogger().println("Pruning data...");
		new Pruner(null, targetDataBase).execute();
	}

	private boolean copySourceToBuildDirectory(AbstractBuild<?, ?> build, final BuildListener buildListener) throws InterruptedException, IOException {
		FilePath workSpace = build.getWorkspace();
		buildListener.getLogger().println("Module root... " + workSpace.toURI().getRawPath());

		List<File> sourceDirectories = new ArrayList<File>();
		Toolkit.findFiles(new File(workSpace.toURI().getRawPath()), new Toolkit.IFileFilter() {
			public boolean matches(File file) {
				if (file == null) {
					return false;
				}
				if (!file.isDirectory()) {
					return false;
				}
				String filePath = file.getAbsolutePath();
				filePath = Toolkit.replaceAll(filePath, "\\", "/");
				if (!filePath.contains("serenity/source")) {
					return false;
				}
				// Exclude the Subversion directories if there are any
				String[] excludedDirectories = { ".svn", "prop-base", "props", "text-base", "tmp" };
				for (String excludedDirectory : excludedDirectories) {
					if (filePath.indexOf(excludedDirectory) > -1) {
						return false;
					}
				}
				return true;
			}
		}, sourceDirectories);

		FilePath buildDirectory = new FilePath(build.getRootDir());
		FilePath buildSourceDirectory = new FilePath(buildDirectory, IConstants.SERENITY_SOURCE);
		File targetSourceDirectory = new File(buildSourceDirectory.toURI().getRawPath());

		try {
			for (File sourceDirectory : sourceDirectories) {
				buildListener.getLogger().println(
						"Publishing serenity source from... " + sourceDirectory.getAbsolutePath() + ", to... "
								+ buildSourceDirectory.toURI().getRawPath());
				Toolkit.copyFiles(sourceDirectory, targetSourceDirectory);
			}
		} catch (IOException e) {
			Util.displayIOException(e, buildListener);
			e.printStackTrace(buildListener.fatalError("Unable to copy Serenity database file from : " + sourceDirectories + ", to : "
					+ buildDirectory));
		}
		return true;
	}

	@Override
	public Action getProjectAction(AbstractProject abstractProject) {
		logger.debug("getProjectAction(AbstractProject)");
		return new SerenityProjectAction(abstractProject);
	}

	/**
	 * Descriptor for {@link SerenityPublisher}. Used as a singleton. The class is marked as public so that it can be accessed from views.
	 * <p/>
	 * <p/>
	 * See <tt>views/hudson/plugins/coverage/CoveragePublisher/*.jelly</tt> for the actual HTML fragment for the configuration screen.
	 */
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		/**
		 * Constructs a new DescriptorImpl.
		 */
		DescriptorImpl() {
			super(SerenityPublisher.class);
			logger.debug("DescriptorImpl");
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			logger.debug("getDisplayName");
			return "Publish Serenity Report";
		}

		@Override
		public boolean isApplicable(java.lang.Class<? extends AbstractProject> jobType) {
			logger.debug("isApplicable");
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
			logger.debug("configure");
			req.bindParameters(this, "serenity.");
			save();
			return super.configure(req, json);
		}

		/**
		 * Creates a new instance of {@link SerenityPublisher} from a submitted form.
		 */
		@Override
		public SerenityPublisher newInstance(StaplerRequest req, JSONObject json) throws FormException {
			logger.debug("newInstance");
			SerenityPublisher instance = req.bindParameters(SerenityPublisher.class, "serenity.");
			return instance;
		}
	}

	public BuildStepMonitor getRequiredMonitorService() {
		logger.debug("getRequiredMonitorService");
		return BuildStepMonitor.STEP;
	}
}