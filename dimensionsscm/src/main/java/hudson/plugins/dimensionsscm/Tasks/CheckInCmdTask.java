
/* ===========================================================================
 *  Copyright (c) 2007 Serena Software. All rights reserved.
 *
 *  Use of the Sample Code provided by Serena is governed by the following
 *  terms and conditions. By using the Sample Code, you agree to be bound by
 *  the terms contained herein. If you do not agree to the terms herein, do
 *  not install, copy, or use the Sample Code.
 *
 *  1.  GRANT OF LICENSE.  Subject to the terms and conditions herein, you
 *  shall have the nonexclusive, nontransferable right to use the Sample Code
 *  for the sole purpose of developing applications for use solely with the
 *  Serena software product(s) that you have licensed separately from Serena.
 *  Such applications shall be for your internal use only.  You further agree
 *  that you will not: (a) sell, market, or distribute any copies of the
 *  Sample Code or any derivatives or components thereof; (b) use the Sample
 *  Code or any derivatives thereof for any commercial purpose; or (c) assign
 *  or transfer rights to the Sample Code or any derivatives thereof.
 *
 *  2.  DISCLAIMER OF WARRANTIES.  TO THE MAXIMUM EXTENT PERMITTED BY
 *  APPLICABLE LAW, SERENA PROVIDES THE SAMPLE CODE AS IS AND WITH ALL
 *  FAULTS, AND HEREBY DISCLAIMS ALL WARRANTIES AND CONDITIONS, EITHER
 *  EXPRESSED, IMPLIED OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY
 *  IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, OF FITNESS FOR A
 *  PARTICULAR PURPOSE, OF LACK OF VIRUSES, OF RESULTS, AND OF LACK OF
 *  NEGLIGENCE OR LACK OF WORKMANLIKE EFFORT, CONDITION OF TITLE, QUIET
 *  ENJOYMENT, OR NON-INFRINGEMENT.  THE ENTIRE RISK AS TO THE QUALITY OF
 *  OR ARISING OUT OF USE OR PERFORMANCE OF THE SAMPLE CODE, IF ANY,
 *  REMAINS WITH YOU.
 *
 *  3.  EXCLUSION OF DAMAGES.  TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE
 *  LAW, YOU AGREE THAT IN CONSIDERATION FOR RECEIVING THE SAMPLE CODE AT NO
 *  CHARGE TO YOU, SERENA SHALL NOT BE LIABLE FOR ANY DAMAGES WHATSOEVER,
 *  INCLUDING BUT NOT LIMITED TO DIRECT, SPECIAL, INCIDENTAL, INDIRECT, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, DAMAGES FOR LOSS OF
 *  PROFITS OR CONFIDENTIAL OR OTHER INFORMATION, FOR BUSINESS INTERRUPTION,
 *  FOR PERSONAL INJURY, FOR LOSS OF PRIVACY, FOR NEGLIGENCE, AND FOR ANY
 *  OTHER LOSS WHATSOEVER) ARISING OUT OF OR IN ANY WAY RELATED TO THE USE
 *  OF OR INABILITY TO USE THE SAMPLE CODE, EVEN IN THE EVENT OF THE FAULT,
 *  TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY, OR BREACH OF CONTRACT,
 *  EVEN IF SERENA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.  THE
 *  FOREGOING LIMITATIONS, EXCLUSIONS AND DISCLAIMERS SHALL APPLY TO THE
 *  MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW.  NOTWITHSTANDING THE ABOVE,
 *  IN NO EVENT SHALL SERENA'S LIABILITY UNDER THIS AGREEMENT OR WITH RESPECT
 *  TO YOUR USE OF THE SAMPLE CODE AND DERIVATIVES THEREOF EXCEED US$10.00.
 *
 *  4.  INDEMNIFICATION. You hereby agree to defend, indemnify and hold
 *  harmless Serena from and against any and all liability, loss or claim
 *  arising from this agreement or from (i) your license of, use of or
 *  reliance upon the Sample Code or any related documentation or materials,
 *  or (ii) your development, use or reliance upon any application or
 *  derivative work created from the Sample Code.
 *
 *  5.  TERMINATION OF THE LICENSE.  This agreement and the underlying
 *  license granted hereby shall terminate if and when your license to the
 *  applicable Serena software product terminates or if you breach any terms
 *  and conditions of this agreement.
 *
 *  6.  CONFIDENTIALITY.  The Sample Code and all information relating to the
 *  Sample Code (collectively "Confidential Information") are the
 *  confidential information of Serena.  You agree to maintain the
 *  Confidential Information in strict confidence for Serena.  You agree not
 *  to disclose or duplicate, nor allow to be disclosed or duplicated, any
 *  Confidential Information, in whole or in part, except as permitted in
 *  this Agreement.  You shall take all reasonable steps necessary to ensure
 *  that the Confidential Information is not made available or disclosed by
 *  you or by your employees to any other person, firm, or corporation.  You
 *  agree that all authorized persons having access to the Confidential
 *  Information shall observe and perform under this nondisclosure covenant.
 *  You agree to immediately notify Serena of any unauthorized access to or
 *  possession of the Confidential Information.
 *
 *  7.  AFFILIATES.  Serena as used herein shall refer to Serena Software,
 *  Inc. and its affiliates.  An entity shall be considered to be an
 *  affiliate of Serena if it is an entity that controls, is controlled by,
 *  or is under common control with Serena.
 *
 *  8.  GENERAL.  Title and full ownership rights to the Sample Code,
 *  including any derivative works shall remain with Serena.  If a court of
 *  competent jurisdiction holds any provision of this agreement illegal or
 *  otherwise unenforceable, that provision shall be severed and the
 *  remainder of the agreement shall remain in full force and effect.
 * ===========================================================================
 */

