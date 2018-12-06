package agh.edu.learning;

import java.util.List;

// Machine Larning Algorithm
// Strategy pattern
public interface MLA
{
    Double classify(List<Double[]> data);
    void learnFrom(List<Double[]> data);
}
