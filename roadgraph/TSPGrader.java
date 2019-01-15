package roadgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import util.GraphLoader;
import geography.*;

/** Test cases for the Traveling Salesperson Problem (TSP) algorithms 
 * (including greedyShortestCycle and twoOptShortestCycle).
 * 
 * @author ryanwilliamconnor
 */
public class TSPGrader implements Runnable {
	
    public String feedback;

    public int correct;

    private static final int TESTS = 8;

    /** Format readable feedback */
    public static String printOutput(double score, String feedback) {
        return "Score: " + score + "\nFeedback: " + feedback;
    }

    /** Format test number and description */
    public static String appendFeedback(int num, String test) {
        return "\n** Test #" + num + ": " + test + "...";
    }

    public static void main(String[] args) {
        TSPGrader grader = new TSPGrader();

        // Infinite loop detection
        Thread thread = new Thread(grader);
        thread.start();
        long endTime = System.currentTimeMillis() + 10000;
        boolean infinite = false;
        while(thread.isAlive()) {
            // Stop after 10 seconds
            if (System.currentTimeMillis() > endTime) {
                thread.stop();
                infinite = true;
                break;
            }
        }
        if (infinite) {
            System.out.println(printOutput((double)grader.correct / TESTS, 
            				               grader.feedback + 
            				               "\nYour program entered an infinite loop."));
        }
    }

    /** Run a test case on an adjacency list and adjacency matrix.
     * @param i The graph number
     * @param file The file to read from
     * @param desc A description of the graph
     * @param start The point to start from
     * @param end The point to end at
     */
    public void runTest(int i, boolean twoOpt, String file, String desc, 
    					GeographicPoint start, List<GeographicPoint> stops,
    					HashMap<Double,HashMap<Double,Integer>> offLimits) {
        MapGraph graph = new MapGraph();

        feedback += "\n\n" + desc;

        GraphLoader.loadRoadMap("data/graders/extension/" + file, graph);
        CorrectAnswer corr = new CorrectAnswer("data/graders/extension/" + file +
        									   ".answer", false);

        judge(i, twoOpt, graph, corr, start, stops, offLimits);
    }

    /** Compare the user's result with the right answer.
     * @param i: The graph number
     * @param result: The user's graph
     * @param corr: The correct answer
     * @param start: The point to start from
     * @param stops: The stops to visit once each
     * @param offLimits: The locations that are illegal to visit during the tour
     */
    public void judge(int i, boolean twoOpt, MapGraph result, CorrectAnswer corr, 
    				  GeographicPoint start, List<GeographicPoint> stops,
    				  HashMap<Double,HashMap<Double,Integer>> offLimits) {
    	// Correct if paths are same length and have the same elements
        feedback += appendFeedback(i, "Running greedyShortestCycle from (" + 
        							   start.getX() + ", " + start.getY() + 
        							   ") through " + stops);
        
        ArrayList<PathObject> TSPPaths;
        List<GeographicPoint> path;
        List<GeographicPoint> metaPath;
        
        // time the algorithm
        double algorithmTime = 0;
		long algorithmStartTime = System.nanoTime();
	
        if (twoOpt) {
            TSPPaths = result.twoOptShortestCycle(start, stops, offLimits);
        }
        else {
            TSPPaths = result.greedyShortestCycle(start, stops, offLimits);
        }
        
		long algorithmEndTime = System.nanoTime();
		algorithmTime = (double)(algorithmEndTime - algorithmStartTime) / 1000000000; // to get seconds
		System.out.println("Test " + i + " took " + algorithmTime + " seconds");

        path = TSPPaths.get(TSPPaths.size()-1).getPath();
        metaPath = result.constructMetaPath(start, stops, TSPPaths);

		boolean incorrectPath = false;
		
    	for (int j = 0; j < metaPath.size(); j++) {
    		
    		if (!metaPath.get(j).equals(corr.path.get(j))) {
    			incorrectPath = true;
    			break;
    		}
    	}
        
        if (metaPath == null) {
            if (corr.path == null) {
                feedback += "PASSED.";
                correct++;
            } else {
                feedback += "FAILED. Your implementation returned null; expected \n" + 
                			printPath(corr.path) + ".";
            }
        } else if (metaPath.size() != corr.path.size() || 
        		   !corr.path.containsAll(metaPath) ||
        		   incorrectPath) {
            feedback += "FAILED. Expected: \n" + printPath(corr.path) + 
            			"Got: \n" + printPath(metaPath);
            if (metaPath.size() != corr.path.size()) {
                feedback += "Your result has size " + metaPath.size() + 
                			"; expected " + corr.path.size() + ".";
            } else if (incorrectPath && corr.path.containsAll(metaPath)) {
            	
            	feedback += "Path is out of order.";
            } else {
            
            	feedback += "Correct size, but incorrect path.";
            }
        } else {
            feedback += "PASSED.";
            correct++;
        }
    }

