package ca.canada.digital.search.assessment.config;

/**
 * @author Khalid AlHomoud <khalid@alhomoud.me>
 *
 */
public class AirtableServer {

	private String host;
	private int port;
	private String protocol;
	private String token;
	private String termsBaseId;
	private String archiveBaseId;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTermsBaseId() {
		return termsBaseId;
	}

	public void setTermsBaseId(String termsBaseId) {
		this.termsBaseId = termsBaseId;
	}

	public String getArchiveBaseId() {
		return archiveBaseId;
	}

	public void setArchiveBaseId(String archiveBaseId) {
		this.archiveBaseId = archiveBaseId;
	}

}
