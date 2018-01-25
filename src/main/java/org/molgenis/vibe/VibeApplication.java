package org.molgenis.vibe;

import org.apache.jena.query.ResultSet;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.vibe.io.ModelReader;
import org.molgenis.vibe.io.ResultSetPrinter;
import org.molgenis.vibe.io.TripleStoreDbReader;
import org.molgenis.vibe.options_digestion.CommandLineOptionsParser;
import org.molgenis.vibe.options_digestion.OptionsParser;
import org.molgenis.vibe.options_digestion.RunMode;
import org.molgenis.vibe.rdf_querying.DisgenetQueryRunner;

/**
 * The main application class.
 */
public class VibeApplication {
    /**
     * The main method for when used as a standalone application.
     * @param args {@link String}{@code []}
     */
    public static void main(String[] args) {
        VibeApplication app = new VibeApplication();

        try {
            CommandLineOptionsParser appOptions = new CommandLineOptionsParser(args);
            // If RunMode is NONE, shows help message and quits application.
            if(appOptions.getRunMode() == RunMode.NONE) {
                CommandLineOptionsParser.printHelpMessage();
            } else { // Any other RunMode will continue application.
                try {
                    app.run(appOptions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            CommandLineOptionsParser.printHelpMessage();
        }
    }

    /**
     * The actual processing parts of the application.
     * @param appOptions {@link OptionsParser}
     * @throws Exception if {@link VcfEntity#VcfEntity(Entity)} fails
     */
    public void run(OptionsParser appOptions) {
        appOptions.printVerbose("Loading DisGeNET dataset...");
        ModelReader modelReader = new TripleStoreDbReader(appOptions.getDisgenetDataDir());
        DisgenetQueryRunner queryRunner = new DisgenetQueryRunner(modelReader.getModel());

        if(appOptions.getRunMode() == RunMode.GET_GENES_WITH_SINGLE_HPO) {
            appOptions.printVerbose("Running query...");
            ResultSet results = queryRunner.getHpoGenes(appOptions.getHpoTerms()[0].getFormattedId());
            ResultSetPrinter.print(results, true);
        }

//        // Reads in rVCF for further usage.
//        VcfRepository vcf = new VcfRepository(appOptions.getRvcfData().toFile(), "rvcf");
//
//        // Example of iterating through VcfRepository:
//        Iterator<Entity> vcfIterator = vcf.iterator();
//        while(vcfIterator.hasNext())
//        {
//            VcfEntity record = new VcfEntity(vcfIterator.next());
//        }
    }
}
