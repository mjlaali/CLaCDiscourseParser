package org.discourse.parser.argument_labeler.argumentLabeler;

import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getChilderen;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getPathFromRoot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;

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
			childeren.addAll(idxNode, pathNodeChilderen.get(i));
		}
		
		List<ArgumentInstance> instances = childeren.stream()
				.map((child) -> new ArgumentInstance(child, imediateDcParent))
				.collect(Collectors.toList());
		
		Constituent root = pathToRoot.get(0);
		List<Sentence> prevSents = JCasUtil.selectPreceding(Sentence.class, root, 1);
		if (prevSents.size() > 0){
			Sentence prevSent = prevSents.get(0);
			List<ROOT> prevSentRoots = JCasUtil.selectCovered(ROOT.class, prevSent);
			if (prevSentRoots.size() > 0){
				instances.add(0, new ArgumentInstance(prevSentRoots.get(0), imediateDcParent));
			}
		}

		
		return instances;
	}
}