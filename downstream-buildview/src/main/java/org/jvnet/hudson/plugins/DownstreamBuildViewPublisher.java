/**
 * 
 */
package org.jvnet.hudson.plugins;

import hudson.Extension;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildTrigger;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;

import org.kohsuke.stapler.StaplerRequest;

import java.util.logging.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONObject;

/**
 * @author shinod.mohandas
 *
 */
public class DownstreamBuildViewPublisher extends Recorder {
	
	
	
	private static final Logger log = Logger.getLogger(DownstreamBuildViewPublisher.class.getName());

		
	@Override
	public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		build.addAction(new DownstreamBuildViewAction(build));
        return true;
    }
	
	
	public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
	
	public class DownstreamBuildViewAction extends AbstractDownstreamBuildViewAction {
		
		
		private List<DownstreamBuilds> downstreamBuildList ;	
		
		
		
		public DownstreamBuildViewAction(AbstractBuild<?,?> build){
			super(build);
			BuildTrigger buildTrigger = build.getProject().getPublishersList().get(BuildTrigger.class);
            if (buildTrigger != null){
            	List<AbstractProject> childs = buildTrigger.getChildProjects();
            	downstreamBuildList = findDownstream(childs,1,new ArrayList<Integer>());
            }
		}
		
		private List<DownstreamBuilds> findDownstream(List<AbstractProject> childs,int depth,List<Integer> parentChildSize){
			List<DownstreamBuilds>  childList = new ArrayList<DownstreamBuilds>();
			for (Iterator<AbstractProject> iterator = childs.iterator(); iterator.hasNext();) {
				AbstractProject project = iterator.next();
				DownstreamBuilds downstreamBuild = new DownstreamBuilds();
				downstreamBuild.setProjectName(project.getName());
				downstreamBuild.setProjectUrl(project.getUrl());
				downstreamBuild.setBuildNumber(Integer.toString(project.getNextBuildNumber()));
				downstreamBuild.setRootURL(Hudson.getInstance().getRootUrl());
				downstreamBuild.setDepth(depth);
				if(!(parentChildSize.size() > depth))
					parentChildSize.add(childs.size());
				downstreamBuild.setParentChildSize(parentChildSize);
				downstreamBuild.setChildNumber(childs.size());   
				List<AbstractProject> childProjects = project.getDownstreamProjects();
				if(!childProjects.isEmpty()){
					downstreamBuild.setChilds(findDownstream(childProjects,depth+1,parentChildSize));
				}
				
				childList.add(downstreamBuild);
				
			}
			return childList;
		}
		
				
		public class DownstreamBuilds {
			
			private String projectName,projectUrl,buildNumber,imageUrl,rootURL,statusMessage;
			private List<DownstreamBuilds>  childs;
			private int depth,childNumber;
			
			private List<Integer> parentChildSize;

			public List<Integer> getParentChildSize() {
				return parentChildSize;
			}

			public void setParentChildSize(List<Integer> parentChildSize) {
				this.parentChildSize = parentChildSize;
			}
			
			public String getProjectName() {
				return projectName;
			}
			public void setProjectName(String projectName) {
				this.projectName = projectName;
			}
			public String getProjectUrl() {
				return projectUrl;
			}
			public void setProjectUrl(String projectUrl) {
				this.projectUrl = projectUrl;
			}
			public String getBuildNumber() {
				return buildNumber;
			}
			public int getDepth() {
				return depth;
			}
			public void setBuildNumber(String buildNumber) {
				this.buildNumber = buildNumber;
			}
			public int getChildNumber() {
				return childNumber;
			}

			public void setChildNumber(int childNumber) {
				this.childNumber = childNumber;
			}
			public String getImageUrl() {
				AbstractProject proj = Hudson.getInstance().getItemByFullName(projectName,AbstractProject.class);
				Run r = proj.getBuildByNumber(Integer.parseInt(buildNumber));
				return rootURL+"images/16x16/"+getIconName(r);
			}
			public void setImageUrl(String imageUrl) {
				this.imageUrl = imageUrl;
			}
			public List<DownstreamBuilds> getChilds() {
				return childs;
			}

			public void setChilds(List<DownstreamBuilds> childs) {
				this.childs = childs;
			}
			
			public String getRootURL() {
				return rootURL;
			}

			public void setRootURL(String rootURL) {
				this.rootURL = rootURL;
			}
			
			public void setDepth(int depth) {
				this.depth = depth;
			}
			
			
			public String getIconName(Run r) {
		        return r.getIconColor().getImage();
		    }
			
			public String getStatusMessage() {
				AbstractProject proj = Hudson.getInstance().getItemByFullName(projectName,AbstractProject.class);
				Run r = proj.getBuildByNumber(Integer.parseInt(buildNumber));
				if(r.isBuilding())
					return r.getDurationString();
				else 
					return r.getTimestamp().getTime().toString();
			}

			public void setStatusMessage(String statusMessage) {
				this.statusMessage = statusMessage;
			}
			
			
		}
		public List getDownstreamBuildList() {
			return downstreamBuildList;
		}

		public void setDownstreamBuildList(List downstreamBuildList) {
			this.downstreamBuildList = downstreamBuildList;
		}
		
	}
	
	/**
	 * This method will return the descriptorobject.
	 * 
	 * @return DESCRIPTOR
	 */
	@Override
	public DescriptorImpl getDescriptor() {
		return DESCRIPTOR;
	}

	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
	
	/**
     * Descriptor for {@link DownstreamBuildViewPublisher}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>views/hudson/plugins/downstreambuildview/DownstreamBuildViewPublisher/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // this marker indicates Hudson that this is an implementation of an extension point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		
    	private boolean enable = true;
    	
    	public boolean isEnable() {
    		return enable;
    	}
		
    	public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return enable;    // for all types
        }
		

        public String getDisplayName() {
            return "Downstream Build view";
        }
        
        @Override
        public boolean configure(StaplerRequest staplerRequest, JSONObject json) throws FormException {
        	enable = json.getBoolean("enable");
        	save();
            return true; 
        }

        @Override
        public String getHelpFile() {
            return "/help/downstream-help-globalConfig.html";
        }
                
        @Override
        public DownstreamBuildViewPublisher newInstance(StaplerRequest req) throws FormException {
                
        	return new DownstreamBuildViewPublisher();
        }
		
	}
    
    

}