package org.cleartk.corpus.conll2015.statistics;

public class DatasetStatistics {
//	public static final String OUTPUT_DIR = "outputs/statistics";
//	public static final String XMI_DIR = OUTPUT_DIR + "/xmi_%s";
//
//	protected DatasetPath dataset;
//	private File xmiDir;
//
//	public DatasetStatistics(DatasetPath dataset) {
//		this(dataset, dataset.getXmiOutDir());
//	}
//
//	public DatasetStatistics(DatasetPath dataset, String xmiDir) {
//		this.dataset = dataset;
//		this.xmiDir = new File(xmiDir);
//	}
//
//	public void readDataset() throws UIMAException, IOException{
//		readDataset(false);
//	}
//	public void readDataset(boolean parseManually) throws UIMAException, IOException{
//		Collection<File> files = FileUtils.listFiles(new File(dataset.getRawTextsFld()), null, false);
//
//		CollectionReaderDescription reader = UriCollectionReader.getDescriptionFromFiles(files);
//
//		AggregateBuilder builder = new AggregateBuilder();
//		// An annotator that creates an empty treebank view in the CAS
//		builder.add(AnalysisEngineFactory.createEngineDescription(ViewCreatorAnnotator.class, ViewCreatorAnnotator.PARAM_VIEW_NAME,
//				ConllDiscourseGoldAnnotator.GOLD_DISCOURSE_VIEW));
//
//		// A collection reader that creates one CAS per file, containing the file's URI
//		AnalysisEngineDescription textReader = UriToDocumentTextAnnotator.getDescription();
//		builder.add(textReader);
//		builder.add(textReader, CAS.NAME_DEFAULT_SOFA, ConllDiscourseGoldAnnotator.GOLD_DISCOURSE_VIEW);
//
//		if (parseManually){
//			builder.add(SentenceAnnotator.getDescription());
//			builder.add(
//					AnalysisEngineFactory.createEngineDescription(DefaultBerkeleyTokenizer.class,
//							ParserWrapper_ImplBase.PARAM_OUTPUT_TYPES_HELPER_CLASS_NAME,
//							DefaultOutputTypesHelper.class.getName(),
//							ParserWrapper_ImplBase.PARAM_INPUT_TYPES_HELPER_CLASS_NAME,
//							ConllInputTypeHelper.class.getName())
//					);
//			String modelPath = "outputs/parser/model_train/eng_sm6.gr";
//			builder.add(
//					AnalysisEngineFactory.createEngineDescription(
//							ParserAnnotator.class,
//							ParserAnnotator.PARAM_PARSER_MODEL_PATH,
//							modelPath,
//							ParserWrapper_ImplBase.PARAM_OUTPUT_TYPES_HELPER_CLASS_NAME,
//							DefaultOutputTypesHelper.class.getName(),
//							ParserWrapper_ImplBase.PARAM_INPUT_TYPES_HELPER_CLASS_NAME,
//							ConllInputTypeHelper.class.getName()));
//		} else {
//			AnalysisEngineDescription conllSyntaxJsonReader = ConllSyntaxGoldAnnotator.getDescription(dataset.getSyntaxAnnotationFlie());
//			builder.add(conllSyntaxJsonReader);
//			builder.add(conllSyntaxJsonReader, CAS.NAME_DEFAULT_SOFA, ConllDiscourseGoldAnnotator.GOLD_DISCOURSE_VIEW);
//
//			//TODO: These two lines should be moved out of if statement
//			AnalysisEngineDescription conllDiscourseJsonReader = ConllDiscourseGoldAnnotator.getDescription(dataset.getDiscourseGoldAnnotationFile());
//			builder.add(conllDiscourseJsonReader, CAS.NAME_DEFAULT_SOFA, ConllDiscourseGoldAnnotator.GOLD_DISCOURSE_VIEW);
//		}
//
//		if (xmiDir.exists())
//			FileUtils.deleteDirectory(xmiDir);
//		xmiDir.mkdirs();
//
//		builder.add(LineReaderXmiWriter.getDescription(xmiDir));
//
//		SimplePipeline.runPipeline(reader, builder.createAggregateDescription());
//	}
//
//	public void getStatistics(AnalysisEngineDescription... descriptions) throws ResourceInitializationException, UIMAException, IOException{
//		CollectionReaderDescription desc = CollectionReaderFactory.createReaderDescription(
//				XReader.class,
//				FilesCollectionReader.PARAM_ROOT_FILE,
//				xmiDir.getAbsolutePath());
//
//		AggregateBuilder builder = new AggregateBuilder();
//		for (AnalysisEngineDescription description: descriptions){
//			builder.add(description, CAS.NAME_DEFAULT_SOFA, ConllDiscourseGoldAnnotator.GOLD_DISCOURSE_VIEW);
//		}
//
//		SimplePipeline.runPipeline(CollectionReaderFactory.createReader(desc), builder.createAggregateDescription());
//	}
//
//	public void run(AnalysisEngineDescription... description) throws ResourceInitializationException, UIMAException, IOException{
//		CollectionReaderDescription desc = CollectionReaderFactory.createReaderDescription(
//				XReader.class,
//				FilesCollectionReader.PARAM_ROOT_FILE,
//				xmiDir.getAbsolutePath());
//
//		SimplePipeline.runPipeline(CollectionReaderFactory.createReader(desc), description);
//	}
//
//
//	public static void main(String[] args) throws UIMAException, IOException {
//		System.out.println("DatasetStatistics.main()");
//		//		DatasetPath dataset = new ConllDataset("train");
//		DatasetPath dataset = new ConllDataset();
//		DatasetStatistics datasetStatistics = new DatasetStatistics(dataset, String.format(XMI_DIR, dataset.getMode()));
//		datasetStatistics.readDataset();
//		//		datasetStatistics.getStatistics(DCRelationCounter.getDescription("data/analysisResults/dcRelationCnt.txt"));
//	}

}