/*
 * This experimental plugin extends Hudson support for Dimensions SCM repositories
 *
 * @author Tim Payne
 *
 */

// Package name
package hudson.plugins.dimensionsscm;

// Dimensions imports
import hudson.plugins.dimensionsscm.FileScanner;
import hudson.plugins.dimensionsscm.GenericCmdTask;
import hudson.plugins.dimensionsscm.Logger;

// Hudson imports
import hudson.Util;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.model.Node;
import hudson.model.Computer;
import hudson.model.Hudson.MasterComputer;
import hudson.remoting.Callable;
import hudson.remoting.DelegatingCallable;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.model.TaskListener;
import hudson.Launcher;
import hudson.model.StreamBuildListener;
import hudson.Launcher.ProcStarter;

// General imports
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.Exception;
import java.util.Calendar;

/**
 * Class implementation of the checkin process. DUMMY IMPLEMENTATION!!! PLACE HOLDER ONLY!!!
 */
public class CheckInCmdTask extends GenericCmdTask implements FileCallable<Boolean> {

    private boolean forceCheckIn = false;
    private boolean forceTip = false;
    private boolean isStream = false;

    int buildNo = 0;
    private String jobId = "";

    private String workarea = "";
    private String projectId = "";
    private String requests = null;
    private String owningPart = null;

    private String[] patterns;

