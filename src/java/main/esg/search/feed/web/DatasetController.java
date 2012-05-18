package esg.search.feed.web;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller("datasetController")
@RequestMapping("/dataset") 
public class DatasetController {
    
    @RequestMapping(value="/{id}.html", method=RequestMethod.GET)  
    public void facetFeed(@PathVariable("id") String datasetId, final HttpServletResponse response, final Model model) throws Exception {  
        
        System.out.println("Dataset ID="+datasetId);
        
        // set feed title
        model.addAttribute("datasetId", datasetId);
        
        response.sendRedirect("/search");

    }

}
