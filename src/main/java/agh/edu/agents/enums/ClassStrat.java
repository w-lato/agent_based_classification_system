package agh.edu.agents.enums;

public enum ClassStrat
{
    MAJORITY("MAJORITY"), // HARD
    WEIGHTED("WEIGHTED"), // SOFT
    AVERAGE("AVERAGE"),
    FLAT("FLAT"), // sum all probs (from all classfiers) into one input vector
    CLASS("CLASS"); // using machine learning

    ClassStrat(String name) {}
}
