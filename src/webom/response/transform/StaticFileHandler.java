package webom.response.transform;

import java.io.File;

public class StaticFileHandler {
	// TODO: Hash mechanism for fast file lookups?
	// TODO: Transformer!!
	private File root;

	public StaticFileHandler(String path) {
		root = new File(path);
		if (!root.isDirectory()) {
			throw new IllegalArgumentException("The given path is not a directory: '" + path + "'");
		}
		root = new File(path);
	}

	public File getFile(String path) {
		// TODO: Path traversal check
		File requestedFile = new File(root, path);
		return requestedFile;
	}

}
