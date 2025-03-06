package ca.canada.digital.search.assessment.process;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.canada.digital.search.assessment.object.Metadata;

public class MetadataProcess {
	private static Logger LOG = LoggerFactory.getLogger(MetadataProcess.class);
	private Metadata metadata;
	private URI uri;
	
	public MetadataProcess(URI uri) {
		this.uri = uri;
		metadata = new Metadata();
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
				
				Elements keywords = doc.select("meta[name=keywords]"); 
				if (!keywords.isEmpty()) {
					metadata.setKeywords(keywords.first().attr("content"));
				}
				
				Elements lastUpdate = doc.select("meta[name=dcterms.modified]"); 
				if (!lastUpdate.isEmpty()) {
					metadata.setLastUpdate(new SimpleDateFormat("yyyy-MM-dd").parse(lastUpdate.first().attr("content")));
				}
				
				metadata.setTitle(doc.title());
				
			} else {
				LOG.warn("Could be an empty page or a network issue. Invistigate: {}", uri.toString());
			}
			
		} catch (Exception e) {
			LOG.error("Could not extract metadata from {}", uri.toString());
		}
	}
	

}
