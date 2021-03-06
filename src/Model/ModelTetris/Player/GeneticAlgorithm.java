package Model.ModelTetris.Player;

import Model.ModelTetris.Tetris;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Irindul on 21/02/2017.
 * Genetic algorithm to find a proper evaluator
 */
public class GeneticAlgorithm implements Runnable{


    private List<ArtificialIntelligence> specimens;

    public GeneticAlgorithm() {
        specimens = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            specimens.add(new ArtificialIntelligence(new Tetris(), Evaluator.getRandomEvaluator()));
        }
    }

    private Evaluator run(int success){

        List<ArtificialIntelligence> offsprings;
        int nbGen = 0;
        while(!overSuccess(success)){

            specimens.forEach(ArtificialIntelligence::reset);

            System.out.println("Generation : " + nbGen++);

            ExecutorService execute = Executors.newFixedThreadPool(100);


            specimens.forEach(execute::execute);
            execute.shutdown();

            while(!execute.isTerminated()){
                //Waiting for all threads to terminate
            }
            
            System.out.println("Threads ternimated");
            

            offsprings = selectionCrossover();
            offsprings.forEach(player -> {
                player.getEvaluator().normalization();
                Random rd = new Random();
                int mutate = rd.nextInt(100);
                if(mutate < getPourcentageRate()){
                    player.getEvaluator().mutation();
                }

            });

            specimens.sort(Comparator.comparingDouble(ArtificialIntelligence::getScore));


           // specimens.sort((o1, o2) -> o1.getScore() > o2.getScore() ? 1 : -1);

            int numberDeletion = offsprings.size();
            for (int i = 0; i < numberDeletion; i++) {
                specimens.remove(i);
            }

            specimens.addAll(offsprings);
        }

        specimens.sort((o1, o2) -> o1.getScore() < o2.getScore() ? 1 : -1);


        return specimens.get(0).getEvaluator();

    }

    private double  getPourcentageRate(){
        /*double square = score * score;
        double limit = 0.05;
        return ((1/(square + 1)) + limit) * 100;*/
        return 5;
    }

    private List<ArtificialIntelligence> selectionCrossover() {
        List<ArtificialIntelligence> offsprings = new ArrayList<>();
        List<ArtificialIntelligence> selected = new ArrayList<>();
        int numberOfCrossover = (int) ((double) 30/100 * specimens.size());
        int numberSelection = (int) ( (double) 10/100 * specimens.size());

        Random rd = new Random();
        while (offsprings.size() < numberOfCrossover){

            while(selected.size() < numberSelection){
                int ind = rd.nextInt(specimens.size());
                selected.add(specimens.get(ind));
            }

            selected.sort((o1, o2) -> o1.getScore() < o2.getScore() ? 1 : -1);
            ArtificialIntelligence parent = selected.get(0);
            ArtificialIntelligence parent2 = selected.get(1);

            Evaluator evChild = Evaluator.crossover(parent.getEvaluator(), parent2.getEvaluator());

            offsprings.add(new ArtificialIntelligence(new Tetris(), evChild));

        }
        return offsprings;

    }

    private boolean overSuccess(int success) {

        int max = specimens.stream()
                .max((o1, o2) -> o1.getScore() > o2.getScore() ? 1 : -1)
               .get()
               .getScore();
        return max >= success;
    }

    @Override
    public void run() {
        this.run(2000).display();
    }
}
