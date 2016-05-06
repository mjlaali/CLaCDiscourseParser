package ca.concordia.clac.penn.analysis;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;
import org.cleartk.corpus.conll2015.ConllDatasetPathFactory;
import org.cleartk.corpus.conll2015.loader.ConllDataLoader;
import org.cleartk.corpus.conll2015.loader.ConllDataLoaderFactory;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseRelation;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.S;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.SBAR;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.SINV;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.VP;
import ir.laali.tools.ds.DSManagment;
import ir.laali.tools.ds.DSPrinter;

public class RelationInspector extends JCasAnnotator_ImplBase{
	public static final String PARAM_OUTPUT_FILE = "outputFile"; 

	@ConfigurationParameter(name=PARAM_OUTPUT_FILE)
	private File outputFile;

	public static AnalysisEngineDescription	getDescription(File outputFile) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(RelationInspector.class, PARAM_OUTPUT_FILE, outputFile);
	}

	enum Info{
		Arg1IsNull, Arg2IsNull, ArgsAreNotNull, OneArgIsNull, ArgsAtTheSameSent, ArgSentsAreReversed, Arg2SentFollowsArg1, DiscontinueArgSents, Arg1Arg2HaveSameConstituent

	}

	Map<Info, Integer> statsExplicit = new TreeMap<>();
	Map<Info, Integer> statsImplicit = new TreeMap<>();
	Map<String, Integer> statsConstituent = new TreeMap<>();
	Map<String, Integer> statsArgConstituentExplicit = new TreeMap<>();
	Map<String, Integer> statsArgConstituentImplciit = new TreeMap<>();
	Set<Constituent> seen = new HashSet<>();
	int max = 0;

	public static <T> Map<T, Integer> addToMap(Map<T, Integer> map, T key, int toAdd){
		Integer cnt = map.get(key);
		if (cnt == null)
			cnt = 0;
		map.put(key, cnt + toAdd);
		return map;
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		addToMap(statsConstituent, "S", JCasUtil.select(aJCas, S.class).size());
		addToMap(statsConstituent, "SBAR", JCasUtil.select(aJCas, SBAR.class).size());
		addToMap(statsConstituent, "VP", JCasUtil.select(aJCas, VP.class).size());
		addToMap(statsConstituent, "SINV", JCasUtil.select(aJCas, SINV.class).size());

		Map<Sentence, Collection<Constituent>> indexCovered = JCasUtil.indexCovered(aJCas, Sentence.class, Constituent.class);
		for (Entry<Sentence, Collection<Constituent>> anEntry: indexCovered.entrySet()){
			int cnt = 0;
			for (Constituent cons: anEntry.getValue()){
				switch (cons.getConstituentType()) {
				case "S":
				case "SBAR":
				case "VP":
				case "SINV":
					cnt++;
					break;
				default:
					break;
				}
			}
			if (cnt > max)
				max = cnt;
		}

		Map<DiscourseArgument, Collection<Constituent>> coveringConstituent = JCasUtil.indexCovering(aJCas, DiscourseArgument.class, Constituent.class);
		Map<Constituent, Collection<Sentence>> coveringSentences = JCasUtil.indexCovering(aJCas, Constituent.class, Sentence.class);

		Collection<DiscourseRelation> relations = JCasUtil.select(aJCas, DiscourseRelation.class);

		for (DiscourseRelation relation: relations){
			DiscourseArgument arg1 = relation.getArguments(0);
			DiscourseArgument arg2 = relation.getArguments(1);

			Constituent arg1Constituent = ArgConstituentTypes.pickTheSmallest(coveringConstituent.get(arg1));
			Constituent arg2Constituent = ArgConstituentTypes.pickTheSmallest(coveringConstituent.get(arg2));
			if (relation.getRelationType().equals("Explicit"))
				saveStatusOf(arg1Constituent, arg2Constituent, coveringSentences, aJCas, statsExplicit, statsArgConstituentExplicit);
			else 
				saveStatusOf(arg1Constituent, arg2Constituent, coveringSentences, aJCas, statsImplicit, statsArgConstituentImplciit);
		}
		seen.clear();
	}

	private void saveStatusOf(Constituent arg1Constituent, Constituent arg2Constituent, 
			Map<Constituent, Collection<Sentence>> coveringSentences, JCas aJCas, 
			Map<Info, Integer> stats, Map<String, Integer> statsArgConstituents) {
		if (arg1Constituent == null)
			DSManagment.incValue(stats, Info.Arg1IsNull);
		if (arg2Constituent == null)
			DSManagment.incValue(stats, Info.Arg2IsNull);

		if (arg1Constituent != null && arg2Constituent != null){
			if (arg1Constituent.equals(arg2Constituent)){
				DSManagment.incValue(stats, Info.Arg1Arg2HaveSameConstituent);
			} else {
				saveTheType(arg1Constituent, statsArgConstituents);
				saveTheType(arg2Constituent, statsArgConstituents);
			}
			DSManagment.incValue(stats, Info.ArgsAreNotNull);
			Sentence sent1 = coveringSentences.get(arg1Constituent).iterator().next();
			Sentence sent2 = coveringSentences.get(arg2Constituent).iterator().next();
			if (sent1.getBegin() > sent2.getEnd())
				DSManagment.incValue(stats, Info.ArgSentsAreReversed);
			if (sent1.equals(sent2))
				DSManagment.incValue(stats, Info.ArgsAtTheSameSent);
			else if (JCasUtil.selectCovered(aJCas, Sentence.class, sent1.getEnd(), sent2.getBegin()).isEmpty())
				DSManagment.incValue(stats, Info.Arg2SentFollowsArg1);
			else
				DSManagment.incValue(stats, Info.DiscontinueArgSents);

		} else
			DSManagment.incValue(stats, Info.OneArgIsNull);
	}

	private void saveTheType(Constituent argConstituent, Map<String, Integer> statsArgConstituents) {
		if (seen.contains(argConstituent))
			return;
		seen.add(argConstituent);
		switch (argConstituent.getConstituentType()) {
		case "S":
		case "SBAR":
		case "VP":
		case "SINV":
			DSManagment.incValue(statsArgConstituents, argConstituent.getConstituentType());
			break;
		default:
			DSManagment.incValue(statsArgConstituents, "Invalid");
			break;
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();

		DSPrinter.printMap("Explicit Type", statsExplicit.entrySet(), System.out);
		System.out.println();
		DSPrinter.printMap("Implicit Type", statsImplicit.entrySet(), System.out);

		System.out.println();
		System.out.println();
		System.out.println("Max = " + max);
		DSPrinter.printMap("Constituents", statsConstituent.entrySet(), System.out);
		DSPrinter.printMap("Explicit Constituents", statsArgConstituentExplicit.entrySet(), System.out);
		DSPrinter.printMap("Implicit Constituents", statsArgConstituentImplciit.entrySet(), System.out);

	}

	public static void main(String[] args) throws ResourceInitializationException, UIMAException, IOException {
		File dataFld = new File("../discourse.conll.dataset/data");
		DatasetMode mode = DatasetMode.train;

		ConllDatasetPath path = new ConllDatasetPathFactory().makeADataset2016(dataFld, mode);
		ConllDataLoader loader = ConllDataLoaderFactory.getInstance(path);
		SimplePipeline.runPipeline(loader.getReader(), loader.getAnnotator(), getDescription(new File("outputs/analysis/argInspector.txt")));
	}
}
