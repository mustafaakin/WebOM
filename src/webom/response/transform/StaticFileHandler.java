package webom.response.transform;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.JadeTemplate;

public class StaticFileHandler {
	public enum Template {
		JADE
	}

	private final JadeConfiguration config = new JadeConfiguration();
	private final static Map<String, Object> emptyMap = new HashMap<String, Object>();
	private Template template;

	private File root;

	public StaticFileHandler(String path, Template template) {
		config.setPrettyPrint(true);
		this.template = template;
		root = new File(path);
		if (!root.isDirectory()) {
			throw new IllegalArgumentException("The given path is not a directory: '" + path + "'");
		}
		root = new File(path);
	}

	public File getFile(String path) throws Exception {
		// TODO: Path traversal check
		if ( path.equals("/")){
			path = "index.html";
		}
		File requestedFile = new File(root, path);
		if (template != null && (path.endsWith(".htm") || path.endsWith(".html"))) {
			String name = path.substring(0, path.lastIndexOf("."));

			if (template == Template.JADE) {
				File jadeFile = new File(root, name + ".jade");
				if (jadeFile.exists()) {
					JadeTemplate jadeTemplate = config.getTemplate(jadeFile.getAbsolutePath());
					String html = config.renderTemplate(jadeTemplate, emptyMap);
					// TODO: Add my own caching later
					FileUtils.writeStringToFile(requestedFile, html);
				}
			}
		}
		return requestedFile;
	}

}
