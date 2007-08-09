package hudson.plugins.violations.parse;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;


import hudson.plugins.violations.model.FullFileModel;
import hudson.plugins.violations.model.Violation;

/**
 * Parses a pmd xml report file.
 */
public class PMDParser extends AbstractTypeParser {

    /**
     * Parse the pmd xml file.
     * @throws IOException if there is a problem reading the file.
     * @throws XmlPullParserException if there is a problem parsing the file.
     */
    protected void execute()
        throws IOException, XmlPullParserException {

        // Ensure that the top level tag is "pmd"
        expectNextTag("pmd");
        getParser().next(); // consume the "pmd" tag
        // loop tru the child elements, getting the "file" ones
        while (skipToTag("file")) {
            parseFileElement();
        }
    }

    private void parseFileElement()
        throws IOException, XmlPullParserException {

        String absoluteFileName = checkNotBlank("name");
        getParser().next();  // consume "file" tag
        FullFileModel fileModel = getFileModel(absoluteFileName);

        // Loop tru the child elements, getting the "error" ones
        while (skipToTag("violation")) {
            fileModel.addViolation(parseViolationElement());
        }
        endElement();
    }

    private Violation parseViolationElement()
        throws IOException, XmlPullParserException {
        Violation ret = new Violation();
        ret.setType("pmd");
        String line = getParser().getAttributeValue("", "beginline");
        if (line == null) {
            // in pmd version 3.9 and lower the attribute is "line"
            line = getParser().getAttributeValue("", "line");
        }
        ret.setLine(line);
        ret.setSource(getParser().getAttributeValue("", "rule"));
        ret.setSeverity(getParser().getAttributeValue("", "priority"));
        ret.setMessage(getNextText("Expecting text"));
        getParser().next();
        endElement(); // violation element
        return ret;
    }
}
