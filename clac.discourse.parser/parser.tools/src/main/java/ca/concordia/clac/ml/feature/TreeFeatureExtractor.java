package ca.concordia.clac.ml.feature;

import static ca.concordia.clac.ml.feature.FeatureExtractors.convertToList;
import static ca.concordia.clac.ml.feature.FeatureExtractors.getFunction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class TreeFeatureExtractor {
	
	public static Function<Constituent, Constituent> getParent(){
		return getFunction(Constituent::getParent).andThen(t -> (Constituent) t);
	}
	
	public static <T extends Annotation> Function<T, String> getConstituentType(){
		return (T ann) -> {
				if (ann instanceof Constituent)
					return ((Constituent)ann).getConstituentType();
				if (ann instanceof Token)
					return Optional.of((Token)ann).map(Token::getPos).map(POS::getPosValue).orElse(null);
				return null;
		};
	}
	
	public static Function<Constituent, List<Annotation>> getSiblings(){
		return (cons) -> {
			return Optional.of(cons).map(getParent())
					.map(Constituent::getChildren)
					.map(convertToList(Annotation.class))
					.orElse(Collections.emptyList());
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
