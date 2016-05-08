package org.cleartk.corpus.conll2015.loader;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;
import org.cleartk.corpus.conll2015.ConllDatasetPathFactory;
import org.junit.Test;

public class ConllDataLoaderFactoryTest {

	@Test
	public void whenTrialDatasetIsNotPreporcessThenLoaderPlusAnnotatorIsConstructed() throws IOException{
		File dataFld = new File("data/");
		ConllDatasetPath path = new ConllDatasetPathFactory().makeADataset2016(dataFld, DatasetMode.trial);
		System.out.println(ConllDataLoaderFactory.getPreprocessFilesLocation(path));
		FileUtils.deleteDirectory(ConllDataLoaderFactory.getPreprocessFilesLocation(path));
		
		ConllDataLoader instance = ConllDataLoaderFactory.getInstance(path);
		
		assertThat(instance).isInstanceOf(LoaderPlusAnnotator.class);
	}
	
	@Test
	public void whenTrialDatasetIsPreporcessedThenPreProcessedDatasetLoaderIsConstructed() throws IOException, ResourceInitializationException, UIMAException{
		File dataFld = new File("data/");
		ConllDatasetPath path = new ConllDatasetPathFactory().makeADataset2016(dataFld, DatasetMode.trial);
		FileUtils.deleteDirectory(ConllDataLoaderFactory.getPreprocessFilesLocation(path));
		
		ConllDataLoader instance = ConllDataLoaderFactory.getInstance(path);
		SimplePipeline.runPipeline(instance.getReader(), instance.getAnnotator(false));
		
		instance = ConllDataLoaderFactory.getInstance(path);
		assertThat(instance).isInstanceOf(PreprocessedDataLoader.class);
	}

}
