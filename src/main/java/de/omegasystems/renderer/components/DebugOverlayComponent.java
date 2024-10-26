package de.omegasystems.renderer.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.omegasystems.App;
import de.omegasystems.core.Renderer;
import de.omegasystems.core.RenderingComponent;

public class DebugOverlayComponent implements RenderingComponent {
    private Renderer renderer;

    private String projectVersion;
    private String projectName;

    @Override
    public void draw(Graphics2D g, Dimension size) {
        if (renderer != null) {
            Dimension drawingDimensions = renderer.getDrawingDimensions();
            Dimension screenSize = renderer.getScreenSize();

            drawStrings(g,
                    "Name: " + projectName + " (v" + projectVersion + ")",
                    "Drawing Dimensions: " + drawingDimensions.width + "x" + drawingDimensions.height,
                    "Screen Size: " + screenSize.width + "x" + screenSize.height);

        }
    }

    private void drawStrings(Graphics2D g, String... lines) {
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(Color.WHITE);

        for (int i = 0; i < lines.length; i++) {
            g.drawString(lines[i], 10, 20 * (i + 1));
        }
    }

    @Override
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;

        loadProperties();
    }

    private void loadProperties() {
        if (App.getInstance().isDevEnv()) {
            loadPropertiesFromDev();
            return;
        }
        Properties properties = new Properties();
        try (InputStream input = App.getInstance().loadResourceFile("build.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find version.properties");
            }
            properties.load(input);
            projectVersion = properties.getProperty("projectVersion");
            projectName = properties.getProperty("projectName");
        } catch (IOException ex) {
            throw new RuntimeException("Error reading version from properties file", ex);
        }
    }

    private void loadPropertiesFromDev() {
        try {
            // Specify the path to the pom.xml file (assumes it is in the root of the
            // project)
            File pomFile = new File("pom.xml");
            if (!pomFile.exists()) {
                throw new RuntimeException("pom.xml not found at " + pomFile.getAbsolutePath());
            }

            // Create a DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse the pom.xml file to get a Document object
            Document document = builder.parse(pomFile);
            document.getDocumentElement().normalize();

            // Get the data from the pom.xml
            projectName = getStringFromPom(document, "name");
            projectVersion = getStringFromPom(document, "version");


        } catch (Exception e) {
            throw new RuntimeException("Error reading pom.xml version", e);
        }
    }

    private String getStringFromPom(Document document, String key) {
        NodeList nodes = document.getElementsByTagName(key);
        if (nodes.getLength() > 0) {
            Node node = nodes.item(0);
            return node.getTextContent();
        }
        return null;
    }
}