    /** Print a search path in readable form */
    public String printPath(List<GeographicPoint> path) {
        String ret = "";
        for (GeographicPoint point : path) {
            ret += point + "\n";
        }
        return ret;
    }

    /** Run the grader */
    @Override
    public void run() {
        feedback = "";

        correct = 0;

        try {
      
        	List<GeographicPoint> testStops;
        	
        	testStops = createTestStops(1);
            runTest(1, false, "residentialLine.txt", 
            		"MAP: Straight line with only residential roads", 
            		new GeographicPoint(0, 0), 
            		testStops, 
            		new HashMap<Double,HashMap<Double,Integer>>());
            
        	testStops = createTestStops(2);
            runTest(2, false, "mixedLine.txt", 
            		"MAP: Straight line with mixed road types", 
            		new GeographicPoint(0, 0), 
            		testStops,
            		new HashMap<Double,HashMap<Double,Integer>>());
            
        	testStops = createTestStops(3);
            runTest(3, false, "secondaryStopsLine.txt", 
            		"MAP: Line with intersections in between stops and mixed road types", 
            		new GeographicPoint(0, 0), 
            		testStops,
            		new HashMap<Double,HashMap<Double,Integer>>());
            
        	testStops = createTestStops(4);    
            runTest(4, false, "lectureGreedy.txt", 
            		"MAP: Simple example from UCSD lecture where 2-opt TSP improves upon greedy TSP", 
            		new GeographicPoint(0, 0), 
            		testStops,
            		new HashMap<Double,HashMap<Double,Integer>>());
            
        	testStops = createTestStops(5);  
            runTest(5, true, "lecture2Opt.txt", 
            		"MAP: Simple example from UCSD lecture where 2-opt TSP improves upon greedy TSP", 
            		new GeographicPoint(0, 0), 
            		testStops,
            		new HashMap<Double,HashMap<Double,Integer>>());
            
        	testStops = createTestStops(6);  
            runTest(6, false, "lectureGreedyMixedRoads.txt", 
            		"MAP: Simple example from UCSD lecture where 2-opt TSP improves upon greedy TSP, " +
            				"but with mixed road types", 
            		new GeographicPoint(0, 0), 
            		testStops,
            		new HashMap<Double,HashMap<Double,Integer>>());
            
        	testStops = createTestStops(7);  
            runTest(7, true, "lecture2OptMixedRoads.txt", 
            		"MAP: Simple example from UCSD lecture where 2-opt TSP improves upon greedy TSP, " +
            				"but with mixed road types", 
            		new GeographicPoint(0, 0), 
            		testStops,
            		new HashMap<Double,HashMap<Double,Integer>>());
            
        	testStops = createTestStops(8);  
            runTest(8, true, "san_diego.txt", 
            		"MAP: Real San Diego map data", 
            		new GeographicPoint(32.7178178, -117.1565474), 
            		testStops,
            		new HashMap<Double,HashMap<Double,Integer>>());
            
        	testStops = createTestStops(9);  
            runTest(9, true, "lectureRTW.txt", 
            		"MAP: A round the world TSP with eight cities", 
            		new GeographicPoint(32.7153300,-117.1572600), // start in San Diego
            		testStops,
            		new HashMap<Double,HashMap<Double,Integer>>());
			
            if (correct == TESTS)
                feedback = "All tests passed. Great job!" + feedback;
            else
                feedback = "Some tests failed. Check your code for errors, " +
                		   "then try again: " + feedback;
			
        } catch (Exception e) {
            feedback += "\nError during runtime: " + e;
            e.printStackTrace();
        }
            
        System.out.println(printOutput((double)correct / TESTS, feedback));
    }
    
