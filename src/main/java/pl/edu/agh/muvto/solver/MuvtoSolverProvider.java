package pl.edu.agh.muvto.solver;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.uma.jmetal.solution.BinarySolution;

import fj.P;
import fj.P2;

public class MuvtoSolverProvider {

    private static final Logger logger
        = LoggerFactory.getLogger(MuvtoSolverProvider.class);

    private Map<MuvtoProblem, BinarySolution> solutionsMap = new HashMap<>();
    private LinkedList<MuvtoProblem> problemQueue = new LinkedList<>();
    private MuvtoWorker[] workers;

    @Autowired
    private MuvtoSolver muvtoSolver;

    public MuvtoSolverProvider(int workersNumber) {
        workers = new MuvtoWorker[workersNumber];
    }

    @PostConstruct
    private void init() {
        for (int i=0; i<workers.length; i++) {
            workers[i] = new MuvtoWorker(muvtoSolver,
                                         problemQueue,
                                         solutionsMap);
            workers[i].start();
        }
    }

    private static class MuvtoWorker extends Thread {

        private static final Logger logger
            = LoggerFactory.getLogger(MuvtoWorker.class);

        private MuvtoSolver muvtoSolver;
        private LinkedList<MuvtoProblem> problemQueue;
        private Map<MuvtoProblem, BinarySolution> solutionsMap;

        public MuvtoWorker(MuvtoSolver solver,
                           LinkedList<MuvtoProblem> queue,
                           Map<MuvtoProblem, BinarySolution> map) {
            muvtoSolver = solver;
            problemQueue = queue;
            solutionsMap = map;
        }

        public void run() {
            MuvtoProblem problem = null;
            while (true) {

                synchronized (problemQueue) {
                    while (problemQueue.isEmpty()) {
                        try {
                            problemQueue.wait();
                        } catch (InterruptedException ignored) {
                            ignored.printStackTrace();
                        }
                    }
                    problem = problemQueue.removeFirst();
                }

                synchronized (logger) {
                    logger.info("worker calculates solution");
                }

                BinarySolution solution = muvtoSolver.solve(problem);
                solutionsMap.put(problem, solution);

                synchronized (logger) {
                    logger.info("solution calculation completed");
                }
            }
        }
    }

    public void addPredictedProblem(MuvtoProblem problem) {
        synchronized (problemQueue) {
            problemQueue.add(problem);
            problemQueue.notify();
        }
    }

    public BinarySolution getSolution(MuvtoProblem realProblem,
                                      double maxAllowedDist) {
        return solutionsMap.keySet().stream()
            .map(problem -> P.p(problem, realProblem.distance(problem)))
            .filter(p -> p._2() < maxAllowedDist)
            .min(Comparator.comparing(P2::_2))
            .map(p -> {
                String message = "using predicted solution [distance=%f]";
                logger.info(String.format(message, p._2()));
                return solutionsMap.get(p._1());
            }).orElseGet(() -> {
                logger.warn("calculating solution from a scratch!");
                return muvtoSolver.solve(realProblem);
            });
    }

}
