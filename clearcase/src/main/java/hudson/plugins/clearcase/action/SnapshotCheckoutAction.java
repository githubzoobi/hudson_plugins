/**
 * The MIT License
 *
 * Copyright (c) 2007-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Erik Ramfelt,
 *                          Henrik Lynggaard, Peter Liljenberg, Andrew Bayer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.clearcase.action;

import java.io.IOException;
import java.util.Set;

import hudson.FilePath;
import hudson.Launcher;
import hudson.plugins.clearcase.ClearTool;
import hudson.plugins.clearcase.util.PathUtil;

/**
 * Check out action that will check out files into a snapshot view.
 */
public class SnapshotCheckoutAction extends AbstractCheckoutAction {

    private final String configSpec;
    private final boolean useUpdate;
    private final ClearTool cleartool;
    private final String[] loadRules;

    public SnapshotCheckoutAction(ClearTool clearTool, String configSpec, String[] loadRules, boolean useUpdate) {
        this.cleartool = clearTool;
        this.configSpec = configSpec;
        this.loadRules = loadRules;
        this.useUpdate = useUpdate;        
    }

    public boolean checkout(Launcher launcher, FilePath workspace, String viewName) throws IOException, InterruptedException {

        boolean updateView = useUpdate;        
        boolean localViewPathExists = new FilePath(workspace, viewName).exists();
        String jobConfigSpec = PathUtil.convertPathForOS(configSpec, launcher);
        boolean viewTagExists = cleartool.doesViewExist(viewName);
        
        if (localViewPathExists) {
            if (updateView) {
                String currentConfigSpec = getLoadRuleFreeConfigSpec(cleartool.catcs(viewName).trim());
                if (!jobConfigSpec.trim().replaceAll("\r\n", "\n").equals(currentConfigSpec)) {
                    updateView = false;
                }
                Set<String> configSpecLoadRules = extractLoadRules(cleartool.catcs(viewName).trim());
                if (loadRulesNeedUpdating(configSpecLoadRules)) {
                    updateView = false;
                }
            }
            else {
                cleartool.rmview(viewName);
                localViewPathExists = false;
                viewTagExists = false;
            }                
        }

        if (!localViewPathExists) {
            if (viewTagExists) {
                launcher.getListener().fatalError("View path for " + viewName + " does not exist, but the view tag does.\n"
                                                  + "View cannot be created - build aborting.");
                return false;
            }
            else {
                cleartool.mkview(viewName, null);
                updateView = false;
            }
        }
        
        if (updateView) {
            try {
                cleartool.setcs(viewName, null);
            } catch (IOException e) {
                launcher.getListener().fatalError(e.toString());
                return false;
            }
        }
        else {
            String newConfigSpec = jobConfigSpec + "\n";
            for (String loadRule : loadRules) {
                // Make sure the load rule starts with \ or /, as appropriate
                if (!(loadRule.startsWith("\\")) && !(loadRule.startsWith("/"))) {
                    loadRule = PathUtil.fileSepForOS(launcher.isUnix()) + loadRule;
                }
                
                newConfigSpec += "load " + loadRule.trim() + "\n";
            }
            newConfigSpec = PathUtil.convertPathForOS(newConfigSpec, launcher);
            try {
                cleartool.setcs(viewName, PathUtil.convertPathForOS(newConfigSpec, launcher));
            } catch (IOException e) {
                launcher.getListener().fatalError(e.toString());
                return false;
            }
        }

        return true;
    }

    private boolean loadRulesNeedUpdating(Set<String> configSpecLoadRules) {
        boolean recreate = false;
        for (String loadRule : loadRules) {
            if (!configSpecLoadRules.contains(loadRule)) {
                System.out
                    .println("Load rule: "
                             + loadRule
                             + " not found in current config spec, resetting config spec or recreating view");
                recreate = true;
            }
        }
        return recreate;
    }

}
