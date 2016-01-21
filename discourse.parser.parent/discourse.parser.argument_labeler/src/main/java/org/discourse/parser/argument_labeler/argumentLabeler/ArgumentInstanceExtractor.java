package org.discourse.parser.argument_labeler.argumentLabeler;

import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getChilderen;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getPathFromRoot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class ArgumentInstanceExtractor implements Function<DiscourseConnective, List<ArgumentInstance>>{
	private int todoCnt;

	public List<ArgumentInstance> apply(DiscourseConnective discourseConnective) {
		List<Constituent> pathToRoot = getPathFromRoot(DiscourseConnective.class).apply(discourseConnective);
		
		if (pathToRoot.size() == 0){
			System.out.println("Arg2Labeler.process(): TODO [" + (todoCnt++)
					+ "]\t<" + TokenListTools.getTokenListText(discourseConnective) +
					">\t:" + discourseConnective.getCoveredText());
			return Collections.emptyList();
		}
		
		Constituent imediateDcParent = pathToRoot.get(pathToRoot.size() - 1);
		
		List<List<Annotation>> pathNodeChilderen = pathToRoot.stream()
				.map(getChilderen())
				.collect(Collectors.toList());
		
		List<Annotation> childeren = new ArrayList<>(pathNodeChilderen.get(0));
		for (int i = 1; i < pathToRoot.size(); i++){
			int idxNode = childeren.indexOf(pathToRoot.get(i));
			childeren.remove(idxNode);
			childeren.addAll(pathNodeChilderen.get(i));
		}
		
		List<ArgumentInstance> instances = childeren.stream()
				.map((child) -> new ArgumentInstance(child, imediateDcParent))
				.collect(Collectors.toList());
		
		return instances;
	}
}