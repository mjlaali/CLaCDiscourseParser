package org.cleartk.corpus.conll2015;

import java.io.File;

import org.cleartk.corpus.conll2015.statistics.DatasetStatistics;
import org.cleartk.discourse_parsing.errorAnalysis.DiscourseRelationsExporter;

public class ConllDataset implements DatasetPath{
	public static final String OUTPUT_MODEL_DIRECTORY = "outputs/parser/model_%s";
	public static final String JSON_OUT_FILE = "outputs/parser/system_%s.json";

	private String rawTextsFld;
	private String discourseGoldAnnotationFile;
	private String syntaxAnnotationFlie;
	private String baseFld;
	private String mode;
	
	public ConllDataset() {
		this(new File(ConllJSON.TRIAL_DATASET_FLD).getParentFile().getAbsolutePath(), "trial");
	}
	
	public ConllDataset(String tag) {
		this(ConllJSON.BASE_DATASET_FLD, tag);
		
	}
	
	public ConllDataset(String baseFld, String mode) {
		for (File f: new File(baseFld).listFiles()){
			if (f.getName().contains(mode))
				this.setBaseFld(f.getAbsolutePath());
		}
		if (getBaseFld() == null)
			throw new RuntimeException("The base fld is not valid folder for the CoNLL dataset: " + baseFld);
		this.setMode(mode);
		setRawTextsFld(findFile("raw"));
		setDiscourseGoldAnnotationFile(findFile("data"));
		setSyntaxAnnotationFlie(findFile("parses"));
		
		String[] values = new String[]{getRawTextsFld(), getDiscourseGoldAnnotationFile(), getSyntaxAnnotationFlie(), baseFld, mode};
		for (String val: values){
			if (val == null)
				throw new RuntimeException("There is null value in the automatically found directories");
		}
	}
	
	public String findFile(String... tags){
		for (File file: new File(getBaseFld()).listFiles()){
			String name = file.getName();
			if (name.contains(".bak"))
				continue;
			boolean found = true;
			for (String tag: tags){
				if (!name.contains(tag)){
					found = false;
					break;
				}
			}
			if (found)
				return file.getAbsolutePath();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.cleartk.corpus.conll2015.DatasetPath#getSystemTextOutputFile()
	 */
	@Override
	public String getExplicitRelSystemOutFile(){
		String exportFilePath = String.format("outputs/parser/" + DiscourseRelationsExporter.FILE_NAME_CONVENTION,
				getMode() , "system", "explicit");
		return exportFilePath;
	}

	@Override
	public String getImplicitRelSystemOutFile(){
		String implicitFilePath = String.format("outputs/parser/" + DiscourseRelationsExporter.FILE_NAME_CONVENTION,
				getMode() , "system", "implicit");
		return implicitFilePath;
	}

	@Override
	public String getXmiOutDir(){
		return String.format(DatasetStatistics.XMI_DIR, getMode());
	}
	
	/* (non-Javadoc)
	 * @see org.cleartk.corpus.conll2015.DatasetPath#getModelDir()
	 */
	@Override
	public String getModelDir() {
		String getModelDir = String.format(OUTPUT_MODEL_DIRECTORY, getMode());
		return getModelDir;
	}

	/* (non-Javadoc)
	 * @see org.cleartk.corpus.conll2015.DatasetPath#getReportFile()
	 */
	@Override
	public String getReportFile() {
		return String.format("outputs/parser/system_%s_report.txt", getMode());
	}
	
	/* (non-Javadoc)
	 * @see org.cleartk.corpus.conll2015.DatasetPath#getJsonFile()
	 */
	@Override
	public String getJsonFile(){
		return String.format(JSON_OUT_FILE, getMode());
	}

	/* (non-Javadoc)
	 * @see org.cleartk.corpus.conll2015.DatasetPath#getRawTextsFld()
	 */
	@Override
	public String getRawTextsFld() {
		return rawTextsFld;
	}

	public void setRawTextsFld(String rawTextsFld) {
		this.rawTextsFld = rawTextsFld;
	}

	/* (non-Javadoc)
	 * @see org.cleartk.corpus.conll2015.DatasetPath#getDiscourseGoldAnnotationFile()
	 */
	@Override
	public String getDiscourseGoldAnnotationFile() {
		return discourseGoldAnnotationFile;
	}

	public void setDiscourseGoldAnnotationFile(
			String discourseGoldAnnotationFile) {
		this.discourseGoldAnnotationFile = discourseGoldAnnotationFile;
	}

	/* (non-Javadoc)
	 * @see org.cleartk.corpus.conll2015.DatasetPath#getSyntaxAnnotationFlie()
	 */
	@Override
	public String getSyntaxAnnotationFlie() {
		return syntaxAnnotationFlie;
	}

	public void setSyntaxAnnotationFlie(String syntaxAnnotationFlie) {
		this.syntaxAnnotationFlie = syntaxAnnotationFlie;
	}

	/* (non-Javadoc)
	 * @see org.cleartk.corpus.conll2015.DatasetPath#getBaseFld()
	 */
	@Override
	public String getBaseFld() {
		return baseFld;
	}

	public void setBaseFld(String baseFld) {
		this.baseFld = baseFld;
	}

	/* (non-Javadoc)
	 * @see org.cleartk.corpus.conll2015.DatasetPath#getMode()
	 */
	@Override
	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

}