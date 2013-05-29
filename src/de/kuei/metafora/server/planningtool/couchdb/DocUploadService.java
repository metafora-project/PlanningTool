package de.kuei.metafora.server.planningtool.couchdb;

import java.io.IOException;

import com.fourspaces.couchdb.Database;
import com.fourspaces.couchdb.Document;
import com.fourspaces.couchdb.Session;
import com.fourspaces.couchdb.ViewResults;

public class DocUploadService {

	public static String tomcatserver = "https://metafora.ku-eichstaett.de";
	public static String server = "metafora.ku-eichstaett.de";
	public static String user = "admin";
	public static String password = "didPfCDB";

	private static final int port = 5984;

	private static final String databaseName = "gwtfilebase";

	/**
	 * @param name
	 *            name of the map-document to be stored
	 * @param content
	 *            content of the document (i.e. the xml-String)
	 * @return Returns the id of the document in the database or null if the
	 *         document could not be stored
	 */
	public String uploadMap(String name, String content) {
		Session session = new Session(server, port, user, password);
		int version = 0;

		Database db = null;
		db = session.getDatabase(databaseName);

		// Mistake in couchdb4j-documentation: method view(String fullname)
		// needs fullname such as "docname/viewname" with docname as name of
		// Document containing the view in DB
		ViewResults results = db.view("pt-maps/nameToVersions");
		if (results != null) {
			/*
			 * Versions start with 0. This loop finds the version-number with
			 * which the document has to be saved.
			 */
			for (Document doc : results.getResults()) {
				int ver = doc.getInt("value");
				if (doc.getString("key").equals(name) && ver >= version) {
					version = ver + 1;
				}
			}
		} else {
			System.err.println("View nameToVersions failed.");
		}

		Document doc = new Document();
		doc.put("filename", name);
		doc.put("filetype", "application/xml");
		doc.put("pttype", "map");
		doc.put("data", content);
		doc.put("version", version);

		try {
			db.saveDocument(doc);
			return doc.getId();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * This method retrieves the IDs of all documents in the DB with filetype
	 * "map", that is also of the documents with non-maximal version-number
	 * 
	 * @return String-array of the IDs as returned from couchDB4j's getId()
	 */
	public String[] getIDs() {
		Session session = new Session(server, port, user, password);

		Database db = null;
		db = session.getDatabase(databaseName);

		ViewResults results = db.view("pt-maps/map_ids");

		String[] ids = new String[results.getResults().size()];
		int i = 0;
		for (Document doc : results.getResults()) {
			ids[i] = doc.getId();
			i++;
		}

		return ids;
	}

	/**
	 * This method gets the filename-value of the DB-entry with the specified ID
	 * 
	 * @param id
	 *            ID of the document to be retrieved
	 * @return filename of the document specified by the given ID or null, if no
	 *         entry with this ID exists
	 */
	public String getFilename(String id) {
		Session session = new Session(server, port, user, password);

		Database db = null;
		db = session.getDatabase(databaseName);

		Document doc = null;
		try {
			doc = db.getDocument(id);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (doc != null) {
			return doc.getString("filename");
		} else {
			return null;
		}
	}

	/**
	 * This method gets the data-value of the DB-entry with the specified ID
	 * 
	 * @param id
	 *            ID of the document to be retrieved
	 * @return data of the document specified by the given ID or null, if no
	 *         entry with this ID exists or if the document has no data
	 */
	public String getData(String id) {
		// TODO: couchdb ssl
		Session session = new Session(server, port, user, password);

		Database db = null;
		db = session.getDatabase(databaseName);

		Document doc = null;
		try {
			doc = db.getDocument(id);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (doc != null) {
			return doc.getString("data");
		} else {
			return null;
		}
	}

	/**
	 * @author Kerstin call this method to test if the map name of the graph you
	 *         want to store into the CouchDB already exists. Version will be -1
	 *         if the mapname does not exist
	 * 
	 * @param mapname
	 *            : name of may you want to test
	 * @return mapname + version if necessary; example:
	 *         "default_map; version: 3"
	 */
	public String testDocName(String mapname) {
		int version = -1;

		Session session = new Session(server, port, user, password);

		Database db = null;
		db = session.getDatabase(databaseName);

		ViewResults results = db.view("pt-maps/nameToVersions");

		for (Document doc : results.getResults()) {
			int ver = doc.getInt("value");
			if (doc.getString("key").equals(mapname) && ver > version) {
				version = ver;
			}
		}

		return mapname + "; version: " + version;
	}

	/**
	 * Gets the Version number of the document with the specified ID or -1 if
	 * this document is not a map or doesn't exist
	 * 
	 * @param id
	 *            ID of the document
	 * @return version number of the document or -1
	 */
	public int getVersion(String id) {
		int version = -1;
		Session session = new Session(server, port, user, password);

		Database db = null;
		db = session.getDatabase(databaseName);

		Document doc = null;
		try {
			doc = db.getDocument(id);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (doc != null && doc.containsKey("version")) {
			version = doc.getInt("version");
		}
		return version;

	}

	/**
	 * Gets the URL to the Document specified by the ID. Remark that no
	 * validation of the ID takes place and thus the link may be broken.
	 * 
	 * @param id
	 *            ID of the document
	 * @return (theoretical) URL of the document with the specified ID
	 */
	public String getLinkToDoc(String id) {
		String url = tomcatserver + "/workbench/development/fileupload?id="
				+ id;
		return url;
	}
}
