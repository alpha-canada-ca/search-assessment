FROM openjdk:11-jre-slim

ARG GECKODRIVER_VERSION=0.34.0

# install Firefox
RUN apt update && \
    apt install -y wget

#RUN install -d -m 0755 /etc/apt/keyrings && \
#    wget -q https://packages.mozilla.org/apt/repo-signing-key.gpg -O- | tee /etc/apt/keyrings/packages.mozilla.org.asc > /dev/null && \
#    echo "deb [signed-by=/etc/apt/keyrings/packages.mozilla.org.asc] https://packages.mozilla.org/apt mozilla main" | tee -a /etc/apt/sources.list.d/mozilla.list > /dev/null && \
#    echo "Package: *\nPin: origin packages.mozilla.org\nPin-Priority: 1000" | tee /etc/apt/preferences.d/mozilla

RUN apt update && \
    apt install -y firefox-esr

# Install Geckodriver
RUN wget https://github.com/mozilla/geckodriver/releases/download/v$GECKODRIVER_VERSION/geckodriver-v$GECKODRIVER_VERSION-linux64.tar.gz \
    && tar -xf geckodriver-v$GECKODRIVER_VERSION-linux64.tar.gz \
	&& rm geckodriver-v$GECKODRIVER_VERSION-linux64.tar.gz \
	&& mv geckodriver /usr/bin/

#RUN chmod +x /app/bin/geckodriver*


COPY ./target/search-assessment-0.0.1-SNAPSHOT-shaded.jar /usr/app/
COPY ./configuration.yml /usr/app/
WORKDIR /usr/app
EXPOSE 3001
ENTRYPOINT ["java", "-jar", "search-assessment-0.0.1-SNAPSHOT-shaded.jar", "server", "configuration.yml"]