package ca.concordia.clac.ml.feature;

import static ca.concordia.clac.ml.feature.FeatureExtractors.nullSafeCall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class TreeFeatureExtractor {
	
	public static Function<Constituent, Constituent> getParent(){
		return nullSafeCall((Constituent cons) -> (Constituent) cons.getParent());
	}

	public static <T extends Annotation> Function<T, String> getConstituentType(){
		return nullSafeCall((T ann) -> {
			try{
				if (ann instanceof Constituent)
					return ((Constituent)ann).getConstituentType();
				else if (ann instanceof Token)
					return ((Token)ann).getPos().getPosValue();
			} catch (NullPointerException e){
				
			}
			return null;
		});
	}
	
	public static Function<Constituent, List<Annotation>> getSiblings(){
		Function<Constituent, FSArray> getChilderen = getParent().andThen((par) -> par.getChildren());
		return (cons) -> {
			FSArray childeren = getChilderen.apply(cons);
			if (childeren == null)
				return Collections.emptyList();
			List<Annotation> res = new ArrayList<>();
			for (int i = 0; i < childeren.size(); i++){
				res.add((Annotation)childeren.get(i));
			}
			return res;
		};
		
	}
	

	
	public static Function<Constituent, Annotation> getLeftSibling(){
		return (cons) -> {
			List<Annotation> siblings = getSiblings().apply(cons);
			Integer index = siblings.indexOf(cons);
			if (index == -1 || index == 0)
				return null;
			return siblings.get(index - 1);
		};
	}
	
	public static Function<Constituent, Annotation> getRightSibling(){
		return (cons) -> {
			List<Annotation> siblings = getSiblings().apply(cons);
			Integer index = siblings.indexOf(cons);
			if (index == -1 || index == siblings.size() - 1)
				return null;
			return siblings.get(index + 1);
		};
	}

}
