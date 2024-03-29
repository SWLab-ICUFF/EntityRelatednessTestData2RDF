package uff.ic.swlab.dataset_ertd.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.naming.InvalidNameException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.Asserts;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.logging.log4j.LogManager;

public class SWLabHost {

    public String hostname;
    public int httpPort;
    public int ftpPort;

    private SWLabHost() {

    }

    public SWLabHost(String hostname, int httpPort, int ftpPort) {
        this.hostname = hostname;
        this.httpPort = httpPort;
        this.ftpPort = ftpPort;
    }

    public String baseHttpUrl() {
        return "http://" + hostname + (httpPort == 80 ? "" : ":" + httpPort) + "/";
    }

    public String linkedDataNS() {
        return baseHttpUrl() + "resource/";
    }

    public String getBackupURL(String datasetname) {
        return String.format(baseHttpUrl() + "fuseki/$/backup/%1$s", datasetname);
    }

    public String getQuadsURL(String datasetname) {
        return String.format(baseHttpUrl() + "fuseki/%1s/", datasetname);
    }

    public String getSparqlURL(String datasetname) {
        return String.format(baseHttpUrl() + "fuseki/%1s/sparql", datasetname);
    }

    public String getUpdateURL(String datasetname) {
        return String.format(baseHttpUrl() + "fuseki/%1$s/update", datasetname);
    }

    public String getDataURL(String datasetname) {
        return String.format(baseHttpUrl() + "fuseki/%1s/data", datasetname);
    }

    public String updateURL(String datasetname) {
        return String.format(baseHttpUrl() + "fuseki/%1s/update", datasetname);
    }

    public synchronized List<String> listGraphNames(String datasetname, long timeout) {
        List<String> graphNames = new ArrayList<>();

        String queryString = "select distinct ?g where {graph ?g {[] ?p [].}}";
        //try ( QueryExecution exec = new QueryEngineHTTP(getSparqlURL(datasetname), queryString, HttpClients.createDefault())) {
        //    ((QueryEngineHTTP) exec).setTimeout(timeout);
        try ( QueryExecution exec = QueryExecutionHTTP.service(getSparqlURL(datasetname))
                .query(queryString)
                .timeout(timeout)
                .build()) {
            ResultSet rs = exec.execSelect();
            while (rs.hasNext())
                graphNames.add(rs.next().getResource("g").getURI());
        } catch (Exception e) {
        }

        return graphNames;
    }

    public synchronized Model execConstruct(String queryString, String datasetname) {
        Model result = ModelFactory.createDefaultModel();
        //try (final QueryExecution exec = new QueryEngineHTTP(getSparqlURL(datasetname), queryString, HttpClients.createDefault())) {
        //    ((QueryEngineHTTP) exec).setModelContentType(WebContent.contentTypeRDFXML);
        try ( QueryExecution exec = QueryExecutionHTTP.service(getSparqlURL(datasetname))
                .query(queryString)
                .acceptHeader(WebContent.contentTypeRDFXML)
                .build()) {
            exec.execConstruct(result);
        }
        return result;
    }

    public synchronized void execUpdate(String queryString, String datasetname) {
        UpdateRequest request = UpdateFactory.create(queryString);
        UpdateProcessor execution = UpdateExecutionFactory.createRemote(request, getUpdateURL(datasetname));
        execution.execute();
    }

