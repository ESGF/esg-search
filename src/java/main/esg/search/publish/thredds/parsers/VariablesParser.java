package esg.search.publish.thredds.parsers;

import org.springframework.util.StringUtils;

import thredds.catalog.InvDataset;
import thredds.catalog.ThreddsMetadata.Variable;
import thredds.catalog.ThreddsMetadata.Variables;
import esg.search.core.Record;
import esg.search.publish.thredds.ThreddsPars;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Class that parses a THREDDS <variables> element.
 * 
 * <variables vocabulary="CF-1.0">
 *      <variable name="ps" vocabulary_name="surface_air_pressure" units="Pa">Surface Air Pressure</variable>
 *      <variable name="cl" vocabulary_name="cloud_area_fraction_in_atmosphere_layer" units="%">Cloud Area Fraction in Atmosphere Layer</variable>
 *      <variable name="zg" vocabulary_name="geopotential_height" units="m">Geopotential Height</variable>
 * </variables>
 * 
 * @author Luca Cinquini
 *
 */
public class VariablesParser implements ThreddsElementParser {

    @Override
    public void parse(final InvDataset dataset, final Record record, final DatasetSummary ds) {
        
        for (final Variables variables : dataset.getVariables()) {
            final String vocabulary = variables.getVocabulary();
            for (final Variable variable : variables.getVariableList()) {
                record.addField(SolrXmlPars.FIELD_VARIABLE, variable.getName());
                if (vocabulary.equals(ThreddsPars.CF)) {
                    // convert all CF names to lower case, and join by "_"
                    record.addField(SolrXmlPars.FIELD_CF_STANDARD_NAME, 
                                    variable.getVocabularyName().toLowerCase().replaceAll("\\s+", "_"));
                    // do not include if containing upper case letters or spaces
                    //final Matcher matcher = NON_CF_PATTERN.matcher(variable.getVocabularyName());
                    //if (!matcher.matches()) record.addField(SolrXmlPars.FIELD_CF_STANDARD_NAME, variable.getVocabularyName());
                }
                if (StringUtils.hasText(variable.getDescription())) record.addField(SolrXmlPars.FIELD_VARIABLE_LONG_NAME, variable.getDescription());
            }
        }        

    }

}
