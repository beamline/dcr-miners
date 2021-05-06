package beamline.dcr.testsuite;

import org.apache.commons.lang3.tuple.Triple;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class PerformanceStatistics {
    public String getPrecisionRecallString(String baselinePath, String minedRelationsPath){

        Set<Triple<String,String,String>> baseLineRelations = parseJsonFile(baselinePath);
        Set<Triple<String,String,String>> minedRelations = parseJsonFile(minedRelationsPath);

        Double tp = 0.0;
        for (Triple<String, String, String> relation : minedRelations){
            if (baseLineRelations.contains(relation)) tp++;
        }
        Double fn = baseLineRelations.size() - tp;
        Double fp = minedRelations.size() - tp;

        Double precision = tp/(tp+fp);
        Double recall = tp/(tp+fn);

        System.out.println(tp);
        String scoreString =String.format("tp: %s, fp: %s, fn: %s\nprecision: %s, recall: %s",
                tp,fp,fn,precision,recall);



        return scoreString;
    }

    public Set<Triple<String,String, String>> parseJsonFile(String jsonFilePath){
        Set<Triple<String,String,String>> relationSet = new LinkedHashSet<>();
        try {
            FileReader reader = new FileReader(jsonFilePath);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);

            JSONArray relations = (JSONArray) jsonObject.get("Relation");

            for (Object relation : relations){
                if (relation instanceof JSONObject ) {
                    JSONObject jsonRelation = (JSONObject) relation;
                    relationSet.add(Triple.of(jsonRelation.get("source").toString(),
                            jsonRelation.get("target").toString(),
                            jsonRelation.get("type").toString()));
                }
            }
            return relationSet;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
