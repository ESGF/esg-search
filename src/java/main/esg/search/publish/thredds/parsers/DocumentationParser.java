package esg.search.publish.thredds.parsers;

import org.springframework.util.StringUtils;

import thredds.catalog.InvDataset;
import thredds.catalog.InvDocumentation;
import esg.search.core.Record;
import esg.search.core.RecordHelper;
import esg.search.query.api.QueryParameters;

/**
 * Class that parses a THREDDS <documentation> element.
 * 
 * @author Luca Cinquini
 *
 */
public class DocumentationParser implements ThreddsElementParser {

    @Override
    public void parse(final InvDataset dataset, final Record record) {
        
        // <documentation type="...">.......</documentation>
        for (final InvDocumentation documentation : dataset.getDocumentation()) {
            // inline documentation
            final String content = documentation.getInlineContent();
            if (StringUtils.hasText(content)) {
                record.addField(QueryParameters.FIELD_DESCRIPTION, content);
            }
            // xlink documentation
            final String href = documentation.getXlinkHref();
            if (StringUtils.hasText(href)) {
                record.addField(QueryParameters.FIELD_XLINK, RecordHelper.encodeXlinkTuple(href, documentation.getXlinkTitle(), documentation.getType()) );
            }
        }

    }

}
