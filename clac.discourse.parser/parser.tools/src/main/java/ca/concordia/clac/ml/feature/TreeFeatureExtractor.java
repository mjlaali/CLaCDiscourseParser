package ca.concordia.clac.ml.feature;

import static ca.concordia.clac.ml.feature.FeatureExtractors.nullSafeCall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.uima.jcas.cas.FSArray;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class TreeFeatureExtractor {
	
	public static Function<Constituent, Constituent> getParent(){
		return nullSafeCall((Constituent cons) -> (Constituent) cons.getParent());
	}

	public static Function<Constituent, String> getConstituentType(){
		return nullSafeCall((Constituent cons) -> cons.getConstituentType());
	}
	
	public static Function<Constituent, List<Constituent>> getSiblings(){
		Function<Constituent, FSArray> getChilderen = getParent().andThen((par) -> par.getChildren());
		return (cons) -> {
			FSArray childeren = getChilderen.apply(cons);
			if (childeren == null)
				return Collections.emptyList();
			List<Constituent> res = new ArrayList<>();
			for (int i = 0; i < childeren.size(); i++){
				res.add((Constituent)childeren.get(i));
			}
			return res;
		};
		
	}
	
	public static BiFunction<Constituent, List<Constituent>, Integer> findIndex(){
		return (cons, list) -> {
			for (int i = 0; i < list.size(); i++){
				if (cons.equals(list.get(i)))
					return i;
			}
			return -1;
		};
	}
	
	public static Function<Constituent, Constituent> getLeftSibling(){
		return (cons) -> {
			List<Constituent> siblings = getSiblings().apply(cons);
			Integer index = findIndex().apply(cons, siblings);
			if (index == -1 || index == 0)
				return null;
			return siblings.get(index - 1);
		};
	}
	
	public static Function<Constituent, Constituent> getRightSibling(){
		return (cons) -> {
			List<Constituent> siblings = getSiblings().apply(cons);
			Integer index = findIndex().apply(cons, siblings);
			if (index == -1 || index == siblings.size() - 1)
				return null;
			return siblings.get(index + 1);
		};
	}

}
