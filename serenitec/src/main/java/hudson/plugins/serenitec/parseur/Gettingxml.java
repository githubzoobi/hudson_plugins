/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.3 $
 * @since $Date: 2008/07/16 15:11:09 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec.parseur;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Gettingxml
{
    static class Analyse extends DefaultHandler
    {
        private final ArrayList<ReportEntry> entries;
        private ArrayList<ReportPointeur> pointeurs;
        private ArrayList<ReportDescription> descriptions;
        private ReportEntry entry;
        private StringBuffer buffer;
        private ReportPointeur pointeur;
        private ReportDescription description;

        //FLAGS
        boolean f_report = false;
        boolean f_entry = false;
        boolean f_name = false;
        boolean f_severity = false;
        boolean f_descriptions = false;
        boolean f_description = false;
        boolean f_fulltext = false;
        boolean f_helpreference = false;
        boolean f_pointeurs = false;
        boolean f_pointeur = false;
        boolean f_filename = false;
        boolean f_fullpath = false;
        boolean f_linenumber = false;

        /**
         * Analyse
         */
        public Analyse()
        {
            super();
            entries = new ArrayList<ReportEntry>();
        }

        /**
         * @param ch,
         *            start, length
         */
        @Override
        public void characters(final char[] ch, final int start,
                final int length) throws SAXException
        {
            final String lecture = new String(ch, start, length);
            if (buffer != null)
            {
                buffer.append(lecture);
            }
        }

        /**
         *
         */
        @Override
        public void endDocument() throws SAXException
        {
            resultat = entries;
        }

        /**
         * @param uri,
         *            localName, qName
         */
        @Override
        public void endElement(final String uri, final String localName,
                final String qNam) throws SAXException
        {
            String qName = qNam;
            qName = qName.replaceAll("tns:", "");
            if (qName.equals("name") && f_entry && f_name)
            {
                entry.setName(buffer.toString());
                f_name = false;
            }
            else if (qName.equals("Severity") && f_entry && f_severity)
            {
                entry.setSeverity(Integer.parseInt(buffer.toString()));
                f_severity = false;
            }
            else if (qName.equals("Descriptions") && f_entry && f_descriptions)
            {
                f_descriptions = false;
                entry.setDescriptions(descriptions);
            }
            else if (qName.equals("Description") && f_entry && f_description)
            {
                f_description = false;
                descriptions.add(description);
            }
            else if (qName.equals("FullText") && f_entry && f_descriptions
                    && f_description && f_fulltext)
            {
                description.setDescription(buffer.toString());
                f_fulltext = false;
            }
            else if (qName.equals("HelpReference") && f_entry && f_descriptions
                    && f_description && f_helpreference)
            {
                description.setHelpreference(buffer.toString());
                f_helpreference = false;
            }
            else if (qName.equals("Pointers") && f_entry && f_pointeurs)
            {
                entry.setPointeurs(pointeurs);
                f_pointeurs = false;
            }
            else if (qName.equals("Pointer") && f_entry && f_pointeurs
                    && f_pointeur)
            {
                pointeurs.add(pointeur);
                f_pointeur = false;
            }
            else if (qName.equals("FileName") && f_entry && f_pointeurs
                    && f_pointeur && f_filename)
            {
                pointeur.setFilename(buffer.toString());
                f_filename = false;
            }
            else if (qName.equals("FullPath") && f_entry && f_pointeurs
                    && f_pointeur && f_fullpath)
            {
                pointeur.setFullpath(buffer.toString());
                f_fullpath = false;
            }
            else if (qName.equals("LineNumber") && f_entry && f_pointeurs
                    && f_pointeur && f_linenumber)
            {
                pointeur.setLinenumber(Integer.parseInt(buffer.toString()));
                f_linenumber = false;
            }
            else if (qName.equals("Entry") && f_entry)
            {
                entries.add(entry);
                f_entry = false;
            }
            else if (qName.equals("Report") && f_report)
            {
                f_report = false;
            }
        }


        /*
         * (non-Javadoc)
         *
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
         *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
        public void startElement(final String uri, final String localName,
                final String qNam, final Attributes attributes)
            throws SAXException
        {
            String qName = qNam.replaceAll("tns:", "");
            if (qName.equals("Entry"))
            {
                entry = new ReportEntry();
                buffer = new StringBuffer();
                f_entry = true;
            }
            else if (qName.equals("name") && f_entry)
            {
                f_name = true;
                buffer = new StringBuffer();
            }
            else if (qName.equals("Severity") && f_entry)
            {
                f_severity = true;
                buffer = new StringBuffer();
            }
            else if (qName.equals("Descriptions") && f_entry)
            {
                f_descriptions = true;
                descriptions = new ArrayList<ReportDescription>();
                buffer = new StringBuffer();
            }
            else if (qName.equals("Description") && f_entry && f_descriptions)
            {
                f_description = true;
                description = new ReportDescription();
                description.setLanguage(attributes.getValue("languageCode"));
                buffer = new StringBuffer();
            }
            else if (qName.equals("FullText") && f_entry && f_descriptions
                    && f_description)
            {
                f_fulltext = true;
                buffer = new StringBuffer();
            }
            else if (qName.equals("HelpReference") && f_entry && f_descriptions
                    && f_description)
            {
                f_helpreference = true;
                buffer = new StringBuffer();
            }
            else if (qName.equals("Pointers") && f_entry)
            {
                f_pointeurs = true;
                pointeurs = new ArrayList<ReportPointeur>();
                buffer = new StringBuffer();
            }
            else if (qName.equals("Pointer") && f_entry && f_pointeurs)
            {
                f_pointeur = true;
                pointeur = new ReportPointeur();
                pointeur.setIsfixed(Boolean.parseBoolean(
                        attributes.getValue("IsFixed")));
                buffer = new StringBuffer();
            }
            else if (qName.equals("FileName") && f_entry && f_pointeurs
                    && f_pointeur)
            {
                f_filename = true;
                buffer = new StringBuffer();
            }
            else if (qName.equals("FullPath") && f_entry && f_pointeurs
                    && f_pointeur)
            {
                f_fullpath = true;
                buffer = new StringBuffer();
            }
            else if (qName.equals("LineNumber") && f_entry && f_pointeurs
                    && f_pointeur)
            {
                f_linenumber = true;
                buffer = new StringBuffer();
            }
            else
            {
                buffer = new StringBuffer();
            }
        }
    }

    // Attribute
    private static ArrayList<ReportEntry> resultat;
    private static String            xml;

    /**
     * @param xml1
     */
    public Gettingxml(final String xml1)
    {
        resultat = new ArrayList<ReportEntry>();
        xml = xml1;
    }

    /**
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void parse() throws ParserConfigurationException, SAXException,
            IOException
    {
        final SAXParserFactory fabrique = SAXParserFactory.newInstance();
        final SAXParser parseur = fabrique.newSAXParser();
        final File file = new File(xml);
        final DefaultHandler gestionnaire = new Analyse();
        parseur.parse(file, gestionnaire);
    }

    /**
     * @return HashMap<String, Rule>
     */
    public ArrayList<ReportEntry> result()
    {
        return resultat;
    }
}
