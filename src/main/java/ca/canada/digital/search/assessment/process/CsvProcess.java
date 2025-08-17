package ca.canada.digital.search.assessment.process;

import ca.canada.digital.search.assessment.model.Assessment;
import ca.canada.digital.search.assessment.model.TermAssessment;
import ca.canada.digital.search.assessment.object.CsvHeader;
import ca.canada.digital.search.assessment.util.DateUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CsvProcess {
    private static final Logger LOG = LoggerFactory.getLogger(CsvProcess.class);
    private OutputStream out;
    private Assessment assessment;

    public CsvProcess(OutputStream out, Assessment assessment) {
        this.out = out;
        this.assessment = assessment;
    }

    public void execute() {
        writeCsv();
    }

    private void writeCsv() {
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

            CSVFormat.Builder builder = CSVFormat.Builder.create();
            CSVFormat csvFormat = builder
                    .setHeader(Arrays.stream(CsvHeader.values()).map(CsvHeader::getHeader).toArray(String[]::new))
                    .build();
            CSVPrinter printer = csvFormat.print(writer);


            if (assessment != null && !assessment.getTermAssessments().isEmpty()) {
                for (TermAssessment.SearchType searchType : TermAssessment.SearchType.values()) {
                    int sequence = 0;
                    for (TermAssessment ta : assessment.getTermAssessments()) {
                        sequence++;
                        String searchTerms = ta.getTerm();
                        boolean isPass = ta.getPass();
                        String targetUrl = ta.getTargetUrl();
                        String title = StringUtils.isEmpty(ta.getMetadata().getTitle()) ? null : ta.getMetadata().getTitle();
                        String descriptions = StringUtils.isEmpty(ta.getMetadata().getDescription()) ? null : ta.getMetadata().getDescription();
                        ;
                        String h1 = StringUtils.isEmpty(ta.getMetadata().getH1()) ? null : ta.getMetadata().getH1();
                        ;
                        String type = searchType.toString();
                        String lastUpdate = DateUtil.dateToString(ta.getMetadata().getLastUpdate());
                        int position = ta.getPosition();

                        printer.printRecord(sequence, searchTerms, isPass ? "Pass" : "Fail", targetUrl, title,
                                descriptions, h1, lastUpdate, position, type);

                    }
                }
            }
            printer.flush();
            printer.close();

            writer.close();

            out.flush();

        } catch (IOException e) {
            LOG.error("Could not complete the REST request.", e);
        }

    }
}
