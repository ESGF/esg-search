package esg.search.publish.impl;

import java.net.URI;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import esg.search.publish.api.MetadataRepositoryCrawler;
import esg.search.publish.api.MetadataRepositoryType;
import esg.search.publish.api.PublishingService;

public class PublishingServiceClient {
	
	// object holding command line options
	static Options options;
	
	// default options
	static String CONFIG = "classpath:esg/search/config/client-application-context.xml";
	static String FILTER = "*";
	static String RECURSIVE = "true";
	static String TYPE = "THREDDS";
	static URI SCHEMA = null;
	static String PUBLISH = "true";
	
	static {
	   
		options = new Options();
		
		// mandatory
		Option url = new Option("u", "url", true, "URL to harvest (mandatory)");
		url.setRequired(true);
		options.addOption(url);
		
		// optional
		options.addOption("l","logfile", true, "optional log file to capture list of catalogs harvested, and status");
		options.addOption("c","config", true, "optional configuration file for custom harvesting functionality");
		//options.addOption("dp","dataset_properties", true, "optional static properties for each dataset");
		//options.addOption("fp","file_properties", true, "optional static properties for each file");
		//options.addOption("ap","aggregation_properties", true, "optional static properties for each aggregation");
		options.addOption("t","type", true, "optional metadata repository type (default: THREDDS)");
		options.addOption("f","filter", true, "optional regular expression filter for parsing sub-catalogs (default: no filtering applied)");
		options.addOption("r","recursive", true, "optional recursive behaviour (default: true)");
		options.addOption("p","publish", true, "optional flag to publish (true) or un-publish (false) (default: true)");
		//options.addOption("s","schema", true, "optional additional schema for record validation (default: use core validation schemas only)");
		
		// Java properties
		//Option jp  = new Option("D", "java.ext.dirs=../lib/fetched", true, "optional location of jars directory (if not in classpath already)");
		//jp.setArgs(2);
		//options.addOption(jp);
		
	}
	
	public static void main(String[] args) {
		
		CommandLineParser parser = new BasicParser();
		ClassPathXmlApplicationContext context = null;
		
		try {
			
		    // parse the command line arguments
		    CommandLine cmdline = parser.parse( options, args );
		    
			if (args.length==0) {
				
				help();
				
			} else {
				
				// mandatory arguments
				String url = cmdline.getOptionValue("url");
				
				// optional arguments with default
				String config = cmdline.getOptionValue("config", CONFIG); // application configuration file
			    boolean publish = Boolean.parseBoolean( cmdline.getOptionValue("publish", PUBLISH) ); // "true" to publish, "false" to unpublish
			    String filter = cmdline.getOptionValue("filter", FILTER); // regular expression filter
			    URI schema = SCHEMA; // validation schema
			    if (cmdline.hasOption("schema")) schema = URI.create(cmdline.getOptionValue("schema"));
			    boolean recursive = Boolean.parseBoolean( cmdline.getOptionValue("recursive", RECURSIVE) ); // "true" to parse the repository recursively
			    MetadataRepositoryType type = MetadataRepositoryType.valueOf( cmdline.getOptionValue("type", TYPE) ); // metadata repository type
			    
				
				// retrieve objects from context
				context = new ClassPathXmlApplicationContext(config);
				// main publishing class
			    final PublishingService publishingService = (PublishingService)context.getBean("publishingService");
			    // publishing crawler that could be customized with a listener
			    final MetadataRepositoryCrawler metadataRepositoryCrawler = (MetadataRepositoryCrawler)context.getBean("metadataRepositoryCrawler");	
			    
			    // optional arguments with no default
			    if (cmdline.hasOption("logfile")) {
			    	final FileLogger logger = new FileLogger(cmdline.getOptionValue("logfile"));
			    	metadataRepositoryCrawler.setListener(logger);
			    }


			    if (publish) {
			    	publishingService.publish(url, filter, recursive, type, schema);    // recursive=true, schema=null
			    } else {
			    	publishingService.unpublish(url, filter, recursive, type);  // recursive=true
			    }
				
				
			}
		    
		} catch (Exception e ) {
			System.out.println( "Error: " + e.getMessage() +"\n" );
			help();
			
		} finally {
			if (context!=null) context.close();
		} 
				
		
	}
	
	/**
	 * Prints out help text.
	 */
	static void help() {
		
		HelpFormatter formatter = new HelpFormatter();
		int width = 200;
		String cmdLineSyntax = "java esg.search.publish.impl.PublishingServiceClient [-Djava.ext.dirs=../lib/fetched] -u <url> [other optional arguments]";
		//String header = "Required options: "+options.getRequiredOptions();
		String header = "";
		String footer = "Example (all in one line):\n"
				      + "java -Djava.ext.dirs=../lib/fetched esg.search.publish.impl.PublishingServiceClient -u http://esg-datanode.jpl.nasa.gov/thredds/esgcet/catalog.xml"
				      + " -c  classpath:esg/search/config/my-client-application-context.xml"
				      + " -l /tmp/publishing.log"
				      + " -t THREDDS"
				      + " -f '.*obs4MIPs.*'"
				      + " -r true"
				      + " -p true";
		formatter.printHelp( width, cmdLineSyntax, header, options, footer );

	}

}
