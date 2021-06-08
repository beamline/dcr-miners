package beamline.dcr.testsuite;

import beamline.dcr.model.DcrModel;
import org.apache.commons.lang3.tuple.Triple;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public class ModelComparison {

    private DcrModel originalDcrModel;
    private DcrModel comparativeDcrModel;

    public ModelComparison(DcrModel dcrModel) {
        this.originalDcrModel = dcrModel;
    }

    public double getPrecision(){
        double tp = getIntersectionSize();
        double fp = originalDcrModel.getRelations().size() - tp;

        return tp / (tp + fp);
    }

    private int getIntersectionSize(){
        Set<Triple<String,String, DcrModel.RELATION>> otherModel = comparativeDcrModel.getRelations();
        Set<Triple<String,String, DcrModel.RELATION>> minedRelations = originalDcrModel.getRelations();

        int intersection = 0;
        for (Triple<String, String, DcrModel.RELATION> relation : minedRelations){
            if (comparativeDcrModel.containsRelation(relation)) intersection++;
        }

        return intersection;
    }
    public double getRecall(){
        double tp = getIntersectionSize();
        double fn = comparativeDcrModel.getRelations().size() - tp;

        return tp / (tp + fn);
    }

    public double getJaccardSimilarity(){

        double intersection = getIntersectionSize();

        double union = originalDcrModel.getRelations().size() + comparativeDcrModel.getRelations().size() - intersection;

        return intersection/union;
    }

    public void loadComparativeModel(String xmlGraphPath) throws ParserConfigurationException, IOException, SAXException {
        this.comparativeDcrModel = new DcrModel();
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(new File(xmlGraphPath));

        //Set activity list
        NodeList eventList = doc.getElementsByTagName("events").item(0).getChildNodes();

        for (int i = 0; i < eventList.getLength(); i++) {
            Node event = eventList.item(i);
            if (event.getNodeName()=="event"){
                Element eventElement = (Element) event;
            }
        }

        //Set constraints in unionRelationSet
        NodeList constraints = doc.getElementsByTagName("constraints").item(0).getChildNodes();
        for (int j = 0; j < constraints.getLength(); j++) {
            Node childNode = constraints.item(j);
            switch (childNode.getNodeName()){
                case "conditions":
                case "responses":
                case "excludes":
                case "includes":
                    addToRelationSet(childNode.getChildNodes());
                    break;

            }
        }
    }
    private void addToRelationSet(NodeList constraintList){
        for(int i = 0; i < constraintList.getLength(); i++){
            Node constraint = constraintList.item(i);

            if(constraint.getNodeType() == Node.ELEMENT_NODE){

                Element constraintElement = (Element) constraint;

                String source = constraintElement.getAttribute("sourceId");
                String target = constraintElement.getAttribute("targetId");

                DcrModel.RELATION relation = DcrModel.RELATION.valueOf(constraint.getNodeName().toUpperCase());
                this.comparativeDcrModel.addRelation(Triple.of(source,target, relation));

            }
        }

    }
}
