package ca.canada.digital.search.assessment.process;

import ca.canada.digital.search.assessment.model.Metadata;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MetadataProcess {
    private static final Logger LOG = LoggerFactory.getLogger(MetadataProcess.class);
    private final Metadata metadata;
    private final URI uri;

    public MetadataProcess(URI uri) {
        this.uri = uri;
        metadata = new Metadata();
        metadata.setUrl(uri.toString());
        metadata.setLastUpdate(LocalDateTime.now());
    }

    public Metadata execute() {
        setMetadata();

        return metadata;
    }

    private void setMetadata() {
        try {

            LOG.info("{}: {}", "Extracting metadata from: ", uri.toString());
            InputStream is = new URL(uri.toString()).openStream();
            Document doc = Jsoup.parse(is, "UTF-8", uri.toString());

            if (doc != null && !StringUtils.isEmpty(doc.html())) {

                Elements desc = doc.select("meta[name=description]");
                if (!desc.isEmpty()) {
                    metadata.setDescription(desc.get(0).attr("content"));
                }

                Element h1Element = doc.selectFirst("h1");
                String h1 = h1Element.text().trim();
                if (!StringUtils.isEmpty(h1)) {
                    metadata.setH1(h1);
                }

                Elements lastUpdate = doc.select("meta[name=dcterms.modified]");
                if (!lastUpdate.isEmpty()) {
                    metadata.setLastUpdate(LocalDate
                            .parse(lastUpdate.first().attr("content"), DateTimeFormatter.ISO_LOCAL_DATE)
                            .atStartOfDay());
                }
                metadata.setTitle(doc.title());

            } else {
                LOG.warn("Could be an empty page or a network issue. Invistigate: {}", uri);
            }

        } catch (Exception e) {
            LOG.error("Could not extract metadata from {}", uri.toString());
        }
    }


}
