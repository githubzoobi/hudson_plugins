package hudson.plugins.pxe;

import hudson.model.Describable;
import hudson.model.Hudson;
import hudson.DescriptorExtensionList;
import org.jvnet.hudson.tftpd.Data;

import java.io.IOException;

/**
 * Configuration of a bootable operating system.
 *
 * <p>
 * Say "Ubuntu 2008.10 + such and such preseed config".
 *
 * <p>
 * These objects are bound to URL as <tt>"/pxe/configuration/{@linkplain #getId() ID}/"</tt>
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class BootConfiguration implements Describable<BootConfiguration> {
    /**
     * For serving dynamic data from TFTP, it's often useful to have an unique ID per {@link BootConfiguration}.
     * This method provides that.
     */
    public String getId() {
        return String.valueOf(hashCode());
    }

    public BootConfigurationDescriptor getDescriptor() {
        return (BootConfigurationDescriptor) Hudson.getInstance().getDescriptor(getClass());
    }

    /**
     * Serves data from TFTP.
     *
     * <p>
     * This mechanism is useful when you need to generate the data to be served on the fly.
     * Static resources can be more easily served by simply placing them as resources
     * under /tftp.
     *
     * <p>
     * The TFTP file namespace is a shared resources among all {@link BootConfiguration}s,
     * so plugins are expected to use some prefix to avoid collisions.
     *
     * @param fileName
     *      Full path that represents a resource requested by a TFTP client. Such as "foo/bar/zot".
     * @return
     *      null if no such file exists, as far as this plugin is concerned.
     *      The PXE plugin will continue to search other {@link BootConfiguration}s to
     *      see if anyone understands it.
     * @throws IOException
     *      If a problem occurs. The PXE plugin will abort the search and the download will fail.
     */
    public Data tftp(String fileName) throws IOException {
        return null;
    }

    /**
     * Returns the fragment to be merged into <tt>pxelinux.cfg/default</tt>.
     */
    public abstract String getPxeLinuxConfigFragment() throws IOException;

    /**
     * Returns all the registered {@link BootConfigurationDescriptor}s.
     */
    public static DescriptorExtensionList<BootConfiguration, BootConfigurationDescriptor> all() {
        return Hudson.getInstance().getDescriptorList(BootConfiguration.class);
    }
}
