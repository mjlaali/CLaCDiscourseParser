package org.cleartk.corpus.conll2015;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

public class DataSetTest {

	@Test
	public void whenInitializingWithDefaulatConstructorThenDataSetIsInitializedWithTrialData(){
		DatasetPath dataSet = new ConllDataset();
		
		assertThat(dataSet.getDiscourseGoldAnnotationFile()).isEqualTo(new File(ConllJSON.TRIAL_DISCOURSE_FILE).getAbsolutePath());
		assertThat(dataSet.getRawTextsFld()).isEqualTo(new File(ConllJSON.TRIAL_RAW_TEXT_LD).getAbsolutePath());
		assertThat(dataSet.getSyntaxAnnotationFlie()).isEqualTo(new File(ConllJSON.TRIAL_SYNTAX_FILE).getAbsolutePath());
	}
}
