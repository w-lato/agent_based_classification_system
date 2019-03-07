package agh.edu.learning;

import agh.edu.agents.enums.S_Type;
import agh.edu.learning.params.*;
import weka.core.Instances;

public class ParamsFactory
{
    public static Params getMLP(int class_num, int features_num)
    {
        return new ParamsMLP( class_num, features_num );
    }
    public static Params getMLP(Instances data)
    {
        return new ParamsMLP( data.numClasses(), data.numAttributes() - 1);
    }

    public static Params getParams(S_Type type, Instances data )
    {
        if( type.equals( S_Type.MLP ) ) return getMLP(data);
        else return getParams( type );
    }

    public static Params getParams(S_Type type)
    {
        switch (type)
        {
            case SMO: return new ParamsSMO();
            case NA: return new ParamsNB();
            case IBK: return new ParamsIBk();
            case LOG: return new ParamsLog();
            case RF: return new ParamsRF();
            case ADA: return new ParamsADA();
            default: return null;
        }
    }
}
