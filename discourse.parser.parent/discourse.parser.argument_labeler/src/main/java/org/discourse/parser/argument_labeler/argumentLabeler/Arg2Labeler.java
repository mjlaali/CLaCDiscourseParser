package org.discourse.parser.argument_labeler.argumentLabeler;

import static ca.concordia.clac.ml.feature.FeatureExtractors.makeFeature;
import static ca.concordia.clac.ml.feature.FeatureExtractors.multiMap;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getLeftSibling;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getParent;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getPath;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getRightSibling;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.join;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.mapOneByOneTo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;

import ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseVsNonDiscourseClassifier;
import ca.concordia.clac.ml.classifier.ClassifierAlgorithmFactory;
import ca.concordia.clac.ml.classifier.InstanceExtractor;
import ca.concordia.clac.ml.feature.TreeFeatureExtractor;
import ca.concordia.clac.uima.Tools;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

class Arg2Instance{
	public Arg2Instance(Constituent aNode,	DiscourseConnective discourseConnective, Constituent imediateDcParent) {
		this.constituent = aNode;
		this.discourseConnective = discourseConnective;
		this.imediateDcParent = imediateDcParent;
	}
	
	public Constituent getConstituent() {
		return constituent;
	}
	
	public DiscourseConnective getDiscourseConnective() {
		return discourseConnective;
	}
	
	public Constituent getImediateDcParent() {
		return imediateDcParent;
	}
	
	Constituent constituent;
	Constituent imediateDcParent;
	DiscourseConnective discourseConnective;
	
	
}

class SelectedDiscourseRelation{
	public SelectedDiscourseRelation(DiscourseRelation selectedRelation, List<Token> arg1Token, List<Token> arg2Token) {
		this.discourseRelation = selectedRelation;
		this.arg1Tokens = new HashSet<Token>(arg1Token);
		this.arg2Tokens = new HashSet<Token>(arg2Token);

	}

	Set<Token> arg1Tokens = null, arg2Tokens = null;
	DiscourseRelation discourseRelation;
}


enum NodeArgType {
	Arg1, Arg2, Non, Relation
}

class Arg2InstanceLabeler{
	
	public static String getLabel(Arg2Instance instance){
		NodeArgType res;
		DiscourseRelation discourseRelation = instance.getDiscourseConnective().getDiscourseRelation();
		if (discourseRelation == null)
			return null;
		
		DiscourseArgument arguments = discourseRelation.getArguments(1);
		List<Token> arg2Tokens = TokenListTools.convertToTokens(arguments);
		List<Token> nodeTokens = JCasUtil.selectCovered(Token.class, instance.getConstituent());
		if (nodeTokens.containsAll(arg2Tokens)){
			res = NodeArgType.Arg2;
		} else
			res = NodeArgType.Non;
		return res.toString();
	}
}

class Arg2InstanceExtractor implements InstanceExtractor<Arg2Instance>{
	private int todoCnt;
	private Map<DiscourseConnective, SelectedDiscourseRelation> selectedDiscourseRelations;


	@Override
	public Collection<Arg2Instance> getInstances(JCas aJCas) {
		List<Arg2Instance> instances = new ArrayList<Arg2Instance>();
		selectedDiscourseRelations = new HashMap<DiscourseConnective, SelectedDiscourseRelation>();
		
		for (DiscourseConnective discourseConnective: JCasUtil.select(aJCas, DiscourseConnective.class)){
			List<Constituent> coveringNodes = new ArrayList<Constituent>(JCasUtil.selectCovering(Constituent.class, discourseConnective));
			Constituent imediateDcParent = coveringNodes.size() == 0 ? null : coveringNodes.get(coveringNodes.size() - 1);
			if (imediateDcParent == null){
				System.out.println("Arg2Labeler.process(): TODO [" + (todoCnt++)
						+ "]\t<" + TokenListTools.getTokenListText(discourseConnective) +
						">\t:" + discourseConnective.getCoveredText()); 
				continue;
			}
			for (Constituent aNode: createCandidates(discourseConnective,
					coveringNodes, imediateDcParent)){
				instances.add(new Arg2Instance(aNode, discourseConnective, imediateDcParent));
			};

			DiscourseRelation selectedRelation = null;
			if (JCasUtil.exists(aJCas, DiscourseRelation.class)){
				selectedRelation = selectDiscourseRelation(discourseConnective);
				if (selectedRelation == null){
					System.err.println("Arg2Labeler.process()" + Tools.getDocName(aJCas) + "\t" + discourseConnective.getCoveredText() + "\t" +discourseConnective.getBegin());
					selectDiscourseRelation(discourseConnective);
					continue;
				}
				selectedDiscourseRelations.put(discourseConnective, new SelectedDiscourseRelation(selectedRelation, 
						TokenListTools.convertToTokens(selectedRelation.getArguments(0)), TokenListTools.convertToTokens(selectedRelation.getArguments(1))));
			}

		}
		return instances;
	}
	