    /**
     * Utility routine to create command file for dmcli
     *
     * @param File
     * @param File
     * @return File
     * @throws IOException
     */
    private File createCmdFile(final File area, final File tempFile)
            throws IOException {
        Calendar nowDateCal = Calendar.getInstance();
        File logFile = new File("a");
        FileWriter logFileWriter = null;
        PrintWriter fmtWriter = null;
        File tmpFile = null;

        try {
            tmpFile = logFile.createTempFile("dmCm"+nowDateCal.getTimeInMillis(),null,null);
            logFileWriter = new FileWriter(tmpFile);
            fmtWriter = new PrintWriter(logFileWriter,true);

            String ciCmd = "DELIVER /BRIEF /ADD /UPDATE /DELETE ";
            if (version == 10 || !isStream)
                ciCmd = "UPLOAD ";

            ciCmd += " /USER_FILELIST=\""+tempFile.getAbsolutePath()+"\"";
            ciCmd += " /WORKSET=\""+projectId+"\"";
            ciCmd += " /COMMENT=\"Build artifacts saved by Hudson for job '"+jobId+"' - build "+buildNo+"\"";
            ciCmd += " /USER_DIRECTORY=\""+area.getAbsolutePath()+"\"";
            if (requests != null && requests.length() > 0) {
                if (requests.indexOf(",")==0) {
                    ciCmd += "/CHANGE_DOC_IDS=(\"" + requests + "\") ";
                } else {
                    ciCmd += "/CHANGE_DOC_IDS=("+ requests +") ";
                }
            }
            if (owningPart != null && owningPart.length() > 0) {
                ciCmd += "/PART=\"" + owningPart + "\"";
            }
            if (!isStream) {
                if (forceCheckIn) {
                    ciCmd += "/FORCE_CHECKIN ";
                }
                if (forceTip) {
                    ciCmd += "/FORCE_TIP ";
                }
            }

            fmtWriter.println(ciCmd);
            fmtWriter.flush();
        } catch (Exception e) {
            throw new IOException("Unable to write command log - " + e.getMessage());
        } finally {
            fmtWriter.close();
        }

        return tmpFile;
    }

    /*
     * Default constructor
     */
    public CheckInCmdTask(String userName, String passwd,
                             String database, String server,
                             String projectId, String requestId,
                             boolean forceCheckIn, boolean forceTip,
                             String[] patterns, int version,
                             boolean isStream,
                             int buildNo, String jobId,
                             String owningPart,
                             FilePath workspace,
                             TaskListener listener) {

        this.workspace = workspace;
        this.listener = listener;
        this.isStream = isStream;

        // Server details
        this.userName = userName;
        this.passwd = passwd;
        this.database = database;
        this.server = server;
        this.version = version;

        // Config details
        this.projectId = projectId;
        this.forceCheckIn = forceCheckIn;
        this.forceTip = forceTip;
        this.patterns = patterns;
        this.requests = requestId;
        this.buildNo = buildNo;
        this.jobId = jobId;
        this.owningPart = owningPart;
    }


