package webom.core;

import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Serie;

public class Reporter {
	private static Reporter INSTANCE = new Reporter();

	private InfluxDB influxDB = null;
	private String dbName = null;

	private Reporter() {

	}

	public static Reporter getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Reporter();
		}
		return INSTANCE;
	}

	public static void setReporter(String url, String user, String pass, String dbName) {
		Reporter r = getInstance();
		r.influxDB = InfluxDBFactory.connect(url, user, pass);
		r.dbName = dbName;
	}

	public static void path(String resolvedPath, String handlerPath, String handlerCls, double time, int status) {
		Serie serie = new Serie.Builder("paths").columns("resolvedPath", "handlerPath", "handlerCls", "elapsed", "status").values(resolvedPath, handlerPath, handlerCls, time, status).build();
		INSTANCE.write(serie);
	}

	private void write(Serie... series) {
		if (this.influxDB != null) {
			// Cache them, write periodically
			this.influxDB.write(this.dbName, TimeUnit.MILLISECONDS, series);
		}
		// If not set, just ignore sending
	}
}
