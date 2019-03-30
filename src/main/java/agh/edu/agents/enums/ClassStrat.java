package agh.edu.agents.enums;

public enum ClassStrat
{
    MAJORITY("MAJORITY"), // HARD
    WEIGHTED("WEIGHTED"), // SOFT
    PROB_WEIGHT("PROB_WEIGHT"); // SUM OF PROBS MULTIPLIED BY THE WEIGHT
//    AVERAGE("AVERAGE"),
//    FLAT("FLAT"), // sum all probs (from all classfiers) into one input vector
//    CLASS("CLASS"); // using machine learning

    ClassStrat(String name) {}
}
