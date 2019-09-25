package uk.co.flax.luwak.demo;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.flax.luwak.*;
import uk.co.flax.luwak.matchers.HighlightingMatcher;
import uk.co.flax.luwak.matchers.HighlightsMatch;
import uk.co.flax.luwak.presearcher.TermFilteredPresearcher;
import uk.co.flax.luwak.queryparsers.LuceneQueryParser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JpLuwakDemo {
    public static final Analyzer ANALYZER = new JapaneseAnalyzer();

    public static final String FIELD = "text";

    public static final Logger logger = LoggerFactory.getLogger(LuwakDemo.class);

    public static void main(String... args) throws Exception {
        new JpLuwakDemo("src/test/resources/demoqueries", "src/test/resources/gutenberg");
    }

    public JpLuwakDemo(String queriesFile, String inputDirectory) throws Exception {

        try (Monitor monitor = new Monitor(new LuceneQueryParser(FIELD, ANALYZER), new TermFilteredPresearcher())) {
            addQueries(monitor, queriesFile);
            DocumentBatch batch = DocumentBatch.of(buildDocs(inputDirectory));
            Matches<HighlightsMatch> matches = monitor.match(batch, HighlightingMatcher.FACTORY);
            outputMatches(matches);
        }
    }

    static void addQueries(Monitor monitor, String queriesFile) throws Exception {
        List<MonitorQuery> queries = new ArrayList<>();
        int count = 0;
        logger.info("Loading queries from {}", queriesFile);
        try (FileInputStream fis = new FileInputStream(queriesFile);
             BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charsets.UTF_8))) {
            String queryString;
            while ((queryString = br.readLine()) != null) {
                if (Strings.isNullOrEmpty(queryString))
                    continue;
                logger.info("Parsing [{}]", queryString);
                queries.add(new MonitorQuery(String.format(Locale.ROOT, "%d-%s", count++, queryString), queryString));
            }
        }
        monitor.update(queries);
        logger.info("Added {} queries to monitor", count);
    }

    static List<InputDocument> buildDocs(String inputDirectory) throws Exception {
        List<InputDocument> docs = new ArrayList<>();
        logger.info("Reading documents from {}", inputDirectory);
        for (Path filePath : Files.newDirectoryStream(FileSystems.getDefault().getPath(inputDirectory))) {
            String content;
            try (FileInputStream fis = new FileInputStream(filePath.toFile());
                 InputStreamReader reader = new InputStreamReader(fis, Charsets.UTF_8)) {
                content = CharStreams.toString(reader);
                InputDocument doc = InputDocument.builder(filePath.toString())
                        .addField(FIELD, content, new StandardAnalyzer())
                        .build();
                docs.add(doc);
            }
        }
        return docs;
    }

    static void outputMatches(Matches<HighlightsMatch> matches) {

        logger.info("Matched batch of {} documents in {} milliseconds with {} queries run",
                matches.getBatchSize(), matches.getSearchTime(), matches.getQueriesRun());
        for (DocumentMatches<HighlightsMatch> docMatches : matches) {
            logger.info("Matches from {}", docMatches.getDocId());
            for (HighlightsMatch match : docMatches) {
                logger.info("\tQuery: {} ({} hits)", match.getQueryId(), match.getHitCount());
            }
        }

    }





}
