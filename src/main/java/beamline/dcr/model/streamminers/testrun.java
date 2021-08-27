package beamline.dcr.model.streamminers;

import java.util.*;

public class testrun {

    public static void main(String[] args) {
        Map<Integer,Integer> mapTest = new HashMap<>();
        Queue<Integer> windowQueue = new LinkedList<>();

        int maxSize = 5;

        int[] lol = {1,2,3,4,5,4,1,4,1,6,7};

        for(int l : lol){
            if (mapTest.containsKey(l)){
                windowQueue.remove(l);
                windowQueue.add(l);
            }else{

                if(windowQueue.size()>=maxSize){
                    windowQueue.poll();
                    mapTest.remove(l);
                }
                mapTest.put(l,l);
                windowQueue.add(l);
            }


        }

    }
}