    public List<GeographicPoint> createTestStops(int test) {
    	
    	List<GeographicPoint> testStops = new ArrayList<GeographicPoint>();
    	
    	if (test < 3) {
    		
        	for (int i = 1; i < 7; i++) {
        		
            	testStops.add(new GeographicPoint(i, i));
        	}
    	}
    	else if (test == 3) {
        	
    		for (int i = 1; i < 7; i++) {
            	
    			testStops.add(new GeographicPoint(i*10, i*10));
        	}
    	}
    	else if (test < 8) {
    		
            testStops.add(new GeographicPoint(5, 0));
            testStops.add(new GeographicPoint(5, -8));
            testStops.add(new GeographicPoint(0, -6));
    	}
    	else if (test == 8) {
    		
    		testStops.add(new GeographicPoint(32.7198953, -117.1565681));
    		testStops.add(new GeographicPoint(32.719891, -117.158371));
    		testStops.add(new GeographicPoint(32.719902, -117.16112));
    		testStops.add(new GeographicPoint(32.719891, -117.163849));
    		testStops.add(new GeographicPoint(32.7198573, -117.1674044));
    		testStops.add(new GeographicPoint(32.71777, -117.167381));
    		testStops.add(new GeographicPoint(32.715717, -117.167374));
    		testStops.add(new GeographicPoint(32.7125442, -117.1665097));
    		testStops.add(new GeographicPoint(32.7114208, -117.164695));
    		testStops.add(new GeographicPoint(32.7114919, -117.1610414));
    		testStops.add(new GeographicPoint(32.7115072, -117.157387));
    		testStops.add(new GeographicPoint(32.7125906, -117.155593));
    		testStops.add(new GeographicPoint(32.715726, -117.1556095));
    		testStops.add(new GeographicPoint(32.7167929, -117.1556217));
    		testStops.add(new GeographicPoint(32.7167505, -117.1583407));
    		testStops.add(new GeographicPoint(32.717812, -117.1592692));
    		testStops.add(new GeographicPoint(32.7178034, -117.1620054));
    		testStops.add(new GeographicPoint(32.7177746, -117.1647364));
    		testStops.add(new GeographicPoint(32.714638, -117.164703));
    		testStops.add(new GeographicPoint(32.7125556, -117.1628801));
    		testStops.add(new GeographicPoint(32.7125762, -117.1592358));
    		testStops.add(new GeographicPoint(32.7146753, -117.1583403));
    		testStops.add(new GeographicPoint(32.7157252, -117.1574393));
    	}
    	else {
    		
    		testStops.add(new GeographicPoint(-12.0431800, -77.0282400)); // lima
    		testStops.add(new GeographicPoint(48.8534100, 2.3488000)); // paris
    		testStops.add(new GeographicPoint(30.0626300, 31.2496700)); // cairo
    		testStops.add(new GeographicPoint(-26.2022700, 28.0436300)); // johannesburg
    		testStops.add(new GeographicPoint(13.0878400, 80.2784700)); // chennai
    		testStops.add(new GeographicPoint(39.9075000, 116.3972300)); // beijing
    		testStops.add(new GeographicPoint(-31.9522400, 115.8614000)); // perth
    	}
    	
    	return testStops;
    }
}
