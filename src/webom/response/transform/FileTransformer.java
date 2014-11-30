package webom.response.transform;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;

import webom.request.Request;
import webom.response.Response;
import webom.util.HTTPStatus;

public class FileTransformer implements ResponseTransofmer<File> {

	@Override
	public void transform(Request request, Response response, File file) {
		// TODO: Check if file exists!!

		try {
			// Get a stream out of file
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

			// Set the corresponding headers
			response.getRaw().setContentLengthLong(file.length());

			if (response.getContentType() == null) {
				Tika tika = new Tika();
				response.getRaw().setContentType(tika.detect(file.getPath()));
			} else {
				response.getRaw().setContentType(response.getContentType());
			}

			response.setStatus(HTTPStatus.OK);

			// Copy the file stream to servlet stream
			IOUtils.copy(bis, response.getRaw().getOutputStream());
			IOUtils.closeQuietly(bis);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