    /*
     * Process the checkin
     *
     * @param File
     * @param File
     * @param File
     * @return boolean
     */
    public Boolean execute(final File exe, final File param, final File area)
                throws IOException {

        FilePath wa = new FilePath(area);
        boolean bRet = true;
        Launcher proc = new Launcher.LocalLauncher(listener);

        try
        {
            listener.getLogger().println("[DIMENSIONS] Scanning workspace for files to be saved into Dimensions...");
            listener.getLogger().flush();
            FilePath wd = new FilePath(area);
            File dir = new File (wd.getRemote());
            FileScanner fs = new FileScanner(dir,patterns,-1);
            File[] validFiles = fs.toArray();
            String cmdLog = null;

            if (fs.getFiles().size() > 0) {

                if (requests != null) {
                    requests = requests.replaceAll(" ","");
                    requests = requests.toUpperCase();
                }

                File tempFile = null;
                PrintWriter fmtWriter = null;

                try {
                    File logFile = new File("a");
                    FileWriter logFileWriter = null;
                    Calendar nowDateCal = Calendar.getInstance();
                    tempFile = logFile.createTempFile("dmCm"+nowDateCal.getTimeInMillis(),null,null);
                    logFileWriter = new FileWriter(tempFile);
                    fmtWriter = new PrintWriter(logFileWriter,true);

                    for (File f : validFiles) {
                        fmtWriter.println(f.getAbsolutePath());
                    }
                    fmtWriter.flush();
                } catch (Exception e) {
                    bRet = false;
                    throw new IOException("Unable to write command log - " + e.getMessage());
                } finally {
                    fmtWriter.close();
                }

                File cmdFile = createCmdFile(area,tempFile);
                if (cmdFile == null) {
                    listener.getLogger().println("[DIMENSIONS] Error: Cannot create command file for Dimensions login.");
                    param.delete();
                    tempFile.delete();
                    return false;
                }

                listener.getLogger().println("[DIMENSIONS] Loading files into Dimensions project \""+projectId+"\"...");

                // Debug for printing out files
                //String filesToLoad = new String(fu.loadFile(tempFile));
                //if (filesToLoad!=null)
                //    filesToLoad += "\n";
                //if (filesToLoad != null) {
                //    filesToLoad = filesToLoad.replaceAll("\n\n","\n");
                //    listener.getLogger().println(filesToLoad.replaceAll("\n","\n[DIMENSIONS] - "));
                //}

                listener.getLogger().flush();

                String[] cmd = new String[5];
                cmd[0] = exe.getAbsolutePath();
                cmd[1] = "-param";
                cmd[2] = param.getAbsolutePath();
                cmd[3] = "cmd";
                cmd[4] = cmdFile.getAbsolutePath();

                File tmpFile = null;

                // Need to capture output into a file so I can parse it
                try {
                    File logFile = new File("a");
                    Calendar nowDateCal = Calendar.getInstance();
                    tmpFile = logFile.createTempFile("dmCm"+nowDateCal.getTimeInMillis(),null,null);

                    FileOutputStream fos = new FileOutputStream(tmpFile);
                    StreamBuildListener os = new StreamBuildListener(fos);

                    try {
                        Launcher.ProcStarter ps = proc.launch();
                        ps.cmds(cmd);
                        ps.stdout(os.getLogger());
                        ps.stdin(null);
                        ps.pwd(wa);
                        int cmdResult = ps.join();
                        cmdFile.delete();
                        if (cmdResult != 0) {
                            listener.fatalError("Execution of checkout failed with exit code " + cmdResult);
                            bRet = false;
                        }
                    } finally {
                        os.getLogger().flush();
                        fos.close();
                        os = null;
                        fos = null;
                    }
                } finally {
                    tempFile.delete();
                }

                if (tmpFile != null) {
                    // Get the log file into a string for processing...
                    String outputStr = new String(fu.loadFile(tmpFile));
                    tmpFile.delete();
                    tmpFile = null;

                    if (cmdLog==null)
                        cmdLog = "\n";

                    cmdLog += outputStr;
                    cmdLog += "\n";
                }
            } else {
                listener.getLogger().println("[DIMENSIONS] No build artifacts found for checking in");
            }

            listener.getLogger().flush();

            param.delete();

            if (cmdLog != null && cmdLog.length() > 0 && listener.getLogger() != null) {
                listener.getLogger().println("[DIMENSIONS] (Note: Dimensions command output was - ");
                cmdLog = cmdLog.replaceAll("\n\n","\n");
                listener.getLogger().println(cmdLog.replaceAll("\n","\n[DIMENSIONS] ") + ")");
                listener.getLogger().flush();
            }

            if (!bRet) {
                listener.getLogger().println("[DIMENSIONS] ==========================================================");
                listener.getLogger().println("[DIMENSIONS] The Dimensions checkin command returned a failure status.");
                listener.getLogger().println("[DIMENSIONS] Please review the command output and correct any issues");
                listener.getLogger().println("[DIMENSIONS] that may have been detected.");
                listener.getLogger().println("[DIMENSIONS] ==========================================================");
                listener.getLogger().flush();
            }

            return bRet;
        }
        catch(Exception e)
        {
            String errMsg = e.getMessage();
            if (errMsg == null) {
                errMsg = "An unknown error occurred. Please try the operation again.";
            }
            listener.fatalError("Unable to run checkout callout - " + errMsg);
            // e.printStackTrace();
            //throw new IOException("Unable to run checkout callout - " + e.getMessage());
            bRet = false;
        }
        return bRet;
    }
}

