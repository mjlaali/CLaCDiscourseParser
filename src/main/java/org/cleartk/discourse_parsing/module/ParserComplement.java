package org.cleartk.discourse_parsing.module;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ArgType;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.RelationType;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;

public class ParserComplement extends ConllDiscourseGoldAnnotator{
	public static final String PARAM_ADD_IMPLICIT_RELATIONS = "PARAM_ADD_IMPLICIT_RELATIONS";
	
	@ConfigurationParameter(
			name = PARAM_ADD_IMPLICIT_RELATIONS,
			mandatory = true)
	private boolean addImplicitRelations;


	private static final String DEFAULT_SENSE = "Expansion.Conjunction";

	public static AnalysisEngineDescription getDescription(String discourseFilePath, boolean addImplicitRelations) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				ParserComplement.class,
				PARAM_DISCOURSE_JSON_FILE,
				discourseFilePath, 
				PARAM_ADD_MULTIPLE_SENSES, 
				false, 
				PARAM_ADD_IMPLICIT_RELATIONS, 
				addImplicitRelations);
	}
	
	private List<DiscourseRelation> discourseRelations = new ArrayList<DiscourseRelation>();
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		discourseRelations.clear();
		super.process(aJCas);
		
		if (JCasUtil.exists(aJCas, DiscourseRelation.class)){
			addSenses(aJCas);
		} else if (JCasUtil.exists(aJCas, DiscourseConnective.class)){
			addRelations(aJCas);
		} else {
			addRelaitons(aJCas);
		}
		
	}
	
	private void addRelaitons(JCas aJCas) {
		for (DiscourseRelation discourseRelation: discourseRelations){
			discourseRelation.addToIndexes();
		}
	}

	private void addRelations(JCas aJCas) {
		for (DiscourseConnective discourseConnective: JCasUtil.select(aJCas, DiscourseConnective.class)){
			DiscourseRelation connectiveDiscourseRelation = null;
			for (DiscourseRelation goldDiscourseRelation: discourseRelations){
				DiscourseConnective goldDiscourseConnective = goldDiscourseRelation.getDiscourseConnective();
				if (isEqual(discourseConnective, goldDiscourseConnective)){
					connectiveDiscourseRelation = goldDiscourseRelation;
				}
			}
			
			if (connectiveDiscourseRelation == null){
				connectiveDiscourseRelation = new DiscourseRelation(aJCas);
				connectiveDiscourseRelation.setRelationType(RelationType.Explicit.toString());
				connectiveDiscourseRelation.setSense(DEFAULT_SENSE);
				connectiveDiscourseRelation.setDiscourseConnective(discourseConnective);
				connectiveDiscourseRelation.setDiscourseConnectiveText(TokenListTools.getTokenListText(discourseConnective));
				
				List<DiscourseArgument> args = new ArrayList<DiscourseArgument>();
				for (ArgType argType: ArgType.values()){
					DiscourseArgument arg = new DiscourseArgument(aJCas);
					arg.setArgumentType(argType.toString());
					arg.setTokens(discourseConnective.getTokens());
					args.add(arg);
				}
				
				connectiveDiscourseRelation.setArguments(new FSArray(aJCas, args.size()));
				FSCollectionFactory.fillArrayFS(connectiveDiscourseRelation.getArguments(), args);

			}
			connectiveDiscourseRelation.addToIndexes();
		}
	}

	private boolean isEqual(DiscourseConnective discourseConnective,
			DiscourseConnective goldDiscourseConnective) {
		if (discourseConnective == null || goldDiscourseConnective == null)
			return false;
		return discourseConnective.getBegin() == goldDiscourseConnective.getBegin() &&
				discourseConnective.getEnd() == goldDiscourseConnective.getEnd() && 
				TokenListTools.getTokenListText(discourseConnective).equals(TokenListTools.getTokenListText(goldDiscourseConnective));
	}

	private void addSenses(JCas aJCas) {
		for (DiscourseRelation discourseRelation: JCasUtil.select(aJCas, DiscourseRelation.class)){
			String sense = null;
			DiscourseConnective discourseConnective = discourseRelation.getDiscourseConnective();
			for (DiscourseRelation goldDiscourseRelation: discourseRelations){
				DiscourseConnective goldDiscourseConnective = goldDiscourseRelation.getDiscourseConnective();
				if (isEqual(discourseConnective, goldDiscourseConnective)){
					sense = goldDiscourseRelation.getSense();
				}
			}
			
			if (sense == null){
				sense = DEFAULT_SENSE;
			}
			
			discourseRelation.setSense(sense);
		}
	}

	@Override
	protected void addToIndex(DiscourseRelation discourseRelation) {
		if (addImplicitRelations || discourseRelation.getRelationType().equals(RelationType.Explicit.toString())){
			discourseRelations.add(discourseRelation);
		}
	}
}
