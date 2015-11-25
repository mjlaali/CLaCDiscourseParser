package ca.concordia.clac.ml.scop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class Scopes {

	public static <IN_ANN extends Annotation> Function<IN_ANN, List<Constituent>> getPathToRoot(Class<IN_ANN> cls){
		return (inAnn) -> {
			Constituent[] constituents = JCasUtil.selectCovering(Constituent.class, inAnn).toArray(new Constituent[0]);
			
			Constituent parent = null;
			for (int i = 0; i < constituents.length; i++){
				boolean found = false;
				for (int j = i; j < constituents.length; j++){
					Constituent consParent = (Constituent) constituents[j].getParent();
//					System.out.print(constituents[j].getConstituentType() + "->");
//					System.out.println(consParent == null ? "null" : consParent.getConstituentType());
					if (parent == consParent || (consParent != null && consParent.equals(parent))){
						Constituent temp = constituents[i];
						constituents[i] = constituents[j];
						constituents[j] = temp;
						found = true;
						break;
					} 
				}
				if (found)
					parent = constituents[i];
				else 
					System.err.println("Cannot found the childeren of node " + parent);
					
			}
				
			return new ArrayList<>(Arrays.asList(constituents));
		};
	}
}