    public void backupDataset(String datasetname) throws Exception, IOException {
        System.out.println(String.format("Requesting backup of the Fuseki dataset %1$s...", datasetname));
        String backupUrl = getBackupURL(datasetname);
        HttpClient httpclient = HttpClients.createDefault();
        try {
            HttpResponse response = httpclient.execute(new HttpPost(backupUrl));
            int statuscode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null && statuscode == 200)
                try (final InputStream instream = entity.getContent()) {
                System.out.println(IOUtils.toString(instream, "utf-8"));
                System.out.println("Done.");
            } else
                System.out.println("Backup request failed.");
        } catch (Throwable e) {
            System.out.println("Backup request failed.");
        }
    }

    public synchronized void putModel(Model sourceModel, String datasetname) throws InvalidNameException {
        Asserts.notNull(sourceModel, "graphUri");
        Asserts.notEmpty(datasetname, "datasetname");
        Asserts.notBlank(datasetname, "datasetname");

        //DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(getDataURL(datasetname), HttpClients.createDefault());
        //accessor.putModel(sourceModel);
        //Logger.getLogger("info").log(Level.INFO, String.format("Dataset saved (<%1$s>).", "default graph"));
        //
        // Doc: https://jena.apache.org/documentation/rdfconnection/
        try ( RDFConnection conn = RDFConnection.connect(getDataURL(datasetname))) {
            conn.put(sourceModel);
            LogManager.getLogger("info").log(org.apache.logging.log4j.Level.INFO, String.format("Dataset saved (<%1$s>).", "default graph"));
        }
    }

    public synchronized void putModel(Model sourceModel, String datasetname, String graphUri) throws
            InvalidNameException {
        Asserts.notNull(sourceModel, "graphUri");
        Asserts.notEmpty(datasetname, "datasetname");
        Asserts.notBlank(datasetname, "datasetname");
        Asserts.notEmpty(graphUri, "graphUri");
        Asserts.notBlank(graphUri, "graphUri");

        //if (graphUri != null && !graphUri.equals("")) {
        //    DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(getDataURL(datasetname), HttpClients.createDefault());
        //    accessor.putModel(graphUri, sourceModel);
        //    Logger.getLogger("info").log(Level.INFO, String.format("Dataset saved (<%1s>).", graphUri));
        //    return;
        //}
        //throw new InvalidNameException(String.format("Invalid graph URI: %1s.", graphUri));
        //
        // Doc: https://jena.apache.org/documentation/rdfconnection/
        try ( RDFConnection conn = RDFConnection.connect(getDataURL(datasetname))) {
            conn.put(graphUri, sourceModel);
            LogManager.getLogger("info").log(org.apache.logging.log4j.Level.INFO, String.format("Dataset saved (<%1s>).", graphUri));
        }
    }

    public synchronized Model getModel(String datasetname) {
        Asserts.notEmpty(datasetname, "datasetname");
        Asserts.notBlank(datasetname, "datasetname");

        //DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(getDataURL(datasetname), HttpClients.createDefault());
        //Model model = accessor.getModel();
        //if (model != null)
        //    return model;
        //else
        //    return ModelFactory.createDefaultModel();
        try ( RDFConnection conn = RDFConnection.connect(getDataURL(datasetname))) {
            Model model = conn.fetch();
            return model == null ? ModelFactory.createDefaultModel() : model;
        }
    }

    public synchronized Model getModel(String datasetname, String graphUri) throws InvalidNameException {
        Asserts.notEmpty(datasetname, "datasetname");
        Asserts.notBlank(datasetname, "datasetname");
        Asserts.notEmpty(graphUri, "graphUri");
        Asserts.notBlank(graphUri, "graphUri");

        //if (graphUri != null && !graphUri.equals("")) {
        //    DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(getDataURL(datasetname), HttpClients.createDefault());
        //    Model model = accessor.getModel(graphUri);
        //    if (model != null)
        //        return model;
        //    else
        //        return ModelFactory.createDefaultModel();
        //}
        //throw new InvalidNameException(String.format("Invalid graph URI: %1s.", graphUri));
        //
        // Doc: https://jena.apache.org/documentation/rdfconnection/
        try ( RDFConnection conn = RDFConnection.connect(getDataURL(datasetname))) {
            Model model = conn.fetch(graphUri);
            return model == null ? ModelFactory.createDefaultModel() : model;
        }
    }

    public synchronized void uploadBinaryFile(String localFilename, String remoteName, String user, String pass) throws
            IOException, Exception {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(hostname, ftpPort);

        try ( InputStream in = new FileInputStream(localFilename);) {
            String[] dirs = remoteName.split("/");
            if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode()))
                if (ftpClient.login(user, pass)) {
                    //ftpClient.execPBSZ(0);
                    //ftpClient.execPROT("P");
                    ftpClient.enterLocalPassiveMode();
                    //ftpClient.enterRemotePassiveMode();
                    ftpClient.mkd(String.join("/", Arrays.copyOfRange(dirs, 0, dirs.length - 1)));
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                    ftpClient.storeFile(remoteName, in);
                    ftpClient.logout();
                }
            ftpClient.disconnect();
        } catch (IOException e) {
            try {
                ftpClient.disconnect();
            } catch (IOException e2) {
            }
            throw e;
        }
    }

    public synchronized void mkDirsViaFTP(String remoteFilename, String user, String pass) throws Exception {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(hostname, ftpPort);

        try {
            if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode()))
                if (ftpClient.login(user, pass)) {
                    String[] dirs = remoteFilename.split("/");
                    //ftpClient.execPBSZ(0);
                    //ftpClient.execPROT("P");
                    ftpClient.enterLocalPassiveMode();
                    //ftpClient.enterRemotePassiveMode();
                    ftpClient.mkd(String.join("/", Arrays.copyOfRange(dirs, 0, dirs.length - 1)));
                    ftpClient.logout();
                }
            ftpClient.disconnect();
        } catch (IOException e) {
            try {
                ftpClient.disconnect();
            } catch (IOException e2) {
            }
            throw e;
        }
    }
}
