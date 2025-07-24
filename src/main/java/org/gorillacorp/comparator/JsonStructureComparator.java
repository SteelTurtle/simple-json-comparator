package org.gorillacorp.comparator;

import org.gorillacorp.comparator.utils.CliArgumentsParser;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Main class for comparing JSON structures.
 * This class serves as the entry point for the application and orchestrates the comparison process.
 */
public class JsonStructureComparator {
    private static final Logger log = getLogger(JsonStructureComparator.class);

    public static void main(String... args) {
        if (args.length < 2 || args.length > 4) {
            log.error(CliArgumentsParser.USAGE);
            System.exit(1);
        }

        var file1Path = args[0];
        var file2Path = args[1];

        // Check if CSV export is requested
        if (args.length >= 3) {
            CliArgumentsParser.parseExportToCsvOptionAndRun(args, file1Path, file2Path);
        } else {
            CliArgumentsParser.parseMandatoryOptionsAndRun(file1Path, file2Path);
        }
    }
}
