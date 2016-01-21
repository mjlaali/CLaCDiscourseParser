package ca.concordia.clac.ml.feature;

import static ca.concordia.clac.ml.feature.FeatureExtractors.convertToList;
import static ca.concordia.clac.ml.feature.FeatureExtractors.getFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class TreeFeatureExtractor {
	
	public static Function<Annotation, Constituent> getParent(){
		return (ann) -> {
			if (ann instanceof Token)
				return getFunction(Token::getParent).andThen(t -> (Constituent) t).apply((Token) ann);
			if (ann instanceof Constituent)
				return getFunction(Constituent::getParent).andThen(t -> (Constituent) t).apply((Constituent) ann);
			return null;
		};
	}

	public static BiFunction<Annotation, Annotation, List<Annotation>> getPath(){
		return (source, target) -> {
			List<Constituent> fromSource = getPathToRoot(Annotation.class).apply(source);
			List<Constituent> fromTarget = getPathToRoot(Annotation.class).apply(target);
			
			int commonRoot;
			for (commonRoot = 0; commonRoot < Math.min(fromSource.size(), fromTarget.size()); commonRoot++){
				if (!fromSource.get(commonRoot).equals(fromTarget.get(commonRoot)))
					break;
			}
			--commonRoot;
			
			List<Annotation> path = new ArrayList<>();
			path.add(source);
			for (int j = fromSource.size() - 1; j > commonRoot; j--)
				path.add(fromSource.get(j));
			
			if ((commonRoot - 1) < fromSource.size())
				path.add(fromSource.get(commonRoot));
			else {
				path.add(fromTarget.get(commonRoot));
			}
			
			path.add(null);	//we change direction here.
			for (int j = commonRoot + 1; j < fromTarget.size(); j++){
				path.add(fromTarget.get(j));
			}
			path.add(target);
			return path;
		};
	}

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
			
			int idx = -1;
			for (int i = 0; i < constituents.length; i++){
				if (constituents[i].equals(inAnn))
					idx = i;
			}
				
			List<Constituent> res = new ArrayList<>(Arrays.asList(constituents));
			if (idx != -1)
				res = res.subList(0, idx);
			return res;
		};
	}
	
	public static <T extends Annotation> Function<T, String> getConstituentType(){
		return (ann) -> {
				if (ann == null)
					return null;
				if (ann instanceof Constituent)
					return ((Constituent)ann).getConstituentType();
				if (ann instanceof Token)
					return Optional.of((Token)ann).map(Token::getPos).map(POS::getPosValue).orElse(null);
				return null;
		};
	}
	
	public static Function<Annotation, List<Annotation>> getChilderen(){
		return (Annotation ann) -> {
			if (ann instanceof Constituent)
				return Optional.of((Constituent)ann).map(Constituent::getChildren)
						.map(convertToList(Annotation.class))
						.orElse(Collections.emptyList());
			if (ann instanceof Token)
				return Collections.emptyList();
			return null;
	};
	}
	
	public static Function<Annotation, List<Annotation>> getSiblings(){
		return (cons) -> {
			return Optional.of(cons).map(getParent())
					.map(getChilderen())
					.orElse(Collections.emptyList());
		};
		
	}
	
	public static Function<Annotation, Annotation> getLeftSibling(){
		return (cons) -> {
			List<Annotation> siblings = getSiblings().apply(cons);
			Integer index = siblings.indexOf(cons);
			if (index == -1 || index == 0)
				return null;
			return siblings.get(index - 1);
		};
	}
	
	public static Function<Annotation, Annotation> getRightSibling(){
		return (cons) -> {
			List<Annotation> siblings = getSiblings().apply(cons);
			Integer index = siblings.indexOf(cons);
			if (index == -1 || index == siblings.size() - 1)
				return null;
			return siblings.get(index + 1);
		};
	}

}