	private List<Constituent> createCandidates(
			DiscourseConnective discourseConnective,
			List<Constituent> coveringNodes, Constituent imediateDcParent) {
		List<Constituent> instances = new ArrayList<Constituent>();
		final Set<String> targetTag = new TreeSet<String>(Arrays.asList(new String[]{"S", "SBAR"}));
		for (Constituent node: coveringNodes){
			String nodeType = node.getConstituentType();
			if (targetTag.contains(nodeType))
				instances.add(node);
		}
		Collections.reverse(instances);
		return instances;
	}
	
	public static DiscourseRelation selectDiscourseRelation(DiscourseConnective discourseConnective) {
		List<DiscourseRelation> discourseRelations = JCasUtil.selectCovering(DiscourseRelation.class, discourseConnective);
		List<Token> connectiveTokenList = TokenListTools.convertToTokens(discourseConnective);
		DiscourseRelation selectedRelation = null;
		for (DiscourseRelation discourseRelation: discourseRelations){
			DiscourseConnective relationConnective = discourseRelation.getDiscourseConnective();
			if (relationConnective == null)	//it is an implicit relation
				continue;
			List<Token> relationConnectiveTokenList = TokenListTools.convertToTokens(relationConnective);
			if (TokenListTools.isEqualTokenList(connectiveTokenList, relationConnectiveTokenList)){
				selectedRelation = discourseRelation;
				break;
			}
		}
		
		return selectedRelation;
	}
}

public class Arg2Labeler implements ClassifierAlgorithmFactory<String, Arg2Instance>{

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
	}

	@Override
	public InstanceExtractor<Arg2Instance> getExtractor(JCas jCas) {
		return new Arg2InstanceExtractor();
	}

	@Override
	public List<Function<Arg2Instance, List<Feature>>> getFeatureExtractor(JCas jCas) {
		Function<Arg2Instance, DiscourseConnective> convertToDc = Arg2Instance::getDiscourseConnective;
		Function<Arg2Instance, List<Feature>> dcFeatures = convertToDc.andThen(
				DiscourseVsNonDiscourseClassifier.getDiscourseConnectiveFeatures()
				);
		
		Function<Arg2Instance, Constituent> convertToConstituent = Arg2Instance::getConstituent;
		Function<Arg2Instance, Feature> childPatterns =
								convertToConstituent.andThen(
										TreeFeatureExtractor.getChilderen()).andThen(
										mapOneByOneTo(TreeFeatureExtractor.getConstituentType())).andThen(
										join(Collectors.joining("-"))).andThen(
										makeFeature("ChildPat"));
		Function<Arg2Instance, Feature> ntCtx = convertToConstituent
				.andThen(multiMap(
						getConstituentType(), 
						getParent().andThen(getConstituentType()), 
						getLeftSibling().andThen(getConstituentType()),
						getRightSibling().andThen(getConstituentType())
						))
				.andThen(join(Collectors.joining("-")))
				.andThen(makeFeature("NT-Ctx"));
		
		Function<Arg2Instance, List<Constituent>> pathExtractor = (inst) -> getPath().apply(inst.getImediateDcParent(), inst.getConstituent()); 
		Function<Arg2Instance, Feature> path = pathExtractor
				.andThen(mapOneByOneTo(getConstituentType()))
				.andThen(join(Collectors.joining("-")))
				.andThen(makeFeature("CON-NT-Path"));

		return Arrays.asList(dcFeatures, multiMap(childPatterns, ntCtx, path));
	}

	@Override
	public Function<Arg2Instance, String> getLabelExtractor(JCas jCas) {
		return Arg2InstanceLabeler::getLabel;
	}

	@Override
	public BiConsumer<String, Arg2Instance> getLabeller(JCas jCas) {
		// TODO Auto-generated method stub
		return null;
	}

}
