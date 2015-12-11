package pl.edu.agh.muvto.solver;

import java.util.HashMap;
import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.uma.jmetal.solution.BinarySolution;

@Component
public class MuvtoSolverProvider {
	private LinkedList<MuvtoProblem> problemQueue;
	private MuvtoWorker[] workers;
	private HashMap<MuvtoProblem, BinarySolution> solutionsMap;

	@Autowired
	private MuvtoSolver muvtoSolver;
	

	public MuvtoSolverProvider() {
		
		int workersNumber = 5;
		problemQueue = new LinkedList<MuvtoProblem>();
		solutionsMap = new HashMap<MuvtoProblem, BinarySolution>();
		
		workers = new MuvtoWorker[workersNumber];
		
        for (int i=0; i<workersNumber; i++) {
            workers[i] = new MuvtoWorker(muvtoSolver);
            workers[i].start();
        }
	}
	
	private class MuvtoWorker extends Thread{
		
		private MuvtoSolver muvtoSolver;
		
		public MuvtoWorker(MuvtoSolver solver){
			muvtoSolver = solver;
		}
		
		
        public void run() {
        	MuvtoProblem problem = null;
            while (true) {
                synchronized(problemQueue) {
                    while (problemQueue.isEmpty()) {
                        try
                        {
                        	problemQueue.wait();
                        }
                        catch (InterruptedException ignored)
                        {
                        }
                    }

                    problem = problemQueue.removeFirst();
                }

                solutionsMap.put(problem, muvtoSolver.solve(problem));
            }
        }
	}
	
	public void addPredictedProblem(MuvtoProblem problem){
		problemQueue.add(problem);
	}
	
	public BinarySolution getSolution(MuvtoProblem problem, float maxDist){
		BinarySolution solution = null;
		float minDist = Float.MAX_VALUE;
		
		for(MuvtoProblem predictedProblem : solutionsMap.keySet()){
			float newDist = predictedProblem.distance(problem);
			if (newDist < minDist){
				minDist = newDist;
				solution = solutionsMap.get(predictedProblem);
			}
		}
		
		if (minDist < maxDist){
			return solution;
		}
		
		solution = muvtoSolver.solve(problem);
		
		return solution;
	}
	
}
