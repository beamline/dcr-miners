package beamline.dcr.testsoftware;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.tuple.Pair;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;

import org.jfree.data.xy.XYSeriesCollection;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PlotResults {
    private final List<String> constraintList;
    private final List<Pair<List<Double>,String>> performanceLists;



    public PlotResults(String resultFilePath) {



        this.constraintList = new ArrayList<>();
        this.performanceLists = new ArrayList<>();
        this.performanceLists.add(Pair.of(new ArrayList<>(),"Jaccard"));
        this.performanceLists.add(Pair.of(new ArrayList<>(),"Model Precision"));
        this.performanceLists.add(Pair.of( new ArrayList<>(),"Model Recall"));
        this.performanceLists.add(Pair.of(new ArrayList<>(),"Log fitness"));
        this.performanceLists.add(Pair.of( new ArrayList<>(),"Log Precision"));

        try(
                BufferedReader br = new BufferedReader(new FileReader(resultFilePath));
                CSVParser parser = CSVFormat.DEFAULT.withDelimiter(',').withHeader().parse(br)

        ) {
            for(CSVRecord record : parser) {
                this.constraintList.add(record.get(0));
                for(int i = 0; i < performanceLists.size(); i++){
                    List<Double> performanceMetricList = performanceLists.get(i).getLeft();

                    performanceMetricList.add(Double.valueOf(record.get(i+1)));

                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void saveScatterPlot(String filePath) throws IOException {
        for(int i = 0; i < performanceLists.size(); i++){
            List<Double> performanceMetricList = performanceLists.get(i).getLeft();
            String title = performanceLists.get(i).getRight();
            JFreeChart scatterPlot = createPlot(performanceMetricList,title);
            ChartUtils.saveChartAsPNG(new File(filePath+"/"+title+".png"), scatterPlot, 1200, 900);

        }




    }
    private JFreeChart createPlot(List<Double> performanceMetricList,String title){
        XYDataset dataset = createDataset(performanceMetricList);

        JFreeChart scatterPlot = ChartFactory.createScatterPlot(
                title, // Chart title
                "Constraints", // X-Axis Label
                "Performance", // Y-Axis Label

                dataset // Dataset for the Chart
        );

        scatterPlot.getXYPlot().setDomainAxis(getSymbolAxis());

        scatterPlot.getXYPlot().getDomainAxis().setVerticalTickLabels(true);

        return scatterPlot;

    }
    private XYDataset createDataset(List<Double> listToPlot) {

        final XYSeries dataSeries = new XYSeries( "" );
        for (int i = 0; i<constraintList.size(); i++){
            dataSeries.add( i , listToPlot.get(i));
        }

        final XYSeriesCollection dataset = new XYSeriesCollection( );
        dataset.addSeries( dataSeries );

        return dataset;
    }
    private SymbolAxis getSymbolAxis(){


        SymbolAxis xAxis = new SymbolAxis("Series",
                constraintList.toArray(new String[0]));
        xAxis.setGridBandsVisible(false);

        return xAxis;
    }


}
