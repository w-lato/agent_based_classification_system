package agh.edu.agents.enums;

// TODO add only addition of probs with no multiplication
public enum ClassStrat
{
    MAJORITY("MAJORITY"), // HARD
    WEIGHTED("WEIGHTED"), // SOFT
//    PROB_ENTROPY("PROB_ENTROPY"), // SOFT
    F1_SCORE_VOTING("F1_SCORE_VOTING"),
    PROB_WEIGHT("PROB_WEIGHT"), // SUM OF PROBS MULTIPLIED BY THE WEIGHT
    F1_SCORE_PROB("F1_SCORE_PROB"); // SUM OF PROBS MULT. BY THE F_SCORE
//    AVERAGE("AVERAGE"),
//    FLAT("FLAT"), // sum all probs (from all classfiers) into one input vector
//    CLASS("CLASS"); // using machine learning

    ClassStrat(String name) {}
}
